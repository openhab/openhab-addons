/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.airq.internal;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.airq.internal.AirqBindingConstants.THING_TYPE_AIRQ;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.airq.internal.action.AirqActions;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests optional air-Q measurements and sanitized diagnostics.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class AirqHandlerTest {
    private final ThingUID thingUID = new ThingUID(THING_TYPE_AIRQ, "test");
    private final Thing thing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID).build();
    private final ThingHandlerCallback callback = requireNonNull(mock(ThingHandlerCallback.class));
    private final AirqHandler handler = new AirqHandler(thing, requireNonNull(mock(HttpClient.class)));

    AirqHandlerTest() {
        handler.setCallback(callback);
    }

    static Stream<Arguments> particleCounts() {
        return Stream.of(Arguments.of("cnt0_3", "fine-dust-cnt00-3"), Arguments.of("cnt0_5", "fine-dust-cnt00-5"),
                Arguments.of("cnt1", "fine-dust-cnt01"), Arguments.of("cnt2_5", "fine-dust-cnt02-5"),
                Arguments.of("cnt5", "fine-dust-cnt05"), Arguments.of("cnt10", "fine-dust-cnt10"));
    }

    @ParameterizedTest
    @MethodSource("particleCounts")
    void particleCountsAreUnitlessNumbers(String key, String channel) throws Exception {
        // datenlesen.md: particles per 100 ml
        poll("{\"" + key + "\": [12.5, 0.25]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new DecimalType(12.5f));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#" + channel + "-maxerr"),
                new DecimalType(0.25f));
    }

    static Stream<Arguments> defaultUnits() {
        // datenlesen.md and measurement.md: the units the device reports unless configured otherwise
        Unit<?> microgram = Units.MICROGRAM_PER_CUBICMETRE;
        return Stream.of(Arguments.of("co", "co", AirqUnits.MILLIGRAM_PER_CUBICMETRE),
                Arguments.of("co2", "co2", Units.PARTS_PER_MILLION), Arguments.of("dewpt", "dewpt", SIUnits.CELSIUS),
                Arguments.of("h2s", "h2s", microgram), Arguments.of("humidity", "humidity-relative", Units.PERCENT),
                Arguments.of("humidity_abs", "humidity-absolute", AirqUnits.GRAM_PER_CUBICMETRE),
                Arguments.of("no2", "no2", microgram), Arguments.of("o3", "o3", microgram),
                Arguments.of("oxygen", "o2", Units.PERCENT), Arguments.of("pm1", "fine-dust-conc01", microgram),
                Arguments.of("pm2_5", "fine-dust-conc02-5", microgram),
                Arguments.of("pm10", "fine-dust-conc10", microgram),
                Arguments.of("pressure", "pressure", AirqUnits.HECTOPASCAL),
                Arguments.of("radon", "radon", Units.BECQUEREL_PER_CUBIC_METRE), Arguments.of("so2", "so2", microgram),
                Arguments.of("sound", "sound", Units.DECIBEL),
                Arguments.of("temperature", "temperature", SIUnits.CELSIUS),
                Arguments.of("tvoc", "tvoc", Units.PARTS_PER_BILLION),
                Arguments.of("virus", "virus-free", Units.PERCENT), Arguments.of("mold", "mold-free", Units.PERCENT));
    }

    @ParameterizedTest
    @MethodSource("defaultUnits")
    void measurementsAndErrorBarsHaveTheDefaultUnits(String key, String channel, Unit<?> unit) throws Exception {
        poll("{\"" + key + "\": [12.5, 0.25]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new QuantityType<>(12.5, unit));
        // datenlesen.md: the error bar has the unit of the value, a temperature difference is given in kelvin
        Unit<?> errorUnit = unit.isCompatible(Units.KELVIN) ? Units.KELVIN : unit;
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#" + channel + "-maxerr"),
                new QuantityType<>(0.25, errorUnit));
    }

    // 2. LifeData.md: a measurement of a device with the default units
    private static final String LIVE_DATA = """
            {"co2":[1216.0,86.5],"no2":[15.66,3.4],"co":[0.136266,0.03992],"ch2o_M10":[4.27,2.7],
             "humidity":[59.375,4.85],"sound_max":[52.6,2.8],"humidity_abs":[10.291,0.7],"timestamp":1793046696000,
             "tvoc":[1330.0,210.7],"pressure_rel":[1022.35,2.26],"health":894,"dHdt":-0.01,"sound":[48.8,3.3],
             "dCO2dt":16.94,"pm2_5":[0,10],"pressure":[975.93,1.0],"pm1":[0,10],"uptime":1068539,
             "temperature":[19.978,0.54],"measuretime":2004,"dewpt":[12.007,1.05],"performance":321.32372,
             "pm10":[3,10]}
            """;

    @Test
    void liveDataIsPublishedInTheDefaultUnits() throws Exception {
        poll(LIVE_DATA);

        assertThat(value("measurements#co", AirqUnits.MILLIGRAM_PER_CUBICMETRE), closeTo(0.136266, 1e-9));
        assertThat(value("maxerr#co-maxerr", AirqUnits.MILLIGRAM_PER_CUBICMETRE), closeTo(0.03992, 1e-9));
        assertThat(value("measurements#no2", Units.MICROGRAM_PER_CUBICMETRE), closeTo(15.66, 1e-9));
        assertThat(value("measurements#temperature", SIUnits.CELSIUS), closeTo(19.978, 1e-9));
        assertThat(value("maxerr#temperature-maxerr", Units.KELVIN), closeTo(0.54, 1e-9));
        assertThat(value("measurements#dewpt", SIUnits.CELSIUS), closeTo(12.007, 1e-9));
        assertThat(value("measurements#pressure", AirqUnits.HECTOPASCAL), closeTo(975.93, 1e-9));
        assertThat(value("measurements#humidity-relative", Units.PERCENT), closeTo(59.375, 1e-9));
        assertThat(value("measurements#humidity-absolute", AirqUnits.GRAM_PER_CUBICMETRE), closeTo(10.291, 1e-9));
        assertThat(value("measurements#fine-dust-conc10", Units.MICROGRAM_PER_CUBICMETRE), closeTo(3, 1e-9));
        assertThat(value("maxerr#fine-dust-conc10-maxerr", Units.MICROGRAM_PER_CUBICMETRE), closeTo(10, 1e-9));
        assertThat(value("measurements#co2", Units.PARTS_PER_MILLION), closeTo(1216, 1e-9));
        assertThat(value("measurements#tvoc", Units.PARTS_PER_BILLION), closeTo(1330, 1e-9));
        assertThat(value("measurements#sound", Units.DECIBEL), closeTo(48.8, 1e-9));
    }

    @Test
    void configuredUnitsAreConvertedToTheChannelUnits() throws Exception {
        // measurement.md: temperature, dew point, pressure and radon in other units
        poll(thing, """
                {"units": {"temperature": "degF", "dewpt": "K", "pressure": "kPa", "radon": "pCi/L"}}
                """, """
                {"temperature": [68, 1.8], "dewpt": [285.15, 0.9], "pressure": [101.325, 0.1], "radon": [2.7, 0.27]}
                """);

        assertThat(value("measurements#temperature", SIUnits.CELSIUS), closeTo(20, 1e-9));
        // error bars are differences: 1.8 °F are 1 K, not -16.8 °C
        assertThat(value("maxerr#temperature-maxerr", Units.KELVIN), closeTo(1, 1e-9));
        assertThat(value("measurements#dewpt", SIUnits.CELSIUS), closeTo(12, 1e-9));
        assertThat(value("maxerr#dewpt-maxerr", Units.KELVIN), closeTo(0.9, 1e-9));
        assertThat(value("measurements#pressure", AirqUnits.HECTOPASCAL), closeTo(1013.25, 1e-9));
        assertThat(value("maxerr#pressure-maxerr", AirqUnits.HECTOPASCAL), closeTo(1, 1e-9));
        // 1 pCi/L = 37 Bq/m³
        assertThat(value("measurements#radon", Units.BECQUEREL_PER_CUBIC_METRE), closeTo(99.9, 1e-9));
        assertThat(value("maxerr#radon-maxerr", Units.BECQUEREL_PER_CUBIC_METRE), closeTo(9.99, 1e-9));
    }

    // measurement.md: c[mg/m³] = c[ppm] * M / 22.4 * 273.15 / (273.15 + T) * P / 1013.25
    private static double milligramPerPpm(double molarMass, double temperature, double pressure) {
        return molarMass / 22.4 * 273.15 / (273.15 + temperature) * pressure / 1013.25;
    }

    @Test
    void volumeFractionsAndMassDensitiesAreConvertedWithTheMolarMass() throws Exception {
        // sensorinfo.md: the molar mass of each gas sensor, e.g. CO 28.01 g/mol
        poll(thing, """
                {"units": {"co": "ppm", "no2": "ppb", "co2": "mg/m^3", "temperature": "degF", "pressure": "kPa"},
                 "SensorInfo": {"co": {"Molar Mass": 28.01}, "no2": {"Molar Mass": 46.01},
                                "co2": {"Molar Mass": 44.01}}}
                """, """
                {"co": [2, 0.2], "no2": [10, 1], "co2": [1800, 90], "temperature": [77, 0.9], "pressure": [98, 0.1]}
                """);

        // 77 °F = 25 °C and 98 kPa = 980 hPa from the same measurement
        double co = milligramPerPpm(28.01, 25, 980);
        assertThat(value("measurements#co", AirqUnits.MILLIGRAM_PER_CUBICMETRE), closeTo(2 * co, 1e-9));
        assertThat(value("maxerr#co-maxerr", AirqUnits.MILLIGRAM_PER_CUBICMETRE), closeTo(0.2 * co, 1e-9));
        // c[µg/m³] = c[ppb] * M / 22.4 * 273.15 / (273.15 + T) * P / 1013.25
        double no2 = milligramPerPpm(46.01, 25, 980);
        assertThat(value("measurements#no2", Units.MICROGRAM_PER_CUBICMETRE), closeTo(10 * no2, 1e-9));
        double co2 = milligramPerPpm(44.01, 25, 980);
        assertThat(value("measurements#co2", Units.PARTS_PER_MILLION), closeTo(1800 / co2, 1e-9));
        assertThat(value("maxerr#co2-maxerr", Units.PARTS_PER_MILLION), closeTo(90 / co2, 1e-9));
    }

    @ParameterizedTest
    @CsvSource({ "oxygen, measurements#o2, %, ug/m^3", "humidity_abs, measurements#humidity-absolute, g/m^3, ppm",
            "co, measurements#co, mg/m^3, ppm", "co2, measurements#co2, ppm, ug/m^3",
            "no2, measurements#no2, ug/m^3, ppm", "so2, measurements#so2, ug/m^3, ppm",
            "o3, measurements#o3, ug/m^3, ppm", "h2s, measurements#h2s, ug/m^3, ppm",
            "ethanol, measurements#ethanol, ug/m^3, ppm", "c3h8_MIPEX, measurements#c3h8-mipex, %, ug/m^3",
            "ch4_MIPEX, measurements#ch4-mipex, %, ug/m^3", "tvoc_ionsc, measurements#tvoc-ionsc, ppb, ug/m^3",
            "nh3_MR100, measurements#nh3-mr100, ug/m^3, ppm", "acid_M100, measurements#acid-m100, ppb, ug/m^3",
            "h2_M1000, measurements#h2-m1000, ug/m^3, ppm", "no_M250, measurements#no-m250, ug/m^3, ppm",
            "cl2_M20, measurements#cl2-m20, ug/m^3, ppm", "ch2o_M10, measurements#ch2o-m10, ug/m^3, ppm",
            "n2o, measurements#n2o, ug/m^3, ppm", "r32, measurements#r32, %, ug/m^3",
            "r454b, measurements#r454b, %, ug/m^3", "r454c, measurements#r454c, %, ug/m^3" })
    void everyGasSensorConvertsBetweenVolumeFractionAndMassDensity(String key, String channel, String channelUnit,
            String configuredUnit) throws Exception {
        // measurement.md: the units of these sensors may be changed to %, mg/m^3, ug/m^3, ppm or ppb
        poll(thing,
                "{\"units\": {\"" + key + "\": \"" + configuredUnit + "\"}, \"SensorInfo\": {\"" + key
                        + "\": {\"Molar Mass\": 30.0}}}",
                "{\"" + key + "\": [10, 1], \"temperature\": [20, 0.5], \"pressure\": [1013.25, 1]}");

        double perPpm = milligramPerPpm(30.0, 20, 1013.25);
        QuantityType<?> expected = "ppm".equals(configuredUnit)
                ? new QuantityType<>(10 * perPpm, AirqUnits.MILLIGRAM_PER_CUBICMETRE)
                : new QuantityType<>(0.01 / perPpm, Units.PARTS_PER_MILLION);
        Unit<?> unit = requireNonNull(AirqUnits.deviceUnit(channelUnit));
        double value = requireNonNull(expected.toUnit(unit)).doubleValue();
        assertThat(value(channel, unit), closeTo(value, value * 1e-9));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"co\": [2, 0.2], \"pressure\": [1013.25, 1]}",
            "{\"co\": [2, 0.2], \"temperature\": [20, 0.5]}" })
    void concentrationsWithoutTemperatureOrPressureAreUndefined(String payload) throws Exception {
        // measurement.md: the conversion needs the temperature and pressure of the same measurement
        poll(thing, "{\"units\": {\"co\": \"ppm\"}, \"SensorInfo\": {\"co\": {\"Molar Mass\": 28.01}}}", payload);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co"), UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#co-maxerr"), UnDefType.UNDEF);
    }

    @Test
    void deprecatedPpmAndPpbIsUsedForSensorsWithoutConfiguredUnit() throws Exception {
        // measurement.md: ppm&ppb is only evaluated if no units are configured
        poll(thing, """
                {"ppm&ppb": true, "units": {"no2": "ug/m^3"},
                 "SensorInfo": {"co": {"Molar Mass": 28.01}, "no2": {"Molar Mass": 46.01}}}
                """, """
                {"co": [2, 0.2], "no2": [10, 1], "temperature": [20, 0.5], "pressure": [1013.25, 1]}
                """);

        assertThat(value("measurements#co", AirqUnits.MILLIGRAM_PER_CUBICMETRE),
                closeTo(2 * milligramPerPpm(28.01, 20, 1013.25), 1e-9));
        assertThat(value("measurements#no2", Units.MICROGRAM_PER_CUBICMETRE), closeTo(10, 1e-9));
    }

    @Test
    void concentrationsWithoutMolarMassAreUndefined() throws Exception {
        poll(thing, "{\"units\": {\"co\": \"ppm\"}}", """
                {"co": [2, 0.2], "temperature": [20, 0.5], "pressure": [1013.25, 1]}
                """);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co"), UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#co-maxerr"), UnDefType.UNDEF);
    }

    @Test
    void measurementsInUnknownUnitsAreUndefined() throws Exception {
        // a reading in an unknown unit can't be interpreted, the other readings are not affected
        poll(thing, "{\"units\": {\"co\": \"furlong\", \"so2\": 5}}",
                "{\"co\": [2, 0.2], \"so2\": [7, 1], \"no2\": [10, 1]}");

        for (String channel : List.of("measurements#co", "maxerr#co-maxerr", "measurements#so2", "maxerr#so2-maxerr")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel), UnDefType.UNDEF);
        }
        assertThat(value("measurements#no2", Units.MICROGRAM_PER_CUBICMETRE), closeTo(10, 1e-9));
    }

    @Test
    void impossibleConditionsDoNotConvertConcentrations() {
        QuantityType<?> co = new QuantityType<>(2, Units.PARTS_PER_MILLION);

        assertThat(AirqUnits.convertConcentration(co, AirqUnits.MILLIGRAM_PER_CUBICMETRE, 28.01, -273.15, 1013.25),
                nullValue());
        assertThat(AirqUnits.convertConcentration(co, AirqUnits.MILLIGRAM_PER_CUBICMETRE, 28.01, 20, 0), nullValue());
        assertThat(AirqUnits.convertConcentration(co, AirqUnits.MILLIGRAM_PER_CUBICMETRE, Double.NaN, 20, 1013.25),
                nullValue());
    }

    @Test
    void meanParticleSizeIsInMicrometres() throws Exception {
        // datenlesen.md: TypPS in µm
        poll("{\"TypPS\": 0.5}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#avg-fine-dust-size"),
                new QuantityType<>(0.5, AirqUnits.MICROMETRE));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"TypPS\": null}", "{\"TypPS\": \"n/a\"}", "{\"TypPS\": [0.5, 0.1]}" })
    void meanParticleSizeIsUndefinedWhenNotOutput(String payload) throws Exception {
        // datenlesen.md: TypPS is not output by default
        poll(payload);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#avg-fine-dust-size"),
                UnDefType.UNDEF);
    }

    @Test
    void configurationIsReadBeforeTheFirstMeasurement() throws Exception {
        AirqHandler pollingHandler = requireNonNull(spy(handler()));
        pollingHandler.setCallback(callback);
        doReturn("{}").when(pollingHandler).getDecryptedContentString(anyString());

        pollingHandler.pollData();
        pollingHandler.pollData();

        var order = inOrder(pollingHandler);
        order.verify(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/config"));
        order.verify(pollingHandler, times(2)).getDecryptedContentString(ArgumentMatchers.endsWith("/data"));
        verify(pollingHandler, times(1)).getDecryptedContentString(ArgumentMatchers.endsWith("/config"));
    }

    private State lastState(String channel) {
        ChannelUID channelUID = new ChannelUID(thingUID, channel);
        return mockingDetails(callback).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("stateUpdated")
                        && channelUID.equals(invocation.getArgument(0)))
                .map(invocation -> invocation.getArgument(1, State.class)).reduce((first, second) -> second)
                .orElseThrow();
    }

    private double value(String channel, Unit<?> unit) {
        State state = lastState(channel);
        assertThat(state, instanceOf(QuantityType.class));
        return requireNonNull(((QuantityType<?>) state).toUnit(unit)).doubleValue();
    }

    @Test
    void publishesRealTvocReadingInPartsPerBillion() throws Exception {
        poll("{\"tvoc\": [2353, 401.2]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#tvoc"),
                new QuantityType<>(2353, Units.PARTS_PER_BILLION));
    }

    @ParameterizedTest
    @CsvSource({ "dCO2dt, measurements#d-co2dt", "dHdt, measurements#d-hdt", "door_event, events#door-event",
            "health, measurements#health", "performance, measurements#performance" })
    void preservesLegacyScalarMappings(String key, String channel) throws Exception {
        poll("{\"" + key + "\": 125}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel), new DecimalType(125));
    }

    @ParameterizedTest
    @CsvSource({ "health, health-index", "performance, performance-index" })
    void publishesBothRawAndScaledIndices(String key, String channel) throws Exception {
        poll("{\"" + key + "\": 855}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + key),
                new DecimalType(855));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new QuantityType<>(85.5, Units.PERCENT));
    }

    @ParameterizedTest
    @CsvSource({ "894, 89.4, OFF, OFF", "0, 0, OFF, OFF", "-200, -20, OFF, ON", "-800, -80, ON, OFF" })
    void healthIndexReportsGasAndFireAlarms(int health, double percent, OnOffType fire, OnOffType gas)
            throws Exception {
        // datenlesen.md: health is 0 to 1000, -200 for gas alarm, -800 for fire alarm
        poll(thing, ALARMS_ENABLED, "{\"health\": " + health + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#health"),
                new DecimalType(health));
        assertThat(value("measurements#health-index", Units.PERCENT), closeTo(percent, 1e-9));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#fire-alarm-active"), fire);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#gas-alarm-active"), gas);
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"health\": null}" })
    void alarmsAreUndefinedWithoutHealthIndex(String payload) throws Exception {
        poll(thing, ALARMS_ENABLED, payload);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#fire-alarm-active"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#gas-alarm-active"),
                UnDefType.UNDEF);
    }

    @Test
    void liveDataReportsNoAlarm() throws Exception {
        // 2. LifeData.md: "health":894
        poll(thing, ALARMS_ENABLED, LIVE_DATA);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#fire-alarm-active"),
                OnOffType.OFF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#gas-alarm-active"),
                OnOffType.OFF);
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = { "{}; 894", "{\"FireAlarm\": false, \"GasAlarm\": false}; 894",
            "{\"FireAlarm\": false, \"GasAlarm\": false}; -200", "{\"FireAlarm\": false, \"GasAlarm\": false}; -800" })
    void disabledAlarmsAreUndefined(String configuration, int health) throws Exception {
        // alarm.md: FireAlarm and GasAlarm are false by default, a disabled alarm is never raised
        poll(thing, configuration, "{\"health\": " + health + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#fire-alarm-active"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#gas-alarm-active"),
                UnDefType.UNDEF);
    }

    @Test
    void anAlarmEnabledByCommandIsReportedWithTheNextMeasurement() throws Exception {
        String password = "secret";
        JsonObject configuration = new JsonObject();
        configuration.addProperty("content", handler.encrypt("{}".getBytes(StandardCharsets.UTF_8), password));
        JsonObject data = new JsonObject();
        data.addProperty("content", handler.encrypt("{\"health\": 894}".getBytes(StandardCharsets.UTF_8), password));
        JsonObject response = new JsonObject();
        response.addProperty("content", handler.encrypt("{}".getBytes(StandardCharsets.UTF_8), password));
        AirqHandler commandHandler = new AirqHandler(thing, requireNonNull(mock(HttpClient.class))) {
            @Override
            protected Result getData(String address, String method, @Nullable String body) {
                if ("POST".equals(method)) {
                    return new Result(response.toString(), 200);
                }
                return new Result((address.endsWith("/config") ? configuration : data).toString(), 200);
            }
        };
        commandHandler.setCallback(callback);
        commandHandler.config.password = password;
        ChannelUID fireAlarmActive = new ChannelUID(thingUID, "events#fire-alarm-active");

        commandHandler.pollData();
        requireNonNull(verify(callback)).stateUpdated(fireAlarmActive, UnDefType.UNDEF);
        commandHandler.handleCommand(new ChannelUID(thingUID, "settings#fire-alarm-enabled"), OnOffType.ON);
        commandHandler.pollData();

        requireNonNull(verify(callback)).stateUpdated(fireAlarmActive, OnOffType.OFF);
    }

    @Test
    void onlyTheEnabledAlarmIsReported() throws Exception {
        poll(thing, "{\"FireAlarm\": true, \"GasAlarm\": false}", "{\"health\": 894}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#fire-alarm-active"),
                OnOffType.OFF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#gas-alarm-active"),
                UnDefType.UNDEF);
    }

    @Test
    void wifiSignalStrengthIsInDecibelMilliwatts() throws Exception {
        // the WLAN config of a real air-Q; datenlesen.md: typically -30 dBm (very good) to -90 dBm (very poor)
        readConfiguration("{\"WLAN config\": {\"RSSI\": -61, \"Channel\": 11, \"Status\": \"Got IP\"}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#wifi-rssi"),
                new QuantityType<>(-61, Units.DECIBEL_MILLIWATTS));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "null", "\"x\"" })
    void wifiSignalStrengthIsUndefinedWithoutConnection(String rssi) throws Exception {
        // datenlesen.md: an RSSI of 0 dBm means that no connection is present
        readConfiguration("{\"WLAN config\": {\"RSSI\": " + rssi + ", \"SSID\": \"ssid\"}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#wifi-rssi"), UnDefType.UNDEF);
        // the rest of the WLAN config is still read
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#wlan-config-ssid"),
                new StringType("ssid"));
    }

    @ParameterizedTest
    @CsvSource({ "flow1, flow1", "flow2, flow2", "flow3, flow3", "flow4, flow4" })
    void airFlowsAreInLitresPerMinute(String key, String channel) throws Exception {
        // datenlesen.md: air flow in slm, standard litres per minute
        poll("{\"" + key + "\": 1.25}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new QuantityType<>(new BigDecimal("1.25"), Units.LITRE_PER_MINUTE));
    }

    @ParameterizedTest
    @CsvSource({ "pm1_SPS30, pm1-sps30", "pm2_5_SPS30, pm2-5-sps30", "pm4_SPS30, pm4-sps30", "pm10_SPS30, pm10-sps30" })
    void sps30ParticulatesAreInMicrogramsPerCubicMetre(String key, String channel) throws Exception {
        // datenlesen.md: SPS30 particulates concentration in µg/m³
        poll("{\"" + key + "\": [12, 3]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new QuantityType<>(12, Units.MICROGRAM_PER_CUBICMETRE));
    }

    @ParameterizedTest
    @CsvSource({ "cnt0_5_SPS30, cnt0-5-sps30", "cnt1_SPS30, cnt1-sps30", "cnt2_5_SPS30, cnt2-5-sps30",
            "cnt4_SPS30, cnt4-sps30", "cnt10_SPS30, cnt10-sps30" })
    void sps30CountsArePerCubicCentimetre(String key, String channel) throws Exception {
        // datenlesen.md: SPS30 particle number concentrations in 1/cm³, a plain number
        poll("{\"" + key + "\": 7.5}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new DecimalType(new BigDecimal("7.5")));
    }

    @Test
    void sps30MeanParticleSizeIsInMicrometres() throws Exception {
        // datenlesen.md: the mean particulates size of the SPS30 in µm
        poll("{\"TypPS_SPS30\": 0.62}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#avg-fine-dust-size-sps30"),
                new QuantityType<>(new BigDecimal("0.62"), AirqUnits.MICROMETRE));
    }

    @ParameterizedTest
    @CsvSource({ "person, person", "window_event, window-event", "window_open, window-open", "door_event, door-event" })
    void eventsOfTheSamplePayloadArePlainNumbers(String key, String channel) throws Exception {
        // datenlesen.md: "door_event": 0, "person": 1, "window_event": 0, "window_open": 0 in the sample payload
        poll("{\"" + key + "\": 1}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "events#" + channel),
                new DecimalType(1));
    }

    @ParameterizedTest
    @CsvSource({ "person, events#person", "window_event, events#window-event", "window_open, events#window-open",
            "door_event, events#door-event", "flow1, measurements#flow1", "pm4_SPS30, measurements#pm4-sps30",
            "cnt4_SPS30, measurements#cnt4-sps30", "TypPS_SPS30, measurements#avg-fine-dust-size-sps30" })
    void measurementsWithoutErrorChannelAreUndefinedWhenMalformedOrAbsent(String key, String channel) throws Exception {
        poll("{\"" + key + "\": {\"x\": 1}}");
        poll("{}");

        verify(callback, times(2)).stateUpdated(new ChannelUID(thingUID, channel), UnDefType.UNDEF);
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void preservesLegacyStatusTimestampAndUptime() throws Exception {
        poll("{\"Status\": \"OK\", \"timestamp\": 0, \"uptime\": 123}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#status"), new StringType("OK"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#timestamp"),
                new DateTimeType(Instant.EPOCH));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#uptime"),
                new QuantityType<>(123, Units.SECOND));
    }

    @Test
    void statusListsTheMessageOfEachSensor() throws Exception {
        // datenlesen.md: the example of a Status object
        poll("""
                {"Status": {"co": "co sensor still in warm up phase; waiting time = 1543 s",
                            "no2": "no2 sensor still in warm up phase; waiting time = 3343 s",
                            "o3": "o3 sensor still in warm up phase; waiting time = 3343 s",
                            "so2": "so2 sensor still in warm up phase; waiting time = 3343 s"}}
                """);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#status"), new StringType(
                "co: co sensor still in warm up phase; waiting time = 1543 s, no2: no2 sensor still in warm up phase; "
                        + "waiting time = 3343 s, o3: o3 sensor still in warm up phase; waiting time = 3343 s, "
                        + "so2: so2 sensor still in warm up phase; waiting time = 3343 s"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "[\"co\"]", "null" })
    void statusInAnotherFormIsUndefined(String status) throws Exception {
        poll("{\"Status\": " + status + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#status"), UnDefType.UNDEF);
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"humidity\": null, \"co2\": null}" })
    void preservesLegacyMissingValueBehavior(String payload) throws Exception {
        poll(payload);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#humidity-relative"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#humidity-relative-maxerr"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co2"), UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#co2-maxerr"), UnDefType.UNDEF);
        assertThat(
                mockingDetails(callback).getInvocations().stream()
                        .filter(invocation -> invocation.getMethod().getName().equals("stateUpdated"))
                        .map(invocation -> invocation.getArgument(0, ChannelUID.class)).toList(),
                not(hasItem(new ChannelUID(thingUID, "measurements#ch2o-m10"))));
    }

    @Test
    void malformedLegacyPairGoesOfflineAndRecovers() throws Exception {
        poll("{\"co\": []}");

        requireNonNull(verify(callback)).statusUpdated(thing, new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.COMMUNICATION_ERROR, "Syntax error while parsing response from device"));

        poll("{\"co\": [12.5, 0.25]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co"),
                new QuantityType<>(12.5, AirqUnits.MILLIGRAM_PER_CUBICMETRE));
        requireNonNull(verify(callback)).statusUpdated(thing,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    // entschlüsseln.md: a JSON object with "content", or in some cases the encrypted string directly
    private String encryptedResponse(String form, String payload) throws Exception {
        String encrypted = handler.encrypt(payload.getBytes(StandardCharsets.UTF_8), "");
        JsonObject wrapped = new JsonObject();
        wrapped.addProperty("content", encrypted);
        return switch (form) {
            case "object" -> wrapped.toString();
            case "text" -> encrypted;
            case "quoted" -> "\"" + encrypted + "\"";
            default -> " " + encrypted + "\n";
        };
    }

    @ParameterizedTest
    @ValueSource(strings = { "object", "text", "quoted", "padded" })
    void pollingAcceptsAllEncryptedResponseForms(String form) throws Exception {
        pollResponses(encryptedResponse(form, "{\"co\": [12.5, 0.25]}"));

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co"),
                new QuantityType<>(12.5, AirqUnits.MILLIGRAM_PER_CUBICMETRE));
        requireNonNull(verify(callback)).statusUpdated(thing,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "object", "text", "quoted", "padded" })
    void configurationAcceptsAllEncryptedResponseForms(String form) throws Exception {
        AirqHandler configurationHandler = respondingHandler("/config",
                List.of(encryptedResponse(form, "{\"devicename\": \"device\"}")));

        configurationHandler.getConfigData();

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#device-name"),
                new StringType("device"));
    }

    static Stream<String> malformedResponses() throws Exception {
        AirqHandler encryptor = handler();
        return Stream.of("", " ", "[]", "{}", "{", "null", "{\"content\": null}", "{\"content\": {}}",
                "{\"content\": 5}", "\"\"", "not base64!", "AAAA",
                "{\"content\": \"" + encryptor.encrypt("[]".getBytes(StandardCharsets.UTF_8), "") + "\"}",
                "{\"content\": \"" + encryptor.encrypt("{".getBytes(StandardCharsets.UTF_8), "") + "\"}");
    }

    @ParameterizedTest
    @MethodSource("malformedResponses")
    void malformedResponsesKeepPollingAlive(String body) throws Exception {
        JsonObject valid = new JsonObject();
        valid.addProperty("content", handler.encrypt("{\"co\": [12.5, 0.25]}".getBytes(StandardCharsets.UTF_8), ""));

        pollResponses(body, valid.toString());

        requireNonNull(verify(callback)).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co"),
                new QuantityType<>(12.5, AirqUnits.MILLIGRAM_PER_CUBICMETRE));
        requireNonNull(verify(callback)).statusUpdated(thing,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    @ParameterizedTest
    @MethodSource("malformedResponses")
    void malformedConfigurationResponsesDoNotThrow(String body) throws Exception {
        respondingHandler("/config", List.of(body)).getConfigData();

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void unexpectedRuntimeExceptionsKeepPollingAlive() throws Exception {
        AirqHandler failingHandler = requireNonNull(spy(handler()));
        failingHandler.setCallback(callback);
        doThrow(new IllegalStateException("boom")).when(failingHandler).getData(anyString(), eq("GET"), isNull());

        failingHandler.pollData();
        failingHandler.getConfigData();

        requireNonNull(verify(callback)).statusUpdated(thing, new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.COMMUNICATION_ERROR, "Failed to process measurement data: boom"));
    }

    @ParameterizedTest
    @CsvSource({ "airqsetup, airqsetup00000000000000000000000", "'', 00000000000000000000000000000000",
            "abcdefghijklmnopqrstuvwxyz012345, abcdefghijklmnopqrstuvwxyz012345",
            "abcdefghijklmnopqrstuvwxyz0123456789, abcdefghijklmnopqrstuvwxyz012345",
            "pässwort, pässwort00000000000000000000000" })
    void aesKeyIsUtf8PasswordPaddedOrTruncatedToThirtyTwoBytes(String password, String expectedKey) {
        // entschlüsseln.md and verschlüsseln.md, e.g. airqsetup -> airqsetup00000000000000000000000
        assertThat(AirqHandler.aesKey(password), is(expectedKey.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void aesKeyTruncatesUtf8BytesNotCharacters() {
        // 31 characters plus "ä", which is 2 bytes in UTF-8: the key ends with the first byte of "ä"
        byte[] key = AirqHandler.aesKey("abcdefghijklmnopqrstuvwxyz01234ä");

        assertThat(key.length, is(32));
        assertThat(key[31], is((byte) 0xC3));
    }

    @ParameterizedTest
    @ValueSource(strings = { "airqsetup", "pässwort", "abcdefghijklmnopqrstuvwxyz0123456789" })
    void encryptedDataDecryptsWithTheSamePassword(String password) throws Exception {
        String encrypted = handler.encrypt("{\"co\": 1}".getBytes(StandardCharsets.UTF_8), password);

        assertThat(handler.decrypt(encrypted.getBytes(StandardCharsets.UTF_8), password), is("{\"co\": 1}"));
    }

    @Test
    void settingsArePostedFormEncoded() throws Exception {
        Request request = mockedRequest();

        new AirqHandler(thing, mockedClient(request)).getData("http://device/config", "POST", "request=ab+/cd==");

        ArgumentCaptor<ContentProvider> content = ArgumentCaptor.forClass(ContentProvider.class);
        requireNonNull(verify(request)).header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        requireNonNull(verify(request)).content(content.capture());
        // konfigschreiben.md posts the base64 request without URL-encoding it
        assertThat(StandardCharsets.UTF_8.decode(content.getValue().iterator().next()).toString(),
                is("request=ab+/cd=="));
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 204, 299 })
    void successfulSettingResponsesAreDecrypted(int status) throws Exception {
        AirqHandler postingHandler = postingHandler(status);

        postingHandler.handleCommand(new ChannelUID(thingUID, "settings#wifi-info"), OnOffType.ON);

        requireNonNull(verify(postingHandler)).decrypt(any(), anyString());
    }

    @ParameterizedTest
    @ValueSource(ints = { 199, 300, 400, 403, 500 })
    void failedSettingResponsesAreNotDecrypted(int status) throws Exception {
        AirqHandler postingHandler = postingHandler(status);

        postingHandler.handleCommand(new ChannelUID(thingUID, "settings#wifi-info"), OnOffType.ON);

        verify(postingHandler, never()).decrypt(any(), anyString());
    }

    private AirqHandler postingHandler(int status) throws Exception {
        JsonObject response = new JsonObject();
        response.addProperty("content", handler.encrypt("{}".getBytes(StandardCharsets.UTF_8), ""));
        AirqHandler postingHandler = requireNonNull(spy(handler()));
        doReturn(new AirqHandler.Result(response.toString(), status)).when(postingHandler).getData(anyString(),
                eq("POST"), anyString());
        return postingHandler;
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "{\"Wifi\": \"Error: unknown value\"}|true", "ERROR|true",
            "{\"Averaging\": \"success\"}|false", "{}|false", "''|false" })
    void changeLogErrorsAreDetected(String changeLog, boolean error) {
        // konfigschreiben.md: the device returns a success and error log for the requested changes
        assertThat(AirqHandler.reportsError(changeLog), is(error));
    }

    @Test
    void readsAreSentWithoutContent() throws Exception {
        Request request = mockedRequest();

        new AirqHandler(thing, mockedClient(request)).getData("http://device/data", "GET", null);

        requireNonNull(verify(request)).method("GET");
        verify(request, never()).header(eq(HttpHeader.CONTENT_TYPE), anyString());
        verify(request, never()).content(any());
    }

    private static Request mockedRequest() throws Exception {
        Request request = requireNonNull(mock(Request.class, RETURNS_SELF));
        ContentResponse response = requireNonNull(mock(ContentResponse.class));
        doReturn("{}").when(response).getContentAsString();
        doReturn(200).when(response).getStatus();
        doReturn(response).when(request).send();
        return request;
    }

    private static HttpClient mockedClient(Request request) {
        HttpClient httpClient = requireNonNull(mock(HttpClient.class));
        doReturn(request).when(httpClient).newRequest(anyString());
        return httpClient;
    }

    private static AirqHandler handler() {
        return new AirqHandler(ThingBuilder.create(THING_TYPE_AIRQ, new ThingUID(THING_TYPE_AIRQ, "test")).build(),
                requireNonNull(mock(HttpClient.class)));
    }

    private void pollResponses(String... bodies) throws Exception {
        AirqHandler pollingHandler = respondingHandler("/data", List.of(bodies));
        for (int i = 0; i < bodies.length; i++) {
            pollingHandler.pollData();
        }
    }

    // answers the requests to path with the given bodies, and the other requests with an empty object
    private AirqHandler respondingHandler(String path, List<String> bodies) throws Exception {
        List<String> responses = new ArrayList<>(bodies);
        JsonObject empty = new JsonObject();
        empty.addProperty("content", handler.encrypt("{}".getBytes(StandardCharsets.UTF_8), ""));
        AirqHandler respondingHandler = new AirqHandler(thing, requireNonNull(mock(HttpClient.class))) {
            @Override
            protected Result getData(String address, String method, @Nullable String body) {
                assertThat(method, is("GET"));
                return new Result(address.endsWith(path) ? responses.removeFirst() : empty.toString(), 200);
            }
        };
        respondingHandler.setCallback(callback);
        return respondingHandler;
    }

    @Test
    void preservesLegacyConfigurationMappings() throws Exception {
        AirqHandler configurationHandler = requireNonNull(spy(handler));
        doReturn(new AirqHandler.Result("{\"content\": \"encoded\"}", 200)).when(configurationHandler)
                .getData(anyString(), eq("GET"), isNull());
        doReturn("""
                {"Wifi": true, "WLANssid": ["first", "second"], "pass": "password", "TimeServer": "time.example",
                 "geopos": {"lat": 50, "long": 10}, "devicename": "device", "SecondsMeasurementDelay": 5,
                 "NightMode": {"StartDay": "08:00", "StartNight": "22:30", "BrightnessNight": 2, "FanNightOff": true},
                 "Rejection": "50Hz+60Hz",
                 "WLAN config": {"MAC": "mac", "IP address": "ip"}, "AutoUpdate": 90, "Averaging": 30,
                 "warmup-phase": false, "air-Q-Hardware-Version": "hardware", "sensors": ["co2", "sound"],
                 "Industry": true, "id": "device-id", "SensorInfo": "info"}
                    """).when(configurationHandler).decrypt("encoded".getBytes(StandardCharsets.UTF_8), "");

        configurationHandler.getConfigData();

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#wifi"), OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#ssid"),
                new StringType("first, second"));
        // the undocumented key "pass" is not published, the channel was removed
        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, "settings#password")), any());
        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, "password")), any());
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#time-server"),
                new StringType("time.example"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#location"),
                new PointType(new DecimalType(50f), new DecimalType(10f)));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#device-name"),
                new StringType("device"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#averaging-rhythm"),
                new QuantityType<>(5, Units.SECOND));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-start-day"),
                new QuantityType<>(480, Units.MINUTE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-start-night"),
                new QuantityType<>(1350, Units.MINUTE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#power-freq-suppression"),
                new StringType("50Hz+60Hz"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-brightness-night"),
                new DecimalType(2));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-fan-night-off"),
                OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#wlan-config-mac"),
                new StringType("mac"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#wlan-config-ip-address"),
                new StringType("ip"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#auto-update"),
                new QuantityType<>(90, Units.DAY));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#averaging"),
                new DecimalType(30));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#warmup-phase"), OnOffType.OFF);
        assertThat(thing.getProperties(),
                allOf(hasEntry("hardwareVersion", "hardware"), hasEntry("sensorList", "co2, sound"),
                        hasEntry("industry", "true"), hasEntry("id", "device-id"), hasEntry("sensorInfo", "info")));
    }

    @Test
    void propertiesOmitJsonQuotesAndReplaceTheOldIndustryProperty() throws Exception {
        thing.setProperty("Industry", "false");

        // excerpt of the /config answer of an air-Q Pro
        readConfiguration(thing, """
                {"air-Q-Hardware-Version": "D", "air-Q-Software-Version": "1.26.0_D_2.2.2", "Industry": false,
                 "id": "91e5157abe8a02b2f3cd428558decacb",
                 "SensorInfo": {"co2": {"Unit": "ppm", "Value Name": "co2"}}}
                """);

        assertThat(thing.getProperties(),
                allOf(hasEntry("hardwareVersion", "D"), hasEntry("softwareVersion", "1.26.0_D_2.2.2"),
                        hasEntry("industry", "false"), hasEntry("id", "91e5157abe8a02b2f3cd428558decacb"),
                        hasEntry("sensorInfo", "{\"co2\":{\"Unit\":\"ppm\",\"Value Name\":\"co2\"}}"),
                        not(hasKey("Industry"))));
    }

    @Test
    void removedChannelsAreNotPublished() throws Exception {
        // "pass" is not documented, DeleteKey is only a command and ErrorBars is no setting of the device
        readConfiguration("{\"pass\": \"secret\", \"DeleteKey\": \"WLANssid\", \"ErrorBars\": true}");

        for (String channel : List.of("settings#password", "settings#delete-key", "settings#error-bars", "password",
                "deleteKey", "errorBars")) {
            verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, channel)), any());
        }
    }

    @Test
    void soundInfoSummarizesThresholdSignals() throws Exception {
        Thing legacyThing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID)
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, "soundPressure"), "Switch").build())
                .build();

        // example from deviceconfig.md, "Acoustic signals"
        readConfiguration(legacyThing, """
                {"SoundInfo": {"co2": {
                  "1200": {"freq": 900, "vol": 100, "pass": "up", "length": 2000, "repeat": false},
                  "2000": {"freq": 1200, "vol": 100, "pass": "down", "length": 2000, "repeat": true}}}}
                """);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#sound-info"),
                new StringType("co2 1200 up: 900 Hz, 100 %, 2000 ms; co2 2000 down: 1200 Hz, 100 %, 2000 ms, repeat"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "soundPressure"), OnOffType.ON);

        // device default
        readConfiguration(legacyThing, "{\"SoundInfo\": {}}");
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#sound-info"),
                new StringType(""));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "soundPressure"), OnOffType.OFF);
    }

    @Test
    void measurementDurationUsesMilliseconds() throws Exception {
        poll("{\"measuretime\": 2006}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#measure-time"),
                new QuantityType<>(2006, MetricPrefix.MILLI(Units.SECOND)));
    }

    @ParameterizedTest
    @CsvSource({ "night-mode-start-day, 480 min, StartDay, 08:00", "night-mode-start-night, 81000 s, StartNight, 22:30",
            "night-mode-start-day, 0 min, StartDay, 00:00", "night-mode-start-night, 1439 min, StartNight, 23:59" })
    void timeCommandsAreEncodedAsDeviceClockTimes(String channel, String command, String key, String expected)
            throws Exception {
        JsonObject settings = requireNonNull(sendSettings(channel, QuantityType.valueOf(command)));

        assertThat(settings.getAsJsonObject("NightMode").get(key).getAsString(), is(expected));
    }

    @ParameterizedTest
    @CsvSource({ "nightModeStartDay, 08:00, StartDay", "nightModeStartNight, 22:30, StartNight",
            "nightModeStartDay, 00:00, StartDay", "nightModeStartNight, 23:59, StartNight",
            "settings#night-mode-start-day, 08:00, StartDay", "settings#night-mode-start-night, 22:30, StartNight" })
    void stringClockCommandsPreserveLegacyAndGroupedChannels(String channel, String clock, String key)
            throws Exception {
        JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), new StringType(clock)));

        assertThat(settings.getAsJsonObject("NightMode").get(key).getAsString(), is(clock));
    }

    @ParameterizedTest
    @ValueSource(strings = { "24:00", "08:60", "invalid", "08:00:30", "8:00", "480", "" })
    void malformedStringClockCommandsDoNotSendRequests(String clock) throws Exception {
        for (String channel : List.of("nightModeStartDay", "nightModeStartNight", "settings#night-mode-start-day",
                "settings#night-mode-start-night")) {
            assertThat(sendSettings(new ChannelUID(thingUID, channel), new StringType(clock)), nullValue());
        }
    }

    @Test
    void measurementDelayRejectsStringClockCommands() throws Exception {
        assertThat(sendSettings("averagingRhythm", new StringType("08:00")), nullValue());
    }

    @ParameterizedTest
    @CsvSource({ "00:00, 0", "08:00, 480", "23:59, 1439" })
    void clockReadingsUseMinutesSinceMidnight(String clock, int minutes) throws Exception {
        readConfiguration("{\"NightMode\": {\"StartDay\": \"" + clock + "\"}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-start-day"),
                new QuantityType<>(minutes, Units.MINUTE));
    }

    @ParameterizedTest
    @ValueSource(strings = { "24:00", "08:60", "invalid", "08:00:30" })
    void invalidClockReadingsBecomeUndefined(String clock) throws Exception {
        readConfiguration("{\"NightMode\": {\"StartDay\": \"" + clock + "\"}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-start-day"),
                UnDefType.UNDEF);
    }

    private void readConfiguration(String payload) throws Exception {
        readConfiguration(thing, payload);
    }

    private void readConfiguration(Thing configuredThing, String payload) throws Exception {
        JsonObject response = new JsonObject();
        response.addProperty("content", handler.encrypt(payload.getBytes(StandardCharsets.UTF_8), ""));
        AirqHandler configurationHandler = new AirqHandler(configuredThing, requireNonNull(mock(HttpClient.class))) {
            @Override
            protected Result getData(String address, String method, @Nullable String body) {
                assertThat(method, is("GET"));
                assertThat(body, nullValue());
                return new Result(response.toString(), 200);
            }
        };
        configurationHandler.setCallback(callback);
        configurationHandler.getConfigData();
    }

    @Test
    void plainNumericTimeCommandsUseChannelUnits() throws Exception {
        JsonObject day = requireNonNull(sendSettings("night-mode-start-day", new DecimalType(480)));
        assertThat(day.getAsJsonObject("NightMode").get("StartDay").getAsString(), is("08:00"));
        JsonObject delay = requireNonNull(sendSettings("averaging-rhythm", new DecimalType(5)));
        assertThat(delay.get("SecondsMeasurementDelay").getAsInt(), is(5));
    }

    @Test
    void measurementDelayCommandUsesSeconds() throws Exception {
        JsonObject settings = requireNonNull(sendSettings("averaging-rhythm", new QuantityType<>(2, Units.MINUTE)));

        assertThat(settings.get("SecondsMeasurementDelay").getAsInt(), is(120));
    }

    @ParameterizedTest
    @ValueSource(strings = { "50Hz", "60Hz", "50Hz+60Hz" })
    void suppressionCommandsPreserveAllDeviceModes(String mode) throws Exception {
        JsonObject settings = requireNonNull(sendSettings("power-freq-suppression", new StringType(mode)));

        assertThat(settings.get("Rejection").getAsString(), is(mode));
    }

    @ParameterizedTest
    @CsvSource({ "night-mode-start-day, -1 min", "night-mode-start-day, 1440 min", "night-mode-start-night, 0.5 min",
            "averaging-rhythm, -1 s", "averaging-rhythm, 1.5 s", "averaging-rhythm, 2147483648 s",
            "night-mode-start-day, 2 Hz" })
    void invalidNumericSettingsDoNotSendRequests(String channel, String command) throws Exception {
        assertThat(sendSettings(channel, QuantityType.valueOf(command)), nullValue());
    }

    private @Nullable JsonObject sendSettings(String channel, Command command) throws Exception {
        return sendSettings(new ChannelUID(thingUID, "settings#" + channel), command);
    }

    private @Nullable JsonObject sendSettings(ChannelUID channel, Command command) throws Exception {
        List<String> requests = new ArrayList<>();
        String password = "secret";
        JsonObject response = new JsonObject();
        response.addProperty("content", handler.encrypt("{}".getBytes(StandardCharsets.UTF_8), password));
        AirqHandler commandHandler = new AirqHandler(thing, requireNonNull(mock(HttpClient.class))) {
            @Override
            protected Result getData(String address, String method, @Nullable String body) {
                assertThat(method, is("POST"));
                requests.add(requireNonNull(body));
                return new Result(response.toString(), 200);
            }
        };
        commandHandler.config.password = password;
        commandHandler.handleCommand(channel, command);
        if (requests.isEmpty()) {
            return null;
        }
        assertThat(requests, hasSize(1));
        return json(commandHandler.decrypt(
                requests.getFirst().substring("request=".length()).getBytes(StandardCharsets.UTF_8), password));
    }

    private void poll(String payload) throws Exception {
        poll(thing, payload);
    }

    private void poll(Thing configuredThing, String payload) throws Exception {
        poll(configuredThing, "{}", payload);
    }

    // the configuration is read before the first measurement, it contains the units of the measurements
    private void poll(Thing configuredThing, String configuration, String payload) throws Exception {
        AirqHandler pollingHandler = requireNonNull(
                spy(new AirqHandler(configuredThing, requireNonNull(mock(HttpClient.class)))));
        pollingHandler.setCallback(callback);
        doReturn(payload).when(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/data"));
        doReturn(configuration).when(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/config"));
        pollingHandler.pollData();
    }

    @Test
    void retainedFlatMeasurementChannelsReceiveStatesAlongsideGroupedChannels() throws Exception {
        Thing legacyThing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID)
                .withChannels(ChannelBuilder.create(new ChannelUID(thingUID, "co"), "Number:Density").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "co_maxerr"), "Number:Dimensionless").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "so2"), "Number").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "so2_maxerr"), "Number:Dimensionless").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "measurements#co"), "Number:Density").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "maxerr#co-maxerr"), "Number:Density").build())
                .build();

        poll(legacyThing, "{\"co\": [12.5, 0.25], \"so2\": [7.5, 1.5]}");

        // the released flat channels keep their item types (thing type version 4)
        for (String channel : List.of("co", "measurements#co")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel),
                    new QuantityType<>(12.5, AirqUnits.MILLIGRAM_PER_CUBICMETRE));
        }
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#co-maxerr"),
                new QuantityType<>(0.25, AirqUnits.MILLIGRAM_PER_CUBICMETRE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "co_maxerr"), new DecimalType(0.25));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "so2"), new DecimalType(7.5));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "so2_maxerr"), new DecimalType(1.5));
        assertThat(
                mockingDetails(callback).getInvocations().stream()
                        .filter(invocation -> invocation.getMethod().getName().equals("stateUpdated"))
                        .map(invocation -> invocation.getArgument(0, ChannelUID.class)).toList(),
                not(hasItem(new ChannelUID(thingUID, "temperature"))));
    }

    @Test
    void retainedFlatTimeChannelsPreserveLegacyRepresentationsAndRecover() throws Exception {
        Thing legacyThing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID)
                .withChannels(ChannelBuilder.create(new ChannelUID(thingUID, "nightModeStartDay"), "String").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "nightModeStartNight"), "String").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "averagingRhythm"), "Number").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "uptime"), "Number:Time").build())
                .build();

        readConfiguration(legacyThing, "{\"NightMode\": {\"StartDay\": \"24:00\", \"StartNight\": \"invalid\"}}");
        for (String channel : List.of("nightModeStartDay", "nightModeStartNight")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel), UnDefType.UNDEF);
        }
        readConfiguration(legacyThing,
                "{\"SecondsMeasurementDelay\": 5, \"NightMode\": {\"StartDay\": \"08:00\", \"StartNight\": \"23:59\"}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "nightModeStartDay"),
                new StringType("08:00"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "nightModeStartNight"),
                new StringType("23:59"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "averagingRhythm"), new DecimalType(5));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-start-day"),
                new QuantityType<>(480, Units.MINUTE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#averaging-rhythm"),
                new QuantityType<>(5, Units.SECOND));

        poll(legacyThing, "{\"uptime\": 123}");
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "uptime"),
                new QuantityType<>(123, Units.SECOND));
    }

    @Test
    void renamedMeasurementChannelsKeepOriginalFlatIds() throws Exception {
        Thing legacyThing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID).withChannels(
                ChannelBuilder.create(new ChannelUID(thingUID, "humidityRelative"), "Number").build(),
                ChannelBuilder.create(new ChannelUID(thingUID, "humidityRelative_maxerr"), "Number").build(),
                ChannelBuilder.create(new ChannelUID(thingUID, "measurements#humidityRelative"), "Number").build(),
                ChannelBuilder.create(new ChannelUID(thingUID, "maxerr#humidityRelative_maxerr"), "Number").build(),
                ChannelBuilder.create(new ChannelUID(thingUID, "measurements#humidity-relative"), "Number").build(),
                ChannelBuilder.create(new ChannelUID(thingUID, "maxerr#humidity-relative-maxerr"), "Number").build())
                .build();

        poll(legacyThing, "{\"humidity\": [45, 0.5]}");

        for (String channel : List.of("measurements#humidityRelative", "measurements#humidity-relative")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel),
                    new QuantityType<>(45, Units.PERCENT));
        }
        for (String channel : List.of("maxerr#humidityRelative_maxerr", "maxerr#humidity-relative-maxerr")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel),
                    new QuantityType<>(0.5, Units.PERCENT));
        }
        // plain Number flat channels keep receiving the number
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "humidityRelative"),
                new DecimalType(45));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "humidityRelative_maxerr"),
                new DecimalType(0.5));
    }

    @ParameterizedTest
    @CsvSource({ "wifiInfo, WifiInfo", "settings#wifi-info, WifiInfo", "fireAlarm, FireAlarm",
            "settings#fire-alarm-enabled, FireAlarm", "cloudUpload, cloudUpload",
            "settings#cloud-upload, cloudUpload" })
    void renamedCommandsAndLegacyCommandsUseOriginalDeviceKeys(String channel, String key) throws Exception {
        JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), OnOffType.ON));

        assertThat(settings.size(), is(1));
        assertThat(settings.get(key).getAsBoolean(), is(true));
    }

    @ParameterizedTest
    @CsvSource({ "settings#night-mode-brightness-day, BrightnessDay",
            "settings#night-mode-brightness-night, BrightnessNight", "nightModeBrightnessDay, BrightnessDay",
            "nightModeBrightnessNight, BrightnessNight", "settings#nightModeBrightnessDay, BrightnessDay",
            "settings#nightModeBrightnessNight, BrightnessNight" })
    void nightModeBrightnessCommandsUseNumericDeviceSettings(String channel, String key) throws Exception {
        // deviceconfig.md: int, 0 = LEDs off, 1 to 10
        for (int level : List.of(0, 6, 10)) {
            for (Command command : List.of(new DecimalType(level), new DecimalType(level + ".0"),
                    new StringType(String.valueOf(level)), new QuantityType<>(level, Units.ONE))) {
                JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), command));

                assertThat(settings, is(json("{\"NightMode\": {\"" + key + "\": " + level + "}}")));
                assertThat(settings.getAsJsonObject("NightMode").get(key).getAsJsonPrimitive().getAsString(),
                        is(String.valueOf(level)));
            }
        }
    }

    @ParameterizedTest
    @CsvSource({ "settings#night-mode-fan-night-off, FanNightOff", "settings#night-mode-wifi-night-off, WifiNightOff",
            "nightModeFanNightOff, FanNightOff", "nightModeWifiNightOff, WifiNightOff",
            "settings#nightModeFanNightOff, FanNightOff", "settings#nightModeWifiNightOff, WifiNightOff",
            // deviceconfig.md: NightMode {Activated: bool, AlarmNightOff: bool}
            "settings#night-mode-activated, Activated", "settings#night-mode-alarm-night-off, AlarmNightOff" })
    void nightModeSwitchCommandsUseBooleanDeviceSettings(String channel, String key) throws Exception {
        for (OnOffType command : List.of(OnOffType.ON, OnOffType.OFF)) {
            JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), command));
            JsonObject expectedNightMode = new JsonObject();
            expectedNightMode.addProperty(key, command == OnOffType.ON);
            JsonObject expected = new JsonObject();
            expected.add("NightMode", expectedNightMode);

            assertThat(settings, is(expected));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "settings#night-mode-brightness-day", "settings#night-mode-brightness-night" })
    void invalidBrightnessCommandsDoNotSendRequests(String channel) throws Exception {
        for (Command command : List.of(OnOffType.ON, new StringType("invalid"), new StringType("NaN"),
                new StringType("Infinity"), new StringType(""), new QuantityType<>(2.5, Units.SECOND),
                new DecimalType("2.5"), new StringType("2.5"), new QuantityType<>(2.5, Units.ONE), new DecimalType(-1),
                new DecimalType(11), new QuantityType<>(250, Units.PERCENT), new DecimalType(2147483648L))) {
            assertThat(sendSettings(new ChannelUID(thingUID, channel), command), nullValue());
        }
    }

    static Stream<Arguments> settingCommands() {
        Stream<Arguments> switches = Stream.of(new String[] { "wifi-info", "WifiInfo" },
                new String[] { "fire-alarm-enabled", "FireAlarm" }, new String[] { "cloud-upload", "cloudUpload" },
                new String[] { "auto-drift-compensation", "AutoDriftCompensation" },
                new String[] { "advanced-data-processing", "AdvancedDataProcessing" },
                new String[] { "gas-alarm-enabled", "GasAlarm" },
                new String[] { "alarm-forwarding", "AlarmForwarding" }, new String[] { "ppm-and-ppb", "ppm&ppb" },
                new String[] { "night-mode-fan-night-off", "FanNightOff" },
                new String[] { "night-mode-wifi-night-off", "WifiNightOff" })
                .flatMap(mapping -> Stream.of(OnOffType.ON, OnOffType.OFF).map(command -> {
                    JsonObject value = new JsonObject();
                    value.addProperty(mapping[1], command == OnOffType.ON);
                    JsonObject settings = value;
                    if (mapping[0].startsWith("night-mode-")) {
                        settings = new JsonObject();
                        settings.add("NightMode", value);
                    }
                    return Arguments.of(mapping[0], command, settings);
                }));
        Stream<Arguments> strings = Stream.of(
                Arguments.of("time-server", new StringType("time.example"), json("{\"TimeServer\":\"time.example\"}")),
                Arguments.of("room-type", new StringType("office"), json("{\"RoomType\":\"office\"}")),
                Arguments.of("log-level", new StringType("Error"), json("{\"logging\":\"Error\"}")),
                Arguments.of("log-level", new StringType("Warning"), json("{\"logging\":\"Warning\"}")),
                Arguments.of("log-level", new StringType("Info"), json("{\"logging\":\"Info\"}")),
                Arguments.of("power-freq-suppression", new StringType("50Hz"), json("{\"Rejection\":\"50Hz\"}")),
                Arguments.of("power-freq-suppression", new StringType("60Hz"), json("{\"Rejection\":\"60Hz\"}")),
                Arguments.of("power-freq-suppression", new StringType("50Hz+60Hz"),
                        json("{\"Rejection\":\"50Hz+60Hz\"}")),
                Arguments.of("building-standard", new StringType("None"), json("{\"BuildingStandard\":\"None\"}")),
                Arguments.of("building-standard", new StringType("WELL"), json("{\"BuildingStandard\":\"WELL\"}")),
                Arguments.of("building-standard", new StringType("RESET"), json("{\"BuildingStandard\":\"RESET\"}")),
                Arguments.of("country-code", new StringType("DE"), json("{\"country_code\":\"DE\"}")),
                Arguments.of("country-code", new StringType("US"), json("{\"country_code\":\"US\"}")));
        return Stream.concat(Stream.concat(switches, strings),
                Stream.of(Arguments.of("location", new PointType(new DecimalType(50), new DecimalType(10)),
                        json("{\"geopos\":{\"lat\":50,\"long\":10}}"))));
    }

    @ParameterizedTest
    @MethodSource("settingCommands")
    void settingsCommandsEncodeOnlyTheirDeviceSetting(String channel, Command command, JsonObject expected)
            throws Exception {
        assertThat(sendSettings(channel, command), is(expected));
    }

    @ParameterizedTest
    @MethodSource("settingCommands")
    void settingsCommandsIgnoreWrongDataTypes(String channel, Command accepted, JsonObject expected) throws Exception {
        for (Command command : List.of(OnOffType.ON, new StringType("invalid"), new DecimalType(1),
                new QuantityType<>(1, Units.SECOND), new PointType(new DecimalType(50), new DecimalType(10)),
                RefreshType.REFRESH)) {
            if (!accepted.getClass().equals(command.getClass())) {
                assertThat(channel + " must reject " + command.getClass().getSimpleName(),
                        sendSettings(channel, command), nullValue());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "settings#warmup-phase", "warmupPhase" })
    void warmupPhaseOffAbortsTheWarmup(String channel) throws Exception {
        // measurement.md: {"warmup-phase": false} aborts a running warm-up phase
        JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), OnOffType.OFF));

        assertThat(settings, is(json("{\"warmup-phase\": false}")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "settings#warmup-phase", "warmupPhase" })
    void warmupPhaseCannotBeStarted(String channel) throws Exception {
        assertThat(sendSettings(new ChannelUID(thingUID, channel), OnOffType.ON), nullValue());
        assertThat(sendSettings(new ChannelUID(thingUID, channel), new StringType("OFF")), nullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = { "wifi", "unknown", "delete-key", "error-bars", "errorBars", "initial-cal-finished",
            "sound-info", "daytime" })
    void unwritableOrUnknownChannelsDoNotSendSettings(String channel) throws Exception {
        for (Command command : List.of(OnOffType.ON, OnOffType.OFF, new StringType("value"), new DecimalType(1),
                new QuantityType<>(1, Units.SECOND), RefreshType.REFRESH)) {
            assertThat(sendSettings(channel, command), nullValue());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "night-mode-start-day", "night-mode-start-night", "averaging-rhythm",
            "night-mode-brightness-day", "night-mode-brightness-night" })
    void numericSettingsRejectSwitchLocationAndRefreshCommands(String channel) throws Exception {
        for (Command command : List.of(OnOffType.ON, OnOffType.OFF,
                new PointType(new DecimalType(50), new DecimalType(10)), RefreshType.REFRESH)) {
            assertThat(sendSettings(channel, command), nullValue());
        }
    }

    static Stream<Arguments> integerSettingCommands() {
        return Stream.of(Arguments.of("settings#auto-update", new DecimalType(30), json("{\"AutoUpdate\":30}")),
                Arguments.of("settings#auto-update", new DecimalType(0), json("{\"AutoUpdate\":0}")),
                Arguments.of("settings#auto-update", new QuantityType<>(2, Units.WEEK), json("{\"AutoUpdate\":14}")),
                Arguments.of("settings#auto-update", OnOffType.ON, json("{\"AutoUpdate\":90}")),
                Arguments.of("autoUpdate", OnOffType.ON, json("{\"AutoUpdate\":90}")),
                Arguments.of("autoUpdate", OnOffType.OFF, json("{\"AutoUpdate\":0}")),
                Arguments.of("settings#averaging", new DecimalType(10), json("{\"Averaging\":10}")),
                Arguments.of("settings#averaging", new DecimalType(1), json("{\"Averaging\":1}")),
                Arguments.of("settings#averaging", new QuantityType<>(5, Units.ONE), json("{\"Averaging\":5}")),
                Arguments.of("averaging", OnOffType.ON, json("{\"Averaging\":30}")),
                Arguments.of("averaging", OnOffType.OFF, json("{\"Averaging\":1}")));
    }

    @ParameterizedTest
    @MethodSource("integerSettingCommands")
    void integerSettingCommandsSendDeviceIntegers(String channel, Command command, JsonObject expected)
            throws Exception {
        assertThat(sendSettings(new ChannelUID(thingUID, channel), command), is(expected));
    }

    @ParameterizedTest
    @CsvSource({ "auto-update, -1", "auto-update, 1.5", "auto-update, 36 h", "auto-update, 2 s", "averaging, 0",
            "averaging, 2.5", "averaging, 1 s" })
    void invalidIntegerSettingCommandsDoNotSendRequests(String channel, String value) throws Exception {
        Command command = value.contains(" ") ? QuantityType.valueOf(value) : new DecimalType(value);

        assertThat(sendSettings(channel, command), nullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = { "auto-update", "averaging" })
    void integerSettingsRejectStringLocationAndRefreshCommands(String channel) throws Exception {
        for (Command command : List.of(new StringType("30"), new PointType(new DecimalType(50), new DecimalType(10)),
                RefreshType.REFRESH)) {
            assertThat(sendSettings(channel, command), nullValue());
        }
    }

    @Test
    void retainedFlatIntegerSettingChannelsStaySwitches() throws Exception {
        Thing legacyThing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID)
                .withChannels(ChannelBuilder.create(new ChannelUID(thingUID, "autoUpdate"), "Switch").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "averaging"), "Switch").build())
                .build();

        readConfiguration(legacyThing, "{\"AutoUpdate\": 0, \"Averaging\": 1}");
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "autoUpdate"), OnOffType.OFF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "averaging"), OnOffType.OFF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#auto-update"),
                new QuantityType<>(0, Units.DAY));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#averaging"),
                new DecimalType(1));

        readConfiguration(legacyThing, "{\"AutoUpdate\": 90, \"Averaging\": 30}");
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "autoUpdate"), OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "averaging"), OnOffType.ON);
    }

    static Stream<Arguments> measurements() {
        return Stream.concat(
                Stream.of("c2h4o", "nh3_MR100", "ash3", "br2", "ch4s", "cl2_M20", "clo2", "cs2", "ethanol", "c2h4",
                        "ch2o_M10", "f2", "hcl", "hcn", "hf", "h2_M1000", "h2o2", "n2o", "no_M250", "ph3", "sih4")
                        .map(key -> Arguments.of(key, Units.MICROGRAM_PER_CUBICMETRE)),
                Stream.of(Arguments.of("acid_M100", Units.PARTS_PER_BILLION),
                        Arguments.of("tvoc_ionsc", Units.PARTS_PER_BILLION),
                        Arguments.of("pressure_rel", MetricPrefix.HECTO(SIUnits.PASCAL)),
                        Arguments.of("sound_max", Units.DECIBEL), Arguments.of("ch4_MIPEX", Units.PERCENT),
                        Arguments.of("c3h8_MIPEX", Units.PERCENT), Arguments.of("r32", Units.PERCENT),
                        Arguments.of("r454b", Units.PERCENT), Arguments.of("r454c", Units.PERCENT)));
    }

    @ParameterizedTest
    @MethodSource("measurements")
    void newChannelMetadataMatchesMappedUnitsAndUpgrade(String key, Unit<?> unit) throws Exception {
        String channel = key.toLowerCase(Locale.ROOT).replace('_', '-');
        Document metadata = xml("thing/thing-types.xml");
        Document upgrades = xml("update/update.xml");
        for (String[] groupedChannel : new String[][] { { "measurements", channel, "advanced-measurements" },
                { "maxerr", channel + "-maxerr", "advanced-maxerr" } }) {
            String groupId = groupedChannel[0];
            String id = groupedChannel[1];
            Element definition = element(metadata,
                    "//channel-group-type[@id='" + groupId + "']/channels/channel[@id='" + id + "']");
            String typeId = definition.getAttribute("typeId");
            assertThat(id, matchesPattern("[a-z0-9]+(?:-[a-z0-9]+)*"));
            assertThat(typeId, matchesPattern("[a-z0-9]+(?:-[a-z0-9]+)*"));
            Element type = element(metadata, "//channel-type[@id='" + typeId + "']");
            assertThat(type.getAttribute("advanced"), is("true"));
            assertThat(element(type, "state").getAttribute("readOnly"), is("true"));
            String hint = element(type, "item-type").getAttribute("unitHint");
            assertThat(QuantityType.valueOf("1 " + hint).getUnit(), is(unit));
            Element upgrade = element(upgrades, "//instruction-set[@targetVersion='5']/add-channel[@id='" + id + "']");
            assertThat(upgrade.getAttribute("groupIds"), is(groupedChannel[2]));
            assertThat(element(upgrade, "type").getTextContent(), is("airq:" + typeId));
            // version 6 moves the channel from its group of version 5
            Element move = element(upgrades, "//instruction-set[@targetVersion='6']/add-channel[@id='" + id
                    + "' and @groupIds='" + groupId + "']");
            assertThat(element(move, "type").getTextContent(), is("airq:" + typeId));
        }
    }

    @Test
    void allChannelsAreGroupedAndExistingThingsAreMigrated() throws Exception {
        Document metadata = xml("thing/thing-types.xml");
        Document upgrades = xml("update/update.xml");
        assertThat(element(metadata, "//thing-type[@id='airq']/properties/property[@name='thingTypeVersion']")
                .getTextContent(), is("6"));
        String[] groupIds = { "status", "measurements", "events", "maxerr", "settings" };
        List<Element> channels = new ArrayList<>();
        for (String groupId : groupIds) {
            Element group = findElement(metadata, "//channel-group-type[@id='" + groupId + "']");
            assertThat("Missing channel group " + groupId, group, notNullValue());
            group = requireNonNull(group);
            assertThat(group.hasAttribute("advanced"), is(false));
            List<Element> groupChannels = elements(group, "channels/channel");
            List<String> channelIds = groupChannels.stream().map(channel -> channel.getAttribute("id")).toList();
            assertThat(channelIds.stream().distinct().count(), is((long) channelIds.size()));
            for (Element channel : groupChannels) {
                assertThat(channel.getAttribute("id"), matchesPattern("[a-z0-9]+(?:-[a-z0-9]+)*"));
                assertThat(channel.getAttribute("typeId"), matchesPattern("[a-z0-9]+(?:-[a-z0-9]+)*"));
            }
            channels.addAll(groupChannels);
            assertThat(findElement(metadata,
                    "//thing-type/channel-groups/channel-group[@id='" + groupId + "' and @typeId='" + groupId + "']"),
                    notNullValue());
        }
        List<Element> channelTypes = elements(metadata, "//channel-type");
        List<String> typeIds = channelTypes.stream().map(type -> type.getAttribute("id")).toList();
        assertThat(typeIds.stream().distinct().count(), is((long) typeIds.size()));
        for (Element instructionType : elements(upgrades, "//instruction-set/*/type")) {
            String typeId = instructionType.getTextContent().substring("airq:".length());
            assertThat("Missing historical channel type " + typeId,
                    findElement(metadata, "//channel-type[@id='" + typeId + "']"), notNullValue());
        }

        assertThat(elements(upgrades, "//instruction-set[@targetVersion='5']/remove-channel"), empty());
        assertThat(elements(upgrades, "//instruction-set[@targetVersion='5']/update-channel"), empty());
        List<String> changes = new ArrayList<>();
        for (Element instruction : elements(upgrades, "//instruction-set[@targetVersion='6']/*")) {
            Element type = findElement(instruction, "type");
            changes.add(instruction.getTagName() + " " + instruction.getAttribute("groupIds") + "#"
                    + instruction.getAttribute("id") + (type != null ? " " + type.getTextContent() : ""));
        }
        // version 6 changes the item types of channels that keep their group, moves the channels of version 5 to the
        // groups status, measurements, events, maxerr and settings, and adds the new channels
        assertThat(changes, hasSize(255));
        assertThat(changes, hasItems("update-channel measurements#so2 airq:so2", "update-channel #so2 airq:so2-number",
                "update-channel measurements#health airq:health-raw",
                "update-channel measurements#performance airq:performance-raw",
                "update-channel maxerr#co-maxerr airq:co-maxerr", "remove-channel general#sound-pressure",
                "remove-channel general#password", "remove-channel #password", "remove-channel general#delete-key",
                "remove-channel #deleteKey", "remove-channel general#error-bars", "remove-channel #errorBars",
                "remove-channel general#auto-update", "add-channel settings#auto-update airq:auto-update",
                "remove-channel general#status", "add-channel status#status airq:status",
                "remove-channel measurements#door-event", "add-channel events#door-event airq:door",
                "remove-channel advanced-measurements#c2h4o", "add-channel measurements#c2h4o airq:c2h4o",
                "remove-channel advanced-maxerr#c2h4o-maxerr", "add-channel maxerr#c2h4o-maxerr airq:density-maxerr",
                "add-channel events#person airq:person", "add-channel settings#country-code airq:country-code"));
        // only removals refer to the groups of version 5
        assertThat(changes.stream().filter(change -> !change.startsWith("remove-channel"))
                .filter(change -> Stream.of("general#", "advanced-measurements#", "advanced-maxerr#")
                        .anyMatch(group -> change.contains(" " + group)))
                .toList(), empty());
        assertThat(elements(upgrades, "//instruction-set[@targetVersion='7']"), empty());

        List<Element> additions = elements(upgrades, "//instruction-set[@targetVersion='5']/add-channel");
        assertThat(additions, hasSize(162));
        // channels after applying the instructions of version 5 and version 6
        Map<String, String> upgraded = new LinkedHashMap<>();
        for (Element addition : additions) {
            upgraded.put(addition.getAttribute("groupIds") + "#" + addition.getAttribute("id"),
                    element(addition, "type").getTextContent());
        }
        assertThat(upgraded.size(), is(additions.size()));
        for (Element instruction : elements(upgrades, "//instruction-set[@targetVersion='6']/*")) {
            String channel = instruction.getAttribute("groupIds") + "#" + instruction.getAttribute("id");
            if (instruction.getAttribute("groupIds").isEmpty()) {
                // a retained flat channel of thing type version 4, which is not part of the grouped channels
                assertThat(channel, instruction.getTagName(),
                        is("#so2".equals(channel) ? "update-channel" : "remove-channel"));
            } else if ("remove-channel".equals(instruction.getTagName())) {
                assertThat(upgraded.remove(channel), notNullValue());
            } else {
                // add-channel and update-channel
                assertThat(channel, "add-channel".equals(instruction.getTagName()), is(!upgraded.containsKey(channel)));
                upgraded.put(channel, element(instruction, "type").getTextContent());
            }
        }
        assertThat(upgraded.size(), is(channels.size()));
        for (Entry<String, String> channel : upgraded.entrySet()) {
            String[] id = channel.getKey().split("#");
            Element definition = element(metadata,
                    "//channel-group-type[@id='" + id[0] + "']/channels/channel[@id='" + id[1] + "']");
            assertThat(channel.getValue(), is("airq:" + definition.getAttribute("typeId")));
        }
        assertThat(channels, hasSize(191));
    }

    @SuppressWarnings("null")
    @Test
    void commandsUseTheGroupLessChannelId() throws Exception {
        AirqHandler commandHandler = requireNonNull(spy(handler));
        commandHandler.config.password = "secret";
        JsonObject response = new JsonObject();
        response.addProperty("content",
                commandHandler.encrypt("{}".getBytes(StandardCharsets.UTF_8), commandHandler.config.password));
        doReturn(new AirqHandler.Result(response.toString(), 200)).when(commandHandler).getData(anyString(), eq("POST"),
                anyString());

        commandHandler.handleCommand(new ChannelUID(thingUID, "settings#wifi-info"), OnOffType.ON);

        requireNonNull(verify(commandHandler)).getData(anyString(), eq("POST"), anyString());
    }

    @ParameterizedTest
    @CsvSource({ "seconds-measurement-delay, s", "mtime, ms", "uptime, s", "nightmode-start-day, min",
            "nightmode-start-night, min", "auto-update, d" })
    void timeChannelTypesHaveMatchingUnitHints(String typeId, String unit) throws Exception {
        Element itemType = element(xml("thing/thing-types.xml"), "//channel-type[@id='" + typeId + "']/item-type");

        assertThat(itemType.getTextContent(), is("Number:Time"));
        assertThat(itemType.getAttribute("unitHint"), is(unit));
    }

    @ParameterizedTest
    @CsvSource({ "nightmode-brightness-day, Number", "nightmode-brightness-night, Number",
            "nightmodeBrightnessDay, Number:Dimensionless", "nightmodeBrightnessNight, Number:Dimensionless" })
    void brightnessChannelTypesAreLimitedToDeviceLevels(String typeId, String itemType) throws Exception {
        // deviceconfig.md: BrightnessDay and BrightnessNight are int, 0 to 10; the released flat channels keep
        // their item type
        Document thingTypes = xml("thing/thing-types.xml");
        Element state = element(thingTypes, "//channel-type[@id='" + typeId + "']/state");

        assertThat(element(thingTypes, "//channel-type[@id='" + typeId + "']/item-type").getTextContent(),
                is(itemType));
        assertThat(state.getAttribute("min"), is("0"));
        assertThat(state.getAttribute("max"), is("10"));
        assertThat(state.getAttribute("step"), is("1"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "[\"WegerLAN\"]|WegerLAN", "[\"WegerLAN\", \"Gast\"]|WegerLAN, Gast",
            // all networks deleted
            "[]|''",
            // entries that are not names are skipped
            "[\"a\", 5, {\"x\": 1}, null, [\"y\"], \"b\"]|a, b" })
    void configuredWifiNetworksAreListed(String networks, String shown) throws Exception {
        readConfiguration("{\"WLANssid\": " + networks + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#ssid"), new StringType(shown));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"WegerLAN\": \"key\"}", "\"WegerLAN\"", "5" })
    void configuredWifiNetworksInAnotherFormAreUndefined(String networks) throws Exception {
        // GET /config returns a list of names, the object with the keys is the SD card format
        readConfiguration("{\"WLANssid\": " + networks + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#ssid"), UnDefType.UNDEF);
        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, "settings#ssid")),
                argThat(state -> state.toString().contains("key")));
    }

    @Test
    void userCalibrationsListTheOffsetAndTimeOfEachSensor() throws Exception {
        // calibration.md: /usercalibration adds a timestamp to the sensor's usercalib entry, deleting it stores 0
        readConfiguration(
                "{\"usercalib\": {\"so2\": {\"offset\": 12.5, \"timestamp\": 1793046696}, \"co\": {\"offset\": 0, \"timestamp\": 1793050296}}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#user-calib"),
                new StringType("so2: offset=12.5 [" + calibrationTime(1793046696) + "], co: offset=0 ["
                        + calibrationTime(1793050296) + "]"));
    }

    @Test
    void userCalibrationsWithoutTimestampShowTheOffset() throws Exception {
        readConfiguration("{\"usercalib\": {\"so2\": {\"offset\": -3}}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#user-calib"),
                new StringType("so2: offset=-3"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "{}|''",
            // entries without a numeric offset are skipped
            "{\"so2\": 5, \"co\": {\"timestamp\": 1}, \"no2\": {\"offset\": \"x\"}, \"o3\": {\"offset\": 2}}|o3: offset=2" })
    void userCalibrationEntriesThatAreNotCalibrationsAreSkipped(String usercalib, String shown) throws Exception {
        readConfiguration("{\"usercalib\": " + usercalib + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#user-calib"),
                new StringType(shown));
    }

    @ParameterizedTest
    @ValueSource(strings = { "[\"so2\"]", "\"so2\"", "5" })
    void userCalibrationsInAnotherFormAreUndefinedAndTheConfigurationIsStillRead(String usercalib) throws Exception {
        readConfiguration("{\"usercalib\": " + usercalib + ", \"InitialCalFinished\": true}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#user-calib"), UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#initial-cal-finished"),
                OnOffType.ON);
    }

    private static String calibrationTime(long epochSeconds) {
        return new java.text.SimpleDateFormat("dd.MM.yyyy' 'HH:mm:ss").format(new java.util.Date(epochSeconds * 1000));
    }

    @Test
    void nightModeSettingsAreReadAlsoWhenTheNightModeIsOff() throws Exception {
        // deviceconfig.md: the example of the night mode settings, and whether the device is in day operation
        readConfiguration("""
                {"NightMode": {"Activated": false, "StartDay": "07:00", "StartNight": "21:00", "BrightnessDay": 6.0,
                               "BrightnessNight": 6.0, "FanNightOff": false, "WifiNightOff": false,
                               "AlarmNightOff": true},
                 "daytime": true}
                """);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-activated"),
                OnOffType.OFF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-alarm-night-off"),
                OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#daytime"), OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-start-day"),
                new QuantityType<>(420, Units.MINUTE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-start-night"),
                new QuantityType<>(1260, Units.MINUTE));
    }

    @Test
    void nightModeWithoutActivatedKeyIsUsed() throws Exception {
        // deviceconfig.md: firmware before 1.80 has no Activated key, it is handled as true
        readConfiguration("{\"NightMode\": {\"StartDay\": \"07:00\"}, \"daytime\": false}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-activated"),
                OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#daytime"), OnOffType.OFF);
    }

    @ParameterizedTest
    @CsvSource({ "0, 00:00 UTC", "420, 07:00 UTC", "1260, 21:00 UTC", "1439, 23:59 UTC" })
    void nightModeTimesAreShownInUtc(int minutes, String shown) throws Exception {
        // deviceconfig.md: the night mode times are UTC
        for (String typeId : List.of("nightmode-start-day", "nightmode-start-night")) {
            String pattern = element(xml("thing/thing-types.xml"), "//channel-type[@id='" + typeId + "']/state")
                    .getAttribute("pattern");

            assertThat(new QuantityType<>(minutes, Units.MINUTE).format(pattern), is(shown));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "nightmodeStartDay", "nightmodeStartNight" })
    void retainedFlatNightModeTimesStayStrings(String typeId) throws Exception {
        // thing type version 4 published HH:mm strings
        Document thingTypes = xml("thing/thing-types.xml");

        assertThat(element(thingTypes, "//channel-type[@id='" + typeId + "']/item-type").getTextContent(),
                is("String"));
        assertThat(findElement(thingTypes, "//channel-type[@id='" + typeId + "']/state"), nullValue());
    }

    @ParameterizedTest
    @CsvSource({ "maxerr, measurements, 56" })
    void errorChannelsHaveTheItemTypeAndUnitOfTheirMeasurement(String errorGroup, String measurementGroup,
            int channelCount) throws Exception {
        // datenlesen.md: [value, error] is value ± error in the same unit
        Document thingTypes = xml("thing/thing-types.xml");
        List<Element> errorChannels = elements(thingTypes,
                "//channel-group-type[@id='" + errorGroup + "']/channels/channel");
        assertThat(errorChannels, hasSize(channelCount));
        for (Element errorChannel : errorChannels) {
            String measurementId = errorChannel.getAttribute("id").replace("-maxerr", "");
            Element measurement = element(thingTypes, "//channel-group-type[@id='" + measurementGroup
                    + "']/channels/channel[@id='" + measurementId + "']");
            Element errorType = element(thingTypes,
                    "//channel-type[@id='" + errorChannel.getAttribute("typeId") + "']/item-type");
            Element valueType = element(thingTypes,
                    "//channel-type[@id='" + measurement.getAttribute("typeId") + "']/item-type");

            assertThat(measurementId, errorType.getTextContent(), is(valueType.getTextContent()));
            // temperature errors are differences in kelvin
            assertThat(measurementId, errorType.getAttribute("unitHint"),
                    is("Number:Temperature".equals(valueType.getTextContent()) ? "K"
                            : valueType.getAttribute("unitHint")));
            assertThat(measurementId,
                    element(thingTypes, "//channel-type[@id='" + errorChannel.getAttribute("typeId") + "']/state")
                            .getAttribute("pattern"),
                    not(containsString("%%")));
        }
    }

    @Test
    void retainedFlatErrorChannelsShowTheUnitOfTheirNumber() throws Exception {
        // the flat error channels keep their item type and receive the number in the unit of the grouped channel
        Document thingTypes = xml("thing/thing-types.xml");
        List<Element> flatTypes = elements(thingTypes, "//channel-type[contains(@id, '_maxerr')]");
        assertThat(flatTypes, hasSize(27));
        for (Element flatType : flatTypes) {
            String typeId = flatType.getAttribute("id");
            String groupedId = typeId.replace("_maxerr", "-maxerr").replace('_', '-');
            Element grouped = element(thingTypes, "//channel-type[@id='" + groupedId + "']");
            String unit = element(grouped, "item-type").getAttribute("unitHint");
            String expected = element(grouped, "state").getAttribute("pattern").replace(" %unit%",
                    unit.isEmpty() ? "" : " " + ("%".equals(unit) ? "%%" : unit));

            assertThat(typeId, element(flatType, "item-type").getTextContent(), is("Number:Dimensionless"));
            assertThat(typeId, element(flatType, "state").getAttribute("pattern"), is(expected));
        }
    }

    @Test
    void retainedFlatSo2ChannelShowsTheUnitOfItsNumber() throws Exception {
        // thing type version 4: so2 is a plain Number in µg/m³, the grouped channel is Number:Density
        Document thingTypes = xml("thing/thing-types.xml");
        Element flatType = element(thingTypes, "//channel-type[@id='so2-number']");
        Element grouped = element(thingTypes, "//channel-type[@id='so2']");

        assertThat(element(flatType, "item-type").getTextContent(), is("Number"));
        assertThat(element(flatType, "item-type").getAttribute("unitHint"), is(""));
        assertThat(element(flatType, "state").getAttribute("pattern"), is(element(grouped, "state")
                .getAttribute("pattern").replace("%unit%", element(grouped, "item-type").getAttribute("unitHint"))));
        assertThat(element(flatType, "label").getTextContent(), is(element(grouped, "label").getTextContent()));
    }

    @Test
    void unitsFollowTheSpecification() throws Exception {
        Document thingTypes = xml("thing/thing-types.xml");

        // datenlesen.md: SO₂ in µg/m³, dCO2dt in ppb/s, dHdt in mg/m³/s
        assertThat(element(thingTypes, "//channel-type[@id='so2']/item-type").getTextContent(), is("Number:Density"));
        assertThat(element(thingTypes, "//channel-type[@id='so2']/item-type").getAttribute("unitHint"), is("µg/m³"));
        assertThat(element(thingTypes, "//channel-type[@id='dco2dt']/state").getAttribute("pattern"), is("%.2f ppb/s"));
        assertThat(element(thingTypes, "//channel-type[@id='dhdt']/state").getAttribute("pattern"), is("%.2f mg/m³/s"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/main/resources/OH-INF/thing/thing-types.xml",
            "src/main/resources/OH-INF/i18n/airq.properties", "src/main/resources/OH-INF/i18n/airq_de.properties",
            "src/main/resources/OH-INF/i18n/airq_it.properties", "README.md" })
    void unitsUseTheMicroSign(String file) throws Exception {
        // the micro sign U+00B5, as in openHAB's µg/m³, not the Greek letter mu U+03BC
        assertThat(Files.readString(Path.of(file), StandardCharsets.UTF_8), not(containsString("μ")));
    }

    @Test
    void readmeDescribesEachChannelLikeItsChannelType() throws Exception {
        // | channel | type | rw | description |, the maximum errors are described together
        Map<String, List<String>> rows = new LinkedHashMap<>();
        for (String line : Files.readAllLines(Path.of("README.md"), StandardCharsets.UTF_8)) {
            String[] cells = line.split("\\|");
            if (cells.length == 5 && cells[0].isEmpty() && cells[1].trim().matches("[a-z0-9-]+")) {
                rows.put(cells[1].trim(), List.of(cells[2].trim(), cells[4].trim()));
            }
        }
        Document thingTypes = xml("thing/thing-types.xml");
        for (String group : List.of("status", "measurements", "events", "settings")) {
            for (Element channel : elements(thingTypes, "//channel-group-type[@id='" + group + "']/channels/channel")) {
                String id = channel.getAttribute("id");
                Element type = element(thingTypes, "//channel-type[@id='" + channel.getAttribute("typeId") + "']");
                Element description = findElement(type, "description");
                String text = (description != null ? description : element(type, "label")).getTextContent()
                        .replaceAll("\\s+", " ").replaceAll("\\.$", "");

                assertThat(id, rows.get(id), is(List.of(element(type, "item-type").getTextContent(), text)));
            }
        }
    }

    @Test
    void newUpdateInstructionsUseTheMicroSign() throws Exception {
        // released instruction sets (up to version 4) must not be changed and keep their labels
        List<Element> instructionSets = elements(xml("update/update.xml"), "//instruction-set[@targetVersion >= 5]");
        assertThat(instructionSets, hasSize(2));
        for (Element instructionSet : instructionSets) {
            assertThat(instructionSet.getAttribute("targetVersion"), instructionSet.getTextContent(),
                    not(containsString("μ")));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "degC", "degF", "K", "hPa", "kPa", "mbar", "bar", "psi", "%", "mg/m^3", "ug/m^3", "ppm",
            "ppb", "Bq/m^3", "pCi/L" })
    void allUnitsOfTheSpecificationAreKnown(String unit) {
        // measurement.md: the possible units of the "units" setting
        assertThat(AirqUnits.deviceUnit(unit), notNullValue());
    }

    // deviceconfig.md: the room types air-Q accepts for RoomType
    private static final List<String> ROOM_TYPES = List.of("living-room", "bedroom", "kitchen", "bathroom", "office",
            "workshop", "children", "toilet", "hallway", "cellar", "attic", "outdoor", "garage", "medical", "classroom",
            "other");

    @ParameterizedTest
    @ValueSource(strings = { "room-type", "RoomType" })
    void roomTypeChannelTypesOfferTheDeviceRoomTypes(String typeId) throws Exception {
        List<String> options = new ArrayList<>();
        for (Element option : elements(xml("thing/thing-types.xml"),
                "//channel-type[@id='" + typeId + "']/state/options/option")) {
            options.add(option.getAttribute("value"));
        }

        assertThat(options, is(ROOM_TYPES));
    }

    // the decrypted requests of a setWifi call and its result
    private record WifiRequest(List<JsonObject> requests, boolean accepted) {
    }

    private WifiRequest setWifi(int status, String changeLog, @Nullable String ssid, @Nullable String password,
            @Nullable String bssid, boolean hidden, boolean restart) throws Exception {
        List<String> bodies = new ArrayList<>();
        JsonObject response = new JsonObject();
        response.addProperty("content", handler.encrypt(changeLog.getBytes(StandardCharsets.UTF_8), "secret"));
        AirqHandler wifiHandler = new AirqHandler(thing, requireNonNull(mock(HttpClient.class))) {
            @Override
            protected Result getData(String address, String method, @Nullable String body) {
                assertThat(address.endsWith("/config"), is(true));
                assertThat(method, is("POST"));
                bodies.add(requireNonNull(body));
                return new Result(response.toString(), status);
            }
        };
        wifiHandler.config.password = "secret";
        AirqActions actions = new AirqActions();
        actions.setThingHandler(wifiHandler);

        boolean accepted = AirqActions.setWifi(actions, ssid, password, bssid, hidden, restart);

        List<JsonObject> requests = new ArrayList<>();
        for (String body : bodies) {
            requests.add(json(wifiHandler.decrypt(body.substring("request=".length()).getBytes(StandardCharsets.UTF_8),
                    "secret")));
        }
        return new WifiRequest(requests, accepted);
    }

    static Stream<Arguments> wifiSettings() {
        // deviceconfig.md: the examples for adding a Wi-Fi network
        return Stream.of(
                Arguments.of("Your Wifi SSID", "Your Wifi key", null, false, true,
                        "{\"WiFissid\": \"Your Wifi SSID\", \"WiFipass\": \"Your Wifi key\", \"reset\": true}"),
                Arguments.of("Your Wifi SSID", "Your Wifi key", "a0c86d65ee71", false, true,
                        "{\"WiFissid\": \"Your Wifi SSID\", \"WiFipass\": \"Your Wifi key\","
                                + " \"WiFibssid\": \"a0c86d65ee71\", \"reset\": true}"),
                Arguments.of("Ihre WLAN-SSID", "Ihr WLAN-Key", null, true, true,
                        "{\"WiFissid\": \"Ihre WLAN-SSID\", \"WiFipass\": \"Ihr WLAN-Key\", \"WiFihidden\": true,"
                                + " \"reset\": true}"),
                Arguments.of("Ihre WLAN-SSID", "Ihr WLAN-Key", "A0:C8:6D:65:EE:71", true, true,
                        "{\"WiFissid\": \"Ihre WLAN-SSID\", \"WiFipass\": \"Ihr WLAN-Key\","
                                + " \"WiFibssid\": \"a0c86d65ee71\", \"WiFihidden\": true, \"reset\": true}"),
                // an unsecured network has an empty key
                Arguments.of("Open", "", "", false, true,
                        "{\"WiFissid\": \"Open\", \"WiFipass\": \"\", \"reset\": true}"),
                // further networks are remembered without a restart, "reset" may be omitted
                Arguments.of("Second", "key", null, false, false, "{\"WiFissid\": \"Second\", \"WiFipass\": \"key\"}"));
    }

    @ParameterizedTest
    @MethodSource("wifiSettings")
    void setWifiActionSendsTheWifiSettings(String ssid, String password, @Nullable String bssid, boolean hidden,
            boolean restart, String expected) throws Exception {
        WifiRequest wifi = setWifi(200, "{}", ssid, password, bssid, hidden, restart);

        assertThat(wifi.requests(), contains(json(expected)));
        assertThat(wifi.accepted(), is(true));
    }

    static Stream<Arguments> invalidWifiSettings() {
        return Stream.of(Arguments.of(null, "key", null), Arguments.of("", "key", null),
                Arguments.of("x".repeat(33), "key", null), Arguments.of("ä".repeat(17), "key", null),
                Arguments.of("ssid", null, null), Arguments.of("ssid", "key", "a0c86d65ee7"),
                Arguments.of("ssid", "key", "a0c86d65ee71ff"), Arguments.of("ssid", "key", "g0c86d65ee71"));
    }

    @ParameterizedTest
    @MethodSource("invalidWifiSettings")
    void setWifiActionRejectsInvalidSettings(@Nullable String ssid, @Nullable String password, @Nullable String bssid)
            throws Exception {
        WifiRequest wifi = setWifi(200, "{}", ssid, password, bssid, false, true);

        assertThat(wifi.requests(), empty());
        assertThat(wifi.accepted(), is(false));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "500|{}", "200|{\"WiFissid\": \"Error: invalid\"}" })
    void setWifiActionReportsRejectedSettings(int status, String changeLog) throws Exception {
        assertThat(setWifi(status, changeLog, "ssid", "key", null, false, true).accepted(), is(false));
    }

    @Test
    void setWifiActionNeedsAnAirqThing() {
        assertThat(AirqActions.setWifi(new AirqActions(), "ssid", "key", null, false, true), is(false));
    }

    @Test
    void handlerProvidesTheActions() {
        assertThat(handler.getServices(), contains(AirqActions.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "settings#ssid", "ssid", "SSID" })
    void ssidChannelIsReadOnly(String channel) throws Exception {
        // Wi-Fi networks are added with the setWifi action, the channel lists the configured networks
        assertThat(
                sendSettings(new ChannelUID(thingUID, channel), new StringType(
                        "{\"WiFissid\": \"a\", \"WiFipass\": \"b\", \"WiFibssid\": \"\", \"reset\": \"true\"}")),
                nullValue());
        assertThat(
                element(xml("thing/thing-types.xml"), "//channel-type[@id='wla-nssid']/state").getAttribute("readOnly"),
                is("true"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "settings#device-name", "deviceName" })
    void deviceNameCommandsSetTheDeviceName(String channel) throws Exception {
        // deviceconfig.md: {"devicename": "Living-Room 1"}
        assertThat(sendSettings(new ChannelUID(thingUID, channel), new StringType("Living-Room 1")),
                is(json("{\"devicename\": \"Living-Room 1\"}")));
    }

    @Test
    void deviceNameIsSentWithoutUnicodeEscapes() throws Exception {
        // deviceconfig.md: air-Q cannot convert \\u escape sequences such as \\ud83d\\ude01 back
        List<String> requests = new ArrayList<>();
        AirqHandler commandHandler = new AirqHandler(thing, requireNonNull(mock(HttpClient.class))) {
            @Override
            protected Result getData(String address, String method, @Nullable String body) {
                requests.add(requireNonNull(body));
                return new Result("", 200);
            }
        };

        commandHandler.handleCommand(new ChannelUID(thingUID, "settings#device-name"),
                new StringType("Room 😁 <Küche> & 'Bad'="));

        assertThat(
                commandHandler.decrypt(
                        requests.getFirst().substring("request=".length()).getBytes(StandardCharsets.UTF_8), ""),
                is("{\"devicename\":\"Room 😁 <Küche> & 'Bad'=\"}"));
    }

    @Test
    void deviceNameChannelTypesAreWritable() throws Exception {
        Document thingTypes = xml("thing/thing-types.xml");

        assertThat(findElement(thingTypes, "//channel-type[@id='device-name']/state[@readOnly='true']"), nullValue());
        assertThat(findElement(thingTypes, "//channel-type[@id='deviceName']/state[@readOnly='true']"), nullValue());
    }

    @Test
    void roomTypeOptionsAreSentAsRoomType() throws Exception {
        for (String roomType : ROOM_TYPES) {
            assertThat(sendSettings("room-type", new StringType(roomType)),
                    is(json("{\"RoomType\": \"" + roomType + "\"}")));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "health", "performance" })
    void rawIndicesArePlainNumbersAndTheFlatChannelsKeepTheirType(String index) throws Exception {
        // datenlesen.md: an index from 0 to 1000 without a unit
        Document thingTypes = xml("thing/thing-types.xml");
        Element grouped = element(thingTypes,
                "//channel-group-type[@id='measurements']/channels/channel[@id='" + index + "']");
        assertThat(grouped.getAttribute("typeId"), is(index + "-raw"));
        assertThat(element(thingTypes, "//channel-type[@id='" + index + "-raw']/item-type").getTextContent(),
                is("Number"));
        // the released flat channel of thing type version 4
        assertThat(element(thingTypes, "//channel-type[@id='" + index + "']/item-type").getTextContent(),
                is("Number:Dimensionless"));
        Element upgrade = element(xml("update/update.xml"), "//instruction-set[@targetVersion='6']/update-channel[@id='"
                + index + "' and @groupIds='measurements']");
        assertThat(element(upgrade, "type").getTextContent(), is("airq:" + index + "-raw"));
    }

    @Test
    void uptimeBelongsToStatusInMetadataAndUpgrade() throws Exception {
        assertThat(findElement(xml("thing/thing-types.xml"),
                "//channel-group-type[@id='status']/channels/channel[@id='uptime']"), notNullValue());
        assertThat(findElement(xml("thing/thing-types.xml"),
                "//channel-group-type[@id='measurements']/channels/channel[@id='uptime']"), nullValue());
        assertThat(element(xml("update/update.xml"), "//instruction-set[@targetVersion='5']/add-channel[@id='uptime']")
                .getAttribute("groupIds"), is("general"));
        assertThat(
                findElement(xml("update/update.xml"),
                        "//instruction-set[@targetVersion='6']/add-channel[@id='uptime' and @groupIds='status']"),
                notNullValue());
    }

    private static Document xml(String resource) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(Path.of("src/main/resources/OH-INF", resource).toFile());
    }

    private static Element element(org.w3c.dom.Node node, String expression) throws Exception {
        return (Element) requireNonNull(
                XPathFactory.newInstance().newXPath().evaluate(expression, node, XPathConstants.NODE));
    }

    private static @Nullable Element findElement(org.w3c.dom.Node node, String expression) throws Exception {
        return (Element) XPathFactory.newInstance().newXPath().evaluate(expression, node, XPathConstants.NODE);
    }

    private static List<Element> elements(org.w3c.dom.Node node, String expression) throws Exception {
        org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) XPathFactory.newInstance().newXPath()
                .evaluate(expression, node, XPathConstants.NODESET);
        List<Element> result = new ArrayList<>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            result.add((Element) nodeList.item(index));
        }
        return result;
    }

    @ParameterizedTest
    @MethodSource("measurements")
    void publishesMeasurementsAndErrorsWithUnits(String key, Unit<?> unit) throws Exception {
        poll("{\"" + key + "\": [12.5, 0.25]}");
        String channel = key.toLowerCase(Locale.ROOT).replace('_', '-');

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new QuantityType<>(12.5, unit));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#" + channel + "-maxerr"),
                new QuantityType<>(0.25, unit));
    }

    @ParameterizedTest
    @MethodSource("measurements")
    void supportsScalarReadingsWithoutInventingErrors(String key, Unit<?> unit) throws Exception {
        poll("{\"" + key + "\": 12.5}");
        String channel = key.toLowerCase(Locale.ROOT).replace('_', '-');

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new QuantityType<>(12.5, unit));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#" + channel + "-maxerr"),
                UnDefType.UNDEF);
    }

    @Test
    void silentlyIgnoresAbsentAndUnknownMeasurements() throws Exception {
        poll("{\"future_sensor\": [1, 2]}");

        assertThat(
                mockingDetails(callback).getInvocations().stream()
                        .filter(invocation -> invocation.getMethod().getName().equals("stateUpdated"))
                        .map(invocation -> invocation.getArgument(0, ChannelUID.class)).toList(),
                allOf(not(hasItem(new ChannelUID(thingUID, "future_sensor"))),
                        not(hasItem(new ChannelUID(thingUID, "measurements#ch2o-m10")))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "[]", "[1]", "[1,2,3]", "[1,null]", "[null,2]", "[1,\"secret\"]", "\"secret\"",
            "true", "{}" })
    void invalidatesMalformedReadingsAndRecovers(String reading) throws Exception {
        poll("{\"ch2o_M10\": " + reading + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#ch2o-m10"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#ch2o-m10-maxerr"),
                UnDefType.UNDEF);

        poll("{\"ch2o_M10\": [3, 0.5]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#ch2o-m10"),
                new QuantityType<>(3, Units.MICROGRAM_PER_CUBICMETRE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#ch2o-m10-maxerr"),
                new QuantityType<>(0.5, Units.MICROGRAM_PER_CUBICMETRE));
    }

    @Test
    void retainsMeasurementsButOmitsSensitiveAndUnknownValues() {
        JsonObject data = json("""
                {"co2": [450, 5], "ch2o_M10": [12.5, 0.25], "sound_max": 50, "pressure_rel": null,
                 "DeviceID": "private-id", "id": 12345, "pass": "secret", "token": 987654,
                 "WiFipass": "wifi-secret", "geopos": {"lat": 50, "long": 10},
                 "Status": {"sensor": "private-message"}, "future_sensor": [42, 2],
                 "temperature": "sensitive", "tvoc": [1, "sensitive"]}
                """);

        JsonObject sanitized = AirqHandler.sanitizeData(data);

        assertThat(sanitized.get("co2"), is(data.get("co2")));
        assertThat(sanitized.get("ch2o_M10"), is(data.get("ch2o_M10")));
        assertThat(sanitized.get("sound_max"), is(data.get("sound_max")));
        assertThat(sanitized.get("pressure_rel").isJsonNull(), is(true));
        for (String key : new String[] { "DeviceID", "id", "pass", "token", "WiFipass", "geopos", "Status",
                "future_sensor", "temperature", "tvoc" }) {
            assertThat(sanitized.get(key).getAsString(), is("<omitted>"));
        }
        assertThat(data.get("pass").getAsString(), is("secret"));
    }

    // GET /config of a real air-Q Science with a radon sensor, network data replaced
    // alarm.md: the fire and gas alarm settings
    private static final String ALARMS_ENABLED = "{\"FireAlarm\": true, \"GasAlarm\": true}";
    private static final String SCIENCE_CONFIGURATION = """
            {"SoundInfo": {}, "WLAN config": {"IP address": "ip", "Gateway": "gateway", "MAC": "mac",
              "Net Mask": "mask", "RSSI": -58, "Channel": 11, "BSSID": "bssid", "SSID": "ssid", "Status": "Got IP"},
             "AutoDriftCompensation": true, "SecondsMeasurementDelay": 120, "Industry": false, "RoomType": "office",
             "boots": "1/0/0/0/5", "WLANssid": ["ssid"], "AutoUpdate": 0, "uptime": 21469,
             "NightMode": {"Activated": false, "WifiNightOff": false, "StartDay": "07:00",
              "last_summer_winter_time_switch": 0, "StartNight": "21:00", "AlarmNightOff": false,
              "FanNightOff": false, "BrightnessNight": 6, "BrightnessDay": 6},
             "daytime": true, "Altitude": [392, 10], "RoomHeight": 0.0,
             "SensorInfo": {"pressure": {"Unit": "hPa", "Value Name": "pressure"},
              "temperature": {"Unit": {"humidity": "%", "temperature": "degC", "dewpt": "degC",
               "humidity_abs": "g/m^3"}, "Value Name": ["temperature", "humidity", "humidity_abs", "dewpt"]},
              "tvoc": {"Unit": "ppb", "Molar Mass": 46.07, "Value Name": "tvoc"},
              "radon": {"Unit": "Bq/m^3", "Value Name": "radon"}},
             "BuildingStandard": "None", "logging": "Error", "geopos": {"lat": 0.0, "long": 0.0}, "WifiInfo": true,
             "Wifi": true, "ppm&ppb": false, "cloud_connection": "CONNECTED", "cloudUpload": false, "GasAlarm": false,
             "InitialCalFinished": true, "warmup-phase": false, "usercalib": {}, "type": "airQ Science", "units": {},
             "Rejection": "50Hz", "devicename": "airQ Radon Science", "AlarmForwarding": false, "FireAlarm": false,
             "Averaging": 30, "TimeServer": "pool.ntp.org",
             "sensors": ["dewpt", "humidity", "humidity_abs", "pressure", "pressure_rel", "radon", "temperature",
              "tvoc"],
             "deactivated_sensors": ["fahrenheit", "measuretime", "mold", "pm_cnts", "uptime", "virus", "wifi"],
             "country_code": "None", "RoomArea": 0.0}
            """;
    // the sensors of GET /config of a real air-Q Pro
    private static final String PRO_CONFIGURATION = """
            {"SensorInfo": {"pressure": {"Value Name": ["pressure", "pressure_rel"]}, "tvoc": {"Value Name": "tvoc"},
              "particulates": {"Value Name": ["pm1", "pm2_5", "pm10", "cnt0_3", "cnt0_5", "cnt1", "cnt2_5", "cnt5",
               "cnt10", "TypPS"]},
              "co": {"Value Name": "co"}, "co2": {"Value Name": "co2"},
              "temperature": {"Value Name": ["temperature", "humidity", "humidity_abs", "dewpt"]},
              "ch2o_M10": {"Value Name": "ch2o_M10"}, "no2": {"Value Name": "no2"},
              "sound": {"Value Name": ["sound", "sound_max"]}},
             "sensors": ["ch2o_M10", "co", "co2", "dewpt", "humidity", "humidity_abs", "no2", "particulates",
              "pressure", "pressure_rel", "sound", "sound_max", "temperature", "tvoc"],
             "deactivated_sensors": ["mold", "pm_cnts", "virus", "fahrenheit", "measuretime", "uptime", "wifi"]}
            """;
    // 2. LifeData.md: measurements of the real air-Q Pro
    private static final String PRO_MEASUREMENTS = """
            {"co2": [1216.0, 86.5], "no2": [15.66, 3.4], "co": [0.136266, 0.03992], "ch2o_M10": [4.27, 2.7],
             "humidity": [59.375, 4.85], "sound_max": [52.6, 2.8], "humidity_abs": [10.291, 0.7],
             "timestamp": 1793046696000, "tvoc": [1330.0, 210.7], "pressure_rel": [1022.35, 2.26], "health": 894,
             "dHdt": -0.01, "sound": [48.8, 3.3], "dCO2dt": 16.94, "pm2_5": [0, 10], "Status": "OK",
             "pressure": [975.93, 1.0], "pm1": [0, 10], "uptime": 1068539, "temperature": [19.978, 0.54],
             "measuretime": 2004, "dewpt": [12.007, 1.05], "performance": 321.32372, "pm10": [3, 10]}
            """;

    @Test
    void channelsOfARadonScienceAreReducedToItsSensorsAndSettings() throws Exception {
        Set<String> channels = channelsAfterPolling(SCIENCE_CONFIGURATION, """
                {"temperature": [21.5, 0.5], "humidity": [45.0, 4.0], "humidity_abs": [8.5, 0.7],
                 "dewpt": [9.2, 1.0], "pressure": [975.9, 1.0], "pressure_rel": [1022.4, 2.3], "tvoc": [120, 20],
                 "radon": [12, 4], "health": 950, "performance": 800, "dHdt": 0.0, "timestamp": 1793046696000,
                 "Status": "OK", "uptime": 21469, "measuretime": 2004}
                """);

        assertThat(channels, hasItems("measurements#temperature", "measurements#humidity-relative",
                "measurements#humidity-absolute", "measurements#dewpt", "measurements#d-hdt", "measurements#pressure",
                "measurements#tvoc", "measurements#radon", "maxerr#radon-maxerr", "measurements#health",
                "measurements#health-index", "measurements#performance", "measurements#performance-index",
                // only in "sensors", an altitude is set
                "measurements#pressure-rel", "maxerr#pressure-rel-maxerr",
                // sent although listed in deactivated_sensors
                "status#uptime", "status#measure-time", "status#status", "status#timestamp", "events#fire-alarm-active",
                "events#gas-alarm-active", "settings#altitude", "settings#country-code",
                "settings#night-mode-last-time-switch", "status#wlan-config-ssid", "status#cloud-connection",
                // from the WLAN config, although the measurement wifi is deactivated
                "status#wifi-rssi",
                // the retained flat channel of thing type version 4
                "co"));
        assertThat(channels,
                not(anyOf(hasItem("measurements#co2"), hasItem("maxerr#co2-maxerr"), hasItem("measurements#d-co2dt"),
                        hasItem("measurements#fine-dust-conc02-5"), hasItem("measurements#c2h4o"),
                        hasItem("maxerr#c2h4o-maxerr"), hasItem("measurements#flow1"),
                        hasItem("measurements#pm1-sps30"), hasItem("measurements#virus-free"),
                        hasItem("maxerr#mold-free-maxerr"), hasItem("events#door-event"), hasItem("events#person"),
                        // not in the configuration of this firmware
                        hasItem("settings#advanced-data-processing"))));
        assertThat(channels.stream().filter(channel -> channel.startsWith("measurements#")).toList(), hasSize(13));
    }

    @Test
    void channelsOfAProFollowItsParticulateSensorAndTheDeactivatedCounts() throws Exception {
        Set<String> channels = channelsAfterPolling(PRO_CONFIGURATION, PRO_MEASUREMENTS);

        assertThat(channels, hasItems("measurements#co2", "measurements#d-co2dt", "measurements#co", "measurements#no2",
                "measurements#ch2o-m10", "maxerr#ch2o-m10-maxerr", "measurements#sound", "measurements#sound-max",
                "maxerr#sound-max-maxerr", "measurements#pressure-rel", "measurements#fine-dust-conc01",
                "measurements#fine-dust-conc02-5", "measurements#fine-dust-conc10", "maxerr#fine-dust-conc10-maxerr"));
        // pm_cnts is deactivated, a real air-Q Pro sends no counts although SensorInfo lists them
        assertThat(channels,
                not(anyOf(hasItem("measurements#fine-dust-cnt00-3"), hasItem("maxerr#fine-dust-cnt00-3-maxerr"),
                        hasItem("measurements#avg-fine-dust-size"), hasItem("measurements#radon"),
                        hasItem("measurements#o3"), hasItem("measurements#particulates"))));
    }

    @Test
    void meanParticleSizeOfTheSps30FollowsPmCnts() throws Exception {
        // datenlesen.md: TypPS_SPS30 is output with pm_cnts, the SPS30 concentrations and counts are not
        String sps30 = PRO_CONFIGURATION.replace("\"no2\": {\"Value Name\": \"no2\"}",
                "\"no2\": {\"Value Name\": \"no2\"}, \"sps30\": {\"Value Name\": [\"pm1_SPS30\", \"pm2_5_SPS30\","
                        + " \"pm4_SPS30\", \"pm10_SPS30\", \"cnt0_5_SPS30\", \"cnt1_SPS30\", \"cnt2_5_SPS30\","
                        + " \"cnt4_SPS30\", \"cnt10_SPS30\", \"TypPS_SPS30\"]}");

        Set<String> channels = channelsAfterPolling(sps30, PRO_MEASUREMENTS);

        assertThat(channels, hasItems("measurements#pm1-sps30", "measurements#pm4-sps30", "measurements#cnt0-5-sps30",
                "measurements#cnt10-sps30"));
        assertThat(channels, not(hasItem("measurements#avg-fine-dust-size-sps30")));
        assertThat(channelsAfterPolling(sps30.replace("\"mold\", \"pm_cnts\", ", "\"mold\", "), PRO_MEASUREMENTS),
                hasItem("measurements#avg-fine-dust-size-sps30"));
    }

    @Test
    void aFailedChannelUpdateKeepsTheThingOnline() throws Exception {
        AirqHandler pollingHandler = channelHandler(groupedThing(), PRO_CONFIGURATION);
        doThrow(new IllegalStateException("registry not ready")).when(callback).createChannelBuilders(any(), any());
        doReturn(PRO_MEASUREMENTS).when(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/data"));

        pollingHandler.pollData();

        verify(callback, never()).thingUpdated(any());
        verify(callback, never()).statusUpdated(any(), argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        requireNonNull(verify(callback, atLeastOnce())).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void particleCountsAreKeptWhilePmCntsIsActive() throws Exception {
        Set<String> channels = channelsAfterPolling(PRO_CONFIGURATION.replace("\"mold\", \"pm_cnts\", ", "\"mold\", "),
                PRO_MEASUREMENTS);

        assertThat(channels, hasItems("measurements#fine-dust-cnt00-3", "maxerr#fine-dust-cnt00-3-maxerr",
                "measurements#fine-dust-cnt10", "measurements#avg-fine-dust-size"));
    }

    @Test
    void onlyTheAlwaysSentChannelsRemainWithoutSensorsAndSettings() throws Exception {
        // every channel depends on a sensor, a virtual sensor or a key of the configuration, except these
        Set<String> channels = channelsAfterPolling("{\"SensorInfo\": {}}", "{\"Status\": \"OK\"}");

        assertThat(channels,
                containsInAnyOrder("co", "status#status", "status#uptime", "status#timestamp", "status#measure-time",
                        "measurements#health", "measurements#health-index", "measurements#performance",
                        "measurements#performance-index", "measurements#virus-free", "maxerr#virus-free-maxerr",
                        "measurements#mold-free", "maxerr#mold-free-maxerr", "events#fire-alarm-active",
                        "events#gas-alarm-active"));
    }

    @Test
    void allMeasurementChannelsAreKeptWhenTheDeviceDoesNotReportItsSensors() throws Exception {
        Set<String> channels = channelsAfterPolling("{\"Wifi\": true}", "{\"Status\": \"OK\"}");

        assertThat(channels, hasItems("measurements#co2", "maxerr#c2h4o-maxerr", "measurements#flow4",
                "measurements#fine-dust-cnt00-3", "settings#wifi"));
        assertThat(channels, not(anyOf(hasItem("settings#altitude"), hasItem("events#door-event"))));
    }

    @Test
    void eventChannelsAreAddedOnceTheDeviceSendsThem() throws Exception {
        AirqHandler pollingHandler = channelHandler(groupedThing(), PRO_CONFIGURATION);
        doReturn(PRO_MEASUREMENTS)
                .doReturn(PRO_MEASUREMENTS.replace("\"Status\": \"OK\"", "\"Status\": \"OK\", \"door_event\": 0"))
                .when(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/data"));

        pollingHandler.pollData();
        assertThat(channelIds(pollingHandler.getThing()), not(hasItem("events#door-event")));
        pollingHandler.pollData();
        assertThat(channelIds(pollingHandler.getThing()), hasItem("events#door-event"));
        // the next configuration keeps the channel
        pollingHandler.getConfigData();
        assertThat(channelIds(pollingHandler.getThing()), hasItem("events#door-event"));
    }

    @Test
    void channelsOfAddedSensorsAreAddedAgainWithTheirExistingChannels() throws Exception {
        AirqHandler pollingHandler = channelHandler(groupedThing(), SCIENCE_CONFIGURATION);
        doReturn("{\"Status\": \"OK\"}").when(pollingHandler)
                .getDecryptedContentString(ArgumentMatchers.endsWith("/data"));
        pollingHandler.pollData();
        Channel temperature = requireNonNull(
                pollingHandler.getThing().getChannel(new ChannelUID(thingUID, "measurements#temperature")));
        assertThat(channelIds(pollingHandler.getThing()), not(hasItem("measurements#co2")));

        // a CO2 sensor was added
        doReturn(SCIENCE_CONFIGURATION.replace("\"radon\": {\"Unit\": \"Bq/m^3\", \"Value Name\": \"radon\"}",
                "\"radon\": {\"Value Name\": \"radon\"}, \"co2\": {\"Value Name\": \"co2\"}")).when(pollingHandler)
                .getDecryptedContentString(ArgumentMatchers.endsWith("/config"));
        pollingHandler.getConfigData();

        assertThat(channelIds(pollingHandler.getThing()),
                hasItems("measurements#co2", "maxerr#co2-maxerr", "measurements#d-co2dt"));
        assertThat(pollingHandler.getThing().getChannel(new ChannelUID(thingUID, "measurements#temperature")),
                sameInstance(temperature));
    }

    @Test
    void channelsAreNotChangedWhenTheConfigurationCannotBeRead() throws Exception {
        Thing groupedThing = groupedThing();
        AirqHandler pollingHandler = channelHandler(groupedThing, "not json");
        doReturn(PRO_MEASUREMENTS).when(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/data"));

        pollingHandler.pollData();

        verify(callback, never()).thingUpdated(any());
        assertThat(pollingHandler.getThing().getChannels(), hasSize(groupedThing.getChannels().size()));
    }

    @Test
    void channelsAreNotChangedWhileAChannelGroupTypeIsUnknown() throws Exception {
        Thing groupedThing = groupedThing();
        AirqHandler pollingHandler = channelHandler(groupedThing, SCIENCE_CONFIGURATION);
        doReturn(List.of()).when(callback).createChannelBuilders(any(), eq(new ChannelGroupTypeUID("airq", "events")));
        doReturn("{\"Status\": \"OK\"}").when(pollingHandler)
                .getDecryptedContentString(ArgumentMatchers.endsWith("/data"));

        pollingHandler.pollData();

        verify(callback, never()).thingUpdated(any());
        assertThat(pollingHandler.getThing().getChannels(), hasSize(groupedThing.getChannels().size()));
    }

    @Test
    void allChannelsAreKeptAndAddedAgainWhenRemovalIsSwitchedOff() throws Exception {
        Thing reducedThing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID).withChannels(groupedThing().getChannels()
                .stream().filter(channel -> !"measurements#co2".equals(channel.getUID().getId())).toList()).build();
        AirqHandler pollingHandler = channelHandler(reducedThing, SCIENCE_CONFIGURATION);
        pollingHandler.config.removeUnsupportedChannels = false;
        doReturn("{\"Status\": \"OK\"}").when(pollingHandler)
                .getDecryptedContentString(ArgumentMatchers.endsWith("/data"));

        pollingHandler.pollData();

        assertThat(channelIds(pollingHandler.getThing()), hasItems("measurements#co2", "events#door-event"));
        assertThat(pollingHandler.getThing().getChannels(), hasSize(groupedThing().getChannels().size()));
    }

    private Set<String> channelsAfterPolling(String configuration, String measurements) throws Exception {
        AirqHandler pollingHandler = channelHandler(groupedThing(), configuration);
        doReturn(measurements).when(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/data"));
        pollingHandler.pollData();
        return channelIds(pollingHandler.getThing());
    }

    // a handler whose callback provides the channels of the channel groups of thing-types.xml
    private AirqHandler channelHandler(Thing groupedThing, String configuration) throws Exception {
        Map<String, List<Element>> groups = new LinkedHashMap<>();
        Document metadata = xml("thing/thing-types.xml");
        for (Element group : elements(metadata, "//channel-group-type")) {
            groups.put(group.getAttribute("id"), elements(group, "channels/channel"));
        }
        doAnswer(invocation -> {
            String groupId = ((ChannelGroupTypeUID) invocation.getArgument(1)).getId();
            List<ChannelBuilder> builders = new ArrayList<>();
            for (Element channel : groups.getOrDefault(groupId, List.of())) {
                Element type = element(metadata, "//channel-type[@id='" + channel.getAttribute("typeId") + "']");
                builders.add(ChannelBuilder
                        .create(new ChannelUID(thingUID, groupId + "#" + channel.getAttribute("id")),
                                element(type, "item-type").getTextContent())
                        .withType(new ChannelTypeUID("airq", channel.getAttribute("typeId"))));
            }
            return builders;
        }).when(callback).createChannelBuilders(any(), any());
        AirqHandler pollingHandler = requireNonNull(
                spy(new AirqHandler(groupedThing, requireNonNull(mock(HttpClient.class)))));
        pollingHandler.setCallback(callback);
        doReturn(configuration).when(pollingHandler).getDecryptedContentString(ArgumentMatchers.endsWith("/config"));
        return pollingHandler;
    }

    // a Thing upgraded from version 4 with all grouped channels and one retained flat channel
    private Thing groupedThing() throws Exception {
        List<Channel> channels = new ArrayList<>();
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, "co"), "Number:Density").build());
        Document metadata = xml("thing/thing-types.xml");
        for (Element group : elements(metadata, "//channel-group-type")) {
            for (Element channel : elements(group, "channels/channel")) {
                channels.add(
                        ChannelBuilder
                                .create(new ChannelUID(thingUID,
                                        group.getAttribute("id") + "#" + channel.getAttribute("id")), "String")
                                .build());
            }
        }
        return ThingBuilder.create(THING_TYPE_AIRQ, thingUID).withChannels(channels).build();
    }

    private static Set<String> channelIds(Thing configuredThing) {
        return configuredThing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Test
    void everyChannelOfTheGroupsIsPublishedAndOnlyThose() throws Exception {
        // every key of the measurements and the configuration that the binding reads
        StringBuilder data = new StringBuilder("{\"Status\": \"OK\", \"timestamp\": 1793046696000, \"health\": 894,"
                + " \"performance\": 321.3, \"dCO2dt\": 16.94, \"dHdt\": -0.01, \"measuretime\": 2004,"
                + " \"uptime\": 1068539, \"TypPS\": 0.62");
        for (String key : List.of("acid_M100", "ash3", "br2", "c2h4", "c2h4o", "c3h8_MIPEX", "ch2o_M10", "ch4_MIPEX",
                "ch4s", "cl2_M20", "clo2", "cnt0_3", "cnt0_5", "cnt0_5_SPS30", "cnt1", "cnt10", "cnt10_SPS30",
                "cnt1_SPS30", "cnt2_5", "cnt2_5_SPS30", "cnt4_SPS30", "cnt5", "co", "co2", "cs2", "dewpt", "door_event",
                "ethanol", "f2", "flow1", "flow2", "flow3", "flow4", "h2_M1000", "h2o2", "h2s", "hcl", "hcn", "hf",
                "humidity", "humidity_abs", "mold", "n2o", "nh3_MR100", "no2", "no_M250", "o3", "oxygen", "person",
                "ph3", "pm1", "pm10", "pm10_SPS30", "pm1_SPS30", "pm2_5", "pm2_5_SPS30", "pm4_SPS30", "pressure",
                "pressure_rel", "r32", "r454b", "r454c", "radon", "sih4", "so2", "sound", "sound_max", "temperature",
                "tvoc", "tvoc_ionsc", "virus", "window_event", "window_open")) {
            data.append(", \"").append(key).append("\": [1.5, 0.5]");
        }
        poll(data.append('}').toString());
        readConfiguration("""
                {"Wifi": true, "WLANssid": ["net"], "WifiInfo": true, "TimeServer": "pool.ntp.org",
                 "geopos": {"lat": 0.0, "long": 0.0}, "daytime": true, "devicename": "airQ Pro", "RoomType": "office",
                 "logging": "Error", "FireAlarm": false, "cloudUpload": false, "SecondsMeasurementDelay": 120,
                 "Rejection": "50Hz", "AutoDriftCompensation": true, "AutoUpdate": 90, "AdvancedDataProcessing": false,
                 "ppm&ppb": false, "GasAlarm": false, "SoundInfo": {}, "AlarmForwarding": false,
                 "usercalib": {"co": {"offset": -0.7, "timestamp": 1793004929}}, "InitialCalFinished": true,
                 "Averaging": 30, "warmup-phase": false, "country_code": "DE", "cloud_connection": "DISCONNECTED",
                 "boots": "13/41/0/239/78", "Altitude": ["392", 10], "BuildingStandard": "None", "RoomHeight": 2.4,
                 "RoomArea": 20.0,
                 "WLAN config": {"IP address": "ip", "Gateway": "gateway", "MAC": "mac", "Net Mask": "mask",
                  "BSSID": "bssid", "SSID": "ssid", "RSSI": -70},
                 "NightMode": {"Activated": false, "WifiNightOff": false, "StartDay": "07:00",
                  "last_summer_winter_time_switch": 0, "StartNight": "21:00", "AlarmNightOff": false,
                  "FanNightOff": false, "BrightnessNight": 6, "BrightnessDay": 6}}
                """);

        ArgumentCaptor<ChannelUID> published = ArgumentCaptor.forClass(ChannelUID.class);
        requireNonNull(verify(callback, atLeastOnce())).stateUpdated(published.capture(), any());
        Set<String> publishedIds = published.getAllValues().stream().map(ChannelUID::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> definedIds = new TreeSet<>();
        for (Element group : elements(xml("thing/thing-types.xml"), "//channel-group-type")) {
            for (Element channel : elements(group, "channels/channel")) {
                definedIds.add(group.getAttribute("id") + "#" + channel.getAttribute("id"));
            }
        }
        assertThat("published but not defined", publishedIds.stream().filter(id -> !definedIds.contains(id)).toList(),
                empty());
        assertThat("defined but not published", definedIds.stream().filter(id -> !publishedIds.contains(id)).toList(),
                empty());
    }

    @Test
    void deviceDetailsOfARealDeviceArePublished() throws Exception {
        // excerpt of the /config answer of an air-Q Pro
        readConfiguration("""
                {"type": "airQ Select", "SN": "91e5157abe", "country_code": "DE", "cloud_connection": "DISCONNECTED",
                 "boots": "13/41/0/239/78", "Altitude": ["392", 10], "BuildingStandard": "None",
                 "RoomHeight": 0.0, "RoomArea": 0.0,
                 "NightMode": {"Activated": false, "WifiNightOff": false, "StartDay": "07:00",
                  "last_summer_winter_time_switch": 0, "StartNight": "21:00", "AlarmNightOff": false,
                  "FanNightOff": false, "BrightnessNight": 6, "BrightnessDay": 6}}
                """);

        assertThat(thing.getProperties(),
                allOf(hasEntry("deviceType", "airQ Select"), hasEntry("serialNumber", "91e5157abe")));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#country-code"),
                new StringType("DE"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#cloud-connection"),
                new StringType("DISCONNECTED"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "status#boots"),
                new StringType("13/41/0/239/78"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#altitude"),
                new QuantityType<>(392, SIUnits.METRE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#altitude-error"),
                new QuantityType<>(10, SIUnits.METRE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#building-standard"),
                new StringType("None"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#room-height"),
                new QuantityType<>(0.0, SIUnits.METRE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#room-area"),
                new QuantityType<>(0.0, SIUnits.SQUARE_METRE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#night-mode-last-time-switch"),
                new DecimalType(0));
    }

    @Test
    void altitudeIsUndefinedWhileNotSet() throws Exception {
        // deviceconfig.md: the default [-10000.0, 100.0]
        readConfiguration("{\"Altitude\": [-10000.0, 100.0]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#altitude"), UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#altitude-error"),
                new QuantityType<>(100.0, SIUnits.METRE));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"Altitude\": 127}", "{\"Altitude\": [127.0]}", "{\"Altitude\": [\"high\", \"low\"]}",
            "{\"Altitude\": {\"altitude\": 127}}", "{}" })
    void malformedOrMissingAltitudesAreUndefined(String configuration) throws Exception {
        readConfiguration(configuration);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#altitude"), UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "settings#altitude-error"),
                UnDefType.UNDEF);
        // the configuration is still read after the altitude
        requireNonNull(verify(callback)).stateUpdated(eq(new ChannelUID(thingUID, "settings#room-height")), any());
    }

    @Test
    void altitudeCommandsKeepTheOtherHalfOfTheSetting() throws Exception {
        // deviceconfig.md: [altitude in m, estimated error in m]
        assertThat(sendAltitude("altitude", new QuantityType<>(400, SIUnits.METRE)),
                is(json("{\"Altitude\":[400.0,10.0]}")));
        assertThat(sendAltitude("altitude", new DecimalType(127.5)), is(json("{\"Altitude\":[127.5,10.0]}")));
        assertThat(sendAltitude("altitude", QuantityType.valueOf("1000 ft")), is(json("{\"Altitude\":[304.8,10.0]}")));
        assertThat(sendAltitude("altitude-error", new QuantityType<>(2, SIUnits.METRE)),
                is(json("{\"Altitude\":[392.0,2.0]}")));
    }

    @Test
    void invalidAltitudeCommandsDoNotSendRequests() throws Exception {
        assertThat(sendAltitude("altitude-error", new QuantityType<>(-1, SIUnits.METRE)), nullValue());
        assertThat(sendAltitude("altitude", new QuantityType<>(5, Units.SECOND)), nullValue());
        assertThat(sendAltitude("altitude", new StringType("400")), nullValue());
        // the other half of the setting is not known before the configuration was read
        assertThat(sendSettings("altitude", new QuantityType<>(400, SIUnits.METRE)), nullValue());
    }

    @Test
    void unknownBuildingStandardsAreNotSent() throws Exception {
        assertThat(sendSettings("building-standard", new StringType("LEED")), nullValue());
        assertThat(sendSettings("building-standard", new StringType("well")), nullValue());
    }

    @Test
    void unsupportedCountryCodesAreNotSent() throws Exception {
        // deviceconfig.md: only the listed ISO 3166 codes are supported
        assertThat(sendSettings("country-code", new StringType("ZA")), nullValue());
        assertThat(sendSettings("country-code", new StringType("de")), nullValue());
        assertThat(sendSettings("country-code", new StringType("None")), nullValue());
    }

    @Test
    void roomSizeCommandsUseMetres() throws Exception {
        // example of deviceconfig.md
        assertThat(sendSettings("room-height", new QuantityType<>(2.4, SIUnits.METRE)),
                is(json("{\"RoomHeight\":2.4}")));
        assertThat(sendSettings("room-area", new QuantityType<>(20, SIUnits.SQUARE_METRE)),
                is(json("{\"RoomArea\":20.0}")));
        assertThat(sendSettings("room-height", QuantityType.valueOf("250 cm")), is(json("{\"RoomHeight\":2.5}")));
        assertThat(sendSettings("room-area", new DecimalType(12.5)), is(json("{\"RoomArea\":12.5}")));
        assertThat(sendSettings("room-height", new DecimalType(0)), is(json("{\"RoomHeight\":0.0}")));
    }

    @Test
    void invalidRoomSizeCommandsDoNotSendRequests() throws Exception {
        assertThat(sendSettings("room-height", new QuantityType<>(-1, SIUnits.METRE)), nullValue());
        assertThat(sendSettings("room-area", new QuantityType<>(20, SIUnits.METRE)), nullValue());
        assertThat(sendSettings("room-height", new StringType("2.4")), nullValue());
    }

    // reads the altitude of a real device first, then sends the command
    private @Nullable JsonObject sendAltitude(String channel, Command command) throws Exception {
        List<String> requests = new ArrayList<>();
        String password = "secret";
        JsonObject configuration = new JsonObject();
        configuration.addProperty("content",
                handler.encrypt("{\"Altitude\": [\"392\", 10]}".getBytes(StandardCharsets.UTF_8), password));
        JsonObject response = new JsonObject();
        response.addProperty("content", handler.encrypt("{}".getBytes(StandardCharsets.UTF_8), password));
        AirqHandler commandHandler = new AirqHandler(thing, requireNonNull(mock(HttpClient.class))) {
            @Override
            protected Result getData(String address, String method, @Nullable String body) {
                if ("GET".equals(method)) {
                    return new Result(configuration.toString(), 200);
                }
                requests.add(requireNonNull(body));
                return new Result(response.toString(), 200);
            }
        };
        commandHandler.setCallback(callback);
        commandHandler.config.password = password;
        commandHandler.getConfigData();
        commandHandler.handleCommand(new ChannelUID(thingUID, "settings#" + channel), command);
        if (requests.isEmpty()) {
            return null;
        }
        assertThat(requests, hasSize(1));
        return json(commandHandler.decrypt(
                requests.getFirst().substring("request=".length()).getBytes(StandardCharsets.UTF_8), password));
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }
}

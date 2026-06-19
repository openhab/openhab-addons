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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.openhab.core.library.types.DateTimeType;

/**
 * Tests the {@link EcowittDataParser} against payloads captured from a real gateway (GW1200A, firmware V1.4.7).
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class EcowittDataParserTest {

    private final EcowittDataParser parser = new EcowittDataParser();

    @Test
    void testLiveData() {
        List<MeasuredValue> data = parser.parseLiveData(resource("livedata_celsius.json"));
        Assertions.assertThat(data)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("temperature-outdoor", "21.4 °C"), new Tuple("humidity-outdoor", "41 %"),
                        new Tuple("temperature-feels-like", "21.4 °C"),
                        new Tuple("vapor-pressure-deficit", "1.504 kPa"), new Tuple("temperature-dew-point", "7.6 °C"),
                        new Tuple("speed-wind", "0 m/s"), new Tuple("speed-gust", "0 m/s"),
                        new Tuple("wind-max-day", "0.6 m/s"),
                        // 0x15 is reported in W/m², so it is routed to the solar-radiation channel, not illumination
                        new Tuple("irradiation-solar", "365.66 W/m²"), new Tuple("uv-index", "3"),
                        new Tuple("direction-wind", "29 °"), new Tuple("direction-wind-avg-10min", "23 °"),
                        new Tuple("rain-event", "0 mm"), new Tuple("rain-rate", "0 mm/h"),
                        new Tuple("rain-24-hours", "0 mm"), new Tuple("rain-day", "0 mm"),
                        new Tuple("rain-week", "0 mm"), new Tuple("rain-month", "0 mm"), new Tuple("rain-year", "0 mm"),
                        new Tuple("piezo-rain-state", "OFF"), new Tuple("piezo-rain-event", "0 mm"),
                        new Tuple("piezo-rain-rate", "0 mm/h"), new Tuple("piezo-rain-24-hours", "0 mm"),
                        new Tuple("piezo-rain-day", "0 mm"), new Tuple("piezo-rain-week", "0 mm"),
                        new Tuple("piezo-rain-month", "0 mm"), new Tuple("piezo-rain-year", "0 mm"),
                        new Tuple("temperature-indoor", "22.3 °C"), new Tuple("humidity-indoor", "49 %"),
                        new Tuple("pressure-absolute", "1003.8 hPa"), new Tuple("pressure-relative", "1003.8 hPa"));
    }

    @Test
    void testUnitsAreNormalizedToCanonical() {
        // the same gateway configured to imperial units must yield the same canonical channel states
        Map<String, String> states = stateByChannel(parser.parseLiveData(resource("livedata_fahrenheit.json")));
        Assertions.assertThat(states).containsEntry("temperature-outdoor", "20 °C") // 68 °F
                .containsEntry("temperature-dew-point", "9.7222 °C") // 49.5 °F
                .containsEntry("wind-max-day", "0.2682 m/s") // 0.6 mph
                .containsEntry("pressure-absolute", "1003.7254 hPa") // 29.64 inHg
                .containsEntry("piezo-rain-state", "ON") // srain_piezo = 1
                .containsEntry("vapor-pressure-deficit", "1.153 kPa") // kept in kPa
                .containsEntry("irradiation-solar", "461.08 W/m²");
    }

    @Test
    void testLightUnitRouting() {
        // depending on the gateway's light-unit setting, 0x15 is an illuminance (lux/kLux) or an irradiance (W/m²);
        // each is routed to the matching channel and normalized to its canonical unit, never cross-converted
        Assertions
                .assertThat(stateByChannel(
                        parser.parseLiveData("{\"common_list\":[{\"id\":\"0x15\",\"val\":\"12.3 Klux\"}]}")))
                .containsEntry("illumination", "12300 lx"); // kLux -> canonical lux
        Assertions
                .assertThat(stateByChannel(
                        parser.parseLiveData("{\"common_list\":[{\"id\":\"0x15\",\"val\":\"365.66 W/m2\"}]}")))
                .containsEntry("irradiation-solar", "365.66 W/m²"); // W/m² -> dedicated solar-radiation channel
    }

    @Test
    void testBlackGlobeSensor() {
        // the WN38 black globe sensor reports its black globe (0xA1) and wet bulb globe (0xA2) temperatures in the
        // common_list group; both must be routed to their dedicated temperature channels and normalized to °C
        Assertions
                .assertThat(stateByChannel(parser.parseLiveData(
                        "{\"common_list\":[{\"id\":\"0xA1\",\"val\":\"104.4\",\"unit\":\"F\",\"battery\":\"5\"},"
                                + "{\"id\":\"0xA2\",\"val\":\"84.6\",\"unit\":\"F\"}]}")))
                .containsEntry("temperature-black-globe", "40.2222 °C") // 104.4 °F
                .containsEntry("temperature-wet-bulb-globe", "29.2222 °C"); // 84.6 °F
    }

    @Test
    void testLightning() {
        // the WH57 lightning sensor reports its values in the field-keyed "lightning" group; the distance is given in
        // miles when the gateway is set to imperial units and must be normalized to its canonical km
        Map<String, String> states = stateByChannel(
                parser.parseLiveData("{\"lightning\":[{\"distance\":\"7.4 mi\",\"date\":\"2026-06-11T23:10:31\","
                        + "\"timestamp\":\"06/11/2026 23:10:31\",\"count\":\"0\",\"battery\":\"5\"}]}"));
        // the API reports local time without a zone offset, so the expected value is anchored to the test's zone
        String expectedTime = new DateTimeType(
                LocalDateTime.parse("2026-06-11T23:10:31").atZone(ZoneId.systemDefault())).toString();
        Assertions.assertThat(states).containsEntry("lightning-distance", "11.9091 km") // 7.4 mi
                .containsEntry("lightning-counter", "0").containsEntry("lightning-time", expectedTime);
    }

    @Test
    void testRegisteredSensors() {
        Map<SensorGatewayBinding, SensorDevice> sensors = parser.parseSensors(List.of(resource("sensors_page1.json")),
                false);
        // the disabled WH34 slot (id FFFFFFFE, idst 0) must be skipped
        Assertions.assertThat(sensors.values())
                .extracting(device -> device.getSensorGatewayBinding().getSensor().name(), SensorDevice::getId,
                        SensorDevice::getSignal, device -> device.getBatteryStatus().isLow())
                .containsExactlyInAnyOrder(new Tuple("WS85", 0x2C75, 4, false), new Tuple("WH65", 0x2B, 4, false),
                        new Tuple("WH25", 0xB9, 4, true));
    }

    @Test
    void testVoltageSensorBatteryReportedAsLevel() {
        // unlike the binary protocol, where the WH51 battery field is a raw voltage (val * 0.1 V), the Ecowitt HTTP
        // API reports it already as a 0-5 level (5 = full). It must therefore not be misread as 0.5 V and flagged low.
        Map<SensorGatewayBinding, SensorDevice> sensors = parser.parseSensors(List.of("[{\"img\":\"wh51\","
                + "\"type\":\"14\",\"name\":\"Soil moisture CH1\",\"id\":\"FBF08\",\"batt\":\"5\",\"rssi\":\"-85\","
                + "\"signal\":\"4\",\"idst\":\"1\"}]"), false);
        Assertions.assertThat(sensors.values())
                .extracting(device -> device.getSensorGatewayBinding().getSensor().name(),
                        device -> device.getBatteryStatus().isLow(), device -> device.getBatteryStatus().getLevel())
                .containsExactly(new Tuple("WH51", false, 5));
    }

    @Test
    void testSystemInfo() {
        @Nullable
        SystemInfo systemInfo = parser.parseSystemInfo(resource("device_info.json"));
        Assertions.assertThat(systemInfo).isNotNull();
        Assertions.assertThat(systemInfo.getFrequency()).isEqualTo(868);
        Assertions.assertThat(systemInfo.isDst()).isTrue();
        Assertions.assertThat(systemInfo.isUseWh24()).isFalse();
        Assertions.assertThat(systemInfo.getDateTime().toString()).isEqualTo("2026-06-09T17:46");
    }

    @Test
    void testVersion() {
        String version = resource("version.json");
        Assertions.assertThat(parser.parseFirmwareVersion(version)).isEqualTo("GW1200A_V1.4.7");
        Assertions.assertThat(parser.parseSensorPageCount(version)).isEqualTo(4);
        Assertions.assertThat(parser.isEcowittPlatform(version)).isTrue();
    }

    private static Map<String, String> stateByChannel(List<MeasuredValue> values) {
        return values.stream()
                .collect(java.util.stream.Collectors.toMap(MeasuredValue::getChannelId, v -> v.getState().toString()));
    }

    private static String resource(String name) {
        try (InputStream in = EcowittDataParserTest.class.getResourceAsStream("/http/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing test resource /http/" + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

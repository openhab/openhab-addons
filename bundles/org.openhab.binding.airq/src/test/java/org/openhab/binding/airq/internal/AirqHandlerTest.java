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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    static Stream<Arguments> legacyPairs() {
        return Stream.concat(Stream
                .of(new String[] { "cnt0_3", "fine-dust-cnt00-3" }, new String[] { "cnt0_5", "fine-dust-cnt00-5" },
                        new String[] { "cnt1", "fine-dust-cnt01" }, new String[] { "cnt2_5", "fine-dust-cnt02-5" },
                        new String[] { "cnt5", "fine-dust-cnt05" }, new String[] { "cnt10", "fine-dust-cnt10" },
                        new String[] { "co", "co" }, new String[] { "dewpt", "dewpt" }, new String[] { "h2s", "h2s" },
                        new String[] { "humidity", "humidity-relative" },
                        new String[] { "humidity_abs", "humidity-absolute" }, new String[] { "no2", "no2" },
                        new String[] { "o3", "o3" }, new String[] { "oxygen", "o2" },
                        new String[] { "pm1", "fine-dust-conc01" }, new String[] { "pm2_5", "fine-dust-conc02-5" },
                        new String[] { "pm10", "fine-dust-conc10" }, new String[] { "pressure", "pressure" },
                        new String[] { "radon", "radon" }, new String[] { "so2", "so2" },
                        new String[] { "temperature", "temperature" }, new String[] { "virus", "virus-free" },
                        new String[] { "mold", "mold-free" })
                .map(mapping -> Arguments.of(mapping[0], mapping[1], null)),
                Stream.of(Arguments.of("co2", "co2", Units.PARTS_PER_MILLION),
                        Arguments.of("tvoc", "tvoc", Units.PARTS_PER_BILLION),
                        Arguments.of("sound", "sound", Units.DECIBEL)));
    }

    @ParameterizedTest
    @MethodSource("legacyPairs")
    void preservesLegacyPairChannelsAndStateTypes(String key, String channel, @Nullable Unit<?> unit) throws Exception {
        poll("{\"" + key + "\": [12.5, 0.25]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                unit == null ? new DecimalType(12.5f) : new QuantityType<>(12.5f, unit));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#" + channel + "-maxerr"),
                new DecimalType(0.25f));
    }

    @Test
    void publishesRealTvocReadingInPartsPerBillion() throws Exception {
        poll("{\"tvoc\": [2353, 401.2]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#tvoc"),
                new QuantityType<>(2353, Units.PARTS_PER_BILLION));
    }

    @ParameterizedTest
    @CsvSource({ "TypPS, avg-fine-dust-size", "dCO2dt, d-co2dt", "dHdt, d-hdt", "door_event, door-event",
            "health, health", "performance, performance" })
    void preservesLegacyScalarMappings(String key, String channel) throws Exception {
        poll("{\"" + key + "\": 125}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new DecimalType(125));
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

    @Test
    void preservesLegacyStatusTimestampAndUptime() throws Exception {
        poll("{\"Status\": \"OK\", \"timestamp\": 0, \"uptime\": 123}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#status"), new StringType("OK"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#timestamp"),
                new DateTimeType(Instant.EPOCH));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#uptime"),
                new QuantityType<>(123, Units.SECOND));
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
        assertThat(
                mockingDetails(callback).getInvocations().stream()
                        .filter(invocation -> invocation.getMethod().getName().equals("stateUpdated"))
                        .map(invocation -> invocation.getArgument(0, ChannelUID.class)).toList(),
                allOf(not(hasItem(new ChannelUID(thingUID, "maxerr#co2-maxerr"))),
                        not(hasItem(new ChannelUID(thingUID, "advanced-measurements#ch2o-m10")))));
    }

    @Test
    void malformedLegacyPairGoesOfflineAndRecovers() throws Exception {
        poll("{\"co\": []}");

        requireNonNull(verify(callback)).statusUpdated(thing, new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.COMMUNICATION_ERROR, "Syntax error while parsing response from device"));

        poll("{\"co\": [12.5, 0.25]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co"),
                new DecimalType(12.5f));
        requireNonNull(verify(callback)).statusUpdated(thing,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
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
                 "WLAN config": {"MAC": "mac", "IP address": "ip"}, "AutoUpdate": 1,
                 "warmup-phase": false, "air-Q-Hardware-Version": "hardware", "sensors": ["co2", "sound"],
                 "Industry": true, "id": "device-id", "SensorInfo": "info"}
                    """).when(configurationHandler).decrypt("encoded".getBytes(StandardCharsets.UTF_8), "");

        configurationHandler.getConfigData();

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#wifi"), OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#ssid"),
                new StringType("first, second"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#password"),
                new StringType("password"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#time-server"),
                new StringType("time.example"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#location"),
                new PointType(new DecimalType(50f), new DecimalType(10f)));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#device-name"),
                new StringType("device"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#averaging-rhythm"),
                new QuantityType<>(5, Units.SECOND));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#night-mode-start-day"),
                new QuantityType<>(480, Units.MINUTE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#night-mode-start-night"),
                new QuantityType<>(1350, Units.MINUTE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#power-freq-suppression"),
                new StringType("50Hz+60Hz"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#night-mode-brightness-night"),
                new DecimalType(2));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#night-mode-fan-night-off"),
                OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#wlan-config-mac"),
                new StringType("mac"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#wlan-config-ip-address"),
                new StringType("ip"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#auto-update"), OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#warmup-phase"), OnOffType.OFF);
        assertThat(thing.getProperties(),
                allOf(hasEntry("hardwareVersion", "\"hardware\""), hasEntry("sensorList", "co2, sound"),
                        hasEntry("Industry", "true"), hasEntry("id", "\"device-id\""),
                        hasEntry("sensorInfo", "\"info\"")));
    }

    @Test
    void measurementDurationUsesMilliseconds() throws Exception {
        poll("{\"measuretime\": 2006}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#measure-time"),
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
            "general#night-mode-start-day, 08:00, StartDay", "general#night-mode-start-night, 22:30, StartNight" })
    void stringClockCommandsPreserveLegacyAndGroupedChannels(String channel, String clock, String key)
            throws Exception {
        JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), new StringType(clock)));

        assertThat(settings.getAsJsonObject("NightMode").get(key).getAsString(), is(clock));
    }

    @ParameterizedTest
    @ValueSource(strings = { "24:00", "08:60", "invalid", "08:00:30", "8:00", "480", "" })
    void malformedStringClockCommandsDoNotSendRequests(String clock) throws Exception {
        for (String channel : List.of("nightModeStartDay", "nightModeStartNight", "general#night-mode-start-day",
                "general#night-mode-start-night")) {
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

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#night-mode-start-day"),
                new QuantityType<>(minutes, Units.MINUTE));
    }

    @ParameterizedTest
    @ValueSource(strings = { "24:00", "08:60", "invalid", "08:00:30" })
    void invalidClockReadingsBecomeUndefined(String clock) throws Exception {
        readConfiguration("{\"NightMode\": {\"StartDay\": \"" + clock + "\"}}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#night-mode-start-day"),
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
        return sendSettings(new ChannelUID(thingUID, "general#" + channel), command);
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
        AirqHandler pollingHandler = requireNonNull(
                spy(new AirqHandler(configuredThing, requireNonNull(mock(HttpClient.class)))));
        pollingHandler.setCallback(callback);
        doReturn(payload).when(pollingHandler).getDecryptedContentString(anyString(), eq("GET"), isNull());
        pollingHandler.pollData();
    }

    @Test
    void retainedFlatMeasurementChannelsReceiveStatesAlongsideGroupedChannels() throws Exception {
        Thing legacyThing = ThingBuilder.create(THING_TYPE_AIRQ, thingUID)
                .withChannels(ChannelBuilder.create(new ChannelUID(thingUID, "co"), "Number").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "co_maxerr"), "Number").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "measurements#co"), "Number").build(),
                        ChannelBuilder.create(new ChannelUID(thingUID, "maxerr#co-maxerr"), "Number").build())
                .build();

        poll(legacyThing, "{\"co\": [12.5, 0.25]}");

        for (String channel : List.of("co", "measurements#co")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel), new DecimalType(12.5f));
        }
        for (String channel : List.of("co_maxerr", "maxerr#co-maxerr")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel), new DecimalType(0.25f));
        }
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
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#night-mode-start-day"),
                new QuantityType<>(480, Units.MINUTE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#averaging-rhythm"),
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

        for (String channel : List.of("humidityRelative", "measurements#humidityRelative",
                "measurements#humidity-relative")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel), new DecimalType(45));
        }
        for (String channel : List.of("humidityRelative_maxerr", "maxerr#humidityRelative_maxerr",
                "maxerr#humidity-relative-maxerr")) {
            requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, channel), new DecimalType(0.5));
        }
    }

    @ParameterizedTest
    @CsvSource({ "wifiInfo, WifiInfo", "general#wifi-info, WifiInfo", "fireAlarm, FireAlarm",
            "general#fire-alarm, FireAlarm", "cloudUpload, cloudUpload", "general#cloud-upload, cloudUpload" })
    void renamedCommandsAndLegacyCommandsUseOriginalDeviceKeys(String channel, String key) throws Exception {
        JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), OnOffType.ON));

        assertThat(settings.size(), is(1));
        assertThat(settings.get(key).getAsBoolean(), is(true));
    }

    @ParameterizedTest
    @CsvSource({ "general#night-mode-brightness-day, BrightnessDay",
            "general#night-mode-brightness-night, BrightnessNight", "nightModeBrightnessDay, BrightnessDay",
            "nightModeBrightnessNight, BrightnessNight", "general#nightModeBrightnessDay, BrightnessDay",
            "general#nightModeBrightnessNight, BrightnessNight" })
    void nightModeBrightnessCommandsUseNumericDeviceSettings(String channel, String key) throws Exception {
        for (Command command : List.of(new DecimalType("2.5"), new StringType("2.5"),
                new QuantityType<>(2.5, Units.ONE), new QuantityType<>(250, Units.PERCENT))) {
            JsonObject settings = requireNonNull(sendSettings(new ChannelUID(thingUID, channel), command));
            JsonObject expectedNightMode = new JsonObject();
            expectedNightMode.addProperty(key, 2.5);
            JsonObject expected = new JsonObject();
            expected.add("NightMode", expectedNightMode);

            assertThat(settings, is(expected));
        }
    }

    @ParameterizedTest
    @CsvSource({ "general#night-mode-fan-night-off, FanNightOff", "general#night-mode-wifi-night-off, WifiNightOff",
            "nightModeFanNightOff, FanNightOff", "nightModeWifiNightOff, WifiNightOff",
            "general#nightModeFanNightOff, FanNightOff", "general#nightModeWifiNightOff, WifiNightOff" })
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
    @ValueSource(strings = { "general#night-mode-brightness-day", "general#night-mode-brightness-night" })
    void invalidBrightnessCommandsDoNotSendRequests(String channel) throws Exception {
        for (Command command : List.of(OnOffType.ON, new StringType("invalid"), new StringType("NaN"),
                new StringType("Infinity"), new StringType(""), new QuantityType<>(2.5, Units.SECOND))) {
            assertThat(sendSettings(new ChannelUID(thingUID, channel), command), nullValue());
        }
    }

    static Stream<Arguments> settingCommands() {
        Stream<Arguments> switches = Stream.of(new String[] { "wifi-info", "WifiInfo" },
                new String[] { "fire-alarm", "FireAlarm" }, new String[] { "cloud-upload", "cloudUpload" },
                new String[] { "auto-drift-compensation", "AutoDriftCompensation" },
                new String[] { "auto-update", "AutoUpdate" },
                new String[] { "advanced-data-processing", "AdvancedDataProcessing" },
                new String[] { "gas-alarm", "GasAlarm" }, new String[] { "sound-pressure", "SoundInfo" },
                new String[] { "alarm-forwarding", "AlarmForwarding" }, new String[] { "averaging", "averaging" },
                new String[] { "error-bars", "ErrorBars" }, new String[] { "ppm-and-ppb", "ppm&ppb" },
                new String[] { "night-mode-fan-night-off", "FanNightOff" },
                new String[] { "night-mode-wifi-night-off", "WifiNightOff" })
                .flatMap(mapping -> Stream.of(OnOffType.ON, OnOffType.OFF).map(command -> {
                    JsonObject value = new JsonObject();
                    if ("auto-update".equals(mapping[0])) {
                        value.addProperty(mapping[1], command == OnOffType.ON ? 1 : 0);
                    } else {
                        value.addProperty(mapping[1], command == OnOffType.ON);
                    }
                    JsonObject settings = value;
                    if (mapping[0].startsWith("night-mode-")) {
                        settings = new JsonObject();
                        settings.add("NightMode", value);
                    }
                    return Arguments.of(mapping[0], command, settings);
                }));
        Stream<Arguments> strings = Stream.of(
                Arguments.of("time-server", new StringType("time.example"), json("{\"timeServer\":\"time.example\"}")),
                Arguments.of("room-type", new StringType("office"), json("{\"RoomType\":\"office\"}")),
                Arguments.of("log-level", new StringType("Error"), json("{\"logging\":\"Error\"}")),
                Arguments.of("log-level", new StringType("Warning"), json("{\"logging\":\"Warning\"}")),
                Arguments.of("log-level", new StringType("Info"), json("{\"logging\":\"Info\"}")),
                Arguments.of("power-freq-suppression", new StringType("50Hz"), json("{\"Rejection\":\"50Hz\"}")),
                Arguments.of("power-freq-suppression", new StringType("60Hz"), json("{\"Rejection\":\"60Hz\"}")),
                Arguments.of("power-freq-suppression", new StringType("50Hz+60Hz"),
                        json("{\"Rejection\":\"50Hz+60Hz\"}")),
                Arguments.of("SSID", new StringType(
                        "{\"WiFissid\":\"network\",\"WiFipass\":\"secret\",\"WiFibssid\":\"mac\",\"reset\":\"true\"}"),
                        json("{\"WiFissid\":\"network\",\"WiFipass\":\"secret\",\"WiFibssid\":\"mac\",\"reset\":\"true\"}")));
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
    @ValueSource(strings = { "wifi", "unknown", "delete-key", "device-name", "initial-cal-finished" })
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
        for (String[] groupedChannel : new String[][] { { "advanced-measurements", channel },
                { "advanced-maxerr", channel + "-maxerr" } }) {
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
            assertThat(upgrade.getAttribute("groupIds"), is(groupId));
            assertThat(element(upgrade, "type").getTextContent(), is("airq:" + typeId));
        }
    }

    @Test
    void allChannelsAreGroupedAndExistingThingsAreMigrated() throws Exception {
        Document metadata = xml("thing/thing-types.xml");
        Document upgrades = xml("update/update.xml");
        assertThat(element(metadata, "//thing-type[@id='airq']/properties/property[@name='thingTypeVersion']")
                .getTextContent(), is("5"));
        String[] groupIds = { "general", "measurements", "advanced-measurements", "maxerr", "advanced-maxerr" };
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
        assertThat(elements(upgrades, "//instruction-set[@targetVersion='6']"), empty());
        List<Element> additions = elements(upgrades, "//instruction-set[@targetVersion='5']/add-channel");
        assertThat(additions, hasSize(162));
        List<String> addedIds = additions.stream()
                .map(addition -> addition.getAttribute("groupIds") + "#" + addition.getAttribute("id")).toList();
        assertThat(addedIds.stream().distinct().count(), is((long) channels.size()));
        for (Element addition : additions) {
            String id = addition.getAttribute("id");
            String groupId = addition.getAttribute("groupIds");
            Element definition = element(metadata,
                    "//channel-group-type[@id='" + groupId + "']/channels/channel[@id='" + id + "']");
            assertThat(element(addition, "type").getTextContent(), is("airq:" + definition.getAttribute("typeId")));
        }
        assertThat(channels, hasSize(161 + 1));
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

        commandHandler.handleCommand(new ChannelUID(thingUID, "general#wifi-info"), OnOffType.ON);

        requireNonNull(verify(commandHandler)).getData(anyString(), eq("POST"), anyString());
    }

    @ParameterizedTest
    @CsvSource({ "seconds-measurement-delay, s", "mtime, ms", "uptime, s", "nightmode-start-day, min",
            "nightmode-start-night, min" })
    void timeChannelTypesHaveMatchingUnitHints(String typeId, String unit) throws Exception {
        Element itemType = element(xml("thing/thing-types.xml"), "//channel-type[@id='" + typeId + "']/item-type");

        assertThat(itemType.getTextContent(), is("Number:Time"));
        assertThat(itemType.getAttribute("unitHint"), is(unit));
    }

    @Test
    void uptimeBelongsToGeneralInMetadataAndUpgrade() throws Exception {
        assertThat(findElement(xml("thing/thing-types.xml"),
                "//channel-group-type[@id='general']/channels/channel[@id='uptime']"), notNullValue());
        assertThat(findElement(xml("thing/thing-types.xml"),
                "//channel-group-type[@id='measurements']/channels/channel[@id='uptime']"), nullValue());
        assertThat(element(xml("update/update.xml"), "//instruction-set[@targetVersion='5']/add-channel[@id='uptime']")
                .getAttribute("groupIds"), is("general"));
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

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "advanced-measurements#" + channel),
                new QuantityType<>(12.5, unit));
        requireNonNull(verify(callback)).stateUpdated(
                new ChannelUID(thingUID, "advanced-maxerr#" + channel + "-maxerr"), new QuantityType<>(0.25, unit));
    }

    @ParameterizedTest
    @MethodSource("measurements")
    void supportsScalarReadingsWithoutInventingErrors(String key, Unit<?> unit) throws Exception {
        poll("{\"" + key + "\": 12.5}");
        String channel = key.toLowerCase(Locale.ROOT).replace('_', '-');

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "advanced-measurements#" + channel),
                new QuantityType<>(12.5, unit));
        requireNonNull(verify(callback))
                .stateUpdated(new ChannelUID(thingUID, "advanced-maxerr#" + channel + "-maxerr"), UnDefType.UNDEF);
    }

    @Test
    void silentlyIgnoresAbsentAndUnknownMeasurements() throws Exception {
        poll("{\"future_sensor\": [1, 2]}");

        assertThat(
                mockingDetails(callback).getInvocations().stream()
                        .filter(invocation -> invocation.getMethod().getName().equals("stateUpdated"))
                        .map(invocation -> invocation.getArgument(0, ChannelUID.class)).toList(),
                allOf(not(hasItem(new ChannelUID(thingUID, "future_sensor"))),
                        not(hasItem(new ChannelUID(thingUID, "advanced-measurements#ch2o-m10")))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "[]", "[1]", "[1,2,3]", "[1,null]", "[null,2]", "[1,\"secret\"]", "\"secret\"",
            "true", "{}" })
    void invalidatesMalformedReadingsAndRecovers(String reading) throws Exception {
        poll("{\"ch2o_M10\": " + reading + "}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "advanced-measurements#ch2o-m10"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "advanced-maxerr#ch2o-m10-maxerr"),
                UnDefType.UNDEF);

        poll("{\"ch2o_M10\": [3, 0.5]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "advanced-measurements#ch2o-m10"),
                new QuantityType<>(3, Units.MICROGRAM_PER_CUBICMETRE));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "advanced-maxerr#ch2o-m10-maxerr"),
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

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }
}

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
import org.openhab.core.thing.binding.builder.ThingBuilder;
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
        return Stream.concat(
                Stream.of(new String[] { "cnt0_3", "fineDustCnt00_3" }, new String[] { "cnt0_5", "fineDustCnt00_5" },
                        new String[] { "cnt1", "fineDustCnt01" }, new String[] { "cnt2_5", "fineDustCnt02_5" },
                        new String[] { "cnt5", "fineDustCnt05" }, new String[] { "cnt10", "fineDustCnt10" },
                        new String[] { "co", "co" }, new String[] { "dewpt", "dewpt" }, new String[] { "h2s", "h2s" },
                        new String[] { "humidity", "humidityRelative" },
                        new String[] { "humidity_abs", "humidityAbsolute" }, new String[] { "no2", "no2" },
                        new String[] { "o3", "o3" }, new String[] { "oxygen", "o2" },
                        new String[] { "pm1", "fineDustConc01" }, new String[] { "pm2_5", "fineDustConc02_5" },
                        new String[] { "pm10", "fineDustConc10" }, new String[] { "pressure", "pressure" },
                        new String[] { "radon", "radon" }, new String[] { "so2", "so2" },
                        new String[] { "temperature", "temperature" }, new String[] { "virus", "virus_free" },
                        new String[] { "mold", "mold_free" })
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
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#" + channel + "_maxerr"),
                new DecimalType(0.25f));
    }

    @Test
    void publishesRealTvocReadingInPartsPerBillion() throws Exception {
        poll("{\"tvoc\": [2353, 401.2]}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#tvoc"),
                new QuantityType<>(2353, Units.PARTS_PER_BILLION));
    }

    @ParameterizedTest
    @CsvSource({ "TypPS, avgFineDustSize", "dCO2dt, dCO2dt", "dHdt, dHdt", "door_event, doorEvent",
            "measuretime, measureTime", "health, health", "performance, performance" })
    void preservesLegacyScalarMappings(String key, String channel) throws Exception {
        poll("{\"" + key + "\": 125}");

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#" + channel),
                new DecimalType(125));
    }

    @ParameterizedTest
    @CsvSource({ "health, healthIndex", "performance, performanceIndex" })
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
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#uptime"),
                new QuantityType<>(123, Units.SECOND));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"humidity\": null, \"co2\": null}" })
    void preservesLegacyMissingValueBehavior(String payload) throws Exception {
        poll(payload);

        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#humidityRelative"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "maxerr#humidityRelative_maxerr"),
                UnDefType.UNDEF);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "measurements#co2"), UnDefType.UNDEF);
        assertThat(
                mockingDetails(callback).getInvocations().stream()
                        .filter(invocation -> invocation.getMethod().getName().equals("stateUpdated"))
                        .map(invocation -> invocation.getArgument(0, ChannelUID.class)).toList(),
                allOf(not(hasItem(new ChannelUID(thingUID, "maxerr#co2_maxerr"))),
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
                 "NightMode": {"StartDay": "08:00", "BrightnessNight": 2, "FanNightOff": true},
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
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#timeServer"),
                new StringType("time.example"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#location"),
                new PointType(new DecimalType(50f), new DecimalType(10f)));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#deviceName"),
                new StringType("device"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#averagingRhythm"),
                new DecimalType(5));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#nightModeStartDay"),
                new StringType("08:00"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#nightModeBrightnessNight"),
                new DecimalType(2));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#nightModeFanNightOff"),
                OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#wlanConfigMac"),
                new StringType("mac"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#wlanConfigIPAddress"),
                new StringType("ip"));
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#autoUpdate"), OnOffType.ON);
        requireNonNull(verify(callback)).stateUpdated(new ChannelUID(thingUID, "general#warmupPhase"), OnOffType.OFF);
        assertThat(thing.getProperties(),
                allOf(hasEntry("hardwareVersion", "\"hardware\""), hasEntry("sensorList", "co2, sound"),
                        hasEntry("Industry", "true"), hasEntry("id", "\"device-id\""),
                        hasEntry("sensorInfo", "\"info\"")));
    }

    private void poll(String payload) throws Exception {
        AirqHandler pollingHandler = requireNonNull(spy(handler));
        doReturn(payload).when(pollingHandler).getDecryptedContentString(anyString(), eq("GET"), isNull());
        pollingHandler.pollData();
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
            channels.addAll(elements(group, "channels/channel"));
            assertThat(findElement(metadata,
                    "//thing-type/channel-groups/channel-group[@id='" + groupId + "' and @typeId='" + groupId + "']"),
                    notNullValue());
        }

        assertThat(elements(upgrades, "//instruction-set[@targetVersion='5']/remove-channel"), empty());
        List<Element> legacyMoves = elements(upgrades, "//instruction-set[@targetVersion='5']/update-channel");
        assertThat(legacyMoves, hasSize(101));
        for (Element move : legacyMoves) {
            Element definition = element(metadata, "//channel-group-type[@id='" + move.getAttribute("groupIds")
                    + "']/channels/channel[@id='" + move.getAttribute("id") + "']");
            assertThat(element(move, "type").getTextContent(), is("airq:" + definition.getAttribute("typeId")));
        }
        List<Element> additions = elements(upgrades, "//instruction-set[@targetVersion='5']/add-channel");
        assertThat(additions, hasSize(61));
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

        commandHandler.handleCommand(new ChannelUID(thingUID, "general#wifiInfo"), OnOffType.ON);

        requireNonNull(verify(commandHandler)).getData(anyString(), eq("POST"), anyString());
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

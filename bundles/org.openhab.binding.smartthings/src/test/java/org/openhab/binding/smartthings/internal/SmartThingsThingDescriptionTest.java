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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.xml.internal.ThingDescriptionReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Tests for SmartThings thing description metadata.
 */
@NonNullByDefault
class SmartThingsThingDescriptionTest {
    private record ExpectedChannel(String groupId, String typeId, String capability, String attribute) {
    }

    @Test
    void thingDescriptionFilesCanBeParsedByOpenhabXmlReader() throws Exception {
        ThingDescriptionReader reader = new ThingDescriptionReader();
        URL thingDirectory = SmartThingsThingDescriptionTest.class.getResource("/OH-INF/thing");
        assertNotNull(thingDirectory);

        try (Stream<Path> thingDescriptionFiles = Files.list(Path.of(thingDirectory.toURI()))) {
            List<String> resourceNames = thingDescriptionFiles
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .map(path -> "OH-INF/thing/" + path.getFileName()).sorted().toList();
            assertFalse(resourceNames.isEmpty());

            for (String resourceName : resourceNames) {
                URL resource = SmartThingsThingDescriptionTest.class.getResource("/" + resourceName);
                assertNotNull(resource);
                assertNotNull(reader.readFromXML(resource), resourceName);
            }
        }
    }

    @Test
    void dynamicThingDiscoveryIsAdvancedBridgeConfiguration() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/account.xml");

        Element parameter = findParameter(document, "useDynamicThings");

        assertNotNull(parameter);
        Node advanced = parameter.getElementsByTagName("advanced").item(0);
        assertNotNull(advanced);
        assertEquals("true", advanced.getTextContent());
    }

    @Test
    void callbackUrlIsAdvancedBridgeConfiguration() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/account.xml");

        Element parameter = findParameter(document, "callbackUrl");

        assertNotNull(parameter);
        Node advanced = parameter.getElementsByTagName("advanced").item(0);
        assertNotNull(advanced);
        assertEquals("true", advanced.getTextContent());
    }

    @Test
    void configParameterLabelsUseTitleStyleCapitalization() throws Exception {
        Document accountDocument = parseThingDescription("OH-INF/thing/account.xml");
        Document sceneDocument = parseThingDescription("OH-INF/thing/scene.xml");

        assertEquals("App Name", findParameterLabel(accountDocument, "appName"));
        assertEquals("Create Things Using Dynamic Capability Discovery",
                findParameterLabel(accountDocument, "useDynamicThings"));
        assertEquals("Location ID (Optional)", findParameterLabel(sceneDocument, "locationId"));
    }

    @Test
    void airConditionerThingExposesEssentialChannels() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/airconditioner.xml");
        Map<String, ExpectedChannel> expectedChannels = Map.ofEntries(
                Map.entry("switch", new ExpectedChannel("control", "system.power", "switch", "switch")),
                Map.entry("mode",
                        new ExpectedChannel("control", "ac-mode", "airConditionerMode", "airConditionerMode")),
                Map.entry("fan-mode", new ExpectedChannel("control", "fan-mode", "airConditionerFanMode", "fanMode")),
                Map.entry("fan-oscillation-mode",
                        new ExpectedChannel("control", "fan-oscillation-mode", "fanOscillationMode",
                                "fanOscillationMode")),
                Map.entry("setpoint",
                        new ExpectedChannel("control", "ac-setpoint", "thermostatCoolingSetpoint", "coolingSetpoint")),
                Map.entry("temperature",
                        new ExpectedChannel("environment", "ac-temperature", "temperatureMeasurement", "temperature")),
                Map.entry("humidity",
                        new ExpectedChannel("environment", "ac-humidity", "relativeHumidityMeasurement", "humidity")),
                Map.entry("power",
                        new ExpectedChannel("energy", "ac-power-consumption", "powerConsumptionReport",
                                "powerConsumption")),
                Map.entry("energy",
                        new ExpectedChannel("energy", "ac-energy", "powerConsumptionReport", "powerConsumption")),
                Map.entry("operating-state",
                        new ExpectedChannel("advanced", "ac-auto-cleaning-operating-state", "custom.autoCleaningMode",
                                "operatingState")),
                Map.entry("progress",
                        new ExpectedChannel("advanced", "ac-auto-cleaning-progress", "custom.autoCleaningMode",
                                "progress")),
                Map.entry("dust-filter-status",
                        new ExpectedChannel("advanced", "ac-filter-status", "custom.dustFilter", "dustFilterStatus")));

        assertEquals(12, getChannelCount(document));
        assertEquals(Set.of("control", "environment", "energy", "advanced"), findChannelGroupIds(document));

        for (Map.Entry<String, ExpectedChannel> entry : expectedChannels.entrySet()) {
            Element channel = findChannel(document, entry.getKey());
            ExpectedChannel expected = entry.getValue();

            assertEquals(expected.groupId(), findChannelGroupId(document, channel), entry.getKey());
            assertEquals(expected.typeId(), channel.getAttribute("typeId"), entry.getKey());
            assertEquals("main", findPropertyValue(channel, "component"), entry.getKey());
            assertEquals(expected.capability(), findPropertyValue(channel, "capability"), entry.getKey());
            assertEquals(expected.attribute(), findPropertyValue(channel, "attribute"), entry.getKey());
        }
    }

    @Test
    void airConditionerThingKeepsSecondaryChannelsAdvanced() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/airconditioner.xml");
        Set<String> advancedChannels = Set.of("operating-state", "progress", "dust-filter-status");

        assertEquals(3, advancedChannels.size());
        assertEquals(12, getChannelCount(document));

        NodeList channels = document.getElementsByTagName("channel");
        int advancedCount = 0;
        for (int i = 0; i < channels.getLength(); i++) {
            Element channel = (Element) channels.item(i);
            String channelId = channel.getAttribute("id");
            boolean isAdvanced = isAdvancedChannelType(document, channel.getAttribute("typeId"));
            if (advancedChannels.contains(channelId)) {
                assertTrue(isAdvanced, channelId);
                assertEquals("advanced", findChannelGroupId(document, channel), channelId);
                advancedCount++;
            } else {
                assertFalse(isAdvanced, channelId);
            }
        }

        assertEquals(3, advancedCount);
    }

    @Test
    void airConditionerChannelTypesUseSuitableItemTypes() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/airconditioner.xml");
        Map<String, String> expectedItemTypes = Map.ofEntries(Map.entry("ac-mode", "String"),
                Map.entry("fan-mode", "String"), Map.entry("fan-oscillation-mode", "String"),
                Map.entry("ac-temperature", "Number:Temperature"), Map.entry("ac-setpoint", "Number:Temperature"),
                Map.entry("ac-humidity", "Number:Dimensionless"), Map.entry("ac-power-consumption", "Number:Power"),
                Map.entry("ac-energy", "Number:Energy"),
                Map.entry("ac-auto-cleaning-progress", "Number:Dimensionless"));

        for (Map.Entry<String, String> entry : expectedItemTypes.entrySet()) {
            assertEquals(entry.getValue(), findChannelTypeItemType(document, entry.getKey()), entry.getKey());
        }
        assertEquals("Wh", findPropertyValue(findChannel(document, "energy"), "unit"));
        assertEquals("%.0f Wh", findChannelTypeStatePattern(document, "ac-energy"));
        assertEquals(Map.of("auto", "Auto", "cool", "Cool", "dry", "Dry", "fanOnly", "Fan Only", "heat", "Heat", "wind",
                "Clean"), findChannelTypeStateOptions(document, "ac-mode"));
        assertEquals(
                Map.of("auto", "Auto", "low", "Low", "medium", "Medium", "high", "High", "turbo", "Turbo", "quiet",
                        "Quiet", "sleep", "Sleep", "windFree", "WindFree"),
                findChannelTypeStateOptions(document, "fan-mode"));
        assertEquals("Setpoint", findChannelTypeLabel(document, "ac-setpoint"));
    }

    @Test
    void airConditionerWritableChannelsDeclareSmartThingsCommands() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/airconditioner.xml");
        Map<String, String> expectedCommands = Map.of("mode", "setAirConditionerMode", "fan-mode", "setFanMode",
                "fan-oscillation-mode", "setFanOscillationMode", "setpoint", "setCoolingSetpoint");

        for (Map.Entry<String, String> entry : expectedCommands.entrySet()) {
            assertEquals(entry.getValue(), findPropertyValue(findChannel(document, entry.getKey()), "command"),
                    entry.getKey());
        }
    }

    @Test
    void airConditionerDoesNotExposeOptionalModeOrAutoCleaningModeCommandChannels() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/airconditioner.xml");

        assertFalse(hasChannel(document, "ac-optional-mode"));
        assertFalse(hasChannelType(document, "ac-optional-mode"));
        assertFalse(hasChannelType(document, "air-conditioner-mode"));
        assertFalse(hasChannelType(document, "ac-cooling-setpoint"));
        assertFalse(hasChannel(document, "auto-cleaning-mode"));
        assertFalse(hasChannelType(document, "ac-auto-cleaning-mode"));
    }

    @Test
    void genericTelevisionUsesCleanLabelsAndDescription() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/tv.xml");

        assertEquals("SmartThings TV", findThingTypeLabel(document, "generic-television"));
        assertEquals("A television connected via SmartThings Cloud.",
                findThingTypeDescription(document, "generic-television"));
        assertEquals(Set.of("control", "picture", "remote"),
                findThingTypeChannelGroupIds(document, "generic-television"));
        assertEquals("tv-control-group", findThingTypeChannelGroupTypeId(document, "generic-television", "control"));
        assertEquals("tv-picture-group", findThingTypeChannelGroupTypeId(document, "generic-television", "picture"));
        assertEquals("tv-remote-group", findThingTypeChannelGroupTypeId(document, "generic-television", "remote"));
        assertEquals("Control", findChannelGroupTypeLabel(document, "tv-control-group"));
        assertFalse(hasChannelGroupType(document, "main-group-type-generic-tv"));

        assertExpectedChannels(document, Map.ofEntries(
                Map.entry("switch", new ExpectedChannel("tv-control-group", "system.power", "switch", "switch")),
                Map.entry("volume", new ExpectedChannel("tv-control-group", "system.volume", "audioVolume", "volume")),
                Map.entry("mute", new ExpectedChannel("tv-control-group", "system.mute", "audioMute", "mute")),
                Map.entry("input-source",
                        new ExpectedChannel("tv-control-group", "tv-input-source", "mediaInputSource", "inputSource")),
                Map.entry("channel", new ExpectedChannel("tv-control-group", "tv-channel", "tvChannel", "tvChannel")),
                Map.entry("playback",
                        new ExpectedChannel("tv-control-group", "system.media-control", "mediaPlayback",
                                "playbackStatus")),
                Map.entry("picture-mode",
                        new ExpectedChannel("tv-picture-group", "tv-picture-mode", "custom.picturemode",
                                "pictureMode")),
                Map.entry("sound-mode",
                        new ExpectedChannel("tv-picture-group", "tv-sound-mode", "custom.soundmode", "soundMode")),
                Map.entry("channel-up",
                        new ExpectedChannel("tv-remote-group", "tv-channel-up", "tvChannel", "tvChannel")),
                Map.entry("channel-down",
                        new ExpectedChannel("tv-remote-group", "tv-channel-down", "tvChannel", "tvChannel"))));
    }

    @Test
    void frameTvThingExposesUsefulChannels() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/tv.xml");
        Map<String, ExpectedChannel> expectedChannels = Map.ofEntries(
                Map.entry("switch", new ExpectedChannel("frame-control-group", "system.power", "switch", "switch")),
                Map.entry("volume",
                        new ExpectedChannel("frame-control-group", "system.volume", "audioVolume", "volume")),
                Map.entry("mute", new ExpectedChannel("frame-control-group", "system.mute", "audioMute", "mute")),
                Map.entry("input-source",
                        new ExpectedChannel("frame-control-group", "tv-input-source", "mediaInputSource",
                                "inputSource")),
                Map.entry("channel",
                        new ExpectedChannel("frame-control-group", "tv-channel", "tvChannel", "tvChannel")),
                Map.entry("playback",
                        new ExpectedChannel("frame-control-group", "system.media-control", "mediaPlayback",
                                "playbackStatus")),
                Map.entry("art-mode",
                        new ExpectedChannel("frame-control-group", "frame-art-mode", "samsungvd.ambient", "ambient")),
                Map.entry("picture-mode",
                        new ExpectedChannel("tv-picture-group", "tv-picture-mode", "custom.picturemode",
                                "pictureMode")),
                Map.entry("sound-mode",
                        new ExpectedChannel("tv-picture-group", "tv-sound-mode", "custom.soundmode", "soundMode")),
                Map.entry("channel-up",
                        new ExpectedChannel("tv-remote-group", "tv-channel-up", "tvChannel", "tvChannel")),
                Map.entry("channel-down",
                        new ExpectedChannel("tv-remote-group", "tv-channel-down", "tvChannel", "tvChannel")));

        assertEquals("Samsung The Frame", findThingTypeLabel(document, "Samsung_The_Frame"));
        assertEquals(Set.of("control", "picture", "remote"),
                findThingTypeChannelGroupIds(document, "Samsung_The_Frame"));
        assertEquals("frame-control-group", findThingTypeChannelGroupTypeId(document, "Samsung_The_Frame", "control"));
        assertEquals("tv-picture-group", findThingTypeChannelGroupTypeId(document, "Samsung_The_Frame", "picture"));
        assertEquals("tv-remote-group", findThingTypeChannelGroupTypeId(document, "Samsung_The_Frame", "remote"));

        assertExpectedChannels(document, expectedChannels);
    }

    @Test
    void frameTvKeepsRemoteChannelsAdvancedAndOmitsInfoChannels() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/tv.xml");

        assertTrue(isAdvancedChannelType(document,
                findChannel(document, "tv-remote-group", "channel-up").getAttribute("typeId")));
        assertTrue(isAdvancedChannelType(document,
                findChannel(document, "tv-remote-group", "channel-down").getAttribute("typeId")));
        for (String groupId : Set.of("frame-control-group", "tv-picture-group", "tv-remote-group")) {
            assertFalse(hasChannel(document, groupId, "firmware-version"));
            assertFalse(hasChannel(document, groupId, "diagnostics"));
            assertFalse(hasChannel(document, groupId, "error"));
            assertFalse(hasChannel(document, groupId, "online-status"));
            assertFalse(hasChannel(document, groupId, "tv-channel-name"));
        }
    }

    @Test
    void frameTvWritableChannelsDeclareSmartThingsCommands() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/tv.xml");

        assertEquals("setVolume", findPropertyValue(findChannel(document, "frame-control-group", "volume"), "command"));
        assertEquals("setInputSource",
                findPropertyValue(findChannel(document, "frame-control-group", "input-source"), "command"));
        assertEquals("setTvChannel",
                findPropertyValue(findChannel(document, "frame-control-group", "channel"), "command"));
        assertEquals("setAmbientOn",
                findPropertyValue(findChannel(document, "frame-control-group", "art-mode"), "command"));
        assertEquals("no-argument-command", findPropertyValue(findChannel(document, "frame-control-group", "art-mode"),
                SmartThingsBindingConstants.CONVERTER));
        assertEquals("media-control", findPropertyValue(findChannel(document, "frame-control-group", "playback"),
                SmartThingsBindingConstants.CONVERTER));
        assertEquals("setPictureMode",
                findPropertyValue(findChannel(document, "tv-picture-group", "picture-mode"), "command"));
        assertEquals("setSoundMode",
                findPropertyValue(findChannel(document, "tv-picture-group", "sound-mode"), "command"));
        assertEquals("channelUp", findPropertyValue(findChannel(document, "tv-remote-group", "channel-up"), "command"));
        assertEquals("no-argument-command", findPropertyValue(findChannel(document, "tv-remote-group", "channel-up"),
                SmartThingsBindingConstants.CONVERTER));
        assertEquals("channelDown",
                findPropertyValue(findChannel(document, "tv-remote-group", "channel-down"), "command"));
        assertEquals("no-argument-command", findPropertyValue(findChannel(document, "tv-remote-group", "channel-down"),
                SmartThingsBindingConstants.CONVERTER));
    }

    @Test
    void tvOnlyCommandOnlyChannelsDoNotPredictStateUpdates() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/tv.xml");

        for (String groupId : Set.of("tv-control-group", "frame-control-group")) {
            for (String channelId : Set.of("switch", "volume", "mute", "playback")) {
                assertFalse(hasChannelAutoUpdatePolicy(document, groupId, channelId), groupId + "#" + channelId);
            }
        }

        for (String channelTypeId : Set.of("tv-input-source", "tv-channel", "frame-art-mode", "tv-picture-mode",
                "tv-sound-mode")) {
            assertFalse(hasChannelTypeAutoUpdatePolicy(document, channelTypeId), channelTypeId);
        }

        for (String channelTypeId : Set.of("tv-channel-up", "tv-channel-down")) {
            assertEquals("veto", findChannelTypeAutoUpdatePolicy(document, channelTypeId), channelTypeId);
        }
    }

    @Test
    void frameTvChannelTypesUseSuitableItemTypes() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/tv.xml");
        Map<String, String> expectedItemTypes = Map.of("tv-input-source", "String", "tv-channel", "String",
                "frame-art-mode", "Switch", "tv-picture-mode", "String", "tv-sound-mode", "String", "tv-channel-up",
                "Switch", "tv-channel-down", "Switch");

        for (Map.Entry<String, String> entry : expectedItemTypes.entrySet()) {
            assertEquals(entry.getValue(), findChannelTypeItemType(document, entry.getKey()), entry.getKey());
        }
        assertFalse(isChannelTypeReadOnly(document, "frame-art-mode"));
        assertFalse(hasChannelTypeAutoUpdatePolicy(document, "frame-art-mode"));
    }

    @Test
    void ovenThingExposesStatusChannels() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/oven.xml");

        assertEquals("Samsung Oven", findThingTypeLabel(document, "Samsung_Oven"));
        assertEquals("A Samsung oven connected via SmartThings Cloud.",
                findThingTypeDescription(document, "Samsung_Oven"));
        assertEquals(Set.of("status"), findThingTypeChannelGroupIds(document, "Samsung_Oven"));
        assertEquals("oven-status-group", findThingTypeChannelGroupTypeId(document, "Samsung_Oven", "status"));

        assertExpectedChannels(document,
                Map.ofEntries(
                        Map.entry("completion-time",
                                new ExpectedChannel("oven-status-group", "oven-completion-time",
                                        "samsungce.ovenOperatingState", "completionTime")),
                        Map.entry("operating-state",
                                new ExpectedChannel("oven-status-group", "oven-operating-state",
                                        "samsungce.ovenOperatingState", "operatingState")),
                        Map.entry("progress",
                                new ExpectedChannel("oven-status-group", "oven-progress",
                                        "samsungce.ovenOperatingState", "progress")),
                        Map.entry("oven-job-state",
                                new ExpectedChannel("oven-status-group", "oven-job-state",
                                        "samsungce.ovenOperatingState", "ovenJobState")),
                        Map.entry("operation-time", new ExpectedChannel("oven-status-group", "oven-operation-time",
                                "samsungce.ovenOperatingState", "operationTime"))));

        for (String channelTypeId : Set.of("oven-operating-state", "oven-job-state")) {
            assertEquals("String", findChannelTypeItemType(document, channelTypeId), channelTypeId);
        }
        assertEquals("DateTime", findChannelTypeItemType(document, "oven-completion-time"));
        assertEquals("DateTime", findChannelTypeItemType(document, "oven-operation-time"));
        assertEquals("Number:Dimensionless", findChannelTypeItemType(document, "oven-progress"));
        assertEquals("%.0f %%", findChannelTypeStatePattern(document, "oven-progress"));

        for (String channelTypeId : Set.of("oven-completion-time", "oven-operating-state", "oven-progress",
                "oven-job-state", "oven-operation-time")) {
            assertTrue(isChannelTypeReadOnly(document, channelTypeId), channelTypeId);
        }
        assertTrue(isAdvancedChannelType(document, "oven-operating-state"));
        assertFalse(isAdvancedChannelType(document, "oven-operation-time"));
    }

    @Test
    void soundbarThingExposesControlChannels() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/soundbar.xml");

        assertEquals("Samsung Soundbar", findThingTypeLabel(document, "Samsung_Soundbar"));
        assertEquals("A Samsung soundbar connected via SmartThings Cloud.",
                findThingTypeDescription(document, "Samsung_Soundbar"));
        assertEquals(Set.of("control"), findThingTypeChannelGroupIds(document, "Samsung_Soundbar"));
        assertEquals("soundbar-control-group",
                findThingTypeChannelGroupTypeId(document, "Samsung_Soundbar", "control"));

        assertExpectedChannels(document, Map.ofEntries(
                Map.entry("switch", new ExpectedChannel("soundbar-control-group", "system.power", "switch", "switch")),
                Map.entry("volume",
                        new ExpectedChannel("soundbar-control-group", "system.volume", "audioVolume", "volume")),
                Map.entry("mute", new ExpectedChannel("soundbar-control-group", "system.mute", "audioMute", "mute")),
                Map.entry("input-source",
                        new ExpectedChannel("soundbar-control-group", "soundbar-input-source",
                                "samsungvd.audioInputSource", "inputSource")),
                Map.entry("playback", new ExpectedChannel("soundbar-control-group", "system.media-control",
                        "mediaPlayback", "supportedPlaybackCommands"))));

        assertEquals("String", findChannelTypeItemType(document, "soundbar-input-source"));
        assertEquals("setInputSource",
                findPropertyValue(findChannel(document, "soundbar-control-group", "input-source"), "command"));
        assertFalse(hasChannelTypeAutoUpdatePolicy(document, "soundbar-input-source"));
    }

    @Test
    void soundbarOnlyMetadataBackedPlaybackChannelDoesNotPredictStateUpdates() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/soundbar.xml");

        for (String channelId : Set.of("switch", "volume", "mute")) {
            assertFalse(hasChannelAutoUpdatePolicy(document, "soundbar-control-group", channelId), channelId);
        }
        assertEquals("veto", findChannelAutoUpdatePolicy(document, "soundbar-control-group", "playback"));
    }

    private Document parseThingDescription(String resourceName) throws Exception {
        InputStream stream = SmartThingsThingDescriptionTest.class.getResourceAsStream("/" + resourceName);
        assertNotNull(stream);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(stream);
    }

    private void assertExpectedChannels(Document document, Map<String, ExpectedChannel> expectedChannels) {
        for (Map.Entry<String, ExpectedChannel> entry : expectedChannels.entrySet()) {
            ExpectedChannel expected = entry.getValue();
            Element channel = findChannel(document, expected.groupId(), entry.getKey());

            assertEquals(expected.typeId(), channel.getAttribute("typeId"), entry.getKey());
            assertEquals("main", findPropertyValue(channel, "component"), entry.getKey());
            assertEquals(expected.capability(), findPropertyValue(channel, "capability"), entry.getKey());
            assertEquals(expected.attribute(), findPropertyValue(channel, "attribute"), entry.getKey());
        }
    }

    private Element findParameter(Document document, String parameterName) {
        NodeList parameters = document.getElementsByTagName("parameter");
        for (int i = 0; i < parameters.getLength(); i++) {
            Element parameter = (Element) parameters.item(i);
            if (parameterName.equals(parameter.getAttribute("name"))) {
                return parameter;
            }
        }
        throw new AssertionError("Missing parameter: " + parameterName);
    }

    private Element findChannel(Document document, String channelId) {
        NodeList channels = document.getElementsByTagName("channel");
        for (int i = 0; i < channels.getLength(); i++) {
            Element channel = (Element) channels.item(i);
            if (channelId.equals(channel.getAttribute("id"))) {
                return channel;
            }
        }
        throw new AssertionError("Missing channel: " + channelId);
    }

    private Element findChannel(Document document, String groupTypeId, String channelId) {
        Element groupType = findChannelGroupType(document, groupTypeId);
        NodeList channels = groupType.getElementsByTagName("channel");
        for (int i = 0; i < channels.getLength(); i++) {
            Element channel = (Element) channels.item(i);
            if (channelId.equals(channel.getAttribute("id"))) {
                return channel;
            }
        }
        throw new AssertionError("Missing channel: " + groupTypeId + "#" + channelId);
    }

    private boolean hasChannel(Document document, String channelId) {
        NodeList channels = document.getElementsByTagName("channel");
        for (int i = 0; i < channels.getLength(); i++) {
            Element channel = (Element) channels.item(i);
            if (channelId.equals(channel.getAttribute("id"))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasChannel(Document document, String groupTypeId, String channelId) {
        Element groupType = findChannelGroupType(document, groupTypeId);
        NodeList channels = groupType.getElementsByTagName("channel");
        for (int i = 0; i < channels.getLength(); i++) {
            Element channel = (Element) channels.item(i);
            if (channelId.equals(channel.getAttribute("id"))) {
                return true;
            }
        }
        return false;
    }

    private int getChannelCount(Document document) {
        return document.getElementsByTagName("channel").getLength();
    }

    private String findPropertyValue(Element channel, String propertyName) {
        NodeList properties = channel.getElementsByTagName("property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if (propertyName.equals(property.getAttribute("name"))) {
                return property.getTextContent();
            }
        }
        throw new AssertionError("Missing channel property: " + propertyName);
    }

    private String findThingTypeLabel(Document document, String thingTypeId) {
        Element thingType = findThingType(document, thingTypeId);
        Node label = thingType.getElementsByTagName("label").item(0);
        assertNotNull(label);
        return label.getTextContent();
    }

    private String findThingTypeDescription(Document document, String thingTypeId) {
        Element thingType = findThingType(document, thingTypeId);
        Node description = thingType.getElementsByTagName("description").item(0);
        assertNotNull(description);
        return description.getTextContent();
    }

    private Set<String> findThingTypeChannelGroupIds(Document document, String thingTypeId) {
        Set<String> groupIds = new HashSet<>();
        Element thingType = findThingType(document, thingTypeId);
        NodeList channelGroups = thingType.getElementsByTagName("channel-group");
        for (int i = 0; i < channelGroups.getLength(); i++) {
            Element channelGroup = (Element) channelGroups.item(i);
            groupIds.add(channelGroup.getAttribute("id"));
        }
        return groupIds;
    }

    private String findThingTypeChannelGroupTypeId(Document document, String thingTypeId, String channelGroupId) {
        Element thingType = findThingType(document, thingTypeId);
        NodeList channelGroups = thingType.getElementsByTagName("channel-group");
        for (int i = 0; i < channelGroups.getLength(); i++) {
            Element channelGroup = (Element) channelGroups.item(i);
            if (channelGroupId.equals(channelGroup.getAttribute("id"))) {
                return channelGroup.getAttribute("typeId");
            }
        }
        throw new AssertionError("Missing channel group: " + thingTypeId + "#" + channelGroupId);
    }

    private Element findThingType(Document document, String thingTypeId) {
        NodeList thingTypes = document.getElementsByTagName("thing-type");
        for (int i = 0; i < thingTypes.getLength(); i++) {
            Element thingType = (Element) thingTypes.item(i);
            if (thingTypeId.equals(thingType.getAttribute("id"))) {
                return thingType;
            }
        }
        throw new AssertionError("Missing thing type: " + thingTypeId);
    }

    private Set<String> findChannelGroupIds(Document document) {
        Set<String> groupIds = new HashSet<>();
        NodeList channelGroups = document.getElementsByTagName("channel-group");
        for (int i = 0; i < channelGroups.getLength(); i++) {
            Element channelGroup = (Element) channelGroups.item(i);
            groupIds.add(channelGroup.getAttribute("id"));
        }
        return groupIds;
    }

    private String findChannelGroupId(Document document, Element channel) {
        Node channelGroupType = channel.getParentNode().getParentNode();
        if (channelGroupType instanceof Element groupTypeElement) {
            String groupTypeId = groupTypeElement.getAttribute("id");
            NodeList channelGroups = document.getElementsByTagName("channel-group");
            for (int i = 0; i < channelGroups.getLength(); i++) {
                Element channelGroup = (Element) channelGroups.item(i);
                if (groupTypeId.equals(channelGroup.getAttribute("typeId"))) {
                    return channelGroup.getAttribute("id");
                }
            }
        }
        throw new AssertionError("Missing channel group for channel: " + channel.getAttribute("id"));
    }

    private Element findChannelGroupType(Document document, String channelGroupTypeId) {
        NodeList channelGroupTypes = document.getElementsByTagName("channel-group-type");
        for (int i = 0; i < channelGroupTypes.getLength(); i++) {
            Element channelGroupType = (Element) channelGroupTypes.item(i);
            if (channelGroupTypeId.equals(channelGroupType.getAttribute("id"))) {
                return channelGroupType;
            }
        }
        throw new AssertionError("Missing channel group type: " + channelGroupTypeId);
    }

    private boolean hasChannelGroupType(Document document, String channelGroupTypeId) {
        NodeList channelGroupTypes = document.getElementsByTagName("channel-group-type");
        for (int i = 0; i < channelGroupTypes.getLength(); i++) {
            Element channelGroupType = (Element) channelGroupTypes.item(i);
            if (channelGroupTypeId.equals(channelGroupType.getAttribute("id"))) {
                return true;
            }
        }
        return false;
    }

    private String findChannelGroupTypeLabel(Document document, String channelGroupTypeId) {
        Element channelGroupType = findChannelGroupType(document, channelGroupTypeId);
        Node label = channelGroupType.getElementsByTagName("label").item(0);
        assertNotNull(label);
        return label.getTextContent();
    }

    private String findChannelTypeItemType(Document document, String channelTypeId) {
        Element channelType = findChannelType(document, channelTypeId);
        Node itemType = channelType.getElementsByTagName("item-type").item(0);
        assertNotNull(itemType);
        return itemType.getTextContent();
    }

    private String findChannelTypeStatePattern(Document document, String channelTypeId) {
        Element channelType = findChannelType(document, channelTypeId);
        Node state = channelType.getElementsByTagName("state").item(0);
        assertNotNull(state);
        return ((Element) state).getAttribute("pattern");
    }

    private boolean isChannelTypeReadOnly(Document document, String channelTypeId) {
        Element channelType = findChannelType(document, channelTypeId);
        Node state = channelType.getElementsByTagName("state").item(0);
        return state instanceof Element element && "true".equals(element.getAttribute("readOnly"));
    }

    private String findChannelTypeAutoUpdatePolicy(Document document, String channelTypeId) {
        Element channelType = findChannelType(document, channelTypeId);
        Node autoUpdatePolicy = channelType.getElementsByTagName("autoUpdatePolicy").item(0);
        assertNotNull(autoUpdatePolicy);
        return autoUpdatePolicy.getTextContent();
    }

    private boolean hasChannelTypeAutoUpdatePolicy(Document document, String channelTypeId) {
        Element channelType = findChannelType(document, channelTypeId);
        return channelType.getElementsByTagName("autoUpdatePolicy").getLength() > 0;
    }

    private String findChannelAutoUpdatePolicy(Document document, String groupId, String channelId) {
        Element channel = findChannel(document, groupId, channelId);
        Node autoUpdatePolicy = channel.getElementsByTagName("autoUpdatePolicy").item(0);
        assertNotNull(autoUpdatePolicy);
        return autoUpdatePolicy.getTextContent();
    }

    private boolean hasChannelAutoUpdatePolicy(Document document, String groupId, String channelId) {
        Element channel = findChannel(document, groupId, channelId);
        return channel.getElementsByTagName("autoUpdatePolicy").getLength() > 0;
    }

    private String findChannelTypeLabel(Document document, String channelTypeId) {
        Element channelType = findChannelType(document, channelTypeId);
        Node label = channelType.getElementsByTagName("label").item(0);
        assertNotNull(label);
        return label.getTextContent();
    }

    private Map<String, String> findChannelTypeStateOptions(Document document, String channelTypeId) {
        Map<String, String> result = new HashMap<>();
        Element channelType = findChannelType(document, channelTypeId);
        NodeList options = channelType.getElementsByTagName("option");
        for (int i = 0; i < options.getLength(); i++) {
            Element option = (Element) options.item(i);
            result.put(option.getAttribute("value"), option.getTextContent());
        }
        return result;
    }

    private Element findChannelType(Document document, String channelTypeId) {
        NodeList channelTypes = document.getElementsByTagName("channel-type");
        for (int i = 0; i < channelTypes.getLength(); i++) {
            Element channelType = (Element) channelTypes.item(i);
            if (channelTypeId.equals(channelType.getAttribute("id"))) {
                return channelType;
            }
        }
        throw new AssertionError("Missing channel type: " + channelTypeId);
    }

    private boolean hasChannelType(Document document, String channelTypeId) {
        NodeList channelTypes = document.getElementsByTagName("channel-type");
        for (int i = 0; i < channelTypes.getLength(); i++) {
            Element channelType = (Element) channelTypes.item(i);
            if (channelTypeId.equals(channelType.getAttribute("id"))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdvancedChannelType(Document document, String channelTypeId) {
        if (channelTypeId.startsWith("system.")) {
            return false;
        }
        return "true".equals(findChannelType(document, channelTypeId).getAttribute("advanced"));
    }

    private String findParameterLabel(Document document, String parameterName) {
        Element parameter = findParameter(document, parameterName);
        Node label = parameter.getElementsByTagName("label").item(0);
        assertNotNull(label);
        return label.getTextContent();
    }
}

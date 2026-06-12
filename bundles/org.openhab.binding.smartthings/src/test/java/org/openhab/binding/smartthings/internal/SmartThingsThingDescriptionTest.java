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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
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
                Map.entry("air-conditioner-mode",
                        new ExpectedChannel("control", "air-conditioner-mode", "airConditionerMode",
                                "airConditionerMode")),
                Map.entry("fan-mode", new ExpectedChannel("control", "fan-mode", "airConditionerFanMode", "fanMode")),
                Map.entry("fan-oscillation-mode",
                        new ExpectedChannel("control", "fan-oscillation-mode", "fanOscillationMode",
                                "fanOscillationMode")),
                Map.entry("cooling-setpoint",
                        new ExpectedChannel("control", "ac-cooling-setpoint", "thermostatCoolingSetpoint",
                                "coolingSetpoint")),
                Map.entry("temperature",
                        new ExpectedChannel("environment", "ac-temperature", "temperatureMeasurement", "temperature")),
                Map.entry("humidity",
                        new ExpectedChannel("environment", "ac-humidity", "relativeHumidityMeasurement", "humidity")),
                Map.entry("power",
                        new ExpectedChannel("energy", "ac-power-consumption", "powerConsumptionReport",
                                "powerConsumption")),
                Map.entry("energy",
                        new ExpectedChannel("energy", "ac-energy", "powerConsumptionReport", "powerConsumption")),
                Map.entry("ac-optional-mode",
                        new ExpectedChannel("advanced", "ac-optional-mode", "custom.airConditionerOptionalMode",
                                "acOptionalMode")),
                Map.entry("auto-cleaning-mode",
                        new ExpectedChannel("advanced", "ac-auto-cleaning-mode", "custom.autoCleaningMode",
                                "autoCleaningMode")),
                Map.entry("operating-state",
                        new ExpectedChannel("advanced", "ac-auto-cleaning-operating-state", "custom.autoCleaningMode",
                                "operatingState")),
                Map.entry("progress",
                        new ExpectedChannel("advanced", "ac-auto-cleaning-progress", "custom.autoCleaningMode",
                                "progress")),
                Map.entry("dust-filter-status",
                        new ExpectedChannel("advanced", "ac-filter-status", "custom.dustFilter", "dustFilterStatus")));

        assertEquals(14, getChannelCount(document));
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
        Set<String> advancedChannels = Set.of("ac-optional-mode", "auto-cleaning-mode", "operating-state", "progress",
                "dust-filter-status");

        assertEquals(5, advancedChannels.size());
        assertEquals(14, getChannelCount(document));

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

        assertEquals(5, advancedCount);
    }

    @Test
    void airConditionerChannelTypesUseSuitableItemTypes() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/airconditioner.xml");
        Map<String, String> expectedItemTypes = Map.ofEntries(Map.entry("air-conditioner-mode", "String"),
                Map.entry("fan-mode", "String"), Map.entry("fan-oscillation-mode", "String"),
                Map.entry("ac-temperature", "Number:Temperature"),
                Map.entry("ac-cooling-setpoint", "Number:Temperature"),
                Map.entry("ac-humidity", "Number:Dimensionless"), Map.entry("ac-power-consumption", "Number:Power"),
                Map.entry("ac-energy", "Number:Energy"), Map.entry("ac-auto-cleaning-mode", "Switch"),
                Map.entry("ac-auto-cleaning-progress", "Number:Dimensionless"));

        for (Map.Entry<String, String> entry : expectedItemTypes.entrySet()) {
            assertEquals(entry.getValue(), findChannelTypeItemType(document, entry.getKey()), entry.getKey());
        }
        assertEquals("Wh", findPropertyValue(findChannel(document, "energy"), "unit"));
        assertEquals("%.0f Wh", findChannelTypeStatePattern(document, "ac-energy"));
        assertEquals(Map.of("off", "Off", "quiet", "Quiet", "sleep", "Sleep", "windFree", "WindFree"),
                findChannelTypeStateOptions(document, "ac-optional-mode"));
    }

    private Document parseThingDescription(String resourceName) throws Exception {
        InputStream stream = SmartThingsThingDescriptionTest.class.getResourceAsStream("/" + resourceName);
        assertNotNull(stream);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(stream);
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

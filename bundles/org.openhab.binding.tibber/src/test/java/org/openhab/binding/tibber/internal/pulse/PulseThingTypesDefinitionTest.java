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
package org.openhab.binding.tibber.internal.pulse;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Tests the metadata of the binding against the {@code channels} and {@code thing-lifecycle} specs: "Channel set",
 * "Channel types and read-only", "Channel presence", "Skeleton content removed", and "Configuration parameters". The
 * thing type file of the Pulse part ({@code pulse-thing-types.xml}) is read as XML from the class path, so the test
 * sees what the framework will load. Checks across the three XML files of the binding are in
 * {@link MergedDefinitionsTest}.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class PulseThingTypesDefinitionTest {

    /** Channel id to item type, as in the "Channel set" table. */
    private static final Map<String, String> ITEM_TYPES = new LinkedHashMap<>();
    static {
        ITEM_TYPES.put("power", "Number:Power");
        ITEM_TYPES.put("energy-import", "Number:Energy");
        ITEM_TYPES.put("energy-export", "Number:Energy");
        ITEM_TYPES.put("power-l1", "Number:Power");
        ITEM_TYPES.put("power-l2", "Number:Power");
        ITEM_TYPES.put("power-l3", "Number:Power");
        ITEM_TYPES.put("voltage-l1", "Number:ElectricPotential");
        ITEM_TYPES.put("voltage-l2", "Number:ElectricPotential");
        ITEM_TYPES.put("voltage-l3", "Number:ElectricPotential");
        ITEM_TYPES.put("current-l1", "Number:ElectricCurrent");
        ITEM_TYPES.put("current-l2", "Number:ElectricCurrent");
        ITEM_TYPES.put("current-l3", "Number:ElectricCurrent");
        ITEM_TYPES.put("frequency", "Number:Frequency");
    }

    private static final Set<String> ADVANCED = Set.of("power-l1", "power-l2", "power-l3", "voltage-l1", "voltage-l2",
            "voltage-l3", "current-l1", "current-l2", "current-l3", "frequency");

    private @Nullable Document document;
    private @Nullable Document cloudChannelTypes;
    private String text = "";

    @BeforeEach
    void load() throws Exception {
        try (InputStream in = PulseThingTypesDefinitionTest.class
                .getResourceAsStream("/OH-INF/thing/pulse-thing-types.xml")) {
            assertNotNull(in);
            byte[] bytes = in.readAllBytes();
            text = new String(bytes, StandardCharsets.UTF_8);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            document = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(bytes));
        }
        try (InputStream in = PulseThingTypesDefinitionTest.class
                .getResourceAsStream("/OH-INF/thing/channel-types.xml")) {
            assertNotNull(in);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            cloudChannelTypes = factory.newDocumentBuilder().parse(in);
        }
    }

    private Document doc() {
        Document d = document;
        assertNotNull(d);
        return d;
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && name.equals(element.getTagName())) {
                result.add(element);
            }
        }
        return result;
    }

    private static @Nullable Element child(Element parent, String name) {
        List<Element> found = children(parent, name);
        return found.isEmpty() ? null : found.get(0);
    }

    private static Element required(Element parent, String name) {
        Element element = child(parent, name);
        assertNotNull(element, name);
        return element;
    }

    private Element byId(String tag, String id) {
        for (Element element : children(doc().getDocumentElement(), tag)) {
            if (id.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        throw new AssertionError("no <" + tag + "> with id " + id);
    }

    /**
     * The channel type of a meter channel, from the Pulse file or from the channel types shared with the cloud part.
     */
    private Element channelTypeOf(Element channel) {
        String typeId = channel.getAttribute("typeId");
        Document shared = cloudChannelTypes;
        assertNotNull(shared);
        for (Document source : List.of(doc(), shared)) {
            for (Element element : children(source.getDocumentElement(), "channel-type")) {
                if (typeId.equals(element.getAttribute("id"))) {
                    return element;
                }
            }
        }
        throw new AssertionError("no channel type " + typeId + " for channel " + channel.getAttribute("id"));
    }

    private static Element parameter(Element thingType, String name) {
        Element config = child(thingType, "config-description");
        assertNotNull(config);
        for (Element parameter : children(config, "parameter")) {
            if (name.equals(parameter.getAttribute("name"))) {
                return parameter;
            }
        }
        throw new AssertionError("no parameter " + name);
    }

    private static String textOf(Element parent, String name) {
        Element element = child(parent, name);
        assertNotNull(element, name);
        return element.getTextContent().trim();
    }

    @Test
    void whenTheThingTypesAreListedThenNoSkeletonContentRemains() {
        for (String forbidden : new String[] { "sample", "channel1", "refreshInterval" }) {
            assertFalse(text.contains(forbidden), forbidden);
        }
        assertEquals(1, children(doc().getDocumentElement(), "bridge-type").size());
        assertEquals(1, children(doc().getDocumentElement(), "thing-type").size());
    }

    @Test
    void whenTheBridgeTypeIsReadThenItHasHostnameAndPasswordAndNoChannels() {
        Element bridge = byId("bridge-type", "pulse-bridge");

        Element hostname = parameter(bridge, "hostname");
        Element password = parameter(bridge, "password");

        assertEquals("true", hostname.getAttribute("required"));
        assertEquals("text", hostname.getAttribute("type"));
        assertEquals("true", password.getAttribute("required"));
        assertEquals("password", textOf(password, "context"));
        assertEquals(2, children(required(bridge, "config-description"), "parameter").size());
        assertNull(child(bridge, "channels"));
        assertNull(child(bridge, "channel-groups"));
    }

    @Test
    void whenTheBridgeTypeIsReadThenItsRepresentationPropertyIsTheHostLabelOfBridgeDiscovery() {
        Element bridge = byId("bridge-type", "pulse-bridge");

        assertEquals(PROPERTY_HOST_LABEL, textOf(bridge, "representation-property"));
        assertEquals("hostLabel", PROPERTY_HOST_LABEL);
        assertEquals("hostname", CONFIG_HOSTNAME);
        // hostLabel is a thing property, not a configuration parameter, so the framework does not copy it into the
        // configuration of a thing created from an entry.
        assertThrows(AssertionError.class, () -> parameter(bridge, PROPERTY_HOST_LABEL));
        assertEquals("hostname", parameter(bridge, CONFIG_HOSTNAME).getAttribute("name"));
    }

    @Test
    void whenTheBridgeTypeIsReadThenItsElementsAreInTheOrderTheSchemaAndTheCoreReaderExpect() {
        // The Maven build checks only that the file is well-formed. The reader of the core takes the children of a
        // type in a fixed order (label, description, category, semantic-equipment-tag, channels, properties,
        // representation-property, config-description) and the schema demands the same sequence.
        List<String> order = List.of("label", "description", "category", "semantic-equipment-tag", "channels",
                "channel-groups", "properties", "representation-property", "config-description",
                "config-description-ref");
        Element bridge = byId("bridge-type", "pulse-bridge");

        List<String> found = new ArrayList<>();
        for (Node node = bridge.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element) {
                found.add(element.getTagName());
            }
        }

        assertEquals(List.of("label", "description", "semantic-equipment-tag", "representation-property",
                "config-description"), found);
        int previous = -1;
        for (String name : found) {
            int index = order.indexOf(name);
            assertTrue(index > previous, name + " is out of order");
            previous = index;
        }
    }

    @Test
    void whenTheMeterTypeIsReadThenItNeedsTheBridgeAndAdvertisesDeviceIdAndStaleTimeout() {
        Element meter = byId("thing-type", "pulse-meter");

        Element refs = child(meter, "supported-bridge-type-refs");
        assertNotNull(refs);
        assertEquals("pulse-bridge", children(refs, "bridge-type-ref").get(0).getAttribute("id"));
        Element deviceId = parameter(meter, "deviceId");
        assertEquals("true", deviceId.getAttribute("required"));
        Element stale = parameter(meter, "staleTimeout");
        assertEquals("integer", stale.getAttribute("type"));
        assertEquals("s", stale.getAttribute("unit"));
        assertEquals("10", stale.getAttribute("min"));
        assertEquals("60", textOf(stale, "default"));
        assertEquals("true", textOf(stale, "advanced"));
        assertEquals(2, children(required(meter, "config-description"), "parameter").size());
        assertEquals("deviceId", textOf(meter, "representation-property"));
    }

    @Test
    void whenTheMeterChannelsAreListedThenExactlyTheChannelsOfTheSpecExistWithTheirItemTypes() {
        Element meter = byId("thing-type", "pulse-meter");
        Element channels = child(meter, "channels");
        assertNotNull(channels);

        Map<String, String> itemTypeOfChannel = new LinkedHashMap<>();
        for (Element channel : children(channels, "channel")) {
            itemTypeOfChannel.put(channel.getAttribute("id"), textOf(channelTypeOf(channel), "item-type"));
        }

        assertEquals(ITEM_TYPES, itemTypeOfChannel);
    }

    @Test
    void whenTheChannelTypesAreReadThenEveryChannelIsReadOnlyAndOnlyTheLessCommonOnesAreAdvanced() {
        Element meter = byId("thing-type", "pulse-meter");
        Element channels = child(meter, "channels");
        assertNotNull(channels);

        for (Element channel : children(channels, "channel")) {
            Element channelType = channelTypeOf(channel);
            Element state = child(channelType, "state");
            assertNotNull(state, channel.getAttribute("id"));
            assertEquals("true", state.getAttribute("readOnly"), channel.getAttribute("id"));
            assertNull(child(channelType, "command"), channel.getAttribute("id"));
            assertEquals(ADVANCED.contains(channel.getAttribute("id")),
                    "true".equals(channelType.getAttribute("advanced")), channel.getAttribute("id"));
        }
    }

    @Test
    void whenTheChannelIdsAreComparedWithTheConstantsThenCountersAndLiveValuesCoverAllOfThem() {
        Set<String> all = new HashSet<>(LIVE_CHANNELS);
        all.addAll(COUNTER_CHANNELS);

        assertEquals(ITEM_TYPES.keySet(), all);
        assertTrue(LIVE_CHANNELS.stream().noneMatch(COUNTER_CHANNELS::contains));
        assertEquals(Set.of("energy-import", "energy-export"), COUNTER_CHANNELS);
    }

    @Test
    void whenTheStatusTextKeysAreListedThenEachIsUniqueAndUnderTheOfflinePrefix() {
        List<String> keys = List.of(TEXT_INVALID_HOSTNAME, TEXT_EMPTY_PASSWORD, TEXT_INVALID_DEVICE_ID,
                TEXT_INVALID_STALE_TIMEOUT, TEXT_NO_BRIDGE, TEXT_PASSWORD_REJECTED, TEXT_NO_STREAM, TEXT_HTTP_STATUS,
                TEXT_UNREACHABLE, TEXT_CLOSED_BY_BRIDGE, TEXT_SILENT, TEXT_INVALID_DATA, TEXT_NO_READING);

        assertEquals(keys.size(), new HashSet<>(keys).size());
        keys.forEach(key -> assertTrue(key.startsWith("@text/offline."), key));
    }
}

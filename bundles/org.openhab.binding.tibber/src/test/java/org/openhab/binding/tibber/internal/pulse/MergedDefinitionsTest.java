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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests the scenarios of the {@code channels}, {@code thing-lifecycle}, and {@code discovery} specs that concern the
 * definitions of the whole binding: the thing type files of the cloud part and of the Pulse part are read together as
 * the framework reads them.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class MergedDefinitionsTest {

    private static final String BASE = "/OH-INF/thing/";
    private static final List<String> FILES = List.of("channel-types.xml", "thing-types.xml", "pulse-thing-types.xml");

    private static final Set<String> CLOUD_CHANNEL_TYPE_IDS = Set.of("price", "advanced-price", "price-level", "cost",
            "energy-detailed", "energy", "power", "voltage", "current", "event");
    private static final Set<String> PULSE_CHANNEL_TYPE_IDS = Set.of("pulse-power", "pulse-energy-import",
            "pulse-energy-export", "pulse-phase-power", "pulse-phase-voltage", "pulse-phase-current", "pulse-frequency",
            "pulse-last-update");

    private static Document load(String file) throws IOException, ParserConfigurationException, SAXException {
        try (InputStream in = MergedDefinitionsTest.class.getResourceAsStream(BASE + file)) {
            assertNotNull(in, file);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(in);
        }
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

    private static Element only(Element parent, String name) {
        List<Element> found = children(parent, name);
        assertEquals(1, found.size(), name);
        return found.get(0);
    }

    private static String text(Element parent, String name) {
        return only(parent, name).getTextContent().trim();
    }

    /** All elements of the given name from the given files, in file order, with the file name for messages. */
    private static Map<Element, String> elements(String tag, List<String> files) throws Exception {
        Map<Element, String> result = new LinkedHashMap<>();
        for (String file : files) {
            for (Element element : children(load(file).getDocumentElement(), tag)) {
                result.put(element, file);
            }
        }
        return result;
    }

    private static Element channelType(String id) throws Exception {
        for (Element element : elements("channel-type", FILES).keySet()) {
            if (id.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        throw new AssertionError("no channel type " + id);
    }

    private static Set<String> tags(Element channelType) {
        Set<String> result = new HashSet<>();
        // "point" and "property" since core 5.3, "tag" before (still accepted, deprecated)
        for (String name : List.of("point", "property", "tag")) {
            children(only(channelType, "tags"), name).forEach(tag -> result.add(tag.getTextContent().trim()));
        }
        return result;
    }

    // channels: No duplicate channel type id

    @Test
    void whenAllChannelTypeIdsOfTheBindingAreListedThenNoIdOccursTwiceAndNoPulseIdEqualsACloudId() throws Exception {
        List<String> ids = new ArrayList<>();
        elements("channel-type", FILES).keySet().forEach(e -> ids.add(e.getAttribute("id")));

        assertEquals(CLOUD_CHANNEL_TYPE_IDS.size() + PULSE_CHANNEL_TYPE_IDS.size(), ids.size());
        assertEquals(ids.size(), new HashSet<>(ids).size(), "duplicate channel type id in " + ids);
        for (String cloudId : CLOUD_CHANNEL_TYPE_IDS) {
            assertTrue(ids.contains(cloudId), cloudId);
        }
        Set<String> pulse = new HashSet<>(ids);
        pulse.removeAll(CLOUD_CHANNEL_TYPE_IDS);
        assertEquals(PULSE_CHANNEL_TYPE_IDS, pulse);
    }

    @Test
    void whenTheChannelTypesOfThePulseFileAreListedThenEachIdStartsWithPulse() throws Exception {
        Set<String> ids = new HashSet<>();
        elements("channel-type", List.of("pulse-thing-types.xml")).keySet().forEach(e -> ids.add(e.getAttribute("id")));

        assertEquals(PULSE_CHANNEL_TYPE_IDS, ids);
        ids.forEach(id -> assertTrue(id.startsWith("pulse-"), id));
    }

    @Test
    void whenTheChannelsOfThePulseMeterAreListedThenTheyReferenceTheChannelTypesOfTheTable() throws Exception {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("power", "pulse-power");
        expected.put("energy-import", "pulse-energy-import");
        expected.put("energy-export", "pulse-energy-export");
        for (String phase : List.of("l1", "l2", "l3")) {
            expected.put("power-" + phase, "pulse-phase-power");
            expected.put("voltage-" + phase, "pulse-phase-voltage");
            expected.put("current-" + phase, "pulse-phase-current");
        }
        expected.put("frequency", "pulse-frequency");
        expected.put("last-update", "pulse-last-update");
        Element meter = thingType("pulse-meter");

        Map<String, String> actual = new LinkedHashMap<>();
        children(only(meter, "channels"), "channel")
                .forEach(c -> actual.put(c.getAttribute("id"), c.getAttribute("typeId")));

        assertEquals(expected, actual);
        Set<String> used = new HashSet<>(actual.values());
        assertEquals(PULSE_CHANNEL_TYPE_IDS, used);
        for (String typeId : used) {
            channelType(typeId);
        }
    }

    // channels: Cloud channel type unchanged

    @Test
    void whenTheCloudChannelTypePowerIsReadThenItIsTheOneOfTheCloudPart() throws Exception {
        Element power = channelType("power");

        assertEquals(1, elements("channel-type", List.of("channel-types.xml")).keySet().stream()
                .filter(e -> "power".equals(e.getAttribute("id"))).count());
        assertEquals("Number:Power", text(power, "item-type"));
        assertEquals("Power Consumption/Production", text(power, "label"));
        assertEquals("Power consumption/production for a given time interval", text(power, "description"));
        assertEquals(Set.of("Measurement", "Power"), tags(power));
        Element state = only(power, "state");
        assertEquals("true", state.getAttribute("readOnly"));
        assertEquals("%.0f %unit%", state.getAttribute("pattern"));
        assertEquals("", power.getAttribute("advanced"));
    }

    // thing-lifecycle: Thing types of the Pulse part, Cloud definition identical

    private static Element thingType(String id) throws Exception {
        for (String tag : List.of("thing-type", "bridge-type")) {
            for (Element element : elements(tag, FILES).keySet()) {
                if (id.equals(element.getAttribute("id"))) {
                    return element;
                }
            }
        }
        throw new AssertionError("no thing type " + id);
    }

    @Test
    void whenTheThingTypesOfTheBindingAreListedThenTheCloudAndThePulseTypesExistOnceEach() throws Exception {
        List<String> ids = new ArrayList<>();
        for (String tag : List.of("thing-type", "bridge-type")) {
            elements(tag, FILES).keySet().forEach(e -> ids.add(tag + ":" + e.getAttribute("id")));
        }

        assertEquals(List.of("thing-type:tibberapi", "thing-type:pulse-meter", "bridge-type:pulse-bridge"), ids);
        for (String file : FILES) {
            assertEquals("tibber", load(file).getDocumentElement().getAttribute("bindingId"), file);
        }
    }

    @Test
    void whenThePulseMeterIsReadThenItsOnlySupportedBridgeTypeIsThePulseBridgeAndTheCloudTypeIsNoBridge()
            throws Exception {
        Element meter = thingType("pulse-meter");

        List<Element> refs = children(only(meter, "supported-bridge-type-refs"), "bridge-type-ref");

        assertEquals(1, refs.size());
        assertEquals("pulse-bridge", refs.get(0).getAttribute("id"));
        assertEquals("thing-type", thingType("tibberapi").getTagName());
        assertEquals("bridge-type", thingType("pulse-bridge").getTagName());
    }

    @Test
    void whenTheCloudThingTypeIsReadThenItsDefinitionIsUnchanged() throws Exception {
        Element api = thingType("tibberapi");

        assertEquals("Tibber API", text(api, "label"));
        assertEquals("Tibber API connector", text(api, "description"));
        Map<String, String> groups = new LinkedHashMap<>();
        children(only(api, "channel-groups"), "channel-group")
                .forEach(g -> groups.put(g.getAttribute("id"), g.getAttribute("typeId")));
        assertEquals(Map.of("price", "price", "live", "live", "statistics", "statistics", "history", "history"),
                groups);
        Map<String, String> properties = new LinkedHashMap<>();
        children(only(api, "properties"), "property")
                .forEach(p -> properties.put(p.getAttribute("name"), p.getTextContent().trim()));
        assertEquals(Map.of("vendor", "Tibber", "thingTypeVersion", "8"), properties);
        List<String> parameters = new ArrayList<>();
        children(only(api, "config-description"), "parameter").forEach(p -> parameters.add(p.getAttribute("name")));
        assertEquals(List.of("token", "homeid", "updateHour"), parameters);
        assertNull(children(api, "supported-bridge-type-refs").stream().findFirst().orElse(null));
    }
}

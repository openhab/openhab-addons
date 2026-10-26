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
package org.openhab.binding.awtrixng.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test cases that keep the thing type definitions and the Java mapping of the channels in step.
 *
 * @author Keith T. Garner - Initial contribution
 */
class ThingTypesTest {

    private static Document thingTypes;
    private static Document configs;

    @BeforeAll
    static void load() throws IOException, SAXException, ParserConfigurationException {
        thingTypes = parse("/OH-INF/thing/thing-types.xml");
        configs = parse("/OH-INF/config/config.xml");
    }

    private static Document parse(String resource) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream in = ThingTypesTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, resource);
            return factory.newDocumentBuilder().parse(in);
        }
    }

    private static Stream<Element> elements(Document document, String tag) {
        NodeList nodes = document.getElementsByTagName(tag);
        return Stream.iterate(0, i -> i < nodes.getLength(), i -> i + 1).map(i -> (Element) nodes.item(i));
    }

    private static Element thingType(String tag, String id) {
        return elements(thingTypes, tag).filter(e -> id.equals(e.getAttribute("id"))).findFirst().orElseThrow();
    }

    private static Set<String> channelIds(Element thingType) {
        Set<String> ids = new LinkedHashSet<>();
        NodeList channels = thingType.getElementsByTagName("channel");
        for (int i = 0; i < channels.getLength(); i++) {
            assertTrue(ids.add(((Element) channels.item(i)).getAttribute("id")), "duplicate channel");
        }
        return ids;
    }

    @Test
    void clockDeclaresEverySettingsChannel() {
        Set<String> declared = channelIds(thingType("bridge-type", "clock"));

        for (Map.Entry<String, ?> entry : SettingsChannels.all()) {
            assertTrue(declared.contains(entry.getKey()), "settings channel not on the clock: " + entry.getKey());
        }
    }

    @Test
    void appDeclaresEveryPayloadChannelAndTheControlChannels() {
        Set<String> declared = channelIds(thingType("thing-type", "app"));

        for (Map.Entry<String, ?> entry : AppChannels.all()) {
            assertTrue(declared.contains(entry.getKey()), "payload channel not on the app: " + entry.getKey());
        }
        for (String control : new String[] { "active", "enabled", "reset" }) {
            assertTrue(declared.contains(control), control);
        }
    }

    @Test
    void appDoesNotDeclareChannelsWithoutBehavior() {
        Set<String> expected = new HashSet<>(
                Set.of("active", "enabled", "reset", "button-left", "button-right", "button-select"));
        AppChannels.all().forEach(entry -> expected.add(entry.getKey()));

        assertEquals(expected, channelIds(thingType("thing-type", "app")));
    }

    @Test
    void everyChannelTypeIsDefinedAndUsed() {
        Set<String> defined = new HashSet<>();
        elements(thingTypes, "channel-type").forEach(e -> assertTrue(defined.add(e.getAttribute("id")), "duplicate"));
        Set<String> used = new HashSet<>();
        elements(thingTypes, "channel").forEach(e -> {
            String typeId = e.getAttribute("typeId");
            if (!typeId.startsWith("system.")) {
                assertTrue(defined.contains(typeId), "undefined channel type: " + typeId);
                used.add(typeId);
            }
        });

        Set<String> unused = new HashSet<>(defined);
        unused.removeAll(used);
        assertEquals(Set.of(), unused, "channel types that no channel uses");
    }

    @Test
    void clockIsASubThingOfTheMqttBroker() {
        NodeList refs = thingType("bridge-type", "clock").getElementsByTagName("bridge-type-ref");

        assertEquals(1, refs.getLength());
        assertEquals("broker", ((Element) refs.item(0)).getAttribute("id"));
        assertEquals("mqtt", ((Element) refs.item(0)).getAttribute("bindingId"));
    }

    @Test
    void everyThingTypeHasItsConfigDescription() {
        Set<String> descriptions = new HashSet<>();
        elements(configs, "config-description").forEach(e -> descriptions.add(e.getAttribute("uri")));

        Stream.concat(elements(thingTypes, "thing-type"), elements(thingTypes, "bridge-type")).forEach(type -> {
            NodeList refs = type.getElementsByTagName("config-description-ref");
            assertEquals(1, refs.getLength(), type.getAttribute("id"));
            assertTrue(descriptions.contains(((Element) refs.item(0)).getAttribute("uri")), type.getAttribute("id"));
        });
    }

    @Test
    void appIsASubThingOfTheClock() {
        Element app = thingType("thing-type", "app");

        NodeList refs = app.getElementsByTagName("bridge-type-ref");
        assertEquals(1, refs.getLength());
        assertEquals("clock", ((Element) refs.item(0)).getAttribute("id"));
    }
}

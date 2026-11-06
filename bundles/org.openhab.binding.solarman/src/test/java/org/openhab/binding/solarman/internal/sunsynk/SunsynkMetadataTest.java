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
package org.openhab.binding.solarman.internal.sunsynk;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.typeprovider.ChannelUtils;
import org.w3c.dom.Element;

/**
 * Verifies that every profile entry has matching centralized identifiers and XML access metadata.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SunsynkMetadataTest {
    @Test
    void everyProfileChannelAndTypeMatchesThePublishedXml() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream stream = Objects
                .requireNonNull(SunsynkMetadataTest.class.getResourceAsStream("/OH-INF/thing/sunsynk.xml"))) {
            var document = factory.newDocumentBuilder().parse(stream);
            Map<String, Element> types = new HashMap<>();
            var nodes = document.getElementsByTagName("channel-type");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element type = (Element) nodes.item(i);
                assertNull(types.put(type.getAttribute("id"), type));
            }
            assertEquals(new HashSet<>(SolarmanBindingConstants.SUNSYNK_CHANNEL_TYPE_IDS.values()), types.keySet());
            assertEquals(928, types.size());
            var things = document.getElementsByTagName("thing-type");
            assertEquals(5, things.getLength());
            for (var profileEntry : SolarmanBindingConstants.SUNSYNK_PROFILES.entrySet()) {
                SunsynkProfile profile = new SunsynkProfile(profileEntry.getValue());
                String thingId = profileEntry.getKey().getAsString().split(":")[1];
                Element thing = null;
                for (int i = 0; i < things.getLength(); i++) {
                    Element candidate = (Element) things.item(i);
                    if (thingId.equals(candidate.getAttribute("id"))) {
                        thing = candidate;
                    }
                }
                Element currentThing = Objects.requireNonNull(thing);
                assertEquals("false", currentThing.getAttribute("listed"));
                var channelNodes = currentThing.getElementsByTagName("channel");
                Map<String, String> channels = new HashMap<>();
                for (int i = 0; i < channelNodes.getLength(); i++) {
                    Element channel = (Element) channelNodes.item(i);
                    assertNull(channels.put(channel.getAttribute("id"), channel.getAttribute("typeId")));
                }
                assertEquals(profile.sensors().size(), channels.size());
                for (SunsynkSensor sensor : profile.sensors()) {
                    String typeId = Objects.requireNonNull(SolarmanBindingConstants.SUNSYNK_CHANNEL_TYPE_IDS
                            .get(profileEntry.getValue() + ":" + sensor.id()));
                    assertEquals(typeId, channels.get(sensor.channelId()));
                    Element type = Objects.requireNonNull(types.get(typeId));
                    Element state = (Element) type.getElementsByTagName("state").item(0);
                    assertEquals(Boolean.toString(!sensor.writable()), state.getAttribute("readOnly"));
                    String expectedItemType = switch (sensor.kind()) {
                        case "SwitchRWSensor", "BinarySensor" -> "Switch";
                        case "SystemTimeRWSensor" -> "DateTime";
                        case "SelectRWSensor", "EnumSensor", "TimeRWSensor", "FaultSensor", "HVFaultSensor",
                                "InverterStateSensor", "SDStatusSensor", "RegisterStringSensor", "RegisterTextSensor" ->
                            "String";
                        default -> {
                            ParameterItem numeric = new ParameterItem();
                            numeric.setUom(sensor.definition().path("unit").asText());
                            yield ChannelUtils.getItemType(numeric);
                        }
                    };
                    assertEquals(expectedItemType, sensor.itemType());
                    if (sensor.writable()) {
                        assertEquals("veto", type.getElementsByTagName("autoUpdatePolicy").item(0).getTextContent());
                    }
                    assertEquals(expectedItemType, type.getElementsByTagName("item-type").item(0).getTextContent(),
                            typeId);
                    var expectedOptions = new HashSet<String>();
                    sensor.definition().path("options").properties()
                            .forEach(option -> expectedOptions.add(option.getValue().asText()));
                    var actualOptions = new HashSet<String>();
                    var optionNodes = state.getElementsByTagName("option");
                    for (int i = 0; i < optionNodes.getLength(); i++) {
                        actualOptions.add(((Element) optionNodes.item(i)).getAttribute("value"));
                    }
                    assertEquals(expectedOptions, actualOptions, typeId);
                }
            }
        }
    }
}

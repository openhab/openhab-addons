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
package org.openhab.binding.knx.internal.wizard;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.ChannelKeyTypes;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.Pairing;
import org.openhab.binding.knx.internal.project.KnxProposal.Part;
import org.openhab.binding.knx.internal.wizard.ProposalItems.Item;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags;

/**
 * Tests for the Item types, tags and categories the wizard proposes.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProposalItemsTest {

    private record Row(String tag, String category, String dimension) {
    }

    // the tag table as the page asks for it, each data type with the dimension the binding gives its unit
    private static final Map<String, Row> TABLE = new LinkedHashMap<>();
    static {
        TABLE.put("9.001", new Row("Temperature", "temperature", "Temperature"));
        TABLE.put("9.027", new Row("Temperature", "temperature", "Temperature"));
        TABLE.put("14.068", new Row("Temperature", "temperature", "Temperature"));
        TABLE.put("9.007", new Row("Humidity", "humidity", "Dimensionless"));
        TABLE.put("9.008", new Row("CO2", "carbondioxide", "Dimensionless"));
        TABLE.put("9.004", new Row("Illuminance", "sun", "Illuminance"));
        TABLE.put("9.006", new Row("Pressure", "pressure", "Pressure"));
        TABLE.put("14.058", new Row("Pressure", "pressure", "Pressure"));
        TABLE.put("9.005", new Row("Wind", "wind", "Speed"));
        TABLE.put("9.028", new Row("Wind", "wind", "Speed"));
        TABLE.put("9.024", new Row("Power", "energy", "Power"));
        TABLE.put("14.056", new Row("Power", "energy", "Power"));
        TABLE.put("13.010", new Row("Energy", "energy", "Energy"));
        TABLE.put("13.013", new Row("Energy", "energy", "Energy"));
        TABLE.put("29.010", new Row("Energy", "energy", "Energy"));
        TABLE.put("7.012", new Row("Current", "energy", "ElectricCurrent"));
        TABLE.put("9.021", new Row("Current", "energy", "ElectricCurrent"));
        TABLE.put("14.019", new Row("Current", "energy", "ElectricCurrent"));
        TABLE.put("9.020", new Row("Voltage", "energy", "ElectricPotential"));
        TABLE.put("14.027", new Row("Voltage", "energy", "ElectricPotential"));
    }

    static KnxDatapointType type(String text) {
        KnxDatapointType type = KnxDatapointType.parse(text);
        assertNotNull(type, text);
        return type;
    }

    static Channel channel(String channelType, String... keysAndTypes) {
        List<Part> parts = new java.util.ArrayList<>();
        for (int i = 0; i < keysAndTypes.length; i += 2) {
            KnxGroupAddress address = new KnxGroupAddress("P-0001-0_GA-" + i, (31 << 11) | (7 << 8) | i, "31/7/" + i,
                    "", null, List.of(type(keysAndTypes[i + 1])), false, List.of());
            parts.add(new Part(keysAndTypes[i], type(keysAndTypes[i + 1]), address, false, null, false, Pairing.NONE,
                    List.of(), keysAndTypes[i + 1] + ":31/7/" + i));
        }
        return new Channel(channelType, List.of(channelType), "Label", true, false, parts, List.of(), List.of(),
                List.of());
    }

    @Test
    void everyChannelTypeHasTheItemTypeOfDeviceXml() throws Exception {
        String xml = Files.readString(Path.of("src", "main", "resources", "OH-INF", "thing", "device.xml"),
                StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("<channel-type id=\"([a-z]+)\">\\s*<item-type>([A-Za-z]+)</item-type>")
                .matcher(xml);
        Map<String, String> fromXml = new TreeMap<>();
        while (matcher.find()) {
            fromXml.put(matcher.group(1), matcher.group(2));
        }
        assertEquals(new TreeSet<>(ChannelKeyTypes.channelTypes()), fromXml.keySet());
        for (Map.Entry<String, String> entry : fromXml.entrySet()) {
            // 7.001 counts pulses: a number without a unit
            KnxDatapointType dataType = "number".equals(entry.getKey()) ? type("7.001") : type("1.001");
            assertEquals(List.of(entry.getValue()), ProposalItems.allowedTypes(entry.getKey(), dataType),
                    entry.getKey());
            assertEquals(entry.getValue(), ProposalItems.itemType(entry.getKey(), dataType), entry.getKey());
        }
    }

    @Test
    void aNumberWithADimensionAllowsItAndPlainNumber() {
        assertEquals(List.of("Number:Temperature", "Number"), ProposalItems.allowedTypes("number", type("9.001")));
        assertEquals("Number:Temperature", ProposalItems.itemType("number", type("9.001")));
        assertEquals(List.of("Number:Dimensionless", "Number"), ProposalItems.allowedTypes("number", type("5.001")));
        assertEquals(List.of("Number:Power", "Number"), ProposalItems.allowedTypes("number", type("14.056")));
    }

    @Test
    void aNumberWithoutADimensionIsPlainNumber() {
        assertEquals(List.of("Number"), ProposalItems.allowedTypes("number", type("7.001")));
        assertEquals(List.of("Number"), ProposalItems.allowedTypes("number", type("17.001")));
        assertEquals(List.of("Number"), ProposalItems.allowedTypes("number", type("1.022")));
        assertEquals("Number", ProposalItems.itemType("number", null));
    }

    @Test
    void aChannelTypeOutsideTheTableHasNoItemType() {
        assertEquals(List.of(), ProposalItems.allowedTypes("switch-control", type("1.001")));
        assertEquals(List.of(), ProposalItems.allowedTypes("telegram-trigger", type("1.001")));
        assertNull(ProposalItems.itemType("switch-control", type("1.001")));
        Channel none = new Channel(null, List.of(), "Label", false, false, channel("switch", "ga", "1.001").parts(),
                List.of(), List.of(), List.of());
        assertNull(ProposalItems.of(none));
    }

    @Test
    void everyRowOfTheTagTable() {
        for (Map.Entry<String, Row> entry : TABLE.entrySet()) {
            KnxDatapointType dataType = type(entry.getKey());
            Row row = entry.getValue();
            assertEquals(List.of(ProposalItems.MEASUREMENT, row.tag()), ProposalItems.tags("number", dataType),
                    entry.getKey());
            assertEquals(row.category(), ProposalItems.category("number", dataType), entry.getKey());
            assertEquals("Number:" + row.dimension(), ProposalItems.itemType("number", dataType), entry.getKey());
        }
    }

    @Test
    void aTypeNotInTheTableHasNoTags() {
        for (int main = 0; main < 1000; main++) {
            for (int sub = 0; sub < 1000; sub++) {
                KnxDatapointType dataType = new KnxDatapointType(main, sub);
                if (!TABLE.containsKey(dataType.toString())) {
                    assertEquals(List.of(), ProposalItems.tags("number", dataType), dataType.toString());
                    assertNull(ProposalItems.category("number", dataType), dataType.toString());
                }
            }
        }
        assertEquals(List.of(), ProposalItems.tags("number", new KnxDatapointType(9, null)));
        assertEquals(List.of(), ProposalItems.tags("number", null));
        assertEquals(List.of(), ProposalItems.tags("string", type("9.001")));
        assertNull(ProposalItems.category("string", type("9.001")));
        assertEquals(List.of(), ProposalItems.tags("switch", type("1.001")));
        assertEquals(List.of(), ProposalItems.tags("dimmer", type("1.001")));
    }

    @Test
    void everyTagIsADefaultSemanticTag() throws Exception {
        Set<String> semantic = new TreeSet<>();
        for (Class<?> nested : DefaultSemanticTags.class.getDeclaredClasses()) {
            for (Field field : nested.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.get(null) instanceof SemanticTag tag) {
                    semantic.add(tag.getName());
                }
            }
        }
        Set<String> used = new TreeSet<>();
        for (int main = 0; main < 1000; main++) {
            for (int sub = 0; sub < 1000; sub++) {
                used.addAll(ProposalItems.tags("number", new KnxDatapointType(main, sub)));
            }
        }
        assertEquals(11, used.size(), used.toString());
        for (String tag : used) {
            assertTrue(semantic.contains(tag), tag);
        }
    }

    @Test
    void theItemOfAChannel() {
        Item temperature = ProposalItems.of(channel("number", "ga", "9.001"));
        assertNotNull(temperature);
        assertEquals(new Item("Number:Temperature", List.of("Number:Temperature", "Number"), "temperature",
                List.of("Measurement", "Temperature")), temperature);

        Item dimmer = ProposalItems.of(channel("dimmer", "switch", "1.001", "position", "5.001"));
        assertEquals(new Item("Dimmer", List.of("Dimmer"), null, List.of()), dimmer);

        Item text = ProposalItems.of(channel("string", "ga", "16.000"));
        assertEquals(new Item("String", List.of("String"), null, List.of()), text);
    }
}

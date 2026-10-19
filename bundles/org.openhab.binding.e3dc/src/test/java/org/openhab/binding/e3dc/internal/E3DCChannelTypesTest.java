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
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Guards the agreement between the channel model in Java and the channel type definitions in the XML.
 *
 * <p>
 * Nothing else checks this: a channel without a channel type, or a definition without a channel, only
 * shows up at runtime, and a unit that does not match the item type is silently dropped by the
 * framework. Both are easy to introduce when a tag mapping is changed.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCChannelTypesTest {

    private static final Path THING_DIRECTORY = Path.of("src", "main", "resources", "OH-INF", "thing");

    /** The item types whose channel types may name a unit in their state pattern. */
    private static final Set<String> UNIT_ITEM_TYPES = Set.of("Number:Power", "Number:Energy",
            "Number:ElectricPotential", "Number:ElectricCurrent", "Number:Frequency", "Number:Temperature",
            "Number:Time", "Number:DataAmount");

    private static final Pattern CHANNEL_TYPE = Pattern
            .compile("<channel-type id=\"([^\"]+)\"[^>]*>(.*?)</channel-type>", Pattern.DOTALL);
    private static final Pattern ITEM_TYPE = Pattern.compile("<item-type>([^<]+)</item-type>");

    /** A channel type definition as it is declared in the XML. */
    private record ChannelType(String id, String itemType, String body) {
    }

    private static Map<String, ChannelType> channelTypes() throws IOException {
        Map<String, ChannelType> types = new HashMap<>();
        try (Stream<Path> files = Files.list(THING_DIRECTORY)) {
            for (Path file : files.filter(path -> path.getFileName().toString().endsWith("channel-types.xml"))
                    .toList()) {
                String text = Files.readString(file);
                Matcher matcher = CHANNEL_TYPE.matcher(text);
                while (matcher.find()) {
                    String id = matcher.group(1);
                    String body = matcher.group(2);
                    Matcher itemType = ITEM_TYPE.matcher(body);
                    String type = itemType.find() ? itemType.group(1) : "";
                    ChannelType previous = types.put(id, new ChannelType(id, type, body));
                    assertNull(previous, "channel type " + id + " is defined more than once");
                }
            }
        }
        return types;
    }

    private static List<E3DCChannelRegistry> registries() {
        return List.of(E3DCChannelSpec.powerPlant(), E3DCChannelSpec.wallbox(), E3DCChannelSpec.powerMeter());
    }

    /**
     * The channel id in the XML is the group id followed by the channel id, which is how the framework
     * resolves a channel type for a channel.
     */
    private static String expectedTypeId(String groupId, String channelId) {
        return groupId + "-" + channelId;
    }

    @Test
    void everyChannelHasAChannelTypeDefinition() throws IOException {
        Map<String, ChannelType> types = channelTypes();
        List<String> missing = new ArrayList<>();
        for (E3DCChannelRegistry registry : registries()) {
            for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : registry.groups().entrySet()) {
                for (String channelId : group.getValue().keySet()) {
                    String typeId = expectedTypeId(group.getKey(), channelId);
                    if (!types.containsKey(typeId)) {
                        missing.add(typeId);
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "these channels have no channel type definition: " + missing);
    }

    /**
     * Every channel of every group declares its channel type, so the definitions actually referenced by
     * the things are the source of truth for which types are in use. This covers the writable channels
     * as well, which have no counterpart in the registries.
     */
    private static Set<String> referencedTypeIds() throws IOException {
        Pattern typeId = Pattern.compile("<channel id=\"[^\"]+\" typeId=\"([^\"]+)\"");
        Set<String> referenced = new HashSet<>();
        try (Stream<Path> files = Files.list(THING_DIRECTORY)) {
            for (Path file : files.filter(path -> path.getFileName().toString().endsWith("channel-groups.xml"))
                    .toList()) {
                Matcher matcher = typeId.matcher(Files.readString(file));
                while (matcher.find()) {
                    referenced.add(matcher.group(1));
                }
            }
        }
        return referenced;
    }

    @Test
    void everyChannelTypeDefinitionIsUsedByAChannel() throws IOException {
        Set<String> referenced = referencedTypeIds();
        List<String> unused = channelTypes().keySet().stream().filter(id -> !referenced.contains(id)).sorted().toList();
        assertTrue(unused.isEmpty(), "these channel types are defined but never used: " + unused);
    }

    /**
     * A group that declares a channel with a type that does not exist leaves the channel without a
     * definition, so the framework cannot create it.
     */
    @Test
    void everyReferencedChannelTypeExists() throws IOException {
        Map<String, ChannelType> types = channelTypes();
        List<String> missing = referencedTypeIds().stream().filter(id -> !types.containsKey(id)).sorted().toList();
        assertTrue(missing.isEmpty(), "these channels reference a missing channel type: " + missing);
    }

    /**
     * A unit placeholder only means something for the dimensioned item types, and a percentage is
     * dimensionless. A pattern that names a unit the item type cannot carry renders without it.
     */
    @Test
    void statePatternsMatchTheirItemType() throws IOException {
        Map<String, ChannelType> types = channelTypes();
        List<String> wrong = new ArrayList<>();
        for (ChannelType type : types.values()) {
            boolean hasState = type.body().contains("<state");
            assertTrue(hasState, type.id() + " has no state element, so it renders without a unit");
            String state = type.body().substring(type.body().indexOf("<state"));
            boolean namesUnit = state.contains("%unit%");
            String itemType = type.itemType();
            if (namesUnit && !UNIT_ITEM_TYPES.contains(itemType)) {
                wrong.add(type.id() + " (" + itemType + ") names a unit");
            }
            boolean namesPercent = state.contains("%%") || state.contains("% ");
            if (namesPercent && !"Number:Dimensionless".equals(itemType)) {
                wrong.add(type.id() + " (" + itemType + ") names a percentage");
            }
        }
        assertTrue(wrong.isEmpty(), "state patterns do not match their item type: " + wrong);
    }

    @Test
    void everyChannelTypeHasALabelAndADescription() throws IOException {
        List<String> incomplete = new ArrayList<>();
        for (ChannelType type : channelTypes().values()) {
            if (!type.body().contains("<label>") || !type.body().contains("<description>")) {
                incomplete.add(type.id());
            }
        }
        assertTrue(incomplete.isEmpty(), "these channel types lack a label or a description: " + incomplete);
    }

    /**
     * A channel that reports one of a fixed set of values has to declare them as options, otherwise the
     * user sees a bare number instead of what the value means. The declared values are also the ones the
     * device is expected to send, so they must be numbers.
     */
    @Test
    void channelsWithAFixedValueSetDeclareOptions() throws IOException {
        List<String> expected = List.of("system-mode", "wallbox-number-phases");
        Pattern option = Pattern.compile("<option value=\"([^\"]+)\">([^<]+)</option>");
        for (String id : expected) {
            ChannelType type = channelTypes().get(id);
            assertNotNull(type, "channel type " + id + " is missing");
            Matcher matcher = option.matcher(type.body());
            List<String> values = new ArrayList<>();
            while (matcher.find()) {
                values.add(matcher.group(1));
                assertFalse(matcher.group(2).isBlank(), id + " has an option without a label");
            }
            assertFalse(values.isEmpty(), id + " declares no options");
            for (String value : values) {
                assertDoesNotThrow(() -> Integer.parseInt(value), id + " has the non numeric option " + value);
            }
        }
        // the power mode is documented as automatic, idle, discharge, charge and grid charge
        ChannelType powerMode = Objects.requireNonNull(channelTypes().get("system-mode"));
        Matcher matcher = option.matcher(powerMode.body());
        List<String> modes = new ArrayList<>();
        while (matcher.find()) {
            modes.add(matcher.group(1));
        }
        assertEquals(List.of("0", "1", "2", "3", "4"), modes, "the power modes are numbered from zero");
    }

    /**
     * An option list tells the user which of a few discrete values a channel reports. That only makes
     * sense for a plain number, a dimension less share or a string; a channel carrying a unit reports a
     * measurement that must not be narrowed to a list.
     */
    @Test
    void optionsAreOnlyDeclaredForDiscreteChannels() throws IOException {
        List<String> wrong = new ArrayList<>();
        for (ChannelType type : channelTypes().values()) {
            if (type.body().contains("<option ") && type.itemType().contains(":")) {
                wrong.add(type.id() + " (" + type.itemType() + ") restricts a measurement to options");
            }
        }
        assertTrue(wrong.isEmpty(), "measurements must not carry an option list: " + wrong);
    }

    /**
     * A channel the binding can write must not be marked read only, otherwise the framework rejects
     * every command for it.
     */
    @Test
    void writableChannelsAreNotMarkedReadOnly() throws IOException {
        Map<String, ChannelType> types = channelTypes();
        List<String> wrong = new ArrayList<>();
        for (E3DCCommandRegistry commands : List.of(E3DCCommandSpec.powerPlant(), E3DCCommandSpec.wallbox())) {
            for (Map.Entry<String, Map<String, E3DCCommandSpec>> group : commands.groups().entrySet()) {
                for (String channelId : group.getValue().keySet()) {
                    ChannelType type = types.get(expectedTypeId(group.getKey(), channelId));
                    if (type == null) {
                        wrong.add(group.getKey() + "#" + channelId + " has no channel type");
                    } else if (type.body().contains("readOnly=\"true\"")) {
                        wrong.add(group.getKey() + "#" + channelId + " is writable but read only");
                    }
                }
            }
        }
        assertTrue(wrong.isEmpty(), "writable channels are declared incorrectly: " + wrong);
    }
}

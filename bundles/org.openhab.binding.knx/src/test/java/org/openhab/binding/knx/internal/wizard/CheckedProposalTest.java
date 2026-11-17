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
import static org.openhab.binding.knx.internal.wizard.WizardFixtures.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.channel.GroupAddressConfiguration;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Channel;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Item;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.LocationGroup;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Part;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Read;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Thing;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags;

import io.calimero.GroupAddress;

/**
 * Tests for the checked model of the wizard: the channel text of a part, the label cleaning, and what the model
 * refuses to hold. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class CheckedProposalTest {

    static Set<String> readGAs(String text) {
        GroupAddressConfiguration configuration = Objects.requireNonNull(GroupAddressConfiguration.parse(text));
        return configuration.getReadGAs().stream().map(GroupAddress::toString).collect(Collectors.toSet());
    }

    @Test
    void aPartGivesItsChannelText() {
        assertEquals("1.001:31/7/1+<31/7/2", part("ga", "1.001", "31/7/1", "31/7/2", Read.STATUS).text());
        assertEquals("1.001:<31/7/1+31/7/2", part("ga", "1.001", "31/7/1", "31/7/2", Read.MAIN).text());
        assertEquals("1.001:31/7/1+31/7/2", part("ga", "1.001", "31/7/1", "31/7/2", Read.NONE).text());
        assertEquals("9.001:<31/7/8", part("ga", "9.001", "31/7/8", null, Read.MAIN).text());
        assertEquals("5.001:31/7/12+<31/7/13", part("position", "5.001", "31/7/12", "31/7/13", Read.STATUS).text());
        assertEquals("232.600:31/7/9", part("hsb", "232.600", "31/7/9", null, Read.NONE).text());
    }

    @Test
    void theTextReadsWhatThePartSays() {
        assertEquals(Set.of("31/7/2"), readGAs(part("ga", "1.001", "31/7/1", "31/7/2", Read.STATUS).text()));
        assertEquals(Set.of("31/7/1"), readGAs(part("ga", "1.001", "31/7/1", "31/7/2", Read.MAIN).text()));
        assertEquals(Set.of(), readGAs(part("ga", "1.001", "31/7/1", "31/7/2", Read.NONE).text()));
        GroupAddressConfiguration paired = Objects.requireNonNull(
                GroupAddressConfiguration.parse(part("ga", "1.001", "31/7/1", "31/7/2", Read.STATUS).text()));
        assertEquals("31/7/1", paired.getMainGA().toString());
        assertEquals("1.001", paired.getDPT());
        assertEquals(2, paired.getListenGAs().size());
    }

    @Test
    void labelsAreCleaned() {
        assertEquals("Ceiling light", CheckedProposal.cleanLabel("Ceiling\n light"));
        assertEquals("ab", CheckedProposal.cleanLabel("a\r\n\tb\u0000\u0007"));
        assertEquals("ab", CheckedProposal
                .cleanLabel("a" + Character.toString(0x2028) + "b" + Character.toString(0x2029) + "\u0085"));
        assertEquals("Lamp", CheckedProposal.cleanLabel("  Lamp \n"));
        assertEquals("x".repeat(100), CheckedProposal.cleanLabel("x".repeat(150)));
        assertEquals("ü".repeat(100), CheckedProposal.cleanLabel("ü".repeat(101)));
        assertEquals("[On] \"x\" \\", CheckedProposal.cleanLabel("[On] \"x\" \\"));
        assertEquals("", CheckedProposal.cleanLabel("\n\r"));
    }

    @Test
    void itemLabelsHaveNoBrackets() {
        assertEquals("Temperature (°C)", CheckedProposal.cleanItemLabel("Temperature [°C]"));
        assertEquals("(%s) x", CheckedProposal.cleanItemLabel("[%s]\n x"));
    }

    @Test
    void withoutReadAtStartTheModelHoldsNoReadMark() {
        CheckedProposal proposal = sample(false);
        assertFalse(proposal.readAtStart());
        for (Thing thing : proposal.things()) {
            for (Channel channel : thing.channels()) {
                for (Part part : channel.parts()) {
                    assertEquals(Read.NONE, part.read(), part.toString());
                    assertFalse(part.text().contains("<"), part.text());
                }
            }
        }
        assertTrue(sample(true).things().get(0).channels().get(0).parts().get(0).text().contains("<"));
    }

    @Test
    void everyLocationTagIsADefaultSemanticTag() throws Exception {
        Set<String> semantic = new TreeSet<>();
        for (Field field : DefaultSemanticTags.Location.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.get(null) instanceof SemanticTag tag) {
                semantic.add(tag.getName());
            }
        }
        assertEquals(List.of("Building", "Floor", "Room", "Corridor", "Outdoor"), ProposalLocations.TAGS);
        for (String tag : ProposalLocations.TAGS) {
            assertTrue(semantic.contains(tag), tag);
        }
    }

    @Test
    void theTagAndTheIconComeFromTheKindOfSpace() {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("Building", "Building house");
        expected.put("BuildingPart", "Building house");
        expected.put("Floor", "Floor groundfloor");
        expected.put("Room", "Room group");
        expected.put("Corridor", "Corridor corridor");
        expected.put("Stairway", "Corridor group");
        expected.put("Area", "Outdoor garden");
        expected.put("DistributionBoard", "null group");
        expected.put("Something new", "null group");
        expected.put("", "null group");
        expected.forEach((type, tagAndIcon) -> {
            assertEquals(tagAndIcon, ProposalLocations.tag(type) + " " + ProposalLocations.category(type), type);
            String tag = ProposalLocations.tag(type);
            assertTrue(tag == null || ProposalLocations.TAGS.contains(tag), type);
            assertTrue(CheckedProposal.isCategory(ProposalLocations.category(type)), type);
        });
    }

    @Test
    void aLocationGroupLiesOnlyInAnEarlierGroupAndCarriesOnlyALocationTag() {
        Channel light = channel("K", "Light", "Light", "switch", "Light", "Switch",
                part("ga", "1.001", "31/7/1", null, Read.NONE));
        LocationGroup house = new LocationGroup("gHouse", "House", "Building", null, null, false);
        LocationGroup floor = new LocationGroup("gFloor", "Floor", "Floor", "", "gHouse", false);
        assertNull(floor.category());
        proposal(true, List.of(house, floor), thing("K", "K", light));
        assertThrows(IllegalArgumentException.class,
                () -> proposal(true, List.of(floor, house), thing("K", "K", light)));
        assertThrows(IllegalArgumentException.class, () -> proposal(true, List.of(floor), thing("K", "K", light)));
        assertThrows(IllegalArgumentException.class,
                () -> new LocationGroup("gHouse", "House", "Kitchen", null, null, false));
        assertThrows(IllegalArgumentException.class,
                () -> new LocationGroup("gHouse", "House", "Lightbulb", null, null, false));
        assertThrows(IllegalArgumentException.class,
                () -> new LocationGroup("gHouse", "House", null, null, "gHouse", false));
        assertThrows(IllegalArgumentException.class,
                () -> new LocationGroup("gHouse", "House", null, "-x", null, false));
        assertThrows(IllegalArgumentException.class,
                () -> new LocationGroup("gHouse", "House", null, null, "g House", false));
    }

    @Test
    void aChannelKeyOutsideItsTypeIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "x", "x", "switch", "x", "Switch",
                part("address", "1.001", "31/7/1", null, Read.NONE)));
        assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "x", "x", "switch", "x", "Switch",
                part("hsb", "1.001", "31/7/1", null, Read.NONE)));
        assertThrows(IllegalArgumentException.class,
                () -> channel("Kitchen", "x", "x", "dimmer", "x", "Dimmer",
                        part("switch", "1.001", "31/7/1", null, Read.NONE),
                        part("switch", "1.001", "31/7/2", null, Read.NONE)));
        assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "x", "x", "switch-control", "x", "Switch",
                part("ga", "1.001", "31/7/1", null, Read.NONE)));
        assertThrows(IllegalArgumentException.class,
                () -> channel("Kitchen", "x", "x", "switch", "x", "Switch", new Part[0]));
    }

    @Test
    void aDataTypeThatDoesNotWorkOnItsKeyIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "x", "x", "switch", "x", "Switch",
                part("ga", "5.001", "31/7/1", null, Read.NONE)));
        assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "x", "x", "rollershutter", "x",
                "Rollershutter", part("stopMove", "1.007", "31/7/1", null, Read.NONE)));
        assertThrows(IllegalArgumentException.class, () -> part("ga", "9", "31/7/1", null, Read.NONE));
    }

    @Test
    void aBadAddressIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> part("ga", "1.001", "31/7/1+<31/7/2", null, Read.NONE));
        assertThrows(IllegalArgumentException.class, () -> part("ga", "1.001", "31/7/1", "1.2.3", Read.NONE));
        assertThrows(IllegalArgumentException.class, () -> part("ga", "1.001", "31/9/1", null, Read.NONE));
        assertThrows(IllegalArgumentException.class, () -> part("ga", "1.001", "31/7/1", null, Read.STATUS));
    }

    @Test
    void uidsMustFitTheBridgeAndEachOther() {
        Channel light = channel("Kitchen", "Light", "Light", "switch", "Light", "Switch",
                part("ga", "1.001", "31/7/1", null, Read.NONE));
        assertThrows(IllegalArgumentException.class, () -> new CheckedProposal("knx:ip:other",
                List.of(thing("Kitchen", "Kitchen", light)), List.of(), true));
        assertThrows(IllegalArgumentException.class, () -> new CheckedProposal("hue:bridge:example",
                List.of(thing("Kitchen", "Kitchen", light)), List.of(), true));
        assertThrows(IllegalArgumentException.class, () -> thing("Hall", "Hall", light));
        assertThrows(IllegalArgumentException.class, () -> new Channel(light.uid(), light.id(), light.label(),
                light.channelType(), light.parts(),
                item("Light", "Switch", "Light", "knx:device:example:Kitchen:Other", null, List.of(), List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> new Thing("knx:device:example:Kitchen", "Kitchen", "Kitchen", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> proposal(true, List.of(), thing("Kitchen", "Kitchen", light), thing("Kitchen", "Again", light)));
        assertThrows(IllegalArgumentException.class, () -> thing("Kitchen", "Kitchen", light, light));
        assertThrows(IllegalArgumentException.class, () -> thing("a b", "Kitchen"));
    }

    @Test
    void aWordOfTheGrammarIsNoIdOrName() {
        Part ga = part("ga", "1.001", "31/7/1", null, Read.NONE);
        String uid = "knx:device:example:Kitchen:Light";
        for (String keyword : ProposalNamesTest.THING_KEYWORDS) {
            Channel channel = channel(keyword, "Light", "Light", "switch", "Light", "Switch", ga);
            assertThrows(IllegalArgumentException.class, () -> thing(keyword, "Kitchen", channel), keyword);
            assertThrows(IllegalArgumentException.class,
                    () -> channel("Kitchen", keyword, "Light", "switch", "Light", "Switch", ga), keyword);
        }
        for (String keyword : ProposalNamesTest.ITEM_KEYWORDS) {
            assertThrows(IllegalArgumentException.class,
                    () -> item(keyword, "Switch", "Light", uid, null, List.of(), List.of()), keyword);
            assertThrows(IllegalArgumentException.class,
                    () -> new LocationGroup(keyword, "Kitchen", "Room", null, null, false), keyword);
        }
    }

    @Test
    void itemsMustBeWellFormed() {
        String uid = "knx:device:example:Kitchen:Light";
        assertThrows(IllegalArgumentException.class,
                () -> item("1Light", "Switch", "Light", uid, null, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> item("Light", "Switch", "Light [%s]", uid, null, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> item("Light", "Switch", "", uid, null, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> item("Light", "Switch", "Light", uid, "light>", List.of(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> item("Light", "Switch", "Light", uid, null, List.of("a\"b"), List.of()));
        assertThrows(IllegalArgumentException.class, () -> item("Light", "Switch", "Light", uid, null,
                List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> item("Light", "Switch", "Light", uid, null, List.of(), List.of("g Kitchen")));
        for (String category : List.of("-x", "true", "false")) {
            assertThrows(IllegalArgumentException.class,
                    () -> item("Light", "Switch", "Light", uid, category, List.of(), List.of()), category);
        }
        for (String category : List.of("a-b", "x_1")) {
            assertEquals(category, item("Light", "Switch", "Light", uid, category, List.of(), List.of()).category());
        }
        assertNull(item("Light", "Switch", "Light", uid, "", List.of(), List.of()).category());
    }

    @Test
    void anItemTypeTheChannelDoesNotAllowIsRefused() {
        for (String type : List.of("Group", "Number:Foo", "Dimmer", "Switch ", "")) {
            assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "Light", "Light", "switch", "Light",
                    type, part("ga", "1.001", "31/7/1", null, Read.NONE)), type);
        }
        assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "T", "T", "number", "T", "Number:Power",
                part("ga", "9.001", "31/7/1", null, Read.NONE)));
        channel("Kitchen", "T", "T", "number", "T", "Number", part("ga", "9.001", "31/7/1", null, Read.NONE));
    }

    @Test
    void labelsMustBeClean() {
        assertThrows(IllegalArgumentException.class, () -> thing("Kitchen", "Kit\nchen", channel("Kitchen", "Light",
                "Light", "switch", "Light", "Switch", part("ga", "1.001", "31/7/1", null, Read.NONE))));
        assertThrows(IllegalArgumentException.class, () -> channel("Kitchen", "Light", " Light", "switch", "Light",
                "Switch", part("ga", "1.001", "31/7/1", null, Read.NONE)));
        assertThrows(IllegalArgumentException.class,
                () -> new LocationGroup("gKitchen", "Kitchen [1]", "Room", null, null, false));
        assertThrows(IllegalArgumentException.class,
                () -> new LocationGroup("g-Kitchen", "Kitchen", "Room", null, null, false));
    }

    @Test
    void groupsMustBeRoomGroupsOfTheProposalAndNamesUnique() {
        Channel light = channel("Kitchen", "Light", "Light", "switch", "Light", "Switch",
                part("ga", "1.001", "31/7/1", null, Read.NONE));
        Channel grouped = withItem(light,
                item("Light", "Switch", "Light", light.uid(), null, List.of(), List.of("gLiving")));
        assertThrows(IllegalArgumentException.class,
                () -> proposal(true, List.of(new LocationGroup("gKitchen", "Kitchen", "Room", null, null, false)),
                        thing("Kitchen", "K", grouped)));
        proposal(true, List.of(new LocationGroup("gLiving", "Living", "Room", null, null, false)),
                thing("Kitchen", "K", grouped));

        assertThrows(IllegalArgumentException.class, () -> proposal(true,
                List.of(new LocationGroup("Light", "Light", "Room", null, null, false)), thing("K", "K", light)));
        assertThrows(IllegalArgumentException.class,
                () -> proposal(true, List.of(new LocationGroup("gA", "A", "Room", null, null, false),
                        new LocationGroup("gA", "B", "Room", null, null, false)), thing("K", "K", light)));
        Channel other = channel("Kitchen", "Other", "Other", "switch", "Light", "Switch",
                part("ga", "1.001", "31/7/2", null, Read.NONE));
        assertThrows(IllegalArgumentException.class,
                () -> proposal(true, List.of(), thing("Kitchen", "K", light, other)));
    }

    @Test
    void theSampleIsAValidModel() {
        CheckedProposal proposal = sample(true);
        assertEquals("example", proposal.bridgeId());
        assertEquals(2, proposal.things().size());
        assertEquals("knx:device:example:Kitchen", proposal.things().get(0).uid());
    }

    @Test
    void aSerialBridgeIsAccepted() {
        Channel light = new Channel("knx:device:bus1:Kitchen:Light", "Light", "Light", "switch",
                List.of(part("ga", "1.001", "31/7/1", null, Read.NONE)),
                new Item("Light", "Switch", "Light", null, List.of(), List.of(), "knx:device:bus1:Kitchen:Light"));
        CheckedProposal proposal = new CheckedProposal("knx:serial:bus1",
                List.of(new Thing("knx:device:bus1:Kitchen", "Kitchen", "Kitchen", List.of(light))), List.of(), true);
        assertEquals("bus1", proposal.bridgeId());
    }
}

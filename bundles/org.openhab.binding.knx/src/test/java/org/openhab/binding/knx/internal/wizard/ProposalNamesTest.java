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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxProposal;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.Pairing;
import org.openhab.binding.knx.internal.project.KnxProposal.Part;
import org.openhab.binding.knx.internal.project.KnxProposal.Room;
import org.openhab.binding.knx.internal.project.KnxProposal.Source;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;
import org.openhab.binding.knx.internal.wizard.ProposalNames.ChannelNames;
import org.openhab.binding.knx.internal.wizard.ProposalNames.Names;
import org.openhab.binding.knx.internal.wizard.ProposalNames.ThingNames;

/**
 * Tests for the ids, UIDs, Item names and room group names the wizard proposes. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProposalNamesTest {
    private static final String BRIDGE = "knx:ip:example";
    private static final Predicate<String> NONE = name -> false;

    static KnxGroupAddress ga(int n) {
        return new KnxGroupAddress("P-0001-0_GA-" + n, (31 << 11) | (7 << 8) | n, "31/7/" + n, "Address " + n, null,
                List.of(new KnxDatapointType(1, 1)), false, List.of());
    }

    static Part part(int n) {
        return new Part("ga", new KnxDatapointType(1, 1), ga(n), false, null, false, Pairing.NONE, List.of(),
                "1.001:31/7/" + n);
    }

    static Channel channel(String label, Channel... split) {
        return new Channel("switch", List.of("switch"), label, true, false, List.of(part(1)), Arrays.asList(split),
                List.of(), List.of());
    }

    static Thing thing(String label, @Nullable String room, Channel... channels) {
        return new Thing(label, Source.FUNCTION, "F-1", room == null ? null : "S-" + room, room,
                Arrays.asList(channels));
    }

    static KnxProposal proposal(List<Room> rooms, Thing... things) {
        return new KnxProposal(Arrays.asList(things), rooms, List.of(), List.of(), true);
    }

    static Names names(KnxProposal proposal) {
        return new ProposalNames(NONE, NONE).names(BRIDGE, proposal);
    }

    static List<String> thingIds(Names names) {
        return names.things().stream().map(ThingNames::id).toList();
    }

    static List<String> channelIds(Names names) {
        List<String> ids = new ArrayList<>();
        for (ThingNames thing : names.things()) {
            for (ChannelNames channel : thing.channels()) {
                ids.add(channel.id());
                channel.split().forEach(s -> ids.add(s.id()));
            }
        }
        return ids;
    }

    static List<String> itemNames(Names names) {
        List<String> items = new ArrayList<>();
        for (ThingNames thing : names.things()) {
            for (ChannelNames channel : thing.channels()) {
                items.add(channel.itemName());
                channel.split().forEach(s -> items.add(s.itemName()));
            }
        }
        return items;
    }

    @Test
    void accentsAreFolded() {
        assertEquals("Kuche_Decor", ProposalNames.fold("Küche Décor"));
        assertEquals("Strasse", ProposalNames.fold("Straße"));
        assertEquals("Etat_cuisine", ProposalNames.fold("État cuisine"));
        assertEquals("Licht_Kuche", ProposalNames.id("  Licht -- Küche!! ", "thing"));
        assertEquals("Wohnzimmer_Decke", ProposalNames.itemName("Wohnzimmer", "Decke"));
        assertEquals("gRuckseite", ProposalNames.groupName("Rückseite"));
    }

    @Test
    void emptyNamesGetTheFallback() {
        assertEquals("thing", ProposalNames.id("", "thing"));
        assertEquals("channel", ProposalNames.id("!!! ---", "channel"));
        assertEquals("item", ProposalNames.itemName(null, "%%"));
        assertEquals("gRoom", ProposalNames.groupName("--"));

        Names names = names(proposal(List.of(), thing("%%", null, channel("..."))));
        assertEquals(List.of("thing"), thingIds(names));
        assertEquals(List.of("channel"), channelIds(names));
        assertEquals(List.of("item"), itemNames(names));
    }

    @Test
    void longNamesAreCut() {
        assertEquals("a".repeat(40), ProposalNames.id("a".repeat(60), "thing"));
        assertEquals("a".repeat(39), ProposalNames.id("a".repeat(39) + " b", "thing"));
        assertEquals("b".repeat(80), ProposalNames.itemName(null, "b".repeat(100)));
        assertEquals(80, ProposalNames.groupName("c".repeat(100)).length());
    }

    @Test
    void aLeadingDigitGetsAnUnderscoreInItemNamesOnly() {
        assertEquals("_1st_floor", ProposalNames.itemName(null, "1st floor"));
        assertEquals("_2_Lamp", ProposalNames.itemName("2", "Lamp"));
        assertEquals("1st_floor", ProposalNames.id("1st floor", "thing"));
        assertEquals("g1st_floor", ProposalNames.groupName("1st floor"));
    }

    /** The words of core's Thing grammar (5.2 and 5.3) that an {@code ID} token cannot be without a {@code ^}. */
    static final List<String> THING_KEYWORDS = List.of("Thing", "Bridge", "Type", "State", "Trigger", "true", "false",
            "Switch", "Rollershutter", "Number", "String", "Dimmer", "Contact", "DateTime", "Color", "Player",
            "Location", "Call", "Image");
    /** The words of core's Items grammar that an Item name cannot be. */
    static final List<String> ITEM_KEYWORDS = List.of("true", "false");

    @Test
    void aWordOfTheThingGrammarIsNeverAThingOrChannelId() {
        for (String keyword : THING_KEYWORDS) {
            assertEquals(keyword + "_", ProposalNames.id(keyword, "thing"));
            Names names = names(proposal(List.of(), thing(keyword, null, channel(keyword), channel(keyword))));
            assertEquals(List.of(keyword + "_"), thingIds(names));
            assertEquals("knx:device:example:" + keyword + "_", names.things().get(0).uid());
            channelIds(names).forEach(id -> assertFalse(THING_KEYWORDS.contains(id), id));
            assertEquals(keyword + "_", channelIds(names).get(0));
        }
        assertEquals(Set.copyOf(THING_KEYWORDS), ProposalNames.THING_KEYWORDS);
        assertEquals(Set.copyOf(ITEM_KEYWORDS), ProposalNames.ITEM_KEYWORDS);
        assertEquals("switch", ProposalNames.id("switch", "channel"));
        assertEquals("Switches", ProposalNames.id("Switches", "channel"));
    }

    @Test
    void aWordOfTheItemsGrammarIsNeverAnItemName() {
        for (String keyword : ITEM_KEYWORDS) {
            assertEquals(keyword + "_", ProposalNames.itemName(null, keyword));
            assertEquals(List.of(keyword + "_"),
                    itemNames(names(proposal(List.of(), thing("Lamp", null, channel(keyword))))));
        }
        assertEquals("True", ProposalNames.itemName(null, "True"));
        assertEquals("Switch", ProposalNames.itemName(null, "Switch"));
    }

    @Test
    void collisionsWithinTheProposalGetANumber() {
        Names names = names(proposal(List.of(), thing("Kitchen", "Kitchen", channel("Light"), channel("Light")),
                thing("Kitchen", "Kitchen", channel("Light"))));
        assertEquals(List.of("Kitchen", "Kitchen_2"), thingIds(names));
        assertEquals(List.of("Light", "Light_2", "Light_3"), channelIds(names));
        assertEquals(List.of("Kitchen_Light", "Kitchen_Light_2", "Kitchen_Light_3"), itemNames(names));
    }

    @Test
    void splitChannelsCount() {
        Names names = names(proposal(List.of(),
                thing("Hall", null, channel("Dimmer", channel("Dimmer"), channel("Dimmer level")), channel("Dimmer"))));
        assertEquals(List.of("Dimmer_", "Dimmer_2", "Dimmer_level", "Dimmer_3"), channelIds(names));
        assertEquals(List.of("Dimmer", "Dimmer_2", "Dimmer_level", "Dimmer_3"), itemNames(names));
        ChannelNames combined = names.things().get(0).channels().get(0);
        assertEquals(2, combined.split().size());
        assertEquals("knx:device:example:Hall:Dimmer_2", combined.split().get(0).uid());
    }

    @Test
    void collisionsAgainstTheRegistriesGetANumber() {
        Set<String> things = Set.of("knx:device:example:Kitchen", "knx:device:example:Kitchen_2");
        Set<String> items = Set.of("Kitchen_Light", "gKitchen");
        Names names = new ProposalNames(things::contains, items::contains).names(BRIDGE,
                proposal(List.of(new Room("S-Kitchen", "Kitchen", "Room", null)),
                        thing("Kitchen", "Kitchen", channel("Light"))));
        assertEquals(List.of("Kitchen_3"), thingIds(names));
        assertEquals("knx:device:example:Kitchen_3", names.things().get(0).uid());
        assertEquals(List.of("Kitchen_Light_2"), itemNames(names));
        assertEquals("gKitchen_2", names.rooms().get(0).groupName());
    }

    @Test
    void theThingRegistryIsAskedForTheUid() {
        List<String> asked = new ArrayList<>();
        new ProposalNames(uid -> {
            asked.add(uid);
            return false;
        }, NONE).names("knx:serial:bus1", proposal(List.of(), thing("Kitchen", null, channel("Light"))));
        assertEquals(List.of("knx:device:bus1:Kitchen"), asked);
    }

    @Test
    void aNumberKeepsTheLimit() {
        String label = "d".repeat(45);
        Names names = names(proposal(List.of(), thing(label, null, channel(label), channel(label)),
                thing(label, null, channel("x"))));
        assertEquals(List.of("d".repeat(40), "d".repeat(38) + "_2"), thingIds(names));
        assertEquals(List.of("d".repeat(40), "d".repeat(38) + "_2", "x"), channelIds(names));
        assertEquals(List.of("d".repeat(45), "d".repeat(45) + "_2", "x"), itemNames(names));

        String longItem = "e".repeat(90);
        Names items = names(proposal(List.of(), thing("T", null, channel(longItem), channel(longItem))));
        assertEquals(List.of("e".repeat(80), "e".repeat(78) + "_2"), itemNames(items));
    }

    @Test
    void twoRoomsOfTheSameNameGetTwoGroups() {
        Names names = names(
                proposal(List.of(new Room("S-1", "Kitchen", "Room", null), new Room("S-2", "Kitchen", "Room", null)),
                        thing("Lamp", null, channel("gKitchen"))));
        assertEquals("S-1", names.rooms().get(0).roomId());
        assertEquals("S-2", names.rooms().get(1).roomId());
        // the Item gKitchen of the proposal takes the plain name first
        assertEquals("gKitchen_2", names.rooms().get(0).groupName());
        assertEquals("gKitchen_3", names.rooms().get(1).groupName());
    }

    @Test
    void uidsAreBuiltFromTheBridge() {
        Names names = new ProposalNames(NONE, NONE).names("knx:serial:bus1",
                proposal(List.of(), thing("Kitchen", null, channel("Light"))));
        assertEquals("knx:device:bus1:Kitchen", names.things().get(0).uid());
        assertEquals("knx:device:bus1:Kitchen:Light", names.things().get(0).channels().get(0).uid());
        assertEquals("knx:device:bus1:Kitchen", ProposalNames.thingUID("knx:serial:bus1", "Kitchen"));
    }

    @Test
    void everyNameMatchesItsPatternAndTheSameInputGivesTheSameNames() {
        KnxProposal proposal = proposal(
                List.of(new Room("S-1", "Küche / Bar", "Room", null)), thing("3 Küche", "Küche / Bar",
                        channel("[Licht] \"Decke\"", channel("ä"), channel("5")), channel("\n")),
                thing("Ölfeld", null, channel("Ölfeld")));
        Names names = names(proposal);
        thingIds(names).forEach(id -> assertTrue(ProposalNames.ID.matcher(id).matches(), id));
        channelIds(names).forEach(id -> assertTrue(ProposalNames.ID.matcher(id).matches(), id));
        itemNames(names).forEach(name -> assertTrue(ProposalNames.ITEM_NAME.matcher(name).matches(), name));
        names.rooms().forEach(room -> assertTrue(ProposalNames.ITEM_NAME.matcher(room.groupName()).matches()));
        assertEquals(names, names(proposal));
    }

    private static KnxProposal twoFloors() {
        return proposal(List.of(new Room("S-1", "House", "Building", null), new Room("S-2", "Down", "Floor", "S-1"),
                new Room("S-3", "Bath", "Room", "S-2"), new Room("S-4", "Up", "Floor", "S-1"),
                new Room("S-5", "Bath", "Room", "S-4")), thing("Light", "Bath", channel("Light")));
    }

    private static List<String> groups(Names names) {
        return names.rooms().stream().map(room -> room.groupName() + (room.existing() ? " there" : "")).toList();
    }

    @Test
    void equalRoomNamesOnTwoFloorsGetACounter() {
        assertEquals(List.of("gHouse", "gDown", "gBath", "gUp", "gBath_2"), groups(names(twoFloors())));
    }

    @Test
    void anExistingGroupOfTheSamePlaceIsUsedAndOneUnderAnotherFloorIsNot() {
        Map<String, List<String>> groupsInOpenhab = Map.of("gHouse", List.of(), "gDown", List.of("gHouse"), "gUp",
                List.of("gHouse", "gOther"), "gBath", List.of("gDown"), "gBath_2", List.of("gUp"));
        ProposalNames names = new ProposalNames(NONE,
                name -> groupsInOpenhab.containsKey(name) || "gDown_2".equals(name), groupsInOpenhab::get);

        assertEquals(List.of("gHouse there", "gDown there", "gBath there", "gUp there", "gBath_2 there"),
                groups(names.names(BRIDGE, twoFloors())));

        KnxProposal upOnly = proposal(List.of(new Room("S-1", "House", "Building", null),
                new Room("S-4", "Up", "Floor", "S-1"), new Room("S-5", "Bath", "Room", "S-4")),
                thing("Light", "Bath", channel("Light")));
        assertEquals(List.of("gHouse there", "gUp there", "gBath_2 there"), groups(names.names(BRIDGE, upOnly)));
    }

    @Test
    void anExistingItemThatIsNoGroupIsNeverUsed() {
        ProposalNames names = new ProposalNames(NONE, Set.of("gHouse", "gDown")::contains,
                name -> "gDown".equals(name) ? List.of("gElsewhere") : null);

        assertEquals(List.of("gHouse_2", "gDown_2", "gBath", "gUp", "gBath_2"),
                groups(names.names(BRIDGE, twoFloors())));
    }
}

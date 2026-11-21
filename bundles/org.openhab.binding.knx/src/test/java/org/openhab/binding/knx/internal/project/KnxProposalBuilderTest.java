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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.project.ChannelFixtures.*;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.ChannelFixtures.Obj;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.LeftOut;
import org.openhab.binding.knx.internal.project.KnxProposal.Part;
import org.openhab.binding.knx.internal.project.KnxProposal.Source;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;
import org.openhab.binding.knx.internal.project.KnxProposalBuilder.Selection;

/**
 * Tests for the Things, channels, notes and read marks of the wizard's proposal. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProposalBuilderTest {

    static int ga(int n) {
        return raw(31, 7, n);
    }

    static Selection addresses(Integer... raws) {
        return new Selection(List.of(raws), List.of(), List.of(), List.of(), true);
    }

    static Selection functions(String... ids) {
        return new Selection(List.of(), List.of(ids), List.of(), List.of(), true);
    }

    static Selection spaces(String... ids) {
        return new Selection(List.of(), List.of(), List.of(ids), List.of(), true);
    }

    static Selection devices(String... ids) {
        return new Selection(List.of(), List.of(), List.of(), List.of(ids), true);
    }

    static KnxProposal propose(KnxProject project, Selection selection) {
        return new KnxProposalBuilder(project).propose(selection, Set.of());
    }

    /** Per channel {@code key=text}, parts joined by {@code ,}. */
    static List<String> texts(Thing thing) {
        return thing.channels().stream().map(KnxProposalBuilderTest::text).toList();
    }

    static String text(Channel channel) {
        return String.join(",", channel.parts().stream().map(p -> p.key() + "=" + p.text()).toList());
    }

    static List<String> labels(Thing thing) {
        return thing.channels().stream().map(Channel::label).toList();
    }

    static Thing only(KnxProposal proposal) {
        assertEquals(1, proposal.things().size(), proposal.toString());
        return proposal.things().get(0);
    }

    static Channel onlyChannel(KnxProposal proposal) {
        Thing thing = only(proposal);
        assertEquals(1, thing.channels().size(), thing.toString());
        return thing.channels().get(0);
    }

    private static KnxSpace kitchen(KnxFunction... functions) {
        return space("S-1", "Kitchen", List.of(), List.of(), List.of(functions));
    }

    @Test
    void aFunctionIsOneThingWithItsAddresses() {
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"), address(3, ga(3), "1.001", "")),
                List.of(), List.of(kitchen(
                        function("F-1", "Ceiling light", ref(1, "Light", null), ref(2, "", null), ref(3, "", null)))));

        Thing thing = only(propose(project, functions("F-1")));

        assertEquals("Ceiling light", thing.label());
        assertEquals(Source.FUNCTION, thing.source());
        assertEquals("F-1", thing.sourceId());
        assertEquals("S-1", thing.roomId());
        assertEquals("Kitchen", thing.room());
        assertEquals(List.of("Light", "Address 2", "31/7/3"), labels(thing));
        assertEquals(List.of("ga=1.001:31/7/1", "ga=1.001:31/7/2", "ga=1.001:31/7/3"), texts(thing));
    }

    @Test
    void aRoomTakesItsFunctionsAndThoseOfTheSpacesBelowInWalkOrder() {
        KnxSpace inner = space("S-2", "Pantry", List.of(), List.of(),
                List.of(function("F-2", "Pantry light", ref(2, "", null))));
        KnxSpace outer = space("S-1", "Kitchen", List.of(inner), List.of(1),
                List.of(function("F-1", "Kitchen light", ref(1, "", null))));
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("Actuator", null, object("O-1", 1, "A", null, null, 5))), List.of(outer));

        KnxProposal proposal = propose(project, spaces("S-1"));

        assertEquals(List.of("Kitchen light", "Pantry light"), proposal.things().stream().map(Thing::label).toList());
        assertEquals("S-2", proposal.things().get(1).roomId());
        assertEquals(List.of(new KnxProposal.Room("S-1", "Kitchen", "Room", null),
                new KnxProposal.Room("S-2", "Pantry", "Room", "S-1")), proposal.rooms());
    }

    @Test
    void aNestedRoomTakesOnlyItsOwnFunctions() {
        KnxSpace inner = space("S-2", "Pantry", List.of(), List.of(),
                List.of(function("F-2", "Pantry light", ref(2, "", null))));
        KnxSpace outer = space("S-1", "Kitchen", List.of(inner), List.of(),
                List.of(function("F-1", "Kitchen light", ref(1, "", null))));
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")), List.of(),
                List.of(outer));

        assertEquals("Pantry light", only(propose(project, spaces("S-2"))).label());
    }

    @Test
    void aRoomWithoutAnyFunctionBelowTakesItsDevicesAndThoseBelow() {
        KnxSpace inner = space("S-2", "Pantry", List.of(), List.of(2), List.of());
        KnxSpace outer = space("S-1", "Kitchen", List.of(inner), List.of(1), List.of());
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("First", null, object("O-1", 1, "A", null, null, 1)),
                        device("Second", null, object("O-2", 1, "B", null, null, 2))),
                List.of(outer));

        KnxProposal proposal = propose(project, spaces("S-1"));

        assertEquals(List.of("First", "Second"), proposal.things().stream().map(Thing::label).toList());
        assertEquals(List.of(Source.DEVICE, Source.DEVICE), proposal.things().stream().map(Thing::source).toList());
        assertEquals("S-1", proposal.things().get(0).roomId());
        assertEquals("S-2", proposal.things().get(1).roomId());
    }

    @Test
    void anAddressInTwoFunctionsLandsInTheFirstByTheWalk() {
        KnxSpace first = space("S-1", "Kitchen", List.of(), List.of(),
                List.of(function("F-1", "Kitchen light", ref(1, "", null), ref(3, "", null))));
        KnxSpace second = space("S-2", "Hall", List.of(), List.of(),
                List.of(function("F-2", "Hall light", ref(2, "", null), ref(3, "", null))));
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"), address(3, ga(3), "1.001")), List.of(),
                List.of(first, second));

        KnxProposal proposal = propose(project, functions("F-2", "F-1"));

        assertEquals(List.of("Kitchen light", "Hall light"), proposal.things().stream().map(Thing::label).toList());
        assertEquals(List.of("ga=1.001:31/7/1", "ga=1.001:31/7/3"), texts(proposal.things().get(0)));
        assertEquals(List.of("ga=1.001:31/7/2"), texts(proposal.things().get(1)));
    }

    @Test
    void functionsThenDevicesThenLooseAddresses() {
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"), address(3, ga(3), "1.001")),
                List.of(device("Actuator", null, object("O-1", 1, "A", null, null, 2))),
                List.of(kitchen(function("F-1", "Kitchen light", ref(1, "", null)))));

        KnxProposal proposal = propose(project,
                new Selection(List.of(ga(3)), List.of("F-1"), List.of(), List.of("P-1_DI-1"), true));

        assertEquals(List.of(Source.FUNCTION, Source.DEVICE, Source.ADDRESSES),
                proposal.things().stream().map(Thing::source).toList());
        assertEquals(KnxProposalBuilder.LOOSE_ADDRESSES_LABEL, proposal.things().get(2).label());
        assertEquals("", proposal.things().get(2).sourceId());
        assertNull(proposal.things().get(2).roomId());
    }

    @Test
    void devicesInTopologyOrderWhateverTheSelectionOrder() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("First", null, object("O-1", 1, "A", null, null, 1)),
                        device("Second", null, object("O-2", 1, "B", null, null, 2))),
                List.of());

        KnxProposal proposal = propose(project, devices("P-1_DI-2", "P-1_DI-1"));

        assertEquals(List.of("First", "Second"), proposal.things().stream().map(Thing::label).toList());
    }

    @Test
    void looseAddressesAreOneThingLabelledByTheirNames() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "9.001", "")), List.of(),
                List.of());

        Thing thing = only(propose(project, addresses(ga(2), ga(1))));

        assertEquals(Source.ADDRESSES, thing.source());
        assertEquals(List.of("Address 1", "31/7/2"), labels(thing));
        assertEquals(List.of("ga=1.001:31/7/1", "ga=9.001:31/7/2"), texts(thing));
    }

    @Test
    void aDeviceThingIsLabelledByItsNameElseItsAddressElseItsId() {
        KnxIndividualAddress address = new KnxIndividualAddress(7, 7, 3);
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"), address(3, ga(3), "1.001")),
                List.of(device("Dimmer", null, object("O-1", 1, "A", null, null, 1)),
                        device("", address, object("O-2", 1, "B", null, null, 2)),
                        device("", null, object("O-3", 1, "C", null, null, 3))),
                List.of());

        KnxProposal proposal = propose(project, devices("P-1_DI-1", "7.7.3", "P-1_DI-3"));

        assertEquals(List.of("Dimmer", "7.7.3", "P-1_DI-3"), proposal.things().stream().map(Thing::label).toList());
        assertEquals(List.of("P-1_DI-1", "P-1_DI-2", "P-1_DI-3"),
                proposal.things().stream().map(Thing::sourceId).toList());
    }

    @Test
    void aDeviceThingHoldsTheAddressesItsObjectsSendOrReceiveOnWithTheObjectLabels() {
        KnxSpace room = space("S-1", "Kitchen", List.of(), List.of(1), List.of());
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"), address(3, ga(3), "1.001")),
                List.of(device("Actuator", null, object("O-1", 4, "Switch", "On/Off", null, 1, 3).flags("C-W--"),
                        object("O-2", 5, "Lock", null, null, 2).receiveOnly(), object("O-3", 6, "Idle", null, null))),
                List.of(room));

        Thing thing = only(propose(project, devices("P-1_DI-1")));

        assertEquals("S-1", thing.roomId());
        assertEquals(List.of("4 – Switch – On/Off", "5 – Lock", "4 – Switch – On/Off"), labels(thing));
        assertEquals(List.of("ga=1.001:31/7/1", "ga=1.001:31/7/2", "ga=1.001:31/7/3"), texts(thing));
    }

    @Test
    void aDeviceThingCarriesNoAddress() {
        for (Class<?> type : List.of(KnxProposal.class, Thing.class, Channel.class, Part.class)) {
            for (RecordComponent component : type.getRecordComponents()) {
                assertNotEquals("address", component.getName(), type.getSimpleName());
                assertNotEquals(KnxIndividualAddress.class, component.getType(), type.getSimpleName());
                assertNotEquals(KnxDevice.class, component.getType(), type.getSimpleName());
            }
        }
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001")),
                List.of(device("Dimmer", new KnxIndividualAddress(7, 7, 4), object("O-1", 1, "A", null, null, 1))),
                List.of());
        String all = propose(project, devices("7.7.4")).toString();
        assertFalse(all.contains("7.7.4"), all);
        assertFalse(all.contains("individual"), all);
    }

    @Test
    void unknownEntriesAreListedAsGiven() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001")),
                List.of(device("Dimmer", new KnxIndividualAddress(7, 7, 4))),
                List.of(kitchen(function("F-1", "Kitchen light", ref(1, "", null)))));

        KnxProposal proposal = propose(project, new Selection(List.of(ga(9), ga(1)), List.of("F-9", "F-1"),
                List.of("S-9"), List.of("P-1_DI-9", "7.7.9", "nonsense"), true));

        assertEquals(List.of("31/7/9", "F-9", "S-9", "P-1_DI-9", "7.7.9", "nonsense"), proposal.unknown());
    }

    @Test
    void aChosenFunctionWithoutAnAddressIsLeftOut() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001")), List.of(),
                List.of(kitchen(function("F-1", "Empty"), function("F-2", "Light", ref(1, "", null)),
                        function("F-3", "Bare"))));

        KnxProposal proposal = propose(project, functions("F-3", "F-2", "F-1", "F-9"));

        assertEquals("F-2", only(proposal).sourceId());
        assertEquals(List.of(new LeftOut(Source.FUNCTION, "F-1"), new LeftOut(Source.FUNCTION, "F-3")),
                proposal.leftOut());
        assertEquals(List.of("F-9"), proposal.unknown());
    }

    @Test
    void aChosenDeviceWithoutAnAddressOfItsOwnLeftIsLeftOut() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("Kept by the function", null, object("O-1", 1, "A", null, null, 1)),
                        device("Actuator", null, object("O-2", 1, "A", null, null, 1, 2)),
                        device("Kept by the actuator", null, object("O-3", 1, "A", null, null, 2)),
                        device("Without a link", new KnxIndividualAddress(7, 7, 4), object("O-4", 1, "A", null, null))),
                List.of(kitchen(function("F-1", "Light", ref(1, "", null)))));

        KnxProposal proposal = propose(project, new Selection(List.of(ga(1), ga(9)), List.of("F-1"), List.of(),
                List.of("P-1_DI-1", "P-1_DI-2", "P-1_DI-3", "7.7.4", "7.7.9"), true));

        assertEquals(List.of("F-1", "P-1_DI-2"), proposal.things().stream().map(Thing::sourceId).toList());
        assertEquals(List.of(new LeftOut(Source.DEVICE, "P-1_DI-1"), new LeftOut(Source.DEVICE, "P-1_DI-3"),
                new LeftOut(Source.DEVICE, "P-1_DI-4")), proposal.leftOut());
        assertEquals(List.of("31/7/9", "7.7.9"), proposal.unknown());
    }

    @Test
    void entriesChosenThroughASpaceAreLeftOutAsWell() {
        KnxSpace devicesOnly = space("S-1", "Cabinet", List.of(), List.of(1, 2), List.of());
        KnxSpace withFunctions = space("S-2", "Kitchen", List.of(), List.of(),
                List.of(function("F-1", "Light", ref(1, "", null)), function("F-2", "Empty")));
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001")),
                List.of(device("Actuator", null, object("O-1", 1, "A", null, null, 1)),
                        device("Without a link", null, object("O-2", 1, "A", null, null))),
                List.of(devicesOnly, withFunctions));

        KnxProposal proposal = propose(project, spaces("S-1", "S-2"));

        assertEquals(List.of("F-1"), proposal.things().stream().map(Thing::sourceId).toList());
        assertEquals(List.of(new LeftOut(Source.FUNCTION, "F-2"), new LeftOut(Source.DEVICE, "P-1_DI-1"),
                new LeftOut(Source.DEVICE, "P-1_DI-2")), proposal.leftOut());
    }

    @Test
    void nothingIsLeftOutWhenEveryChosenEntryGetsAThingAndLooseAddressesNeverAre() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("Actuator", null, object("O-1", 1, "A", null, null, 2))),
                List.of(kitchen(function("F-1", "Light", ref(1, "", null)))));

        KnxProposal proposal = propose(project,
                new Selection(List.of(ga(1), ga(2)), List.of("F-1"), List.of(), List.of("P-1_DI-1"), true));

        assertEquals(List.of(Source.FUNCTION, Source.DEVICE), proposal.things().stream().map(Thing::source).toList());
        assertEquals(List.of(), proposal.leftOut());
    }

    @Test
    void anAddressWithoutDataTypeIsNotSelectedAndKeepsTheMapperNotes() {
        KnxProject project = projectOf(List.of(address(1, ga(1), null), address(2, ga(2), "9")), List.of(), List.of());

        Thing thing = only(propose(project, addresses(ga(1), ga(2))));

        Channel none = thing.channels().get(0);
        assertFalse(none.selected());
        assertNull(none.channelType());
        assertEquals(List.of(), none.allowedChannelTypes());
        assertEquals(List.of(KnxChannelMapper.NOTE_NO_DATA_TYPE, KnxChannelMapper.NOTE_NO_LINKED_OBJECT), none.notes());
        Channel noSub = thing.channels().get(1);
        assertFalse(noSub.selected());
        assertNull(noSub.channelType());
        assertEquals(List.of(KnxChannelMapper.NOTE_NO_SUB_NUMBER, KnxChannelMapper.NOTE_NO_LINKED_OBJECT),
                noSub.notes());
        assertEquals("ga=31/7/2", text(noSub));
    }

    @Test
    void aTypeNoChannelWorksForIsNotSelected() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.010"), address(2, ga(2), "15.000")), List.of(),
                List.of());

        for (Channel channel : only(propose(project, addresses(ga(1), ga(2)))).channels()) {
            assertFalse(channel.selected());
            assertNull(channel.channelType());
            assertEquals(List.of(), channel.allowedChannelTypes());
            assertTrue(channel.notes().contains(KnxProposalBuilder.NOTE_NO_CHANNEL_TYPE), channel.toString());
        }
    }

    @Test
    void allowedChannelTypesByThePerKeyTable() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "20.102"),
                address(3, ga(3), "3.007"), address(4, ga(4), "1.008"), address(5, ga(5), "232.600"),
                address(6, ga(6), "5.001"), address(7, ga(7), "6.020"), address(8, ga(8), "1.022")), List.of(),
                List.of());

        Thing thing = only(propose(project, addresses(ga(1), ga(2), ga(3), ga(4), ga(5), ga(6), ga(7), ga(8))));

        assertEquals(
                Arrays.asList("switch", "number", "dimmer", "rollershutter", "color", "number", "string", "number"),
                thing.channels().stream().map(Channel::channelType).toList());
        assertEquals(
                List.of(List.of("switch"), List.of("number", "string"), List.of("dimmer"), List.of("rollershutter"),
                        List.of("color"), List.of("number"), List.of("string"), List.of("number")),
                thing.channels().stream().map(Channel::allowedChannelTypes).toList());
        assertEquals(List.of("ga", "ga", "increaseDecrease", "upDown", "hsb", "ga", "ga", "ga"),
                thing.channels().stream().map(c -> c.parts().get(0).key()).toList());
        assertTrue(thing.channels().stream().allMatch(Channel::selected));
        assertTrue(thing.channels().stream().noneMatch(Channel::combined));
    }

    @Test
    void anAddressTheBridgeUsesAlreadyIsNotSelected() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")), List.of(),
                List.of());

        Thing thing = only(new KnxProposalBuilder(project).propose(addresses(ga(1), ga(2)), Set.of(ga(2))));

        assertTrue(thing.channels().get(0).selected());
        assertEquals(List.of(), thing.channels().get(0).alreadyConfigured());
        Channel used = thing.channels().get(1);
        assertFalse(used.selected());
        assertEquals(List.of(ga(2)), used.alreadyConfigured().stream().map(KnxGroupAddress::rawAddress).toList());
        assertEquals(List.of(KnxChannelMapper.NOTE_NO_LINKED_OBJECT, KnxProposalBuilder.NOTE_ALREADY_CONFIGURED),
                used.notes());
    }

    @Test
    void theMapperReadMarkOnTheMainAddress() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("Sensor", null, object("O-1", 1, "A", null, null, 1).read())), List.of());

        Thing thing = only(propose(project, addresses(ga(1), ga(2))));

        assertEquals(List.of("ga=1.001:<31/7/1", "ga=1.001:31/7/2"), texts(thing));
        assertTrue(thing.channels().get(0).parts().get(0).read());
        assertFalse(thing.channels().get(1).parts().get(0).read());
    }

    @Test
    void aFurtherLinkOfAnInputIsNotAddedToItsChannel() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("Sensor", null, object("O-1", 1, "A", null, null, 1, 2).flags("C-W--"),
                        object("O-2", 2, "B", null, null, 2).read())),
                List.of());

        Thing thing = only(propose(project, addresses(ga(1), ga(2))));

        assertEquals(List.of("ga=1.001:31/7/1", "ga=1.001:<31/7/2"), texts(thing));
    }

    @Test
    void aFilterDeviceGivesNoChannelAnAddressOfItAndNoReadMarkOnItsFurtherLinks() {
        List<KnxGroupAddress> addresses = new ArrayList<>(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"), address(9, ga(9), "1.001")));
        List<Integer> command = new ArrayList<>(List.of(20, 1));
        List<Integer> status = new ArrayList<>(List.of(2));
        for (int n = 20; n < 40; n++) {
            addresses.add(address(n, ga(n), "1.001"));
            command.add(n);
            status.add(n);
        }
        KnxProject project = projectOf(addresses,
                List.of(device("Filter", null, new Obj("O-F1", 1, "Dummy 1", null, command, null).flags("CRWT-"),
                        new Obj("O-F2", 2, "Dummy 2", null, status, null).flags("CRWT-")),
                        device("Actuator", null, object("O-1", 1, "Switch", null, null, 1, 9).flags("C-W--"),
                                object("O-2", 2, "State", null, null, 2).flags("CR-T-"))),
                List.of());

        Thing thing = only(propose(project, addresses(ga(1), ga(2), ga(21))));

        assertEquals(List.of("ga=1.001:31/7/1", "ga=1.001:<31/7/2", "ga=1.001:31/7/21"), texts(thing));
        assertTrue(thing.channels().get(1).statusOnly());
    }

    @Test
    void noReadMarkWhenReadAtStartIsOff() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001")),
                List.of(device("Sensor", null, object("O-1", 1, "A", null, null, 1).read())), List.of());

        Channel channel = onlyChannel(
                propose(project, new Selection(List.of(ga(1)), List.of(), List.of(), List.of(), false)));

        assertEquals("ga=1.001:31/7/1", text(channel));
        assertFalse(channel.parts().get(0).read());
    }

    @Test
    void aStatusAddressNothingPairsWithIsAStatusOnlyChannel() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001")),
                List.of(device("Actuator", null, object("O-1", 1, "Status", null, null, 1).flags("CR-T-"),
                        object("O-2", 2, "Switch", null, null, 2).flags("C-W--"))),
                List.of());

        Thing thing = only(propose(project, addresses(ga(1), ga(2))));

        assertTrue(thing.channels().get(0).statusOnly());
        assertTrue(thing.channels().get(0).selected());
        assertFalse(thing.channels().get(1).statusOnly());
        assertEquals(List.of("ga=1.001:<31/7/1", "ga=1.001:31/7/2"), texts(thing));
    }

    @Test
    void channelsAreOrderedByRawAddress() {
        KnxProject project = projectOf(List.of(address(3, ga(3), "1.001"), address(1, ga(1), "1.001")), List.of(),
                List.of(kitchen(function("F-1", "Kitchen light", ref(3, "", null), ref(1, "", null)))));

        assertEquals(List.of("ga=1.001:31/7/1", "ga=1.001:31/7/3"), texts(only(propose(project, functions("F-1")))));
    }

    @Test
    void theSameInputGivesTheSameOutput() {
        KnxSpace room = space("S-1", "Kitchen", List.of(), List.of(1),
                List.of(function("F-1", "Kitchen light", ref(1, "Do", "DoAbc"), ref(2, "Info", "InfoAbc"))));
        List<KnxGroupAddress> addresses = List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"),
                address(3, ga(3), "5.001"), address(4, ga(4), "9.001"));
        List<Dev> devices = List.of(device("Actuator", null, object("O-1", 1, "Switch", null, null, 1).flags("C-W--"),
                object("O-2", 2, "Status", null, null, 2).flags("CR-T-"),
                object("O-3", 3, "Value", null, null, 3, 4).flags("CRW--")));
        Selection selection = new Selection(List.of(ga(4), ga(3)), List.of("F-1"), List.of("S-1"), List.of("P-1_DI-1"),
                true);

        KnxProposal first = propose(projectOf(addresses, devices, List.of(room)), selection);
        KnxProposal second = propose(projectOf(addresses, devices, List.of(room)), selection);

        assertEquals(first, second);
        assertEquals(first.toString(), second.toString());
    }

    private static KnxSpace typed(String id, String name, String type, List<KnxSpace> spaces, List<Integer> devices,
            KnxFunction... functions) {
        return new KnxSpace(id, name, type, null, null, null, spaces, devices.stream().map(d -> "P-1_DI-" + d).toList(),
                List.of(functions));
    }

    /**
     * A building with a part, two floors with a room Bath each, a function at the level of the upper floor, an
     * empty room and a device that is in no space.
     */
    private static KnxProject house() {
        KnxSpace bathDown = typed("S-5", "Bath", "Room", List.of(), List.of(),
                function("F-1", "Bath light down", ref(1, "", null)));
        KnxSpace empty = typed("S-6", "Store", "Room", List.of(), List.of());
        KnxSpace down = typed("S-3", "Ground floor", "Floor", List.of(bathDown, empty), List.of());
        KnxSpace bathUp = typed("S-7", "Bath", "Room", List.of(), List.of(),
                function("F-2", "Bath light up", ref(2, "", null)));
        KnxSpace up = typed("S-4", "Upper floor", "Floor", List.of(bathUp), List.of(),
                function("F-3", "Landing light", ref(3, "", null)));
        KnxSpace part = typed("S-2", "East wing", "BuildingPart", List.of(down, up), List.of());
        KnxSpace building = typed("S-1", "House", "Building", List.of(part), List.of());
        KnxSpace other = typed("S-9", "Shed", "Building", List.of(), List.of());
        return projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1.001"), address(3, ga(3), "1.001"),
                        address(4, ga(4), "1.001")),
                List.of(device("Loose", null, object("O-1", 1, "A", null, null, 4))), List.of(building, other));
    }

    @Test
    void theRoomsAreEverySpaceOnThePathOfAThingAParentBeforeItsChildren() {
        KnxProposal proposal = propose(house(),
                new Selection(List.of(), List.of("F-2", "F-1", "F-3"), List.of(), List.of("P-1_DI-1"), true));

        assertEquals(List.of(new KnxProposal.Room("S-1", "House", "Building", null),
                new KnxProposal.Room("S-2", "East wing", "BuildingPart", "S-1"),
                new KnxProposal.Room("S-3", "Ground floor", "Floor", "S-2"),
                new KnxProposal.Room("S-5", "Bath", "Room", "S-3"),
                new KnxProposal.Room("S-4", "Upper floor", "Floor", "S-2"),
                new KnxProposal.Room("S-7", "Bath", "Room", "S-4")), proposal.rooms());
        assertEquals(List.of("Bath light down S-5", "Landing light S-4", "Bath light up S-7", "Loose null"),
                proposal.things().stream().map(thing -> thing.label() + " " + thing.roomId()).toList());
    }

    @Test
    void aSpaceUsedByNoThingBelowItGivesNoRoom() {
        KnxProposal proposal = propose(house(), functions("F-1"));

        assertEquals(List.of("S-1", "S-2", "S-3", "S-5"), proposal.rooms().stream().map(KnxProposal.Room::id).toList());
    }
}

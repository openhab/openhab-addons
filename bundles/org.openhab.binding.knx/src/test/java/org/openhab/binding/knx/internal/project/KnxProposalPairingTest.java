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
import static org.openhab.binding.knx.internal.project.KnxProposalBuilderTest.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.Pairing;
import org.openhab.binding.knx.internal.project.KnxProposal.Part;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;

/**
 * Tests for pairing a command with its status address: each rule pairs only when it gives exactly one address.
 * All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProposalPairingTest {

    /** The part of the channel whose main address is {@code ga(n)}. */
    private static Part part(KnxProposal proposal, int n) {
        for (Thing thing : proposal.things()) {
            for (Channel channel : thing.channels()) {
                for (Part part : channel.parts()) {
                    if (part.groupAddress().rawAddress() == ga(n)) {
                        return part;
                    }
                }
            }
        }
        throw new AssertionError("no channel for 31/7/" + n + " in " + proposal);
    }

    private static boolean hasChannel(KnxProposal proposal, int n) {
        return proposal.things().stream().flatMap(t -> t.channels().stream()).flatMap(c -> c.parts().stream())
                .anyMatch(p -> p.groupAddress().rawAddress() == ga(n));
    }

    private static @Nullable Integer status(Part part) {
        KnxGroupAddress status = part.statusAddress();
        return status == null ? null : status.rawAddress();
    }

    private static List<Integer> candidates(Part part) {
        return part.candidates().stream().map(KnxGroupAddress::rawAddress).toList();
    }

    private static List<KnxGroupAddress> switches(int count) {
        List<KnxGroupAddress> addresses = new ArrayList<>();
        for (int n = 1; n <= count; n++) {
            addresses.add(address(n, ga(n), "1.001"));
        }
        return addresses;
    }

    private static KnxSpace room(KnxFunction... functions) {
        return space("S-1", "Kitchen", List.of(), List.of(), List.of(functions));
    }

    // rule 1: ROLE

    @Test
    void aRolePairPairsByRole() {
        KnxProject project = projectOf(switches(2), List.of(),
                List.of(room(function("F-1", "Light", ref(1, "Do", "DoAbc"), ref(2, "Info", "InfoAbc")))));

        KnxProposal proposal = propose(project, functions("F-1"));

        Part part = part(proposal, 1);
        assertEquals(Pairing.ROLE, part.pairing());
        assertEquals(ga(2), status(part));
        assertEquals("1.001:31/7/1+31/7/2", part.text());
        assertFalse(hasChannel(proposal, 2));
    }

    @Test
    void aRoleThatIsExactlyInfoPairsNothing() {
        KnxProject project = projectOf(switches(2), List.of(),
                List.of(room(function("F-1", "Light", ref(1, "Do", "DoAbc"), ref(2, "Info", "Info")))));

        KnxProposal proposal = propose(project, functions("F-1"));

        assertEquals(Pairing.NONE, part(proposal, 1).pairing());
        assertNull(part(proposal, 1).statusAddress());
        assertTrue(hasChannel(proposal, 2));
    }

    @Test
    void aRoleWithTwoInfoMatchesFallsToTheNextRule() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 3).flags("CR-T-").channel("CH-1"))),
                List.of(room(function("F-1", "Light", ref(1, "Do", "DoAbc"), ref(2, "Info", "InfoAbc"),
                        ref(3, "Info", "InfoAbc")))));

        Part part = part(propose(project, functions("F-1")), 1);

        assertEquals(Pairing.OBJECTS, part.pairing());
        assertEquals(ga(3), status(part));
    }

    @Test
    void theRoleRuleHoldsOnlyInAFunctionThing() {
        KnxProject project = projectOf(switches(2), List.of(),
                List.of(room(function("F-1", "Light", ref(1, "Do", "DoAbc"), ref(2, "Info", "InfoAbc")))));

        assertEquals(Pairing.NONE, part(propose(project, addresses(ga(1), ga(2))), 1).pairing());
    }

    @Test
    void aRolePartnerOfAnotherTypeDoesNotPair() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "5.001")), List.of(),
                List.of(room(function("F-1", "Light", ref(1, "Do", "DoAbc"), ref(2, "Info", "InfoAbc")))));

        assertEquals(Pairing.NONE, part(propose(project, functions("F-1")), 1).pairing());
    }

    // rule 2: OBJECTS

    @Test
    void oneStatusInTheDeviceChannelPairsByObjects() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of());

        KnxProposal proposal = propose(project, devices("P-1_DI-1"));

        Part part = part(proposal, 1);
        assertEquals(Pairing.OBJECTS, part.pairing());
        assertEquals(ga(2), status(part));
        assertFalse(hasChannel(proposal, 2));
        assertEquals(1, only(proposal).channels().size());
    }

    @Test
    void aStatusOfAnotherModuleInstanceDoesNotCount() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1").module("MI-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1").module("MI-2"))),
                List.of());

        assertNotEquals(Pairing.OBJECTS, part(propose(project, addresses(ga(1))), 1).pairing());
    }

    @Test
    void twoStatusesInTheChannelAreDecidedByTheObjectNames() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Light switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Fan feedback", null, null, 2).flags("CR-T-").channel("CH-1"),
                        object("O-3", 3, "Light switch status", null, null, 3).flags("CR-T-").channel("CH-1"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.OBJECTS, part.pairing());
        assertEquals(ga(3), status(part));
    }

    @Test
    void twoStatusesTheObjectNamesDoNotDecideFallToTheNextRule() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Gamma", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Alpha status", null, null, 2).flags("CR-T-").channel("CH-1"),
                        object("O-3", 3, "Beta status", null, null, 3).flags("CR-T-").channel("CH-1"))),
                List.of(room(function("F-1", "Light", ref(1, "", null), ref(3, "", null)))));

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.FUNCTION, part.pairing());
        assertEquals(ga(3), status(part));
    }

    @Test
    void twoStatusesTheObjectNamesBothMatchFallToTheNextRule() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Switch status", null, null, 2).flags("CR-T-").channel("CH-1"),
                        object("O-3", 3, "Switch status", null, null, 3).flags("CR-T-").channel("CH-1"))),
                List.of(room(function("F-1", "Light", ref(1, "", null), ref(3, "", null)))));

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.FUNCTION, part.pairing());
        assertEquals(ga(3), status(part));
    }

    @Test
    void twoStatusesTheObjectNamesBothMatchAndNoOtherRuleDecidesStayCandidates() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Switch status", null, null, 2).flags("CR-T-").channel("CH-1"),
                        object("O-3", 3, "Switch status", null, null, 3).flags("CR-T-").channel("CH-1"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.NONE, part.pairing());
        assertNull(status(part));
        assertEquals(List.of(ga(2), ga(3)), candidates(part));
    }

    @Test
    void aCentralCommandReceivedBySeveralChannelsIsNotPairedByObjects() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Channel A", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Feedback", null, null, 2).flags("CR-T-").channel("CH-1"),
                        object("O-3", 3, "Channel B", null, null, 1).flags("C-W--").channel("CH-2").receiveOnly(),
                        object("O-4", 4, "Feedback", null, null, 3).flags("CR-T-").channel("CH-2"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.NONE, part.pairing());
        assertEquals(List.of(ga(2), ga(3)), candidates(part));
    }

    @Test
    void aReceiverWithoutDeviceChannelGivesNothingByObjectsAndRuleThreeTakesTheDevice() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null, object("O-1", 1, "Switch", null, null, 1).flags("C-W--"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-"))),
                List.of(room(function("F-1", "Light", ref(1, "", null), ref(2, "", null)))));

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.FUNCTION, part.pairing());
        assertEquals(ga(2), status(part));
    }

    @Test
    void aPushButtonIsNoReceiverWhileAnActuatorListens() {
        KnxProject project = projectOf(switches(3), List.of(
                device("Push button", null, object("O-1", 1, "Button", null, null, 1).flags("C-WT-").channel("CH-1"),
                        object("O-2", 2, "LED", null, null, 2).flags("CR-T-").channel("CH-1")),
                device("Actuator", null, object("O-3", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-4", 2, "Status", null, null, 3).flags("CR-T-").channel("CH-1"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.OBJECTS, part.pairing());
        assertEquals(ga(3), status(part));
    }

    @Test
    void withoutAnActuatorThePushButtonIsTheReceiver() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Push button", null,
                        object("O-1", 1, "Button", null, null, 1).flags("C-WT-").channel("CH-1"),
                        object("O-2", 2, "LED", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.OBJECTS, part.pairing());
        assertEquals(ga(2), status(part));
    }

    // rule 3: FUNCTION

    private static KnxProject twoStatusesWithoutChannel(KnxSpace... locations) {
        return projectOf(switches(3),
                List.of(device("Actuator", null, object("O-1", 1, "Switch", null, null, 1).flags("C-W--"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-"),
                        object("O-3", 3, "Status", null, null, 3).flags("CR-T-"))),
                List.of(locations));
    }

    @Test
    void aFunctionThingNarrowsToItsOwnFunction() {
        KnxProject project = twoStatusesWithoutChannel(
                room(function("F-1", "Light", ref(1, "", null), ref(3, "", null)),
                        function("F-2", "Other", ref(2, "", null))));

        Part part = part(propose(project, functions("F-1")), 1);

        assertEquals(Pairing.FUNCTION, part.pairing());
        assertEquals(ga(3), status(part));
    }

    @Test
    void anotherThingNarrowsToTheFunctionsHoldingTheCommand() {
        KnxProject project = twoStatusesWithoutChannel(
                room(function("F-1", "Light", ref(1, "", null), ref(2, "", null)), function("F-2", "Other")));

        Part part = part(propose(project, devices("P-1_DI-1")), 1);

        assertEquals(Pairing.FUNCTION, part.pairing());
        assertEquals(ga(2), status(part));
    }

    @Test
    void aCommandInNoFunctionIsNotPairedByFunction() {
        KnxProject project = twoStatusesWithoutChannel(room(function("F-2", "Other", ref(2, "", null))));

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.NONE, part.pairing());
        assertEquals(List.of(ga(2), ga(3)), candidates(part));
    }

    @Test
    void twoStatusesInTheFunctionDoNotPairByFunction() {
        KnxProject project = twoStatusesWithoutChannel(
                room(function("F-1", "Light", ref(1, "", null), ref(2, "", null), ref(3, "", null))));

        assertEquals(Pairing.NONE, part(propose(project, functions("F-1")), 1).pairing());
    }

    // rule 4: NAMES

    @Test
    void theGroupAddressNamesChooseAmongTheCandidates() {
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001", "Kitchen light"), address(2, ga(2), "1.001", "Hall light status"),
                        address(3, ga(3), "1.001", "Kitchen light status")),
                List.of(device("Actuator", null, object("O-1", 1, "Switch", null, null, 1).flags("C-W--"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-"),
                        object("O-3", 3, "Status", null, null, 3).flags("CR-T-"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.NAMES, part.pairing());
        assertEquals(ga(3), status(part));
    }

    @Test
    void twoNamesThatMatchDoNotPair() {
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001", "Kitchen light"), address(2, ga(2), "1.001", "Kitchen light fb"),
                        address(3, ga(3), "1.001", "Kitchen light status")),
                List.of(device("Actuator", null, object("O-1", 1, "Switch", null, null, 1).flags("C-W--"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-"),
                        object("O-3", 3, "Status", null, null, 3).flags("CR-T-"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.NONE, part.pairing());
        assertEquals(List.of(ga(2), ga(3)), candidates(part));
    }

    @Test
    void theNameRule() {
        assertFalse(KnxProposalBuilder.namesMatch("Status", "Info"));
        assertFalse(KnxProposalBuilder.namesMatch("Status", "Status"));
        assertFalse(KnxProposalBuilder.namesMatch("", ""));
        assertTrue(KnxProposalBuilder.namesMatch("État cuisine", "cuisine"));
        assertTrue(KnxProposalBuilder.namesMatch("Rückmeldung Küche", "Küche"));
        assertTrue(KnxProposalBuilder.namesMatch("Kitchen light", "Kitchen light switch"));
        assertTrue(KnxProposalBuilder.namesMatch("Fan", "Fan status"));
        assertFalse(KnxProposalBuilder.namesMatch("Fan", "Fanatic"));
        assertTrue(KnxProposalBuilder.namesMatch("Lamp", "Lamp-post"));
        assertFalse(KnxProposalBuilder.namesMatch("Hall light", "Kitchen light"));
        assertTrue(KnxProposalBuilder.namesMatch("LIGHT_DESK", "light desk feedback"));
    }

    // rule 5: NONE

    @Test
    void nothingToChooseFromIsNoneWithAtMostTwentyCandidatesByAddress() {
        List<KnxGroupAddress> addresses = switches(23);
        List<ChannelFixtures.Obj> objects = new ArrayList<>();
        objects.add(object("O-1", 1, "Switch", null, null, 1).flags("C-W--"));
        for (int n = 23; n >= 2; n--) {
            objects.add(object("O-" + n, n, "Status", null, null, n).flags("CR-T-"));
        }
        KnxProject project = projectOf(addresses, List.of(new Dev("Actuator", null, objects)), List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.NONE, part.pairing());
        assertNull(part.statusAddress());
        List<Integer> expected = new ArrayList<>();
        for (int n = 2; n <= 21; n++) {
            expected.add(ga(n));
        }
        assertEquals(expected, candidates(part));
    }

    @Test
    void addressesOfAnotherTypeDoNotCount() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "5.001")),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals(Pairing.NONE, part.pairing());
        assertEquals(List.of(), candidates(part));
    }

    @Test
    void aTypeWithoutSubNumberMatchesByMainNumber() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "1")),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of());

        assertEquals(ga(2), status(part(propose(project, addresses(ga(1))), 1)));
    }

    @Test
    void aPairedStatusAddressNeedNotBeSelected() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of());

        assertEquals(ga(2), status(part(propose(project, addresses(ga(1))), 1)));
    }

    // read marks and channels

    @Test
    void theReadMarkMovesToTheStatusAddress() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("CRW--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals("1.001:31/7/1+<31/7/2", part.text());
        assertFalse(part.read());
        assertTrue(part.statusRead());
    }

    @Test
    void aStatusThatIsOnlyAFurtherLinkOfAReadableObjectIsNotReadAndTheMarkFallsBackToTheCommand() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null, object("O-1", 1, "Switch", null, null, 1).flags("CRW--"),
                        object("O-2", 2, "Status", null, null, 3, 2).flags("CR-T-"))),
                List.of(room(function("F-1", "Light", ref(1, "Do", "DoAbc"), ref(2, "Info", "InfoAbc")))));

        Part part = part(propose(project, functions("F-1")), 1);

        assertEquals(ga(2), status(part));
        assertEquals("1.001:<31/7/1+31/7/2", part.text());
        assertTrue(part.read());
        assertFalse(part.statusRead());
    }

    @Test
    void aStatusOnlyAFilterDeviceAnswersIsNotReadAndTheMarkFallsBackToTheCommand() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null, object("O-1", 1, "Switch", null, null, 1).flags("CRW--")),
                        device("", null, object("O-F", 1, "Value", null, null, 2).flags("CR-T-")).of(filterProduct())),
                List.of(room(function("F-1", "Light", ref(1, "Do", "DoAbc"), ref(2, "Info", "InfoAbc")))));

        Part part = part(propose(project, functions("F-1")), 1);

        assertEquals(ga(2), status(part));
        assertEquals("1.001:<31/7/1+31/7/2", part.text());
        assertTrue(part.read());
        assertFalse(part.statusRead());
    }

    @Test
    void aFilterDeviceTickedByHandStillGetsItsThingWithoutReadMark() {
        KnxProject project = projectOf(switches(1), List.of(
                device("", new KnxIndividualAddress(15, 15, 1), object("O-F", 1, "Value", null, null, 1).flags("CRWT-"))
                        .of(filterProduct())),
                List.of());

        KnxProposal proposal = propose(project, devices("P-1_DI-1"));

        assertEquals("1.001:31/7/1", part(proposal, 1).text());
        assertFalse(part(proposal, 1).read());
    }

    @Test
    void theReadMarkStaysOnTheCommandWhenTheStatusCannotBeRead() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("CRW--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("-R-T-").channel("CH-1"))),
                List.of());

        Part part = part(propose(project, addresses(ga(1))), 1);

        assertEquals("1.001:<31/7/1+31/7/2", part.text());
        assertTrue(part.read());
        assertFalse(part.statusRead());
    }

    @Test
    void aStatusSharedByTwoCommandsIsReadOnceInTheWholeProposal() {
        KnxProject project = projectOf(switches(3),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1, 2).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 3).flags("CR-T-").channel("CH-1"))),
                List.of());

        KnxProposal proposal = propose(project, addresses(ga(1), ga(2)));

        assertEquals("1.001:31/7/1+<31/7/3", part(proposal, 1).text());
        assertEquals("1.001:31/7/2+31/7/3", part(proposal, 2).text());
        assertEquals(ga(3), status(part(proposal, 2)));
        assertEquals(1, proposal.toString().chars().filter(c -> c == '<').count(), proposal.toString());
    }

    @Test
    void aStatusUsedByAPairingHasNoChannelAnywhere() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of(room(function("F-1", "Light", ref(1, "", null)))));

        KnxProposal proposal = propose(project,
                new KnxProposalBuilder.Selection(List.of(ga(2)), List.of("F-1"), List.of(), List.of(), true));

        assertEquals(1, proposal.things().size());
        assertEquals(ga(2), status(part(proposal, 1)));
        assertFalse(hasChannel(proposal, 2));
    }

    @Test
    void aStatusNoCommandOfTheProposalPairsWithIsStatusOnly() {
        KnxProject project = projectOf(switches(2),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Status", null, null, 2).flags("CR-T-").channel("CH-1"))),
                List.of());

        KnxProposal proposal = propose(project, addresses(ga(2)));

        Channel channel = onlyChannel(proposal);
        assertTrue(channel.statusOnly());
        assertEquals("ga=1.001:<31/7/2", text(channel));
        assertEquals(Pairing.NONE, channel.parts().get(0).pairing());
    }
}

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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.ChannelFixtures.Obj;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;

/**
 * Tests for the combined dimmer, rollershutter and color channels of the proposal. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProposalCombiningTest {

    /** Command addresses {@code 31/7/1} … of the given types, each received by one object of channel CH-1. */
    private static KnxProject commands(@Nullable String channel, String... types) {
        List<KnxGroupAddress> addresses = new ArrayList<>();
        List<Obj> objects = new ArrayList<>();
        List<KnxFunctionGroupAddress> refs = new ArrayList<>();
        for (int n = 1; n <= types.length; n++) {
            addresses.add(address(n, ga(n), types[n - 1]));
            Obj object = object("O-" + n, n, "Object " + n, null, null, n).flags("C-W--");
            objects.add(channel == null ? object : object.channel(channel));
            refs.add(ref(n, "Part " + n, null));
        }
        KnxSpace room = space("S-1", "Kitchen", List.of(), List.of(1),
                List.of(new KnxFunction("F-1", "Light", "FT-1", refs)));
        return projectOf(addresses, List.of(new Dev("Actuator", null, objects)), List.of(room));
    }

    private static List<@Nullable String> types(Thing thing) {
        return thing.channels().stream().map(Channel::channelType).toList();
    }

    @Test
    void aDimmerIsOneChannelWithEachPartPairedAndItsSplit() {
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "5.001"), address(3, ga(3), "3.007"),
                        address(4, ga(4), "1.001"), address(5, ga(5), "5.001")),
                List.of(device("Actuator", null,
                        object("O-1", 1, "Switch", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "Value", null, null, 2).flags("C-W--").channel("CH-1"),
                        object("O-3", 3, "Dim", null, null, 3).flags("C-W--").channel("CH-1"),
                        object("O-4", 4, "Switch status", null, null, 4).flags("CR-T-").channel("CH-1"),
                        object("O-5", 5, "Value status", null, null, 5).flags("CR-T-").channel("CH-1"))),
                List.of(space("S-1", "Kitchen", List.of(), List.of(),
                        List.of(function("F-1", "Light", ref(3, "Dimming", null), ref(2, "Brightness", null),
                                ref(1, "Light switch", null), ref(4, "", null), ref(5, "", null))))));

        Thing thing = only(propose(project, functions("F-1")));

        assertEquals(1, thing.channels().size(), thing.toString());
        Channel dimmer = thing.channels().get(0);
        assertEquals("dimmer", dimmer.channelType());
        assertTrue(dimmer.combined());
        assertTrue(dimmer.selected());
        assertEquals(List.of("dimmer"), dimmer.allowedChannelTypes());
        assertEquals("Light switch", dimmer.label());
        assertEquals("switch=1.001:31/7/1+<31/7/4,position=5.001:31/7/2+<31/7/5,increaseDecrease=3.007:31/7/3",
                text(dimmer));
        assertEquals(List.of(KnxProposal.Pairing.OBJECTS, KnxProposal.Pairing.OBJECTS, KnxProposal.Pairing.NONE),
                dimmer.parts().stream().map(KnxProposal.Part::pairing).toList());
        assertEquals(List.of("ga=1.001:31/7/1+<31/7/4", "ga=5.001:31/7/2+<31/7/5", "increaseDecrease=3.007:31/7/3"),
                dimmer.split().stream().map(KnxProposalBuilderTest::text).toList());
        assertEquals(Arrays.asList("switch", "number", "dimmer"),
                dimmer.split().stream().map(Channel::channelType).toList());
        assertEquals(List.of("Light switch", "Brightness", "Dimming"),
                dimmer.split().stream().map(Channel::label).toList());
        assertTrue(dimmer.split().stream().noneMatch(Channel::combined));
    }

    @Test
    void aDimmerWithoutIncreaseDecrease() {
        Thing thing = only(propose(commands("CH-1", "1.001", "5.001"), functions("F-1")));

        assertEquals(List.of("switch=1.001:31/7/1,position=5.001:31/7/2"), texts(thing));
    }

    @Test
    void aDimmerWithOneSwitchTooManyIsNotCombined() {
        Thing thing = only(propose(commands("CH-1", "1.001", "5.001", "3.007", "1.001"), functions("F-1")));

        assertEquals(Arrays.asList("switch", "number", "dimmer", "switch"), types(thing));
        assertTrue(thing.channels().stream().noneMatch(Channel::combined));
    }

    @Test
    void aDimmerNeedsNoUpDownCommand() {
        Thing thing = only(propose(commands("CH-1", "1.001", "5.001", "1.008", "1.008"), functions("F-1")));

        assertTrue(thing.channels().stream().noneMatch(Channel::combined), thing.toString());
    }

    @Test
    void aRollershutterIsOneChannelWithItsSplit() {
        Thing thing = only(propose(commands("CH-1", "1.008", "1.010", "5.001"), functions("F-1")));

        assertEquals(1, thing.channels().size(), thing.toString());
        Channel shutter = thing.channels().get(0);
        assertEquals("rollershutter", shutter.channelType());
        assertEquals("upDown=1.008:31/7/1,stopMove=1.010:31/7/2,position=5.001:31/7/3", text(shutter));
        assertEquals(Arrays.asList("rollershutter", null, "number"),
                shutter.split().stream().map(Channel::channelType).toList());
        assertFalse(shutter.split().get(1).selected());
    }

    @Test
    void aRollershutterWithOneStopTooManyIsNotCombined() {
        Thing thing = only(propose(commands("CH-1", "1.008", "1.010", "1.010", "5.001"), functions("F-1")));

        assertTrue(thing.channels().stream().noneMatch(Channel::combined), thing.toString());
    }

    @Test
    void aStepStopAddressNextToARollershutterStaysApartAndUnselected() {
        Thing thing = only(propose(commands("CH-1", "1.008", "1.007", "5.001"), functions("F-1")));

        assertEquals(List.of("upDown=1.008:31/7/1,position=5.001:31/7/3", "ga=1.007:31/7/2"), texts(thing));
        Channel step = thing.channels().get(1);
        assertFalse(step.selected());
        assertEquals("switch", step.channelType());
        assertTrue(step.notes().contains(KnxProposalBuilder.NOTE_STEP_STOP), step.toString());
    }

    @Test
    void aStepStopAddressWithoutRollershutterIsASwitch() {
        Thing thing = only(propose(commands("CH-1", "1.007", "5.001"), functions("F-1")));

        assertTrue(thing.channels().get(0).selected());
        assertFalse(thing.channels().get(0).notes().contains(KnxProposalBuilder.NOTE_STEP_STOP));
    }

    @Test
    void aColorChannelWithItsSplit() {
        Thing thing = only(propose(commands("CH-1", "232.600", "1.001", "5.001", "3.007"), functions("F-1")));

        assertEquals(1, thing.channels().size(), thing.toString());
        Channel color = thing.channels().get(0);
        assertEquals("color", color.channelType());
        assertEquals(List.of("color"), color.allowedChannelTypes());
        assertEquals("hsb=232.600:31/7/1,switch=1.001:31/7/2,position=5.001:31/7/3,increaseDecrease=3.007:31/7/4",
                text(color));
        assertEquals("Part 1", color.label());
        assertEquals(Arrays.asList("color", "switch", "number", "dimmer"),
                color.split().stream().map(Channel::channelType).toList());
    }

    @Test
    void aColorChannelWithOneSwitchTooManyIsNotCombined() {
        Thing thing = only(propose(commands("CH-1", "232.600", "1.001", "5.001", "1.001"), functions("F-1")));

        assertTrue(thing.channels().stream().noneMatch(Channel::combined), thing.toString());
    }

    @Test
    void aDeviceCombinesOnlyWithinOneDeviceChannel() {
        KnxProject withChannel = commands("CH-1", "1.001", "5.001");
        KnxProject withoutChannel = commands(null, "1.001", "5.001");

        assertEquals(List.of("switch=1.001:31/7/1,position=5.001:31/7/2"),
                texts(only(propose(withChannel, devices("P-1_DI-1")))));
        assertEquals(List.of("ga=1.001:31/7/1", "ga=5.001:31/7/2"),
                texts(only(propose(withoutChannel, devices("P-1_DI-1")))));
    }

    @Test
    void aDeviceCombinesEachOfItsChannels() {
        KnxProject project = projectOf(
                List.of(address(1, ga(1), "1.001"), address(2, ga(2), "5.001"), address(3, ga(3), "1.001"),
                        address(4, ga(4), "5.001")),
                List.of(device("Actuator", null, object("O-1", 1, "A", null, null, 1).flags("C-W--").channel("CH-1"),
                        object("O-2", 2, "B", null, null, 2).flags("C-W--").channel("CH-1"),
                        object("O-3", 3, "C", null, null, 3).flags("C-W--").channel("CH-2"),
                        object("O-4", 4, "D", null, null, 4).flags("C-W--").channel("CH-2"))),
                List.of());

        assertEquals(List.of("switch=1.001:31/7/1,position=5.001:31/7/2", "switch=1.001:31/7/3,position=5.001:31/7/4"),
                texts(only(propose(project, devices("P-1_DI-1")))));
    }

    @Test
    void aDeviceDoesNotCombineAcrossModuleInstances() {
        KnxProject project = projectOf(List.of(address(1, ga(1), "1.001"), address(2, ga(2), "5.001")),
                List.of(device("Actuator", null,
                        object("O-1", 1, "A", null, null, 1).flags("C-W--").channel("CH-1").module("MI-1"),
                        object("O-2", 2, "B", null, null, 2).flags("C-W--").channel("CH-1").module("MI-2"))),
                List.of());

        assertEquals(List.of("ga=1.001:31/7/1", "ga=5.001:31/7/2"), texts(only(propose(project, devices("P-1_DI-1")))));
    }

    @Test
    void looseAddressesNeverCombine() {
        assertEquals(List.of("ga=1.001:31/7/1", "ga=5.001:31/7/2"),
                texts(only(propose(commands("CH-1", "1.001", "5.001"), addresses(ga(1), ga(2))))));
    }
}

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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * Tests how many photovoltaic trackers an installation has and how the channels follow that number.
 *
 * <p>
 * The number of trackers depends on the installation: one, two, three or more. The first two channels are
 * declared in the thing description, the others are created from what the device answers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCTrackersTest {

    private static final ThingUID THING = new ThingUID(new ThingTypeUID("e3dc", "power-plant"), "plant");

    /**
     * The container of the inverter as the device answers it: every direct current value arrives once per
     * tracker in a container of its own.
     *
     * @param reported the number of used strings the device reports, or {@code null} to leave it out
     * @param containers the number of containers the device answers with
     */
    private static RscpValue inverter(@Nullable Integer reported, int containers) {
        List<RscpValue> data = new ArrayList<>();
        data.add(RscpValue.uchar8(RscpTag.PVI_INDEX, 0));
        if (reported != null) {
            data.add(RscpValue.uchar8(RscpTag.PVI_USED_STRING_COUNT, reported));
        }
        for (int tracker = 0; tracker < containers; tracker++) {
            data.add(wrapped(RscpTag.PVI_DC_POWER, tracker, 1000 + tracker));
            data.add(wrapped(RscpTag.PVI_DC_VOLTAGE, tracker, 300 + tracker));
            data.add(wrapped(RscpTag.PVI_DC_CURRENT, tracker, 3 + tracker));
        }
        return RscpValue.container(RscpTag.PVI_DATA, data);
    }

    private static RscpValue wrapped(int tag, int index, double value) {
        return RscpValue.container(tag, List.of(RscpValue.uchar8(RscpTag.PVI_INDEX, index),
                RscpValue.of(RscpTag.PVI_VALUE, RscpType.FLOAT32, (float) value)));
    }

    private static List<Channel> thingChannels() {
        List<Channel> channels = new ArrayList<>();
        for (String id : List.of("tracker-1-power", "tracker-2-power", "ac-frequency")) {
            channels.add(ChannelBuilder.create(new ChannelUID(THING, E3DCBindingConstants.GROUP_INVERTER, id), "Number")
                    .build());
        }
        return channels;
    }

    private static List<String> idsOf(List<Channel> channels) {
        return channels.stream().map(channel -> channel.getUID().getIdWithoutGroup()).toList();
    }

    @Test
    void anInstallationWithOneTrackerHasOne() {
        assertEquals(1, E3DCTrackers.discover(inverter(1, 1)).count());
    }

    @Test
    void anInstallationWithTwoTrackersHasTwo() {
        assertEquals(2, E3DCTrackers.discover(inverter(2, 2)).count());
    }

    @Test
    void anInstallationWithThreeTrackersHasThree() {
        assertEquals(3, E3DCTrackers.discover(inverter(3, 3)).count());
    }

    @Test
    void theContainersAreCountedWhenTheDeviceReportsNoNumber() {
        E3DCTrackers.Discovery missing = E3DCTrackers.discover(inverter(null, 3));
        assertNull(missing.reported());
        assertEquals(3, missing.count());
        // a reported zero says nothing either
        assertEquals(3, E3DCTrackers.discover(inverter(0, 3)).count());
    }

    /**
     * An S10 has three trackers, but a house may use two of them. The device may still answer with a container
     * for the third, which must not become a channel.
     */
    @Test
    void aTrackerThatIsNotUsedGetsNoChannel() {
        E3DCTrackers.Discovery found = E3DCTrackers.discover(inverter(2, 3));
        assertEquals(2, found.count());
        assertEquals(3, found.power());
    }

    @Test
    void aReportedNumberAboveWhatTheDeviceAnswersIsNotTrusted() {
        assertEquals(2, E3DCTrackers.discover(inverter(3, 2)).count());
    }

    @Test
    void theNumberOfTrackersIsLimited() {
        assertEquals(E3DCTrackers.MAX_TRACKERS, E3DCTrackers.discover(inverter(40, 40)).count());
    }

    @Test
    void nothingIsKnownWithoutAnyHint() {
        E3DCTrackers.Discovery found = E3DCTrackers.discover(inverter(null, 0));
        assertEquals(0, found.count());
        assertNull(E3DCTrackers.reconcile(thingChannels(), THING, found.count()),
                "without a number the channels stay as they are");
    }

    @Test
    void noChannelIsAddedForTwoTrackers() {
        assertNull(E3DCTrackers.reconcile(thingChannels(), THING, 2));
        assertNull(E3DCTrackers.reconcile(thingChannels(), THING, 1), "the fixed channels are never removed");
        assertTrue(E3DCTrackers.specs(2).isEmpty());
    }

    @Test
    void theChannelsOfFurtherTrackersAreAdded() {
        List<Channel> channels = E3DCTrackers.reconcile(thingChannels(), THING, 4);

        assertNotNull(channels);
        List<String> ids = idsOf(channels);
        for (String id : List.of("tracker-3-power", "tracker-3-voltage", "tracker-3-current", "tracker-4-power",
                "tracker-4-voltage", "tracker-4-current")) {
            assertTrue(ids.contains(id), id + " is missing");
        }
        assertFalse(ids.contains("tracker-5-power"));
        assertTrue(ids.contains("ac-frequency"), "the other channels stay");
        assertEquals(9, ids.size());
        Channel power = channels.stream().filter(channel -> channel.getUID().getId().equals("inverter#tracker-3-power"))
                .findFirst().orElseThrow();
        assertEquals("Number:Power", power.getAcceptedItemType());
        assertEquals("Tracker 3 Power", power.getLabel());
        assertEquals("e3dc:inverter-tracker-power", String.valueOf(power.getChannelTypeUID()));
    }

    @Test
    void channelsThatExistAreNotAddedAgain() {
        List<Channel> channels = requireChanged(E3DCTrackers.reconcile(thingChannels(), THING, 3));
        assertNull(E3DCTrackers.reconcile(channels, THING, 3), "a second pass has nothing left to do");
    }

    @Test
    void theChannelsOfTrackersTheDeviceNoLongerHasAreRemoved() {
        List<Channel> four = requireChanged(E3DCTrackers.reconcile(thingChannels(), THING, 4));
        List<Channel> two = requireChanged(E3DCTrackers.reconcile(four, THING, 3));

        List<String> ids = idsOf(two);
        assertTrue(ids.contains("tracker-3-power"));
        assertFalse(ids.contains("tracker-4-power"), "the fourth tracker is gone");
        assertTrue(ids.contains("tracker-1-power") && ids.contains("tracker-2-power"));
    }

    @Test
    void aChannelOfAnotherGroupWithATrackerNameIsLeftAlone() {
        List<Channel> channels = thingChannels();
        channels.add(ChannelBuilder.create(new ChannelUID(THING, "other", "tracker-7-power"), "Number").build());

        assertNull(E3DCTrackers.reconcile(channels, THING, 2));
    }

    @Test
    void theTrackerOfAChannelIdIsRead() {
        assertEquals(3, E3DCTrackers.trackerOf("tracker-3-power"));
        assertEquals(12, E3DCTrackers.trackerOf("tracker-12-current"));
        assertEquals(0, E3DCTrackers.trackerOf("string-count"));
        assertEquals(0, E3DCTrackers.trackerOf("tracker-x-power"));
        assertEquals(0, E3DCTrackers.trackerOf("tracker-3-other"));
    }

    @Test
    void theValuesOfFurtherTrackersAreReadByPosition() {
        RscpValue response = RscpValue.container(0, List.of(inverter(3, 3)));
        Map<String, E3DCChannelSpec> specs = E3DCTrackers.specs(3);

        assertEquals(3, specs.size());
        for (Map.Entry<String, Integer> expected : Map
                .of("tracker-3-power", 1002, "tracker-3-voltage", 302, "tracker-3-current", 5).entrySet()) {
            E3DCChannelSpec spec = specs.get(expected.getKey());
            assertNotNull(spec, expected.getKey() + " has no spec");
            RscpValue value = E3DCThingHandler.findValue(response, spec, null);
            assertNotNull(value, expected.getKey() + " finds no value");
            Number number = value.asNumber();
            assertNotNull(number);
            assertEquals(expected.getValue().doubleValue(), number.doubleValue(), 0.001);
        }
    }

    @Test
    void aTrackerTheDeviceDoesNotAnswerStaysEmpty() {
        RscpValue response = RscpValue.container(0, List.of(inverter(3, 2)));
        E3DCChannelSpec spec = E3DCTrackers.specs(3).get("tracker-3-power");
        assertNotNull(spec);
        assertNull(E3DCThingHandler.findValue(response, spec, null));
    }

    @Test
    void theLogShowsTheIndexOfEveryContainer() {
        String text = E3DCTrackers.describeValues(inverter(3, 3));

        assertTrue(text.contains("#1 power 1000.0 (index 0)"), text);
        assertTrue(text.contains("#3 power 1002.0 (index 2)"), text);
        assertEquals("no DC containers", E3DCTrackers.describeValues(inverter(null, 0)));
    }

    /**
     * The channel types of the added channels are declared in a file of their own, because no channel group
     * references them and the check of {@link E3DCChannelTypesTest} requires every type to be used by a group.
     */
    @Test
    void theChannelTypesOfFurtherTrackersAreDeclared() throws IOException {
        String xml = Files
                .readString(Path.of("src", "main", "resources", "OH-INF", "thing", "inverter-tracker-types.xml"));
        for (Map.Entry<String, String> type : Map
                .of("inverter-tracker-power", "Number:Power", "inverter-tracker-voltage", "Number:ElectricPotential",
                        "inverter-tracker-current", "Number:ElectricCurrent")
                .entrySet()) {
            int start = xml.indexOf("<channel-type id=\"" + type.getKey() + "\"");
            assertTrue(start >= 0, type.getKey() + " is not declared");
            String body = xml.substring(start, xml.indexOf("</channel-type>", start));
            assertTrue(body.contains("<item-type>" + type.getValue() + "</item-type>"), type.getKey());
            assertTrue(body.contains("<label>") && body.contains("<description>"), type.getKey());
            assertTrue(body.contains("%unit%") && body.contains("readOnly=\"true\""), type.getKey());
        }
    }

    private static <T> T requireChanged(@Nullable T value) {
        assertNotNull(value);
        return Objects.requireNonNull(value);
    }
}

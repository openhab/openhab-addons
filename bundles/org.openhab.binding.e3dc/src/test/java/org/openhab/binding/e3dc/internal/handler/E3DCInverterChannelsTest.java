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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCInverterChannels;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Tests how the channels of the photovoltaic trackers and temperature sensors follow what the inverter answers.
 *
 * <p>
 * The inverter is asked for the indexes 0 to 5 and creates channels for the ones that answer with a value, even
 * when an index in between is missing.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCInverterChannelsTest {

    private static final ThingUID THING = new ThingUID(new ThingTypeUID("e3dc", "power-plant"), "plant");

    /**
     * The container of the inverter as the device answers it: every value arrives in a container of its own that
     * carries the index of the request.
     *
     * @param trackers the indexes of the trackers that answer
     * @param sensors the indexes of the temperature sensors that answer
     */
    private static RscpValue inverter(Set<Integer> trackers, Set<Integer> sensors) {
        List<RscpValue> data = new ArrayList<>();
        data.add(RscpValue.uchar8(RscpTag.PVI_INDEX, 0));
        data.add(RscpValue.uchar8(RscpTag.PVI_USED_STRING_COUNT, 1));
        for (int tracker : trackers) {
            data.add(wrapped(RscpTag.PVI_DC_POWER, tracker, 1000 + tracker));
            data.add(wrapped(RscpTag.PVI_DC_VOLTAGE, tracker, 300 + tracker));
            data.add(wrapped(RscpTag.PVI_DC_CURRENT, tracker, 3 + tracker));
        }
        for (int sensor : sensors) {
            data.add(wrapped(RscpTag.PVI_TEMPERATURE, sensor, 30 + sensor));
        }
        return RscpValue.container(RscpTag.PVI_DATA, data);
    }

    private static RscpValue wrapped(int tag, int index, double value) {
        return RscpValue.container(tag, List.of(RscpValue.uchar8(RscpTag.PVI_INDEX, index),
                RscpValue.of(RscpTag.PVI_VALUE, RscpType.FLOAT32, (float) value)));
    }

    private static List<Channel> thingChannels() {
        List<Channel> channels = new ArrayList<>();
        channels.add(ChannelBuilder
                .create(new ChannelUID(THING, E3DCBindingConstants.GROUP_INVERTER, "ac-frequency"), "Number").build());
        return channels;
    }

    private static List<String> idsOf(List<Channel> channels) {
        return channels.stream().map(channel -> channel.getUID().getIdWithoutGroup()).toList();
    }

    private static E3DCInverterChannels.Found found(Set<Integer> trackers, Set<Integer> sensors) {
        return new E3DCInverterChannels.Found(new TreeSet<>(trackers), new TreeSet<>(sensors));
    }

    @Test
    void everyIndexThatAnswersIsFound() {
        E3DCInverterChannels.Found found = E3DCInverterChannels.discover(inverter(Set.of(0, 1, 2), Set.of(0, 1)));

        assertEquals(Set.of(0, 1, 2), found.trackers());
        assertEquals(Set.of(0, 1), found.sensors());
    }

    /** The device reports one used string although two deliver values, so that number must not limit anything. */
    @Test
    void theReportedStringCountDoesNotLimitTheTrackers() {
        assertEquals(2, E3DCInverterChannels.discover(inverter(Set.of(0, 1), Set.of())).trackers().size());
    }

    /** An index in between that fails to answer must not hide the ones behind it. */
    @Test
    void anIndexInBetweenThatDoesNotAnswerIsSkipped() {
        E3DCInverterChannels.Found found = E3DCInverterChannels.discover(inverter(Set.of(0, 2), Set.of(1, 3)));

        assertEquals(Set.of(0, 2), found.trackers());
        assertEquals(Set.of(1, 3), found.sensors());
    }

    @Test
    void anIndexBeyondTheProbedOnesIsIgnored() {
        E3DCInverterChannels.Found found = E3DCInverterChannels.discover(inverter(Set.of(0, 9), Set.of(7)));

        assertEquals(Set.of(0), found.trackers());
        assertTrue(found.sensors().isEmpty());
    }

    @Test
    void aContainerWithoutValueOrIndexIsNoAnswer() {
        RscpValue inverter = RscpValue.container(RscpTag.PVI_DATA,
                List.of(RscpValue.container(RscpTag.PVI_DC_POWER, List.of(RscpValue.uchar8(RscpTag.PVI_INDEX, 1))),
                        RscpValue.container(RscpTag.PVI_DC_VOLTAGE,
                                List.of(RscpValue.of(RscpTag.PVI_VALUE, RscpType.FLOAT32, 1.0f)))));

        assertTrue(E3DCInverterChannels.discover(inverter).isEmpty());
    }

    @Test
    void nothingIsAddedWithoutAnAnswer() {
        assertNull(E3DCInverterChannels.reconcile(thingChannels(), THING, found(Set.of(), Set.of())));
    }

    @Test
    void theChannelsOfEveryFoundTrackerAndSensorAreAdded() {
        List<Channel> channels = E3DCInverterChannels.reconcile(thingChannels(), THING,
                found(Set.of(0, 1, 2), Set.of(0, 1)));

        assertNotNull(channels);
        List<String> ids = idsOf(channels);
        for (String id : List.of("tracker-1-power", "tracker-1-voltage", "tracker-1-current", "tracker-2-power",
                "tracker-3-power", "tracker-3-voltage", "tracker-3-current", "temperature-1", "temperature-2")) {
            assertTrue(ids.contains(id), id + " is missing");
        }
        assertFalse(ids.contains("tracker-4-power"));
        assertFalse(ids.contains("temperature-3"));
        assertTrue(ids.contains("ac-frequency"), "the other channels stay");
        assertEquals(1 + 9 + 2, ids.size());

        Channel power = channels.stream().filter(channel -> channel.getUID().getId().equals("inverter#tracker-3-power"))
                .findFirst().orElseThrow();
        assertEquals("Number:Power", power.getAcceptedItemType());
        assertEquals("Tracker 3 Power", power.getLabel());
        assertEquals("e3dc:inverter-tracker-power", String.valueOf(power.getChannelTypeUID()));
        Channel temperature = channels.stream()
                .filter(channel -> channel.getUID().getId().equals("inverter#temperature-2")).findFirst().orElseThrow();
        assertEquals("Number:Temperature", temperature.getAcceptedItemType());
        assertEquals("Temperature 2", temperature.getLabel());
        assertEquals("e3dc:inverter-temperature", String.valueOf(temperature.getChannelTypeUID()));
    }

    @Test
    void channelsThatExistAreNotAddedAgain() {
        E3DCInverterChannels.Found found = found(Set.of(0, 1), Set.of(0));
        List<Channel> channels = requireChanged(E3DCInverterChannels.reconcile(thingChannels(), THING, found));

        assertNull(E3DCInverterChannels.reconcile(channels, THING, found), "a second pass has nothing left to do");
    }

    @Test
    void aTrackerThatNoLongerAnswersKeepsItsChannels() {
        List<Channel> channels = requireChanged(
                E3DCInverterChannels.reconcile(thingChannels(), THING, found(Set.of(0, 1, 2), Set.of())));

        assertNull(E3DCInverterChannels.reconcile(channels, THING, found(Set.of(0, 1), Set.of())),
                "channels are never removed");
    }

    /** A thing created by an earlier version has channels of types that are gone, which must keep their links. */
    @Test
    void channelsOfAnEarlierVersionAreConvertedAndKeepTheirId() {
        List<Channel> old = thingChannels();
        old.add(ChannelBuilder
                .create(new ChannelUID(THING, E3DCBindingConstants.GROUP_INVERTER, "tracker-1-power"), "Number:Power")
                .withType(new ChannelTypeUID("e3dc", "inverter-tracker-1-power")).build());
        old.add(ChannelBuilder.create(new ChannelUID(THING, E3DCBindingConstants.GROUP_INVERTER, "temperature-2"),
                "Number:Temperature").withType(new ChannelTypeUID("e3dc", "inverter-temperature-2")).build());

        List<Channel> channels = requireChanged(
                E3DCInverterChannels.reconcile(old, THING, found(Set.of(0), Set.of(1))));

        Channel tracker = channels.stream()
                .filter(channel -> channel.getUID().getId().equals("inverter#tracker-1-power")).findFirst()
                .orElseThrow();
        assertEquals("e3dc:inverter-tracker-power", String.valueOf(tracker.getChannelTypeUID()));
        Channel sensor = channels.stream().filter(channel -> channel.getUID().getId().equals("inverter#temperature-2"))
                .findFirst().orElseThrow();
        assertEquals("e3dc:inverter-temperature", String.valueOf(sensor.getChannelTypeUID()));
        assertEquals(1, channels.stream().filter(channel -> channel.getUID().getId().equals("inverter#tracker-1-power"))
                .count(), "no duplicate");
    }

    @Test
    void aChannelOfAnotherGroupWithATrackerNameIsLeftAlone() {
        List<Channel> channels = thingChannels();
        channels.add(ChannelBuilder.create(new ChannelUID(THING, "other", "tracker-7-power"), "Number").build());

        assertNull(E3DCInverterChannels.reconcile(channels, THING, found(Set.of(), Set.of())));
    }

    @Test
    void theNumberOfAChannelIdIsRead() {
        assertEquals(3, E3DCInverterChannels.trackerOf("tracker-3-power"));
        assertEquals(12, E3DCInverterChannels.trackerOf("tracker-12-current"));
        assertEquals(0, E3DCInverterChannels.trackerOf("string-count"));
        assertEquals(0, E3DCInverterChannels.trackerOf("tracker-x-power"));
        assertEquals(0, E3DCInverterChannels.trackerOf("tracker-3-other"));
        assertEquals(2, E3DCInverterChannels.sensorOf("temperature-2"));
        assertEquals(0, E3DCInverterChannels.sensorOf("temperature-x"));
        assertEquals(0, E3DCInverterChannels.sensorOf("tracker-1-power"));
    }

    /** The values are read by the index the container carries, so a gap in between does not shift them. */
    @Test
    void theValuesAreReadByTheIndexOfTheContainer() {
        RscpValue response = RscpValue.container(0, List.of(inverter(Set.of(0, 2), Set.of(1))));
        Map<String, E3DCChannelSpec> specs = E3DCInverterChannels.specs(found(Set.of(0, 2), Set.of(1)));

        assertEquals(7, specs.size());
        Map<String, Double> expected = Map.of("tracker-1-power", 1000.0, "tracker-3-power", 1002.0, "tracker-3-voltage",
                302.0, "tracker-3-current", 5.0, "temperature-2", 31.0);
        for (Map.Entry<String, Double> entry : expected.entrySet()) {
            E3DCChannelSpec spec = specs.get(entry.getKey());
            assertNotNull(spec, entry.getKey() + " has no spec");
            RscpValue value = E3DCThingHandler.findValue(response, spec, null);
            assertNotNull(value, entry.getKey() + " finds no value");
            Number number = value.asNumber();
            assertNotNull(number);
            assertEquals(entry.getValue(), number.doubleValue(), 0.001, entry.getKey());
        }
    }

    @Test
    void aTrackerTheDeviceDoesNotAnswerStaysEmpty() {
        RscpValue response = RscpValue.container(0, List.of(inverter(Set.of(0, 1), Set.of())));
        E3DCChannelSpec spec = E3DCInverterChannels.specs(found(Set.of(2), Set.of())).get("tracker-3-power");

        assertNotNull(spec);
        assertNull(E3DCThingHandler.findValue(response, spec, null), "another tracker must not fill the channel");
    }

    @Test
    void theLogShowsTheValuesOfEveryIndex() {
        String text = E3DCInverterChannels.describeValues(inverter(Set.of(0, 2), Set.of(1)));

        assertTrue(text.contains("#1 power 1000.0"), text);
        assertTrue(text.contains("#3 power 1002.0"), text);
        assertTrue(text.contains("temperature #2 31.0"), text);
        assertEquals("no DC or temperature containers",
                E3DCInverterChannels.describeValues(inverter(Set.of(), Set.of())));
    }

    /**
     * The channel types of the added channels are declared in a file of their own, because no channel group
     * references them and the check of {@link E3DCChannelTypesTest} requires every type to be used by a group.
     */
    @Test
    void theChannelTypesOfTheAddedChannelsAreDeclared() throws IOException {
        String xml = Files
                .readString(Path.of("src", "main", "resources", "OH-INF", "thing", "inverter-indexed-types.xml"));
        for (Map.Entry<String, String> type : Map.of("inverter-tracker-power", "Number:Power",
                "inverter-tracker-voltage", "Number:ElectricPotential", "inverter-tracker-current",
                "Number:ElectricCurrent", "inverter-temperature", "Number:Temperature").entrySet()) {
            int start = xml.indexOf("<channel-type id=\"" + type.getKey() + "\"");
            assertTrue(start >= 0, type.getKey() + " is not declared");
            String body = xml.substring(start, xml.indexOf("</channel-type>", start));
            assertTrue(body.contains("<item-type>" + type.getValue() + "</item-type>"), type.getKey());
            assertTrue(body.contains("<label>") && body.contains("<description>"), type.getKey());
            assertTrue(body.contains("%unit%") && body.contains("readOnly=\"true\""), type.getKey());
        }
    }

    /** The inverter channels of the thing description no longer carry trackers or sensors. */
    @Test
    void theRegistryHasNoTrackerOrTemperatureChannels() {
        Map<String, E3DCChannelSpec> inverter = E3DCChannelSpec.powerPlant().groups()
                .get(E3DCBindingConstants.GROUP_INVERTER);

        assertNotNull(inverter);
        assertTrue(inverter.keySet().stream()
                .noneMatch(id -> E3DCInverterChannels.trackerOf(id) > 0 || E3DCInverterChannels.sensorOf(id) > 0));
    }

    private static <T> T requireChanged(@Nullable T value) {
        assertNotNull(value);
        return Objects.requireNonNull(value);
    }
}

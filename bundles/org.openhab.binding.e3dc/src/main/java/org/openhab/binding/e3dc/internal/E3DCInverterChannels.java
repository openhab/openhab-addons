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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The photovoltaic trackers and temperature sensors of the inverter, whose number depends on the installation.
 *
 * <p>
 * The device reports the number of strings it uses wrongly, so the binding asks for the indexes 0 to 5 of
 * both on every poll, see {@link E3DCContainerRequests#INDEX_PROBES}. An index the inverter does not have is
 * answered with an error. For every index that answers with a value the thing gets its channels: power,
 * voltage and current for a tracker, the temperature for a sensor. Channels are only added, never removed,
 * so an item that is linked to a channel keeps its link when the device fails to answer once.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCInverterChannels {

    public static final String POWER = "power";
    public static final String VOLTAGE = "voltage";
    public static final String CURRENT = "current";

    private static final List<String> KINDS = List.of(POWER, VOLTAGE, CURRENT);

    private static final String TRACKER_PREFIX = "tracker-";
    private static final String TEMPERATURE_PREFIX = "temperature-";

    /** The channel type every added temperature channel has. */
    public static final String TEMPERATURE_TYPE = "inverter-temperature";

    /**
     * The indexes the inverter answered with a value.
     *
     * @param trackers the indexes of the trackers, counted from 0
     * @param sensors the indexes of the temperature sensors, counted from 0
     */
    public record Found(SortedSet<Integer> trackers, SortedSet<Integer> sensors) {

        /** Whether the inverter answered nothing for any tracker or sensor. */
        public boolean isEmpty() {
            return trackers.isEmpty() && sensors.isEmpty();
        }

        /** The facts in one line, which is what a log needs to tell how the channels came about. */
        public String describe() {
            return "trackers " + display(trackers) + ", temperature sensors " + display(sensors);
        }

        private static String display(Set<Integer> indexes) {
            return indexes.isEmpty() ? "none" : indexes.stream().map(index -> "#" + (index + 1)).toList().toString();
        }
    }

    private E3DCInverterChannels() {
    }

    /**
     * Reads which trackers and temperature sensors the inverter answered with a value.
     *
     * <p>
     * Every value arrives in a container of its own, which carries the index the request asked for next to the
     * value. A container without index or without value says nothing and is skipped.
     *
     * @param inverter the container with the inverter data
     */
    public static Found discover(RscpValue inverter) {
        SortedSet<Integer> trackers = new TreeSet<>();
        for (int tag : List.of(RscpTag.PVI_DC_POWER, RscpTag.PVI_DC_VOLTAGE, RscpTag.PVI_DC_CURRENT)) {
            collect(inverter, tag, trackers);
        }
        SortedSet<Integer> sensors = new TreeSet<>();
        collect(inverter, RscpTag.PVI_TEMPERATURE, sensors);
        return new Found(trackers, sensors);
    }

    private static void collect(RscpValue inverter, int tag, Set<Integer> indexes) {
        for (RscpValue container : inverter.findAll(tag)) {
            Number index = container.findNumber(RscpTag.PVI_INDEX);
            if (index != null && container.find(RscpTag.PVI_VALUE) != null && index.intValue() >= 0
                    && index.intValue() < E3DCContainerRequests.INDEX_PROBES) {
                indexes.add(index.intValue());
            }
        }
    }

    /** The channel id of one value of a tracker, counted from 1. */
    public static String trackerChannelId(int tracker, String kind) {
        return TRACKER_PREFIX + tracker + "-" + kind;
    }

    /** The channel id of a temperature sensor, counted from 1. */
    public static String temperatureChannelId(int sensor) {
        return TEMPERATURE_PREFIX + sensor;
    }

    /** The number of the tracker a channel id belongs to, or 0 when it is no tracker channel. */
    public static int trackerOf(String channelId) {
        for (String kind : KINDS) {
            String suffix = "-" + kind;
            if (channelId.startsWith(TRACKER_PREFIX) && channelId.endsWith(suffix)) {
                return number(channelId.substring(TRACKER_PREFIX.length(), channelId.length() - suffix.length()));
            }
        }
        return 0;
    }

    /** The number of the temperature sensor a channel id belongs to, or 0 when it is no temperature channel. */
    public static int sensorOf(String channelId) {
        return channelId.startsWith(TEMPERATURE_PREFIX) ? number(channelId.substring(TEMPERATURE_PREFIX.length())) : 0;
    }

    private static int number(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * The value channels of the trackers and sensors that were found, which are read from the answer by the
     * index the container carries.
     *
     * @param found the trackers and sensors the inverter answered
     * @return the channels by id
     */
    public static Map<String, E3DCChannelSpec> specs(Found found) {
        Map<String, E3DCChannelSpec> specs = new LinkedHashMap<>();
        for (int tracker : found.trackers()) {
            specs.put(trackerChannelId(tracker + 1, POWER),
                    E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA, RscpTag.PVI_DC_POWER, RscpTag.PVI_VALUE, tracker));
            specs.put(trackerChannelId(tracker + 1, VOLTAGE), E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA,
                    RscpTag.PVI_DC_VOLTAGE, RscpTag.PVI_VALUE, tracker));
            specs.put(trackerChannelId(tracker + 1, CURRENT), E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA,
                    RscpTag.PVI_DC_CURRENT, RscpTag.PVI_VALUE, tracker));
        }
        for (int sensor : found.sensors()) {
            specs.put(temperatureChannelId(sensor + 1), E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA,
                    RscpTag.PVI_TEMPERATURE, RscpTag.PVI_VALUE, sensor));
        }
        return specs;
    }

    /**
     * The channels of a thing once the trackers and sensors that were found are added.
     *
     * <p>
     * Missing channels are created and nothing is removed. A channel of a tracker or sensor that an earlier
     * version declared with a type of its own is rebuilt with the type used now, under the same id, so the
     * links of the items stay. Everything else stays as it is.
     *
     * @param existing the channels the thing has now
     * @param thing the thing the channels belong to
     * @param found the trackers and sensors the inverter answered
     * @return the new list of channels, or {@code null} when nothing has to change
     */
    public static @Nullable List<Channel> reconcile(List<Channel> existing, ThingUID thing, Found found) {
        List<Channel> channels = new ArrayList<>();
        boolean changed = false;
        for (Channel channel : existing) {
            Channel rebuilt = null;
            if (E3DCBindingConstants.GROUP_INVERTER.equals(channel.getUID().getGroupId())) {
                String id = channel.getUID().getIdWithoutGroup();
                int tracker = trackerOf(id);
                int sensor = sensorOf(id);
                if (tracker > 0 && !isCurrentType(channel, "inverter-tracker-" + kindOf(id))) {
                    rebuilt = tracker(channel.getUID(), tracker, kindOf(id));
                } else if (sensor > 0 && !isCurrentType(channel, TEMPERATURE_TYPE)) {
                    rebuilt = temperature(channel.getUID(), sensor);
                }
            }
            channels.add(rebuilt != null ? rebuilt : channel);
            changed |= rebuilt != null;
        }
        for (int tracker : found.trackers()) {
            for (String kind : KINDS) {
                ChannelUID uid = uid(thing, trackerChannelId(tracker + 1, kind));
                if (!exists(channels, uid)) {
                    channels.add(tracker(uid, tracker + 1, kind));
                    changed = true;
                }
            }
        }
        for (int sensor : found.sensors()) {
            ChannelUID uid = uid(thing, temperatureChannelId(sensor + 1));
            if (!exists(channels, uid)) {
                channels.add(temperature(uid, sensor + 1));
                changed = true;
            }
        }
        return changed ? channels : null;
    }

    private static ChannelUID uid(ThingUID thing, String channelId) {
        return new ChannelUID(thing, E3DCBindingConstants.GROUP_INVERTER, channelId);
    }

    private static boolean exists(List<Channel> channels, ChannelUID uid) {
        return channels.stream().anyMatch(channel -> channel.getUID().equals(uid));
    }

    private static boolean isCurrentType(Channel channel, String typeId) {
        ChannelTypeUID type = channel.getChannelTypeUID();
        return type != null && typeId.equals(type.getId());
    }

    /** The kind of value a tracker channel id ends with. */
    private static String kindOf(String channelId) {
        for (String kind : KINDS) {
            if (channelId.endsWith("-" + kind)) {
                return kind;
            }
        }
        return POWER;
    }

    private static Channel tracker(ChannelUID uid, int tracker, String kind) {
        String itemType;
        String label;
        String description;
        switch (kind) {
            case POWER -> {
                itemType = "Number:Power";
                label = "Tracker " + tracker + " Power";
                description = "Direct current power of photovoltaic tracker " + tracker;
            }
            case VOLTAGE -> {
                itemType = "Number:ElectricPotential";
                label = "Tracker " + tracker + " Voltage";
                description = "Direct current voltage of photovoltaic tracker " + tracker;
            }
            default -> {
                itemType = "Number:ElectricCurrent";
                label = "Tracker " + tracker + " Current";
                description = "Direct current of photovoltaic tracker " + tracker;
            }
        }
        return ChannelBuilder.create(uid, itemType)
                .withType(new ChannelTypeUID(E3DCBindingConstants.BINDING_ID, "inverter-tracker-" + kind))
                .withLabel(label).withDescription(description).build();
    }

    private static Channel temperature(ChannelUID uid, int sensor) {
        return ChannelBuilder.create(uid, "Number:Temperature")
                .withType(new ChannelTypeUID(E3DCBindingConstants.BINDING_ID, TEMPERATURE_TYPE))
                .withLabel("Temperature " + sensor)
                .withDescription("Temperature of sensor " + sensor + " of the inverter").build();
    }

    /**
     * The values of every tracker and sensor in the answer, one line per index.
     *
     * @param inverter the container with the inverter data
     */
    public static String describeValues(RscpValue inverter) {
        Found found = discover(inverter);
        if (found.isEmpty()) {
            return "no DC or temperature containers";
        }
        StringBuilder text = new StringBuilder();
        for (int tracker : found.trackers()) {
            text.append("#").append(tracker + 1).append(" power ")
                    .append(valueOf(inverter, RscpTag.PVI_DC_POWER, tracker)).append(" voltage ")
                    .append(valueOf(inverter, RscpTag.PVI_DC_VOLTAGE, tracker)).append(" current ")
                    .append(valueOf(inverter, RscpTag.PVI_DC_CURRENT, tracker)).append("; ");
        }
        for (int sensor : found.sensors()) {
            text.append("temperature #").append(sensor + 1).append(" ")
                    .append(valueOf(inverter, RscpTag.PVI_TEMPERATURE, sensor)).append("; ");
        }
        return text.toString();
    }

    private static String valueOf(RscpValue inverter, int tag, int index) {
        for (RscpValue container : inverter.findAll(tag)) {
            Number reported = container.findNumber(RscpTag.PVI_INDEX);
            if (reported != null && reported.intValue() == index) {
                Number value = container.findNumber(RscpTag.PVI_VALUE);
                return value != null ? value.toString() : "?";
            }
        }
        return "-";
    }
}

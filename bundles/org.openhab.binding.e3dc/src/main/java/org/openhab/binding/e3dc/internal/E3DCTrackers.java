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
 * The photovoltaic trackers of the inverter, whose number depends on the installation.
 *
 * <p>
 * The first {@link #FIXED_TRACKERS} trackers are declared in the thing description. Every further tracker
 * is added as a channel at runtime once the device has reported how many it has, and removed again when
 * the device reports fewer. The number is taken from {@code PVI_USED_STRING_COUNT}; when the device does
 * not report it, the containers the device answers with are counted instead.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
final class E3DCTrackers {

    /** The trackers declared in the thing description, which exist for every installation. */
    static final int FIXED_TRACKERS = 2;

    /** The upper limit for the trackers, which keeps a faulty answer from creating an endless list. */
    static final int MAX_TRACKERS = 8;

    static final String POWER = "power";
    static final String VOLTAGE = "voltage";
    static final String CURRENT = "current";

    private static final List<String> KINDS = List.of(POWER, VOLTAGE, CURRENT);

    /**
     * What the answer of the inverter says about its trackers.
     *
     * @param reported the number the device reports as used strings, or {@code null} when it reports none
     * @param power the number of containers with the direct current power
     * @param voltage the number of containers with the direct current voltage
     * @param current the number of containers with the direct current
     * @param count the number of trackers the channels are created for, or 0 when nothing is known
     */
    record Discovery(@Nullable Integer reported, int power, int voltage, int current, int count) {

        /** The facts in one line, which is what a log needs to tell how the number came about. */
        String describe() {
            return "string-count " + (reported != null ? reported : "not reported") + ", DC containers power " + power
                    + ", voltage " + voltage + ", current " + current + " -> " + count + " trackers";
        }
    }

    private E3DCTrackers() {
    }

    /**
     * Reads how many trackers the device has from the answer of the inverter.
     *
     * <p>
     * The reported number wins, because a device with a tracker it does not use may still answer with a
     * container for it. It is never larger than the containers actually answered, since a tracker without a
     * container would only be an empty channel.
     *
     * @param inverter the container with the inverter data
     */
    static Discovery discover(RscpValue inverter) {
        Number used = inverter.findNumber(RscpTag.PVI_USED_STRING_COUNT);
        Integer reported = used != null ? Integer.valueOf(used.intValue()) : null;
        int power = inverter.findAll(RscpTag.PVI_DC_POWER).size();
        int voltage = inverter.findAll(RscpTag.PVI_DC_VOLTAGE).size();
        int current = inverter.findAll(RscpTag.PVI_DC_CURRENT).size();
        int containers = Math.max(power, Math.max(voltage, current));
        int count = reported != null && reported > 0 ? reported : containers;
        if (containers > 0) {
            count = Math.min(count, containers);
        }
        return new Discovery(reported, power, voltage, current, Math.max(0, Math.min(count, MAX_TRACKERS)));
    }

    /** The channel id of one value of a tracker, counted from 1. */
    static String channelId(int tracker, String kind) {
        return "tracker-" + tracker + "-" + kind;
    }

    /**
     * The number of the tracker a channel id belongs to, or 0 when it is no tracker channel.
     */
    static int trackerOf(String channelId) {
        for (String kind : KINDS) {
            String suffix = "-" + kind;
            if (channelId.startsWith("tracker-") && channelId.endsWith(suffix)) {
                String number = channelId.substring("tracker-".length(), channelId.length() - suffix.length());
                try {
                    return Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    /**
     * The value channels of the trackers beyond the fixed ones, which are read from the answer by the
     * position of the tracker.
     *
     * @param count the number of trackers of the installation
     * @return the channels by id, empty when the installation has no more than the fixed trackers
     */
    static Map<String, E3DCChannelSpec> specs(int count) {
        Map<String, E3DCChannelSpec> specs = new LinkedHashMap<>();
        for (int tracker = FIXED_TRACKERS + 1; tracker <= Math.min(count, MAX_TRACKERS); tracker++) {
            specs.putAll(E3DCChannelSpec.trackerChannels(tracker));
        }
        return specs;
    }

    /**
     * The channels of a thing once the trackers are adapted to the number the device has.
     *
     * <p>
     * Channels of trackers beyond the number are dropped and missing ones are created. Everything else stays
     * as it is, so the links of the items to the other channels are not touched.
     *
     * @param existing the channels the thing has now
     * @param thing the thing the channels belong to
     * @param count the number of trackers of the installation
     * @return the new list of channels, or {@code null} when nothing has to change
     */
    static @Nullable List<Channel> reconcile(List<Channel> existing, ThingUID thing, int count) {
        List<Channel> channels = new ArrayList<>();
        boolean changed = false;
        for (Channel channel : existing) {
            int tracker = E3DCBindingConstants.GROUP_INVERTER.equals(channel.getUID().getGroupId())
                    ? trackerOf(channel.getUID().getIdWithoutGroup())
                    : 0;
            if (tracker > FIXED_TRACKERS && tracker > count) {
                changed = true;
            } else {
                channels.add(channel);
            }
        }
        for (int tracker = FIXED_TRACKERS + 1; tracker <= Math.min(count, MAX_TRACKERS); tracker++) {
            for (String kind : KINDS) {
                ChannelUID uid = new ChannelUID(thing, E3DCBindingConstants.GROUP_INVERTER, channelId(tracker, kind));
                if (existing.stream().noneMatch(channel -> channel.getUID().equals(uid))) {
                    channels.add(channel(uid, tracker, kind));
                    changed = true;
                }
            }
        }
        return changed ? channels : null;
    }

    private static Channel channel(ChannelUID uid, int tracker, String kind) {
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

    /**
     * The values of every tracker in the answer, one line per position.
     *
     * <p>
     * The trackers are told apart by their position only, so the log shows the index tag of each container
     * next to its value. Whether the index repeats the position is what the position based lookup relies on.
     *
     * @param inverter the container with the inverter data
     */
    static String describeValues(RscpValue inverter) {
        List<RscpValue> power = inverter.findAll(RscpTag.PVI_DC_POWER);
        List<RscpValue> voltage = inverter.findAll(RscpTag.PVI_DC_VOLTAGE);
        List<RscpValue> current = inverter.findAll(RscpTag.PVI_DC_CURRENT);
        int positions = Math.max(power.size(), Math.max(voltage.size(), current.size()));
        StringBuilder text = new StringBuilder();
        for (int position = 0; position < positions; position++) {
            if (position > 0) {
                text.append("; ");
            }
            text.append("#").append(position + 1).append(" power ").append(valueAt(power, position)).append(" voltage ")
                    .append(valueAt(voltage, position)).append(" current ").append(valueAt(current, position));
        }
        return positions == 0 ? "no DC containers" : text.toString();
    }

    private static String valueAt(List<RscpValue> containers, int position) {
        if (position >= containers.size()) {
            return "-";
        }
        RscpValue container = containers.get(position);
        Number value = container.findNumber(RscpTag.PVI_VALUE);
        Number index = container.findNumber(RscpTag.PVI_INDEX);
        return (value != null ? value.toString() : "?") + (index != null ? " (index " + index + ")" : "");
    }
}

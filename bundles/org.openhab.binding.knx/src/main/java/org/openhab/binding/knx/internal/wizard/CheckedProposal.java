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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.ChannelKeyTypes;
import org.openhab.binding.knx.internal.project.KnxDatapointType;

import io.calimero.GroupAddress;
import io.calimero.KNXFormatException;

/**
 * What the wizard writes as text and turns into create steps, after every field was checked. Only selected
 * channels are in it, and only Things that have one. The records refuse what the binding or core would refuse
 * or misread: a key outside the channel type, a data type that does not work on its key, an address the binding
 * cannot parse, an id or name outside its pattern, a label that is not cleaned, an Item in a group that is not one
 * of the proposal's location groups, a location group before the group it lies in. Without {@code readAtStart} the
 * model holds no read mark.
 *
 * @param bridgeUID the UID of the bridge, {@code knx:ip:<id>} or {@code knx:serial:<id>}
 * @param locationGroups the location groups, a group after the group it lies in
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record CheckedProposal(String bridgeUID, List<Thing> things, List<LocationGroup> locationGroups,
        boolean readAtStart) {

    private static final Pattern CATEGORY = Pattern.compile("[a-z0-9_][a-z0-9_-]{0,39}");
    public static final Pattern TAG = Pattern.compile("[A-Za-z0-9_ -]{1,40}");
    public static final int MAX_TAGS = 10;
    public static final int MAX_LABEL = 100;

    private static final Pattern BRIDGE_UID = Pattern.compile("knx:(ip|serial):[A-Za-z0-9_-]+");
    private static final Pattern ADDRESS = Pattern.compile("[0-9]{1,5}(/[0-9]{1,4}){0,2}");
    // the data type part the binding's channel text accepts
    private static final Pattern DATA_TYPE = Pattern.compile("[0-9]{1,3}\\.[0-9]{3,5}");

    public CheckedProposal {
        require(BRIDGE_UID.matcher(bridgeUID).matches(), "bridge UID");
        things = readAtStart ? List.copyOf(things) : things.stream().map(Thing::withoutReads).toList();
        locationGroups = List.copyOf(locationGroups);
        String thingPrefix = "knx:device:" + bridgeUID.substring(bridgeUID.lastIndexOf(':') + 1) + ":";
        Set<String> thingUIDs = new HashSet<>();
        Set<String> groupNames = new HashSet<>();
        for (LocationGroup group : locationGroups) {
            String parent = group.parent();
            require(parent == null || groupNames.contains(parent), "parent that is no earlier location group");
            require(groupNames.add(group.name()), "location group name twice");
        }
        Set<String> itemNames = new HashSet<>();
        for (Thing thing : things) {
            require(thing.uid().equals(thingPrefix + thing.id()), "Thing UID");
            require(thingUIDs.add(thing.uid()), "Thing UID twice");
            for (Channel channel : thing.channels()) {
                Item item = channel.item();
                require(!groupNames.contains(item.name()) && itemNames.add(item.name()), "Item name twice");
                require(groupNames.containsAll(item.groups()), "group that is no location group of the proposal");
            }
        }
    }

    /** The last part of the bridge UID. */
    public String bridgeId() {
        return bridgeUID.substring(bridgeUID.lastIndexOf(':') + 1);
    }

    /** Which one address of a part carries the read mark {@code <}. */
    public enum Read {
        NONE,
        MAIN,
        STATUS
    }

    /**
     * A label as the model holds it: line breaks and control characters removed, no white space at the ends, at
     * most {@value #MAX_LABEL} characters.
     */
    public static String cleanLabel(String label) {
        StringBuilder kept = new StringBuilder(label.length());
        label.codePoints().filter(c -> !Character.isISOControl(c) && Character.getType(c) != Character.LINE_SEPARATOR
                && Character.getType(c) != Character.PARAGRAPH_SEPARATOR).forEach(kept::appendCodePoint);
        String cleaned = kept.toString().strip();
        if (cleaned.codePointCount(0, cleaned.length()) > MAX_LABEL) {
            cleaned = cleaned.substring(0, cleaned.offsetByCodePoints(0, MAX_LABEL)).strip();
        }
        return cleaned;
    }

    /**
     * An Item label as the model holds it: {@link #cleanLabel(String)}, and {@code [ ]} as {@code ( )}, since in an
     * {@code .items} file the bracket part of a label is the state pattern.
     */
    public static String cleanItemLabel(String label) {
        return cleanLabel(label).replace('[', '(').replace(']', ')');
    }

    /** Whether the text can stand as the icon of an Item in a {@code .items} file. */
    public static boolean isCategory(String category) {
        return CATEGORY.matcher(category).matches() && !ProposalNames.ITEM_KEYWORDS.contains(category);
    }

    /**
     * A Thing of type {@code knx:device}. It carries no configuration: no individual address, so the add-on never
     * polls the device.
     *
     * @param uid {@code knx:device:<bridge id>:<id>}
     */
    public record Thing(String uid, String id, String label, List<Channel> channels) {

        public Thing {
            channels = List.copyOf(channels);
            require(ProposalNames.isId(id), "Thing id");
            requireLabel(label, cleanLabel(label));
            require(!channels.isEmpty(), "Thing without channel");
            Set<String> ids = new HashSet<>();
            for (Channel channel : channels) {
                require(channel.uid().equals(uid + ":" + channel.id()), "channel UID");
                require(ids.add(channel.id()), "channel id twice");
            }
        }

        Thing withoutReads() {
            return new Thing(uid, id, label, channels.stream().map(Channel::withoutReads).toList());
        }
    }

    /**
     * A channel and the Item linked to it.
     *
     * @param uid the Thing's UID and {@code :<id>}
     * @param channelType a channel type of {@link ChannelKeyTypes}
     * @param parts one per key, each key of the channel type at most once, in the order of the text
     */
    public record Channel(String uid, String id, String label, String channelType, List<Part> parts, Item item) {

        public Channel {
            parts = List.copyOf(parts);
            require(ProposalNames.isId(id), "channel id");
            requireLabel(label, cleanLabel(label));
            List<String> keys = ChannelKeyTypes.keysOf(channelType);
            require(!keys.isEmpty(), "channel type");
            require(!parts.isEmpty(), "channel without part");
            Set<String> used = new HashSet<>();
            for (Part part : parts) {
                require(keys.contains(part.key()) && used.add(part.key()), "key");
                require(ChannelKeyTypes.works(channelType, part.key(), part.dataType()), "data type on its key");
            }
            require(item.channelUID().equals(uid), "Item of another channel");
            require(ProposalItems.allowedTypes(channelType, parts.get(0).dataType()).contains(item.type()),
                    "Item type");
        }

        Channel withoutReads() {
            return new Channel(uid, id, label, channelType, parts.stream().map(Part::withoutRead).toList(), item);
        }
    }

    /**
     * One key of a channel.
     *
     * @param dataType the data type, with a sub number
     * @param groupAddress the main address, written as the project writes it
     * @param statusAddress the status address, {@code null} when none
     */
    public record Part(String key, KnxDatapointType dataType, String groupAddress, @Nullable String statusAddress,
            Read read) {

        public Part {
            require(dataType.sub() != null && DATA_TYPE.matcher(dataType.toString()).matches(), "data type");
            requireAddress(groupAddress);
            if (statusAddress != null) {
                requireAddress(statusAddress);
            }
            require(read != Read.STATUS || statusAddress != null, "read mark without status address");
        }

        /** The value of the key as the binding reads it, {@code 1.001:31/7/1+<31/7/2}; the only such text. */
        public String text() {
            StringBuilder text = new StringBuilder();
            text.append(dataType).append(':');
            text.append(read == Read.MAIN ? "<" : "").append(groupAddress);
            String status = statusAddress;
            if (status != null) {
                text.append('+').append(read == Read.STATUS ? "<" : "").append(status);
            }
            return text.toString();
        }

        Part withoutRead() {
            return new Part(key, dataType, groupAddress, statusAddress, Read.NONE);
        }
    }

    /**
     * The Item of a channel.
     *
     * @param type one of the channel's allowed Item types
     * @param category the icon, {@code null} when none; {@code ""} is taken as none
     * @param groups names of location groups of the proposal
     * @param channelUID the UID of the channel the Item is linked to
     */
    public record Item(String name, String type, String label, @Nullable String category, List<String> tags,
            List<String> groups, String channelUID) {

        public Item {
            tags = List.copyOf(tags);
            groups = List.copyOf(groups);
            if (category != null && category.isEmpty()) {
                category = null;
            }
            require(ProposalNames.isItemName(name), "Item name");
            requireLabel(label, cleanItemLabel(label));
            require(category == null || isCategory(category), "category");
            require(tags.size() <= MAX_TAGS && tags.stream().allMatch(tag -> TAG.matcher(tag).matches()), "tags");
            require(groups.stream().allMatch(ProposalNames::isItemName), "groups");
        }
    }

    /**
     * The group Item of a space.
     *
     * @param tag one of {@link ProposalLocations#TAGS}, {@code null} when none
     * @param category the icon, {@code null} when none; {@code ""} is taken as none
     * @param parent the name of the location group this one lies in, {@code null} when none
     * @param existing openHAB has a group Item of this name already: it is used as it is and not created
     */
    public record LocationGroup(String name, String label, @Nullable String tag, @Nullable String category,
            @Nullable String parent, boolean existing) {

        public LocationGroup {
            if (category != null && category.isEmpty()) {
                category = null;
            }
            require(ProposalNames.isItemName(name), "location group name");
            requireLabel(label, cleanItemLabel(label));
            require(tag == null || ProposalLocations.TAGS.contains(tag), "location tag");
            require(category == null || isCategory(category), "category");
            require(parent == null || ProposalNames.isItemName(parent) && !parent.equals(name), "parent");
        }
    }

    /** The raw number of an address the model holds. */
    static int rawAddress(String address) {
        try {
            return new GroupAddress(address).getRawAddress();
        } catch (KNXFormatException e) {
            throw new IllegalArgumentException("address", e);
        }
    }

    private static void requireAddress(String address) {
        require(ADDRESS.matcher(address).matches(), "address");
        rawAddress(address);
    }

    private static void requireLabel(String label, String cleaned) {
        require(!label.isEmpty() && label.equals(cleaned), "label");
    }

    private static void require(boolean condition, String what) {
        if (!condition) {
            throw new IllegalArgumentException("not a checked proposal: " + what);
        }
    }
}

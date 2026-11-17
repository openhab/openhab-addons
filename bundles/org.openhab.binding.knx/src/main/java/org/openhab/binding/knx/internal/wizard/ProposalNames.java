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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProposal;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.Room;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;

/**
 * The ids, UIDs, Item names and location group names the wizard proposes, made from the labels and kept unique within
 * the proposal and against the registries. The registries come in as functions.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ProposalNames {

    /** A Thing id or a channel id. */
    public static final Pattern ID = Pattern.compile("[A-Za-z0-9_]{1,40}");
    /** An Item name or a location group name. */
    public static final Pattern ITEM_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,79}");
    /**
     * The words of core's Thing grammar that a Thing or channel id cannot be in a {@code .things} file: its
     * {@code ID} token would be read as the keyword.
     */
    public static final Set<String> THING_KEYWORDS = Set.of("Thing", "Bridge", "Type", "State", "Trigger", "true",
            "false", "Switch", "Rollershutter", "Number", "String", "Dimmer", "Contact", "DateTime", "Color", "Player",
            "Location", "Call", "Image");
    /** The words of core's Items grammar that an Item name cannot be in an {@code .items} file. */
    public static final Set<String> ITEM_KEYWORDS = Set.of("true", "false");

    public static final String THING_FALLBACK = "thing";
    public static final String CHANNEL_FALLBACK = "channel";
    static final String ITEM_FALLBACK = "item";
    static final String ROOM_FALLBACK = "Room";
    static final String GROUP_PREFIX = "g";

    private static final int MAX_ID = 40;
    private static final int MAX_ITEM_NAME = 80;
    private static final int MAX_NUMBER = 100_000;
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern OTHER = Pattern.compile("[^A-Za-z0-9]+");

    /**
     * The names of a proposal, parallel to it: one entry per Thing, per channel and per split channel, in the
     * proposal's order.
     *
     * @param rooms one per room of the proposal, in its order
     */
    public record Names(List<ThingNames> things, List<RoomNames> rooms) {

        public Names {
            things = List.copyOf(things);
            rooms = List.copyOf(rooms);
        }
    }

    public record ThingNames(String id, String uid, List<ChannelNames> channels) {

        public ThingNames {
            channels = List.copyOf(channels);
        }
    }

    /**
     * @param split the names of the channel's split channels, parallel to {@link Channel#split()}
     */
    public record ChannelNames(String id, String uid, String itemName, List<ChannelNames> split) {

        public ChannelNames {
            split = List.copyOf(split);
        }
    }

    /**
     * @param existing openHAB has a group Item of this name already and the proposal uses it
     */
    public record RoomNames(String roomId, String groupName, boolean existing) {
    }

    private final Predicate<String> thingExists;
    private final Predicate<String> itemExists;
    private final Function<String, @Nullable Collection<String>> groupsOfGroup;

    /**
     * @param thingExists whether a Thing of this UID exists
     * @param itemExists whether an Item of this name exists
     */
    public ProposalNames(Predicate<String> thingExists, Predicate<String> itemExists) {
        this(thingExists, itemExists, name -> null);
    }

    /**
     * @param groupsOfGroup the names of the groups an existing group Item of this name is in; {@code null} when no
     *            Item has the name or the Item is no group
     */
    public ProposalNames(Predicate<String> thingExists, Predicate<String> itemExists,
            Function<String, @Nullable Collection<String>> groupsOfGroup) {
        this.thingExists = thingExists;
        this.itemExists = itemExists;
        this.groupsOfGroup = groupsOfGroup;
    }

    public Names names(String bridgeUID, KnxProposal proposal) {
        Set<String> thingIds = new HashSet<>();
        Set<String> channelIds = new HashSet<>();
        Set<String> itemNames = new HashSet<>();
        List<ThingNames> things = new ArrayList<>();
        for (Thing thing : proposal.things()) {
            String id = unique(id(thing.label(), THING_FALLBACK), MAX_ID,
                    candidate -> thingIds.contains(candidate) || thingExists.test(thingUID(bridgeUID, candidate)));
            thingIds.add(id);
            String uid = thingUID(bridgeUID, id);
            List<ChannelNames> channels = new ArrayList<>();
            for (Channel channel : thing.channels()) {
                channels.add(channel(uid, thing.room(), channel, channelIds, itemNames));
            }
            things.add(new ThingNames(id, uid, channels));
        }
        Set<String> groupNames = new HashSet<>();
        Map<String, Room> roomById = new HashMap<>();
        Map<String, String> nameById = new HashMap<>();
        List<RoomNames> rooms = new ArrayList<>();
        for (Room room : proposal.rooms()) {
            roomById.put(room.id(), room);
            Set<String> above = new HashSet<>();
            Set<String> climbed = new HashSet<>();
            String parentId = room.parentId();
            while (parentId != null && climbed.add(parentId)) {
                String parentName = nameById.get(parentId);
                if (parentName != null) {
                    above.add(parentName);
                }
                Room parent = roomById.get(parentId);
                parentId = parent == null ? null : parent.parentId();
            }
            String name = unique(groupName(room.name()), MAX_ITEM_NAME, candidate -> groupNames.contains(candidate)
                    || itemNames.contains(candidate) || itemExists.test(candidate) && !isSameGroup(candidate, above));
            groupNames.add(name);
            nameById.put(room.id(), name);
            rooms.add(new RoomNames(room.id(), name, itemExists.test(name)));
        }
        return new Names(things, rooms);
    }

    /**
     * Whether the existing Item of this name is a group that can stand for the space: a group that lies in no group,
     * or in the group of a space above. A group of the same name under another floor is not the same room.
     */
    private boolean isSameGroup(String name, Set<String> above) {
        Collection<String> groups = groupsOfGroup.apply(name);
        return groups != null && (groups.isEmpty() || groups.stream().anyMatch(above::contains));
    }

    private ChannelNames channel(String thingUID, @Nullable String room, Channel channel, Set<String> channelIds,
            Set<String> itemNames) {
        String id = unique(id(channel.label(), CHANNEL_FALLBACK), MAX_ID, channelIds::contains);
        channelIds.add(id);
        String item = unique(itemName(room, channel.label()), MAX_ITEM_NAME,
                candidate -> itemNames.contains(candidate) || itemExists.test(candidate));
        itemNames.add(item);
        List<ChannelNames> split = new ArrayList<>();
        for (Channel part : channel.split()) {
            split.add(channel(thingUID, room, part, channelIds, itemNames));
        }
        return new ChannelNames(id, thingUID + ":" + id, item, split);
    }

    /** {@code knx:device:<bridge id>:<id>}; the bridge id is the last part of the bridge UID. */
    public static String thingUID(String bridgeUID, String id) {
        return "knx:device:" + bridgeUID.substring(bridgeUID.lastIndexOf(':') + 1) + ":" + id;
    }

    /**
     * Accents folded, every other character outside {@code [A-Za-z0-9]} as {@code _}, runs of {@code _} as one,
     * none at the ends.
     */
    public static String fold(String text) {
        String decomposed = Normalizer.normalize(text.replace("ß", "ss").replace("ẞ", "SS"), Normalizer.Form.NFD);
        String plain = MARKS.matcher(decomposed).replaceAll("");
        return strip(OTHER.matcher(plain).replaceAll("_"));
    }

    /** Whether a Thing or channel id matches {@link #ID} and is no word of the Thing grammar. */
    public static boolean isId(String id) {
        return ID.matcher(id).matches() && !THING_KEYWORDS.contains(id);
    }

    /** Whether an Item or room group name matches {@link #ITEM_NAME} and is no word of the Items grammar. */
    public static boolean isItemName(String name) {
        return ITEM_NAME.matcher(name).matches() && !ITEM_KEYWORDS.contains(name);
    }

    /** A Thing or channel id from a label, before it is made unique. */
    public static String id(String label, String fallback) {
        String folded = fold(label);
        String id = folded.isEmpty() ? fallback : cut(folded, MAX_ID);
        return THING_KEYWORDS.contains(id) ? id + "_" : id;
    }

    /** {@code <room>_<label>} when a room is known, else the label, folded; before it is made unique. */
    public static String itemName(@Nullable String room, String label) {
        String folded = fold(room == null ? label : room + "_" + label);
        if (folded.isEmpty()) {
            folded = ITEM_FALLBACK;
        } else if (Character.isDigit(folded.charAt(0))) {
            folded = "_" + folded;
        }
        String name = cut(folded, MAX_ITEM_NAME);
        return ITEM_KEYWORDS.contains(name) ? name + "_" : name;
    }

    /** {@code g} and the room's name folded; before it is made unique. */
    public static String groupName(String roomName) {
        String folded = fold(roomName);
        return cut(GROUP_PREFIX + (folded.isEmpty() ? ROOM_FALLBACK : folded), MAX_ITEM_NAME);
    }

    /** The base when free, else {@code base_2}, {@code base_3}, ..., the base cut so the limit holds. */
    static String unique(String base, int max, Predicate<String> taken) {
        if (!taken.test(base)) {
            return base;
        }
        for (int n = 2; n < MAX_NUMBER; n++) {
            String suffix = "_" + n;
            String stem = cut(base, max - suffix.length());
            // the _ a grammar word got: Dimmer_ gives Dimmer_2, not Dimmer__2
            if (stem.length() > 1 && stem.endsWith("_")) {
                stem = stem.substring(0, stem.length() - 1);
            }
            String candidate = stem + suffix;
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("no free name");
    }

    private static String cut(String name, int max) {
        if (name.length() <= max) {
            return name;
        }
        String cut = name.substring(0, max);
        int end = cut.length();
        while (end > 1 && cut.charAt(end - 1) == '_') {
            end--;
        }
        return cut.substring(0, end);
    }

    private static String strip(String text) {
        int start = 0;
        int end = text.length();
        while (start < end && text.charAt(start) == '_') {
            start++;
        }
        while (end > start && text.charAt(end - 1) == '_') {
            end--;
        }
        return text.substring(start, end);
    }
}

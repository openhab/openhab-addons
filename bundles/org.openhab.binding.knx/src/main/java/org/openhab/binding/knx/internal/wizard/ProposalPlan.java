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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.channel.GroupAddressConfiguration;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Channel;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Item;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.LocationGroup;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Part;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Thing;

import io.calimero.GroupAddress;

/**
 * The steps that create a checked proposal through core's REST API, in the order the page runs them, with the
 * bodies as core takes them, and the read telegrams the created channels cause.
 *
 * @param steps the location groups that are not there yet, a group after the group it lies in, then the Things,
 *            then the Items, then the links
 * @param readAddresses the addresses the channels read, each once, in the order of the steps
 * @param readTelegrams the read telegrams each time the add-on schedules its reads: the sum over all channels and
 *            keys of {@link GroupAddressConfiguration#getReadGAs()}
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record ProposalPlan(List<Step> steps, List<String> readAddresses, int readTelegrams, Counts counts) {

    public ProposalPlan {
        steps = List.copyOf(steps);
        readAddresses = List.copyOf(readAddresses);
    }

    public enum Kind {
        GROUP,
        THING,
        ITEM,
        LINK
    }

    /**
     * One call: {@code POST /rest/things} for a {@code THING}, {@code PUT /rest/items/{itemName}} for a
     * {@code GROUP} or an {@code ITEM}, {@code PUT /rest/links/{itemName}/{channelUID}} for a {@code LINK}. Exactly
     * one body, the one of its kind.
     *
     * @param uid the Thing's UID, only for a {@code THING}
     * @param itemName the Item's or group's name, for a {@code GROUP}, an {@code ITEM} and a {@code LINK}
     * @param channelUID the channel's UID, only for a {@code LINK}
     */
    public record Step(Kind kind, @Nullable String uid, @Nullable String itemName, @Nullable String channelUID,
            @Nullable ThingBody thing, @Nullable ItemBody item, @Nullable LinkBody link) {

        public Step {
            boolean fits = switch (kind) {
                case THING -> uid != null && itemName == null && channelUID == null && thing != null && item == null
                        && link == null;
                case GROUP, ITEM -> uid == null && itemName != null && channelUID == null && thing == null
                        && item != null && link == null;
                case LINK -> uid == null && itemName != null && channelUID != null && thing == null && item == null
                        && link != null;
            };
            if (!fits) {
                throw new IllegalArgumentException("a step needs the ids and the one body of its kind");
            }
        }
    }

    /** The body of {@code POST /rest/things}, with the field names of core's {@code ThingDTO}. */
    public record ThingBody(String UID, String thingTypeUID, String bridgeUID, String label,
            Map<String, String> configuration, List<ChannelBody> channels) {

        public ThingBody {
            configuration = copy(configuration);
            channels = List.copyOf(channels);
        }
    }

    /**
     * A channel of a {@link ThingBody}, with the field names of core's {@code ChannelDTO}. Core answers 500 without
     * {@code properties} or {@code defaultTags}.
     */
    public record ChannelBody(String uid, String id, String channelTypeUID, String kind, String itemType, String label,
            Map<String, String> configuration, Map<String, String> properties, List<String> defaultTags) {

        public ChannelBody {
            configuration = copy(configuration);
            properties = copy(properties);
            defaultTags = List.copyOf(defaultTags);
        }
    }

    /** The body of {@code PUT /rest/items/{name}}, with the field names of core's {@code ItemDTO}. */
    public record ItemBody(String type, String name, String label, @Nullable String category, List<String> tags,
            List<String> groupNames) {

        public ItemBody {
            tags = List.copyOf(tags);
            groupNames = List.copyOf(groupNames);
        }
    }

    /**
     * The body of {@code PUT /rest/links/{itemName}/{channelUID}}, with the field names of core's
     * {@code ItemChannelLinkDTO}; both names must equal the path or core answers 400.
     */
    public record LinkBody(String itemName, String channelUID, Map<String, String> configuration) {

        public LinkBody {
            configuration = copy(configuration);
        }
    }

    public record Counts(int things, int channels, int items, int links, int groups) {
    }

    static final String THING_TYPE_UID = "knx:device";
    static final String GROUP_TYPE = "Group";
    private static final String KIND = "STATE";

    public static ProposalPlan of(CheckedProposal proposal) {
        List<Step> groups = new ArrayList<>();
        for (LocationGroup group : proposal.locationGroups()) {
            if (group.existing()) {
                continue;
            }
            String tag = group.tag();
            String parent = group.parent();
            groups.add(new Step(Kind.GROUP, null, group.name(), null, null,
                    new ItemBody(GROUP_TYPE, group.name(), group.label(), group.category(),
                            tag == null ? List.of() : List.of(tag), parent == null ? List.of() : List.of(parent)),
                    null));
        }
        List<Step> things = new ArrayList<>();
        List<Step> items = new ArrayList<>();
        List<Step> links = new ArrayList<>();
        Map<Integer, String> reads = new LinkedHashMap<>();
        int telegrams = 0;
        int channelCount = 0;
        for (Thing thing : proposal.things()) {
            List<ChannelBody> channels = new ArrayList<>();
            for (Channel channel : thing.channels()) {
                Map<String, String> configuration = new LinkedHashMap<>();
                for (Part part : channel.parts()) {
                    String text = part.text();
                    configuration.put(part.key(), text);
                    GroupAddressConfiguration parsed = GroupAddressConfiguration.parse(text);
                    if (parsed == null) {
                        throw new IllegalStateException("a channel text of the checked model does not parse");
                    }
                    telegrams += parsed.getReadGAs().size();
                    for (GroupAddress read : parsed.getReadGAs()) {
                        reads.putIfAbsent(read.getRawAddress(), addressText(part, read));
                    }
                }
                String itemType = Objects.requireNonNull(
                        ProposalItems.itemType(channel.channelType(), channel.parts().get(0).dataType()));
                channels.add(new ChannelBody(channel.uid(), channel.id(), "knx:" + channel.channelType(), KIND,
                        itemType, channel.label(), configuration, Map.of(), List.of()));
                Item item = channel.item();
                items.add(new Step(Kind.ITEM, null, item.name(), null, null, new ItemBody(item.type(), item.name(),
                        item.label(), item.category(), item.tags(), item.groups()), null));
                links.add(new Step(Kind.LINK, null, item.name(), channel.uid(), null, null,
                        new LinkBody(item.name(), channel.uid(), Map.of())));
            }
            channelCount += channels.size();
            things.add(new Step(Kind.THING, thing.uid(), null, null,
                    new ThingBody(thing.uid(), THING_TYPE_UID, proposal.bridgeUID(), thing.label(), Map.of(), channels),
                    null, null));
        }
        List<Step> steps = new ArrayList<>(groups);
        steps.addAll(things);
        steps.addAll(items);
        steps.addAll(links);
        return new ProposalPlan(steps, List.copyOf(reads.values()), telegrams,
                new Counts(things.size(), channelCount, items.size(), links.size(), groups.size()));
    }

    /** The read address as the model writes it. */
    private static String addressText(Part part, GroupAddress read) {
        List<String> addresses = new ArrayList<>();
        addresses.add(part.groupAddress());
        String status = part.statusAddress();
        if (status != null) {
            addresses.add(status);
        }
        for (String address : addresses) {
            if (CheckedProposal.rawAddress(address) == read.getRawAddress()) {
                return address;
            }
        }
        return read.toString();
    }

    private static Map<String, String> copy(Map<String, String> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }
}

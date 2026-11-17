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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * What the wizard proposes for a selection of a project: Things with their channels, command and status paired.
 * Names, ids and Items are not part of it.
 *
 * @param things the Things: functions in the order of a depth-first walk of the building tree, then devices in
 *            topology order, then one Thing for the loose addresses
 * @param rooms the spaces the Things sit in and every space above one of them, each once, a space before the
 *            spaces inside it
 * @param unknown the selection entries the project does not hold, as given; a group address in the project's style
 * @param leftOut the chosen functions and devices of the project that got no Thing, in the order of the Things
 * @param readAtStart whether read marks were made at all
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxProposal(List<Thing> things, List<Room> rooms, List<String> unknown, List<LeftOut> leftOut,
        boolean readAtStart) {

    public KnxProposal {
        things = List.copyOf(things);
        rooms = List.copyOf(rooms);
        unknown = List.copyOf(unknown);
        leftOut = List.copyOf(leftOut);
    }

    /**
     * A chosen function or device without a Thing: it has no address, or every address of it is kept by an
     * earlier Thing.
     *
     * @param source {@code FUNCTION} or {@code DEVICE}
     * @param sourceId the function's id or the device's id
     */
    public record LeftOut(Source source, String sourceId) {
    }

    /** Where a Thing comes from. */
    public enum Source {
        FUNCTION,
        DEVICE,
        ADDRESSES
    }

    /** Which rule paired a command with its status address; {@code NONE} when no rule gave exactly one. */
    public enum Pairing {
        ROLE,
        OBJECTS,
        FUNCTION,
        NAMES,
        NONE
    }

    /**
     * A space of the building tree a Thing is placed in, or one above such a space.
     *
     * @param type the type as the project writes it, {@link KnxSpace#type()}
     * @param parentId the id of the space this one lies in, {@code null} for an outermost space
     */
    public record Room(String id, String name, String type, @Nullable String parentId) {
    }

    /**
     * A proposed Thing of type {@code knx:device}. It carries no setting of the device Thing: no individual
     * address, so the add-on never polls the device.
     *
     * @param label the function's name, the device's name, or {@code KNX} for loose addresses
     * @param sourceId the function's id, the device's id, {@code ""} for loose addresses
     * @param roomId the id of the space of the function or the device, {@code null} when none
     * @param room the name of that space, {@code null} when none
     * @param channels ordered by the raw address of their first part
     */
    public record Thing(String label, Source source, String sourceId, @Nullable String roomId, @Nullable String room,
            List<Channel> channels) {

        public Thing {
            channels = List.copyOf(channels);
        }
    }

    /**
     * A proposed channel.
     *
     * @param channelType the channel type id, {@code null} when no type works for the data type
     * @param allowedChannelTypes the channel types that may be chosen instead; a combined channel has only its own
     * @param label the label
     * @param selected whether the channel is proposed for creation; false with a note when it cannot or should not
     * @param statusOnly a status address no command of the proposal pairs with
     * @param parts one per key, in the order of {@link ChannelKeyTypes#keysOf(String)}
     * @param split the channels a combined channel is when not combined; empty for a channel of one part
     * @param alreadyConfigured the addresses of the parts (main and status) a channel of the bridge already uses
     * @param notes why the channel is incomplete or not selected, the mapper's notes first
     */
    public record Channel(@Nullable String channelType, List<String> allowedChannelTypes, String label,
            boolean selected, boolean statusOnly, List<Part> parts, List<Channel> split,
            List<KnxGroupAddress> alreadyConfigured, List<String> notes) {

        public Channel {
            allowedChannelTypes = List.copyOf(allowedChannelTypes);
            parts = List.copyOf(parts);
            split = List.copyOf(split);
            alreadyConfigured = List.copyOf(alreadyConfigured);
            notes = List.copyOf(notes);
        }

        /** Whether the channel is made of more than one part. */
        public boolean combined() {
            return parts.size() > 1;
        }
    }

    /**
     * One key of a channel.
     *
     * @param key the configuration key: {@code ga}, {@code switch}, {@code position}, ...
     * @param dataType the data type of the main address, {@code null} when none is known
     * @param groupAddress the main address, the one the channel writes to
     * @param read whether the main address carries the read mark
     * @param statusAddress the paired status address, {@code null} when none
     * @param statusRead whether the status address carries the read mark
     * @param pairing the rule that paired the status address
     * @param candidates the status addresses the rules had to choose from when none was paired, at most 20, by
     *            address
     * @param text the value of the key as the binding reads it, {@code 1.001:31/7/1+<31/7/2}
     */
    public record Part(String key, @Nullable KnxDatapointType dataType, KnxGroupAddress groupAddress, boolean read,
            @Nullable KnxGroupAddress statusAddress, boolean statusRead, Pairing pairing,
            List<KnxGroupAddress> candidates, String text) {

        public Part {
            candidates = List.copyOf(candidates);
        }
    }
}

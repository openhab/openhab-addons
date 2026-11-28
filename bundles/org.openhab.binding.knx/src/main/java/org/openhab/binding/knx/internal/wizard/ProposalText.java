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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Channel;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Item;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.LocationGroup;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Part;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Thing;

/**
 * The {@code .things} and {@code .items} text of a checked proposal, for the user to copy. A Thing has no
 * configuration block; its channels and the Items say the same as the create steps.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record ProposalText(String thingsText, String itemsText) {

    private static final String INDENT = "    ";

    public static ProposalText of(CheckedProposal proposal) {
        StringBuilder things = new StringBuilder();
        StringBuilder items = new StringBuilder();
        items.append(groupLines(proposal));
        for (Thing thing : proposal.things()) {
            if (!things.isEmpty()) {
                things.append('\n');
            }
            things.append("Thing ").append(thing.uid()).append(' ').append(quoted(thing.label())).append(" (")
                    .append(proposal.bridgeUID()).append(") {\n");
            for (Channel channel : thing.channels()) {
                things.append(channelLine(channel));
                items.append(itemLine(channel.item(), channel.item().channelUID()));
            }
            things.append("}\n");
        }
        return new ProposalText(things.toString(), items.toString());
    }

    /**
     * {@code Group <name> "<label>" [<icon>] [(parent)] [["tag"]]} per location group that is to be created, a group
     * after the group it lies in, and an empty line after them.
     */
    private static String groupLines(CheckedProposal proposal) {
        StringBuilder lines = new StringBuilder();
        for (LocationGroup group : proposal.locationGroups()) {
            if (group.existing()) {
                continue;
            }
            lines.append("Group ").append(group.name()).append(' ').append(quoted(group.label()));
            String category = group.category();
            if (category != null) {
                lines.append(" <").append(category).append('>');
            }
            String parent = group.parent();
            if (parent != null) {
                lines.append(" (").append(parent).append(')');
            }
            String tag = group.tag();
            if (tag != null) {
                lines.append(" [").append(quoted(tag)).append(']');
            }
            lines.append('\n');
        }
        if (!lines.isEmpty()) {
            lines.append('\n');
        }
        return lines.toString();
    }

    /**
     * The lines to paste into Things that exist already.
     *
     * @param thingsText per Thing a comment that names its target and, indented as inside a Thing block, its channel
     *            lines; a comment above a line whose channel id the target has already
     * @param itemsText the location groups and the Items, linked to the channels of the targets
     * @param existing per target with such ids, the channel ids it has already or gets twice
     * @param withoutTarget how many Things have no target; they have a comment and no line
     */
    public record Lines(String thingsText, String itemsText, Map<String, List<String>> existing, int withoutTarget) {
    }

    /**
     * @param targets per UID of a Thing of the proposal the UID of the Thing its lines are for
     * @param channelIdsOf the channel ids a Thing of this UID has; empty when there is no such Thing
     */
    public static Lines linesOf(CheckedProposal proposal, Map<String, String> targets,
            Function<String, Set<String>> channelIdsOf) {
        StringBuilder things = new StringBuilder();
        StringBuilder items = new StringBuilder(groupLines(proposal));
        Map<String, Set<String>> taken = new HashMap<>();
        Map<String, List<String>> existing = new LinkedHashMap<>();
        int withoutTarget = 0;
        for (Thing thing : proposal.things()) {
            if (!things.isEmpty()) {
                things.append('\n');
            }
            String target = targets.get(thing.uid());
            if (target == null) {
                withoutTarget++;
                things.append("// ").append(quoted(thing.label())).append(": no existing Thing is chosen\n");
                continue;
            }
            things.append("// Channels of ").append(quoted(thing.label())).append(" for the Thing ").append(target)
                    .append(": paste them before its closing brace\n");
            Set<String> ids = Objects
                    .requireNonNull(taken.computeIfAbsent(target, uid -> new HashSet<>(channelIdsOf.apply(uid))));
            for (Channel channel : thing.channels()) {
                if (!ids.add(channel.id())) {
                    Objects.requireNonNull(existing.computeIfAbsent(target, uid -> new ArrayList<>()))
                            .add(channel.id());
                    things.append(INDENT).append("// the Thing has a channel ").append(channel.id())
                            .append(" already: rename this one\n");
                }
                things.append(channelLine(channel));
                items.append(itemLine(channel.item(), target + ":" + channel.id()));
            }
        }
        return new Lines(things.toString(), items.toString(), existing, withoutTarget);
    }

    /** {@code Type <type> : <id> "<label>" [ <key>="<text>", ... ]}, indented as inside a Thing block. */
    private static String channelLine(Channel channel) {
        List<String> keys = new ArrayList<>();
        for (Part part : channel.parts()) {
            keys.add(part.key() + "=" + quoted(part.text()));
        }
        return INDENT + "Type " + channel.channelType() + " : " + channel.id() + ' ' + quoted(channel.label()) + " [ "
                + String.join(", ", keys) + " ]\n";
    }

    /** {@code <Type> <name> "<label>" [<icon>] [(groups)] [["tags"]] { channel="<channel UID>" }} */
    private static String itemLine(Item item, String channelUID) {
        StringBuilder line = new StringBuilder();
        line.append(item.type()).append(' ').append(item.name()).append(' ').append(quoted(item.label()));
        String category = item.category();
        if (category != null) {
            line.append(" <").append(category).append('>');
        }
        if (!item.groups().isEmpty()) {
            line.append(" (").append(String.join(", ", item.groups())).append(')');
        }
        if (!item.tags().isEmpty()) {
            line.append(" [").append(String.join(", ", item.tags().stream().map(ProposalText::quoted).toList()))
                    .append(']');
        }
        line.append(" { channel=").append(quoted(channelUID)).append(" }\n");
        return line.toString();
    }

    private static String quoted(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

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
package org.openhab.binding.knx.internal.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxProposal;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.LeftOut;
import org.openhab.binding.knx.internal.project.KnxProposal.Part;
import org.openhab.binding.knx.internal.project.KnxProposal.Room;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;
import org.openhab.binding.knx.internal.projectstore.ProjectNames;
import org.openhab.binding.knx.internal.rest.dto.CandidateDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.CreateStepDTO;
import org.openhab.binding.knx.internal.rest.dto.ExistingChannelsDTO;
import org.openhab.binding.knx.internal.rest.dto.ItemBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.LeftOutDTO;
import org.openhab.binding.knx.internal.rest.dto.LinkBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.ProblemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalCountsDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalLinesDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalPreviewDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedChannelDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedItemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedPartDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedRoomDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedThingDTO;
import org.openhab.binding.knx.internal.rest.dto.ThingBodyDTO;
import org.openhab.binding.knx.internal.wizard.CheckedProposal;
import org.openhab.binding.knx.internal.wizard.ProposalItems;
import org.openhab.binding.knx.internal.wizard.ProposalLocations;
import org.openhab.binding.knx.internal.wizard.ProposalNames.ChannelNames;
import org.openhab.binding.knx.internal.wizard.ProposalNames.Names;
import org.openhab.binding.knx.internal.wizard.ProposalNames.RoomNames;
import org.openhab.binding.knx.internal.wizard.ProposalNames.ThingNames;
import org.openhab.binding.knx.internal.wizard.ProposalPlan;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.ChannelBody;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.ItemBody;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.LinkBody;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.Step;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.ThingBody;
import org.openhab.binding.knx.internal.wizard.ProposalText;
import org.openhab.binding.knx.internal.wizard.ProposalValidator;

/**
 * Fills the answer classes of the wizard's calls 21 and 22, field by field. Labels go out cleaned as the checked
 * model cleans them, so a proposal sent back unchanged passes the label check.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class KnxProposalMapper {

    private KnxProposalMapper() {
    }

    /**
     * @param channelsByAddress per raw address the UIDs of the bridge's channels that use it
     */
    static ProposalDTO proposal(KnxProposal proposal, Names names, Map<Integer, Set<String>> channelsByAddress,
            int thingsWithoutHandler) {
        ProposalDTO dto = new ProposalDTO();
        List<ProposedThingDTO> things = new ArrayList<>();
        for (int i = 0; i < proposal.things().size(); i++) {
            things.add(thing(proposal.things().get(i), names.things().get(i), channelsByAddress));
        }
        dto.things = things;
        Map<String, String> groupNames = new LinkedHashMap<>();
        names.rooms().forEach(room -> groupNames.put(room.roomId(), room.groupName()));
        List<ProposedRoomDTO> rooms = new ArrayList<>();
        for (int i = 0; i < proposal.rooms().size(); i++) {
            rooms.add(room(proposal.rooms().get(i), names.rooms().get(i), groupNames));
        }
        dto.rooms = rooms;
        dto.unknown = proposal.unknown().stream().map(ProjectNames::clean).toList();
        dto.leftOut = proposal.leftOut().stream().map(KnxProposalMapper::leftOut).toList();
        dto.readAtStart = proposal.readAtStart();
        dto.thingsWithoutHandler = thingsWithoutHandler;
        return dto;
    }

    private static LeftOutDTO leftOut(LeftOut leftOut) {
        LeftOutDTO dto = new LeftOutDTO();
        dto.source = leftOut.source().name();
        dto.sourceId = leftOut.sourceId();
        return dto;
    }

    private static ProposedThingDTO thing(Thing thing, ThingNames names, Map<Integer, Set<String>> channelsByAddress) {
        ProposedThingDTO dto = new ProposedThingDTO();
        dto.id = names.id();
        dto.uid = names.uid();
        dto.label = label(thing.label(), names.id());
        dto.source = thing.source().name();
        dto.sourceId = thing.sourceId();
        dto.roomId = thing.roomId();
        String room = thing.room();
        dto.room = room == null ? null : ProjectNames.clean(room);
        List<ProposedChannelDTO> channels = new ArrayList<>();
        for (int i = 0; i < thing.channels().size(); i++) {
            channels.add(channel(thing.channels().get(i), names.channels().get(i), channelsByAddress));
        }
        dto.channels = channels;
        return dto;
    }

    private static ProposedChannelDTO channel(Channel channel, ChannelNames names,
            Map<Integer, Set<String>> channelsByAddress) {
        ProposedChannelDTO dto = new ProposedChannelDTO();
        dto.id = names.id();
        dto.channelType = channel.channelType();
        dto.allowedChannelTypes = channel.allowedChannelTypes();
        dto.label = label(channel.label(), names.id());
        dto.selected = channel.selected();
        dto.statusOnly = channel.statusOnly();
        dto.combined = channel.combined();
        dto.parts = channel.parts().stream().map(KnxProposalMapper::part).toList();
        ProposalItems.Item item = ProposalItems.of(channel);
        if (item != null) {
            ProposedItemDTO itemDto = new ProposedItemDTO();
            itemDto.name = names.itemName();
            itemDto.type = item.type();
            itemDto.allowedTypes = item.allowedTypes();
            itemDto.allowedItemTypes = ProposalItems.allowedTypesByChannelType(channel);
            itemDto.label = itemLabel(channel.label(), names.itemName());
            itemDto.category = item.category();
            itemDto.tags = item.tags();
            itemDto.groups = List.of();
            dto.item = itemDto;
        }
        if (channel.combined()) {
            List<ProposedChannelDTO> split = new ArrayList<>();
            for (int i = 0; i < channel.split().size(); i++) {
                split.add(channel(channel.split().get(i), names.split().get(i), channelsByAddress));
            }
            dto.split = split;
        }
        Set<String> used = new TreeSet<>();
        for (KnxGroupAddress address : channel.alreadyConfigured()) {
            used.addAll(channelsByAddress.getOrDefault(address.rawAddress(), Set.of()));
        }
        dto.alreadyConfigured = List.copyOf(used);
        dto.notes = channel.notes().stream().map(ProjectNames::clean).toList();
        return dto;
    }

    private static ProposedPartDTO part(Part part) {
        ProposedPartDTO dto = new ProposedPartDTO();
        dto.key = part.key();
        KnxDatapointType dataType = part.dataType();
        dto.dataType = dataType == null ? null : dataType.toString();
        dto.groupAddress = part.groupAddress().address();
        dto.groupAddressName = ProjectNames.clean(part.groupAddress().name());
        dto.secured = part.groupAddress().secured();
        dto.read = part.read();
        KnxGroupAddress status = part.statusAddress();
        dto.statusAddress = status == null ? null : status.address();
        dto.statusName = status == null ? null : ProjectNames.clean(status.name());
        dto.statusRead = part.statusRead();
        dto.pairing = part.pairing().name();
        List<CandidateDTO> candidates = new ArrayList<>();
        for (KnxGroupAddress candidate : part.candidates()) {
            CandidateDTO candidateDto = new CandidateDTO();
            candidateDto.address = candidate.address();
            candidateDto.name = ProjectNames.clean(candidate.name());
            candidates.add(candidateDto);
        }
        dto.candidates = candidates;
        dto.text = part.text();
        return dto;
    }

    /** A space without a tag says {@code ""}: a request without the field means a room. */
    private static ProposedRoomDTO room(Room room, RoomNames names, Map<String, String> groupNames) {
        ProposedRoomDTO dto = new ProposedRoomDTO();
        dto.id = room.id();
        dto.name = ProjectNames.clean(room.name());
        dto.groupName = names.groupName();
        dto.label = itemLabel(room.name(), names.groupName());
        dto.type = ProjectNames.clean(room.type());
        String parentId = room.parentId();
        dto.parentId = parentId;
        dto.parent = parentId == null ? null : groupNames.get(parentId);
        String tag = ProposalLocations.tag(room.type());
        dto.tag = tag == null ? "" : tag;
        dto.allowedTags = ProposalLocations.TAGS;
        dto.category = ProposalLocations.category(room.type());
        dto.exists = names.existing();
        return dto;
    }

    /** The label as the checked model holds it; the fallback when nothing is left of it. */
    private static String label(String label, String fallback) {
        String cleaned = CheckedProposal.cleanLabel(ProjectNames.clean(label, CheckedProposal.MAX_LABEL));
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private static String itemLabel(String label, String fallback) {
        String cleaned = CheckedProposal.cleanItemLabel(ProjectNames.clean(label, CheckedProposal.MAX_LABEL));
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    /** The answer of call 22: without an error from the checked model, with one the problems and the reads. */
    /**
     * @param channelIdsOf the channel ids a Thing of this UID has; empty when there is no such Thing
     */
    static ProposalPreviewDTO preview(ProposalValidator.Result result, Function<String, Set<String>> channelIdsOf) {
        ProposalPreviewDTO dto = new ProposalPreviewDTO();
        dto.problems = result.problems().stream().map(KnxProposalMapper::problem).toList();
        CheckedProposal model = result.proposal();
        if (model == null || result.hasError()) {
            dto.readAddresses = result.readAddresses();
            dto.readTelegrams = result.readTelegrams();
            return dto;
        }
        ProposalText text = ProposalText.of(model);
        ProposalPlan plan = ProposalPlan.of(model);
        dto.thingsText = text.thingsText();
        dto.itemsText = text.itemsText();
        dto.readAddresses = plan.readAddresses();
        dto.readTelegrams = plan.readTelegrams();
        ProposalCountsDTO counts = new ProposalCountsDTO();
        counts.things = plan.counts().things();
        counts.channels = plan.counts().channels();
        counts.items = plan.counts().items();
        counts.links = plan.counts().links();
        counts.groups = plan.counts().groups();
        dto.counts = counts;
        dto.steps = plan.steps().stream().map(KnxProposalMapper::step).toList();
        dto.existingGroups = model.locationGroups().stream().filter(CheckedProposal.LocationGroup::existing)
                .map(CheckedProposal.LocationGroup::name).toList();
        if (!result.targets().isEmpty()) {
            ProposalText.Lines lines = ProposalText.linesOf(model, result.targets(), channelIdsOf);
            ProposalLinesDTO linesDto = new ProposalLinesDTO();
            linesDto.thingsText = lines.thingsText();
            linesDto.itemsText = lines.itemsText();
            List<ExistingChannelsDTO> existing = new ArrayList<>();
            lines.existing().forEach((target, ids) -> {
                ExistingChannelsDTO entry = new ExistingChannelsDTO();
                entry.target = target;
                entry.channelIds = List.copyOf(ids);
                existing.add(entry);
            });
            linesDto.existingChannels = existing;
            linesDto.withoutTarget = lines.withoutTarget();
            dto.lines = linesDto;
        }
        return dto;
    }

    private static ProblemDTO problem(ProposalValidator.Problem problem) {
        ProblemDTO dto = new ProblemDTO();
        dto.severity = problem.severity().name();
        dto.code = problem.code();
        dto.where = problem.where();
        dto.text = problem.text();
        return dto;
    }

    private static CreateStepDTO step(Step step) {
        CreateStepDTO dto = new CreateStepDTO();
        dto.kind = step.kind().name();
        dto.uid = step.uid();
        dto.itemName = step.itemName();
        dto.channelUID = step.channelUID();
        ThingBody thing = step.thing();
        if (thing != null) {
            dto.thing = new ThingBodyDTO(thing.UID(), thing.thingTypeUID(), thing.bridgeUID(), thing.label(),
                    texts(thing.configuration()), thing.channels().stream().map(KnxProposalMapper::channel).toList());
        }
        ItemBody item = step.item();
        if (item != null) {
            ItemBodyDTO itemDto = new ItemBodyDTO();
            itemDto.type = item.type();
            itemDto.name = item.name();
            itemDto.label = item.label();
            itemDto.category = item.category();
            itemDto.tags = item.tags();
            itemDto.groupNames = item.groupNames();
            dto.item = itemDto;
        }
        LinkBody link = step.link();
        if (link != null) {
            LinkBodyDTO linkDto = new LinkBodyDTO();
            linkDto.itemName = link.itemName();
            linkDto.channelUID = link.channelUID();
            linkDto.configuration = texts(link.configuration());
            dto.link = linkDto;
        }
        return dto;
    }

    private static ChannelBodyDTO channel(ChannelBody channel) {
        ChannelBodyDTO dto = new ChannelBodyDTO();
        dto.uid = channel.uid();
        dto.id = channel.id();
        dto.channelTypeUID = channel.channelTypeUID();
        dto.kind = channel.kind();
        dto.itemType = channel.itemType();
        dto.label = channel.label();
        dto.configuration = texts(channel.configuration());
        dto.properties = texts(channel.properties());
        dto.defaultTags = channel.defaultTags();
        return dto;
    }

    private static Map<String, String> texts(Map<String, String> map) {
        return new LinkedHashMap<>(map);
    }
}

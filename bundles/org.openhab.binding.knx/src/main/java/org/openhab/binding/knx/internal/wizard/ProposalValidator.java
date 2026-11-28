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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.channel.GroupAddressConfiguration;
import org.openhab.binding.knx.internal.project.ChannelKeyTypes;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProposalBuilder;
import org.openhab.binding.knx.internal.rest.dto.PreviewRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedChannelDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedItemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedPartDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedRoomDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedThingDTO;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Channel;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Item;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.LocationGroup;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Part;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Read;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Thing;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;
import io.calimero.KNXFormatException;

/**
 * Builds the checked model of call 22 from the bridge's project, the registries and the few fields of the request
 * a user may edit: per Thing {@code id}, {@code label}, {@code channels}; per channel {@code id}, {@code label},
 * {@code channelType}, {@code selected}, {@code parts}, {@code item}; per part {@code key}, {@code groupAddress},
 * {@code statusAddress}, {@code read}, {@code statusRead}; per Item {@code name}, {@code type}, {@code label},
 * {@code category}, {@code tags}, {@code groups}; per location group {@code groupName}, {@code label},
 * {@code tag}, {@code category} and {@code parent}; {@code readAtStart}. No other field of the request is read: data
 * types, names and the channel texts come
 * from the project. A problem text never repeats request text; it names at most an address of
 * the project or an id that matched its pattern.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ProposalValidator {

    public static final String BAD_ID = "bad-id";
    public static final String BAD_ITEM_NAME = "bad-item-name";
    public static final String BAD_LABEL = "bad-label";
    public static final String BAD_TAG = "bad-tag";
    public static final String BAD_CATEGORY = "bad-category";
    public static final String BAD_KEY = "bad-key";
    public static final String DUPLICATE_ID = "duplicate-id";
    public static final String DUPLICATE_ITEM = "duplicate-item";
    public static final String THING_EXISTS = "thing-exists";
    public static final String ITEM_EXISTS = "item-exists";
    public static final String UNKNOWN_GROUP = "unknown-group";
    public static final String UNKNOWN_ADDRESS = "unknown-address";
    public static final String BAD_ADDRESS = "bad-address";
    public static final String TYPE_NOT_ALLOWED = "type-not-allowed";
    public static final String NO_DATA_TYPE = "no-data-type";
    public static final String NOTHING_SELECTED = "nothing-selected";
    public static final String ALREADY_CONFIGURED = "already-configured";
    public static final String STATUS_ONLY = "status-only";
    public static final String SECURED_WITHOUT_KEYRING = "secured-without-keyring";
    public static final String READ_WITHOUT_FLAG = "read-without-flag";
    public static final String GROUP_CYCLE = "group-cycle";
    public static final String BAD_TARGET = "bad-target";

    private static final Pattern TARGET = Pattern.compile("knx:device:[A-Za-z0-9_-]{1,80}(:[A-Za-z0-9_-]{1,80})?");

    /** At most this many Things, channels, parts and location groups each. */
    public static final int MAX_ENTRIES = 2000;
    /** A label longer than this, before cleaning, is refused. */
    public static final int MAX_LABEL_INPUT = 200;

    public enum Severity {
        ERROR,
        WARNING
    }

    /**
     * @param where the place in the request: {@code things/0/channels/1/parts/0/statusAddress}
     * @param text a fixed text, at most with an address of the project or a checked id
     */
    public record Problem(Severity severity, String code, String where, String text) {
    }

    /**
     * @param proposal the checked model; {@code null} when a problem is an error
     * @param readAddresses the addresses the channels read, each once; with an error those of the parts that
     *            could be checked
     * @param readTelegrams the sum of {@code getReadGAs().size()} over those parts
     * @param targets per UID of a Thing of the model the UID of the existing Thing its channel lines are for; only
     *            for the text, the model and the create steps do not know it
     */
    public record Result(@Nullable CheckedProposal proposal, List<Problem> problems, List<String> readAddresses,
            int readTelegrams, Map<String, String> targets) {

        public Result {
            targets = Map.copyOf(targets);
            problems = List.copyOf(problems);
            readAddresses = List.copyOf(readAddresses);
        }

        public boolean hasError() {
            return problems.stream().anyMatch(problem -> problem.severity() == Severity.ERROR);
        }
    }

    /** A request that misses a required field, holds a {@code null} list entry or too many entries. */
    public static final class BadRequest extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final String where;
        private final String reason;

        BadRequest(String where, String reason) {
            super(where + ": " + reason);
            this.where = where;
            this.reason = reason;
        }

        public String where() {
            return where;
        }

        public String reason() {
            return reason;
        }
    }

    private static final String MISSING = "is missing";
    private static final String NOT_AN_ENTRY = "is not an object";

    private final KnxProject project;
    private final KnxProposalBuilder facts;
    private final String bridgeUID;
    private final Predicate<String> thingExists;
    private final Predicate<String> itemExists;
    private final Predicate<String> groupExists;
    private final Set<Integer> configuredAddresses;
    private final boolean keyringLoaded;

    /**
     * @param bridgeUID the UID of the bridge of the call's path
     * @param thingExists whether a Thing of this UID exists
     * @param itemExists whether an Item of this name exists
     * @param configuredAddresses the raw addresses channels of the bridge use already
     * @param keyringLoaded whether the bridge has a keyring loaded
     */
    public ProposalValidator(KnxProject project, String bridgeUID, Predicate<String> thingExists,
            Predicate<String> itemExists, Set<Integer> configuredAddresses, boolean keyringLoaded) {
        this(project, bridgeUID, thingExists, itemExists, name -> false, configuredAddresses, keyringLoaded);
    }

    /**
     * @param groupExists whether a group Item of this name exists; a location group of that name is used as it is
     */
    public ProposalValidator(KnxProject project, String bridgeUID, Predicate<String> thingExists,
            Predicate<String> itemExists, Predicate<String> groupExists, Set<Integer> configuredAddresses,
            boolean keyringLoaded) {
        this.project = project;
        this.facts = new KnxProposalBuilder(project);
        this.bridgeUID = bridgeUID;
        this.thingExists = thingExists;
        this.itemExists = itemExists;
        this.groupExists = groupExists;
        this.configuredAddresses = Set.copyOf(configuredAddresses);
        this.keyringLoaded = keyringLoaded;
    }

    /** One check of one request. */
    private final class Run {
        final List<Problem> problems = new ArrayList<>();
        final boolean readAtStart;
        final Set<Integer> marked = new HashSet<>();
        final Map<Integer, String> reads = new LinkedHashMap<>();
        int telegrams;
        final Set<String> itemNames = new HashSet<>();
        final Set<String> groupNames = new HashSet<>();

        Run(boolean readAtStart) {
            this.readAtStart = readAtStart;
        }

        void error(String code, String where, String text) {
            problems.add(new Problem(Severity.ERROR, code, where, text));
        }

        void warning(String code, String where, String text) {
            problems.add(new Problem(Severity.WARNING, code, where, text));
        }

        int errors() {
            return (int) problems.stream().filter(problem -> problem.severity() == Severity.ERROR).count();
        }
    }

    /**
     * @throws BadRequest when a required field is missing, a list holds a {@code null} entry or there are too many
     *             entries
     */
    public Result validate(PreviewRequestDTO request) {
        List<ProposedThingDTO> things = entries(request.things, "things", ProposedThingDTO.class);
        List<ProposedRoomDTO> rooms = request.roomGroups == null ? List.of()
                : entries(request.roomGroups, "roomGroups", ProposedRoomDTO.class);
        Boolean readAtStart = request.readAtStart;
        Run run = new Run(readAtStart == null || readAtStart);
        countEntries(things);

        List<LocationGroup> locationGroups = locationGroups(run, rooms);

        List<Thing> checked = new ArrayList<>();
        Set<String> thingIds = new HashSet<>();
        Map<String, String> targets = new LinkedHashMap<>();
        int selected = 0;
        for (int t = 0; t < things.size(); t++) {
            ProposedThingDTO thing = things.get(t);
            String where = "things/" + t;
            List<ProposedChannelDTO> channels = entries(thing.channels, where + "/channels", ProposedChannelDTO.class);
            List<Integer> chosen = new ArrayList<>();
            for (int c = 0; c < channels.size(); c++) {
                if (channels.get(c).selected) {
                    chosen.add(c);
                }
            }
            if (chosen.isEmpty()) {
                continue;
            }
            selected += chosen.size();
            int errorsBefore = run.errors();
            String id = required(thing.id, where + "/id");
            String label = required(thing.label, where + "/label");
            String uid = ProposalNames.thingUID(bridgeUID, id);
            boolean idValid = ProposalNames.isId(id);
            if (!idValid) {
                run.error(BAD_ID, where + "/id",
                        "A Thing id holds letters, digits and _ only, at most 40, and is no .things keyword.");
            } else if (!thingIds.add(id)) {
                run.error(DUPLICATE_ID, where + "/id", "Two Things have the id " + id + ".");
            } else if (thingExists.test(uid)) {
                run.error(THING_EXISTS, where + "/id", "A Thing " + uid + " exists already.");
            }
            String cleanLabel = label(run, label, where + "/label", false);
            List<Channel> made = new ArrayList<>();
            Set<String> channelIds = new HashSet<>();
            for (int c : chosen) {
                Channel channel = channel(run, channels.get(c), uid, idValid, channelIds, where + "/channels/" + c);
                if (channel != null) {
                    made.add(channel);
                }
            }
            if (run.errors() == errorsBefore && cleanLabel != null) {
                checked.add(new Thing(uid, id, cleanLabel, made));
                String target = target(run, thing.target, where + "/target");
                if (target != null) {
                    targets.put(uid, target);
                }
            }
        }
        if (selected == 0) {
            run.error(NOTHING_SELECTED, "things", "No channel is selected.");
        }
        CheckedProposal proposal = run.errors() == 0
                ? new CheckedProposal(bridgeUID, checked, locationGroups, run.readAtStart)
                : null;
        return new Result(proposal, run.problems, List.copyOf(run.reads.values()), run.telegrams, targets);
    }

    /**
     * The location groups, a group after the group it lies in. A parent that is no group of the request, and a
     * group that lies in itself over its parents, is a problem.
     */
    private List<LocationGroup> locationGroups(Run run, List<ProposedRoomDTO> rooms) {
        List<@Nullable LocationGroup> made = new ArrayList<>();
        Map<String, Integer> indexByName = new LinkedHashMap<>();
        for (int i = 0; i < rooms.size(); i++) {
            LocationGroup group = locationGroup(run, rooms.get(i), "roomGroups/" + i);
            made.add(group);
            if (group != null) {
                indexByName.put(group.name(), i);
            }
        }
        for (int i = 0; i < rooms.size(); i++) {
            String parent = rooms.get(i).parent;
            String name = rooms.get(i).groupName;
            if (parent == null || parent.isEmpty()) {
                continue;
            }
            if (!run.groupNames.contains(parent) || parent.equals(name)) {
                run.error(UNKNOWN_GROUP, "roomGroups/" + i + "/parent",
                        "A location group can only lie in another location group of this run.");
            }
        }
        if (run.errors() > 0 || made.contains(null)) {
            return List.of();
        }
        List<LocationGroup> ordered = new ArrayList<>();
        Set<String> placed = new HashSet<>();
        List<Integer> waiting = new ArrayList<>();
        for (int i = 0; i < made.size(); i++) {
            waiting.add(i);
        }
        while (!waiting.isEmpty()) {
            List<Integer> still = new ArrayList<>();
            for (int i : waiting) {
                LocationGroup group = Objects.requireNonNull(made.get(i));
                String parent = group.parent();
                if (parent == null || placed.contains(parent)) {
                    ordered.add(group);
                    placed.add(group.name());
                } else {
                    still.add(i);
                }
            }
            if (still.size() == waiting.size()) {
                for (int i : still) {
                    run.error(GROUP_CYCLE, "roomGroups/" + i + "/parent",
                            "This location group lies in itself over its parents.");
                }
                return List.of();
            }
            waiting = still;
        }
        return ordered;
    }

    private @Nullable LocationGroup locationGroup(Run run, ProposedRoomDTO room, String where) {
        String name = required(room.groupName, where + "/groupName");
        String label = required(room.label, where + "/label");
        int errorsBefore = run.errors();
        boolean existing = false;
        if (!ProposalNames.isItemName(name)) {
            run.error(BAD_ITEM_NAME, where + "/groupName",
                    "A group name is a letter or _, then letters, digits and _, at most 80; not true or false.");
        } else if (!run.groupNames.add(name)) {
            run.error(DUPLICATE_ITEM, where + "/groupName", "Two groups or Items have the name " + name + ".");
        } else if (groupExists.test(name)) {
            existing = true;
        } else if (itemExists.test(name)) {
            run.error(ITEM_EXISTS, where + "/groupName", "An Item " + name + " exists already.");
        }
        String cleanLabel = label(run, label, where + "/label", true);
        // a request without the field is one of before the location groups: every group was a room
        String sent = room.tag;
        String tag = sent == null ? ProposalLocations.ROOM : sent;
        if (!tag.isEmpty() && !ProposalLocations.TAGS.contains(tag)) {
            run.error(BAD_TAG, where + "/tag", "This is not one of the location tags a group can get here.");
        }
        String category = room.category;
        if (category != null && !category.isEmpty() && !CheckedProposal.isCategory(category)) {
            run.error(BAD_CATEGORY, where + "/category",
                    "A category holds small letters, digits, _ and - only, at most 40; it does not start with -"
                            + " and is not true or false.");
        }
        String parent = room.parent;
        if (run.errors() != errorsBefore || cleanLabel == null) {
            return null;
        }
        // a parent that cannot be one is reported with the others, by the caller
        boolean parentUsable = parent != null && ProposalNames.isItemName(parent) && !parent.equals(name);
        return new LocationGroup(name, cleanLabel, tag.isEmpty() ? null : tag, category, parentUsable ? parent : null,
                existing);
    }

    /**
     * The UID of the existing Thing the channel lines are for: as sent, or made from an id as a Thing below this
     * bridge has it. A text that is neither is a warning and counts as none; it never stops the create steps.
     */
    private @Nullable String target(Run run, @Nullable String sent, String where) {
        if (sent == null || sent.isEmpty()) {
            return null;
        }
        if (TARGET.matcher(sent).matches()) {
            return sent;
        }
        if (ProposalNames.ID.matcher(sent).matches()) {
            return ProposalNames.thingUID(bridgeUID, sent);
        }
        run.warning(BAD_TARGET, where, "This is neither the UID of a KNX device Thing nor a Thing id.");
        return null;
    }

    private @Nullable Channel channel(Run run, ProposedChannelDTO channel, String thingUID, boolean thingIdValid,
            Set<String> channelIds, String where) {
        int errorsBefore = run.errors();
        String id = required(channel.id, where + "/id");
        String label = required(channel.label, where + "/label");
        List<ProposedPartDTO> parts = entries(channel.parts, where + "/parts", ProposedPartDTO.class);
        if (!ProposalNames.isId(id)) {
            run.error(BAD_ID, where + "/id",
                    "A channel id holds letters, digits and _ only, at most 40, and is no .things keyword.");
        } else if (!channelIds.add(id)) {
            run.error(DUPLICATE_ID, where + "/id", "Two channels of this Thing have the id " + id + ".");
        }
        String cleanLabel = label(run, label, where + "/label", false);
        String channelType = channel.channelType;
        List<String> keys = channelType == null ? List.of() : ChannelKeyTypes.keysOf(channelType);
        if (keys.isEmpty()) {
            run.error(TYPE_NOT_ALLOWED, where + "/channelType", "This channel type is not offered.");
        }
        ProposedItemDTO item = keys.isEmpty() ? channel.item : required(channel.item, where + "/item");
        if (parts.isEmpty()) {
            run.error(BAD_KEY, where + "/parts", "A channel needs at least one key.");
        }
        List<Part> made = new ArrayList<>();
        Set<String> usedKeys = new HashSet<>();
        boolean statusOnly = false;
        for (int p = 0; p < parts.size(); p++) {
            String partWhere = where + "/parts/" + p;
            ProposedPartDTO part = parts.get(p);
            String key = required(part.key, partWhere + "/key");
            String mainText = required(part.groupAddress, partWhere + "/groupAddress");
            boolean keyValid = keys.contains(key) && usedKeys.add(key);
            if (!keys.isEmpty() && !keyValid) {
                run.error(BAD_KEY, partWhere + "/key",
                        "This key is not one of the channel type, or it is there twice.");
            }
            KnxGroupAddress main = address(run, mainText, partWhere + "/groupAddress");
            if (main == null) {
                continue;
            }
            statusOnly |= facts.isStatusAddress(main);
            KnxDatapointType dataType = facts.channelDataType(main);
            if (dataType == null) {
                run.error(NO_DATA_TYPE, partWhere + "/groupAddress",
                        "The project gives " + main.address() + " no data type a channel can hold.");
            } else if (keyValid && channelType != null && !ChannelKeyTypes.works(channelType, key, dataType)) {
                run.error(TYPE_NOT_ALLOWED, partWhere + "/key",
                        "The data type of " + main.address() + " does not work on this key.");
            }
            KnxGroupAddress status = null;
            String statusText = part.statusAddress;
            boolean statusValid = true;
            if (statusText != null && !statusText.isEmpty()) {
                status = address(run, statusText, partWhere + "/statusAddress");
                if (status == null) {
                    statusValid = false;
                } else if (status.rawAddress() == main.rawAddress()) {
                    run.error(BAD_ADDRESS, partWhere + "/statusAddress",
                            "The status address is the part's own address.");
                    statusValid = false;
                } else if (!facts.sameDataType(main, status)) {
                    run.error(TYPE_NOT_ALLOWED, partWhere + "/statusAddress",
                            "The status address " + status.address() + " has another data type.");
                    statusValid = false;
                }
            }
            warnings(run, main, partWhere + "/groupAddress");
            if (status != null && statusValid) {
                warnings(run, status, partWhere + "/statusAddress");
            }
            if (dataType == null || !statusValid) {
                continue;
            }
            Part checked = part(run, key, dataType, main, status, part.read, part.statusRead, partWhere);
            if (checked != null) {
                made.add(checked);
            }
        }
        if (statusOnly) {
            run.warning(STATUS_ONLY, where, "This channel's address is a status address; openHAB may send to it.");
        }
        KnxDatapointType firstType = made.size() == parts.size() && !made.isEmpty() ? made.get(0).dataType() : null;
        Item checkedItem = item == null ? null
                : item(run, item, keys.isEmpty() ? null : channelType, firstType, thingUID + ":" + id, where + "/item");
        if (run.errors() != errorsBefore || !thingIdValid || cleanLabel == null || checkedItem == null
                || channelType == null) {
            return null;
        }
        return new Channel(thingUID + ":" + id, id, cleanLabel, channelType, made, checkedItem);
    }

    /** The part with at most one read mark, and only on an address no earlier part of the request marks. */
    private @Nullable Part part(Run run, String key, KnxDatapointType dataType, KnxGroupAddress main,
            @Nullable KnxGroupAddress status, boolean read, boolean statusRead, String where) {
        Read mark = Read.NONE;
        KnxGroupAddress readAddress = null;
        if (run.readAtStart) {
            if (statusRead && status != null) {
                mark = Read.STATUS;
                readAddress = status;
            } else if (read) {
                mark = Read.MAIN;
                readAddress = main;
            }
        }
        if (readAddress != null && !run.marked.add(readAddress.rawAddress())) {
            mark = Read.NONE;
            readAddress = null;
        }
        GroupAddressConfiguration parsed = null;
        Part part = null;
        try {
            part = new Part(key, dataType, main.address(), status == null ? null : status.address(), mark);
            parsed = GroupAddressConfiguration.parse(part.text());
        } catch (IllegalArgumentException e) {
            parsed = null;
        }
        if (part == null || parsed == null) {
            LoggerFactory.getLogger(ProposalValidator.class).warn("KNX wizard: a channel text it made does not parse");
            run.error(BAD_ADDRESS, where, "The channel text of this key does not parse.");
            return null;
        }
        if (readAddress != null && !facts.canRead(readAddress)) {
            run.warning(READ_WITHOUT_FLAG, where, "No object of the project answers a read on " + readAddress.address()
                    + "; the read telegram is still sent.");
        }
        run.telegrams += parsed.getReadGAs().size();
        for (GroupAddress address : parsed.getReadGAs()) {
            KnxGroupAddress inProject = project.groupAddress(address.getRawAddress());
            run.reads.putIfAbsent(address.getRawAddress(),
                    inProject == null ? address.toString() : inProject.address());
        }
        return part;
    }

    private @Nullable Item item(Run run, ProposedItemDTO item, @Nullable String channelType,
            @Nullable KnxDatapointType dataType, String channelUID, String where) {
        int errorsBefore = run.errors();
        String name = required(item.name, where + "/name");
        String type = required(item.type, where + "/type");
        String label = required(item.label, where + "/label");
        List<String> tags = item.tags == null ? List.of() : entries(item.tags, where + "/tags", String.class);
        List<String> groups = item.groups == null ? List.of() : entries(item.groups, where + "/groups", String.class);
        if (!ProposalNames.isItemName(name)) {
            run.error(BAD_ITEM_NAME, where + "/name",
                    "An Item name is a letter or _, then letters, digits and _, at most 80; not true or false.");
        } else if (run.groupNames.contains(name) || !run.itemNames.add(name)) {
            run.error(DUPLICATE_ITEM, where + "/name", "Two groups or Items have the name " + name + ".");
        } else if (itemExists.test(name)) {
            run.error(ITEM_EXISTS, where + "/name", "An Item " + name + " exists already.");
        }
        if (channelType != null && dataType != null
                && !ProposalItems.allowedTypes(channelType, dataType).contains(type)) {
            run.error(TYPE_NOT_ALLOWED, where + "/type", "This Item type does not fit the channel.");
        }
        String cleanLabel = label(run, label, where + "/label", true);
        String category = item.category;
        if (category != null && !category.isEmpty() && !CheckedProposal.isCategory(category)) {
            run.error(BAD_CATEGORY, where + "/category",
                    "A category holds small letters, digits, _ and - only, at most 40; it does not start with -"
                            + " and is not true or false.");
        }
        if (tags.size() > CheckedProposal.MAX_TAGS
                || !tags.stream().allMatch(tag -> CheckedProposal.TAG.matcher(tag).matches())) {
            run.error(BAD_TAG, where + "/tags",
                    "At most 10 tags, each of letters, digits, space, _ and - only, at most 40.");
        }
        for (int g = 0; g < groups.size(); g++) {
            if (!run.groupNames.contains(groups.get(g))) {
                run.error(UNKNOWN_GROUP, where + "/groups/" + g,
                        "An Item can only go into a location group of this run.");
            }
        }
        if (run.errors() != errorsBefore || cleanLabel == null) {
            return null;
        }
        return new Item(name, type, cleanLabel, category, tags, groups, channelUID);
    }

    private void warnings(Run run, KnxGroupAddress address, String where) {
        if (configuredAddresses.contains(address.rawAddress())) {
            run.warning(ALREADY_CONFIGURED, where, "A channel of this bridge already uses " + address.address() + ".");
        }
        if (address.secured() && !keyringLoaded) {
            run.warning(SECURED_WITHOUT_KEYRING, where,
                    address.address() + " is secured and the bridge has no keyring loaded.");
        }
    }

    /** The address of the project the text names; {@code null} with a problem when there is none. */
    private @Nullable KnxGroupAddress address(Run run, String text, String where) {
        int raw;
        try {
            raw = new GroupAddress(text).getRawAddress();
        } catch (KNXFormatException | IllegalArgumentException e) {
            run.error(BAD_ADDRESS, where, "This is not a group address.");
            return null;
        }
        KnxGroupAddress address = project.groupAddress(raw);
        if (address == null) {
            run.error(UNKNOWN_ADDRESS, where, "The project has no such group address.");
        }
        return address;
    }

    /** The cleaned label; {@code null} with a problem when it is too long or empty after cleaning. */
    private @Nullable String label(Run run, String label, String where, boolean item) {
        String cleaned = item ? CheckedProposal.cleanItemLabel(label) : CheckedProposal.cleanLabel(label);
        if (label.length() > MAX_LABEL_INPUT || cleaned.isEmpty()) {
            run.error(BAD_LABEL, where, "A label must not be empty or longer than 200 characters.");
            return null;
        }
        return cleaned;
    }

    private void countEntries(List<ProposedThingDTO> things) {
        if (things.size() > MAX_ENTRIES) {
            throw new BadRequest("things", "more than " + MAX_ENTRIES + " entries");
        }
        int channels = 0;
        int parts = 0;
        for (ProposedThingDTO thing : things) {
            List<ProposedChannelDTO> list = thing.channels;
            if (list == null) {
                continue;
            }
            channels += list.size();
            for (Object channel : list) {
                List<ProposedPartDTO> partList = channel instanceof ProposedChannelDTO dto ? dto.parts : null;
                if (partList != null) {
                    parts += partList.size();
                }
            }
        }
        if (channels > MAX_ENTRIES || parts > MAX_ENTRIES) {
            throw new BadRequest("things", "more than " + MAX_ENTRIES + " channels or keys in all");
        }
    }

    private static <T> T required(@Nullable T value, String where) {
        if (value == null) {
            throw new BadRequest(where, MISSING);
        }
        return value;
    }

    /** The entries of a list from JSON, which can hold {@code null} although the list type says otherwise. */
    private static <T> List<T> entries(@Nullable List<?> list, String where, Class<T> type) {
        if (list == null) {
            throw new BadRequest(where, MISSING);
        }
        if (list.size() > MAX_ENTRIES) {
            throw new BadRequest(where, "more than " + MAX_ENTRIES + " entries");
        }
        List<T> entries = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object entry = list.get(i);
            if (!type.isInstance(entry)) {
                throw new BadRequest(where + "/" + i, NOT_AN_ENTRY);
            }
            entries.add(type.cast(entry));
        }
        return entries;
    }
}

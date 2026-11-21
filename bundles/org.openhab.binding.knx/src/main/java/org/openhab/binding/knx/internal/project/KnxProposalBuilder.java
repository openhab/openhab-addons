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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.AddressTypes.AddressType;
import org.openhab.binding.knx.internal.project.ChannelTypeTable.Row;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.LeftOut;
import org.openhab.binding.knx.internal.project.KnxProposal.Pairing;
import org.openhab.binding.knx.internal.project.KnxProposal.Part;
import org.openhab.binding.knx.internal.project.KnxProposal.Room;
import org.openhab.binding.knx.internal.project.KnxProposal.Source;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;

/**
 * Makes the wizard's proposal for a selection of a project: which Things, which channels, which command goes with
 * which status address and which address is read at start. It reads the project only; it knows no registry and
 * sets no setting of a device Thing.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class KnxProposalBuilder {

    public static final String LOOSE_ADDRESSES_LABEL = "KNX";
    public static final String NOTE_NO_CHANNEL_TYPE = "no channel type works for this data type";
    public static final String NOTE_ALREADY_CONFIGURED = "a channel of this bridge already uses this address";
    public static final String NOTE_STEP_STOP = "step/stop: not a stopMove type";

    private static final String GA = "ga";
    private static final String INFO = "Info";
    private static final int MAX_CANDIDATES = 20;
    private static final int MIN_NAME_LENGTH = 4;
    // one list for all languages; it only helps to choose among candidates the objects already gave
    private static final Set<String> STATUS_WORDS = Set.of("status", "state", "feedback", "info", "fb", "rm",
            "rückmeldung", "rueckmeldung", "zustand", "terugmelding", "terugmeld", "retour", "état", "etat", "stato",
            "estado");
    private static final Pattern INDIVIDUAL_ADDRESS = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,3})");

    /**
     * What the user ticked.
     *
     * @param groupAddresses raw group addresses
     * @param functions function ids
     * @param spaces space ids
     * @param devices device ids or individual addresses ({@code 7.7.1})
     * @param readAtStart false: no read mark at all
     */
    public record Selection(List<Integer> groupAddresses, List<String> functions, List<String> spaces,
            List<String> devices, boolean readAtStart) {

        public Selection {
            groupAddresses = List.copyOf(groupAddresses);
            functions = List.copyOf(functions);
            spaces = List.copyOf(spaces);
            devices = List.copyOf(devices);
        }
    }

    /** A function found by the walk of the building tree, with the space that holds it. */
    private record PlacedFunction(KnxFunction function, KnxSpace space) {
    }

    /** An address of a Thing with the channel label it gets there and its role in the Thing's function. */
    private record Entry(KnxGroupAddress address, String label, @Nullable String role) {
    }

    private record Draft(String label, Source source, String sourceId, @Nullable KnxSpace space,
            @Nullable KnxFunction function, @Nullable KnxDevice device, List<Entry> entries) {
    }

    /** The status address a rule chose for a command, or the candidates when none was chosen. */
    private record Pair(@Nullable KnxGroupAddress status, Pairing pairing, List<KnxGroupAddress> candidates) {
    }

    private static final Pair UNPAIRED = new Pair(null, Pairing.NONE, List.of());

    /** A key of a combined channel: how many commands of the data type it takes. No key: none may be there. */
    private record Slot(@Nullable String key, String dataType, int min, int max) {
    }

    private record Combination(String channelType, List<Slot> slots) {
    }

    // tried in this order on the commands of one function or device channel; color first, as its parts would
    // also make a dimmer
    private static final List<Combination> COMBINATIONS = List.of( //
            new Combination("color",
                    List.of(new Slot("hsb", "232.600", 1, 1), new Slot("switch", "1.001", 0, 1),
                            new Slot("position", "5.001", 0, 1), new Slot("increaseDecrease", "3.007", 0, 1))),
            new Combination("rollershutter",
                    List.of(new Slot("upDown", "1.008", 1, 1), new Slot("stopMove", "1.010", 0, 1),
                            new Slot("position", "5.001", 0, 1))),
            new Combination("dimmer", List.of(new Slot("switch", "1.001", 1, 1), new Slot("position", "5.001", 1, 1),
                    new Slot("increaseDecrease", "3.007", 0, 1), new Slot(null, "1.008", 0, 0))));
    private static final String STEP_STOP = "1.007";

    /** A device channel: the channel and the module instance of a device's objects. */
    private record DeviceChannel(String channelId, @Nullable String moduleInstanceId) {
    }

    private record PartDraft(String key, KnxGroupAddress main, AddressType type, Pair pair) {
    }

    private record ChannelDraft(@Nullable String channelType, List<String> allowed, String label, boolean statusOnly,
            List<PartDraft> parts, List<ChannelDraft> split, List<String> notes, boolean deselected) {

        int firstRaw() {
            return parts.stream().mapToInt(p -> p.main().rawAddress()).min().orElse(0);
        }
    }

    private final KnxProject project;
    private final KnxChannelMapper mapper;
    private final List<PlacedFunction> functions;
    private final Map<Integer, AddressType> types = new HashMap<>();

    public KnxProposalBuilder(KnxProject project) {
        this.project = project;
        this.mapper = new KnxChannelMapper(project);
        this.functions = walk();
    }

    /**
     * @param configuredAddresses the raw addresses the channels of the bridge use already
     */
    public KnxProposal propose(Selection selection, Set<Integer> configuredAddresses) {
        List<PlacedFunction> walk = functions;
        Set<String> unknown = new LinkedHashSet<>();

        List<KnxGroupAddress> loose = new ArrayList<>();
        for (int raw : selection.groupAddresses()) {
            KnxGroupAddress address = project.groupAddress(raw);
            if (address == null) {
                unknown.add(project.info().groupAddressStyle().format(raw));
            } else {
                loose.add(address);
            }
        }
        Set<String> functionIds = new HashSet<>();
        for (String id : selection.functions()) {
            if (walk.stream().anyMatch(p -> p.function().id().equals(id))) {
                functionIds.add(id);
            } else {
                unknown.add(id);
            }
        }
        Set<String> deviceIds = new HashSet<>();
        for (String id : selection.spaces()) {
            KnxSpace space = findSpace(id);
            if (space == null) {
                unknown.add(id);
                continue;
            }
            List<KnxSpace> below = subtree(space);
            if (below.stream().anyMatch(s -> !s.functions().isEmpty())) {
                below.forEach(s -> s.functions().forEach(f -> functionIds.add(f.id())));
            } else {
                below.forEach(s -> project.devicesIn(s).forEach(d -> deviceIds.add(d.id())));
            }
        }
        for (String text : selection.devices()) {
            KnxDevice device = findDevice(text);
            if (device == null) {
                unknown.add(text);
            } else {
                deviceIds.add(device.id());
            }
        }

        List<Draft> drafts = new ArrayList<>();
        Set<String> placed = new HashSet<>();
        for (PlacedFunction function : walk) {
            if (functionIds.contains(function.function().id()) && placed.add(function.function().id())) {
                drafts.add(functionDraft(function));
            }
        }
        for (KnxDevice device : project.topology().devices()) {
            if (deviceIds.contains(device.id()) && placed.add(device.id())) {
                drafts.add(deviceDraft(device));
            }
        }
        if (!loose.isEmpty()) {
            List<Entry> entries = loose.stream().map(a -> new Entry(a, nameOrAddress(a.name(), a), null)).toList();
            drafts.add(new Draft(LOOSE_ADDRESSES_LABEL, Source.ADDRESSES, "", null, null, null, entries));
        }
        drafts = withoutDoubles(drafts);

        Map<Integer, Pair> pairs = new HashMap<>();
        Set<Integer> usedStatus = new HashSet<>();
        for (Draft draft : drafts) {
            for (Entry entry : draft.entries()) {
                if (!isStatus(entry.address())) {
                    Pair pair = pair(draft, entry);
                    pairs.put(entry.address().rawAddress(), pair);
                    KnxGroupAddress status = pair.status();
                    if (status != null) {
                        usedStatus.add(status.rawAddress());
                    }
                }
            }
        }

        Set<Integer> marked = new HashSet<>();
        List<Thing> things = new ArrayList<>();
        Set<String> placedIn = new HashSet<>();
        List<LeftOut> leftOut = new ArrayList<>();
        for (Draft draft : drafts) {
            List<ChannelDraft> channels = new ArrayList<>();
            for (Entry entry : draft.entries()) {
                KnxGroupAddress address = entry.address();
                if (usedStatus.contains(address.rawAddress())) {
                    continue;
                }
                boolean status = isStatus(address);
                Pair pair = status ? UNPAIRED : pairs.getOrDefault(address.rawAddress(), UNPAIRED);
                channels.add(single(address, entry.label(), status, pair));
            }
            channels = combine(draft, channels);
            channels.sort(Comparator.comparingInt(ChannelDraft::firstRaw));
            List<Channel> made = new ArrayList<>();
            for (ChannelDraft channel : channels) {
                made.add(channel(channel, marked, configuredAddresses, selection.readAtStart()));
            }
            if (made.isEmpty()) {
                if (draft.source() != Source.ADDRESSES) {
                    leftOut.add(new LeftOut(draft.source(), draft.sourceId()));
                }
                continue;
            }
            KnxSpace space = draft.space();
            if (space != null) {
                placedIn.add(space.id());
            }
            things.add(new Thing(draft.label(), draft.source(), draft.sourceId(), space == null ? null : space.id(),
                    space == null ? null : space.name(), made));
        }
        return new KnxProposal(things, rooms(placedIn), List.copyOf(unknown), leftOut, selection.readAtStart());
    }

    /** The spaces with these ids and every space above one of them, a space before the spaces inside it. */
    private List<Room> rooms(Set<String> placed) {
        List<KnxSpace> walk = spacesInWalkOrder(project.locations());
        Map<String, String> parentOf = new HashMap<>();
        for (KnxSpace space : walk) {
            for (KnxSpace inside : space.spaces()) {
                parentOf.putIfAbsent(inside.id(), space.id());
            }
        }
        Set<String> wanted = new HashSet<>();
        for (String id : placed) {
            @Nullable
            String up = id;
            while (up != null && wanted.add(up)) {
                up = parentOf.get(up);
            }
        }
        List<Room> rooms = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (KnxSpace space : walk) {
            if (wanted.contains(space.id()) && seen.add(space.id())) {
                rooms.add(new Room(space.id(), space.name(), space.type(), parentOf.get(space.id())));
            }
        }
        return rooms;
    }

    /**
     * The data type a channel text of the address holds, as the proposal writes it; {@code null} when the project
     * gives none with a sub number in the range the text can hold.
     */
    public @Nullable KnxDatapointType channelDataType(KnxGroupAddress address) {
        AddressType type = type(address);
        return type.writable() ? type.type() : null;
    }

    /** Whether the status address has the command's data type, by the rule the pairing uses. */
    public boolean sameDataType(KnxGroupAddress command, KnxGroupAddress status) {
        KnxDatapointType one = type(command).type();
        KnxDatapointType other = type(status).type();
        return one != null && other != null && sameType(one, other);
    }

    /** Whether the address is a status address: the sending address of an object with read and transmit, no write. */
    public boolean isStatusAddress(KnxGroupAddress address) {
        return isStatus(address);
    }

    /** Whether an object linked to the address answers a read, as the mapper decides it. */
    public boolean canRead(KnxGroupAddress address) {
        return mapper.canRead(address);
    }

    /** The functions in the order of a depth-first walk of the building tree, each once. */
    private List<PlacedFunction> walk() {
        List<PlacedFunction> found = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (KnxSpace space : spacesInWalkOrder(project.locations())) {
            for (KnxFunction function : space.functions()) {
                if (seen.add(function.id())) {
                    found.add(new PlacedFunction(function, space));
                }
            }
        }
        return found;
    }

    private static List<KnxSpace> spacesInWalkOrder(List<KnxSpace> top) {
        List<KnxSpace> spaces = new ArrayList<>();
        Deque<KnxSpace> pending = new ArrayDeque<>();
        for (int i = top.size() - 1; i >= 0; i--) {
            pending.push(top.get(i));
        }
        while (!pending.isEmpty()) {
            KnxSpace space = pending.pop();
            spaces.add(space);
            for (int i = space.spaces().size() - 1; i >= 0; i--) {
                pending.push(space.spaces().get(i));
            }
        }
        return spaces;
    }

    private @Nullable KnxSpace findSpace(String id) {
        for (KnxSpace space : spacesInWalkOrder(project.locations())) {
            if (space.id().equals(id)) {
                return space;
            }
        }
        return null;
    }

    private static List<KnxSpace> subtree(KnxSpace space) {
        return spacesInWalkOrder(List.of(space));
    }

    private @Nullable KnxDevice findDevice(String text) {
        Matcher matcher = INDIVIDUAL_ADDRESS.matcher(text);
        if (matcher.matches()) {
            int area = Integer.parseInt(matcher.group(1));
            int line = Integer.parseInt(matcher.group(2));
            int number = Integer.parseInt(matcher.group(3));
            if (area <= 15 && line <= 15 && number <= 255) {
                return project.device(new KnxIndividualAddress(area, line, number));
            }
            return null;
        }
        return project.deviceById(text);
    }

    private Draft functionDraft(PlacedFunction placed) {
        KnxFunction function = placed.function();
        List<Entry> entries = new ArrayList<>();
        for (KnxFunctionGroupAddress ref : function.groupAddresses()) {
            KnxGroupAddress address = project.groupAddress(ref.groupAddressId());
            if (address != null) {
                String label = ref.name().isBlank() ? nameOrAddress(address.name(), address) : ref.name();
                entries.add(new Entry(address, label, ref.role()));
            }
        }
        String label = function.name().isBlank() ? function.id() : function.name();
        return new Draft(label, Source.FUNCTION, function.id(), placed.space(), function, null, entries);
    }

    private Draft deviceDraft(KnxDevice device) {
        Map<Integer, Entry> entries = new LinkedHashMap<>();
        List<KnxGroupObject> objects = project.groupObjectsOf(device);
        for (KnxGroupObject object : objects) {
            String sending = object.sendingGroupAddressId();
            KnxGroupAddress address = sending == null ? null : project.groupAddress(sending);
            if (address != null) {
                entries.putIfAbsent(address.rawAddress(),
                        new Entry(address, nameOrAddress(KnxChannelMapper.label(object), address), null));
            }
        }
        for (KnxGroupObject object : objects) {
            for (String id : object.groupAddressIds()) {
                KnxGroupAddress address = project.groupAddress(id);
                if (address != null) {
                    entries.putIfAbsent(address.rawAddress(),
                            new Entry(address, nameOrAddress(KnxChannelMapper.label(object), address), null));
                }
            }
        }
        String label = project.displayNameOf(device);
        if (label.isBlank()) {
            KnxIndividualAddress individual = device.individualAddress();
            label = individual != null ? individual.toString() : device.id();
        }
        return new Draft(label, Source.DEVICE, device.id(), project.spaceOf(device), null, device,
                List.copyOf(entries.values()));
    }

    private static String nameOrAddress(String name, KnxGroupAddress address) {
        return name.isBlank() ? address.address() : name;
    }

    /** The drafts with each address only in the first draft that holds it, and once there. */
    private static List<Draft> withoutDoubles(List<Draft> drafts) {
        Set<Integer> taken = new HashSet<>();
        List<Draft> kept = new ArrayList<>(drafts.size());
        for (Draft draft : drafts) {
            List<Entry> entries = draft.entries().stream().filter(e -> taken.add(e.address().rawAddress())).toList();
            kept.add(new Draft(draft.label(), draft.source(), draft.sourceId(), draft.space(), draft.function(),
                    draft.device(), entries));
        }
        return kept;
    }

    private AddressType type(KnxGroupAddress address) {
        AddressType type = types.get(address.rawAddress());
        if (type == null) {
            type = AddressTypes.resolve(project, address);
            types.put(address.rawAddress(), type);
        }
        return type;
    }

    private boolean isStatus(KnxGroupAddress address) {
        return mapper.isStatus(address);
    }

    /**
     * The status address for an address that is not a status address itself: the first rule that gives exactly
     * one wins. Rules 2 to 4 hold only for a command address.
     */
    private Pair pair(Draft draft, Entry entry) {
        KnxGroupAddress command = entry.address();
        KnxDatapointType type = type(command).type();
        if (type == null) {
            return UNPAIRED;
        }
        KnxGroupAddress byRole = byRole(draft, entry, type);
        if (byRole != null) {
            return new Pair(byRole, Pairing.ROLE, List.of());
        }
        if (!isCommand(command)) {
            return UNPAIRED;
        }
        List<KnxGroupObject> receivers = receivers(command);

        Map<Integer, KnxGroupAddress> sameChannel = new TreeMap<>();
        Map<Integer, KnxGroupAddress> sameName = new TreeMap<>();
        for (KnxGroupObject receiver : receivers) {
            if (receiver.channelId() == null) {
                continue;
            }
            for (KnxGroupObject object : objectsOfDevice(receiver.deviceId())) {
                if (Objects.equals(receiver.channelId(), object.channelId())
                        && Objects.equals(receiver.moduleInstanceId(), object.moduleInstanceId())) {
                    KnxGroupAddress status = statusSentBy(object, command, type);
                    if (status != null) {
                        sameChannel.put(status.rawAddress(), status);
                        if (namesMatch(receiver.name(), object.name())) {
                            sameName.put(status.rawAddress(), status);
                        }
                    }
                }
            }
        }
        if (sameChannel.size() == 1) {
            return new Pair(sameChannel.values().iterator().next(), Pairing.OBJECTS, List.of());
        }
        if (sameName.size() == 1) {
            return new Pair(sameName.values().iterator().next(), Pairing.OBJECTS, List.of());
        }

        Map<Integer, KnxGroupAddress> input = sameChannel;
        if (input.isEmpty()) {
            Set<String> devices = new LinkedHashSet<>();
            receivers.forEach(r -> devices.add(r.deviceId()));
            for (String device : devices) {
                for (KnxGroupObject object : objectsOfDevice(device)) {
                    KnxGroupAddress status = statusSentBy(object, command, type);
                    if (status != null) {
                        input.put(status.rawAddress(), status);
                    }
                }
            }
        }

        Set<Integer> inFunctions = new HashSet<>();
        KnxFunction own = draft.function();
        if (own != null) {
            inFunctions.addAll(rawAddresses(own));
        } else {
            for (PlacedFunction placed : functions) {
                Set<Integer> addresses = rawAddresses(placed.function());
                if (addresses.contains(command.rawAddress())) {
                    inFunctions.addAll(addresses);
                }
            }
        }
        List<KnxGroupAddress> byFunction = input.values().stream().filter(a -> inFunctions.contains(a.rawAddress()))
                .toList();
        if (byFunction.size() == 1) {
            return new Pair(byFunction.get(0), Pairing.FUNCTION, List.of());
        }

        List<KnxGroupAddress> byName = input.values().stream().filter(a -> namesMatch(command.name(), a.name()))
                .toList();
        if (byName.size() == 1) {
            return new Pair(byName.get(0), Pairing.NAMES, List.of());
        }
        return new Pair(null, Pairing.NONE, input.values().stream().limit(MAX_CANDIDATES).toList());
    }

    /** Rule 1: the one other address of the Thing's function whose role is {@code Info} and the end of this one's. */
    private @Nullable KnxGroupAddress byRole(Draft draft, Entry entry, KnxDatapointType type) {
        KnxFunction function = draft.function();
        String role = entry.role();
        if (function == null || role == null || role.startsWith(INFO)) {
            return null;
        }
        Map<Integer, KnxGroupAddress> partners = new TreeMap<>();
        for (KnxFunctionGroupAddress ref : function.groupAddresses()) {
            String other = ref.role();
            KnxGroupAddress address = project.groupAddress(ref.groupAddressId());
            if (other == null || address == null || address.rawAddress() == entry.address().rawAddress()
                    || !other.startsWith(INFO)) {
                continue;
            }
            String rest = other.substring(INFO.length());
            KnxDatapointType partnerType = type(address).type();
            if (!rest.isEmpty() && role.endsWith(rest) && partnerType != null && sameType(type, partnerType)) {
                partners.put(address.rawAddress(), address);
            }
        }
        return partners.size() == 1 ? partners.values().iterator().next() : null;
    }

    /** Not a status address, and a linked object writes on it that is not one with read and transmit. */
    private boolean isCommand(KnxGroupAddress address) {
        if (isStatus(address)) {
            return false;
        }
        for (KnxGroupObject object : project.groupObjectsLinkedTo(address)) {
            KnxGroupObjectFlags flags = object.flags();
            if (flags.write() && !(flags.read() && flags.transmit())) {
                return true;
            }
        }
        return false;
    }

    /** The objects linked to a command with write and without transmit; only when there is none, those with both. */
    private List<KnxGroupObject> receivers(KnxGroupAddress command) {
        List<KnxGroupObject> listening = new ArrayList<>();
        List<KnxGroupObject> sending = new ArrayList<>();
        for (KnxGroupObject object : project.groupObjectsLinkedTo(command)) {
            if (object.flags().write()) {
                (object.flags().transmit() ? sending : listening).add(object);
            }
        }
        return listening.isEmpty() ? sending : listening;
    }

    private List<KnxGroupObject> objectsOfDevice(String deviceId) {
        KnxDevice device = project.deviceById(deviceId);
        return device == null ? List.of() : project.groupObjectsOf(device);
    }

    /** The address the object sends on when it is a status address of the command's type, other than the command. */
    private @Nullable KnxGroupAddress statusSentBy(KnxGroupObject object, KnxGroupAddress command,
            KnxDatapointType type) {
        String sending = object.sendingGroupAddressId();
        KnxGroupAddress address = sending == null ? null : project.groupAddress(sending);
        if (address == null || address.rawAddress() == command.rawAddress() || !isStatus(address)) {
            return null;
        }
        KnxDatapointType other = type(address).type();
        return other != null && sameType(type, other) ? address : null;
    }

    private Set<Integer> rawAddresses(KnxFunction function) {
        Set<Integer> addresses = new HashSet<>();
        for (KnxFunctionGroupAddress ref : function.groupAddresses()) {
            KnxGroupAddress address = project.groupAddress(ref.groupAddressId());
            if (address != null) {
                addresses.add(address.rawAddress());
            }
        }
        return addresses;
    }

    /** Main and sub number equal; the main numbers when one has no sub number. */
    private static boolean sameType(KnxDatapointType one, KnxDatapointType other) {
        Integer sub = one.sub();
        Integer otherSub = other.sub();
        return one.main() == other.main() && (sub == null || otherSub == null || sub.equals(otherSub));
    }

    /**
     * Whether two names mean the same thing apart from a status word: lower-cased, split at every character that
     * is not a letter or digit, status words dropped; then equal, or one holds the other and the shorter has at
     * least four characters. Both must keep something.
     */
    static boolean namesMatch(String one, String other) {
        String a = withoutStatusWords(one);
        String b = withoutStatusWords(other);
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        if (a.equals(b)) {
            return true;
        }
        boolean aShorter = a.length() <= b.length();
        String shorter = aShorter ? a : b;
        String longer = aShorter ? b : a;
        return longer.contains(shorter) && shorter.codePointCount(0, shorter.length()) >= MIN_NAME_LENGTH;
    }

    private static String withoutStatusWords(String name) {
        StringBuilder spaced = new StringBuilder(name.length());
        name.toLowerCase(Locale.ROOT).codePoints()
                .forEach(c -> spaced.appendCodePoint(Character.isLetterOrDigit(c) ? c : ' '));
        List<String> kept = new ArrayList<>();
        for (String token : spaced.toString().split(" ")) {
            if (!token.isEmpty() && !STATUS_WORDS.contains(token)) {
                kept.add(token);
            }
        }
        return String.join(" ", kept);
    }

    /**
     * The channels of a function or a device Thing with the commands of one function, or of one device channel,
     * combined where a combination fits them exactly.
     */
    private List<ChannelDraft> combine(Draft draft, List<ChannelDraft> singles) {
        KnxDevice device = draft.device();
        if (draft.source() == Source.ADDRESSES) {
            return singles;
        }
        Map<DeviceChannel, List<ChannelDraft>> groups = new LinkedHashMap<>();
        for (ChannelDraft single : singles) {
            KnxGroupAddress main = single.parts().get(0).main();
            AddressType type = single.parts().get(0).type();
            if (single.statusOnly() || !type.writable() || type.type() == null || !isCommand(main)) {
                continue;
            }
            DeviceChannel group = device == null ? new DeviceChannel("", null) : deviceChannel(device, main);
            if (group != null) {
                List<ChannelDraft> members = groups.get(group);
                if (members == null) {
                    members = new ArrayList<>();
                    groups.put(group, members);
                }
                members.add(single);
            }
        }
        List<ChannelDraft> result = new ArrayList<>(singles);
        for (List<ChannelDraft> group : groups.values()) {
            List<ChannelDraft> rest = new ArrayList<>(group);
            boolean shutter = false;
            for (Combination combination : COMBINATIONS) {
                List<ChannelDraft> parts = fit(combination, rest);
                if (parts == null) {
                    continue;
                }
                shutter |= "rollershutter".equals(combination.channelType());
                if (parts.size() > 1) {
                    rest.removeAll(parts);
                    int at = result.indexOf(parts.get(0));
                    result.removeAll(parts);
                    result.add(Math.min(at, result.size()), combined(combination, parts));
                }
            }
            if (shutter) {
                for (ChannelDraft single : rest) {
                    if (STEP_STOP.equals(String.valueOf(single.parts().get(0).type().type()))) {
                        List<String> notes = new ArrayList<>(single.notes());
                        notes.add(NOTE_STEP_STOP);
                        result.set(result.indexOf(single), new ChannelDraft(single.channelType(), single.allowed(),
                                single.label(), false, single.parts(), single.split(), notes, true));
                    }
                }
            }
        }
        return result;
    }

    /** The commands that fill the combination's keys in key order; {@code null} when a count does not fit. */
    private static @Nullable List<ChannelDraft> fit(Combination combination, List<ChannelDraft> commands) {
        List<ChannelDraft> parts = new ArrayList<>();
        for (Slot slot : combination.slots()) {
            List<ChannelDraft> ofType = commands.stream()
                    .filter(c -> slot.dataType().equals(String.valueOf(c.parts().get(0).type().type()))).toList();
            if (ofType.size() < slot.min() || ofType.size() > slot.max()) {
                return null;
            }
            if (slot.key() != null && ofType.size() == 1) {
                parts.add(ofType.get(0));
            }
        }
        return parts;
    }

    private ChannelDraft combined(Combination combination, List<ChannelDraft> singles) {
        List<PartDraft> parts = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        for (ChannelDraft single : singles) {
            PartDraft part = single.parts().get(0);
            String key = combination.slots().stream()
                    .filter(s -> s.dataType().equals(String.valueOf(part.type().type()))).map(Slot::key)
                    .filter(Objects::nonNull).findFirst().orElseThrow();
            parts.add(new PartDraft(key, part.main(), part.type(), part.pair()));
            notes.addAll(part.type().notes());
        }
        return new ChannelDraft(combination.channelType(), List.of(combination.channelType()), singles.get(0).label(),
                false, parts, singles, notes, false);
    }

    /** The one device channel of the device's writing objects on the address; {@code null} when not exactly one. */
    private @Nullable DeviceChannel deviceChannel(KnxDevice device, KnxGroupAddress address) {
        Set<DeviceChannel> found = new LinkedHashSet<>();
        for (KnxGroupObject object : project.groupObjectsLinkedTo(address)) {
            if (object.deviceId().equals(device.id()) && object.flags().write()) {
                String channelId = object.channelId();
                if (channelId == null) {
                    return null;
                }
                found.add(new DeviceChannel(channelId, object.moduleInstanceId()));
            }
        }
        return found.size() == 1 ? found.iterator().next() : null;
    }

    private ChannelDraft single(KnxGroupAddress address, String label, boolean statusOnly, Pair pair) {
        AddressType type = type(address);
        KnxDatapointType dataType = type.type();
        String channelType = null;
        String key = GA;
        List<String> allowed = List.of();
        if (type.writable() && dataType != null) {
            Row row = ChannelTypeTable.lookup(dataType);
            if (row != null && !GA.equals(row.key())) {
                allowed = List.of(row.channelType());
            } else {
                allowed = ChannelKeyTypes.singleKeyChannelTypesFor(dataType);
            }
            if (row != null) {
                channelType = row.channelType();
                key = row.key();
            } else if (!allowed.isEmpty()) {
                channelType = allowed.get(0);
            }
        }
        List<String> notes = new ArrayList<>(type.notes());
        if (type.writable() && dataType != null && channelType == null) {
            notes.add(NOTE_NO_CHANNEL_TYPE);
        }
        return new ChannelDraft(channelType, allowed, label, statusOnly,
                List.of(new PartDraft(key, address, type, pair)), List.of(), notes, channelType == null);
    }

    private Channel channel(ChannelDraft draft, Set<Integer> marked, Set<Integer> configured, boolean readAtStart) {
        List<Channel> split = new ArrayList<>();
        Set<Integer> markedWhenSplit = new HashSet<>(marked);
        for (ChannelDraft part : draft.split()) {
            split.add(channel(part, markedWhenSplit, configured, readAtStart));
        }
        List<Part> parts = new ArrayList<>();
        Set<KnxGroupAddress> used = new LinkedHashSet<>();
        for (PartDraft part : draft.parts()) {
            parts.add(part(part, marked, readAtStart));
            if (configured.contains(part.main().rawAddress())) {
                used.add(part.main());
            }
            KnxGroupAddress status = part.pair().status();
            if (status != null && configured.contains(status.rawAddress())) {
                used.add(status);
            }
        }
        List<String> notes = new ArrayList<>(new LinkedHashSet<>(draft.notes()));
        if (!used.isEmpty()) {
            notes.add(NOTE_ALREADY_CONFIGURED);
        }
        boolean selected = !draft.deselected() && used.isEmpty();
        return new Channel(draft.channelType(), draft.allowed(), draft.label(), selected, draft.statusOnly(), parts,
                split, List.copyOf(used), notes);
    }

    /** The part with at most one read mark, and only on an address no earlier channel marks. */
    private Part part(PartDraft draft, Set<Integer> marked, boolean readAtStart) {
        KnxGroupAddress main = draft.main();
        Pair pair = draft.pair();
        KnxGroupAddress status = pair.status();
        boolean mainRead = false;
        boolean statusRead = false;
        if (readAtStart) {
            if (status != null && mapper.canRead(status)) {
                statusRead = marked.add(status.rawAddress());
            } else if (mapper.canRead(main)) {
                mainRead = marked.add(main.rawAddress());
            }
        }
        StringBuilder text = new StringBuilder();
        KnxDatapointType dataType = draft.type().type();
        if (draft.type().writable() && dataType != null) {
            text.append(dataType).append(':');
        }
        text.append(mainRead ? "<" : "").append(main.address());
        if (status != null) {
            text.append('+').append(statusRead ? "<" : "").append(status.address());
        }
        return new Part(draft.key(), dataType, main, mainRead, status, statusRead, pair.pairing(), pair.candidates(),
                text.toString());
    }
}

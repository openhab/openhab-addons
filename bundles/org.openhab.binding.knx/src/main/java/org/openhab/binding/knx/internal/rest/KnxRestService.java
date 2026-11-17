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

import java.io.IOException;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.monitor.TelegramFilter;
import org.openhab.binding.knx.internal.monitor.TelegramFilterText;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.project.KnxChannelMapper;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectCheck;
import org.openhab.binding.knx.internal.project.KnxProposal;
import org.openhab.binding.knx.internal.project.KnxProposalBuilder;
import org.openhab.binding.knx.internal.projectstore.ConfiguredAddresses;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.FileState;
import org.openhab.binding.knx.internal.projectstore.ProjectNames;
import org.openhab.binding.knx.internal.projectstore.StoredProject;
import org.openhab.binding.knx.internal.rest.dto.BridgeDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelSuggestionDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelSuggestionsDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelUseDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.DevicePageDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceThingDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressPageDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringDTO;
import org.openhab.binding.knx.internal.rest.dto.MonitorDTO;
import org.openhab.binding.knx.internal.rest.dto.PreviewRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProjectCheckDTO;
import org.openhab.binding.knx.internal.rest.dto.ProjectDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalPreviewDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.SpaceDTO;
import org.openhab.binding.knx.internal.rest.dto.TelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.TelegramPageDTO;
import org.openhab.binding.knx.internal.rest.dto.TopologyDTO;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.wizard.ProposalNames;
import org.openhab.binding.knx.internal.wizard.ProposalValidator;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;

/**
 * What the REST endpoints do, one public method per endpoint, without any JAX-RS type. Every parameter comes as
 * the text the caller sent, or null, and is read here; a failure is a {@link KnxRestException}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxRestService {
    /** A bridge UID this add-on can own: {@code knx:ip:<id>} or {@code knx:serial:<id>}. */
    private static final Pattern BRIDGE_UID = Pattern.compile("knx:(ip|serial):[\\w-]{1,200}");
    private static final Pattern DEVICE_ADDRESS = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,3})");
    private static final Pattern COUNT = Pattern.compile("\\d{1,9}");
    private static final Pattern NUMBER = Pattern.compile("\\d{1,18}");
    /** The monitor filters a query takes, read by {@link TelegramFilterText}. */
    static final List<String> FILTER_KEYS = List.of("src", "dst", "type", "dir", "dpt", "from", "to", "context");
    static final int TELEGRAMS_DEFAULT = 200;
    static final int TELEGRAMS_MAX = 5000;
    static final int PAGE_DEFAULT = 1000;
    static final int PAGE_MAX = 10000;
    static final int CHANNEL_ADDRESSES_MAX = 2000;
    private static final String TIME_TEXT = "not a time such as -10m, 14:05:30, 2026-11-14T14:05 or an instant ending in Z";
    private static final String GROUP_ADDRESSES = "groupAddresses";
    private static final String SELECTION = "selection";

    private final KNXHandlerFactory factory;
    private final KnxProjectStore store;
    private final ThingRegistry thingRegistry;
    private final ItemRegistry itemRegistry;
    private final ItemChannelLinkRegistry linkRegistry;
    private final Clock clock;
    private final ZoneId zone;
    private final GroupTelegramSender sender;

    KnxRestService(KNXHandlerFactory factory, KnxProjectStore store, ThingRegistry thingRegistry,
            ItemRegistry itemRegistry, ItemChannelLinkRegistry linkRegistry) {
        this(factory, store, thingRegistry, itemRegistry, linkRegistry, Clock.systemUTC(), ZoneId.systemDefault());
    }

    KnxRestService(KNXHandlerFactory factory, KnxProjectStore store, ThingRegistry thingRegistry,
            ItemRegistry itemRegistry, ItemChannelLinkRegistry linkRegistry, Clock clock, ZoneId zone) {
        this.factory = factory;
        this.store = store;
        this.thingRegistry = thingRegistry;
        this.itemRegistry = itemRegistry;
        this.linkRegistry = linkRegistry;
        this.clock = clock;
        this.zone = zone;
        this.sender = new GroupTelegramSender(clock);
    }

    /**
     * Number 24: sends one group read or one group write through the bridge's client, when the bridge's setting
     * {@code groupMonitorSend} allows it.
     */
    public GroupTelegramDTO groupTelegram(@Nullable String bridge, @Nullable GroupTelegramRequestDTO request) {
        KNXBridgeBaseThingHandler handler = handler(bridge);
        ThingUID uid = handler.getThing().getUID();
        return sender.send(handler, request, address -> {
            StoredProject stored = store.fileState(uid) == FileState.LOADED ? store.get(uid) : null;
            KnxGroupAddress known = stored == null ? null : stored.project().groupAddress(address.getRawAddress());
            KnxDatapointType type = known == null ? null : known.datapointType();
            return type == null || type.sub() == null ? null : type.toString();
        });
    }

    /** Number 1: the bridges with a handler in UID order, then the stored projects of bridges without one. */
    public List<BridgeDTO> bridges() {
        List<KNXBridgeBaseThingHandler> handlers = new ArrayList<>(factory.getBridges());
        handlers.sort(Comparator.comparing(handler -> handler.getThing().getUID().getAsString()));
        List<BridgeDTO> result = new ArrayList<>();
        Set<ThingUID> present = new HashSet<>();
        for (KNXBridgeBaseThingHandler handler : handlers) {
            ThingUID uid = handler.getThing().getUID();
            present.add(uid);
            FileState state = store.fileState(uid);
            result.add(KnxDtoMapper.bridge(handler, state, store.get(uid)));
        }
        for (ThingUID uid : store.stored()) {
            if (!present.contains(uid)) {
                FileState state = store.fileState(uid);
                result.add(KnxDtoMapper.storedBridge(uid, state, store.get(uid)));
            }
        }
        return result;
    }

    /** Number 2: a bridge with a handler, or one whose stored project is still there. */
    public BridgeDTO bridge(@Nullable String bridge) {
        ThingUID uid = bridgeUid(bridge);
        KNXBridgeBaseThingHandler handler = findHandler(uid);
        if (handler != null) {
            return KnxDtoMapper.bridge(handler, store.fileState(uid), store.get(uid));
        }
        if (!store.stored().contains(uid)) {
            throw new KnxRestException(KnxRestError.UNKNOWN_BRIDGE);
        }
        return KnxDtoMapper.storedBridge(uid, store.fileState(uid), store.get(uid));
    }

    /** Number 8: the state of the stored project; always an answer for a bridge UID of this add-on. */
    public ProjectDTO project(@Nullable String bridge) {
        ThingUID uid = bridgeUid(bridge);
        return KnxDtoMapper.project(store.fileState(uid), store.get(uid));
    }

    /** Number 3: the header of the group monitor. */
    public MonitorDTO monitor(@Nullable String bridge) {
        TelegramMonitor monitor = handler(bridge).getTelegramMonitor();
        return KnxDtoMapper.monitor(monitor, monitor.lastNumber());
    }

    /**
     * Number 4: rows of the group monitor, oldest first; the newest ones, or after a row number the oldest ones above
     * it. The last number is read before the rows, so a row may come twice when reading on, never a hole.
     *
     * @param filter the texts of the keys of {@link #FILTER_KEYS} that were given; null values count as not given
     * @param monitor the id of the monitor the caller's numbers belong to
     */
    public TelegramPageDTO telegrams(@Nullable String bridge, Map<String, @Nullable String> filter,
            @Nullable String limit, @Nullable String after, @Nullable String monitor) {
        TelegramMonitor telegramMonitor = runningMonitor(bridge);
        TelegramFilter.Builder builder = filter(filter);
        int rows = count("limit", limit, TELEGRAMS_DEFAULT, TELEGRAMS_MAX);
        long afterNumber = after == null ? -1 : number("after", after);
        String id = telegramMonitor.id();
        long lastNumber = telegramMonitor.lastNumber();
        boolean reset = (monitor != null && !monitor.equals(id)) || afterNumber > lastNumber;
        if (reset) {
            afterNumber = 0;
        }
        boolean readingOn = afterNumber >= 0;
        if (readingOn) {
            builder.afterNumber(afterNumber);
        }
        List<TelegramRecord> found = telegramMonitor.query(builder.limit(rows + 1).build());
        boolean more = found.size() > rows;
        if (more) {
            found = readingOn ? found.subList(0, rows) : found.subList(found.size() - rows, found.size());
        }
        TelegramPageDTO page = new TelegramPageDTO();
        page.telegrams = found.stream().map(KnxDtoMapper::telegram).toList();
        page.lastNumber = lastNumber;
        page.more = more;
        page.monitorId = id;
        page.reset = reset;
        return page;
    }

    /** Number 5: forgets the rows and the last values; the numbers go on. */
    public void clearTelegrams(@Nullable String bridge) {
        handler(bridge).getTelegramMonitor().clear();
    }

    /** Number 6: the last telegram per group address, by address. */
    public List<TelegramDTO> lastValues(@Nullable String bridge) {
        List<TelegramRecord> last = new ArrayList<>(runningMonitor(bridge).lastTelegrams().values());
        last.sort(Comparator.comparingInt(row -> row.destination().getRawAddress()));
        return last.stream().map(KnxDtoMapper::telegram).toList();
    }

    /** Number 7: what the loaded keyring says; {@code loaded} false and nothing else without one. */
    public KeyringDTO keyring(@Nullable String bridge) {
        KNXBridgeBaseThingHandler handler = handler(bridge);
        KeyringFacts facts = handler.getKeyringFacts();
        if (facts == null) {
            return KnxDtoMapper.noKeyring();
        }
        return KnxDtoMapper.keyring(facts, handler.getKeyringFileName(), handler.isKeyringSignatureValid(),
                handler.getOwnAddress());
    }

    /** Number 9: a page of the group addresses; with a handler, whether a channel uses each and its last value. */
    public GroupAddressPageDTO groupAddresses(@Nullable String bridge, @Nullable String offset,
            @Nullable String limit) {
        ThingUID uid = bridgeUid(bridge);
        int first = count("offset", offset, 0, Integer.MAX_VALUE);
        int rows = count("limit", limit, PAGE_DEFAULT, PAGE_MAX);
        KnxProject project = storedProject(uid).project();
        KNXBridgeBaseThingHandler handler = findHandler(uid);
        @Nullable
        Set<Integer> configured = null;
        Map<Integer, TelegramRecord> last = Map.of();
        if (handler != null) {
            configured = new HashSet<>();
            for (KnxConfiguredAddress address : ConfiguredAddresses.of(handler.getThing()).addresses()) {
                configured.add(address.rawAddress());
            }
            last = new HashMap<>();
            for (TelegramRecord row : handler.getTelegramMonitor().lastTelegrams().values()) {
                last.put(row.destination().getRawAddress(), row);
            }
        }
        List<KnxGroupAddress> all = project.groupAddresses();
        List<GroupAddressDTO> items = new ArrayList<>();
        for (KnxGroupAddress address : page(all, first, rows)) {
            items.add(KnxDtoMapper.groupAddress(address,
                    configured == null ? null : configured.contains(address.rawAddress()),
                    last.get(address.rawAddress())));
        }
        GroupAddressPageDTO page = new GroupAddressPageDTO();
        page.items = items;
        page.total = all.size();
        page.offset = first;
        return page;
    }

    /**
     * Number 23: one group address with the channels of this bridge's Things that hold it and their Items. The
     * list is null without a bridge handler.
     */
    public GroupAddressDetailDTO groupAddress(@Nullable String bridge, @Nullable String address) {
        ThingUID uid = bridgeUid(bridge);
        GroupAddress asked;
        try {
            asked = TelegramFilterText.groupAddress(address == null ? "" : address);
        } catch (IllegalArgumentException e) {
            throw KnxRestException.badInput("address", "not a group address");
        }
        KnxGroupAddress inProject = storedProject(uid).project().groupAddress(asked.getRawAddress());
        if (inProject == null) {
            throw new KnxRestException(KnxRestError.UNKNOWN_GROUP_ADDRESS);
        }
        KNXBridgeBaseThingHandler handler = findHandler(uid);
        if (handler == null) {
            return KnxDtoMapper.groupAddressDetail(inProject, null, null, null);
        }
        TelegramRecord last = null;
        for (TelegramRecord row : handler.getTelegramMonitor().lastTelegrams().values()) {
            if (row.destination().getRawAddress() == asked.getRawAddress()) {
                last = row;
            }
        }
        // an address whose label names no channel of a known Thing is configured all the same, as call 9 says
        boolean configured = ConfiguredAddresses.of(handler.getThing()).addresses().stream()
                .anyMatch(held -> held.rawAddress() == asked.getRawAddress());
        return KnxDtoMapper.groupAddressDetail(inProject, configured, usedBy(handler.getThing(), asked.getRawAddress()),
                last);
    }

    private List<ChannelUseDTO> usedBy(Bridge bridge, int rawAddress) {
        Map<ThingUID, Thing> things = new HashMap<>();
        bridge.getThings().forEach(thing -> things.put(thing.getUID(), thing));
        List<ChannelUseDTO> uses = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (KnxConfiguredAddress address : ConfiguredAddresses.of(bridge).addresses()) {
            if (address.rawAddress() != rawAddress) {
                continue;
            }
            ChannelUID channel;
            try {
                channel = new ChannelUID(channelUid(address));
            } catch (IllegalArgumentException e) {
                continue;
            }
            Thing thing = things.get(channel.getThingUID());
            String role = address.main() ? "main" : address.status() ? "status" : "listening";
            if (thing != null && seen.add(channel.getAsString() + ' ' + role)) {
                uses.add(KnxDtoMapper.channelUse(thing, channel, thing.getChannel(channel), role,
                        linkRegistry.getLinkedItemNames(channel)));
            }
        }
        return uses;
    }

    /** Number 10: a page of the devices, in the order of the project's topology. */
    public DevicePageDTO devices(@Nullable String bridge, @Nullable String offset, @Nullable String limit) {
        ThingUID uid = bridgeUid(bridge);
        int first = count("offset", offset, 0, Integer.MAX_VALUE);
        int rows = count("limit", limit, PAGE_DEFAULT, PAGE_MAX);
        KnxProject project = storedProject(uid).project();
        List<KnxDevice> all = project.topology().devices();
        List<DeviceDTO> items = new ArrayList<>();
        for (KnxDevice device : page(all, first, rows)) {
            items.add(KnxDtoMapper.device(project, device));
        }
        DevicePageDTO page = new DevicePageDTO();
        page.items = items;
        page.total = all.size();
        page.offset = first;
        return page;
    }

    /** Number 11: a device with its group objects, by individual address or project id. */
    public DeviceDetailDTO device(@Nullable String bridge, @Nullable String device) {
        KnxProject project = storedProject(bridgeUid(bridge)).project();
        return KnxDtoMapper.deviceDetail(project, findDevice(project, device));
    }

    /** Number 12: the channel lines the console prints for a device. */
    public List<ChannelSuggestionDTO> deviceChannels(@Nullable String bridge, @Nullable String device) {
        KnxProject project = storedProject(bridgeUid(bridge)).project();
        return new KnxChannelMapper(project).forDevice(findDevice(project, device)).stream()
                .map(KnxDtoMapper::channelSuggestion).toList();
    }

    /** Number 13: the channel lines for a list of group addresses; those not in the project are listed apart. */
    public ChannelSuggestionsDTO channels(@Nullable String bridge, @Nullable ChannelRequestDTO request) {
        ThingUID uid = bridgeUid(bridge);
        List<GroupAddress> asked = channelRequest(request);
        KnxProject project = storedProject(uid).project();
        KnxChannelMapper mapper = new KnxChannelMapper(project);
        List<ChannelSuggestionDTO> suggestions = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        for (GroupAddress address : asked) {
            KnxGroupAddress inProject = project.groupAddress(address.getRawAddress());
            if (inProject == null) {
                unknown.add(address.toString());
            } else {
                suggestions.add(KnxDtoMapper.channelSuggestion(mapper.forGroupAddress(inProject)));
            }
        }
        ChannelSuggestionsDTO answer = new ChannelSuggestionsDTO();
        answer.suggestions = suggestions;
        answer.unknown = unknown;
        return answer;
    }

    /** Number 14: areas, lines and their devices, and the devices without a place. */
    public TopologyDTO topology(@Nullable String bridge) {
        return KnxDtoMapper.topology(storedProject(bridgeUid(bridge)).project());
    }

    /** Number 15: the building structure as a tree. */
    public List<SpaceDTO> locations(@Nullable String bridge) {
        KnxProject project = storedProject(bridgeUid(bridge)).project();
        return project.locations().stream().map(space -> KnxDtoMapper.space(project, space)).toList();
    }

    /** Number 16: the differences between the bridge's channels and its project, as {@code check-project}. */
    public ProjectCheckDTO check(@Nullable String bridge) {
        KNXBridgeBaseThingHandler handler = handler(bridge);
        KnxProject project = storedProject(handler.getThing().getUID()).project();
        ConfiguredAddresses configured = ConfiguredAddresses.of(handler.getThing());
        ProjectCheckDTO check = new ProjectCheckDTO();
        check.differences = new KnxProjectCheck(project).check(configured.addresses()).stream()
                .map(KnxDtoMapper::difference).toList();
        check.thingsWithoutHandler = configured.thingsWithoutHandler();
        return check;
    }

    /**
     * Number 21: the wizard's proposal for a selection of the project, with ids and names that are free in the
     * registries. It changes nothing and keeps nothing for call 22.
     */
    public ProposalDTO proposal(@Nullable String bridge, @Nullable ProposalRequestDTO request) {
        KNXBridgeBaseThingHandler handler = handler(bridge);
        Bridge thing = handler.getThing();
        ThingUID uid = thing.getUID();
        KnxProposalBuilder.Selection selection = selection(request);
        KnxProject project = storedProject(uid).project();
        ConfiguredAddresses configured = ConfiguredAddresses.of(thing);
        Map<Integer, Set<String>> channels = new HashMap<>();
        for (KnxConfiguredAddress address : configured.addresses()) {
            Set<String> uids = channels.get(address.rawAddress());
            if (uids == null) {
                uids = new HashSet<>();
                channels.put(address.rawAddress(), uids);
            }
            uids.add(channelUid(address));
        }
        KnxProposal proposal = new KnxProposalBuilder(project).propose(selection, channels.keySet());
        ProposalNames.Names names = new ProposalNames(this::thingExists, this::itemExists, this::groupsOfGroup)
                .names(uid.getAsString(), proposal);
        ProposalDTO dto = KnxProposalMapper.proposal(proposal, names, channels, configured.thingsWithoutHandler());
        dto.deviceThings = thing.getThings().stream()
                .filter(device -> KNXBindingConstants.THING_TYPE_DEVICE.equals(device.getThingTypeUID()))
                .map(KnxRestService::deviceThing).sorted(Comparator.comparing(device -> device.uid)).toList();
        return dto;
    }

    private static DeviceThingDTO deviceThing(Thing device) {
        DeviceThingDTO dto = new DeviceThingDTO();
        dto.uid = device.getUID().getAsString();
        String label = device.getLabel();
        dto.label = label == null ? null : ProjectNames.clean(label);
        dto.channelIds = device.getChannels().stream().map(channel -> channel.getUID().getId()).sorted().toList();
        return dto;
    }

    /**
     * Number 22: the checked proposal as text and create steps. Only the fields a user may edit are read from the
     * request; the rest comes from the project and the registries. It changes nothing.
     */
    public ProposalPreviewDTO preview(@Nullable String bridge, @Nullable PreviewRequestDTO request) {
        KNXBridgeBaseThingHandler handler = handler(bridge);
        Bridge thing = handler.getThing();
        ThingUID uid = thing.getUID();
        if (request == null) {
            throw KnxRestException.badInput("things", "a preview request is needed");
        }
        KnxProject project = storedProject(uid).project();
        Set<Integer> configured = new HashSet<>();
        ConfiguredAddresses.of(thing).addresses().forEach(address -> configured.add(address.rawAddress()));
        ProposalValidator validator = new ProposalValidator(project, uid.getAsString(), this::thingExists,
                this::itemExists, name -> groupsOfGroup(name) != null, configured, handler.getKeyringFacts() != null);
        ProposalValidator.Result result;
        try {
            result = validator.validate(request);
        } catch (ProposalValidator.BadRequest e) {
            throw KnxRestException.badInput(e.where(), e.reason());
        }
        return KnxProposalMapper.preview(result, this::channelIdsOf);
    }

    private boolean thingExists(String uid) {
        try {
            return thingRegistry.get(new ThingUID(uid)) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Set<String> channelIdsOf(String thingUid) {
        @Nullable
        Thing thing;
        try {
            thing = thingRegistry.get(new ThingUID(thingUid));
        } catch (IllegalArgumentException e) {
            return Set.of();
        }
        Set<String> ids = new HashSet<>();
        if (thing != null) {
            thing.getChannels().forEach(channel -> ids.add(channel.getUID().getId()));
        }
        return ids;
    }

    private boolean itemExists(String name) {
        return itemRegistry.get(name) != null;
    }

    /** The groups a group Item of this name is in; {@code null} when no Item has the name or it is no group. */
    private @Nullable List<String> groupsOfGroup(String name) {
        return itemRegistry.get(name) instanceof GroupItem group ? group.getGroupNames() : null;
    }

    /** The channel UID of a configured address: its label is the channel UID, a space and the key. */
    private static String channelUid(KnxConfiguredAddress address) {
        String label = address.label();
        int space = label.lastIndexOf(' ');
        return space < 0 ? label : label.substring(0, space);
    }

    private static KnxProposalBuilder.Selection selection(@Nullable ProposalRequestDTO request) {
        if (request == null) {
            throw KnxRestException.badInput(SELECTION, "a selection is needed");
        }
        List<String> addressTexts = request.groupAddresses;
        List<String> functions = request.functions;
        List<String> spaces = request.spaces;
        List<String> devices = request.devices;
        long total = 0;
        for (List<String> list : Arrays.asList(addressTexts, functions, spaces, devices)) {
            total += list == null ? 0 : list.size();
        }
        if (total == 0) {
            throw KnxRestException.badInput(SELECTION, "nothing is selected");
        }
        if (total > CHANNEL_ADDRESSES_MAX) {
            throw KnxRestException.badInput(SELECTION, "more than " + CHANNEL_ADDRESSES_MAX + " entries in all");
        }
        List<Integer> addresses = new ArrayList<>();
        if (addressTexts != null) {
            for (int i = 0; i < addressTexts.size(); i++) {
                // JSON can hold null entries although the list type says otherwise
                Object entry = addressTexts.get(i);
                String failure = "entry " + (i + 1) + " is not a group address";
                if (!(entry instanceof String text)) {
                    throw KnxRestException.badInput(GROUP_ADDRESSES, failure);
                }
                try {
                    addresses.add(TelegramFilterText.groupAddress(text).getRawAddress());
                } catch (IllegalArgumentException e) {
                    throw KnxRestException.badInput(GROUP_ADDRESSES, failure);
                }
            }
        }
        Boolean readAtStart = request.readAtStart;
        return new KnxProposalBuilder.Selection(addresses, ids("functions", functions), ids("spaces", spaces),
                ids("devices", devices), readAtStart == null || readAtStart);
    }

    private static List<String> ids(String name, @Nullable List<String> list) {
        if (list == null) {
            return List.of();
        }
        List<String> ids = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof String id)) {
                throw KnxRestException.badInput(name, "entry " + (i + 1) + " is not an id");
            }
            ids.add(id);
        }
        return ids;
    }

    /** Number 18: deletes the stored project, also one of a bridge without handler or one that cannot be used. */
    public void removeProject(@Nullable String bridge) {
        ThingUID uid = bridgeUid(bridge);
        boolean removed;
        try {
            removed = store.remove(uid);
        } catch (IOException e) {
            LoggerFactory.getLogger(KnxRestService.class).warn("KNX REST project remove: {} ({})",
                    KnxRestError.STORE_FAILED.code(), e.getClass().getName());
            throw new KnxRestException(KnxRestError.STORE_FAILED);
        }
        if (!removed) {
            throw new KnxRestException(KnxRestError.NO_PROJECT);
        }
    }

    /**
     * {@return a builder with the filters of the given keys of {@link #FILTER_KEYS}, read as the console reads them}
     */
    TelegramFilter.Builder filter(Map<String, @Nullable String> parameters) {
        TelegramFilter.Builder builder = TelegramFilter.builder();
        for (String key : FILTER_KEYS) {
            String value = parameters.get(key);
            if (value == null) {
                continue;
            }
            try {
                TelegramFilterText.apply(builder, key, value, clock, zone);
            } catch (IllegalArgumentException e) {
                // the time library's message repeats the text it could not read
                throw KnxRestException.badInput(key,
                        e.getCause() instanceof DateTimeException ? TIME_TEXT : String.valueOf(e.getMessage()));
            }
        }
        return builder;
    }

    private TelegramMonitor runningMonitor(@Nullable String bridge) {
        TelegramMonitor monitor = handler(bridge).getTelegramMonitor();
        if (monitor.status().bufferSize() == 0) {
            throw new KnxRestException(KnxRestError.MONITOR_OFF);
        }
        return monitor;
    }

    private static int count(String name, @Nullable String text, int defaultValue, int max) {
        if (text == null) {
            return defaultValue;
        }
        if (COUNT.matcher(text).matches()) {
            int value = Integer.parseInt(text);
            if (value <= max) {
                return value;
            }
        }
        throw KnxRestException.badInput(name, "not a number from 0 to " + max);
    }

    static long number(String name, String text) {
        if (!NUMBER.matcher(text).matches()) {
            throw KnxRestException.badInput(name, "not a row number from 0 on");
        }
        return Long.parseLong(text);
    }

    private static <T> List<T> page(List<T> all, int first, int rows) {
        int from = Math.min(first, all.size());
        int to = (int) Math.min((long) from + rows, all.size());
        return all.subList(from, to);
    }

    private static KnxDevice findDevice(KnxProject project, @Nullable String text) {
        KnxDevice device = null;
        if (text != null) {
            Matcher address = DEVICE_ADDRESS.matcher(text);
            if (address.matches()) {
                int area = Integer.parseInt(address.group(1));
                int line = Integer.parseInt(address.group(2));
                int number = Integer.parseInt(address.group(3));
                if (area <= 15 && line <= 15 && number <= 255) {
                    device = project.device(new KnxIndividualAddress(area, line, number));
                }
            } else {
                device = project.deviceById(text);
            }
        }
        if (device == null) {
            throw new KnxRestException(KnxRestError.UNKNOWN_DEVICE);
        }
        return device;
    }

    private static List<GroupAddress> channelRequest(@Nullable ChannelRequestDTO request) {
        List<String> texts = request == null ? null : request.groupAddresses;
        if (texts == null || texts.isEmpty()) {
            throw KnxRestException.badInput(GROUP_ADDRESSES, "a list of group addresses is needed");
        }
        if (texts.size() > CHANNEL_ADDRESSES_MAX) {
            throw KnxRestException.badInput(GROUP_ADDRESSES, "more than " + CHANNEL_ADDRESSES_MAX + " entries");
        }
        List<GroupAddress> addresses = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i++) {
            // JSON can hold null entries although the list type says otherwise
            Object entry = texts.get(i);
            String failure = "entry " + (i + 1) + " is not a group address";
            if (!(entry instanceof String text)) {
                throw KnxRestException.badInput(GROUP_ADDRESSES, failure);
            }
            try {
                addresses.add(TelegramFilterText.groupAddress(text));
            } catch (IllegalArgumentException e) {
                throw KnxRestException.badInput(GROUP_ADDRESSES, failure);
            }
        }
        return addresses;
    }

    /** {@return the UID of a bridge this add-on can own, exactly as written} */
    static ThingUID bridgeUid(@Nullable String text) {
        if (text == null || !BRIDGE_UID.matcher(text).matches()) {
            throw new KnxRestException(KnxRestError.UNKNOWN_BRIDGE);
        }
        try {
            return new ThingUID(text);
        } catch (IllegalArgumentException e) {
            throw new KnxRestException(KnxRestError.UNKNOWN_BRIDGE);
        }
    }

    private @Nullable KNXBridgeBaseThingHandler findHandler(ThingUID uid) {
        for (KNXBridgeBaseThingHandler handler : factory.getBridges()) {
            if (uid.equals(handler.getThing().getUID())) {
                return handler;
            }
        }
        return null;
    }

    /** {@return the handler of the bridge; for the endpoints that need the running bridge} */
    KNXBridgeBaseThingHandler handler(@Nullable String bridge) {
        KNXBridgeBaseThingHandler handler = findHandler(bridgeUid(bridge));
        if (handler == null) {
            throw new KnxRestException(KnxRestError.UNKNOWN_BRIDGE);
        }
        return handler;
    }

    /** {@return the loaded project of the bridge; for the endpoints that read the project} */
    StoredProject storedProject(ThingUID uid) {
        FileState state = store.fileState(uid);
        if (state == FileState.UNREADABLE || state == FileState.NEWER_VERSION) {
            throw new KnxRestException(KnxRestError.PROJECT_UNUSABLE);
        }
        StoredProject stored = state == FileState.LOADED ? store.get(uid) : null;
        if (stored == null) {
            throw new KnxRestException(KnxRestError.NO_PROJECT);
        }
        return stored;
    }
}

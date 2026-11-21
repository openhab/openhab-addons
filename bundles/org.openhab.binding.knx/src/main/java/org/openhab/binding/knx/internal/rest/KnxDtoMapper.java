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
import java.util.Collection;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.monitor.MonitorStatus;
import org.openhab.binding.knx.internal.monitor.NotDecryptedReason;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.project.KnxArea;
import org.openhab.binding.knx.internal.project.KnxChannelSuggestion;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxFunction;
import org.openhab.binding.knx.internal.project.KnxFunctionGroupAddress;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxGroupObject;
import org.openhab.binding.knx.internal.project.KnxGroupObjectFlags;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxLine;
import org.openhab.binding.knx.internal.project.KnxProduct;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectCheck.Difference;
import org.openhab.binding.knx.internal.project.KnxProjectInfo;
import org.openhab.binding.knx.internal.project.KnxSpace;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.FileState;
import org.openhab.binding.knx.internal.projectstore.ProjectNames;
import org.openhab.binding.knx.internal.projectstore.StoredProject;
import org.openhab.binding.knx.internal.rest.dto.AreaDTO;
import org.openhab.binding.knx.internal.rest.dto.BridgeDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelSuggestionDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelUseDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.DifferenceDTO;
import org.openhab.binding.knx.internal.rest.dto.FunctionAddressDTO;
import org.openhab.binding.knx.internal.rest.dto.FunctionDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupObjectDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringInterfaceDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringUploadDTO;
import org.openhab.binding.knx.internal.rest.dto.LineDTO;
import org.openhab.binding.knx.internal.rest.dto.MonitorDTO;
import org.openhab.binding.knx.internal.rest.dto.NotDecryptedDTO;
import org.openhab.binding.knx.internal.rest.dto.ProjectDTO;
import org.openhab.binding.knx.internal.rest.dto.SpaceDTO;
import org.openhab.binding.knx.internal.rest.dto.TelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.TopologyDTO;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.KeyringFacts.InterfaceFacts;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeUID;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Fills the answer classes field by field. The only place where reader records, monitor records and keyring facts
 * are read for an answer, so that no key, password or library object reaches the JSON writer.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class KnxDtoMapper {
    /** The longest description or label kept; names are cut as on the console. */
    static final int LONG_TEXT = 300;

    private KnxDtoMapper() {
    }

    static BridgeDTO bridge(KNXBridgeBaseThingHandler handler, FileState projectState, @Nullable StoredProject stored) {
        Thing thing = handler.getThing();
        BridgeDTO dto = storedBridge(thing.getUID(), projectState, stored);
        dto.present = true;
        dto.label = thing.getLabel();
        dto.status = thing.getStatus().name();
        MonitorStatus status = handler.getTelegramMonitor().status();
        MonitorStatus.Connection connection = status.connection();
        if (connection != null) {
            dto.connection = connection.kind().name();
            dto.ownAddress = connection.ownAddress().toString();
        }
        dto.bufferSize = status.bufferSize();
        dto.keyringLoaded = handler.getKeyringFacts() != null;
        dto.sendAllowed = GroupTelegramSender.allowed(handler);
        return dto;
    }

    static BridgeDTO storedBridge(ThingUID uid, FileState projectState, @Nullable StoredProject stored) {
        BridgeDTO dto = new BridgeDTO();
        dto.uid = uid.getAsString();
        dto.present = false;
        dto.projectState = projectState.name();
        if (projectState == FileState.LOADED && stored != null) {
            dto.projectName = ProjectNames.clean(stored.project().info().name());
            dto.projectSpaces = stored.project().locations().size();
        }
        return dto;
    }

    static ProjectDTO project(FileState state, @Nullable StoredProject stored) {
        ProjectDTO dto = new ProjectDTO();
        if (state != FileState.LOADED || stored == null) {
            dto.state = (state == FileState.LOADED ? FileState.NONE : state).name();
            return dto;
        }
        dto.state = state.name();
        KnxProject project = stored.project();
        KnxProjectInfo info = project.info();
        dto.name = ProjectNames.clean(info.name());
        dto.installation = ProjectNames.clean(project.installationName());
        dto.importedAt = stored.importedAt().toString();
        dto.sourceName = ProjectNames.clean(stored.sourceName());
        String language = stored.language();
        dto.language = language == null ? null : ProjectNames.clean(language);
        dto.groupAddressStyle = info.groupAddressStyle().name();
        dto.toolVersion = cleanOrNull(info.toolVersion());
        dto.lastModified = cleanOrNull(info.lastModified());
        dto.groupAddresses = project.groupAddresses().size();
        dto.securedGroupAddresses = (int) project.groupAddresses().stream().filter(KnxGroupAddress::secured).count();
        dto.devices = project.topology().devices().size();
        dto.groupObjects = project.groupObjects().size();
        dto.ignoredInstallations = project.ignoredInstallations();
        return dto;
    }

    static MonitorDTO monitor(TelegramMonitor monitor, long lastNumber) {
        MonitorStatus status = monitor.status();
        MonitorDTO dto = new MonitorDTO();
        MonitorStatus.Connection connection = status.connection();
        if (connection != null) {
            dto.connection = connection.kind().name();
            dto.ownAddress = connection.ownAddress().toString();
        }
        dto.bufferSize = status.bufferSize();
        dto.rows = status.rows();
        dto.lastNumber = lastNumber;
        dto.monitorId = monitor.id();
        dto.received = status.received();
        dto.sent = status.sent();
        Map<String, Long> notDecrypted = new LinkedHashMap<>();
        for (NotDecryptedReason reason : NotDecryptedReason.values()) {
            Long count = status.notDecrypted().get(reason);
            if (count != null) {
                notDecrypted.put(reason.name(), count);
            }
        }
        dto.notDecrypted = notDecrypted;
        dto.notDecryptedOtherReason = status.notDecryptedOtherReason();
        dto.plainOnSecuredAddress = status.plainOnSecuredAddress();
        List<NotDecryptedDTO> senders = new ArrayList<>();
        for (Map.Entry<GroupAddress, List<IndividualAddress>> entry : monitor.notDecryptedSenders().entrySet()) {
            NotDecryptedDTO sender = new NotDecryptedDTO();
            sender.groupAddress = entry.getKey().toString();
            sender.senders = entry.getValue().stream().map(IndividualAddress::toString).toList();
            senders.add(sender);
        }
        dto.notDecryptedSenders = senders;
        return dto;
    }

    static TelegramDTO telegram(TelegramRecord row) {
        TelegramDTO dto = new TelegramDTO();
        dto.number = row.number();
        dto.time = row.time().toString();
        dto.source = row.source().toString();
        dto.sourceName = cleanOrNull(row.sourceName());
        dto.destination = row.destination().toString();
        dto.destinationName = cleanOrNull(row.destinationName());
        dto.type = row.type().name();
        dto.direction = row.direction().name();
        byte[] payload = row.payload();
        dto.payload = payload.length == 0 ? null : HexFormat.of().withUpperCase().formatHex(payload);
        dto.dataType = row.dataType();
        dto.value = row.value();
        dto.unit = row.unit();
        dto.security = row.security().name();
        NotDecryptedReason reason = row.reason();
        dto.reason = reason == null ? null : reason.name();
        SendOutcome outcome = row.outcome();
        dto.outcome = outcome == null ? null : outcome.name();
        dto.plainOnSecuredAddress = row.plainOnSecuredAddress();
        return dto;
    }

    static KeyringDTO noKeyring() {
        KeyringDTO dto = new KeyringDTO();
        dto.loaded = false;
        return dto;
    }

    static KeyringDTO keyring(KeyringFacts facts, String fileName, boolean signatureValid,
            Optional<IndividualAddress> ownAddress) {
        KeyringDTO dto = new KeyringDTO();
        dto.loaded = true;
        dto.fileName = fileName;
        dto.signatureValid = signatureValid;
        dto.backboneKeyPresent = facts.hasBackboneKey();
        dto.securedGroupAddresses = facts.securedGroupAddresses().size();
        List<KeyringInterfaceDTO> interfaces = new ArrayList<>();
        for (InterfaceFacts entry : facts.interfaces()) {
            KeyringInterfaceDTO iface = new KeyringInterfaceDTO();
            iface.address = entry.address().toString();
            iface.type = entry.type().name();
            IndividualAddress host = entry.host();
            iface.host = host == null ? null : host.toString();
            iface.userId = entry.userId() == 0 ? null : entry.userId();
            iface.linkedSecuredGroupAddresses = entry.linkedSecuredGroups().size();
            iface.own = ownAddress.isPresent() && ownAddress.get().equals(entry.address());
            interfaces.add(iface);
        }
        dto.interfaces = interfaces;
        return dto;
    }

    /** @param signatureValid whether the signature matches the given password, null without one */
    static KeyringUploadDTO keyringUpload(KeyringFacts facts, String fileName, @Nullable Boolean signatureValid,
            boolean shadowedByConfigFolder, Optional<IndividualAddress> ownAddress) {
        KeyringUploadDTO dto = new KeyringUploadDTO();
        dto.stored = true;
        dto.fileName = fileName;
        dto.signatureValid = signatureValid;
        dto.shadowedByConfigFolder = shadowedByConfigFolder;
        KeyringDTO keyring = keyring(facts, fileName, Boolean.TRUE.equals(signatureValid), ownAddress);
        keyring.signatureValid = signatureValid;
        dto.keyring = keyring;
        return dto;
    }

    /**
     * @param configured whether a channel of the bridge uses the address, null without a bridge handler
     * @param last the last telegram to the address, if any
     */
    static GroupAddressDTO groupAddress(KnxGroupAddress address, @Nullable Boolean configured,
            @Nullable TelegramRecord last) {
        GroupAddressDTO dto = new GroupAddressDTO();
        fillGroupAddress(dto, address, configured, last);
        return dto;
    }

    /**
     * @param configured as in the list of call 9, null without a bridge handler
     * @param usedBy the channels that hold the address, null without a bridge handler
     */
    static GroupAddressDetailDTO groupAddressDetail(KnxGroupAddress address, @Nullable Boolean configured,
            @Nullable List<ChannelUseDTO> usedBy, @Nullable TelegramRecord last) {
        GroupAddressDetailDTO dto = new GroupAddressDetailDTO();
        fillGroupAddress(dto, address, configured, last);
        dto.usedBy = usedBy;
        return dto;
    }

    static ChannelUseDTO channelUse(Thing thing, ChannelUID uid, @Nullable Channel channel, String role,
            Collection<String> items) {
        ChannelUseDTO dto = new ChannelUseDTO();
        dto.thingUID = thing.getUID().getAsString();
        dto.thingLabel = cleanOrNull(thing.getLabel());
        dto.channelId = uid.getId();
        ChannelTypeUID type = channel == null ? null : channel.getChannelTypeUID();
        dto.channelLabel = channel == null ? null : cleanOrNull(channel.getLabel());
        dto.channelType = type == null ? null : type.getId();
        dto.role = role;
        dto.items = items.stream().sorted().toList();
        return dto;
    }

    private static void fillGroupAddress(GroupAddressDTO dto, KnxGroupAddress address, @Nullable Boolean configured,
            @Nullable TelegramRecord last) {
        dto.address = ProjectNames.clean(address.address());
        dto.raw = address.rawAddress();
        dto.name = ProjectNames.clean(address.name());
        dto.description = cleanLongOrNull(address.description());
        List<String> dataTypes = address.datapointTypes().stream().map(KnxDatapointType::toString).toList();
        dto.dataType = dataTypes.isEmpty() ? null : dataTypes.get(0);
        dto.dataTypes = dataTypes;
        dto.secured = address.secured();
        dto.range = address.rangePath().stream().map(ProjectNames::clean).toList();
        dto.configured = configured;
        dto.last = last == null ? null : telegram(last);
    }

    static DeviceDTO device(KnxProject project, KnxDevice device) {
        DeviceDTO dto = new DeviceDTO();
        fillDevice(dto, project, device);
        return dto;
    }

    static DeviceDetailDTO deviceDetail(KnxProject project, KnxDevice device) {
        DeviceDetailDTO dto = new DeviceDetailDTO();
        fillDevice(dto, project, device);
        dto.groupObjects = project.groupObjectsOf(device).stream().map(object -> groupObject(project, object)).toList();
        return dto;
    }

    private static void fillDevice(DeviceDTO dto, KnxProject project, KnxDevice device) {
        dto.id = device.id();
        KnxIndividualAddress address = device.individualAddress();
        dto.address = address == null ? null : address.toString();
        dto.name = ProjectNames.clean(project.displayNameOf(device));
        dto.description = cleanLongOrNull(device.description());
        KnxProduct product = project.productOf(device);
        if (product != null) {
            dto.product = cleanOrNull(product.text());
            dto.orderNumber = cleanOrNull(product.orderNumber());
        }
        KnxSpace room = project.spaceOf(device);
        dto.room = room == null ? null : ProjectNames.clean(room.name());
        List<KnxGroupObject> objects = project.groupObjectsOf(device);
        dto.objects = objects.size();
        dto.linkedObjects = (int) objects.stream().filter(KnxGroupObject::isLinked).count();
        dto.filterDevice = project.isFilterDevice(device);
    }

    private static GroupObjectDTO groupObject(KnxProject project, KnxGroupObject object) {
        GroupObjectDTO dto = new GroupObjectDTO();
        dto.number = object.number();
        dto.name = ProjectNames.clean(object.name());
        dto.text = cleanLongOrNull(object.text());
        dto.function = cleanOrNull(object.functionText());
        KnxDatapointType dataType = object.datapointType();
        dto.dataType = dataType == null ? null : dataType.toString();
        dto.sizeBits = object.sizeBits();
        KnxGroupObjectFlags flags = object.flags();
        dto.read = flags.read();
        dto.write = flags.write();
        dto.communication = flags.communication();
        dto.transmit = flags.transmit();
        dto.update = flags.update();
        dto.readOnInit = flags.readOnInit();
        List<String> addresses = new ArrayList<>();
        for (String id : object.groupAddressIds()) {
            String text = addressText(project, id);
            if (text != null) {
                addresses.add(text);
            }
        }
        dto.groupAddresses = addresses;
        String sending = object.sendingGroupAddressId();
        dto.sendingGroupAddress = sending == null ? null : addressText(project, sending);
        return dto;
    }

    /** {@return the address of the group address with this id, null if the project does not know the id} */
    private static @Nullable String addressText(KnxProject project, String id) {
        KnxGroupAddress address = project.groupAddress(id);
        return address == null ? null : ProjectNames.clean(address.address());
    }

    static ChannelSuggestionDTO channelSuggestion(KnxChannelSuggestion suggestion) {
        ChannelSuggestionDTO dto = new ChannelSuggestionDTO();
        KnxGroupAddress address = suggestion.groupAddress();
        dto.groupAddress = ProjectNames.clean(address.address());
        dto.name = ProjectNames.clean(address.name());
        dto.key = suggestion.key();
        dto.value = suggestion.value();
        dto.channelType = suggestion.channelType();
        dto.label = ProjectNames.clean(suggestion.label(), LONG_TEXT);
        dto.text = suggestion.text();
        dto.notes = suggestion.notes().stream().map(ProjectNames::clean).toList();
        return dto;
    }

    static TopologyDTO topology(KnxProject project) {
        TopologyDTO dto = new TopologyDTO();
        List<AreaDTO> areas = new ArrayList<>();
        for (KnxArea area : project.topology().areas()) {
            AreaDTO areaDto = new AreaDTO();
            areaDto.address = Integer.toString(area.address());
            areaDto.name = ProjectNames.clean(area.name());
            List<LineDTO> lines = new ArrayList<>();
            for (KnxLine line : area.lines()) {
                LineDTO lineDto = new LineDTO();
                lineDto.address = area.address() + "." + line.address();
                lineDto.name = ProjectNames.clean(line.name());
                lineDto.devices = line.allDevices().stream().map(device -> device(project, device)).toList();
                lines.add(lineDto);
            }
            areaDto.lines = lines;
            areas.add(areaDto);
        }
        dto.areas = areas;
        dto.unplacedDevices = project.topology().unplacedDevices().stream().map(device -> device(project, device))
                .toList();
        return dto;
    }

    /** The stored project holds spaces at most 64 deep, so the recursion is bounded. */
    static SpaceDTO space(KnxProject project, KnxSpace space) {
        SpaceDTO dto = new SpaceDTO();
        dto.id = space.id();
        dto.name = ProjectNames.clean(space.name());
        dto.type = ProjectNames.clean(space.type());
        dto.number = cleanOrNull(space.number());
        dto.usage = cleanOrNull(space.usage());
        dto.spaces = space.spaces().stream().map(child -> space(project, child)).toList();
        dto.devices = space.deviceIds();
        List<FunctionDTO> functions = new ArrayList<>();
        for (KnxFunction function : space.functions()) {
            FunctionDTO functionDto = new FunctionDTO();
            functionDto.id = function.id();
            functionDto.name = ProjectNames.clean(function.name());
            functionDto.type = ProjectNames.clean(function.type());
            List<FunctionAddressDTO> addresses = new ArrayList<>();
            for (KnxFunctionGroupAddress address : function.groupAddresses()) {
                FunctionAddressDTO addressDto = new FunctionAddressDTO();
                addressDto.groupAddress = addressText(project, address.groupAddressId());
                addressDto.name = ProjectNames.clean(address.name());
                addressDto.role = cleanOrNull(address.role());
                addresses.add(addressDto);
            }
            functionDto.groupAddresses = addresses;
            functions.add(functionDto);
        }
        dto.functions = functions;
        return dto;
    }

    static DifferenceDTO difference(Difference difference) {
        DifferenceDTO dto = new DifferenceDTO();
        dto.kind = difference.kind().name();
        KnxGroupAddress inProject = difference.projectAddress();
        dto.address = inProject == null ? new GroupAddress(difference.configured().rawAddress()).toString()
                : ProjectNames.clean(inProject.address());
        dto.configuredDataType = cleanOrNull(difference.configured().datapointType());
        KnxDatapointType projectType = difference.projectType();
        dto.projectDataType = projectType == null ? null : projectType.toString();
        dto.label = ProjectNames.clean(difference.configured().label(), LONG_TEXT);
        dto.noteOnly = difference.noteOnly();
        return dto;
    }

    static @Nullable String cleanOrNull(@Nullable String text) {
        return text == null ? null : ProjectNames.clean(text);
    }

    static @Nullable String cleanLongOrNull(@Nullable String text) {
        return text == null ? null : ProjectNames.clean(text, LONG_TEXT);
    }
}

/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.wolfsmartset.internal.handler;

import static org.openhab.binding.wolfsmartset.internal.WolfSmartsetBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wolfsmartset.internal.config.WolfSmartsetSystemConfiguration;
import org.openhab.binding.wolfsmartset.internal.discovery.WolfSmartsetSystemDiscoveryService;
import org.openhab.binding.wolfsmartset.internal.dto.*;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WolfSmartsetSystemBridgeHandler} is the handler for an WolfSmartset system.
 *
 * @author Bo Biene - Initial contribution
 */
@NonNullByDefault
public class WolfSmartsetSystemBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(WolfSmartsetSystemBridgeHandler.class);

    private ChannelTypeRegistry channelTypeRegistry;

    private @NonNullByDefault({}) String systemId;

    private final Map<String, WolfSmartsetUnitThingHandler> unitHandlers = new ConcurrentHashMap<>();

    private @Nullable GetSystemListDTO savedSystem;
    private @Nullable List<Pair<SubMenuEntryDTO, MenuItemTabViewDTO>> savedUnits;
    private Map<String, State> stateCache = new ConcurrentHashMap<>();
    private Map<ChannelUID, Boolean> channelReadOnlyMap = new HashMap<>();

    public WolfSmartsetSystemBridgeHandler(Bridge bridge, ChannelTypeRegistry channelTypeRegistry) {
        super(bridge);
        this.channelTypeRegistry = channelTypeRegistry;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for first api sync");
    }

    public @Nullable WolfSmartsetAccountBridgeHandler getAccountBridgeHanlder() {
        return (WolfSmartsetAccountBridgeHandler) this.getBridge().getHandler();
    }

    @Override
    public void initialize() {
        systemId = getConfigAs(WolfSmartsetSystemConfiguration.class).systemId;
        logger.debug("SystemBridge: Initializing system '{}'", systemId);
        initializeReadOnlyChannels();
        clearSavedState();
        updateStatus(WolfSmartsetUtils.isBridgeOnline(getBridge()) ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(WolfSmartsetSystemDiscoveryService.class);
    }

    @Override
    public void dispose() {
        logger.debug("SystemBridge: Disposing system '{}'", systemId);
    }

    @Override
    public void childHandlerInitialized(ThingHandler unitHandler, Thing unitThing) {
        String unitId = (String) unitThing.getConfiguration().get(CONFIG_UNIT_ID);
        unitHandlers.put(unitId, (WolfSmartsetUnitThingHandler) unitHandler);
        logger.debug("SystemBridge: Saving unit handler for {} with id {}", unitThing.getUID(), unitId);
        var accountBridgeHandler = getAccountBridgeHanlder();
        if (accountBridgeHandler != null)
            accountBridgeHandler.scheduleRefreshJob();
    }

    @Override
    public void childHandlerDisposed(ThingHandler unitHandler, Thing unitThing) {
        String unitId = (String) unitThing.getConfiguration().get(CONFIG_UNIT_ID);
        unitHandlers.remove(unitId);
        logger.debug("SystemBridge: Removing unit handler for {} with id {}", unitThing.getUID(), unitId);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State state = stateCache.get(channelUID.getId());
            if (state != null) {
                updateState(channelUID.getId(), state);
            }
            return;
        }
        if (isChannelReadOnly(channelUID)) {
            logger.debug("Can't apply command '{}' to '{}' because channel is readonly", command, channelUID.getId());
            return;
        }
    }

    public Collection<WolfSmartsetUnitThingHandler> getUnitHandler() {
        return unitHandlers.values();
    }

    public List<Pair<SubMenuEntryDTO, MenuItemTabViewDTO>> getUnits() {
        List<Pair<SubMenuEntryDTO, MenuItemTabViewDTO>> localSavedUnits = savedUnits;
        return localSavedUnits == null ? EMPTY_UNITS : localSavedUnits;
    }

    public @Nullable GetSystemListDTO getSystemConfig() {
        return savedSystem;
    }

    public String getSystemId() {
        return systemId;
    }

    public void updateConfiguration(@Nullable GetSystemListDTO system,
            @Nullable GetGuiDescriptionForGatewayDTO systemDescription) {
        if (system != null && systemDescription != null) {
            logger.debug("SystemBridge: Updating channels for system id {}, name {}", system.getId(), system.getName());
            updateStatus(ThingStatus.ONLINE);
            savedSystem = system;

            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, system.getGatewaySoftwareVersion());
            properties.put(THING_PROPERTY_GATEWAY_ID, system.getGatewayId().toString());
            properties.put(THING_PROPERTY_GATEWAY_USERNAME, system.getGatewayUsername());
            properties.put(THING_PROPERTY_INSTALLATION_DATE, system.getInstallationDate());
            properties.put(THING_PROPERTY_LOCATION, system.getLocation());
            properties.put(THING_PROPERTY_OPERATOR_NAME, system.getOperatorName());
            properties.put(THING_PROPERTY_USERNAME_OWNER, system.getUserNameOwner());
            properties.put(THING_PROPERTY_ACCESSLEVEL, system.getAccessLevel().toString());
            updateProperties(properties);

            updateUnitsConfiguration(systemDescription);
            updateEquipmentStatus(system);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to retrieve configuration");
        }
    }

    private void updateUnitsConfiguration(GetGuiDescriptionForGatewayDTO systemDescription) {
        List<Pair<SubMenuEntryDTO, MenuItemTabViewDTO>> listUnits = new ArrayList<>();
        var fachmannNode = systemDescription.getMenuItems().stream()
                .filter(m -> "Fachmann".equalsIgnoreCase(m.getName())).findFirst();

        if (fachmannNode.isPresent()) {
            for (var submenu : fachmannNode.get().getSubMenuEntries()) {
                for (var tabmenu : submenu.getTabViews()) {
                    listUnits.add(Pair.of(submenu, tabmenu));

                    var handler = unitHandlers.get(tabmenu.BundleId.toString());
                    if (handler != null) {
                        handler.updateConfiguration(submenu, tabmenu);
                    }
                }
            }

        } else {
        }
        savedUnits = listUnits;
    }

    public void updateSystemState(@Nullable GetSystemStateListDTO systemState) {
    }

    public void updateFaultMessages(@Nullable ReadFaultMessagesDTO faultMessages) {
    }

    private void updateEquipmentStatus(GetSystemListDTO system) {
        // final String grp = CHGRP_EQUIPMENT_STATUS + "#";
        // updateChannel(grp + CH_EQUIPMENT_STATUS, WolfSmartsetUtils.undefOrString(system.equipmentStatus));
    }

    private @Nullable WolfSmartsetAccountBridgeHandler getBridgeHandler() {
        WolfSmartsetAccountBridgeHandler handler = null;
        Bridge bridge = getBridge();
        if (bridge != null) {
            handler = (WolfSmartsetAccountBridgeHandler) bridge.getHandler();
        }
        return handler;
    }

    @SuppressWarnings("null")
    private boolean isChannelReadOnly(ChannelUID channelUID) {
        Boolean isReadOnly = channelReadOnlyMap.get(channelUID);
        return isReadOnly != null ? isReadOnly : true;
    }

    private void clearSavedState() {
        savedSystem = null;
        savedUnits = null;
        stateCache.clear();
    }

    private void initializeReadOnlyChannels() {
        channelReadOnlyMap.clear();
        for (Channel channel : thing.getChannels()) {
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null) {
                ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID, null);
                if (channelType != null) {
                    channelReadOnlyMap.putIfAbsent(channel.getUID(), channelType.getState().isReadOnly());
                }
            }
        }
    }
}

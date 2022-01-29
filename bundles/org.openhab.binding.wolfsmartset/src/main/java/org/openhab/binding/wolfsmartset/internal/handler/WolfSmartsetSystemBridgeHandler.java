/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wolfsmartset.internal.config.WolfSmartsetSystemConfiguration;
import org.openhab.binding.wolfsmartset.internal.discovery.WolfSmartsetSystemDiscoveryService;
import org.openhab.binding.wolfsmartset.internal.dto.GetGuiDescriptionForGatewayDTO;
import org.openhab.binding.wolfsmartset.internal.dto.GetSystemListDTO;
import org.openhab.binding.wolfsmartset.internal.dto.GetSystemStateListDTO;
import org.openhab.binding.wolfsmartset.internal.dto.ReadFaultMessagesDTO;
import org.openhab.binding.wolfsmartset.internal.dto.SubMenuEntryWithMenuItemTabView;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
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

    private @NonNullByDefault({}) String systemId;

    private final Map<String, WolfSmartsetUnitThingHandler> unitHandlers = new ConcurrentHashMap<>();

    private @Nullable GetSystemListDTO savedSystem;
    private @Nullable List<SubMenuEntryWithMenuItemTabView> savedUnits;
    private Map<String, State> stateCache = new ConcurrentHashMap<>();

    public WolfSmartsetSystemBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        systemId = getConfigAs(WolfSmartsetSystemConfiguration.class).systemId;
        logger.debug("SystemBridge: Initializing system '{}'", systemId);
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
        var accountBridgeHandler = getAccountBridgeHandler();
        if (accountBridgeHandler != null) {
            accountBridgeHandler.scheduleRefreshJob();
        }
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
        }
    }

    /**
     * Return the associated account bridge handler
     * 
     * @return returns the {@link WolfSmartsetAccountBridgeHandler} linked to this
     *         {@link WolfSmartsetSystemBridgeHandler}
     */
    public @Nullable WolfSmartsetAccountBridgeHandler getAccountBridgeHandler() {
        var bridgeHandler = this.getBridge();
        if (bridgeHandler != null) {
            return (WolfSmartsetAccountBridgeHandler) bridgeHandler.getHandler();
        }
        return null;
    }

    /**
     * Return the subordinated unit handler
     * 
     * @return a List of {@link WolfSmartsetUnitThingHandler} with the subordinated unit handler
     */
    public Collection<WolfSmartsetUnitThingHandler> getUnitHandler() {
        return unitHandlers.values();
    }

    /**
     * Returns the list configuration of the units available for this system
     * 
     * @return a list of {@link SubMenuEntryWithMenuItemTabView} representing the available units for this system
     */
    public List<SubMenuEntryWithMenuItemTabView> getUnits() {
        List<SubMenuEntryWithMenuItemTabView> localSavedUnits = savedUnits;
        return localSavedUnits == null ? EMPTY_UNITS : localSavedUnits;
    }

    /**
     * Return the configuration of this system
     * 
     * @return {@link GetSystemListDTO} representing the this system
     */
    public @Nullable GetSystemListDTO getSystemConfig() {
        return savedSystem;
    }

    /**
     * Return the id of this system
     * 
     * @return the id of this system
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * Update the system state with the dto
     * 
     * @param systemState {@link GetSystemStateListDTO} the dto representing the current state of this system
     */
    public void updateSystemState(@Nullable GetSystemStateListDTO systemState) {
        if (systemState != null) {
            if (systemState.getIsSystemDeleted()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "System has been deleted");
            } else if (systemState.getIsSystemShareDeleted()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "System share has been removed");
            } else if (systemState.getIsSystemShareRejected()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "System share has been rejected");
            }
        }
    }

    /**
     * Process the available fault messages
     * 
     * @param faultMessages {@link ReadFaultMessagesDTO} the dto representing the list of the current faultmessages
     */
    public void updateFaultMessages(@Nullable ReadFaultMessagesDTO faultMessages) {
        if (faultMessages != null) {
            if (faultMessages.getCurrentMessages() != null) {
                for (var message : faultMessages.getCurrentMessages()) {
                    logger.warn("System {} faultmessage: {}, since {}", systemId, message.getDescription(),
                            message.getOccurTimeLocal());
                }
            }
        }
    }

    /**
     * Update the configuration of the system and the subordinated units
     * 
     * @param system {@link GetSystemListDTO} representing this system
     * @param systemDescription {@link GetGuiDescriptionForGatewayDTO} repesenting the units of this system
     */
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
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to retrieve configuration");
        }
    }

    private void updateUnitsConfiguration(GetGuiDescriptionForGatewayDTO systemDescription) {
        List<SubMenuEntryWithMenuItemTabView> listUnits = new ArrayList<>();
        var fachmannNode = systemDescription.getMenuItems().stream()
                .filter(m -> "Fachmann".equalsIgnoreCase(m.getName())).findFirst();

        if (fachmannNode.isPresent()) {
            for (var submenu : fachmannNode.get().getSubMenuEntries()) {
                for (var tabmenu : submenu.getTabViews()) {
                    listUnits.add(new SubMenuEntryWithMenuItemTabView(submenu, tabmenu));

                    var handler = unitHandlers.get(tabmenu.bundleId.toString());
                    if (handler != null) {
                        handler.updateConfiguration(submenu, tabmenu);
                    }
                }
            }

        }
        savedUnits = listUnits;
    }

    private void clearSavedState() {
        savedSystem = null;
        savedUnits = null;
        stateCache.clear();
    }
}

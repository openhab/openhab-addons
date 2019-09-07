/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_ID;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.SUPPORTED_INTERFACES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeGroups.SmartHomeGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Lukas Knoeller
 */
public class SmartHomeDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartHomeDeviceHandler.class);

    private @Nullable Connection connection;
    private @Nullable SmartHomeBaseDevice smartHomeBaseDevice;
    private Gson gson;
    private Map<String, HandlerBase> handlers = new HashMap<>();

    @Nullable
    AccountHandler accountHandler;
    Thing thing;

    public SmartHomeDeviceHandler(Thing thing, Gson gson) {
        super(thing);
        this.thing = thing;
        this.gson = gson;
    }

    synchronized public boolean setDeviceAndUpdateThingState(AccountHandler accountHandler,
            @Nullable SmartHomeBaseDevice smartHomeBaseDevice) {
        try {
            if (this.accountHandler != accountHandler) {
                this.accountHandler = accountHandler;
            }
            if (smartHomeBaseDevice == null) {
                updateStatus(ThingStatus.UNKNOWN);
                return false;
            }
            this.smartHomeBaseDevice = smartHomeBaseDevice;
            updateStatus(ThingStatus.ONLINE);

            Set<String> unusedHandlers = new HashSet<>();
            unusedHandlers.addAll(handlers.keySet());
            Map<String, List<SmartHomeCapability>> capabilities = new HashMap<>();
            GetCapabilities(capabilities, accountHandler, smartHomeBaseDevice);
            for (String interfaceName : capabilities.keySet()) {
                HandlerBase handler = handlers.get(interfaceName);
                if (handler != null) {
                    unusedHandlers.remove(interfaceName);
                } else {
                    Function<String, HandlerBase> creator = Constants.HandlerFactory.get(interfaceName);
                    if (creator != null) {
                        handler = creator.apply(interfaceName);
                        handlers.put(interfaceName, handler);
                    }
                }
                if (handler != null) {
                    handler.intialize(this, capabilities.get(interfaceName));
                }
            }
            for (String interfaceName : unusedHandlers) {
                HandlerBase handler = handlers.get(interfaceName);
                handlers.remove(interfaceName);
                handler.cleanUp();
            }
        } catch (

        IllegalArgumentException e) {
            logger.debug("Exception while adding channel {}.", e);
        }
        return true;
    }

    public @Nullable AccountHandler findAccountHandler() {
        return this.accountHandler;
    }

    public String findId() {
        String id = (String) getConfig().get(DEVICE_PROPERTY_ID);
        if (id == null) {
            return "";
        }
        return id;
    }

    @Override
    public void updateState(String channelId, State state) {
        super.updateState(new ChannelUID(getThing().getUID(), channelId), state);
    }

    @Override
    public void initialize() {
        logger.info("{} initialized", getClass().getSimpleName());
        Bridge bridge = this.getBridge();
        if (bridge != null) {
            AccountHandler account = (AccountHandler) bridge.getHandler();
            this.accountHandler = account;
            if (account != null) {
                account.addSmartHomeDeviceHandler(this);
                setDeviceAndUpdateThingState(account, smartHomeBaseDevice);
            }
        }
    }

    public void addChannelToDevice(String channelId, String itemType, ChannelTypeUID channelTypeUID) {
        Channel channel = getThing().getChannel(channelId);
        if (channel != null) {
            if (channelTypeUID.equals(channel.getChannelTypeUID()) && itemType.equals(channel.getAcceptedItemType())) {
                // channel exist with the same settings
                return;
            }
            // channel exist with other settings, remove it first
            removeChannelFromDevice(channelId);
        }
        updateThing(
                editThing().withChannel(ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId), itemType)
                        .withType(channelTypeUID).build()).build());
    }

    public void removeChannelFromDevice(String channelId) {
        updateThing(editThing().withoutChannel(new ChannelUID(getThing().getUID(), channelId)).build());
    }

    public void updateChannelStates(List<SmartHomeBaseDevice> allDevices,
            Map<String, JsonArray> applianceIdToCapabilityStates) {
        SmartHomeBaseDevice smartHomeBaseDevice = this.smartHomeBaseDevice;
        if (smartHomeBaseDevice == null) {
            updateStatus(ThingStatus.UNKNOWN);
            return;
        }
        boolean stateFound = false;
        Map<String, List<JsonObject>> mapInterfaceToStates = new HashMap<>();
        for (SmartHomeDevice shd : GetSupportedSmartHomeDevices(smartHomeBaseDevice, allDevices)) {
            JsonArray states = applianceIdToCapabilityStates.get(shd.applianceId);
            if (states == null) {
                continue;
            }
            stateFound = true;

            for (int i = 0; i < states.size(); i++) {
                String stateJson = states.get(i).getAsString();
                if (stateJson.startsWith("{") && stateJson.endsWith("}")) {
                    JsonObject state = this.gson.fromJson(stateJson, JsonObject.class);
                    String interfaceName = state.get("namespace").getAsString();
                    List<JsonObject> stateList = mapInterfaceToStates.get(interfaceName);
                    if (stateList == null) {
                        stateList = new ArrayList<>();
                        mapInterfaceToStates.put(interfaceName, stateList);
                    }
                    stateList.add(state);
                }
            }
        }
        for (HandlerBase handlerBase : handlers.values()) {
            for (String interfaceName : handlerBase.GetSupportedInterface()) {
                List<JsonObject> stateList = mapInterfaceToStates.get(interfaceName);
                if (stateList == null) {
                    stateList = new ArrayList<>();
                }
                handlerBase.updateChannels(interfaceName, stateList);
            }
        }
        if (stateFound) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AccountHandler accountHandler = this.accountHandler;
        if (accountHandler == null) {
            return;
        }
        Connection connection = accountHandler.findConnection();
        if (connection == null) {
            return;
        }

        try {
            if (command instanceof RefreshType) {
                accountHandler.forceDelayedSmartHomeStateUpdate();
                return;
            }
            SmartHomeBaseDevice smartHomeBaseDevice = this.smartHomeBaseDevice;
            if (smartHomeBaseDevice == null) {
                return;
            }
            Set<SmartHomeDevice> devices = GetSupportedSmartHomeDevices(smartHomeBaseDevice,
                    accountHandler.getLastKnownSmartHomeDevice());
            if (devices == null) {
                return;
            }
            String channelId = channelUID.getId();
            boolean forcedDelayedUpdate = false;
            for (String interfaceName : handlers.keySet()) {
                HandlerBase handlerBase = handlers.get(interfaceName);
                if (!handlerBase.hasChannel(channelId)) {
                    continue;
                }
                for (SmartHomeDevice shd : devices) {

                    String entityId = shd.entityId;
                    if (entityId == null) {
                        continue;
                    }
                    SmartHomeCapability[] capabilties = shd.capabilities;
                    if (capabilties == null) {
                        return;
                    }
                    if (!forcedDelayedUpdate) {
                        forcedDelayedUpdate = true;
                        accountHandler.forceDelayedSmartHomeStateUpdate();
                    }
                    if (handlerBase.handleCommand(connection, shd, entityId, capabilties, channelUID.getId(),
                            command)) {
                        logger.debug("Command {} sent to {}", command, shd.findId());
                    }
                }
            }
        } catch (

        Exception e) {
            logger.warn("Handle command failed {}", e);
        }

        logger.trace("Command {} received from channel '{}'", command, channelUID);

    }

    public boolean initialize(AccountHandler handler) {
        this.accountHandler = handler;
        return true;
    }

    private static void GetCapabilities(Map<String, List<SmartHomeCapability>> result, AccountHandler accountHandler,
            SmartHomeBaseDevice device) {
        if (device instanceof SmartHomeDevice) {
            SmartHomeDevice shd = (SmartHomeDevice) device;
            for (SmartHomeCapability capability : shd.capabilities) {
                List<SmartHomeCapability> list = result.get(capability.interfaceName);
                if (list == null) {
                    list = new ArrayList<SmartHomeCapability>();
                    result.put(capability.interfaceName, list);
                }
                list.add(capability);
            }
        }
        if (device instanceof SmartHomeGroup) {
            for (SmartHomeDevice shd : GetSupportedSmartHomeDevices(device,
                    accountHandler.getLastKnownSmartHomeDevice())) {
                GetCapabilities(result, accountHandler, shd);
            }
        }
    }

    public static Set<SmartHomeDevice> GetSupportedSmartHomeDevices(@Nullable SmartHomeBaseDevice baseDevice,
            List<SmartHomeBaseDevice> allDevices) {
        Set<SmartHomeDevice> result = new HashSet<>();
        if (baseDevice == null) {
            return result;
        }
        if (baseDevice instanceof SmartHomeDevice) {
            SmartHomeDevice shd = (SmartHomeDevice) baseDevice;
            boolean supportedDevice = false;
            for (SmartHomeCapability capability : shd.capabilities) {
                if (SUPPORTED_INTERFACES.contains(capability.interfaceName)) {
                    supportedDevice = true;
                    break;
                }
            }
            if (supportedDevice) {
                result.add(shd);
            }
        } else {
            SmartHomeGroup shg = (SmartHomeGroup) baseDevice;
            for (SmartHomeBaseDevice device : allDevices) {
                if (device instanceof SmartHomeDevice) {
                    SmartHomeDevice shd = (SmartHomeDevice) device;
                    if (shd.tags != null && shd.tags.tagNameToValueSetMap != null
                            && shd.tags.tagNameToValueSetMap.groupIdentity != null
                            && Arrays.asList(shd.tags.tagNameToValueSetMap.groupIdentity)
                                    .contains(shg.applianceGroupIdentifier.value)) {

                        boolean supportedDevice = false;
                        for (SmartHomeCapability capability : shd.capabilities) {
                            if (SUPPORTED_INTERFACES.contains(capability.interfaceName)) {
                                supportedDevice = true;
                                break;
                            }
                        }
                        if (supportedDevice) {
                            result.add(shd);
                            continue;
                        }
                    }
                }
            }
        }
        return result;
    }

    public @Nullable StateDescription findStateDescription(Channel channel, StateDescription originalStateDescription,
            @Nullable Locale locale) {
        String channelId = channel.getUID().getId();
        for (HandlerBase handler : handlers.values()) {
            if (handler.hasChannel(channelId)) {
                return handler.findStateDescription(channelId, originalStateDescription, locale);
            }
        }
        return null;
    }

}

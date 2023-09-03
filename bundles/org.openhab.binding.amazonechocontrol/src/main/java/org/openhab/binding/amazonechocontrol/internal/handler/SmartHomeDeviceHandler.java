/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_ID;
import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.SUPPORTED_INTERFACES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroupIdentifiers;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroupIdentity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroups.SmartHomeGroup;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeTags;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.smarthome.Constants;
import org.openhab.binding.amazonechocontrol.internal.smarthome.HandlerBase;
import org.openhab.binding.amazonechocontrol.internal.smarthome.HandlerBase.ChannelInfo;
import org.openhab.binding.amazonechocontrol.internal.smarthome.HandlerBase.UpdateChannelResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class SmartHomeDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartHomeDeviceHandler.class);

    private @Nullable SmartHomeBaseDevice smartHomeBaseDevice;
    private final Gson gson;
    private final Map<String, HandlerBase> handlers = new HashMap<>();
    private final Map<String, JsonArray> lastStates = new HashMap<>();

    public SmartHomeDeviceHandler(Thing thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    public synchronized void setDeviceAndUpdateThingState(AccountHandler accountHandler,
            @Nullable SmartHomeBaseDevice smartHomeBaseDevice) {
        if (smartHomeBaseDevice == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Can't find smarthomeBaseDevice");
            return;
        }
        boolean changed = this.smartHomeBaseDevice == null;
        this.smartHomeBaseDevice = smartHomeBaseDevice;

        Set<String> unusedChannels = new HashSet<>();
        thing.getChannels().forEach(channel -> unusedChannels.add(channel.getUID().getId()));

        Set<String> unusedHandlers = new HashSet<>(handlers.keySet());

        Map<String, List<SmartHomeCapability>> capabilities = new HashMap<>();
        getCapabilities(capabilities, accountHandler, smartHomeBaseDevice);

        ThingBuilder thingBuilder = editThing();

        for (String interfaceName : capabilities.keySet()) {
            HandlerBase handler = handlers.get(interfaceName);
            if (handler != null) {
                unusedHandlers.remove(interfaceName);
            } else {
                Function<SmartHomeDeviceHandler, HandlerBase> creator = Constants.HANDLER_FACTORY.get(interfaceName);
                if (creator != null) {
                    handler = creator.apply(this);
                    handlers.put(interfaceName, handler);
                }
            }
            if (handler != null) {
                Collection<ChannelInfo> required = handler
                        .initialize(capabilities.getOrDefault(interfaceName, List.of()));
                for (ChannelInfo channelInfo : required) {
                    unusedChannels.remove(channelInfo.channelId);
                    if (addChannelToDevice(thingBuilder, channelInfo.channelId, channelInfo.itemType,
                            channelInfo.channelTypeUID)) {
                        changed = true;
                    }
                }
            }
        }

        unusedHandlers.forEach(handlers::remove);
        if (!unusedChannels.isEmpty()) {
            changed = true;
            unusedChannels.stream().map(id -> new ChannelUID(thing.getUID(), id)).forEach(thingBuilder::withoutChannel);
        }

        if (changed) {
            updateThing(thingBuilder.build());
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Thing has changed.");
            accountHandler.forceDelayedSmartHomeStateUpdate(getId());
        }
    }

    public String getId() {
        String id = (String) getConfig().get(DEVICE_PROPERTY_ID);
        if (id == null) {
            return "";
        }
        return id;
    }

    @Override
    public void updateState(String channelId, State state) {
        super.updateState(new ChannelUID(thing.getUID(), channelId), state);
    }

    @Override
    public void initialize() {
        AccountHandler accountHandler = getAccountHandler();
        if (accountHandler != null) {
            accountHandler.addSmartHomeDeviceHandler(this);
            setDeviceAndUpdateThingState(accountHandler, smartHomeBaseDevice);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridgehandler not found");
        }
    }

    private boolean addChannelToDevice(ThingBuilder thingBuilder, String channelId, String itemType,
            ChannelTypeUID channelTypeUID) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            if (channelTypeUID.equals(channel.getChannelTypeUID()) && itemType.equals(channel.getAcceptedItemType())) {
                // channel exist with the same settings
                return false;
            }
            // channel exist with other settings, remove it first
            thingBuilder.withoutChannel(channel.getUID());
        }
        thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thing.getUID(), channelId), itemType)
                .withType(channelTypeUID).build());
        return true;
    }

    public void updateChannelStates(List<SmartHomeBaseDevice> allDevices,
            Map<String, JsonArray> applianceIdToCapabilityStates) {
        logger.trace("Updating {} with {}", allDevices, applianceIdToCapabilityStates);
        AccountHandler accountHandler = getAccountHandler();
        SmartHomeBaseDevice smartHomeBaseDevice = this.smartHomeBaseDevice;
        if (smartHomeBaseDevice == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Can't find smarthomeBaseDevice!");
            return;
        }

        boolean stateFound = false;
        Map<String, List<JsonObject>> mapInterfaceToStates = new HashMap<>();
        SmartHomeDevice firstDevice = null;
        for (SmartHomeDevice smartHomeDevice : getSupportedSmartHomeDevices(smartHomeBaseDevice, allDevices)) {
            String applianceId = smartHomeDevice.applianceId;
            if (applianceId == null) {
                continue;
            }
            JsonArray states = applianceIdToCapabilityStates.get(applianceId);
            if (states != null) {
                stateFound = true;
                if (smartHomeBaseDevice.isGroup()) {
                    // for groups, store the last state of all devices
                    lastStates.put(applianceId, states);
                }
            } else {
                states = lastStates.get(applianceId);
                if (states == null) {
                    continue;
                }
            }
            if (firstDevice == null) {
                firstDevice = smartHomeDevice;
            }
            for (JsonElement stateElement : states) {
                String stateJson = stateElement.getAsString();
                if (stateJson.startsWith("{") && stateJson.endsWith("}")) {
                    JsonObject state = Objects.requireNonNull(gson.fromJson(stateJson, JsonObject.class));
                    String interfaceName = Objects.requireNonNullElse(state.get("namespace"), JsonNull.INSTANCE)
                            .getAsString();
                    Objects.requireNonNull(mapInterfaceToStates.computeIfAbsent(interfaceName, k -> new ArrayList<>()))
                            .add(state);
                }
            }
        }

        for (HandlerBase handlerBase : handlers.values()) {
            UpdateChannelResult result = new UpdateChannelResult();
            for (String interfaceName : handlerBase.getSupportedInterface()) {
                List<JsonObject> stateList = mapInterfaceToStates.get(interfaceName);
                if (stateList != null) {
                    try {
                        handlerBase.updateChannels(interfaceName, stateList, result);
                    } catch (Exception e) {
                        // We catch all exceptions, otherwise all other things are not updated!
                        logger.debug("Updating states failed", e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                e.getLocalizedMessage());
                    }
                }
            }

            if (result.needSingleUpdate && smartHomeBaseDevice instanceof SmartHomeDevice smartHomeDevice
                    && accountHandler != null) {
                accountHandler.forceDelayedSmartHomeStateUpdate(smartHomeDevice.findId());
            }
        }

        if (stateFound) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "State not found");
        }
    }

    private @Nullable AccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof AccountHandler accountHandler) {
                return accountHandler;
            }
        }

        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            logger.debug("accountHandler is null in {}", thing.getUID());
            return;
        }
        Connection connection = accountHandler.findConnection();
        if (connection == null) {
            logger.debug("connection is null in {}", thing.getUID());
            return;
        }

        try {
            if (command instanceof RefreshType) {
                accountHandler.forceDelayedSmartHomeStateUpdate(getId());
                return;
            }
            SmartHomeBaseDevice smartHomeBaseDevice = this.smartHomeBaseDevice;
            if (smartHomeBaseDevice == null) {
                logger.debug("smarthomeBaseDevice is null in {}", thing.getUID());
                return;
            }
            Set<SmartHomeDevice> devices = getSupportedSmartHomeDevices(smartHomeBaseDevice,
                    accountHandler.getLastKnownSmartHomeDevices());
            String channelId = channelUID.getId();

            for (String interfaceName : handlers.keySet()) {
                HandlerBase handlerBase = handlers.get(interfaceName);
                if (handlerBase == null || !handlerBase.hasChannel(channelId)) {
                    continue;
                }
                for (SmartHomeDevice smartHomeDevice : devices) {
                    String entityId = smartHomeDevice.entityId;
                    if (entityId == null) {
                        continue;
                    }
                    accountHandler.forceDelayedSmartHomeStateUpdate(getId()); // block updates
                    if (handlerBase.handleCommand(connection, smartHomeDevice, entityId,
                            smartHomeDevice.getCapabilities(), channelUID.getId(), command)) {
                        accountHandler.forceDelayedSmartHomeStateUpdate(getId()); // force update again to restart
                        // update timer
                        logger.debug("Command {} sent to {}", command, smartHomeDevice.findId());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Handle command failed", e);
        }
    }

    private static void getCapabilities(Map<String, List<SmartHomeCapability>> result, AccountHandler accountHandler,
            SmartHomeBaseDevice device) {
        if (device instanceof SmartHomeDevice smartHomeDevice) {
            for (SmartHomeCapability capability : smartHomeDevice.getCapabilities()) {
                String interfaceName = capability.interfaceName;
                if (interfaceName != null) {
                    Objects.requireNonNull(result.computeIfAbsent(interfaceName, name -> new ArrayList<>()))
                            .add(capability);
                }
            }
        }
        if (device instanceof SmartHomeGroup) {
            for (SmartHomeDevice smartHomeDevice : getSupportedSmartHomeDevices(device,
                    accountHandler.getLastKnownSmartHomeDevices())) {
                getCapabilities(result, accountHandler, smartHomeDevice);
            }
        }
    }

    public static Set<SmartHomeDevice> getSupportedSmartHomeDevices(@Nullable SmartHomeBaseDevice baseDevice,
            List<SmartHomeBaseDevice> allDevices) {
        if (baseDevice == null) {
            return Collections.emptySet();
        }
        Set<SmartHomeDevice> result = new HashSet<>();
        if (baseDevice instanceof SmartHomeDevice smartHomeDevice) {
            if (smartHomeDevice.getCapabilities().stream().map(capability -> capability.interfaceName)
                    .anyMatch(SUPPORTED_INTERFACES::contains)) {
                result.add(smartHomeDevice);
            }
        } else {
            SmartHomeGroup smartHomeGroup = (SmartHomeGroup) baseDevice;
            for (SmartHomeBaseDevice device : allDevices) {
                if (device instanceof SmartHomeDevice smartHomeDevice) {
                    JsonSmartHomeTags.JsonSmartHomeTag tags = smartHomeDevice.tags;
                    if (tags != null) {
                        JsonSmartHomeGroupIdentity.SmartHomeGroupIdentity tagNameToValueSetMap = tags.tagNameToValueSetMap;
                        JsonSmartHomeGroupIdentifiers.SmartHomeGroupIdentifier applianceGroupIdentifier = smartHomeGroup.applianceGroupIdentifier;
                        if (tagNameToValueSetMap != null) {
                            List<String> groupIdentity = Objects.requireNonNullElse(tagNameToValueSetMap.groupIdentity,
                                    List.of());
                            if (applianceGroupIdentifier != null && applianceGroupIdentifier.value != null
                                    && groupIdentity.contains(applianceGroupIdentifier.value)) {
                                if (smartHomeDevice.getCapabilities().stream()
                                        .map(capability -> capability.interfaceName)
                                        .anyMatch(SUPPORTED_INTERFACES::contains)) {
                                    result.add(smartHomeDevice);
                                }
                            }
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

/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroups.SmartHomeGroup;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.smarthome.Constants;
import org.openhab.binding.amazonechocontrol.internal.smarthome.HandlerBase;
import org.openhab.binding.amazonechocontrol.internal.smarthome.HandlerBase.ChannelInfo;
import org.openhab.binding.amazonechocontrol.internal.smarthome.HandlerBase.UpdateChannelResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class SmartHomeDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartHomeDeviceHandler.class);

    private @Nullable SmartHomeBaseDevice smartHomeBaseDevice;
    private final Gson gson;
    private final Map<String, @Nullable HandlerBase> handlers = new HashMap<>();
    private final Map<String, @Nullable JsonArray> lastStates = new HashMap<>();

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
                Supplier<HandlerBase> creator = Constants.HANDLER_FACTORY.get(interfaceName);
                if (creator != null) {
                    handler = creator.get();
                    handlers.put(interfaceName, handler);
                }
            }
            if (handler != null) {
                Collection<ChannelInfo> required = handler.initialize(this, capabilities.get(interfaceName));
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
        AccountHandler accountHandler = getAccountHandler();
        SmartHomeBaseDevice smartHomeBaseDevice = this.smartHomeBaseDevice;
        if (smartHomeBaseDevice == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Can't find smarthomeBaseDevice!");
            return;
        }

        boolean stateFound = false;
        Map<String, List<JsonObject>> mapInterfaceToStates = new HashMap<>();
        SmartHomeDevice firstDevice = null;
        for (SmartHomeDevice shd : getSupportedSmartHomeDevices(smartHomeBaseDevice, allDevices)) {
            JsonArray states = applianceIdToCapabilityStates.get(shd.applianceId);
            String applianceId = shd.applianceId;
            if (applianceId == null) {
                continue;
            }
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
                firstDevice = shd;
            }
            for (JsonElement stateElement : states) {
                String stateJson = stateElement.getAsString();
                if (stateJson.startsWith("{") && stateJson.endsWith("}")) {
                    JsonObject state = gson.fromJson(stateJson, JsonObject.class);
                    String interfaceName = state.get("namespace").getAsString();
                    mapInterfaceToStates.computeIfAbsent(interfaceName, k -> new ArrayList<>()).add(state);
                }
            }
        }
        for (HandlerBase handlerBase : handlers.values()) {
            if (handlerBase == null) {
                continue;
            }
            UpdateChannelResult result = new UpdateChannelResult();

            for (String interfaceName : handlerBase.getSupportedInterface()) {
                List<JsonObject> stateList = mapInterfaceToStates.getOrDefault(interfaceName, Collections.emptyList());
                try {
                    handlerBase.updateChannels(interfaceName, stateList, result);
                } catch (Exception e) {
                    // We catch all exceptions, otherwise all other things are not updated!
                    logger.debug("Updating states failed", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                }
            }

            if (result.needSingleUpdate && smartHomeBaseDevice instanceof SmartHomeDevice && accountHandler != null) {
                SmartHomeDevice shd = (SmartHomeDevice) smartHomeBaseDevice;
                accountHandler.forceDelayedSmartHomeStateUpdate(shd.findId());
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
            if (bridgeHandler instanceof AccountHandler) {
                return (AccountHandler) bridgeHandler;
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
                for (SmartHomeDevice shd : devices) {
                    String entityId = shd.entityId;
                    if (entityId == null) {
                        continue;
                    }
                    SmartHomeCapability[] capabilities = shd.capabilities;
                    if (capabilities == null) {
                        logger.debug("capabilities is null in {}", thing.getUID());
                        return;
                    }
                    accountHandler.forceDelayedSmartHomeStateUpdate(getId()); // block updates
                    if (handlerBase.handleCommand(connection, shd, entityId, capabilities, channelUID.getId(),
                            command)) {
                        accountHandler.forceDelayedSmartHomeStateUpdate(getId()); // force update again to restart
                        // update timer
                        logger.debug("Command {} sent to {}", command, shd.findId());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Handle command failed", e);
        }
    }

    private static void getCapabilities(Map<String, List<SmartHomeCapability>> result, AccountHandler accountHandler,
            SmartHomeBaseDevice device) {
        if (device instanceof SmartHomeDevice) {
            SmartHomeDevice shd = (SmartHomeDevice) device;
            SmartHomeCapability[] capabilities = shd.capabilities;
            if (capabilities == null) {
                return;
            }
            for (SmartHomeCapability capability : capabilities) {
                String interfaceName = capability.interfaceName;
                if (interfaceName != null) {
                    result.computeIfAbsent(interfaceName, name -> new ArrayList<>()).add(capability);
                }
            }
        }
        if (device instanceof SmartHomeGroup) {
            for (SmartHomeDevice shd : getSupportedSmartHomeDevices(device,
                    accountHandler.getLastKnownSmartHomeDevices())) {
                getCapabilities(result, accountHandler, shd);
            }
        }
    }

    public static Set<SmartHomeDevice> getSupportedSmartHomeDevices(@Nullable SmartHomeBaseDevice baseDevice,
            List<SmartHomeBaseDevice> allDevices) {
        if (baseDevice == null) {
            return Collections.emptySet();
        }
        Set<SmartHomeDevice> result = new HashSet<>();
        if (baseDevice instanceof SmartHomeDevice) {
            SmartHomeDevice shd = (SmartHomeDevice) baseDevice;
            SmartHomeCapability[] capabilities = shd.capabilities;
            if (capabilities != null) {
                if (Arrays.stream(capabilities).map(capability -> capability.interfaceName)
                        .anyMatch(SUPPORTED_INTERFACES::contains)) {
                    result.add(shd);
                }
            }
        } else {
            SmartHomeGroup shg = (SmartHomeGroup) baseDevice;
            for (SmartHomeBaseDevice device : allDevices) {
                if (device instanceof SmartHomeDevice) {
                    SmartHomeDevice shd = (SmartHomeDevice) device;
                    if (shd.tags != null && shd.tags.tagNameToValueSetMap != null
                            && shd.tags.tagNameToValueSetMap.groupIdentity != null
                            && shg.applianceGroupIdentifier != null && shg.applianceGroupIdentifier.value != null
                            && Arrays.asList(shd.tags.tagNameToValueSetMap.groupIdentity)
                                    .contains(shg.applianceGroupIdentifier.value)) {
                        SmartHomeCapability[] capabilities = shd.capabilities;
                        if (capabilities != null) {
                            if (Arrays.stream(capabilities).map(capability -> capability.interfaceName)
                                    .anyMatch(SUPPORTED_INTERFACES::contains)) {
                                result.add(shd);
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
            if (handler != null && handler.hasChannel(channelId)) {
                return handler.findStateDescription(channelId, originalStateDescription, locale);
            }
        }
        return null;
    }
}

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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.BINDING_ID;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_CONTROLLER_BRIGHTNESS;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_CONTROLLER_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_CONTROLLER_COLOR_TEMPERATURE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_CONTROLLER_POWER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_ID;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_TURN_OFF;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_TURN_ON;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_BRIGHTNESS;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR_TEMPERATURE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_POWER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_DIMMER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_STRING;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_SWITCH;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.SUPPORTED_INTERFACES;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroups.SmartHomeGroup;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lukas Knoeller
 */

public class SmartHomeDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartHomeDeviceHandler.class);

    private @Nullable Connection connection;
    private @Nullable SmartHomeBaseDevice smartHomeBaseDevice;

    Storage<String> stateStorage;

    @Nullable
    AccountHandler accountHandler;
    Thing thing;

    public SmartHomeDeviceHandler(Thing thing, Storage<String> storage) {
        super(thing);
        this.thing = thing;
        this.stateStorage = storage;
    }

    public boolean setDeviceAndUpdateThingState(AccountHandler accountHandler,
            @Nullable SmartHomeBaseDevice smartHomeBaseDevice) {
        if (this.accountHandler != accountHandler) {
            this.accountHandler = accountHandler;
        }
        if (smartHomeBaseDevice == null) {
            updateStatus(ThingStatus.UNKNOWN);
            return false;
        }
        this.smartHomeBaseDevice = smartHomeBaseDevice;
        updateStatus(ThingStatus.ONLINE);

        Set<String> capabilities = GetCapabilities(accountHandler, smartHomeBaseDevice);
        try {
            if (capabilities.contains(INTERFACE_POWER)) {
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_POWER), ITEM_TYPE_SWITCH,
                        new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_POWER));
            }

            if (capabilities.contains(INTERFACE_BRIGHTNESS)) {
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_BRIGHTNESS), ITEM_TYPE_DIMMER,
                        new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_BRIGHTNESS));
            }

            if (capabilities.contains(INTERFACE_COLOR_TEMPERATURE)) {
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_COLOR_TEMPERATURE),
                        ITEM_TYPE_STRING, new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_COLOR_TEMPERATURE));
            }

            if (capabilities.contains(INTERFACE_COLOR)) {
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_COLOR), ITEM_TYPE_STRING,
                        new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_COLOR));
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Exception while adding channel {}.", e);
        }

        // try {
        // String state = null;
        // int brightness = -1;
        // String color = null;
        // connection = accountHandler.findConnection();
        // if (thing.getProperties().keySet().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE + 0)) {
        // state = connection.getLightGroupState(thing);
        // brightness = connection.getLightGroupBrightness(thing);
        // } else {
        // state = connection.getBulbState(thing);
        // brightness = connection.getBulbBrightness(thing);
        // color = connection.getBulbColor(thing);
        // }
        // if (state != null) {
        // updateBulbState(thing.getChannel(CHANNEL_CONTROLLER_POWER).getUID(), state);
        // }
        // if (brightness != -1) {
        // updateBrightness(thing.getChannel(CHANNEL_CONTROLLER_BRIGHTNESS).getUID(),
        // brightness);
        // }
        // if (color != null) {
        // updateColor(thing.getChannel(CHANNEL_CONTROLLER_COLOR).getUID(), color);
        // }
        // } catch (IOException | URISyntaxException e) {
        // logger.error(e.getMessage());
        // }
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

    public void addChannelToDevice(ChannelUID channelUID, String itemType, ChannelTypeUID channelTypeUID) {
        updateThing(editThing()
                .withChannel(ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID).build()).build());
    }

    public void updateState(AccountHandler accountHandler, @Nullable SmartHomeBaseDevice smartHomeBaseDevice) {
        if (!setDeviceAndUpdateThingState(accountHandler, smartHomeBaseDevice)) {
            this.logger.debug("Handle updateState {} aborted: Not online", this.getThing().getUID());
            return;
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

            SmartHomeBaseDevice smartHomeBaseDevice = this.smartHomeBaseDevice;
            if (smartHomeBaseDevice == null) {
                return;
            }
            Set<SmartHomeDevice> devices = null;
            if (smartHomeBaseDevice instanceof SmartHomeDevice) {
                devices = new HashSet<>();
                devices.add((SmartHomeDevice) smartHomeBaseDevice);
            } else if (smartHomeBaseDevice instanceof SmartHomeGroup) {
                devices = GetSupportedSmartHomeDevices((SmartHomeGroup) smartHomeBaseDevice,
                        accountHandler.getLastKnownSmartHomeDevice());
            }
            if (devices == null) {
                return;
            }
            String channelId = channelUID.getId();
            for (SmartHomeDevice shd : devices) {
                String entityId = shd.entityId;
                if (entityId == null) {
                    continue;
                }
                if (channelId.equals(CHANNEL_CONTROLLER_POWER)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_POWER)) {
                    if (command instanceof OnOffType) {

                        if (command.equals(OnOffType.ON)) {
                            connection.smartHomeCommand(entityId, DEVICE_TURN_ON);
                        } else {
                            connection.smartHomeCommand(entityId, DEVICE_TURN_OFF);
                        }
                    }
                }
                if (channelId.equals(CHANNEL_CONTROLLER_COLOR)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_COLOR)) {
                    if (command instanceof StringType) {
                        String commandText = ((StringType) command).toFullString();
                        if (StringUtils.isNotEmpty(commandText)) {
                            connection.smartHomeCommand(entityId, "setColor", "color", commandText);
                        }
                    }
                }
                if (channelId.equals(CHANNEL_CONTROLLER_COLOR_TEMPERATURE)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_COLOR_TEMPERATURE)) {
                    if (command instanceof StringType) {
                        String commandText = ((StringType) command).toFullString();
                        if (StringUtils.isNotEmpty(commandText)) {
                            connection.smartHomeCommand(entityId, "setColorTemperature", "colorTemperatureName",
                                    commandText);
                        }
                    }
                }
                if (channelId.equals(CHANNEL_CONTROLLER_BRIGHTNESS)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_BRIGHTNESS)) {
                    if (command instanceof PercentType) {
                        connection.smartHomeCommand(entityId, "setBrightness", "brightness",
                                ((PercentType) command).floatValue() / 100);
                    }
                }

            }

        } catch (Exception e) {
            logger.warn("Handle command failed {}", e);
        }

        logger.trace("Command {} received from channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            updateState(this.accountHandler, this.smartHomeBaseDevice);
        }
    }

    public void updateBulbState(ChannelUID channelUID, String command) {
        if (channelUID == null) {
            logger.error("No channelUID specified. Could not update state.");
        } else {
            if (command.equals("ON")) {
                updateState(channelUID, OnOffType.ON);
            } else if (command.equals("OFF")) {
                updateState(channelUID, OnOffType.OFF);
            }
        }
    }

    public void updateBrightness(ChannelUID channelUID, int brightness) {
        if (channelUID == null) {
            logger.error("No channelUID specified. Could not update brightness.");
        } else {
            updateState(channelUID, new PercentType(brightness));
        }
    }

    public void updateColor(ChannelUID channelUID, String color) {
        updateState(channelUID, new StringType(color));
    }

    public boolean initialize(AccountHandler handler) {
        updateState(CHANNEL_CONTROLLER_POWER, OnOffType.OFF);
        if (this.accountHandler != handler) {
            this.accountHandler = handler;
        }
        return true;
    }

    private static Set<String> GetCapabilities(AccountHandler accountHandler, SmartHomeBaseDevice device) {
        Set<String> result = new HashSet<>();
        if (device instanceof SmartHomeDevice) {
            SmartHomeDevice shd = (SmartHomeDevice) device;
            for (SmartHomeCapability capability : shd.capabilities) {
                result.add(capability.interfaceName);
            }
        }
        if (device instanceof SmartHomeGroup) {
            for (SmartHomeDevice shd : GetSupportedSmartHomeDevices((SmartHomeGroup) device,
                    accountHandler.getLastKnownSmartHomeDevice())) {
                for (SmartHomeCapability capability : shd.capabilities) {
                    result.add(capability.interfaceName);
                }
            }
        }
        return result;
    }

    public static Set<SmartHomeDevice> GetSupportedSmartHomeDevices(SmartHomeGroup shg,
            List<SmartHomeBaseDevice> allDevices) {
        Set<SmartHomeDevice> result = new HashSet<>();
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
        return result;
    }
}

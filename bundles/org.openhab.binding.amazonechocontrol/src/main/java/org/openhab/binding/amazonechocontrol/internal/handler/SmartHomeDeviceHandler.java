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
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_CONTROLLER_COLOR_NAME;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_CONTROLLER_COLOR_TEMPERATURE_NAME;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_CONTROLLER_POWER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_ID;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_TURN_OFF;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_TURN_ON;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_BRIGHTNESS;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR_TEMPERATURE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_POWER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_SECURITY_PANEL;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_DIMMER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_STRING;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_SWITCH;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.SUPPORTED_INTERFACES;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroups.SmartHomeGroup;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
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

    @Nullable
    AccountHandler accountHandler;
    Thing thing;

    public SmartHomeDeviceHandler(Thing thing, Gson gson) {
        super(thing);
        this.thing = thing;
        this.gson = gson;
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
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_COLOR_TEMPERATURE_NAME),
                        ITEM_TYPE_STRING, new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_COLOR_TEMPERATURE_NAME));
            }

            if (capabilities.contains(INTERFACE_COLOR)) {
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_COLOR), ITEM_TYPE_COLOR,
                        new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_COLOR));
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_COLOR_NAME), ITEM_TYPE_STRING,
                        new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_COLOR_NAME));
            }

            if (capabilities.contains(INTERFACE_SECURITY_PANEL)) {
                addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_POWER), ITEM_TYPE_SWITCH,
                        new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROLLER_POWER));
            }
        } catch (IllegalArgumentException e) {
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

    public void updateState(List<SmartHomeBaseDevice> allDevices,
            Map<String, JsonArray> applianceIdToCapabilityStates) {
        SmartHomeBaseDevice smartHomeBaseDevice = this.smartHomeBaseDevice;
        if (smartHomeBaseDevice == null) {
            updateStatus(ThingStatus.UNINITIALIZED);
            return;
        }
        Boolean power = null;
        HSBType color = null;
        Integer brightness = null;
        String colorTemperaturName = null;
        String colorName = null;
        boolean stateFound = false;
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
                    if (INTERFACE_POWER.equals(interfaceName)) {
                        String value = state.get("value").getAsString();
                        // For groups take true if all true
                        if ("ON".equals(value)) {
                            power = true;
                        } else if (power == null) {
                            power = false;
                        }
                    } else if (INTERFACE_BRIGHTNESS.equals(interfaceName)) {
                        int value = state.get("value").getAsInt();
                        // For groups take the maximum
                        if (brightness == null) {
                            brightness = value;
                        } else if (value > brightness) {
                            brightness = value;
                        }
                    } else if (INTERFACE_COLOR.equals(interfaceName)) {
                        JsonObject value = state.get("value").getAsJsonObject();
                        // For groups take the maximum
                        if (color == null) {
                            color = new HSBType(new DecimalType(value.get("hue").getAsInt()),
                                    new PercentType(value.get("saturation").getAsInt() * 100),
                                    new PercentType(value.get("brightness").getAsInt() * 100));
                        }
                    } else if ("Alexa.ColorPropertiesController".equals(interfaceName)) {
                        Set<String> capabilities = GetCapabilities(accountHandler, smartHomeBaseDevice);
                        if (capabilities.contains(INTERFACE_COLOR_TEMPERATURE)) {
                            // For groups take the first
                            if (colorTemperaturName == null) {
                                colorTemperaturName = state.get("value").getAsJsonObject().get("name").getAsString();
                            }
                        } else if (capabilities.contains(INTERFACE_COLOR)) {
                            // For groups take the first
                            if (colorName == null) {
                                colorName = state.get("value").getAsJsonObject().get("name").getAsString();
                            }
                        }
                    }
                }
            }
            if (this.isLinked(CHANNEL_CONTROLLER_POWER)) {
                updateState(CHANNEL_CONTROLLER_POWER,
                        power == null ? UnDefType.UNDEF : (power ? OnOffType.ON : OnOffType.OFF));
            }
            if (this.isLinked(CHANNEL_CONTROLLER_BRIGHTNESS)) {
                updateState(CHANNEL_CONTROLLER_BRIGHTNESS,
                        brightness == null ? UnDefType.UNDEF : new PercentType(brightness));
            }
            if (this.isLinked(CHANNEL_CONTROLLER_COLOR_TEMPERATURE_NAME)) {
                updateState(CHANNEL_CONTROLLER_COLOR_TEMPERATURE_NAME,
                        colorTemperaturName == null ? UnDefType.UNDEF : new StringType(colorTemperaturName));
            }
            if (this.isLinked(CHANNEL_CONTROLLER_COLOR)) {
                updateState(CHANNEL_CONTROLLER_COLOR, color == null ? UnDefType.UNDEF : color);
            }
            if (this.isLinked(CHANNEL_CONTROLLER_COLOR_NAME)) {
                updateState(CHANNEL_CONTROLLER_COLOR_NAME,
                        colorName == null ? UnDefType.UNDEF : new StringType(colorName));
            }
            if (stateFound) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.UNKNOWN);
            }
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
            for (SmartHomeDevice shd : devices) {
                String entityId = shd.entityId;
                if (entityId == null) {
                    continue;
                }
                if (channelId.equals(CHANNEL_CONTROLLER_POWER)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_POWER)) {
                    if (command instanceof OnOffType) {
                        accountHandler.forceDelayedSmartHomeStateUpdate();
                        if (command.equals(OnOffType.ON)) {
                            connection.smartHomeCommand(entityId, DEVICE_TURN_ON);
                        } else {
                            connection.smartHomeCommand(entityId, DEVICE_TURN_OFF);
                        }
                    }
                }
                if (channelId.equals(CHANNEL_CONTROLLER_COLOR)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_COLOR)) {
                    if (command instanceof HSBType) {
                        accountHandler.forceDelayedSmartHomeStateUpdate();
                        HSBType color = ((HSBType) command);
                        JsonObject colorObject = new JsonObject();
                        colorObject.addProperty("hue", color.getHue());
                        colorObject.addProperty("saturation", color.getSaturation().floatValue() / 100);
                        colorObject.addProperty("brightness", color.getBrightness().floatValue() / 100);
                        String colorJson = colorObject.toString();
                        connection.smartHomeCommand(entityId, "setColor", "color", colorObject);
                    }
                }
                if (channelId.equals(CHANNEL_CONTROLLER_COLOR_NAME)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_COLOR)) {
                    if (command instanceof StringType) {
                        String colorName = ((StringType) command).toFullString();
                        if (StringUtils.isNotEmpty(colorName)) {
                            accountHandler.forceDelayedSmartHomeStateUpdate();
                            connection.smartHomeCommand(entityId, "setColor", "colorName", colorName);
                        }
                    }
                }
                if (channelId.equals(CHANNEL_CONTROLLER_COLOR_TEMPERATURE_NAME)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_COLOR_TEMPERATURE)) {
                    if (command instanceof StringType) {
                        String colorTemperatureName = ((StringType) command).toFullString();
                        if (StringUtils.isNotEmpty(colorTemperatureName)) {
                            accountHandler.forceDelayedSmartHomeStateUpdate();
                            connection.smartHomeCommand(entityId, "setColorTemperature", "colorTemperatureName",
                                    colorTemperatureName);
                        }
                    }
                }
                if (channelId.equals(CHANNEL_CONTROLLER_BRIGHTNESS)
                        && GetCapabilities(accountHandler, shd).contains(INTERFACE_BRIGHTNESS)) {
                    if (command instanceof PercentType) {
                        accountHandler.forceDelayedSmartHomeStateUpdate();
                        connection.smartHomeCommand(entityId, "setBrightness", "brightness",
                                ((PercentType) command).floatValue() / 100);
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Handle command failed {}", e);
        }

        logger.trace("Command {} received from channel '{}'", command, channelUID);

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
            for (SmartHomeDevice shd : GetSupportedSmartHomeDevices(device,
                    accountHandler.getLastKnownSmartHomeDevice())) {
                for (SmartHomeCapability capability : shd.capabilities) {
                    result.add(capability.interfaceName);
                }
            }
        }
        return result;
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
}

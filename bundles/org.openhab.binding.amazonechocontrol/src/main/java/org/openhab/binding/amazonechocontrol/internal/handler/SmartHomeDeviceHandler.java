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
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_LIGHT_BRIGHTNESS;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_LIGHT_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_LIGHT_WHITE_TEMPERATURE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_STATE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_LIGHT_ENTITY_ID;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_PROPERTY_LIGHT_SUBDEVICE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_TURN_OFF;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DEVICE_TURN_ON;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_BRIGHTNESS;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_COLOR_TEMPERATURE;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.INTERFACE_POWER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_DIMMER;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_STRING;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.ITEM_TYPE_SWITCH;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lukas Knoeller
 */

public class SmartHomeDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartHomeDeviceHandler.class);

    private @Nullable ScheduledFuture<?> updateStateJob;
    private @Nullable Connection connection;
    private @Nullable SmartHomeDevice smartHomeDevice;

    Storage<String> stateStorage;

    @Nullable
    AccountHandler accountHandler;
    Thing thing;

    public SmartHomeDeviceHandler(Thing thing, Storage<String> storage) {
        super(thing);
        this.thing = thing;
        this.stateStorage = storage;
    }

    public @Nullable AccountHandler findAccountHandler() {
        return this.accountHandler;
    }

    public @Nullable SmartHomeDevice findSmartHomeDevice() {
        return this.smartHomeDevice;
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
                updateStatus(ThingStatus.ONLINE);
            }

            List<Thing> things = account.getThing().getThings();
            for (Thing thing : things) {
                for (String key : thing.getProperties().values()) {
                    try {
                        if (key.contains(INTERFACE_POWER)) {
                            addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_STATE), ITEM_TYPE_SWITCH,
                                    new ChannelTypeUID(BINDING_ID, CHANNEL_STATE));
                        }

                        if (key.contains(INTERFACE_BRIGHTNESS)) {
                            addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_BRIGHTNESS),
                                    ITEM_TYPE_DIMMER, new ChannelTypeUID(BINDING_ID, CHANNEL_LIGHT_BRIGHTNESS));
                        }

                        if (key.contains(INTERFACE_COLOR_TEMPERATURE)) {
                            addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_WHITE_TEMPERATURE),
                                    ITEM_TYPE_STRING, new ChannelTypeUID(BINDING_ID, CHANNEL_LIGHT_WHITE_TEMPERATURE));
                        }

                        if (key.contains(INTERFACE_COLOR)) {
                            addChannelToDevice(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_COLOR), ITEM_TYPE_STRING,
                                    new ChannelTypeUID(BINDING_ID, CHANNEL_LIGHT_COLOR));
                        }
                    } catch (IllegalArgumentException e) {
                        logger.debug("Exception while adding channel {}.", e);
                    }
                }
            }

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (account != null) {
                        List<Thing> things = account.getThing().getThings();
                        for (Thing thing : things) {
                            try {
                                String state = null;
                                int brightness = -1;
                                String color = null;
                                connection = accountHandler.findConnection();
                                if (thing.getProperties().keySet().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE + 0)) {
                                    state = connection.getLightGroupState(thing);
                                    brightness = connection.getLightGroupBrightness(thing);
                                } else {
                                    state = connection.getBulbState(thing);
                                    brightness = connection.getBulbBrightness(thing);
                                    color = connection.getBulbColor(thing);
                                }
                                if (state != null) {
                                    updateBulbState(thing.getChannel(CHANNEL_STATE).getUID(), state);
                                }
                                if (brightness != -1) {
                                    updateBrightness(thing.getChannel(CHANNEL_LIGHT_BRIGHTNESS).getUID(), brightness);
                                }
                                if (color != null) {
                                    updateColor(thing.getChannel(CHANNEL_LIGHT_COLOR).getUID(), color);
                                }
                            } catch (IOException | URISyntaxException e) {
                                logger.error(e.getMessage());
                            }
                        }
                    }
                }
            };

            Configuration config = accountHandler.getThing().getConfiguration();

            updateStateJob = scheduler.scheduleWithFixedDelay(runnable, 0,
                    ((BigDecimal) config.getProperties().get("pollingIntervalSmartHome")).longValue(),
                    TimeUnit.SECONDS);
        }
    }

    public void addChannelToDevice(ChannelUID channelUID, String itemType, ChannelTypeUID channelTypeUID) {
        updateThing(editThing()
                .withChannel(ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID).build()).build());
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            updateStateJob.cancel(false);
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AccountHandler accountHandler = this.accountHandler;
        if (accountHandler == null) {
            return;
        }
        try {
            Map<String, String> props = this.thing.getProperties();
            String entityId = props.get(DEVICE_PROPERTY_LIGHT_ENTITY_ID);
            String channelId = channelUID.getId();
            if (channelId.equals(CHANNEL_STATE)) {
                if (command instanceof OnOffType) {
                    connection = accountHandler.findConnection();
                    for (Map.Entry<String, String> entry : props.entrySet()) {
                        if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                            if (command.equals(OnOffType.ON)) {
                                connection.smartHomeCommand(entry.getValue(), DEVICE_TURN_ON, null, 0.00);
                            } else {
                                connection.smartHomeCommand(entry.getValue(), DEVICE_TURN_OFF, null, 0.00);
                            }
                        } else if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_ENTITY_ID)
                                && !entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                            if (command.equals(OnOffType.ON)) {
                                connection.smartHomeCommand(entityId, DEVICE_TURN_ON, null, 0.00);
                            } else {
                                connection.smartHomeCommand(entityId, DEVICE_TURN_OFF, null, 0.00);
                            }
                        }
                    }
                }
            }
            if (channelId.equals(CHANNEL_LIGHT_COLOR)) {
                if (command instanceof StringType) {
                    String commandText = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(commandText)) {
                        connection = accountHandler.findConnection();
                        for (Map.Entry<String, String> entry : props.entrySet()) {
                            if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                                connection.smartHomeCommand(entry.getValue(), "setColor", commandText, 0.00);
                            } else if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_ENTITY_ID)
                                    && !entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                                connection.smartHomeCommand(entityId, "setColor", commandText, 0.00);
                            }
                        }
                    }
                }
            }
            if (channelId.equals(CHANNEL_LIGHT_WHITE_TEMPERATURE)) {
                if (command instanceof StringType) {
                    String commandText = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(commandText)) {
                        connection = accountHandler.findConnection();
                        for (Map.Entry<String, String> entry : props.entrySet()) {
                            if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                                connection.smartHomeCommand(entry.getValue(), "setColorTemperature", commandText, 0.00);
                            } else if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_ENTITY_ID)
                                    && !entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                                connection.smartHomeCommand(entityId, "setColorTemperature", commandText, 0.00);
                            }
                        }
                    }
                }
            }
            if (channelId.equals(CHANNEL_LIGHT_BRIGHTNESS)) {
                Float percentage = null;
                if (command instanceof PercentType) {
                    percentage = ((PercentType) command).floatValue() / 100;
                }
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        percentage = 100f;
                    } else {
                        percentage = 0f;
                    }
                }
                if (percentage != null) {
                    connection = accountHandler.findConnection();
                    for (Map.Entry<String, String> entry : props.entrySet()) {
                        if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                            connection.smartHomeCommand(entry.getValue(), "setBrightness", null, percentage);
                        } else if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_ENTITY_ID)
                                && !entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                            connection.smartHomeCommand(entityId, "setBrightness", null, percentage);
                        }
                    }
                }
            }
            if (channelId.equals(CHANNEL_STATE)) {
                if (command instanceof OnOffType) {
                    connection = accountHandler.findConnection();

                    for (Map.Entry<String, String> entry : props.entrySet()) {
                        if (entry.getKey().contains(DEVICE_PROPERTY_LIGHT_ENTITY_ID)
                                && !entry.getKey().contains(DEVICE_PROPERTY_LIGHT_SUBDEVICE)) {
                            if (command.equals(OnOffType.ON)) {
                                connection.smartHomeCommand(entityId, DEVICE_TURN_ON, null, 0.00);
                            } else {
                                connection.smartHomeCommand(entityId, DEVICE_TURN_OFF, null, 0.00);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Handle command failed {}", e);
        }

        logger.trace("Command {} received from channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            updateSmartHomeDevices();
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
        updateState(CHANNEL_STATE, OnOffType.OFF);
        if (this.accountHandler != handler) {
            this.accountHandler = handler;
        }
        return true;
    }

    public List<Object> updateSmartHomeDevices() {

        Connection currentConnection = connection;
        if (currentConnection == null) {
            return new ArrayList<Object>();
        }

        List<Object> smartHomeDevices = null;
        try {
            if (currentConnection.getIsLoggedIn()) {
                smartHomeDevices = currentConnection.getSmarthomeDeviceList();
            }
        } catch (IOException | URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }

        if (smartHomeDevices != null) {
            return smartHomeDevices;
        }

        return new ArrayList<Object>();
    }

}

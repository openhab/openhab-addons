/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.handler;

import static org.openhab.binding.vera2.VeraBindingConstants.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.vera2.VeraBindingConstants;
import org.openhab.binding.vera2.config.VeraDeviceConfiguration;
import org.openhab.binding.vera2.controller.Vera.json.Device;
import org.openhab.binding.vera2.internal.converter.VeraDeviceStateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeraDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dmitriy Ponomarev
 */
public class VeraDeviceHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private DevicePolling devicePolling;
    private ScheduledFuture<?> pollingJob;
    private VeraDeviceConfiguration mConfig = null;

    public VeraDeviceHandler(Thing thing) {
        super(thing);
        devicePolling = new DevicePolling();
    }

    private class Initializer implements Runnable {
        @Override
        public void run() {
            try {
                VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
                if (veraBridgeHandler != null && veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    ThingStatusInfo statusInfo = veraBridgeHandler.getThing().getStatusInfo();
                    logger.debug("Change device status to bridge status: {}", statusInfo);

                    // Set thing status to bridge status
                    updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());

                    try {
                        logger.debug("Add channels");
                        Device device = veraBridgeHandler.getController().getDevice(mConfig.getDeviceId());
                        if (device != null && !device.category.equals("0")) {
                            logger.debug("Finded {} device", device.name);
                            addDeviceAsChannel(device);
                            // TODO addCommandClassThermostatModeAsChannel(modes, mConfig.getDeviceId());
                        }
                    } catch (Exception e) {
                        logger.error("{}", e.getMessage());
                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "Error occurred when adding device as channel.");
                        }
                    }

                    // Initialize device polling
                    if (pollingJob == null || pollingJob.isCancelled()) {
                        logger.debug("Starting polling job at intervall {}",
                                veraBridgeHandler.getVeraBridgeConfiguration().getPollingInterval());
                        pollingJob = scheduler.scheduleAtFixedRate(devicePolling, 10,
                                veraBridgeHandler.getVeraBridgeConfiguration().getPollingInterval(), TimeUnit.SECONDS);
                    } else {
                        // Called when thing or bridge updated ...
                        logger.debug("Polling is already active");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Devices not loaded");
                }
            } catch (Exception e) {
                logger.error("{}", e.getMessage());
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Error occurred when adding device as channel.");
                }
            }
        }
    };

    /**
     * Remove all linked items from openHAB connector observer list
     */
    private class Disposer implements Runnable {
        @Override
        public void run() {
            // Vera bridge have to be ONLINE because configuration is needed
            VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
            if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                logger.debug("Vera bridge handler not found or not ONLINE.");
                // status update will remove finally
                updateStatus(ThingStatus.REMOVED);
                return;
            }
            // status update will remove finally
            updateStatus(ThingStatus.REMOVED);
        }

    };

    protected synchronized VeraBridgeHandler getVeraBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof VeraBridgeHandler) {
            return (VeraBridgeHandler) handler;
        } else {
            return null;
        }
    }

    private VeraDeviceConfiguration loadAndCheckConfiguration() {
        VeraDeviceConfiguration config = getConfigAs(VeraDeviceConfiguration.class);
        if (config.getDeviceId() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Couldn't create device, deviceId is missing.");
            return null;
        }
        return config;
    }

    @Override
    public void initialize() {
        setLocation();
        logger.debug("Initializing Vera device handler ...");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Checking configuration and bridge...");
        mConfig = loadAndCheckConfiguration();
        if (mConfig != null) {
            logger.debug("Configuration complete: {}", mConfig);
            scheduler.schedule(new Initializer(), 2, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "DeviceId required!");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Vera device handler ...");
        if (mConfig != null && mConfig.getDeviceId() != null) {
            mConfig.setDeviceId(null);
        }
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        logger.debug("Handle removal Vera device ...");
        scheduler.execute(new Disposer());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Only called if status ONLINE or OFFLINE
        logger.debug("Vera bridge status changed: {}", bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge status is offline.");
        } else if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            // Initialize thing, if all OK the status of device thing will be ONLINE
            scheduler.execute(new Initializer());
        }
    }

    private class DevicePolling implements Runnable {
        @Override
        public void run() {
            // logger.debug("Starting polling for device: {}", getThing().getLabel());
            for (Channel channel : getThing().getChannels()) {
                // logger.debug("Checking link state of channel: {}", channel.getLabel());
                if (isLinked(channel.getUID().getId())) {
                    // logger.debug("Refresh items that linked with channel: {}", channel.getLabel());
                    try {
                        refreshChannel(channel);
                    } catch (Throwable t) {
                        if (t instanceof Exception) {
                            logger.error("Error occurred when performing polling:{}", t.getMessage());
                        } else if (t instanceof Error) {
                            logger.error("Error occurred when performing polling:{}", t.getMessage());
                        } else {
                            logger.error("Error occurred when performing polling: Unexpected error");
                        }
                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                                    "Error occurred when performing polling.");
                        }
                    }
                } else {
                    logger.debug("Polling for device: {} not possible (channel {} not linked", thing.getLabel(),
                            channel.getLabel());
                }
            }
            refreshLastUpdate();
        }
    };

    private synchronized void setLocation() {
        Map<String, String> properties = getThing().getProperties();
        // Load location from properties
        String location = properties.get(VeraBindingConstants.PROP_ROOM);
        if (location != null && !location.equals("") && getThing().getLocation() == null) {
            logger.debug("Set location to {}", location);
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withLocation(location);
            thingBuilder.withLabel(thing.getLabel());
            updateThing(thingBuilder.build());
        }
    }

    protected void refreshLastUpdate() {
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        updateProperty(PROP_LAST_UPDATE, formatter.format(Calendar.getInstance().getTime()));
    }

    protected void refreshAllChannels() {
        scheduler.execute(new DevicePolling());
    }

    private void refreshChannel(Channel channel) {
        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }

        // Check device id associated with channel
        String deviceId = channel.getProperties().get(DEVICE_CONFIG_ID);
        if (deviceId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "Not found deviceId for channel: " + channel.getChannelTypeUID());
            logger.debug("Vera device disconnected");
            return;
        }

        Device device = veraBridgeHandler.getController().getDevice(deviceId);
        if (device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Channel refresh for device: " + deviceId
                    + " with channel: " + channel.getChannelTypeUID() + " failed!");
            logger.debug("Vera device disconnected");
            return;
        }

        try {
            updateState(channel.getUID(), VeraDeviceStateConverter.toState(device, channel));
            ThingStatusInfo statusInfo = veraBridgeHandler.getThing().getStatusInfo();
            updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
        } catch (IllegalArgumentException iae) {
            logger.debug(
                    "IllegalArgumentException ({}) during refresh channel for device: {} (level: {}) with channel: {}",
                    iae.getMessage(), device.name, device.level, channel.getChannelTypeUID());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Channel refresh for device: " + device.name
                    + " (level: " + device.level + ") with channel: " + channel.getChannelTypeUID() + " failed!");
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Vera device channel linked: {}", channelUID);
        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }
        super.channelLinked(channelUID); // performs a refresh command
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.debug("Vera device channel unlinked: {}", channelUID);
        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }
        super.channelUnlinked(channelUID);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // Refresh update time
        logger.debug("Handle update for channel: {} with new state: {}", channelUID.getId(), newState.toString());

        refreshLastUpdate();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, final Command command) {
        logger.debug("Handle command for channel: {} with command: {}", channelUID.getId(), command.toString());

        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }

        final Channel channel = getThing().getChannel(channelUID.getId());
        final String deviceId = channel.getProperties().get("deviceId");

        if (deviceId != null) {
            Device device = veraBridgeHandler.getController().getDevice(deviceId);
            if (device != null) {
                try {
                    if (command instanceof RefreshType) {
                        logger.debug("Handle command: RefreshType");
                        refreshChannel(channel);
                    } else {
                        if (command instanceof PercentType) {
                            logger.debug("Handle command: PercentType");
                            veraBridgeHandler.getController().setDimLevel(device, ((PercentType) command).toString());
                        }
                        if (command instanceof DecimalType) {
                            logger.debug("Handle command: DecimalType");
                            veraBridgeHandler.getController().setDimLevel(device, ((DecimalType) command).toString());
                        }
                        if (command instanceof OnOffType) {
                            logger.debug("Handle command: OnOffType");
                            if (command.equals(OnOffType.ON)) {
                                veraBridgeHandler.getController().turnDeviceOn(device);
                            } else if (command.equals(OnOffType.OFF)) {
                                veraBridgeHandler.getController().turnDeviceOff(device);
                            }
                        } else if (command instanceof OpenClosedType) {
                            logger.debug("Handle command: OpenClosedType");
                            if (command.equals(OpenClosedType.CLOSED)) {
                                veraBridgeHandler.getController().turnDeviceOn(device);
                            } else if (command.equals(OpenClosedType.OPEN)) {
                                veraBridgeHandler.getController().turnDeviceOff(device);
                            }
                        } else {
                            logger.warn("Unknown command type: {}, {}, {}, {}", command, deviceId, device.category,
                                    device.categoryName);
                        }
                    }
                } catch (UnsupportedOperationException e) {
                    logger.warn("Unknown command: {}", e.getMessage());
                }
            } else {
                logger.warn("Device {} not loaded", deviceId);
            }
        }
    }

    protected synchronized void addDeviceAsChannel(Device device) {
        if (device != null) {
            logger.debug("Add device as channel: {}", device.name);

            HashMap<String, String> properties = new HashMap<>();
            properties.put("deviceId", device.id);

            String id = "";
            String acceptedItemType = "";

            int category = Integer.parseInt(device.category);
            int subcategory = Integer.parseInt(device.subcategory);
            switch (category) {
                case 0:
                case 1: // Interface
                    break;
                case 2: // Dimmable Light
                    switch (subcategory) {
                        case 1:
                        case 2:
                        case 3:
                            id = SWITCH_MULTILEVEL_CHANNEL;
                            acceptedItemType = "Dimmer";
                            break;
                        case 4:
                            id = SWITCH_COLOR_CHANNEL;
                            acceptedItemType = "Color";
                            break;
                    }
                    break;
                case 3: // Switch
                    id = SWITCH_CONTROL_CHANNEL;
                    acceptedItemType = "Switch";
                    break;
                case 4: // Security Sensor
                    switch (subcategory) {
                        case 1:
                            id = SENSOR_DOOR_WINDOW_CHANNEL;
                            acceptedItemType = "Contact";
                            break;
                        case 2:
                            id = SENSOR_FLOOD_CHANNEL;
                            acceptedItemType = "Switch";
                        case 3:
                            id = SENSOR_MOTION_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        case 4:
                            id = SENSOR_SMOKE_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        case 5:
                            id = SENSOR_CO_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        case 6:
                            id = SENSOR_BINARY_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                    }
                    break;
                case 5: // TODO HVAC
                    logger.warn("TODO: HVAC: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 6: // TODO Camera
                    logger.warn("TODO: Camera: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 7: // Door Lock
                    id = DOORLOCK_CHANNEL;
                    acceptedItemType = "Switch";
                    break;
                case 8: // Window Covering
                    id = SWITCH_ROLLERSHUTTER_CHANNEL;
                    acceptedItemType = "Rollershutter";
                    break;
                case 9: // TODO Remote Control
                    logger.warn("TODO: Remote Control: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 10: // TODO IR Transmitter
                    logger.warn("TODO: IR Transmitter: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 11: // TODO Generic I/O
                    logger.warn("TODO: Generic I/O: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 12: // Generic Sensor
                    id = SENSOR_BINARY_CHANNEL;
                    acceptedItemType = "Switch";
                    break;
                case 13: // TODO Serial Port
                    logger.warn("TODO: Serial Port I/O: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 14: // Scene Controller
                    id = SWITCH_CONTROL_CHANNEL;
                    acceptedItemType = "Switch";
                    break;
                case 15: // TODO A/V
                    logger.warn("TODO: A/V: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 16: // Humidity Sensor
                    id = SENSOR_HUMIDITY_CHANNEL;
                    acceptedItemType = "Number";
                    break;
                case 17: // Temperature Sensor
                    id = SENSOR_TEMPERATURE_CHANNEL;
                    acceptedItemType = "Number";
                    break;
                case 18: // Light Sensor
                    id = SENSOR_LUMINOSITY_CHANNEL;
                    acceptedItemType = "Number";
                    break;
                case 19: // TODO Z-Wave Interface
                    logger.warn("TODO: Z-Wave Interface: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 20: // TODO Insteon Interface
                    logger.warn("TODO: Insteon Interface: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 21: // Power Meter
                    id = SENSOR_ENERGY_CHANNEL;
                    acceptedItemType = "Number";
                    break;
                case 22: // TODO Alarm Panel
                    logger.warn("TODO: Alarm Panel: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 23: // TODO Alarm Partition
                    logger.warn("TODO: Alarm Partition: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 24: // TODO Siren
                    logger.warn("TODO: Siren: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 25: // TODO Weather
                    logger.warn("TODO: Weather: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 26: // TODO Philips Controller
                    logger.warn("TODO: Philips Controller: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 27: // TODO Appliance
                    logger.warn("TODO: Appliance: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
                    break;
                case 28: // UV Sensor
                    id = SENSOR_ULTRAVIOLET_CHANNEL;
                    acceptedItemType = "Number";
                    break;
                default:
                    logger.warn("Unknown device type: {}, {}, {}, {}", device.id, device.name, device.category,
                            device.categoryName);
            }

            // If at least one rule could mapped to a channel
            if (!id.equals("")) {
                addChannel(id, acceptedItemType, device.name, properties);

                logger.debug("Channel for device added with channel id: {}, accepted item type: {} and title: {}", id,
                        acceptedItemType, device.name);

                if (device.batterylevel != null && !device.batterylevel.isEmpty()) {
                    addChannel(BATTERY_CHANNEL, "Number", "Battery", properties);
                }
            } else {
                // Thing status will not be updated because thing could have more than one channel
                logger.warn("No channel for device added: {}", device);
            }
        }
    }

    private synchronized void addChannel(String id, String acceptedItemType, String label,
            HashMap<String, String> properties) {
        String channelId = id + "-" + properties.get("deviceId");
        boolean channelExists = false;
        // Check if a channel for this device exist.
        List<Channel> channels = getThing().getChannels();
        for (Channel channel : channels) {
            if (channel.getUID().getId().equals(channelId)) {
                channelExists = true;
            }
        }
        if (!channelExists) {
            ThingBuilder thingBuilder = editThing();
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, id);
            ChannelBuilder channelBuilder = ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId),
                    acceptedItemType);
            channelBuilder.withType(channelTypeUID);
            channelBuilder.withLabel(label);
            channelBuilder.withProperties(properties);
            thingBuilder.withChannel(channelBuilder.build());
            thingBuilder.withLabel(thing.getLabel());
            updateThing(thingBuilder.build());
        }
    }
}

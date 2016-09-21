/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.handler;

import static de.fh_zwickau.informatik.sensor.ZWayConstants.*;
import static org.openhab.binding.zway.ZWayBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fh_zwickau.informatik.sensor.model.devices.Color;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceCommand;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.devices.types.Battery;
import de.fh_zwickau.informatik.sensor.model.devices.types.Doorlock;
import de.fh_zwickau.informatik.sensor.model.devices.types.SensorBinary;
import de.fh_zwickau.informatik.sensor.model.devices.types.SensorMultilevel;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchBinary;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchControl;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchMultilevel;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchRGBW;
import de.fh_zwickau.informatik.sensor.model.devices.types.SwitchToggle;
import de.fh_zwickau.informatik.sensor.model.devices.types.Thermostat;
import de.fh_zwickau.informatik.sensor.model.devices.types.ToggleButton;

/**
 * The {@link ZWayDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Hecker - Initial contribution
 */
public abstract class ZWayDeviceHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private DevicePolling devicePolling;
    private ScheduledFuture<?> pollingJob;

    /**
     * Initialize polling job and register all linked item in openHAB connector as observer
     */
    private class Initializer implements Runnable {

        @Override
        public void run() {
            // https://community.openhab.org/t/oh2-major-bug-with-scheduled-jobs/12350/11
            // If any execution of the task encounters an exception, subsequent executions are
            // suppressed. Otherwise, the task will only terminate via cancellation or
            // termination of the executor.
            try {
                // Z-Way bridge have to be ONLINE because configuration is needed
                ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
                if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    logger.debug("Z-Way bridge handler not found or not ONLINE.");
                    return;
                }

                // Initialize device polling
                if (pollingJob == null || pollingJob.isCancelled()) {
                    logger.debug("Starting polling job at intervall {}",
                            zwayBridgeHandler.getZWayBridgeConfiguration().getPollingInterval());
                    pollingJob = scheduler.scheduleAtFixedRate(devicePolling, 10,
                            zwayBridgeHandler.getZWayBridgeConfiguration().getPollingInterval(), TimeUnit.SECONDS);
                } else {
                    // Called when thing or bridge updated ...
                    logger.debug("Polling is allready active");
                }

                // Register all linked items on server start
                if (zwayBridgeHandler.getZWayBridgeConfiguration().getObserverMechanismEnabled()) {
                    for (Channel channel : getThing().getChannels()) {
                        if (isLinked(channel.getUID().getId())) {
                            String deviceId = channel.getProperties().get("deviceId");

                            Set<String> items = linkRegistry.getLinkedItems(channel.getUID());
                            for (String item : items) {
                                logger.debug("Linked item found - starting register command for openHAB item: {}",
                                        item);
                                zwayRegisterOpenHabItem(item, deviceId);
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                if (t instanceof Exception) {
                    logger.error(((Exception) t).getMessage());
                } else if (t instanceof Error) {
                    logger.error(((Error) t).getMessage());
                } else {
                    logger.error("Unexpected error");
                }
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Error occurred when starting polling and registering item as observer.");
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
            // Z-Way bridge have to be ONLINE because configuration is needed
            ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
            if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                logger.debug("Z-Way bridge handler not found or not ONLINE.");

                // status update will remove finally
                updateStatus(ThingStatus.REMOVED);

                return;
            }

            // Remove all linked items in Z-Way server
            if (zwayBridgeHandler.getZWayBridgeConfiguration().getObserverMechanismEnabled()) {
                for (Channel channel : getThing().getChannels()) {
                    if (isLinked(channel.getUID().getId())) {
                        Set<String> items = linkRegistry.getLinkedItems(channel.getUID());
                        for (String item : items) {
                            logger.debug("Linked item found - starting remove command for openHAB item: {}", item);
                            zwayUnsubscribeOpenHabItem(item);
                        }
                    }
                }
            }

            // status update will remove finally
            updateStatus(ThingStatus.REMOVED);
        }
    };

    public ZWayDeviceHandler(Thing thing) {
        super(thing);

        devicePolling = new DevicePolling();
    }

    protected synchronized ZWayBridgeHandler getZWayBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof ZWayBridgeHandler) {
            return (ZWayBridgeHandler) handler;
        } else {
            return null;
        }
    }

    @Override
    public void initialize() {
        // Start an extra thread to check the connection, because it takes sometimes more
        // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
        scheduler.execute(new Initializer());
    }

    @Override
    public void dispose() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        super.dispose();
    }

    @Override
    public void handleRemoval() {
        logger.debug("Handle removal Z-Way device ...");

        // Start an extra thread, because it takes sometimes more
        // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
        scheduler.execute(new Disposer());

        // super.handleRemoval() called in every case in scheduled task ...
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("Z-Way bridge handler initialized.");

        // Not used: Initialization of bridge is partly in another thread and at this point not complete
    }

    @Override
    public void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("Z-Way bridge handler disposed.");
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Only called if status ONLINE or OFFLINE
        logger.debug("Z-Way bridge status changed: {}", bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge status is offline.");
        } else if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            // Initialize thing, if all OK the status of device thing will be ONLINE

            // Start an extra thread to check the connection, because it takes sometimes more
            // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
            scheduler.execute(new Initializer());
        }
    }

    private class DevicePolling implements Runnable {
        @Override
        public void run() {
            logger.debug("Starting polling for device: {}", getThing().getLabel());
            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                for (Channel channel : getThing().getChannels()) {
                    logger.debug("Checking link state of channel: {}", channel.getLabel());
                    if (isLinked(channel.getUID().getId())) {
                        logger.debug("Refresh items that linked with channel: {}", channel.getLabel());

                        refreshChannel(channel);
                    } else {
                        logger.debug("Polling for device: {} not possible (channel {} not linked", thing.getLabel(),
                                channel.getLabel());
                    }
                }
            } else {
                logger.debug("Polling not possible, Z-Way device isn't ONLINE");
            }
        }
    };

    private void refreshChannel(final Channel channel) {
        // Check Z-Way bridge handler
        ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
        if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        // Check device id associated with channel
        final String deviceId = channel.getProperties().get("deviceId");
        if (deviceId == null) {
            logger.debug("ZAutomation device id not found.");
            return;
        }

        // Load and check device from Z-Way server
        DeviceList deviceList = zwayBridgeHandler.getZWayApi().getDevices();
        if (deviceList != null) {
            Device device = deviceList.getDeviceById(deviceId);
            if (device == null) {
                logger.debug("ZAutomation device not found.");
                return;
            }

            // Store level locally
            String level = device.getMetrics().getLevel();

            // Set item state to level depending on device type
            if (device instanceof Battery) {
                updateState(channel.getUID(), getMultilevelState(level));
            } else if (device instanceof Doorlock) {
                updateState(channel.getUID(), getBinaryState(level.toLowerCase()));
            } else if (device instanceof SensorBinary) {
                updateState(channel.getUID(), getBinaryState(level.toLowerCase()));
            } else if (device instanceof SensorMultilevel) {
                updateState(channel.getUID(), getMultilevelState(level));
            } else if (device instanceof SwitchBinary) {
                updateState(channel.getUID(), getBinaryState(level.toLowerCase()));
            } else if (device instanceof SwitchMultilevel) {
                updateState(channel.getUID(), getMultilevelState(level));
            } else if (device instanceof SwitchRGBW) {
                updateState(channel.getUID(), getColorState(device.getMetrics().getColor()));
            } else if (device instanceof SwitchToggle) {
                // ?
            } else if (device instanceof Thermostat) {
                // TODO
            } else if (device instanceof SwitchControl) {
                updateState(channel.getUID(), getBinaryState(level.toLowerCase()));
            } else if (device instanceof ToggleButton) {
                // TODO
            }
        } else {
            logger.warn("Devices not loaded");
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Z-Way device channel linked: {}", channelUID);

        ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
        if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        // Method called when channel linked and not when server started!!!

        if (zwayBridgeHandler.getZWayBridgeConfiguration().getObserverMechanismEnabled()) {
            // Load device id from channel's properties for the compatibility of ZAutomation and ZWave devices
            Channel channel = thing.getChannel(channelUID.getId());
            String deviceId = channel.getProperties().get("deviceId");

            Set<String> items = linkRegistry.getLinkedItems(channelUID);
            for (String item : items) {
                logger.debug("Linked item found - starting register command for openHAB item: {}", item);
                zwayRegisterOpenHabItem(item, deviceId);
            }
        }

        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.debug("Z-Way device channel unlinked: {}", channelUID);

        ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
        if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        if (zwayBridgeHandler.getZWayBridgeConfiguration().getObserverMechanismEnabled()) {
            // TODO no items for this channel available at this point!
            // before method called by system the item removed
            Set<String> items = linkRegistry.getLinkedItems(channelUID);
            for (String item : items) {
                logger.debug("Linked item found - starting remove command for openHAB item: {}", item);
                zwayUnsubscribeOpenHabItem(item);
            }
        }

        super.channelUnlinked(channelUID);
    }

    private void zwayRegisterOpenHabItem(String openHABItemName, String deviceId) {
        ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
        if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        // Preconditions are OK, starting registration ...
        Map<String, String> params = new HashMap<String, String>();
        params.put("openHabAlias", zwayBridgeHandler.getZWayBridgeConfiguration().getOpenHabAlias());
        params.put("openHabItemName", openHABItemName);
        params.put("vDevName", deviceId);
        DeviceCommand command = new DeviceCommand("OpenHabConnector", "registerOpenHabItem", params);

        String message = zwayBridgeHandler.getZWayApi().getDeviceCommand(command);
        if (message != null) {
            logger.debug("Device registration finished successfully: {}", message);
        } else {
            logger.warn("Device registration failed");
        }
    }

    private void zwayUnsubscribeOpenHabItem(String openHABItemName) {
        ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
        if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        // Preconditions are OK, starting unsubscribing ...
        Map<String, String> params = new HashMap<String, String>();
        params.put("openHabAlias", zwayBridgeHandler.getZWayBridgeConfiguration().getOpenHabAlias());
        params.put("openHabItemName", openHABItemName);
        DeviceCommand command = new DeviceCommand("OpenHabConnector", "removeOpenHabItem", params);

        String message = zwayBridgeHandler.getZWayApi().getDeviceCommand(command);
        if (message != null) {
            logger.debug("Device unsubscribing finished successfully: {}", message);
        } else {
            logger.warn("Device unsubscribing failed");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, final Command command) {
        logger.debug("Handle command for channel: {} with command: {}", channelUID.getId(), command.toString());

        // Check Z-Way bridge handler
        ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
        if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        // Load device id from channel's properties for the compatibility of ZAutomation and ZWave devices
        final Channel channel = getThing().getChannel(channelUID.getId());
        final String deviceId = channel.getProperties().get("deviceId");

        if (deviceId == null) {
            logger.debug("ZAutomation device id not found.");
            return;
        }

        DeviceList deviceList = zwayBridgeHandler.getZWayApi().getDevices();
        if (deviceList != null) {
            Device device = deviceList.getDeviceById(deviceId);
            if (device == null) {
                logger.debug("ZAutomation device not found.");
                return;
            }

            try {
                if (command instanceof RefreshType) {
                    logger.debug("Handle command: RefreshType");

                    refreshChannel(channel);
                } else {
                    if (device instanceof Battery) {
                        // possible commands: update()
                    } else if (device instanceof Doorlock) {
                        // possible commands: open(), close()
                        if (command instanceof OnOffType) {
                            logger.debug("Handle command: OnOffType");
                            if (command.equals(OnOffType.ON)) {
                                device.open();
                            } else if (command.equals(OnOffType.OFF)) {
                                device.close();
                            }
                        }
                    } else if (device instanceof SensorBinary) {
                        // possible commands: update()
                    } else if (device instanceof SensorMultilevel) {
                        // possible commands: update()
                    } else if (device instanceof SwitchBinary) {
                        // possible commands: update(), on(), off()
                        if (command instanceof OnOffType) {
                            logger.debug("Handle command: OnOffType");

                            if (command.equals(OnOffType.ON)) {
                                device.on();
                            } else if (command.equals(OnOffType.OFF)) {
                                device.off();
                            }
                        }
                    } else if (device instanceof SwitchMultilevel) {
                        // possible commands: update(), on(), up(), off(), down(), min(), max(), upMax(), increase(),
                        // decrease(), exact(level), exactSmooth(level, duration), stop(), startUp(), startDown()
                        if (command instanceof DecimalType) {
                            logger.debug("Handle command: DecimalType");

                            device.exact(command.toString());
                        }
                    } else if (device instanceof SwitchRGBW) {
                        // possible commands: on(), off(), exact(red, green, blue)
                        if (command instanceof HSBType) {
                            logger.debug("Handle command: HSBType");

                            HSBType hsb = (HSBType) command;

                            // first set on/off
                            if (hsb.getBrightness().intValue() > 0) {
                                if (device.getMetrics().getLevel().toLowerCase().equals("off")) {
                                    device.on();
                                }

                                // then set color
                                int red = (int) Math.round(255 * (hsb.getRed().doubleValue() / 100));
                                int green = (int) Math.round(255 * (hsb.getGreen().doubleValue() / 100));
                                int blue = (int) Math.round(255 * (hsb.getBlue().doubleValue() / 100));

                                device.exact(red, green, blue);
                            } else {
                                device.off();
                            }
                        }
                    } else if (device instanceof SwitchToggle) {
                        // possible commands: ?
                    } else if (device instanceof Thermostat) {
                        // possible commands: setMode(mode), setTemp(temp)
                        // TODO
                    } else if (device instanceof SwitchControl) {
                        // possible commands: on(), off(), exact(level), upstart(), upstop(), downstart(), downstop()
                        if (command instanceof OnOffType) {
                            logger.debug("Handle command: OnOffType");

                            if (command.equals(OnOffType.ON)) {
                                device.on();
                            } else if (command.equals(OnOffType.OFF)) {
                                device.off();
                            }
                        }
                    } else if (device instanceof ToggleButton) {
                        // possible commands: on(), off(), exact(level), upstart(), upstop(), downstart(), downstop()
                        // TODO
                    }
                }
            } catch (UnsupportedOperationException e) {
                logger.warn("Unknown command: {}", e.getMessage());
            }
        } else {
            logger.warn("Devices not loaded");
        }
    }

    /**
     * Transforms an value in an openHAB type.
     *
     * @param multisensor value
     * @return transformed openHAB state
     */
    private State getMultilevelState(String multisensorValue) {
        if (multisensorValue != null) {
            return new DecimalType(multisensorValue);
        }
        return UnDefType.UNDEF;
    }

    /**
     * Transforms an value in an openHAB type.
     *
     * @param binary switch value
     * @return transformed openHAB state
     */
    private State getBinaryState(String binarySwitchState) {
        if (binarySwitchState != null) {
            if (binarySwitchState.equals("on")) {
                return OnOffType.ON;
            } else if (binarySwitchState.equals("off")) {
                return OnOffType.OFF;
            }
        }
        return UnDefType.UNDEF;
    }

    /**
     * Transforms an value in an openHAB type.
     *
     * @param Z-Way color value
     * @return transformed openHAB state
     */
    private State getColorState(Color colorSwitchState) {
        if (colorSwitchState != null && colorSwitchState.getRed() != null && colorSwitchState.getGreen() != null
                && colorSwitchState.getBlue() != null) {

            HSBType hsbType = HSBType.fromRGB(colorSwitchState.getRed(), colorSwitchState.getGreen(),
                    colorSwitchState.getBlue());

            return hsbType;
        }

        return UnDefType.UNDEF;
    }

    protected synchronized void addDeviceAsChannel(Device device) {
        logger.debug("Add virtual device as channel: {}", device.getMetrics().getTitle());

        // Device.probeType
        // |
        // Device.metrics.probeType
        // |
        // Device.metrics.icon
        // |
        // Command class
        // |
        // Default, depends on device type

        if (device != null) {
            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put("deviceId", device.getDeviceId());

            String id = "";
            String acceptedItemType = "";

            // 1. Set basically channel types without further information
            if (device instanceof Battery) {
                id = BATTERY_CHANNEL;
                acceptedItemType = "Number";
            } else if (device instanceof Doorlock) {
                id = DOORLOCK_CHANNEL;
                acceptedItemType = "Switch";
            } else if (device instanceof SensorBinary) {
                id = SENSOR_BINARY_CHANNEL;
                acceptedItemType = "Switch";
            } else if (device instanceof SensorMultilevel) {
                id = SENSOR_MULTILEVEL_CHANNEL;
                acceptedItemType = "Number";
            } else if (device instanceof SwitchBinary) {
                id = SWITCH_BINARY_CHANNEL;
                acceptedItemType = "Switch";
            } else if (device instanceof SwitchMultilevel) {
                id = SWITCH_MULTILEVEL_CHANNEL;
                acceptedItemType = "Number";
            } else if (device instanceof SwitchToggle) {
                // ?
            } else if (device instanceof SwitchRGBW) {
                id = SWITCH_COLOR_CHANNEL;
                acceptedItemType = "Color";
            } else if (device instanceof Thermostat) {
                // TODO
            } else if (device instanceof SwitchControl) {
                id = SWITCH_CONTROL_CHANNEL;
                acceptedItemType = "Switch";
            } else if (device instanceof ToggleButton) {
                // TODO
            }

            // 2. Check if device information includes further information about sensor type
            if (!device.getProbeType().equals("")) {
                if (device instanceof SensorMultilevel) {
                    switch (device.getProbeType()) {
                        case PROBE_TYPE_TEMPERATURE:
                            id = SENSOR_TEMPERATURE_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        case PROBE_TYPE_LUMINOSITY:
                            id = SENSOR_LUMINOSITY_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        case PROBE_TYPE_HUMIDITY:
                            id = SENSOR_HUMIDITY_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        case PROBE_TYPE_BAROMETER:
                            id = SENSOR_BAROMETER_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        case PROBE_TYPE_ULTRAVIOLET:
                            id = SENSOR_ULTRAVIOLET_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        case PROBE_TYPE_ENERGY:
                            id = SENSOR_ENERGY_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        case PROBE_TYPE_METER_ELECTRIC_KILOWATT_PER_HOUR:
                            id = SENSOR_METER_KWH_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        case PROBE_TYPE_METER_ELECTRIC_WATT:
                            id = SENSOR_METER_W_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        default:
                            break;
                    }
                } else if (device instanceof SensorBinary) {
                    switch (device.getProbeType()) {
                        case PROBE_TYPE_GENERAL_PURPOSE:
                            if (device.getMetrics().getIcon().equals(ICON_MOTION)) {
                                id = SENSOR_MOTION_CHANNEL;
                                acceptedItemType = "Switch";
                            }
                            break;
                        case PROBE_TYPE_SMOKE:
                            id = SENSOR_SMOKE_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        case PROBE_TYPE_CO:
                            id = SENSOR_CO_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        case PROBE_TYPE_FLOOD:
                            id = SENSOR_FLOOD_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        case PROBE_TYPE_COOLING:
                            // TODO
                            break;
                        case PROBE_TYPE_TAMPER:
                            id = SENSOR_TAMPER_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        case PROBE_TYPE_DOOR_WINDOW:
                            id = SENSOR_DOOR_WINDOW_CHANNEL;
                            acceptedItemType = "Contact";
                            break;
                        case PROBE_TYPE_MOTION:
                            id = SENSOR_MOTION_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        default:
                            break;
                    }
                } else if (device instanceof SwitchMultilevel) {
                    switch (device.getProbeType()) {
                        case PROBE_TYPE_SWITCH_COLOR_COLD_WHITE:
                            id = SWITCH_COLOR_TEMPERATURE_CHANNEL;
                            acceptedItemType = "Dimmer";
                            break;
                        case PROBE_TYPE_SWITCH_COLOR_SOFT_WHITE:
                            id = SWITCH_COLOR_TEMPERATURE_CHANNEL;
                            acceptedItemType = "Dimmer";
                            break;
                        default:
                            break;
                    }
                }
            } else if (!device.getMetrics().getProbeTitle().equals("")) {
                if (device instanceof SensorMultilevel) {
                    switch (device.getMetrics().getProbeTitle()) {
                        case PROBE_TITLE_CO2_LEVEL:
                            id = SENSOR_CO2_CHANNEL;
                            acceptedItemType = "Number";
                            break;
                        default:
                            break;
                    }
                }
            } else if (!device.getMetrics().getIcon().equals("")) {
                if (device instanceof SwitchBinary) {
                    switch (device.getMetrics().getIcon()) {
                        case ICON_SWITCH:
                            id = SWITCH_POWER_OUTLET_CHANNEL;
                            acceptedItemType = "Switch";
                            break;
                        default:
                            break;
                    }
                }
            } else {
                // Eventually take account of the command classes
            }

            // If at least one rule could mapped to a channel
            if (!id.equals("")) {
                addChannel(id, acceptedItemType, device.getMetrics().getTitle(), properties);

                logger.debug(
                        "Channel for virtual device added with channel id: {}, accepted item type: {} and title: {}",
                        id, acceptedItemType, device.getMetrics().getTitle());
            } else {
                // Thing status will not be updated because thing could have more than one channel
                logger.warn("No channel for virtual device added: {}", device);
            }
        }
    }

    private synchronized void addChannel(String id, String acceptedItemType, String label,
            HashMap<String, String> properties) {
        boolean channelExists = false;

        // Check if a channel for this virtual device exist. Attention: same channel type could multiple assigned to a
        // thing. That's why not check the existence of channel type.
        List<Channel> channels = getThing().getChannels();
        for (Channel channel : channels) {
            if (channel.getProperties().get("deviceId").equals(properties.get("deviceId"))) {
                channelExists = true;
            }
        }

        if (!channelExists) {
            ThingBuilder thingBuilder = editThing();
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, id);
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), id + "-" + properties.get("deviceId")),
                            acceptedItemType)
                    .withType(channelTypeUID).withLabel(label).withProperties(properties).build();
            thingBuilder.withChannel(channel);
            thingBuilder.withLabel(thing.getLabel());
            updateThing(thingBuilder.build());
        }
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.handler;

import static org.openhab.binding.energenie.EnergenieBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Handler for the Mi|Home Subdevices
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class EnergenieSubdevicesHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_ENERGY_MONITOR,
            THING_TYPE_MOTION_SENSOR, THING_TYPE_OPEN_SENSOR);

    /**
     * The number of pairing retries that the handler will make
     */
    private static final int PAIRING_RETRY_COUNT = 3;

    /**
     * Time in milliseconds between a single pairing start and end.
     * The user has to press the pairing button in this interval.
     */
    private static final int PARING_WAIT_TIME_MSEC = 15000;

    /**
     * Default update interval in seconds
     */
    public static final BigDecimal DEFAULT_UPDATE_INTERVAL = new BigDecimal(10);

    private final Logger logger = LoggerFactory.getLogger(EnergenieSubdevicesHandler.class);

    /**
     * Update interval in seconds
     */
    private long updateInterval;

    /**
     * A unique device representation in the Mi|Home REST API. Required when executing most of the requests to the
     * Mi|Home REST API
     */
    private Integer energenieID;

    private ScheduledFuture<?> updateTask;

    private ScheduledFuture<?> pairingTask;

    private EnergenieGatewayHandler gatewayHandler;

    public EnergenieSubdevicesHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing with UID {}", getThing().getUID().getAsString());

        // The configuration is validated by the framework before calling initialize() on the handler
        Configuration configuration = getConfig();
        BigDecimal interval = (BigDecimal) configuration.get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL);
        this.updateInterval = interval.longValue();

        Bridge bridge = getBridge();
        if (bridge != null) {
            // Set the gateway handler
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof EnergenieGatewayHandler) {
                this.gatewayHandler = (EnergenieGatewayHandler) handler;
            } else {
                throw new IllegalStateException(
                        "BridgeHandler should be of type " + EnergenieGatewayHandler.class.getName());
            }
            // Check the bridge status
            ThingStatus bridgeStatus = bridge.getStatus();
            if (bridgeStatus == ThingStatus.ONLINE) {
                initializeInternal();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Bridge status is " + bridgeStatus.toString());
            }
        } else {
            logger.warn("Can't initialize ThingHandler, bridge is missing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Bridge is missing");
        }

    }

    private void initializeInternal() {
        // Bridge is ONLINE and this should not be the case, we simply double check
        if (gatewayHandler == null || !isGatewayReady()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Bridge isn't set up correctly");
            return;
        }

        Integer deviceID = getDeviceID();
        if (deviceID != null) {
            logger.debug("Device with ID {} is already paired", deviceID);

            this.energenieID = deviceID;

            JsonSubdevice subdevice = gatewayHandler.getSubdeviceData(this.energenieID);
            if (subdevice != null) {
                updateThingProperties(subdevice);

                updateStatus(ThingStatus.ONLINE);

                scheduleRegularUpdate(updateInterval);
            } else {
                logger.debug("Mi|Home server may be temporary unavailable or device with ID {} may be deleted.",
                        this.energenieID);
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Missing data for paired device with ID " + this.energenieID);
            }
        } else {
            schedulePairing(gatewayHandler.getGatewayId());
        }
    }

    private boolean isGatewayReady() {
        return gatewayHandler.getGatewayId() > 0;
    }

    private Integer getDeviceID() {
        Integer id = null;
        // The thing has been once initialized and the deviceID has been persisted
        logger.debug("Searching deviceID in the thing configuration.");
        Configuration config = getConfig();
        BigDecimal deviceID = (BigDecimal) config.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID);
        if (deviceID != null) {
            id = deviceID.intValue();
            return id;
        }

        // The thing has been created by a DiscoveryService and the deviceID is included in the properties
        logger.debug("Searching device ID in the thing properties.");
        Map<String, String> properties = editProperties();
        if (properties.containsKey(EnergenieBindingConstants.PROPERTY_DEVICE_ID)) {
            String propertyValue = properties.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID);
            try {
                id = Integer.parseInt(propertyValue);
            } catch (NumberFormatException e) {
                logger.debug("Can't parse property {} as int, value is {}",
                        EnergenieBindingConstants.PROPERTY_DEVICE_ID, propertyValue);
            }
        }
        return id;
    }

    private void schedulePairing(final int gatewayID) {
        if (pairingTask != null && !pairingTask.isDone()) {
            logger.info("Pairing in progress !");
            return;
        }
        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();
        final EnergenieDeviceTypes type = EnergenieBindingConstants.THING_TYPE_TO_DEVICE_TYPE.get(thingTypeUID);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < PAIRING_RETRY_COUNT; i++) {
                        logger.debug("Trying to pair the device of type {}, attempt {}", type, i + 1);
                        if (pairDevice(gatewayID, type)) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred during execution of pairing task for gateway with ID {}.",
                            gatewayID, e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Pairing failed. Please retry or check the log for more details.");
                }
            }
        };
        scheduler.schedule(runnable, 0, TimeUnit.SECONDS);
        logger.info("Pairing for device of type {} to Mi|Home gateway with ID {} has been started", type, gatewayID);
    }

    private boolean pairDevice(int gatewayID, EnergenieDeviceTypes deviceType) {
        // List device before starting the pairing
        logger.debug(
                "Gathering information about existing devices of type {}, registered on the Mi|Home gateway with id {}",
                deviceType, gatewayID);
        JsonSubdevice[] subdevicesBefore = gatewayHandler.listSubdevices();
        if (subdevicesBefore == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to get information for subdevices");
            return false;
        }

        // Start the paring
        logger.debug("Initializing device registration for type {} on gateway with ID {}.", deviceType, gatewayID);
        boolean registrationSuccessful = gatewayHandler.initializePairing(gatewayID, deviceType);
        if (!registrationSuccessful) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Unable to start pairing");
            return false;
        }

        // Wait the user to push the button
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING,
                "Please push the pairing button on device of type " + deviceType);
        try {
            Thread.sleep(PARING_WAIT_TIME_MSEC);
        } catch (InterruptedException e) {
            logger.error("Pairing was interrupted ", e);
            Thread.currentThread().interrupt();
            return false;
        }

        logger.debug(
                "Gathering information about existing devices of type {}, registered on the Mi|Home gateway with id {}",
                deviceType, gatewayID);
        // List devices again and see if a new device is added
        JsonSubdevice[] subdevicesAfter = gatewayHandler.listSubdevices();
        if (subdevicesAfter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to get information for subdevices");
            return false;
        }

        logger.debug("Searching for new devices of type {}", deviceType);
        JsonSubdevice newDevice = getNewDevice(subdevicesBefore, subdevicesAfter, deviceType, gatewayID);
        if (newDevice == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Can't find device of type " + deviceType + " for gateway " + gatewayID);
            return false;
        }

        finishPairing(newDevice);

        return true;
    }

    private void finishPairing(JsonSubdevice newDevice) {
        // Get the device ID and persist it
        logger.info("Getting the device ID");
        int deviceID = newDevice.getID();
        Configuration configuration = editConfiguration();
        this.energenieID = deviceID;
        // We persist the device ID, it indicates that the device is already added in the Mi|Home cloud
        configuration.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, this.energenieID);
        updateConfiguration(configuration);

        updateThingProperties(newDevice);

        // Update the label in the Mi|Home portal
        String label = getThing().getLabel();
        JsonSubdevice updateResponse = gatewayHandler.updateSubdevice(this.energenieID, label);
        if (updateResponse == null) {
            logger.warn("Failed to update label of newly paired subdevice {} to {}", this.energenieID, label);
        }

        updateStatus(ThingStatus.ONLINE);

        scheduleRegularUpdate(updateInterval);
    }

    private void scheduleRegularUpdate(long interval) {
        if (updateTask != null) {
            this.updateTask.cancel(true);
            this.updateTask = null;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (getThing().getStatus() == ThingStatus.ONLINE) {
                        JsonSubdevice subdevice = gatewayHandler.getSubdeviceData(energenieID);
                        if (subdevice != null) {
                            // Update the channels
                            List<Channel> channels = getThing().getChannels();
                            for (Channel channel : channels) {
                                ChannelUID uid = channel.getUID();
                                if (isLinked(uid.getId())) {
                                    updateThingState(subdevice, uid);
                                }
                            }

                            // Update the label if changed
                            String newLabel = subdevice.getLabel();
                            if (newLabel != null) {
                                String currentLabel = getThing().getLabel();
                                if (!newLabel.equals(currentLabel)) {
                                    getThing().setLabel(newLabel);
                                }
                            }
                        } else {
                            logger.info(
                                    "Regular update will be stopped! Device may be deleted from the Mi|Home Server.");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Subdevice " + energenieID + " is missing. It might be deleted!");
                        }

                    }
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
                    // The Exception will be caught by the ScheduleExecutorService and it will stop subsequent
                    // executions
                    throw new IllegalStateException("ScheduleExecutorService will stop subsequent executions.", e);
                }

            }
        };
        logger.info("Starting refresh task at interval of {} seconds for device with ID {}", interval, energenieID);
        this.updateTask = scheduler.scheduleWithFixedDelay(runnable, 0, interval, TimeUnit.SECONDS);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();
        ThingStatusDetail thingStatusDetail = getThing().getStatusInfo().getStatusDetail();

        switch (bridgeStatus) {
            case ONLINE:
                bridgeStatusChangedToOnline(thingStatusDetail);
                break;
            case OFFLINE:

            case UNKNOWN:

            case UNINITIALIZED:
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, bridgeStatus.toString());
                }
                break;
            case REMOVED:
                logger.warn(
                        "Gateway has been removed, subdevice {} will be removed as it has been deleted from the Mi|Home server.",
                        energenieID);
                updateStatus(ThingStatus.REMOVED);
                break;
            default:
                // No action needed, we keep the current thing status
                break;
        }
    }

    private void bridgeStatusChangedToOnline(ThingStatusDetail thingStatusDetail) {
        switch (thingStatusDetail) {
            case HANDLER_INITIALIZING_ERROR:
                // The initialization was once interrupted we will retry it
                initializeInternal();
                break;
            case COMMUNICATION_ERROR:

            case BRIDGE_OFFLINE:
                // We set the status back to online, as the bridge is available now
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;
            default:
                // No action needed, we keep the current thing status
                break;
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        // Validate against the config description
        validateConfigurationParameters(configurationParameters);

        // Update the configuration
        Configuration configuration = editConfiguration();
        BigDecimal interval = (BigDecimal) configurationParameters
                .get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL);
        configuration.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL, interval);
        updateConfiguration(configuration);

        this.updateInterval = interval.longValue();

        // Reschedule the job if it is already started
        scheduleRegularUpdate(this.updateInterval);
    }

    private JsonSubdevice getNewDevice(JsonSubdevice[] oldDevices, JsonSubdevice[] newDevices,
            EnergenieDeviceTypes deviceType, int gatewayID) {
        Set<Integer> oldIDs = new HashSet<Integer>();
        // Save the IDs of the old devices that are of the searched type for this bridge
        for (JsonSubdevice device : oldDevices) {
            EnergenieDeviceTypes type = device.getType();
            int gateway = device.getParentID();

            if (deviceType == type && gateway == gatewayID) {
                int deviceID = device.getID();
                oldIDs.add(deviceID);
            }
        }

        // Search for new devices and return the first occurrence
        // We are not interested of the others(if the user pairs multiple device simultaneously), as the thing handler
        // is responsible only for a single device
        for (JsonSubdevice device : newDevices) {
            EnergenieDeviceTypes type = device.getType();
            int deviceID = device.getID();
            int gateway = device.getParentID();

            boolean isNewDeviceFound = (type == deviceType) && (gateway == gatewayID) && (!oldIDs.contains(deviceID));
            if (isNewDeviceFound) {
                return device;
            }
        }

        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ThingStatus status = getThing().getStatus();
        if (status == ThingStatus.ONLINE) {
            JsonSubdevice subdevice = gatewayHandler.getSubdeviceData(energenieID);
            if (subdevice != null) {
                if (command instanceof RefreshType && channelUID.getId().equals(CHANNEL_STATE)) {
                    updateThingState(subdevice, channelUID);
                } else {
                    logger.warn("Unsupported command {} for channel with UID {}", command.toFullString(), channelUID);
                }
            } else {
                logger.warn("Can't execute command {}. No data for device with ID {}", command, this.energenieID);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Subdevice " + energenieID + " is missing, it might be deleted!");
            }
        } else {
            logger.warn("Device with ID {} is in status {}. Not able to handle command {} for channel with UID {}",
                    this.energenieID, status, command.toFullString(), channelUID);
        }

    }

    private void updateThingState(JsonSubdevice subdevice, ChannelUID channelUID) {
        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();

        String channelID = channelUID.getId();
        State state = null;

        switch (thingTypeUID.getId()) {
            case EnergenieBindingConstants.THING_ID_MOTION_SENSOR:
                state = getMotionSensorState(subdevice, channelID);
                break;
            case EnergenieBindingConstants.THING_ID_OPEN_SENSOR:
                state = getOpenSensorState(subdevice, channelID);
                break;
            case EnergenieBindingConstants.THING_ID_ENERGY_MONITOR:
                state = getEnergyMonitorState(subdevice, channelID);
                break;
            default:
                logger.warn("Channel with UID {} won't be updated! It is not supported from thing type {}", channelUID,
                        thingTypeUID);
                return;
        }

        if (state != null) {
            logger.debug("About to update state for channel {} to {}", channelUID, state.toFullString());
            updateState(channelUID, state);
        } else {
            logger.info("Channel with UID {} won't be updated. Server response is incomplete.", channelUID);
        }
    }

    private State getEnergyMonitorState(JsonSubdevice subdevice, String channelID) {
        State state = UnDefType.UNDEF;
        switch (channelID) {
            case EnergenieBindingConstants.CHANNEL_REAL_POWER: {
                state = new DecimalType(subdevice.getRealPower());
                break;
            }
            case EnergenieBindingConstants.CHANNEL_TODAY_CONSUMPTION: {
                state = new DecimalType(subdevice.getTodayWh());
                break;
            }
            case EnergenieBindingConstants.CHANNEL_VOLTAGE: {
                state = new DecimalType(subdevice.getVoltage());
                break;
            }
        }
        return state;
    }

    private State getOpenSensorState(JsonSubdevice subdevice, String channelID) {
        State state = UnDefType.UNDEF;
        if (channelID.equals(EnergenieBindingConstants.CHANNEL_STATE)) {
            Integer jsonElement = subdevice.getSensorState();
            if (jsonElement != null) {
                return jsonElement == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
            } else {
                state = UnDefType.NULL;
            }
        }
        return state;
    }

    private State getMotionSensorState(JsonSubdevice subdevice, String channelID) {
        State state = UnDefType.UNDEF;
        if (channelID.equals(EnergenieBindingConstants.CHANNEL_STATE)) {
            Integer value = subdevice.getSensorState();
            if (value != null) {
                return value == 0 ? OnOffType.OFF : OnOffType.ON;
            } else {
                state = UnDefType.NULL;
            }
        }
        return state;
    }

    protected void updateThingProperties(JsonSubdevice deviceObj) {
        Map<String, String> properties = editProperties();
        properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, Integer.toString(deviceObj.getID()));
        properties.put(EnergenieBindingConstants.PROPERTY_TYPE, deviceObj.getType().toString());
        properties.put(EnergenieBindingConstants.PROPERTY_GATEWAY_ID, Integer.toString(deviceObj.getParentID()));
        updateProperties(properties);
        logger.debug("Thing properties updated");
    }

    @Override
    public void thingUpdated(Thing updatedThing) {
        ThingUID oldBridgeUID = this.thing.getBridgeUID();
        ThingUID newBridgeUID = updatedThing.getBridgeUID();
        if (newBridgeUID != null && !newBridgeUID.equals(oldBridgeUID)) {
            logger.warn(
                    "Mi|Home API doesn't support changing the bridige. The device should be removed and paired to the new bridge");

        }

        String oldLocation = this.thing.getLocation();
        String newLocation = updatedThing.getLocation();
        if (newLocation != null && !newLocation.equals(oldLocation)) {
            this.thing.setLocation(newLocation);
        }

        String oldLabel = this.thing.getLabel();
        String newLabel = updatedThing.getLabel();
        if (newLabel != null && !newLabel.equals(oldLabel)) {
            JsonSubdevice subdevice = gatewayHandler.updateSubdevice(energenieID, newLabel);
            if (subdevice != null) {
                this.thing.setLabel(newLabel);
            } else {
                logger.error("Label of device {} can't be changed to {}. Please check the log for more details.",
                        updatedThing.getUID(), newLabel);
                return;
            }
        }
    }

    @Override
    public void handleRemoval() {
        logger.debug("About to unregister device");
        if (energenieID != null) {
            boolean isUnregistered = gatewayHandler.unregisterSubdevice(energenieID);
            if (!isUnregistered) {
                logger.warn(
                        "Unregistration unsuccessful for subdevice with ID {}. Please try to unregister the device from the Mi|Home server.",
                        energenieID);
                return;
            }
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void dispose() {
        logger.info("Handler with status {} will be disposed", getThing().getStatus());

        if (updateTask != null) {
            logger.debug("Stopping update task for device with ID {}", this.energenieID);
            this.updateTask.cancel(true);
            this.updateTask = null;
        }

        if (pairingTask != null) {
            logger.debug("Stopping pairing task for devive with ID {}", this.energenieID);
            this.pairingTask.cancel(true);
            this.pairingTask = null;
        }
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.MiHomeBindingConstants.*;

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
import org.openhab.binding.mihome.MiHomeBindingConstants;
import org.openhab.binding.mihome.internal.api.constants.DeviceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Handler for the Mi|Home Subdevices
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class MiHomeSubdevicesHandler extends BaseThingHandler {

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
    private static final int WAIT_TIME_PAIRING = 15000;

    /**
     * Default update interval in seconds
     */
    public static final BigDecimal DEFAULT_UPDATE_INTERVAL = new BigDecimal(10);

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Update interval in seconds
     */
    private long updateInterval;

    /**
     * A unique device representation in the MiHome REST API. Required when executing most of the requests to the
     * Mi|Home REST API
     */
    private Integer mihomeID;

    private ScheduledFuture<?> updateTask;

    private ScheduledFuture<?> pairingTask;

    private MiHomeGatewayHandler gatewayHandler;

    public MiHomeSubdevicesHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing with UID {}", getThing().getUID().getAsString());

        // The configuration is validated by the framework before calling initialize() on the handler
        Configuration configuration = getConfig();
        BigDecimal interval = (BigDecimal) configuration.get(MiHomeBindingConstants.CONFIG_UPDATE_ITNERVAL);
        this.updateInterval = interval.longValue();

        Bridge bridge = getBridge();
        if (bridge != null) {
            // Set the gateway handler
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MiHomeGatewayHandler) {
                this.gatewayHandler = (MiHomeGatewayHandler) handler;
            } else {
                throw new IllegalStateException(
                        "BridgeHandler should be of type " + MiHomeGatewayHandler.class.getName());
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
            logger.warn("Cann't initialize ThingHandler, bridge is missing");
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

            this.mihomeID = deviceID;

            JsonObject data = gatewayHandler.getSubdeviceData(this.mihomeID);
            if (data != null) {
                updateThingProperties(data);

                updateStatus(ThingStatus.ONLINE);

                scheduleRegularUpdate(updateInterval);
            } else {
                logger.warn("Mi|Home server may be temporary unavailable or device with ID {} may be deleted.",
                        this.mihomeID);
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Missing data for paired device with ID " + this.mihomeID);
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
        BigDecimal deviceID = (BigDecimal) config.get(MiHomeBindingConstants.PROPERTY_DEVICE_ID);
        if (deviceID != null) {
            id = deviceID.intValue();
            return id;
        }

        // The thing has been created by a DiscoveryService and the deviceID is included in the properties
        logger.debug("Searching device ID in the thing properties.");
        Map<String, String> properties = editProperties();
        if (properties.containsKey(MiHomeBindingConstants.PROPERTY_DEVICE_ID)) {
            String propertyValue = properties.get(MiHomeBindingConstants.PROPERTY_DEVICE_ID);
            try {
                id = Integer.parseInt(propertyValue);
            } catch (NumberFormatException e) {
                logger.debug("Cann't parse property {} as int, value is {}", MiHomeBindingConstants.PROPERTY_DEVICE_ID,
                        propertyValue);
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
        final String type = MiHomeBindingConstants.THING_TYPE_TO_DEVICE_TYPE.get(thingTypeUID);

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
                    logger.error("Exception occurred during execution of pairing task for gateway with ID {}",
                            gatewayID, e);
                }
            }
        };
        scheduler.schedule(runnable, 0, TimeUnit.SECONDS);
        logger.info("Pairing for device of type {} to Mi|Home gateway with ID {} has been started", type, gatewayID);
    }

    private boolean pairDevice(int gatewayID, String deviceType) {
        // List device before starting the pairing
        logger.debug(
                "Gathering information about existing devices of type {}, registered on the Mi|Home gateway with id {}",
                deviceType, gatewayID);
        JsonArray subdevicesBefore = gatewayHandler.listSubdevices();
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
            Thread.sleep(WAIT_TIME_PAIRING);
        } catch (InterruptedException e) {
            logger.error("Pairing was interrupted ", e);
            Thread.currentThread().interrupt();
            return false;
        }

        logger.debug(
                "Gathering information about existing devices of type {}, registered on the Mi|Home gateway with id {}",
                deviceType, gatewayID);
        // List devices again and see if a new device is added
        JsonArray subdevicesAfter = gatewayHandler.listSubdevices();
        if (subdevicesAfter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to get information for subdevices");
            return false;
        }

        logger.debug("Searching for new devices of type {}", deviceType);
        JsonObject newDevice = getNewDevice(subdevicesBefore, subdevicesAfter, deviceType, gatewayID);
        if (newDevice == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Cann't find device of type " + deviceType + " for gateway " + gatewayID);
            return false;
        }

        finishPairing(newDevice);

        return true;
    }

    private void finishPairing(JsonObject newDevice) {
        // Get the device ID and persist it
        logger.info("Getting the device ID");
        int deviceID = newDevice.get(DeviceConstants.DEVICE_ID_KEY).getAsInt();
        Configuration configuration = editConfiguration();
        this.mihomeID = deviceID;
        // We persist the device ID, it indicates that the device is already added in the Mi|Home cloud
        configuration.put(MiHomeBindingConstants.PROPERTY_DEVICE_ID, this.mihomeID);
        updateConfiguration(configuration);

        updateThingProperties(newDevice);

        // Update the label in the MiHome portal
        String label = getThing().getLabel();
        JsonObject updateResponse = gatewayHandler.updateSubdevice(this.mihomeID, label);
        if (updateResponse == null) {
            logger.warn("Failed to udapte label of newly paired subdevice {} to {}", this.mihomeID, label);
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
                        JsonObject data = gatewayHandler.getSubdeviceData(mihomeID);
                        if (data != null) {
                            // Update the channels
                            List<Channel> channels = getThing().getChannels();
                            for (Channel channel : channels) {
                                ChannelUID uid = channel.getUID();
                                if (isLinked(uid.getId())) {
                                    updateThingState(data, uid);
                                }
                            }

                            // Update the label if changed
                            String serverLabel = data.get(DeviceConstants.DEVICE_LABEL_KEY).getAsString();
                            String currentLabel = getThing().getLabel();
                            if (serverLabel != currentLabel) {
                                getThing().setLabel(serverLabel);
                            }
                        } else {
                            logger.info(
                                    "Regular update will be stopped! Device may be deleted from the MiHome Server.");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Subdevice " + mihomeID + " is missing. It might be deleted!");
                        }

                    }
                } catch (Exception e) {
                    logger.error("Exception occurred during execution of update task for device with ID {} : ",
                            mihomeID, e);
                }

            }
        };
        logger.info("Starting refresh task at interval of {} seconds for device with ID {}", interval, mihomeID);
        this.updateTask = scheduler.scheduleAtFixedRate(runnable, 0, interval, TimeUnit.SECONDS);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();
        ThingStatusDetail thingStatusDetail = getThing().getStatusInfo().getStatusDetail();

        switch (bridgeStatus) {
            case ONLINE:
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
                        "Gateway has been removed, subdevice {} will be removed as it has been deleted from the MiHome server.",
                        mihomeID);
                updateStatus(ThingStatus.REMOVED);
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
        BigDecimal interval = (BigDecimal) configurationParameters.get(MiHomeBindingConstants.CONFIG_UPDATE_ITNERVAL);
        configuration.put(MiHomeBindingConstants.CONFIG_UPDATE_ITNERVAL, interval);
        updateConfiguration(configuration);

        this.updateInterval = interval.longValue();

        // Reschedule the job if it is already started
        scheduleRegularUpdate(this.updateInterval);
    }

    private JsonObject getNewDevice(JsonArray oldDevices, JsonArray newDevices, String deviceType, int gatewayID) {
        Set<Integer> oldIDs = new HashSet<Integer>();
        // Save the IDs of the old devices that are of the searched type for this bridge
        for (JsonElement device : oldDevices) {
            JsonObject deviceObj = (JsonObject) device;
            String type = deviceObj.get(DeviceConstants.DEVICE_TYPE_KEY).getAsString();
            int gateway = deviceObj.get(DeviceConstants.SUBDEVICE_PARENT_ID_KEY).getAsInt();

            if (type.equals(deviceType) && gateway == gatewayID) {
                int deviceID = deviceObj.get(DeviceConstants.DEVICE_ID_KEY).getAsInt();
                oldIDs.add(deviceID);
            }
        }

        // Search for new devices
        for (JsonElement device : newDevices) {
            JsonObject deviceObj = (JsonObject) device;
            String type = deviceObj.get(DeviceConstants.DEVICE_TYPE_KEY).getAsString();
            int deviceID = deviceObj.get(DeviceConstants.DEVICE_ID_KEY).getAsInt();
            int gateway = deviceObj.get(DeviceConstants.SUBDEVICE_PARENT_ID_KEY).getAsInt();

            boolean isNewDeviceFound = type.equals(deviceType) && gateway == gatewayID && !oldIDs.contains(deviceID);
            if (isNewDeviceFound) {
                return deviceObj;
            }
        }

        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ThingStatus status = getThing().getStatus();
        if (status == ThingStatus.ONLINE) {
            JsonObject deviceData = gatewayHandler.getSubdeviceData(mihomeID);
            if (deviceData != null) {
                if (command instanceof RefreshType && channelUID.getId().equals(CHANNEL_STATE)) {
                    updateThingState(deviceData, channelUID);
                } else {
                    logger.warn("Unsupported command {} for channel with UID {}", command.toFullString(),
                            channelUID.getAsString());
                }
            } else {
                logger.warn("Cann't execute command {}. No data for device with ID {}", command, this.mihomeID);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Subdevice " + mihomeID + " is missing, it might be deleted!");
            }
        } else {
            logger.warn("Device with ID {} is in status {}. Not able to handle command {} for channel with UID {}",
                    this.mihomeID, status, command.toFullString(), channelUID.getAsString());
        }

    }

    private void updateThingState(JsonObject deviceData, ChannelUID channelUID) {
        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();

        String channelID = channelUID.getId();
        State state = null;

        switch (thingTypeUID.getId()) {
            case MiHomeBindingConstants.THING_ID_MOTION_SENSOR:
                state = getMotionSensorState(deviceData, channelID);
                break;
            case MiHomeBindingConstants.THING_ID_OPEN_SENSOR:
                state = getOpenSensorState(deviceData, channelID);
                break;
            case MiHomeBindingConstants.THING_ID_ENERGY_MONITOR:
                state = getEnergyMonitorState(deviceData, channelID);
                break;
            default:
                logger.warn("Channel with UID {} won't be updated! It is not supported from thing type {}",
                        channelUID.getAsString(), thingTypeUID.getAsString());
        }

        if (state != null) {
            logger.debug("About to update state for channel {} to {}", channelUID.getAsString(), state.toFullString());
            updateState(channelUID, state);
        } else {
            logger.info("Channel with UID {} won't be updated. Server response is incomplete.",
                    channelUID.getAsString());
        }
    }

    private State getEnergyMonitorState(JsonObject deviceData, String channelID) {
        State state = null;
        JsonElement jsonElement = null;
        switch (channelID) {
            case MiHomeBindingConstants.CHANNEL_REAL_POWER:
                jsonElement = deviceData.get(DeviceConstants.MONITOR_REAL_POWER_KEY);
                if (jsonElement != null && !jsonElement.isJsonNull()) {
                    double rawState = jsonElement.getAsDouble();
                    state = new DecimalType(rawState);
                }
                break;
            case MiHomeBindingConstants.CHANNEL_TODAY_CONSUMPTION:
                jsonElement = deviceData.get(DeviceConstants.MONITOR_TODAY_CONSUMPTION_KEY);
                if (jsonElement != null && !jsonElement.isJsonNull()) {
                    double rawState = jsonElement.getAsDouble();
                    state = new DecimalType(rawState);
                }
                break;
            case MiHomeBindingConstants.CHANNEL_VOLTAGE:
                jsonElement = deviceData.get(DeviceConstants.MONITOR_VOLTAGE_KEY);
                if (jsonElement != null && !jsonElement.isJsonNull()) {
                    double rawState = jsonElement.getAsDouble();
                    state = new DecimalType(rawState);
                }
                break;
        }
        return state;
    }

    private State getOpenSensorState(JsonObject deviceData, String channelID) {
        State state = null;
        if (channelID.equals(MiHomeBindingConstants.CHANNEL_STATE)) {
            JsonElement jsonElement = deviceData.get(DeviceConstants.SENSOR_STATE_KEY);
            if (jsonElement != null) {
                if (jsonElement.isJsonNull()) {
                    state = UnDefType.UNDEF;
                } else {
                    int rawState = jsonElement.getAsInt();

                    switch (rawState) {
                        case 0:
                            state = OpenClosedType.CLOSED;
                            break;
                        case 1:
                            state = OpenClosedType.OPEN;
                            break;
                    }
                }
            }
        }
        return state;
    }

    private State getMotionSensorState(JsonObject deviceData, String channelID) {
        State state = null;
        if (channelID.equals(MiHomeBindingConstants.CHANNEL_STATE)) {
            JsonElement jsonElement = deviceData.get(DeviceConstants.SENSOR_STATE_KEY);
            if (jsonElement != null) {
                if (jsonElement.isJsonNull()) {
                    state = UnDefType.UNDEF;
                } else {
                    int rawState = jsonElement.getAsInt();
                    switch (rawState) {
                        case 0:
                            state = OnOffType.OFF;
                            break;
                        case 1:
                            state = OnOffType.ON;
                            break;
                    }
                }
            }
        }
        return state;
    }

    protected void updateThingProperties(JsonObject deviceObj) {
        Map<String, String> properties = editProperties();

        JsonElement idProperty = deviceObj.get(DeviceConstants.DEVICE_ID_KEY);
        if (idProperty != null && !idProperty.isJsonNull()) {
            properties.put(MiHomeBindingConstants.PROPERTY_DEVICE_ID, idProperty.getAsString());
        }
        JsonElement typeProperty = deviceObj.get(DeviceConstants.DEVICE_TYPE_KEY);
        if (typeProperty != null && !typeProperty.isJsonNull()) {
            properties.put(MiHomeBindingConstants.PROPERTY_TYPE, typeProperty.getAsString());
        }
        JsonElement gatewayID = deviceObj.get(DeviceConstants.SUBDEVICE_PARENT_ID_KEY);
        if (gatewayID != null && !gatewayID.isJsonNull()) {
            properties.put(MiHomeBindingConstants.PROPERTY_GATEWAY_ID, gatewayID.getAsString());
        }

        updateProperties(properties);
        logger.debug("Thing properties updated");
    }

    @Override
    public void thingUpdated(Thing thing) {
        ThingUID oldBridgeUID = this.thing.getBridgeUID();
        ThingUID newBridgeUID = thing.getBridgeUID();
        if (newBridgeUID != null && !newBridgeUID.equals(oldBridgeUID)) {
            logger.warn(
                    "Mi|Home API doesn't support changing the bridige. The device should be removed and paired to the new bridge");

        }

        String oldLocation = this.thing.getLocation();
        String newLocation = thing.getLocation();
        if (newLocation != null && !newLocation.equals(oldLocation)) {
            this.thing.setLocation(newLocation);
        }

        String oldLabel = this.thing.getLabel();
        String newLabel = thing.getLabel();
        if (newLabel != null && !newLabel.equals(oldLabel)) {
            JsonObject updateResponse = gatewayHandler.updateSubdevice(mihomeID, newLabel);
            if (updateResponse != null) {
                this.thing.setLabel(newLabel);
            } else {
                logger.error("Label of device {} cann't be changed to {}. Please check the log for more details.",
                        thing.getUID(), newLabel);
                return;
            }
        }
    }

    @Override
    public void handleRemoval() {
        logger.debug("About to unregister device");
        if (mihomeID != null) {
            boolean isUnregistered = gatewayHandler.unregisterSubdevice(mihomeID);
            if (!isUnregistered) {
                logger.warn(
                        "Unregistration unsuccessful for subdevice with ID {}. Please try to unregister the device from the MiHome server.",
                        mihomeID);
                return;
            }
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void dispose() {
        logger.info("Handler with status {} will be disposed", getThing().getStatus());

        if (updateTask != null) {
            logger.debug("Stopping update task for device with ID {}", this.mihomeID);
            this.updateTask.cancel(true);
            this.updateTask = null;
        }

        if (pairingTask != null) {
            logger.debug("Stopping pairing task for devive with ID {}", this.mihomeID);
            this.pairingTask.cancel(true);
            this.pairingTask = null;
        }
    }
}

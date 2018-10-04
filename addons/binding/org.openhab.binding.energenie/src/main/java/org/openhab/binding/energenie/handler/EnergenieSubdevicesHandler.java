/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.handler;

import static org.openhab.binding.energenie.EnergenieBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.internal.api.JsonResponseUtil;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulHttpResponseException;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulJsonResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

/**
 * Handler for the Mi|Home Subdevices
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class EnergenieSubdevicesHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_ENERGY_MONITOR,
            THING_TYPE_MOTION_SENSOR, THING_TYPE_OPEN_SENSOR);

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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Bridge is OFFLINE. Please check your connection.");
            }
        } else {
            logger.warn("Can't initialize ThingHandler, bridge is missing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge is missing");
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
            JsonSubdevice subdevice = null;
            try {
                subdevice = gatewayHandler.getSubdeviceData(this.energenieID);
            } catch (IOException e) {
                logger.error("An error occurred while trying to get subdevice data. Please check your connection", e);
            } catch (UnsuccessfulJsonResponseException e) {
                JsonObject responseData = e.getResponse().get(JsonResponseConstants.DATA_KEY).getAsJsonObject();
                String errorMessage = JsonResponseUtil.getErrorMessageFromResponse(responseData);
                logger.error(errorMessage);
            } catch (UnsuccessfulHttpResponseException e) {
                logger.error("HTTP request failed: {}", e.getResponse().getReason());
            }

            if (subdevice != null) {
                updateStatus(ThingStatus.ONLINE);
                updateThingProperties(subdevice);
                scheduleRegularUpdate(updateInterval);
            }
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
                Double doubleId = Double.valueOf(propertyValue);
                id = doubleId.intValue();
            } catch (NumberFormatException e) {
                logger.debug("Can't parse property {}, value is {}", EnergenieBindingConstants.PROPERTY_DEVICE_ID,
                        propertyValue);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
            }
        }
        return id;
    }

    private void scheduleRegularUpdate(long interval) {
        if (updateTask != null) {
            this.updateTask.cancel(true);
            this.updateTask = null;
        }

        Runnable runnable = () -> {
            try {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    JsonSubdevice subdevice = null;
                    try {
                        subdevice = gatewayHandler.getSubdeviceData(energenieID);
                    } catch (IOException e) {
                        logger.error(
                                "An error occurred while trying to get subdivice data for divice with ID: {}. Please check your connection",
                                energenieID, e);
                    } catch (UnsuccessfulJsonResponseException e) {
                        JsonObject responseData = e.getResponse().get(JsonResponseConstants.DATA_KEY).getAsJsonObject();
                        String errorMessage = JsonResponseUtil.getErrorMessageFromResponse(responseData);
                        logger.error("The JSON response status is: {} and it contains the following error: {}",
                                responseData, errorMessage);
                    } catch (UnsuccessfulHttpResponseException e) {
                        logger.error(
                                "EnergenieApiManager returned unsuccessful http response: {} while trying to find gateway with ID {}.",
                                e.getResponse().getReason(), energenieID);
                    }
                    if (subdevice != null) {
                        // Update the channels
                        List<Channel> channels = getThing().getChannels();
                        for (Channel channel : channels) {
                            ChannelUID uid = channel.getUID();
                            if (isLinked(uid.getId())) {
                                updateChannelState(subdevice, uid);
                            }
                        }

                    } else {
                        logger.debug("Regular update will be stopped! Device may be deleted from the Mi|Home Server.");
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

        };

        logger.debug("Starting refresh task at interval of {} seconds for device with ID {}", interval, energenieID);
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
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Gateway is OFFLINE or missing. Please check your connection.");
                }
                break;
            case REMOVED:
                logger.warn("Gateway has been removed, subdevice {} will be removed.", energenieID);
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ThingStatus status = getThing().getStatus();
        if (status == ThingStatus.ONLINE) {
            JsonSubdevice subdevice = null;
            try {
                subdevice = gatewayHandler.getSubdeviceData(energenieID);
            } catch (IOException e) {
                logger.error("An error occurred while trying to execute: {}. Please check your connection",
                        e.getMessage(), e);
            } catch (UnsuccessfulJsonResponseException e) {
                JsonObject responseData = e.getResponse().get(JsonResponseConstants.DATA_KEY).getAsJsonObject();
                String errorMessage = JsonResponseUtil.getErrorMessageFromResponse(responseData);
                logger.error(errorMessage);
            } catch (UnsuccessfulHttpResponseException e) {
                logger.error("HTTP request failed: {}", e.getResponse().getReason());
            }
            if (subdevice != null) {
                if (command instanceof RefreshType && channelUID.getId().equals(CHANNEL_STATE)) {
                    updateChannelState(subdevice, channelUID);
                } else {
                    logger.debug("Unsupported command {} for channel with UID {}", command.toFullString(), channelUID);
                }
            } else {
                logger.debug("Can't execute command {}. No data for device with ID {}", command, this.energenieID);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Subdevice " + energenieID + " is missing, it might be deleted!");
            }
        } else {
            logger.debug("Device with ID {} is in status {}. Not able to handle command {} for channel with UID {}",
                    this.energenieID, status, command.toFullString(), channelUID);
        }

    }

    private void updateChannelState(JsonSubdevice subdevice, ChannelUID channelUID) {
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
                logger.debug("Channel with UID {} won't be updated! It is not supported from thing type {}", channelUID,
                        thingTypeUID);
                return;
        }

        if (state != null) {
            logger.debug("About to update state for channel {} to {}", channelUID, state.toFullString());
            updateState(channelUID, state);
        } else {
            logger.info("Channel with UID {} won't be updated. Server response is incomplete.", channelUID);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "The server's response is incomplete.");
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
    public void handleRemoval() {
        logger.debug("About to unregister device");
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        logger.debug("Handler with status {} will be disposed", getThing().getStatus());

        if (updateTask != null) {
            logger.debug("Stopping update task for device with ID {}", this.energenieID);
            this.updateTask.cancel(true);
            this.updateTask = null;
        }
    }

    public ScheduledFuture<?> getUpdateTask() {
        return updateTask;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }
}

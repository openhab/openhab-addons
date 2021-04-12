/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.tapocontrol.internal.device;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.TapoControlConfiguration;
import org.openhab.binding.tapocontrol.internal.api.TapoConnector;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class as base for TAPO-Device device implementations.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public abstract class TapoDevice extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoDevice.class);
    protected final String uid;
    protected @NonNullByDefault({}) TapoConnector connector;
    protected @NonNullByDefault({}) TapoControlConfiguration config;
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected TapoCredentials credentials;

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoDevice(Thing thing, TapoCredentials credentials) {
        super(thing);
        this.uid = getThing().getUID().getAsString();
        this.credentials = credentials;
    }

    /**
     * INITIALIZE DEVICE
     */
    @Override
    public void initialize() {
        logger.debug("initialize thing-device ({})", uid);
        String ipAddress = getThing().getConfiguration().get(CONFIG_DEVICE_IP).toString();
        this.connector = new TapoConnector(getThing().getUID(), ipAddress, credentials);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // background initialization (delay it a little bit):
        scheduler.schedule(this::connect, 2000, TimeUnit.MILLISECONDS);
        startScheduler();
    }

    /**
     * DISPOSE
     */
    @Override
    public void dispose() {
        stopScheduler();
        connector.logout();
    }

    /**
     * Checks if the response object contains errors and if so throws an {@link IOException} when an error code was set.
     *
     * @throws IOException if an error code was set in the response object
     */
    protected void checkErrors() throws IOException {
        final Integer errorCode = this.connector.errorCode();

        if (errorCode != 0) {
            throw new IOException("Error (" + errorCode + "): " + this.connector.errorMessage());
        }
    }

    /**
     * Start scheduler
     */
    protected void startScheduler() {
        String interval = getThing().getConfiguration().get(CONFIG_UPDATE_INTERVAL).toString();
        Integer pollingInterval = Integer.valueOf(interval);

        if (pollingInterval > 0) {
            this.pollingJob = scheduler.scheduleWithFixedDelay(this::schedulerAction, 0, pollingInterval,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Stop scheduler
     */
    protected void stopScheduler() {
        if (this.pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Scheduler Action
     */
    protected void schedulerAction() {
        if (checkDeviceConnection()) {
            queryDeviceInfo();
        }
    }

    /**
     * Check Device Connection and Login
     * Connection will only be checked if device no configuration error
     * 
     * @return true if is connected
     */
    protected Boolean checkDeviceConnection() {
        ThingStatus deviceState = getThing().getStatus();
        ThingStatusDetail deviceStateDetail = getThing().getStatusInfo().getStatusDetail();
        if (IGNORE_CONFIG_ERROR || deviceStateDetail != ThingStatusDetail.CONFIGURATION_ERROR) {
            if (this.connector.isOnline(true)) {
                /* try to login if not */
                if (deviceState == ThingStatus.ONLINE && this.connector.loggedIn()) {
                    return true;
                } else {
                    return connect();
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
        return false;
    }

    /**
     * Handle command
     * 
     */
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * query device Properties
     * 
     */
    public void queryDeviceInfo() {
        TapoDeviceInfo deviceInfo = connector.queryInfo();
        devicePropertiesChanged(deviceInfo);
        if (connector.hasError()) {
            logger.debug("({}) queryDeviceInfo received error : {}", uid, connector.errorMessage());
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, connector.errorMessage());
        }
    }

    /**
     * Connect (login) to device
     * 
     */
    private Boolean connect() {
        Boolean loginSuccess = false;

        try {
            if (connector.isOnline(true)) {
                loginSuccess = connector.login();
                if (loginSuccess) {
                    TapoDeviceInfo deviceInfo = connector.queryInfo();
                    if (isThingModel(deviceInfo.getModel())) {
                        updateStatus(ThingStatus.ONLINE);
                        devicePropertiesChanged(deviceInfo);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "wrong device found: '" + deviceInfo.getModel() + "'");
                        return false;
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, connector.errorMessage());
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return loginSuccess;
    }

    /***
     * Check if ThingType is model
     * 
     * @param model
     * @return
     */
    protected Boolean isThingModel(String model) {
        try {
            ThingTypeUID foundType = new ThingTypeUID(BINDING_ID, model);
            ThingTypeUID expectedType = getThing().getThingTypeUID();
            return expectedType.equals(foundType);
        } catch (Exception e) {
            logger.warn("({}) verify thing model throws : {}", uid, e.getMessage());
            return false;
        }
    }

    /**
     * Get ChannelID including group
     * 
     * @param group String channel-group
     * @param channel String channel-name
     * @return String channelID
     */
    protected String getChannelID(String group, String channel) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CHANNEL_GROUP_THING_SET.contains(thingTypeUID) && group.length() > 0) {
            return group + "#" + channel;
        }
        return channel;
    }

    /**
     * Get Channel from ChannelID
     * 
     * @param channelID String channelID
     * @return String channel-name
     */
    protected String getChannelFromID(ChannelUID channelID) {
        String channel = channelID.getIdWithoutGroup();
        channel = channel.replace(CHANNEL_GROUP_ACTUATOR + "#", "");
        channel = channel.replace(CHANNEL_GROUP_DEVICE + "#", "");
        return channel;
    }

    /**
     * UPDATE PROPERTIES
     * 
     * If only one property must be changed, there is also a convenient method
     * updateProperty(String name, String value).
     * 
     * @param TapoDeviceInfo
     */
    protected void devicePropertiesChanged(TapoDeviceInfo deviceInfo) {
        /* device properties */
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, deviceInfo.getMAC());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getFirmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, deviceInfo.getHardwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceInfo.getModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceInfo.getSerial());
        // properties.put(PROPERTY_WIFI_LEVEL, deviceInfo.getSignalLevel().toString());
        updateProperties(properties);
    }

    /**
     * update channel state
     * 
     * @param channelID
     * @param value
     */
    public void publishState(String channelID, State value) {
        updateState(channelID, value);
    }
}

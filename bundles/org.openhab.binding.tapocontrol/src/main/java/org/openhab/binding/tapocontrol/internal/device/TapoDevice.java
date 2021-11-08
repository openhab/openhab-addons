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
import static org.openhab.binding.tapocontrol.internal.helpers.TapoErrorConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.TapoControlConfiguration;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
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
    private final HttpClient httpClient;
    protected final String uid;
    private String ipAddress = "";
    // protected @NonNullByDefault({}) TapoDeviceConnector connector;
    protected @NonNullByDefault({}) TapoControlConfiguration config;
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected @Nullable TapoDeviceConnector connector;
    protected TapoCredentials credentials;

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoDevice(Thing thing, HttpClient httpClient) {
        super(thing);
        this.uid = getThing().getUID().getAsString();
        this.credentials = new TapoCredentials();
        this.httpClient = httpClient;
    }

    /***********************************
     *
     * INIT AND SETTINGS
     *
     ************************************/

    /**
     * INITIALIZE DEVICE
     */
    @Override
    public void initialize() {
        logger.debug("initialize thing-device ({})", uid);
        String username = "";
        String password = "";
        try {
            Bridge bridge = this.getBridge();
            username = bridge.getConfiguration().get(CONFIG_EMAIL).toString();
            password = bridge.getConfiguration().get(CONFIG_PASS).toString();
            this.credentials.setCredectials(username, password);
            this.ipAddress = getThing().getConfiguration().get(CONFIG_DEVICE_IP).toString();
        } catch (Exception e) {
            logger.debug("({}) configuration error : {}", uid, e.getMessage());
        }
        TapoErrorHandler configError = checkSettings();
        if (!configError.hasError()) {
            this.connector = new TapoDeviceConnector(this, credentials, httpClient);

            // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
            updateStatus(ThingStatus.UNKNOWN);

            // background initialization (delay it a little bit):
            scheduler.schedule(this::connect, 2000, TimeUnit.MILLISECONDS);
            startScheduler();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError.getMessage());
        }
    }

    /**
     * DISPOSE
     */
    @Override
    public void dispose() {
        try {
            stopScheduler();
            connector.logout();
        } catch (Exception e) {
            // handle exception
        }
        super.dispose();
    }

    /**
     * CHECK SETTINGS
     * 
     * @return TapoErrorHandler with configuration-errors
     */
    protected TapoErrorHandler checkSettings() {
        TapoErrorHandler configErr = new TapoErrorHandler();
        /* check bridge */
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null || !(bridge.getHandler() instanceof TapoBridgeHandler)) {
            logger.error("({}) Device has no brigde", uid);
            configErr.raiseError(ERR_NO_BRIDGE);
            return configErr;
        }
        /* check ip-address */
        if (!this.ipAddress.matches(IPV4_REGEX)) {
            logger.error("({}) IP-Address not matching : '{}'", uid, ipAddress);
            configErr.raiseError(ERR_CONF_IP);
            return configErr;
        }
        /* check credentials */
        if (this.credentials.getUsername().isEmpty() || this.credentials.getPassword().isEmpty()) {
            logger.error("({}) Password or Username not set (bridge)", uid);
            configErr.raiseError(ERR_CONF_CREDENTIALS);
            return configErr;
        }
        return configErr;
    }

    /**
     * Checks if the response object contains errors and if so throws an {@link IOException} when an error code was set.
     *
     * @throws IOException if an error code was set in the response object
     */
    protected void checkErrors() throws IOException {
        final Integer errorCode = connector.getError().getCode();

        if (errorCode != 0) {
            throw new IOException("Error (" + errorCode + "): " + connector.getError().getMessage());
        }
    }

    /***********************************
     *
     * SCHEDULER
     *
     ************************************/

    /**
     * Start scheduler
     */
    protected void startScheduler() {
        String interval = getThing().getConfiguration().get(CONFIG_UPDATE_INTERVAL).toString();
        Integer pollingInterval = Integer.valueOf(interval);

        if (pollingInterval > 0) {
            if (pollingInterval < POLLING_MIN_INTERVAL_S) {
                pollingInterval = POLLING_MIN_INTERVAL_S;
            }
            logger.trace("({}) starScheduler: create job with interval : {}", uid, pollingInterval);
            this.pollingJob = scheduler.scheduleWithFixedDelay(this::schedulerAction, pollingInterval, pollingInterval,
                    TimeUnit.SECONDS);
        } else {
            stopScheduler();
        }
    }

    /**
     * Stop scheduler
     */
    protected void stopScheduler() {
        if (this.pollingJob != null) {
            logger.trace("({}) stopScheduler", uid);
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Scheduler Action
     */
    protected void schedulerAction() {
        logger.trace("({}) schedulerAction", uid);
        queryDeviceInfo();
    }

    /***********************************
     *
     * THING
     *
     ************************************/

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
     * CHECK IF RECEIVED DATA ARE FROM THE EXPECTED DEVICE
     * Compare MAC-Adress
     * 
     * @param deviceInfo
     * @return true if is the expected device
     */
    protected Boolean isExpectedThing(TapoDeviceInfo deviceInfo) {
        try {
            String expectedThingUID = getThing().getProperties().get(DEVICE_REPRASENTATION_PROPERTY);
            String foundThingUID = deviceInfo.getRepresentationProperty();
            String foundModel = deviceInfo.getModel();
            if (expectedThingUID == null || expectedThingUID.isBlank()) {
                return isThingModel(foundModel);
            }
            /* sometimes received mac was with and sometimes without "-" from device */
            expectedThingUID = unformatMac(expectedThingUID);
            foundThingUID = unformatMac(foundThingUID);
            return expectedThingUID.equals(foundThingUID);
        } catch (Exception e) {
            logger.warn("({}) verify thing model throws : {}", uid, e.getMessage());
            return false;
        }
    }

    /**
     * Return ThingUID
     */
    public ThingUID getThingUID() {
        return getThing().getUID();
    }

    /***********************************
     *
     * DEVICE PROPERTIES
     *
     ************************************/

    /**
     * query device Properties
     * 
     */
    public void queryDeviceInfo() {
        if (connector.loggedIn()) {
            connector.queryInfo();
        } else {
            logger.debug("({}) tried to query DeviceInfo but not loggedIn", uid);
            connect();
        }
    }

    /**
     * SET DEVICE INFOs to device
     * 
     * @param deviceInfo
     */
    public void setDeviceInfo(TapoDeviceInfo deviceInfo) {
        if (isExpectedThing(deviceInfo)) {
            devicePropertiesChanged(deviceInfo);
            handleConnectionState();
        } else {
            logger.warn("({}) wrong device found - found: {} with mac:{}", uid, deviceInfo.getModel(),
                    deviceInfo.getRepresentationProperty());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "found type:'" + deviceInfo.getModel() + "' with mac:'" + deviceInfo.getRepresentationProperty()
                            + "'. Check IP-Address");
        }
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

    /***********************************
     *
     * CONNECTION
     *
     ************************************/

    /**
     * Connect (login) to device
     * 
     */
    public Boolean connect() {
        Boolean loginSuccess = false;

        try {
            loginSuccess = connector.login();
            if (loginSuccess) {
                connector.queryInfo();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        connector.getError().getMessage());
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.UNKNOWN);
        }
        return loginSuccess;
    }

    /**
     * disconnect device
     */
    public void disconnect() {
        connector.logout();
    }

    /**
     * handle device state by connector error
     */
    public void handleConnectionState() {
        ThingStatus deviceState = getThing().getStatus();
        Integer errorCode = connector.getError().getCode();

        if (errorCode == 0) {
            if (deviceState != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else if (LIST_REAUTH_ERRORS.contains(errorCode)) {
            connect();
        } else if (LIST_COMMUNICATION_ERRORS.contains(errorCode)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, connector.getError().getMessage());
            disconnect();
        } else if (LIST_CONFIGURATION_ERRORS.contains(errorCode)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, connector.getError().getMessage());
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, connector.getError().getMessage());
        }
    }

    /**
     * Return IP-Address of device
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /***********************************
     *
     * CHANNELS
     *
     ************************************/
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
}

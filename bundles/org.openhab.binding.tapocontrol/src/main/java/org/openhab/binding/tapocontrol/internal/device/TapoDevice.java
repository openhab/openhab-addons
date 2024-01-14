/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.structures.TapoChildData;
import org.openhab.binding.tapocontrol.internal.structures.TapoDeviceConfiguration;
import org.openhab.binding.tapocontrol.internal.structures.TapoDeviceInfo;
import org.openhab.binding.tapocontrol.internal.structures.TapoEnergyData;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
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
    protected final TapoErrorHandler deviceError = new TapoErrorHandler();
    protected final String uid;
    protected TapoDeviceConfiguration config = new TapoDeviceConfiguration();
    protected TapoDeviceInfo deviceInfo;
    protected @Nullable ScheduledFuture<?> startupJob;
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected @NonNullByDefault({}) TapoDeviceConnector connector;
    protected @NonNullByDefault({}) TapoBridgeHandler bridge;

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    protected TapoDevice(Thing thing) {
        super(thing);
        this.deviceInfo = new TapoDeviceInfo();
        this.uid = getThing().getUID().getAsString();
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
        try {
            this.config = getConfigAs(TapoDeviceConfiguration.class);
            Bridge bridgeThing = getBridge();
            if (bridgeThing != null) {
                BridgeHandler bridgeHandler = bridgeThing.getHandler();
                if (bridgeHandler != null) {
                    this.bridge = (TapoBridgeHandler) bridgeHandler;
                    this.connector = new TapoDeviceConnector(this, bridge);
                }
            }
        } catch (Exception e) {
            logger.debug("({}) configuration error : {}", uid, e.getMessage());
        }
        TapoErrorHandler configError = checkSettings();
        if (!configError.hasError()) {
            activateDevice();
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
            stopScheduler(this.startupJob);
            stopScheduler(this.pollingJob);
            connector.logout();
        } catch (Exception e) {
            // handle exception
        }
        super.dispose();
    }

    /**
     * ACTIVATE DEVICE
     */
    private void activateDevice() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // background initialization (delay it a little bit):
        this.startupJob = scheduler.schedule(this::delayedStartUp, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * CHECK SETTINGS
     *
     * @return TapoErrorHandler with configuration-errors
     */
    protected TapoErrorHandler checkSettings() {
        TapoErrorHandler configErr = new TapoErrorHandler();

        /* check bridge */
        if (!(bridge instanceof TapoBridgeHandler)) {
            configErr.raiseError(ERR_CONFIG_NO_BRIDGE);
            return configErr;
        }
        /* check ip-address */
        if (!config.ipAddress.matches(IPV4_REGEX)) {
            configErr.raiseError(ERR_CONFIG_IP);
            return configErr;
        }
        /* check credentials */
        if (!bridge.getCredentials().areSet()) {
            configErr.raiseError(ERR_CONFIG_CREDENTIALS);
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
        final Integer errorCode = deviceError.getCode();

        if (errorCode != 0) {
            throw new IOException("Error (" + errorCode + "): " + deviceError.getMessage());
        }
    }

    /***********************************
     *
     * SCHEDULER
     *
     ************************************/
    /**
     * delayed OneTime StartupJob
     */
    private void delayedStartUp() {
        connect();
        startPollingScheduler();
    }

    /**
     * Start scheduler
     */
    protected void startPollingScheduler() {
        int pollingInterval = this.config.pollingInterval;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        if (pollingInterval > 0) {
            if (pollingInterval < POLLING_MIN_INTERVAL_S) {
                pollingInterval = POLLING_MIN_INTERVAL_S;
            }
            logger.debug("({}) startScheduler: create job with interval : {} {}", uid, pollingInterval, timeUnit);
            this.pollingJob = scheduler.scheduleWithFixedDelay(this::pollingSchedulerAction, pollingInterval,
                    pollingInterval, timeUnit);
        } else {
            logger.debug("({}) scheduler disabled with config '0'", uid);
            stopScheduler(this.pollingJob);
        }
    }

    /**
     * Stop scheduler
     *
     * @param scheduler {@code ScheduledFeature<?>} which schould be stopped
     */
    protected void stopScheduler(@Nullable ScheduledFuture<?> scheduler) {
        if (scheduler != null) {
            scheduler.cancel(true);
            scheduler = null;
        }
    }

    /**
     * Scheduler Action
     */
    protected void pollingSchedulerAction() {
        logger.trace("({}) schedulerAction", uid);
        queryDeviceInfo();
    }

    /***********************************
     *
     * ERROR HANDLER
     *
     ************************************/
    /**
     * return device Error
     *
     * @return
     */
    public TapoErrorHandler getErrorHandler() {
        return this.deviceError;
    }

    public TapoErrorCode getError() {
        return this.deviceError.getError();
    }

    /**
     * set device error
     *
     * @param tapoError TapoErrorHandler-Object
     */
    public void setError(TapoErrorHandler tapoError) {
        this.deviceError.set(tapoError);
        handleConnectionState();
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
            String expectedThingUID = getThing().getProperties().get(DEVICE_REPRESENTATION_PROPERTY);
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
     */
    public void queryDeviceInfo() {
        queryDeviceInfo(false);
    }

    /**
     * query device Properties
     *
     * @param ignoreGap ignore gap to last query. query anyway (force)
     */
    public void queryDeviceInfo(boolean ignoreGap) {
        deviceError.reset();
        if (connector.loggedIn()) {
            connector.queryInfo(ignoreGap);
            // query energy usage
            if (SUPPORTED_ENERGY_DATA_UIDS.contains(getThing().getThingTypeUID())) {
                connector.getEnergyUsage();
            }
            // query childs data
            if (SUPPORTED_CHILDS_DATA_UIDS.contains(getThing().getThingTypeUID())) {
                connector.queryChildDevices();
            }
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
        this.deviceInfo = deviceInfo;
        if (isExpectedThing(deviceInfo)) {
            devicePropertiesChanged(deviceInfo);
            handleConnectionState();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "found type:'" + deviceInfo.getModel() + "' with mac:'" + deviceInfo.getRepresentationProperty()
                            + "'. Check IP-Address");
        }
    }

    /**
     * Set Device EnergyData to device
     *
     * @param energyData
     */
    public void setEnergyData(TapoEnergyData energyData) {
        publishState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_POWER),
                getPowerType(energyData.getCurrentPower(), Units.WATT));
        publishState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_USAGE_TODAY),
                getEnergyType(energyData.getTodayEnergy(), Units.WATT_HOUR));
        publishState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_RUNTIME_TODAY),
                getTimeType(energyData.getTodayRuntime(), Units.MINUTE));
    }

    /**
     * Set Device Child data to device
     *
     * @param hostData
     */
    public void setChildData(TapoChildData hostData) {
        hostData.getChildDeviceList().forEach(child -> {
            publishState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT + Integer.toString(child.getPosition())),
                    getOnOffType(child.getDeviceOn()));
        });
    }

    /**
     * Handle full responsebody received from connector
     *
     * @param responseBody
     */
    public void responsePasstrough(String responseBody) {
    }

    /**
     * UPDATE PROPERTIES
     *
     * If only one property must be changed, there is also a convenient method
     * updateProperty(String name, String value).
     *
     * @param deviceInfo
     */
    protected void devicePropertiesChanged(TapoDeviceInfo deviceInfo) {
        /* device properties */
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, deviceInfo.getMAC());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getFirmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, deviceInfo.getHardwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceInfo.getModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceInfo.getSerial());
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
        deviceError.reset();
        Boolean loginSuccess = false;

        try {
            loginSuccess = connector.login();
            if (loginSuccess) {
                queryDeviceInfo(true);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, deviceError.getMessage());
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
        TapoErrorCode errorCode = deviceError.getError();

        if (errorCode == TapoErrorCode.NO_ERROR) {
            if (deviceState != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            switch (errorCode.getType()) {
                case COMMUNICATION_RETRY:
                    connect();
                    break;
                case COMMUNICATION_ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, deviceError.getMessage());
                    disconnect();
                    break;
                case CONFIGURATION_ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, deviceError.getMessage());
                    break;
                default:
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, deviceError.getMessage());
            }
        }
    }

    /**
     * Return IP-Address of device
     */
    public String getIpAddress() {
        return this.config.ipAddress;
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
        channel = channel.replace(CHANNEL_GROUP_EFFECTS + "#", "");
        channel = channel.replace(CHANNEL_GROUP_ENERGY + "#", "");
        return channel;
    }
}

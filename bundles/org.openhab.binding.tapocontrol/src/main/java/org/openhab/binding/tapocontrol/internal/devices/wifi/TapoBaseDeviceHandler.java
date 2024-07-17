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
package org.openhab.binding.tapocontrol.internal.devices.wifi;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode;
import org.openhab.binding.tapocontrol.internal.devices.bridge.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoBaseDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoEnergyData;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class as base for TAPO-Device device implementations.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public abstract class TapoBaseDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoBaseDeviceHandler.class);
    protected final TapoErrorHandler deviceError = new TapoErrorHandler();
    protected final String uid;
    protected TapoDeviceConfiguration deviceConfig = new TapoDeviceConfiguration();
    protected TapoBaseDeviceData baseDeviceData = new TapoBaseDeviceData();
    protected TapoEnergyData energyData = new TapoEnergyData();
    protected @Nullable ScheduledFuture<?> startupJob;
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected @NonNullByDefault({}) TapoDeviceConnector connector;
    protected @NonNullByDefault({}) TapoBridgeHandler bridge;

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    protected TapoBaseDeviceHandler(Thing thing) {
        super(thing);
        this.uid = getThing().getUID().getAsString();
    }

    /***********************************
     * INIT AND SETTINGS
     ************************************/

    /**
     * INITIALIZE DEVICE
     */
    @Override
    public void initialize() {
        try {
            deviceConfig = getConfigAs(TapoDeviceConfiguration.class);
            initConnector();
            if (checkRequirements()) {
                activateDevice();
            }
        } catch (TapoErrorHandler te) {
            logger.warn("({}) deviceConfiguration error : {}", uid, te.getMessage());
            setError(te);
        } catch (Exception e) {
            logger.debug("({}) error initializing device : {}", uid, e.getMessage());
        }
    }

    /**
     * Init TapoBridgeHandler TapoDeviceConnector
     */
    protected void initConnector() {
        Bridge bridgeThing = getBridge();
        if (bridgeThing != null) {
            BridgeHandler bridgeHandler = bridgeThing.getHandler();
            if (bridgeHandler instanceof TapoBridgeHandler tapoBridgeHandler) {
                this.bridge = tapoBridgeHandler;
                this.connector = new TapoDeviceConnector(this, bridge);
            }
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
    protected void activateDevice() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // background initialization (delay it a little bit):
        this.startupJob = scheduler.schedule(this::delayedStartUp, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * Check if bridge is set
     */
    protected boolean checkBridge() throws TapoErrorHandler {
        /* check bridge */
        if (!(bridge instanceof TapoBridgeHandler)) {
            throw new TapoErrorHandler(ERR_CONFIG_NO_BRIDGE);
        }
        /* check credentials */
        if (!bridge.getCredentials().areSet()) {
            throw new TapoErrorHandler(ERR_CONFIG_CREDENTIALS);
        }
        return true;
    }

    /**
     * Check if Bridge is set and deviceConfiguration is set
     */
    protected boolean checkRequirements() throws TapoErrorHandler {
        return (checkBridge() && deviceConfig.checkConfig());
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
     * SCHEDULER
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
        int pollingInterval = deviceConfig.pollingInterval;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        if (pollingInterval > 0) {
            if (pollingInterval < POLLING_MIN_INTERVAL_S) {
                pollingInterval = POLLING_MIN_INTERVAL_S;
            }
            logger.debug("({}) startScheduler: create job with interval : {} {}", uid, pollingInterval, timeUnit);
            this.pollingJob = scheduler.scheduleWithFixedDelay(this::pollingSchedulerAction, pollingInterval,
                    pollingInterval, timeUnit);
        } else {
            logger.debug("({}) scheduler disabled with deviceConfig '0'", uid);
            stopScheduler(this.pollingJob);
        }
    }

    /**
     * Stop scheduler
     *
     * @param scheduler ScheduledFeature which should be stopped
     */
    protected void stopScheduler(@Nullable ScheduledFuture<?> scheduler) {
        if (scheduler != null) {
            scheduler.cancel(true);
        }
    }

    /**
     * Scheduler Action
     */
    protected void pollingSchedulerAction() {
        logger.trace("({}) schedulerAction", uid);
        queryDeviceData();
    }

    /***********************************
     * ERROR HANDLER
     ************************************/
    /**
     * return device Error
     */
    public TapoErrorHandler getErrorHandler() {
        return deviceError;
    }

    public TapoErrorCode getError() {
        return deviceError.getError();
    }

    /**
     * set device error
     *
     * @param tapoError TapoErrorHandler-Object
     */
    public void setError(TapoErrorHandler tapoError) {
        deviceError.set(tapoError);
        handleConnectionState();
    }

    /***********************************
     * THING
     ************************************/

    /***
     * Check if ThingType is model
     *
     * @param model
     * @return
     */
    protected Boolean isThingModel(String model) {
        try {
            model = getDeviceModel(model);
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
     * @param baseDeviceData basebaseDeviceData
     * @return true if is the expected device
     */
    protected boolean isExpectedThing(TapoBaseDeviceData baseDeviceData) {
        try {
            String expectedThingUID = getThing().getProperties().get(DEVICE_REPRESENTATION_PROPERTY);
            String foundThingUID = baseDeviceData.getRepresentationProperty();
            String foundModel = baseDeviceData.getModel();
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

    /**
     * Return ipAdress
     */
    public String getIpAddress() {
        return deviceConfig.ipAddress;
    }

    /*
     * return device configuration
     */
    public TapoDeviceConfiguration getDeviceConfig() {
        return deviceConfig;
    }

    /***********************************
     * DEVICE PROPERTIES
     ************************************/

    /**
     * query default device properties
     * query baseDeviceData, energyData (if available for device) and childData (if available for device).
     */
    public void queryDeviceData() {
        queryDeviceData(false);
    }

    /**
     * query default device properties
     * query baseDeviceData, energyData (if available for device)
     * 
     * @param ignoreGap ignore gap to last query. query anyway (force)
     */
    public void queryDeviceData(boolean ignoreGap) {
        deviceError.reset();
        if (isLoggedIn(LOGIN_RETRIES)) {
            if (SUPPORTED_ENERGY_DATA_UIDS.contains(getThing().getThingTypeUID())) {
                List<TapoRequest> requests = new ArrayList<>();
                requests.add(new TapoRequest(DEVICE_CMD_GETINFO));
                requests.add(new TapoRequest(DEVICE_CMD_GETENERGY));
                connector.sendMultipleRequest(requests, ignoreGap);
            } else {
                connector.sendQueryCommand(DEVICE_CMD_GETINFO, ignoreGap);
            }
        }
    }

    /**
     * Function called by {@link org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector} if new data were
     * received
     * 
     * @param queryCommand command where new data belong to
     */
    public void newDataResult(String queryCommand) {
        switch (queryCommand) {
            case DEVICE_CMD_GETINFO:
                baseDeviceData = connector.getResponseData(TapoBaseDeviceData.class);
                updateBaseDeviceData();
                break;
            case DEVICE_CMD_GETENERGY:
                energyData = connector.getResponseData(TapoEnergyData.class);
                updateEnergyData(energyData);
                break;
            case DEVICE_CMD_CUSTOM:
                responsePasstrough(connector.getResponseData(TapoResponse.class));
                break;
            default:
                responsePasstrough(connector.getResponseData(TapoResponse.class));
                break;
        }
        handleConnectionState();
    }

    /**
     * handle baseDeviceData
     */
    private void updateBaseDeviceData() {
        if (!baseDeviceData.getDeviceId().isBlank()) {
            if (isExpectedThing(baseDeviceData)) {
                updateDeviceProperties(baseDeviceData);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "found type:'" + baseDeviceData.getModel() + "' with mac:'"
                                + baseDeviceData.getRepresentationProperty() + "'. Check IP-Address");
            }
        } else {
            logger.debug("({}) tried to update device with empty data", uid);
        }
    }

    /**
     * Handle full responsebody received from connector
     * 
     * @param fullResponse complete TapoResponse
     */
    public void responsePasstrough(TapoResponse fullResponse) {
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
    public boolean connect() {
        deviceError.reset();
        boolean loginSuccess = false;

        try {
            loginSuccess = connector.login();
            if (loginSuccess) {
                queryDeviceData(true);
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
     * check if is device is loged in.
     * 
     * @param totalRetries times to retry login
     */
    public boolean isLoggedIn(int totalRetries) {
        if (connector.isLoggedIn()) {
            return true;
        } else {
            logger.debug("({}) check if logged in but is not", uid);
            for (int count = 0; count < totalRetries; count++) {
                connect();
            }
            return false;
        }
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

    /***********************************
     * CHANNELS
     ************************************/

    /**
     * Update Basic Device Properties
     *
     * If only one property must be changed, there is also a convenient method
     * updateProperty(String name, String value).
     *
     * @param baseDeviceData Object BaseDeviceData
     */
    protected void updateDeviceProperties(TapoBaseDeviceData baseDeviceData) {
        /* device properties */
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, baseDeviceData.getMAC());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, baseDeviceData.getFirmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, baseDeviceData.getHardwareVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, baseDeviceData.getModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, baseDeviceData.getDeviceId());
        updateProperties(properties);
    }

    /**
     * Update Energy Data Channels
     */
    protected void updateEnergyData(TapoEnergyData energyData) {
        updateState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_POWER),
                getPowerType(energyData.getCurrentPower(), Units.WATT));
        updateState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_USAGE_TODAY),
                getEnergyType(energyData.getTodayEnergy(), Units.WATT_HOUR));
        updateState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_RUNTIME_TODAY),
                getTimeType(energyData.getTodayRuntime(), Units.MINUTE));
        updateState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_USAGE_MONTH),
                getEnergyType(energyData.getMonthEnergy(), Units.WATT_HOUR));
        updateState(getChannelID(CHANNEL_GROUP_ENERGY, CHANNEL_NRG_RUNTIME_MONTH),
                getTimeType(energyData.getMonthRuntime(), Units.MINUTE));
    }

    /**
     * Update custom channels of device
     * 
     * @param baseDeviceData extended devicebelonging dataclass
     */
    protected void updateChannels(final Class<? extends TapoBaseDeviceData> baseDeviceData) {
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
}

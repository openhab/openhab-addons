/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.handler;

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mideaac.internal.MideaACConfiguration;
import org.openhab.binding.mideaac.internal.connection.CommandHelper;
import org.openhab.binding.mideaac.internal.connection.ConnectionManager;
import org.openhab.binding.mideaac.internal.connection.exception.MideaAuthenticationException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaConnectionException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaException;
import org.openhab.binding.mideaac.internal.discovery.DiscoveryHandler;
import org.openhab.binding.mideaac.internal.discovery.MideaACDiscoveryService;
import org.openhab.binding.mideaac.internal.dto.CloudDTO;
import org.openhab.binding.mideaac.internal.dto.CloudProviderDTO;
import org.openhab.binding.mideaac.internal.dto.CloudsDTO;
import org.openhab.binding.mideaac.internal.security.TokenKey;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MideaACHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Justan Oldman - Last Response added
 * @author Bob Eckhoff - Longer Polls and OH developer guidelines
 * @author Leo Siepel - Refactored class, improved seperation of concerns
 */
@NonNullByDefault
public class MideaACHandler extends BaseThingHandler implements DiscoveryHandler {

    private final Logger logger = LoggerFactory.getLogger(MideaACHandler.class);
    private final CloudsDTO clouds;
    private final boolean imperialUnits;
    private boolean isPollRunning = false;
    private final HttpClient httpClient;

    private MideaACConfiguration config = new MideaACConfiguration();
    private Map<String, String> properties = new HashMap<>();
    // Default parameters are the same as in the MideaACConfiguration class
    private ConnectionManager connectionManager = new ConnectionManager("", 6444, 4, "", "", "", "", "", "", 0, false);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private @Nullable ScheduledFuture<?> scheduledTask;

    private Callback callbackLambda = (response) -> {
        this.updateChannels(response);
    };

    /**
     * Initial creation of the Midea AC Handler
     * 
     * @param thing Thing
     * @param unitProvider OH core unit provider
     * @param httpClient http Client
     * @param clouds CloudsDTO
     */
    public MideaACHandler(Thing thing, UnitProvider unitProvider, HttpClient httpClient, CloudsDTO clouds) {
        super(thing);
        this.thing = thing;
        this.imperialUnits = unitProvider.getMeasurementSystem() instanceof ImperialUnits;
        this.httpClient = httpClient;
        this.clouds = clouds;
    }

    /**
     * Returns Cloud Provider
     * 
     * @return clouds
     */
    public CloudsDTO getClouds() {
        return clouds;
    }

    /**
     * This method handles the AC Channels that can be set (non-read only)
     * The command set is formed using the previous command to only
     * change the item requested and leave the others the same.
     * The command set which is then sent to the device via the connectionManager.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling channelUID {} with command {}", channelUID.getId(), command.toString());
        ConnectionManager connectionManager = this.connectionManager;

        if (command instanceof RefreshType) {
            try {
                connectionManager.getStatus(callbackLambda);
            } catch (MideaAuthenticationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (MideaConnectionException | MideaException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            return;
        }
        try {
            Response lastresponse = connectionManager.getLastResponse();
            if (channelUID.getId().equals(CHANNEL_POWER)) {
                connectionManager.sendCommand(CommandHelper.handlePower(command, lastresponse), callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_OPERATIONAL_MODE)) {
                connectionManager.sendCommand(CommandHelper.handleOperationalMode(command, lastresponse),
                        callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_TARGET_TEMPERATURE)) {
                connectionManager.sendCommand(CommandHelper.handleTargetTemperature(command, lastresponse),
                        callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
                connectionManager.sendCommand(CommandHelper.handleFanSpeed(command, lastresponse, config.version),
                        callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_ECO_MODE)) {
                connectionManager.sendCommand(CommandHelper.handleEcoMode(command, lastresponse), callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_TURBO_MODE)) {
                connectionManager.sendCommand(CommandHelper.handleTurboMode(command, lastresponse), callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_SWING_MODE)) {
                connectionManager.sendCommand(CommandHelper.handleSwingMode(command, lastresponse, config.version),
                        callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_SCREEN_DISPLAY)) {
                connectionManager.sendCommand(CommandHelper.handleScreenDisplay(command, lastresponse), callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_TEMPERATURE_UNIT)) {
                connectionManager.sendCommand(CommandHelper.handleTempUnit(command, lastresponse), callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_SLEEP_FUNCTION)) {
                connectionManager.sendCommand(CommandHelper.handleSleepFunction(command, lastresponse), callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_ON_TIMER)) {
                connectionManager.sendCommand(CommandHelper.handleOnTimer(command, lastresponse), callbackLambda);
            } else if (channelUID.getId().equals(CHANNEL_OFF_TIMER)) {
                connectionManager.sendCommand(CommandHelper.handleOffTimer(command, lastresponse), callbackLambda);
            }
        } catch (MideaConnectionException | MideaAuthenticationException e) {
            logger.warn("Unable to proces command: {}", e.getMessage());
        }
    }

    /**
     * Initialize is called on first pass or when a device parameter is changed
     * The basic check is if the information from Discovery (or the user update)
     * is valid. Because V2 devices do not require a cloud provider (or token/key)
     * The first check is for the IP, port and deviceID. The second part
     * checks the security configuration if required (V3 device).
     */
    @Override
    public void initialize() {
        if (isPollRunning) {
            stopScheduler();
        }
        config = getConfigAs(MideaACConfiguration.class);

        if (!config.isValid()) {
            logger.warn("Configuration invalid for {}", thing.getUID());
            if (config.isDiscoveryNeeded()) {
                logger.warn("Discovery needed, discovering....{}", thing.getUID());
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Configuration missing, discovery needed. Discovering...");
                MideaACDiscoveryService discoveryService = new MideaACDiscoveryService();

                try {
                    discoveryService.discoverThing(config.ipAddress, this);
                    return;
                } catch (Exception e) {
                    logger.error("Discovery failure for {}: {}", thing.getUID(), e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Discovery failure. Check configuration.");
                    return;
                }
            } else {
                logger.debug("MideaACHandler config of {} is invalid. Check configuration", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid MideaAC config. Check configuration.");
                return;
            }
        } else {
            logger.debug("Non-security Configuration valid for {}", thing.getUID());
        }

        if (config.version == 3 && !config.isV3ConfigValid()) {
            if (config.isTokenKeyObtainable()) {
                CloudProviderDTO cloudProvider = CloudProviderDTO.getCloudProvider(config.cloud);
                getTokenKeyCloud(cloudProvider);
                return;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configuration invalid and no account info to retrieve from cloud");
                return;
            }
        } else {
            logger.debug("Security Configuration (V3 Device) valid for {}", thing.getUID());
        }

        updateStatus(ThingStatus.UNKNOWN);

        connectionManager = new org.openhab.binding.mideaac.internal.connection.ConnectionManager(config.ipAddress,
                config.ipPort, config.timeout, config.key, config.token, config.cloud, config.email, config.password,
                config.deviceId, config.version, config.promptTone);

        startScheduler(2, config.pollingTime, TimeUnit.SECONDS);
    }

    /**
     * Starts the Scheduler for the Polling
     * 
     * @param initialDelay Seconds before first Poll
     * @param delay Seconds between Polls
     * @param unit Seconds
     */
    private void startScheduler(long initialDelay, long delay, TimeUnit unit) {
        if (scheduledTask == null) {
            isPollRunning = true;
            scheduledTask = scheduler.scheduleWithFixedDelay(this::pollJob, initialDelay, delay, unit);
            logger.debug("Scheduled task started");
        } else {
            logger.debug("Scheduler already running");
        }
    }

    private void pollJob() {
        ConnectionManager connectionManager = this.connectionManager;

        try {
            connectionManager.getStatus(callbackLambda);
            updateStatus(ThingStatus.ONLINE);
        } catch (MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MideaConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MideaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateChannel(String channelName, State state) {
        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            return;
        }
        Channel channel = thing.getChannel(channelName);
        if (channel != null && isLinked(channel.getUID())) {
            updateState(channel.getUID(), state);
        }
    }

    private void updateChannels(Response response) {
        updateChannel(CHANNEL_POWER, OnOffType.from(response.getPowerState()));
        updateChannel(CHANNEL_APPLIANCE_ERROR, OnOffType.from(response.getApplianceError()));
        updateChannel(CHANNEL_OPERATIONAL_MODE, new StringType(response.getOperationalMode().toString()));
        updateChannel(CHANNEL_FAN_SPEED, new StringType(response.getFanSpeed().toString()));
        updateChannel(CHANNEL_ON_TIMER, new StringType(response.getOnTimer().toChannel()));
        updateChannel(CHANNEL_OFF_TIMER, new StringType(response.getOffTimer().toChannel()));
        updateChannel(CHANNEL_SWING_MODE, new StringType(response.getSwingMode().toString()));
        updateChannel(CHANNEL_AUXILIARY_HEAT, OnOffType.from(response.getAuxHeat()));
        updateChannel(CHANNEL_ECO_MODE, OnOffType.from(response.getEcoMode()));
        updateChannel(CHANNEL_TEMPERATURE_UNIT, OnOffType.from(response.getFahrenheit()));
        updateChannel(CHANNEL_SLEEP_FUNCTION, OnOffType.from(response.getSleepFunction()));
        updateChannel(CHANNEL_TURBO_MODE, OnOffType.from(response.getTurboMode()));
        updateChannel(CHANNEL_SCREEN_DISPLAY, OnOffType.from(response.getDisplayOn()));
        updateChannel(CHANNEL_HUMIDITY, new DecimalType(response.getHumidity()));

        QuantityType<Temperature> targetTemperature = new QuantityType<Temperature>(response.getTargetTemperature(),
                SIUnits.CELSIUS);
        QuantityType<Temperature> alternateTemperature = new QuantityType<Temperature>(
                response.getAlternateTargetTemperature(), SIUnits.CELSIUS);
        QuantityType<Temperature> outdoorTemperature = new QuantityType<Temperature>(response.getOutdoorTemperature(),
                SIUnits.CELSIUS);
        QuantityType<Temperature> indoorTemperature = new QuantityType<Temperature>(response.getIndoorTemperature(),
                SIUnits.CELSIUS);

        if (imperialUnits) {
            targetTemperature = Objects.requireNonNull(targetTemperature.toUnit(ImperialUnits.FAHRENHEIT));
            alternateTemperature = Objects.requireNonNull(alternateTemperature.toUnit(ImperialUnits.FAHRENHEIT));
            indoorTemperature = Objects.requireNonNull(indoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
            outdoorTemperature = Objects.requireNonNull(outdoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
        }

        updateChannel(CHANNEL_TARGET_TEMPERATURE, targetTemperature);
        updateChannel(CHANNEL_ALTERNATE_TARGET_TEMPERATURE, alternateTemperature);
        updateChannel(CHANNEL_INDOOR_TEMPERATURE, indoorTemperature);
        updateChannel(CHANNEL_OUTDOOR_TEMPERATURE, outdoorTemperature);
    }

    @Override
    public void discovered(DiscoveryResult discoveryResult) {
        logger.debug("Discovered {}", thing.getUID());
        Map<String, Object> discoveryProps = discoveryResult.getProperties();
        Configuration configuration = editConfiguration();

        Object propertyDeviceId = Objects.requireNonNull(discoveryProps.get(CONFIG_DEVICEID));
        configuration.put(CONFIG_DEVICEID, propertyDeviceId.toString());

        Object propertyIpPort = Objects.requireNonNull(discoveryProps.get(CONFIG_IP_PORT));
        configuration.put(CONFIG_IP_PORT, propertyIpPort.toString());

        Object propertyVersion = Objects.requireNonNull(discoveryProps.get(CONFIG_VERSION));
        BigDecimal bigDecimalVersion = new BigDecimal((String) propertyVersion);
        logger.trace("Property Version in Handler {}", bigDecimalVersion.intValue());
        configuration.put(CONFIG_VERSION, bigDecimalVersion.intValue());

        updateConfiguration(configuration);

        properties = editProperties();

        Object propertySN = Objects.requireNonNull(discoveryProps.get(PROPERTY_SN));
        properties.put(PROPERTY_SN, propertySN.toString());

        Object propertySSID = Objects.requireNonNull(discoveryProps.get(PROPERTY_SSID));
        properties.put(PROPERTY_SSID, propertySSID.toString());

        Object propertyType = Objects.requireNonNull(discoveryProps.get(PROPERTY_TYPE));
        properties.put(PROPERTY_TYPE, propertyType.toString());

        updateProperties(properties);
        initialize();
    }

    /**
     * Gets the token and key from the Cloud
     * 
     * @param cloudProvider Cloud Provider account
     */
    public void getTokenKeyCloud(CloudProviderDTO cloudProvider) {
        logger.debug("Retrieving Token and/or Key from cloud");
        CloudDTO cloud = getClouds().get(config.email, config.password, cloudProvider);
        if (cloud != null) {
            cloud.setHttpClient(httpClient);
            if (cloud.login()) {
                TokenKey tk = cloud.getToken(config.deviceId);
                Configuration configuration = editConfiguration();

                configuration.put(CONFIG_TOKEN, tk.token());
                configuration.put(CONFIG_KEY, tk.key());
                updateConfiguration(configuration);

                logger.trace("Token: {}", tk.token());
                logger.trace("Key: {}", tk.key());
                logger.debug("Token and Key obtained from cloud, saving, back to initialize");
                initialize();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                        "Can't retrieve Token and Key from Cloud; email, password and/or cloud parameter error"));
                logger.warn("Can't retrieve Token and Key from Cloud; email, password and/or cloud parameter error");
            }
        }
    }

    private void stopScheduler() {
        ScheduledFuture<?> localScheduledTask = this.scheduledTask;

        if (localScheduledTask != null && !localScheduledTask.isCancelled()) {
            localScheduledTask.cancel(true);
            logger.debug("Scheduled task cancelled.");
            isPollRunning = false;
            scheduledTask = null;
        }
    }

    @Override
    public void dispose() {
        stopScheduler();
        connectionManager.dispose(true);
    }
}

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

import java.io.IOException;
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
import org.openhab.binding.mideaac.internal.cloud.Cloud;
import org.openhab.binding.mideaac.internal.cloud.CloudProvider;
import org.openhab.binding.mideaac.internal.connection.CommandHelper;
import org.openhab.binding.mideaac.internal.connection.ConnectionManager;
import org.openhab.binding.mideaac.internal.connection.exception.MideaAuthenticationException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaConnectionException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaException;
import org.openhab.binding.mideaac.internal.discovery.DiscoveryHandler;
import org.openhab.binding.mideaac.internal.discovery.MideaACDiscoveryService;
import org.openhab.binding.mideaac.internal.handler.capabilities.CapabilitiesResponse;
import org.openhab.binding.mideaac.internal.handler.capabilities.CapabilityParser;
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
 * @author Bob Eckhoff - Longer Polls, OH developer guidelines added other messages
 * @author Leo Siepel - Refactored class, improved separation of concerns
 */
@NonNullByDefault
public class MideaACHandler extends BaseThingHandler implements DiscoveryHandler, Callback {
    private final Logger logger = LoggerFactory.getLogger(MideaACHandler.class);
    private final boolean imperialUnits;
    private final HttpClient httpClient;

    private MideaACConfiguration config = new MideaACConfiguration();
    private Map<String, String> properties = new HashMap<>();
    // Default parameters are the same as in the MideaACConfiguration class
    private ConnectionManager connectionManager = new ConnectionManager("", 6444, 4, "", "", "", "", "", "", 0, false);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private @Nullable ScheduledFuture<?> scheduledTask;
    private @Nullable ScheduledFuture<?> scheduledKeyTokenUpdate;
    private @Nullable ScheduledFuture<?> scheduledEnergyUpdate;

    private final Callback callbackLambda = new Callback() {
        @Override
        public void updateChannels(Response response) {
            MideaACHandler.this.updateChannels(response);
        }

        @Override
        public void updateChannels(CapabilitiesResponse capabilitiesResponse) {
            MideaACHandler.this.updateChannels(capabilitiesResponse);
        }

        @Override
        public void updateChannels(EnergyResponse energyUpdate) {
            MideaACHandler.this.updateChannels(energyUpdate);
        }

        @Override
        public void updateHumidityFromEnergy(EnergyResponse energyUpdate) {
            MideaACHandler.this.updateHumidityFromEnergy(energyUpdate);
        }

        @Override
        public void updateChannels(HumidityResponse humidityResponse) {
            MideaACHandler.this.updateChannels(humidityResponse);
        }

        @Override
        public void updateChannels(TemperatureResponse temperatureResponse) {
            MideaACHandler.this.updateChannels(temperatureResponse);
        }
    };

    /**
     * Initial creation of the Midea AC Handler
     * 
     * @param thing Thing
     * @param unitProvider OH core unit provider
     * @param httpClient http Client
     */
    public MideaACHandler(Thing thing, UnitProvider unitProvider, HttpClient httpClient) {
        super(thing);
        this.thing = thing;
        this.imperialUnits = unitProvider.getMeasurementSystem() instanceof ImperialUnits;
        this.httpClient = httpClient;
    }

    /**
     * This method handles the AC Channels that can be set (non-read only)
     * The command set is formed using the previous command to only
     * change the item requested and leave the others the same.
     * The command set which is then sent to the device via the connectionManager.
     * For a Refresh both regular and energy polls are triggerred.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling channelUID {} with command {}", channelUID.getId(), command.toString());
        ConnectionManager connectionManager = this.connectionManager;

        if (command instanceof RefreshType) {
            try {
                connectionManager.getStatus(callbackLambda);
                // Read only Energy and Humidity channels not updated with routine poll
                CommandSet energyUpdate = new CommandSet();
                energyUpdate.energyPoll();
                connectionManager.sendCommand(energyUpdate, this);
                CommandSet humidityUpdate = new CommandSet();
                humidityUpdate.humidityPoll();
                connectionManager.sendCommand(humidityUpdate, this);
            } catch (MideaAuthenticationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (MideaConnectionException | MideaException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (IOException e) {
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
            } else if (channelUID.getId().equals(CHANNEL_MAXIMUM_HUMIDITY)) {
                connectionManager.sendCommand(CommandHelper.handleMaximumHumidity(command, lastresponse),
                        callbackLambda);
            }
        } catch (MideaConnectionException | MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MideaException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * To initialize an AC Thing, first check if discovery on your LAN has
     * been completed. In the discovery process the IP address, IP port, device ID
     * and version are established. Next for V.3 devices, if the token and key are not
     * known, the cloud needs to be contacted to get the token and key using either
     * the default NetHome Plus cloud or your own cloud account with your password and
     * email. LAN discovery DOES NOT include the token key retrieval needed to communicate
     * with the AC. V2 devices bypass the cloud connection step because they use a simplier
     * hard-coded encryption. Next the Connection Manager is established. Then a command
     * is formed and sent to retrieve the AC capabilities if they have not been
     * discovered. Capabilities are not returned in the initial LAN Discovery.
     * Lastly the routine polling, token key update and Energy polling frequency are set.
     * 
     */
    @Override
    public void initialize() {
        config = getConfigAs(MideaACConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        // Check for valid discovery configurations and discover again if not
        // Mostly needed for partial textual configurations. UI discovery should be valid
        if (!config.isValid()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Required configuration parameter(s) are missing or invalid.");

            if (config.isDiscoveryPossible()) {
                // Mark thing as UNKNOWN with message while attempting discovery.
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Retriving required parameters from device or the cloud.");
                MideaACDiscoveryService discoveryService = new MideaACDiscoveryService();

                // Run discovery asynchronously and end this initialization thread.
                // If successful, initialize() will be called again.
                scheduler.execute(() -> {
                    try {
                        discoveryService.discoverThing(config.ipAddress, this);
                    } catch (Exception e) {
                        // Required parameter discovery failed
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Could not retrieve required parameters from device or the cloud.");
                    }
                });
                return;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid configuration parameters provided. Retrieval from device or cloud not possible.");
                return;
            }
        } else {
            logger.debug("Discovery parameters are valid for {}", thing.getUID());
        }

        // Check for valid token and key and/or contact cloud account to get them
        if (config.version == 3 && !config.isV3ConfigValid()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Required configuration parameter(s) are missing or invalid.");

            if (config.isTokenKeyObtainable()) {
                // Mark thing as UNKNOWN with message while attempting token/key retrieval.
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Retriving required parameters from device or the cloud.");

                // Run token and key retrieval asynchronously and end this initialization thread.
                // If successful, initialize() will be called again.
                scheduler.execute(() -> {
                    try {
                        CloudProvider cloudProvider = CloudProvider.getCloudProvider(config.cloud);
                        getTokenKeyCloud(cloudProvider);
                    } catch (Exception e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Could not retrieve required parameters from device or the cloud.");
                    }
                });
                return;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid configuration parameters provided. Retrieval from device or cloud not possible.");
                return;
            }
        } else {
            logger.debug("Valid token and key for V.3 device {}", thing.getUID());
        }

        // Initialize connectionManager for communication with the AC
        connectionManager = new org.openhab.binding.mideaac.internal.connection.ConnectionManager(config.ipAddress,
                config.ipPort, config.timeout, config.key, config.token, config.cloud, config.email, config.password,
                config.deviceId, config.version, config.promptTone);

        // Form and send capabilities command if not already present in properties (async)
        if (!properties.containsKey("modeFanOnly")) {
            scheduler.execute(() -> {
                try {
                    CommandSet initializationCommand = new CommandSet();
                    initializationCommand.getCapabilities();
                    this.connectionManager.sendCommand(initializationCommand, this);

                    // Check if additional capabilities should be fetched
                    CapabilityParser parser = new CapabilityParser();
                    logger.debug("additional capabilities {}", parser.hasAdditionalCapabilities());
                    if (parser.hasAdditionalCapabilities()) {
                        // Attempt to fetch additional capabilities after a short delay
                        scheduler.schedule(() -> {
                            try {
                                CommandSet additionalCommand = new CommandSet();
                                additionalCommand.getAdditionalCapabilities();
                                this.connectionManager.sendCommand(additionalCommand, this);
                            } catch (Exception e) {
                                logger.debug("AC additional capabilities not returned {}", e.getMessage());
                            }
                        }, 2, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    logger.debug("AC capabilities not returned {}", e.getMessage());
                }
            });
        }

        // Establish routine polling per configuration or defaults
        if (scheduledTask == null) {
            scheduledTask = scheduler.scheduleWithFixedDelay(this::pollJob, 5, config.pollingTime, TimeUnit.SECONDS);
            logger.debug("Scheduled task started, Poll Time {} seconds", config.pollingTime);
        } else {
            logger.debug("Scheduler already running");
        }

        // Establish token key update frequency, if not disabled
        if (config.keyTokenUpdate != 0 && scheduledKeyTokenUpdate == null) {
            scheduledKeyTokenUpdate = scheduler.scheduleWithFixedDelay(
                    () -> getTokenKeyCloud(CloudProvider.getCloudProvider(config.cloud)), config.keyTokenUpdate,
                    config.keyTokenUpdate, TimeUnit.HOURS);
            logger.debug("Token Key Update Scheduler started, update interval {} hours", config.keyTokenUpdate);
        } else {
            logger.debug("Token Key Scheduler already running or disabled");
        }

        // Establish Energy polling, if not disabled.
        if (config.energyPoll != 0 && scheduledEnergyUpdate == null) {
            scheduledEnergyUpdate = scheduler.scheduleWithFixedDelay(this::energyUpdate, 1, config.energyPoll,
                    TimeUnit.MINUTES);
            logger.debug("Scheduled Energy Update started, Poll Time {} minutes", config.energyPoll);
        } else {
            logger.debug("Energy Scheduler already running or disabled");
        }
    }

    private void energyUpdate() {
        ConnectionManager connectionManager = this.connectionManager;

        try {
            CommandSet energyUpdate = new CommandSet();
            energyUpdate.energyPoll();
            connectionManager.sendCommand(energyUpdate, this);
        } catch (MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MideaConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MideaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        } catch (IOException e) {
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

    @Override
    public void updateChannels(Response response) {
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
        updateChannel(CHANNEL_FILTER_STATUS, OnOffType.from(response.getFilterStatus()));
        updateChannel(CHANNEL_MAXIMUM_HUMIDITY, new DecimalType(response.getMaximumHumidity()));

        QuantityType<Temperature> targetTemperature = new QuantityType<Temperature>(response.getTargetTemperature(),
                SIUnits.CELSIUS);
        QuantityType<Temperature> outdoorTemperature = new QuantityType<Temperature>(response.getOutdoorTemperature(),
                SIUnits.CELSIUS);
        QuantityType<Temperature> indoorTemperature = new QuantityType<Temperature>(response.getIndoorTemperature(),
                SIUnits.CELSIUS);

        if (imperialUnits) {
            targetTemperature = Objects.requireNonNull(targetTemperature.toUnit(ImperialUnits.FAHRENHEIT));
            indoorTemperature = Objects.requireNonNull(indoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
            outdoorTemperature = Objects.requireNonNull(outdoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
        }

        updateChannel(CHANNEL_TARGET_TEMPERATURE, targetTemperature);
        updateChannel(CHANNEL_INDOOR_TEMPERATURE, indoorTemperature);
        updateChannel(CHANNEL_OUTDOOR_TEMPERATURE, outdoorTemperature);
    }

    // Handle capabilities responses
    @Override
    public void updateChannels(CapabilitiesResponse capabilitiesResponse) {
        CapabilityParser parser = new CapabilityParser();
        parser.parse(capabilitiesResponse.getRawData());

        properties = editProperties();

        parser.getCapabilities().forEach((capabilityId, capabilityMap) -> {
            capabilityMap.forEach((key, value) -> {
                properties.put(key, String.valueOf(value));
            });
        });

        parser.getNumericCapabilities().forEach((capabilityId, temperatureMap) -> {
            temperatureMap.forEach((key, value) -> {
                properties.put(key, String.valueOf(value));
            });
        });

        // Default to false if "display_control" is missing from DISPLAY_CONTROL response
        if (!properties.containsKey("displayControl")) {
            properties.put("displayControl", "false - default");
        }
        // Default to true if "fan_only" is missing from MODE response
        if (!properties.containsKey("modeFanOnly")) {
            properties.put("modeFanOnly", "true - default");
        }
        // Defaults if FAN_SPEED_CONTROL is missing from response
        if (!properties.containsKey("fanLow")) {
            properties.put("fanLow", "true - default");
        }
        if (!properties.containsKey("fanMedium")) {
            properties.put("fanMedium", "true - default");
        }
        if (!properties.containsKey("fanHigh")) {
            properties.put("fanHigh", "true - default");
        }
        if (!properties.containsKey("fanAuto")) {
            properties.put("fanAuto", "true - default");
        }
        // Defaults if no TEMPERATURES were in response
        if (!properties.containsKey("coolMinTemperature")) {
            properties.put("minTargetTemperature", "17°C / 62°F default");
        }
        if (!properties.containsKey("heatMaxTemperature")) {
            properties.put("maxTargetTemperature", "30°C / 86°F default");
        }

        updateProperties(properties);

        logger.debug("Capabilities and temperature settings parsed and stored in properties: {}", properties);
    }

    // Handle Energy response updates - Config flags sets what decoding to use
    @Override
    public void updateChannels(EnergyResponse energyUpdate) {
        if (config.energyDecode) {
            updateChannel(CHANNEL_ENERGY_CONSUMPTION, new DecimalType(energyUpdate.getKilowattHoursBCD()));
            updateChannel(CHANNEL_CURRENT_DRAW, new DecimalType(energyUpdate.getAmperesBCD()));
            updateChannel(CHANNEL_POWER_CONSUMPTION, new DecimalType(energyUpdate.getWattsBCD()));
        } else {
            updateChannel(CHANNEL_ENERGY_CONSUMPTION, new DecimalType(energyUpdate.getKilowattHours()));
            updateChannel(CHANNEL_CURRENT_DRAW, new DecimalType(energyUpdate.getAmperes()));
            updateChannel(CHANNEL_POWER_CONSUMPTION, new DecimalType(energyUpdate.getWatts()));
        }
    }

    // Handle Humidity from energy poll command (0xC1)
    @Override
    public void updateHumidityFromEnergy(EnergyResponse energyUpdate) {
        updateChannel(CHANNEL_HUMIDITY, new DecimalType(energyUpdate.getHumidity()));
    }

    // Handle unsolicted Humidity response in room (0xA0)
    @Override
    public void updateChannels(HumidityResponse humidityResponse) {
        updateChannel(CHANNEL_HUMIDITY, new DecimalType(humidityResponse.getHumidity()));
    }

    // Handle unsolicted Temperature and Humidity response in room (0xA1)
    // Temperatures are also updated via the poll
    @Override
    public void updateChannels(TemperatureResponse temperatureResponse) {
        updateChannel(CHANNEL_HUMIDITY, new DecimalType(temperatureResponse.getHumidity()));

        QuantityType<Temperature> outdoorTemperature = new QuantityType<Temperature>(
                temperatureResponse.getOutdoorTemperature(), SIUnits.CELSIUS);
        QuantityType<Temperature> indoorTemperature = new QuantityType<Temperature>(
                temperatureResponse.getIndoorTemperature(), SIUnits.CELSIUS);

        if (imperialUnits) {
            indoorTemperature = Objects.requireNonNull(indoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
            outdoorTemperature = Objects.requireNonNull(outdoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
        }

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
    public void getTokenKeyCloud(CloudProvider cloudProvider) {
        if (scheduledTask != null) {
            stopScheduler();
        }
        logger.debug("Retrieving Token and/or Key from cloud");
        Cloud cloud = new Cloud(config.email, config.password, cloudProvider, httpClient);
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid configuration parameters provided. Retrieval from device or cloud not possible.");
        }
    }

    private void stopScheduler() {
        ScheduledFuture<?> localScheduledTask = this.scheduledTask;

        if (localScheduledTask != null && !localScheduledTask.isCancelled()) {
            localScheduledTask.cancel(true);
            logger.debug("Scheduled task cancelled.");
            scheduledTask = null;
        }
    }

    private void stopTokenKeyUpdate() {
        ScheduledFuture<?> localScheduledTask = this.scheduledKeyTokenUpdate;

        if (localScheduledTask != null && !localScheduledTask.isCancelled()) {
            localScheduledTask.cancel(true);
            logger.debug("Scheduled Key Token Update cancelled.");
            scheduledKeyTokenUpdate = null;
        }
    }

    private void stopEnergyUpdate() {
        ScheduledFuture<?> localScheduledTask = this.scheduledEnergyUpdate;

        if (localScheduledTask != null && !localScheduledTask.isCancelled()) {
            localScheduledTask.cancel(true);
            logger.debug("Scheduled Energy Update cancelled.");
            scheduledEnergyUpdate = null;
        }
    }

    @Override
    public void dispose() {
        stopScheduler();
        stopTokenKeyUpdate();
        stopEnergyUpdate();
        connectionManager.dispose(true);
    }
}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.midea.internal.handler;

import static org.openhab.binding.midea.internal.MideaBindingConstants.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.midea.internal.callbacks.ACCallback;
import org.openhab.binding.midea.internal.connection.exception.MideaAuthenticationException;
import org.openhab.binding.midea.internal.connection.exception.MideaConnectionException;
import org.openhab.binding.midea.internal.connection.exception.MideaException;
import org.openhab.binding.midea.internal.devices.ac.ACCommandHelper;
import org.openhab.binding.midea.internal.devices.ac.ACCommandSet;
import org.openhab.binding.midea.internal.devices.ac.EnergyResponse;
import org.openhab.binding.midea.internal.devices.ac.HumidityResponse;
import org.openhab.binding.midea.internal.devices.ac.Response;
import org.openhab.binding.midea.internal.devices.ac.TemperatureResponse;
import org.openhab.binding.midea.internal.devices.capabilities.CapabilitiesResponse;
import org.openhab.binding.midea.internal.devices.capabilities.CapabilityParser;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
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
 * @author Bob Eckhoff - Energy scheduling, humidity via energy poll added, separated AC
 *         and Dehumidifier handlers
 */
@NonNullByDefault
public class MideaACHandler extends AbstractMideaHandler implements ACCallback {
    private final Logger logger = LoggerFactory.getLogger(MideaACHandler.class);
    private final boolean imperialUnits;
    private @Nullable ScheduledFuture<?> scheduledEnergyUpdate;

    /**
     * Initial creation of the Midea AC Handler
     * 
     * @param thing Thing
     * @param unitProvider OH core unit provider
     * @param httpClient http Client
     */
    public MideaACHandler(Thing thing, UnitProvider unitProvider, HttpClient httpClient) {
        super(thing, unitProvider, httpClient);
        this.thing = thing;
        this.imperialUnits = unitProvider.getMeasurementSystem() instanceof ImperialUnits;
    }

    /**
     * Send capabilities command(s) if we don't yet have them stored in properties.
     */
    @Override
    public void initialize() {
        super.initialize(); // common plumbing
        startEnergyScheduler(); // AC-specific
    }

    private void startEnergyScheduler() {
        // Energy polling
        if (config.energyPoll != 0 && scheduledEnergyUpdate == null) {
            scheduledEnergyUpdate = scheduler.scheduleWithFixedDelay(this::energyUpdate, 1, config.energyPoll,
                    TimeUnit.MINUTES);
            logger.debug("Scheduled Energy Update started, Poll Time {} minutes", config.energyPoll);
        } else {
            logger.debug("Energy Scheduler already running or disabled");
        }
    }

    private void energyUpdate() {
        try {
            ACCommandSet energyUpdate = new ACCommandSet();
            energyUpdate.energyPoll();
            connectionManager.sendCommand(energyUpdate, this);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected void refreshDeviceState() {
        try {
            connectionManager.getStatus(this);
            // If we reach here, the device is online.
            updateStatus(ThingStatus.ONLINE);
        } catch (MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MideaConnectionException | MideaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected void refreshDeviceStateAll() {
        try {
            connectionManager.getStatus(this);
            humidityUpdate();
            energyUpdate();
        } catch (MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MideaConnectionException | MideaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void humidityUpdate() {
        try {
            ACCommandSet humidityUpdate = new ACCommandSet();
            humidityUpdate.humidityPoll();
            connectionManager.sendCommand(humidityUpdate, this);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    /**
     * This method handles the AC Channels that can be set (non-read only)
     * The command set is formed using the previous command to only
     * change the item requested and leave the others the same.
     * The command set which is then sent to the device via the connectionManager.
     * For a Refresh both regular and energy and humidity polls are triggerred.
     */
    @Override
    protected void handleDeviceCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling channelUID {} with command {}", channelUID.getId(), command.toString());

        try {
            Response lastresponse = connectionManager.getLastResponse();
            if (channelUID.getId().equals(CHANNEL_POWER)) {
                connectionManager.sendCommand(ACCommandHelper.handlePower(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_OPERATIONAL_MODE)) {
                connectionManager.sendCommand(ACCommandHelper.handleOperationalMode(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_TARGET_TEMPERATURE)) {
                connectionManager.sendCommand(ACCommandHelper.handleTargetTemperature(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
                connectionManager.sendCommand(ACCommandHelper.handleFanSpeed(command, lastresponse, config.version),
                        this);
            } else if (channelUID.getId().equals(CHANNEL_ECO_MODE)) {
                connectionManager.sendCommand(ACCommandHelper.handleEcoMode(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_TURBO_MODE)) {
                connectionManager.sendCommand(ACCommandHelper.handleTurboMode(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_SWING_MODE)) {
                connectionManager.sendCommand(ACCommandHelper.handleSwingMode(command, lastresponse, config.version),
                        this);
            } else if (channelUID.getId().equals(CHANNEL_SCREEN_DISPLAY)) {
                connectionManager.sendCommand(ACCommandHelper.handleScreenDisplay(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_TEMPERATURE_UNIT)) {
                connectionManager.sendCommand(ACCommandHelper.handleTempUnit(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_SLEEP_FUNCTION)) {
                connectionManager.sendCommand(ACCommandHelper.handleSleepFunction(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_ON_TIMER)) {
                connectionManager.sendCommand(ACCommandHelper.handleOnTimer(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_OFF_TIMER)) {
                connectionManager.sendCommand(ACCommandHelper.handleOffTimer(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_MAXIMUM_HUMIDITY)) {
                connectionManager.sendCommand(ACCommandHelper.handleMaximumHumidity(command, lastresponse), this);
            }
        } catch (MideaConnectionException | MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MideaException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Send capabilities command(s) if we don't yet have them stored in properties.
     */
    @Override
    protected void requestCapabilitiesIfMissing() {
        if (properties.containsKey("modeFanOnly")) {
            return;
        }

        scheduler.execute(() -> {
            try {
                ACCommandSet initializationCommand = new ACCommandSet();
                initializationCommand.getCapabilities();
                this.connectionManager.sendCommand(initializationCommand, this);

                // Check if additional capabilities are available and fetch them if so
                CapabilityParser parser = new CapabilityParser();
                logger.debug("additional capabilities {}", parser.hasAdditionalCapabilities());
                if (parser.hasAdditionalCapabilities()) {
                    scheduler.schedule(() -> {
                        try {
                            ACCommandSet additionalCommand = new ACCommandSet();
                            additionalCommand.getAdditionalCapabilities();
                            this.connectionManager.sendCommand(additionalCommand, this);
                        } catch (Exception ex) {
                            logger.debug("AC additional capabilities not returned {}", ex.getMessage());
                        }
                    }, 2, TimeUnit.SECONDS);
                }
            } catch (Exception ex) {
                // Will not affect AC device readiness, just log the issue
                logger.debug("AC capabilities not returned {}", ex.getMessage());
            }
        });
    }

    @Override
    public void updateChannels(Response response) {
        updateChannel(CHANNEL_POWER, OnOffType.from(response.getPowerState()));
        updateChannel(CHANNEL_FAN_SPEED, new StringType(response.getFanSpeed().toString()));
        updateChannel(CHANNEL_ON_TIMER, new StringType(response.getOnTimer().toChannel()));
        updateChannel(CHANNEL_OFF_TIMER, new StringType(response.getOffTimer().toChannel()));
        updateChannel(CHANNEL_APPLIANCE_ERROR, OnOffType.from(response.getApplianceError()));
        updateChannel(CHANNEL_OPERATIONAL_MODE, new StringType(response.getOperationalMode().toString()));
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
        super.dispose(); // stop common schedulers + connection manager
        stopEnergyUpdate(); // AC-specific
    }
}

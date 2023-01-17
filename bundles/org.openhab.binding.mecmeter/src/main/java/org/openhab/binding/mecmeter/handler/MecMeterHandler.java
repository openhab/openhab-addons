/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mecmeter.handler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.mecmeter.MecMeterBindingConstants;
import org.openhab.binding.mecmeter.MecMeterDeviceConfiguration;
import org.openhab.binding.mecmeter.internal.dto.MecMeterResponse;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MecMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Pazour - Initial contribution
 * @author Klaus Berger - Initial contribution
 * @author Kai Kreuzer - Refactoring for openHAB 3
 */
@NonNullByDefault
public class MecMeterHandler extends BaseThingHandler {

    private static final int API_TIMEOUT = 5000; // set on 5000ms - not specified in datasheet

    private static final String USERNAME = "admin";

    private final Logger logger = LoggerFactory.getLogger(MecMeterHandler.class);

    private Gson gson = new Gson();

    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> pollFuture;

    private @Nullable MecMeterResponse powerMeterResponse;

    public MecMeterHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        } else {
            logger.debug("Received unsupported command {}.", command);
        }
    }

    /**
     * function which is called to refresh the data
     */
    public void refresh() {
        updateData();
        updateChannels();
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.debug("removing thing..");
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
    }

    @Override
    public void initialize() {
        MecMeterDeviceConfiguration config = getConfig().as(MecMeterDeviceConfiguration.class);
        String configCheck = config.isValid();

        if (configCheck != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configCheck);
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);

        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        pollFuture = scheduler.scheduleWithFixedDelay(() -> {
            refresh();
        }, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    /**
     * Get new data
     * Function to save Response of the powermeter
     */
    private void updateData() {
        powerMeterResponse = getRealtimeData();
    }

    /**
     * Get new realtime data over the network
     *
     * @return MecMeterResponse class where json values "are saved"
     */
    private @Nullable MecMeterResponse getRealtimeData() {
        MecMeterResponse result = null;
        boolean resultOk = false;
        String errorMsg = null;

        MecMeterDeviceConfiguration config = getConfig().as(MecMeterDeviceConfiguration.class);

        try {
            String basicAuthentication = "Basic " + Base64.getEncoder()
                    .encodeToString(new String(USERNAME + ":" + config.password).getBytes(StandardCharsets.ISO_8859_1));

            String location = MecMeterBindingConstants.POWERMETER_DATA_URL.replace("%IP%", config.ip.strip());

            ContentResponse response = httpClient.newRequest(location).method(HttpMethod.GET)
                    .header(HttpHeader.AUTHORIZATION, basicAuthentication).timeout(API_TIMEOUT, TimeUnit.MILLISECONDS)
                    .send();
            if (response.getStatus() != 200) {
                errorMsg = "Reading meter did not succeed: " + response.getReason();
                logger.error("Request to meter failed: HTTP {}: {}", response.getStatus(), response.getReason());
            } else {
                result = gson.fromJson(response.getContentAsString(), MecMeterResponse.class);
                if (result == null) {
                    errorMsg = "no data returned";
                    logger.error("no data returned from meter at {}", location);
                } else {
                    resultOk = true;
                }
            }
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.error("Error running power meter request: {}", e.getMessage());
        } catch (IllegalStateException e) {
            errorMsg = "Connection failed";
            logger.error("Error running powermeter request: {}", e.getMessage());
        } catch (InterruptedException e) {
            logger.debug("Http request has been interrupted: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.debug("Http request ran into a timeout: {}", e.getMessage());
            errorMsg = "Connection to power meter timed out.";
        } catch (ExecutionException e) {
            logger.debug("Http request did not succeed: {}", e.getMessage());
            errorMsg = "Connection problem: " + e.getMessage();
        }

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        }

        return resultOk ? result : null;
    }

    /**
     * Update all Channels
     */
    protected void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId());
        }
    }

    /**
     * Update the channel state
     *
     * @param channelId the id identifying the channel to be updated
     */
    protected void updateChannel(String channelId) {
        if (!isLinked(channelId)) {
            return;
        }
        State state = getState(channelId);
        if (state != null) {
            updateState(channelId, state);
        }
    }

    /**
     * Get the state of a given channel
     *
     * @param channelId the id identifying the channel to be updated
     * @return state of the channel
     */
    protected @Nullable State getState(String channelId) {
        MecMeterResponse response = powerMeterResponse;
        if (response == null) {
            return null;
        } else {
            switch (channelId) {
                /* General */
                case MecMeterBindingConstants.FREQUENCY:
                    return new QuantityType<>(response.getFrequency(), Units.HERTZ);
                case MecMeterBindingConstants.TEMPERATURE:
                    return new QuantityType<>(response.getTemperature(), SIUnits.CELSIUS);
                case MecMeterBindingConstants.OPERATIONAL_TIME:
                    return new QuantityType<>(response.getOperationalTime() / 1000, Units.SECOND);

                /* Voltage */
                case MecMeterBindingConstants.VOLTAGE_PHASE_1:
                    return new QuantityType<>(response.getVoltagePhase1(), Units.VOLT);
                case MecMeterBindingConstants.VOLTAGE_PHASE_2:
                    return new QuantityType<>(response.getVoltagePhase2(), Units.VOLT);
                case MecMeterBindingConstants.VOLTAGE_PHASE_3:
                    return new QuantityType<>(response.getVoltagePhase3(), Units.VOLT);
                case MecMeterBindingConstants.VOLTAGE_PHASE_3_TO_PHASE_2:
                    return new QuantityType<>(response.getVoltagePhase3ToPhase2(), Units.VOLT);
                case MecMeterBindingConstants.VOLTAGE_PHASE_2_TO_PHASE_1:
                    return new QuantityType<>(response.getVoltagePhase2ToPhase1(), Units.VOLT);
                case MecMeterBindingConstants.VOLTAGE_PHASE_1_TO_PHASE_3:
                    return new QuantityType<>(response.getVoltagePhase1ToPhase3(), Units.VOLT);
                case MecMeterBindingConstants.AVERAGE_VOLTAGE_PHASE_2_PHASE:
                    return new QuantityType<>(response.getAverageVoltagePhaseToPhase(), Units.VOLT);
                case MecMeterBindingConstants.AVERAGE_VOLTAGE_NEUTRAL_2_PHASE:
                    return new QuantityType<>(response.getAverageVoltageNeutralToPhase(), Units.VOLT);

                /* Current */
                case MecMeterBindingConstants.CURRENT_PHASE_1:
                    return new QuantityType<>(response.getCurrentPhase1(), Units.AMPERE);
                case MecMeterBindingConstants.CURRENT_PHASE_2:
                    return new QuantityType<>(response.getCurrentPhase2(), Units.AMPERE);
                case MecMeterBindingConstants.CURRENT_PHASE_3:
                    return new QuantityType<>(response.getCurrentPhase3(), Units.AMPERE);
                case MecMeterBindingConstants.CURRENT_SUM:
                    return new QuantityType<>(response.getCurrentSum(), Units.AMPERE);

                /* Angles */
                case MecMeterBindingConstants.PHASE_ANGLE_TO_CURRENT_PHASE_1:
                    return new QuantityType<>(response.getPhaseAngleCurrentToVoltagePhase1(), Units.DEGREE_ANGLE);
                case MecMeterBindingConstants.PHASE_ANGLE_TO_CURRENT_PHASE_2:
                    return new QuantityType<>(response.getPhaseAngleCurrentToVoltagePhase2(), Units.DEGREE_ANGLE);
                case MecMeterBindingConstants.PHASE_ANGLE_TO_CURRENT_PHASE_3:
                    return new QuantityType<>(response.getPhaseAngleCurrentToVoltagePhase3(), Units.DEGREE_ANGLE);
                case MecMeterBindingConstants.PHASE_ANGLE_PHASE_1_3:
                    return new QuantityType<>(response.getPhaseAnglePhase1To3(), Units.DEGREE_ANGLE);
                case MecMeterBindingConstants.PHASE_ANGLE_PHASE_2_3:
                    return new QuantityType<>(response.getPhaseAnglePhase2To3(), Units.DEGREE_ANGLE);

                /* Power */
                case MecMeterBindingConstants.ACTIVE_POWER_PHASE_1:
                    return new QuantityType<>(response.getActivePowerPhase1(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_POWER_PHASE_2:
                    return new QuantityType<>(response.getActivePowerPhase2(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_POWER_PHASE_3:
                    return new QuantityType<>(response.getActivePowerPhase3(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_POWER_SUM:
                    return new QuantityType<>(response.getActivePowerSum(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_FUND_POWER_PHASE_1:
                    return new QuantityType<>(response.getActiveFundamentalPowerPhase1(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_FUND_POWER_PHASE_2:
                    return new QuantityType<>(response.getActiveFundamentalPowerPhase2(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_FUND_POWER_PHASE_3:
                    return new QuantityType<>(response.getActiveFundamentalPowerPhase3(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_FUND_POWER_ALL:
                    return new QuantityType<>(response.getActiveFundamentalPowerSum(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_HARM_POWER_PHASE_1:
                    return new QuantityType<>(response.getActiveHarmonicPowerPhase1(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_HARM_POWER_PHASE_2:
                    return new QuantityType<>(response.getActiveHarmonicPowerPhase2(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_HARM_POWER_PHASE_3:
                    return new QuantityType<>(response.getActiveHarmonicPowerPhase3(), Units.WATT);
                case MecMeterBindingConstants.ACTIVE_HARM_POWER_ALL:
                    return new QuantityType<>(response.getActiveHarmonicPowerSum(), Units.WATT);
                case MecMeterBindingConstants.REACTIVE_POWER_PHASE_1:
                    return new QuantityType<>(response.getReactivePowerPhase1(), Units.VAR);
                case MecMeterBindingConstants.REACTIVE_POWER_PHASE_2:
                    return new QuantityType<>(response.getReactivePowerPhase2(), Units.VAR);
                case MecMeterBindingConstants.REACTIVE_POWER_PHASE_3:
                    return new QuantityType<>(response.getReactivePowerPhase3(), Units.VAR);
                case MecMeterBindingConstants.REACTIVE_POWER_ALL:
                    return new QuantityType<>(response.getReactivePowerSum(), Units.VAR);
                case MecMeterBindingConstants.APP_POWER_PHASE_1:
                    return new QuantityType<>(response.getApparentPowerPhase1(), Units.VOLT_AMPERE);
                case MecMeterBindingConstants.APP_POWER_PHASE_2:
                    return new QuantityType<>(response.getApparentPowerPhase2(), Units.VOLT_AMPERE);
                case MecMeterBindingConstants.APP_POWER_PHASE_3:
                    return new QuantityType<>(response.getApparentPowerPhase3(), Units.VOLT_AMPERE);
                case MecMeterBindingConstants.APP_POWER_ALL:
                    return new QuantityType<>(response.getApparentPowerSum(), Units.VOLT_AMPERE);

                /* Forward Energy */
                case MecMeterBindingConstants.FORWARD_ACTIVE_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getForwardActiveEnergyPhase1(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getForwardActiveEnergyPhase2(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getForwardActiveEnergyPhase3(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_ENERGY_ALL:
                    return new QuantityType<>(response.getForwardActiveEnergySum(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_FUND_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getForwardActiveFundamentalEnergyPhase1(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_FUND_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getForwardActiveFundamentalEnergyPhase2(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_FUND_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getForwardActiveFundamentalEnergyPhase3(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_FUND_ENERGY_ALL:
                    return new QuantityType<>(response.getForwardActiveFundamentalEnergySum(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_HARM_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getForwardActiveHarmonicEnergyPhase1(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_HARM_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getForwardActiveHarmonicEnergyPhase2(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_HARM_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getForwardActiveHarmonicEnergyPhase3(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_ACTIVE_HARM_ENERGY_ALL:
                    return new QuantityType<>(response.getForwardActiveHarmonicEnergySum(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.FORWARD_REACTIVE_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getForwardReactiveEnergyPhase1(), Units.VAR_HOUR);
                case MecMeterBindingConstants.FORWARD_REACTIVE_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getForwardReactiveEnergyPhase2(), Units.VAR_HOUR);
                case MecMeterBindingConstants.FORWARD_REACTIVE_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getForwardReactiveEnergyPhase3(), Units.VAR_HOUR);
                case MecMeterBindingConstants.FORWARD_REACTIVE_ENERGY_ALL:
                    return new QuantityType<>(response.getForwardReactiveEnergySum(), Units.VAR_HOUR);

                /* Reverse Energy */
                case MecMeterBindingConstants.REVERSE_ACTIVE_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getReverseActiveEnergyPhase1(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getReverseActiveEnergyPhase2(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getReverseActiveEnergyPhase3(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_ENERGY_ALL:
                    return new QuantityType<>(response.getReverseActiveEnergySum(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_FUND_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getReverseActiveFundamentalEnergyPhase1(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_FUND_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getReverseActiveFundamentalEnergyPhase2(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_FUND_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getReverseActiveFundamentalEnergyPhase3(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_FUND_ENERGY_ALL:
                    return new QuantityType<>(response.getReverseActiveFundamentalEnergySum(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_HARM_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getReverseActiveHarmonicEnergyPhase1(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_HARM_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getReverseActiveHarmonicEnergyPhase2(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_HARM_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getReverseActiveHarmonicEnergyPhase3(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_ACTIVE_HARM_ENERGY_ALL:
                    return new QuantityType<>(response.getReverseActiveHarmonicEnergySum(), Units.KILOWATT_HOUR);
                case MecMeterBindingConstants.REVERSE_REACTIVE_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getReverseReactiveEnergyPhase1(), Units.VAR_HOUR);
                case MecMeterBindingConstants.REVERSE_REACTIVE_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getReverseReactiveEnergyPhase2(), Units.VAR_HOUR);
                case MecMeterBindingConstants.REVERSE_REACTIVE_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getReverseReactiveEnergyPhase3(), Units.VAR_HOUR);
                case MecMeterBindingConstants.REVERSE_REACTIVE_ENERGY_ALL:
                    return new QuantityType<>(response.getReverseReactiveEnergySum(), Units.VAR_HOUR);

                /* Apparent Energy */
                case MecMeterBindingConstants.APP_ENERGY_PHASE_1:
                    return new QuantityType<>(response.getApparentEnergyConsumptionPhase1(), Units.VOLT_AMPERE_HOUR);
                case MecMeterBindingConstants.APP_ENERGY_PHASE_2:
                    return new QuantityType<>(response.getApparentEnergyConsumptionPhase2(), Units.VOLT_AMPERE_HOUR);
                case MecMeterBindingConstants.APP_ENERGY_PHASE_3:
                    return new QuantityType<>(response.getApparentEnergyConsumptionPhase3(), Units.VOLT_AMPERE_HOUR);
                case MecMeterBindingConstants.APP_ENERGY_ALL:
                    return new QuantityType<>(response.getApparentEnergyConsumptionSum(), Units.VOLT_AMPERE_HOUR);

                /* Power Factor */
                case MecMeterBindingConstants.POWER_FACTOR_PHASE_1:
                    return new QuantityType<>(response.getPowerFactorPhase1(), Units.ONE);
                case MecMeterBindingConstants.POWER_FACTOR_PHASE_2:
                    return new QuantityType<>(response.getPowerFactorPhase2(), Units.ONE);
                case MecMeterBindingConstants.POWER_FACTOR_PHASE_3:
                    return new QuantityType<>(response.getPowerFactorPhase3(), Units.ONE);
                case MecMeterBindingConstants.POWER_FACTOR_ALL:
                    return new QuantityType<>(response.getPowerFactorSum(), Units.ONE);
            }
        }
        return null;
    }
}

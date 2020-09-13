/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal;

import static org.openhab.binding.hydrawise.internal.HydrawiseBindingConstants.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCloudApiClient;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.model.Controller;
import org.openhab.binding.hydrawise.internal.api.model.CustomerDetailsResponse;
import org.openhab.binding.hydrawise.internal.api.model.Forecast;
import org.openhab.binding.hydrawise.internal.api.model.Relay;
import org.openhab.binding.hydrawise.internal.api.model.StatusScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HydrawiseCloudHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HydrawiseCloudHandler extends HydrawiseHandler {
    /**
     * 74.2 F
     */
    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile("^(\\d{1,3}.?\\d?)\\s([C,F])");
    /**
     * 9 mph
     */
    private static final Pattern WIND_SPEED_PATTERN = Pattern.compile("^(\\d{1,3})\\s([a-z]{3})");
    private final Logger logger = LoggerFactory.getLogger(HydrawiseCloudHandler.class);
    private HydrawiseCloudApiClient client;
    private int controllerId;

    public HydrawiseCloudHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.client = new HydrawiseCloudApiClient(httpClient);
    }

    @Override
    protected void configure()
            throws NotConfiguredException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        HydrawiseCloudConfiguration configuration = getConfig().as(HydrawiseCloudConfiguration.class);

        this.refresh = Math.max(configuration.refresh, MIN_REFRESH_SECONDS);

        client.setApiKey(configuration.apiKey);

        CustomerDetailsResponse customerDetails = client.getCustomerDetails();

        List<Controller> controllers = customerDetails.controllers;
        if (controllers.isEmpty()) {
            throw new NotConfiguredException("No controllers found on account");
        }

        Controller controller = null;
        // try and use ID from user configuration
        if (configuration.controllerId != null) {
            controller = getController(configuration.controllerId.intValue(), controllers);
            if (controller == null) {
                throw new NotConfiguredException("No controller found for id " + configuration.controllerId);
            }
        } else {
            // try and use ID from saved property
            String controllerId = getThing().getProperties().get(PROPERTY_CONTROLLER_ID);
            if (StringUtils.isNotBlank(controllerId)) {
                try {
                    controller = getController(Integer.parseInt(controllerId), controllers);

                } catch (NumberFormatException e) {
                    logger.debug("Can not parse property vaue {}", controllerId);
                }
            }
            // use current controller ID
            if (controller == null) {
                controller = getController(customerDetails.controllerId, controllers);
            }
        }

        if (controller == null) {
            throw new NotConfiguredException("No controller found");
        }

        controllerId = controller.controllerId.intValue();
        updateControllerProperties(controller);
        logger.debug("Controller id {}", controllerId);
    }

    /**
     * Poll the controller for updates.
     */
    @Override
    protected void pollController() throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        List<Controller> controllers = client.getCustomerDetails().controllers;
        Controller controller = getController(controllerId, controllers);
        if (controller != null && !controller.online) {
            throw new HydrawiseConnectionException("Controller is offline");
        }
        StatusScheduleResponse status = client.getStatusSchedule(controllerId);
        updateSensors(status);
        updateForecast(status);
        updateZones(status);
    }

    @Override
    protected void sendRunCommand(int seconds, @Nullable Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        if (relay != null) {
            client.runRelay(seconds, relay.relayId);
        }
    }

    @Override
    protected void sendRunCommand(@Nullable Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        if (relay != null) {
            client.runRelay(relay.relayId);
        }
    }

    @Override
    protected void sendStopCommand(@Nullable Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        if (relay != null) {
            client.stopRelay(relay.relayId);
        }
    }

    @Override
    protected void sendRunAllCommand()
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.runAllRelays(controllerId);
    }

    @Override
    protected void sendRunAllCommand(int seconds)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.runAllRelays(seconds, controllerId);
    }

    @Override
    protected void sendStopAllCommand()
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.stopAllRelays(controllerId);
    }

    private void updateSensors(StatusScheduleResponse status) {
        status.sensors.forEach(sensor -> {
            String group = "sensor" + sensor.input;
            updateGroupState(group, CHANNEL_SENSOR_MODE, new DecimalType(sensor.type));
            updateGroupState(group, CHANNEL_SENSOR_NAME, new StringType(sensor.name));
            updateGroupState(group, CHANNEL_SENSOR_OFFTIMER, new DecimalType(sensor.offtimer));
            updateGroupState(group, CHANNEL_SENSOR_TIMER, new DecimalType(sensor.timer));
            // Some fields are missing depending on sensor type.
            if (sensor.offlevel != null) {
                updateGroupState(group, CHANNEL_SENSOR_OFFLEVEL, new DecimalType(sensor.offlevel));
            }
            if (sensor.active != null) {
                updateGroupState(group, CHANNEL_SENSOR_ACTIVE, sensor.active > 0 ? OnOffType.ON : OnOffType.OFF);
            }
        });
    }

    private void updateForecast(StatusScheduleResponse status) {
        int i = 1;
        for (Forecast forecast : status.forecast) {
            String group = "forecast" + (i++);
            updateGroupState(group, CHANNEL_FORECAST_CONDITIONS, new StringType(forecast.conditions));
            updateGroupState(group, CHANNEL_FORECAST_DAY, new StringType(forecast.day));
            updateGroupState(group, CHANNEL_FORECAST_HUMIDITY, new DecimalType(forecast.humidity));
            updateTemperature(forecast.tempHi, group, CHANNEL_FORECAST_TEMPERATURE_HIGH);
            updateTemperature(forecast.tempLo, group, CHANNEL_FORECAST_TEMPERATURE_LOW);
            updateWindspeed(forecast.wind, group, CHANNEL_FORECAST_WIND);
        }
    }

    private void updateTemperature(String tempString, String group, String channel) {
        Matcher matcher = TEMPERATURE_PATTERN.matcher(tempString);
        if (matcher.matches()) {
            try {
                updateGroupState(group, channel, new QuantityType<>(Double.valueOf(matcher.group(1)),
                        "C".equals(matcher.group(2)) ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse temperature string {} ", tempString);
            }
        }
    }

    private void updateWindspeed(String windString, String group, String channel) {
        Matcher matcher = WIND_SPEED_PATTERN.matcher(windString);
        if (matcher.matches()) {
            try {
                updateGroupState(group, channel, new QuantityType<>(Integer.parseInt(matcher.group(1)),
                        "kph".equals(matcher.group(2)) ? SIUnits.KILOMETRE_PER_HOUR : ImperialUnits.MILES_PER_HOUR));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse wind string {} ", windString);
            }
        }
    }

    private void updateControllerProperties(Controller controller) {
        getThing().setProperty(PROPERTY_CONTROLLER_ID, String.valueOf(controller.controllerId));
        getThing().setProperty(PROPERTY_NAME, controller.name);
        getThing().setProperty(PROPERTY_DESCRIPTION, controller.description);
        getThing().setProperty(PROPERTY_LOCATION, controller.latitude + "," + controller.longitude);
        getThing().setProperty(PROPERTY_ADDRESS, controller.address);
    }

    private @Nullable Controller getController(int controllerId, List<Controller> controllers) {
        Optional<@NonNull Controller> optionalController = controllers.stream()
                .filter(c -> controllerId == c.controllerId.intValue()).findAny();
        return optionalController.isPresent() ? optionalController.get() : null;
    }
}

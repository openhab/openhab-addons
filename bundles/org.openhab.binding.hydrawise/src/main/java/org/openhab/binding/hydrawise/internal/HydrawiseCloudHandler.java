/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.apache.commons.lang.StringUtils;
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
    private final Logger logger = LoggerFactory.getLogger(HydrawiseCloudHandler.class);
    private HydrawiseCloudApiClient client;
    int controllerId;
    /**
     * 74.2 F
     */
    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile("^(\\d{1,3}.?\\d?)\\s([C,F])");
    /**
     * 9 mph
     */
    private static final Pattern WIND_SPEED_PATTERN = Pattern.compile("^(\\d{1,3})\\s([a-z]{3})");

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

        List<Controller> controllers = customerDetails.getControllers();
        if (controllers.size() == 0) {
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
            if (StringUtils.isNotBlank(getThing().getProperties().get(PROPERTY_CONTROLLER_ID))) {
                try {
                    controller = getController(Integer.parseInt(getThing().getProperties().get(PROPERTY_CONTROLLER_ID)),
                            controllers);

                } catch (NumberFormatException e) {
                    logger.debug("Can not parse property vaue {}",
                            getThing().getProperties().get(PROPERTY_CONTROLLER_ID));
                }
            }
            // use current controller ID
            if (controller == null) {
                controller = getController(customerDetails.getControllerId(), controllers);
            }
        }

        if (controller == null) {
            throw new NotConfiguredException("No controller found");
        }

        controllerId = controller.getControllerId().intValue();
        updateControllerProperties(controller);
        logger.debug("Controller id {}", controllerId);
    }

    /**
     * Poll the controller for updates.
     */
    @Override
    protected void pollController() throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        List<Controller> controllers = client.getCustomerDetails().getControllers();
        Controller controller = getController(controllerId, controllers);
        if (controller != null && !controller.getOnline()) {
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
            client.runRelay(seconds, relay.getRelayId());
        }
    }

    @Override
    protected void sendRunCommand(@Nullable Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        if (relay != null) {
            client.runRelay(relay.getRelayId());
        }
    }

    @Override
    protected void sendStopCommand(@Nullable Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        if (relay != null) {
            client.stopRelay(relay.getRelayId());
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
        status.getSensors().forEach(sensor -> {
            String group = "sensor" + sensor.getInput();
            updateGroupState(group, CHANNEL_SENSOR_ACTIVE, sensor.getActive() > 0 ? OnOffType.ON : OnOffType.OFF);
            updateGroupState(group, CHANNEL_SENSOR_MODE, new DecimalType(sensor.getType()));
            updateGroupState(group, CHANNEL_SENSOR_NAME, new StringType(sensor.getName()));
            updateGroupState(group, CHANNEL_SENSOR_OFFLEVEL, new DecimalType(sensor.getOfflevel()));
            updateGroupState(group, CHANNEL_SENSOR_OFFTIMER, new DecimalType(sensor.getOfftimer()));
            updateGroupState(group, CHANNEL_SENSOR_TIMER, new DecimalType(sensor.getTimer()));
        });
    }

    private void updateForecast(StatusScheduleResponse status) {
        int i = 1;
        for (Forecast forecast : status.getForecast()) {
            String group = "forecast" + (i++);
            updateGroupState(group, CHANNEL_FORECAST_CONDITIONS, new StringType(forecast.getConditions()));
            updateGroupState(group, CHANNEL_FORECAST_DAY, new StringType(forecast.getDay()));
            updateGroupState(group, CHANNEL_FORECAST_HUMIDITY, new DecimalType(forecast.getHumidity()));
            updateTemperature(forecast.getTempHi(), group, CHANNEL_FORECAST_TEMPERATURE_HIGH);
            updateTemperature(forecast.getTempLo(), group, CHANNEL_FORECAST_TEMPERATURE_LOW);
            updateWindspeed(forecast.getWind(), group, CHANNEL_FORECAST_WIND);
        }
    }

    private void updateTemperature(String tempString, String group, String channel) {
        Matcher matcher = TEMPERATURE_PATTERN.matcher(tempString);
        if (matcher.matches()) {
            try {
                updateGroupState(group, channel, new QuantityType<Temperature>(Double.valueOf(matcher.group(1)),
                        "C".equals(matcher.group(2)) ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT));
            } catch (RuntimeException e) {
                logger.debug("Could not parse temperature string {} ", tempString);
            }
        }
    }

    private void updateWindspeed(String windString, String group, String channel) {
        Matcher matcher = WIND_SPEED_PATTERN.matcher(windString);
        if (matcher.matches()) {
            try {
                updateGroupState(group, channel, new QuantityType<Speed>(Integer.parseInt(matcher.group(1)),
                        "kph".equals(matcher.group(2)) ? SIUnits.KILOMETRE_PER_HOUR : ImperialUnits.MILES_PER_HOUR));
            } catch (RuntimeException e) {
                logger.debug("Could not parse wind string {} ", windString);
            }
        }
    }

    private void updateControllerProperties(Controller controller) {
        getThing().setProperty(PROPERTY_CONTROLLER_ID, String.valueOf(controller.getControllerId()));
        getThing().setProperty(PROPERTY_NAME, controller.getName());
        getThing().setProperty(PROPERTY_DESCRIPTION, controller.getDescription());
        getThing().setProperty(PROPERTY_LOCATION, controller.getLatitude() + "," + controller.getLongitude());
        getThing().setProperty(PROPERTY_ADDRESS, controller.getAddress());
    }

    private @Nullable Controller getController(int controllerId, List<Controller> controllers) {
        try {
            return controllers.stream().filter(c -> controllerId == c.getControllerId().intValue()).findAny().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}

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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCloudApiClient;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.model.Controller;
import org.openhab.binding.hydrawise.internal.api.model.CustomerDetailsResponse;
import org.openhab.binding.hydrawise.internal.api.model.Forecast;
import org.openhab.binding.hydrawise.internal.api.model.Relay;
import org.openhab.binding.hydrawise.internal.api.model.Running;
import org.openhab.binding.hydrawise.internal.api.model.StatusScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HydrawiseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
// @NonNullByDefault
public class HydrawiseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HydrawiseHandler.class);

    private @Nullable HydrawiseConfiguration config;

    /**
     * Minimum amount of time we can poll for updates
     */
    private static final int MIN_REFRESH_SECONDS = 5;

    /**
     * Minimum amount of time we can poll after a command
     */
    private static final int COMMAND_REFRESH_SECONDS = 5;

    /**
     * Our poll rate
     */
    private int refresh;

    /**
     * Future to poll for updated
     */
    private @Nullable ScheduledFuture<?> pollFuture;

    private Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Relay> relayMap = Collections.synchronizedMap(new HashMap<>());
    /**
     * Matches x days x hours x hours x minutes ago | Not scheduled
     */
    // private static final Pattern LAST_RUN_PATTERN = Pattern
    // .compile("(^Not scheduled)?((\\d{1,2}) days)?\\s?((\\d{1,2}) hours)?\\s?((\\d{1,2}) minutes)?");
    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile("^(\\d{1,3}.?\\d?)\\s([C,F])");
    private static final Pattern WIND_SPEED_PATTERN = Pattern.compile("^(\\d{1,3})\\s([a-z]{3})");

    private static long MAX_RUN_TIME = 157680000;

    HydrawiseCloudApiClient client;
    int controllerId;
    HttpClient httpClient;

    public HydrawiseHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Controller is NOT ONLINE and is not responding to commands");
            return;
        }

        stateMap.remove(channelUID.getAsString());
        if (command instanceof RefreshType) {
            stateMap.clear();
        }

        String group = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();

        Relay relay = relayMap.get(group);
        if (relay == null) {
            logger.debug("Zone not found {}", group);
            return;
        }

        try {
            clearPolling();
            switch (channelId) {
                case CHANNEL_ZONE_RUN_CUSTOM:
                    if (!(command instanceof DecimalType)) {
                        logger.warn("Invalid command type for run custom {}", command.getClass().getName());
                        return;
                    }
                    client.runRelay(((DecimalType) command).intValue(), relay.getRelayId());
                    break;
                case CHANNEL_ZONE_RUN:
                    if (!(command instanceof OnOffType)) {
                        logger.warn("Invalid command type for run {}", command.getClass().getName());
                        return;
                    }
                    if (command == OnOffType.ON) {
                        client.runRelay(relay.getRelayId());
                    } else {
                        client.stopRelay(relay.getRelayId());
                    }
                    break;
            }
            initPolling(COMMAND_REFRESH_SECONDS);
        } catch (HydrawiseCommandException | HydrawiseConnectionException e) {
            logger.debug("Could not issue command", e);
            initPolling(COMMAND_REFRESH_SECONDS);
        } catch (HydrawiseAuthenticationException e) {
            logger.debug("Credentials not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Credentials not valid");
            configure();
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        configure();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        clearPolling();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached value so the new channel gets updated on the next poll
        stateMap.remove(channelUID.getId());
    }

    private void configure() {
        clearPolling();

        stateMap.clear();
        relayMap.clear();

        HydrawiseCloudConfiguration configuration = getConfig().as(HydrawiseCloudConfiguration.class);
        if (StringUtils.isBlank(configuration.apiKey)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key connot be empty");
            return;
        }
        this.refresh = configuration.refresh.intValue() > MIN_REFRESH_SECONDS ? configuration.refresh.intValue()
                : MIN_REFRESH_SECONDS;

        Controller controller = null;
        try {
            client = new HydrawiseCloudApiClient(configuration.apiKey, httpClient);
            CustomerDetailsResponse customerDetails = client.getCustomerDetails();
            List<Controller> controllers = customerDetails.getControllers();
            if (controllers.size() == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No controllers found on account");
                return;
            }
            // try and use ID from user configuration
            if (configuration.controllerId != null) {
                controller = getController(configuration.controllerId.intValue(), controllers);
                if (controller == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No controller found for id " + configuration.controllerId);
                    return;
                }
            } else {
                // try and use ID from saved property
                if (StringUtils.isNotBlank(getThing().getProperties().get(PROPERTY_CONTROLLER_ID))) {
                    try {
                        controller = getController(
                                Integer.parseInt(getThing().getProperties().get(PROPERTY_CONTROLLER_ID)), controllers);

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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No controller found");
                return;
            }

            controllerId = controller.getControllerId().intValue();
            updateControllerProperties(controller);
            logger.debug("Controller id {}", controllerId);
            initPolling(0);
        } catch (HydrawiseConnectionException e) {
            logger.debug("Could not connect to service");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (HydrawiseAuthenticationException e) {
            logger.debug("Credentials not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Credentials not valid");
        }
    }

    /**
     * Starts/Restarts polling with an initial delay. This allows changes in the poll cycle for when commands are sent
     * and we need to poll sooner then the next refresh cycle.
     */
    private synchronized void initPolling(int initalDelay) {
        clearPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(this::pollController, initalDelay, refresh, TimeUnit.SECONDS);
    }

    /**
     * Stops/clears this thing's polling future
     */
    private void clearPolling() {
        ScheduledFuture<?> localFuture = pollFuture;
        if (isFutureValid(localFuture)) {
            if (localFuture != null) {
                localFuture.cancel(false);
            }
        }
    }

    private boolean isFutureValid(@Nullable ScheduledFuture<?> future) {
        return future != null && !future.isCancelled();
    }

    /**
     * Poll the controller for updates.
     */
    private void pollController() {
        ScheduledFuture<?> localFuture = pollFuture;
        try {
            List<Controller> controllers = client.getCustomerDetails().getControllers();
            Controller controller = getController(controllerId, controllers);

            if (!isFutureValid(localFuture)) {
                return;
            }

            if (controller != null && !controller.getOnline()) {
                logger.debug("Controller is offline");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Controller is offline");
                return;
            }

            StatusScheduleResponse status = client.getStatusSchedule(controllerId);
            if (!isFutureValid(localFuture)) {
                return;
            }

            logger.debug("Controller {} last contact {} seconds ago, {} total zones", status.getControllerId(),
                    status.getLastContact(), status.getRelays().size());

            updateSensors(status);
            updateForecast(status);
            updateZones(status);

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (HydrawiseConnectionException e) {
            // poller will continue to run, set offline until next run
            logger.debug("Exception polling", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (HydrawiseAuthenticationException e) {
            // if are creds are not valid, we need to try re authorizing again
            logger.debug("Authorization Exception during polling", e);
            clearPolling();
            configure();
        }

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
            updateGroupState(group, CHANNEL_FORECAST_ICON, new StringType(BASE_IMAGE_URL + forecast.getIcon()));
            updateTemperature(forecast.getTempHi(), group, CHANNEL_FORECAST_TEMPERATURE_HIGH);
            updateTemperature(forecast.getTempLo(), group, CHANNEL_FORECAST_TEMPERATURE_LOW);
            updateWindspeed(forecast.getWind(), group, CHANNEL_FORECAST_WIND);
        }
    }

    private void updateTemperature(String tempString, String group, String channel) {
        Matcher matcher = TEMPERATURE_PATTERN.matcher(tempString);
        if (matcher.matches()) {
            try {
                updateGroupState(group, channel, new QuantityType<Temperature>(Float.parseFloat(matcher.group(1)),
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
                updateGroupState(group, channel, new QuantityType<Speed>(Integer.parseInt(matcher.group(1)),
                        "kph".equals(matcher.group(2)) ? SIUnits.KILOMETRE_PER_HOUR : ImperialUnits.MILES_PER_HOUR));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse wind string {} ", windString);
            }
        }
    }

    private void updateZones(StatusScheduleResponse status) {
        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        status.getRelays().forEach(r -> {
            String group = "zone" + r.getRelayNumber();
            relayMap.put(group, r);
            logger.trace("Updateing Zone {} {} ", group, r.getName());
            updateGroupState(group, CHANNEL_ZONE_NAME, new StringType(r.getName()));
            updateGroupState(group, CHANNEL_ZONE_TYPE, new DecimalType(r.getType()));
            updateGroupState(group, CHANNEL_ZONE_TIME,
                    r.getRunTimeSeconds() != null ? new DecimalType(r.getRunTimeSeconds()) : UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_ZONE_ICON, new StringType(BASE_IMAGE_URL + r.getIcon()));

            if (r.getTime() >= MAX_RUN_TIME) {
                updateGroupState(group, CHANNEL_ZONE_NEXT_RUN_TIME_TIME, UnDefType.UNDEF);
            } else {
                updateGroupState(group, CHANNEL_ZONE_NEXT_RUN_TIME_TIME,
                        new DateTimeType(now.plusSeconds(r.getTime()).truncatedTo(ChronoUnit.MINUTES)));
            }

            Optional<Running> running = status.getRunning().stream().filter(z -> z.getRelayId() == r.getRelayId())
                    .findAny();
            if (running.isPresent()) {
                updateGroupState(group, CHANNEL_ZONE_RUN, OnOffType.ON);
                updateGroupState(group, CHANNEL_ZONE_TIME_LEFT, new DecimalType(running.get().getTimeLeft()));
                logger.debug("{} Time Left {}", r.getName(), running.get().getTimeLeft());

            } else {
                updateGroupState(group, CHANNEL_ZONE_RUN, OnOffType.OFF);
                updateGroupState(group, CHANNEL_ZONE_TIME_LEFT, new DecimalType(0));

            }
        });
    }

    private void updateControllerProperties(Controller controller) {
        getThing().setProperty(PROPERTY_CONTROLLER_ID, String.valueOf(controller.getControllerId()));
        getThing().setProperty(PROPERTY_NAME, controller.getName());
        getThing().setProperty(PROPERTY_DESCRIPTION, controller.getDescription());
        getThing().setProperty(PROPERTY_LOCATION, controller.getLatitude() + "," + controller.getLongitude());
        getThing().setProperty(PROPERTY_ADDRESS, controller.getAddress());
    }

    private void updateGroupState(String group, String channelID, State state) {
        String channelName = group + "#" + channelID;
        State oldState = stateMap.put(channelName, state);
        if (!state.equals(oldState)) {
            ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelName);
            logger.debug("updateState updating {} {}", channelUID, state);
            updateState(channelUID, state);
        }
    }

    private @Nullable Controller getController(int controllerId, List<Controller> controllers) {
        try {
            return controllers.stream().filter(c -> controllerId == c.getControllerId().intValue()).findAny().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}

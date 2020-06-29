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
package org.openhab.binding.radiothermostat.internal.handler;

import static org.openhab.binding.radiothermostat.internal.RadioThermostatBindingConstants.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.radiothermostat.internal.RadioThermostatConfiguration;
import org.openhab.binding.radiothermostat.internal.RadioThermostatStateDescriptionProvider;
import org.openhab.binding.radiothermostat.internal.RadioThermostatThingActions;
import org.openhab.binding.radiothermostat.internal.communication.RadioThermostatConnector;
import org.openhab.binding.radiothermostat.internal.communication.RadioThermostatEvent;
import org.openhab.binding.radiothermostat.internal.communication.RadioThermostatEventListener;
import org.openhab.binding.radiothermostat.internal.dto.RadioThermostatDTO;
import org.openhab.binding.radiothermostat.internal.dto.RadioThermostatHumidityDTO;
import org.openhab.binding.radiothermostat.internal.dto.RadioThermostatRuntimeDTO;
import org.openhab.binding.radiothermostat.internal.dto.RadioThermostatTstatDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RadioThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Based on the 'airquality' binding by Kuba Wolanin
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatHandler extends BaseThingHandler implements RadioThermostatEventListener {
    private static final int DEFAULT_REFRESH_PERIOD = 2;
    private static final int DEFAULT_LOG_REFRESH_PERIOD = 10;

    private @Nullable final RadioThermostatStateDescriptionProvider stateDescriptionProvider;

    private final Logger logger = LoggerFactory.getLogger(RadioThermostatHandler.class);

    private final Gson gson;
    private final RadioThermostatConnector connector;
    private final RadioThermostatDTO rthermData = new RadioThermostatDTO();

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> logRefreshJob;

    private @Nullable RadioThermostatConfiguration config;

    public RadioThermostatHandler(Thing thing,
            @Nullable RadioThermostatStateDescriptionProvider stateDescriptionProvider, HttpClient httpClient) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        gson = new Gson();
        connector = new RadioThermostatConnector(httpClient);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Initializing RadioThermostat handler.");
        this.config = getConfigAs(RadioThermostatConfiguration.class);
        connector.setThermostatHostName(config.hostName);
        connector.addEventListener(this);

        // populate fan mode options based on thermostat model
        List<StateOption> fanModeOptions = getFanModeOptions(config.isCT80);
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), FAN_MODE), fanModeOptions);

        // if we are not a CT-80, remove the humidity & program mode channel
        if (!config.isCT80) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());
            channels.removeIf(c -> (c.getUID().getId().equals(HUMIDITY)));
            channels.removeIf(c -> (c.getUID().getId().equals(PROGRAM_MODE)));
            updateThing(editThing().withChannels(channels).build());
        }
        startAutomaticRefresh();
        if (!config.disableLogs || config.isCT80) {
            startAutomaticLogRefresh();
        }

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(RadioThermostatThingActions.class);
    }

    /**
     * Start the job to periodically update data from the thermostat
     */
    @SuppressWarnings("null")
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                // send an async call to the thermostat to get the 'tstat' data
                connector.getAsyncThermostatData(DEFAULT_RESOURCE);
            };

            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    /**
     * Start the job to periodically update humidity and runtime date from the thermostat
     */
    @SuppressWarnings("null")
    private void startAutomaticLogRefresh() {
        if (logRefreshJob == null || logRefreshJob.isCancelled()) {
            Runnable runnable = () -> {
                // Request humidity data from the thermostat if we are a CT80
                if (config.isCT80) {
                    // send an async call to the thermostat to get the humidity data
                    connector.getAsyncThermostatData(HUMIDITY_RESOURCE);
                }

                if (!config.disableLogs) {
                    // send an async call to the thermostat to get the runtime data
                    connector.getAsyncThermostatData(RUNTIME_RESOURCE);
                }
            };

            int delay = ((config.logRefresh != null) ? config.logRefresh.intValue() : DEFAULT_LOG_REFRESH_PERIOD) * 60;
            logRefreshJob = scheduler.scheduleWithFixedDelay(runnable, 30, delay, TimeUnit.SECONDS);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        logger.debug("Disposing the RadioThermostat handler.");
        connector.removeEventListener(this);

        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        if (logRefreshJob != null) {
            logRefreshJob.cancel(true);
            logRefreshJob = null;
        }
    }

    public void handleRawCommand(@Nullable String rawCommand) {
        connector.sendCommand(null, null, rawCommand);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId(), rthermData);
        } else {
            Integer cmdInt = -1;
            String cmdStr = command.toString();
            if (cmdStr != null) {
                try {
                    // parse out an Integer from the string
                    // ie '70.5 F' becomes 70, also handles negative numbers
                    cmdInt = NumberFormat.getInstance().parse(cmdStr).intValue();
                } catch (ParseException e) {
                    logger.debug("Command: {} -> Not an integer", cmdStr);
                }
            }

            switch (channelUID.getId()) {
                case MODE:
                    // only do if commanded mode is different than current mode
                    if (!cmdInt.equals(rthermData.getThermostatData().getMode())) {
                        connector.sendCommand("tmode", cmdStr);

                        // set the new operating mode, reset everything else,
                        // because refreshing the tstat data below is really slow.
                        rthermData.getThermostatData().setMode(cmdInt);
                        rthermData.getThermostatData().setHeatTarget(0);
                        rthermData.getThermostatData().setCoolTarget(0);
                        updateChannel(SET_POINT, rthermData);
                        rthermData.getThermostatData().setHold(0);
                        updateChannel(HOLD, rthermData);
                        rthermData.getThermostatData().setProgramMode(-1);
                        updateChannel(PROGRAM_MODE, rthermData);

                        // now just trigger a refresh of the thermost to get the new active setpoint
                        // this takes a while for the JSON request to complete (async).
                        connector.getAsyncThermostatData(DEFAULT_RESOURCE);
                    }
                    break;
                case FAN_MODE:
                    rthermData.getThermostatData().setFanMode(cmdInt);
                    connector.sendCommand("fmode", cmdStr);
                    break;
                case PROGRAM_MODE:
                    rthermData.getThermostatData().setProgramMode(cmdInt);
                    connector.sendCommand("program_mode", cmdStr);
                    break;
                case HOLD:
                    if (command instanceof OnOffType && command == OnOffType.ON) {
                        rthermData.getThermostatData().setHold(1);
                        connector.sendCommand("hold", "1");
                    } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                        rthermData.getThermostatData().setHold(0);
                        connector.sendCommand("hold", "0");
                    }
                    break;
                case SET_POINT:
                    String cmdKey = null;
                    if (rthermData.getThermostatData().getMode() == 1) {
                        cmdKey = "t_heat";
                        rthermData.getThermostatData().setHeatTarget(cmdInt);
                    } else if (rthermData.getThermostatData().getMode() == 2) {
                        cmdKey = "t_cool";
                        rthermData.getThermostatData().setCoolTarget(cmdInt);
                    } else {
                        // don't do anything if we are not in heat or cool mode
                        break;
                    }
                    connector.sendCommand(cmdKey, cmdInt.toString());
                    break;
                default:
                    logger.warn("Unsupported command: {}", command.toString());
            }
        }
    }

    /**
     * Handle a RadioThermostat event received from the listeners
     *
     * @param event the event received from the listeners
     */
    @Override
    public void onNewMessageEvent(RadioThermostatEvent event) {
        logger.debug("onNewMessageEvent: key {} = {}", event.getKey(), event.getValue());

        String evtKey = event.getKey();
        String evtVal = event.getValue();

        if (KEY_ERROR.equals(evtKey)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Error retrieving data from Thermostat ");
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

            // Map the JSON response to the correct object and update appropriate channels
            switch (evtKey) {
                case DEFAULT_RESOURCE:
                    rthermData.setThermostatData(gson.fromJson(evtVal, RadioThermostatTstatDTO.class));
                    updateAllChannels();
                    break;
                case HUMIDITY_RESOURCE:
                    rthermData.setHumidity(gson.fromJson(evtVal, RadioThermostatHumidityDTO.class).getHumidity());
                    updateChannel(HUMIDITY, rthermData);
                    break;
                case RUNTIME_RESOURCE:
                    rthermData.setRuntime(gson.fromJson(evtVal, RadioThermostatRuntimeDTO.class));
                    updateChannel(TODAY_HEAT_RUNTIME, rthermData);
                    updateChannel(TODAY_COOL_RUNTIME, rthermData);
                    updateChannel(YESTERDAY_HEAT_RUNTIME, rthermData);
                    updateChannel(YESTERDAY_COOL_RUNTIME, rthermData);
                    break;
            }
        }
    }

    /**
     * Update the channel from the last Thermostat data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId, RadioThermostatDTO rthermData) {
        if (isLinked(channelId)) {
            Object value;
            try {
                value = getValue(channelId, rthermData);
            } catch (Exception e) {
                logger.debug("Error setting {} value", channelId.toUpperCase());
                return;
            }

            State state = null;
            if (value == null) {
                state = UnDefType.UNDEF;
            } else if (value instanceof PointType) {
                state = (PointType) value;
            } else if (value instanceof ZonedDateTime) {
                state = new DateTimeType((ZonedDateTime) value);
            } else if (value instanceof QuantityType<?>) {
                state = (QuantityType<?>) value;
            } else if (value instanceof BigDecimal) {
                state = new DecimalType((BigDecimal) value);
            } else if (value instanceof Integer) {
                state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
            } else if (value instanceof String) {
                state = new StringType(value.toString());
            } else if (value instanceof OnOffType) {
                state = (OnOffType) value;
            } else {
                logger.warn("Update channel {}: Unsupported value type {}", channelId,
                        value.getClass().getSimpleName());
            }
            logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                    (value == null) ? "null" : value.getClass().getSimpleName());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    /**
     * Update a given channelId from the thermostat data
     * 
     * @param the channel id to be updated
     * @param data the RadioThermostat dto
     * @return the value to be set in the state
     */
    public static @Nullable Object getValue(String channelId, RadioThermostatDTO data) {
        switch (channelId) {
            case TEMPERATURE:
                if (data.getThermostatData().getTemperature() != null) {
                    return new QuantityType<Temperature>(data.getThermostatData().getTemperature(),
                            API_TEMPERATURE_UNIT);
                } else {
                    return null;
                }
            case HUMIDITY:
                if (data.getHumidity() != null) {
                    return new QuantityType<>(data.getHumidity(), API_HUMIDITY_UNIT);
                } else {
                    return null;
                }
            case MODE:
                return data.getThermostatData().getMode();
            case FAN_MODE:
                return data.getThermostatData().getFanMode();
            case PROGRAM_MODE:
                return data.getThermostatData().getProgramMode();
            case SET_POINT:
                if (data.getThermostatData().getSetpoint() != 0) {
                    return new QuantityType<Temperature>(data.getThermostatData().getSetpoint(), API_TEMPERATURE_UNIT);
                } else {
                    return null;
                }
            case OVERRIDE:
                return data.getThermostatData().getOverride();
            case HOLD:
                return OnOffType.from(data.getThermostatData().getHold() == 1);
            case STATUS:
                return data.getThermostatData().getStatus();
            case FAN_STATUS:
                return data.getThermostatData().getFanStatus();
            case DAY:
                return data.getThermostatData().getTime().getDayOfWeek();
            case HOUR:
                return data.getThermostatData().getTime().getHour();
            case MINUTE:
                return data.getThermostatData().getTime().getMinute();
            case DATE_STAMP:
                return data.getThermostatData().getTime().getThemostatDateTime();
            case TODAY_HEAT_RUNTIME:
                return new QuantityType<>(data.getRuntime().getToday().getHeatTime().getRuntime(), API_MINUTES_UNIT);
            case TODAY_COOL_RUNTIME:
                return new QuantityType<>(data.getRuntime().getToday().getCoolTime().getRuntime(), API_MINUTES_UNIT);
            case YESTERDAY_HEAT_RUNTIME:
                return new QuantityType<>(data.getRuntime().getYesterday().getHeatTime().getRuntime(),
                        API_MINUTES_UNIT);
            case YESTERDAY_COOL_RUNTIME:
                return new QuantityType<>(data.getRuntime().getYesterday().getCoolTime().getRuntime(),
                        API_MINUTES_UNIT);
        }
        return null;
    }

    /**
     * Updates all channels from rthermData
     */
    private void updateAllChannels() {
        // Update all channels from rthermData
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId(), rthermData);
        }
    }

    /**
     * Build a list of fan modes based on what model thermostat is used
     * 
     * @return list of state options for thermostat fan modes
     */
    private List<StateOption> getFanModeOptions(boolean isCT80) {
        List<StateOption> fanModeOptions = new ArrayList<>();

        fanModeOptions.add(new StateOption("0", "Auto"));
        if (isCT80) {
            fanModeOptions.add(new StateOption("1", "Auto/Circulate"));
        }
        fanModeOptions.add(new StateOption("2", "On"));

        return fanModeOptions;
    }
}

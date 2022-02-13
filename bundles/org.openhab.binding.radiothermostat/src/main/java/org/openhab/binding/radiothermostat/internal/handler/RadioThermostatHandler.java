/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
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

    private final RadioThermostatStateDescriptionProvider stateDescriptionProvider;
    private final Logger logger = LoggerFactory.getLogger(RadioThermostatHandler.class);

    private final Gson gson;
    private final RadioThermostatConnector connector;
    private final RadioThermostatDTO rthermData = new RadioThermostatDTO();

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> logRefreshJob;
    private @Nullable ScheduledFuture<?> clockSyncJob;

    private int refreshPeriod = DEFAULT_REFRESH_PERIOD;
    private int logRefreshPeriod = DEFAULT_LOG_REFRESH_PERIOD;
    private boolean isCT80 = false;
    private boolean disableLogs = false;
    private boolean clockSync = false;
    private String setpointCmdKeyPrefix = "t_";

    public RadioThermostatHandler(Thing thing, RadioThermostatStateDescriptionProvider stateDescriptionProvider,
            HttpClient httpClient) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        gson = new Gson();
        connector = new RadioThermostatConnector(httpClient);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RadioThermostat handler.");
        RadioThermostatConfiguration config = getConfigAs(RadioThermostatConfiguration.class);

        final String hostName = config.hostName;
        final Integer refresh = config.refresh;
        final Integer logRefresh = config.logRefresh;
        this.isCT80 = config.isCT80;
        this.disableLogs = config.disableLogs;
        this.clockSync = config.clockSync;

        if (hostName == null || "".equals(hostName)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Thermostat Host Name must be specified");
            return;
        }

        if (refresh != null) {
            this.refreshPeriod = refresh;
        }

        if (logRefresh != null) {
            this.logRefreshPeriod = logRefresh;
        }

        connector.setThermostatHostName(hostName);
        connector.addEventListener(this);

        // The setpoint mode is controlled by the name of setpoint attribute sent to the thermostat.
        // Temporary mode uses setpoint names prefixed with "t_" while absolute mode uses "a_"
        if (config.setpointMode.equals("absolute")) {
            this.setpointCmdKeyPrefix = "a_";
        }

        // populate fan mode options based on thermostat model
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), FAN_MODE), getFanModeOptions());

        // if we are not a CT-80, remove the humidity & program mode channel
        if (!this.isCT80) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());
            channels.removeIf(c -> (c.getUID().getId().equals(HUMIDITY)));
            channels.removeIf(c -> (c.getUID().getId().equals(PROGRAM_MODE)));
            updateThing(editThing().withChannels(channels).build());
        }

        updateStatus(ThingStatus.UNKNOWN);

        startAutomaticRefresh();

        if (!this.disableLogs || this.isCT80) {
            startAutomaticLogRefresh();
        }

        if (this.clockSync) {
            scheduleClockSyncJob();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(RadioThermostatThingActions.class);
    }

    /**
     * Start the job to periodically update data from the thermostat
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                // send an async call to the thermostat to get the 'tstat' data
                connector.getAsyncThermostatData(DEFAULT_RESOURCE);
            };

            refreshJob = null;
            this.refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, refreshPeriod, TimeUnit.MINUTES);
        }
    }

    /**
     * Schedule the clock sync job
     */
    private void scheduleClockSyncJob() {
        ScheduledFuture<?> clockSyncJob = this.clockSyncJob;
        if (clockSyncJob == null || clockSyncJob.isCancelled()) {
            clockSyncJob = null;
            this.clockSyncJob = scheduler.scheduleWithFixedDelay(this::syncThermostatClock, 1, 60, TimeUnit.MINUTES);
        }
    }

    /**
     * Sync the thermostat's clock with the host system clock
     */
    private void syncThermostatClock() {
        Calendar c = Calendar.getInstance();

        // The thermostat week starts as Monday = 0, subtract 2 since in standard DoW Monday = 2
        int thermDayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 2;
        // Sunday will be -1, so add 7 to make it 6
        if (thermDayOfWeek < 0) {
            thermDayOfWeek += 7;
        }

        connector.sendCommand(null, null,
                String.format(JSON_TIME, thermDayOfWeek, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)),
                TIME_RESOURCE);
    }

    /**
     * Start the job to periodically update humidity and runtime date from the thermostat
     */
    private void startAutomaticLogRefresh() {
        ScheduledFuture<?> logRefreshJob = this.logRefreshJob;
        if (logRefreshJob == null || logRefreshJob.isCancelled()) {
            Runnable runnable = () -> {
                // Request humidity data from the thermostat if we are a CT80
                if (this.isCT80) {
                    // send an async call to the thermostat to get the humidity data
                    connector.getAsyncThermostatData(HUMIDITY_RESOURCE);
                }

                if (!this.disableLogs) {
                    // send an async call to the thermostat to get the runtime data
                    connector.getAsyncThermostatData(RUNTIME_RESOURCE);
                }
            };

            logRefreshJob = null;
            this.logRefreshJob = scheduler.scheduleWithFixedDelay(runnable, (!this.clockSync ? 1 : 2), logRefreshPeriod,
                    TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the RadioThermostat handler.");
        connector.removeEventListener(this);

        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }

        ScheduledFuture<?> logRefreshJob = this.logRefreshJob;
        if (logRefreshJob != null) {
            logRefreshJob.cancel(true);
            this.logRefreshJob = null;
        }

        ScheduledFuture<?> clockSyncJob = this.clockSyncJob;
        if (clockSyncJob != null) {
            clockSyncJob.cancel(true);
            this.clockSyncJob = null;
        }
    }

    public void handleRawCommand(@Nullable String rawCommand) {
        connector.sendCommand(null, null, rawCommand, DEFAULT_RESOURCE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId(), rthermData);
        } else {
            Integer cmdInt = -1;
            String cmdStr = command.toString();
            try {
                // parse out an Integer from the string
                // ie '70.5 F' becomes 70, also handles negative numbers
                cmdInt = NumberFormat.getInstance().parse(cmdStr).intValue();
            } catch (ParseException e) {
                logger.debug("Command: {} -> Not an integer", cmdStr);
            }

            switch (channelUID.getId()) {
                case MODE:
                    // only do if commanded mode is different than current mode
                    if (!cmdInt.equals(rthermData.getThermostatData().getMode())) {
                        connector.sendCommand("tmode", cmdStr, DEFAULT_RESOURCE);

                        // set the new operating mode, reset everything else,
                        // because refreshing the tstat data below is really slow.
                        rthermData.getThermostatData().setMode(cmdInt);
                        rthermData.getThermostatData().setHeatTarget(Double.valueOf(0));
                        rthermData.getThermostatData().setCoolTarget(Double.valueOf(0));
                        updateChannel(SET_POINT, rthermData);
                        rthermData.getThermostatData().setHold(0);
                        updateChannel(HOLD, rthermData);
                        rthermData.getThermostatData().setProgramMode(-1);
                        updateChannel(PROGRAM_MODE, rthermData);

                        // now just trigger a refresh of the thermostat to get the new active setpoint
                        // this takes a while for the JSON request to complete (async).
                        connector.getAsyncThermostatData(DEFAULT_RESOURCE);
                    }
                    break;
                case FAN_MODE:
                    rthermData.getThermostatData().setFanMode(cmdInt);
                    connector.sendCommand("fmode", cmdStr, DEFAULT_RESOURCE);
                    break;
                case PROGRAM_MODE:
                    rthermData.getThermostatData().setProgramMode(cmdInt);
                    connector.sendCommand("program_mode", cmdStr, DEFAULT_RESOURCE);
                    break;
                case HOLD:
                    if (command instanceof OnOffType && command == OnOffType.ON) {
                        rthermData.getThermostatData().setHold(1);
                        connector.sendCommand("hold", "1", DEFAULT_RESOURCE);
                    } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                        rthermData.getThermostatData().setHold(0);
                        connector.sendCommand("hold", "0", DEFAULT_RESOURCE);
                    }
                    break;
                case SET_POINT:
                    String cmdKey = null;
                    if (rthermData.getThermostatData().getMode() == 1) {
                        cmdKey = this.setpointCmdKeyPrefix + "heat";
                        rthermData.getThermostatData().setHeatTarget(Double.valueOf(cmdInt));
                    } else if (rthermData.getThermostatData().getMode() == 2) {
                        cmdKey = this.setpointCmdKeyPrefix + "cool";
                        rthermData.getThermostatData().setCoolTarget(Double.valueOf(cmdInt));
                    } else {
                        // don't do anything if we are not in heat or cool mode
                        break;
                    }
                    connector.sendCommand(cmdKey, cmdInt.toString(), DEFAULT_RESOURCE);
                    break;
                case REMOTE_TEMP:
                    if (cmdInt != -1) {
                        QuantityType<?> remoteTemp = ((QuantityType<Temperature>) command)
                                .toUnit(ImperialUnits.FAHRENHEIT);
                        connector.sendCommand("rem_temp", String.valueOf(remoteTemp.intValue()), REMOTE_TEMP_RESOURCE);
                    } else {
                        connector.sendCommand("rem_mode", "0", REMOTE_TEMP_RESOURCE);
                    }
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
                    RadioThermostatHumidityDTO dto = gson.fromJson(evtVal, RadioThermostatHumidityDTO.class);
                    if (dto != null) {
                        rthermData.setHumidity(dto.getHumidity());
                    }
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
                // workaround for some thermostats that don't report that the fan is on during heating or cooling
                if (data.getThermostatData().getStatus() > 0) {
                    return 1;
                } else {
                    return data.getThermostatData().getFanStatus();
                }
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
    private List<StateOption> getFanModeOptions() {
        List<StateOption> fanModeOptions = new ArrayList<>();

        fanModeOptions.add(new StateOption("0", "Auto"));
        if (this.isCT80) {
            fanModeOptions.add(new StateOption("1", "Auto/Circulate"));
        }
        fanModeOptions.add(new StateOption("2", "On"));

        return fanModeOptions;
    }
}

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

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.radiothermostat.internal.RadioThermostatBindingConstants.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.radiothermostat.internal.RadioThermostatConfiguration;
import org.openhab.binding.radiothermostat.internal.RadioThermostatHttpException;
import org.openhab.binding.radiothermostat.internal.RadioThermostatStateDescriptionProvider;
import org.openhab.binding.radiothermostat.internal.json.RadioThermostatData;
import org.openhab.binding.radiothermostat.internal.json.RadioThermostatJsonHumidity;
import org.openhab.binding.radiothermostat.internal.json.RadioThermostatJsonModel;
import org.openhab.binding.radiothermostat.internal.json.RadioThermostatJsonName;
import org.openhab.binding.radiothermostat.internal.json.RadioThermostatJsonResponse;
import org.openhab.binding.radiothermostat.internal.json.RadioThermostatJsonRuntime;
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
public class RadioThermostatHandler extends BaseThingHandler {
    private @Nullable final RadioThermostatStateDescriptionProvider stateDescriptionProvider;

    private Logger logger = LoggerFactory.getLogger(RadioThermostatHandler.class);

    private final HttpClient httpClient;

    private static final String URL = "http://%hostName%/%resource%";

    private static final String DEFAULT_RESOURCE = "tstat";
    private static final String RUNTIME_RESOURCE = "tstat/datalog";
    private static final String HUMIDITY_RESOURCE = "tstat/humidity";
    private static final String MODEL_RESOURCE = "tstat/model";
    private static final String NAME_RESOURCE = "sys/name";

    private static final int DEFAULT_REFRESH_PERIOD = 2;
    private static final int DEFAULT_LOG_REFRESH_PERIOD = 10;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> logRefreshJob;

    private RadioThermostatData rthermData = new RadioThermostatData();

    private Gson gson;

    private int initialized = 0;
    private int retryCounter = 0;

    public RadioThermostatHandler(Thing thing,
            @Nullable RadioThermostatStateDescriptionProvider stateDescriptionProvider, HttpClient httpClient) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.httpClient = httpClient;
        gson = new Gson();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RadioThermostat handler.");
        RadioThermostatConfiguration config = getConfigAs(RadioThermostatConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        startAutomaticRefresh();
        if (!(config.disableLogs == 1 && config.disableHumidity == 1)) {
            startAutomaticLogRefresh();
        }
    }

    /**
     * Start the job to periodically update data from the thermostat
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                if (initialized == 0) {
                    setupConnection();
                }

                try {
                    // Request new data from the thermostat
                    retryCounter = 0;
                    Object result = getRadioThermostatData(DEFAULT_RESOURCE);
                    if (result instanceof RadioThermostatJsonResponse) {
                        rthermData.setThermostatData((RadioThermostatJsonResponse) result);

                        // Update all channels with the new data
                        updateAllChannels();
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage());
                }
            };

            RadioThermostatConfiguration config = getConfigAs(RadioThermostatConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    /**
     * Start the job to periodically update humidity and runtime date from the thermostat
     */
    private void startAutomaticLogRefresh() {
        if (logRefreshJob == null || logRefreshJob.isCancelled()) {
            RadioThermostatConfiguration config = getConfigAs(RadioThermostatConfiguration.class);
            Runnable runnable = () -> {
                try {
                    // Request humidity data from the thermostat if we are a CT80
                    if (isCT80() && config.disableHumidity != 1) {
                        retryCounter = 0;
                        Object result = getRadioThermostatData(HUMIDITY_RESOURCE);
                        if (result instanceof RadioThermostatJsonHumidity) {
                            rthermData.setHumidity(((RadioThermostatJsonHumidity) result).getHumidity());
                        }
                    }

                    if (config.disableLogs != 1) {
                        // Request runtime data from the thermostat
                        retryCounter = 0;
                        Object result = getRadioThermostatData(RUNTIME_RESOURCE);
                        if (result instanceof RadioThermostatJsonRuntime) {
                            rthermData.setRuntime((RadioThermostatJsonRuntime) result);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage());
                }
            };

            int delay = (config.logRefresh != null) ? config.logRefresh.intValue() : DEFAULT_LOG_REFRESH_PERIOD;
            logRefreshJob = scheduler.scheduleWithFixedDelay(runnable, 1, delay, TimeUnit.MINUTES);
        }
    }

    private void setupConnection() {
        String errorMsg = null;

        // test the thermostat connection by getting the thermostat name and model
        Object nameResult = getRadioThermostatData(NAME_RESOURCE);
        if (nameResult instanceof RadioThermostatJsonName) {
            rthermData.setName(((RadioThermostatJsonName) nameResult).getName());
        } else {
            errorMsg = "Unable to get thermostat name";
        }

        Object modelResult = getRadioThermostatData(MODEL_RESOURCE);
        if (modelResult instanceof RadioThermostatJsonModel) {
            rthermData.setModel(((RadioThermostatJsonModel) modelResult).getModel());
        } else {
            errorMsg = "Unable to get thermostat model";
        }

        // populate fan mode options based on thermostat model
        List<StateOption> fanModeOptions = getFanModeOptions();
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), FAN_MODE), fanModeOptions);

        // if we are not a CT-80, remove the humidity & program mode channel
        if (!isCT80()) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());
            channels.removeIf(c -> (c.getUID().getId().equals(HUMIDITY)));
            channels.removeIf(c -> (c.getUID().getId().equals(PROGRAM_MODE)));
            updateThing(editThing().withChannels(channels).build());
        }

        if (errorMsg == null) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                    rthermData.getName() + " " + rthermData.getModel());
            initialized = 1;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the RadioThermostat handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        if (logRefreshJob != null && !logRefreshJob.isCancelled()) {
            logRefreshJob.cancel(true);
            logRefreshJob = null;
        }
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
                    // remove all non-numeric characters except negative '-' and parse int
                    cmdInt = Integer.parseInt(cmdStr.replaceAll("[^\\d-]", ""));
                } catch (NumberFormatException e) {
                    logger.debug("Command: {} -> Not an integer", cmdStr);
                    return;
                }
            }

            switch (channelUID.getId()) {
                case JSON_CMD:
                    sendCommand(null, null, command.toString());
                    break;
                case MODE:
                    // only do if commanded mode is different than current mode
                    if (!cmdInt.equals(rthermData.getThermostatData().getMode())) {
                        sendCommand("tmode", cmdStr);

                        // set the new operating mode, reset everything else,
                        // because refreshing the tstat data below is really slow.
                        rthermData.getThermostatData().setMode(cmdInt);
                        rthermData.getThermostatData().setHeatTarget(0);
                        rthermData.getThermostatData().setCoolTarget(0);
                        rthermData.getThermostatData().setHold(0);
                        rthermData.getThermostatData().setProgramMode(-1);
                        updateAllChannels();

                        // now just go ahead and refresh tstat data to update with the new active setpoint
                        // this takes a while for the JSON request to complete.
                        Object result = getRadioThermostatData(DEFAULT_RESOURCE);
                        if (result instanceof RadioThermostatJsonResponse) {
                            rthermData.setThermostatData((RadioThermostatJsonResponse) result);
                            updateAllChannels();
                        }
                    }
                    break;
                case FAN_MODE:
                    rthermData.getThermostatData().setFanMode(cmdInt);
                    updateChannel(channelUID.getId(), rthermData);
                    sendCommand("fmode", cmdStr);
                    break;
                case PROGRAM_MODE:
                    rthermData.getThermostatData().setProgramMode(cmdInt);
                    updateChannel(channelUID.getId(), rthermData);
                    sendCommand("program_mode", cmdStr);
                    break;
                case HOLD:
                    if (command instanceof OnOffType && command == OnOffType.ON) {
                        rthermData.getThermostatData().setHold(1);
                        updateChannel(channelUID.getId(), rthermData);
                        sendCommand("hold", "1");
                    } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                        rthermData.getThermostatData().setHold(0);
                        updateChannel(channelUID.getId(), rthermData);
                        sendCommand("hold", "0");
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
                    updateChannel(channelUID.getId(), rthermData);
                    sendCommand(cmdKey, cmdInt.toString());
                    break;
                default:
                    logger.warn("Unsupported command: {}", command.toString());
            }
        }
    }

    /**
     * Update the channel from the last Thermostat data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId, RadioThermostatData rthermData) {
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
     * Build request URL from configuration data
     *
     * @return a valid URL for the thermostat's JSON interface
     */
    private String buildRequestURL(String resource) {
        RadioThermostatConfiguration config = getConfigAs(RadioThermostatConfiguration.class);

        String hostName = config.hostName;

        String urlStr = URL.replace("%hostName%", hostName);
        urlStr = urlStr.replace("%resource%", resource);

        return urlStr;
    }

    /**
     * Request new data from the thermostat
     *
     * @param the resource URL constant for a particular thermostat JSON resource
     * @return an object mapping to one of the various thermostat JSON responses or null in case of error
     */
    private @Nullable Object getRadioThermostatData(String resource) {
        Object result = null;
        String errorMsg = null;

        String urlStr = buildRequestURL(resource);
        logger.debug("URL = {}", urlStr);

        try {
            // Run the HTTP request and get the JSON response from the thermostat
            ContentResponse contentResponse = httpClient.newRequest(urlStr).method(GET).timeout(20, TimeUnit.SECONDS)
                    .send();

            String response = contentResponse.getContentAsString();
            logger.debug("thermostatResponse = {}", response);

            // Map the JSON response to the correct object
            if (DEFAULT_RESOURCE.equals(resource)) {
                result = gson.fromJson(response, RadioThermostatJsonResponse.class);
            } else if (HUMIDITY_RESOURCE.equals(resource)) {
                result = gson.fromJson(response, RadioThermostatJsonHumidity.class);
            } else if (RUNTIME_RESOURCE.equals(resource)) {
                result = gson.fromJson(response, RadioThermostatJsonRuntime.class);
            } else if (MODEL_RESOURCE.equals(resource)) {
                result = gson.fromJson(response, RadioThermostatJsonModel.class);
            } else if (NAME_RESOURCE.equals(resource)) {
                result = gson.fromJson(response, RadioThermostatJsonName.class);
            }

            if (result != null) {
                if (initialized == 1) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                            rthermData.getName() + " " + rthermData.getModel());
                }
                return result;
            } else {
                retryCounter++;
                if (retryCounter == 1) {
                    logger.warn("Error in contacting the thermostat, retrying once");
                    return getRadioThermostatData(resource);
                }
                errorMsg = "missing data object";
                logger.warn("Error in thermostat response: {}", errorMsg);
            }

        } catch (IllegalStateException | InterruptedException | TimeoutException | ExecutionException e) {
            errorMsg = e.getMessage();
            logger.warn("Error running thermostat request: {}", errorMsg);
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        return null;
    }

    /**
     * Sends a command to the thermostat
     *
     * @param the JSON attribute key for the value to be updated
     * @param the value to be updated in the thermostat
     * @return the JSON response string from the thermostat
     */
    private String sendCommand(String cmdKey, @Nullable String cmdVal) {
        return sendCommand(cmdKey, cmdVal, null);
    }

    /**
     * Sends a command to the thermostat
     *
     * @param the JSON attribute key for the value to be updated
     * @param the value to be updated in the thermostat
     * @param JSON string to send directly to the thermostat instead of a key/value pair
     * @return the JSON response string from the thermostat
     */
    private String sendCommand(@Nullable String cmdKey, @Nullable String cmdVal, @Nullable String cmdJson) {
        // if we got a cmdJson string send that, otherwise build the json from the key and val params
        String postJson = cmdJson != null ? cmdJson : "{\"" + cmdKey + "\":" + cmdVal + "}";
        String urlStr = buildRequestURL(DEFAULT_RESOURCE);

        String output = null;

        try {
            Request request = httpClient.POST(urlStr);
            request.header(HttpHeader.ACCEPT, "text/plain");
            request.header(HttpHeader.CONTENT_TYPE, "text/plain");
            request.content(new StringContentProvider(postJson), "application/json");

            ContentResponse contentResponse = request.send();
            int httpStatus = contentResponse.getStatus();

            if (httpStatus != OK_200) {
                throw new RadioThermostatHttpException("Thermostat HTTP response code was: " + httpStatus);
            }
            output = contentResponse.getContentAsString();
        } catch (RadioThermostatHttpException | InterruptedException | TimeoutException | ExecutionException
                | IllegalStateException e) {
            logger.warn("Error executing thermostat command: {}, {}", postJson, e.getMessage());
        }

        return output;

    }

    /**
     * Update a given channelId from the thermostat data
     * 
     * @param the channel id to be updated
     * @param data
     * @return
     * @throws Exception
     */
    public static @Nullable Object getValue(String channelId, RadioThermostatData data) throws Exception {
        switch (channelId) {
            case TEMPERATURE:
                if (!data.getThermostatData().getTemperature().equals(new Double(0))) {
                    return new QuantityType<Temperature>(data.getThermostatData().getTemperature(),
                            API_TEMPERATURE_UNIT);
                } else {
                    return null;
                }
            case HUMIDITY:
                if (data.getHumidity() != 0) {
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
                if (data.getThermostatData().getHold() == 1) {
                    return OnOffType.ON;
                } else {
                    return OnOffType.OFF;
                }
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
            case LAST_UPDATE:
                return ZonedDateTime.now();
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
     * Check if the thermostat is a CT80 model
     * 
     * @return boolean indicating whether or not the thermostat is a CT80 model
     */
    private boolean isCT80() {
        return (rthermData.getModel() != null && rthermData.getModel().contains("CT80"));
    }

    /**
     * Build a list of fan modes based on what model thermostat is used
     * 
     * @return list of state options for thermostat fan modes
     */
    private List<StateOption> getFanModeOptions() {
        List<StateOption> fanModeOptions = new ArrayList<>();

        fanModeOptions.add(new StateOption("0", "Auto"));
        if (isCT80()) {
            fanModeOptions.add(new StateOption("1", "Auto/Circulate"));
        }
        fanModeOptions.add(new StateOption("2", "On"));

        return fanModeOptions;
    }

}

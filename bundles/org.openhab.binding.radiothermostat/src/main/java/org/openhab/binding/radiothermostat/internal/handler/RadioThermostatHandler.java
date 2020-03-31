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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RadioThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Based on the 'airquality' binding by Kuba Wolanin
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatHandler extends BaseThingHandler {
    private final RadioThermostatStateDescriptionProvider stateDescriptionProvider;

    private Logger logger = LoggerFactory.getLogger(RadioThermostatHandler.class);

    private static final String URL = "http://%hostName%/%resource%";
    
    private static final String DEFAULT_RESOURCE    = "tstat";
    private static final String RUNTIME_RESOURCE    = "tstat/datalog";
    private static final String HUMIDITY_RESOURCE   = "tstat/humidity";
    private static final String MODEL_RESOURCE      = "tstat/model";
    private static final String NAME_RESOURCE       = "sys/name";

    private static final int DEFAULT_REFRESH_PERIOD = 2;
    private static final int DEFAULT_LOG_REFRESH_PERIOD = 10;

    private ScheduledFuture<?> refreshJob;
    private ScheduledFuture<?> logRefreshJob;

    private RadioThermostatData rthermData = new RadioThermostatData();
    
    private Gson gson;

    private int initialized = 0;
    private int retryCounter = 0;

    public RadioThermostatHandler(Thing thing, RadioThermostatStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        gson = new Gson();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RadioThermostat handler.");

        RadioThermostatConfiguration config = getConfigAs(RadioThermostatConfiguration.class);
        logger.debug("config hostName = {}", config.hostName); 
        logger.debug("config refresh = {}", config.refresh);
        logger.debug("config logRefresh = {}", config.logRefresh);
        logger.debug("config disableLogs = {}", config.disableLogs);
        logger.debug("config disableHumidity = {}", config.disableHumidity);

        String errorMsg = null;

        if (StringUtils.trimToNull(config.hostName) == null) {
            errorMsg = "Parameter 'hostName' is mandatory and must be configured";
        }
        if (config.refresh != null && config.refresh < 1) {
            errorMsg = "Parameter 'refresh' must be at least 1 minute";
        }
        if (config.logRefresh != null && config.logRefresh < 5) {
            errorMsg = "Parameter 'logRefresh' must be at least 5 minutes";
        }
        if (config.disableLogs != null && (config.disableLogs != 0 && config.disableLogs != 1)) {
            errorMsg = "Parameter 'disableLogs' must be either 0 or 1";
        }
        if (config.disableHumidity != null && (config.disableHumidity != 0 && config.disableHumidity != 1)) {
            errorMsg = "Parameter 'disableHumidity' must be either 0 or 1";
        }

        //test the thermostat connection by getting the thermostat name and model
        if (errorMsg == null) {
            try {
                Object nameResult = getRadioThermostatData(NAME_RESOURCE);
                if (nameResult instanceof RadioThermostatJsonName) {
                    rthermData.setName(((RadioThermostatJsonName) nameResult).getName());
                } else {
                    errorMsg = "Unable to get thermostat name";
                }
                
                Thread.sleep(2000);
                
                Object modelResult = getRadioThermostatData(MODEL_RESOURCE);
                if (modelResult instanceof RadioThermostatJsonModel) {
                    rthermData.setModel(((RadioThermostatJsonModel) modelResult).getModel());
                } else {
                    errorMsg = "Unable to get thermostat model";
                }
            } catch (Exception e) {
                logger.error("Exception occurred attempting to connect with thermostat: {}", e.getMessage(), e);
            }
        }
        
        // populate fan mode options based on thermostat model
        List<StateOption> fanModeOptions = getFanModeOptions();
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), FAN_MODE), fanModeOptions);
        
        if (errorMsg == null) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, rthermData.getName() + " " + rthermData.getModel());
            initialized = 1;
            startAutomaticRefresh();
            if (!(config.disableLogs == 1 && config.disableHumidity == 1)) {
                startAutomaticLogRefresh();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job to periodically update data from the thermostat
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
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
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
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
                        Thread.sleep(2000);
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
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            int delay = (config.logRefresh != null) ? config.logRefresh.intValue() : DEFAULT_LOG_REFRESH_PERIOD;
            logRefreshJob = scheduler.scheduleWithFixedDelay(runnable, 1, delay, TimeUnit.MINUTES);
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
            // remove all non-numeric characters except negative '-'
            String cmdStr = command.toString().replaceAll("[^\\d-]", "");
            Integer cmdInt =  Integer.parseInt(cmdStr);
            
            switch (channelUID.getId()) {
            case MODE:
                //only do if commanded mode is different than current mode
                if (!cmdInt.equals(rthermData.getThermostatData().getMode())) {
                    sendCommand("tmode", cmdStr);
                    
                    // set the new operating mode, reset everything else,
                    // because refreshing the tstat data below is really slow.
                    rthermData.getThermostatData().setMode(cmdInt);
                    rthermData.getThermostatData().setHeatTarget(null);
                    rthermData.getThermostatData().setCoolTarget(null);
                    rthermData.getThermostatData().setHold(0);
                    rthermData.getThermostatData().setProgramMode(-1);
                    updateAllChannels();
                    
                    //now just go ahead and refresh tstat data to update with the new active setpoint
                    // this takes a while for the JSON request to complete.
                    Object result = getRadioThermostatData(DEFAULT_RESOURCE);
                    if (result instanceof RadioThermostatJsonResponse) {
                        rthermData.setThermostatData((RadioThermostatJsonResponse) result);
                        updateAllChannels();
                    }
                }
                break;
            case FAN_MODE:
                sendCommand("fmode", cmdStr);
                rthermData.getThermostatData().setFanMode(cmdInt);
                break;
            case PROGRAM_MODE:
                sendCommand("program_mode", cmdStr);
                rthermData.getThermostatData().setProgramMode(cmdInt);
                break;
            case HOLD:;
                sendCommand("hold", cmdStr);
                rthermData.getThermostatData().setHold(cmdInt);
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
                    //don't do anything if we are not in heat or cool mode
                    break;
                }
                sendCommand(cmdKey, cmdStr);
                break;
            default:
                logger.error("Unsupported command: {}", command.toString());
            }
            
            // update the value in the commanded channel
            updateChannel(channelUID.getId(), rthermData);
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

        String hostName = StringUtils.trimToEmpty(config.hostName);

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
    private Object getRadioThermostatData(String resource) {
        Object result = null;
        String errorMsg = null;

        String urlStr = buildRequestURL(resource);
        logger.debug("URL = {}", urlStr);

        try {
            // Run the HTTP request and get the JSON response from the thermostat
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();

            try {
                String response = IOUtils.toString(connection.getInputStream());
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
                
            } finally {
                IOUtils.closeQuietly(connection.getInputStream());
            }

            if (result != null ) {
                if (initialized == 1) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, rthermData.getName() + " " + rthermData.getModel());
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

        } catch (MalformedURLException e) {
            errorMsg = e.getMessage();
            logger.warn("Constructed url is not valid: {}", errorMsg);
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.warn("Error running thermostat request: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            errorMsg = e.getMessage();
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
    private String sendCommand(String cmdKey, String cmdVal) {
        String urlStr = buildRequestURL(DEFAULT_RESOURCE);
        String postJson = "{\""+ cmdKey + "\":" + cmdVal + "}";
        byte[] out = postJson.getBytes(StandardCharsets.US_ASCII);
        String output = null;
        String errorMsg = null;
        
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setFixedLengthStreamingMode(out.length);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.connect();
            
            OutputStream os = conn.getOutputStream();
            os.write(postJson.getBytes(StandardCharsets.US_ASCII));
            
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            try {
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new HTTPException(conn.getResponseCode());
                }
                output = br.readLine();
                
            } catch (IOException | HTTPException e) {
                logger.error("Exception occurred during execution: {}", e.getMessage(), e);
            } finally {
                br.close();
                os.close();
                conn.disconnect();
            }

        } catch (MalformedURLException e) {
            errorMsg = e.getMessage();
            logger.warn("Constructed url is not valid: {}", errorMsg);
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.warn("Error running thermostat command: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            logger.error("Exception occurred during execution: {}", e.getMessage(), e);
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
    public static Object getValue(String channelId, RadioThermostatData data) throws Exception {
        String[] fields = StringUtils.split(channelId, "#");        
        
        if (data != null) {
            switch (fields[0]) {
                case NAME:
                    return data.getName();
                case MODEL:
                    return data.getModel();
                case TEMPERATURE:
                    if (data.getThermostatData().getTemperature() != null) {
                        return new QuantityType<Temperature>(data.getThermostatData().getTemperature(), API_TEMPERATURE_UNIT);
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
                    if (data.getThermostatData().getSetpoint() != null) {
                        return new QuantityType<Temperature>(data.getThermostatData().getSetpoint(), API_TEMPERATURE_UNIT);
                    } else {
                        return null;
                    }            
                case OVERRIDE:
                    return data.getThermostatData().getOverride();
                case HOLD:
                    return data.getThermostatData().getHold();
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
                case TODAY_HEAT_HOUR:
                    return data.getRuntime().getToday().getHeatTime().getHour();
                case TODAY_HEAT_MINUTE:
                    return data.getRuntime().getToday().getHeatTime().getMinute();
                case TODAY_COOL_HOUR:
                    return data.getRuntime().getToday().getCoolTime().getHour();
                case TODAY_COOL_MINUTE:
                    return data.getRuntime().getToday().getCoolTime().getMinute();
                case YESTERDAY_HEAT_HOUR:
                    return data.getRuntime().getYesterday().getHeatTime().getHour();
                case YESTERDAY_HEAT_MINUTE:
                    return data.getRuntime().getYesterday().getHeatTime().getMinute();
                case YESTERDAY_COOL_HOUR:
                    return data.getRuntime().getYesterday().getCoolTime().getHour();
                case YESTERDAY_COOL_MINUTE:
                    return data.getRuntime().getYesterday().getCoolTime().getMinute();       
            }
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
        
        fanModeOptions.add(new StateOption("0","Auto"));
        if (isCT80()) {
            fanModeOptions.add(new StateOption("1","Auto/Circulate"));
        }
        fanModeOptions.add(new StateOption("2","On"));
        
        return fanModeOptions;
    }

}

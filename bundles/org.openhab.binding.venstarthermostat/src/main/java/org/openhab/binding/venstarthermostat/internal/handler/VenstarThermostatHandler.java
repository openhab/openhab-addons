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
package org.openhab.binding.venstarthermostat.internal.handler;

import static org.openhab.binding.venstarthermostat.internal.VenstarThermostatBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.venstarthermostat.internal.VenstarThermostatConfiguration;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarAwayMode;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarAwayModeSerializer;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarFanMode;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarFanModeSerializer;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarFanState;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarFanStateSerializer;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarInfoData;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarResponse;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarRuntime;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarRuntimeData;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarScheduleMode;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarScheduleModeSerializer;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSchedulePart;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSchedulePartSerializer;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSensor;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSensorData;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSystemMode;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSystemModeSerializer;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSystemState;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSystemStateSerializer;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link VenstarThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author William Welliver - Initial contribution
 * @author Dan Cunningham - Migration to Jetty, annotations and various improvements
 * @author Matthew Davies - added code to include away mode in binding
 */
@NonNullByDefault
public class VenstarThermostatHandler extends ConfigStatusThingHandler {
    private static final int TIMEOUT_SECONDS = 30;
    private static final int UPDATE_AFTER_COMMAND_SECONDS = 2;
    private Logger log = LoggerFactory.getLogger(VenstarThermostatHandler.class);
    private List<VenstarSensor> sensorData = new ArrayList<>();
    private VenstarInfoData infoData = new VenstarInfoData();
    private VenstarRuntimeData runtimeData = new VenstarRuntimeData();
    private Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());
    private @Nullable Future<?> updatesTask;
    private @Nullable URL baseURL;
    private int refresh;
    private final HttpClient httpClient;
    private final Gson gson;

    // Venstar Thermostats are most commonly installed in the US, so start with a reasonable default.
    private Unit<Temperature> unitSystem = ImperialUnits.FAHRENHEIT;

    public VenstarThermostatHandler(Thing thing) {
        super(thing);
        httpClient = new HttpClient(new SslContextFactory.Client(true));
        gson = new GsonBuilder().registerTypeAdapter(VenstarSystemState.class, new VenstarSystemStateSerializer())
                .registerTypeAdapter(VenstarSystemMode.class, new VenstarSystemModeSerializer())
                .registerTypeAdapter(VenstarAwayMode.class, new VenstarAwayModeSerializer())
                .registerTypeAdapter(VenstarFanMode.class, new VenstarFanModeSerializer())
                .registerTypeAdapter(VenstarFanState.class, new VenstarFanStateSerializer())
                .registerTypeAdapter(VenstarScheduleMode.class, new VenstarScheduleModeSerializer())
                .registerTypeAdapter(VenstarSchedulePart.class, new VenstarSchedulePartSerializer()).create();

        log.trace("VenstarThermostatHandler for thing {}", getThing().getUID());
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> status = new ArrayList<>();
        VenstarThermostatConfiguration config = getConfigAs(VenstarThermostatConfiguration.class);
        if (config.username.isBlank()) {
            log.warn("username is empty");
            status.add(ConfigStatusMessage.Builder.error(CONFIG_USERNAME).withMessageKeySuffix(EMPTY_INVALID)
                    .withArguments(CONFIG_USERNAME).build());
        }

        if (config.password.isBlank()) {
            log.warn("password is empty");
            status.add(ConfigStatusMessage.Builder.error(CONFIG_PASSWORD).withMessageKeySuffix(EMPTY_INVALID)
                    .withArguments(CONFIG_PASSWORD).build());
        }

        if (config.refresh < 10) {
            log.warn("refresh is too small: {}", config.refresh);

            status.add(ConfigStatusMessage.Builder.error(CONFIG_REFRESH).withMessageKeySuffix(REFRESH_INVALID)
                    .withArguments(CONFIG_REFRESH).build());
        }
        return status;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            log.debug("Controller is NOT ONLINE and is not responding to commands");
            return;
        }

        stopUpdateTasks();
        if (command instanceof RefreshType) {
            log.debug("Refresh command requested for {}", channelUID);
            stateMap.clear();
            startUpdatesTask(0);
        } else {
            stateMap.remove(channelUID.getAsString());
            if (channelUID.getId().equals(CHANNEL_HEATING_SETPOINT)) {
                QuantityType<Temperature> quantity = commandToQuantityType(command, unitSystem);
                double value = quantityToRoundedTemperature(quantity, unitSystem).doubleValue();
                log.debug("Setting heating setpoint to {}", value);
                setHeatingSetpoint(value);
            } else if (channelUID.getId().equals(CHANNEL_COOLING_SETPOINT)) {
                QuantityType<Temperature> quantity = commandToQuantityType(command, unitSystem);
                double value = quantityToRoundedTemperature(quantity, unitSystem).doubleValue();
                log.debug("Setting cooling setpoint to {}", value);
                setCoolingSetpoint(value);
            } else if (channelUID.getId().equals(CHANNEL_SYSTEM_MODE)) {
                VenstarSystemMode value;
                try {
                    if (command instanceof StringType) {
                        value = VenstarSystemMode.valueOf(((StringType) command).toString().toUpperCase());
                    } else {
                        value = VenstarSystemMode.fromInt(((DecimalType) command).intValue());
                    }
                    log.debug("Setting system mode to  {}", value);
                    setSystemMode(value);
                    updateIfChanged(CHANNEL_SYSTEM_MODE_RAW, new StringType(value.toString()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid System Mode");
                }
            } else if (channelUID.getId().equals(CHANNEL_AWAY_MODE)) {
                VenstarAwayMode value;
                try {
                    if (command instanceof StringType) {
                        value = VenstarAwayMode.valueOf(((StringType) command).toString().toUpperCase());
                    } else {
                        value = VenstarAwayMode.fromInt(((DecimalType) command).intValue());
                    }
                    log.debug("Setting away mode to  {}", value);
                    setAwayMode(value);
                    updateIfChanged(CHANNEL_AWAY_MODE_RAW, new StringType(value.toString()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid Away Mode");
                }

            } else if (channelUID.getId().equals(CHANNEL_FAN_MODE)) {
                VenstarFanMode value;
                try {
                    if (command instanceof StringType) {
                        value = VenstarFanMode.valueOf(((StringType) command).toString().toUpperCase());
                    } else {
                        value = VenstarFanMode.fromInt(((DecimalType) command).intValue());
                    }
                    log.debug("Setting fan mode to  {}", value);
                    setFanMode(value);
                    updateIfChanged(CHANNEL_FAN_MODE_RAW, new StringType(value.toString()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid Fan Mode");
                }
            } else if (channelUID.getId().equals(CHANNEL_SCHEDULE_MODE)) {
                VenstarScheduleMode value;
                try {
                    if (command instanceof StringType) {
                        value = VenstarScheduleMode.valueOf(((StringType) command).toString().toUpperCase());
                    } else {
                        value = VenstarScheduleMode.fromInt(((DecimalType) command).intValue());
                    }
                    log.debug("Setting schedule mode to  {}", value);
                    setScheduleMode(value);
                    updateIfChanged(CHANNEL_SCHEDULE_MODE_RAW, new StringType(value.toString()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid Schedule Mode");
                }
            }

            startUpdatesTask(UPDATE_AFTER_COMMAND_SECONDS);
        }
    }

    @Override
    public void dispose() {
        stopUpdateTasks();
        if (httpClient.isStarted()) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                log.debug("Could not stop HttpClient", e);
            }
        }
    }

    @Override
    public void initialize() {
        connect();
    }

    protected void goOnline() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void goOffline(ThingStatusDetail detail, String reason) {
        if (getThing().getStatus() != ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, detail, reason);
        }
    }

    @SuppressWarnings("null") // compiler does not see new URL(url) as never being null
    private void connect() {
        stopUpdateTasks();
        VenstarThermostatConfiguration config = getConfigAs(VenstarThermostatConfiguration.class);
        try {
            baseURL = new URL(config.url);
            if (!httpClient.isStarted()) {
                httpClient.start();
            }
            httpClient.getAuthenticationStore().clearAuthentications();
            httpClient.getAuthenticationStore().clearAuthenticationResults();
            httpClient.getAuthenticationStore().addAuthentication(
                    new DigestAuthentication(baseURL.toURI(), "thermostat", config.username, config.password));
            refresh = config.refresh;
            startUpdatesTask(0);
        } catch (Exception e) {
            log.debug("Could not conntect to URL  {}", config.url, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    /**
     * Start the poller after an initial delay
     *
     * @param initialDelay
     */
    private synchronized void startUpdatesTask(int initialDelay) {
        stopUpdateTasks();
        updatesTask = scheduler.scheduleWithFixedDelay(this::updateData, initialDelay, refresh, TimeUnit.SECONDS);
    }

    /**
     * Stop the poller
     */
    @SuppressWarnings("null")
    private void stopUpdateTasks() {
        Future<?> localUpdatesTask = updatesTask;
        if (isFutureValid(localUpdatesTask)) {
            localUpdatesTask.cancel(false);
        }
    }

    private boolean isFutureValid(@Nullable Future<?> future) {
        return future != null && !future.isCancelled();
    }

    private State getTemperature() {
        Optional<VenstarSensor> optSensor = sensorData.stream()
                .filter(sensor -> sensor.getName().equalsIgnoreCase("Thermostat")).findAny();
        if (optSensor.isPresent()) {
            return new QuantityType<Temperature>(optSensor.get().getTemp(), unitSystem);
        }

        return UnDefType.UNDEF;
    }

    private State getHumidity() {
        Optional<VenstarSensor> optSensor = sensorData.stream()
                .filter(sensor -> sensor.getName().equalsIgnoreCase("Thermostat")).findAny();
        if (optSensor.isPresent()) {
            return new QuantityType<Dimensionless>(optSensor.get().getHum(), Units.PERCENT);
        }

        return UnDefType.UNDEF;
    }

    private State getOutdoorTemperature() {
        Optional<VenstarSensor> optSensor = sensorData.stream()
                .filter(sensor -> sensor.getName().equalsIgnoreCase("Outdoor")).findAny();
        if (optSensor.isPresent()) {
            return new QuantityType<Temperature>(optSensor.get().getTemp(), unitSystem);
        }

        return UnDefType.UNDEF;
    }

    private void setCoolingSetpoint(double cool) {
        double heat = getHeatingSetpoint().doubleValue();
        VenstarSystemMode mode = infoData.getSystemMode();
        VenstarFanMode fanmode = infoData.getFanMode();
        updateControls(heat, cool, mode, fanmode);
    }

    private void setSystemMode(VenstarSystemMode mode) {
        double cool = getCoolingSetpoint().doubleValue();
        double heat = getHeatingSetpoint().doubleValue();
        VenstarFanMode fanmode = infoData.getFanMode();
        updateControls(heat, cool, mode, fanmode);
    }

    private void setHeatingSetpoint(double heat) {
        double cool = getCoolingSetpoint().doubleValue();
        VenstarSystemMode mode = infoData.getSystemMode();
        VenstarFanMode fanmode = infoData.getFanMode();
        updateControls(heat, cool, mode, fanmode);
    }

    private void setFanMode(VenstarFanMode fanmode) {
        double cool = getCoolingSetpoint().doubleValue();
        double heat = getHeatingSetpoint().doubleValue();
        VenstarSystemMode mode = infoData.getSystemMode();
        updateControls(heat, cool, mode, fanmode);
    }

    private void setAwayMode(VenstarAwayMode away) {
        // This function updates the away mode via a POST to the thermostat's local API's /settings endpoint.
        //
        // The /settings endpoint supports a number of additional parameters (tempunits, de/humedifier
        // setpoints, etc). However, newer Venstar firmwares will reject any POST to /settings that
        // contains a `schedule` parameter when the thermostat is currently in away mode.
        //
        // Separating the updates to change `schedule` and `away` ensures that the thermostat will not
        // reject attempts to un-set away mode due to the presence of the `schedule` parameter.
        Map<String, String> params = new HashMap<>();
        params.put("away", String.valueOf(away.mode()));
        VenstarResponse res = updateThermostat("/settings", params);
        if (res != null) {
            log.debug("Updated thermostat");
            // update our local copy until the next refresh occurs
            infoData.setAwayMode(away);
        }
    }

    private void setScheduleMode(VenstarScheduleMode schedule) {
        // This function updates the schedule mode via a POST to the thermostat's local API's /settings endpoint.
        //
        // The /settings endpoint supports a number of additional parameters (tempunits, de/humedifier
        // setpoints, etc). However, newer Venstar firmwares will reject any POST to /settings that
        // contains a `schedule` parameter when the thermostat is currently in away mode.
        //
        // Separating the updates to change `schedule` and `away` ensures that the thermostat will not
        // reject attempts to un-set away mode due to the presence of the `schedule` parameter.
        Map<String, String> params = new HashMap<>();
        params.put("schedule", String.valueOf(schedule.mode()));
        VenstarResponse res = updateThermostat("/settings", params);
        if (res != null) {
            log.debug("Updated thermostat");
            // update our local copy until the next refresh occurs
            infoData.setScheduleMode(schedule);
            // add other parameters here in the same way
        }
    }

    private QuantityType<Temperature> getCoolingSetpoint() {
        return new QuantityType<Temperature>(infoData.getCooltemp(), unitSystem);
    }

    private QuantityType<Temperature> getHeatingSetpoint() {
        return new QuantityType<Temperature>(infoData.getHeattemp(), unitSystem);
    }

    private ZonedDateTime getTimestampRuntime(VenstarRuntime runtime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = LocalDateTime.now().atZone(zoneId);
        int diff = now.getOffset().getTotalSeconds();
        ZonedDateTime z = ZonedDateTime.ofInstant(Instant.ofEpochSecond(runtime.getTimeStamp() - diff), zoneId);
        return z;
    }

    private void updateScheduleMode(VenstarScheduleMode schedule) {
    }

    private void updateControls(double heat, double cool, VenstarSystemMode mode, VenstarFanMode fanmode) {
        // this function corresponds to the thermostat local API POST /control instruction
        // the function can be expanded with other parameters which are changed via POST /control
        // controls that can be included are thermostat mode, fan mode, heat temp, cool temp (all done already)
        Map<String, String> params = new HashMap<>();
        if (heat > 0) {
            params.put("heattemp", String.valueOf(heat));
        }
        if (cool > 0) {
            params.put("cooltemp", String.valueOf(cool));
        }
        params.put("mode", String.valueOf(mode.mode()));
        params.put("fan", String.valueOf(fanmode.mode()));
        VenstarResponse res = updateThermostat("/control", params);
        if (res != null) {
            log.debug("Updated thermostat");
            // update our local copy until the next refresh occurs
            infoData.setCooltemp(cool);
            infoData.setHeattemp(heat);
            infoData.setSystemMode(mode);
            infoData.setFanMode(fanmode);
            // add other parameters here in the same way
        }
    }

    /**
     * Function to send data to the thermostat and update the Thing state if there is an error
     *
     * @param path
     * @param params
     * @return VenstarResponse object or null if there was an error
     */
    private @Nullable VenstarResponse updateThermostat(String path, Map<String, String> params) {
        try {
            String result = postData(path, params);
            VenstarResponse res = gson.fromJson(result, VenstarResponse.class);
            if (res != null && res.isSuccess()) {
                return res;
            } else {
                String reason = res == null ? "invalid response" : res.getReason();
                log.debug("Failed to update thermostat: {}", reason);
                goOffline(ThingStatusDetail.COMMUNICATION_ERROR, reason);
            }
        } catch (VenstarCommunicationException | JsonSyntaxException e) {
            log.debug("Unable to fetch info data", e);
            String message = e.getMessage();
            goOffline(ThingStatusDetail.COMMUNICATION_ERROR, message != null ? message : "");
        } catch (VenstarAuthenticationException e) {
            goOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Authorization Failed");
        }
        return null;
    }

    private void updateData() {
        try {
            Future<?> localUpdatesTask = updatesTask;
            String response = getData("/query/sensors");
            if (!isFutureValid(localUpdatesTask)) {
                return;
            }
            VenstarSensorData res = gson.fromJson(response, VenstarSensorData.class);
            sensorData = res.getSensors();
            updateIfChanged(CHANNEL_TEMPERATURE, getTemperature());
            updateIfChanged(CHANNEL_EXTERNAL_TEMPERATURE, getOutdoorTemperature());
            updateIfChanged(CHANNEL_HUMIDITY, getHumidity());

            response = getData("/query/runtimes");
            if (!isFutureValid(localUpdatesTask)) {
                return;
            }

            runtimeData = Objects.requireNonNull(gson.fromJson(response, VenstarRuntimeData.class));
            List<VenstarRuntime> runtimes = runtimeData.getRuntimes();
            Collections.reverse(runtimes);// reverse the list so that the most recent runtime data is first in the list
            int nRuntimes = Math.min(7, runtimes.size());// check how many runtimes are available, might be less than
                                                         // seven if equipment
                                                         // was reset, and also might be more than 7, so limit to 7
            for (int i = 0; i < nRuntimes; i++) {
                VenstarRuntime rt = runtimes.get(i);
                updateIfChanged(CHANNEL_TIMESTAMP_RUNTIME_DAY + i, new DateTimeType(getTimestampRuntime(rt)));
                updateIfChanged(CHANNEL_HEAT1_RUNTIME_DAY + i, new DecimalType(rt.getHeat1Runtime()));
                updateIfChanged(CHANNEL_HEAT2_RUNTIME_DAY + i, new DecimalType(rt.getHeat2Runtime()));
                updateIfChanged(CHANNEL_COOL1_RUNTIME_DAY + i, new DecimalType(rt.getCool1Runtime()));
                updateIfChanged(CHANNEL_COOL2_RUNTIME_DAY + i, new DecimalType(rt.getCool2Runtime()));
                updateIfChanged(CHANNEL_AUX1_RUNTIME_DAY + i, new DecimalType(rt.getAux1Runtime()));
                updateIfChanged(CHANNEL_AUX2_RUNTIME_DAY + i, new DecimalType(rt.getAux2Runtime()));
                updateIfChanged(CHANNEL_FC_RUNTIME_DAY + i, new DecimalType(rt.getFreeCoolRuntime()));
            }

            response = getData("/query/info");
            if (!isFutureValid(localUpdatesTask)) {
                return;
            }
            infoData = Objects.requireNonNull(gson.fromJson(response, VenstarInfoData.class));
            updateUnits(infoData);
            updateIfChanged(CHANNEL_HEATING_SETPOINT, getHeatingSetpoint());
            updateIfChanged(CHANNEL_COOLING_SETPOINT, getCoolingSetpoint());
            updateIfChanged(CHANNEL_SYSTEM_STATE, new StringType(infoData.getSystemState().stateName()));
            updateIfChanged(CHANNEL_SYSTEM_MODE, new StringType(infoData.getSystemMode().modeName()));
            updateIfChanged(CHANNEL_SYSTEM_STATE_RAW, new DecimalType(infoData.getSystemState().state()));
            updateIfChanged(CHANNEL_SYSTEM_MODE_RAW, new DecimalType(infoData.getSystemMode().mode()));
            updateIfChanged(CHANNEL_AWAY_MODE, new StringType(infoData.getAwayMode().modeName()));
            updateIfChanged(CHANNEL_AWAY_MODE_RAW, new DecimalType(infoData.getAwayMode().mode()));
            updateIfChanged(CHANNEL_FAN_MODE, new StringType(infoData.getFanMode().modeName()));
            updateIfChanged(CHANNEL_FAN_MODE_RAW, new DecimalType(infoData.getFanMode().mode()));
            updateIfChanged(CHANNEL_FAN_STATE, OnOffType.from(infoData.getFanState().stateName()));
            updateIfChanged(CHANNEL_FAN_STATE_RAW, new DecimalType(infoData.getFanState().state()));
            updateIfChanged(CHANNEL_SCHEDULE_MODE, new StringType(infoData.getScheduleMode().modeName()));
            updateIfChanged(CHANNEL_SCHEDULE_MODE_RAW, new DecimalType(infoData.getScheduleMode().mode()));
            updateIfChanged(CHANNEL_SCHEDULE_PART, new StringType(infoData.getSchedulePart().partName()));
            updateIfChanged(CHANNEL_SCHEDULE_PART_RAW, new DecimalType(infoData.getSchedulePart().part()));

            goOnline();
        } catch (VenstarCommunicationException | JsonSyntaxException e) {
            log.debug("Unable to fetch info data", e);
            String message = e.getMessage();
            goOffline(ThingStatusDetail.COMMUNICATION_ERROR, message != null ? message : "");
        } catch (VenstarAuthenticationException e) {
            goOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Authorization Failed");
        }
    }

    private void updateIfChanged(String channelID, State state) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelID);
        State oldState = stateMap.put(channelUID.toString(), state);
        if (!state.equals(oldState)) {
            log.trace("updating channel {} with state {} (old state {})", channelUID, state, oldState);
            updateState(channelUID, state);
        }
    }

    private void updateUnits(VenstarInfoData infoData) {
        int tempunits = infoData.getTempunits();
        if (tempunits == 0) {
            unitSystem = ImperialUnits.FAHRENHEIT;
        } else if (tempunits == 1) {
            unitSystem = SIUnits.CELSIUS;
        } else {
            log.warn("Thermostat returned unknown unit system type: {}", tempunits);
        }
    }

    private String getData(String path) throws VenstarAuthenticationException, VenstarCommunicationException {
        try {
            URL getURL = new URL(baseURL, path);
            Request request = httpClient.newRequest(getURL.toURI()).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return sendRequest(request);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new VenstarCommunicationException(e);
        }
    }

    private String postData(String path, Map<String, String> params)
            throws VenstarAuthenticationException, VenstarCommunicationException {
        try {
            URL postURL = new URL(baseURL, path);
            Request request = httpClient.newRequest(postURL.toURI()).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .method(HttpMethod.POST);
            params.forEach(request::param);
            return sendRequest(request);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new VenstarCommunicationException(e);
        }
    }

    private String sendRequest(Request request) throws VenstarAuthenticationException, VenstarCommunicationException {
        log.trace("sendRequest: requesting {}", request.getURI());
        try {
            ContentResponse response = request.send();
            log.trace("Response code {}", response.getStatus());
            if (response.getStatus() == 401) {
                throw new VenstarAuthenticationException();
            }

            if (response.getStatus() != 200) {
                throw new VenstarCommunicationException(
                        "Error communicating with thermostat. Error Code: " + response.getStatus());
            }
            String content = response.getContentAsString();
            log.trace("sendRequest: response {}", content);
            return content;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new VenstarCommunicationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <U extends Quantity<U>> QuantityType<U> commandToQuantityType(Command command, Unit<U> defaultUnit) {
        if (command instanceof QuantityType) {
            return (QuantityType<U>) command;
        }
        return new QuantityType<U>(new BigDecimal(command.toString()), defaultUnit);
    }

    protected DecimalType commandToDecimalType(Command command) {
        if (command instanceof DecimalType) {
            return (DecimalType) command;
        }
        return new DecimalType(new BigDecimal(command.toString()));
    }

    private BigDecimal quantityToRoundedTemperature(QuantityType<Temperature> quantity, Unit<Temperature> unit)
            throws IllegalArgumentException {
        QuantityType<Temperature> temparatureQuantity = quantity.toUnit(unit);
        if (temparatureQuantity == null) {
            return quantity.toBigDecimal();
        }

        BigDecimal value = temparatureQuantity.toBigDecimal();
        BigDecimal increment = CELSIUS == unit ? new BigDecimal("0.5") : new BigDecimal("1");
        BigDecimal divisor = value.divide(increment, 0, RoundingMode.HALF_UP);
        return divisor.multiply(increment);
    }

    @SuppressWarnings("serial")
    private class VenstarAuthenticationException extends Exception {
        public VenstarAuthenticationException() {
            super("Invalid Credentials");
        }
    }

    @SuppressWarnings("serial")
    private class VenstarCommunicationException extends Exception {
        public VenstarCommunicationException(Exception e) {
            super(e);
        }

        public VenstarCommunicationException(String message) {
            super(message);
        }
    }
}

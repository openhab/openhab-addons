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
package org.openhab.binding.venstarthermostat.internal.handler;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.binding.venstarthermostat.internal.VenstarThermostatBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.venstarthermostat.internal.VenstarThermostatConfiguration;
import org.openhab.binding.venstarthermostat.internal.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link VenstarThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author William Welliver - Initial contribution
 * @author Dan Cunningham - Migration to Jetty, annotations and various improvements
 */
@NonNullByDefault
public class VenstarThermostatHandler extends ConfigStatusThingHandler {

    private static final int TIMEOUT_SECONDS = 30;
    private static final int UPDATE_AFTER_COMMAND_SECONDS = 2;

    private Logger log = LoggerFactory.getLogger(VenstarThermostatHandler.class);
    private List<VenstarSensor> sensorData = new ArrayList<>();
    private VenstarInfoData infoData = new VenstarInfoData();
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
        httpClient = new HttpClient(new SslContextFactory(true));
        gson = new GsonBuilder().registerTypeAdapter(VenstarSystemState.class, new VenstarSystemStateSerializer())
                .registerTypeAdapter(VenstarSystemMode.class, new VenstarSystemModeSerializer())
                .create();

        log.trace("VenstarThermostatHandler for thing {}", getThing().getUID());
    }

    @SuppressWarnings("null") // compiler does not see conf.refresh == null check
    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> status = new ArrayList<>();
        VenstarThermostatConfiguration config = getConfigAs(VenstarThermostatConfiguration.class);
        if (StringUtils.isBlank(config.username)) {
            log.warn("username is empty");
            status.add(ConfigStatusMessage.Builder.error(CONFIG_USERNAME).withMessageKeySuffix(EMPTY_INVALID)
                    .withArguments(CONFIG_USERNAME).build());
        }

        if (StringUtils.isBlank(config.password)) {
            log.warn("password is empty");
            status.add(ConfigStatusMessage.Builder.error(CONFIG_PASSWORD).withMessageKeySuffix(EMPTY_INVALID)
                    .withArguments(CONFIG_PASSWORD).build());
        }

        if (config.refresh == null || config.refresh < 10) {
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
                int value = quantityToRoundedTemperature(quantity, unitSystem).intValue();

                log.debug("Setting heating setpoint to {}", value);
                setHeatingSetpoint(value);
            } else if (channelUID.getId().equals(CHANNEL_COOLING_SETPOINT)) {
                QuantityType<Temperature> quantity = commandToQuantityType(command, unitSystem);
                int value = quantityToRoundedTemperature(quantity, unitSystem).intValue();

                log.debug("Setting cooling setpoint to {}", value);
                setCoolingSetpoint(value);
            } else if (channelUID.getId().equals(CHANNEL_SYSTEM_MODE)) {
                VenstarSystemMode value;
                if (command instanceof StringType) {
                    value = VenstarSystemMode.valueOf(((StringType)command).toString().toUpperCase());

                } else {
                    value = VenstarSystemMode.fromInt(((DecimalType)command).intValue());
                }
                log.debug("Setting system mode to  {}", value);
                setSystemMode(value);
                updateIfChanged(CHANNEL_SYSTEM_MODE_RAW, new StringType("" + value));

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
        String url = getThing().getProperties().get(PROPERTY_URL);
        try {
            baseURL = new URL(url);
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
            log.debug("Could not conntect to URL  {}", url, e);
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
            return new QuantityType<Dimensionless>(optSensor.get().getHum(), SmartHomeUnits.PERCENT);
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

    private void setCoolingSetpoint(int cool) {
        int heat = getHeatingSetpoint().intValue();
        VenstarSystemMode mode = getSystemMode();

        updateThermostat(heat, cool, mode);
    }

    private void setSystemMode(VenstarSystemMode mode) {
        int cool = getCoolingSetpoint().intValue();
        int heat = getHeatingSetpoint().intValue();

        updateThermostat(heat, cool, mode);
    }

    private void setHeatingSetpoint(int heat) {
        int cool = getCoolingSetpoint().intValue();
        VenstarSystemMode mode = getSystemMode();

        updateThermostat(heat, cool, mode);
    }

    private QuantityType<Temperature> getCoolingSetpoint() {
        return new QuantityType<Temperature>(infoData.getCooltemp(), unitSystem);
    }

    private QuantityType<Temperature> getHeatingSetpoint() {
        return new QuantityType<Temperature>(infoData.getHeattemp(), unitSystem);
    }

    private VenstarSystemState getSystemState() {
        return infoData.getState();
    }

    private VenstarSystemMode getSystemMode() {
        return infoData.getMode();
    }

    private void updateThermostat(int heat, int cool, VenstarSystemMode mode) {
        Map<String, String> params = new HashMap<>();
        log.debug("Updating thermostat {}  heat:{} cool {} mode: {}", getThing().getLabel(), heat, cool, mode);
        if (heat > 0) {
            params.put("heattemp", String.valueOf(heat));
        }
        if (cool > 0) {
            params.put("cooltemp", String.valueOf(cool));
        }
        params.put("mode", "" + mode.mode());
        try {
            String result = postData("/control", params);
            VenstarResponse res = gson.fromJson(result, VenstarResponse.class);
            if (res.isSuccess()) {
                log.debug("Updated thermostat");
                // update our local copy until the next refresh occurs
                infoData = new VenstarInfoData(cool, heat, infoData.getState(), mode);
            } else {
                log.debug("Failed to update thermostat: {}", res.getReason());
                goOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Thermostat update failed: " + res.getReason());
            }
        } catch (VenstarCommunicationException | JsonSyntaxException e) {
            log.debug("Unable to fetch info data", e);
            goOffline(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (VenstarAuthenticationException e) {
            goOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Authorization Failed");
        }
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

            response = getData("/query/info");
            if (!isFutureValid(localUpdatesTask)) {
                return;
            }
            infoData = gson.fromJson(response, VenstarInfoData.class);
            updateUnits(infoData);
            updateIfChanged(CHANNEL_HEATING_SETPOINT, getHeatingSetpoint());
            updateIfChanged(CHANNEL_COOLING_SETPOINT, getCoolingSetpoint());
            updateIfChanged(CHANNEL_SYSTEM_STATE, new StringType(getSystemState().stateName()));
            updateIfChanged(CHANNEL_SYSTEM_MODE, new StringType(getSystemMode().modeName()));
            updateIfChanged(CHANNEL_SYSTEM_STATE_RAW, new DecimalType(getSystemState().state()));
            updateIfChanged(CHANNEL_SYSTEM_MODE_RAW, new DecimalType(getSystemMode().mode()));

            goOnline();
        } catch (VenstarCommunicationException | JsonSyntaxException e) {
            log.debug("Unable to fetch info data", e);
            goOffline(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
                        "Error communitcating with thermostat. Error Code: " + response.getStatus());
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

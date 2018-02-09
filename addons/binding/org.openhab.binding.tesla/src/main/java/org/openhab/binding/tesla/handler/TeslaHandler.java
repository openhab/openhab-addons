/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.handler;

import static org.openhab.binding.tesla.TeslaBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.glassfish.jersey.client.ClientProperties;
import org.openhab.binding.tesla.TeslaBindingConstants;
import org.openhab.binding.tesla.TeslaBindingConstants.EventKeys;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.binding.tesla.internal.protocol.ChargeState;
import org.openhab.binding.tesla.internal.protocol.ClimateState;
import org.openhab.binding.tesla.internal.protocol.DriveState;
import org.openhab.binding.tesla.internal.protocol.GUIState;
import org.openhab.binding.tesla.internal.protocol.TokenRequest;
import org.openhab.binding.tesla.internal.protocol.TokenRequestPassword;
import org.openhab.binding.tesla.internal.protocol.TokenRequestRefreshToken;
import org.openhab.binding.tesla.internal.protocol.TokenResponse;
import org.openhab.binding.tesla.internal.protocol.Vehicle;
import org.openhab.binding.tesla.internal.protocol.VehicleState;
import org.openhab.binding.tesla.internal.throttler.QueueChannelThrottler;
import org.openhab.binding.tesla.internal.throttler.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TeslaHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 */
public class TeslaHandler extends BaseThingHandler {

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 3000;
    private static final int EVENT_STREAM_READ_TIMEOUT = 200000;
    private static final int EVENT_TIMESTAMP_AGE_LIMIT = 3000;
    private static final int EVENT_TIMESTAMP_MAX_DELTA = 10000;
    private static final int FAST_STATUS_REFRESH_INTERVAL = 15000;
    private static final int SLOW_STATUS_REFRESH_INTERVAL = 60000;
    private static final int CONNECT_RETRY_INTERVAL = 15000;
    private static final int MAXIMUM_ERRORS_IN_INTERVAL = 2;
    private static final int ERROR_INTERVAL_SECONDS = 15;

    private Logger logger = LoggerFactory.getLogger(TeslaHandler.class);

    // Vehicle state variables
    protected Vehicle vehicle;
    protected String vehicleJSON;
    protected DriveState driveState;
    protected GUIState guiState;
    protected VehicleState vehicleState;
    protected ChargeState chargeState;
    protected ClimateState climateState;

    // REST Client API variables
    protected final Client teslaClient = ClientBuilder.newClient();
    protected Client eventClient = ClientBuilder.newClient();
    public final WebTarget teslaTarget = teslaClient.target(TESLA_OWNERS_URI);
    public final WebTarget tokenTarget = teslaTarget.path(TESLA_ACCESS_TOKEN_URI);
    public final WebTarget vehiclesTarget = teslaTarget.path(API_VERSION).path(VEHICLES);
    public final WebTarget vehicleTarget = vehiclesTarget.path(VEHICLE_ID_PATH);
    public final WebTarget dataRequestTarget = vehicleTarget.path(DATA_REQUEST_PATH);
    public final WebTarget commandTarget = vehicleTarget.path(COMMAND_PATH);
    protected WebTarget eventTarget;

    // Threading and Job related variables
    protected ScheduledFuture<?> connectJob;
    protected Thread eventThread;
    protected ScheduledFuture<?> fastStateJob;
    protected ScheduledFuture<?> slowStateJob;
    protected QueueChannelThrottler stateThrottler;

    protected long lastTimeStamp;
    protected long intervalTimestamp = 0;
    protected int intervalErrors = 0;
    protected ReentrantLock lock;

    private StorageService storageService;
    protected Gson gson = new Gson();
    protected TeslaChannelSelectorProxy teslaChannelSelectorProxy = new TeslaChannelSelectorProxy();
    private TokenResponse logonToken;

    public TeslaHandler(Thing thing, StorageService storageService) {
        super(thing);
        this.storageService = storageService;
    }

    @Override
    public void initialize() {

        logger.trace("Initializing the Tesla handler for {}", this.getStorageKey());

        updateStatus(ThingStatus.UNKNOWN);

        lock = new ReentrantLock();

        lock.lock();
        try {
            if (connectJob == null || connectJob.isCancelled()) {
                connectJob = scheduler.scheduleWithFixedDelay(connectRunnable, 0, CONNECT_RETRY_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            eventThread = new Thread(eventRunnable, "ESH-Tesla-Event Stream-" + getThing().getUID());
            eventThread.start();

            Map<Object, Rate> channels = new HashMap<Object, Rate>();
            channels.put(TESLA_DATA_THROTTLE, new Rate(1, 1, TimeUnit.SECONDS));
            channels.put(TESLA_COMMAND_THROTTLE, new Rate(20, 1, TimeUnit.MINUTES));

            Rate firstRate = new Rate(20, 1, TimeUnit.MINUTES);
            Rate secondRate = new Rate(200, 10, TimeUnit.MINUTES);
            stateThrottler = new QueueChannelThrottler(firstRate, scheduler, channels);
            stateThrottler.addRate(secondRate);

            if (fastStateJob == null || fastStateJob.isCancelled()) {
                fastStateJob = scheduler.scheduleWithFixedDelay(fastStateRunnable, 0, FAST_STATUS_REFRESH_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            if (slowStateJob == null || slowStateJob.isCancelled()) {
                slowStateJob = scheduler.scheduleWithFixedDelay(slowStateRunnable, 0, SLOW_STATUS_REFRESH_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {

        logger.trace("Disposing the Tesla handler for {}", getThing().getUID());

        lock.lock();
        try {
            if (fastStateJob != null && !fastStateJob.isCancelled()) {
                fastStateJob.cancel(true);
                fastStateJob = null;
            }

            if (slowStateJob != null && !slowStateJob.isCancelled()) {
                slowStateJob.cancel(true);
                slowStateJob = null;
            }

            if (!eventThread.isInterrupted()) {
                eventThread.interrupt();
                eventThread = null;
            }

            if (connectJob != null && !connectJob.isCancelled()) {
                connectJob.cancel(true);
                connectJob = null;
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        String channelID = channelUID.getId();
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromChannelID(channelID);

        if (command instanceof RefreshType) {
            if (isAwake()) {
                // Request the state of all known variables. This is sub-optimal, but the requests get scheduled and
                // throttled so we are safe not to break the Tesla SLA
                requestData(TESLA_DRIVE_STATE);
                requestData(TESLA_VEHICLE_STATE);
                requestData(TESLA_CHARGE_STATE);
                requestData(TESLA_CLIMATE_STATE);
                requestData(TESLA_GUI_STATE);
            }
        } else {
            if (selector != null) {
                try {
                    switch (selector) {
                        case CHARGE_LIMIT_SOC: {
                            if (command instanceof PercentType) {
                                setChargeLimit(((PercentType) command).intValue());
                            } else if (command instanceof OnOffType && command == OnOffType.ON) {
                                setChargeLimit(100);
                            } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                                setChargeLimit(0);
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.INCREASE) {
                                setChargeLimit(Math.min(chargeState.charge_limit_soc + 1, 100));
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.DECREASE) {
                                setChargeLimit(Math.max(chargeState.charge_limit_soc - 1, 0));
                            }
                            break;
                        }
                        case TEMPERATURE: {
                            if (command instanceof DecimalType) {
                                if (getThing().getProperties().containsKey("temperatureunits")
                                        && getThing().getProperties().get("temperatureunits").equals("F")) {
                                    float fTemp = ((DecimalType) command).floatValue();
                                    float cTemp = ((fTemp - 32.0f) * 5.0f / 9.0f);
                                    setTemperature(cTemp);
                                } else {
                                    setTemperature(((DecimalType) command).floatValue());
                                }
                            }
                            break;
                        }
                        case SUN_ROOF_STATE: {
                            if (command instanceof StringType) {
                                setSunroof(command.toString());
                            }
                            break;
                        }
                        case SUN_ROOF: {
                            if (command instanceof PercentType) {
                                moveSunroof(((PercentType) command).intValue());
                            } else if (command instanceof OnOffType && command == OnOffType.ON) {
                                moveSunroof(100);
                            } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                                moveSunroof(0);
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.INCREASE) {
                                moveSunroof(Math.min(chargeState.charge_limit_soc + 1, 100));
                            } else if (command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.DECREASE) {
                                moveSunroof(Math.max(chargeState.charge_limit_soc - 1, 0));
                            }
                            break;
                        }
                        case CHARGE_TO_MAX: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    setMaxRangeCharging(true);
                                } else {
                                    setMaxRangeCharging(false);
                                }
                            }
                            break;
                        }
                        case CHARGE: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    charge(true);
                                } else {
                                    charge(false);
                                }
                            }
                            break;
                        }
                        case FLASH: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    flashLights();
                                }
                            }
                            break;
                        }
                        case HONK_HORN: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    honkHorn();
                                }
                            }
                            break;
                        }
                        case CHARGEPORT: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    openChargePort();
                                }
                            }
                            break;
                        }
                        case DOOR_LOCK: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    lockDoors(true);
                                } else {
                                    lockDoors(false);
                                }
                            }
                            break;
                        }
                        case AUTO_COND: {
                            if (command instanceof OnOffType) {
                                if (((OnOffType) command) == OnOffType.ON) {
                                    autoConditioning(true);
                                } else {
                                    autoConditioning(false);
                                }
                            }
                            break;
                        }
                        default:
                            break;
                    }
                    return;
                } catch (IllegalArgumentException e) {
                    logger.warn(
                            "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                            channelID, command.toString());
                }
            }
        }
    }

    public void sendCommand(String command, String payLoad, WebTarget target) {
        Request request = new Request(command, payLoad, target);
        if (stateThrottler != null) {
            stateThrottler.submit(TESLA_COMMAND_THROTTLE, request);
        }
    }

    public void sendCommand(String command) {
        sendCommand(command, "{}");
    }

    public void sendCommand(String command, String payLoad) {
        Request request = new Request(command, payLoad, commandTarget);
        if (stateThrottler != null) {
            stateThrottler.submit(TESLA_COMMAND_THROTTLE, request);
        }
    }

    public void sendCommand(String command, WebTarget target) {
        Request request = new Request(command, "{}", target);
        if (stateThrottler != null) {
            stateThrottler.submit(TESLA_COMMAND_THROTTLE, request);
        }
    }

    public void requestData(String command, String payLoad) {
        Request request = new Request(command, payLoad, dataRequestTarget);
        if (stateThrottler != null) {
            stateThrottler.submit(TESLA_DATA_THROTTLE, request);
        }
    }

    public void requestData(String command) {
        requestData(command, null);
    }

    public void queryVehicle(String parameter) {
        WebTarget target = vehicleTarget.path(parameter);
        sendCommand(parameter, null, target);
    }

    protected String invokeAndParse(String command, String payLoad, WebTarget target) {

        logger.debug("Invoking: {}", command);

        if (vehicle.id != null) {
            Response response;

            if (payLoad != null) {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicle.id).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                } else {
                    response = target.resolveTemplate("vid", vehicle.id).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                }
            } else {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicle.id)
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                } else {
                    response = target.resolveTemplate("vid", vehicle.id).request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                }
            }

            JsonParser parser = new JsonParser();

            if (response != null && response.getStatus() == 200) {
                try {
                    JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
                    logger.trace("Request : {}:{}:{} yields {}", new Object[] { command, payLoad, target.toString(),
                            jsonObject.get("response").toString() });
                    return jsonObject.get("response").toString();
                } catch (Exception e) {
                    logger.error("An exception occurred while invoking a REST request : '{}'", e.getMessage());
                }
            } else {
                logger.error("An error occurred while communicating with the vehicle during request {} : {}:{}",
                        new Object[] { command, (response != null) ? response.getStatus() : "",
                                (response != null) ? response.getStatusInfo() : "No Response" });

                if (intervalErrors == 0 && response != null && response.getStatus() == 401) {
                    authenticate();
                }

                intervalErrors++;
                if (intervalErrors >= MAXIMUM_ERRORS_IN_INTERVAL) {
                    logger.warn("Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                            MAXIMUM_ERRORS_IN_INTERVAL, ERROR_INTERVAL_SECONDS);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    eventClient.close();
                    return null;
                }

                if ((System.currentTimeMillis() - intervalTimestamp) > 1000 * ERROR_INTERVAL_SECONDS) {
                    logger.trace("Resetting the error counter. ({} errors in the last interval)", intervalErrors);
                    intervalTimestamp = System.currentTimeMillis();
                    intervalErrors = 0;
                }
            }
        }

        return null;
    }

    public void parseAndUpdate(String request, String payLoad, String result) {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = null;

        try {
            if (request != null && result != null && !"null".equals(result)) {
                // first, update state objects
                switch (request) {
                    case TESLA_DRIVE_STATE: {
                        driveState = gson.fromJson(result, DriveState.class);
                        break;
                    }
                    case TESLA_GUI_STATE: {
                        guiState = gson.fromJson(result, GUIState.class);
                        break;
                    }
                    case TESLA_VEHICLE_STATE: {
                        vehicleState = gson.fromJson(result, VehicleState.class);
                        break;
                    }
                    case TESLA_CHARGE_STATE: {
                        chargeState = gson.fromJson(result, ChargeState.class);
                        if (chargeState.charging_state != null && "Charging".equals(chargeState.charging_state)) {
                            updateState(CHANNEL_CHARGE, OnOffType.ON);
                        } else {
                            updateState(CHANNEL_CHARGE, OnOffType.OFF);
                        }

                        break;
                    }
                    case TESLA_CLIMATE_STATE: {
                        climateState = gson.fromJson(result, ClimateState.class);
                        break;
                    }
                }

                // secondly, reformat the response string to a JSON compliant
                // object for some specific non-JSON compatible requests
                switch (request) {
                    case TESLA_MOBILE_ENABLED_STATE: {
                        jsonObject = new JsonObject();
                        jsonObject.addProperty(TESLA_MOBILE_ENABLED_STATE, result);
                        break;
                    }
                    default: {
                        jsonObject = parser.parse(result).getAsJsonObject();
                        break;
                    }
                }
            }

            // process the result
            if (jsonObject != null && result != null && !"null".equals(result)) {
                // deal with responses for "set" commands, which get confirmed
                // positively, or negatively, in which case a reason for failure
                // is provided
                if (jsonObject.get("reason") != null && jsonObject.get("reason").getAsString() != null) {
                    boolean requestResult = jsonObject.get("result").getAsBoolean();
                    logger.debug("The request ({}) execution was {}, and reported '{}'", new Object[] { request,
                            requestResult ? "successful" : "not successful", jsonObject.get("reason").getAsString() });
                } else {
                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();

                    long resultTimeStamp = 0;
                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                        if ("timestamp".equals(entry.getKey())) {
                            resultTimeStamp = Long.valueOf(entry.getValue().getAsString());
                            if (logger.isTraceEnabled()) {
                                Date date = new Date(resultTimeStamp);
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                logger.trace("The request result timestamp is {}", dateFormatter.format(date));
                            }
                            break;
                        }
                    }

                    try {
                        lock.lock();

                        boolean proceed = true;
                        if (resultTimeStamp < lastTimeStamp && request == TESLA_DRIVE_STATE) {
                            proceed = false;
                        }

                        if (proceed) {
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                try {
                                    TeslaChannelSelector selector = TeslaChannelSelector
                                            .getValueSelectorFromRESTID(entry.getKey());
                                    if (!selector.isProperty()) {
                                        if (!entry.getValue().isJsonNull()) {
                                            updateState(selector.getChannelID(), teslaChannelSelectorProxy.getState(
                                                    entry.getValue().getAsString(), selector, editProperties()));
                                        } else {
                                            updateState(selector.getChannelID(), UnDefType.UNDEF);
                                        }
                                    } else {
                                        if (!entry.getValue().isJsonNull()) {
                                            Map<String, String> properties = editProperties();
                                            properties.put(selector.getChannelID(), entry.getValue().getAsString());
                                            updateProperties(properties);
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    logger.trace("The variable/value pair '{}':'{}' is not (yet) supported",
                                            entry.getKey(), entry.getValue());
                                } catch (ClassCastException | IllegalStateException e) {
                                    logger.trace("An exception occurred while converting the JSON data : '{}'",
                                            e.getMessage(), e);
                                }
                            }
                        } else {
                            logger.warn("The result for request '{}' is discarded due to an out of sync timestamp",
                                    request);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        } catch (Exception p) {
            logger.error("An exception occurred while parsing data received from the vehicle: '{}'", p.getMessage());
        }
    }

    protected boolean isAwake() {
        return (vehicle != null) ? (!"asleep".equals(vehicle.state) && vehicle.vehicle_id != null) : false;
    }

    protected boolean isInMotion() {
        if (driveState != null) {
            if (driveState.speed != null && driveState.shift_state != null) {
                return !"Undefined".equals(driveState.speed)
                        && (!"P".equals(driveState.shift_state) || !"Undefined".equals(driveState.shift_state));
            }
        }
        return false;
    }

    public void setChargeLimit(int percent) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("percent", percent);
        sendCommand(TESLA_COMMAND_SET_CHARGE_LIMIT, gson.toJson(payloadObject), commandTarget);
        requestData(TESLA_CHARGE_STATE);
    }

    public void setSunroof(String state) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("state", state);
        sendCommand(TESLA_COMMAND_SUN_ROOF, gson.toJson(payloadObject), commandTarget);
        requestData(TESLA_VEHICLE_STATE);
    }

    public void moveSunroof(int percent) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("state", "move");
        payloadObject.addProperty("percent", percent);
        sendCommand(TESLA_COMMAND_SUN_ROOF, gson.toJson(payloadObject), commandTarget);
        requestData(TESLA_VEHICLE_STATE);
    }

    public void setTemperature(float temperature) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("driver_temp", temperature);
        payloadObject.addProperty("passenger_temp", temperature);
        sendCommand(TESLA_COMMAND_SET_TEMP, gson.toJson(payloadObject), commandTarget);
        requestData(TESLA_CLIMATE_STATE);
    }

    public void setMaxRangeCharging(boolean b) {
        if (b) {
            sendCommand(TESLA_COMMAND_CHARGE_MAX, commandTarget);
        } else {
            sendCommand(TESLA_COMMAND_CHARGE_STD, commandTarget);
        }
        requestData(TESLA_CHARGE_STATE);
    }

    public void charge(boolean b) {
        if (b) {
            sendCommand(TESLA_COMMAND_CHARGE_START, commandTarget);
        } else {
            sendCommand(TESLA_COMMAND_CHARGE_STOP, commandTarget);
        }
        requestData(TESLA_CHARGE_STATE);
    }

    public void flashLights() {
        sendCommand(TESLA_COMMAND_FLASH_LIGHTS, commandTarget);
    }

    public void honkHorn() {
        sendCommand(TESLA_COMMAND_HONK_HORN, commandTarget);
    }

    public void openChargePort() {
        sendCommand(TESLA_COMMAND_OPEN_CHARGE_PORT, commandTarget);
        requestData(TESLA_CHARGE_STATE);
    }

    public void lockDoors(boolean b) {
        if (b) {
            sendCommand(TESLA_COMMAND_DOOR_LOCK, commandTarget);
        } else {
            sendCommand(TESLA_COMMAND_DOOR_UNLOCK, commandTarget);
        }
        requestData(TESLA_VEHICLE_STATE);
    }

    public void autoConditioning(boolean b) {
        if (b) {
            sendCommand(TESLA_COMMAND_AUTO_COND_START, commandTarget);
        } else {
            sendCommand(TESLA_COMMAND_AUTO_COND_STOP, commandTarget);
        }
        requestData(TESLA_CLIMATE_STATE);
    }

    protected Vehicle queryVehicle() {

        // get a list of vehicles
        Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + logonToken.access_token).get();

        logger.debug("Querying the vehicle : Response : {}:{}", response.getStatus(), response.getStatusInfo());

        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
        Vehicle[] vehicleArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Vehicle[].class);

        for (int i = 0; i < vehicleArray.length; i++) {
            logger.debug("Querying the vehicle : VIN : {}", vehicleArray[i].vin);
            if (vehicleArray[i].vin.equals(getConfig().get(VIN))) {
                vehicleJSON = gson.toJson(vehicleArray[i]);
                parseAndUpdate("queryVehicle", null, vehicleJSON);
                return vehicleArray[i];
            }
        }

        return null;
    }

    private String getStorageKey() {
        return this.getThing().getUID().getId();
    }

    private ThingStatusDetail authenticate() {

        Storage<Object> storage = storageService.getStorage(TeslaBindingConstants.BINDING_ID);

        String storedToken = (String) storage.get(getStorageKey());
        TokenResponse token = storedToken == null ? null : gson.fromJson(storedToken, TokenResponse.class);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        boolean hasExpired = true;

        if (token != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(token.created_at * 1000);
            logger.info("Found a request token created at {}", dateFormatter.format(calendar.getTime()));
            calendar.setTimeInMillis(token.created_at * 1000 + 60 * token.expires_in);

            Date now = new Date();

            if (calendar.getTime().before(now)) {
                logger.info("The token has expired at {}", dateFormatter.format(calendar.getTime()));
                hasExpired = true;
            } else {
                hasExpired = false;
            }
        }

        String username = (String) getConfig().get(USERNAME);

        if (!StringUtils.isEmpty(username) && hasExpired) {
            String password = (String) getConfig().get(PASSWORD);
            return authenticate(username, password);
        }

        if (token == null || StringUtils.isEmpty(token.refresh_token)) {
            return ThingStatusDetail.CONFIGURATION_ERROR;
        }

        TokenRequestRefreshToken tokenRequest = null;
        try {
            tokenRequest = new TokenRequestRefreshToken(token.refresh_token);
        } catch (GeneralSecurityException e) {
            logger.error("An exception occurred while requesting a new token : '{}'", e.getMessage(), e);
        }

        String payLoad = gson.toJson(tokenRequest);

        Response response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

        if (response == null) {
            logger.debug("Authenticating : Response was null");
        } else {
            logger.debug("Authenticating : Response : {}:{}", response.getStatus(), response.getStatusInfo());

            if (response.getStatus() == 200 && response.hasEntity()) {

                String responsePayLoad = response.readEntity(String.class);
                TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);

                if (tokenResponse != null && !StringUtils.isEmpty(tokenResponse.access_token)) {
                    storage.put(getStorageKey(), gson.toJson(tokenResponse));
                    this.logonToken = tokenResponse;
                    return ThingStatusDetail.NONE;
                }

                return ThingStatusDetail.NONE;

            } else if (response.getStatus() == 401) {
                if (!StringUtils.isEmpty(username)) {
                    String password = (String) getConfig().get(PASSWORD);
                    return authenticate(username, password);
                } else {
                    return ThingStatusDetail.CONFIGURATION_ERROR;
                }
            } else if (response.getStatus() == 503 || response.getStatus() == 502) {
                return ThingStatusDetail.COMMUNICATION_ERROR;
            }
        }
        return ThingStatusDetail.CONFIGURATION_ERROR;
    }

    private ThingStatusDetail authenticate(String username, String password) {

        TokenRequest token = null;
        try {
            token = new TokenRequestPassword(username, password);
        } catch (GeneralSecurityException e) {
            logger.error("An exception occurred while building a password request token : '{}'", e.getMessage(), e);
        }

        if (token != null) {
            String payLoad = gson.toJson(token);

            Response response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

            if (response != null) {
                logger.debug("Authenticating : Response : {}:{}", response.getStatus(), response.getStatusInfo());

                if (response.getStatus() == 200 && response.hasEntity()) {

                    String responsePayLoad = response.readEntity(String.class);
                    TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);

                    if (StringUtils.isNotEmpty(tokenResponse.access_token)) {
                        Storage<Object> storage = storageService.getStorage(TeslaBindingConstants.BINDING_ID);
                        storage.put(getStorageKey(), gson.toJson(tokenResponse));
                        this.logonToken = tokenResponse;
                        return ThingStatusDetail.NONE;
                    }

                } else if (response.getStatus() == 401) {
                    return ThingStatusDetail.CONFIGURATION_ERROR;
                } else if (response.getStatus() == 503 || response.getStatus() == 502) {
                    return ThingStatusDetail.COMMUNICATION_ERROR;
                }
            }
        }
        return ThingStatusDetail.CONFIGURATION_ERROR;
    }

    protected Runnable fastStateRunnable = new Runnable() {

        @Override
        public void run() {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                if (isAwake()) {
                    requestData(TESLA_DRIVE_STATE);
                    requestData(TESLA_VEHICLE_STATE);
                } else {
                    if (vehicle != null) {
                        sendCommand(TESLA_COMMAND_WAKE_UP);
                    } else {
                        vehicle = queryVehicle();
                    }
                }
            }
        }
    };

    protected Runnable slowStateRunnable = new Runnable() {

        @Override
        public void run() {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                if (isAwake()) {
                    requestData(TESLA_CHARGE_STATE);
                    requestData(TESLA_CLIMATE_STATE);
                    requestData(TESLA_GUI_STATE);
                    queryVehicle(TESLA_MOBILE_ENABLED_STATE);
                    parseAndUpdate("queryVehicle", null, vehicleJSON);
                } else {
                    if (vehicle != null) {
                        sendCommand(TESLA_COMMAND_WAKE_UP);
                    } else {
                        vehicle = queryVehicle();
                    }
                }
            }
        }
    };

    protected Runnable connectRunnable = new Runnable() {

        @Override
        public void run() {

            try {
                lock.lock();

                if (getThing().getStatus() != ThingStatus.ONLINE) {

                    logger.debug("Setting up an authenticated connection to the Tesla back-end");

                    ThingStatusDetail authenticationResult = authenticate();

                    if (authenticationResult != ThingStatusDetail.NONE) {
                        updateStatus(ThingStatus.OFFLINE, authenticationResult);
                    } else {
                        // get a list of vehicles
                        Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                                .header("Authorization", "Bearer " + logonToken.access_token).get();

                        if (response != null && response.getStatus() == 200 && response.hasEntity()) {
                            if ((vehicle = queryVehicle()) != null) {
                                logger.debug("Found the vehicle with VIN '{}' in the list of vehicles you own",
                                        getConfig().get(VIN));
                                updateStatus(ThingStatus.ONLINE);
                                intervalErrors = 0;
                                intervalTimestamp = System.currentTimeMillis();
                            } else {
                                logger.warn("Unable to find the vehicle with VIN '{}' in the list of vehicles you own",
                                        getConfig().get(VIN));
                                updateStatus(ThingStatus.OFFLINE);
                            }
                        } else {
                            if (response != null) {
                                logger.error("Error fetching the list of vehicles : {}:{}", response.getStatus(),
                                        response.getStatusInfo());
                                updateStatus(ThingStatus.OFFLINE);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while connecting to the Tesla back-end: '{}'", e.getMessage());
            } finally {
                lock.unlock();
            }

        }
    };

    protected Runnable eventRunnable = new Runnable() {

        Response eventResponse;
        BufferedReader eventBufferedReader;
        InputStreamReader eventInputStreamReader;
        boolean isEstablished = false;

        protected boolean establishEventStream() {
            try {
                if (!isEstablished) {
                    eventBufferedReader = null;

                    eventClient = ClientBuilder.newClient()
                            .property(ClientProperties.CONNECT_TIMEOUT, EVENT_STREAM_CONNECT_TIMEOUT)
                            .property(ClientProperties.READ_TIMEOUT, EVENT_STREAM_READ_TIMEOUT)
                            .register(new Authenticator((String) getConfig().get(USERNAME), vehicle.tokens[0]));
                    eventTarget = eventClient.target(TESLA_EVENT_URI).path(vehicle.vehicle_id + "/").queryParam(
                            "values", StringUtils.join(EventKeys.values(), ',', 1, EventKeys.values().length));
                    eventResponse = eventTarget.request(MediaType.TEXT_PLAIN_TYPE).get();

                    logger.debug("Event Stream : Establishing the event stream : Response : {}:{}",
                            eventResponse.getStatus(), eventResponse.getStatusInfo());

                    if (eventResponse.getStatus() == 200) {
                        InputStream dummy = (InputStream) eventResponse.getEntity();
                        eventInputStreamReader = new InputStreamReader(dummy);
                        eventBufferedReader = new BufferedReader(eventInputStreamReader);
                        isEstablished = true;
                    } else if (eventResponse.getStatus() == 401) {
                        updateStatus(ThingStatus.OFFLINE);
                        isEstablished = false;
                    } else {
                        isEstablished = false;
                    }
                }
            } catch (Exception e) {
                logger.error(
                        "Event Stream : An exception occurred while establishing the event stream for the vehicle: '{}'",
                        e.getMessage());
                isEstablished = false;
            }

            return isEstablished;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (getThing().getStatus() == ThingStatus.ONLINE) {
                        if (isAwake()) {
                            if (establishEventStream()) {
                                String line = eventBufferedReader.readLine();

                                while (line != null) {
                                    logger.debug("Event Stream : Received an event: '{}'", line);
                                    String vals[] = line.split(",");
                                    long currentTimeStamp = Long.valueOf(vals[0]);
                                    long systemTimeStamp = System.currentTimeMillis();
                                    if (logger.isDebugEnabled()) {
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                        logger.debug("STS {} CTS {} Delta {}",
                                                dateFormatter.format(new Date(systemTimeStamp)),
                                                dateFormatter.format(new Date(currentTimeStamp)),
                                                systemTimeStamp - currentTimeStamp);
                                    }
                                    if (systemTimeStamp - currentTimeStamp < EVENT_TIMESTAMP_AGE_LIMIT) {
                                        if (currentTimeStamp > lastTimeStamp) {
                                            lastTimeStamp = Long.valueOf(vals[0]);
                                            if (logger.isDebugEnabled()) {
                                                SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                        "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                                logger.debug("Event Stream : Event stamp is {}",
                                                        dateFormatter.format(new Date(lastTimeStamp)));
                                            }
                                            for (int i = 0; i < EventKeys.values().length; i++) {
                                                TeslaChannelSelector selector = TeslaChannelSelector
                                                        .getValueSelectorFromRESTID((EventKeys.values()[i]).toString());
                                                if (!selector.isProperty()) {
                                                    State newState = teslaChannelSelectorProxy.getState(vals[i],
                                                            selector, editProperties());
                                                    if (newState != null && !"".equals(vals[i])) {
                                                        updateState(selector.getChannelID(), newState);
                                                    } else {
                                                        updateState(selector.getChannelID(), UnDefType.UNDEF);
                                                    }
                                                } else {
                                                    Map<String, String> properties = editProperties();
                                                    properties.put(selector.getChannelID(),
                                                            (selector.getState(vals[i])).toString());
                                                    updateProperties(properties);
                                                }
                                            }
                                        } else {
                                            if (logger.isDebugEnabled()) {
                                                SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                        "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                                logger.debug(
                                                        "Event Stream : Discarding an event with an out of sync timestamp {} (last is {})",
                                                        dateFormatter.format(new Date(currentTimeStamp)),
                                                        dateFormatter.format(new Date(lastTimeStamp)));
                                            }
                                        }
                                    } else {
                                        if (logger.isDebugEnabled()) {
                                            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                    "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                            logger.debug(
                                                    "Event Stream : Discarding an event that differs {} ms from the system time: {} (system is {})",
                                                    systemTimeStamp - currentTimeStamp,
                                                    dateFormatter.format(currentTimeStamp),
                                                    dateFormatter.format(systemTimeStamp));
                                        }
                                        if (systemTimeStamp - currentTimeStamp > EVENT_TIMESTAMP_MAX_DELTA) {
                                            if (logger.isTraceEnabled()) {
                                                logger.trace("Event Stream : The event stream will be reset");
                                            }
                                            isEstablished = false;
                                        }
                                    }

                                    line = eventBufferedReader.readLine();
                                }

                                if (line == null) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("Event Stream : The end of stream was reached");
                                    }
                                    isEstablished = false;
                                }
                            }
                        } else {
                            logger.debug("Event stream : The vehicle is not awake");
                            if (vehicle != null) {
                                // wake up the vehicle until streaming token <> 0
                                logger.debug("Event stream : Waking up the vehicle");
                                sendCommand(TESLA_COMMAND_WAKE_UP);
                            } else {
                                logger.debug("Event stream : Querying the vehicle");
                                vehicle = queryVehicle();
                            }
                        }
                    } else {
                        Thread.sleep(250);
                    }
                } catch (IOException | NumberFormatException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Event Stream : An exception occurred while reading events : '{}'",
                                e.getMessage());
                    }
                    isEstablished = false;
                } catch (InterruptedException e) {
                    isEstablished = false;
                }

                if (Thread.interrupted()) {
                    logger.debug("Event Stream : the Event Stream was interrupted");
                    return;
                }
            }
        }
    };

    protected class Request implements Runnable {

        private String request;
        private String payLoad;
        private WebTarget target;

        public Request(String request, String payLoad, WebTarget target) {
            this.request = request;
            this.payLoad = payLoad;
            this.target = target;
        }

        @Override
        public void run() {
            try {

                String result = "";

                if (isAwake() && getThing().getStatus() == ThingStatus.ONLINE) {
                    result = invokeAndParse(request, payLoad, target);
                }

                if (result != null && !"".equals(result)) {
                    parseAndUpdate(request, payLoad, result);
                }
            } catch (Exception e) {
                logger.error("An exception occurred while executing a request to the vehicle: '{}'", e.getMessage());
            }
        }
    }

    protected class Authenticator implements ClientRequestFilter {

        private final String user;
        private final String password;

        public Authenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            final String basicAuthentication = getBasicAuthentication();
            headers.add("Authorization", basicAuthentication);
        }

        private String getBasicAuthentication() {
            String token = this.user + ":" + this.password;
            try {
                return "Basic " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException("Cannot encode with UTF-8", ex);
            }
        }
    }
}

/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.tesla.TeslaBindingConstants.EventKeys;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.binding.tesla.internal.protocol.ChargeState;
import org.openhab.binding.tesla.internal.protocol.ClimateState;
import org.openhab.binding.tesla.internal.protocol.DriveState;
import org.openhab.binding.tesla.internal.protocol.GUIState;
import org.openhab.binding.tesla.internal.protocol.TokenRequest;
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
 */
public class TeslaHandler extends BaseThingHandler {

    public static final int EVENT_REFRESH_INTERVAL = 200;
    public static final int FAST_STATUS_REFRESH_INTERVAL = 15000;
    public static final int SLOW_STATUS_REFRESH_INTERVAL = 60000;
    public static final int MINIMUM_EVENT_INTERVAL = 15000;

    private Logger logger = LoggerFactory.getLogger(TeslaHandler.class);

    // Vehicle state variables
    protected Vehicle vehicle;
    protected String vehicleJSON;
    protected DriveState driveState;
    protected GUIState guiState;
    protected VehicleState vehicleState;
    protected ChargeState chargeState;
    protected ClimateState climateState;
    protected String accessToken;

    // REST Client API variables
    protected Client teslaClient = ClientBuilder.newClient();
    protected Client eventClient;
    protected WebTarget teslaTarget = teslaClient.target(TESLA_OWNERS_URI);
    protected WebTarget tokenTarget = teslaTarget.path(TESLA_ACCESS_TOKEN_URI);
    protected WebTarget vehiclesTarget = teslaTarget.path(API_VERSION).path(VEHICLES);
    protected WebTarget vehicleTarget = vehiclesTarget.path(VEHICLE_ID_PATH);
    protected WebTarget dataRequestTarget = vehicleTarget.path(DATA_REQUEST_PATH);
    protected WebTarget commandTarget = vehicleTarget.path(COMMAND_PATH);
    protected WebTarget eventTarget;

    // Threading and Job related variables
    protected ScheduledFuture<?> eventJob;
    protected ScheduledFuture<?> fastStateJob;
    protected ScheduledFuture<?> slowStateJob;
    protected QueueChannelThrottler stateThrottler;
    protected Response eventResponse;
    protected BufferedReader eventBufferedReader;
    protected InputStreamReader eventInputStreamReader;
    protected long lastEventSystemTime = 0;
    protected String lastEventTimeStamp = "";

    protected Gson gson = new Gson();
    protected TeslaChannelSelectorProxy teslaChannelSelectorProxy = new TeslaChannelSelectorProxy();
    private JsonParser parser = new JsonParser();

    public TeslaHandler(Thing thing) {
        super(thing);

    }

    @Override
    public void initialize() {

        logger.trace("Initializing the Tesla handler for {}", getThing().getUID());

        connect();

        if (getThing().getStatus() == ThingStatus.ONLINE) {

            if (eventJob == null || eventJob.isCancelled()) {
                eventJob = scheduler.scheduleAtFixedRate(eventRunnable, 0, EVENT_REFRESH_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            Map<Object, Rate> channels = new HashMap<Object, Rate>();
            channels.put(TESLA_DATA_THROTTLE, new Rate(10, 10, TimeUnit.SECONDS));
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
        }
    }

    @Override
    public void dispose() {

        logger.trace("Disposing the Tesla handler for {}", getThing().getUID());

        if (eventJob != null && !eventJob.isCancelled()) {
            eventJob.cancel(true);
            eventJob = null;
        }

        if (fastStateJob != null && !fastStateJob.isCancelled()) {
            fastStateJob.cancel(true);
            fastStateJob = null;
        }

        if (slowStateJob != null && !slowStateJob.isCancelled()) {
            slowStateJob.cancel(true);
            slowStateJob = null;
        }
    }

    private void connect() {

        logger.trace("Setting up an authenticated connection to the Tesla back-end");

        ThingStatusDetail authenticationResult = authenticate((String) getConfig().get(USERNAME),
                (String) getConfig().get(PASSWORD));

        if (authenticationResult != ThingStatusDetail.NONE) {
            updateStatus(ThingStatus.OFFLINE, authenticationResult);
        } else {
            // get a list of vehicles
            logger.trace("Getting a list of vehicles");
            Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", "Bearer " + accessToken).get();

            if (response != null && response.getStatus() == 200 && response.hasEntity()) {

                if ((vehicle = queryVehicle()) != null) {

                    logger.debug("Found the vehicle with VIN '{}' in the list of vehicles you own",
                            getConfig().get(VIN));
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    logger.warn("Unable to find the vehicle with VIN '{}' in the list of vehicles you own",
                            getConfig().get(VIN));
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        String channelID = channelUID.getId();
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromChannelID(channelID);

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
                        } else
                            if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
                            setChargeLimit(Math.min(chargeState.charge_limit_soc + 1, 100));
                        } else if (command instanceof IncreaseDecreaseType
                                && command == IncreaseDecreaseType.DECREASE) {
                            setChargeLimit(Math.max(chargeState.charge_limit_soc - 1, 0));
                        }
                        break;
                    }
                    case TEMPERATURE: {
                        if (command instanceof DecimalType) {
                            setTemperature(((DecimalType) command).floatValue());
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
                        } else
                            if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
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
        if (vehicle.id != null) {
            Response response;

            if (payLoad != null) {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicle.id).request()
                            .header("Authorization", "Bearer " + accessToken)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                } else {
                    response = target.resolveTemplate("vid", vehicle.id).request()
                            .header("Authorization", "Bearer " + accessToken)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                }
            } else {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicle.id)
                            .request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer " + accessToken)
                            .get();
                } else {
                    response = target.resolveTemplate("vid", vehicle.id).request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + accessToken).get();
                }
            }

            JsonParser parser = new JsonParser();

            if (response != null && response.getStatus() == 200) {
                try {
                    JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
                    return jsonObject.get("response").toString();
                } catch (Exception e) {
                    logger.error("An exception occurred while invoking a REST request : '{}'", e.getMessage());
                }
            } else {
                logger.error("An error occured while communicating with the vehicle: '{}:{}'",
                        (response != null) ? response.getStatus() : "",
                        (response != null) ? response.getStatusInfo() : "No Response");
            }
        }

        return null;

    }

    public void parseAndUpdate(String request, String payLoad, String result) {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = null;

        try {
            if (request != null && result != null && result != "null") {
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
                        ChannelUID theChannelUID = new ChannelUID(getThing().getUID(), "charge");
                        if (chargeState.charging_state != null && chargeState.charging_state.equals("Charging")) {
                            updateState(theChannelUID, OnOffType.ON);
                        } else {
                            updateState(theChannelUID, OnOffType.OFF);
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
            if (jsonObject != null && result != null && !result.equals("null")) {
                // deal with responses for "set" commands, which get confirmed
                // positively, or negatively, in which case a reason for failure
                // is provided
                if (jsonObject.get("reason") != null && jsonObject.get("reason").getAsString() != null) {
                    boolean requestResult = jsonObject.get("result").getAsBoolean();
                    logger.debug("The request ({}) execution was {}, and reported '{}'", new Object[] { request,
                            requestResult ? "successful" : "not successful", jsonObject.get("reason").getAsString() });
                } else {
                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                        try {
                            TeslaChannelSelector selector = TeslaChannelSelector
                                    .getValueSelectorFromRESTID(entry.getKey());
                            if (!selector.isProperty()) {
                                ChannelUID theChannelUID = new ChannelUID(getThing().getUID(), selector.getChannelID());
                                if (!entry.getValue().isJsonNull()) {
                                    updateState(theChannelUID, teslaChannelSelectorProxy
                                            .getState(entry.getValue().getAsString(), selector, editProperties()));
                                } else {
                                    updateState(theChannelUID, UnDefType.UNDEF);
                                }
                            } else {
                                if (!entry.getValue().isJsonNull()) {
                                    Map<String, String> properties = editProperties();
                                    properties.put(selector.getChannelID(), entry.getValue().getAsString());
                                    updateProperties(properties);
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Unable to handle the variable/value pair '{}':'{}'", entry.getKey(),
                                    entry.getValue());
                        }
                    }
                }
            }
        } catch (Exception p) {
            logger.error("An exception occurred while parsing data received from the vehicle: '{}'", p.getMessage());
        }

    }

    protected boolean isAwake() {
        return (vehicle != null) ? (vehicle.state != "asleep" && vehicle.vehicle_id != null) : false;
    }

    protected boolean isInMotion() {
        if (driveState != null) {
            if (driveState.speed != null && driveState.shift_state != null) {
                return !driveState.speed.equals("Undefined")
                        && (!driveState.shift_state.equals("P") || !driveState.shift_state.equals("Undefined"));
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
                .header("Authorization", "Bearer " + accessToken).get();

        logger.trace("Querying the vehicle : Response : {}", response.getStatusInfo());

        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
        Vehicle[] vehicleArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Vehicle[].class);

        for (int i = 0; i < vehicleArray.length; i++) {
            logger.trace("Querying the vehicle : VIN : {}", vehicleArray[i].vin);
            if (vehicleArray[i].vin.equals(getConfig().get(VIN))) {
                vehicleJSON = gson.toJson(vehicleArray[i]);
                parseAndUpdate("queryVehicle", null, vehicleJSON);
                return vehicleArray[i];
            }
        }

        return null;
    }

    protected ThingStatusDetail authenticate(String username, String password) {

        TokenRequest token = new TokenRequest(username, password);
        String payLoad = gson.toJson(token);

        Response response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

        logger.trace("Authenticating : Response : {}", response.getStatusInfo());

        if (response != null) {
            if (response.getStatus() == 200 && response.hasEntity()) {

                String responsePayLoad = response.readEntity(String.class);
                JsonObject readObject = parser.parse(responsePayLoad).getAsJsonObject();

                for (Entry<String, JsonElement> entry : readObject.entrySet()) {
                    switch (entry.getKey()) {
                        case "access_token": {
                            accessToken = entry.getValue().getAsString();
                            logger.trace("Authenticating : Setting access code to : {}", accessToken);
                            return ThingStatusDetail.NONE;
                        }
                    }
                }
            } else if (response.getStatus() == 401) {
                return ThingStatusDetail.CONFIGURATION_ERROR;
            } else if (response.getStatus() == 503) {
                return ThingStatusDetail.COMMUNICATION_ERROR;
            }
        }
        return ThingStatusDetail.CONFIGURATION_ERROR;
    }

    protected Runnable fastStateRunnable = new Runnable() {

        @Override
        public void run() {
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
    };

    protected Runnable slowStateRunnable = new Runnable() {

        @Override
        public void run() {
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
    };

    protected Runnable eventRunnable = new Runnable() {

        protected void establishEventStream() throws Exception {
            eventClient = ClientBuilder.newClient()
                    .register(new Authenticator((String) getConfig().get(USERNAME), vehicle.tokens[0]));
            eventTarget = eventClient.target(TESLA_EVENT_URI).path(vehicle.vehicle_id + "/").queryParam("values",
                    StringUtils.join(EventKeys.values(), ',', 1, EventKeys.values().length));
            eventResponse = eventTarget.request(MediaType.TEXT_PLAIN_TYPE).get();

            logger.trace("Establishing the event stream : Response : {}", eventResponse.getStatusInfo());

            if (eventResponse.getStatus() == 200) {
                InputStream dummy = (InputStream) eventResponse.getEntity();
                eventInputStreamReader = new InputStreamReader(dummy);
                eventBufferedReader = new BufferedReader(eventInputStreamReader);
            } else {
                throw new Exception("Establishing event stream failed with code " + eventResponse.getStatus());
            }
        }

        @Override
        public void run() {
            try {
                if (isAwake()) {
                    if (eventBufferedReader == null || (!isInMotion()
                            && (System.currentTimeMillis() - lastEventSystemTime > MINIMUM_EVENT_INTERVAL))) {
                        try {
                            establishEventStream();
                        } catch (Exception e) {
                            logger.error(
                                    "An exception occurred while establishing the event stream for the vehicle: '{}'",
                                    e.getMessage());
                            connect();
                            eventBufferedReader = null;
                        }
                    }

                    try {
                        if (eventBufferedReader != null) {
                            String line = null;
                            try {
                                line = eventBufferedReader.readLine();
                            } catch (Exception e) {
                                // we just move on. If we are here, then is most
                                // probably due to Premature EOF exceptions
                            }
                            if (line != null) {
                                lastEventSystemTime = System.currentTimeMillis();
                                logger.debug("Received an event: '{}'", line);
                                String vals[] = line.split(",");
                                if (!vals[0].equals(lastEventTimeStamp)) {
                                    lastEventTimeStamp = vals[0];
                                    for (int i = 0; i < EventKeys.values().length; i++) {
                                        try {
                                            TeslaChannelSelector selector = TeslaChannelSelector
                                                    .getValueSelectorFromRESTID((EventKeys.values()[i]).toString());
                                            if (!selector.isProperty()) {
                                                State newState = teslaChannelSelectorProxy.getState(vals[i], selector,
                                                        editProperties());
                                                if (newState != null && !vals[i].equals("")) {
                                                    updateState(new ChannelUID(getThing().getUID(),
                                                            selector.getChannelID()), newState);
                                                } else {
                                                    updateState(new ChannelUID(getThing().getUID(),
                                                            selector.getChannelID()), UnDefType.UNDEF);

                                                }
                                            } else {
                                                Map<String, String> properties = editProperties();
                                                properties.put(selector.getChannelID(),
                                                        (selector.getState(vals[i])).toString());
                                                updateProperties(properties);
                                            }
                                        } catch (Exception e) {
                                            logger.warn(
                                                    "An exception occurred while processing an event received from the vehicle; '{}'",
                                                    e.getMessage());
                                        }
                                    }
                                }
                            } else {
                                eventBufferedReader = null;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("An exception occurred while reading event inputs from vehicle '{}' : {}",
                                vehicle.vin, e.getMessage());
                        eventBufferedReader = null;
                    }
                } else {
                    logger.debug("Event stream : The vehicle is not awake");
                    if (vehicle != null) {
                        // wake up the vehicle until streaming token <> 0
                        logger.debug("eventRunnalbe: Wake up vehicle");
                        sendCommand(TESLA_COMMAND_WAKE_UP);
                    } else {
                        logger.debug("Event stream : Querying the vehicle");
                        vehicle = queryVehicle();
                    }
                }
            } catch (Exception t) {
                logger.error("An exception ocurred in the event thread: '{}'", t.getMessage());
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

                logger.trace("Executing the request {}:{}:{}", new Object[] { request, payLoad, target });

                String result = "";

                if (isAwake() && getThing().getStatus() == ThingStatus.ONLINE) {
                    result = invokeAndParse(request, payLoad, target);
                    logger.trace("The request result is : {}", result);
                }

                if (result != null && result != "") {
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

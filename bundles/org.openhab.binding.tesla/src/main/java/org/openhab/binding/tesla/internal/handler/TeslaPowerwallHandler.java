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
package org.openhab.binding.tesla.internal.handler;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator; 
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
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
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.TeslaBindingConstants.EventKeys;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler.Authenticator;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler.Request;
import org.openhab.binding.tesla.internal.protocol.Powerwall;
import org.openhab.binding.tesla.internal.protocol.PowerwallState;
import org.openhab.binding.tesla.internal.throttler.QueueChannelThrottler;
import org.openhab.binding.tesla.internal.throttler.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TeslaPowerwallHandler} is responsible for handling commands, which are sent
 * to one of the channels of a specific powerwall
 *
 * @author Paul Smedley - Initial contribution
 */
public class TeslaPowerwallHandler extends TeslaVehicleHandler {

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 3000;
    private static final int EVENT_STREAM_READ_TIMEOUT = 200000;
    private static final int EVENT_STREAM_PAUSE = 5000;
    private static final int EVENT_TIMESTAMP_AGE_LIMIT = 3000;
    private static final int EVENT_TIMESTAMP_MAX_DELTA = 10000;
    private static final int FAST_STATUS_REFRESH_INTERVAL = 15000;
    private static final int EVENT_MAXIMUM_ERRORS_IN_INTERVAL = 10;
    private static final int EVENT_ERROR_INTERVAL_SECONDS = 15;
    private static final int API_SLEEP_INTERVAL_MINUTES = 20;
    private static final int MOVE_THRESHOLD_INTERVAL_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(TeslaPowerwallHandler.class);

    protected WebTarget eventTarget;

    // Vehicle state variables
    protected Powerwall powerwall;
    protected String powerwallJSON;
    protected PowerwallState powerwallState;
    protected TeslaAccountHandler account;

    protected QueueChannelThrottler stateThrottler;

    protected Client eventClient = ClientBuilder.newClient();
    protected TeslaChannelSelectorProxy teslaChannelSelectorProxy = new TeslaChannelSelectorProxy();
    protected Thread eventThread;
    protected ScheduledFuture<?> fastStateJob;

    private final Gson gson = new Gson();
    private final JsonParser parser = new JsonParser();

    public TeslaPowerwallHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Initializing the Tesla Powerwall handler for {}", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("We don't do anything yet but we got here!");
        account = (TeslaAccountHandler) getBridge().getHandler();
        scheduler.execute(() -> queryPowerwallAndUpdate());

//        lock.lock();
        try {
            Map<Object, Rate> channels = new HashMap<>();
            channels.put(DATA_THROTTLE, new Rate(1, 1, TimeUnit.SECONDS));
            channels.put(COMMAND_THROTTLE, new Rate(20, 1, TimeUnit.MINUTES));

            Rate firstRate = new Rate(20, 1, TimeUnit.MINUTES);
            Rate secondRate = new Rate(200, 10, TimeUnit.MINUTES);
            stateThrottler = new QueueChannelThrottler(firstRate, scheduler, channels);
            stateThrottler.addRate(secondRate);

            if (fastStateJob == null || fastStateJob.isCancelled()) {
                fastStateJob = scheduler.scheduleWithFixedDelay(fastStateRunnable, 0, FAST_STATUS_REFRESH_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }
        } finally {
//            lock.unlock();
        }
/*
        if (enableEvents) {
            if (eventThread == null) {
                eventThread = new Thread(eventRunnable, "openHAB-Tesla-Events-" + getThing().getUID());
                eventThread.start();
            }
        }
*/
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Tesla handler for {}", getThing().getUID());
        try {
            if (fastStateJob != null && !fastStateJob.isCancelled()) {
                fastStateJob.cancel(true);
                fastStateJob = null;
            }

            if (eventThread != null && !eventThread.isInterrupted()) {
                eventThread.interrupt();
                eventThread = null;
            }
        } finally {
        }

        eventClient.close();
    }

    /**
     * Retrieves the unique vehicle id this handler is associated with
     *
     * @return the vehicle id
     */
    public String getVehicleId() {
        return powerwall.id;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {} {}", channelUID, command);
        String channelID = channelUID.getId();
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromChannelID(channelID);

        if (command instanceof RefreshType) {
            // Request the state of all known variables. This is sub-optimal, but the requests get scheduled and
            // throttled so we are safe not to break the Tesla SLA
            requestAllData();
        } else {
            if (selector != null) {
                try {
// do nothing                  
                } catch (IllegalArgumentException e) {
                    logger.warn(
                            "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                            channelID, command.toString());
                }
            }
        }
    }

    public void sendCommand(String command, String payLoad, WebTarget target) {
        Request request = account.newRequest(this, command, payLoad, target);
    }

    public void sendCommand(String command) {
        sendCommand(command, "{}");
    }

    public void sendCommand(String command, String payLoad) {
        Request request = account.newRequest(this, command, payLoad, account.commandTarget);
    }

    public void sendCommand(String command, WebTarget target) {
        Request request = account.newRequest(this, command, "{}", target);
    }

    public void requestData(String command, String payLoad) {
        logger.debug("requestData, command = {}, payload = {}, powerwallTarget = {}",command,payLoad, account.powerwallTarget);
        Request request = account.newRequest(this, command, payLoad, account.powerwallTarget);
    }

    @Override
    protected void updateStatus(@NonNull ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    protected void updateStatus(@NonNull ThingStatus status, @NonNull ThingStatusDetail statusDetail) {
        super.updateStatus(status, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public void requestData(String command) {
        logger.debug("requestData(), command = {}",command);
        requestData(command, null);
    }

    public void queryPowerwall(String parameter) {
        WebTarget target = account.powerwallTarget.path(parameter);
        sendCommand(parameter, null, target);
    }

    public void requestAllData() {
          String battery_id = (String) getConfig().get(BATTERY_ID);
          String result = "";
          result = account.invokeAndParse(battery_id, "powerwall_state", null, account.powerwallTarget);
          parseAndUpdate("powerwall_state", null, result);
          result = account.invokeAndParse(battery_id, "powerwall_stats", null, account.powerwallStatsTarget);
          parseAndUpdate("powerwall_stats", null, result);
    }

    protected Powerwall queryPowerwall() {
        String authHeader = account.getAuthHeader();

        if (authHeader != null) {
            try {
                // get a list of powerwalls
                Response response = account.productsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Authorization", authHeader).get();

                logger.debug("Querying the products : Response : {}:{}", response.getStatus(), response.getStatusInfo());

                if (!checkResponse(response, true)) {
                    logger.error("An error occurred while querying the product");
                    return null;
                }

                JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
                Powerwall[] powerwallArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Powerwall[].class);

                for (Powerwall powerwall : powerwallArray) {
                    logger.debug("Querying the powerwall: ID {}", powerwall.id);
                    if (powerwall.id.equals(getConfig().get(BATTERY_ID))) {
                        powerwallJSON = gson.toJson(powerwall);
                        parseAndUpdate("queryPowerwall", null, powerwallJSON);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Powerwall sitename is {}/battery_id {}", powerwall.site_name, powerwall.id);
                        }
                        return powerwall;
                    }
                }
            } catch (ProcessingException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    protected void queryPowerwallAndUpdate() {
        powerwall = queryPowerwall();
        
        if (powerwall != null) {
            parseAndUpdate("queryPowerwall", null, powerwallJSON);
        } else {
            logger.debug("powerwall == null");
        }
    }

    public void parseAndUpdate(String request, String payLoad, String result) {
        final Double LOCATION_THRESHOLD = .0000001;
        logger.debug("parseandupdate");
        JsonObject jsonObject = null;

        try {
            if (request != null && result != null && !"null".equals(result)) {
                updateStatus(ThingStatus.ONLINE);
                //logger.debug("request = {}, result = {}", request, result);
                // first, update state objects
                switch (request) {
                    case "queryPowerwall": {
                        if (powerwall != null ) {
                            // in case powerwall changed to awake, refresh all data
                                logger.debug("Powerwall is now awake, updating all data");
                                requestAllData();
                        }

                        break;
                    }
                    case "powerwall_state": {
                        powerwallState = gson.fromJson(result, PowerwallState.class);
                        jsonObject = parser.parse(result).getAsJsonObject();
                        updateState("reservepercent", new QuantityType<Dimensionless>(new Float(powerwallState.backup.get("backup_reserve_percent").toString()),
                                   SmartHomeUnits.PERCENT));
                        Iterator < JsonElement > itr = powerwallState.power_reading.iterator();
                        JsonObject power_readingjsonObject = (JsonObject) itr.next();
                        updateState("solarpower", new QuantityType<Power>(new Float(power_readingjsonObject.get("solar_power").toString()), SmartHomeUnits.WATT));
                        updateState("loadpower", new QuantityType<Power>(new Float(power_readingjsonObject.get("load_power").toString()), SmartHomeUnits.WATT));
                        updateState("gridpower", new QuantityType<Power>(new Float(power_readingjsonObject.get("grid_power").toString()), SmartHomeUnits.WATT));
                        updateState("powerwallpower", new QuantityType<Power>(new Float(power_readingjsonObject.get("battery_power").toString()), SmartHomeUnits.WATT));
                        updateState("gridstatus", new StringType(powerwallState.grid_status));
                        updateState("currentopstate", new StringType(powerwallState.operation));
                        if (powerwallState.user_settings.get("storm_mode_enabled").toString() == "true") 
                            updateState("stormwatch", OnOffType.ON);
                        else
                            updateState("stormwatch", OnOffType.OFF);

                        break;
                    }
                    case "powerwall_stats": {
                        jsonObject = parser.parse(result).getAsJsonObject();
                        updateState("batterypercent", new QuantityType<Dimensionless>(new Float(jsonObject.get("percentage_charged").toString()),
                                   SmartHomeUnits.PERCENT));
                        break;
                    }

                }

                // secondly, reformat the response string to a JSON compliant
                // object for some specific non-JSON compatible requests
                switch (request) {
                    case MOBILE_ENABLED_STATE: {
                        jsonObject = new JsonObject();
                        jsonObject.addProperty(MOBILE_ENABLED_STATE, result);
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
//                        lock.lock();

                        boolean proceed = true;
                        if (resultTimeStamp < lastTimeStamp && request == DRIVE_STATE) {
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
                                            if (logger.isTraceEnabled()) {
                                                logger.trace(
                                                        "The variable/value pair '{}':'{}' is successfully processed",
                                                        entry.getKey(), entry.getValue());
                                            }
                                        } else {
                                            updateState(selector.getChannelID(), UnDefType.UNDEF);
                                        }
                                    } else {
                                        if (!entry.getValue().isJsonNull()) {
                                            Map<String, String> properties = editProperties();
                                            properties.put(selector.getChannelID(), entry.getValue().getAsString());
                                            updateProperties(properties);
                                            if (logger.isTraceEnabled()) {
                                                logger.trace(
                                                        "The variable/value pair '{}':'{}' is successfully used to set property '{}'",
                                                        entry.getKey(), entry.getValue(), selector.getChannelID());
                                            }
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
//                        lock.unlock();
                    }
                }
            }
        } catch (Exception p) {
            logger.error("An exception occurred while parsing data received from the powerwall: '{}'", p.getMessage());
        }
    }

    protected Runnable fastStateRunnable = () -> {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            boolean allowQuery = allowQuery();

            if (allowQuery) {
              requestAllData();
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
                            .register(new Authenticator((String) getConfig().get(CONFIG_USERNAME), vehicle.tokens[0]));
                    eventTarget = eventClient.target(URI_EVENT).path(vehicle.vehicle_id + "/").queryParam("values",
                            StringUtils.join(EventKeys.values(), ',', 1, EventKeys.values().length));
                    eventResponse = eventTarget.request(MediaType.TEXT_PLAIN_TYPE).get();

                    logger.debug("Event Stream: Establishing the event stream: Response: {}:{}",
                            eventResponse.getStatus(), eventResponse.getStatusInfo());

                    if (eventResponse.getStatus() == 200) {
                        InputStream dummy = (InputStream) eventResponse.getEntity();
                        eventInputStreamReader = new InputStreamReader(dummy);
                        eventBufferedReader = new BufferedReader(eventInputStreamReader);
                        isEstablished = true;
                    } else if (eventResponse.getStatus() == 401) {
                        isEstablished = false;
                    } else {
                        isEstablished = false;
                    }

                    if (!isEstablished) {
                        eventIntervalErrors++;
                        if (eventIntervalErrors >= EVENT_MAXIMUM_ERRORS_IN_INTERVAL) {
                            logger.warn(
                                    "Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                                    EVENT_MAXIMUM_ERRORS_IN_INTERVAL, EVENT_ERROR_INTERVAL_SECONDS);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                            eventClient.close();
                        }

                        if ((System.currentTimeMillis() - eventIntervalTimestamp) > 1000
                                * EVENT_ERROR_INTERVAL_SECONDS) {
                            logger.trace("Resetting the error counter. ({} errors in the last interval)",
                                    eventIntervalErrors);
                            eventIntervalTimestamp = System.currentTimeMillis();
                            eventIntervalErrors = 0;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(
                        "Event stream: An exception occurred while establishing the event stream for the vehicle: '{}'",
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
                                    logger.debug("Event stream: Received an event: '{}'", line);
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
                                                logger.debug("Event Stream: Event stamp is {}",
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
                                                        "Event stream: Discarding an event with an out of sync timestamp {} (last is {})",
                                                        dateFormatter.format(new Date(currentTimeStamp)),
                                                        dateFormatter.format(new Date(lastTimeStamp)));
                                            }
                                        }
                                    } else {
                                        if (logger.isDebugEnabled()) {
                                            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                                                    "yyyy-MM-dd'T'HH:mm:ss.SSS");
                                            logger.debug(
                                                    "Event Stream: Discarding an event that differs {} ms from the system time: {} (system is {})",
                                                    systemTimeStamp - currentTimeStamp,
                                                    dateFormatter.format(currentTimeStamp),
                                                    dateFormatter.format(systemTimeStamp));
                                        }
                                        if (systemTimeStamp - currentTimeStamp > EVENT_TIMESTAMP_MAX_DELTA) {
                                            logger.trace("Event stream: The event stream will be reset");
                                            isEstablished = false;
                                        }
                                    }
                                    line = eventBufferedReader.readLine();
                                }
                                logger.trace("Event stream: The end of stream was reached");
                                isEstablished = false;
                            }
                        } else {
                            logger.debug("Event stream: The vehicle is not awake");
                            if (vehicle != null) {
                                if (allowWakeUp) {
                                    // wake up the vehicle until streaming token <> 0
                                    logger.debug("Event stream: Waking up the vehicle");
                                    wakeUp();
                                }
                            } else {
                                vehicle = queryVehicle();
                            }
                            Thread.sleep(EVENT_STREAM_PAUSE);
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    logger.debug("Event stream: An exception occurred while reading events: '{}'", e.getMessage());
                    isEstablished = false;
                } catch (InterruptedException e) {
                    isEstablished = false;
                }

                if (Thread.interrupted()) {
                    logger.debug("Event stream: the event stream was interrupted");
                    return;
                }
            }
        }
    };

}

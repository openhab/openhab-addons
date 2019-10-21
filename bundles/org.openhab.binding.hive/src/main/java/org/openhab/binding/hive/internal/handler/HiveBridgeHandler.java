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
package org.openhab.binding.hive.internal.handler;

import static org.openhab.binding.hive.internal.HiveBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.discovery.HiveDiscoveryService;
import org.openhab.binding.hive.internal.dto.HiveAttributes;
import org.openhab.binding.hive.internal.dto.HiveLoginResponse;
import org.openhab.binding.hive.internal.dto.HiveNode;
import org.openhab.binding.hive.internal.dto.HiveNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link HiveBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Foot - Initial contribution
 */
@NonNullByDefault
public class HiveBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HiveBridgeHandler.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private String username = "";
    private String password = "";
    public String token = "";
    private static HttpClient client = new HttpClient(new SslContextFactory(true));
    private Boolean online = false;
    protected Gson gson = new Gson();
    @Nullable public HiveDiscoveryService hiveDiscoveryService;
    @Nullable protected ScheduledFuture<?> refreshJob;

    public HiveBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    @Override
    public void initialize() {
        // See if our login works
        if (getToken()) {
            updateStatus(ThingStatus.ONLINE);
            online = true;
            if (hiveDiscoveryService != null) {
                hiveDiscoveryService.startBackgroundDiscovery();
            }
            checkForDevices();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to login, please check your username/password");
        }
    }

    public void requestRefresh() {
        startAutomaticRefresh();
    }

    private void startAutomaticRefresh() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        refreshJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateChannels();
            }
        }, 5, 30, TimeUnit.SECONDS);
    }

    private void updateChannels() {
        if (getThing().getThings().isEmpty()) {
            return;
        }
        for (Thing handler : getThing().getThings()) {
            ThingHandler thingHandler = handler.getHandler();
            if (thingHandler instanceof HiveThermostatHandler) {
                HiveThermostatHandler thermostatHandler = (HiveThermostatHandler) thingHandler;
                thermostatHandler.updateChannel();
            }
        }
    }

    @Override
    public void dispose() {
        if (hiveDiscoveryService != null) {
            hiveDiscoveryService.stopBackgroundDiscovery();
        }
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private boolean getToken() {
        Configuration configuration = getConfig();

        username = (String) configuration.get(CONFIG_USER_NAME);
        password = (String) configuration.get(CONFIG_PASSWORD);
        token = (String) configuration.get(CONFIG_TOKEN);

        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                logger.warn("Unable to start httpclient: {}", e.getMessage());
                return false;
            }
        }
        // Test the token to see if it's working, if it is, use it
        ContentResponse response;
        int statusCode = 0;

        JsonObject sessionObject = new JsonObject();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("caller", "WEB");
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", password);
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        sessionObject.add("sessions", array);

        Request request = client.POST("https://api-prod.bgchprod.info:443/omnia/auth/sessions");
        request.content(new StringContentProvider(gson.toJson(sessionObject)), "application/json");
        request.timeout(5000, TimeUnit.MILLISECONDS);
        request.header("Accept", "application/vnd.alertme.zoo-6.1+json");
        request.header("X-Omnia-Client", "Openhab 2");

        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Unable to communicate with Hive API: {}", e.getMessage());
            return false;
        }

        statusCode = response.getStatus();

        if (statusCode != HttpStatus.OK_200) {
            String statusLine = response.getStatus() + " " + response.getReason();
            logger.warn("Error communicating with Hive API: {}", statusLine);
            return false;
        }

        HiveLoginResponse o = gson.fromJson(response.getContentAsString(), HiveLoginResponse.class);
        if (o.sessions != null) {
            configuration.put(CONFIG_TOKEN, o.sessions.get(0).sessionId);
            token = o.sessions.get(0).sessionId;
            return true;
        }
        logger.warn("Hive API did not provide token: {}", response.getContentAsString());

        return false;
    }

    public void checkForDevices() {
        if (online) {
            ContentResponse response;
            try {
                response = client.newRequest("https://api-prod.bgchprod.info:443/omnia/nodes").method(HttpMethod.GET)
                        .header("Accept", "application/vnd.alertme.zoo-6.1+json")
                        .header("Content-Type", "application/vnd.alertme.zoo-6.1+json")
                        .header("X-Omnia-Client", "Openhab 2").header("X-Omnia-Access-Token", token)
                        .timeout(DISCOVER_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                if (e.getMessage().contains("WWW-Authenticate header")) {
                    getToken();
                } else {
                    logger.warn("Failed to get new token from Hive API: {}", e.getMessage());
                }
                return;
            }
            int statusCode = response.getStatus();

            if (statusCode == HttpStatus.UNAUTHORIZED_401) {
                // Token expired, get a new one and try again
                getToken();
                statusCode = response.getStatus();
            }

            if (statusCode != HttpStatus.OK_200) {
                String statusLine = response.getStatus() + " " + response.getReason();
                logger.warn("Error while reading from Hive API: {}", statusLine);
                return;
            }

            HiveNodes o = gson.fromJson(response.getContentAsString(), HiveNodes.class);

            if (o.nodes.size() > 0) {
                // Loop through the nodes and add thermostats but only if they are showing a temperature as the api
                // reports lots of thermostats for some reason
                HiveNode thermostat = null;
                HiveNode systemInfo = null;
                for (HiveNode node : o.nodes) {
                    if (node.attributes != null && node.attributes.nodeType != null
                            && node.attributes.nodeType.reportedValue.equals(THERMOSTAT_NODE_TYPE)
                            && node.attributes.temperature != null) {
                        // If this a thermostat, and it has a current temperature, add it to discovery results
                        thermostat = node;
                    }
                    if (node.attributes != null && node.attributes.batteryLevel != null) {
                        // If this a thermostat, and it has a current temperature, add it to discovery results
                        systemInfo = node;
                    }
                }
                if (thermostat != null) {
                    if (systemInfo != null) {
                        thermostat.linkedNode = systemInfo.id;
                        thermostat.firmwareVersion = systemInfo.attributes.softwareVersion.displayValue;
                        thermostat.model = systemInfo.attributes.model.displayValue;
                        thermostat.macAddress = systemInfo.attributes.macAddress.displayValue;
                    }
                    if (hiveDiscoveryService != null) {
                        hiveDiscoveryService.addDevice(thermostat);
                    }
                }
            }
        }
    }

    public HiveAttributes getThermostatReading(Thing thing) {
        HiveAttributes reading = new HiveAttributes();

        if (online) {
            ContentResponse response;
            int statusCode = 0;
            String responseString = "";
            HiveNodes o = null;

            // Get thermostat reading
            try {
                response = client.newRequest("https://api-prod.bgchprod.info:443/omnia/nodes/" + thing.getUID().getId())
                        .method(HttpMethod.GET).header("Accept", "application/vnd.alertme.zoo-6.1+json")
                        .header("Content-Type", "application/vnd.alertme.zoo-6.1+json")
                        .header("X-Omnia-Client", "Openhab 2").header("X-Omnia-Access-Token", token)
                        .timeout(DISCOVER_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();

                statusCode = response.getStatus();

                if (statusCode == HttpStatus.UNAUTHORIZED_401) {
                    // Token expired, get a new one and try again
                    getToken();
                    statusCode = response.getStatus();
                }

                if (statusCode != HttpStatus.OK_200) {
                    // If it failed, log the error
                    String statusLine = response.getStatus() + " " + response.getReason();
                    logger.warn("Error while reading from Hive API: {}", statusLine);
                    return reading;
                }

                responseString = response.getContentAsString();
                o = gson.fromJson(responseString, HiveNodes.class);
                reading = o.nodes.get(0).attributes;
                reading.isValid = true;
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Failed to get thermostat reading: {}", e.getMessage());
                return reading;
            }
            try {
                // Get battery level
                response = client
                        .newRequest("https://api-prod.bgchprod.info:443/omnia/nodes/"
                                + thing.getProperties().get("linkedDevice"))
                        .method(HttpMethod.GET).header("Accept", "application/vnd.alertme.zoo-6.1+json")
                        .header("Content-Type", "application/vnd.alertme.zoo-6.1+json")
                        .header("X-Omnia-Client", "Openhab 2").header("X-Omnia-Access-Token", token)
                        .timeout(DISCOVER_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();

                statusCode = response.getStatus();
                if (statusCode != HttpStatus.OK_200) {
                    // If it failed, log the error
                    String statusLine = response.getStatus() + " " + response.getReason();
                    logger.warn("Error while reading from Hive API: {}", statusLine);
                    return reading;
                }
                responseString = response.getContentAsString();
                o = gson.fromJson(responseString, HiveNodes.class);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Failed to get battery leve: {}", e.getMessage());
            }

            reading.batteryLevel = o.nodes.get(0).attributes.batteryLevel;
            return reading;
        }
        return reading;
    }

    private void callClient(String setObject, ThingUID uid, String type) {
        try {
            ContentResponse response;
            response = client.newRequest("https://api-prod.bgchprod.info:443/omnia/nodes/" + uid.getId())
                    .method(HttpMethod.PUT).header("Accept", "application/vnd.alertme.zoo-6.1+json")
                    .header("Content-Type", "application/vnd.alertme.zoo-6.1+json")
                    .header("X-Omnia-Client", "Openhab 2").header("X-Omnia-Access-Token", token)
                    .timeout(DISCOVER_TIMEOUT_SECONDS, TimeUnit.SECONDS).content(new StringContentProvider(setObject))
                    .send();

            int statusCode = response.getStatus();

            if (statusCode == HttpStatus.UNAUTHORIZED_401) {
                // Token expired, get a new one and try again
                getToken();
                statusCode = response.getStatus();
            }

            if (statusCode != HttpStatus.OK_200) {
                // If it failed, log the error
                String statusLine = response.getStatus() + " " + response.getReason();
                logger.warn("Error while reading from Hive API: {}", statusLine);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to update {}: {}", type, e.getMessage());
        }
    }

    public void boost(ThingUID uid, OnOffType boost, int duration) {
        if (online) {
            if (boost == OnOffType.ON) {
                String setObject = "{\"nodes\": [{\"attributes\": {\"activeHeatCoolMode\": {\"targetValue\": \"BOOST\"},"
                        + "\"scheduleLockDuration\": {\"targetValue\": " + duration + "}}}]}";
                callClient(setObject, uid, "boost");
            } else if (boost == OnOffType.OFF) {
                String setObject = "{\"nodes\": [{\"attributes\": {\"activeHeatCoolMode\": {\"targetValue\": \"HEAT\"}, "
                        + "\"activeScheduleLock\": {\"targetValue\": \"True\"}}}]}";
                callClient(setObject, uid, "boost");
            }
        }
    }

    public void setTargetTemperature(ThingUID uid, float f) {
        if (online) {
            String setObject = "{\"nodes\": [{\"attributes\": {\"targetHeatTemperature\": {\"targetValue\": " + f
                    + "}}}]}";
            callClient(setObject, uid, "target temperature");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(HiveDiscoveryService.class);
    }

    public void setDiscoveryService(HiveDiscoveryService thingDiscoveryService) {
        this.hiveDiscoveryService = thingDiscoveryService;
    }
}

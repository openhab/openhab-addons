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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.discovery.TeslaVehicleDiscoveryService;
import org.openhab.binding.tesla.internal.protocol.TokenRequest;
import org.openhab.binding.tesla.internal.protocol.TokenRequestPassword;
import org.openhab.binding.tesla.internal.protocol.TokenRequestRefreshToken;
import org.openhab.binding.tesla.internal.protocol.TokenResponse;
import org.openhab.binding.tesla.internal.protocol.Vehicle;
import org.openhab.binding.tesla.internal.protocol.VehicleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TeslaAccountHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 * @author Kai Kreuzer - refactored to use separate vehicle handlers
 */
public class TeslaAccountHandler extends BaseBridgeHandler {

    public static final int API_MAXIMUM_ERRORS_IN_INTERVAL = 2;
    public static final int API_ERROR_INTERVAL_SECONDS = 15;
    private static final int CONNECT_RETRY_INTERVAL = 15000;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Logger logger = LoggerFactory.getLogger(TeslaAccountHandler.class);

    // REST Client API variables
    private final Client teslaClient = ClientBuilder.newClient();
    private final WebTarget teslaTarget = teslaClient.target(URI_OWNERS);
    private final WebTarget tokenTarget = teslaTarget.path(URI_ACCESS_TOKEN);
    final WebTarget vehiclesTarget = teslaTarget.path(API_VERSION).path(VEHICLES);
    final WebTarget vehicleTarget = vehiclesTarget.path(PATH_VEHICLE_ID);
    final WebTarget dataRequestTarget = vehicleTarget.path(PATH_DATA_REQUEST);
    final WebTarget commandTarget = vehicleTarget.path(PATH_COMMAND);
    final WebTarget wakeUpTarget = vehicleTarget.path(PATH_WAKE_UP);

    // Threading and Job related variables
    protected ScheduledFuture<?> connectJob;

    protected long lastTimeStamp;
    protected long apiIntervalTimestamp;
    protected int apiIntervalErrors;
    protected long eventIntervalTimestamp;
    protected int eventIntervalErrors;
    protected ReentrantLock lock;

    private final Gson gson = new Gson();
    private final JsonParser parser = new JsonParser();

    private TokenResponse logonToken;
    private final Set<VehicleListener> vehicleListeners = new HashSet<>();

    public TeslaAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Tesla account handler for {}", this.getStorageKey());

        updateStatus(ThingStatus.UNKNOWN);

        lock = new ReentrantLock();
        lock.lock();

        try {
            if (connectJob == null || connectJob.isCancelled()) {
                connectJob = scheduler.scheduleWithFixedDelay(connectRunnable, 0, CONNECT_RETRY_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        logger.trace("Disposing the Tesla account handler for {}", getThing().getUID());

        lock.lock();
        try {
            if (connectJob != null && !connectJob.isCancelled()) {
                connectJob.cancel(true);
                connectJob = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void scanForVehicles() {
        scheduler.execute(() -> queryVehicles());
    }

    public void addVehicleListener(VehicleListener listener) {
        this.vehicleListeners.add(listener);
    }

    public void removeVehicleListener(VehicleListener listener) {
        this.vehicleListeners.remove(listener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    public String getAuthHeader() {
        if (logonToken != null) {
            return "Bearer " + logonToken.access_token;
        } else {
            return null;
        }
    }

    protected boolean checkResponse(Response response, boolean immediatelyFail) {
        if (response != null && response.getStatus() == 200) {
            return true;
        } else {
            apiIntervalErrors++;
            if (immediatelyFail || apiIntervalErrors >= API_MAXIMUM_ERRORS_IN_INTERVAL) {
                if (immediatelyFail) {
                    logger.warn("Got an unsuccessful result, setting vehicle to offline and will try again");
                } else {
                    logger.warn("Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                            API_MAXIMUM_ERRORS_IN_INTERVAL, API_ERROR_INTERVAL_SECONDS);
                }

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else if ((System.currentTimeMillis() - apiIntervalTimestamp) > 1000 * API_ERROR_INTERVAL_SECONDS) {
                logger.trace("Resetting the error counter. ({} errors in the last interval)", apiIntervalErrors);
                apiIntervalTimestamp = System.currentTimeMillis();
                apiIntervalErrors = 0;
            }
        }

        return false;
    }

    protected Vehicle[] queryVehicles() {
        String authHeader = getAuthHeader();

        if (authHeader != null) {
            // get a list of vehicles
            Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", authHeader).get();

            logger.debug("Querying the vehicle: Response: {}:{}", response.getStatus(), response.getStatusInfo());

            if (!checkResponse(response, true)) {
                logger.error("An error occurred while querying the vehicle");
                return null;
            }

            JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
            Vehicle[] vehicleArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Vehicle[].class);

            for (Vehicle vehicle : vehicleArray) {
                String responseString = invokeAndParse(vehicle.id, VEHICLE_CONFIG, null, dataRequestTarget);
                if (StringUtils.isBlank(responseString)) {
                    continue;
                }
                VehicleConfig vehicleConfig = gson.fromJson(responseString, VehicleConfig.class);
                for (VehicleListener listener : vehicleListeners) {
                    listener.vehicleFound(vehicle, vehicleConfig);
                }
                for (Thing vehicleThing : getThing().getThings()) {
                    if (vehicle.vin.equals(vehicleThing.getConfiguration().get(VIN))) {
                        TeslaVehicleHandler vehicleHandler = (TeslaVehicleHandler) vehicleThing.getHandler();
                        if (vehicleHandler != null) {
                            logger.debug("Querying the vehicle: VIN {}", vehicle.vin);
                            String vehicleJSON = gson.toJson(vehicle);
                            vehicleHandler.parseAndUpdate("queryVehicle", null, vehicleJSON);
                            logger.trace("Vehicle is id {}/vehicle_id {}/tokens {}", vehicle.id, vehicle.vehicle_id,
                                    vehicle.tokens);
                        }
                    }
                }
            }
            return vehicleArray;
        } else {
            return new Vehicle[0];
        }
    }

    private String getStorageKey() {
        return this.getThing().getUID().getId();
    }

    private ThingStatusInfo authenticate() {
        TokenResponse token = logonToken;

        boolean hasExpired = true;

        if (token != null) {
            Instant tokenCreationInstant = Instant.ofEpochMilli(token.created_at * 1000);
            logger.debug("Found a request token created at {}", dateFormatter.format(tokenCreationInstant));
            Instant tokenExpiresInstant = Instant.ofEpochMilli(token.created_at * 1000 + 60 * token.expires_in);

            if (tokenExpiresInstant.isBefore(Instant.now())) {
                logger.debug("The token has expired at {}", dateFormatter.format(tokenExpiresInstant));
                hasExpired = true;
            } else {
                hasExpired = false;
            }
        }

        if (hasExpired) {
            String username = (String) getConfig().get(CONFIG_USERNAME);
            String refreshToken = (String) getConfig().get(CONFIG_REFRESHTOKEN);
            if (refreshToken == null || StringUtils.isEmpty(refreshToken)) {
                if (!StringUtils.isEmpty(username)) {
                    String password = (String) getConfig().get(CONFIG_PASSWORD);
                    return authenticate(username, password);
                } else {
                    return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Neither a refresh token nor credentials are provided.");
                }
            }

            TokenRequestRefreshToken tokenRequest = null;
            try {
                tokenRequest = new TokenRequestRefreshToken(refreshToken);
            } catch (GeneralSecurityException e) {
                logger.error("An exception occurred while requesting a new token: '{}'", e.getMessage(), e);
            }

            String payLoad = gson.toJson(tokenRequest);
            Response response = null;
            try {
                response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
            } catch (ProcessingException e) {
                return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            logger.debug("Authenticating: Response: {}:{}", response.getStatus(), response.getStatusInfo());

            if (response.getStatus() == 200 && response.hasEntity()) {
                String responsePayLoad = response.readEntity(String.class);
                TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);
                if (!refreshToken.equals(tokenResponse.refresh_token)) {
                    Configuration configuration = editConfiguration();
                    configuration.put(CONFIG_REFRESHTOKEN, tokenResponse.refresh_token);
                    updateConfiguration(configuration);
                }

                if (!StringUtils.isEmpty(tokenResponse.access_token)) {
                    this.logonToken = tokenResponse;
                    logger.trace("Access Token is {}", logonToken.access_token);
                }
                return new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            } else if (response.getStatus() == 401) {
                if (!StringUtils.isEmpty(username)) {
                    String password = (String) getConfig().get(CONFIG_PASSWORD);
                    return authenticate(username, password);
                } else {
                    return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Refresh token is not valid and no credentials are provided.");
                }
            } else {
                return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "HTTP returncode " + response.getStatus());
            }
        }
        return new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    private ThingStatusInfo authenticate(String username, String password) {
        TokenRequest token = null;
        try {
            token = new TokenRequestPassword(username, password);
        } catch (GeneralSecurityException e) {
            logger.error("An exception occurred while building a password request token: '{}'", e.getMessage(), e);
        }

        if (token != null) {
            String payLoad = gson.toJson(token);

            Response response = tokenTarget.request().post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));

            if (response != null) {
                logger.debug("Authenticating: Response : {}:{}", response.getStatus(), response.getStatusInfo());

                if (response.getStatus() == 200 && response.hasEntity()) {
                    String responsePayLoad = response.readEntity(String.class);
                    TokenResponse tokenResponse = gson.fromJson(responsePayLoad.trim(), TokenResponse.class);

                    if (StringUtils.isNotEmpty(tokenResponse.access_token)) {
                        this.logonToken = tokenResponse;
                        Configuration cfg = editConfiguration();
                        cfg.put(TeslaBindingConstants.CONFIG_REFRESHTOKEN, logonToken.refresh_token);
                        cfg.remove(TeslaBindingConstants.CONFIG_PASSWORD);
                        updateConfiguration(cfg);
                        return new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                    }
                } else if (response.getStatus() == 401) {
                    return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Invalid credentials.");
                } else {
                    return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "HTTP returncode " + response.getStatus());
                }
            } else {
                logger.debug("Authenticating: Response was null");
                return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed retrieving a response from the server.");
            }
        }
        return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Cannot build request from credentials.");
    }

    protected String invokeAndParse(String vehicleId, String command, String payLoad, WebTarget target) {
        logger.debug("Invoking: {}", command);

        if (vehicleId != null) {
            Response response;

            if (payLoad != null) {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicleId).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                } else {
                    response = target.resolveTemplate("vid", vehicleId).request()
                            .header("Authorization", "Bearer " + logonToken.access_token)
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                }
            } else {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicleId)
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                } else {
                    response = target.resolveTemplate("vid", vehicleId).request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();
                }
            }

            if (!checkResponse(response, false)) {
                logger.debug("An error occurred while communicating with the vehicle during request {}: {}:{}", command,
                        (response != null) ? response.getStatus() : "",
                        (response != null) ? response.getStatusInfo() : "No Response");
                return null;
            }

            try {
                JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
                logger.trace("Request : {}:{}:{} yields {}", command, payLoad, target, jsonObject.get("response"));
                return jsonObject.get("response").toString();
            } catch (Exception e) {
                logger.error("An exception occurred while invoking a REST request: '{}'", e.getMessage());
            }
        }

        return null;
    }

    protected Runnable connectRunnable = () -> {
        try {
            lock.lock();

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                logger.debug("Setting up an authenticated connection to the Tesla back-end");

                ThingStatusInfo authenticationResult = authenticate();
                updateStatus(authenticationResult.getStatus(), authenticationResult.getStatusDetail(),
                        authenticationResult.getDescription());

                if (authenticationResult.getStatus() == ThingStatus.ONLINE) {
                    // get a list of vehicles
                    Response response = vehiclesTarget.request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", "Bearer " + logonToken.access_token).get();

                    if (response != null && response.getStatus() == 200 && response.hasEntity()) {
                        updateStatus(ThingStatus.ONLINE);
                        for (Vehicle vehicle : queryVehicles()) {
                            Bridge bridge = getBridge();
                            if (bridge != null) {
                                List<Thing> things = bridge.getThings();
                                for (int i = 0; i < things.size(); i++) {
                                    Thing thing = things.get(i);
                                    TeslaVehicleHandler handler = (TeslaVehicleHandler) thing.getHandler();
                                    if (handler != null) {
                                        if (vehicle.vin.equals(thing.getConfiguration().get(VIN))) {
                                            logger.debug(
                                                    "Found the vehicle with VIN '{}' in the list of vehicles you own",
                                                    getConfig().get(VIN));
                                            apiIntervalErrors = 0;
                                            apiIntervalTimestamp = System.currentTimeMillis();
                                        } else {
                                            logger.warn(
                                                    "Unable to find the vehicle with VIN '{}' in the list of vehicles you own",
                                                    getConfig().get(VIN));
                                            handler.updateStatus(ThingStatus.OFFLINE,
                                                    ThingStatusDetail.CONFIGURATION_ERROR,
                                                    "Vin is not available through this account.");
                                        }
                                    }
                                }
                            }
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
            logger.error("An exception occurred while connecting to the Tesla back-end: '{}'", e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    };

    public static class Authenticator implements ClientRequestFilter {
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
            return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected class Request implements Runnable {

        private TeslaVehicleHandler handler;
        private String request;
        private String payLoad;
        private WebTarget target;

        public Request(TeslaVehicleHandler handler, String request, String payLoad, WebTarget target) {
            this.handler = handler;
            this.request = request;
            this.payLoad = payLoad;
            this.target = target;
        }

        @Override
        public void run() {
            try {
                String result = "";

                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    result = invokeAndParse(handler.getVehicleId(), request, payLoad, target);
                    if (result != null && !"".equals(result)) {
                        handler.parseAndUpdate(request, payLoad, result);
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while executing a request to the vehicle: '{}'", e.getMessage(), e);
            }
        }

    }

    public Request newRequest(TeslaVehicleHandler teslaVehicleHandler, String command, String payLoad,
            WebTarget target) {
        return new Request(teslaVehicleHandler, command, payLoad, target);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(TeslaVehicleDiscoveryService.class);
    }

}

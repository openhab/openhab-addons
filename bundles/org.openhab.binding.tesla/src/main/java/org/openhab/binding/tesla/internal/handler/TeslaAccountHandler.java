/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.discovery.TeslaVehicleDiscoveryService;
import org.openhab.binding.tesla.internal.protocol.dto.Vehicle;
import org.openhab.binding.tesla.internal.protocol.dto.VehicleConfig;
import org.openhab.binding.tesla.internal.protocol.dto.VehicleData;
import org.openhab.binding.tesla.internal.protocol.dto.sso.TokenResponse;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeMigrationService;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
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
@NonNullByDefault
public class TeslaAccountHandler extends BaseBridgeHandler {

    public static final int API_MAXIMUM_ERRORS_IN_INTERVAL = 3;
    public static final int API_ERROR_INTERVAL_SECONDS = 15;
    private static final int CONNECT_RETRY_INTERVAL = 15000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Logger logger = LoggerFactory.getLogger(TeslaAccountHandler.class);

    // REST Client API variables
    private final WebTarget teslaTarget;
    WebTarget vehiclesTarget; // this cannot be marked final as it is used in the runnable
    WebTarget productsTarget; // this cannot be marked final as it is used in the runnable
    final WebTarget vehicleTarget;
    final WebTarget dataRequestTarget;
    final WebTarget commandTarget;
    final WebTarget wakeUpTarget;

    private final TeslaSSOHandler ssoHandler;
    private final ThingTypeMigrationService thingTypeMigrationService;

    // Threading and Job related variables
    @Nullable
    protected ScheduledFuture<?> connectJob;

    protected long lastTimeStamp;
    protected long apiIntervalTimestamp;
    protected int apiIntervalErrors;
    protected long eventIntervalTimestamp;
    protected int eventIntervalErrors;

    protected ReentrantLock lock = new ReentrantLock();

    private final Gson gson = new Gson();

    @Nullable
    private TokenResponse logonToken;
    private final Set<VehicleListener> vehicleListeners = new HashSet<>();

    public TeslaAccountHandler(Bridge bridge, Client teslaClient, HttpClientFactory httpClientFactory,
            ThingTypeMigrationService thingTypeMigrationService) {
        super(bridge);
        this.teslaTarget = teslaClient.target(URI_OWNERS);
        this.ssoHandler = new TeslaSSOHandler(httpClientFactory.getCommonHttpClient());
        this.thingTypeMigrationService = thingTypeMigrationService;

        this.vehiclesTarget = teslaTarget.path(API_VERSION).path(VEHICLES);
        this.productsTarget = teslaTarget.path(API_VERSION).path(PRODUCTS);
        this.vehicleTarget = vehiclesTarget.path(PATH_VEHICLE_ID);
        this.dataRequestTarget = vehicleTarget.path(PATH_DATA_REQUEST).queryParam("endpoints",
                "location_data;charge_state;climate_state;vehicle_state;gui_settings;vehicle_config");
        this.commandTarget = vehicleTarget.path(PATH_COMMAND);
        this.wakeUpTarget = vehicleTarget.path(PATH_WAKE_UP);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Tesla account handler for {}", this.getStorageKey());

        updateStatus(ThingStatus.UNKNOWN);

        connectJob = scheduler.scheduleWithFixedDelay(connectRunnable, 0, CONNECT_RETRY_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Tesla account handler for {}", getThing().getUID());
        ScheduledFuture<?> connectJob = this.connectJob;
        if (connectJob != null && !connectJob.isCancelled()) {
            connectJob.cancel(true);
            this.connectJob = null;
        }
    }

    public void scanForVehicles() {
        scheduler.execute(this::queryVehicles);
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

    public @Nullable String getAuthHeader() {
        String accessToken = getAccessToken();
        if (accessToken != null) {
            return "Bearer " + accessToken;
        } else {
            return null;
        }
    }

    public @Nullable String getAccessToken() {
        TokenResponse logonToken = this.logonToken;
        if (logonToken != null) {
            return logonToken.accessToken;
        } else {
            return null;
        }
    }

    protected boolean checkResponse(@Nullable Response response, boolean immediatelyFail) {
        if (response != null && response.getStatus() == 200) {
            return true;
        } else if (response != null && response.getStatus() == 401) {
            logger.debug("The access token has expired, trying to get a new one.");
            ThingStatusInfo authenticationResult = authenticate();
            updateStatus(authenticationResult.getStatus(), authenticationResult.getStatusDetail(),
                    authenticationResult.getDescription());
        } else {
            apiIntervalErrors++;
            if (immediatelyFail || apiIntervalErrors >= API_MAXIMUM_ERRORS_IN_INTERVAL) {
                if (immediatelyFail) {
                    logger.warn("Got an unsuccessful result, setting vehicle to offline and will try again");
                } else {
                    logger.warn("Reached the maximum number of errors ({}) for the current interval ({} seconds)",
                            API_MAXIMUM_ERRORS_IN_INTERVAL, API_ERROR_INTERVAL_SECONDS);
                    apiIntervalErrors = 0;
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
            Response response = productsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", authHeader).get();

            logger.debug("Querying the vehicle: Response: {}: {}", response.getStatus(),
                    response.getStatusInfo().getReasonPhrase());

            if (!checkResponse(response, true)) {
                logger.debug("An error occurred while querying the vehicle");
                return new Vehicle[0];
            }

            JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();
            Vehicle[] vehicleArray = gson.fromJson(jsonObject.getAsJsonArray("response"), Vehicle[].class);
            if (vehicleArray == null) {
                logger.debug("Response resulted in unexpected null array");
                return new Vehicle[0];
            }
            for (Vehicle vehicle : vehicleArray) {
                String responseString = invokeAndParse(vehicle.id, null, null, dataRequestTarget, 0);
                VehicleConfig vehicleConfig = null;
                if (responseString != null && !responseString.isBlank()) {
                    VehicleData vehicleData = gson.fromJson(responseString, VehicleData.class);
                    if (vehicleData != null) {
                        vehicleConfig = vehicleData.vehicleConfig;
                    }
                }
                for (VehicleListener listener : vehicleListeners) {
                    listener.vehicleFound(vehicle, vehicleConfig);
                }
                for (Thing vehicleThing : getThing().getThings()) {
                    if (vehicle.vin.equals(vehicleThing.getConfiguration().get(VIN))) {
                        TeslaVehicleHandler vehicleHandler = (TeslaVehicleHandler) vehicleThing.getHandler();
                        if (vehicleHandler != null) {
                            if (TeslaBindingConstants.THING_TYPE_VEHICLE.equals(vehicleThing.getThingTypeUID())
                                    && vehicleConfig != null) {
                                // Seems the type of this vehicle has not been identified before, so let's switch the
                                // thing type of it
                                thingTypeMigrationService.migrateThingType(vehicleThing, vehicleConfig.identifyModel(),
                                        vehicleThing.getConfiguration());
                                break;
                            }
                            logger.debug("Querying the vehicle: VIN {}", vehicle.vin);
                            String vehicleJSON = gson.toJson(vehicle);
                            vehicleHandler.parseAndUpdate("queryVehicle", null, vehicleJSON);
                            logger.trace("Vehicle is id {}/vehicle_id {}/tokens {}", vehicle.id, vehicle.vehicleId,
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

    ThingStatusInfo authenticate() {
        TokenResponse token = logonToken;

        boolean hasExpired = true;
        logger.debug("Current authentication time {}", DATE_FORMATTER.format(Instant.now()));

        if (token != null) {
            Instant tokenCreationInstant = Instant.ofEpochMilli(token.createdAt * 1000);
            Instant tokenExpiresInstant = Instant.ofEpochMilli((token.createdAt + token.expiresIn) * 1000);
            logger.debug("Found a request token from {}", DATE_FORMATTER.format(tokenCreationInstant));
            logger.debug("Access token expiration time {}", DATE_FORMATTER.format(tokenExpiresInstant));

            if (tokenExpiresInstant.isBefore(Instant.now())) {
                logger.debug("The access token has expired");
                hasExpired = true;
            } else {
                logger.debug("The access token has not expired yet");
                hasExpired = false;
            }
        }

        if (hasExpired) {
            String refreshToken = (String) getConfig().get(CONFIG_REFRESHTOKEN);

            if (refreshToken == null || refreshToken.isEmpty()) {
                return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No refresh token is provided.");
            }

            this.logonToken = ssoHandler.getAccessToken(refreshToken);
            if (this.logonToken == null) {
                return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Failed to obtain access token for API.");
            }
        }

        return new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    protected @Nullable String invokeAndParse(@Nullable String vehicleId, @Nullable String command,
            @Nullable String payLoad, WebTarget target, int noOfretries) {
        logger.debug("Invoking: {}", command);

        if (vehicleId != null) {
            Response response;

            if (payLoad != null) {
                if (command != null) {
                    response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicleId).request()
                            .header("Authorization", getAuthHeader())
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                } else {
                    response = target.resolveTemplate("vid", vehicleId).request()
                            .header("Authorization", getAuthHeader())
                            .post(Entity.entity(payLoad, MediaType.APPLICATION_JSON_TYPE));
                }
            } else if (command != null) {
                response = target.resolveTemplate("cmd", command).resolveTemplate("vid", vehicleId)
                        .request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", getAuthHeader()).get();
            } else {
                response = target.resolveTemplate("vid", vehicleId).request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Authorization", getAuthHeader()).get();
            }

            if (!checkResponse(response, false)) {
                if (response == null) {
                    logger.debug(
                            "An error occurred while communicating with the vehicle during request, the response was null");
                    return null;
                }
                logger.debug("An error occurred while communicating with the vehicle during request {}: {}: {}",
                        command, response.getStatus(), response.getStatusInfo().getReasonPhrase());
                if (response.getStatus() == 408 && noOfretries > 0) {
                    try {
                        // we give the vehicle a moment to wake up and try the request again
                        Thread.sleep(TimeUnit.SECONDS.toMillis(API_ERROR_INTERVAL_SECONDS));
                        logger.debug("Retrying to send the command {}.", command);
                        return invokeAndParse(vehicleId, command, payLoad, target, noOfretries - 1);
                    } catch (InterruptedException e) {
                    }
                }
                return null;
            }

            try {
                JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();
                logger.trace("Request : {}:{} yields {}", command, payLoad, jsonObject.get("response"));
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

            ThingStatusInfo status = getThing().getStatusInfo();
            if ((status.getStatus() != ThingStatus.ONLINE
                    && status.getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR)
                    || hasUnidentifiedVehicles()) {
                logger.debug("Setting up an authenticated connection to the Tesla back-end");

                ThingStatusInfo authenticationResult = authenticate();
                updateStatus(authenticationResult.getStatus(), authenticationResult.getStatusDetail(),
                        authenticationResult.getDescription());

                if (authenticationResult.getStatus() == ThingStatus.ONLINE) {
                    // get a list of vehicles
                    Response response = productsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                            .header("Authorization", getAuthHeader()).get();

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
                    } else if (response != null) {
                        logger.error("Error fetching the list of vehicles : {}:{}", response.getStatus(),
                                response.getStatusInfo());
                        updateStatus(ThingStatus.OFFLINE);
                    }
                } else if (authenticationResult.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
                    // make sure to set thing to CONFIGURATION_ERROR in case of failed authentication in order not to
                    // hit request limit on retries on the Tesla SSO endpoints.
                    updateStatus(ThingStatus.OFFLINE, authenticationResult.getStatusDetail());
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while connecting to the Tesla back-end: '{}'", e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    };

    private boolean hasUnidentifiedVehicles() {
        return getThing().getThings().stream()
                .anyMatch(vehicle -> TeslaBindingConstants.THING_TYPE_VEHICLE.equals(vehicle.getThingTypeUID()));
    }

    protected class Request implements Runnable {

        private static final int NO_OF_RETRIES = 3;

        private TeslaVehicleHandler handler;
        private String request;
        @Nullable
        private String payLoad;
        private WebTarget target;
        private boolean allowWakeUpForCommands;

        public Request(TeslaVehicleHandler handler, String request, @Nullable String payLoad, WebTarget target,
                boolean allowWakeUpForCommands) {
            this.handler = handler;
            this.request = request;
            this.payLoad = payLoad;
            this.target = target;
            this.allowWakeUpForCommands = allowWakeUpForCommands;
        }

        @Override
        public void run() {
            try {
                String result = "";

                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    result = invokeAndParse(handler.getVehicleId(), request, payLoad, target,
                            allowWakeUpForCommands ? NO_OF_RETRIES : 0);
                    if (result != null && !"".equals(result)) {
                        handler.parseAndUpdate(request, payLoad, result);
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while executing a request to the vehicle: '{}'", e.getMessage(), e);
            }
        }
    }

    public Request newRequest(TeslaVehicleHandler teslaVehicleHandler, String command, @Nullable String payLoad,
            WebTarget target, boolean allowWakeUpForCommands) {
        return new Request(teslaVehicleHandler, command, payLoad, target, allowWakeUpForCommands);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(TeslaVehicleDiscoveryService.class);
    }
}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bmwcardata.internal.handler;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataApi;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.DeviceAuthorization;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.PollResult;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthException;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataStreamClient;
import org.openhab.binding.bmwcardata.internal.api.Tokens;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.Container;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.api.dto.VehicleMapping;
import org.openhab.binding.bmwcardata.internal.config.AccountConfiguration;
import org.openhab.binding.bmwcardata.internal.discovery.BMWCarDataVehicleDiscoveryService;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link BMWCarDataAccountHandler} authorizes the binding for a BMW CarData client, keeps the tokens fresh,
 * receives the data stream of all vehicles and provides REST API access to the vehicle handlers.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataAccountHandler extends BaseBridgeHandler implements BMWCarDataStreamClient.Listener {

    private static final String CONTAINER_NAME = "openHAB";
    private static final String CONTAINER_PURPOSE_PREFIX = "openHAB BMW CarData binding";
    // bump the revision whenever the descriptors of VehicleChannel change, so that the container is replaced
    private static final String CONTAINER_PURPOSE = CONTAINER_PURPOSE_PREFIX + " rev 5";
    private static final Duration REFRESH_MARGIN = Duration.ofMinutes(5);
    private static final long RETRY_DELAY_SECONDS = 60;
    private static final long STREAM_STOP_TIMEOUT_SECONDS = 10;
    private static final int SLOW_DOWN_INCREMENT_SECONDS = 5;
    private static final String STORAGE_KEY_SESSION = "session";

    /**
     * The persisted part of an authorized session.
     */
    private record StoredSession(String clientId, String refreshToken, String gcid) {
    }

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataAccountHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;
    private final Storage<String> storage;
    private final Object containerLock = new Object();

    private AccountConfiguration config = new AccountConfiguration();
    private @Nullable BMWCarDataAuthClient authClient;
    private @Nullable BMWCarDataApi api;
    private volatile @Nullable Tokens tokens;
    private volatile @Nullable BMWCarDataStreamClient streamClient;
    private volatile @Nullable BMWCarDataVehicleDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> authJob;
    private volatile boolean disposed;
    // guarded by containerLock
    private @Nullable String containerId;

    public BMWCarDataAccountHandler(Bridge bridge, HttpClient httpClient, Gson gson, Storage<String> storage) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = gson;
        this.storage = storage;
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccountConfiguration.class);
        String clientId = config.clientId.trim();
        if (clientId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-client-id");
            return;
        }
        authClient = new BMWCarDataAuthClient(httpClient, gson, clientId);
        api = new BMWCarDataApi(httpClient, gson, this::getAccessToken);
        disposed = false;
        updateStatus(ThingStatus.UNKNOWN);
        scheduleAuthJob(this::authenticate, 0);
    }

    @Override
    public void dispose() {
        stopAuthJob();
        stopStream();
        tokens = null;
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        // stop first, so that a running token refresh cannot store the session again
        stopAuthJob();
        storage.remove(STORAGE_KEY_SESSION);
        super.handleRemoval();
    }

    private synchronized void stopAuthJob() {
        disposed = true;
        ScheduledFuture<?> job = authJob;
        if (job != null) {
            job.cancel(true);
            authJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge has no channels
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(BMWCarDataVehicleDiscoveryService.class);
    }

    public void setDiscoveryService(@Nullable BMWCarDataVehicleDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Retrieves the vehicles mapped to the account. Costs one request of the daily quota.
     */
    public List<VehicleMapping> fetchVehicleMappings() throws BMWCarDataException {
        return call(requireApi()::getVehicleMappings);
    }

    /**
     * Retrieves the static data of a vehicle. Costs one request of the daily quota.
     */
    public BasicData fetchBasicData(String vin) throws BMWCarDataException {
        BMWCarDataApi api = requireApi();
        return call(() -> api.getBasicData(vin));
    }

    /**
     * Retrieves the current values of all descriptors used by the binding. Costs one request of the daily quota, plus
     * up to three if the container of the binding has to be set up first.
     */
    public Map<String, TelematicValue> fetchTelematicData(String vin) throws BMWCarDataException {
        BMWCarDataApi api = requireApi();
        String containerId = getOrCreateContainer(api);
        try {
            return call(() -> api.getTelematicData(vin, containerId));
        } catch (BMWCarDataException e) {
            if (e.getHttpStatus() == 404) {
                // the container may have been deleted in the CarData portal
                synchronized (containerLock) {
                    if (containerId.equals(this.containerId)) {
                        this.containerId = null;
                    }
                }
            }
            throw e;
        }
    }

    @Override
    public void onStreamData(String vin, Map<String, TelematicValue> data) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof BMWCarDataVehicleHandler vehicleHandler
                    && vin.equals(vehicleHandler.getVin())) {
                vehicleHandler.updateTelematicData(data);
                return;
            }
        }
        logger.trace("Ignoring stream data of a vehicle without thing");
    }

    @Override
    public void onStreamConnectionChanged(boolean connected, @Nullable String reason) {
        if (connected) {
            logger.debug("Connected to BMW CarData streaming");
        } else {
            logger.debug("Disconnected from BMW CarData streaming: {}", reason);
        }
    }

    private String getAccessToken() throws BMWCarDataException {
        Tokens tokens = this.tokens;
        if (tokens == null || tokens.isExpired(Instant.now())) {
            throw new BMWCarDataException("Not authorized");
        }
        return tokens.accessToken();
    }

    private BMWCarDataApi requireApi() throws BMWCarDataException {
        BMWCarDataApi api = this.api;
        if (api == null) {
            throw new BMWCarDataException("Account is not initialized");
        }
        return api;
    }

    @FunctionalInterface
    private interface ApiCall<T> {
        T execute() throws BMWCarDataException;
    }

    private <T> T call(ApiCall<T> call) throws BMWCarDataException {
        try {
            return call.execute();
        } catch (BMWCarDataAuthException e) {
            // the access token was rejected before its expiry, try to obtain a new one
            if (tokens != null) {
                scheduleAuthJob(this::refreshTokens, 0);
            }
            throw e;
        }
    }

    private String getOrCreateContainer(BMWCarDataApi api) throws BMWCarDataException {
        // the lock is held during the requests to avoid that concurrent polls create duplicate containers
        synchronized (containerLock) {
            String id = containerId;
            if (id != null) {
                return id;
            }
            for (Container container : call(api::getContainers)) {
                String existingId = container.containerId;
                String purpose = container.purpose;
                if (existingId == null || !CONTAINER_NAME.equals(container.name) || purpose == null
                        || !purpose.startsWith(CONTAINER_PURPOSE_PREFIX)) {
                    continue;
                }
                if (id == null && CONTAINER_PURPOSE.equals(purpose) && Container.STATE_ACTIVE.equals(container.state)) {
                    id = existingId;
                } else {
                    // container of an older binding revision or a duplicate
                    try {
                        call(() -> {
                            api.deleteContainer(existingId);
                            return null;
                        });
                    } catch (BMWCarDataException e) {
                        logger.debug("Failed to delete obsolete container: {}", e.getMessage());
                    }
                }
            }
            if (id == null) {
                id = call(() -> api.createContainer(CONTAINER_NAME, CONTAINER_PURPOSE, VehicleChannel.descriptors()));
                logger.debug("Created telematic data container {}", id);
            }
            containerId = id;
            return id;
        }
    }

    private synchronized void scheduleAuthJob(Runnable job, long delaySeconds) {
        if (disposed) {
            return;
        }
        ScheduledFuture<?> previousJob = authJob;
        if (previousJob != null) {
            previousJob.cancel(false);
        }
        authJob = scheduler.schedule(job, delaySeconds, TimeUnit.SECONDS);
    }

    private void authenticate() {
        BMWCarDataAuthClient authClient = this.authClient;
        if (authClient == null || disposed) {
            return;
        }
        StoredSession session = loadSession();
        if (session == null) {
            startDeviceAuthorization();
            return;
        }
        try {
            onAuthenticated(authClient.refresh(session.refreshToken(), session.gcid(), null));
        } catch (BMWCarDataAuthException e) {
            logger.debug("Stored authorization is no longer valid: {}", e.getMessage());
            storage.remove(STORAGE_KEY_SESSION);
            startDeviceAuthorization();
        } catch (BMWCarDataException e) {
            updateOfflineIfActive(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleAuthJob(this::authenticate, RETRY_DELAY_SECONDS);
        }
    }

    private void startDeviceAuthorization() {
        BMWCarDataAuthClient authClient = this.authClient;
        if (authClient == null || disposed) {
            return;
        }
        try {
            DeviceAuthorization authorization = authClient.requestDeviceCode();
            updateOfflineIfActive(ThingStatusDetail.CONFIGURATION_PENDING, "@text/offline.authorization-pending [\""
                    + authorization.verificationUri() + "\", \"" + authorization.userCode() + "\"]");
            scheduleAuthJob(() -> pollDeviceAuthorization(authorization, authorization.interval()),
                    authorization.interval());
        } catch (BMWCarDataException e) {
            updateOfflineIfActive(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleAuthJob(this::startDeviceAuthorization, RETRY_DELAY_SECONDS);
        }
    }

    private void pollDeviceAuthorization(DeviceAuthorization authorization, int interval) {
        BMWCarDataAuthClient authClient = this.authClient;
        if (authClient == null || disposed) {
            return;
        }
        if (authorization.isExpired(Instant.now())) {
            startDeviceAuthorization();
            return;
        }
        PollResult result;
        try {
            result = authClient.pollDeviceToken(authorization);
        } catch (BMWCarDataException e) {
            logger.debug("Polling device authorization failed: {}", e.getMessage());
            scheduleAuthJob(() -> pollDeviceAuthorization(authorization, interval), interval);
            return;
        }
        Tokens newTokens = result.tokens();
        switch (result.status()) {
            case PENDING -> scheduleAuthJob(() -> pollDeviceAuthorization(authorization, interval), interval);
            case SLOW_DOWN -> {
                int slowerInterval = interval + SLOW_DOWN_INCREMENT_SECONDS;
                scheduleAuthJob(() -> pollDeviceAuthorization(authorization, slowerInterval), slowerInterval);
            }
            case EXPIRED -> startDeviceAuthorization();
            case DENIED ->
                updateOfflineIfActive(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.access-denied");
            case AUTHORIZED -> {
                if (newTokens != null) {
                    onAuthenticated(newTokens);
                }
            }
        }
    }

    private void onAuthenticated(Tokens newTokens) {
        if (disposed) {
            return;
        }
        tokens = newTokens;
        saveSession(newTokens);
        scheduleTokenRefresh(newTokens);
        updateOnlineIfActive();
        if (config.streaming) {
            startStream(newTokens);
        }
        BMWCarDataVehicleDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null && getThing().getThings().isEmpty()) {
            discoveryService.discoverVehicles();
        }
    }

    private void refreshTokens() {
        BMWCarDataAuthClient authClient = this.authClient;
        Tokens current = tokens;
        if (authClient == null || current == null || disposed) {
            return;
        }
        try {
            Tokens newTokens = authClient.refresh(current.refreshToken(), current.gcid(), current.idToken());
            if (disposed) {
                return;
            }
            tokens = newTokens;
            saveSession(newTokens);
            if (streamClient != null) {
                restartStream(newTokens);
            }
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateOnlineIfActive();
            }
            scheduleTokenRefresh(newTokens);
        } catch (BMWCarDataAuthException e) {
            logger.debug("Authorization is no longer valid: {}", e.getMessage());
            tokens = null;
            stopStream();
            storage.remove(STORAGE_KEY_SESSION);
            startDeviceAuthorization();
        } catch (BMWCarDataException e) {
            logger.debug("Token refresh failed: {}", e.getMessage());
            if (current.isExpired(Instant.now())) {
                updateOfflineIfActive(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            scheduleAuthJob(this::refreshTokens, RETRY_DELAY_SECONDS);
        }
    }

    private void scheduleTokenRefresh(Tokens tokens) {
        long delay = Duration.between(Instant.now(), tokens.expiresAt().minus(REFRESH_MARGIN)).toSeconds();
        scheduleAuthJob(this::refreshTokens, Math.max(delay, RETRY_DELAY_SECONDS));
    }

    private void startStream(Tokens tokens) {
        stopStream();
        try {
            BMWCarDataStreamClient client = new BMWCarDataStreamClient(tokens, gson, scheduler, this);
            streamClient = client;
            // dispose() may have run concurrently before the client was assigned
            if (disposed) {
                stopStream();
                return;
            }
            client.start();
        } catch (BMWCarDataException e) {
            logger.warn("Unable to start BMW CarData streaming: {}", e.getMessage());
        }
    }

    /**
     * Reconnects the stream with a new id token. The broker closes a connection when its id token expires, and it
     * accepts only one connection per customer, so the new connection is opened after the old one has been closed.
     */
    private void restartStream(Tokens newTokens) {
        stopStream().orTimeout(STREAM_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS).whenComplete((result, error) -> {
            // a later refresh or a dispose supersedes this restart
            if (!disposed && newTokens.equals(tokens)) {
                scheduler.execute(() -> {
                    if (!disposed && newTokens.equals(tokens) && streamClient == null) {
                        startStream(newTokens);
                    }
                });
            }
        });
    }

    private CompletableFuture<Boolean> stopStream() {
        BMWCarDataStreamClient client = streamClient;
        streamClient = null;
        return client != null ? client.stop() : CompletableFuture.completedFuture(true);
    }

    private void updateOfflineIfActive(ThingStatusDetail detail, @Nullable String description) {
        if (!disposed) {
            updateStatus(ThingStatus.OFFLINE, detail, description);
        }
    }

    private void updateOnlineIfActive() {
        if (!disposed) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private @Nullable StoredSession loadSession() {
        String json = storage.get(STORAGE_KEY_SESSION);
        if (json == null) {
            return null;
        }
        try {
            StoredSession session = gson.fromJson(json, StoredSession.class);
            // a session of a different client id cannot be refreshed
            if (session != null && config.clientId.trim().equals(session.clientId())) {
                return session;
            }
        } catch (JsonParseException e) {
            logger.debug("Ignoring malformed stored session: {}", e.getMessage());
        }
        return null;
    }

    private void saveSession(Tokens tokens) {
        if (disposed) {
            return;
        }
        storage.put(STORAGE_KEY_SESSION,
                gson.toJson(new StoredSession(config.clientId.trim(), tokens.refreshToken(), tokens.gcid())));
    }
}

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

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
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
import org.openhab.binding.bmwcardata.internal.api.CarDataTokens;
import org.openhab.binding.bmwcardata.internal.api.RequestBudget;
import org.openhab.binding.bmwcardata.internal.api.Tokens;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.Container;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.api.dto.VehicleMapping;
import org.openhab.binding.bmwcardata.internal.config.AccountConfiguration;
import org.openhab.binding.bmwcardata.internal.discovery.BMWCarDataVehicleDiscoveryService;
import org.openhab.binding.bmwcardata.internal.stream.BMWCarDataStreamClient;
import org.openhab.binding.bmwcardata.internal.stream.BridgeStreamSource;
import org.openhab.binding.bmwcardata.internal.stream.StreamListener;
import org.openhab.binding.bmwcardata.internal.stream.StreamMirror;
import org.openhab.binding.bmwcardata.internal.stream.StreamSource;
import org.openhab.binding.bmwcardata.internal.stream.StreamState;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
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
public class BMWCarDataAccountHandler extends BaseBridgeHandler implements StreamListener {

    private static final String CONTAINER_NAME = "openHAB";
    private static final String CONTAINER_PURPOSE_PREFIX = "openHAB BMW CarData binding";
    // bump the revision whenever the descriptors of VehicleChannel change, so that the container is replaced
    static final String CONTAINER_PURPOSE = CONTAINER_PURPOSE_PREFIX + " rev 6";
    private static final Duration REFRESH_MARGIN = Duration.ofMinutes(5);
    private static final long RETRY_DELAY_SECONDS = 60;
    private static final long STREAM_STOP_TIMEOUT_SECONDS = 10;
    private static final int SLOW_DOWN_INCREMENT_SECONDS = 5;
    private static final Duration INITIAL_REFRESH_DELAY = Duration.ofMinutes(2);
    // the stream does not replay values sent while it was down
    private static final Duration CATCH_UP_AFTER_OUTAGE = Duration.ofMinutes(5);
    private static final Duration CATCH_UP_MIN_SPACING = Duration.ofMinutes(30);
    // a single retry after a failure on BMW's side, e.g. a 502 of its gateway, which is usually over by then
    private static final Duration RETRY_AFTER_TRANSIENT_FAILURE = Duration.ofMinutes(15);
    // not secret: the client id the stored authorization belongs to, and the stream user name
    private static final String STORAGE_KEY_CLIENT_ID = "clientId";
    private static final String STORAGE_KEY_GCID = "gcid";
    // the per-thing request budget of earlier versions, removed at start
    private static final String STORAGE_KEY_BUDGET = "budget";
    private static final String STORAGE_KEY_CONTAINER = "container";
    // basic data by VIN; it does not change, so discovery and the vehicle Thing share one request
    private static final String STORAGE_KEY_BASIC_DATA = "basicData.";

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataAccountHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;
    private final Storage<String> storage;
    // request budgets by client ID, so they survive recreating the account thing
    private final Storage<String> budgetStorage;
    private final OAuthFactory oAuthFactory;
    private final ScheduledExecutorService jobScheduler;
    private final AccessTokenRefreshListener refreshListener = this::onAccessTokenResponse;
    private final Object containerLock = new Object();
    // serializes telematic refreshes, so that concurrent triggers do not spend the request budget twice
    private final Object refreshLock = new Object();

    private AccountConfiguration config = new AccountConfiguration();
    private @Nullable BMWCarDataAuthClient authClient;
    private @Nullable BMWCarDataApi api;
    private @Nullable RequestBudget budget;
    private @Nullable ScheduledFuture<?> refreshJob;
    // guarded by this
    private @Nullable ScheduledFuture<?> retryJob;
    // VINs of the vehicles to refresh again, dropped once a later refresh of the vehicle succeeds
    private final Set<String> retryVins = ConcurrentHashMap.newKeySet();
    private volatile @Nullable Instant streamDownSince;
    private volatile @Nullable Instant lastCatchUp;
    // status description while the request budget is used up
    private volatile @Nullable String budgetExhausted;
    private volatile @Nullable StreamState streamState;
    private final Object statusLock = new Object();
    private volatile @Nullable OAuthClientService oAuthService;
    // latest tokens, updated whenever the OAuth client service imports or refreshes them
    private volatile @Nullable Tokens tokens;
    private volatile boolean authorized;
    private volatile @Nullable StreamSource streamSource;
    private volatile @Nullable StreamMirror mirror;
    // whether the local broker of the mirror is connected, null if not known yet
    private volatile @Nullable Boolean mirrorConnected;
    private volatile @Nullable BMWCarDataVehicleDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> authJob;
    private volatile boolean disposed;
    // incremented when the handler stops, so that work started before cannot act on a re-initialized handler
    private volatile int generation;
    // guarded by containerLock
    private @Nullable String containerId;

    public BMWCarDataAccountHandler(Bridge bridge, HttpClient httpClient, Gson gson, Storage<String> storage,
            Storage<String> budgetStorage, OAuthFactory oAuthFactory) {
        this(bridge, httpClient, gson, storage, budgetStorage, oAuthFactory, null);
    }

    /**
     * @param jobScheduler runs the jobs of the handler, {@code null} for the scheduler of the base class
     */
    BMWCarDataAccountHandler(Bridge bridge, HttpClient httpClient, Gson gson, Storage<String> storage,
            Storage<String> budgetStorage, OAuthFactory oAuthFactory, @Nullable ScheduledExecutorService jobScheduler) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = gson;
        this.storage = storage;
        this.budgetStorage = budgetStorage;
        this.oAuthFactory = oAuthFactory;
        this.jobScheduler = jobScheduler != null ? jobScheduler : scheduler;
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
        if (config.usesBroker() && config.brokerHost.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-broker-host");
            return;
        }
        if (config.streaming && config.mirrorToBroker && config.isBridgeMode()) {
            logger.warn("Mirroring the stream to the local broker is ignored in bridge mode, the bridge publishes it");
        }
        authClient = new BMWCarDataAuthClient(httpClient, gson, clientId);

        String authorizedClientId = storage.get(STORAGE_KEY_CLIENT_ID);
        if (authorizedClientId != null && !authorizedClientId.equals(clientId)) {
            logger.debug("The client ID changed, the stored authorization is discarded");
            deleteAuthorization();
        }
        // the device authorization is done by the binding, core's OAuth client service stores and refreshes the tokens
        OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(oAuthHandle(),
                BMWCarDataAuthClient.TOKEN_URL, null, clientId, null, null, false);
        oAuthService.addAccessTokenRefreshListener(refreshListener);
        this.oAuthService = oAuthService;

        // BMW counts the requests per client, also when the account thing is recreated with a new UID
        storage.remove(STORAGE_KEY_BUDGET);
        RequestBudget budget = new RequestBudget(budgetStorage, clientId, config.requestBudget, Clock.systemUTC());
        this.budget = budget;
        api = new BMWCarDataApi(httpClient, gson, this::getAccessToken, budget);
        disposed = false;
        authorized = false;
        budgetExhausted = null;
        streamState = null;
        mirrorConnected = null;
        updateStatus(ThingStatus.UNKNOWN);
        scheduleAuthJob(this::authenticate, 0);
    }

    @Override
    public void dispose() {
        stopAuthJob();
        stopStream();
        stopMirror();
        tokens = null;
        OAuthClientService oAuthService = this.oAuthService;
        this.oAuthService = null;
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(refreshListener);
            oAuthFactory.ungetOAuthService(oAuthHandle());
        }
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        // stop first, so that a running token refresh cannot store the tokens again
        stopAuthJob();
        deleteAuthorization();
        storage.remove(STORAGE_KEY_CONTAINER);
        for (String key : List.copyOf(storage.getKeys())) {
            if (key.startsWith(STORAGE_KEY_BASIC_DATA)) {
                storage.remove(key);
            }
        }
        // the request budget is kept in the shared budget storage, as BMW keeps counting the requests of the client
        super.handleRemoval();
    }

    private String oAuthHandle() {
        return getThing().getUID().getAsString();
    }

    /**
     * Discards the stored authorization, so that the next start requires a new device authorization.
     */
    private void deleteAuthorization() {
        oAuthFactory.deleteServiceAndAccessToken(oAuthHandle());
        storage.remove(STORAGE_KEY_CLIENT_ID);
        storage.remove(STORAGE_KEY_GCID);
        tokens = null;
    }

    private synchronized void stopAuthJob() {
        disposed = true;
        generation++;
        ScheduledFuture<?> job = authJob;
        if (job != null) {
            job.cancel(true);
            authJob = null;
        }
        ScheduledFuture<?> refresh = refreshJob;
        if (refresh != null) {
            refresh.cancel(true);
            refreshJob = null;
        }
        ScheduledFuture<?> retry = retryJob;
        if (retry != null) {
            retry.cancel(true);
            retryJob = null;
        }
        retryVins.clear();
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
     * Retrieves the static data of a vehicle. Costs one request of the daily quota the first time; the data is kept,
     * as it does not change, so discovery and the vehicle Thing share that request.
     */
    public BasicData fetchBasicData(String vin) throws BMWCarDataException {
        @Nullable
        BasicData cached = getCachedBasicData(vin);
        if (cached != null) {
            return cached;
        }
        BMWCarDataApi api = requireApi();
        BasicData basicData = call(() -> api.getBasicData(vin));
        storage.put(STORAGE_KEY_BASIC_DATA + vin, gson.toJson(basicData));
        return basicData;
    }

    private @Nullable BasicData getCachedBasicData(String vin) {
        @Nullable
        String cached = storage.get(STORAGE_KEY_BASIC_DATA + vin);
        if (cached == null) {
            return null;
        }
        try {
            return gson.fromJson(cached, BasicData.class);
        } catch (JsonParseException e) {
            storage.remove(STORAGE_KEY_BASIC_DATA + vin);
            return null;
        }
    }

    /**
     * Retrieves the current values of all descriptors used by the binding. Costs one request of the daily quota, plus
     * up to three if the container of the binding has to be set up first.
     */
    private Map<String, TelematicValue> fetchTelematicData(BMWCarDataApi api, String vin) throws BMWCarDataException {
        String containerId = getOrCreateContainer(api);
        try {
            return call(() -> api.getTelematicData(vin, containerId));
        } catch (BMWCarDataException e) {
            if (e.getHttpStatus() == 404) {
                // the container may have been deleted in the CarData portal
                synchronized (containerLock) {
                    if (containerId.equals(this.containerId)) {
                        this.containerId = null;
                        storage.remove(STORAGE_KEY_CONTAINER);
                    }
                }
            }
            throw e;
        }
    }

    /**
     * Reads the current values of all vehicles, one request per vehicle. The refresh is skipped as a whole if the
     * request budget cannot cover all vehicles.
     */
    private void refreshTelematicData(String reason) {
        refreshTelematicData(reason, getVehicles(), true);
    }

    private List<BMWCarDataVehicleHandler> getVehicles() {
        return getThing().getThings().stream().map(Thing::getHandler).filter(BMWCarDataVehicleHandler.class::isInstance)
                .map(BMWCarDataVehicleHandler.class::cast).filter(vehicle -> BMWCarDataApi.isValidVin(vehicle.getVin()))
                .toList();
    }

    /**
     * Reads the current values of the given vehicles. Vehicles whose refresh failed on BMW's side are refreshed once
     * more after {@link #RETRY_AFTER_TRANSIENT_FAILURE}, if {@code retry} is set.
     */
    private void refreshTelematicData(String reason, List<BMWCarDataVehicleHandler> vehicles, boolean retry) {
        int generation = this.generation;
        BMWCarDataApi api = this.api;
        RequestBudget budget = this.budget;
        if (api == null || budget == null || isStale(generation) || tokens == null || vehicles.isEmpty()) {
            return;
        }
        synchronized (refreshLock) {
            int needed = vehicles.size() + (containerId == null ? 1 : 0);
            if (!budget.hasRoomFor(needed)) {
                reportBudgetExhausted(budget);
                return;
            }
            logger.debug("Refreshing telematic data of {} vehicle(s) ({})", vehicles.size(), reason);
            for (BMWCarDataVehicleHandler vehicle : vehicles) {
                // also after waiting for the lock, so that a cancelled refresh does not spend the budget
                if (isStale(generation)) {
                    return;
                }
                try {
                    Map<String, TelematicValue> data = fetchTelematicData(api, vehicle.getVin());
                    retryVins.remove(vehicle.getVin());
                    vehicle.updateTelematicData(data);
                    vehicle.refreshSucceeded();
                } catch (BMWCarDataException e) {
                    // disposing interrupts the request, which must not set the vehicles OFFLINE
                    if (isStale(generation)) {
                        return;
                    }
                    if (e.isRateLimited()) {
                        reportBudgetExhausted(budget);
                        return;
                    }
                    if (!e.isTransient()) {
                        vehicle.refreshFailed(e.getMessage());
                        continue;
                    }
                    if (retry) {
                        retryVins.add(vehicle.getVin());
                    }
                    if (isStreamConnected()) {
                        // the stream keeps the vehicle up to date, a failed poll does not make it unreachable
                        logger.debug("Refreshing telematic data failed, relying on the stream: {}", e.getMessage());
                    } else {
                        vehicle.refreshFailed(e.getMessage());
                    }
                }
            }
            clearBudgetExhausted();
        }
        if (retry) {
            scheduleRetry(generation);
        }
    }

    /**
     * @return whether work started in the given lifecycle belongs to a disposed or re-initialized handler
     */
    private boolean isStale(int generation) {
        return disposed || generation != this.generation || Thread.currentThread().isInterrupted();
    }

    private boolean isStreamConnected() {
        return config.streaming && streamState == StreamState.CONNECTED;
    }

    private synchronized void scheduleRetry(int generation) {
        if (isStale(generation) || retryJob != null || retryVins.isEmpty()) {
            return;
        }
        logger.debug("Retrying the refresh of {} vehicle(s) in {} minutes", retryVins.size(),
                RETRY_AFTER_TRANSIENT_FAILURE.toMinutes());
        retryJob = jobScheduler.schedule(() -> retryRefresh(generation), RETRY_AFTER_TRANSIENT_FAILURE.toSeconds(),
                TimeUnit.SECONDS);
    }

    /**
     * Refreshes the vehicles still waiting for a retry, resolved by VIN, as vehicle Things may have been removed in
     * the meantime. A failure of the retry does not schedule another one.
     */
    private void retryRefresh(int generation) {
        synchronized (this) {
            if (isStale(generation)) {
                return;
            }
            // failures of refreshes running from now on can schedule the next retry
            retryJob = null;
        }
        Set<String> vins = new HashSet<>(retryVins);
        retryVins.removeAll(vins);
        refreshTelematicData("retry",
                getVehicles().stream().filter(vehicle -> vins.contains(vehicle.getVin())).toList(), false);
    }

    private void scheduleRefresh() {
        synchronized (this) {
            if (disposed || refreshJob != null) {
                return;
            }
            long initialDelay = INITIAL_REFRESH_DELAY.toSeconds();
            if (config.refreshInterval > 0) {
                refreshJob = jobScheduler.scheduleWithFixedDelay(() -> refreshTelematicData("periodic"), initialDelay,
                        TimeUnit.HOURS.toSeconds(config.refreshInterval), TimeUnit.SECONDS);
            } else {
                refreshJob = jobScheduler.schedule(() -> refreshTelematicData("startup"), initialDelay,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void reportBudgetExhausted(RequestBudget budget) {
        logger.debug("Skipping BMW CarData requests, {} of {} requests used in the last 24 hours", budget.used(),
                budget.budget());
        budgetExhausted = "@text/online.budget-exhausted [\"" + budget.used() + "\", \"" + budget.budget() + "\"]";
        updateOnlineStatus();
    }

    private void clearBudgetExhausted() {
        if (budgetExhausted != null) {
            budgetExhausted = null;
            updateOnlineStatus();
        }
    }

    /**
     * Sets the ONLINE status of an authorized account, with a description of a stream or request budget problem.
     */
    private void updateOnlineStatus() {
        if (disposed || !authorized) {
            return;
        }
        String description = null;
        StreamState state = streamState;
        if (config.streaming && state != null && state != StreamState.CONNECTED) {
            description = switch (state) {
                case DISCONNECTED ->
                    config.isBridgeMode() ? "@text/online.broker-disconnected" : "@text/online.stream-disconnected";
                case UPSTREAM_DISCONNECTED -> "@text/online.bridge-disconnected";
                default -> "@text/online.bridge-unknown";
            };
        } else {
            description = config.isMirrorActive() && Boolean.FALSE.equals(mirrorConnected)
                    ? "@text/online.mirror-disconnected"
                    : budgetExhausted;
        }
        synchronized (statusLock) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, description);
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
    public void onStreamStateChanged(StreamState state, @Nullable String detail) {
        logger.debug("BMW CarData stream state {}{}", state, detail != null ? ": " + detail : "");
        streamState = state;
        StreamMirror mirror = this.mirror;
        if (mirror != null) {
            mirror.publishStreamState(state == StreamState.CONNECTED);
        }
        Instant now = Instant.now();
        if (state == StreamState.CONNECTED) {
            Instant downSince = streamDownSince;
            streamDownSince = null;
            Instant last = lastCatchUp;
            if (downSince != null && Duration.between(downSince, now).compareTo(CATCH_UP_AFTER_OUTAGE) >= 0
                    && (last == null || Duration.between(last, now).compareTo(CATCH_UP_MIN_SPACING) >= 0)) {
                lastCatchUp = now;
                jobScheduler.execute(() -> refreshTelematicData("stream outage"));
            }
        } else if (streamDownSince == null) {
            streamDownSince = now;
        }
        updateOnlineStatus();
    }

    /**
     * Supplies the REST client with a valid access token. Core refreshes the token first if it has expired.
     */
    private String getAccessToken() throws BMWCarDataException {
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService == null || !authorized) {
            throw new BMWCarDataException("Not authorized");
        }
        try {
            AccessTokenResponse response = oAuthService.getAccessTokenResponse();
            String accessToken = response != null ? response.getAccessToken() : null;
            if (accessToken == null) {
                throw new BMWCarDataException("Not authorized");
            }
            return accessToken;
        } catch (OAuthResponseException e) {
            throw CarDataTokens.toException(e);
        } catch (OAuthException e) {
            throw CarDataTokens.toException(e);
        } catch (IOException e) {
            throw CarDataTokens.toException(e);
        }
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
            // the stored id saves listing the containers after each restart; it is bound to the container revision
            String stored = storage.get(STORAGE_KEY_CONTAINER);
            String suffix = "|" + CONTAINER_PURPOSE;
            if (stored != null && stored.endsWith(suffix)) {
                id = stored.substring(0, stored.length() - suffix.length());
                containerId = id;
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
            storage.put(STORAGE_KEY_CONTAINER, id + suffix);
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
        authJob = jobScheduler.schedule(job, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * Starts with the stored authorization if there is one, otherwise with a new device authorization.
     */
    private void authenticate() {
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService == null || disposed) {
            return;
        }
        AccessTokenResponse stored;
        try {
            // refreshes the token if it has expired
            stored = oAuthService.getAccessTokenResponse();
        } catch (OAuthResponseException e) {
            handleAuthFailure(CarDataTokens.toException(e), this::authenticate);
            return;
        } catch (OAuthException e) {
            handleAuthFailure(CarDataTokens.toException(e), this::authenticate);
            return;
        } catch (IOException e) {
            handleAuthFailure(CarDataTokens.toException(e), this::authenticate);
            return;
        }
        if (stored == null || stored.getRefreshToken() == null) {
            startDeviceAuthorization();
            return;
        }
        // confirms that the stored refresh token still works and provides a fresh id token for the stream
        refreshTokens();
    }

    private void startDeviceAuthorization() {
        BMWCarDataAuthClient authClient = this.authClient;
        if (authClient == null || disposed) {
            return;
        }
        authorized = false;
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
                    importTokens(newTokens);
                }
            }
        }
    }

    /**
     * Hands the tokens of a completed device authorization to core's OAuth client service, which stores them
     * encrypted and refreshes them from then on.
     */
    private void importTokens(Tokens newTokens) {
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService == null || disposed) {
            return;
        }
        try {
            oAuthService.importAccessTokenResponse(CarDataTokens.toAccessTokenResponse(newTokens, Instant.now()));
        } catch (OAuthException e) {
            // the service has been closed, the handler is being disposed
            logger.debug("Could not store the tokens: {}", e.getMessage());
            return;
        }
        storage.put(STORAGE_KEY_CLIENT_ID, config.clientId.trim());
        storage.put(STORAGE_KEY_GCID, newTokens.gcid());
        tokens = newTokens;
        onAuthenticated();
    }

    private void onAuthenticated() {
        Tokens current = tokens;
        if (disposed || current == null) {
            return;
        }
        authorized = true;
        scheduleTokenRefresh(current);
        updateOnlineStatus();
        startStream(current);
        scheduleRefresh();
        BMWCarDataVehicleDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null && getThing().getThings().isEmpty()) {
            discoveryService.discoverVehicles();
        }
    }

    /**
     * Refreshes the tokens before they expire, as the stream needs a valid id token for every (re)connect.
     */
    private void refreshTokens() {
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService == null || disposed) {
            return;
        }
        try {
            onAccessTokenResponse(oAuthService.refreshToken());
        } catch (OAuthResponseException e) {
            handleAuthFailure(CarDataTokens.toException(e), this::refreshTokens);
            return;
        } catch (OAuthException e) {
            handleAuthFailure(CarDataTokens.toException(e), this::refreshTokens);
            return;
        } catch (IOException e) {
            handleAuthFailure(CarDataTokens.toException(e), this::refreshTokens);
            return;
        }
        Tokens current = tokens;
        if (current == null) {
            handleAuthFailure(new BMWCarDataAuthException("Incomplete token response", 0), this::refreshTokens);
            return;
        }
        if (!authorized) {
            onAuthenticated();
            return;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateOnlineStatus();
        }
        scheduleTokenRefresh(current);
    }

    /**
     * Called by core's OAuth client service after every successful refresh, also if a REST request triggered it.
     */
    private void onAccessTokenResponse(AccessTokenResponse response) {
        if (disposed) {
            return;
        }
        Tokens previous = tokens;
        Tokens refreshed = CarDataTokens.fromAccessTokenResponse(response, previous, storage.get(STORAGE_KEY_GCID));
        if (refreshed == null) {
            logger.debug("The token response lacks the id token or gcid needed for the stream");
            return;
        }
        storage.put(STORAGE_KEY_GCID, refreshed.gcid());
        tokens = refreshed;
        if (streamSource != null && previous != null && !refreshed.idToken().equals(previous.idToken())) {
            restartStream(refreshed);
        }
    }

    /**
     * Starts a new device authorization if the stored authorization was rejected, otherwise retries later.
     */
    private void handleAuthFailure(BMWCarDataException e, Runnable retry) {
        if (disposed) {
            return;
        }
        if (e instanceof BMWCarDataAuthException) {
            logger.debug("Authorization is no longer valid: {}", e.getMessage());
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService != null) {
                try {
                    // clears the stored token, but keeps the service for the import after the new authorization
                    oAuthService.importAccessTokenResponse(new AccessTokenResponse());
                } catch (OAuthException ex) {
                    // the service has been closed, the handler is being disposed
                }
            }
            tokens = null;
            stopStream();
            startDeviceAuthorization();
            return;
        }
        logger.debug("Token refresh failed: {}", e.getMessage());
        Tokens current = tokens;
        if (current == null || current.isExpired(Instant.now())) {
            updateOfflineIfActive(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        scheduleAuthJob(retry, RETRY_DELAY_SECONDS);
    }

    private void scheduleTokenRefresh(Tokens tokens) {
        long delay = Duration.between(Instant.now(), tokens.expiresAt().minus(REFRESH_MARGIN)).toSeconds();
        scheduleAuthJob(this::refreshTokens, Math.max(delay, RETRY_DELAY_SECONDS));
    }

    /**
     * Starts the configured stream source. In bridge mode the binding does not connect to BMW's streaming broker, as
     * BMW accepts only one connection per account and the bridge holds it.
     */
    private void startStream(Tokens tokens) {
        stopStream();
        if (!config.streaming) {
            return;
        }
        if (streamDownSince == null) {
            streamDownSince = Instant.now();
        }
        StreamSource source;
        try {
            source = createStreamSource(tokens);
        } catch (BMWCarDataException e) {
            logger.warn("Unable to start BMW CarData streaming: {}", e.getMessage());
            return;
        }
        streamSource = source;
        // dispose() may have run concurrently before the source was assigned
        if (disposed) {
            stopStream();
            return;
        }
        source.start();
    }

    /**
     * Reconnects the direct stream with a new id token. The broker closes a connection when its id token expires, and
     * it accepts only one connection per customer, so the new connection is opened after the old one has been closed.
     */
    private void restartStream(Tokens newTokens) {
        if (config.isBridgeMode()) {
            // the bridge authenticates at BMW itself
            return;
        }
        stopStream(false).orTimeout(STREAM_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS).whenComplete((result, error) -> {
            // a later refresh or a dispose supersedes this restart
            if (!disposed && newTokens.equals(tokens)) {
                jobScheduler.execute(() -> {
                    if (!disposed && newTokens.equals(tokens) && streamSource == null) {
                        startStream(newTokens);
                    }
                });
            }
        });
    }

    /**
     * Creates the configured stream source, replaced in tests so that they do not connect to a broker.
     */
    StreamSource createStreamSource(Tokens tokens) throws BMWCarDataException {
        if (config.isBridgeMode()) {
            return new BridgeStreamSource(config.brokerSettings(), "openhab-bmwcardata-" + getThing().getUID().getId(),
                    jobScheduler, this);
        }
        StreamMirror mirror = startMirror();
        return new BMWCarDataStreamClient(tokens, jobScheduler, this, mirror != null ? mirror::publishMessage : null);
    }

    private CompletableFuture<Boolean> stopStream() {
        return stopStream(true);
    }

    /**
     * @param reportDisconnect whether to report the disconnect to the mirror; not for a reconnect with a new id token
     */
    private CompletableFuture<Boolean> stopStream(boolean reportDisconnect) {
        StreamMirror mirror = this.mirror;
        if (reportDisconnect && mirror != null && streamSource != null) {
            mirror.publishStreamState(false);
        }
        StreamSource source = streamSource;
        streamSource = null;
        streamState = null;
        return source != null ? source.stop() : CompletableFuture.completedFuture(true);
    }

    /**
     * Starts mirroring the direct stream to the local broker if configured. The mirror keeps its own connection, so it
     * is independent of reconnects of the BMW stream.
     *
     * @return the running mirror, or {@code null} if mirroring is not active
     */
    private synchronized @Nullable StreamMirror startMirror() {
        StreamMirror current = mirror;
        if (current != null || !config.isMirrorActive() || disposed) {
            return current;
        }
        try {
            StreamMirror newMirror = new StreamMirror(config.brokerSettings(),
                    "openhab-bmwcardata-mirror-" + getThing().getUID().getId(), jobScheduler,
                    this::onMirrorBrokerState);
            mirror = newMirror;
            newMirror.start();
            return newMirror;
        } catch (BMWCarDataException e) {
            logger.warn("Unable to mirror the CarData stream to the local broker: {}", e.getMessage());
            return null;
        }
    }

    private synchronized void stopMirror() {
        StreamMirror current = mirror;
        mirror = null;
        mirrorConnected = null;
        if (current != null) {
            current.stop();
        }
    }

    /**
     * The state of the local broker of the mirror only affects the status description, not the status, as the data
     * of BMW still arrives.
     */
    private void onMirrorBrokerState(boolean connected) {
        mirrorConnected = connected;
        updateOnlineStatus();
    }

    private void updateOfflineIfActive(ThingStatusDetail detail, @Nullable String description) {
        if (!disposed) {
            synchronized (statusLock) {
                updateStatus(ThingStatus.OFFLINE, detail, description);
            }
        }
    }
}

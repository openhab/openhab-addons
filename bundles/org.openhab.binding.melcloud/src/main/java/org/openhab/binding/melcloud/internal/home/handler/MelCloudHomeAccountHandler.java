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
package org.openhab.binding.melcloud.internal.home.handler;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudHomeAuthException;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeAuthService;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeRequestPacer;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeTokenResponse;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeUserContext;
import org.openhab.binding.melcloud.internal.home.config.MelCloudHomeAccountConfig;
import org.openhab.binding.melcloud.internal.home.discovery.MelCloudHomeUnitDiscoveryService;
import org.openhab.binding.melcloud.internal.home.websocket.MelCloudHomeWebSocketListener;
import org.openhab.binding.melcloud.internal.logging.SensitiveDataMasker;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MelCloudHomeAccountHandler} logs in to the MELCloud Home platform, keeps the access token refreshed, and
 * centrally polls {@code GET /context} for every unit, fanning the result out to the registered unit handlers.
 * It also owns the shared {@link MelCloudHomeRequestPacer} and an optional WebSocket connection that only triggers
 * out-of-cycle polls; failures there fall back to plain polling.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAccountHandler extends BaseBridgeHandler {

    private static final long REFRESH_SAFETY_MARGIN_SECONDS = 60;
    private static final long RETRY_DELAY_SECONDS = 60;
    private static final long CONTEXT_POLL_INTERVAL_SECONDS = 60;

    /** Must be 4-20 characters, [a-zA-Z0-9-_] only, per {@link WebSocketFactory#createWebSocketClient(String)}. */
    private static final String WEBSOCKET_CONSUMER_NAME = "melcloud-home";
    private static final long REALTIME_DEBOUNCE_SECONDS = 2;
    private static final long REALTIME_RECONNECT_INITIAL_SECONDS = 5;
    private static final long REALTIME_RECONNECT_MAX_SECONDS = 300;
    private static final long REALTIME_CONNECT_TIMEOUT_SECONDS = 10;
    private static final int POLL_FAILURES_BEFORE_OFFLINE = 3;

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeAccountHandler.class);

    private final MelCloudHomeAuthService authService;
    private final MelCloudHomeApiClient apiClient;
    private final StorageService storageService;
    private final WebSocketFactory webSocketFactory;

    private final Map<String, MelCloudHomeAtaUnitListener> ataUnitListeners = new ConcurrentHashMap<>();
    private final Map<String, MelCloudHomeAtwUnitListener> atwUnitListeners = new ConcurrentHashMap<>();
    private final MelCloudHomeRequestPacer requestPacer = new MelCloudHomeRequestPacer(scheduler);

    private MelCloudHomeAccountConfig config = new MelCloudHomeAccountConfig();
    private @Nullable Storage<MelCloudHomeAuthState> storage;
    private volatile @Nullable String accessToken;
    private volatile @Nullable ScheduledFuture<?> refreshFuture;
    private volatile @Nullable ScheduledFuture<?> contextPollFuture;
    private volatile boolean disposed;

    private volatile @Nullable WebSocketClient webSocketClient;
    private volatile boolean realtimeConnected;
    private volatile @Nullable ScheduledFuture<?> realtimeDebounceFuture;
    private volatile @Nullable ScheduledFuture<?> realtimeReconnectFuture;
    private volatile long realtimeReconnectDelaySeconds = REALTIME_RECONNECT_INITIAL_SECONDS;
    /** Identifies the current connection attempt, so callbacks of a superseded or abandoned session are ignored. */
    private final AtomicLong realtimeGeneration = new AtomicLong();
    private final AtomicInteger consecutivePollFailures = new AtomicInteger();

    public MelCloudHomeAccountHandler(Bridge bridge, MelCloudHomeAuthService authService,
            MelCloudHomeApiClient apiClient, StorageService storageService, WebSocketFactory webSocketFactory) {
        super(bridge);
        this.authService = authService;
        this.apiClient = apiClient;
        this.storageService = storageService;
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MELCloud Home account handler");
        disposed = false;
        config = getConfigAs(MelCloudHomeAccountConfig.class);
        storage = storageService.getStorage(thing.getUID().toString(), MelCloudHomeAuthState.class.getClassLoader());

        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "username and password are required");
            return;
        }

        if (config.enableRealtimeUpdates) {
            webSocketClient = webSocketFactory.createWebSocketClient(WEBSOCKET_CONSUMER_NAME);
        } else {
            logger.debug("MELCloud Home realtime updates disabled by configuration");
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::authenticate);
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        disposed = true;
        cancelScheduledRefresh();
        cancelContextPoll();
        cancelRealtimeDebounce();
        cancelRealtimeReconnect();
        stopWebSocketClient();
    }

    @Override
    public void handleRemoval() {
        Storage<MelCloudHomeAuthState> currentStorage = storage;
        if (currentStorage != null) {
            currentStorage.remove(thing.getUID().toString());
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // This bridge exposes no channels of its own; nothing to handle.
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MelCloudHomeUnitDiscoveryService.class);
    }

    /**
     * Returns the current MELCloud Home Bearer access token, for child Thing handlers to use directly (e.g. for
     * control commands or per-unit telemetry calls that fall outside the shared {@code /context} poll).
     *
     * @throws MelCloudCommException if the bridge is not currently authenticated
     */
    public String getAccessToken() throws MelCloudCommException {
        String token = accessToken;
        if (token == null) {
            throw new MelCloudCommException("MELCloud Home bridge is not authenticated");
        }
        return token;
    }

    /**
     * @return the shared BFF API client, for child Thing handlers to use directly with {@link #getAccessToken()}
     */
    public MelCloudHomeApiClient getApiClient() {
        return apiClient;
    }

    /**
     * @return the pacer shared by this bridge's poll and every registered unit's control calls, so unit handlers can
     *         route their outbound API calls through the same account-wide spacing
     */
    public MelCloudHomeRequestPacer getRequestPacer() {
        return requestPacer;
    }

    /**
     * Performs a one-off {@code /context} fetch, e.g. for the discovery service. Does not affect the regular
     * polling schedule or registered listeners.
     *
     * @throws MelCloudCommException if not authenticated or the request fails
     */
    public MelCloudHomeUserContext fetchUserContext() throws MelCloudCommException {
        return apiClient.fetchUserContext(getAccessToken());
    }

    public void registerAtaUnitListener(String unitId, MelCloudHomeAtaUnitListener listener) {
        ataUnitListeners.put(unitId, listener);
    }

    public void unregisterAtaUnitListener(String unitId) {
        ataUnitListeners.remove(unitId);
    }

    public void registerAtwUnitListener(String unitId, MelCloudHomeAtwUnitListener listener) {
        atwUnitListeners.put(unitId, listener);
    }

    public void unregisterAtwUnitListener(String unitId) {
        atwUnitListeners.remove(unitId);
    }

    private void authenticate() {
        if (disposed) {
            return;
        }
        Storage<MelCloudHomeAuthState> currentStorage = storage;
        MelCloudHomeAuthState state = currentStorage == null ? null : currentStorage.get(thing.getUID().toString());
        String storedRefreshToken = state == null ? null : state.refreshToken();

        if (storedRefreshToken != null) {
            logger.debug("Attempting to resume the MELCloud Home session with a stored refresh token");
            try {
                onLoginSuccess(authService.refreshToken(storedRefreshToken));
                return;
            } catch (MelCloudHomeAuthException e) {
                logger.debug("Stored refresh token was rejected, falling back to full login: {}", e.getMessage());
            } catch (MelCloudCommException e) {
                // A transient failure says nothing about the refresh token; do not spend a full login on it.
                logger.debug("MELCloud Home token refresh failed, will retry: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduleRetry();
                return;
            }
        }

        try {
            onLoginSuccess(authService.login(config.username, config.password));
        } catch (MelCloudHomeAuthException e) {
            logger.warn("MELCloud Home login rejected: {}", e.getMessage());
            accessToken = null;
            cancelContextPoll();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MelCloudCommException e) {
            logger.warn("MELCloud Home login failed: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleRetry();
        }
    }

    private void onLoginSuccess(MelCloudHomeTokenResponse tokenResponse) {
        if (disposed) {
            // dispose() ran while the login was in flight; do not publish state or reschedule anything.
            return;
        }
        accessToken = tokenResponse.accessToken;
        String newRefreshToken = tokenResponse.refreshToken;
        if (newRefreshToken != null) {
            Storage<MelCloudHomeAuthState> currentStorage = storage;
            if (currentStorage != null) {
                currentStorage.put(thing.getUID().toString(), new MelCloudHomeAuthState(newRefreshToken));
                logger.debug("Persisted a new MELCloud Home refresh token to storage");
            } else {
                logger.warn("Received a refresh token but storage is not available (initialize() not run?); "
                        + "it will not survive a restart");
            }
        } else {
            logger.debug("Token response did not include a refresh token; nothing to persist");
        }
        updateStatus(ThingStatus.ONLINE);
        scheduleRefresh(tokenResponse.expiresIn);
        startContextPollIfNeeded();
        startRealtimeUpdatesIfNeeded();
    }

    private void startContextPollIfNeeded() {
        if (contextPollFuture == null) {
            contextPollFuture = scheduler.scheduleWithFixedDelay(this::pollContext, 0, CONTEXT_POLL_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    private void pollContext() {
        if (ataUnitListeners.isEmpty() && atwUnitListeners.isEmpty()) {
            logger.debug("No unit Things registered, skipping /context poll");
            return;
        }
        requestPacer.schedule(this::doPollContext);
    }

    private void doPollContext() {
        if (disposed) {
            return;
        }
        try {
            MelCloudHomeUserContext context = fetchUserContext();
            onPollSucceeded();
            ataUnitListeners.forEach((unitId, listener) -> context.findAtaUnit(unitId)
                    .ifPresentOrElse(listener::onAtaUnitUpdated, () -> {
                        logger.debug("ATA unit {} not found in /context response", SensitiveDataMasker.maskId(unitId));
                        listener.onAtaUnitMissing();
                    }));
            atwUnitListeners.forEach((unitId, listener) -> context.findAtwUnit(unitId)
                    .ifPresentOrElse(listener::onAtwUnitUpdated, () -> {
                        logger.debug("ATW unit {} not found in /context response", SensitiveDataMasker.maskId(unitId));
                        listener.onAtwUnitMissing();
                    }));
        } catch (MelCloudCommException e) {
            onPollFailed(e);
        }
    }

    private void onPollSucceeded() {
        consecutivePollFailures.set(0);
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        if (statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Counts consecutive poll failures. Once {@link #POLL_FAILURES_BEFORE_OFFLINE} are reached the bridge (and with
     * it every unit Thing) goes offline, and the access token is renewed right away, since a revoked token would
     * otherwise keep failing until its scheduled expiry.
     */
    private void onPollFailed(MelCloudCommException e) {
        int failures = consecutivePollFailures.incrementAndGet();
        logger.debug("MELCloud Home /context poll failed ({} in a row), will retry next cycle: {}", failures,
                e.getMessage());
        if (failures == POLL_FAILURES_BEFORE_OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "MELCloud Home could not be reached");
            scheduler.execute(this::authenticate);
        }
    }

    /**
     * Starts the realtime push connection if enabled and not already connected. Safe to call repeatedly (e.g. on
     * every token refresh, not just the first login) since an already-open session is left alone.
     */
    private void startRealtimeUpdatesIfNeeded() {
        WebSocketClient client = webSocketClient;
        if (client == null || realtimeConnected || realtimeReconnectFuture != null) {
            return;
        }
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                logger.debug("Failed to start MELCloud Home WebSocket client: {}", e.getMessage());
                scheduleRealtimeReconnect();
                return;
            }
        }
        connectRealtimeUpdates(client);
    }

    /**
     * Fetches a fresh hash and opens the connection, blocking the bridge's scheduler thread for up to
     * {@link #REALTIME_CONNECT_TIMEOUT_SECONDS} while the handshake completes.
     */
    private void connectRealtimeUpdates(WebSocketClient client) {
        long generation = realtimeGeneration.incrementAndGet();
        try {
            String hash = apiClient.fetchWebSocketHash(getAccessToken());
            URI uri = apiClient.buildWebSocketUri(hash);
            MelCloudHomeWebSocketListener listener = new MelCloudHomeWebSocketListener(this::onRealtimeDeltaReceived,
                    () -> onRealtimeConnected(generation), () -> onRealtimeDisconnected(generation));
            Future<Session> sessionFuture = client.connect(listener, uri);
            try {
                sessionFuture.get(REALTIME_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                // Abandon the handshake, so it cannot complete later next to the retried connection.
                sessionFuture.cancel(true);
                realtimeGeneration.incrementAndGet();
                throw e;
            }
            // Success: onRealtimeConnected() already ran via the listener's onWebSocketConnect callback.
        } catch (MelCloudCommException | IOException | ExecutionException | TimeoutException
                | IllegalArgumentException e) {
            logger.debug("MELCloud Home WebSocket connect failed, will retry: {}", e.getMessage());
            scheduleRealtimeReconnect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void onRealtimeConnected(long generation) {
        if (generation != realtimeGeneration.get()) {
            return;
        }
        realtimeConnected = true;
        realtimeReconnectDelaySeconds = REALTIME_RECONNECT_INITIAL_SECONDS;
    }

    private void onRealtimeDisconnected(long generation) {
        if (generation != realtimeGeneration.get()) {
            return;
        }
        realtimeConnected = false;
        if (!disposed) {
            logger.info("MELCloud Home realtime connection lost; reconnecting (polling continues meanwhile)");
        }
        scheduleRealtimeReconnect();
    }

    private void scheduleRealtimeReconnect() {
        if (disposed || realtimeReconnectFuture != null) {
            return;
        }
        long delay = realtimeReconnectDelaySeconds;
        realtimeReconnectDelaySeconds = Math.min(delay * 2, REALTIME_RECONNECT_MAX_SECONDS);
        realtimeReconnectFuture = scheduler.schedule(this::reconnectRealtimeUpdates, delay, TimeUnit.SECONDS);
    }

    private void reconnectRealtimeUpdates() {
        realtimeReconnectFuture = null;
        WebSocketClient client = webSocketClient;
        if (client == null || disposed) {
            return;
        }
        connectRealtimeUpdates(client);
    }

    /**
     * Called by {@link MelCloudHomeWebSocketListener} for every unit ID mentioned in a {@code unitStateChanged}
     * frame. Never applies the frame's own content as state; only triggers a debounced, out-of-cycle
     * {@link #pollContext()} for units this bridge actually has a registered listener for.
     */
    private void onRealtimeDeltaReceived(String unitId) {
        if (!ataUnitListeners.containsKey(unitId) && !atwUnitListeners.containsKey(unitId)) {
            logger.debug("Ignoring realtime delta for unit {} with no registered listener",
                    SensitiveDataMasker.maskId(unitId));
            return;
        }
        if (realtimeDebounceFuture != null) {
            // A refresh is already pending; this and any other delta in the same window are coalesced into it.
            return;
        }
        realtimeDebounceFuture = scheduler.schedule(this::runDebouncedPoll, REALTIME_DEBOUNCE_SECONDS,
                TimeUnit.SECONDS);
    }

    private void runDebouncedPoll() {
        realtimeDebounceFuture = null;
        pollContext();
    }

    private void scheduleRefresh(long expiresInSeconds) {
        cancelScheduledRefresh();
        if (disposed) {
            return;
        }
        long delaySeconds = Math.max(0, expiresInSeconds - REFRESH_SAFETY_MARGIN_SECONDS);
        logger.debug("Scheduling the next MELCloud Home token refresh in {}s", delaySeconds);
        refreshFuture = scheduler.schedule(this::authenticate, delaySeconds, TimeUnit.SECONDS);
    }

    private void scheduleRetry() {
        cancelScheduledRefresh();
        if (disposed) {
            return;
        }
        logger.debug("Scheduling a MELCloud Home login retry in {}s", RETRY_DELAY_SECONDS);
        refreshFuture = scheduler.schedule(this::authenticate, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void cancelScheduledRefresh() {
        ScheduledFuture<?> future = refreshFuture;
        if (future != null) {
            future.cancel(false);
            refreshFuture = null;
        }
    }

    private void cancelContextPoll() {
        ScheduledFuture<?> future = contextPollFuture;
        if (future != null) {
            future.cancel(true);
            contextPollFuture = null;
        }
    }

    private void cancelRealtimeDebounce() {
        ScheduledFuture<?> future = realtimeDebounceFuture;
        if (future != null) {
            future.cancel(true);
            realtimeDebounceFuture = null;
        }
    }

    private void cancelRealtimeReconnect() {
        ScheduledFuture<?> future = realtimeReconnectFuture;
        if (future != null) {
            future.cancel(true);
            realtimeReconnectFuture = null;
        }
    }

    private void stopWebSocketClient() {
        WebSocketClient client = webSocketClient;
        webSocketClient = null;
        realtimeConnected = false;
        realtimeGeneration.incrementAndGet();
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Error stopping MELCloud Home WebSocket client: {}", e.getMessage());
            }
        }
    }
}

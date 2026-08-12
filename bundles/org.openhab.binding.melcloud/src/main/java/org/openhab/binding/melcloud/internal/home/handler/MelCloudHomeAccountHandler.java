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
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MelCloudHomeAccountHandler} logs in to the MELCloud Home platform, keeps the resulting access token
 * refreshed, and centrally polls {@code GET /context} for every ATA/ATW unit under the account, fanning the result
 * out to registered unit Thing handlers.
 *
 * <p>
 * Implements the login/refresh strategy plus the unit polling/fan-out strategy: one {@code /context} call every
 * {@link #CONTEXT_POLL_INTERVAL_SECONDS} serves every registered unit, rather than each unit Thing polling
 * independently, given the platform's known rate-limit sensitivity.
 *
 * <p>
 * Also owns the single {@link MelCloudHomeRequestPacer} shared by this bridge's {@code /context} poll and every
 * registered unit's control calls, since the MELCloud Home rate limit is account-wide (see ADR-008).
 *
 * <p>
 * Optionally owns a WebSocket connection to the MELCloud Home realtime push channel (see ADR-007). It only ever
 * triggers an out-of-cycle {@code /context} poll — never a state source in its own right — and any failure falls
 * back to the fixed-interval poll without affecting bridge status.
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
     *         route their outbound API calls through the same account-wide spacing (ADR-008)
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
        Storage<MelCloudHomeAuthState> currentStorage = storage;
        MelCloudHomeAuthState state = currentStorage == null ? null : currentStorage.get(thing.getUID().toString());
        String storedRefreshToken = state == null ? null : state.refreshToken();

        if (storedRefreshToken != null) {
            logger.debug("Attempting to resume the MELCloud Home session with a stored refresh token");
            try {
                onLoginSuccess(authService.refreshToken(storedRefreshToken));
                return;
            } catch (MelCloudCommException e) {
                logger.debug("Stored refresh token was rejected, falling back to full login: {}", e.getMessage());
            }
        }

        try {
            onLoginSuccess(authService.login(config.username, config.password));
        } catch (MelCloudHomeAuthException e) {
            logger.warn("MELCloud Home login rejected: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MelCloudCommException e) {
            logger.warn("MELCloud Home login failed: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleRetry();
        }
    }

    private void onLoginSuccess(MelCloudHomeTokenResponse tokenResponse) {
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
        try {
            MelCloudHomeUserContext context = fetchUserContext();
            ataUnitListeners.forEach(
                    (unitId, listener) -> context.findAtaUnit(unitId).ifPresentOrElse(listener::onAtaUnitUpdated,
                            () -> logger.debug("ATA unit {} not found in /context response", unitId)));
            atwUnitListeners.forEach(
                    (unitId, listener) -> context.findAtwUnit(unitId).ifPresentOrElse(listener::onAtwUnitUpdated,
                            () -> logger.debug("ATW unit {} not found in /context response", unitId)));
        } catch (MelCloudCommException e) {
            logger.debug("MELCloud Home /context poll failed, will retry next cycle: {}", e.getMessage());
        }
    }

    /**
     * Starts the realtime push connection if enabled and not already connected. Safe to call repeatedly (e.g. on
     * every token refresh, not just the first login) since an already-open session is left alone.
     */
    private void startRealtimeUpdatesIfNeeded() {
        WebSocketClient client = webSocketClient;
        if (client == null || realtimeConnected) {
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
     * Fetches a fresh hash and opens the connection, blocking this call's own thread (always the bridge's own
     * {@code scheduler}, never a caller outside this class — see {@link #startRealtimeUpdatesIfNeeded()} and
     * {@link #reconnectRealtimeUpdates()}) up to {@link #REALTIME_CONNECT_TIMEOUT_SECONDS} while the handshake
     * completes. {@link WebSocketClient#connect(Object, URI)} returns a plain {@link Future}, not a
     * {@code CompletableFuture} — there is no non-blocking way to observe its outcome, so this mirrors every other
     * blocking network call already made from a scheduler task in this class (e.g. {@link #authenticate()}).
     */
    private void connectRealtimeUpdates(WebSocketClient client) {
        try {
            String hash = apiClient.fetchWebSocketHash(getAccessToken());
            URI uri = apiClient.buildWebSocketUri(hash);
            MelCloudHomeWebSocketListener listener = new MelCloudHomeWebSocketListener(this::onRealtimeDeltaReceived,
                    this::onRealtimeConnected, this::onRealtimeDisconnected);
            Future<Session> sessionFuture = client.connect(listener, uri);
            sessionFuture.get(REALTIME_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            // Success: onRealtimeConnected() already ran via the listener's onWebSocketConnect callback.
        } catch (MelCloudCommException | IOException | ExecutionException | TimeoutException e) {
            logger.debug("MELCloud Home WebSocket connect failed, will retry: {}", e.getMessage());
            scheduleRealtimeReconnect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void onRealtimeConnected() {
        realtimeConnected = true;
        realtimeReconnectDelaySeconds = REALTIME_RECONNECT_INITIAL_SECONDS;
    }

    private void onRealtimeDisconnected() {
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
     * {@link #pollContext()} for units this bridge actually has a registered listener for (ADR-007).
     */
    private void onRealtimeDeltaReceived(String unitId) {
        if (!ataUnitListeners.containsKey(unitId) && !atwUnitListeners.containsKey(unitId)) {
            logger.debug("Ignoring realtime delta for unit {} with no registered listener", unitId);
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
        long delaySeconds = Math.max(0, expiresInSeconds - REFRESH_SAFETY_MARGIN_SECONDS);
        logger.debug("Scheduling the next MELCloud Home token refresh in {}s", delaySeconds);
        refreshFuture = scheduler.schedule(this::authenticate, delaySeconds, TimeUnit.SECONDS);
    }

    private void scheduleRetry() {
        cancelScheduledRefresh();
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
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Error stopping MELCloud Home WebSocket client: {}", e.getMessage());
            }
        }
    }
}

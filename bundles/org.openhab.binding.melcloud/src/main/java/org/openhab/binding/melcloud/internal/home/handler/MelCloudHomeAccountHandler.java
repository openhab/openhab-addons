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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudHomeAuthException;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeAuthService;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeTokenResponse;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeUserContext;
import org.openhab.binding.melcloud.internal.home.config.MelCloudHomeAccountConfig;
import org.openhab.binding.melcloud.internal.home.discovery.MelCloudHomeUnitDiscoveryService;
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
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAccountHandler extends BaseBridgeHandler {

    private static final long REFRESH_SAFETY_MARGIN_SECONDS = 60;
    private static final long RETRY_DELAY_SECONDS = 60;
    private static final long CONTEXT_POLL_INTERVAL_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeAccountHandler.class);

    private final MelCloudHomeAuthService authService;
    private final MelCloudHomeApiClient apiClient;
    private final StorageService storageService;

    private final Map<String, MelCloudHomeAtaUnitListener> ataUnitListeners = new ConcurrentHashMap<>();
    private final Map<String, MelCloudHomeAtwUnitListener> atwUnitListeners = new ConcurrentHashMap<>();

    private MelCloudHomeAccountConfig config = new MelCloudHomeAccountConfig();
    private @Nullable Storage<MelCloudHomeAuthState> storage;
    private volatile @Nullable String accessToken;
    private volatile @Nullable ScheduledFuture<?> refreshFuture;
    private volatile @Nullable ScheduledFuture<?> contextPollFuture;

    public MelCloudHomeAccountHandler(Bridge bridge, MelCloudHomeAuthService authService,
            MelCloudHomeApiClient apiClient, StorageService storageService) {
        super(bridge);
        this.authService = authService;
        this.apiClient = apiClient;
        this.storageService = storageService;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MELCloud Home account handler");
        config = getConfigAs(MelCloudHomeAccountConfig.class);
        storage = storageService.getStorage(thing.getUID().toString(), MelCloudHomeAuthState.class.getClassLoader());

        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "username and password are required");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::authenticate);
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        cancelScheduledRefresh();
        cancelContextPoll();
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
}

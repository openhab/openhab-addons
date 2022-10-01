/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.handler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jellyfin.sdk.Jellyfin;
import org.jellyfin.sdk.JellyfinOptions;
import org.jellyfin.sdk.api.client.ApiClient;
import org.jellyfin.sdk.api.client.exception.ApiClientException;
import org.jellyfin.sdk.api.client.exception.InvalidStatusException;
import org.jellyfin.sdk.api.client.exception.MissingUserIdException;
import org.jellyfin.sdk.api.operations.ItemsApi;
import org.jellyfin.sdk.api.operations.SessionApi;
import org.jellyfin.sdk.api.operations.SystemApi;
import org.jellyfin.sdk.api.operations.TvShowsApi;
import org.jellyfin.sdk.api.operations.UserApi;
import org.jellyfin.sdk.model.ClientInfo;
import org.jellyfin.sdk.model.api.AuthenticateUserByName;
import org.jellyfin.sdk.model.api.AuthenticationResult;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult;
import org.jellyfin.sdk.model.api.BaseItemKind;
import org.jellyfin.sdk.model.api.ItemFields;
import org.jellyfin.sdk.model.api.MessageCommand;
import org.jellyfin.sdk.model.api.PlayCommand;
import org.jellyfin.sdk.model.api.PlaystateCommand;
import org.jellyfin.sdk.model.api.SessionInfo;
import org.jellyfin.sdk.model.api.SystemInfo;
import org.openhab.binding.jellyfin.internal.JellyfinServerConfiguration;
import org.openhab.binding.jellyfin.internal.discovery.JellyfinClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.util.EmptySyncResponse;
import org.openhab.binding.jellyfin.internal.util.SyncCallback;
import org.openhab.binding.jellyfin.internal.util.SyncResponse;
import org.openhab.core.OpenHAB;
import org.openhab.core.cache.ExpiringCache;
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

import io.ktor.http.URLBuilder;
import io.ktor.http.URLProtocol;

/**
 * The {@link JellyfinServerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class JellyfinServerHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(JellyfinServerHandler.class);
    private final ApiClient jellyApiClient;
    private final ExpiringCache<List<SessionInfo>> sessionsCache = new ExpiringCache<>(
            Duration.of(1, ChronoUnit.SECONDS), this::tryGetSessions);
    private JellyfinServerConfiguration config = new JellyfinServerConfiguration();
    private @Nullable ScheduledFuture<?> checkInterval;

    public JellyfinServerHandler(Bridge bridge) {
        super(bridge);
        var options = new JellyfinOptions.Builder();
        options.setClientInfo(new ClientInfo("openHAB", OpenHAB.getVersion()));
        options.setDeviceInfo(new org.jellyfin.sdk.model.DeviceInfo(getThing().getUID().getId(), "openHAB"));
        jellyApiClient = new Jellyfin(options.build()).createApi();
    }

    @Override
    public void initialize() {
        config = getConfigAs(JellyfinServerConfiguration.class);
        jellyApiClient.setBaseUrl(getServerUrl());
        if (config.token.isBlank() || config.userId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Navigate to <your local openhab url>/jellyfin/" + this.getThing().getUID().getId() + " to login.");
            return;
        }
        jellyApiClient.setAccessToken(config.token);
        jellyApiClient.setUserId(UUID.fromString(config.userId));
        updateStatus(ThingStatus.UNKNOWN);
        startChecker();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        super.dispose();
        stopChecker();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(JellyfinClientDiscoveryService.class);
    }

    public String getServerUrl() {
        var builder = new URLBuilder();
        builder.setHost(config.hostname);
        if (config.ssl) {
            builder.setProtocol(URLProtocol.Companion.getHTTPS());
        } else {
            builder.setProtocol(URLProtocol.Companion.getHTTP());
        }
        builder.setPort(config.port);
        builder.setEncodedPath(config.path);
        return builder.buildString();
    }

    public boolean isOnline() {
        var asyncResponse = new SyncResponse<String>();
        new SystemApi(jellyApiClient).getPingSystem(asyncResponse);
        try {
            return asyncResponse.awaitResponse().getStatus() == 200;
        } catch (SyncCallback.SyncCallbackError | ApiClientException e) {
            logger.warn("Response error: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAuthenticated() {
        if (config.token.isBlank() || config.userId.isBlank()) {
            return false;
        }
        var asyncResponse = new SyncResponse<SystemInfo>();
        new SystemApi(jellyApiClient).getSystemInfo(asyncResponse);
        try {
            var systemInfo = asyncResponse.awaitContent();
            var properties = editProperties();
            var productName = systemInfo.getProductName();
            if (productName != null) {
                properties.put(Thing.PROPERTY_VENDOR, productName);
            }
            var version = systemInfo.getVersion();
            if (version != null) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, version);
            }
            updateProperties(properties);
            return true;
        } catch (SyncCallback.SyncCallbackError | ApiClientException e) {
            return false;
        }
    }

    public JellyfinCredentials login(String user, String password)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var asyncCall = new SyncResponse<AuthenticationResult>();
        new UserApi(jellyApiClient).authenticateUserByName(new AuthenticateUserByName(user, password, null), asyncCall);
        var authResult = asyncCall.awaitContent();
        var token = Objects.requireNonNull(authResult.getAccessToken());
        var userId = Objects.requireNonNull(authResult.getUser()).getId().toString();
        return new JellyfinCredentials(token, userId);
    }

    public void updateCredentials(JellyfinCredentials credentials) {
        var currentConfig = getConfig();
        currentConfig.put("token", credentials.getAccessToken());
        currentConfig.put("userId", credentials.getUserId());
        updateConfiguration(currentConfig);
        initialize();
    }

    private void updateStatusUnauthenticated() {
        sessionsCache.invalidateValue();
        updateClients(List.of());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Authentication failed. Navigate to <your local openhab url>/jellyfin/"
                        + this.getThing().getUID().getId() + " to login again.");
    }

    private void checkClientStates() {
        var sessions = sessionsCache.getValue();
        if (sessions != null) {
            logger.debug("Got {} sessions", sessions.size());
            updateClients(sessions);
        } else {
            sessionsCache.invalidateValue();
        }
    }

    private @Nullable List<SessionInfo> tryGetSessions() {
        try {
            if (jellyApiClient.getAccessToken() == null) {
                return null;
            }
            var clientActiveWithInSeconds = config.clientActiveWithInSeconds != 0 ? config.clientActiveWithInSeconds
                    : null;
            return getControllableSessions(clientActiveWithInSeconds);
        } catch (SyncCallback.SyncCallbackError syncCallbackError) {
            logger.warn("Unexpected error while running channel calling server: {}", syncCallbackError.getMessage());
        } catch (ApiClientException e) {
            handleApiException(e);
        }
        return null;
    }

    public void handleApiException(ApiClientException e) {
        logger.warn("Api error: {}", e.getMessage());
        var cause = e.getCause();
        boolean unauthenticated = false;
        if (cause instanceof InvalidStatusException) {
            var status = ((InvalidStatusException) cause).getStatus();
            if (status == 401) {
                unauthenticated = true;
            }
            logger.warn("Api error has invalid status: {}", status);
        }
        if (cause instanceof MissingUserIdException) {
            unauthenticated = true;
        }
        if (unauthenticated) {
            updateStatusUnauthenticated();
        }
    }

    public void updateClientState(JellyfinClientHandler handler) {
        var sessions = sessionsCache.getValue();
        if (sessions != null) {
            updateClientState(handler, sessions);
        } else {
            sessionsCache.invalidateValue();
        }
    }

    public List<SessionInfo> getControllableSessions() throws SyncCallback.SyncCallbackError, ApiClientException {
        return getControllableSessions(null);
    }

    public List<SessionInfo> getControllableSessions(@Nullable Integer activeWithInSeconds)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var asyncContinuation = new SyncResponse<List<SessionInfo>>();
        new SessionApi(jellyApiClient).getSessions(this.jellyApiClient.getUserId(), null, activeWithInSeconds,
                asyncContinuation);
        return asyncContinuation.awaitContent();
    }

    public void sendPlayStateCommand(String sessionId, PlaystateCommand command, @Nullable Long seekPositionTicks)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var awaiter = new EmptySyncResponse();
        new SessionApi(jellyApiClient).sendPlaystateCommand(sessionId, command, seekPositionTicks,
                Objects.requireNonNull(jellyApiClient.getUserId()).toString(), awaiter);
        awaiter.awaitResponse();
    }

    public void sendDeviceMessage(String sessionId, String header, String text, long ms)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var awaiter = new EmptySyncResponse();
        new SessionApi(jellyApiClient).sendMessageCommand(sessionId, new MessageCommand(header, text, ms), awaiter);
        awaiter.awaitResponse();
    }

    public void playItem(String sessionId, PlayCommand playCommand, String itemId, @Nullable Long startPositionTicks)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var awaiter = new EmptySyncResponse();
        new SessionApi(jellyApiClient).play(sessionId, playCommand, List.of(UUID.fromString(itemId)),
                startPositionTicks, null, null, null, null, awaiter);
        awaiter.awaitResponse();
    }

    public void browseToItem(String sessionId, BaseItemKind itemType, String itemId, String itemName)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var awaiter = new EmptySyncResponse();
        new SessionApi(jellyApiClient).displayContent(sessionId, itemType, itemId, itemName, awaiter);
        awaiter.awaitResponse();
    }

    public @Nullable BaseItemDto getSeriesNextUpItem(UUID seriesId)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        return getSeriesNextUpItems(seriesId, 1).stream().findFirst().orElse(null);
    }

    public List<BaseItemDto> getSeriesNextUpItems(UUID seriesId, int limit)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var asyncContinuation = new SyncResponse<BaseItemDtoQueryResult>();
        new TvShowsApi(jellyApiClient).getNextUp(jellyApiClient.getUserId(), null, limit, null, seriesId.toString(),
                null, null, null, null, null, null, null, null, null, asyncContinuation);
        var result = asyncContinuation.awaitContent();
        return Objects.requireNonNull(result.getItems());
    }

    public @Nullable BaseItemDto getSeriesResumeItem(UUID seriesId)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        return getSeriesResumeItems(seriesId, 1).stream().findFirst().orElse(null);
    }

    public List<BaseItemDto> getSeriesResumeItems(UUID seriesId, int limit)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var asyncContinuation = new SyncResponse<BaseItemDtoQueryResult>();
        new ItemsApi(jellyApiClient).getResumeItems(Objects.requireNonNull(jellyApiClient.getUserId()), null, limit,
                null, seriesId, null, null, true, null, null, null, List.of(BaseItemKind.EPISODE), null, null, null,
                asyncContinuation);
        var result = asyncContinuation.awaitContent();
        return Objects.requireNonNull(result.getItems());
    }

    public @Nullable BaseItemDto getSeriesEpisodeItem(UUID seriesId, @Nullable Integer season,
            @Nullable Integer episode) throws SyncCallback.SyncCallbackError, ApiClientException {
        return getSeriesEpisodeItems(seriesId, season, episode, 1).stream().findFirst().orElse(null);
    }

    public List<BaseItemDto> getSeriesEpisodeItems(UUID seriesId, @Nullable Integer season, @Nullable Integer episode,
            int limit) throws SyncCallback.SyncCallbackError, ApiClientException {
        var asyncContinuation = new SyncResponse<BaseItemDtoQueryResult>();
        new TvShowsApi(jellyApiClient).getEpisodes(seriesId, jellyApiClient.getUserId(), null, season, null, null, null,
                null, episode != null ? episode - 1 : null, limit, null, null, null, null, null, asyncContinuation);
        var result = asyncContinuation.awaitContent();
        return Objects.requireNonNull(result.getItems());
    }

    public @Nullable BaseItemDto getItem(UUID id, @Nullable List<ItemFields> fields)
            throws SyncCallback.SyncCallbackError, ApiClientException {
        var asyncContinuation = new SyncResponse<BaseItemDtoQueryResult>();
        new ItemsApi(jellyApiClient).getItems(jellyApiClient.getUserId(), null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, 1, true, null, null, null, fields, null, null, null,
                null, null, null, null, null, null, null, null, null, null, 1, null, null, null, null, null, null, null,
                null, null, null, null, null, List.of(id), null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, false, false, asyncContinuation);
        var response = asyncContinuation.awaitContent();
        return Objects.requireNonNull(response.getItems()).stream().findFirst().orElse(null);
    }

    public @Nullable BaseItemDto searchItem(@Nullable String searchTerm, @Nullable BaseItemKind itemType,
            @Nullable List<ItemFields> fields) throws SyncCallback.SyncCallbackError, ApiClientException {
        return searchItems(searchTerm, itemType, fields, 1).stream().findFirst().orElse(null);
    }

    public List<BaseItemDto> searchItems(@Nullable String searchTerm, @Nullable BaseItemKind itemType,
            @Nullable List<ItemFields> fields, int limit) throws SyncCallback.SyncCallbackError, ApiClientException {
        var asyncContinuation = new SyncResponse<BaseItemDtoQueryResult>();
        var itemTypes = itemType != null ? List.of(itemType) : null;
        new ItemsApi(jellyApiClient).getItems(jellyApiClient.getUserId(), null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, limit, true, searchTerm, null, null, fields, null,
                itemTypes, null, null, null, null, null, null, null, null, null, null, null, 1, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, false, false, asyncContinuation);
        var response = asyncContinuation.awaitContent();
        return Objects.requireNonNull(response.getItems());
    }

    private void startChecker() {
        stopChecker();
        checkInterval = scheduler.scheduleWithFixedDelay(() -> {
            if (!isOnline()) {
                updateStatus(ThingStatus.OFFLINE);
                return;
            } else if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
                if (!isAuthenticated()) {
                    updateStatusUnauthenticated();
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
            }
            checkClientStates();
        }, 0, config.refreshSeconds, TimeUnit.SECONDS);
    }

    private void stopChecker() {
        var checkInterval = this.checkInterval;
        if (checkInterval != null) {
            checkInterval.cancel(true);
            this.checkInterval = null;
        }
    }

    private void updateClients(List<SessionInfo> sessions) {
        var things = getThing().getThings();
        things.forEach((childThing) -> {
            var handler = childThing.getHandler();
            if (handler == null) {
                return;
            }
            if (handler instanceof JellyfinClientHandler) {
                updateClientState((JellyfinClientHandler) handler, sessions);
            } else {
                logger.warn("Found unknown thing-handler instance: {}", handler);
            }
        });
    }

    private void updateClientState(JellyfinClientHandler handler, List<SessionInfo> sessions) {
        @Nullable
        SessionInfo clientSession = sessions.stream()
                .filter(session -> Objects.equals(session.getDeviceId(), handler.getThing().getUID().getId()))
                .sorted((a, b) -> b.getLastActivityDate().compareTo(a.getLastActivityDate())).findFirst().orElse(null);
        handler.updateStateFromSession(clientSession);
    }

    public static class JellyfinCredentials {
        private final String accessToken;
        private final String userId;

        public JellyfinCredentials(String accessToken, String userId) {
            this.accessToken = accessToken;
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}

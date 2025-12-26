/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.ItemsApi;
import org.openhab.binding.jellyfin.internal.api.generated.current.SessionApi;
import org.openhab.binding.jellyfin.internal.api.generated.current.UserLibraryApi;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MessageCommand;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlayCommand;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaystateCommand;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.events.ErrorEvent;
import org.openhab.binding.jellyfin.internal.events.ErrorEventBus;
import org.openhab.binding.jellyfin.internal.events.ErrorEventListener;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.types.ServerState;
import org.openhab.binding.jellyfin.internal.util.client.ClientListUpdater;
import org.openhab.binding.jellyfin.internal.util.config.SystemInfoConfigurationExtractor;
import org.openhab.binding.jellyfin.internal.util.config.UriConfigurationExtractor;
import org.openhab.binding.jellyfin.internal.util.session.SessionManager;
import org.openhab.binding.jellyfin.internal.util.state.ServerStateManager;
import org.openhab.binding.jellyfin.internal.util.user.UserManager;
import org.openhab.core.config.core.validation.ConfigValidationException;
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
 * The {@link ServerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Patrik Gfeller - Adjustments to work independently of the Android SDK
 *         and respective runtime
 *
 */
@NonNullByDefault
public class ServerHandler extends BaseBridgeHandler implements ErrorEventListener {

    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final ErrorEventBus errorEventBus;
    private final SessionEventBus sessionEventBus;
    private final ApiClient apiClient;
    private final Configuration configuration;
    private final TaskManagerInterface taskManager;

    // Utility classes for better separation of concerns
    private final UserManager userManager;
    private final ServerStateManager serverStateManager;
    private final SessionManager sessionManager;

    private ServerState state = ServerState.INITIALIZING;

    private final Map<String, AbstractTask> tasks = new HashMap<>();
    private final Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private final List<String> activeUserIds = new ArrayList<>();

    // Discovery service for automatically discovering client devices
    @Nullable
    private ClientDiscoveryService discoveryService;

    /**
     * Constructor with dependency injection for TaskManager
     *
     * @param bridge The openHAB bridge
     * @param apiClient The API client for Jellyfin communication
     * @param taskManager The task manager that handles all task operations
     */
    public ServerHandler(Bridge bridge, ApiClient apiClient, TaskManagerInterface taskManager) {
        super(bridge);

        this.configuration = this.getConfigAs(Configuration.class);
        this.apiClient = apiClient;
        this.taskManager = taskManager;

        // Initialize utility classes for better separation of concerns
        this.userManager = new UserManager();
        this.serverStateManager = new ServerStateManager();

        // Create event buses
        this.errorEventBus = new ErrorEventBus();
        this.errorEventBus.addListener(this);
        this.sessionEventBus = new SessionEventBus();
        this.sessionManager = new SessionManager(this.sessionEventBus);

        // Initialize tasks through the task manager
        this.tasks.putAll(
                taskManager.initializeTasks(apiClient, errorEventBus, systemInfo -> this.handleConnection(systemInfo),
                        users -> this.handleUsersList(users), this, discoveryService));

        // Optionally add WebSocket task for real-time updates (preferred over polling)
        try {
            if (this.configuration.useWebSocket) {
                var wsHandler = new org.openhab.binding.jellyfin.internal.server.SessionsMessageHandler(apiClient,
                        this.sessionManager);
                // Pass fallback callback that switches to polling when WebSocket exhausts retries
                var wsTask = new org.openhab.binding.jellyfin.internal.server.WebSocketTask(apiClient,
                        this.configuration.token, wsHandler, () -> this.handleWebSocketFallback());
                this.tasks.put(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID, wsTask);
                logger.debug("WebSocketTask added to task registry (useWebSocket=true)");
            } else {
                logger.debug("WebSocket disabled by configuration; using polling only");
            }
        } catch (Exception ex) {
            logger.warn("Failed to initialize WebSocketTask: {}", ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // Server-side helper wrappers
    // ---------------------------------------------------------------------

    /**
     * Handles WebSocket fallback to polling when max reconnection attempts exceeded.
     * Called by WebSocketTask when it exhausts retry attempts.
     * Stops WebSocket task and starts polling task (ServerSyncTask).
     */
    private void handleWebSocketFallback() {
        logger.warn("WebSocket fallback triggered: stopping WebSocket and starting polling task");

        // Stop WebSocket task
        ScheduledFuture<?> wsSchedule = this.scheduledTasks
                .get(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID);
        if (wsSchedule != null) {
            wsSchedule.cancel(true);
            this.scheduledTasks.remove(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID);
        }

        // Dispose WebSocket resources
        AbstractTask wsTask = this.tasks.get(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID);
        if (wsTask instanceof org.openhab.binding.jellyfin.internal.server.WebSocketTask) {
            ((org.openhab.binding.jellyfin.internal.server.WebSocketTask) wsTask).dispose();
        }

        // Start polling task as fallback
        AbstractTask pollingTask = this.tasks
                .get(org.openhab.binding.jellyfin.internal.handler.tasks.ServerSyncTask.TASK_ID);
        if (pollingTask != null && this.scheduler != null) {
            ScheduledFuture<?> scheduledTask = this.scheduler.scheduleWithFixedDelay(pollingTask,
                    pollingTask.getStartupDelay(), pollingTask.getInterval(), TimeUnit.SECONDS);
            this.scheduledTasks.put(org.openhab.binding.jellyfin.internal.handler.tasks.ServerSyncTask.TASK_ID,
                    scheduledTask);
            logger.info("Fallback to polling: ServerSyncTask started");
        } else {
            logger.error("Cannot start polling fallback: ServerSyncTask not available or scheduler is null");
        }
    }

    /**
     * Send a simple message (notification) to a client session.
     */
    public void sendDeviceMessage(@Nullable String sessionId, String header, String message, Integer timeoutMs) {
        try {
            SessionApi sessionApi = new SessionApi(apiClient);
            MessageCommand msg = new MessageCommand();
            msg.setHeader(header);
            msg.setText(message);
            if (timeoutMs != null) {
                msg.setTimeoutMs(timeoutMs.longValue());
            }
            sessionApi.sendMessageCommand(sessionId, msg);
        } catch (Exception e) {
            logger.warn("Failed to send device message to session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Forward a playstate command (pause/resume/stop/seek) to a session.
     */
    public void sendPlayStateCommand(@Nullable String sessionId, PlaystateCommand command,
            @Nullable Long seekPositionTicks, @Nullable String controllingUserId) {
        try {
            SessionApi sessionApi = new SessionApi(apiClient);
            // controllingUserId may be null
            sessionApi.sendPlaystateCommand(sessionId, command, seekPositionTicks == null ? 0L : seekPositionTicks,
                    controllingUserId == null ? null : controllingUserId);
        } catch (Exception e) {
            logger.warn("Failed to send playstate command {} to session {}: {}", command, sessionId, e.getMessage());
        }
    }

    /**
     * Convenience overload without controllingUserId
     */
    public void sendPlayStateCommand(@Nullable String sessionId, PlaystateCommand command,
            @Nullable Long seekPositionTicks) {
        sendPlayStateCommand(sessionId, command, seekPositionTicks, null);
    }

    /**
     * Send a general command to a session (shuffle, repeat, quality, audio track, subtitle, etc.).
     */
    public void sendGeneralCommand(@Nullable String sessionId, Object generalCommand) {
        try {
            org.openhab.binding.jellyfin.internal.api.generated.current.SessionApi sessionApi = new org.openhab.binding.jellyfin.internal.api.generated.current.SessionApi(
                    apiClient);
            if (generalCommand instanceof org.openhab.binding.jellyfin.internal.api.generated.current.model.GeneralCommand) {
                sessionApi.sendFullGeneralCommand(sessionId,
                        (org.openhab.binding.jellyfin.internal.api.generated.current.model.GeneralCommand) generalCommand);
            } else {
                logger.warn("Invalid general command type: {}", generalCommand.getClass().getName());
            }
        } catch (Exception e) {
            logger.warn("Failed to send general command to session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Ask a session to play an item (or list of items) using PlayCommand.
     */
    public void playItem(@Nullable String sessionId, PlayCommand playCommand, String itemId,
            @Nullable Long startPositionTicks) {
        try {
            SessionApi sessionApi = new SessionApi(apiClient);
            // play API expects item ids as UUID list
            java.util.List<java.util.UUID> items = new java.util.ArrayList<>();
            try {
                items.add(java.util.UUID.fromString(itemId));
            } catch (Exception ignore) {
                logger.warn("Invalid UUID for playItem: {}", itemId);
            }
            sessionApi.play(sessionId, playCommand, items, startPositionTicks, null, null, null, null);
        } catch (Exception e) {
            logger.warn("Failed to request play for item {} on session {}: {}", itemId, sessionId, e.getMessage());
        }
    }

    /**
     * Run a simple search for items and return the first result (if any). This is a convenience method.
     */
    public @Nullable BaseItemDto searchItem(@Nullable String userId, String searchTerm, BaseItemKind kind) {
        try {
            // Determine a user id to use, falling back to the first active user if not provided
            java.util.UUID uid = null;
            if (userId != null) {
                try {
                    uid = java.util.UUID.fromString(userId);
                } catch (Exception e) {
                    // ignore and fall back
                    uid = null;
                }
            }
            if (uid == null) {
                synchronized (activeUserIds) {
                    if (!activeUserIds.isEmpty()) {
                        try {
                            uid = java.util.UUID.fromString(activeUserIds.get(0));
                        } catch (Exception ex) {
                            uid = null;
                        }
                    }
                }
            }
            if (uid == null) {
                // No user id available; cannot perform item search
                return null;
            }
            ItemsApi itemsApi = new ItemsApi(apiClient);
            BaseItemDtoQueryResult result = itemsApi.getItems(uid, /* maxOfficialRating */ null,
                    /* hasThemeSong */ null, /* hasThemeVideo */ null, /* hasSubtitles */ null,
                    /* hasSpecialFeature */ null, /* hasTrailer */ null, /* adjacentTo */ null, /* indexNumber */ null,
                    /* parentIndexNumber */ null, /* hasParentalRating */ null, /* isHd */ null, /* is4K */ null,
                    /* locationTypes */ null, /* excludeLocationTypes */ null, /* isMissing */ null,
                    /* isUnaired */ null, /* minCommunityRating */ null, /* minCriticRating */ null,
                    /* minPremiereDate */ null, /* minDateLastSaved */ null, /* minDateLastSavedForUser */ null,
                    /* maxPremiereDate */ null, /* hasOverview */ null, /* hasImdbId */ null, /* hasTmdbId */ null,
                    /* hasTvdbId */ null, /* isMovie */ null, /* isSeries */ null, /* isNews */ null, /* isKids */ null,
                    /* isSports */ null, /* excludeItemIds */ null, /* startIndex */ 0, /* limit */ 1,
                    /* recursive */ null, /* searchTerm */ searchTerm, /* sortOrder */ null, /* parentId */ null,
                    /* fields */ null, /* excludeItemTypes */ null, /* includeItemTypes */ null, /* filters */ null,
                    /* isFavorite */ null, /* mediaTypes */ null, /* imageTypes */ null, /* sortBy */ null,
                    /* isPlayed */ null, /* genres */ null, /* officialRatings */ null, /* tags */ null,
                    /* years */ null, /* enableUserData */ null, /* imageTypeLimit */ null, /* enableImageTypes */ null,
                    /* person */ null, /* personIds */ null, /* personTypes */ null, /* studios */ null,
                    /* artists */ null, /* excludeArtistIds */ null, /* artistIds */ null, /* albumArtistIds */ null,
                    /* contributingArtistIds */ null, /* albums */ null, /* albumIds */ null, /* ids */ null,
                    /* videoTypes */ null, /* minOfficialRating */ null, /* isLocked */ null, /* isPlaceHolder */ null,
                    /* hasOfficialRating */ null, /* collapseBoxSetItems */ null, /* minWidth */ null,
                    /* minHeight */ null, /* maxWidth */ null, /* maxHeight */ null, /* is3D */ null,
                    /* seriesStatus */ null, /* nameStartsWithOrGreater */ null, /* nameStartsWith */ null,
                    /* nameLessThan */ null, /* studioIds */ null, /* genreIds */ null,
                    /* enableTotalRecordCount */ null, /* enableImages */ null);
            if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                return result.getItems().get(0);
            }
        } catch (Exception e) {
            logger.warn("Failed to search for {}: {}", searchTerm, e.getMessage());
        }
        return null;
    }

    /**
     * Convenience overload that allows calling with (searchTerm, kind, userId)
     */
    public @Nullable BaseItemDto searchItem(String searchTerm, BaseItemKind kind, @Nullable String userId) {
        return searchItem(userId, searchTerm, kind);
    }

    /**
     * Return an item by id using the given user id where applicable.
     */
    public @Nullable BaseItemDto getItemById(@Nullable String userId, java.util.UUID itemId) {
        try {
            java.util.UUID uid = null;
            if (userId != null) {
                try {
                    uid = java.util.UUID.fromString(userId);
                } catch (Exception e) {
                    uid = null;
                }
            }
            if (uid == null) {
                // Try to pick one active user if available
                synchronized (activeUserIds) {
                    if (!activeUserIds.isEmpty()) {
                        try {
                            uid = java.util.UUID.fromString(activeUserIds.get(0));
                        } catch (Exception ex) {
                            uid = null;
                        }
                    }
                }
            }
            if (uid == null) {
                return null;
            }
            UserLibraryApi userApi = new UserLibraryApi(apiClient);
            return userApi.getItem(itemId, uid);
        } catch (Exception e) {
            logger.warn("Failed to fetch item {}: {}", itemId, e.getMessage());
            return null;
        }
    }

    /**
     * Sends a display content command to browse to a specific item on a client.
     *
     * @param sessionId The session ID of the client
     * @param itemId The item ID to browse to
     * @param itemType The item type (BaseItemKind enum value)
     * @param itemName The item name for display
     */
    public void browseToItem(@Nullable String sessionId, String itemId, BaseItemKind itemType,
            @Nullable String itemName) {
        try {
            SessionApi sessionApi = new SessionApi(apiClient);
            sessionApi.displayContent(sessionId, itemType, itemId, itemName != null ? itemName : "");
            logger.debug("Sent browse command to session {} for item {}", sessionId, itemId);
        } catch (Exception e) {
            logger.warn("Failed to send browse command to session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to browse to item", e);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ClientDiscoveryService.class);
    }

    /**
     * Called by {@link ClientDiscoveryService} when it has been initialized by the framework.
     *
     * This callback ensures that the discovery service reference is available before initializing
     * the DiscoveryTask through the TaskManager. The openHAB framework injects ThingHandlerServices
     * asynchronously after initialize() completes, so we cannot pass the service reference during
     * ServerHandler initialization.
     *
     * @param discoveryService the initialized discovery service
     */
    public void onDiscoveryServiceInitialized(ClientDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;

        // Now that the discovery service is available, create and register the DiscoveryTask
        AbstractTask discoveryTask = taskManager.createDiscoveryTask(this, discoveryService, errorEventBus);
        tasks.put(discoveryTask.getId(), discoveryTask);

        logger.debug("DiscoveryTask initialized and added to task registry");

        // If we're already in CONNECTED state, the discovery task should be started
        if (state == ServerState.CONNECTED) {
            logger.debug("Server already CONNECTED, starting DiscoveryTask");
            taskManager.processStateChange(ServerState.CONNECTED, tasks, scheduledTasks, scheduler);
        }
    }

    /**
     * Returns the current map of active Jellyfin clients.
     *
     * This method is used by the {@link ClientDiscoveryService} to discover client devices.
     *
     * @return the map of clients, where the key is the session/client ID and the value is the session info
     */
    public Map<String, SessionInfoDto> getClients() {
        return sessionManager.getSessions();
    }

    /**
     * Returns the session event bus for client handlers to subscribe to session updates.
     *
     * @return the session event bus
     */
    public SessionEventBus getSessionEventBus() {
        return sessionEventBus;
    }

    /**
     * Get the current state of the server handler
     *
     * @return The current state
     */
    public ServerState getState() {
        return state;
    }

    /**
     * Set the state of the server handler
     *
     * @param newState The new state
     */
    private synchronized void setState(ServerState newState) {
        ServerState oldState = this.state;
        this.state = newState;
        logger.debug("Server state changed: {} -> {}", oldState, newState);

        // Update running tasks based on the new state
        taskManager.processStateChange(newState, tasks, scheduledTasks, scheduler);
    }

    @Override
    public void onErrorEvent(ErrorEvent event) {
        // Strategy pattern for handling different error types and severities
        switch (event.getSeverity()) {
            case WARNING:
                // Just log, don't change state
                logger.warn("Warning in {}: {}", event.getContext(), event.getException().getMessage());
                break;

            case RECOVERABLE:
                // Set to error state but allow recovery
                logger.error("Recoverable error in {}: {}", event.getContext(), event.getException().getMessage());
                setState(ServerState.ERROR);
                break;

            case FATAL:
                // Set to error state and require restart
                logger.error("Fatal error in {}: {}", event.getContext(), event.getException().getMessage(),
                        event.getException());
                setState(ServerState.ERROR);
                break;
        }
    }

    /**
     * Determines the current state based on the available configuration
     *
     * @return The determined state
     */
    private ServerState determineState() {
        try {
            var stateAnalysis = serverStateManager.analyzeServerState(this.state, this.configuration, this.thing);

            logger.debug("State analysis: {} - {}", stateAnalysis.recommendedState(), stateAnalysis.reason());
            return stateAnalysis.recommendedState();

        } catch (Exception e) {
            logger.warn("Error during state determination: {}", e.getMessage());
            ErrorEvent event = new ErrorEvent(e, ErrorEvent.ErrorType.CONFIGURATION_ERROR,
                    ErrorEvent.ErrorSeverity.FATAL, "determineState");
            errorEventBus.publishEvent(event);
            return ServerState.ERROR;
        }
    }

    @Override
    public void initialize() {
        try {
            setState(ServerState.INITIALIZING);
            scheduler.execute(initializeHandler());
        } catch (Exception e) {
            this.logger.warn("Exception during initialization: {}", e.getMessage());
            ErrorEvent event = new ErrorEvent(e, ErrorEvent.ErrorType.UNKNOWN_ERROR, ErrorEvent.ErrorSeverity.FATAL,
                    "initialize");
            errorEventBus.publishEvent(event);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels on the server bridge require command handling.
        // This method is intentionally left blank.
    }

    @Override
    public void dispose() {
        // Clean up event bus registration
        if (errorEventBus != null) {
            errorEventBus.removeListener(this);
        }
        // Clear session manager state
        if (sessionManager != null) {
            sessionManager.clear();
        }
        // Clear session event bus
        if (sessionEventBus != null) {
            sessionEventBus.clear();
        }
        // Use injected task manager
        taskManager.stopAllTasks(scheduledTasks);
        setState(ServerState.DISPOSED);
        super.dispose();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        // Validate configuration parameters before applying
        super.handleConfigurationUpdate(configurationParameters);

        logger.debug("Configuration update received: {}", configurationParameters.keySet());

        // Check if token was updated (this is the most critical parameter)
        boolean tokenChanged = configurationParameters.containsKey("token");
        boolean connectionParametersChanged = configurationParameters.containsKey("hostname")
                || configurationParameters.containsKey("port") || configurationParameters.containsKey("ssl")
                || configurationParameters.containsKey("path");

        if (tokenChanged || connectionParametersChanged) {
            logger.info("Critical configuration parameters changed (token: {}, connection: {}), re-initializing",
                    tokenChanged, connectionParametersChanged);

            // Stop all running tasks before re-initialization
            taskManager.stopAllTasks(scheduledTasks);

            // Reload configuration from the Thing
            // Note: The configuration object is updated by the framework via reflection
            // but we need to ensure we're using the latest values
            Configuration newConfig = getConfigAs(Configuration.class);

            // Update the configuration fields (framework has already persisted the changes)
            this.configuration.hostname = newConfig.hostname;
            this.configuration.port = newConfig.port;
            this.configuration.ssl = newConfig.ssl;
            this.configuration.path = newConfig.path;
            this.configuration.token = newConfig.token;
            this.configuration.refreshSeconds = newConfig.refreshSeconds;
            this.configuration.clientActiveWithInSeconds = newConfig.clientActiveWithInSeconds;

            // Re-initialize the handler to apply the new configuration
            // This will re-authenticate with the new token and restart tasks
            initialize();
        } else {
            logger.debug("Non-critical configuration parameters changed, applying without re-initialization");

            // For non-critical parameters (like refresh intervals), just update the config object
            Configuration newConfig = getConfigAs(Configuration.class);
            this.configuration.refreshSeconds = newConfig.refreshSeconds;
            this.configuration.clientActiveWithInSeconds = newConfig.clientActiveWithInSeconds;

            // Restart tasks with new refresh interval if needed
            if (configurationParameters.containsKey("refreshSeconds")) {
                taskManager.stopAllTasks(scheduledTasks);
                taskManager.processStateChange(this.state, tasks, scheduledTasks, scheduler);
            }
        }
    }

    /**
     * Processes server initialization with a state-driven approach
     *
     * @return A runnable that handles the initialization process
     */
    private synchronized Runnable initializeHandler() {
        return () -> {
            try {
                // Determine the initial state based on configuration
                ServerState initialState = determineState();
                setState(initialState);

                URI serverUri = resolveServerUri();

                if (initialState == ServerState.DISCOVERED) {
                    updateConfiguration(serverUri);
                } else if (initialState == ServerState.INITIALIZING || initialState == ServerState.NEEDS_AUTHENTICATION
                        || initialState == ServerState.CONFIGURED) {
                    updateThingProperty(Constants.ServerProperties.SERVER_URI, serverUri.toString());
                } else if (initialState == ServerState.ERROR || initialState == ServerState.CONNECTED
                        || initialState == ServerState.DISPOSED) {
                    // These states are not applicable during initialization
                    logger.warn("Unexpected state during initialization: {}", initialState);
                }

                this.apiClient.updateBaseUri(serverUri.toString());

                if (initialState == ServerState.CONFIGURED) {
                    // Authenticate with token - tasks have already been started by setState()
                    this.apiClient.authenticateWithToken(this.configuration.token);
                } else if (initialState == ServerState.DISCOVERED || initialState == ServerState.NEEDS_AUTHENTICATION) {
                    // No token, set offline with configuration error
                    ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.OFFLINE,
                            ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.configuration.no-access-token");
                    this.getThing().setStatusInfo(statusInfo);
                }
                // No specific authentication action for other states

            } catch (Exception e) {
                this.logger.error("Error during initialization: {}", e.getMessage(), e);
                ErrorEvent event = new ErrorEvent(e, ErrorEvent.ErrorType.UNKNOWN_ERROR, ErrorEvent.ErrorSeverity.FATAL,
                        "initializeHandler");
                errorEventBus.publishEvent(event);
            }
        };
    }

    private Object handleConnection(SystemInfo systemInfo) {
        try {
            this.updateThingProperty(Constants.ServerProperties.SERVER_VERSION, systemInfo.getVersion());
            this.updateConfiguration(systemInfo);

            // Update state to connected
            setState(ServerState.CONNECTED);

            // Set thing status to online
            ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
            this.getThing().setStatusInfo(statusInfo);

        } catch (Exception e) {
            logger.warn("Failed to process system information: {}", e.getMessage(), e);
            ErrorEvent event = new ErrorEvent(e, ErrorEvent.ErrorType.API_ERROR, ErrorEvent.ErrorSeverity.RECOVERABLE,
                    "handleConnection");
            errorEventBus.publishEvent(event);
        }
        return null;
    }

    /**
     * Handles the retrieved users list from the server
     *
     * @param users The list of users retrieved from the server
     */
    private void handleUsersList(List<UserDto> users) {
        try {
            // Use UserManager to process users and detect changes
            var userChangeResult = userManager.processUsersList(users, activeUserIds);

            // Update the tracked list atomically
            synchronized (activeUserIds) {
                activeUserIds.clear();
                activeUserIds.addAll(userChangeResult.currentUserIds());
            }
            // Update the client list after updating the user list
            updateClientList();
        } catch (Exception e) {
            logger.warn("Failed to process users list: {}", e.getMessage(), e);
            ErrorEvent event = new ErrorEvent(e, ErrorEvent.ErrorType.API_ERROR, ErrorEvent.ErrorSeverity.WARNING,
                    "handleUsersList");
            errorEventBus.publishEvent(event);
        }
    }

    /**
     * Updates the Jellyfin client list based on the current active users.
     */
    private void updateClientList() {
        try {
            Map<String, SessionInfoDto> newSessions = new HashMap<>();
            ClientListUpdater.updateClients(apiClient, Set.copyOf(activeUserIds), newSessions);

            // Update session manager, which will publish events
            sessionManager.updateSessions(newSessions);

            // Trigger client discovery after updating the client list
            if (discoveryService != null) {
                discoveryService.discoverClients();
            }
        } catch (Exception e) {
            logger.warn("Failed to update client list: {}", e.getMessage(), e);
        }
    }

    /**
     * Updates configuration from a URI
     *
     * @param uri The URI containing server information
     */
    private void updateConfiguration(URI uri) {
        try {
            var extractor = new UriConfigurationExtractor();
            var configUpdate = extractor.extract(uri, this.configuration);

            if (configUpdate.hasChanges()) {
                // Update the current configuration object with new values
                var updatedConfig = configUpdate.configuration();
                this.configuration.hostname = updatedConfig.hostname;
                this.configuration.port = updatedConfig.port;
                this.configuration.ssl = updatedConfig.ssl;
                this.configuration.path = updatedConfig.path;

                logger.info("Configuration changed from URI, updating Thing configuration");

                org.openhab.core.config.core.Configuration config = editConfiguration();
                config.put("hostname", updatedConfig.hostname);
                config.put("port", updatedConfig.port);
                config.put("ssl", updatedConfig.ssl);
                config.put("path", updatedConfig.path);
                updateConfiguration(config);
            } else {
                logger.debug("No configuration changes needed from URI");
            }
        } catch (Exception e) {
            logger.warn("Failed to update configuration from URI: {}", e.getMessage(), e);
            ErrorEvent event = new ErrorEvent(e, ErrorEvent.ErrorType.CONFIGURATION_ERROR,
                    ErrorEvent.ErrorSeverity.WARNING, "updateConfiguration");
            errorEventBus.publishEvent(event);
        }
    }

    private void updateConfiguration(SystemInfo systemInfo) {
        try {
            var extractor = new SystemInfoConfigurationExtractor();
            var configUpdate = extractor.extract(systemInfo, this.configuration);

            if (configUpdate.hasChanges()) {
                // Update the current configuration object with new values
                var updatedConfig = configUpdate.configuration();
                this.configuration.serverName = updatedConfig.serverName;
                this.configuration.hostname = updatedConfig.hostname;

                logger.info("Configuration updated from SystemInfo");

                org.openhab.core.config.core.Configuration config = editConfiguration();
                config.put("serverName", updatedConfig.serverName);
                config.put("hostname", updatedConfig.hostname);
                updateConfiguration(config);
            }
        } catch (Exception e) {
            logger.debug("Failed to update configuration from SystemInfo: {}", e.getMessage());
            ErrorEvent event = new ErrorEvent(e, ErrorEvent.ErrorType.CONFIGURATION_ERROR,
                    ErrorEvent.ErrorSeverity.WARNING, "updateConfiguration");
            errorEventBus.publishEvent(event);
        }
    }

    /**
     * Helper method to update a single Thing property.
     * Creates a new properties map with the updated property and calls updateProperties.
     *
     * @param key The property key
     * @param value The property value
     */
    private void updateThingProperty(String key, String value) {
        Map<String, String> properties = new HashMap<>(thing.getProperties());
        properties.put(key, value);
        updateProperties(properties);
    }

    /**
     * Resolves the effective server URI, preferring the Thing property if present and valid, otherwise falling back to
     * configuration.
     * Throws and logs errors for invalid or unsupported URIs.
     *
     * @return the resolved server URI
     */
    private URI resolveServerUri() {
        String propertyValue = thing.getProperties().get(Constants.ServerProperties.SERVER_URI);
        if (propertyValue != null && !propertyValue.isBlank()) {
            try {
                URI candidate = URI.create(propertyValue);
                String scheme = candidate.getScheme();
                if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                    return candidate;
                } else {
                    logger.error("Thing property '{}' contains unsupported URI scheme: {}",
                            Constants.ServerProperties.SERVER_URI, scheme);
                    throw new IllegalArgumentException("Unsupported URI scheme: " + scheme);
                }
            } catch (Exception ex) {
                logger.error("Thing property '{}' contains invalid URI: {}", Constants.ServerProperties.SERVER_URI,
                        propertyValue, ex);
                throw ex;
            }
        }
        try {
            return this.configuration.getServerURI();
        } catch (java.net.URISyntaxException ex) {
            logger.error("Configuration contains invalid server URI", ex);
            throw new IllegalStateException("Configuration contains invalid server URI", ex);
        }
    }
}

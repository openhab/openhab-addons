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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.events.ErrorEvent;
import org.openhab.binding.jellyfin.internal.events.ErrorEventBus;
import org.openhab.binding.jellyfin.internal.events.ErrorEventListener;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.types.ServerState;
import org.openhab.binding.jellyfin.internal.util.client.ClientListUpdater;
import org.openhab.binding.jellyfin.internal.util.config.ConfigurationManager;
import org.openhab.binding.jellyfin.internal.util.config.SystemInfoConfigurationExtractor;
import org.openhab.binding.jellyfin.internal.util.config.UriConfigurationExtractor;
import org.openhab.binding.jellyfin.internal.util.state.ServerStateManager;
import org.openhab.binding.jellyfin.internal.util.user.UserManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
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
    private final ApiClient apiClient;
    private final Configuration configuration;
    private final TaskManagerInterface taskManager;

    // Utility classes for better separation of concerns
    private final UserManager userManager;
    private final ConfigurationManager configurationManager;
    private final ServerStateManager serverStateManager;

    private ServerState state = ServerState.INITIALIZING;

    private final Map<String, AbstractTask> tasks = new HashMap<>();
    private final Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private final List<String> activeUserIds = new ArrayList<>();

    // Maintains the list of Jellyfin clients (key: session/client id, value: session info)
    private final Map<String, org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto> clients = new HashMap<>();

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
        this.configurationManager = new ConfigurationManager();
        this.serverStateManager = new ServerStateManager();

        // Create event bus and register as listener
        this.errorEventBus = new ErrorEventBus();
        this.errorEventBus.addListener(this);

        // Initialize tasks through the task manager
        this.tasks.putAll(taskManager.initializeTasks(apiClient, errorEventBus,
                systemInfo -> this.handleConnection(systemInfo), users -> this.handleUsersList(users)));
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

            if (stateAnalysis.recommendedState() == ServerState.CONFIGURED && stateAnalysis.serverUri() != null) {
                // Authenticate with token when moving to CONFIGURED state
                this.apiClient.authenticateWithToken(this.configuration.token);
            }

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
        // Use injected task manager
        taskManager.stopAllTasks(scheduledTasks);
        setState(ServerState.DISPOSED);
        super.dispose();
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
                    // Has token, authenticate (tasks will be started by setState)
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
            ClientListUpdater.updateClients(apiClient, Set.copyOf(activeUserIds), clients);
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
            var configUpdate = configurationManager.analyze(new UriConfigurationExtractor(), uri, this.configuration);

            if (configUpdate.hasChanges()) {
                // Apply changes to the current configuration object
                configUpdate.applyTo(this.configuration);

                logger.info("Configuration changed, updating Thing configuration");

                org.openhab.core.config.core.Configuration config = editConfiguration();
                config.put("hostname", configUpdate.hostname());
                config.put("port", configUpdate.port());
                config.put("ssl", configUpdate.ssl());
                config.put("path", configUpdate.path());
                updateConfiguration(config);
            } else {
                logger.debug("No configuration changes needed");
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
            var configUpdate = configurationManager.analyze(new SystemInfoConfigurationExtractor(), systemInfo,
                    this.configuration);

            if (configUpdate.hasChanges()) {
                // Apply changes to the current configuration object
                configUpdate.applyTo(this.configuration);

                logger.info("Configuration updated from SystemInfo");

                org.openhab.core.config.core.Configuration config = editConfiguration();
                config.put("hostname", configUpdate.hostname());
                config.put("port", configUpdate.port());
                config.put("ssl", configUpdate.ssl());
                config.put("path", configUpdate.path());
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

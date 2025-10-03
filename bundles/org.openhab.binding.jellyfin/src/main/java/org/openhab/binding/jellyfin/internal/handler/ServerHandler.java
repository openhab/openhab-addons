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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.exceptions.ExceptionHandler;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.RegistrationTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.TaskFactory;
import org.openhab.binding.jellyfin.internal.handler.tasks.UpdateTask;
import org.openhab.binding.jellyfin.internal.types.ServerState;
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
public class ServerHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final ExceptionHandler exceptionHandler;
    private final ApiClient apiClient;
    private final Configuration configuration;

    private ServerState state = ServerState.INITIALIZING;

    private final Map<String, AbstractTask> tasks = new HashMap<>();
    private final Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public ServerHandler(Bridge bridge, ApiClient apiClient) {
        super(bridge);

        this.exceptionHandler = new ExceptionHandler();
        this.configuration = this.getConfigAs(Configuration.class);
        this.apiClient = apiClient;

        // Create all tasks in the constructor
        this.tasks.put(ConnectionTask.TASK_ID, TaskFactory.createConnectionTask(this.apiClient,
                systemInfo -> this.handleConnection(systemInfo), this.exceptionHandler));
        this.tasks.put(RegistrationTask.TASK_ID,
                TaskFactory.createRegistrationTask(this.apiClient, this.exceptionHandler));
        this.tasks.put(UpdateTask.TASK_ID, TaskFactory.createUpdateTask(this.apiClient, this.exceptionHandler));

        // Additional tasks can be added here in the future
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
     * Start tasks for the specified server state, stopping any tasks that shouldn't run in that state.
     * 
     * @param serverState The server state to start tasks for
     */
    private synchronized void startTasksForState(ServerState serverState) {
        TaskManager.transitionTasksForState(serverState, tasks, scheduledTasks, scheduler);
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
        startTasksForState(newState);
    }

    /**
     * Determines the current state based on the available configuration
     * 
     * @return The determined state
     */
    private ServerState determineState() {
        // If the current state is DISPOSED, return it immediately
        if (this.state == ServerState.DISPOSED) {
            return ServerState.DISPOSED;
        }

        // Check if we have a discovered server
        boolean isDiscovered = thing.getProperties().containsKey(Constants.ServerProperties.SERVER_URI);

        // Check if we have authentication token
        boolean hasToken = (this.configuration.token != null && !this.configuration.token.isEmpty());

        try {
            URI serverURI = this.configuration.getServerURI();
            if (isDiscovered && !hasToken) {
                return ServerState.DISCOVERED;
            } else if (serverURI != null && !hasToken) {
                return ServerState.NEEDS_AUTHENTICATION;
            } else if (serverURI != null && hasToken) {
                return ServerState.CONFIGURED;
            }
        } catch (URISyntaxException e) {
            logger.warn("Invalid server URI configuration: {}", e.getMessage());
            this.exceptionHandler.handle(e);
            return ServerState.ERROR;
        }

        return ServerState.INITIALIZING;
    }

    @Override
    public void initialize() {
        try {
            setState(ServerState.INITIALIZING);
            scheduler.execute(initializeHandler());
        } catch (Exception e) {
            this.logger.warn("Exception during initialization: {}", e.getMessage());
            this.exceptionHandler.handle(e);
            setState(ServerState.ERROR);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels on the server bridge require command handling.
        // This method is intentionally left blank.
    }

    @Override
    public void dispose() {
        // Set state to indicate disposal
        setState(ServerState.DISPOSED);
        // No additional cleanup required
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

                URI serverUri = this.configuration.getServerURI();

                // Step 1: Handle server URI based on state
                switch (initialState) {
                    case DISCOVERED:
                        // Initialize discovered server - get URI from properties
                        serverUri = new URI(thing.getProperties().get(Constants.ServerProperties.SERVER_URI));
                        updateConfiguration(serverUri);
                        break;
                    case INITIALIZING:
                    case NEEDS_AUTHENTICATION:
                    case CONFIGURED:
                        // Add the server URI to the properties for non-discovery results
                        updateThingProperty(Constants.ServerProperties.SERVER_URI, serverUri.toString());
                        break;
                    case ERROR:
                    case CONNECTED:
                    case DISPOSED:
                        // These states are not applicable during initialization
                        logger.warn("Unexpected state during initialization: {}", initialState);
                        break;
                }

                // Step 2: Update API client with server URI for all states
                this.apiClient.updateBaseUri(serverUri.toString());

                // Step 3: Handle authentication based on state
                switch (initialState) {
                    case CONFIGURED:
                        // Has token, authenticate (tasks will be started by setState)
                        this.apiClient.authenticateWithToken(this.configuration.token);
                        break;
                    case DISCOVERED:
                    case NEEDS_AUTHENTICATION:
                        // No token, set offline with configuration error
                        ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.OFFLINE,
                                ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.configuration.no-access-token");
                        this.getThing().setStatusInfo(statusInfo);
                        break;
                    case INITIALIZING:
                    case ERROR:
                    case CONNECTED:
                    case DISPOSED:
                        // No specific authentication action for these states
                        break;
                }

            } catch (Exception e) {
                this.logger.error("Error during initialization: {}", e.getMessage(), e);
                this.exceptionHandler.handle(e);
                setState(ServerState.ERROR);
            }
        };
    }

    private Object handleConnection(SystemInfo systemInfo) {
        try {
            // Log all available server information at INFO level
            logger.info("Jellyfin Server Information:");
            logger.info("  Server Name: {}", systemInfo.getServerName());
            logger.info("  Local Address: {}", systemInfo.getLocalAddress());
            logger.info("  Version: {}", systemInfo.getVersion());

            // Update properties with server version
            updateThingProperty(Constants.ServerProperties.SERVER_VERSION, systemInfo.getVersion());

            logger.info("  Product Name: {}", systemInfo.getProductName());
            logger.info("  Server ID: {}", systemInfo.getId());
            logger.info("  Startup Wizard Completed: {}", systemInfo.getStartupWizardCompleted());
            logger.info("  Web Socket Port: {}", systemInfo.getWebSocketPortNumber());

            // Update configuration with systemInfo data if available
            this.updateConfiguration(systemInfo);

            // Update state to connected
            setState(ServerState.CONNECTED);

            // Set thing status to online
            ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
            this.getThing().setStatusInfo(statusInfo);

            this.stopTasks();
        } catch (Exception e) {
            logger.warn("Failed to process system information: {}", e.getMessage(), e);
            setState(ServerState.ERROR);
        }
        return null;
    }

    private synchronized void stopTasks() {
        TaskManager.stopAllTasks(scheduledTasks);
    }

    /**
     * Updates configuration from a URI
     * 
     * @param uri The URI containing server information
     */
    private void updateConfiguration(URI uri) {
        // Track if any config value has changed
        boolean configChanged = false;

        // Only update values if they differ from current configuration
        if (uri.getHost() != null && !uri.getHost().equals(this.configuration.hostname)) {
            this.configuration.hostname = uri.getHost();
            configChanged = true;
        }

        if (uri.getPort() > 0 && uri.getPort() != this.configuration.port) {
            this.configuration.port = uri.getPort();
            configChanged = true;
        }

        if (uri.getScheme() != null) {
            boolean newSslValue = "https".equalsIgnoreCase(uri.getScheme());
            if (newSslValue != this.configuration.ssl) {
                this.configuration.ssl = newSslValue;
                configChanged = true;
            }
        }

        if (uri.getPath() != null && !uri.getPath().isEmpty() && !uri.getPath().equals(this.configuration.path)) {
            this.configuration.path = uri.getPath();
            configChanged = true;
        }

        // Only save if something has changed
        if (configChanged) {
            logger.info("Configuration changed, updating Thing configuration");

            org.openhab.core.config.core.Configuration config = editConfiguration();

            config.put("hostname", this.configuration.hostname);
            config.put("port", this.configuration.port);
            config.put("ssl", this.configuration.ssl);
            config.put("path", this.configuration.path);

            updateConfiguration(config);
        } else {
            logger.debug("No configuration changes needed");
        }
    }

    private void updateConfiguration(SystemInfo systemInfo) {
        var localAddress = systemInfo.getLocalAddress();

        if (localAddress != null && !localAddress.isEmpty()) {
            try {
                updateConfiguration(new URI(localAddress));
            } catch (Exception e) {
                logger.debug("Failed to parse local address URI: {}", e.getMessage());
                // Don't use exception handler for debug-level issues
            }
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
}

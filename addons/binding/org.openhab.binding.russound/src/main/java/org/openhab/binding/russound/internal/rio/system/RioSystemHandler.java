/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.system;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.russound.internal.discovery.RioSystemDeviceDiscoveryService;
import org.openhab.binding.russound.internal.net.SocketChannelSession;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rio.AbstractBridgeHandler;
import org.openhab.binding.russound.internal.rio.AbstractRioHandlerCallback;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.RioHandlerCallbackListener;
import org.openhab.binding.russound.internal.rio.RioPresetsProtocol;
import org.openhab.binding.russound.internal.rio.RioSystemFavoritesProtocol;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.controller.RioControllerHandler;
import org.openhab.binding.russound.internal.rio.models.GsonUtilities;
import org.openhab.binding.russound.internal.rio.source.RioSourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The bridge handler for a Russound System. This is the entry point into the whole russound system and is generally
 * points to the main controller. This implementation must be attached to a {@link RioSystemHandler} bridge.
 *
 * @author Tim Roberts
 */
public class RioSystemHandler extends AbstractBridgeHandler<RioSystemProtocol> {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(RioSystemHandler.class);

    /**
     * The configuration for the system - will be recreated when the configuration changes and will be null when not
     * online
     */
    private RioSystemConfig config;

    /**
     * The lock used to control access to {@link #config}
     */
    private final ReentrantLock configLock = new ReentrantLock();

    /**
     * The {@link SocketSession} telnet session to the switch. Will be null if not connected.
     */
    private SocketSession session;

    /**
     * The lock used to control access to {@link #session}
     */
    private final ReentrantLock sessionLock = new ReentrantLock();

    /**
     * The retry connection event - will only be created when we are retrying the connection attempt
     */
    private ScheduledFuture<?> retryConnection;

    /**
     * The lock used to control access to {@link #retryConnection}
     */
    private final ReentrantLock retryConnectionLock = new ReentrantLock();

    /**
     * The ping event - will be non-null when online (null otherwise)
     */
    private ScheduledFuture<?> ping;

    /**
     * The lock used to control access to {@link #ping}
     */
    private final ReentrantLock pingLock = new ReentrantLock();

    /**
     * {@link Gson} used for JSON serialization/deserialization
     */
    private final Gson gson = GsonUtilities.createGson();

    /**
     * Callback listener to use when source name changes - will call {@link #refreshNamedHandler(Gson, Class, String)}
     * to
     * refresh the {@link RioConstants#CHANNEL_SYSSOURCES} channel
     */
    private final RioHandlerCallbackListener handlerCallbackListener = new RioHandlerCallbackListener() {
        @Override
        public void stateUpdate(String channelId, State state) {
            refreshNamedHandler(gson, RioSourceHandler.class, RioConstants.CHANNEL_SYSSOURCES);
        }
    };

    /**
     * The protocol for favorites handling
     */
    private final AtomicReference<RioSystemFavoritesProtocol> favoritesProtocol = new AtomicReference<RioSystemFavoritesProtocol>(
            null);

    /**
     * The protocol for presets handling
     */
    private final AtomicReference<RioPresetsProtocol> presetsProtocol = new AtomicReference<RioPresetsProtocol>(null);

    /**
     * The discovery service to discover the zones/sources, etc
     * Will be null if not active.
     */
    private final AtomicReference<RioSystemDeviceDiscoveryService> discoveryService = new AtomicReference<RioSystemDeviceDiscoveryService>(
            null);

    /**
     * Constructs the handler from the {@link Bridge}
     *
     * @param bridge a non-null {@link Bridge} the handler is for
     */
    public RioSystemHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Overrides the base method since we are the source of the {@link SocketSession}.
     *
     * @return the {@link SocketSession} once initialized. Null if not initialized or disposed of
     */
    @Override
    public SocketSession getSocketSession() {
        sessionLock.lock();
        try {
            return session;
        } finally {
            sessionLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link RioSystemProtocol}. Basically we validate the type of command for the channel then call the
     * {@link RioSystemProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link RioSystemProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        String id = channelUID.getId();

        if (id == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(RioConstants.CHANNEL_SYSLANG)) {
            if (command instanceof StringType) {
                getProtocolHandler().setSystemLanguage(((StringType) command).toString());
            } else {
                logger.debug("Received a SYSTEM LANGUAGE channel command with a non StringType: {}", command);
            }
        } else if (id.equals(RioConstants.CHANNEL_SYSALLON)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setSystemAllOn(command == OnOffType.ON);
            } else {
                logger.debug("Received a SYSTEM ALL ON channel command with a non OnOffType: {}", command);
            }
        } else {
            logger.debug("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link RioSystemProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (getProtocolHandler() == null) {
            return;
        }

        // Remove the cache'd value to force a refreshed value
        ((StatefulHandlerCallback) getProtocolHandler().getCallback()).removeState(id);

        if (id.equals(RioConstants.CHANNEL_SYSLANG)) {
            getProtocolHandler().refreshSystemLanguage();

        } else if (id.equals(RioConstants.CHANNEL_SYSSTATUS)) {
            getProtocolHandler().refreshSystemStatus();

        } else if (id.equals(RioConstants.CHANNEL_SYSALLON)) {
            getProtocolHandler().refreshSystemAllOn();

        } else if (id.equals(RioConstants.CHANNEL_SYSCONTROLLERS)) {
            refreshNamedHandler(gson, RioControllerHandler.class, RioConstants.CHANNEL_SYSCONTROLLERS);
        } else if (id.equals(RioConstants.CHANNEL_SYSSOURCES)) {
            refreshNamedHandler(gson, RioSourceHandler.class, RioConstants.CHANNEL_SYSSOURCES);

        } else {
            // Can't refresh any others...
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, then will create the
     * {@link SocketSession} and will attempt to connect via {@link #connect()}.
     */
    @Override
    public void initialize() {
        final RioSystemConfig rioConfig = getRioConfig();

        if (rioConfig == null) {
            return;
        }

        if (rioConfig.getIpAddress() == null || rioConfig.getIpAddress().trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP Address of Russound is missing from configuration");
            return;
        }

        sessionLock.lock();
        try {
            session = new SocketChannelSession(rioConfig.getIpAddress(), RioConstants.RioPort);
        } finally {
            sessionLock.unlock();
        }

        // Try initial connection in a scheduled task
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }

        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Attempts to connect to the system. If successfully connect, the {@link RioSystemProtocol#login()} will be
     * called to log into the system (if needed). Once completed, a ping job will be created to keep the connection
     * alive. If a connection cannot be established (or login failed), the connection attempt will be retried later (via
     * {@link #retryConnect()})
     */
    private void connect() {
        String response = "Server is offline - will try to reconnect later";

        sessionLock.lock();
        pingLock.lock();
        try {
            session.connect();

            final StatefulHandlerCallback callback = new StatefulHandlerCallback(new AbstractRioHandlerCallback() {
                @Override
                public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                    updateStatus(status, detail, msg);
                    if (status != ThingStatus.ONLINE) {
                        disconnect();
                        reconnect();
                    }
                }

                @Override
                public void stateChanged(String channelId, State state) {
                    updateState(channelId, state);
                    fireStateUpdated(channelId, state);
                }

                @Override
                public void setProperty(String propertyName, String propertyValue) {
                    getThing().setProperty(propertyName, propertyValue);
                }

            });

            setProtocolHandler(new RioSystemProtocol(session, callback));
            favoritesProtocol.set(new RioSystemFavoritesProtocol(session, callback));
            presetsProtocol.set(new RioPresetsProtocol(session, callback));

            response = getProtocolHandler().login();
            if (response == null) {
                final RioSystemConfig rioConfig = getRioConfig();
                if (rioConfig != null) {
                    ping = this.scheduler.scheduleWithFixedDelay(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final ThingStatus status = getThing().getStatus();
                                if (status == ThingStatus.ONLINE) {
                                    if (session.isConnected()) {
                                        getProtocolHandler().ping();
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Exception while pinging: {}", e.getMessage(), e);
                            }
                        }
                    }, rioConfig.getPing(), rioConfig.getPing(), TimeUnit.SECONDS);

                    logger.debug("Going online!");
                    updateStatus(ThingStatus.ONLINE);
                    startScan(rioConfig);
                    refreshNamedHandler(gson, RioSourceHandler.class, RioConstants.CHANNEL_SYSSOURCES);
                    refreshNamedHandler(gson, RioControllerHandler.class, RioConstants.CHANNEL_SYSCONTROLLERS);

                    return;
                } else {
                    logger.debug("getRioConfig returned a null!");
                }
            } else {
                logger.warn("Login return {}", response);
            }

        } catch (Exception e) {
            logger.error("Error connecting: {}", e.getMessage(), e);
            // do nothing
        } finally {
            pingLock.unlock();
            sessionLock.unlock();
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
        reconnect();
    }

    /**
     * Retries the connection attempt - schedules a job in {@link RioSystemConfig#getRetryPolling()} seconds to
     * call the {@link #connect()} method. If a retry attempt is pending, the request is ignored.
     */
    @Override
    protected void reconnect() {
        retryConnectionLock.lock();
        try {
            if (retryConnection == null) {
                final RioSystemConfig rioConfig = getRioConfig();
                if (rioConfig != null) {

                    logger.info("Will try to reconnect in {} seconds", rioConfig.getRetryPolling());
                    retryConnection = this.scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            retryConnection = null;
                            try {
                                if (getThing().getStatus() != ThingStatus.ONLINE) {
                                    connect();
                                }
                            } catch (Exception e) {
                                logger.error("Exception connecting: {}", e.getMessage(), e);
                            }
                        }

                    }, rioConfig.getRetryPolling(), TimeUnit.SECONDS);
                }
            } else {
                logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
            }
        } finally {
            retryConnectionLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Attempts to disconnect from the session. The protocol handler will be set to null, the {@link #ping} will be
     * cancelled/set to null and the {@link #session} will be disconnected
     */
    @Override
    protected void disconnect() {
        // Cancel ping
        pingLock.lock();
        try {
            if (ping != null) {
                ping.cancel(true);
                ping = null;
            }
        } finally {
            pingLock.unlock();
        }

        if (getProtocolHandler() != null) {
            getProtocolHandler().watchSystem(false);
            setProtocolHandler(null);
        }

        sessionLock.lock();
        try {
            session.disconnect();
        } catch (IOException e) {
            // ignore - we don't care
        } finally {
            sessionLock.unlock();
        }
    }

    /**
     * Simple gets the {@link RioSystemConfig} from the {@link Thing} and will set the status to offline if not
     * found.
     *
     * @return a possible null {@link RioSystemConfig}
     */
    public RioSystemConfig getRioConfig() {
        configLock.lock();
        try {
            final RioSystemConfig sysConfig = getThing().getConfiguration().as(RioSystemConfig.class);

            if (sysConfig == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            } else {
                config = sysConfig;
            }
            return config;
        } finally {
            configLock.unlock();
        }
    }

    /**
     * Registers the {@link RioSystemDeviceDiscoveryService} with this handler. The discovery service will be called in
     * {@link #startScan(RioSystemConfig)} when a device should be scanned and 'things' discovered from it
     *
     * @param service a possibly null {@link RioSystemDeviceDiscoveryService}
     */
    public void registerDiscoveryService(RioSystemDeviceDiscoveryService service) {
        discoveryService.set(service);
    }

    /**
     * Helper method to possibly start a scan. A scan will ONLY be started if the {@link RioSystemConfig#isScanDevice()}
     * is true and a discovery service has been set ({@link #registerDiscoveryService(RioSystemDeviceDiscoveryService)})
     *
     * @param sysConfig a non-null {@link RioSystemConfig}
     */
    private void startScan(RioSystemConfig sysConfig) {
        final RioSystemDeviceDiscoveryService service = discoveryService.get();
        if (service != null) {
            if (sysConfig != null && sysConfig.isScanDevice()) {
                this.scheduler.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Starting device discovery");
                        service.scanDevice();
                    }
                });
            }
        }
    }

    /**
     * Overrides the base to call {@link #childChanged(ThingHandler)} to recreate the sources/controllers names
     */
    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        childChanged(childHandler, true);
    }

    /**
     * Overrides the base to call {@link #childChanged(ThingHandler)} to recreate the sources/controllers names
     */
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        childChanged(childHandler, false);
    }

    /**
     * Helper method to recreate the {@link RioConstants#CHANNEL_SYSSOURCES} &&
     * {@link RioConstants#CHANNEL_SYSCONTROLLERS} channels
     *
     * @param childHandler a non-null child handler that changed
     * @param added true if added, false otherwise
     * @throw IllegalArgumentException if childHandler is null
     */
    private void childChanged(ThingHandler childHandler, boolean added) {
        if (childHandler == null) {
            throw new IllegalArgumentException("childHandler cannot be null");
        }
        if (childHandler instanceof RioSourceHandler) {
            final RioHandlerCallback callback = ((RioSourceHandler) childHandler).getCallback();
            if (callback != null) {
                if (added) {
                    callback.addListener(RioConstants.CHANNEL_SOURCENAME, handlerCallbackListener);
                } else {
                    callback.removeListener(RioConstants.CHANNEL_SOURCENAME, handlerCallbackListener);
                }
            }
            refreshNamedHandler(gson, RioSourceHandler.class, RioConstants.CHANNEL_SYSSOURCES);
        } else if (childHandler instanceof RioControllerHandler) {
            refreshNamedHandler(gson, RioControllerHandler.class, RioConstants.CHANNEL_SYSCONTROLLERS);
        }

    }

    /**
     * Returns the {@link RioSystemFavoritesProtocol} for the system
     *
     * @return a possibly null {@link RioSystemFavoritesProtocol}
     */
    @Override
    public RioSystemFavoritesProtocol getSystemFavoritesHandler() {
        return favoritesProtocol.get();
    }

    /**
     * Returns the {@link RioPresetsProtocol} for the system
     *
     * @return a possibly null {@link RioPresetsProtocol}
     */
    @Override
    public RioPresetsProtocol getPresetsProtocol() {
        return presetsProtocol.get();
    }
}

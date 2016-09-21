/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rio.system;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.rio.AbstractBridgeHandler;
import org.openhab.binding.russound.rio.RioConstants;
import org.openhab.binding.russound.rio.RioHandlerCallback;
import org.openhab.binding.russound.rio.StatefulHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for a Russound System. This is the entry point into the whole russound system and is generally
 * points to the main controller. This implementation must be attached to a {@link RioSystemHandler} bridge.
 *
 * @author Tim Roberts
 * @version $Id: $Id
 */
public class RioSystemHandler extends AbstractBridgeHandler<RioSystemProtocol> {
    // Logger
    private Logger logger = LoggerFactory.getLogger(RioSystemHandler.class);

    /**
     * The configuration for the system - will be recreated when the configuration changes and will be null when not
     * online
     */
    private RioSystemConfig _config;

    /**
     * The {@link SocketSession} telnet session to the switch. Will be null if not connected.
     */
    private SocketSession _session;

    /**
     * The retry connection event - will only be created when we are retrying the connection attempt
     */
    private ScheduledFuture<?> _retryConnection;

    /**
     * The ping event - will be non-null when online (null otherwise)
     */
    private ScheduledFuture<?> _ping;

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
        return _session;
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
            logger.warn("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(RioConstants.CHANNEL_SYSLANG)) {
            if (command instanceof StringType) {
                getProtocolHandler().setSystemLanguage(((StringType) command).toString());
            } else {
                logger.error("Received a SYSTEM LANGUAGE channel command with a non StringType: " + command);
            }
        } else if (id.equals(RioConstants.CHANNEL_SYSALLON)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setSystemAllOn(command == OnOffType.ON);
            } else {
                logger.error("Received a SYSTEM ALL ON channel command with a non OnOffType: " + command);
            }
        } else {
            logger.error("Unknown/Unsupported Channel id: " + id);
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

        } else if (id.equals(RioConstants.CHANNEL_SYSVERSION)) {
            getProtocolHandler().refreshVersion();
        } else {
            // Can't refresh any others...
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, then will create the
     * {@link SocketSession}, initialize the {@link RioSystemProtocol} and will attempt to connect to the (via
     * {{@link #retryConnect()}.
     */
    @Override
    public void initialize() {
        final RioSystemConfig config = getRioConfig();

        if (config == null) {
            return;
        }

        if (config.getIpAddress() == null || config.getIpAddress().trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP Address of Russound is missing from configuration");
            return;
        }

        _session = new SocketSession(config.getIpAddress(), 9621);
        setProtocolHandler(new RioSystemProtocol(_session, new StatefulHandlerCallback(new RioHandlerCallback() {
            @Override
            public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                updateStatus(status, detail, msg);
                if (status != ThingStatus.ONLINE) {
                    disconnect(true);
                }
            }

            @Override
            public void stateChanged(String channelId, State state) {
                updateState(channelId, state);
            }

        })));

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
        try {
            _session.connect();

            response = getProtocolHandler().login();
            if (response == null) {
                final RioSystemConfig config = getRioConfig();
                if (config != null) {
                    _ping = this.scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final ThingStatus status = getThing().getStatus();
                                if (status == ThingStatus.ONLINE) {
                                    if (_session.isConnected()) {
                                        getProtocolHandler().ping();
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Exception while pinging: " + e);
                            }
                        }
                    }, config.getPing(), config.getPing(), TimeUnit.SECONDS);

                    logger.info("Going online");
                    updateStatus(ThingStatus.ONLINE);
                    return;
                } else {
                    logger.error("getRioConfig returned a null");
                }
            } else {
                logger.error("Login return " + response);
            }

        } catch (Exception e) {
            logger.error("Error connecting: " + e);
            e.printStackTrace();
            // do nothing
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
        retryConnect();
    }

    /**
     * Attempts to disconnect from the session and will optionally retry the connection attempt. The {@link #_ping} will
     * be cancelled and set to null. The {@link #_session} will be disconnected and a retry attempt will be created if
     * not being disposed of (retryConnection==true).
     *
     * @param retryConnection true to retry connection attempts after the disconnect
     */
    private void disconnect(boolean retryConnection) {
        // Cancel ping
        if (_ping != null) {
            _ping.cancel(true);
            _ping = null;
        }

        if (getProtocolHandler() != null) {
            getProtocolHandler().watchSystem(false);
            if (!retryConnection) {
                setProtocolHandler(null);
            }
        }
        try {
            _session.disconnect();
        } catch (IOException e) {
            // ignore - we don't care
        }

        if (retryConnection) {
            retryConnect();
        }
    }

    /**
     * Retries the connection attempt - schedules a job in {@link RioSystemConfig#getRetryPolling()} seconds to
     * call the {@link #connect()} method. If a retry attempt is pending, the request is ignored.
     */
    private void retryConnect() {
        if (_retryConnection == null) {
            final RioSystemConfig config = getRioConfig();
            if (config != null) {

                logger.info("Will try to reconnect in {} seconds", config.getRetryPolling());
                _retryConnection = this.scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        _retryConnection = null;
                        try {
                            connect();
                        } catch (Exception e) {
                            logger.error("Exception connecting");
                            e.printStackTrace();
                        }
                    }

                }, config.getRetryPolling(), TimeUnit.SECONDS);
            }
        } else {
            logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
        }
    }

    /**
     * Simple gets the {@link RioSystemConfig} from the {@link Thing} and will set the status to offline if not
     * found.
     *
     * @return a possible null {@link RioSystemConfig}
     */
    private RioSystemConfig getRioConfig() {
        if (_config == null) {
            final RioSystemConfig config = getThing().getConfiguration().as(RioSystemConfig.class);

            if (config == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            } else {
                _config = config;
            }
        }

        return _config;
    }

    /**
     * {@inheritDoc}
     *
     * Disposes of the handler. Will simply call {@link #disconnect(boolean)} to disconnect and NOT retry the
     * connection
     */
    @Override
    public void dispose() {
        disconnect(false);
    }
}

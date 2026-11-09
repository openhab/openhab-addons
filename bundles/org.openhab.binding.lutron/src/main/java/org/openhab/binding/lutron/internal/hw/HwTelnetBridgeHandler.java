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
package org.openhab.binding.lutron.internal.hw;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.hw.HwTelnetConnection.HwLoginException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a legacy HomeWorks processor reached through its Ethernet (telnet) port.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class HwTelnetBridgeHandler extends HwBridgeHandler implements HwTelnetConnection.Listener {
    private static final int HEARTBEAT_INTERVAL_SECONDS = 60;
    private static final long DEFAULT_IDLE_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(150);

    private final Logger logger = LoggerFactory.getLogger(HwTelnetBridgeHandler.class);

    private HwTelnetBridgeConfig config = new HwTelnetBridgeConfig();

    private volatile @Nullable HwTelnetConnection connection;
    private volatile @Nullable HwTelnetConnection connecting;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> heartbeatJob;
    private volatile boolean disposed;

    // Not final so that tests can shorten it
    volatile long idleTimeoutMs = DEFAULT_IDLE_TIMEOUT_MS;

    public HwTelnetBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        config = getConfigAs(HwTelnetBridgeConfig.class);

        if (config.ipAddress.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "IP address not specified");
            return;
        }
        if (config.port < 1 || config.port > 65535) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Invalid port " + config.port);
            return;
        }

        logger.debug("Initializing HomeWorks telnet bridge {} for {}:{}, user {}, reconnect interval {} min",
                getThing().getUID(), config.ipAddress, config.port, config.user, config.reconnect);

        disposed = false;
        updateStatus(ThingStatus.UNKNOWN, "Connecting");
        scheduler.execute(this::connect);
    }

    private void connect() {
        if (disposed) {
            return;
        }
        closeConnection(false);

        HwTelnetConnection newConnection = new HwTelnetConnection(config.ipAddress, config.port, config.user,
                config.password, this, "OH-binding-" + getThing().getUID() + "-HwTelnetReader");
        // Not under a lock: open() waits for the login, and dispose() must not wait for it.
        connecting = newConnection;
        try {
            newConnection.open();
        } catch (HwLoginException e) {
            if (!disposed) {
                // Retrying with the same credentials cannot succeed, and repeated failures may lock the login.
                logger.info("Login to HomeWorks processor at {}:{} failed: {}. Check the user and password.",
                        config.ipAddress, config.port, e.getMessage());
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return;
        } catch (IOException e) {
            if (!disposed) {
                logger.debug("Could not connect to HomeWorks processor at {}:{}: {}", config.ipAddress, config.port,
                        e.getMessage());
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, String.valueOf(e.getMessage()));
                scheduleReconnect();
            }
            return;
        } finally {
            connecting = null;
        }

        synchronized (this) {
            if (disposed) {
                newConnection.close();
                return;
            }
            connection = newConnection;
        }
        logger.info("Connected to HomeWorks processor at {}:{}", config.ipAddress, config.port);
        // Before going ONLINE, so that the dimmers' level queries sent when the bridge comes online follow them.
        sendStartupCommands();
        if (newConnection.equals(connection)) {
            updateStatus(ThingStatus.ONLINE);
            startHeartbeat();
        }
    }

    // Under the lock, with the disposed check, so that dispose() cannot be followed by a newly scheduled task
    private synchronized void startHeartbeat() {
        if (disposed) {
            return;
        }
        cancelHeartbeat();
        heartbeatJob = scheduler.scheduleWithFixedDelay(this::heartbeat, HEARTBEAT_INTERVAL_SECONDS,
                HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private synchronized void cancelHeartbeat() {
        ScheduledFuture<?> job = heartbeatJob;
        if (job != null) {
            job.cancel(false);
            heartbeatJob = null;
        }
    }

    /**
     * A connection that is lost without being closed, for example when the network drops, produces no error until
     * the operating system gives up on it, which can take hours. So the processor is asked for a reply regularly and
     * the connection is treated as lost when nothing at all has been received for too long.
     */
    void heartbeat() {
        HwTelnetConnection current = connection;
        if (current == null || disposed) {
            return;
        }
        long idle = current.millisSinceLastReceived();
        if (idle > idleTimeoutMs) {
            // Initial silence can mean a lost connection as well as suppressed replies. Always bound it, even
            // when no startup command was answered; monitoring events do not prove that replies are enabled.
            connectionFailed(current, "No response from processor for " + TimeUnit.MILLISECONDS.toSeconds(idle)
                    + " s. Check the network connection and that Suppress Replies is not selected for the login.");
            return;
        }
        // DLMON is answered with a confirmation message when the login's Suppress Replies option is disabled.
        sendCommand("DLMON");
    }

    private synchronized void scheduleReconnect() {
        if (disposed) {
            return;
        }
        cancelReconnect();
        int minutes = config.reconnect > 0 ? config.reconnect : 5;
        logger.debug("Scheduling reconnect to {}:{} in {} minutes", config.ipAddress, config.port, minutes);
        reconnectJob = scheduler.schedule(this::connect, minutes, TimeUnit.MINUTES);
    }

    private synchronized void cancelReconnect() {
        ScheduledFuture<?> job = reconnectJob;
        if (job != null) {
            job.cancel(false);
            reconnectJob = null;
        }
    }

    private void closeConnection(boolean sendQuit) {
        HwTelnetConnection current;
        synchronized (this) {
            current = connection;
            connection = null;
        }
        if (current != null) {
            if (sendQuit) {
                try {
                    current.send("QUIT");
                } catch (IOException e) {
                    logger.debug("Could not send QUIT before closing: {}", e.getMessage());
                }
            }
            current.close();
        }
    }

    @Override
    public void sendCommand(String command) {
        HwTelnetConnection current = connection;
        if (current == null) {
            logger.debug("Not connected to HomeWorks processor, dropping command: {}", command);
            return;
        }
        try {
            current.send(command);
        } catch (IOException e) {
            logger.debug("Error sending '{}' to HomeWorks processor: {}", command, e.getMessage());
            connectionFailed(current, "Error writing to processor: " + e.getMessage());
        }
    }

    @Override
    public void lineReceived(HwTelnetConnection source, String line) {
        handleIncomingMessage(line);
    }

    @Override
    public void connectionClosed(HwTelnetConnection source, String reason) {
        connectionFailed(source, reason);
    }

    private void connectionFailed(HwTelnetConnection source, String reason) {
        synchronized (this) {
            if (disposed || !source.equals(connection)) {
                // A connection that was already replaced or closed on purpose
                return;
            }
            connection = null;
        }
        logger.debug("Lost connection to HomeWorks processor at {}:{}: {}", config.ipAddress, config.port, reason);
        cancelHeartbeat();
        source.close();
        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, reason);
        scheduleReconnect();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing HomeWorks telnet bridge {}", getThing().getUID());
        disposed = true;
        cancelReconnect();
        cancelHeartbeat();
        HwTelnetConnection pending = connecting;
        if (pending != null) {
            pending.close();
        }
        closeConnection(true);
    }
}

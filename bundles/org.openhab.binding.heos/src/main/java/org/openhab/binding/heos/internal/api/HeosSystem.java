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
package org.openhab.binding.heos.internal.api;

import static org.openhab.binding.heos.internal.handler.FutureUtil.cancel;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.json.HeosJsonParser;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosSendCommand;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link HeosSystem} is handling the main commands, which are
 * sent and received by the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosSystem {
    private final Logger logger = LoggerFactory.getLogger(HeosSystem.class);

    private static final int START_DELAY_SEC = 30;
    private static final long LAST_EVENT_THRESHOLD = TimeUnit.HOURS.toMillis(2);

    private final ScheduledExecutorService scheduler;
    private @Nullable ExecutorService singleThreadExecutor;

    private final HeosEventController eventController = new HeosEventController(this);

    private final Telnet eventLine = new Telnet();
    private final HeosSendCommand eventSendCommand = new HeosSendCommand(eventLine);

    private final Telnet commandLine = new Telnet();
    private final HeosSendCommand sendCommand = new HeosSendCommand(commandLine);

    private final HeosJsonParser parser = new HeosJsonParser();
    private final PropertyChangeListener eventProcessor = evt -> {
        String newValue = (String) evt.getNewValue();
        ExecutorService executor = singleThreadExecutor;
        if (executor == null) {
            logger.debug("No executor available ignoring event: {}", newValue);
            return;
        }
        try {
            executor.submit(() -> eventController.handleEvent(parser.parseEvent(newValue)));
        } catch (JsonSyntaxException e) {
            logger.debug("Failed processing event JSON", e);
        }
    };

    private @Nullable ScheduledFuture<?> keepAliveJob;
    private @Nullable ScheduledFuture<?> reconnectJob;

    public HeosSystem(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Establishes the connection to the HEOS-Network if IP and Port is
     * set. The caller has to handle the retry to establish the connection
     * if the method returns {@code false}.
     *
     * @param connectionIP
     * @param connectionPort
     * @param heartbeat
     * @return {@code true} if connection is established else returns {@code false}
     */
    public HeosFacade establishConnection(String connectionIP, int connectionPort, int heartbeat)
            throws IOException, ReadException {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        if (commandLine.connect(connectionIP, connectionPort)) {
            logger.debug("HEOS command line connected at IP {} @ port {}", connectionIP, connectionPort);
            send(HeosCommands.registerChangeEventOff());
        }

        if (eventLine.connect(connectionIP, connectionPort)) {
            logger.debug("HEOS event line connected at IP {} @ port {}", connectionIP, connectionPort);
            eventSendCommand.send(HeosCommands.registerChangeEventOff(), Void.class);
        }

        startHeartBeat(heartbeat);
        startEventListener();

        return new HeosFacade(this, eventController);
    }

    boolean isConnected() {
        return sendCommand.isConnected() && eventSendCommand.isConnected();
    }

    /**
     * Starts the HEOS Heart Beat. This held the connection open even
     * if no data is transmitted. If the connection to the HEOS system
     * is lost, the method reconnects to the HEOS system by calling the
     * {@code establishConnection()} method. If the connection is lost or
     * reconnect the method fires a bridgeEvent via the {@code HeosEvenController.class}
     */
    void startHeartBeat(int heartbeatPulse) {
        keepAliveJob = scheduler.scheduleWithFixedDelay(new KeepAliveRunnable(), START_DELAY_SEC, heartbeatPulse,
                TimeUnit.SECONDS);
    }

    synchronized void startEventListener() throws IOException, ReadException {
        logger.debug("HEOS System Event Listener is starting....");
        eventSendCommand.startInputListener(HeosCommands.registerChangeEventOn());

        logger.debug("HEOS System Event Listener successfully started");
        eventLine.getReadResultListener().addPropertyChangeListener(eventProcessor);
    }

    void closeConnection() {
        logger.debug("Shutting down HEOS Heart Beat");
        cancel(keepAliveJob);
        cancel(this.reconnectJob, false);

        eventLine.getReadResultListener().removePropertyChangeListener(eventProcessor);
        eventSendCommand.stopInputListener(HeosCommands.registerChangeEventOff());
        eventSendCommand.disconnect();
        sendCommand.disconnect();
        @Nullable
        ExecutorService executor = this.singleThreadExecutor;
        if (executor != null && executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    HeosResponseObject<Void> send(String command) throws IOException, ReadException {
        return send(command, Void.class);
    }

    synchronized <T> HeosResponseObject<T> send(String command, Class<T> clazz) throws IOException, ReadException {
        return sendCommand.send(command, clazz);
    }

    /**
     * A class which provides a runnable for the HEOS Heart Beat
     *
     * @author Johannes Einig
     */
    private class KeepAliveRunnable implements Runnable {

        @Override
        public void run() {
            try {
                if (sendCommand.isHostReachable()) {
                    long timeSinceLastEvent = System.currentTimeMillis() - eventController.getLastEventTime();
                    logger.debug("Time since latest event: {} s", timeSinceLastEvent / 1000);

                    if (timeSinceLastEvent > LAST_EVENT_THRESHOLD) {
                        logger.debug("Events haven't been received for too long");
                        resetEventStream();
                        return;
                    }

                    logger.debug("Sending HEOS Heart Beat");
                    HeosResponseObject<Void> response = send(HeosCommands.heartbeat());
                    if (response.result) {
                        return;
                    }
                }
                logger.debug("Connection to HEOS Network lost!");

                // catches a failure during a heart beat send message if connection was
                // getting lost between last Heart Beat but Bridge is online again and not
                // detected by isHostReachable()
            } catch (ReadException | IOException e) {
                logger.debug("Failed at {}", System.currentTimeMillis(), e);
                logger.debug("Failure during HEOS Heart Beat command with message: {}", e.getMessage());
            }
            restartConnection();
        }

        private void restartConnection() {
            reset(a -> eventController.connectionToSystemLost());
        }

        private void resetEventStream() {
            reset(a -> eventController.eventStreamTimeout());
        }

        private void reset(Consumer<@Nullable Void> method) {
            closeConnection();
            method.accept(null);

            cancel(HeosSystem.this.reconnectJob, false);
            reconnectJob = scheduler.scheduleWithFixedDelay(this::reconnect, 1, 5, TimeUnit.SECONDS);
        }

        private void reconnect() {
            logger.debug("Trying to reconnect to HEOS Network...");
            if (!sendCommand.isHostReachable()) {
                return;
            }

            cancel(HeosSystem.this.reconnectJob, false);
            logger.debug("Reconnecting to Bridge");
            scheduler.schedule(eventController::systemReachable, 15, TimeUnit.SECONDS);
        }
    }
}

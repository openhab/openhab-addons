/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pilight.internal;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.AllStatus;
import org.openhab.binding.pilight.internal.dto.Identification;
import org.openhab.binding.pilight.internal.dto.Message;
import org.openhab.binding.pilight.internal.dto.Options;
import org.openhab.binding.pilight.internal.dto.Response;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.binding.pilight.internal.dto.Version;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class listens for updates from the pilight daemon. It is also responsible for requesting
 * and propagating the current pilight configuration.
 *
 * @author Jeroen Idserda - Initial contribution
 * @author Stefan Röllin - Port to openHAB 2 pilight binding
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 *
 */
@NonNullByDefault
public class PilightConnector implements Runnable, Closeable {

    private static final int RECONNECT_DELAY_MSEC = 10 * 1000; // 10 seconds

    private final Logger logger = LoggerFactory.getLogger(PilightConnector.class);

    private final PilightBridgeConfiguration config;

    private final IPilightCallback callback;

    private final ObjectMapper inputMapper = new ObjectMapper(
            new MappingJsonFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false));

    private final ObjectMapper outputMapper = new ObjectMapper(
            new MappingJsonFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false))
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    private @Nullable Socket socket;
    private @Nullable PrintStream printStream;

    private final ScheduledExecutorService scheduler;
    private final ConcurrentLinkedQueue<Action> delayedActionQueue = new ConcurrentLinkedQueue<>();
    private @Nullable ScheduledFuture<?> delayedActionWorkerFuture;

    public PilightConnector(final PilightBridgeConfiguration config, final IPilightCallback callback,
            final ScheduledExecutorService scheduler) {
        this.config = config;
        this.callback = callback;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        try {
            connect();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final @Nullable Socket socket = this.socket;
                    if (socket != null && !socket.isClosed()) {
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            String line = in.readLine();
                            while (!Thread.currentThread().isInterrupted() && line != null) {
                                logger.trace("Received from pilight: {}", line);
                                // ignore empty lines and lines starting with "status"
                                if (!line.isEmpty() && !line.startsWith("{\"status\":")) {
                                    final ObjectMapper inputMapper = this.inputMapper;
                                    if (line.startsWith("{\"message\":\"config\"")) {
                                        final @Nullable Message message = inputMapper.readValue(line, Message.class);
                                        callback.configReceived(message.getConfig());
                                    } else if (line.startsWith("{\"message\":\"values\"")) {
                                        final @Nullable AllStatus status = inputMapper.readValue(line, AllStatus.class);
                                        callback.statusReceived(status.getValues());
                                    } else if (line.startsWith("{\"version\":")) {
                                        final @Nullable Version version = inputMapper.readValue(line, Version.class);
                                        callback.versionReceived(version);
                                    } else if ("1".equals(line)) {
                                        throw new IOException("Connection to pilight lost");
                                    } else {
                                        final @Nullable Status status = inputMapper.readValue(line, Status.class);
                                        callback.statusReceived(List.of(status));
                                    }
                                }

                                line = in.readLine();
                            }
                        }
                    }
                } catch (IOException e) {
                    if (!Thread.currentThread().isInterrupted()) {
                        logger.debug("Error in pilight listener thread: {}", e.getMessage());
                    }
                }

                logger.debug("Disconnected from pilight server at {}:{}", config.getIpAddress(), config.getPort());

                if (!Thread.currentThread().isInterrupted()) {
                    callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null);
                    // empty line received (socket closed) or pilight stopped but binding
                    // is still running, try to reconnect
                    connect();
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Interrupting thread.");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tells the connector to refresh the configuration
     */
    public void refreshConfig() {
        doSendAction(new Action(Action.ACTION_REQUEST_CONFIG));
    }

    /**
     * Tells the connector to refresh the status of all devices
     */
    public void refreshStatus() {
        doSendAction(new Action(Action.ACTION_REQUEST_VALUES));
    }

    /**
     * Stops the listener
     */
    @Override
    public void close() {
        disconnect();
        Thread.currentThread().interrupt();
    }

    private void disconnect() {
        final @Nullable PrintStream printStream = this.printStream;
        if (printStream != null) {
            printStream.close();
            this.printStream = null;
        }

        final @Nullable Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error while closing pilight socket: {}", e.getMessage());
            }
            this.socket = null;
        }
    }

    private boolean isConnected() {
        final @Nullable Socket socket = this.socket;
        return socket != null && !socket.isClosed();
    }

    private void connect() throws InterruptedException {
        disconnect();

        int delay = 0;

        while (!isConnected()) {
            try {
                logger.debug("pilight connecting to {}:{}", config.getIpAddress(), config.getPort());

                Thread.sleep(delay);
                Socket socket = new Socket(config.getIpAddress(), config.getPort());

                Options options = new Options();
                options.setConfig(true);

                Identification identification = new Identification();
                identification.setOptions(options);

                // For some reason, directly using the outputMapper to write to the socket's OutputStream doesn't work.
                PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
                printStream.println(outputMapper.writeValueAsString(identification));

                final @Nullable Response response = inputMapper.readValue(socket.getInputStream(), Response.class);

                if (response.getStatus().equals(Response.SUCCESS)) {
                    logger.debug("Established connection to pilight server at {}:{}", config.getIpAddress(),
                            config.getPort());
                    this.socket = socket;
                    this.printStream = printStream;
                    callback.updateThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                } else {
                    printStream.close();
                    socket.close();
                    logger.debug("pilight client not accepted: {}", response.getStatus());
                }
            } catch (IOException e) {
                final @Nullable PrintStream printStream = this.printStream;
                if (printStream != null) {
                    printStream.close();
                }
                logger.debug("connect failed: {}", e.getMessage());
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            delay = RECONNECT_DELAY_MSEC;
        }
    }

    /**
     * send action to pilight daemon
     *
     * @param action action to send
     */
    public void sendAction(Action action) {
        delayedActionQueue.add(action);
        final @Nullable ScheduledFuture<?> delayedActionWorkerFuture = this.delayedActionWorkerFuture;

        if (delayedActionWorkerFuture == null || delayedActionWorkerFuture.isCancelled()) {
            this.delayedActionWorkerFuture = scheduler.scheduleWithFixedDelay(() -> {
                if (!delayedActionQueue.isEmpty()) {
                    doSendAction(delayedActionQueue.poll());
                } else {
                    final @Nullable ScheduledFuture<?> workerFuture = this.delayedActionWorkerFuture;
                    if (workerFuture != null) {
                        workerFuture.cancel(false);
                    }
                    this.delayedActionWorkerFuture = null;
                }
            }, 0, config.getDelay(), TimeUnit.MILLISECONDS);
        }
    }

    private void doSendAction(Action action) {
        final @Nullable PrintStream printStream = this.printStream;
        if (printStream != null) {
            try {
                printStream.println(outputMapper.writeValueAsString(action));
            } catch (IOException e) {
                logger.debug("Error while sending action '{}' to pilight server: {}", action.getAction(),
                        e.getMessage());
            }
        } else {
            logger.debug("Cannot send action '{}', not connected to pilight!", action.getAction());
        }
    }
}

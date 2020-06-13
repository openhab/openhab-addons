/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.AllStatus;
import org.openhab.binding.pilight.internal.dto.Identification;
import org.openhab.binding.pilight.internal.dto.Message;
import org.openhab.binding.pilight.internal.dto.Options;
import org.openhab.binding.pilight.internal.dto.Response;
import org.openhab.binding.pilight.internal.dto.Status;
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
 *
 */
@NonNullByDefault
public class PilightConnector extends Thread {

    private static final Integer RECONNECT_DELAY_MSEC = 10 * 1000; // 10 seconds

    private final Logger logger = LoggerFactory.getLogger(PilightConnector.class);

    private final PilightBridgeConfiguration config;

    private final IPilightCallback callback;

    private final ObjectMapper inputMapper = new ObjectMapper(
            new MappingJsonFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false));

    private final ObjectMapper outputMapper = new ObjectMapper(
            new MappingJsonFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false))
                    .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    private boolean running = true;

    private @Nullable Socket socket;
    private @Nullable PrintStream printStream;

    private Date lastUpdate = new Date(0);

    private ExecutorService delayedUpdateThreadPool = Executors.newSingleThreadExecutor();

    public PilightConnector(PilightBridgeConfiguration config, IPilightCallback callback) {
        this.config = config;
        this.callback = callback;
        setDaemon(true);
    }

    @Override
    public void run() {
        connect();

        while (running) {
            try {
                final Socket socket = this.socket;
                if (socket != null && !socket.isClosed()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = in.readLine();
                    while (running && line != null) {
                        if (!line.isEmpty()) {
                            logger.trace("Received from pilight: {}", line);
                            if (line.startsWith("{\"message\":\"config\"")) {
                                // Configuration received
                                logger.info("Config received");
                                callback.configReceived(inputMapper.readValue(line, Message.class).getConfig());
                            } else if (line.startsWith("{\"message\":\"values\"")) {
                                AllStatus status = inputMapper.readValue(line, AllStatus.class);
                                callback.statusReceived(status.getValues());
                            } else if (line.startsWith("{\"version\":")) {
                                // version message - skip it
                                logger.info("version received: {}", line);
                            } else if (line.startsWith("{\"status\":")) {
                                // Status message, we're not using this for now.
                                Response response = inputMapper.readValue(line, Response.class);
                                logger.trace("Response success: {}", response.isSuccess());
                            } else if (line.equals("1")) {
                                // pilight stopping
                                throw new IOException("Connection to pilight lost");
                            } else {
                                Status status = inputMapper.readValue(line, Status.class);
                                callback.statusReceived(Collections.singletonList(status));
                            }
                        }
                        line = in.readLine();
                    }
                }
            } catch (IOException e) {
                if (running) {
                    logger.debug("Error in pilight listener thread", e);
                }
            }

            logger.info("Disconnected from pilight server at {}:{}", config.getIpAddress(), config.getPort());

            if (running) {
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null);
                // empty line received (socket closed) or pilight stopped but binding
                // is still running, try to reconnect
                connect();
            }
        }
    }

    /**
     * Tells the connector to refresh the configuration
     */
    public void refreshConfig() {
        logger.trace("refreshConfig");
        doSendAction(new Action(Action.ACTION_REQUEST_CONFIG));
    }

    /**
     * Tells the connector to refresh the status of all devices
     */
    public void refreshStatus() {
        logger.trace("refreshStatus");
        doSendAction(new Action(Action.ACTION_REQUEST_VALUES));
    }

    /**
     * Stops the listener
     */
    public void close() {
        running = false;
        disconnect();
        interrupt();
    }

    private void disconnect() {
        final PrintStream printStream = this.printStream;
        if (printStream != null) {
            printStream.close();
            this.printStream = null;
        }

        final Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error while closing pilight socket", e);
            }
            this.socket = null;
        }
    }

    private boolean isConnected() {
        final Socket socket = this.socket;
        return socket != null && !socket.isClosed();
    }

    private void connect() {
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

                Response response = inputMapper.readValue(socket.getInputStream(), Response.class);

                if (response.getStatus().equals(Response.SUCCESS)) {
                    logger.info("Established connection to pilight server at {}:{}", config.getIpAddress(),
                            config.getPort());
                    this.socket = socket;
                    this.printStream = printStream;
                    callback.updateThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                } else {
                    socket.close();
                    logger.debug("pilight client not accepted: {}", response.getStatus());
                }
            } catch (IOException e) {
                logger.debug("connect failed", e);
                callback.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (InterruptedException e) {
                logger.debug("connect interrupted", e);
            }

            delay = RECONNECT_DELAY_MSEC;
        }
    }

    /**
     * send action to pilight daemon
     *
     * @param action action to send
     */
    public synchronized void sendAction(Action action) {
        DelayedUpdate delayed = new DelayedUpdate(action);
        delayedUpdateThreadPool.execute(delayed);
    }

    private void doSendAction(Action action) {
        final PrintStream printStream = this.printStream;
        if (printStream != null) {
            try {
                printStream.println(outputMapper.writeValueAsString(action));
            } catch (IOException e) {
                logger.debug("Error while sending action '{}' to pilight server", action.getAction(), e);
            }
        } else {
            logger.debug("Cannot send action '{}', not connected to pilight!", action.getAction());
        }
    }

    /**
     * Simple thread to allow calls to pilight to be throttled
     */
    private class DelayedUpdate implements Runnable {

        private final Action action;

        public DelayedUpdate(Action action) {
            this.action = action;
        }

        @Override
        public void run() {
            long delayBetweenUpdates = config.getDelay();

            long diff = new Date().getTime() - lastUpdate.getTime();
            if (diff < delayBetweenUpdates) {
                long delay = Math.min(delayBetweenUpdates - diff, config.getDelay());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    logger.debug("Error while processing pilight throttling delay");
                }
            }

            lastUpdate = new Date();
            doSendAction(action);
        }
    }
}

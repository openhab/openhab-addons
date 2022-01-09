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
package org.openhab.binding.vektiva.internal.handler;

import static org.openhab.binding.vektiva.internal.VektivaBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.vektiva.internal.config.VektivaSmarwiConfiguration;
import org.openhab.binding.vektiva.internal.net.VektivaSmarwiSocket;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VektivaSmarwiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class VektivaSmarwiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VektivaSmarwiHandler.class);

    private VektivaSmarwiConfiguration config = new VektivaSmarwiConfiguration();

    private final HttpClient httpClient;

    private final WebSocketClient webSocketClient;

    private @Nullable Session session;

    private @Nullable ScheduledFuture<?> future = null;

    private int lastPosition = -1;

    public VektivaSmarwiHandler(Thing thing, HttpClient httpClient, WebSocketClient webSocketClient) {
        super(thing);
        this.httpClient = httpClient;
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_STATUS.equals(channelUID.getId()) && command instanceof RefreshType) {
            checkStatus();
            return;
        }

        if (CHANNEL_CONTROL.equals(channelUID.getId())) {
            logger.trace("Received command: {}", command);
            String cmd = getSmarwiCommand(command);
            if (COMMAND_OPEN.equals(cmd) || COMMAND_CLOSE.equals(cmd) || COMMAND_STOP.equals(cmd)) {
                if (RESPONSE_OK.equals(sendCommand(cmd)) && !COMMAND_STOP.equals(cmd)) {
                    lastPosition = COMMAND_OPEN.equals(cmd) ? 0 : 100;
                } else {
                    lastPosition = -1;
                }
            }
            if (command instanceof PercentType) {
                if (RESPONSE_OK.equals(sendCommand(COMMAND_OPEN + "/" + cmd))) {
                    lastPosition = Integer.parseInt(cmd);
                }
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (future != null && !(future.isCancelled() || future.isDone())) {
            future.cancel(true);
        }
        closeSession();
    }

    private void closeSession() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    private String getSmarwiCommand(Command command) {
        if (UpDownType.UP.equals(command)) {
            return COMMAND_OPEN;
        }
        if (UpDownType.DOWN.equals(command)) {
            return COMMAND_CLOSE;
        }
        if (StopMoveType.STOP.equals(command)) {
            return COMMAND_STOP;
        }
        return command.toString();
    }

    private @Nullable String sendCommand(String cmd) {
        String url = "http://" + config.ip + "/cmd/" + cmd;

        try {
            ContentResponse resp = httpClient.newRequest(url).method(HttpMethod.GET).send();
            final String response = resp.getContentAsString();
            logger.trace("Response: {}", response);
            if (resp.getStatus() == 200) {
                if (ThingStatus.ONLINE != getThing().getStatus()) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            return response;
        } catch (InterruptedException e) {
            logger.debug("API execution has been interrupted", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "API execution has been interrupted");
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            logger.debug("Timeout during API execution", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Timeout during API execution");
        } catch (ExecutionException e) {
            logger.debug("Exception during API execution", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception during API execution: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void initialize() {
        config = getConfigAs(VektivaSmarwiConfiguration.class);
        logger.debug("IP address: {}", config.ip);

        future = scheduler.scheduleWithFixedDelay(this::checkStatus, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private synchronized void initializeWebSocketSession() {
        if (config.useWebSockets) {
            closeSession();
            session = createSession();
            if (session != null) {
                logger.debug("WebSocket connected!");
            }
        }
    }

    private void checkStatus() {
        String url = "http://" + config.ip + "/statusn";

        try {
            ContentResponse resp = httpClient.newRequest(url).method(HttpMethod.GET).send();
            final String response = resp.getContentAsString();
            logger.debug("status values: {}", response);
            if (resp.getStatus() == 200) {
                processStatusResponse(response);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "got response code: " + resp.getStatus());
            }
            // reconnect web socket if not connected
            if (config.useWebSockets && (session == null || !session.isOpen())
                    && ThingStatus.ONLINE == getThing().getStatus()) {
                logger.debug("Initializing WebSocket session");
                initializeWebSocketSession();
                return;
            }
        } catch (InterruptedException e) {
            logger.debug("API execution has been interrupted", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "API execution has been interrupted");
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            logger.debug("Timeout during status update", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Timeout during status update");
        } catch (ExecutionException e) {
            logger.debug("Exception during status update", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception during status update: " + e.getMessage());
        }
        session = null;
    }

    public synchronized void processStatusResponse(String content) {
        if (ThingStatus.ONLINE != getThing().getStatus()) {
            updateStatus(ThingStatus.ONLINE);
        }

        Map<String, String> values = Stream.of(content.split("\n")).map(s -> s.split(":")).filter(s -> s.length == 2)
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        updateProperty("Product type", values.getOrDefault("t", NA));
        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, values.getOrDefault("fw", NA));
        updateProperty("Wifi signal", values.getOrDefault("rssi", NA));
        updateProperty("Product name", values.getOrDefault("cid", NA));

        String statusMessage = "Stopped";
        if (!"250".equals(values.getOrDefault("s", NA))) {
            statusMessage = "Moving";
        }

        if ("1".equals(values.getOrDefault("ro", NA))) {
            statusMessage = "Not ready";
        }

        if ("10".equals(values.getOrDefault("e", NA))) {
            statusMessage = "Blocked";
        }

        int position = values.getOrDefault("pos", NA).equals("o") ? 0 : 100;
        if (position == 0 && lastPosition != -1) {
            position = lastPosition;
        }
        updateState(CHANNEL_CONTROL, new PercentType(position));
        updateState(CHANNEL_STATUS, new StringType(statusMessage));
    }

    private @Nullable Session createSession() {
        String url = "ws://" + config.ip + "/ws";
        URI uri = URI.create(url);

        try {
            // The socket that receives events
            VektivaSmarwiSocket socket = new VektivaSmarwiSocket(this);
            // Attempt Connect
            Future<Session> fut = webSocketClient.connect(socket, uri);
            // Wait for Connect
            return fut.get();
        } catch (IOException ex) {
            logger.debug("Cannot connect websocket client", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot connect websocket client");
        } catch (InterruptedException ex) {
            logger.debug("Cannot create websocket session", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot create websocket session");
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            logger.debug("Cannot create websocket session", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot create websocket session");
        }
        return null;
    }
}

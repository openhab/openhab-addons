/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.vektiva.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.vektiva.internal.config.VektivaSmarwiConfiguration;
import org.openhab.binding.vektiva.internal.net.VektivaSmarwiiSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.*;

import static org.openhab.binding.vektiva.VektivaBindingConstants.*;

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

    private HttpClient httpClient = new HttpClient();

    private WebSocketClient webSocketClient = new WebSocketClient();

    private @Nullable Session session;

    private @Nullable ScheduledFuture<?> future = null;

    private int lastPosition = -1;

    public VektivaSmarwiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            checkStatus();
            return;
        }

        if (channelUID.getId().equals(CHANNEL_CONTROL)) {
            logger.debug("Received command: {}", command.toString());
            String cmd = getSmarwiCommand(command);
            if (cmd.equals(COMMAND_OPEN) || cmd.equals(COMMAND_CLOSE) || cmd.equals(COMMAND_STOP)) {
                if (RESPONSE_OK.equals(sendCommand(cmd))) {
                    lastPosition = cmd.equals(COMMAND_OPEN) ? 0 : 100;
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
        if (httpClient.isStarted()) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                //silence
            }
        }
        closeSession();
        if (webSocketClient.isStarted()) {
            try {
                webSocketClient.stop();
            } catch (Exception e) {
                //silence
            }
        }
    }

    private void closeSession() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    private String getSmarwiCommand(Command command) {
        switch (command.toString()) {
            case "UP":
                return COMMAND_OPEN;
            case "DOWN":
                return COMMAND_CLOSE;
            case "STOP":
                return COMMAND_STOP;
            default:
                return command.toString();
        }
    }

    private @Nullable String sendCommand(String cmd) {
        String url = "http://" + config.ip + "/cmd/" + cmd;

        try {
            ContentResponse resp = httpClient.newRequest(url).method(HttpMethod.GET).send();
            logger.trace("Response: {}", resp.getContentAsString());
            if (resp.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            return resp.getContentAsString();
        } catch (InterruptedException e) {
            logger.error("API execution has been interrupted", e);
            updateStatus(ThingStatus.OFFLINE);
        } catch (TimeoutException e) {
            logger.error("Timeout during API execution", e);
            updateStatus(ThingStatus.OFFLINE);
        } catch (ExecutionException e) {
            logger.error("Exception during API execution", e);
            updateStatus(ThingStatus.OFFLINE);
        }
        return null;
    }

    @Override
    public void initialize() {
        config = getConfigAs(VektivaSmarwiConfiguration.class);
        logger.debug("IP address: {}", config.ip);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Cannot start http client!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot start http client!");
            return;
        }

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
            logger.debug("status values: {}", resp.getContentAsString());
            if (resp.getStatus() == 200) {
                processStatusResponse(resp.getContentAsString());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "got response code: " + resp.getStatus());
            }
            // reconnect web socket if not connected
            if (config.useWebSockets && (session == null || !session.isOpen()) && getThing().getStatus().equals(ThingStatus.ONLINE)) {
                logger.debug("Initializing WebSocket session");
                initializeWebSocketSession();
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "exception during status getting");
            session = null;
        }
    }

    public synchronized void processStatusResponse(String content) {
        updateStatus(ThingStatus.ONLINE);
        String[] values = content.split("\n");

        updateProperty("type", getPropertyValue(values, "t"));
        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, getPropertyValue(values, "fw"));
        updateProperty("rssi", getPropertyValue(values, "rssi"));
        updateProperty("name", getPropertyValue(values, "cid"));
        updateProperty("status", getPropertyValue(values, "s"));
        updateProperty("error", getPropertyValue(values, "e"));
        updateProperty("ok", getPropertyValue(values, "ok"));
        updateProperty("ro", getPropertyValue(values, "ro"));
        updateProperty("fix", getPropertyValue(values, "fix"));

        if ("1".equals(getPropertyValue(values, "ro"))) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Online but not ready!");
        }

        int position = getPropertyValue(values, "pos").equals("o") ? 0 : 100;
        if (position == 0 && lastPosition != -1) {
            position = lastPosition;
        }
        updateState(CHANNEL_CONTROL, new PercentType(position));
    }

    private String getPropertyValue(String[] values, String property) {
        for (String val : values) {
            String[] keyVal = val.split(":");
            if (keyVal.length != 2) continue;
            String key = keyVal[0];
            String value = keyVal[1];
            if (property.equals(key)) {
                return value;
            }
        }
        return "N/A";
    }

    private @Nullable Session createSession() {
        String url = "ws://" + config.ip + "/ws";
        URI uri = URI.create(url);

        try {
            webSocketClient.start();
            // The socket that receives events
            VektivaSmarwiiSocket socket = new VektivaSmarwiiSocket(this);
            // Attempt Connect
            Future<Session> fut = webSocketClient.connect(socket, uri);
            // Wait for Connect
            return fut.get();
        } catch (Exception ex) {
            logger.error("Cannot create websocket client/session", ex);
        }
        return null;
    }
}

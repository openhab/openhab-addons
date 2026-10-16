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
package org.openhab.binding.yioremote.internal;

import static org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yioremote.internal.dto.AuthenticationMessage;
import org.openhab.binding.yioremote.internal.dto.DockCommandMessage;
import org.openhab.binding.yioremote.internal.dto.DockMessage;
import org.openhab.binding.yioremote.internal.dto.IRCodeSendMessage;
import org.openhab.binding.yioremote.internal.utils.Websocket;
import org.openhab.binding.yioremote.internal.utils.WebsocketInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link YIOremoteDockHandler} manages the WebSocket connection to a YIO Dock and handles the commands sent to its
 * channels.
 *
 * A single watchdog job runs every {@link YIOremoteBindingConstants#HEARTBEAT_INTERVAL_SECONDS} seconds. While
 * authenticated it sends a ping and checks that the dock answered since the last run; otherwise it (re)connects.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteDockHandler extends BaseThingHandler implements WebsocketInterface {

    private enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        AUTHENTICATING,
        AUTHENTICATED,
        AUTHENTICATION_FAILED
    }

    private final Logger logger = LoggerFactory.getLogger(YIOremoteDockHandler.class);
    private final Gson gson = new Gson();
    private final WebSocketClient webSocketClient;

    private YIOremoteConfiguration config = new YIOremoteConfiguration();
    private @Nullable URI dockUri;
    private @Nullable Websocket websocket;
    private @Nullable ScheduledFuture<?> watchdogJob;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private boolean dockAlive;
    // incremented on every initialize() and dispose(), a watchdog run of an older lifecycle does nothing
    private long lifecycle;
    // receiver setting requested by the user, restored on the dock after every (re)connect
    private boolean receiverEnabled;

    public YIOremoteDockHandler(Thing thing, WebSocketClient webSocketClient) {
        super(thing);
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void initialize() {
        config = getConfigAs(YIOremoteConfiguration.class);
        String host = config.host.trim();

        if (host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-host");
            return;
        }

        try {
            dockUri = new URI("ws", null, host, DOCK_PORT, null, null, null);
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-host [\"" + host + "\"]");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        synchronized (this) {
            long currentLifecycle = ++lifecycle;
            connectionState = ConnectionState.DISCONNECTED;
            watchdogJob = scheduler.scheduleWithFixedDelay(() -> watchdog(currentLifecycle), 0,
                    HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            lifecycle++;
            stopWatchdog();
            closeWebsocket();
            connectionState = ConnectionState.DISCONNECTED;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(YIOremoteDockActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (RECEIVER_SWITCH_CHANNEL.equals(channelUID.getIdWithoutGroup()) && command instanceof OnOffType onOff) {
            synchronized (this) {
                receiverEnabled = onOff == OnOffType.ON;
                if (connectionState != ConnectionState.AUTHENTICATED) {
                    logger.debug("YIO Dock not connected, IR receiver will be switched {} after connecting", onOff);
                    return;
                }
                sendReceiverSetting();
            }
        }
    }

    public void sendIRCode(@Nullable String irCode) {
        if (irCode == null || !IR_CODE_PATTERN.matcher(irCode).matches()) {
            logger.warn("Invalid IR code format '{}', expected e.g. '3;0x20DF40BF;32;0'", irCode);
            return;
        }
        synchronized (this) {
            if (connectionState != ConnectionState.AUTHENTICATED) {
                logger.debug("YIO Dock not connected, IR code {} not sent", irCode);
                return;
            }
            sendMessage(new IRCodeSendMessage(irCode));
        }
    }

    // ---- connection handling ----

    private synchronized void watchdog(long watchdogLifecycle) {
        if (watchdogLifecycle != lifecycle) {
            // handler was disposed or re-initialized while this run was waiting
            return;
        }
        switch (connectionState) {
            case AUTHENTICATED:
                if (dockAlive) {
                    dockAlive = false;
                    sendMessage(new DockCommandMessage(COMMAND_PING));
                } else {
                    logger.debug("No answer from YIO Dock since last ping, reconnecting");
                    connectionLost("@text/offline.comm-error-no-response");
                    connect();
                }
                break;
            case CONNECTING:
            case AUTHENTICATING:
                logger.debug("Connection to YIO Dock not established in time, reconnecting");
                connectionLost("@text/offline.comm-error-no-authentication");
                connect();
                break;
            case DISCONNECTED:
                connect();
                break;
            case AUTHENTICATION_FAILED:
                // wrong access token, wait for a configuration change
                break;
        }
    }

    private synchronized void connect() {
        URI uri = dockUri;
        if (uri == null) {
            return;
        }
        closeWebsocket();
        Websocket newWebsocket = new Websocket(this);
        websocket = newWebsocket;
        connectionState = ConnectionState.CONNECTING;
        logger.debug("Connecting to YIO Dock at {}", uri);
        try {
            webSocketClient.connect(newWebsocket, uri, new ClientUpgradeRequest());
        } catch (IOException e) {
            String message = Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName());
            logger.debug("Connection to YIO Dock failed: {}", message);
            connectionLost("@text/offline.comm-error-connection-failed [\"" + message + "\"]");
        }
    }

    private void connectionLost(String statusDescription) {
        closeWebsocket();
        connectionState = ConnectionState.DISCONNECTED;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, statusDescription);
    }

    private void closeWebsocket() {
        Websocket oldWebsocket = websocket;
        websocket = null;
        if (oldWebsocket != null) {
            oldWebsocket.close();
        }
    }

    private void stopWatchdog() {
        ScheduledFuture<?> job = watchdogJob;
        if (job != null) {
            job.cancel(true);
            watchdogJob = null;
        }
    }

    private void sendMessage(Object message) {
        Websocket localWebsocket = websocket;
        if (localWebsocket != null) {
            String json = gson.toJson(message);
            if (!(message instanceof AuthenticationMessage)) {
                logger.debug("Sending to YIO Dock: {}", json);
            }
            localWebsocket.sendMessage(json);
        }
    }

    // ---- WebSocket events, events of an outdated connection are ignored ----

    @Override
    public synchronized void onConnect(Websocket source) {
        if (!source.equals(websocket)) {
            // connection completed after it was replaced or the handler was disposed
            source.close();
            return;
        }
        logger.debug("Connected to YIO Dock, waiting for authentication request");
    }

    @Override
    public synchronized void onClose(Websocket source, int statusCode, @Nullable String reason) {
        if (source.equals(websocket)) {
            logger.debug("Connection to YIO Dock closed: {} - {}", statusCode, reason);
            connectionLost("@text/offline.comm-error-connection-closed");
        }
    }

    @Override
    public synchronized void onError(Websocket source, Throwable cause) {
        if (source.equals(websocket)) {
            String message = Objects.requireNonNullElse(cause.getMessage(), cause.getClass().getSimpleName());
            logger.debug("Connection error to YIO Dock: {}", message);
            connectionLost("@text/offline.comm-error-connection-failed [\"" + message + "\"]");
        }
    }

    @Override
    public synchronized void onMessage(Websocket source, String message) {
        if (!source.equals(websocket)) {
            return;
        }
        logger.debug("Message received from YIO Dock: {}", message);

        DockMessage dockMessage;
        try {
            dockMessage = gson.fromJson(message, DockMessage.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Ignoring invalid message from YIO Dock: {}", message);
            return;
        }
        if (dockMessage == null) {
            return;
        }

        if (MESSAGE_TYPE_AUTH_REQUIRED.equals(dockMessage.type)) {
            handleAuthenticationRequired();
        } else if (MESSAGE_TYPE_AUTH_OK.equals(dockMessage.type)) {
            handleAuthenticationOk(dockMessage.version);
        } else if (MESSAGE_TYPE_AUTH.equals(dockMessage.type) && connectionState == ConnectionState.AUTHENTICATING) {
            // the dock rejects an invalid token with {"type":"auth","message":"Invalid token"}
            handleAuthenticationRejected(dockMessage.message);
        } else if (connectionState != ConnectionState.AUTHENTICATED) {
            logger.debug("Ignoring message received before authentication: {}", message);
        } else if (MESSAGE_PONG.equals(dockMessage.message)) {
            dockAlive = true;
        } else if (COMMAND_IR_SEND.equals(dockMessage.message)) {
            dockAlive = true;
            Boolean success = dockMessage.success;
            if (success != null) {
                updateGroupState(GROUP_OUTPUT, STATUS_STRING_CHANNEL,
                        new StringType(success ? STATUS_IR_SEND_SUCCESS : STATUS_IR_SEND_FAILURE));
            }
        } else if (COMMAND_IR_RECEIVE.equals(dockMessage.command)) {
            dockAlive = true;
            String code = dockMessage.code;
            if (code != null) {
                updateGroupState(GROUP_OUTPUT, STATUS_STRING_CHANNEL, new StringType(code));
            }
        } else {
            logger.debug("Ignoring unknown message from YIO Dock: {}", message);
        }
    }

    private void handleAuthenticationRequired() {
        connectionState = ConnectionState.AUTHENTICATING;
        logger.debug("Sending authentication to YIO Dock");
        sendMessage(new AuthenticationMessage(config.accessToken));
    }

    private void handleAuthenticationRejected(@Nullable String reason) {
        logger.debug("YIO Dock rejected the access token: {}", reason);
        closeWebsocket();
        // no reconnect until the configuration is changed
        connectionState = ConnectionState.AUTHENTICATION_FAILED;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.conf-error-authentication-failed [\"" + Objects.requireNonNullElse(reason, "") + "\"]");
    }

    private void handleAuthenticationOk(@Nullable String dockVersion) {
        connectionState = ConnectionState.AUTHENTICATED;
        dockAlive = true;
        // newer dock firmware reports its version with auth_ok, older firmware does not
        if (dockVersion != null && !dockVersion.isBlank()) {
            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, dockVersion);
        }
        updateStatus(ThingStatus.ONLINE);
        updateGroupState(GROUP_OUTPUT, STATUS_STRING_CHANNEL, StringType.EMPTY);
        // the dock keeps its receiver setting across connections, so restore the requested one
        sendReceiverSetting();
    }

    private void sendReceiverSetting() {
        sendMessage(new DockCommandMessage(receiverEnabled ? COMMAND_IR_RECEIVE_ON : COMMAND_IR_RECEIVE_OFF));
        updateGroupState(GROUP_INPUT, RECEIVER_SWITCH_CHANNEL, OnOffType.from(receiverEnabled));
    }

    private void updateGroupState(String group, String channelId, State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channelId), state);
    }
}

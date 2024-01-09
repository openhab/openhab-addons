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
package org.openhab.binding.freeboxos.internal.api.rest;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;
import org.openhab.binding.freeboxos.internal.api.rest.VmManager.VirtualMachine;
import org.openhab.binding.freeboxos.internal.handler.ApiConsumerHandler;
import org.openhab.binding.freeboxos.internal.handler.HostHandler;
import org.openhab.binding.freeboxos.internal.handler.VmHandler;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link WebSocketManager} is the Java class register to the websocket server and handle notifications
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WebSocketManager extends RestManager implements WebSocketListener {
    private static final String HOST_UNREACHABLE = "lan_host_l3addr_unreachable";
    private static final String HOST_REACHABLE = "lan_host_l3addr_reachable";
    private static final String VM_CHANGED = "vm_state_changed";
    private static final Register REGISTRATION = new Register(VM_CHANGED, HOST_REACHABLE, HOST_UNREACHABLE);
    private static final String WS_PATH = "ws/event";

    private final Logger logger = LoggerFactory.getLogger(WebSocketManager.class);
    private final Map<MACAddress, ApiConsumerHandler> listeners = new HashMap<>();
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(BINDING_ID);
    private final ApiHandler apiHandler;
    private final WebSocketClient client;
    private Optional<ScheduledFuture<?>> reconnectJob = Optional.empty();
    private volatile @Nullable Session wsSession;

    private record Register(String action, List<String> events) {
        Register(String... events) {
            this("register", List.of(events));
        }
    }

    public WebSocketManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, session.getUriBuilder().path(WS_PATH));
        this.apiHandler = session.getApiHandler();
        this.client = new WebSocketClient(apiHandler.getHttpClient());
    }

    private enum Action {
        REGISTER,
        NOTIFICATION,
        UNKNOWN
    }

    private static record WebSocketResponse(boolean success, Action action, String event, String source,
            @Nullable JsonElement result) {
        public String getEvent() {
            return source + "_" + event;
        }
    }

    public void openSession(@Nullable String sessionToken, int reconnectInterval) {
        if (reconnectInterval > 0) {
            URI uri = getUriBuilder().scheme(getUriBuilder().build().getScheme().contains("s") ? "wss" : "ws").build();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader(ApiHandler.AUTH_HEADER, sessionToken);
            try {
                client.start();
                stopReconnect();
                reconnectJob = Optional.of(scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        closeSession();
                        client.connect(this, uri, request);
                        // Update listeners in case we would have lost data while disconnecting / reconnecting
                        listeners.values()
                                .forEach(host -> host.handleCommand(new ChannelUID(host.getThing().getUID(), REACHABLE),
                                        RefreshType.REFRESH));
                        logger.debug("Websocket manager connected to {}", uri);
                    } catch (IOException e) {
                        logger.warn("Error connecting websocket client: {}", e.getMessage());
                    }
                }, 0, reconnectInterval, TimeUnit.MINUTES));
            } catch (Exception e) {
                logger.warn("Error starting websocket client: {}", e.getMessage());
            }
        }
    }

    private void stopReconnect() {
        reconnectJob.ifPresent(job -> job.cancel(true));
        reconnectJob = Optional.empty();
    }

    public void dispose() {
        stopReconnect();
        closeSession();
        try {
            client.stop();
        } catch (Exception e) {
            logger.warn("Error stopping websocket client: {}", e.getMessage());
        }
    }

    private void closeSession() {
        logger.debug("Awaiting closure from remote");
        Session localSession = wsSession;
        if (localSession != null) {
            localSession.close();
            wsSession = null;
        }
    }

    @Override
    public void onWebSocketConnect(@NonNullByDefault({}) Session wsSession) {
        this.wsSession = wsSession;
        logger.debug("Websocket connection establisehd");
        try {
            wsSession.getRemote().sendString(apiHandler.serialize(REGISTRATION));
        } catch (IOException e) {
            logger.warn("Error registering to websocket: {}", e.getMessage());
        }
    }

    @Override
    public void onWebSocketText(@NonNullByDefault({}) String message) {
        Session localSession = wsSession;
        if (message.toLowerCase(Locale.US).contains("bye") && localSession != null) {
            localSession.close(StatusCode.NORMAL, "Thanks");
            return;
        }

        WebSocketResponse result = apiHandler.deserialize(WebSocketResponse.class, message);
        if (result.success) {
            switch (result.action) {
                case REGISTER:
                    logger.debug("Event registration successfull");
                    break;
                case NOTIFICATION:
                    handleNotification(result);
                    break;
                default:
                    logger.warn("Unhandled notification received: {}", result.action);
            }
        }
    }

    private void handleNotification(WebSocketResponse response) {
        JsonElement json = response.result;
        if (json != null) {
            switch (response.getEvent()) {
                case VM_CHANGED:
                    VirtualMachine vm = apiHandler.deserialize(VirtualMachine.class, json.toString());
                    logger.debug("Received notification for VM {}", vm.id());
                    ApiConsumerHandler handler = listeners.get(vm.mac());
                    if (handler instanceof VmHandler vmHandler) {
                        vmHandler.updateVmChannels(vm);
                    }
                    break;
                case HOST_UNREACHABLE, HOST_REACHABLE:
                    LanHost host = apiHandler.deserialize(LanHost.class, json.toString());
                    ApiConsumerHandler handler2 = listeners.get(host.getMac());
                    if (handler2 instanceof HostHandler hostHandler) {
                        hostHandler.updateConnectivityChannels(host);
                    }
                    break;
                default:
                    logger.warn("Unhandled event received: {}", response.getEvent());
            }
        } else {
            logger.warn("Empty json element in notification");
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, @NonNullByDefault({}) String reason) {
        logger.debug("Socket Closed: [{}] - reason {}", statusCode, reason);
        this.wsSession = null;
    }

    @Override
    public void onWebSocketError(@NonNullByDefault({}) Throwable cause) {
        logger.warn("Error on websocket: {}", cause.getMessage());
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] payload, int offset, int len) {
        /* do nothing */
    }

    public boolean registerListener(MACAddress mac, ApiConsumerHandler hostHandler) {
        if (wsSession != null) {
            listeners.put(mac, hostHandler);
            return true;
        }
        return false;
    }

    public void unregisterListener(MACAddress mac) {
        listeners.remove(mac);
    }
}

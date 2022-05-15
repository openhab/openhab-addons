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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_CLASS_SHELLY;

import java.util.Random;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link Shelly1HttpApi} wraps the Shelly REST API and provides various low level function to access the device api
 * (not
 * cloud api).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyWebSocketRpc implements ShellyWebSocketInterface {
    private final Logger logger = LoggerFactory.getLogger(ShellyWebSocketRpc.class);
    private final Gson gson = new Gson();
    private final Random random = new Random();

    private @Nullable ShellyThingInterface thing;
    private final ShellyThingConfiguration config;
    private final ShellyWebSocket rpcSocket;
    private final ShellyWebSocketInterface wsCaller;
    private SocketStatus actualStatus = SocketStatus.UNINITIALIZED_STATE;
    private @Nullable Future<?> webSocketPollingJob;
    private @Nullable Future<?> webSocketReconnectionPollingJob;

    public static enum SocketStatus {
        UNINITIALIZED_STATE,
        AUTHENTICATION_PROCESS,
        AUTHENTICATION_FAILED,
        AUTHENTICATION_COMPLETE,
        SEND_PING,
        CHECK_PONG,
        CONNECTION_FAILED,
        CONNECTION_ESTABLISHED,
        COMMUNICATION_ERROR,
        RECONNECTION_PROCESS;
    }

    public ShellyWebSocketRpc(ShellyThingInterface thingInterface, ShellyWebSocketInterface wsCaller) {
        this.thing = thingInterface;
        this.config = thingInterface.getThingConfig();
        this.wsCaller = wsCaller;
        this.rpcSocket = new ShellyWebSocket(config.deviceIp);
        rpcSocket.addMessageHandler(this);
    }

    public ShellyWebSocketRpc(ShellyThingConfiguration config, ShellyWebSocketInterface wsCaller) {
        this.config = config;
        this.wsCaller = wsCaller;
        this.rpcSocket = new ShellyWebSocket(config.deviceIp);
        rpcSocket.addMessageHandler(this);
    }

    public void initialize() throws ShellyApiException {
        if (!rpcSocket.isConnected()) {
            rpcSocket.connect();
        }
    }

    public void apiRequest(String src, String method, String data) throws ShellyApiException {
        Shelly2RpcBaseMessage request = builRequest(src, method, data);
        rpcSocket.sendMessage(gson.toJson(request)); // submit, result wull be async
    }

    @Override
    public void onConnect(boolean connected) {
        actualStatus = connected ? SocketStatus.CONNECTION_ESTABLISHED : SocketStatus.CONNECTION_FAILED;
        wsCaller.onConnect(connected);
    }

    @Override
    public void onMessage(String message) {
        wsCaller.onMessage(message);
    }

    @Override
    public void onNotifyStatus(Shelly2RpcNotifyStatus message) {
        wsCaller.onNotifyStatus(message);
    }

    @Override
    public void onNotifyEvent(Shelly2RpcNotifyEvent message) {
        wsCaller.onNotifyEvent(message);
    }

    @Override
    public void onClose() {
        disposeWebsocketPollingJob();
        reconnectWebsocket();
        wsCaller.onClose();
    }

    @Override
    public void onError(Throwable cause) {
        disposeWebsocketPollingJob();
        actualStatus = SocketStatus.COMMUNICATION_ERROR;
        wsCaller.onError(cause);
        reconnectWebsocket();
    }

    private Shelly2RpcBaseMessage builRequest(String src, String method, String data) {
        // "{\"id\":\"Shelly.GetConfig_16198780703520.4696194518809119\",\"src\":\"localweb851\",\"method\":\"Shelly.GetConfig\"}",
        Shelly2RpcBaseMessage request = new Shelly2RpcBaseMessage();
        request.id = random.nextInt();
        request.src = src;
        request.method = SHELLYRPC_METHOD_CLASS_SHELLY + "." + method;
        return request;
    }

    public void reconnectWebsocket() {
        if (webSocketReconnectionPollingJob == null) {
            // webSocketReconnectionPollingJob = scheduler.scheduleWithFixedDelay(this::reconnectWebsocketJob, 0, 30,
            // TimeUnit.SECONDS);
        }
    }

    public void reconnectWebsocketJob() throws ShellyApiException {
        switch (actualStatus) {
            case COMMUNICATION_ERROR:
                logger.debug("Reconnecting YIORemoteHandler");
                try {
                    disposeWebsocketPollingJob();
                    rpcSocket.closeWebsocketSession();
                    if (thing != null) {
                        thing.setThingOffline(ThingStatusDetail.COMMUNICATION_ERROR, "statusupdate.failed");
                    }
                    actualStatus = SocketStatus.RECONNECTION_PROCESS;
                } catch (Exception e) {
                    logger.debug("Connection error {}", e.getMessage());
                }
                rpcSocket.connect();
                break;
            case AUTHENTICATION_COMPLETE:
                if (webSocketReconnectionPollingJob != null) {
                    if (!webSocketReconnectionPollingJob.isCancelled() && webSocketReconnectionPollingJob != null) {
                        webSocketReconnectionPollingJob.cancel(true);
                    }
                    webSocketReconnectionPollingJob = null;
                }
                break;
            default:
                break;
        }
    }

    public void close() {
        disposeWebsocketPollingJob();
        if (webSocketReconnectionPollingJob != null) {
            if (!webSocketReconnectionPollingJob.isCancelled() && webSocketReconnectionPollingJob != null) {
                webSocketReconnectionPollingJob.cancel(true);
            }
            webSocketReconnectionPollingJob = null;
        }
    }

    private void disposeWebsocketPollingJob() {
        if (webSocketPollingJob != null) {
            if (!webSocketPollingJob.isCancelled() && webSocketPollingJob != null) {
                webSocketPollingJob.cancel(true);
            }
            webSocketPollingJob = null;
        }
    }
}

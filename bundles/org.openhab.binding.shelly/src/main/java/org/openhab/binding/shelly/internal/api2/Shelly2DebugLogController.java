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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.dto.ShellyDebugLogJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.mkChannelId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest.Shelly2RpcRequestParams;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2WsConfigResponse.Shelly2WsConfigResult;
import org.openhab.binding.shelly.internal.api2.dto.ShellyDebugLogJsonDTO.Shelly2ConfigParmsDebug;
import org.openhab.binding.shelly.internal.api2.dto.ShellyDebugLogJsonDTO.Shelly2ConfigParmsDebug.Shelly2ConfigParmsDebugWebSocket;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shelly2DebugLogController} arms/disarms the device's Debug Log ({@code debug.websocket.enable}), owns the
 * {@link Shelly2DebugLogSocket} and forwards the received log lines to openHAB's log.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2DebugLogController implements Shelly2DebugLogListener {
    private final Logger logger = LoggerFactory.getLogger(Shelly2DebugLogController.class);

    private final String thingName;
    private final Shelly2ApiRpc api;
    private final ShellyApiConfiguration config;
    private final WebSocketClient client;

    // All access must be guarded by "this"
    private @Nullable Shelly2DebugLogSocket socket;

    public Shelly2DebugLogController(String thingName, Shelly2ApiRpc api, ShellyApiConfiguration config,
            WebSocketClient client) {
        this.thingName = thingName;
        this.api = api;
        this.config = config;
        this.client = client;
    }

    public synchronized void setEnabled(boolean enable) throws ShellyApiException {
        try {
            Shelly2RpcRequestParams params = new Shelly2RpcRequestParams().withConfig();
            params.config.debug = new Shelly2ConfigParmsDebug();
            params.config.debug.websocket = new Shelly2ConfigParmsDebugWebSocket();
            params.config.debug.websocket.enable = enable;
            api.apiRequest(SHELLYRPC_METHOD_SYS_SETCONFIG, params, Shelly2WsConfigResult.class);

            if (enable) {
                open();
            } else {
                closeSocket();
            }
        } finally {
            publishState();
        }
    }

    public synchronized boolean isEnabled() {
        return socket != null;
    }

    /**
     * The device keeps {@code debug.websocket.enable} across restarts, so streaming is resumed after a re-init.
     */
    public synchronized void resume() {
        try {
            open();
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to resume Debug Log streaming: {}", thingName, e.getMessage());
        }
    }

    /**
     * Closes the socket. When the thing is being disabled/removed, the device is disarmed as well instead of
     * streaming with nothing listening until it reboots.
     */
    public synchronized void close(boolean thingStopping) {
        if (thingStopping && socket != null) {
            try {
                setEnabled(false);
            } catch (ShellyApiException e) {
                logger.debug("{}: Unable to disable Debug Log on the device during shutdown: {}", thingName,
                        e.getMessage());
            }
        }
        closeSocket();
    }

    private void open() throws ShellyApiException {
        if (socket != null) {
            return;
        }
        Shelly2DebugLogSocket newSocket = new Shelly2DebugLogSocket(thingName, config.getDebugLogUrl(), client, this);
        socket = newSocket;
        try {
            newSocket.connect(api.buildDebugLogAuthHeader());
        } catch (ShellyApiException e) {
            socket = null;
            throw e;
        }
    }

    private void closeSocket() {
        Shelly2DebugLogSocket socket = this.socket;
        this.socket = null;
        if (socket != null) {
            socket.disconnect();
        }
    }

    private void publishState() {
        try {
            api.getThing().updateChannel(mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_DEBUG),
                    OnOffType.from(socket != null), true);
        } catch (ShellyApiException e) {
            // thing already disposed
        }
    }

    @Override
    public void onDebugLogLine(int level, String data) {
        switch (level) {
            case SHELLY2_DEBUGLOG_LEVEL_ERROR:
            case SHELLY2_DEBUGLOG_LEVEL_WARN:
                logger.warn("{}: [DEVICE] {}", thingName, data);
                break;
            case SHELLY2_DEBUGLOG_LEVEL_VERBOSE:
                if (logger.isTraceEnabled()) {
                    logger.trace("{}: [DEVICE] {}", thingName, data);
                }
                break;
            default: // INFO, DEBUG
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: [DEVICE] {}", thingName, data);
                }
                break;
        }
    }

    @Override
    public synchronized void onDebugLogClosed(Shelly2DebugLogSocket closedSocket) {
        if (closedSocket.equals(socket)) {
            socket = null;
            logger.debug("{}: Debug Log WebSocket closed by the device", thingName);
            publishState();
        }
    }
}

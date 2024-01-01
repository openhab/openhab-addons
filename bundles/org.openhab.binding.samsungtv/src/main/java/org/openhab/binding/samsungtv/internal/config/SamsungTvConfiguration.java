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
package org.openhab.binding.samsungtv.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;

/**
 * Configuration class for {@link SamsungTvHandler}.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Arjan Mels - Added MAC Address
 */
@NonNullByDefault({})
public class SamsungTvConfiguration {
    public static final String PROTOCOL = "protocol";
    public static final String PROTOCOL_NONE = "None";
    public static final String PROTOCOL_LEGACY = "Legacy";
    public static final String PROTOCOL_WEBSOCKET = "WebSocket";
    public static final String PROTOCOL_SECUREWEBSOCKET = "SecureWebSocket";
    public static final String HOST_NAME = "hostName";
    public static final String PORT = "port";
    public static final String MAC_ADDRESS = "macAddress";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String WEBSOCKET_TOKEN = "webSocketToken";
    public static final int PORT_DEFAULT_LEGACY = 55000;
    public static final int PORT_DEFAULT_WEBSOCKET = 8001;
    public static final int PORT_DEFAULT_SECUREWEBSOCKET = 8002;

    public String protocol;
    public String hostName;
    public String macAddress;
    public int port;
    public int refreshInterval;
    public String websocketToken;
}

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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for {@link org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler}.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Arjan Mels - Added MAC Address
 * @author Nick Waterton - added Smartthings, subscription, refactoring
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
    public static final String SUBSCRIPTION = "subscription";
    public static final String WEBSOCKET_TOKEN = "webSocketToken";
    public static final String SMARTTHINGS_API = "smartThingsApiKey";
    public static final String SMARTTHINGS_DEVICEID = "smartThingsDeviceId";
    public static final String SMARTTHINGS_SUBSCRIPTION = "smartThingsSubscription";
    public static final String ORIENTATION_KEY = "orientationKey";
    public static final int PORT_DEFAULT_LEGACY = 55000;
    public static final int PORT_DEFAULT_WEBSOCKET = 8001;
    public static final int PORT_DEFAULT_SECUREWEBSOCKET = 8002;

    public String protocol;
    public String hostName;
    public String macAddress;
    public int port;
    public int refreshInterval;
    public String webSocketToken;
    public String smartThingsApiKey;
    public String smartThingsDeviceId;
    public boolean subscription;
    public boolean smartThingsSubscription;
    public String orientationKey;

    public boolean isWebsocketProtocol() {
        return PROTOCOL_WEBSOCKET.equals(getProtocol()) || PROTOCOL_SECUREWEBSOCKET.equals(getProtocol());
    }

    public String getProtocol() {
        return Optional.ofNullable(protocol).orElse(PROTOCOL_NONE);
    }

    public String getHostName() {
        return Optional.ofNullable(hostName).orElse("");
    }

    public String getMacAddress() {
        return Optional.ofNullable(macAddress).filter(m -> m.length() == 17).orElse("");
    }

    public int getPort() {
        return Optional.ofNullable(port).orElse(PORT_DEFAULT_LEGACY);
    }

    public int getRefreshInterval() {
        return Optional.ofNullable(refreshInterval).orElse(1000);
    }

    public String getWebsocketToken() {
        return Optional.ofNullable(webSocketToken).orElse("");
    }

    public String getSmartThingsApiKey() {
        return Optional.ofNullable(smartThingsApiKey).orElse("");
    }

    public String getSmartThingsDeviceId() {
        return Optional.ofNullable(smartThingsDeviceId).orElse("");
    }

    public boolean getSubscription() {
        return Optional.ofNullable(subscription).orElse(false);
    }

    public boolean getSmartThingsSubscription() {
        return Optional.ofNullable(smartThingsSubscription).orElse(false);
    }

    public String getOrientationKey() {
        return Optional.ofNullable(orientationKey).orElse("");
    }
}

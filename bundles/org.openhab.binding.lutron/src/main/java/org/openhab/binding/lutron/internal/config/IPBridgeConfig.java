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
package org.openhab.binding.lutron.internal.config;

import java.util.Objects;

/**
 * Configuration settings for an {@link org.openhab.binding.lutron.internal.handler.IPBridgeHandler}.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added reconnect, heartbeat and discoveryFile parameters
 */
public class IPBridgeConfig {
    public String ipAddress;
    public String user;
    public String password;
    public String discoveryFile;
    public int reconnect;
    public int heartbeat;
    public int delay = 0;

    public boolean sameConnectionParameters(IPBridgeConfig config) {
        return Objects.equals(ipAddress, config.ipAddress) && Objects.equals(user, config.user)
                && Objects.equals(password, config.password) && (reconnect == config.reconnect)
                && (heartbeat == config.heartbeat) && (delay == config.delay);
    }
}

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
package org.openhab.binding.plugwiseha.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlugwiseHABridgeThingConfig} encapsulates all the configuration options for an instance of the
 * {@link org.openhab.binding.plugwiseha.internal.handler.PlugwiseHABridgeHandler}.
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 */
@NonNullByDefault
public class PlugwiseHABridgeThingConfig {

    private String host = "adam";

    private int port = 80;

    private String username = "smile";

    private String smileId = "";

    private int refresh = 15;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getsmileId() {
        return smileId;
    }

    public int getRefresh() {
        return refresh;
    }

    public boolean isValid() {
        return !host.isBlank() && !username.isBlank() && !smileId.isBlank();
    }

    @Override
    public String toString() {
        return "PlugwiseHABridgeThingConfig{host = " + host + ", port = " + port + ", username = " + username
                + ", smileId = *****, refresh = " + refresh + "}";
    }
}

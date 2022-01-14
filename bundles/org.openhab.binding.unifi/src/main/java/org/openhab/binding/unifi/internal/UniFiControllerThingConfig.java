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
package org.openhab.binding.unifi.internal;

import org.openhab.binding.unifi.internal.handler.UniFiControllerThingHandler;

/**
 * The {@link UniFiControllerThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiControllerThingHandler}.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiControllerThingConfig {

    private String host = "unifi";

    private int port = 8443;

    private String username = "";

    private String password = "";

    private int refresh = 10;

    private boolean unifios = false;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getRefresh() {
        return refresh;
    }

    public boolean isUniFiOS() {
        return unifios;
    }

    public boolean isValid() {
        return !host.isBlank() && !username.isBlank() && !password.isBlank();
    }

    @Override
    public String toString() {
        return "UniFiControllerConfig{host = " + host + ", port = " + port + ", username = " + username
                + ", password = *****, refresh = " + refresh + ", unifios = " + unifios + "}";
    }
}

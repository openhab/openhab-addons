/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifi.internal.handler.UniFiControllerThingHandler;

/**
 * The {@link UniFiControllerThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiControllerThingHandler}.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class UniFiControllerThingConfig {

    private String host = "unifi";

    private int port = 8443;

    private String username = "";

    private String password = "";

    private int refresh = 10;

    private int timeoutSeconds = 5;

    private boolean unifios = false;

    public String getHost() {
        return host;
    }

    private void setHost(final String host) {
        // method to avoid ide auto format mark the field as final
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    private void setPort(final int port) {
        // method to avoid ide auto format mark the field as final
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(final String username) {
        // method to avoid ide auto format mark the field as final
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(final String password) {
        // method to avoid ide auto format mark the field as final
        this.password = password;
    }

    public int getRefresh() {
        return refresh;
    }

    private void setRefresh(final int refresh) {
        // method to avoid ide auto format mark the field as final
        this.refresh = refresh;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isUniFiOS() {
        return unifios;
    }

    private void setUnifiOS(final boolean unifios) {
        // method to avoid ide auto format mark the field as final
        this.unifios = unifios;
    }

    public boolean isValid() {
        return !host.isBlank() && !username.isBlank() && !password.isBlank();
    }

    @Override
    public String toString() {
        return "UniFiControllerConfig{host = " + host + ", port = " + port + ", username = " + username
                + ", password = *****, refresh = " + refresh + ", timeout = " + timeoutSeconds + ", unifios = "
                + unifios + "}";
    }
}

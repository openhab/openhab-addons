/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link WlanThermoNanoConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoNanoConfiguration {

    /**
     * IP Address of WlanThermo.
     */
    private String ipAddress = "";

    /**
     * Username of WlanThermo user.
     */
    private @Nullable String username;

    /**
     * Password of WlanThermo user.
     */

    private @Nullable String password;

    /**
     * Polling interval
     */
    private int pollingInterval = 10;

    public String getIpAddress() {
        return ipAddress;
    }

    public URI getUri(String path) throws URISyntaxException {
        String uri = ipAddress;
        if (!uri.startsWith("http://")) {
            uri = "http://" + uri;
        }

        if (!path.startsWith("/") && !uri.endsWith("/")) {
            uri = uri + "/";
        }
        uri = uri + path;

        return new URI(uri);
    }

    public URI getUri() throws URISyntaxException {
        return getUri("");
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}

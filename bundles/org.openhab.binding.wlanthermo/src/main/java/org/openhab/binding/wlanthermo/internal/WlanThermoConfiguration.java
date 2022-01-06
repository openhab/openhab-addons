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
package org.openhab.binding.wlanthermo.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WlanThermoConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoConfiguration {

    /**
     * IP Address of WlanThermo.
     */
    private String ipAddress = "";

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

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}

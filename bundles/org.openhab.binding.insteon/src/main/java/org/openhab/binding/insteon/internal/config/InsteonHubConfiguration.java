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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InsteonHubConfiguration} is the configuration for an insteon hub bridge.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonHubConfiguration extends InsteonBridgeConfiguration {

    private String hostname = "";
    private int port = 25105;
    private String username = "";
    private String password = "";
    private int hubPollIntervalInMilliseconds = 1000;

    public String getHostname() {
        return hostname;
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

    public int getHubPollInterval() {
        return hubPollIntervalInMilliseconds;
    }

    @Override
    public String getId() {
        return hostname + ":" + port;
    }

    @Override
    public String toString() {
        String s = "";
        s += " hostname=" + hostname;
        s += " port=" + port;
        s += " username=" + username;
        s += " password=" + "*".repeat(password.length());
        s += " hubPollIntervalInMilliseconds=" + hubPollIntervalInMilliseconds;
        s += super.toString();
        return s;
    }
}

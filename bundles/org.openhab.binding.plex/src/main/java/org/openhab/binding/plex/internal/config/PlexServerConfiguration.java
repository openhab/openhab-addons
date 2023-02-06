/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.plex.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlexServerConfiguration} is the class used to match the
 * bridge configuration.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public class PlexServerConfiguration {
    public String host = "";
    public Integer portNumber = 32400;
    public String token = "";
    public Integer refreshRate = 5;
    public String username = "";
    public String password = "";
    public String scheme = "";

    // Channels
    public String currentPlayers = "";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return portNumber;
    }

    public void setPort(int port) {
        this.portNumber = port;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(Integer refreshRate) {
        this.refreshRate = refreshRate;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(String currentPlayers) {
        this.currentPlayers = currentPlayers;
    }
}

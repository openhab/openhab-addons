/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Representation of the spexor connection information
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public class Connection {

    /**
     * Options of a connection type
     *
     * @author Marc Fischer - Initial contribution
     *
     */
    public enum ConnectionType {
        MobileNetwork,
        Wifi
    }

    private String lastConnected = "2020-01-01T00:00:00Z";
    private boolean online;
    private ConnectionType connectionType = ConnectionType.Wifi;

    public String getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(String lastConnected) {
        this.lastConnected = lastConnected;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
}

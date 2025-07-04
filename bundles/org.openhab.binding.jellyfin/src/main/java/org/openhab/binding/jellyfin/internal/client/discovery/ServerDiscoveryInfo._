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
package org.openhab.binding.jellyfin.internal.client.discovery;

import java.util.Objects;

/**
 * Information about a discovered Jellyfin server
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class ServerDiscoveryInfo {

    private final String id;
    private final String name;
    private final String address;

    /**
     * Create a new server discovery info instance
     *
     * @param id The server ID
     * @param name The server name
     * @param address The server address
     */
    public ServerDiscoveryInfo(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    /**
     * Get the server ID
     *
     * @return The server ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the server name
     *
     * @return The server name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the server address
     *
     * @return The server address
     */
    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ServerDiscoveryInfo that = (ServerDiscoveryInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address);
    }

    @Override
    public String toString() {
        return "ServerDiscoveryInfo{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", address='" + address + '\''
                + '}';
    }
}

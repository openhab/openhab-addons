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
package org.openhab.binding.jellyfin.internal.client.model;

import java.util.Objects;

/**
 * The client information is used to identify the client.
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class ClientInfo {

    private final String name;
    private final String version;

    /**
     * Create a new ClientInfo instance
     *
     * @param name Name of the client, this should normally not change
     * @param version The version of the client
     */
    public ClientInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Get the name of the client
     *
     * @return The client name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the version of the client
     *
     * @return The client version
     */
    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ClientInfo that = (ClientInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        return "ClientInfo{" + "name='" + name + '\'' + ", version='" + version + '\'' + '}';
    }
}

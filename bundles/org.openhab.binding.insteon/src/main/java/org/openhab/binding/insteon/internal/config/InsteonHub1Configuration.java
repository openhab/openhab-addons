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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link InsteonHub1Configuration} is the configuration for an insteon hub 1 bridge.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonHub1Configuration extends InsteonBridgeConfiguration {

    private String hostname = "";
    private int port = 9761;

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
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
        s += super.toString();
        return s;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InsteonHub1Configuration other = (InsteonHub1Configuration) obj;
        return hostname.equals(other.hostname) && port == other.port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hostname.hashCode();
        result = prime * result + port;
        return result;
    }

    public static InsteonHub1Configuration valueOf(String hostname, @Nullable Integer port) {
        InsteonHub1Configuration config = new InsteonHub1Configuration();
        config.hostname = hostname;
        if (port != null) {
            config.port = port;
        }
        return config;
    }
}

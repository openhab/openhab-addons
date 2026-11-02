/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atagone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thing configuration parameters for the ATAG ONE thermostat.
 * Field names must match the parameter {@code name} attributes in {@code config.xml}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneConfiguration {

    public static final String CONNECTION_AUTO = "auto";
    public static final String CONNECTION_LOCAL = "local";
    public static final String CONNECTION_REMOTE = "remote";

    public enum ConnectionType {
        LOCAL,
        REMOTE
    }

    public String connection = CONNECTION_AUTO;
    public String hostname = "";
    public int port = 10000;
    public int refreshInterval = 30;
    public String clientId = "";
    public String email = "";
    public String password = "";

    /**
     * @return whether exactly one of email and password is set while the connection is chosen automatically
     */
    public boolean hasIncompleteCredentials() {
        return CONNECTION_AUTO.equals(connection) && email.isBlank() != password.isBlank();
    }

    /**
     * @return whether the portal connection also reads the local API for the report details the portal lacks
     */
    public boolean usesLocalDetails() {
        return CONNECTION_AUTO.equals(connection) && !hostname.isBlank()
                && resolveConnectionType() == ConnectionType.REMOTE;
    }

    /**
     * @return the connection to use, or {@code null} if the settings it needs are missing
     */
    public @Nullable ConnectionType resolveConnectionType() {
        boolean hasHostname = !hostname.isBlank();
        boolean hasCredentials = !email.isBlank() && !password.isBlank();
        switch (connection) {
            case CONNECTION_LOCAL:
                return hasHostname ? ConnectionType.LOCAL : null;
            case CONNECTION_REMOTE:
                return hasCredentials ? ConnectionType.REMOTE : null;
            default:
                if (hasCredentials) {
                    return ConnectionType.REMOTE;
                }
                return hasHostname ? ConnectionType.LOCAL : null;
        }
    }
}

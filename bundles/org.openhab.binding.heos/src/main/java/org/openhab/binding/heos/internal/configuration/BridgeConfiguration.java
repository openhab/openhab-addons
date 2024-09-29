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
package org.openhab.binding.heos.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration wrapper for bridge configuration
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public class BridgeConfiguration {
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * Network address of the HEOS bridge
     */
    public String ipAddress = "";

    /**
     * Username for login to the HEOS account.
     */
    public @Nullable String username;

    /**
     * Password for login to the HEOS account
     */
    public @Nullable String password;

    /**
     * The time in seconds for the HEOS Heartbeat (default = 60 s)
     */
    public int heartbeat;
}

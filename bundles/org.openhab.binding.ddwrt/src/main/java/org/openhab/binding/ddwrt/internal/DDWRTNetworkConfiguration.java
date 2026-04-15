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
package org.openhab.binding.ddwrt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DDWRTNetworkConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTNetworkConfiguration {

    /**
     * Comma-separated list of hostnames or IP addresses of the DD-WRT devices to connect to.
     * Optionally each entry can use the format user@host:port to override the default user and port.
     */
    public String hostnames = "";

    /**
     * Default user name used to authenticate with the DD-WRT devices.
     * Precedence: user@ in hostnames entry > this parameter > ~/.ssh/config > system username.
     */
    public String user = "root";

    /**
     * When true, ignore the user parameter and let the SSH client resolve the username
     * from ~/.ssh/config or fall back to the system username.
     */
    public boolean useSystemUser = false;

    /**
     * Password used to authenticate with the DD-WRT device.
     */
    public String password = "";

    /**
     * Network port used to connect to the DD-WRT device.
     */
    public int port = 0;

    /**
     * Refresh interval, in seconds, for polling the DD-WRT device.
     */
    public int refreshInterval = 3;
}

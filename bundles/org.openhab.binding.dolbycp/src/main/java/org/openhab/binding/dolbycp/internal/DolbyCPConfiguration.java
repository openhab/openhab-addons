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
package org.openhab.binding.dolbycp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DolbyCPConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class DolbyCPConfiguration {
    /**
     * Hostname or IP address of the CP750 device
     */
    public String hostname = "";

    /**
     * TCP Port to connect to (default: 61408)
     */
    public int port = 61408;

    /**
     * Interval in seconds to update channels
     */
    public int refreshInterval = 5;

    /**
     * Reconnect interval in seconds after a broken TCP connection
     */
    public int reconnectInterval = 10;
}

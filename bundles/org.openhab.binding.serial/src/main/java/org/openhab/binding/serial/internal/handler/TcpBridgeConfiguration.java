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
package org.openhab.binding.serial.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class describing the {@link TcpBridgeConfiguration} configuration
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class TcpBridgeConfiguration extends CommonBridgeConfiguration {
    /**
     * IP address or hostname
     */
    public String address = "";

    /**
     * TCP Port
     */
    public int port = 0;

    /**
     * Socket timeout in seconds
     */
    public int timeout = 0;

    /**
     * Keep Alive
     */
    public boolean keepAlive = false;

    /**
     * Reconnection Interval in seconds
     */
    public int reconnectInterval = 10;

    @Override
    public String toString() {
        return "TcpBridgeConfiguration [Address=" + address + ", Port=" + port + ", timeout=" + timeout + ", keepAlive="
                + keepAlive + ", reconnectInterval=" + reconnectInterval + ", charset=" + charset + ", eolPattern="
                + eolPattern + "]";
    }
}

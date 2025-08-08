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
 * Class describing the {@link TcpServerBridgeConfiguration} configuration
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class TcpServerBridgeConfiguration extends CommonBridgeConfiguration {
    /**
     * IP address or hostname
     */
    public String bindAddress = "0.0.0.0";

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

    @Override
    public String toString() {
        return "TcpServerBridgeConfiguration [BindAddress=" + bindAddress + ", Port=" + port + ", timeout=" + timeout
                + ", keepAlive=" + keepAlive + ", charset=" + charset + ", eolPattern=" + eolPattern + "]";
    }
}

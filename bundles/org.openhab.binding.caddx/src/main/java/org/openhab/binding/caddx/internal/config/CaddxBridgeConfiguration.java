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
package org.openhab.binding.caddx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caddx.internal.CaddxProtocol;

/**
 * Configuration class for the Caddx RS232 Serial interface bridge.
 *
 * @author Georgios Moutsos - Initial contribution
 */

@NonNullByDefault
public class CaddxBridgeConfiguration {

    // Caddx Bridge Thing constants
    public static final String PROTOCOL = "protocol";
    public static final String SERIAL_PORT = "serialPort";
    public static final String BAUD = "baud";
    public static final String MAX_ZONE_NUMBER = "maxZoneNumber";
    public static final String IGNORE_ZONE_STATUS_TRANSITIONS = "ignoreZoneStatusTransitions";

    private CaddxProtocol protocol = CaddxProtocol.Binary;
    private @Nullable String serialPort;
    private int baudrate = 9600;
    private int maxZoneNumber = 16;
    private boolean ignoreZoneStatusTransitions = false;

    public CaddxProtocol getProtocol() {
        return protocol;
    }

    public @Nullable String getSerialPort() {
        return serialPort;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public int getMaxZoneNumber() {
        return maxZoneNumber;
    }

    public boolean isIgnoreZoneStatusTransitions() {
        return ignoreZoneStatusTransitions;
    }
}

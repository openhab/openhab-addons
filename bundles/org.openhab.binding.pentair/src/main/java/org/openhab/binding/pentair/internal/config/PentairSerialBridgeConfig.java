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
package org.openhab.binding.pentair.internal.config;

/**
 * Configuration parameters for Serial Bridge
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairSerialBridgeConfig {
    /** path or name of serial port, usually /dev/ttyUSB0 format for linux/mac, COM1 for windows */
    public String serialPort;
    /** ID to use when sending commands on the Pentair RS485 bus. */
    public Integer id;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ serialPort=" + serialPort + ", id=" + id + "}";
    }
}

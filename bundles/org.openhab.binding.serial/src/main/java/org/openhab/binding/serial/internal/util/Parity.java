/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal.util;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.serial.SerialPort;

/**
 * Enum to convert config parity value to serial port value
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public enum Parity {
    NONE("N", SerialPort.PARITY_NONE),
    ODD("O", SerialPort.PARITY_ODD),
    EVEN("E", SerialPort.PARITY_EVEN),
    MARK("M", SerialPort.PARITY_MARK),
    SPACE("S", SerialPort.PARITY_SPACE);

    final String configValue;
    final int serialPortValue;

    private Parity(final String configValue, final int serialPortValue) {
        this.configValue = configValue;
        this.serialPortValue = serialPortValue;
    }

    /**
     * Return the serial port value
     *
     * @return the serial port value
     */
    public int getSerialPortValue() {
        return serialPortValue;
    }

    /**
     * Return the enum value from the config value
     *
     * @param configValue the config value
     * @return the enum value
     */
    public static Parity fromConfig(final String configValue) {
        return Arrays.asList(values()).stream().filter(p -> p.configValue.equals(configValue)).findFirst().orElse(NONE);
    }
}

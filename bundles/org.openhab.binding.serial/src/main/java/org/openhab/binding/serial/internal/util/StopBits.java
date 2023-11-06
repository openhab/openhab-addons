/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Enum to convert config stopBits value to serial port value
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public enum StopBits {
    STOPBITS_1("1", SerialPort.STOPBITS_1),
    STOPBITS_1_5("1.5", SerialPort.STOPBITS_1_5),
    STOPBITS_2("2", SerialPort.STOPBITS_2);

    final String configValue;
    final int serialPortValue;

    private StopBits(final String configValue, final int serialPortValue) {
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
    public static StopBits fromConfig(final String configValue) {
        return Arrays.asList(values()).stream().filter(p -> p.configValue.equals(configValue)).findFirst()
                .orElse(STOPBITS_1);
    }
}

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
package org.openhab.binding.smartmeter.internal.helper;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.serial.SerialPort;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public enum SerialParameter {

    _8N1(SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE),
    _7N1(SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE),
    _7O1(SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD),
    _7E1(SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);

    private int databits;
    private int stopbits;
    private int parity;

    private SerialParameter(int databits, int stopbits, int parity) {
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }

    public int getDatabits() {
        return this.databits;
    }

    public int getStopbits() {
        return stopbits;
    }

    public int getParity() {
        return parity;
    }

    @Override
    public String toString() {
        return name().substring(1);
    }

    /**
     * Returns the enum constant for the serial parameter string.
     * The parameters must be in format 'StartbitsParityStopbits'
     * e.g. '7N1', '8N1'
     *
     * @param params
     * @return The found {@link SerialParameter} or {@link SerialParameter#_8N1} if not found
     */
    public static SerialParameter fromString(String params) {
        try {
            return valueOf("_" + params.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SerialParameter._8N1;
        }
    }
}

/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal;

import org.openhab.core.io.transport.serial.SerialPort;

/**
 * Class describing the serial bridge user configuration
 *
 * @author Mike Major - Initial contribution
 */
public class SerialBridgeConfiguration {
    /**
     * Serial port name
     */
    public String serialPort;

    /**
     * Serial port baud rate
     */
    public int baudrate;

    /**
     * Serial port data bits
     */
    public int databits;

    /**
     * Serial port parity
     */
    public String parity;

    /**
     * Serial port stop bits
     */
    public String stopbits;

    /**
     * Charset
     */
    public String charset;

    @Override
    public String toString() {
        return "SerialBridgeConfiguration [serialPort=" + serialPort + ", Baudrate=" + baudrate + ", Databits="
                + databits + ", Parity=" + parity + ", Stopbits=" + stopbits + "]";
    }

    public int getParityAsInt() {
        int parityInt;

        switch (parity) {
            case "N":
                parityInt = SerialPort.PARITY_NONE;
                break;
            case "O":
                parityInt = SerialPort.PARITY_ODD;
                break;
            case "E":
                parityInt = SerialPort.PARITY_EVEN;
                break;
            case "M":
                parityInt = SerialPort.PARITY_MARK;
                break;
            case "S":
                parityInt = SerialPort.PARITY_SPACE;
                break;
            default:
                parityInt = SerialPort.PARITY_NONE;
                break;
        }

        return parityInt;
    }

    public int getStopBitsAsInt() {
        int stopbitsAsInt;

        switch (stopbits) {
            case "1":
                stopbitsAsInt = SerialPort.STOPBITS_1;
                break;
            case "1.5":
                stopbitsAsInt = SerialPort.STOPBITS_1_5;
                break;
            case "2":
                stopbitsAsInt = SerialPort.STOPBITS_2;
                break;
            default:
                stopbitsAsInt = SerialPort.STOPBITS_1;
                break;
        }

        return stopbitsAsInt;
    }
}

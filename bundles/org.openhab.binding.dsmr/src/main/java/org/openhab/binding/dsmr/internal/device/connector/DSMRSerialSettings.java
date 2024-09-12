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
package org.openhab.binding.dsmr.internal.device.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConfiguration;
import org.openhab.core.io.transport.serial.SerialPort;

/**
 * Class for storing port settings
 * This class does store 4 serial parameters (baudrate, databits, parity, stopbits)
 * for use in {@link DSMRSerialConnector}.
 *
 * This class can also convert a string setting
 * ({@code <speed> <databits><parity><stopbits>})
 * to a {@link DSMRSerialSettings} object (e.g. 115200 8N1)
 *
 * @author M. Volaart - Initial contribution
 * 
 * @author Hilbrand Bouwkamp - Removed auto detecting state checking from this class.
 */
@NonNullByDefault
public class DSMRSerialSettings {

    /**
     * Fixed settings for high speed communication (DSMR V4 and up)
     */
    public static final DSMRSerialSettings HIGH_SPEED_SETTINGS = new DSMRSerialSettings(115200, SerialPort.DATABITS_8,
            SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);

    /**
     * Fixed settings for low speed communication (DSMR V3 and down)
     */
    public static final DSMRSerialSettings LOW_SPEED_SETTINGS = new DSMRSerialSettings(9600, SerialPort.DATABITS_7,
            SerialPort.PARITY_EVEN, SerialPort.STOPBITS_1);

    /**
     * Serial port baudrate
     */
    private final int baudrate;

    /**
     * Serial port databits
     */
    private final int databits;

    /**
     * Serial port parity
     */
    private final int parity;

    /**
     * Serial port stop bits
     */
    private final int stopbits;

    /**
     * Construct a new {@link DSMRSerialSettings} object.
     *
     * @param baudrate baudrate of the port
     * @param databits no data bits to use (use SerialPort.DATABITS_* constant)
     * @param parity parity to use (use SerialPort.PARITY_* constant)
     * @param stopbits no stopbits to use (use SerialPort.STOPBITS_* constant)
     */
    private DSMRSerialSettings(int baudrate, int databits, int parity, int stopbits) {
        this.baudrate = baudrate;
        this.databits = databits;
        this.parity = parity;
        this.stopbits = stopbits;
    }

    /**
     * Returns the baudrate
     *
     * @return baudrate setting
     */
    public int getBaudrate() {
        return baudrate;
    }

    /**
     * Returns the number of data bits
     *
     * @return databits setting
     */
    public int getDataBits() {
        return databits;
    }

    /**
     * Returns the parity setting
     *
     * @return parity setting
     */
    public int getParity() {
        return parity;
    }

    /**
     * Returns the number of stop bits
     *
     * @return stop bits setting
     */
    public int getStopbits() {
        return stopbits;
    }

    @Override
    public String toString() {
        String toString = "Baudrate:" + baudrate + ", databits:" + databits;

        switch (parity) {
            case SerialPort.PARITY_EVEN:
                toString += ", parity:even";
                break;
            case SerialPort.PARITY_NONE:
                toString += ", parity:none";
                break;
            case SerialPort.PARITY_ODD:
                toString += ", parity:odd";
                break;
            default:
                toString += ", parity:<unknown>";
                break;
        }
        switch (stopbits) {
            case SerialPort.STOPBITS_1:
                toString += ", stopbits:1";
                break;
            case SerialPort.STOPBITS_1_5:
                toString += ", stopbits:1.5";
                break;
            case SerialPort.STOPBITS_2:
                toString += ", stopbits:2";
                break;
            default:
                toString += ", stopbits:<unknown>";
                break;
        }
        return toString;
    }

    /**
     * Returns the manual entered port setting if all configuration fields have a value (not null).
     *
     * @param deviceConfiguration manual entered device configuration
     * @return serial configuration.
     */
    public static DSMRSerialSettings getPortSettingsFromConfiguration(DSMRDeviceConfiguration deviceConfiguration) {
        int baudrate = deviceConfiguration.baudrate;
        int databits = deviceConfiguration.databits;

        int parity;
        switch (deviceConfiguration.parity) {
            case "E":
                parity = SerialPort.PARITY_EVEN;
                break;
            case "O":
                parity = SerialPort.PARITY_ODD;
                break;
            case "N":
                parity = SerialPort.PARITY_NONE;
                break;
            default:
                parity = -1;
                break;
        }

        int stopbits;
        switch (deviceConfiguration.stopbits) {
            case "1":
                stopbits = SerialPort.STOPBITS_1;
                break;
            case "1.5":
                stopbits = SerialPort.STOPBITS_1_5;
                break;
            case "2":
                stopbits = SerialPort.STOPBITS_2;
                break;
            default:
                stopbits = -1;
                break;
        }
        return new DSMRSerialSettings(baudrate, databits, parity, stopbits);
    }
}

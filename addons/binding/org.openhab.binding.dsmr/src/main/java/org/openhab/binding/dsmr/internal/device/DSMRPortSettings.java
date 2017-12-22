/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import gnu.io.SerialPort;

/**
 * Class for storing port settings
 * This class does store 4 serial parameters (baudrate, databits, parity, stopbits)
 * for use in DSMRPort.
 *
 * This class can also convert a string setting (<speed> <databits><parity><stopbits>)
 * to a DSMRPortSettings object (e.g. 115200 8N1)
 *
 * @author M. Volaart - Initial contribution
 */
public class DSMRPortSettings {
    /**
     * Fixed settings for high speed communication (DSMR V4 and up)
     */
    public static final DSMRPortSettings HIGH_SPEED_SETTINGS = new DSMRPortSettings(115200, SerialPort.DATABITS_8,
            SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);

    /**
     * Fixed settings for low speed communication (DSMR V3 and down)
     */
    public static final DSMRPortSettings LOW_SPEED_SETTINGS = new DSMRPortSettings(9600, SerialPort.DATABITS_7,
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
     * Construct a new PortSpeed object
     *
     * @param baudrate
     *            baudrate of the port
     * @param databits
     *            no data bits to use (use SerialPort.DATABITS_* constant)
     * @param parity
     *            parity to use (use SerialPort.PARITY_* constant)
     * @param stopbits
     *            no stopbits to use (use SerialPort.STOPBITS_* constant)
     */
    public DSMRPortSettings(int baudrate, int databits, int parity, int stopbits) {
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
            case SerialPort.PARITY_MARK:
                toString += ", parity:mark";
                break;
            case SerialPort.PARITY_NONE:
                toString += ", parity:none";
                break;
            case SerialPort.PARITY_ODD:
                toString += ", parity:odd";
                break;
            case SerialPort.PARITY_SPACE:
                toString += ", parity:space";
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
     *
     * @param portSettings
     * @return
     */
    public static DSMRPortSettings getPortSettingsFromConfiguration(DSMRDeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration == null || deviceConfiguration.serialPortBaudrate == null
                || deviceConfiguration.serialPortDatabits == null || deviceConfiguration.serialPortParity == null
                || deviceConfiguration.serialPortStopbits == null) {
            return null;
        }
        int baudrate = deviceConfiguration.serialPortBaudrate;
        int databits = deviceConfiguration.serialPortDatabits;
        int parity;
        int stopbits;

        if (deviceConfiguration.serialPortParity.equals("E")) {
            parity = SerialPort.PARITY_EVEN;
        } else if (deviceConfiguration.serialPortParity.equals("O")) {
            parity = SerialPort.PARITY_ODD;
        } else if (deviceConfiguration.serialPortParity.equals("N")) {
            parity = SerialPort.PARITY_NONE;
        } else {
            return null;
        }

        if (deviceConfiguration.serialPortStopbits.equals("1")) {
            stopbits = SerialPort.STOPBITS_1;
        } else if (deviceConfiguration.serialPortStopbits.equals("1.5")) {
            stopbits = SerialPort.STOPBITS_1_5;
        } else if (deviceConfiguration.serialPortStopbits.equals("2")) {
            stopbits = SerialPort.STOPBITS_2;
        } else {
            return null;
        }
        return new DSMRPortSettings(baudrate, databits, parity, stopbits);
    }
}

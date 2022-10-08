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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;

/**
 * The Commands supported by the gateway.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum Command {

    /**
     * read current data，reply data size is 2bytes.
     */
    CMD_WS980_LIVEDATA((byte) 0x0b, 2),

    /**
     * send SSID and Password to WIFI module
     */
    CMD_WRITE_SSID((byte) 0x11, 1),

    /**
     * UDP cast for device echo，answer back data size is 2 Bytes
     */
    CMD_BROADCAST((byte) 0x12, 2),

    /**
     * read aw.net setting
     */
    CMD_READ_ECOWITT((byte) 0x1E, 1),

    /**
     * write back awt.net setting
     */
    CMD_WRITE_ECOWITT((byte) 0x1F, 1),

    /**
     * read Wunderground setting
     */
    CMD_READ_WUNDERGROUND((byte) 0x20, 1),

    /**
     * write back Wunderground setting
     */
    CMD_WRITE_WUNDERGROUND((byte) 0x21, 1),

    /**
     * read WeatherObservationsWebsite setting
     */
    CMD_READ_WOW((byte) 0x22, 1),

    /**
     * write back WeatherObservationsWebsite setting
     */
    CMD_WRITE_WOW((byte) 0x23, 1),

    /**
     * read Weathercloud setting
     */
    CMD_READ_WEATHERCLOUD((byte) 0x24, 1),

    /**
     * write back Weathercloud setting
     */
    CMD_WRITE_WEATHERCLOUD((byte) 0x25, 1),

    /**
     * read MAC address
     */
    CMD_READ_SATION_MAC((byte) 0x26, 1),

    /**
     * read Customized sever setting
     */
    CMD_READ_CUSTOMIZED((byte) 0x2A, 1),

    /**
     * write back Customized sever setting
     */
    CMD_WRITE_CUSTOMIZED((byte) 0x2B, 1),

    /**
     * firmware upgrade
     */
    CMD_WRITE_UPDATE((byte) 0x43, 1),

    /**
     * read current firmware version number
     */
    CMD_READ_FIRMWARE_VERSION((byte) 0x50, 1),

    CMD_READ_USR_PATH((byte) 0x51, 1),

    CMD_WRITE_USR_PATH((byte) 0x52, 1),

    // the following command is only valid for GW1000, WH2650 and wn1900

    /**
     * read current data，reply data size is 2bytes.
     */
    CMD_GW1000_LIVEDATA((byte) 0x27, 2),

    /**
     * read Soilmoisture Sensor calibration parameters
     */
    CMD_GET_SOILHUMIAD((byte) 0x28, 1),

    /**
     * write back Soilmoisture Sensor calibration parameters
     */
    CMD_SET_SOILHUMIAD((byte) 0x29, 1),

    /**
     * read multi channel sensor offset value
     */
    CMD_GET_MulCH_OFFSET((byte) 0x2C, 1),

    /**
     * write back multi channel sensor OFFSET value
     */
    CMD_SET_MulCH_OFFSET((byte) 0x2D, 1),

    /**
     * read PM2.5OFFSET calibration data
     */
    CMD_GET_PM25_OFFSET((byte) 0x2E, 1),

    /**
     * writeback PM2.5OFFSET calibration data
     */
    CMD_SET_PM25_OFFSET((byte) 0x2F, 1),

    /**
     * read system info
     */
    CMD_READ_SSSS((byte) 0x30, 1),

    /**
     * write back system info
     */
    CMD_WRITE_SSSS((byte) 0x31, 1),

    /**
     * read rain data
     */
    CMD_READ_RAINDATA((byte) 0x34, 1),

    /**
     * write back rain data
     */
    CMD_WRITE_RAINDATA((byte) 0x35, 1),

    /**
     * read rain gain
     */
    CMD_READ_GAIN((byte) 0x36, 1),

    /**
     * write back rain gain
     */
    CMD_WRITE_GAIN((byte) 0x37, 1),

    /**
     * read sensor set offset calibration value
     */
    CMD_READ_CALIBRATION((byte) 0x38, 1),

    /**
     * write back sensor set offset value
     */
    CMD_WRITE_CALIBRATION((byte) 0x39, 1),

    /**
     * read Sensors ID
     */
    CMD_READ_SENSOR_ID((byte) 0x3A, 1),

    /**
     * write back Sensors ID
     */
    CMD_WRITE_SENSOR_ID((byte) 0x3B, 1),

    /**
     * this is reserved for newly added sensors
     */
    CMD_READ_SENSOR_ID_NEW((byte) 0x3C, 2),

    /**
     * system restart
     */
    CMD_WRITE_REBOOT((byte) 0x40, 1),

    /**
     * reset to default
     */
    CMD_WRITE_RESET((byte) 0x41, 1),

    CMD_READ_CUSTOMIZED_PATH((byte) 0x51, 1),

    CMD_WRITE_CUSTOMIZED_PATH((byte) 0x52, 1),

    /**
     * CO2 OFFSET
     */
    CMD_GET_CO2_OFFSET((byte) 0x53, 1),

    /**
     * CO2 OFFSET
     */
    CMD_SET_CO2_OFFSET((byte) 0x54, 1),

    /**
     * read rain reset time
     */
    CMD_READ_RSTRAIN_TIME((byte) 0x55, 1),

    /**
     * write back rain reset time
     */
    CMD_WRITE_RSTRAIN_TIME((byte) 0x56, 1),

    /**
     * read rain data including piezo (wh90)
     */
    CMD_READ_RAIN((byte) 0x57, 2),

    /**
     * write rain data
     */
    CMD_WRITE_RAIN((byte) 0x58, 1);

    private final byte code;
    private final int sizeBytes;

    Command(byte code, int sizeBytes) {
        this.code = code;
        this.sizeBytes = sizeBytes;
    }

    public byte[] getPayload() {
        byte size = 3; // + rest of payload / not yet implemented
        return new byte[] { (byte) 0xff, (byte) 0xff, code, size, (byte) (code + size) };
    }

    public byte[] getPayloadAlternative() {
        if (sizeBytes == 2) {
            return new byte[] { (byte) 0xff, (byte) 0xff, code, 0, (byte) (sizeBytes + 2),
                    (byte) ((code + sizeBytes + 2) % 0xff) };
        }
        byte size = 3;
        return new byte[] { (byte) 0xff, (byte) 0xff, code, size, (byte) ((code + size) % 0xff) };
    }

    public boolean isHeaderValid(byte[] data) {
        if (data.length < 4 + sizeBytes) {
            return false;
        }
        return data[0] == (byte) 0xff && data[1] == (byte) 0xff && data[2] == code;
    }

    public boolean isResponseValid(byte[] data) {
        return isHeaderValid(data) && Utils.validateChecksum(data, sizeBytes);
    }
}

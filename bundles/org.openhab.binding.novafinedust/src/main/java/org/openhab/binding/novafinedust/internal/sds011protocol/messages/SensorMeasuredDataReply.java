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
package org.openhab.binding.novafinedust.internal.sds011protocol.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Class containing the actual measured values from the sensor
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class SensorMeasuredDataReply extends SensorReply {
    private final byte pm25lowByte;
    private final byte pm25highByte;
    private final byte pm10lowByte;
    private final byte pm10highByte;

    /**
     * Create a new instance by parsing the given 10 bytes.
     *
     */
    public SensorMeasuredDataReply(byte[] bytes) {
        super(bytes);
        pm25lowByte = bytes[2];
        pm25highByte = bytes[3];
        pm10lowByte = bytes[4];
        pm10highByte = bytes[5];
    }

    /**
     * Check if data is valid by checking header, commanderNo, messageTail and checksum.
     */
    public boolean isValidData() {
        return header == Constants.MESSAGE_START && commandID == (byte) 0xC0 && messageTail == Constants.MESSAGE_END
                && checksum == calculateChecksum();
    }

    /**
     * Get the measured PM2.5 value
     *
     * @return the measured PM2.5 value
     */
    public float getPm25() {
        int shiftedValue = (pm25highByte << 8 & 0xFF) | pm25lowByte & 0xFF;
        return ((float) shiftedValue) / 10;
    }

    /**
     * Get the measured PM10 value
     *
     * @return the measured PM10 value
     */
    public float getPm10() {
        int shiftedValue = (pm10highByte << 8 & 0xFF) | pm10lowByte & 0xFF;
        return ((float) shiftedValue) / 10;
    }

    @Override
    public String toString() {
        return String.format(
                "SensorMeasuredDataReply: [valid=%s, PM 2.5=%.1f, PM 10=%.1f, sourceDevice=%s, pm25lowHigh=(%s) pm10lowHigh=(%s)]",
                isValidData(), getPm25(), getPm10(), HexUtils.bytesToHex(new byte[] { deviceID[0], deviceID[1] }),
                HexUtils.bytesToHex(new byte[] { pm25lowByte, pm25highByte }),
                HexUtils.bytesToHex(new byte[] { pm10lowByte, pm10highByte }));
    }
}

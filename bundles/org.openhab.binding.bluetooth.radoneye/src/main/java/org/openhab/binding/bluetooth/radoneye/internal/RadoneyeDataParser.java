/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.radoneye.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RadoneyeDataParser} is responsible for parsing data from Wave Plus device format.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeDataParser {
    public static final String RADON = "radon";

    public static final String DECAY = "decay";

    private static final int EXPECTED_DATA_LEN_V1 = 20;
    private static final int EXPECTED_DATA_LEN_V2 = 12;

    private RadoneyeDataParser() {
    }

    public static Map<String, Number> parseRd200Data(int fwVersion, byte[] data) throws RadoneyeParserException {
        switch (fwVersion) {
            case 1:
                if (data.length != EXPECTED_DATA_LEN_V1) {
                    throw new RadoneyeParserException(String.format("Illegal data structure length '%d'", data.length));
                }

                byte[] radonArray = subArray(data, 2, 6);
                return Map.of(RADON, new BigDecimal(readFloat(radonArray) * 37));
            case 2:
                if (data.length != EXPECTED_DATA_LEN_V2) {
                    throw new RadoneyeParserException(String.format("Illegal data structure length '%d'", data.length));
                }

                return Map.of(RADON, intFromBytes(data[2], data[3]), DECAY, intFromBytes(data[10], data[11]));
            default:
                throw new UnsupportedOperationException("fwVersion: " + fwVersion + " is not implemented");
        }
    }

    private static int intFromBytes(byte lowByte, byte highByte) {
        return (highByte & 0xFF) << 8 | (lowByte & 0xFF);
    }

    // Little endian
    private static int fromByteArrayLE(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= (bytes[i] & 0xFF) << (8 * i);
        }
        return result;
    }

    private static float readFloat(byte[] bytes) {
        int i = fromByteArrayLE(bytes);
        return Float.intBitsToFloat(i);
    }

    private static byte[] subArray(byte[] array, int beg, int end) {
        return Arrays.copyOfRange(array, beg, end + 1);
    }
}

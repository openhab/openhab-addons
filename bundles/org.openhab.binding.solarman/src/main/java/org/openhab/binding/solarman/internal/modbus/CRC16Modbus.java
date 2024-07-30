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
package org.openhab.binding.solarman.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class CRC16Modbus {
    private static final int[] CRC_TABLE = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int crc = i;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc = crc >>> 1;
                }
            }
            CRC_TABLE[i] = crc;
        }
    }

    public static int calculate(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc = (crc >>> 8) ^ CRC_TABLE[(crc ^ b) & 0xFF];
        }
        return crc;
    }
}

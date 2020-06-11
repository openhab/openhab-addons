/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxUtil} Utility class for different calculations / manipulations of data in the model and
 * communicators.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxUtil {

    private final static Logger logger = LoggerFactory.getLogger(ParadoxUtil.class);

    public static byte calculateChecksum(byte[] payload) {
        int result = 0;
        for (byte everyByte : payload) {
            result += everyByte;
        }

        return (byte) (result % 256);
    }

    public static byte getBit(int value, int bitNumber) {
        return (byte) ((value >> bitNumber) & 1);
    }

    public static boolean isBitSet(int value, int bitNumber) {
        return ((value >> bitNumber) & 1) == 1;
    }

    public static void printPacket(String description, byte[] array) {
        if (logger.isTraceEnabled()) {
            logger.trace("Packet payload size: {}", array[1]);
            printByteArray(description, array, array[1] + 16);
        }
    }

    private static void printByteArray(String description, byte[] array, int length) {
        if (description != null && !description.isEmpty()) {
            logger.trace("{}", description);
        }
        int countBytes = 0;
        String result = "";
        for (int index = 0; index < length; index++) {
            countBytes++;
            String st = String.format("0x%02X,\t", array[index]);
            result += st;
            if (countBytes > 7) {
                logger.trace(result);
                countBytes = 0;
                result = "";
                continue;
            }
        }
        if (!result.isEmpty()) {
            logger.trace(result);
        }

    }

    public static byte setBit(byte byteValue, int i, int j) {
        if (j == 1) {
            return (byte) (byteValue | (1 << i));
        } else {
            return (byte) (byteValue & ~(1 << i));
        }
    }

    public static byte getHighNibble(byte value) {
        return (byte) ((value & 0xF0) >> 4);
    }

    public static byte getLowNibble(byte value) {
        return (byte) (value & 0x0F);
    }

    public static byte[] mergeByteArrays(byte[]... arrays) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (byte[] array : arrays) {
                outputStream.write(array);
            }
            byte[] byteArray = outputStream.toByteArray();
            return byteArray;
        } catch (IOException e) {
            logger.warn("Exception merging arrays:", e);
            return new byte[0];
        }
    }

    public static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
    }

    public static byte[] shortToByteArray(short value) {
        return ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.BIG_ENDIAN).putShort(value).array();
    }

    public static byte[] stringToBCD(String pcPassword) {
        return stringToBCD(pcPassword, 4);
    }

    public static byte[] stringToBCD(String pcPassword, int numberOfDigits) {
        byte[] result = new byte[numberOfDigits / 2];
        for (int i = 0, j = 0; i < 2; i++, j += 2) {
            String substring = pcPassword.substring(j, j + 1);
            int parseInt = Integer.parseInt(substring);
            result[i] = (byte) ((parseInt & 0x0F) << 4);

            substring = pcPassword.substring(j + 1, j + 2);
            parseInt = Integer.parseInt(substring);
            result[i] |= (byte) (parseInt & 0x0F);
        }
        return result;
    }
}

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
package org.openhab.binding.solarman.internal.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class LogParser {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter request frame: ");
            String requestFrameString = scanner.nextLine();
            System.out.print("Enter response frame: ");
            String responseFrameString = scanner.nextLine();

            byte[] requestFrame = convertHexToByteArray(requestFrameString);
            byte[] responseFrame = convertHexToByteArray(responseFrameString);

            Integer[] startEnd = parseStartEnd(requestFrame);
            String requestInverterId = parseInverterId(requestFrame);
            System.out.printf("Request was from: 0x%04X to 0x%04X for logger with ID: %s%n", startEnd[0], startEnd[1],
                    requestInverterId);
            String responseInverterId = parseInverterId(responseFrame);
            System.out.printf("Response was from logger with ID: %s%n", responseInverterId);

            byte[] responseFrameRegister = Arrays.copyOfRange(responseFrame, 25, responseFrame.length - 2);

            for (int i = 0; i < startEnd[1] - startEnd[0] + 1; i++) {
                int p1 = 3 + (i * 2);
                ByteBuffer order = ByteBuffer.wrap(responseFrameRegister, p1, 2).order(ByteOrder.BIG_ENDIAN);
                byte[] array = new byte[] { order.get(), order.get() };
                Integer value = (array[0] << 8) + array[1];
                System.err.printf("[0x%04X]: 0x%04X (%d)\n", i + startEnd[0], value, value);
            }
        }
    }

    private static String parseInverterId(byte[] requestFrame) {
        byte[] inverterIdBytes = Arrays.copyOfRange(requestFrame, 7, 11);
        int inverterIdInt = ByteBuffer.wrap(inverterIdBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return String.valueOf(inverterIdInt & 0x00000000ffffffffL);
    }

    private static @NonNull Integer[] parseStartEnd(byte[] requestFrame) {
        int start = (requestFrame[28] << 8) + requestFrame[29];
        int length = (requestFrame[30] << 8) + requestFrame[31];

        return new Integer[] { start, start + length - 1 };
    }

    private static byte[] convertHexToByteArray(String frameString) {
        int len = frameString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(frameString.charAt(i), 16) << 4)
                    + Character.digit(frameString.charAt(i + 1), 16));
        }
        return data;
    }
}

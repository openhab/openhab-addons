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
package org.openhab.binding.onkyo.internal.eiscp;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle Onkyo eISCP protocol.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class EiscpProtocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(EiscpProtocol.class);

    /**
     * Wraps a command in an eISCP data message (data characters).
     *
     * @param msg
     *            eISCP command.
     * @return String holding the full eISCP message packet
     */
    public static String createEiscpPdu(EiscpMessage msg) {
        String data = msg.getCommand() + msg.getValue();
        StringBuilder sb = new StringBuilder();
        int eiscpDataSize = 2 + data.length() + 1; // this is the eISCP data size

        /*
         * This is where I construct the entire message character by character.
         * Each char is represented by a 2 digit hex value
         */
        sb.append("ISCP");
        // the following are all in HEX representing one char

        // 4 char Big Endian Header
        sb.append((char) 0x00);
        sb.append((char) 0x00);
        sb.append((char) 0x00);
        sb.append((char) 0x10);

        // 4 char Big Endian data size
        sb.append((char) ((eiscpDataSize >> 24) & 0xFF));
        sb.append((char) ((eiscpDataSize >> 16) & 0xFF));
        sb.append((char) ((eiscpDataSize >> 8) & 0xFF));
        sb.append((char) (eiscpDataSize & 0xFF));

        // eISCP version = "01";
        sb.append((char) 0x01);

        // 3 chars reserved = "00"+"00"+"00";
        sb.append((char) 0x00);
        sb.append((char) 0x00);
        sb.append((char) 0x00);

        // eISCP data

        // Start Character
        sb.append("!");

        // eISCP data - unit type char '1' is receiver
        sb.append("1");

        // eISCP data - 3 char command and param ie PWR01
        sb.append(data);

        // msg end - EOF
        sb.append((char) 0x0D);

        if (LOGGER.isTraceEnabled()) {
            String d = sb.toString();
            LOGGER.trace("Created eISCP message: {} -> {}", HexUtils.bytesToHex(d.getBytes()), toPrintable(d));
        }

        return sb.toString();
    }

    /**
     * Method to read eISCP message from input stream.
     *
     * @return message
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws EiscpException
     */
    public static EiscpMessage getNextMessage(DataInputStream stream)
            throws IOException, InterruptedException, EiscpException {
        while (true) {
            // 1st 4 chars are the lead in

            byte firstByte = stream.readByte();
            if (firstByte != 'I') {
                LOGGER.trace("Expected character 'I', received '{}'",
                        toPrintable(new String(new byte[] { firstByte })));
                continue;
            }
            if (stream.readByte() != 'S') {
                continue;
            }
            if (stream.readByte() != 'C') {
                continue;
            }
            if (stream.readByte() != 'P') {
                continue;
            }

            // header size
            final int headerSize = (stream.readByte() & 0xFF) << 24 | (stream.readByte() & 0xFF) << 16
                    | (stream.readByte() & 0xFF) << 8 | (stream.readByte() & 0xFF);

            if (headerSize != 16) {
                throw new EiscpException("Unsupported header size: " + headerSize);
            }

            // data size
            final int dataSize = (stream.readByte() & 0xFF) << 24 | (stream.readByte() & 0xFF) << 16
                    | (stream.readByte() & 0xFF) << 8 | (stream.readByte() & 0xFF);

            LOGGER.trace("Data size: {}", dataSize);

            // version
            final byte versionChar = stream.readByte();
            if (versionChar != 1) {
                throw new EiscpException("Unsupported version " + versionChar);
            }

            // skip 3 reserved bytes
            byte b1 = stream.readByte();
            byte b2 = stream.readByte();
            byte b3 = stream.readByte();

            byte[] data = new byte[dataSize];
            int bytesReceived = 0;

            try {
                while (bytesReceived < dataSize) {
                    bytesReceived = bytesReceived + stream.read(data, bytesReceived, data.length - bytesReceived);

                    if (LOGGER.isTraceEnabled()) {
                        // create header for debugging purposes
                        final StringBuilder sb = new StringBuilder();
                        sb.append("ISCP");
                        sb.append((char) 0x00);
                        sb.append((char) 0x00);
                        sb.append((char) 0x00);
                        sb.append((char) 0x10);
                        // 4 char Big Endian data size
                        sb.append((char) ((dataSize >> 24) & 0xFF));
                        sb.append((char) ((dataSize >> 16) & 0xFF));
                        sb.append((char) ((dataSize >> 8) & 0xFF));
                        sb.append((char) (dataSize & 0xFF));
                        // eiscp version;
                        sb.append((char) versionChar);
                        // reserved bytes
                        sb.append((char) b1).append((char) b2).append((char) b3);
                        // data
                        sb.append(new String(data, "UTF-8"));
                        LOGGER.trace("Received eISCP message, {} -> {}", HexUtils.bytesToHex(sb.toString().getBytes()),
                                toPrintable(sb.toString()));
                    }
                }
            } catch (IOException t) {
                if (bytesReceived != dataSize) {
                    LOGGER.debug("Received bad data: '{}'", toPrintable(new String(data, "UTF-8")));
                    throw new EiscpException(
                            "Data missing, expected + " + dataSize + " received " + bytesReceived + " bytes");
                } else {
                    throw t;
                }
            }

            // start char
            final byte startChar = data[0];

            if (startChar != '!') {
                throw new EiscpException("Illegal start char " + startChar);
            }

            // unit type
            @SuppressWarnings("unused")
            final byte unitType = data[1];

            // data should be end to "[EOF]" or "[EOF][CR]" or "[EOF][CR][LF]" characters depend on model
            // [EOF] End of File ASCII Code 0x1A
            // [CR] Carriage Return ASCII Code 0x0D (\r)
            // [LF] Line Feed ASCII Code 0x0A (\n)

            int endBytes = 0;

            // TODO: Simplify this by implementation, which find [EOF] character and ignore rest of the bytes after
            // that. But before that, proper junit test should be implement to be sure that it does not broke
            // anything.

            if (data[dataSize - 5] == (byte) 0x1A && data[dataSize - 4] == '\n' && data[dataSize - 3] == '\n'
                    && data[dataSize - 2] == '\r' && data[dataSize - 1] == '\n') {
                // skip "[EOF][LF][LF][CR][LF]"
                endBytes = 5;
            } else if (data[dataSize - 4] == (byte) 0x1A && data[dataSize - 3] == '\r' && data[dataSize - 2] == '\n'
                    && data[dataSize - 1] == 0x00) {
                // skip "[EOF][CR][LF][NULL]"
                endBytes = 4;
            } else if (data[dataSize - 3] == (byte) 0x1A && data[dataSize - 2] == '\r' && data[dataSize - 1] == '\n') {
                // skip "[EOF][CR][LF]"
                endBytes = 3;
            } else if (data[dataSize - 2] == (byte) 0x1A && data[dataSize - 1] == '\r') {
                // "[EOF][CR]"
                endBytes = 2;
            } else if (data[dataSize - 1] == (byte) 0x1A) {
                // "[EOF]"
                endBytes = 1;
            } else {
                throw new EiscpException("Illegal end of message");
            }

            try {
                String command = new String(Arrays.copyOfRange(data, 2, 5));
                String value = new String(Arrays.copyOfRange(data, 5, data.length - endBytes));
                return new EiscpMessage.MessageBuilder().command(command).value(value).build();
            } catch (Exception e) {
                throw new EiscpException("Fatal error occurred when parsing eISCP message, cause=" + e.getCause());
            }
        }
    }

    public static String toPrintable(final String rawData) {
        final StringBuilder sb = new StringBuilder();

        if (rawData == null) {
            return "";
        }

        for (final char c : rawData.toCharArray()) {
            if (c <= 31 || c == 127) {
                switch (c) {
                    case '\r':
                        sb.append("[CR]");
                        break;
                    case '\n':
                        sb.append("[LF]");
                        break;
                    case (byte) 0x1A:
                        sb.append("[EOF]");
                        break;
                    default:
                        sb.append(String.format("[%02X]", (int) c));
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}

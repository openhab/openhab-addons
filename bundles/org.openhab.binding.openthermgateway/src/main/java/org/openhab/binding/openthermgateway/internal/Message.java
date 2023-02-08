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
package org.openhab.binding.openthermgateway.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Message} represent a single message received from the OpenTherm Gateway.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class Message {

    private static final Pattern MESSAGEPATTERN = Pattern.compile("[TBRA]{1}[A-F0-9]{8}");

    private CodeType codeType;
    private MessageType messageType;
    private int id;
    private String data;

    public CodeType getCodeType() {
        return codeType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getID() {
        return id;
    }

    public @Nullable String getData(ByteType byteType) {
        if (this.data.length() == 4) {
            switch (byteType) {
                case HIGHBYTE:
                    return this.data.substring(0, 2);
                case LOWBYTE:
                    return this.data.substring(2, 4);
                case BOTH:
                    return this.data;
            }
        }

        return null;
    }

    public boolean getBit(ByteType byteType, int pos) {
        @Nullable
        String data = getData(byteType);

        if (data != null) {
            // First parse the hex value to an integer
            int parsed = Integer.parseInt(data, 16);

            // Then right shift it pos positions so that the required bit is at the front
            // and then apply a bitmask of 00000001 (1)
            return ((parsed >> pos) & 1) == 1;
        }

        return false;
    }

    public int getUInt(ByteType byteType) {
        @Nullable
        String data = getData(byteType);

        if (data != null) {
            return Integer.parseInt(data, 16);
        }

        return 0;
    }

    public int getInt(ByteType byteType) {
        @Nullable
        String data = getData(byteType);

        if (data != null) {
            return parseSignedInteger(data);
        }

        return 0;
    }

    public float getFloat() {
        // f8.8, two's complement
        @Nullable
        String data = getData(ByteType.BOTH);

        if (data != null) {
            long value = Long.parseLong(data, 16);

            // left padded with zeros
            String binary = String.format("%16s", Long.toBinaryString(value)).replace(' ', '0');

            if (binary.charAt(0) == '1') {
                // negative value

                String inverted = invertBinary(binary);

                value = Long.parseLong(inverted, 2);
                value = value + 1;
                value = value * -1;
            }

            // divide by 2^8 = 256
            return (float) value / 256;
        }

        return 0;
    }

    public boolean overrides(@Nullable Message other) {
        // If the message is a Request sent to the boiler or an Answer returned to the
        // thermostat, and it's ID is equal to the previous message, then this is an
        // override sent by the OpenTherm Gateway
        return other != null && this.getID() == other.getID() && (codeType == CodeType.R || codeType == CodeType.A);
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", this.codeType, this.id, this.data);
    }

    public Message(CodeType codeType, MessageType messageType, int id, String data) {
        this.codeType = codeType;
        this.messageType = messageType;
        this.id = id;
        this.data = data;
    }

    public static @Nullable Message parse(String message) {
        if (MESSAGEPATTERN.matcher(message).matches()) {
            // For now, only parse TBRA codes
            CodeType codeType = CodeType.valueOf(message.substring(0, 1));
            MessageType messageType = getMessageType(message.substring(1, 3));
            int id = Integer.valueOf(message.substring(3, 5), 16);
            String data = message.substring(5);

            return new Message(codeType, messageType, id, data);
        }

        return null;
    }

    private static MessageType getMessageType(String value) {
        // First parse the hex value to an integer
        int integer = Integer.parseInt(value, 16);

        // Then right shift it 4 bits so that the message type bits are at the front
        int shifted = integer >> 4;

        // Then mask it with 00000111 (7), so that we only get the first 3 bits,
        // effectively cutting off the parity bit.
        int cutoff = shifted & 7;

        switch (cutoff) {
            case 0: // 000
                return MessageType.READDATA;
            case 1: // 001
                return MessageType.WRITEDATA;
            case 2: // 010
                return MessageType.INVALIDDATA;
            case 3: // 011
                return MessageType.RESERVED;
            case 4: // 100
                return MessageType.READACK;
            case 5: // 101
                return MessageType.WRITEACK;
            case 6: // 110
                return MessageType.DATAINVALID;
            case 7: // 111
            default:
                return MessageType.UNKNOWNDATAID;
        }
    }

    private int parseSignedInteger(String data) {
        // First parse the hex value to an unsigned integer value
        int result = Integer.parseInt(data, 16);

        if (data.length() == 4) {
            // This is a two byte value, apply a bitmask of 01111111 11111111 (32767) to cut
            // off the sign bit
            result = result & 32767;

            // Then apply a bitmask of 10000000 00000000 (32768) to check the sign bit
            if ((result & 32768) == 32768) {
                // If the sign is 1000000 00000000 (32768) then it's a negative
                result = -32768 + result;
            }
        } else {
            // This is a one byte value, apply a bitmask of 01111111 (127), to cut off the
            // sign bit
            result = result & 127;

            // Then apply a bitmask of 10000000 (128) to check the sign bit
            if ((result & 128) == 128) {
                // If the sign is 1000000 (128) then it's a negative
                result = -128 + result;
            }
        }

        return result;
    }

    private String invertBinary(String value) {
        // There is probably a better solution, but for now this works
        String result = value;

        result = result.replace('1', 'X');
        result = result.replace('0', '1');
        result = result.replace('X', '0');

        return result;
    }
}

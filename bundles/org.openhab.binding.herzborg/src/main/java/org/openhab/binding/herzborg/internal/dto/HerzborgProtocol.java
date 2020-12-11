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
package org.openhab.binding.herzborg.internal.dto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Herzborg binary protocol
 *
 * @author Pavel Fedin - Initial contribution
 *
 */
public class HerzborgProtocol {
    public static class Function {
        public static final byte READ = 0x01;
        public static final byte WRITE = 0x02;
        public static final byte CONTROL = 0x03;
        public static final byte REQUEST = 0x04;
    }

    public static class ControlAddress {
        public static final byte OPEN = 0x01;
        public static final byte CLOSE = 0x02;
        public static final byte STOP = 0x03;
        public static final byte PERCENT = 0x04;
        public static final byte DELETE_LIMIT = 0x07;
        public static final byte DEFAULT = 0x08;
        public static final byte SET_CONTEXT = 0x09;
        public static final byte RUN_CONTEXT = 0x0A;
        public static final byte DEL_CONTEXT = 0x0B;
    }

    public static class DataAddress {
        public static final byte ID_L = 0x00;
        public static final byte ID_H = 0x01;
        public static final byte POSITION = 0x02;
        public static final byte DEFAULT_DIR = 0x03;
        public static final byte HAND_START = 0x04;
        public static final byte MODE = 0x05;
        public static final byte EXT_SWITCH = 0x27;
        public static final byte EXT_HV_SWITCH = 0x28;
    }

    public static class Packet {
        private static final int HEADER_LENGTH = 5;
        private static final int CRC16_LENGTH = 2;
        public static final int MIN_LENGTH = HEADER_LENGTH + CRC16_LENGTH;

        private static final byte START = 0x55;

        private ByteBuffer dataBuffer;
        private int dataLength; // Packet length without CRC16

        public Packet(byte[] data) {
            dataBuffer = ByteBuffer.wrap(data);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            dataLength = data.length - CRC16_LENGTH;
        }

        private void setHeader(short device_addr, byte function, byte data_addr, int data_length) {
            dataLength = HEADER_LENGTH + data_length;

            dataBuffer = ByteBuffer.allocate(dataLength + CRC16_LENGTH);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);

            dataBuffer.put(START);
            dataBuffer.putShort(device_addr);
            dataBuffer.put(function);
            dataBuffer.put(data_addr);
        }

        private void setCrc16() {
            dataBuffer.putShort(crc16(dataLength));
        }

        public Packet(short device_addr, byte function, byte data_addr) {
            setHeader(device_addr, function, data_addr, 0);
            setCrc16();
        }

        public Packet(short device_addr, byte function, byte data_addr, byte value) {
            int dataLength = (function == Function.WRITE) ? 2 : 1;

            setHeader(device_addr, function, data_addr, dataLength);
            if (function == Function.WRITE) {
                // WRITE command also requires length of data to be written
                dataBuffer.put((byte) 1);
            }
            dataBuffer.put(value);
            setCrc16();
        }

        public byte[] getBuffer() {
            return dataBuffer.array();
        }

        public boolean isValid() {
            return dataBuffer.get(0) == START && crc16(dataLength) == dataBuffer.getShort(dataLength);
        }

        public byte getFunction() {
            return dataBuffer.get(3);
        }

        public byte getDataAddress() {
            return dataBuffer.get(4);
        }

        public byte getDataLength() {
            return dataBuffer.get(HEADER_LENGTH);
        }

        public byte getData(int offset) {
            return dataBuffer.get(HEADER_LENGTH + offset);
        }

        // Herzborg uses modbus variant of CRC16
        // Code adapted from https://habr.com/ru/post/418209/
        private short crc16(int length) {
            int crc = 0xFFFF;
            for (int i = 0; i < length; i++) {
                crc = crc ^ Byte.toUnsignedInt(dataBuffer.get(i));
                for (int j = 0; j < 8; j++) {
                    int mask = ((crc & 0x1) != 0) ? 0xA001 : 0x0000;
                    crc = ((crc >> 1) & 0x7FFF) ^ mask;
                }
            }
            return (short) crc;
        }
    }
}

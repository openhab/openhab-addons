/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal.map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.RoborockException;

class RRMapParserTest {

    @Test
    void parseExtractsImageAndPositions() throws Exception {
        RRMapParser parser = new RRMapParser();

        byte[] imageData = new byte[] { 1, 2, 3, 4 };
        byte[] imageBlock = createImageBlock(2, 2, 100, 200, imageData);
        byte[] robotBlock = createSimpleDataBlock(8, new int[] { 50, 60, 90 });
        byte[] chargerBlock = createSimpleDataBlock(1, new int[] { 10, 20 });
        byte[] pathBlock = createPathBlock(new int[] { 1, 2, 3, 4 });
        byte[] zonesBlock = createZonesBlock(new int[] { 11, 12, 13, 14 });
        byte[] unknownBlock = createOpaqueBlock(77, new byte[] { 0x01, 0x02 });

        byte[] payload = createPayload(imageBlock, robotBlock, chargerBlock, pathBlock, zonesBlock, unknownBlock);
        RRMapData mapData = parser.parse(payload);

        assertEquals(2, mapData.imageWidth());
        assertEquals(2, mapData.imageHeight());
        assertEquals(100, mapData.top());
        assertEquals(200, mapData.left());
        assertArrayEquals(imageData, mapData.imageData());
        assertEquals(50, mapData.robotX());
        assertEquals(60, mapData.robotY());
        assertEquals(90, mapData.robotAngle());
        assertEquals(10, mapData.chargerX());
        assertEquals(20, mapData.chargerY());
        assertEquals(2, mapData.basicPath().size());
        assertEquals(1, mapData.basicPath().get(0).x());
        assertEquals(2, mapData.basicPath().get(0).y());
        assertEquals(3, mapData.basicPath().get(1).x());
        assertEquals(4, mapData.basicPath().get(1).y());
        assertEquals(1, mapData.cleanedZones().size());
        assertEquals(11, mapData.cleanedZones().get(0).x0());
        assertEquals(12, mapData.cleanedZones().get(0).y0());
        assertEquals(13, mapData.cleanedZones().get(0).x1());
        assertEquals(14, mapData.cleanedZones().get(0).y1());
    }

    @Test
    void parseThrowsForCorruptedLength() {
        RRMapParser parser = new RRMapParser();
        byte[] payload = new byte[32];
        payload[2] = 20; // main header length
        payload[20] = 2; // image block type
        payload[22] = 8; // block header length
        payload[24] = (byte) 0xFF; // unrealistic data length
        payload[25] = (byte) 0xFF;
        payload[26] = (byte) 0xFF;
        payload[27] = 0x7F;

        assertThrows(RoborockException.class, () -> parser.parse(payload));
    }

    private static byte[] createPayload(byte[]... blocks) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[20];
        header[2] = 20; // main header length
        output.write(header);
        for (byte[] block : blocks) {
            output.write(block);
        }
        return output.toByteArray();
    }

    private static byte[] createImageBlock(int width, int height, int top, int left, byte[] imageData)
            throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[24];
        writeUInt16LE(header, 0, 2);
        writeUInt16LE(header, 2, 24);
        writeUInt32LE(header, 4, imageData.length);
        writeUInt32LE(header, 8, top);
        writeUInt32LE(header, 12, left);
        writeUInt32LE(header, 16, height);
        writeUInt32LE(header, 20, width);
        output.write(header);
        output.write(imageData);
        return output.toByteArray();
    }

    private static byte[] createSimpleDataBlock(int type, int[] values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int dataLength = values.length * 4;
        byte[] header = new byte[8];
        writeUInt16LE(header, 0, type);
        writeUInt16LE(header, 2, 8);
        writeUInt32LE(header, 4, dataLength);
        output.write(header);
        for (int value : values) {
            byte[] data = new byte[4];
            writeUInt32LE(data, 0, value);
            output.write(data);
        }
        return output.toByteArray();
    }

    private static byte[] createOpaqueBlock(int type, byte[] data) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[8];
        writeUInt16LE(header, 0, type);
        writeUInt16LE(header, 2, 8);
        writeUInt32LE(header, 4, data.length);
        output.write(header);
        output.write(data);
        return output.toByteArray();
    }

    private static byte[] createPathBlock(int[] xyPairs) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[8];
        writeUInt16LE(header, 0, 3);
        writeUInt16LE(header, 2, 8);
        writeUInt32LE(header, 4, xyPairs.length * 2);
        output.write(header);
        for (int value : xyPairs) {
            byte[] data = new byte[2];
            writeUInt16LE(data, 0, value);
            output.write(data);
        }
        return output.toByteArray();
    }

    private static byte[] createZonesBlock(int[] values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[8];
        writeUInt16LE(header, 0, 6);
        writeUInt16LE(header, 2, 8);
        writeUInt32LE(header, 4, values.length * 2);
        output.write(header);
        for (int value : values) {
            byte[] data = new byte[2];
            writeUInt16LE(data, 0, value);
            output.write(data);
        }
        return output.toByteArray();
    }

    private static void writeUInt16LE(byte[] target, int offset, int value) {
        target[offset] = (byte) (value & 0xFF);
        target[offset + 1] = (byte) ((value >>> 8) & 0xFF);
    }

    private static void writeUInt32LE(byte[] target, int offset, int value) {
        target[offset] = (byte) (value & 0xFF);
        target[offset + 1] = (byte) ((value >>> 8) & 0xFF);
        target[offset + 2] = (byte) ((value >>> 16) & 0xFF);
        target[offset + 3] = (byte) ((value >>> 24) & 0xFF);
    }
}

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
        byte[] gotoPathBlock = createTypedPathBlock(4, new int[] { 5, 6, 7, 8 });
        byte[] predictedPathBlock = createTypedPathBlock(5, new int[] { 9, 10, 11, 12 });
        byte[] gotoTargetBlock = createGotoTargetBlock(70, 80);
        byte[] zonesBlock = createZonesBlock(new int[] { 11, 12, 13, 14 });
        byte[] wallsBlock = createWallsBlock(new int[] { 31, 32, 33, 34 });
        byte[] noGoBlock = createAreaBlock(9, new int[] { 41, 42, 43, 44, 45, 46, 47, 48 });
        byte[] mopForbiddenBlock = createAreaBlock(12, new int[] { 51, 52, 53, 54, 55, 56, 57, 58 });
        byte[] carpetForbiddenBlock = createAreaBlock(19, new int[] { 61, 62, 63, 64, 65, 66, 67, 68 });
        byte[] obstaclesBlock = createObstacleBlock(13, new int[] { 101, 102, 2, 111, 112, 3 });
        byte[] ignoredObstaclesBlock = createObstacleBlock(14, new int[] { 121, 122, 4 });
        byte[] obstaclesWithPhotoBlock = createObstacleWithPhotoBlock(15, 131, 132, 48);
        byte[] ignoredObstaclesWithPhotoBlock = createObstacleWithPhotoBlock(16, 141, 142, 51);
        byte[] carpetMapBlock = createOpaqueBlock(17, new byte[] { 0x00, 0x01, 0x00, 0x01 });
        byte[] mopPathBlock = createOpaqueBlock(18, new byte[] { 0x01, 0x00, 0x04, 0x00 });
        byte[] unknownBlock = createOpaqueBlock(77, new byte[] { 0x01, 0x02 });

        byte[] payload = createPayload(imageBlock, robotBlock, chargerBlock, pathBlock, gotoPathBlock,
                predictedPathBlock, gotoTargetBlock, zonesBlock, wallsBlock, noGoBlock, mopForbiddenBlock,
                carpetForbiddenBlock, obstaclesBlock, ignoredObstaclesBlock, obstaclesWithPhotoBlock,
                ignoredObstaclesWithPhotoBlock, carpetMapBlock, mopPathBlock, unknownBlock);
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
        assertEquals(2, mapData.gotoPath().size());
        assertEquals(5, mapData.gotoPath().get(0).x());
        assertEquals(8, mapData.gotoPath().get(1).y());
        assertEquals(2, mapData.predictedPath().size());
        assertEquals(9, mapData.predictedPath().get(0).x());
        assertEquals(12, mapData.predictedPath().get(1).y());
        assertEquals(70, mapData.gotoTargetX());
        assertEquals(80, mapData.gotoTargetY());
        assertEquals(1, mapData.cleanedZones().size());
        assertEquals(11, mapData.cleanedZones().get(0).x0());
        assertEquals(12, mapData.cleanedZones().get(0).y0());
        assertEquals(13, mapData.cleanedZones().get(0).x1());
        assertEquals(14, mapData.cleanedZones().get(0).y1());

        assertEquals(1, mapData.virtualWalls().size());
        assertEquals(31, mapData.virtualWalls().get(0).x0());
        assertEquals(32, mapData.virtualWalls().get(0).y0());
        assertEquals(33, mapData.virtualWalls().get(0).x1());
        assertEquals(34, mapData.virtualWalls().get(0).y1());

        assertEquals(1, mapData.noGoAreas().size());
        assertEquals(41, mapData.noGoAreas().get(0).x0());
        assertEquals(48, mapData.noGoAreas().get(0).y3());

        assertEquals(1, mapData.mopForbiddenAreas().size());
        assertEquals(51, mapData.mopForbiddenAreas().get(0).x0());
        assertEquals(58, mapData.mopForbiddenAreas().get(0).y3());

        assertEquals(1, mapData.carpetForbiddenAreas().size());
        assertEquals(61, mapData.carpetForbiddenAreas().get(0).x0());
        assertEquals(68, mapData.carpetForbiddenAreas().get(0).y3());

        assertEquals(3, mapData.obstacles().size());
        assertEquals(101, mapData.obstacles().get(0).x());
        assertEquals(102, mapData.obstacles().get(0).y());
        assertEquals(2, mapData.obstacles().get(0).type());
        assertEquals("shoes", mapData.obstacles().get(0).typeLabel());
        assertEquals(131, mapData.obstacles().get(2).x());
        assertEquals(132, mapData.obstacles().get(2).y());
        assertEquals(48, mapData.obstacles().get(2).type());
        assertEquals("cable", mapData.obstacles().get(2).typeLabel());

        assertEquals(2, mapData.ignoredObstacles().size());
        assertEquals(121, mapData.ignoredObstacles().get(0).x());
        assertEquals(122, mapData.ignoredObstacles().get(0).y());
        assertEquals(4, mapData.ignoredObstacles().get(0).type());
        assertEquals("pedestal", mapData.ignoredObstacles().get(0).typeLabel());
        assertEquals(141, mapData.ignoredObstacles().get(1).x());
        assertEquals(142, mapData.ignoredObstacles().get(1).y());
        assertEquals(51, mapData.ignoredObstacles().get(1).type());
        assertEquals("fabric/paper balls", mapData.ignoredObstacles().get(1).typeLabel());

        assertArrayEquals(new byte[] { 0x01, 0x00, 0x04, 0x00 }, mapData.mopPathMask());
        assertArrayEquals(new byte[] { 0x00, 0x01, 0x00, 0x01 }, mapData.carpetMapMask());
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
        return createTypedPathBlock(3, xyPairs);
    }

    private static byte[] createTypedPathBlock(int type, int[] xyPairs) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[8];
        writeUInt16LE(header, 0, type);
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

    private static byte[] createGotoTargetBlock(int x, int y) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[8];
        writeUInt16LE(header, 0, 7);
        writeUInt16LE(header, 2, 8);
        writeUInt32LE(header, 4, 4);
        output.write(header);
        byte[] data = new byte[4];
        writeUInt16LE(data, 0, x);
        writeUInt16LE(data, 2, y);
        output.write(data);
        return output.toByteArray();
    }

    private static byte[] createObstacleBlock(int type, int[] xyzTriplets) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[10];
        writeUInt16LE(header, 0, type);
        writeUInt16LE(header, 2, 10);
        writeUInt32LE(header, 4, xyzTriplets.length / 3 * 5);
        writeUInt16LE(header, 8, xyzTriplets.length / 3);
        output.write(header);
        for (int i = 0; i < xyzTriplets.length; i += 3) {
            byte[] data = new byte[5];
            writeUInt16LE(data, 0, xyzTriplets[i]);
            writeUInt16LE(data, 2, xyzTriplets[i + 1]);
            data[4] = (byte) (xyzTriplets[i + 2] & 0xFF);
            output.write(data);
        }
        return output.toByteArray();
    }

    private static byte[] createObstacleWithPhotoBlock(int type, int x, int y, int obstacleType) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[10];
        writeUInt16LE(header, 0, type);
        writeUInt16LE(header, 2, 10);
        writeUInt32LE(header, 4, 28);
        writeUInt16LE(header, 8, 1);
        output.write(header);

        byte[] data = new byte[28];
        writeUInt16LE(data, 0, x);
        writeUInt16LE(data, 2, y);
        writeUInt16LE(data, 4, obstacleType);
        output.write(data);
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

    private static byte[] createWallsBlock(int[] values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[10];
        writeUInt16LE(header, 0, 10);
        writeUInt16LE(header, 2, 10);
        writeUInt32LE(header, 4, values.length * 2);
        writeUInt16LE(header, 8, values.length / 4);
        output.write(header);
        for (int value : values) {
            byte[] data = new byte[2];
            writeUInt16LE(data, 0, value);
            output.write(data);
        }
        return output.toByteArray();
    }

    private static byte[] createAreaBlock(int type, int[] values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[10];
        writeUInt16LE(header, 0, type);
        writeUInt16LE(header, 2, 10);
        writeUInt32LE(header, 4, values.length * 2);
        writeUInt16LE(header, 8, values.length / 8);
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

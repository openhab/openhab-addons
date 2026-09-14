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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RoomAtRobotResolver}.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault({})
class RoomAtRobotResolverTest {

    private static final int MM = 50;

    @Test
    void resolvesSegmentIdAtExactRobotPixel() {
        int width = 10;
        int height = 10;
        byte[] imageData = new byte[width * height];
        // Segment id 5 at pixel (4, 4): classifier 7 in low 3 bits, segment id in high 5 bits.
        int px = 4;
        int py = 4;
        imageData[py * width + px] = (byte) ((5 << 3) | 0x07);

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);
        // Inverse of RoomAtRobotResolver's transform: robotCoord = (px + left + 1) * MM.
        int robotX = (px + 0 + 1) * MM;
        int robotY = (py + 0 + 1) * MM;

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, robotX, robotY);

        assertTrue(segmentId.isPresent());
        assertEquals(5, segmentId.get());
    }

    @Test
    void appliesTopAndLeftOffsetsInTheCoordinateTransform() {
        int width = 10;
        int height = 10;
        int top = 2;
        int left = 3;
        byte[] imageData = new byte[width * height];
        int px = 6;
        int py = 1;
        imageData[py * width + px] = (byte) ((9 << 3) | 0x07);

        RRMapData mapData = mapDataWithImage(width, height, top, left, imageData);
        int robotX = (px + left + 1) * MM;
        int robotY = (py + top + 1) * MM;

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, robotX, robotY);

        assertTrue(segmentId.isPresent());
        assertEquals(9, segmentId.get());
    }

    @Test
    void fallsBackToNearbyRingWhenExactPixelIsNotASegmentPixel() {
        int width = 10;
        int height = 10;
        byte[] imageData = new byte[width * height];
        int px = 5;
        int py = 5;
        imageData[py * width + px] = 0x01; // MAP_WALL
        imageData[(py + 2) * width + (px + 1)] = (byte) ((3 << 3) | 0x07);

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);
        int robotX = (px + 1) * MM;
        int robotY = (py + 1) * MM;

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, robotX, robotY);

        assertTrue(segmentId.isPresent());
        assertEquals(3, segmentId.get());
    }

    @Test
    void doesNotSearchBeyondFallbackRadiusTwo() {
        int width = 10;
        int height = 10;
        byte[] imageData = new byte[width * height];
        int px = 5;
        int py = 5;
        imageData[py * width + px] = 0x01; // MAP_WALL
        // Segment pixel three rings out.
        imageData[(py + 3) * width + px] = (byte) ((3 << 3) | 0x07);

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);
        int robotX = (px + 1) * MM;
        int robotY = (py + 1) * MM;

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, robotX, robotY);

        assertFalse(segmentId.isPresent());
    }

    @Test
    void returnsEmptyWhenPixelIsOutOfImageBounds() {
        int width = 4;
        int height = 4;
        byte[] imageData = new byte[width * height];

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);
        int robotX = 5000;
        int robotY = 5000;

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, robotX, robotY);

        assertFalse(segmentId.isPresent());
    }

    @Test
    void returnsEmptyForNonSegmentPixelWithNoSegmentPixelNearby() {
        int width = 10;
        int height = 10;
        byte[] imageData = new byte[width * height];
        int px = 5;
        int py = 5;
        imageData[py * width + px] = (byte) 0xFF; // MAP_INSIDE

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);
        int robotX = (px + 1) * MM;
        int robotY = (py + 1) * MM;

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, robotX, robotY);

        assertFalse(segmentId.isPresent());
    }

    @Test
    void picksTheDominantSegmentWhenTheFallbackRingHoldsTwoRooms() {
        int width = 10;
        int height = 10;
        byte[] imageData = new byte[width * height];
        int px = 5;
        int py = 5;
        imageData[py * width + px] = 0x01; // MAP_WALL
        // Segment 4 owns one diagonal pixel of the ring, segment 3 the majority.
        imageData[(py - 1) * width + (px - 1)] = (byte) ((4 << 3) | 0x07);
        imageData[(py - 1) * width + px] = (byte) ((3 << 3) | 0x07);
        imageData[py * width + (px + 1)] = (byte) ((3 << 3) | 0x07);
        imageData[(py + 1) * width + px] = (byte) ((3 << 3) | 0x07);

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, (px + 1) * MM, (py + 1) * MM);

        assertTrue(segmentId.isPresent());
        assertEquals(3, segmentId.get());
    }

    @Test
    void returnsEmptyWhenTheLargestSegmentOnTheRingHoldsOnlyHalfOfItsPixels() {
        int width = 10;
        int height = 10;
        byte[] imageData = new byte[width * height];
        int px = 5;
        int py = 5;
        imageData[py * width + px] = 0x01; // MAP_WALL
        // A 2/1/1 split: segment 3 leads but owns exactly half of the ring, not a majority.
        imageData[(py - 1) * width + px] = (byte) ((3 << 3) | 0x07);
        imageData[(py + 1) * width + px] = (byte) ((3 << 3) | 0x07);
        imageData[py * width + (px - 1)] = (byte) ((4 << 3) | 0x07);
        imageData[py * width + (px + 1)] = (byte) ((5 << 3) | 0x07);

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, (px + 1) * MM, (py + 1) * MM);

        assertFalse(segmentId.isPresent());
    }

    @Test
    void returnsEmptyWhenTheNearestFallbackRingIsTiedBetweenTwoRooms() {
        int width = 10;
        int height = 10;
        byte[] imageData = new byte[width * height];
        int px = 5;
        int py = 5;
        imageData[py * width + px] = 0x01; // MAP_WALL
        // Two pixels per segment on the nearest ring; the unambiguous farther ring must not decide it.
        imageData[(py - 1) * width + px] = (byte) ((3 << 3) | 0x07);
        imageData[(py + 1) * width + px] = (byte) ((3 << 3) | 0x07);
        imageData[py * width + (px - 1)] = (byte) ((4 << 3) | 0x07);
        imageData[py * width + (px + 1)] = (byte) ((4 << 3) | 0x07);
        imageData[(py + 2) * width + (px + 2)] = (byte) ((7 << 3) | 0x07);

        RRMapData mapData = mapDataWithImage(width, height, 0, 0, imageData);

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, (px + 1) * MM, (py + 1) * MM);

        assertFalse(segmentId.isPresent());
    }

    /** 65536 x 65536 wraps to exactly 0 in int arithmetic and would pass a guard multiplying in int. */
    @Test
    void returnsEmptyWhenTheHeaderDimensionsOverflowIntArithmetic() {
        RRMapData mapData = mapDataWithLyingHeader(65536, 65536, new byte[100]);

        Optional<Integer> segmentId = assertDoesNotThrow(() -> RoomAtRobotResolver.resolveSegmentId(mapData, 250, 250));

        assertFalse(segmentId.isPresent());
    }

    /**
     * The same lying header, but with enough image data behind it that the bogus index lands inside
     * the array - on a segmented-floor pixel of an unrelated part of the map.
     */
    @Test
    void returnsEmptyRatherThanAForeignPixelWhenTheHeaderDimensionsOverflow() {
        int declaredWidth = 65536;
        byte[] imageData = new byte[300000];
        // Where "pixel (4, 4)" lands when the declared width is believed.
        imageData[4 * declaredWidth + 4] = (byte) ((5 << 3) | 0x07);

        RRMapData mapData = mapDataWithLyingHeader(declaredWidth, 65536, imageData);

        Optional<Integer> segmentId = assertDoesNotThrow(() -> RoomAtRobotResolver.resolveSegmentId(mapData, 250, 250));

        assertFalse(segmentId.isPresent());
    }

    @Test
    void returnsEmptyWhenTheHeaderDimensionsOverflowToANegativeProduct() {
        RRMapData mapData = mapDataWithLyingHeader(65536, 65535, new byte[100]);

        Optional<Integer> segmentId = assertDoesNotThrow(() -> RoomAtRobotResolver.resolveSegmentId(mapData, 250, 250));

        assertFalse(segmentId.isPresent());
    }

    @Test
    void returnsEmptyWhenImageDataIsShorterThanWidthTimesHeight() {
        RRMapData mapData = mapDataWithImage(10, 10, 0, 0, new byte[4]);

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, 250, 250);

        assertFalse(segmentId.isPresent());
    }

    /** A map whose header claims dimensions the actual image data does not cover. */
    private RRMapData mapDataWithLyingHeader(int declaredWidth, int declaredHeight, byte[] imageData) {
        return new RRMapData(declaredWidth, declaredHeight, 0, 0, imageData, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), new byte[0]);
    }

    private RRMapData mapDataWithImage(int width, int height, int top, int left, byte[] imageData) {
        return new RRMapData(width, height, top, left, imageData, null, null, null, null, null, null, null, List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                new byte[width * height]);
    }
}

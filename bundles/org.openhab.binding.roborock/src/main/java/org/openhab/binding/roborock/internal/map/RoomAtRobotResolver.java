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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Resolves the map segment (room) id at the robot's current position.
 * <p>
 * The RR map format already retains a per-pixel segment id in {@link RRMapData#imageData()} (see
 * {@link RRMapRenderer#decodeSegmentId(int)}) - it is decoded there only to pick a fill color and
 * otherwise discarded. This class locates the robot's own pixel in that same retained image data
 * and decodes it the same way, so "which room is the robot in" is answered from data the binding
 * already keeps, without changing the map wire format or {@link RRMapData}'s shape.
 * <p>
 * Coordinates: {@code robotX}/{@code robotY} are in the RR map's raw coordinate units (as parsed by
 * {@code RRMapParser}, same units as {@code chargerX}/{@code chargerY} and path points). They are
 * converted to a pixel index using the same fixed divisor and {@code top}/{@code left} offsets as
 * {@code RRMapRenderer.toXCoord}/{@code toYCoord}, inverted to land on the pixel grid backing
 * {@link RRMapData#imageData()} rather than the rendered/flipped canvas.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public final class RoomAtRobotResolver {

    /** Coordinate-to-pixel conversion divisor, matching {@code RRMapRenderer.MM}. */
    private static final int MM = 50;

    /**
     * Maximum Chebyshev-distance ring searched outward from the robot's own pixel when that pixel
     * is not itself a segmented-floor pixel (for example the robot is on a wall/scan boundary
     * pixel, or docked at the edge of a segment). Two rings is a small, cheap search that still
     * covers the robot's immediate footprint without risking a false match from a distant room.
     */
    private static final int FALLBACK_SEARCH_RADIUS = 2;

    private RoomAtRobotResolver() {
    }

    /**
     * Resolves the segment id of the map pixel at the robot's position, falling back to a small
     * outward search if that exact pixel is not a segmented-floor pixel.
     *
     * @param mapData parsed map data; {@code imageData()}/{@code imageWidth()}/{@code imageHeight()}
     *            /{@code top()}/{@code left()} are used to locate and decode the pixel
     * @param robotX robot X position in raw RR map coordinate units
     * @param robotY robot Y position in raw RR map coordinate units
     * @return the segment id (0-31) if one was found at or near the robot's position, empty otherwise
     */
    public static Optional<Integer> resolveSegmentId(RRMapData mapData, int robotX, int robotY) {
        int width = mapData.imageWidth();
        int height = mapData.imageHeight();
        byte[] imageData = mapData.imageData();
        if (width <= 0 || height <= 0 || imageData.length < width * height) {
            return Optional.empty();
        }

        int centerX = Math.round(robotX / (float) MM) - mapData.left() - 1;
        int centerY = Math.round(robotY / (float) MM) - mapData.top() - 1;

        for (int radius = 0; radius <= FALLBACK_SEARCH_RADIUS; radius++) {
            Optional<Integer> found = searchRing(imageData, width, height, centerX, centerY, radius);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private static Optional<Integer> searchRing(byte[] imageData, int width, int height, int centerX, int centerY,
            int radius) {
        if (radius == 0) {
            return segmentAt(imageData, width, height, centerX, centerY);
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) {
                    // Only the outer ring of this radius: smaller radii were already tried.
                    continue;
                }
                Optional<Integer> found = segmentAt(imageData, width, height, centerX + dx, centerY + dy);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Integer> segmentAt(byte[] imageData, int width, int height, int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return Optional.empty();
        }
        int pixelValue = imageData[y * width + x] & 0xFF;
        int segmentId = RRMapRenderer.decodeSegmentId(pixelValue);
        return segmentId >= 0 ? Optional.of(segmentId) : Optional.empty();
    }
}

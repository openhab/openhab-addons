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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Resolves the map segment (room) id at a position on the map, primarily the robot's own.
 * <p>
 * The RR map format already retains a per-pixel segment id in {@link RRMapData#imageData()} (see
 * {@link RRMapRenderer#decodeSegmentId(int)}) - it is decoded there only to pick a fill color and
 * otherwise discarded. This class locates a given map position's pixel in that same retained image
 * data and decodes it the same way, so "which room is the robot in" is answered from data the
 * binding already keeps, without changing the map wire format or {@link RRMapData}'s shape.
 * <p>
 * The lookup itself is position-agnostic: the same map coordinate space holds the robot position,
 * the charger position and the path points, so the caller decides which of them to ask about. The
 * handler asks for the robot's position while it drives and for the charging dock's position while
 * it is docked, where the robot's own position in a map may lag behind reality.
 * <p>
 * Coordinates: {@code positionX}/{@code positionY} are in the RR map's raw coordinate units (as
 * parsed by {@code RRMapParser}, the same units as {@code robotX}/{@code robotY},
 * {@code chargerX}/{@code chargerY} and path points). They are converted to a pixel index using the
 * same fixed divisor and {@code top}/{@code left} offsets as
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
     * Maximum Chebyshev-distance ring searched outward from the queried pixel when that pixel is
     * not itself a segmented-floor pixel (for example the robot is on a wall/scan boundary pixel,
     * or the charging dock sits at the edge of a segment). Two rings is a small, cheap search that
     * still covers the robot's immediate footprint without risking a false match from a distant
     * room.
     */
    private static final int FALLBACK_SEARCH_RADIUS = 2;

    private RoomAtRobotResolver() {
    }

    /**
     * Resolves the segment id of the map pixel at the given position, falling back to a small
     * outward search if that exact pixel is not a segmented-floor pixel.
     * <p>
     * The fallback is decided by the nearest ring that contains any segmented-floor pixel at all:
     * all of that ring's pixels are collected and the segment holding the strict majority wins. A
     * ring is deliberately never resolved by "first pixel encountered", because at a doorway or
     * room boundary one ring can hold pixels of two different rooms and the answer would then
     * depend on the scan order. If the nearest populated ring is a tie between segments, the
     * position is genuinely ambiguous and this resolves to empty rather than guessing; wider rings
     * are not consulted, since they are weaker evidence than the tied one.
     *
     * @param mapData parsed map data; {@code imageData()}/{@code imageWidth()}/{@code imageHeight()}
     *            /{@code top()}/{@code left()} are used to locate and decode the pixel
     * @param positionX X position in raw RR map coordinate units
     * @param positionY Y position in raw RR map coordinate units
     * @return the segment id (1-30, the range {@link RRMapRenderer#decodeSegmentId(int)} can yield)
     *         if one was unambiguously found at or near the given position, empty otherwise
     */
    public static Optional<Integer> resolveSegmentId(RRMapData mapData, int positionX, int positionY) {
        int width = mapData.imageWidth();
        int height = mapData.imageHeight();
        byte[] imageData = mapData.imageData();
        // The dimensions are unvalidated uint32 values from the map payload, so their product is
        // computed as a long: 65536 x 65536 wraps to 0 in int arithmetic and would pass a guard
        // that multiplies in int, after which the pixel lookups below - which only compare against
        // width and height - would index far outside imageData. Comparing the long product also
        // makes every in-bounds pixel index fit into an int, because a passing check implies
        // width * height <= imageData.length <= Integer.MAX_VALUE.
        if (width <= 0 || height <= 0 || imageData.length < (long) width * height) {
            return Optional.empty();
        }

        int centerX = Math.round(positionX / (float) MM) - mapData.left() - 1;
        int centerY = Math.round(positionY / (float) MM) - mapData.top() - 1;

        Optional<Integer> exactHit = segmentAt(imageData, width, height, centerX, centerY);
        if (exactHit.isPresent()) {
            return exactHit;
        }
        for (int radius = 1; radius <= FALLBACK_SEARCH_RADIUS; radius++) {
            Map<Integer, Integer> ringCandidates = collectRing(imageData, width, height, centerX, centerY, radius);
            if (!ringCandidates.isEmpty()) {
                return dominantSegment(ringCandidates);
            }
        }
        return Optional.empty();
    }

    /**
     * Counts the segmented-floor pixels per segment id on the outer ring of the given radius.
     * Pixels of smaller radii belong to rings already evaluated and are skipped.
     */
    private static Map<Integer, Integer> collectRing(byte[] imageData, int width, int height, int centerX, int centerY,
            int radius) {
        Map<Integer, Integer> segmentCounts = new HashMap<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) {
                    continue;
                }
                segmentAt(imageData, width, height, centerX + dx, centerY + dy)
                        .ifPresent(segmentId -> segmentCounts.merge(segmentId, 1, Integer::sum));
            }
        }
        return segmentCounts;
    }

    /**
     * Returns the segment id with the strictly highest pixel count, or empty when the highest count
     * is shared by more than one segment.
     */
    private static Optional<Integer> dominantSegment(Map<Integer, Integer> segmentCounts) {
        int dominantId = -1;
        int highestCount = 0;
        boolean tied = false;
        for (Map.Entry<Integer, Integer> candidate : segmentCounts.entrySet()) {
            int count = candidate.getValue();
            if (count > highestCount) {
                highestCount = count;
                dominantId = candidate.getKey();
                tied = false;
            } else if (count == highestCount) {
                tied = true;
            }
        }
        return tied || dominantId < 0 ? Optional.empty() : Optional.of(dominantId);
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

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
 * Resolves the map segment (room) id at a map position, decoding pixels the same way as
 * {@link RRMapRenderer}.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public final class RoomAtRobotResolver {

    private static final int MM = 50;

    private static final int FALLBACK_SEARCH_RADIUS = 2;

    private RoomAtRobotResolver() {
    }

    /** The segment id at the given position, or of the nearest ring holding a strict majority. */
    public static Optional<Integer> resolveSegmentId(RRMapData mapData, int positionX, int positionY) {
        int width = mapData.imageWidth();
        int height = mapData.imageHeight();
        byte[] imageData = mapData.imageData();
        // The dimensions are unvalidated uint32 values whose product can wrap in int arithmetic.
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

    private static Optional<Integer> dominantSegment(Map<Integer, Integer> segmentCounts) {
        int dominantId = -1;
        int highestCount = 0;
        int totalCount = 0;
        for (Map.Entry<Integer, Integer> candidate : segmentCounts.entrySet()) {
            int count = candidate.getValue();
            totalCount += count;
            if (count > highestCount) {
                highestCount = count;
                dominantId = candidate.getKey();
            }
        }
        boolean holdsStrictMajority = highestCount * 2 > totalCount;
        return holdsStrictMajority ? Optional.of(dominantId) : Optional.empty();
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

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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parsed Roborock RR map content.
 *
 * @author reyhard - Initial contribution
 */
@NonNullByDefault
public record RRMapData(int imageWidth, int imageHeight, int top, int left, byte[] imageData, @Nullable Integer robotX,
        @Nullable Integer robotY, @Nullable Integer robotAngle, @Nullable Integer chargerX, @Nullable Integer chargerY,
        @Nullable Integer gotoTargetX, @Nullable Integer gotoTargetY, List<MapPoint> basicPath, List<MapPoint> gotoPath,
        List<MapPoint> predictedPath, List<MapZone> cleanedZones, List<MapWall> virtualWalls, List<MapArea> noGoAreas,
        List<MapArea> mopForbiddenAreas, List<MapArea> carpetForbiddenAreas, List<MapObstacle> obstacles,
        List<MapObstacle> ignoredObstacles, byte[] mopPathMask, byte[] carpetMapMask) {

    public RRMapData {
        imageData = imageData.clone();
        mopPathMask = mopPathMask.clone();
        carpetMapMask = carpetMapMask.clone();
    }

    public RRMapData(int imageWidth, int imageHeight, int top, int left, byte[] imageData, @Nullable Integer robotX,
            @Nullable Integer robotY, @Nullable Integer robotAngle, @Nullable Integer chargerX,
            @Nullable Integer chargerY, @Nullable Integer gotoTargetX, @Nullable Integer gotoTargetY,
            List<MapPoint> basicPath, List<MapPoint> gotoPath, List<MapPoint> predictedPath, List<MapZone> cleanedZones,
            List<MapWall> virtualWalls, List<MapArea> noGoAreas, List<MapArea> mopForbiddenAreas,
            List<MapArea> carpetForbiddenAreas, List<MapObstacle> obstacles, List<MapObstacle> ignoredObstacles,
            byte[] mopPathMask) {
        this(imageWidth, imageHeight, top, left, imageData, robotX, robotY, robotAngle, chargerX, chargerY, gotoTargetX,
                gotoTargetY, basicPath, gotoPath, predictedPath, cleanedZones, virtualWalls, noGoAreas,
                mopForbiddenAreas, carpetForbiddenAreas, obstacles, ignoredObstacles, mopPathMask, new byte[0]);
    }

    @Override
    public byte[] imageData() {
        return imageData.clone();
    }

    @Override
    public byte[] mopPathMask() {
        return mopPathMask.clone();
    }

    @Override
    public byte[] carpetMapMask() {
        return carpetMapMask.clone();
    }

    public record MapPoint(int x, int y) {
    }

    public record MapZone(int x0, int y0, int x1, int y1) {
    }

    public record MapWall(int x0, int y0, int x1, int y1) {
    }

    public record MapArea(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3) {
    }

    public record MapObstacle(int x, int y, int type, @Nullable String typeLabel) {
        public MapObstacle(int x, int y, int type) {
            this(x, y, type, null);
        }
    }
}

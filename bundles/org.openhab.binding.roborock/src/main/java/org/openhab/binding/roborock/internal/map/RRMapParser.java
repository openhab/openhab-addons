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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal RR map parser for extracting stable map metadata and image block.
 *
 * @author reyhard - Initial contribution
 */
@NonNullByDefault
public final class RRMapParser {
    /** Block type for charger position data. */
    public static final int BLOCK_CHARGER = 1;
    /** Block type for map image data and image geometry. */
    public static final int BLOCK_IMAGE = 2;
    /** Block type for the already cleaned path polyline. */
    public static final int BLOCK_PATH = 3;
    /** Block type for the go-to path polyline. */
    public static final int BLOCK_GOTO_PATH = 4;
    /** Block type for predicted path points. */
    public static final int BLOCK_PREDICTED_PATH = 5;
    /** Block type for currently cleaned rectangular zones. */
    public static final int BLOCK_CURRENTLY_CLEANED_ZONES = 6;
    /** Block type for go-to target coordinates. */
    public static final int BLOCK_GOTO_TARGET = 7;
    /** Block type for current robot position and optional heading. */
    public static final int BLOCK_ROBOT_POSITION = 8;
    /** Block type for no-go polygon areas. */
    public static final int BLOCK_NO_GO_AREAS = 9;
    /** Block type for virtual wall segments. */
    public static final int BLOCK_VIRTUAL_WALLS = 10;
    /** Block type for map block metadata currently ignored by this parser. */
    public static final int BLOCK_BLOCKS = 11;
    /** Block type for mop-forbidden polygon areas. */
    public static final int BLOCK_MOP_FORBIDDEN_AREAS = 12;
    /** Block type for detected obstacles without photo payload. */
    public static final int BLOCK_OBSTACLES = 13;
    /** Block type for ignored obstacles without photo payload. */
    public static final int BLOCK_IGNORED_OBSTACLES = 14;
    /** Block type for detected obstacles with extended (photo-capable) payload. */
    public static final int BLOCK_OBSTACLES_WITH_PHOTO = 15;
    /** Block type for ignored obstacles with extended (photo-capable) payload. */
    public static final int BLOCK_IGNORED_OBSTACLES_WITH_PHOTO = 16;
    /** Block type for carpet map mask. */
    public static final int BLOCK_CARPET_MAP = 17;
    /** Block type for mop path mask. */
    public static final int BLOCK_MOP_PATH = 18;
    /** Block type for carpet-forbidden polygon areas. */
    public static final int BLOCK_CARPET_FORBIDDEN_AREAS = 19;
    /** Block type for digest/summary payload currently ignored by this parser. */
    public static final int BLOCK_DIGEST = 1024;

    private static final int MAIN_HEADER_LENGTH_OFFSET = 0x02;
    private static final int BLOCK_TYPE_OFFSET = 0x00;
    private static final int BLOCK_HEADER_LENGTH_OFFSET = 0x02;
    private static final int BLOCK_DATA_LENGTH_OFFSET = 0x04;

    private static final Logger LOGGER = LoggerFactory.getLogger(RRMapParser.class);

    public RRMapData parse(byte[] raw) throws RoborockException {
        if (raw.length < 12) {
            throw new RoborockException("RR map payload too short.");
        }

        int blockStartPos = readUInt16LE(raw, MAIN_HEADER_LENGTH_OFFSET);
        if (blockStartPos <= 0 || blockStartPos > raw.length) {
            throw new RoborockException("Invalid RR map main header length.");
        }

        int imageWidth = 0;
        int imageHeight = 0;
        int top = 0;
        int left = 0;
        byte[] imageData = new byte[0];
        @Nullable
        Integer robotX = null;
        @Nullable
        Integer robotY = null;
        @Nullable
        Integer robotAngle = null;
        @Nullable
        Integer chargerX = null;
        @Nullable
        Integer chargerY = null;
        @Nullable
        Integer gotoTargetX = null;
        @Nullable
        Integer gotoTargetY = null;
        List<RRMapData.MapPoint> basicPath = new ArrayList<>();
        List<RRMapData.MapPoint> gotoPath = new ArrayList<>();
        List<RRMapData.MapPoint> predictedPath = new ArrayList<>();
        List<RRMapData.MapZone> cleanedZones = new ArrayList<>();
        List<RRMapData.MapWall> virtualWalls = new ArrayList<>();
        List<RRMapData.MapArea> noGoAreas = new ArrayList<>();
        List<RRMapData.MapArea> mopForbiddenAreas = new ArrayList<>();
        List<RRMapData.MapArea> carpetForbiddenAreas = new ArrayList<>();
        List<RRMapData.MapObstacle> obstacles = new ArrayList<>();
        List<RRMapData.MapObstacle> ignoredObstacles = new ArrayList<>();
        byte[] mopPathMask = new byte[0];
        byte[] carpetMapMask = new byte[0];

        while (blockStartPos + 8 <= raw.length) {
            int blockHeaderLength = readUInt16LE(raw, blockStartPos + BLOCK_HEADER_LENGTH_OFFSET);
            int blockType = readUInt16LE(raw, blockStartPos + BLOCK_TYPE_OFFSET);
            int blockDataLength = readUInt32LE(raw, blockStartPos + BLOCK_DATA_LENGTH_OFFSET);

            if (blockHeaderLength <= 0 || blockStartPos + blockHeaderLength > raw.length) {
                throw new RoborockException("Invalid RR map block header length.");
            }

            int blockDataStart = blockStartPos + blockHeaderLength;
            long blockDataEndLong = (long) blockDataStart + blockDataLength;
            if (blockDataLength < 0 || blockDataEndLong > raw.length || blockDataEndLong < blockDataStart) {
                throw new RoborockException("Invalid RR map block data length.");
            }
            int blockDataEnd = (int) blockDataEndLong;

            switch (blockType) {
                case BLOCK_IMAGE:
                    if (blockHeaderLength >= 16) {
                        top = readUInt32LE(raw, blockStartPos + blockHeaderLength - 16);
                        left = readUInt32LE(raw, blockStartPos + blockHeaderLength - 12);
                        imageHeight = readUInt32LE(raw, blockStartPos + blockHeaderLength - 8);
                        imageWidth = readUInt32LE(raw, blockStartPos + blockHeaderLength - 4);
                    }
                    imageData = Arrays.copyOfRange(raw, blockDataStart, blockDataEnd);
                    break;
                case BLOCK_ROBOT_POSITION:
                    if (blockDataLength >= 8) {
                        robotX = readUInt32LE(raw, blockDataStart);
                        robotY = readUInt32LE(raw, blockDataStart + 4);
                        if (blockDataLength >= 12) {
                            robotAngle = readUInt32LE(raw, blockDataStart + 8);
                        }
                    }
                    break;
                case BLOCK_CHARGER:
                    if (blockDataLength >= 8) {
                        chargerX = readUInt32LE(raw, blockDataStart);
                        chargerY = readUInt32LE(raw, blockDataStart + 4);
                    }
                    break;
                case BLOCK_PATH:
                    parsePath(raw, blockDataStart, blockDataLength, basicPath);
                    break;
                case BLOCK_GOTO_PATH:
                    parsePath(raw, blockDataStart, blockDataLength, gotoPath);
                    break;
                case BLOCK_PREDICTED_PATH:
                    parsePath(raw, blockDataStart, blockDataLength, predictedPath);
                    break;
                case BLOCK_CURRENTLY_CLEANED_ZONES:
                    for (int i = 0; i + 7 < blockDataLength; i += 8) {
                        int x0 = readUInt16LE(raw, blockDataStart + i);
                        int y0 = readUInt16LE(raw, blockDataStart + i + 2);
                        int x1 = readUInt16LE(raw, blockDataStart + i + 4);
                        int y1 = readUInt16LE(raw, blockDataStart + i + 6);
                        cleanedZones.add(new RRMapData.MapZone(x0, y0, x1, y1));
                    }
                    break;
                case BLOCK_GOTO_TARGET:
                    if (blockDataLength >= 4) {
                        gotoTargetX = readUInt16LE(raw, blockDataStart);
                        gotoTargetY = readUInt16LE(raw, blockDataStart + 2);
                    }
                    break;
                case BLOCK_VIRTUAL_WALLS:
                    parseWalls(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength, virtualWalls);
                    break;
                case BLOCK_NO_GO_AREAS:
                    parseAreas(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength, noGoAreas);
                    break;
                case BLOCK_MOP_FORBIDDEN_AREAS:
                    parseAreas(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength,
                            mopForbiddenAreas);
                    break;
                case BLOCK_CARPET_FORBIDDEN_AREAS:
                    parseAreas(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength,
                            carpetForbiddenAreas);
                    break;
                case BLOCK_MOP_PATH:
                    mopPathMask = Arrays.copyOfRange(raw, blockDataStart, blockDataEnd);
                    break;
                case BLOCK_CARPET_MAP:
                    carpetMapMask = Arrays.copyOfRange(raw, blockDataStart, blockDataEnd);
                    break;
                case BLOCK_OBSTACLES:
                    parseObstacles(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength, obstacles,
                            5);
                    break;
                case BLOCK_IGNORED_OBSTACLES:
                    parseObstacles(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength,
                            ignoredObstacles, 5);
                    break;
                case BLOCK_OBSTACLES_WITH_PHOTO:
                    parseObstacles(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength, obstacles,
                            28);
                    break;
                case BLOCK_IGNORED_OBSTACLES_WITH_PHOTO:
                    parseObstacles(raw, blockStartPos, blockHeaderLength, blockDataStart, blockDataLength,
                            ignoredObstacles, 28);
                    break;
                case BLOCK_BLOCKS:
                case BLOCK_DIGEST:
                    break;
                default:
                    LOGGER.trace("Ignoring unsupported RR map block type {}", blockType);
                    break;
            }

            blockStartPos += blockHeaderLength + blockDataLength;
        }

        if (imageData.length == 0) {
            throw new RoborockException("RR map does not contain image data.");
        }

        return new RRMapData(imageWidth, imageHeight, top, left, imageData, robotX, robotY, robotAngle, chargerX,
                chargerY, gotoTargetX, gotoTargetY, List.copyOf(basicPath), List.copyOf(gotoPath),
                List.copyOf(predictedPath), List.copyOf(cleanedZones), List.copyOf(virtualWalls),
                List.copyOf(noGoAreas), List.copyOf(mopForbiddenAreas), List.copyOf(carpetForbiddenAreas),
                List.copyOf(obstacles), List.copyOf(ignoredObstacles), mopPathMask, carpetMapMask);
    }

    private void parsePath(byte[] raw, int blockDataStart, int blockDataLength, List<RRMapData.MapPoint> target) {
        for (int i = 0; i + 3 < blockDataLength; i += 4) {
            int x = readUInt16LE(raw, blockDataStart + i);
            int y = readUInt16LE(raw, blockDataStart + i + 2);
            target.add(new RRMapData.MapPoint(x, y));
        }
    }

    private void parseWalls(byte[] raw, int blockStartPos, int blockHeaderLength, int blockDataStart,
            int blockDataLength, List<RRMapData.MapWall> walls) {
        int points = blockDataLength / 8;
        if (blockHeaderLength >= 10) {
            points = Math.min(points, readUInt16LE(raw, blockStartPos + 8));
        }

        for (int wallIndex = 0; wallIndex < points; wallIndex++) {
            int offset = blockDataStart + wallIndex * 8;
            int x0 = readUInt16LE(raw, offset);
            int y0 = readUInt16LE(raw, offset + 2);
            int x1 = readUInt16LE(raw, offset + 4);
            int y1 = readUInt16LE(raw, offset + 6);
            walls.add(new RRMapData.MapWall(x0, y0, x1, y1));
        }
    }

    private void parseAreas(byte[] raw, int blockStartPos, int blockHeaderLength, int blockDataStart,
            int blockDataLength, List<RRMapData.MapArea> target) {
        int points = blockDataLength / 16;
        if (blockHeaderLength >= 10) {
            points = Math.min(points, readUInt16LE(raw, blockStartPos + 8));
        }

        for (int areaIndex = 0; areaIndex < points; areaIndex++) {
            int offset = blockDataStart + areaIndex * 16;
            int x0 = readUInt16LE(raw, offset);
            int y0 = readUInt16LE(raw, offset + 2);
            int x1 = readUInt16LE(raw, offset + 4);
            int y1 = readUInt16LE(raw, offset + 6);
            int x2 = readUInt16LE(raw, offset + 8);
            int y2 = readUInt16LE(raw, offset + 10);
            int x3 = readUInt16LE(raw, offset + 12);
            int y3 = readUInt16LE(raw, offset + 14);
            target.add(new RRMapData.MapArea(x0, y0, x1, y1, x2, y2, x3, y3));
        }
    }

    private void parseObstacles(byte[] raw, int blockStartPos, int blockHeaderLength, int blockDataStart,
            int blockDataLength, List<RRMapData.MapObstacle> target, int bytesPerRecord) {
        int points = blockDataLength / bytesPerRecord;
        if (blockHeaderLength >= 10) {
            points = Math.min(points, readUInt16LE(raw, blockStartPos + 8));
        }

        for (int i = 0; i < points; i++) {
            int offset = blockDataStart + i * bytesPerRecord;
            int x = readUInt16LE(raw, offset);
            int y = readUInt16LE(raw, offset + 2);
            int type = bytesPerRecord >= 6 ? readUInt16LE(raw, offset + 4) : raw[offset + 4] & 0xFF;
            target.add(new RRMapData.MapObstacle(x, y, type, RRMapRenderer.obstacleTypeLabel(type)));
        }
    }

    private int readUInt16LE(byte[] bytes, int pos) {
        int value = bytes[pos] & 0xFF;
        value |= (bytes[pos + 1] << 8) & 0xFFFF;
        return value;
    }

    private int readUInt32LE(byte[] bytes, int pos) {
        int value = bytes[pos] & 0xFF;
        value |= (bytes[pos + 1] << 8) & 0xFFFF;
        value |= (bytes[pos + 2] << 16) & 0xFFFFFF;
        value |= (bytes[pos + 3] << 24) & 0xFFFFFFFF;
        return value;
    }
}

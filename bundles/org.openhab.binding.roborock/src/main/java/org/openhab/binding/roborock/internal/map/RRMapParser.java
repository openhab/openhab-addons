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
 * @author OpenHAB project contribution
 */
@NonNullByDefault
public final class RRMapParser {
    public static final int BLOCK_CHARGER = 1;
    public static final int BLOCK_IMAGE = 2;
    public static final int BLOCK_PATH = 3;
    public static final int BLOCK_CURRENTLY_CLEANED_ZONES = 6;
    public static final int BLOCK_ROBOT_POSITION = 8;

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
        List<RRMapData.MapPoint> basicPath = new ArrayList<>();
        List<RRMapData.MapZone> cleanedZones = new ArrayList<>();

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
                    for (int i = 0; i + 3 < blockDataLength; i += 4) {
                        int x = readUInt16LE(raw, blockDataStart + i);
                        int y = readUInt16LE(raw, blockDataStart + i + 2);
                        basicPath.add(new RRMapData.MapPoint(x, y));
                    }
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
                chargerY, List.copyOf(basicPath), List.copyOf(cleanedZones));
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

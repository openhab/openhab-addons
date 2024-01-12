/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.robot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RRMapFileParser} is used to parse the RR map file format created by Xiaomi / RockRobo vacuum
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class RRMapFileParser {
    public static final int CHARGER = 1;
    public static final int IMAGE = 2;
    public static final int PATH = 3;
    public static final int GOTO_PATH = 4;
    public static final int GOTO_PREDICTED_PATH = 5;
    public static final int CURRENTLY_CLEANED_ZONES = 6;
    public static final int GOTO_TARGET = 7;
    public static final int ROBOT_POSITION = 8;
    public static final int NO_GO_AREAS = 9;
    public static final int VIRTUAL_WALLS = 10;
    public static final int BLOCKS = 11;
    public static final int MOB_FORBIDDEN_AREA = 12;
    public static final int OBSTACLES = 13;
    public static final int IGNORED_OBSTACLES = 14;
    public static final int OBSTACLES2 = 15;
    public static final int IGNORED_OBSTACLES2 = 16;
    public static final int CARPET_MAP = 17;
    public static final int MOP_PATH = 18;
    public static final int CARPET_FORBIDDEN_AREA = 19;
    public static final int SMART_ZONES_PATH_TYPE = 20;
    public static final int SMART_ZONES = 21;
    public static final int CUSTOM_CARPET = 22;
    public static final int CL_FORBIDDEN_ZONES = 23;
    public static final int FLOOR_MAP = 24;
    public static final int FURNITURES = 25;
    public static final int DOCK_TYPE = 26;
    public static final int ENEMIES = 27;
    public static final int DOOR_ZONES = 28;
    public static final int STUCK_POINTS = 29;
    public static final int CLIFF_ZONES = 30;
    public static final int SMARTDS = 31;
    public static final int FLDIREC = 32;
    public static final int MAP_DATE = 33;
    public static final int NONCE_DATA = 34;
    public static final int DIGEST = 1024;
    public static final int HEADER = 0x7272;

    public static final String PATH_POINT_LENGTH = "pointLength";
    public static final String PATH_POINT_SIZE = "pointSize";
    public static final String PATH_ANGLE = "angle";

    private byte[] image = new byte[] { 0 };
    private final int majorVersion;
    private final int minorVersion;
    private final int mapIndex;
    private final int mapSequence;
    private boolean isValid;

    private int imgHeight;
    private int imgWidth;
    private int imageSize;
    private int top;
    private int left;

    private int chargerX;
    private int chargerY;
    private int roboX;
    private int roboY;
    private int roboA;
    private float gotoX = 0;
    private float gotoY = 0;
    private Map<Integer, ArrayList<float[]>> paths = new HashMap<>();
    private Map<Integer, Map<String, Integer>> pathsDetails = new HashMap<>();
    private Map<Integer, ArrayList<float[]>> areas = new HashMap<>();
    private ArrayList<float[]> walls = new ArrayList<>();
    private ArrayList<float[]> zones = new ArrayList<>();
    private Map<Integer, ArrayList<int[]>> obstacles = new HashMap<>();
    private byte[] blocks = new byte[0];
    private int[] carpetMap = {};
    private int[] mopPath = {};

    private final Logger logger = LoggerFactory.getLogger(RRMapFileParser.class);

    public RRMapFileParser(byte[] raw) {
        boolean printBlockDetails = false;
        int mapHeaderLength = getUInt16(raw, 0x02);
        int mapDataLength = getUInt32LE(raw, 0x04);
        this.majorVersion = getUInt16(raw, 0x08);
        this.minorVersion = getUInt16(raw, 0x0A);
        this.mapIndex = getUInt32LE(raw, 0x0C);
        this.mapSequence = getUInt32LE(raw, 0x10);

        int blockStartPos = getUInt16(raw, 0x02); // main header length
        while (blockStartPos < raw.length) {
            int blockHeaderLength = getUInt16(raw, blockStartPos + 0x02);
            byte[] header = getBytes(raw, blockStartPos, blockHeaderLength);
            int blocktype = getUInt16(header, 0x00);
            int blockDataLength = getUInt32LE(header, 0x04);
            int blockDataStart = blockStartPos + blockHeaderLength;
            byte[] data = getBytes(raw, blockDataStart, blockDataLength);

            switch (blocktype) {
                case CHARGER:
                    this.chargerX = getUInt32LE(raw, blockStartPos + 0x08);
                    this.chargerY = getUInt32LE(raw, blockStartPos + 0x0C);
                    break;
                case IMAGE:
                    this.imageSize = blockDataLength;// (getUInt32LE(raw, blockStartPos + 0x04));
                    if (blockHeaderLength > 0x1C) {
                        logger.debug("block 2 unknown value @pos 8: {}", getUInt32LE(header, 0x08));
                    }
                    this.top = getUInt32LE(header, blockHeaderLength - 16);
                    this.left = getUInt32LE(header, blockHeaderLength - 12);
                    this.imgHeight = (getUInt32LE(header, blockHeaderLength - 8));
                    this.imgWidth = getUInt32LE(header, blockHeaderLength - 4);
                    this.image = data;
                    break;
                case ROBOT_POSITION:
                    this.roboX = getUInt32LE(data, 0x00);
                    this.roboY = getUInt32LE(data, 0x04);
                    if (blockDataLength > 8) { // model S6
                        this.roboA = getUInt32LE(data, 0x08);
                    }
                    break;
                case PATH:
                case GOTO_PATH:
                case GOTO_PREDICTED_PATH:
                    ArrayList<float[]> path = new ArrayList<>();
                    Map<String, Integer> detail = new HashMap<>();
                    int pairs = getUInt32LE(header, 0x04) / 4;
                    detail.put(PATH_POINT_LENGTH, getUInt32LE(header, 0x08));
                    detail.put(PATH_POINT_SIZE, getUInt32LE(header, 0x0C));
                    detail.put(PATH_ANGLE, getUInt32LE(header, 0x10));
                    for (int pathpair = 0; pathpair < pairs; pathpair++) {
                        float x = (getUInt16(getBytes(raw, blockDataStart + pathpair * 4, 2)));
                        float y = getUInt16(getBytes(raw, blockDataStart + pathpair * 4 + 2, 2));
                        path.add(new float[] { x, y });
                    }
                    paths.put(blocktype, path);
                    pathsDetails.put(blocktype, detail);
                    break;
                case CURRENTLY_CLEANED_ZONES:
                    int zonePairs = getUInt16(header, 0x08);
                    for (int zonePair = 0; zonePair < zonePairs; zonePair++) {
                        float x0 = (getUInt16(raw, blockDataStart + zonePair * 8));
                        float y0 = getUInt16(raw, blockDataStart + zonePair * 8 + 2);
                        float x1 = (getUInt16(raw, blockDataStart + zonePair * 8 + 4));
                        float y1 = getUInt16(raw, blockDataStart + zonePair * 8 + 6);
                        zones.add(new float[] { x0, y0, x1, y1 });
                    }
                    break;
                case GOTO_TARGET:
                    this.gotoX = getUInt16(data, 0x00);
                    this.gotoY = getUInt16(data, 0x02);
                    break;
                case DIGEST:
                    isValid = Arrays.equals(data, sha1Hash(getBytes(raw, 0, mapHeaderLength + mapDataLength - 20)));
                    break;
                case VIRTUAL_WALLS:
                    int wallPairs = getUInt16(header, 0x08);
                    for (int wallPair = 0; wallPair < wallPairs; wallPair++) {
                        float x0 = (getUInt16(raw, blockDataStart + wallPair * 8));
                        float y0 = getUInt16(raw, blockDataStart + wallPair * 8 + 2);
                        float x1 = (getUInt16(raw, blockDataStart + wallPair * 8 + 4));
                        float y1 = getUInt16(raw, blockDataStart + wallPair * 8 + 6);
                        walls.add(new float[] { x0, y0, x1, y1 });
                    }
                    break;
                case NO_GO_AREAS:
                case MOB_FORBIDDEN_AREA:
                case CARPET_FORBIDDEN_AREA:
                    int areaPairs = getUInt16(header, 0x08);
                    ArrayList<float[]> area = new ArrayList<>();
                    for (int areaPair = 0; areaPair < areaPairs; areaPair++) {
                        float x0 = (getUInt16(raw, blockDataStart + areaPair * 16));
                        float y0 = getUInt16(raw, blockDataStart + areaPair * 16 + 2);
                        float x1 = (getUInt16(raw, blockDataStart + areaPair * 16 + 4));
                        float y1 = getUInt16(raw, blockDataStart + areaPair * 16 + 6);
                        float x2 = (getUInt16(raw, blockDataStart + areaPair * 16 + 8));
                        float y2 = getUInt16(raw, blockDataStart + areaPair * 16 + 10);
                        float x3 = (getUInt16(raw, blockDataStart + areaPair * 16 + 12));
                        float y3 = getUInt16(raw, blockDataStart + areaPair * 16 + 14);
                        area.add(new float[] { x0, y0, x1, y1, x2, y2, x3, y3 });
                    }
                    areas.put(Integer.valueOf(blocktype & 0xFF), area);
                    break;
                case OBSTACLES:
                case IGNORED_OBSTACLES:
                    int obstaclePairs = getUInt16(header, 0x08);
                    ArrayList<int[]> obstacle = new ArrayList<>();
                    for (int obstaclePair = 0; obstaclePair < obstaclePairs; obstaclePair++) {
                        int x0 = getUInt16(data, obstaclePair * 5 + 0);
                        int y0 = getUInt16(data, obstaclePair * 5 + 2);
                        int u = data[obstaclePair * 5 + 0] & 0xFF;
                        obstacle.add(new int[] { x0, y0, u });
                    }
                    obstacles.put(Integer.valueOf(blocktype & 0xFF), obstacle);
                    break;
                case OBSTACLES2:
                    int obstacle2Pairs = getUInt16(header, 0x08);
                    if (obstacle2Pairs == 0) {
                        break;
                    }
                    int obstacleDataLenght = blockDataLength / obstacle2Pairs;
                    logger.trace("block 15 records lenght: {}", obstacleDataLenght);
                    ArrayList<int[]> obstacle2 = new ArrayList<>();
                    for (int obstaclePair = 0; obstaclePair < obstacle2Pairs; obstaclePair++) {
                        int x0 = getUInt16(data, obstaclePair * obstacleDataLenght + 0);
                        int y0 = getUInt16(data, obstaclePair * obstacleDataLenght + 2);
                        int u0 = getUInt16(data, obstaclePair * obstacleDataLenght + 4);
                        int u1 = getUInt16(data, obstaclePair * obstacleDataLenght + 6);
                        int u2 = getUInt32LE(data, obstaclePair * obstacleDataLenght + 8);
                        if (obstacleDataLenght == 28) {
                            if ((data[obstaclePair * obstacleDataLenght + 12] & 0xFF) == 0) {
                                logger.trace("obstacle with photo: No text");
                            } else {
                                byte[] txt = getBytes(data, obstaclePair * obstacleDataLenght + 12, 16);
                                logger.trace("obstacle with photo: {}", new String(txt));
                            }
                            obstacle2.add(new int[] { x0, y0, u0, u1, u2 });
                        } else {
                            int u3 = getUInt32LE(data, obstaclePair * obstacleDataLenght + 12);
                            obstacle2.add(new int[] { x0, y0, u0, u1, u2, u3 });
                            logger.trace("obstacle without photo.");
                        }
                    }
                    obstacles.put(Integer.valueOf(blocktype & 0xFF), obstacle2);
                    break;
                case IGNORED_OBSTACLES2:
                    int ignoredObstaclePairs = getUInt16(header, 0x08);
                    ArrayList<int[]> ignoredObstacle = new ArrayList<>();
                    for (int obstaclePair = 0; obstaclePair < ignoredObstaclePairs; obstaclePair++) {
                        int x0 = getUInt16(data, obstaclePair * 6 + 0);
                        int y0 = getUInt16(data, obstaclePair * 6 + 2);
                        int u = getUInt16(data, obstaclePair * 6 + 4);
                        ignoredObstacle.add(new int[] { x0, y0, u });
                    }
                    obstacles.put(Integer.valueOf(blocktype & 0xFF), ignoredObstacle);
                    break;
                case CARPET_MAP:
                    carpetMap = new int[blockDataLength];
                    for (int carpetNode = 0; carpetNode < blockDataLength; carpetNode++) {
                        carpetMap[carpetNode] = data[carpetNode] & 0xFF;
                    }
                    break;
                case MOP_PATH:
                    mopPath = new int[blockDataLength];
                    for (int mopNode = 0; mopNode < blockDataLength; mopNode++) {
                        mopPath[mopNode] = data[mopNode] & 0xFF;
                    }
                    break;
                case BLOCKS:
                    int blocksPairs = getUInt16(header, 0x08);
                    blocks = getBytes(data, 0, blocksPairs);
                    break;
                case SMART_ZONES_PATH_TYPE:
                case SMART_ZONES:
                case CUSTOM_CARPET:
                case CL_FORBIDDEN_ZONES:
                case FLOOR_MAP:
                case FURNITURES:
                case DOCK_TYPE:
                case ENEMIES:
                case DOOR_ZONES:
                case STUCK_POINTS:
                case CLIFF_ZONES:
                case SMARTDS:
                case FLDIREC:
                case MAP_DATE:
                case NONCE_DATA:
                    // new blocktypes not yet decoded
                    break;
                default:
                    logger.info("Unknown blocktype {} (pls report to author)", blocktype);
                    printBlockDetails = true;
            }
            if (logger.isTraceEnabled() || printBlockDetails) {
                logger.debug("Blocktype: {}", Integer.toString(blocktype));
                logger.debug("Header len: {}   data len: {} ", Integer.toString(blockHeaderLength),
                        Integer.toString(blockDataLength));
                logger.debug("H: {}", Utils.getSpacedHex(header));
                if (blockDataLength > 0) {
                    logger.debug("D: {}", (blockDataLength < 60 ? Utils.getSpacedHex(data)
                            : Utils.getSpacedHex(getBytes(data, 0, 60))));
                }
                printBlockDetails = false;
            }
            blockStartPos = blockStartPos + blockDataLength + (header[2] & 0xFF);
        }
    }

    public static byte[] readRRMapFile(File file) throws IOException {
        return readRRMapFile(new FileInputStream(file));
    }

    public static byte[] readRRMapFile(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream in = new GZIPInputStream(is)) {
            int bufsize = 1024;
            byte[] buf = new byte[bufsize];
            int readbytes = 0;
            readbytes = in.read(buf);
            while (readbytes != -1) {
                baos.write(buf, 0, readbytes);
                readbytes = in.read(buf);
            }
            baos.flush();
            return baos.toByteArray();
        }
    }

    private byte[] getBytes(byte[] raw, int pos, int len) {
        return java.util.Arrays.copyOfRange(raw, pos, pos + len);
    }

    private int getUInt32LE(byte[] bytes, int pos) {
        int value = bytes[0 + pos] & 0xFF;
        value |= (bytes[1 + pos] << 8) & 0xFFFF;
        value |= (bytes[2 + pos] << 16) & 0xFFFFFF;
        value |= (bytes[3 + pos] << 24) & 0xFFFFFFFF;
        return value;
    }

    private int getUInt16(byte[] bytes) {
        return getUInt16(bytes, 0);
    }

    private int getUInt16(byte[] bytes, int pos) {
        int value = bytes[0 + pos] & 0xFF;
        value |= (bytes[1 + pos] << 8) & 0xFFFF;
        return value;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.printf("RR Map:\tMajor Version: %d Minor version: %d Map Index: %d Map Sequence: %d\r\n", majorVersion,
                minorVersion, mapIndex, mapSequence);
        pw.printf("Image:\tsize: %9d\ttop: %9d\tleft: %9d height: %9d width: %9d\r\n", imageSize, top, left, imgHeight,
                imgWidth);
        pw.printf("Charger pos:\tX: %.0f\tY: %.0f\r\n", getChargerX(), getChargerY());
        pw.printf("Robo pos:\tX: %.0f\tY: %.0f\tAngle: %d\r\n", getRoboX(), getRoboY(), getRoboA());
        pw.printf("Goto:\tX: %.0f\tY: %.0f\r\n", getGotoX(), getGotoY());
        for (Entry<Integer, ArrayList<float[]>> area : areas.entrySet()) {
            switch (area.getKey()) {
                case NO_GO_AREAS:
                    pw.print("Regular No Go zones:\t");
                    break;
                case MOB_FORBIDDEN_AREA:
                    pw.print("Mop No Go zones:\t");
                    break;
                case CARPET_FORBIDDEN_AREA:
                    pw.print("Carpet No Go zones:\t");
                    break;
                default:
                    pw.print("Unknown type zones:\t");
            }
            pw.printf("%d\r\n", area.getValue().size());
            printAreaDetails(area.getValue(), pw);
        }
        pw.printf("Walls:\t%d\r\n", walls.size());
        printAreaDetails(walls, pw);
        pw.printf("Zones:\t%d\r\n", zones.size());
        printAreaDetails(zones, pw);
        for (Entry<Integer, ArrayList<int[]>> obstacleType : obstacles.entrySet()) {
            pw.printf("Obstacles Type (%d):\t%d\r\n", obstacleType.getKey(), obstacleType.getValue().size());
            printObstacleDetails(obstacleType.getValue(), pw);
        }
        pw.printf("Blocks:\t%d\r\n", blocks.length);
        pw.print("Paths:");
        for (Entry<Integer, Map<String, Integer>> pathDetail : pathsDetails.entrySet()) {
            pw.printf("\r\nPath type:\t%d", pathDetail.getKey());
            for (String detail : pathDetail.getValue().keySet()) {
                pw.printf("   %s: %d", detail, pathDetail.getValue().get(detail));
            }
        }
        pw.println();
        pw.printf("Carpet Map:\t%d\r\n", carpetMap.length);
        pw.printf("Mop Path:\t%d\r\n", mopPath.length);
        pw.close();
        return sw.toString();
    }

    private void printAreaDetails(@Nullable ArrayList<float[]> areas, PrintWriter pw) {
        if (areas == null) {
            pw.println("null");
            return;
        }
        areas.forEach(area -> {
            pw.print("\tArea coordinates:");
            for (int i = 0; i < area.length; i++) {
                pw.printf("\t%.0f", area[i]);
            }
            pw.println();
        });
    }

    private void printObstacleDetails(@Nullable ArrayList<int[]> obstacle, PrintWriter pw) {
        if (obstacle == null) {
            pw.println("null");
            return;
        }
        obstacle.forEach(area -> {
            pw.print("\tObstacle coordinates:");
            for (int i = 0; i < area.length; i++) {
                pw.printf("\t%d", area[i]);
            }
            pw.println();
        });
    }

    /**
     * Compute SHA-1 hash value for the byte array
     *
     * @param inBytes ByteArray to be hashed
     * @return hash value
     */
    public static byte[] sha1Hash(byte[] inBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(inBytes);
        } catch (NoSuchAlgorithmException e) {
            return new byte[] { 0x00 };
        }
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getMapIndex() {
        return mapIndex;
    }

    public int getMapSequence() {
        return mapSequence;
    }

    public boolean isValid() {
        return isValid;
    }

    public byte[] getImage() {
        return image;
    }

    public int getImageSize() {
        return imageSize;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public int getTop() {
        return top;
    }

    public int getLeft() {
        return left;
    }

    public ArrayList<float[]> getZones() {
        return zones;
    }

    public float getRoboX() {
        return roboX;
    }

    public float getRoboY() {
        return roboY;
    }

    public float getChargerX() {
        return chargerX;
    }

    public float getChargerY() {
        return chargerY;
    }

    public float getGotoX() {
        return gotoX;
    }

    public float getGotoY() {
        return gotoY;
    }

    public int getRoboA() {
        return roboA;
    }

    public Map<Integer, ArrayList<float[]>> getPaths() {
        return paths;
    }

    public Map<Integer, Map<String, Integer>> getPathsDetails() {
        return pathsDetails;
    }

    public ArrayList<float[]> getWalls() {
        return walls;
    }

    public Map<Integer, ArrayList<float[]>> getAreas() {
        return areas;
    }

    public Map<Integer, ArrayList<int[]>> getObstacles() {
        return obstacles;
    }

    public byte[] getBlocks() {
        return blocks;
    }

    public final int[] getCarpetMap() {
        return carpetMap;
    }

    public final int[] getMopPath() {
        return mopPath;
    }
}

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockException;

/**
 * Renders RR map data as a colorized PNG image with semantic overlays.
 */
@NonNullByDefault
public final class RRMapRenderer {
    private static final int TARGET_MIN_MAX_DIMENSION = 1024;
    private static final int TARGET_MAX_DIMENSION = 2048;
    private static final int MM = 50;

    private static final int MAP_OUTSIDE = 0x00;
    private static final int MAP_WALL = 0x01;
    private static final int MAP_INSIDE = 0xFF;
    private static final int MAP_SCAN = 0x07;

    private static final Color COLOR_MAP_OUTSIDE = new Color(24, 24, 24);
    private static final Color COLOR_MAP_WALL = new Color(92, 92, 92);
    private static final Color COLOR_MAP_INSIDE = new Color(212, 221, 233);
    private static final Color COLOR_MAP_SCAN = new Color(171, 209, 244);
    private static final Color COLOR_MAP_GREY_WALL = new Color(119, 119, 119);

    private static final Color COLOR_CLEANED_ZONE = new Color(117, 201, 110, 90);
    private static final Color COLOR_NO_GO = new Color(224, 45, 45, 96);
    private static final Color COLOR_MOP_FORBIDDEN = new Color(73, 131, 255, 96);
    private static final Color COLOR_CARPET_FORBIDDEN = new Color(255, 175, 64, 96);
    private static final Color COLOR_VIRTUAL_WALL = new Color(234, 43, 43);
    private static final Color COLOR_PATH = new Color(255, 255, 255, 170);
    private static final Color COLOR_GOTO_PATH = new Color(146, 234, 126);
    private static final Color COLOR_PREDICTED_PATH = new Color(255, 164, 64);
    private static final Color COLOR_MOP_PATH = new Color(64, 148, 255, 96);
    private static final Color COLOR_CARPET_MAP = new Color(255, 194, 102, 96);
    private static final Color COLOR_DOCK = new Color(255, 196, 61);
    private static final Color COLOR_ROBOT = new Color(42, 124, 255);
    private static final Color COLOR_GOTO_TARGET = new Color(74, 212, 95);
    private static final Color COLOR_OBSTACLE = new Color(254, 73, 73);
    private static final Color COLOR_IGNORED_OBSTACLE = new Color(188, 188, 188);

    // Mapping mirrors known Roborock obstacle classifier ids from field captures.
    // Keep duplicate labels when multiple ids represent vendor-distinct but UI-equivalent classes,
    // and extend this table conservatively as new ids are validated.
    private static final Map<Integer, String> OBSTACLE_TYPE_LABELS = Map.ofEntries(Map.entry(0, "cable"),
            Map.entry(1, "pet waste"), Map.entry(2, "shoes"), Map.entry(3, "poop"), Map.entry(4, "pedestal"),
            Map.entry(5, "extension cord"), Map.entry(9, "weighting scale"), Map.entry(10, "clothes"),
            Map.entry(25, "dustpan"), Map.entry(26, "furniture with a crossbar"),
            Map.entry(27, "furniture with a crossbar"), Map.entry(34, "clothes"), Map.entry(48, "cable"),
            Map.entry(49, "pet"), Map.entry(50, "pet"), Map.entry(51, "fabric/paper balls"));

    public byte[] renderAsPng(RRMapData mapData) throws RoborockException {
        int width = mapData.imageWidth();
        int height = mapData.imageHeight();
        byte[] imageData = mapData.imageData();
        if (width <= 0 || height <= 0 || imageData.length < width * height) {
            throw new RoborockException("Cannot render map image due to invalid dimensions or data length.");
        }

        int maxDimension = Math.max(width, height);
        float upscaleFactor = maxDimension < TARGET_MIN_MAX_DIMENSION ? TARGET_MIN_MAX_DIMENSION / (float) maxDimension
                : 1.0f;
        int renderWidth = Math.max(1, Math.round(width * upscaleFactor));
        int renderHeight = Math.max(1, Math.round(height * upscaleFactor));

        BufferedImage sourceImage = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = sourceImage.createGraphics();
        try {
            if (upscaleFactor != 1.0f) {
                graphics.scale(upscaleFactor, upscaleFactor);
            }

            drawBaseMap(graphics, mapData);
            configureGraphics(graphics);
            drawCarpetMapMask(graphics, mapData);
            drawMopPathMask(graphics, mapData);
            drawCleanedZones(graphics, mapData);
            drawAreas(graphics, mapData.noGoAreas(), mapData, COLOR_NO_GO);
            drawAreas(graphics, mapData.mopForbiddenAreas(), mapData, COLOR_MOP_FORBIDDEN);
            drawAreas(graphics, mapData.carpetForbiddenAreas(), mapData, COLOR_CARPET_FORBIDDEN);
            drawVirtualWalls(graphics, mapData);
            drawPath(graphics, mapData);
            drawGotoPath(graphics, mapData);
            drawPredictedPath(graphics, mapData);
            drawGotoTarget(graphics, mapData);
            drawObstacles(graphics, mapData);
            drawCharger(graphics, mapData);
            drawRobot(graphics, mapData);
        } finally {
            graphics.dispose();
        }

        BufferedImage outputImage = scaleDownToMaxDimensionIfNeeded(sourceImage);
        BufferedImage orientedImage = flipHorizontally(outputImage);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(orientedImage, "png", output)) {
                throw new RoborockException("No suitable image writer for PNG map rendering.");
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new RoborockException("Failed to encode map image as PNG.", e);
        }
    }

    private BufferedImage scaleDownToMaxDimensionIfNeeded(BufferedImage sourceImage) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int maxDimension = Math.max(width, height);
        if (maxDimension <= 0 || maxDimension <= TARGET_MAX_DIMENSION) {
            return sourceImage;
        }

        float scale = TARGET_MAX_DIMENSION / (float) maxDimension;
        int targetWidth = Math.max(1, Math.round(width * scale));
        int targetHeight = Math.max(1, Math.round(height * scale));

        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D scaledGraphics = scaledImage.createGraphics();
        try {
            scaledGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            scaledGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            scaledGraphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            scaledGraphics.dispose();
        }
        return scaledImage;
    }

    private BufferedImage flipHorizontally(BufferedImage sourceImage) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        BufferedImage mirrored = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = mirrored.createGraphics();
        try {
            graphics.transform(AffineTransform.getScaleInstance(-1, 1));
            graphics.drawImage(sourceImage, -width, 0, null);
        } finally {
            graphics.dispose();
        }
        return mirrored;
    }

    private void configureGraphics(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private void drawBaseMap(Graphics2D graphics, RRMapData mapData) {
        int width = mapData.imageWidth();
        int height = mapData.imageHeight();
        byte[] imageData = mapData.imageData();

        int offset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = imageData[offset++] & 0xFF;
                int drawX = width - 1 - x;
                int drawY = height - 1 - y;
                graphics.setColor(resolveMapPixelColor(value));
                graphics.fillRect(drawX, drawY, 1, 1);
            }
        }
    }

    private Color resolveMapPixelColor(int value) {
        return switch (value) {
            case MAP_OUTSIDE -> COLOR_MAP_OUTSIDE;
            case MAP_WALL -> COLOR_MAP_WALL;
            case MAP_INSIDE -> COLOR_MAP_INSIDE;
            case MAP_SCAN -> COLOR_MAP_SCAN;
            default -> {
                int obstacle = value & 0x07;
                if (obstacle == 0) {
                    yield COLOR_MAP_GREY_WALL;
                } else if (obstacle == 1) {
                    yield Color.BLACK;
                } else if (obstacle == 7) {
                    int roomId = value >>> 3;
                    yield roomColor(roomId);
                }
                yield COLOR_MAP_INSIDE;
            }
        };
    }

    private Color roomColor(int roomId) {
        float hue = (roomId % 24) / 24.0f;
        return Color.getHSBColor(hue, 0.45f, 0.93f);
    }

    private void drawPath(Graphics2D graphics, RRMapData mapData) {
        drawPolyline(graphics, mapData, mapData.basicPath(), COLOR_PATH, 1.0f);
    }

    private void drawGotoPath(Graphics2D graphics, RRMapData mapData) {
        drawPolyline(graphics, mapData, mapData.gotoPath(), COLOR_GOTO_PATH, 1.3f);
    }

    private void drawPredictedPath(Graphics2D graphics, RRMapData mapData) {
        drawPolyline(graphics, mapData, mapData.predictedPath(), COLOR_PREDICTED_PATH, 0.7f);
    }

    private void drawPolyline(Graphics2D graphics, RRMapData mapData, Iterable<RRMapData.MapPoint> points, Color color,
            float strokeWidth) {
        var iterator = points.iterator();
        if (!iterator.hasNext()) {
            return;
        }

        RRMapData.MapPoint first = iterator.next();
        if (!iterator.hasNext()) {
            return;
        }

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D path = new Path2D.Float();
        path.moveTo(toXCoord(first.x(), mapData), toYCoord(first.y(), mapData));
        while (iterator.hasNext()) {
            RRMapData.MapPoint point = iterator.next();
            path.lineTo(toXCoord(point.x(), mapData), toYCoord(point.y(), mapData));
        }
        graphics.draw(path);
    }

    private void drawGotoTarget(Graphics2D graphics, RRMapData mapData) {
        Integer gotoTargetX = mapData.gotoTargetX();
        Integer gotoTargetY = mapData.gotoTargetY();
        if (gotoTargetX == null || gotoTargetY == null) {
            return;
        }

        float x = toXCoord(gotoTargetX.intValue(), mapData);
        float y = toYCoord(gotoTargetY.intValue(), mapData);
        graphics.setColor(COLOR_GOTO_TARGET);
        drawCircle(graphics, x, y, 3.0f);
        drawCircle(graphics, x, y, 1.2f);
    }

    private void drawObstacles(Graphics2D graphics, RRMapData mapData) {
        for (RRMapData.MapObstacle obstacle : mapData.obstacles()) {
            drawObstacle(graphics, mapData, obstacle, COLOR_OBSTACLE);
        }
        for (RRMapData.MapObstacle obstacle : mapData.ignoredObstacles()) {
            drawObstacle(graphics, mapData, obstacle, COLOR_IGNORED_OBSTACLE);
        }
    }

    private void drawObstacle(Graphics2D graphics, RRMapData mapData, RRMapData.MapObstacle obstacle, Color color) {
        float x = toXCoord(obstacle.x(), mapData);
        float y = toYCoord(obstacle.y(), mapData);
        graphics.setColor(color);
        drawFilledCircle(graphics, x, y, 1.8f);
        graphics.setColor(Color.BLACK);
        drawCircle(graphics, x, y, 2.2f);
    }

    private void drawCleanedZones(Graphics2D graphics, RRMapData mapData) {
        graphics.setColor(COLOR_CLEANED_ZONE);
        for (RRMapData.MapZone zone : mapData.cleanedZones()) {
            float x0 = toXCoord(zone.x0(), mapData);
            float y0 = toYCoord(zone.y0(), mapData);
            float x1 = toXCoord(zone.x1(), mapData);
            float y1 = toYCoord(zone.y1(), mapData);
            float sx = Math.min(x0, x1);
            float sy = Math.min(y0, y1);
            float w = Math.max(1.0f, Math.abs(x1 - x0));
            float h = Math.max(1.0f, Math.abs(y1 - y0));
            graphics.fillRect(Math.round(sx), Math.round(sy), Math.round(w), Math.round(h));
        }
    }

    private void drawAreas(Graphics2D graphics, Iterable<RRMapData.MapArea> areas, RRMapData mapData, Color color) {
        graphics.setColor(color);
        for (RRMapData.MapArea area : areas) {
            Path2D polygon = new Path2D.Float();
            polygon.moveTo(toXCoord(area.x0(), mapData), toYCoord(area.y0(), mapData));
            polygon.lineTo(toXCoord(area.x1(), mapData), toYCoord(area.y1(), mapData));
            polygon.lineTo(toXCoord(area.x2(), mapData), toYCoord(area.y2(), mapData));
            polygon.lineTo(toXCoord(area.x3(), mapData), toYCoord(area.y3(), mapData));
            polygon.closePath();
            graphics.fill(polygon);
            graphics.setColor(color.darker());
            graphics.draw(polygon);
            graphics.setColor(color);
        }
    }

    private void drawVirtualWalls(Graphics2D graphics, RRMapData mapData) {
        if (mapData.virtualWalls().isEmpty()) {
            return;
        }

        graphics.setColor(COLOR_VIRTUAL_WALL);
        graphics.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (RRMapData.MapWall wall : mapData.virtualWalls()) {
            graphics.drawLine(Math.round(toXCoord(wall.x0(), mapData)), Math.round(toYCoord(wall.y0(), mapData)),
                    Math.round(toXCoord(wall.x1(), mapData)), Math.round(toYCoord(wall.y1(), mapData)));
        }
    }

    private void drawMopPathMask(Graphics2D graphics, RRMapData mapData) {
        byte[] mopPathMask = mapData.mopPathMask();
        int width = mapData.imageWidth();
        int height = mapData.imageHeight();
        if (mopPathMask.length < width * height) {
            return;
        }

        graphics.setColor(COLOR_MOP_PATH);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((mopPathMask[y * width + x] & 0xFF) > 0) {
                    int drawX = width - 1 - x;
                    int drawY = height - 1 - y;
                    graphics.fillRect(drawX, drawY, 1, 1);
                }
            }
        }
    }

    private void drawCarpetMapMask(Graphics2D graphics, RRMapData mapData) {
        byte[] carpetMapMask = mapData.carpetMapMask();
        int width = mapData.imageWidth();
        int height = mapData.imageHeight();
        if (carpetMapMask.length < width * height) {
            return;
        }

        graphics.setColor(COLOR_CARPET_MAP);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((carpetMapMask[y * width + x] & 0xFF) > 0) {
                    int drawX = width - 1 - x;
                    int drawY = height - 1 - y;
                    graphics.fillRect(drawX, drawY, 1, 1);
                }
            }
        }
    }

    static @Nullable String obstacleTypeLabel(int type) {
        return OBSTACLE_TYPE_LABELS.get(type);
    }

    private void drawCharger(Graphics2D graphics, RRMapData mapData) {
        Integer chargerX = mapData.chargerX();
        Integer chargerY = mapData.chargerY();
        if (chargerX == null || chargerY == null) {
            return;
        }

        float x = toXCoord(chargerX.intValue(), mapData);
        float y = toYCoord(chargerY.intValue(), mapData);
        graphics.setColor(COLOR_DOCK);
        drawFilledCircle(graphics, x, y, 2.8f);
        graphics.setColor(Color.BLACK);
        drawCircle(graphics, x, y, 3.6f);
    }

    private void drawRobot(Graphics2D graphics, RRMapData mapData) {
        Integer robotX = mapData.robotX();
        Integer robotY = mapData.robotY();
        if (robotX == null || robotY == null) {
            return;
        }

        float x = toXCoord(robotX.intValue(), mapData);
        float y = toYCoord(robotY.intValue(), mapData);
        graphics.setColor(COLOR_ROBOT);
        drawFilledCircle(graphics, x, y, 2.6f);
        graphics.setColor(Color.WHITE);
        drawCircle(graphics, x, y, 3.2f);

        Integer angle = mapData.robotAngle();
        if (angle != null) {
            double radians = Math.toRadians(angle.doubleValue());
            int x2 = Math.round(x + (float) (Math.cos(radians) * 5.0d));
            int y2 = Math.round(y - (float) (Math.sin(radians) * 5.0d));
            graphics.drawLine(Math.round(x), Math.round(y), x2, y2);
        }
    }

    private void drawCircle(Graphics2D graphics, float x, float y, float radius) {
        graphics.draw(new Ellipse2D.Float(x - radius, y - radius, radius * 2.0f, radius * 2.0f));
    }

    private void drawFilledCircle(Graphics2D graphics, float x, float y, float radius) {
        graphics.fill(new Ellipse2D.Float(x - radius, y - radius, radius * 2.0f, radius * 2.0f));
    }

    private float toXCoord(int x, RRMapData mapData) {
        return mapData.imageWidth() + mapData.left() - (x / (float) MM);
    }

    private float toYCoord(int y, RRMapData mapData) {
        return mapData.imageHeight() - (y / (float) MM - mapData.top());
    }
}

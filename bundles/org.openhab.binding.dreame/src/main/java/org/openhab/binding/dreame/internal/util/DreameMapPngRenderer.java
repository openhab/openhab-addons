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
package org.openhab.binding.dreame.internal.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameMapGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPathGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPoint;
import org.openhab.binding.dreame.internal.model.DreameMapZoneGeometry;
import org.openhab.binding.dreame.internal.model.DreameMowerPose;

/** Renders a raster fallback of the Dreame map for clients without reliable SVG support. */
@NonNullByDefault
public final class DreameMapPngRenderer {
    private static final int PADDING = 300;
    private static final int MAX_DIMENSION = 1024;

    private DreameMapPngRenderer() {
    }

    public static byte @Nullable [] render(List<DreameMapGeometry> geometries, int currentMapId,
            @Nullable DreameMowerPose pose, boolean rotateClockwise) {
        DreameMapGeometry geometry = geometries.stream().filter(map -> map.mapId() == currentMapId).findFirst()
                .orElse(geometries.isEmpty() ? null : geometries.get(0));
        if (geometry == null || geometry.maxX() <= geometry.minX() || geometry.maxY() <= geometry.minY()) {
            return null;
        }

        int viewX = (rotateClockwise ? geometry.minY() : geometry.minX()) - PADDING;
        int viewY = (rotateClockwise ? geometry.minX() : -geometry.maxY()) - PADDING;
        int viewWidth = (rotateClockwise ? geometry.maxY() - geometry.minY() : geometry.maxX() - geometry.minX())
                + 2 * PADDING;
        int viewHeight = (rotateClockwise ? geometry.maxX() - geometry.minX() : geometry.maxY() - geometry.minY())
                + 2 * PADDING;
        double scale = Math.min((double) MAX_DIMENSION / viewWidth, (double) MAX_DIMENSION / viewHeight);
        int imageWidth = Math.max(1, (int) Math.round(viewWidth * scale));
        int imageHeight = Math.max(1, (int) Math.round(viewHeight * scale));
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(0xEE, 0xF3, 0xEE));
            graphics.fillRect(0, 0, imageWidth, imageHeight);
            graphics.scale(scale, scale);
            graphics.translate(-viewX, -viewY);

            drawPaths(graphics, geometry, rotateClockwise);
            drawZones(graphics, geometry, geometry.zones(), new Color(0x70, 0xB7, 0x7E), new Color(0x24, 0x5C, 0x35),
                    rotateClockwise);
            drawZones(graphics, geometry, geometry.forbiddenAreas(), new Color(0xEF, 0x77, 0x77, 199),
                    new Color(0xA6, 0x11, 0x11), rotateClockwise);
            drawStation(graphics, geometry, rotateClockwise);
            if (pose != null) {
                drawMower(graphics, geometry, pose, rotateClockwise);
            }
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            return ImageIO.write(image, "png", output) ? output.toByteArray() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void drawPaths(Graphics2D graphics, DreameMapGeometry geometry, boolean rotateClockwise) {
        graphics.setColor(new Color(0xC5, 0x8B, 0x2A));
        graphics.setStroke(
                new BasicStroke(90, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[] { 120, 80 }, 0));
        for (DreameMapPathGeometry path : geometry.paths()) {
            graphics.draw(path(geometry, path.points(), rotateClockwise, false));
        }
    }

    private static void drawZones(Graphics2D graphics, DreameMapGeometry geometry, List<DreameMapZoneGeometry> zones,
            Color fill, Color stroke, boolean rotateClockwise) {
        graphics.setStroke(new BasicStroke(35, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        for (DreameMapZoneGeometry zone : zones) {
            Path2D polygon = path(geometry, zone.points(), rotateClockwise, true);
            graphics.setColor(fill);
            graphics.fill(polygon);
            graphics.setColor(stroke);
            graphics.draw(polygon);
        }
    }

    private static void drawStation(Graphics2D graphics, DreameMapGeometry geometry, boolean rotateClockwise) {
        DreameMapPoint station = stationPoint(geometry.paths());
        if (station == null) {
            return;
        }
        int x = rotatedX(geometry, station.x(), station.y(), rotateClockwise);
        int y = rotatedY(geometry, station.x(), station.y(), rotateClockwise);
        graphics.setColor(new Color(0x36, 0x78, 0xC9));
        graphics.fill(new RoundRectangle2D.Double(x - 170, y - 125, 340, 250, 90, 90));
        graphics.setColor(new Color(0x12, 0x3C, 0x70));
        graphics.setStroke(new BasicStroke(35));
        graphics.draw(new RoundRectangle2D.Double(x - 170, y - 125, 340, 250, 90, 90));
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(x - 65, y - 55);
        arrow.lineTo(x + 40, y - 55);
        arrow.lineTo(x + 40, y - 105);
        arrow.lineTo(x + 125, y);
        arrow.lineTo(x + 40, y + 105);
        arrow.lineTo(x + 40, y + 55);
        arrow.lineTo(x - 65, y + 55);
        arrow.closePath();
        graphics.setColor(Color.WHITE);
        graphics.fill(arrow);
    }

    private static void drawMower(Graphics2D graphics, DreameMapGeometry geometry, DreameMowerPose pose,
            boolean rotateClockwise) {
        int x = rotatedX(geometry, pose.x(), pose.y(), rotateClockwise);
        int y = rotatedY(geometry, pose.x(), pose.y(), rotateClockwise);
        double heading = 180.0 - pose.heading() + (rotateClockwise ? 90.0 : 0.0);
        AffineTransform original = graphics.getTransform();
        graphics.translate(x, y);
        graphics.rotate(Math.toRadians(heading));
        graphics.setColor(Color.WHITE);
        graphics.fill(new Ellipse2D.Double(-145, -145, 290, 290));
        graphics.setColor(new Color(0x15, 0x22, 0x38));
        graphics.setStroke(new BasicStroke(35));
        graphics.draw(new Ellipse2D.Double(-145, -145, 290, 290));
        Path2D pointer = new Path2D.Double();
        pointer.moveTo(220, 0);
        pointer.lineTo(-105, -120);
        pointer.lineTo(-55, 0);
        pointer.lineTo(-105, 120);
        pointer.closePath();
        graphics.setColor(new Color(0xE5, 0x39, 0x35));
        graphics.fill(pointer);
        graphics.setColor(new Color(0x7F, 0x10, 0x10));
        graphics.setStroke(new BasicStroke(25, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        graphics.draw(pointer);
        graphics.setTransform(original);
    }

    private static Path2D path(DreameMapGeometry geometry, List<DreameMapPoint> points, boolean rotateClockwise,
            boolean close) {
        Path2D path = new Path2D.Double();
        boolean first = true;
        for (DreameMapPoint point : points) {
            int x = rotatedX(geometry, point.x(), point.y(), rotateClockwise);
            int y = rotatedY(geometry, point.x(), point.y(), rotateClockwise);
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        if (close && !first) {
            path.closePath();
        }
        return path;
    }

    private static int rotatedX(DreameMapGeometry geometry, int x, int y, boolean rotateClockwise) {
        return rotateClockwise ? geometry.minY() + geometry.maxY() - y : geometry.minX() + geometry.maxX() - x;
    }

    private static int rotatedY(DreameMapGeometry geometry, int x, int y, boolean rotateClockwise) {
        return rotateClockwise ? geometry.minX() + geometry.maxX() - x : y - geometry.minY() - geometry.maxY();
    }

    private static @Nullable DreameMapPoint stationPoint(List<DreameMapPathGeometry> paths) {
        return paths.stream().filter(path -> path.type() == 1).flatMap(path -> path.points().stream())
                .min(java.util.Comparator
                        .comparingLong(point -> (long) point.x() * point.x() + (long) point.y() * point.y()))
                .orElse(null);
    }
}

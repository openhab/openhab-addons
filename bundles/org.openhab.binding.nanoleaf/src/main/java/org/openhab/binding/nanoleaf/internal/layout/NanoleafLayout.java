/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

package org.openhab.binding.nanoleaf.internal.layout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.layout.shape.Shape;
import org.openhab.binding.nanoleaf.internal.layout.shape.ShapeFactory;
import org.openhab.binding.nanoleaf.internal.model.GlobalOrientation;
import org.openhab.binding.nanoleaf.internal.model.Layout;
import org.openhab.binding.nanoleaf.internal.model.PanelLayout;
import org.openhab.binding.nanoleaf.internal.model.PositionDatum;

/**
 * Renders the Nanoleaf layout to an image.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NanoleafLayout {

    private static final Color COLOR_BACKGROUND = Color.WHITE;
    private static final Color COLOR_PANEL = Color.BLACK;
    private static final Color COLOR_SIDE = Color.GRAY;
    private static final Color COLOR_TEXT = Color.BLACK;

    public static byte[] render(PanelLayout panelLayout) throws IOException {
        double rotationRadians = 0;
        GlobalOrientation globalOrientation = panelLayout.getGlobalOrientation();
        if (globalOrientation != null) {
            rotationRadians = calculateRotationRadians(globalOrientation);
        }

        Layout layout = panelLayout.getLayout();
        if (layout == null) {
            return new byte[] {};
        }

        List<PositionDatum> panels = layout.getPositionData();
        if (panels == null) {
            return new byte[] {};
        }

        Point2D size[] = findSize(panels, rotationRadians);
        final Point2D min = size[0];
        final Point2D max = size[1];
        Point2D prev = null;
        Point2D first = null;

        int sideCounter = 0;
        BufferedImage image = new BufferedImage(
                (max.getX() - min.getX()) + 2 * NanoleafBindingConstants.LAYOUT_BORDER_WIDTH,
                (max.getY() - min.getY()) + 2 * NanoleafBindingConstants.LAYOUT_BORDER_WIDTH,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        g2.setBackground(COLOR_BACKGROUND);
        g2.clearRect(0, 0, image.getWidth(), image.getHeight());

        for (PositionDatum panel : panels) {
            final ShapeType shapeType = ShapeType.valueOf(panel.getShapeType());

            Shape shape = ShapeFactory.CreateShape(shapeType, panel);
            List<Point2D> outline = toPictureLayout(shape.generateOutline(), image.getHeight(), min, rotationRadians);
            for (int i = 0; i < outline.size(); i++) {
                g2.setColor(COLOR_SIDE);
                Point2D pos = outline.get(i);
                Point2D nextPos = outline.get((i + 1) % outline.size());
                g2.drawLine(pos.getX(), pos.getY(), nextPos.getX(), nextPos.getY());
            }

            for (int i = 0; i < outline.size(); i++) {
                Point2D pos = outline.get(i);
                g2.setColor(COLOR_PANEL);
                g2.fillOval(pos.getX() - NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS / 2,
                        pos.getY() - NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS / 2,
                        NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS, NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS);
            }

            Point2D current = toPictureLayout(new Point2D(panel.getPosX(), panel.getPosY()), image.getHeight(), min,
                    rotationRadians);
            if (sideCounter == 0) {
                first = current;
            }

            g2.setColor(COLOR_SIDE);
            final int expectedSides = shapeType.getNumSides();
            if (shapeType.getDrawingAlgorithm() == DrawingAlgorithm.CORNER) {
                // Special handling of Elements Hexagon Corners, where we get 6 corners instead of 1 shape. They seem to
                // come after each other in the JSON, so this algorithm connects them based on the number of sides the
                // shape is expected to have.
                if (sideCounter > 0 && sideCounter != expectedSides && prev != null) {
                    g2.drawLine(prev.getX(), prev.getY(), current.getX(), current.getY());
                }

                sideCounter++;

                if (sideCounter == expectedSides && first != null) {
                    g2.drawLine(current.getX(), current.getY(), first.getX(), first.getY());
                    sideCounter = 0;
                }
            } else {
                sideCounter = 0;
            }

            prev = current;

            g2.setColor(COLOR_TEXT);
            Point2D textPos = shape.labelPosition(g2, outline);
            g2.drawString(Integer.toString(panel.getPanelId()), textPos.getX(), textPos.getY());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static double calculateRotationRadians(GlobalOrientation globalOrientation) {
        Integer maxObj = globalOrientation.getMax();
        int maxValue = maxObj == null ? 360 : (int) maxObj;
        int value = globalOrientation.getValue(); // 0 - 360 measured counter clockwise.
        return ((double) (maxValue - value)) * (Math.PI / 180);
    }

    private static Point2D[] findSize(Collection<PositionDatum> panels, double rotationRadians) {
        int maxX = 0;
        int maxY = 0;
        int minX = 0;
        int minY = 0;

        for (PositionDatum panel : panels) {
            ShapeType shapeType = ShapeType.valueOf(panel.getShapeType());
            Shape shape = ShapeFactory.CreateShape(shapeType, panel);
            for (Point2D point : shape.generateOutline()) {
                var rotated = point.rotate(rotationRadians);
                maxX = Math.max(rotated.getX(), maxX);
                maxY = Math.max(rotated.getY(), maxY);
                minX = Math.min(rotated.getX(), minX);
                minY = Math.min(rotated.getY(), minY);
            }
        }

        return new Point2D[] { new Point2D(minX, minY), new Point2D(maxX, maxY) };
    }

    private static Point2D toPictureLayout(Point2D original, int imageHeight, Point2D min, double rotationRadians) {
        Point2D rotated = original.rotate(rotationRadians);
        Point2D translated = new Point2D(NanoleafBindingConstants.LAYOUT_BORDER_WIDTH + rotated.getX() - min.getX(),
                imageHeight - NanoleafBindingConstants.LAYOUT_BORDER_WIDTH - rotated.getY() + min.getY());
        return translated;
    }

    private static List<Point2D> toPictureLayout(List<Point2D> originals, int imageHeight, Point2D min,
            double rotationRadians) {
        List<Point2D> result = new ArrayList<Point2D>(originals.size());
        for (Point2D original : originals) {
            result.add(toPictureLayout(original, imageHeight, min, rotationRadians));
        }

        return result;
    }
}

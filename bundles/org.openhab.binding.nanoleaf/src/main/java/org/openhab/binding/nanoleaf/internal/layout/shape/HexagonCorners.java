/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal.layout.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.DrawingSettings;
import org.openhab.binding.nanoleaf.internal.layout.ImagePoint2D;
import org.openhab.binding.nanoleaf.internal.layout.PanelState;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;
import org.openhab.binding.nanoleaf.internal.model.PositionDatum;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.util.ColorUtil;

/**
 * A hexagon shape.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class HexagonCorners extends Panel {

    private static final int CORNER_DIAMETER = 4;

    private final List<PositionDatum> corners;

    public HexagonCorners(ShapeType shapeType, List<PositionDatum> corners) {
        super(shapeType);

        this.corners = Collections.unmodifiableList(new ArrayList<>(corners));
    }

    @Override
    public List<Point2D> generateOutline() {
        List<Point2D> result = new ArrayList<>(corners.size());
        for (PositionDatum corner : corners) {
            result.add(new Point2D(corner.getPosX(), corner.getPosY()));
        }

        return result;
    }

    @Override
    public void draw(Graphics2D graphics, DrawingSettings settings, PanelState state) {
        List<ImagePoint2D> outline = settings.generateImagePoints(generateOutline());
        Polygon p = new Polygon();
        for (int i = 0; i < outline.size(); i++) {
            ImagePoint2D pos = outline.get(i);
            p.addPoint(pos.getX(), pos.getY());
        }

        if (settings.shouldFillWithColor()) {
            Color averageColor = getAverageColor(state);
            graphics.setColor(averageColor);
            graphics.fillPolygon(p);

            // Draw color cradient
            ImagePoint2D center = findCenter(outline);
            for (int i = 0; i < outline.size(); i++) {
                ImagePoint2D corner1Pos = outline.get(i);
                ImagePoint2D corner2Pos = outline.get((i + 1) % outline.size());

                PositionDatum corner1 = corners.get(i);
                PositionDatum corner2 = corners.get((i + 1) % outline.size());

                Color corner1Color = getColor(corner1.getPanelId(), state);
                Color corner2Color = getColor(corner2.getPanelId(), state);
                graphics.setPaint(new BarycentricTriangleGradient(
                        new ImagePoint2D(corner1Pos.getX(), corner1Pos.getY()), corner1Color,
                        new ImagePoint2D(corner2Pos.getX(), corner2Pos.getY()), corner2Color, center, averageColor));

                Polygon wedge = new Polygon();
                wedge.addPoint(corner1Pos.getX(), corner1Pos.getY());
                wedge.addPoint(corner2Pos.getX(), corner2Pos.getY());
                wedge.addPoint(center.getX(), center.getY());
                graphics.fillPolygon(p);
            }
        }

        if (settings.shouldDrawOutline()) {
            graphics.setColor(settings.getOutlineColor());
            graphics.drawPolygon(p);
        }

        if (settings.shouldDrawCorners()) {
            for (PositionDatum corner : corners) {
                ImagePoint2D position = settings.generateImagePoint(new Point2D(corner.getPosX(), corner.getPosY()));
                graphics.setColor(getColor(corner.getPanelId(), state));
                graphics.fillOval(position.getX() - CORNER_DIAMETER / 2, position.getY() - CORNER_DIAMETER / 2,
                        CORNER_DIAMETER, CORNER_DIAMETER);

                if (settings.shouldDrawOutline()) {
                    graphics.setColor(settings.getOutlineColor());
                    graphics.drawOval(position.getX() - CORNER_DIAMETER / 2, position.getY() - CORNER_DIAMETER / 2,
                            CORNER_DIAMETER, CORNER_DIAMETER);
                }
            }
        }

        if (settings.shouldDrawLabels()) {
            graphics.setColor(settings.getLabelColor());

            for (PositionDatum corner : corners) {
                ImagePoint2D position = settings.generateImagePoint(new Point2D(corner.getPosX(), corner.getPosY()));
                graphics.drawString(Integer.toString(corner.getPanelId()), position.getX(), position.getY());
            }
        }
    }

    private ImagePoint2D findCenter(List<ImagePoint2D> outline) {
        Point2D[] bounds = findBounds(outline);
        int midX = bounds[0].getX() + (bounds[1].getX() - bounds[0].getX()) / 2;
        int midY = bounds[0].getY() + (bounds[1].getY() - bounds[0].getY()) / 2;
        return new ImagePoint2D(midX, midY);
    }

    private static Color getColor(int panelId, PanelState state) {
        HSBType color = state.getHSBForPanel(panelId);
        return new Color(ColorUtil.hsbTosRgb(color));
    }

    private Color getAverageColor(PanelState state) {
        float r = 0;
        float g = 0;
        float b = 0;
        for (PositionDatum corner : corners) {
            Color c = getColor(corner.getPanelId(), state);
            r += c.getRed() * c.getRed();
            g += c.getGreen() * c.getGreen();
            b += c.getBlue() * c.getBlue();
        }

        return new Color((int) Math.sqrt((double) r / corners.size()), (int) Math.sqrt((double) g / corners.size()),
                (int) Math.sqrt((double) b / corners.size()));
    }
}

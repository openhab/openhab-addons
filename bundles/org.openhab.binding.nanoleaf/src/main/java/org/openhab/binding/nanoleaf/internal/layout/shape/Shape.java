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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.DrawingSettings;
import org.openhab.binding.nanoleaf.internal.layout.ImagePoint2D;
import org.openhab.binding.nanoleaf.internal.layout.PanelState;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.util.ColorUtil;

/**
 * Draws shapes, which are panels with a single LED.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public abstract class Shape extends Panel {

    private final Point2D position;
    private final int orientation;
    private final int panelId;

    public Shape(ShapeType shapeType, int panelId, Point2D position, int orientation) {
        super(shapeType);
        this.position = position;
        this.orientation = orientation;
        this.panelId = panelId;
    }

    public Point2D getPosition() {
        return position;
    }

    public int getOrientation() {
        return orientation;
    };

    protected int getPanelId() {
        return panelId;
    }

    @Override
    public abstract List<Point2D> generateOutline();

    /**
     * @param graphics The picture to draw on
     * @param outline Outline of the shape to draw inside
     * @return The position where the label of the shape should be placed
     */
    protected abstract ImagePoint2D labelPosition(Graphics2D graphics, List<ImagePoint2D> outline);

    @Override
    public void draw(Graphics2D graphics, DrawingSettings settings, PanelState state) {
        List<ImagePoint2D> outline = settings.generateImagePoints(generateOutline());

        Polygon p = new Polygon();
        for (int i = 0; i < outline.size(); i++) {
            ImagePoint2D pos = outline.get(i);
            p.addPoint(pos.getX(), pos.getY());
        }

        HSBType color = state.getHSBForPanel(getPanelId());
        graphics.setColor(new Color(ColorUtil.hsbTosRgb(color)));
        if (settings.shouldFillWithColor()) {
            graphics.fillPolygon(p);
        }

        if (settings.shouldDrawOutline()) {
            graphics.setColor(settings.getOutlineColor());
            graphics.drawPolygon(p);
        }

        if (settings.shouldDrawLabels()) {
            graphics.setColor(settings.getLabelColor());
            ImagePoint2D textPos = labelPosition(graphics, outline);
            graphics.drawString(Integer.toString(getPanelId()), textPos.getX(), textPos.getY());
        }
    }
}

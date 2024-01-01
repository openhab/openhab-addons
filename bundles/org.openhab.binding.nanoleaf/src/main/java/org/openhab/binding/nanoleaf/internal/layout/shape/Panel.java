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
package org.openhab.binding.nanoleaf.internal.layout.shape;

import java.awt.Graphics2D;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.DrawingSettings;
import org.openhab.binding.nanoleaf.internal.layout.ImagePoint2D;
import org.openhab.binding.nanoleaf.internal.layout.PanelState;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;

/**
 * Panel is a physical piece of plastic you place on the wall and connect to other panels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public abstract class Panel {
    private final ShapeType shapeType;

    public Panel(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * Calculates the minimal bounding rectangle around an outline.
     *
     * @param outline The outline to find the minimal bounding rectangle around
     * @return The opposite points of the minimum bounding rectangle around this shape.
     */
    public Point2D[] findBounds(List<ImagePoint2D> outline) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (ImagePoint2D point : outline) {
            maxX = Math.max(point.getX(), maxX);
            maxY = Math.max(point.getY(), maxY);
            minX = Math.min(point.getX(), minX);
            minY = Math.min(point.getY(), minY);
        }

        return new Point2D[] { new Point2D(minX, minY), new Point2D(maxX, maxY) };
    }

    /**
     * Generate the outline of the shape.
     *
     * @return The points that make up this shape.
     */
    public abstract List<Point2D> generateOutline();

    /**
     * Draws the shape on the the supplied graphics.
     *
     * @param graphics The picture to draw on
     * @param settings Information on how to draw
     * @param state The state of the panels to draw
     */
    public abstract void draw(Graphics2D graphics, DrawingSettings settings, PanelState state);
}

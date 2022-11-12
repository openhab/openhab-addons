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
package org.openhab.binding.nanoleaf.internal.layout.shape;

import java.awt.Graphics2D;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;

/**
 * Shape that can be drawn.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public abstract class Shape {
    private final ShapeType shapeType;
    private final int panelId;
    private final Point2D position;
    private final int orientation;

    public Shape(ShapeType shapeType, int panelId, Point2D position, int orientation) {
        this.shapeType = shapeType;
        this.panelId = panelId;
        this.position = position;
        this.orientation = orientation;
    }

    public int getPanelId() {
        return panelId;
    };

    public Point2D getPosition() {
        return position;
    }

    public int getOrientation() {
        return orientation;
    };

    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * @return The opposite points of the minimum bounding rectangle around this shape.
     */
    public Point2D[] findBounds(List<Point2D> outline) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Point2D point : outline) {
            maxX = Math.max(point.getX(), maxX);
            maxY = Math.max(point.getY(), maxY);
            minX = Math.min(point.getX(), minX);
            minY = Math.min(point.getY(), minY);
        }

        return new Point2D[] { new Point2D(minX, minY), new Point2D(maxX, maxY) };
    }

    /**
     * @return The points that make up this shape.
     */
    public abstract List<Point2D> generateOutline();

    /**
     * @return The position where the label of the shape should be placed
     */
    public abstract Point2D labelPosition(Graphics2D graphics, List<Point2D> outline);
}

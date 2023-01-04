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
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.ImagePoint2D;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;

/**
 * A triangular shape.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Triangle extends Shape {

    public Triangle(ShapeType shapeType, int panelId, Point2D position, int orientation) {
        super(shapeType, panelId, position, orientation);
    }

    @Override
    public List<Point2D> generateOutline() {
        int height = (int) (getShapeType().getSideLength() * Math.sqrt(3) / 2);
        Point2D v1;
        if (pointsUp()) {
            v1 = new Point2D(0, height * 2 / 3);
        } else {
            v1 = new Point2D(0, -height * 2 / 3);
        }

        Point2D v2 = v1.rotate((2.0 / 3.0) * Math.PI);
        Point2D v3 = v1.rotate((-2.0 / 3.0) * Math.PI);
        return Arrays.asList(v1.move(getPosition()), v2.move(getPosition()), v3.move(getPosition()));
    }

    @Override
    protected ImagePoint2D labelPosition(Graphics2D graphics, List<ImagePoint2D> outline) {
        Point2D centroid = new Point2D((outline.get(0).getX() + outline.get(1).getX() + outline.get(2).getX()) / 3,
                (outline.get(0).getY() + outline.get(1).getY() + outline.get(2).getY()) / 3);

        Rectangle2D rect = graphics.getFontMetrics().getStringBounds(Integer.toString(getPanelId()), graphics);
        return new ImagePoint2D(centroid.getX() - (int) (rect.getWidth() / 2),
                centroid.getY() + (int) (rect.getHeight() / 2));
    }

    private boolean pointsUp() {
        // Upward: even multiple of 60 degrees rotation
        return ((getOrientation() / 60) % 2) == 0;
    }
}

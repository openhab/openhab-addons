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
 * A square shape.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Square extends Shape {
    public Square(ShapeType shapeType, int panelId, Point2D position, int orientation) {
        super(shapeType, panelId, position, orientation);
    }

    @Override
    public List<Point2D> generateOutline() {
        int sideLength = (int) getShapeType().getSideLength();

        Point2D current = getPosition();
        Point2D corner2 = new Point2D(current.getX() + sideLength, current.getY());
        Point2D corner3 = new Point2D(current.getX() + sideLength, current.getY() + sideLength);
        Point2D corner4 = new Point2D(current.getX(), current.getY() + sideLength);
        return Arrays.asList(getPosition(), corner2, corner3, corner4);
    }

    @Override
    protected ImagePoint2D labelPosition(Graphics2D graphics, List<ImagePoint2D> outline) {
        // Center of square is average of oposite corners
        ImagePoint2D p0 = outline.get(0);
        ImagePoint2D p2 = outline.get(2);

        Rectangle2D rect = graphics.getFontMetrics().getStringBounds(Integer.toString(getPanelId()), graphics);

        return new ImagePoint2D((p0.getX() + p2.getX()) / 2 - (int) (rect.getWidth() / 2),
                (p0.getY() + p2.getY()) / 2 + (int) (rect.getHeight() / 2));
    }
}

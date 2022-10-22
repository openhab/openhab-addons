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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;

/**
 * A shape without any area.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Point extends Shape {
    public Point(ShapeType shapeType, int panelId, Point2D position, int orientation) {
        super(shapeType, panelId, position, orientation);
    }

    @Override
    public List<Point2D> generateOutline() {
        return Arrays.asList(getPosition());
    }

    @Override
    public Point2D labelPosition(Graphics2D graphics, List<Point2D> outline) {
        return outline.get(0);
    }
}

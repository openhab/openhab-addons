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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;
import org.openhab.binding.nanoleaf.internal.model.PositionDatum;

/**
 * Create the correct chape for a given shape type.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ShapeFactory {

    public static Shape CreateShape(ShapeType shapeType, PositionDatum positionDatum) {
        Point2D pos = new Point2D(positionDatum.getPosX(), positionDatum.getPosY());
        switch (shapeType.getDrawingAlgorithm()) {
            case SQUARE:
                return new Square(shapeType, positionDatum.getPanelId(), pos, positionDatum.getOrientation());

            case TRIANGLE:
                return new Triangle(shapeType, positionDatum.getPanelId(), pos, positionDatum.getOrientation());

            case HEXAGON:
                return new Hexagon(shapeType, positionDatum.getPanelId(), pos, positionDatum.getOrientation());

            default:
                return new Point(shapeType, positionDatum.getPanelId(), pos, positionDatum.getOrientation());
        }
    }
}

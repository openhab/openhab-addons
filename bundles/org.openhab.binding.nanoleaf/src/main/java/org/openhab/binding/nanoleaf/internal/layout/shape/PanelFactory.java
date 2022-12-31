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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.eclipse.jdt.annotation.NonNull;
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
public class PanelFactory {

    public static List<Panel> createPanels(List<PositionDatum> panels) {
        List<Panel> result = new ArrayList<>(panels.size());
        Deque<PositionDatum> panelStack = new ArrayDeque<>(panels);
        while (!panelStack.isEmpty()) {
            PositionDatum panel = Objects.requireNonNull(panelStack.peek());
            final ShapeType shapeType = ShapeType.valueOf(panel.getShapeType());
            Panel shape = createPanel(shapeType, takeFirst(shapeType.getNumLightsPerShape(), panelStack));
            result.add(shape);
        }

        return result;
    }

    /**
     * Return the first n elements from the stack.
     * 
     * @param n The number of elements to return
     * @param stack The stack top get elements from
     * @return The first n elements of the stack.
     */
    private static <@NonNull T> List<@NonNull T> takeFirst(int n, Queue<T> queue) {
        List<T> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            var res = queue.poll();
            if (res != null) {
                result.add(res);
            }
        }

        return result;
    }

    private static Panel createPanel(ShapeType shapeType, List<PositionDatum> positionDatum) {
        switch (shapeType.getDrawingAlgorithm()) {
            case SQUARE:
                PositionDatum squareShape = positionDatum.get(0);
                Point2D pos1 = new Point2D(squareShape.getPosX(), squareShape.getPosY());
                return new Square(shapeType, squareShape.getPanelId(), pos1, squareShape.getOrientation());

            case TRIANGLE:
                PositionDatum triangleShape = positionDatum.get(0);
                Point2D pos2 = new Point2D(triangleShape.getPosX(), triangleShape.getPosY());
                return new Triangle(shapeType, triangleShape.getPanelId(), pos2, triangleShape.getOrientation());

            case HEXAGON:
                PositionDatum hexShape = positionDatum.get(0);
                Point2D pos3 = new Point2D(hexShape.getPosX(), hexShape.getPosY());
                return new Hexagon(shapeType, hexShape.getPanelId(), pos3, hexShape.getOrientation());

            case LINE:
                return new SingleLine(shapeType, positionDatum);

            case CORNER:
                return new HexagonCorners(shapeType, positionDatum);

            case NONE:
                PositionDatum noShape = positionDatum.get(0);
                Point2D pos4 = new Point2D(noShape.getPosX(), noShape.getPosY());
                return new NoDraw(shapeType, noShape.getPanelId(), pos4);

            default:
                PositionDatum shape = positionDatum.get(0);
                Point2D pos5 = new Point2D(shape.getPosX(), shape.getPosY());
                return new Point(shapeType, shape.getPanelId(), pos5);
        }
    }
}

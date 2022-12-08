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
import org.openhab.binding.nanoleaf.internal.layout.DrawingSettings;
import org.openhab.binding.nanoleaf.internal.layout.PanelState;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;

/**
 * A panel that shouldn't be drawn (power connector, controller, ...).
 *
 * Especially lines can have controllers and power connectors etc on top of each other.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NoDraw extends Panel {

    private final Point2D position;

    public NoDraw(ShapeType shapeType, int panelId, Point2D position) {
        super(shapeType);
        this.position = position;
    }

    @Override
    public List<Point2D> generateOutline() {
        return Arrays.asList(position);
    }

    @Override
    public void draw(Graphics2D graphics, DrawingSettings settings, PanelState state) {
    }
}

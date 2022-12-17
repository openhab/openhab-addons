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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.layout.DrawingSettings;
import org.openhab.binding.nanoleaf.internal.layout.ImagePoint2D;
import org.openhab.binding.nanoleaf.internal.layout.PanelState;
import org.openhab.binding.nanoleaf.internal.layout.Point2D;
import org.openhab.binding.nanoleaf.internal.layout.ShapeType;
import org.openhab.core.library.types.HSBType;

/**
 * A shape without any area.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Point extends Panel {

    private static final int POINT_DIAMETER = 4;

    private final Point2D position;
    private final int panelId;

    public Point(ShapeType shapeType, int panelId, Point2D position) {
        super(shapeType);
        this.position = position;
        this.panelId = panelId;
    }

    @Override
    public List<Point2D> generateOutline() {
        return Arrays.asList(position);
    }

    @Override
    public void draw(Graphics2D graphics, DrawingSettings settings, PanelState state) {
        ImagePoint2D pos = settings.generateImagePoint(position);

        if (settings.shouldFillWithColor()) {
            HSBType color = state.getHSBForPanel(panelId);
            graphics.setColor(new Color(color.getRGB()));
            graphics.fillOval(pos.getX(), pos.getY(), POINT_DIAMETER, POINT_DIAMETER);
        }

        if (settings.shouldDrawOutline()) {
            graphics.setColor(settings.getOutlineColor());
            graphics.drawOval(pos.getX(), pos.getY(), POINT_DIAMETER, POINT_DIAMETER);
        }

        if (settings.shouldDrawLabels()) {
            graphics.setColor(settings.getLabelColor());
            graphics.drawString(Integer.toString(panelId), pos.getX(), pos.getY());
        }
    }
}

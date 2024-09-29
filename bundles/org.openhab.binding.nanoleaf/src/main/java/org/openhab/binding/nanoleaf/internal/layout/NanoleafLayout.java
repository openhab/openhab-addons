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
package org.openhab.binding.nanoleaf.internal.layout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.layout.shape.Panel;
import org.openhab.binding.nanoleaf.internal.layout.shape.PanelFactory;
import org.openhab.binding.nanoleaf.internal.model.GlobalOrientation;
import org.openhab.binding.nanoleaf.internal.model.Layout;
import org.openhab.binding.nanoleaf.internal.model.PanelLayout;
import org.openhab.binding.nanoleaf.internal.model.PositionDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders the Nanoleaf layout to an image.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NanoleafLayout {

    private static final Logger logger = LoggerFactory.getLogger(NanoleafLayout.class);
    private static final Color COLOR_BACKGROUND = Color.WHITE;

    public static byte[] render(PanelLayout panelLayout, PanelState state, LayoutSettings settings) throws IOException {
        double rotationRadians = 0;
        GlobalOrientation globalOrientation = panelLayout.getGlobalOrientation();
        if (globalOrientation != null) {
            rotationRadians = calculateRotationRadians(globalOrientation);
        }

        Layout layout = panelLayout.getLayout();
        if (layout == null) {
            logger.warn("Returning no image as we don't have any layout to render");
            return new byte[] {};
        }

        List<PositionDatum> positionDatums = layout.getPositionData();
        if (positionDatums == null) {
            logger.warn("Returning no image as we don't have any position datums to render");
            return new byte[] {};
        }

        ImagePoint2D[] size = findSize(positionDatums, rotationRadians);
        final ImagePoint2D min = size[0];
        final ImagePoint2D max = size[1];

        BufferedImage image = new BufferedImage(
                (max.getX() - min.getX()) + 2 * NanoleafBindingConstants.LAYOUT_BORDER_WIDTH,
                (max.getY() - min.getY()) + 2 * NanoleafBindingConstants.LAYOUT_BORDER_WIDTH,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setBackground(COLOR_BACKGROUND);
        g2.clearRect(0, 0, image.getWidth(), image.getHeight());

        DrawingSettings dc = new DrawingSettings(settings, image.getHeight(), min, rotationRadians);
        List<Panel> panels = PanelFactory.createPanels(positionDatums);
        for (Panel panel : panels) {
            panel.draw(g2, dc, state);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static double calculateRotationRadians(GlobalOrientation globalOrientation) {
        Integer maxObj = globalOrientation.getMax();
        int maxValue = maxObj == null ? 360 : (int) maxObj;
        int value = globalOrientation.getValue(); // 0 - 360 measured counter clockwise.
        return ((double) (maxValue - value)) * (Math.PI / 180);
    }

    private static ImagePoint2D[] findSize(List<PositionDatum> positionDatums, double rotationRadians) {
        int maxX = 0;
        int maxY = 0;
        int minX = 0;
        int minY = 0;

        List<Panel> panels = PanelFactory.createPanels(positionDatums);
        for (Panel shape : panels) {
            for (Point2D point : shape.generateOutline()) {
                var rotated = point.rotate(rotationRadians);
                maxX = Math.max(rotated.getX(), maxX);
                maxY = Math.max(rotated.getY(), maxY);
                minX = Math.min(rotated.getX(), minX);
                minY = Math.min(rotated.getY(), minY);
            }
        }

        return new ImagePoint2D[] { new ImagePoint2D(minX, minY), new ImagePoint2D(maxX, maxY) };
    }
}

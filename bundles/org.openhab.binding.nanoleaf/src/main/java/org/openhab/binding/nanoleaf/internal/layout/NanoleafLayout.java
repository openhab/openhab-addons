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

package org.openhab.binding.nanoleaf.internal.layout;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.model.GlobalOrientation;
import org.openhab.binding.nanoleaf.internal.model.Layout;
import org.openhab.binding.nanoleaf.internal.model.PanelLayout;
import org.openhab.binding.nanoleaf.internal.model.PositionDatum;

/**
 * Renders the Nonoleaf layout to an image.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NanoleafLayout {

    public static byte[] render(PanelLayout panelLayout) throws IOException {
        double rotationRadians = 0;
        GlobalOrientation globalOrientation = panelLayout.getGlobalOrientation();
        if (globalOrientation != null) {
            rotationRadians = calculateRotation(globalOrientation);
        }

        Layout layout = panelLayout.getLayout();
        if (layout == null) {
            return new byte[] {};
        }

        List<PositionDatum> panels = layout.getPositionData();
        if (panels == null) {
            return new byte[] {};
        }

        Point2D size[] = findSize(panels, rotationRadians);
        Point2D min = size[0];
        Point2D max = size[1];
        Point2D prev = null;
        Point2D first = null;

        int sideCounter = 0;

        BufferedImage image = new BufferedImage(
                (max.getX() - min.getX()) + 2 * NanoleafBindingConstants.LAYOUT_BORDER_WIDTH,
                (max.getY() - min.getY()) + 2 * NanoleafBindingConstants.LAYOUT_BORDER_WIDTH,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        for (PositionDatum panel : panels) {
            final int expectedSides = ShapeType.valueOf(panel.getShapeType()).getNumSides();
            var rotated = new Point2D(panel.getPosX(), panel.getPosY()).rotate(rotationRadians);

            Point2D current = new Point2D(NanoleafBindingConstants.LAYOUT_BORDER_WIDTH + rotated.getX() - min.getX(),
                    NanoleafBindingConstants.LAYOUT_BORDER_WIDTH - rotated.getY() - min.getY());

            g2.fillOval(current.getX() - NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS / 2,
                    current.getY() - NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS / 2,
                    NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS, NanoleafBindingConstants.LAYOUT_LIGHT_RADIUS);
            g2.drawString(Integer.toString(panel.getPanelId()), current.getX(), current.getY());

            if (sideCounter == 0) {
                first = current;
            }

            if (sideCounter > 0 && sideCounter != expectedSides && prev != null) {
                g2.drawLine(prev.getX(), prev.getY(), current.getX(), current.getY());
            }

            sideCounter++;

            if (sideCounter == expectedSides && first != null) {
                g2.drawLine(current.getX(), current.getY(), first.getX(), first.getY());
                sideCounter = 0;
            }

            prev = current;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static double calculateRotation(GlobalOrientation globalOrientation) {
        Integer maxObj = globalOrientation.getMax();
        int maxValue = maxObj == null ? 360 : (int) maxObj;
        int value = globalOrientation.getValue(); // 0 - 360 measured counter clockwise.
        return ((double) (maxValue - value)) * (Math.PI / 180);
    }

    private static Point2D[] findSize(Collection<PositionDatum> panels, double rotationRadians) {
        int maxX = 0;
        int maxY = 0;
        int minX = 0;
        int minY = 0;

        for (PositionDatum panel : panels) {
            var rotated = new Point2D(panel.getPosX(), panel.getPosY()).rotate(rotationRadians);
            maxX = Math.max(rotated.getX(), maxX);
            maxY = Math.max(rotated.getY(), maxY);
            minX = Math.min(rotated.getX(), minX);
            minY = Math.min(rotated.getY(), minY);
        }

        return new Point2D[] { new Point2D(minX, minY), new Point2D(maxX, maxY) };
    }
}

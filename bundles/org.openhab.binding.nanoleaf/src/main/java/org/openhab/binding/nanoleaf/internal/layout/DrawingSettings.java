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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;

/**
 * Information to the drawing algorithm about which style to use and how to draw.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class DrawingSettings {

    private static final Color COLOR_SIDE = Color.GRAY;
    private static final Color COLOR_TEXT = Color.BLACK;

    private final LayoutSettings layoutSettings;
    private final int imageHeight;
    private final ImagePoint2D min;
    private final double rotationRadians;

    public DrawingSettings(LayoutSettings layoutSettings, int imageHeight, ImagePoint2D min, double rotationRadians) {
        this.imageHeight = imageHeight;
        this.min = min;
        this.rotationRadians = rotationRadians;
        this.layoutSettings = layoutSettings;
    }

    public boolean shouldDrawLabels() {
        return layoutSettings.shouldDrawLabels();
    }

    public boolean shouldDrawCorners() {
        return layoutSettings.shouldDrawCorners();
    }

    public boolean shouldDrawOutline() {
        return layoutSettings.shouldDrawOutline();
    }

    public boolean shouldFillWithColor() {
        return layoutSettings.shouldFillWithColor();
    }

    public Color getOutlineColor() {
        return COLOR_SIDE;
    }

    public Color getLabelColor() {
        return COLOR_TEXT;
    }

    public ImagePoint2D generateImagePoint(Point2D point) {
        return toPictureLayout(point, imageHeight, min, rotationRadians);
    }

    public List<ImagePoint2D> generateImagePoints(List<Point2D> points) {
        return toPictureLayout(points, imageHeight, min, rotationRadians);
    }

    private static ImagePoint2D toPictureLayout(Point2D original, int imageHeight, ImagePoint2D min,
            double rotationRadians) {
        Point2D rotated = original.rotate(rotationRadians);
        return new ImagePoint2D(NanoleafBindingConstants.LAYOUT_BORDER_WIDTH + rotated.getX() - min.getX(),
                imageHeight - NanoleafBindingConstants.LAYOUT_BORDER_WIDTH - rotated.getY() + min.getY());
    }

    private static List<ImagePoint2D> toPictureLayout(List<Point2D> originals, int imageHeight, ImagePoint2D min,
            double rotationRadians) {
        List<ImagePoint2D> result = new ArrayList<>(originals.size());
        for (Point2D original : originals) {
            result.add(toPictureLayout(original, imageHeight, min, rotationRadians));
        }

        return result;
    }
}

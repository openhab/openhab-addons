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
package org.openhab.binding.nanoleaf.internal.layout.shape;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.PackedColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nanoleaf.internal.layout.ImagePoint2D;

/**
 * Paint for triangles with one color in each corner. Used to make gradients between the colors when
 * dividing a hexagon into 6 triangles.
 *
 * https://codeplea.com/triangular-interpolation is instructive for the math.
 *
 * Inspired by
 * https://github.com/hageldave/JPlotter/blob/9c92731f3b29a2cdb14f3dfdeeed6fffde37eee4/jplotter/src/main/java/hageldave/jplotter/util/BarycentricGradientPaint.java,
 * for how to integrate it into Java AWT but kept so simple that I could understand it. It was however far too big to
 * use as a dependency.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class BarycentricTriangleGradient implements Paint {

    private final Color color1;
    private final Color color2;
    private final Color color3;

    private final ImagePoint2D corner1;
    private final ImagePoint2D corner2;
    private final ImagePoint2D corner3;

    public BarycentricTriangleGradient(ImagePoint2D corner1, Color color1, ImagePoint2D corner2, Color color2,
            ImagePoint2D corner3, Color color3) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.corner3 = corner3;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    @Override
    public @Nullable PaintContext createContext(@Nullable ColorModel cm, @Nullable Rectangle deviceBounds,
            @Nullable Rectangle2D userBounds, @Nullable AffineTransform xform, @Nullable RenderingHints hints) {
        return new BarycentricTriangleGradientContext(corner1, color1, corner2, color2, corner3, color3);
    }

    @Override
    public int getTransparency() {
        return OPAQUE;
    }

    private class BarycentricTriangleGradientContext implements PaintContext {

        private final Color color1;
        private final Color color2;
        private final Color color3;

        private final ImagePoint2D corner1;
        private final ImagePoint2D corner2;
        private final ImagePoint2D corner3;

        private final PackedColorModel colorModel = (PackedColorModel) ColorModel.getRGBdefault();

        public BarycentricTriangleGradientContext(ImagePoint2D corner1, Color color1, ImagePoint2D corner2,
                Color color2, ImagePoint2D corner3, Color color3) {
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.corner3 = corner3;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
        }

        @Override
        public void dispose() {
        }

        @Override
        public @Nullable ColorModel getColorModel() {
            return colorModel;
        }

        @Override
        public Raster getRaster(int x, int y, int w, int h) {
            int[] data = new int[h * w];
            DataBufferInt buffer = new DataBufferInt(data, w * h);
            WritableRaster raster = Raster.createPackedRaster(buffer, w, h, w, colorModel.getMasks(), null);

            float denominator = 1f / (((corner2.getY() - corner3.getY()) * (corner1.getX() - corner3.getX()))
                    + ((corner3.getX() - corner2.getX()) * (corner1.getY() - corner3.getY())));

            for (int yPos = 0; yPos < h; yPos++) {
                int imageY = y + yPos;
                for (int xPos = 0; xPos < w; xPos++) {
                    int imageX = xPos + x;

                    float weight1 = (((corner2.getY() - corner3.getY()) * (imageX - corner3.getX()))
                            + ((corner3.getX() - corner2.getX()) * (imageY - corner3.getY()))) * denominator;
                    float weight2 = (((corner3.getY() - corner1.getY()) * (imageX - corner3.getX()))
                            + ((corner1.getX() - corner3.getX()) * (imageY - corner3.getY()))) * denominator;
                    float weight3 = 1 - weight1 - weight2;

                    if (weight1 < 0 || weight2 < 0 || weight3 < 0) {
                        // Outside of triangle
                        data[yPos * w + xPos] = 0;
                    } else {
                        Color c = mergeColors(weight1, color1, weight2, color2, weight3, color3);
                        data[yPos * w + xPos] = c.getRGB();
                    }
                }
            }

            return raster;
        }

        private Color mergeColors(float weight1, Color color1, float weight2, Color color2, float weight3,
                Color color3) {
            float normalize = 1f / (weight1 + weight2 + weight3);
            float r = (color1.getRed() * weight1 + color2.getRed() * weight2 + color3.getRed() * weight3) * normalize;
            float g = (color1.getGreen() * weight1 + color2.getGreen() * weight2 + color3.getGreen() * weight3)
                    * normalize;
            float b = (color1.getBlue() * weight1 + color2.getBlue() * weight2 + color3.getBlue() * weight3)
                    * normalize;
            return new Color((int) r, (int) g, (int) b);
        }
    }
}

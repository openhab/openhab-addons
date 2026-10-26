/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.awtrixng.internal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.awtrixng.internal.dto.ScreenFrame;

/**
 * The {@link ScreenImage} renders a frame of the clock as a PNG with the pixels enlarged and separated by a gap, so it
 * looks like the LED matrix.
 *
 * @author Thomas Lauterbach - Initial contribution (rendering of the AWTRIX Light binding)
 * @author Keith T. Garner - Adapted to the size reported by AWTRIX NG
 */
@NonNullByDefault
public class ScreenImage {

    private static final int PIXEL_SIZE = 10;
    private static final int GAP = 1;
    // Largest panel AWTRIX NG supports (128 wide) with room to spare, to bound the memory of a bogus frame
    private static final int MAX_PIXELS = 128 * 128;

    private ScreenImage() {
    }

    /**
     * @return the PNG, or an empty array if the frame is inconsistent
     * @throws IOException if the image cannot be encoded
     */
    public static byte[] toPng(ScreenFrame frame) throws IOException {
        int width = frame.width;
        int height = frame.height;
        // A frame from the broker can say "pixels":null, which is not the empty default of the DTO
        int[] pixels = frame.pixels;
        // Long, as the product of two large sizes overflows int, and can then pass for a small one
        if (pixels == null || width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS
                || pixels.length != width * height) {
            return new byte[0];
        }

        int cell = PIXEL_SIZE + GAP;
        BufferedImage image = new BufferedImage(width * cell - GAP, height * cell - GAP, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    graphics.setColor(new Color(pixels[y * width + x]));
                    graphics.fillRect(x * cell, y * cell, PIXEL_SIZE, PIXEL_SIZE);
                }
            }
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
    }
}

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lghombot.internal;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.RawType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CameraUtil} is responsible for parsing the raw yuv 422 image from a LG HomBot.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class CameraUtil {

    private static final Logger logger = LoggerFactory.getLogger(CameraUtil.class);

    private CameraUtil() {
        // No need to instance this class.
    }

    /**
     * This converts a non-interleaved YUV-422 image to a JPEG image.
     * 
     * @param yuvData The uncompressed YUV data
     * @param width The width of image.
     * @param height The height of the image.
     * @return A JPEG image as a State
     */
    static State parseImageFromBytes(byte[] yuvData, int width, int height) {
        final int size = width * height;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < size; i++) {
            double y = yuvData[i] & 0xFF;
            double u = yuvData[size + i / 2] & 0xFF;
            double v = yuvData[(int) (size * 1.5 + i / 2.0)] & 0xFF;

            int r = Math.min(Math.max((int) (y + 1.371 * (v - 128)), 0), 255); // red
            int g = Math.min(Math.max((int) (y - 0.336 * (u - 128) - 0.698 * (v - 128)), 0), 255); // green
            int b = Math.min(Math.max((int) (y + 1.732 * (u - 128)), 0), 255); // blue

            int p = (r << 16) | (g << 8) | b; // pixel
            image.setRGB(i % width, i / width, p);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, "jpg", baos)) {
                logger.debug("Couldn't find JPEG writer.");
            }
        } catch (IOException e) {
            logger.info("IOException creating JPEG image.", e);
        }
        byte[] byteArray = baos.toByteArray();
        if (byteArray != null && byteArray.length > 0) {
            return new RawType(byteArray, "image/jpeg");
        } else {
            return UnDefType.UNDEF;
        }
    }
}

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

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.openhab.binding.awtrixng.internal.dto.ScreenFrame;

/**
 * Test cases for the {@link ScreenImage} rendering.
 *
 * @author Keith T. Garner - Initial contribution
 */
class ScreenImageTest {

    private static ScreenFrame frame(int width, int height, int... pixels) {
        ScreenFrame frame = new ScreenFrame();
        frame.width = width;
        frame.height = height;
        frame.pixels = pixels;
        return frame;
    }

    @Test
    void rendersEnlargedPixelsWithGaps() throws IOException {
        byte[] png = ScreenImage.toPng(frame(2, 2, 0xFF0000, 0x00FF00, 0x0000FF, 0x123456));

        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(21, image.getWidth());
        assertEquals(21, image.getHeight());
        assertEquals(0xFF0000, image.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0xFF0000, image.getRGB(9, 9) & 0xFFFFFF);
        assertEquals(0, image.getRGB(10, 5) & 0xFFFFFF, "gap between pixels");
        assertEquals(0x00FF00, image.getRGB(11, 0) & 0xFFFFFF);
        assertEquals(0x0000FF, image.getRGB(0, 11) & 0xFFFFFF);
        assertEquals(0x123456, image.getRGB(20, 20) & 0xFFFFFF);
    }

    @Test
    void followsTheReportedSize() throws IOException {
        byte[] png = ScreenImage.toPng(frame(4, 1, 1, 2, 3, 4));

        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(43, image.getWidth());
        assertEquals(10, image.getHeight());
    }

    @Test
    void frameWithoutPixelsGivesNoImage() throws IOException {
        assertEquals(0, ScreenImage.toPng(new ScreenFrame()).length);
    }

    @Test
    void inconsistentFrameGivesNoImage() throws IOException {
        assertEquals(0, ScreenImage.toPng(frame(2, 2, 1, 2, 3)).length);
        assertEquals(0, ScreenImage.toPng(frame(0, 0)).length);
        assertEquals(0, ScreenImage.toPng(frame(-1, 1, 1)).length);
    }

    @Test
    void sizeThatOverflowsGivesNoImage() throws IOException {
        // 65536 * 65536 is 0 as an int
        assertEquals(0, ScreenImage.toPng(frame(65536, 65536)).length);
        assertEquals(0, ScreenImage.toPng(frame(129, 129)).length);
    }

    @Test
    void frameWithNullPixelsGivesNoImage() throws IOException {
        ScreenFrame frame = Helper.fromJson("{\"width\":32,\"height\":8,\"pixels\":null}", ScreenFrame.class);

        assertNotNull(frame);
        assertEquals(0, ScreenImage.toPng(frame).length);
    }
}

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
package org.openhab.binding.dreame.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameMapGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPoint;
import org.openhab.binding.dreame.internal.model.DreameMapZoneGeometry;
import org.openhab.binding.dreame.internal.model.DreameMowerPose;

@NonNullByDefault
class DreameMapPngRendererTest {

    private static final DreameMapGeometry GEOMETRY = new DreameMapGeometry(1, 0, 0, 100, 200,
            List.of(new DreameMapZoneGeometry(1, "Garden",
                    List.of(new DreameMapPoint(0, 0), new DreameMapPoint(100, 0), new DreameMapPoint(100, 200)))),
            List.of(), List.of());

    @Test
    void rendersDreameMapAsPng() throws IOException {
        byte[] png = DreameMapPngRenderer.render(List.of(GEOMETRY), 1, new DreameMowerPose(20, 30, 40), false);

        assertNotNull(png);
        assertArrayEquals(new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 }, java.util.Arrays.copyOf(png, 4));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(896, image.getWidth());
        assertEquals(1024, image.getHeight());
    }

    @Test
    void rotatesMovaMapClockwise() throws IOException {
        byte[] png = DreameMapPngRenderer.render(List.of(GEOMETRY), 1, new DreameMowerPose(20, 30, 40), true);

        assertNotNull(png);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(1024, image.getWidth());
        assertEquals(896, image.getHeight());
    }
}

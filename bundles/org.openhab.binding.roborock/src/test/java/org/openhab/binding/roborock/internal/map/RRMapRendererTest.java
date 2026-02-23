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
package org.openhab.binding.roborock.internal.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class RRMapRendererTest {

    private static final int UPSCALE_TARGET_MAX_DIMENSION = 1024;
    private static final int DOWNSCALE_TARGET_MAX_DIMENSION = 2048;

    @Test
    void renderAsPngProducesColorizedImageWithOverlays() throws Exception {
        RRMapRenderer renderer = new RRMapRenderer();

        byte[] baseImage = new byte[20 * 20];
        for (int i = 0; i < baseImage.length; i++) {
            baseImage[i] = (byte) 0xFF;
        }

        RRMapData mapData = new RRMapData(20, 20, 0, 0, baseImage, 500, 500, 0, 750, 500, 650, 500,
                List.of(new RRMapData.MapPoint(500, 500), new RRMapData.MapPoint(450, 500)),
                List.of(new RRMapData.MapPoint(500, 500), new RRMapData.MapPoint(650, 500)),
                List.of(new RRMapData.MapPoint(500, 500), new RRMapData.MapPoint(500, 650)),
                List.of(new RRMapData.MapZone(250, 250, 350, 350)), List.of(new RRMapData.MapWall(600, 200, 600, 400)),
                List.of(new RRMapData.MapArea(650, 650, 700, 650, 700, 700, 650, 700)),
                List.of(new RRMapData.MapArea(550, 650, 600, 650, 600, 700, 550, 700)),
                List.of(new RRMapData.MapArea(450, 650, 500, 650, 500, 700, 450, 700)),
                List.of(new RRMapData.MapObstacle(700, 700, 2)), List.of(new RRMapData.MapObstacle(300, 300, 1)),
                new byte[20 * 20]);

        byte[] png = renderer.renderAsPng(mapData);
        assertTrue(png.length > 8);
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 0x50, png[1]);
        assertEquals((byte) 0x4E, png[2]);
        assertEquals((byte) 0x47, png[3]);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(UPSCALE_TARGET_MAX_DIMENSION, image.getWidth());
        assertEquals(UPSCALE_TARGET_MAX_DIMENSION, image.getHeight());

        assertTrue(hasMatchingPixel(image, 0, 0, image.getWidth() - 1, image.getHeight() - 1,
                c -> !(c.getRed() == c.getGreen() && c.getGreen() == c.getBlue())));

        assertTrue(hasMatchingPixel(image, 0, 0, image.getWidth() - 1, image.getHeight() - 1,
                c -> c.getBlue() > 220 && c.getRed() < 100));

        assertTrue(hasMatchingPixel(image, 0, 0, image.getWidth() - 1, image.getHeight() - 1,
                c -> c.getRed() > 180 && c.getGreen() > 120 && c.getBlue() < 120));

        assertTrue(hasMatchingPixel(image, 0, 0, image.getWidth() - 1, image.getHeight() - 1,
                c -> c.getRed() > 180 && c.getGreen() < 120));

        assertTrue(hasMatchingPixel(image, 0, 0, image.getWidth() - 1, image.getHeight() - 1,
                c -> c.getGreen() > c.getBlue() && c.getGreen() > c.getRed()));

        assertTrue(hasMatchingPixel(image, 0, 0, image.getWidth() - 1, image.getHeight() - 1,
                c -> c.getRed() > c.getGreen() && c.getRed() > c.getBlue()));
    }

    @Test
    void renderAsPngDrawsCarpetMapMaskOverlay() throws Exception {
        RRMapRenderer renderer = new RRMapRenderer();

        int width = 20;
        int height = 20;
        byte[] baseImage = new byte[width * height];
        for (int i = 0; i < baseImage.length; i++) {
            baseImage[i] = (byte) 0xFF;
        }

        byte[] carpetMask = new byte[width * height];
        carpetMask[5 * width + 8] = 0x01;

        RRMapData mapData = new RRMapData(width, height, 0, 0, baseImage, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), new byte[width * height], carpetMask);

        byte[] png = renderer.renderAsPng(mapData);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));

        assertTrue(hasMatchingPixel(image, 0, 0, image.getWidth() - 1, image.getHeight() - 1,
                c -> c.getRed() > 220 && c.getGreen() > 170 && c.getBlue() < 210));
    }

    @Test
    void obstacleTypeLabelReturnsExpectedValues() {
        assertEquals("cable", RRMapRenderer.obstacleTypeLabel(0));
        assertEquals("fabric/paper balls", RRMapRenderer.obstacleTypeLabel(51));
        assertEquals("furniture with a crossbar", RRMapRenderer.obstacleTypeLabel(27));
        assertEquals(null, RRMapRenderer.obstacleTypeLabel(255));
    }

    @Test
    void renderAsPngUpscalesSmallImagesToMinimumTargetMaxDimension() throws Exception {
        RRMapRenderer renderer = new RRMapRenderer();

        RRMapData mapData = createMapData(223, 254);
        byte[] png = renderer.renderAsPng(mapData);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(Math.round(223 * (UPSCALE_TARGET_MAX_DIMENSION / 254.0f)), image.getWidth());
        assertEquals(UPSCALE_TARGET_MAX_DIMENSION, image.getHeight());
    }

    @Test
    void renderAsPngKeepsOriginalSizeWhenAlreadyWithinTargetRange() throws Exception {
        RRMapRenderer renderer = new RRMapRenderer();

        RRMapData mapData = createMapData(1200, 900);
        byte[] png = renderer.renderAsPng(mapData);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(1200, image.getWidth());
        assertEquals(900, image.getHeight());
    }

    @Test
    void renderAsPngDownscalesLargeImagesToMaximumTargetMaxDimension() throws Exception {
        RRMapRenderer renderer = new RRMapRenderer();

        RRMapData mapData = createMapData(3000, 1500);
        byte[] png = renderer.renderAsPng(mapData);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(DOWNSCALE_TARGET_MAX_DIMENSION, image.getWidth());
        assertEquals(Math.round(1500 * (DOWNSCALE_TARGET_MAX_DIMENSION / 3000.0f)), image.getHeight());
    }

    @Test
    void renderAsPngPlacesMarkersAtCoordinateAnchoredPositions() throws Exception {
        RRMapRenderer renderer = new RRMapRenderer();

        int width = 1200;
        int height = 1200;
        byte[] baseImage = new byte[width * height];
        for (int i = 0; i < baseImage.length; i++) {
            baseImage[i] = (byte) 0xFF;
        }

        RRMapData mapData = new RRMapData(width, height, 0, 0, baseImage, 500, 500, 0, 1000, 500, null, null, List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(new RRMapData.MapObstacle(1500, 500, 2)), List.of(), new byte[width * height]);

        byte[] png = renderer.renderAsPng(mapData);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));

        int robotX = renderedX(mapData, image.getWidth(), 500);
        int robotY = renderedY(mapData, 500);
        int chargerX = renderedX(mapData, image.getWidth(), 1000);
        int chargerY = renderedY(mapData, 500);
        int obstacleX = renderedX(mapData, image.getWidth(), 1500);
        int obstacleY = renderedY(mapData, 500);

        assertTrue(hasMatchingPixel(image, robotX - 2, robotY - 2, robotX + 2, robotY + 2,
                c -> c.getBlue() > 180 && c.getBlue() > c.getRed()));
        assertTrue(hasMatchingPixel(image, chargerX - 2, chargerY - 2, chargerX + 2, chargerY + 2,
                c -> c.getRed() > 200 && c.getGreen() > 150 && c.getBlue() < 120));
        assertTrue(hasMatchingPixel(image, obstacleX - 2, obstacleY - 2, obstacleX + 2, obstacleY + 2,
                c -> c.getRed() > 180 && c.getGreen() < 120));

        assertTrue(hasMatchingPixel(image, robotX - 6, robotY - 1, robotX - 3, robotY + 1,
                c -> c.getRed() > 220 && c.getGreen() > 220 && c.getBlue() > 220));
    }

    private RRMapData createMapData(int width, int height) {
        byte[] imageData = new byte[width * height];
        for (int i = 0; i < imageData.length; i++) {
            imageData[i] = (byte) 0xFF;
        }

        return new RRMapData(width, height, 0, 0, imageData, null, null, null, null, null, null, null, List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                new byte[width * height]);
    }

    private int renderedX(RRMapData mapData, int renderWidth, int robotCoordinateX) {
        float sourceX = mapData.imageWidth() + mapData.left() - (robotCoordinateX / 50.0f);
        return renderWidth - 1 - Math.round(sourceX);
    }

    private int renderedY(RRMapData mapData, int robotCoordinateY) {
        float sourceY = mapData.imageHeight() - (robotCoordinateY / 50.0f - mapData.top());
        return Math.round(sourceY);
    }

    private boolean hasMatchingPixel(BufferedImage image, int xMin, int yMin, int xMax, int yMax,
            Predicate<Color> predicate) {
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                Color color = new Color(image.getRGB(x, y), true);
                if (predicate.test(color)) {
                    return true;
                }
            }
        }
        return false;
    }
}

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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Verifies complete and incremental map reconstruction without a device.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameVacuumMapStateTest {
    @Test
    void rendersRoomsPathRobotAndDockInBothFormats() throws Exception {
        DreameVacuumMapState state = new DreameVacuumMapState();
        DreameVacuumMapState.Images images = Objects
                .requireNonNull(state.accept(frame(1, 1, 'I', 0, 0, 3, 2, new byte[] { 1, 1, 2, (byte) 129, 2, 0 },
                        "{\"tr\":\"M0,0L50,0\",\"seg_inf\":{\"1\":{\"name\":\"S8O8Y2hl\",\"type\":6},"
                                + "\"2\":{\"type\":4,\"index\":1}}}")));

        BufferedImage png = Objects.requireNonNull(ImageIO.read(new ByteArrayInputStream(images.png())));
        String svg = new String(images.svg(), StandardCharsets.UTF_8);
        assertEquals(12, png.getWidth());
        assertEquals(8, png.getHeight());
        assertTrue(svg.contains("<g shape-rendering=\"crispEdges\">"));
        assertTrue(svg.contains("</g><path fill=\"none\" stroke=\"#2563eb\""));
        assertTrue(svg.contains("stroke=\"#2563eb\""));
        assertTrue(svg.contains("<title>Robot</title>"));
        assertTrue(svg.contains("<title>Dock</title>"));
        assertTrue(svg.contains(">Küche (1)</text>"));
        assertTrue(svg.contains(">Kitchen 2 (2)</text>"));
        assertEquals("1=Küche, 2=Kitchen 2", images.rooms());
        assertTrue(
                state.diagnostic().contains("roomMetadata=[1:type=6/index=-1/name=true,2:type=4/index=1/name=false]"));
        assertFalse(state.needsBase());
    }

    @Test
    void mergesNewerPartialFramesAfterCloudBaseSnapshot() {
        DreameVacuumMapState state = new DreameVacuumMapState();
        assertNotNull(state.accept(frame(7, 10, 'I', 0, 0, 2, 1, new byte[] { 1, 1 }, "{}")));
        assertNotNull(state.accept(frame(7, 11, 'P', 50, 0, 2, 1, new byte[] { 1, 2 }, "{\"tr\":\"M50,0L50,0\"}")));
        assertFalse(state.needsBase());

        assertNotNull(state.accept(frame(7, 13, 'P', 0, 0, 1, 1, new byte[] { 1 }, "{}")));
        assertFalse(state.needsBase());
        assertNotNull(state.accept(frame(7, 14, 'P', 0, 0, 1, 1, new byte[] { 1 }, "{}")));
        assertNotNull(state.accept(frame(7, 15, 'I', 0, 0, 1, 1, new byte[] { 1 }, "{}")));
        assertFalse(state.needsBase());
    }

    @Test
    void ignoresOldFramesAndRejectsWrongMapAndUnsupportedV3Diff() {
        DreameVacuumMapState state = new DreameVacuumMapState();
        String complete = frame(1, 2, 'I', 0, 0, 1, 1, new byte[] { 1 }, "{}");
        assertNotNull(state.accept(complete));
        assertNull(state.accept(complete));
        assertFalse(state.needsBase());
        assertNull(state.accept(frame(2, 3, 'P', 0, 0, 1, 1, new byte[] { 1 }, "{}")));
        assertTrue(state.needsBase());
        assertNotNull(state.accept(frame(2, 4, 'I', 0, 0, 1, 1, new byte[] { 1 }, "{\"saveMapId\":1}")));
        assertNull(state.accept(frame(2, 5, 'P', 0, 0, 1, 1, new byte[] { 1 }, "{\"saveMapId\":1,\"diff\":[]}")));
        assertTrue(state.needsBase());
    }

    @Test
    void requestsNewBaseWhenFrameCounterResetsForNewCleaningSession() {
        DreameVacuumMapState state = new DreameVacuumMapState();
        assertNotNull(state.accept(frame(1, 1660, 'I', 0, 0, 1, 1, new byte[] { 1 }, "{}")));

        assertNull(state.accept(frame(1, 363, 'P', 0, 0, 1, 1, new byte[] { 1 }, "{}")));
        assertTrue(state.needsBase());
        assertTrue(state.diagnostic().startsWith("missing-base/type=P/map=1/frame=363"));

        assertNotNull(state.accept(frame(1, 364, 'I', 0, 0, 1, 1, new byte[] { 2 }, "{}")));
        assertFalse(state.needsBase());
        assertNotNull(state.accept(frame(1, 365, 'P', 0, 0, 1, 1, new byte[] { 1 }, "{}")));
    }

    @Test
    void enrichesLiveMapWithNamesFromSelectedSavedMap() {
        DreameVacuumMapState state = new DreameVacuumMapState();
        assertEquals("1",
                Objects.requireNonNull(state.accept(frame(5, 10, 'I', 0, 0, 1, 1, new byte[] { 1 }, "{}"))).rooms());
        String saved = frame(5, 1, 'I', 0, 0, 1, 1, new byte[] { 1 },
                "{\"seg_inf\":{\"1\":{\"name\":\"S8O8Y2hl\",\"type\":6}}}");

        DreameVacuumMapState.Images enriched = Objects
                .requireNonNull(state.acceptMapList("{\"curr_id\":5,\"mapstr\":[{\"map\":\"" + saved + "\"}]}"));

        assertEquals("1=Küche", enriched.rooms());
        assertTrue(new String(enriched.svg(), StandardCharsets.UTF_8).contains(">Küche (1)</text>"));
        assertTrue(state.diagnostic().startsWith("published-map-list/map=5/rooms=1"));
    }

    @Test
    void filtersUnknownPixelSegmentsWhenSavedRoomMetadataIsAvailable() {
        DreameVacuumMapState state = new DreameVacuumMapState();
        DreameVacuumMapState.Images images = Objects.requireNonNull(state.accept(frame(5, 10, 'I', 0, 0, 3, 1,
                new byte[] { 1, 13, 56 }, "{\"seg_inf\":{\"1\":{\"name\":\"S8O8Y2hl\",\"type\":6}}}")));

        String svg = new String(images.svg(), StandardCharsets.UTF_8);
        assertEquals("1=Küche", images.rooms());
        assertTrue(svg.contains(">Küche (1)</text>"));
        assertFalse(svg.contains("Room 13"));
        assertFalse(svg.contains("Room 56"));
    }

    @Test
    void cachesSavedRoomNamesUntilLiveMapArrives() {
        DreameVacuumMapState state = new DreameVacuumMapState();
        String saved = frame(5, 1, 'I', 0, 0, 1, 1, new byte[] { 1 },
                "{\"seg_inf\":{\"1\":{\"name\":\"S8O8Y2hl\",\"type\":6}}}");
        assertNull(state.acceptMapList("{\"curr_id\":5,\"mapstr\":[{\"map\":\"" + saved + "\"}]}"));
        assertTrue(state.diagnostic().startsWith("map-list-room-metadata-cached/map=5/rooms=1"));

        assertEquals("1=Küche",
                Objects.requireNonNull(state.accept(frame(5, 10, 'I', 0, 0, 1, 1, new byte[] { 1 }, "{}"))).rooms());
    }

    @Test
    void rejectsMalformedAndOversizedInput() {
        DreameVacuumMapState state = new DreameVacuumMapState();
        assertNull(state.accept("not-base64"));
        assertTrue(state.needsBase());
        assertNull(state.accept("A".repeat(2 * 1024 * 1024 + 1)));
    }

    public static String frame(int mapId, int frameId, int type, int left, int top, int width, int height,
            byte[] pixels, String metadata) {
        byte[] json = metadata.getBytes(StandardCharsets.UTF_8);
        byte[] raw = new byte[27 + pixels.length + json.length];
        number(raw, 0, mapId);
        number(raw, 2, frameId);
        raw[4] = (byte) type;
        number(raw, 5, 50);
        number(raw, 7, 50);
        number(raw, 9, 0);
        number(raw, 11, 0);
        number(raw, 13, 0);
        number(raw, 17, 50);
        number(raw, 19, width);
        number(raw, 21, height);
        number(raw, 23, left);
        number(raw, 25, top);
        System.arraycopy(pixels, 0, raw, 27, pixels.length);
        System.arraycopy(json, 0, raw, 27 + pixels.length, json.length);
        Deflater deflater = new Deflater();
        deflater.setInput(raw);
        deflater.finish();
        byte[] compressed = new byte[raw.length + 64];
        int length = deflater.deflate(compressed);
        deflater.end();
        return Base64.getEncoder().encodeToString(java.util.Arrays.copyOf(compressed, length));
    }

    private static void number(byte[] data, int offset, int value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >> 8);
    }
}

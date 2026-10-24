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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Verifies bounded map rendering and message filtering.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumMapImageTest {
    @Test
    void svgUsesMatchingDimensionsAndVectorPixelRuns() throws Exception {
        byte[] png = Objects.requireNonNull(DreameVacuumMapImage.render(encode(frame())));
        byte[] svg = Objects.requireNonNull(DreameVacuumMapImage.toSvg(png));
        var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(svg));
        assertEquals("svg", document.getDocumentElement().getTagName());
        assertEquals("0 0 2 2", document.getDocumentElement().getAttribute("viewBox"));
        var path = (org.w3c.dom.Element) document.getElementsByTagName("path").item(0);
        assertEquals("M0 1h1v1h-1z", Objects.requireNonNull(path).getAttribute("d"));
        assertEquals(0, document.getElementsByTagName("image").getLength());
        assertNull(DreameVacuumMapImage.toSvg(new byte[] { 1, 2 }));
    }

    @Test
    void rendersCompleteFrameWithCorrectDimensionsAndOrientation() throws IOException {
        byte[] png = Objects.requireNonNull(DreameVacuumMapImage.render(encode(frame())));
        var image = Objects.requireNonNull(ImageIO.read(new ByteArrayInputStream(png)));
        assertEquals(2, image.getWidth());
        assertEquals(2, image.getHeight());
        assertEquals(0x456B85, image.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0xF3F4F6, image.getRGB(0, 0) & 0xFFFFFF);
    }

    @Test
    void rejectsPartialEncryptedTruncatedAndOversizedMaps() throws IOException {
        byte[] raw = frame();
        raw[4] = 80;
        assertNull(DreameVacuumMapImage.render(encode(raw)));
        raw[4] = 73;
        raw[19] = 100;
        assertNull(DreameVacuumMapImage.render(encode(raw)));
        assertNull(DreameVacuumMapImage.render(encode(frame()) + ",secret"));
        assertNull(DreameVacuumMapImage.render("not!base64"));
        assertNull(DreameVacuumMapImage.render(encode(new byte[1024 * 1024 + 1])));
        assertNull(DreameVacuumMapImage.render("eJw="));
    }

    @Test
    void acceptsOnlySuccessfulMapPropertiesForConfirmedModel() throws IOException {
        String payload = "{\"method\":\"properties_changed\",\"params\":[{\"siid\":6,\"piid\":1,\"code\":0,\"value\":\""
                + encode(frame()) + "\"}]}";
        assertNotNull(
                DreameVacuumMapImage.fromMessage(payload.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.r9445d"));
        assertNull(DreameVacuumMapImage.fromMessage(payload.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.other"));
        assertNull(DreameVacuumMapImage.fromMessage(
                payload.replace("properties_changed", "event_occured").getBytes(StandardCharsets.UTF_8),
                "dreame.vacuum.r9445d"));
        assertNull(DreameVacuumMapImage.fromMessage(
                payload.replace("\"code\":0", "\"code\":-1").getBytes(StandardCharsets.UTF_8), "dreame.vacuum.r9445d"));
    }

    @Test
    void extractsOnlyTheMapListObjectName() {
        String payload = "{\"method\":\"properties_changed\",\"params\":[{\"siid\":6,\"piid\":8,\"code\":0,"
                + "\"value\":\"{\\\"object_name\\\":\\\"ali_dreame/device/map-list\\\",\\\"md5\\\":\\\"private\\\"}\"}]}";
        assertEquals("ali_dreame/device/map-list", DreameVacuumMapImage
                .mapListObjectNameFromMessage(payload.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.r9445d"));
        assertNull(DreameVacuumMapImage.mapListObjectNameFromMessage(
                payload.replace("\"piid\":8", "\"piid\":9").getBytes(StandardCharsets.UTF_8), "dreame.vacuum.r9445d"));
        assertEquals("ali_dreame/device/map-list", DreameVacuumMapImage.mapListObjectName(
                "\"{\\\"object_name\\\":\\\"ali_dreame/device/map-list\\\",\\\"md5\\\":\\\"private\\\"}\""));
    }

    private static byte[] frame() {
        byte[] raw = new byte[31];
        raw[4] = 73;
        raw[17] = 50;
        raw[19] = 2;
        raw[21] = 2;
        raw[27] = 1;
        return raw;
    }

    private static String encode(byte[] raw) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream stream = new DeflaterOutputStream(bytes)) {
            stream.write(raw);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }
}

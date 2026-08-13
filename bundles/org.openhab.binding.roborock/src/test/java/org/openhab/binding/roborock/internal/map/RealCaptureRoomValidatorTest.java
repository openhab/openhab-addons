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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.RoborockException;

/**
 * Not a pass/fail unit test: a manual validator for checking {@link RoomAtRobotResolver}'s
 * coordinate transform (and the wider "current room" join) against a real RR map capture, since
 * every other test in this suite builds synthetic byte arrays and none exercises real captured
 * bytes end to end.
 * <p>
 * Usage: pull a real capture via the {@code downloadRrMap} thing action while the vacuum is
 * actively cleaning (so the map payload carries a live robot position), then run:
 *
 * <pre>
 * RRMAP_CAPTURE_FILE=/path/to/roborock-xxxx.rrmap mvn -Dtest=RealCaptureRoomValidatorTest test
 * </pre>
 *
 * The file may be either the raw parser payload or the gzip-compressed file {@code downloadRrMap}
 * writes to disk (auto-detected via the gzip magic header) - point this directly at a downloaded
 * file, no manual decompression needed. When {@code RRMAP_CAPTURE_FILE} is unset, this test is
 * skipped (JUnit "disabled", not "failed"), so it never blocks a normal build.
 * <p>
 * It prints the parsed robot position, the pixel index {@link RoomAtRobotResolver} computed from
 * it, the raw segment byte at that pixel, and the resolved segment id - enough to confirm or
 * disprove the px/py transform's derivation (in particular the "-1" term and the Y-axis
 * orientation) against ground truth: cross-reference the printed px/py against where the robot
 * marker actually renders in the PNG from the same capture (e.g. via the {@code map} channel or
 * {@code RRMapRenderer.renderAsPng}) for the same capture.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault({})
class RealCaptureRoomValidatorTest {

    private static final String CAPTURE_FILE_ENV_VAR = "RRMAP_CAPTURE_FILE";
    private static final int GZIP_MAGIC_BYTE_0 = 0x1f;
    private static final int GZIP_MAGIC_BYTE_1 = 0x8b;

    @Test
    void printsRoomResolutionDiagnosticsForARealCapture() throws IOException, RoborockException {
        String captureFilePath = System.getenv(CAPTURE_FILE_ENV_VAR);
        Assumptions.assumeTrue(captureFilePath != null && !captureFilePath.isBlank(), "Skipped: set "
                + CAPTURE_FILE_ENV_VAR + " to a real .rrmap capture (raw or gzip) to run this validator. " + "Usage: "
                + CAPTURE_FILE_ENV_VAR + "=/path/to/roborock-xxxx.rrmap mvn -Dtest=RealCaptureRoomValidatorTest test");

        byte[] fileBytes = Files.readAllBytes(Path.of(captureFilePath));
        byte[] mapPayload = isGzipped(fileBytes) ? gunzip(fileBytes) : fileBytes;

        RRMapData mapData = new RRMapParser().parse(mapPayload);
        Integer robotX = mapData.robotX();
        Integer robotY = mapData.robotY();

        System.out.println("== RoomAtRobotResolver real-capture validation ==");
        System.out.println("file: " + captureFilePath);
        System.out.println("imageWidth=" + mapData.imageWidth() + " imageHeight=" + mapData.imageHeight() + " top="
                + mapData.top() + " left=" + mapData.left());
        System.out.println("robotX=" + robotX + " robotY=" + robotY);

        if (robotX == null || robotY == null) {
            System.out.println("No robot position in this capture (robot likely docked/idle) - "
                    + "capture a payload while the vacuum is actively cleaning to validate the transform.");
            return;
        }

        int px = Math.round(robotX / 50.0f) - mapData.left() - 1;
        int py = Math.round(robotY / 50.0f) - mapData.top() - 1;
        System.out.println("computed px=" + px + " py=" + py);

        if (px < 0 || py < 0 || px >= mapData.imageWidth() || py >= mapData.imageHeight()) {
            System.out.println("computed pixel is out of image bounds - transform likely needs correcting");
        } else {
            int pixelValue = mapData.imageData()[py * mapData.imageWidth() + px] & 0xFF;
            System.out.println("raw segment byte at (px,py) = 0x" + Integer.toHexString(pixelValue));
        }

        Optional<Integer> segmentId = RoomAtRobotResolver.resolveSegmentId(mapData, robotX, robotY);
        System.out.println("resolved segment id (including radius-2 fallback) = "
                + segmentId.map(String::valueOf).orElse("<none>"));
        System.out.println("Cross-check: render this same capture to PNG and confirm the robot marker sits in the room "
                + "whose color at (px,py) matches the segment id above.");
    }

    private static boolean isGzipped(byte[] bytes) {
        return bytes.length >= 2 && (bytes[0] & 0xFF) == GZIP_MAGIC_BYTE_0 && (bytes[1] & 0xFF) == GZIP_MAGIC_BYTE_1;
    }

    private static byte[] gunzip(byte[] gzipBytes) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new java.io.ByteArrayInputStream(gzipBytes));
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            gzipInputStream.transferTo(output);
            return output.toByteArray();
        }
    }
}

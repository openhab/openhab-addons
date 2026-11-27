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
package org.openhab.binding.bydbatterybox.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Loads frames captured from a real battery. Each fixture file holds comment lines starting with {@code #} and one
 * line with the reply frame in hex. Serial numbers in the fixtures are masked and their CRC recomputed.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public final class Fixtures {

    private Fixtures() {
    }

    public static byte[] frame(String name) {
        String resource = "/fixtures/" + name + ".hex";
        try (InputStream in = Fixtures.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("missing fixture " + resource);
            }
            String hex = new String(in.readAllBytes(), StandardCharsets.US_ASCII).lines()
                    .filter(line -> !line.isBlank() && !line.startsWith("#")).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("no frame in " + resource));
            return HexFormat.of().parseHex(hex.trim());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return a copy of the fixture frame with one byte replaced and the Modbus CRC recomputed
     */
    public static byte[] frameWithByte(String name, int offset, int value) {
        byte[] frame = frame(name);
        frame[offset] = (byte) value;
        int crc = 0xFFFF;
        for (int i = 0; i < frame.length - 2; i++) {
            crc ^= frame[i] & 0xFF;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xA001 : crc >>> 1;
            }
        }
        frame[frame.length - 2] = (byte) crc;
        frame[frame.length - 1] = (byte) (crc >>> 8);
        return frame;
    }

    /**
     * Concatenates the chunk payloads (frame bytes 5..132) of numbered fixtures {@code prefix-1 ... prefix-count}.
     */
    public static byte[] chunkPayloads(String prefix, int count) {
        byte[] buffer = new byte[count * 128];
        for (int i = 0; i < count; i++) {
            System.arraycopy(frame(prefix + "-" + (i + 1)), 5, buffer, i * 128, 128);
        }
        return buffer;
    }
}

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
package org.openhab.binding.tibberpulselocal.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Test fixtures. The anonymised frame has a synthetic meter id and valid CRCs; see {@code design.md} section 7.1.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public final class Fixtures {

    /** Synthetic meter server id used in the fixture. */
    public static final String METER_ID = "0a01454652aabbccddee";
    /** Synthetic Pulse device id used in test headers. */
    public static final String DEVICE_ID = "0123456789abcdef";

    private Fixtures() {
    }

    /**
     * @return the 712-byte anonymised SML frame
     */
    public static byte[] observedFrame() {
        try (InputStream in = Fixtures.class.getResourceAsStream("/sml/frame-observed-anonymised.hex")) {
            if (in == null) {
                throw new IllegalStateException("fixture missing");
            }
            String hex = new String(in.readAllBytes(), StandardCharsets.US_ASCII).replaceAll("\\s+", "");
            return HexFormat.of().parseHex(hex);
        } catch (IOException e) {
            throw new IllegalStateException("fixture unreadable", e);
        }
    }

    /**
     * @return a copy of {@code data} with the byte at {@code index} replaced
     */
    public static byte[] with(byte[] data, int index, int value) {
        byte[] copy = data.clone();
        copy[index] = (byte) value;
        return copy;
    }
}

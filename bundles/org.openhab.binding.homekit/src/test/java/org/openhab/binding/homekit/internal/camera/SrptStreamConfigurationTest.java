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
package org.openhab.binding.homekit.internal.camera;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.camera.tlv.AudioSelectedParameters;
import org.openhab.binding.homekit.internal.camera.tlv.SrtpStreamConfiguration;
import org.openhab.binding.homekit.internal.camera.tlv.SrtpStreamConfiguration.SessionCommand;
import org.openhab.binding.homekit.internal.camera.tlv.VideoSelectedParameters;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;

/**
 * Tests for {@link SrtpStreamConfiguration} TLV encoding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class SrptStreamConfigurationTest {

    private static final byte[] SESSION_ID = new byte[16];

    private static final AudioSelectedParameters AUDIO = new AudioSelectedParameters(AudioCodecType.OPUS, 1, false, 24,
            20, 110, 0x12345678L, 24, 0.5f, false, 13);
    private static final VideoSelectedParameters VIDEO = new VideoSelectedParameters(0x00, 0x02, 1280, 720, 30, 99,
            0x87654321L, 300, 0.5f);

    static {
        Arrays.fill(SESSION_ID, (byte) 0x11);
    }

    @Test
    void testStartEncodesSessionControlAndAudio() {
        Map<Integer, byte[]> root = Tlv8Codec
                .decode(new SrtpStreamConfiguration(SESSION_ID, SessionCommand.START, AUDIO).encode());

        assertNull(root.get(0x02));
        assertArrayEquals(AUDIO.encode(), root.get(0x03));

        byte[] controlBytes = root.get(0x01);
        assertNotNull(controlBytes);
        Map<Integer, byte[]> control = Tlv8Codec.decode(controlBytes);
        assertArrayEquals(SESSION_ID, control.get(0x01));
        assertArrayEquals(new byte[] { 1 }, control.get(0x02));
    }

    @Test
    void testStartWithVideoEncodesVideoBeforeAudio() {
        Map<Integer, byte[]> root = Tlv8Codec
                .decode(new SrtpStreamConfiguration(SESSION_ID, SessionCommand.START, VIDEO, AUDIO).encode());

        assertArrayEquals(VIDEO.encode(), root.get(0x02));
        assertArrayEquals(AUDIO.encode(), root.get(0x03));
    }

    @Test
    void testEndEncodesCommandZero() {
        Map<Integer, byte[]> root = Tlv8Codec
                .decode(new SrtpStreamConfiguration(SESSION_ID, SessionCommand.END, AUDIO).encode());

        byte[] controlBytes = root.get(0x01);
        assertNotNull(controlBytes);
        Map<Integer, byte[]> control = Tlv8Codec.decode(controlBytes);
        assertArrayEquals(new byte[] { 0 }, control.get(0x02));
    }

    @Test
    void testReconfigureEncodesCommandTwo() {
        Map<Integer, byte[]> root = Tlv8Codec
                .decode(new SrtpStreamConfiguration(SESSION_ID, SessionCommand.RECONFIGURE, AUDIO).encode());

        byte[] controlBytes = root.get(0x01);
        assertNotNull(controlBytes);
        Map<Integer, byte[]> control = Tlv8Codec.decode(controlBytes);
        assertArrayEquals(new byte[] { 2 }, control.get(0x02));
    }

    @Test
    void testRejectsSessionIdOfWrongLength() {
        assertThrows(IllegalArgumentException.class,
                () -> new SrtpStreamConfiguration(new byte[15], SessionCommand.START, AUDIO));
    }
}

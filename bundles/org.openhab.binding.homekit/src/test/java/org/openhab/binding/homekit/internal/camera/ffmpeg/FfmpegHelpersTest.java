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
package org.openhab.binding.homekit.internal.camera.ffmpeg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.camera.codec.AudioCodec;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;

/**
 * Tests for {@link FfmpegHelpers}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class FfmpegHelpersTest {

    @Test
    void testRewriteSequenceClosesHoles() {
        byte[] packet = new byte[] { (byte) 0x80, 110, 0x00, 0x0A, 0, 0, 0, 0, 0, 0, 0, 0 };
        int next = FfmpegRtpTranscoder.rewriteSequence(packet, packet.length, -1);
        assertEquals(0x0A, packet[3] & 0xFF);
        assertEquals(0x0B, next);

        packet[2] = 0x01;
        packet[3] = 0x00;
        next = FfmpegRtpTranscoder.rewriteSequence(packet, packet.length, next);
        assertEquals(0x00, packet[2] & 0xFF);
        assertEquals(0x0B, packet[3] & 0xFF);
        assertEquals(0x0C, next);
    }

    @Test
    void testRewriteSequenceWraps() {
        byte[] packet = new byte[] { (byte) 0x80, 110, 0x12, 0x34, 0, 0, 0, 0, 0, 0, 0, 0 };
        int next = FfmpegRtpTranscoder.rewriteSequence(packet, packet.length, 0xFFFF);
        assertEquals(0xFF, packet[2] & 0xFF);
        assertEquals(0xFF, packet[3] & 0xFF);
        assertEquals(0, next);
    }

    @Test
    void testAudioSpecificConfigForAacEld() {
        assertEquals("F8F03000", FfmpegHelpers.audioSpecificConfig(16, 1));
        assertEquals("F8EC3000", FfmpegHelpers.audioSpecificConfig(24, 1));
        assertEquals("F8F05000", FfmpegHelpers.audioSpecificConfig(16, 2));
    }

    @Test
    void testAudioSpecificConfigRejectsUnsupportedInput() {
        assertThrows(IllegalArgumentException.class, () -> FfmpegHelpers.audioSpecificConfig(44, 1));
        assertThrows(IllegalArgumentException.class, () -> FfmpegHelpers.audioSpecificConfig(16, 0));
    }

    @Test
    void testInputSdp() {
        AudioCodec codec = new AudioCodec(AudioCodecType.AAC_ELD, 1, 16, 30);
        String sdp = FfmpegHelpers.aacEldInputSdp(5004, codec, 110);
        assertTrue(sdp.contains("m=audio 5004 RTP/AVP 110\n"));
        assertTrue(sdp.contains("a=rtpmap:110 mpeg4-generic/16000/1\n"));
        assertTrue(sdp.contains("config=F8F03000;"));
        assertTrue(sdp.contains("constantDuration=480"));
    }

    @Test
    void testOpusCommandLine() {
        List<String> cmd = FfmpegHelpers.transcodeCommandLine("/usr/bin/ffmpeg", "/tmp/in.sdp", 6000, List.of(),
                FfmpegHelpers.opusEncoderArgs(), 110);
        assertEquals("/usr/bin/ffmpeg", cmd.get(0));
        assertEquals("/tmp/in.sdp", cmd.get(cmd.indexOf("-i") + 1));
        assertEquals("libopus", cmd.get(cmd.indexOf("-c:a") + 1));
        assertEquals("110", cmd.get(cmd.indexOf("-payload_type") + 1));
        assertEquals("rtp://127.0.0.1:6000", cmd.get(cmd.size() - 1));
    }

    @Test
    void testAacEldEncoderArgs() {
        List<String> args = FfmpegHelpers.aacEldEncoderArgs(new AudioCodec(AudioCodecType.AAC_ELD, 1, 16, 30));
        assertEquals("libfdk_aac", args.get(args.indexOf("-c:a") + 1));
        assertEquals("aac_eld", args.get(args.indexOf("-profile:a") + 1));
        assertEquals("16000", args.get(args.indexOf("-ar") + 1));
        assertEquals("1", args.get(args.indexOf("-ac") + 1));
        assertEquals("480", args.get(args.indexOf("-frame_length") + 1));
        assertEquals("30000", args.get(args.indexOf("-max_delay") + 1));
        assertEquals("+global_header", args.get(args.indexOf("-flags") + 1));
    }

    @Test
    void testAacEldEncoderArgsAt24kHz() {
        List<String> args = FfmpegHelpers.aacEldEncoderArgs(new AudioCodec(AudioCodecType.AAC_ELD, 1, 24, 20));
        assertEquals("24000", args.get(args.indexOf("-ar") + 1));
        assertEquals("480", args.get(args.indexOf("-frame_length") + 1));
        assertEquals("20000", args.get(args.indexOf("-max_delay") + 1));
    }

    @Test
    void testAacEldInputSdpAt24kHz() {
        String sdp = FfmpegHelpers.aacEldInputSdp(5004, new AudioCodec(AudioCodecType.AAC_ELD, 1, 24, 20), 110);
        assertTrue(sdp.contains("constantDuration=480"));
    }
}

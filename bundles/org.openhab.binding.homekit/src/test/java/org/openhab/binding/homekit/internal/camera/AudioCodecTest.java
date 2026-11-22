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

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.camera.codec.AudioCodec;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;

/**
 * Tests for {@link AudioCodec} decoding of SupportedAudioStreamConfiguration.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class AudioCodecTest {

    @Test
    void testDecodesEveryDeclaredCodecAndRate() {
        byte[] tlv = tlv(entry(AudioCodecType.OPUS, 1, 0x00, 0x01, 0x02), entry(AudioCodecType.PCMU, 1, 0x00));

        List<AudioCodec> codecs = AudioCodec.decode(tlv);

        assertEquals(
                List.of(new AudioCodec(AudioCodecType.OPUS, 1, 8, 20), new AudioCodec(AudioCodecType.OPUS, 1, 16, 20),
                        new AudioCodec(AudioCodecType.OPUS, 1, 24, 20), new AudioCodec(AudioCodecType.PCMU, 1, 8, 20)),
                codecs);
    }

    @Test
    void testSkipsCodecsThatCannotBeBridged() {
        byte[] tlv = tlv(entry(AudioCodecType.AAC_ELD, 1, 0x00, 0x02), entry(AudioCodecType.MSBC, 1, 0x00),
                entry(AudioCodecType.PCMA, 1, 0x01));

        assertEquals(List.of(new AudioCodec(AudioCodecType.AAC_ELD, 1, 24, 20)), AudioCodec.decode(tlv));
    }

    @Test
    void testRejectsRatesThatCodecDoesNotAllow() {
        byte[] tlv = tlv(entry(AudioCodecType.AMR_WB, 1, 0x00, 0x01), entry(AudioCodecType.PCMU, 1, 0x01));

        assertEquals(List.of(new AudioCodec(AudioCodecType.AMR_WB, 1, 16, 20)), AudioCodec.decode(tlv));
    }

    @Test
    void testReadsRepeatedCodecEntriesInSequence() {
        byte[] tlv = tlv(entry(AudioCodecType.AMR, 1, 0x00), entry(AudioCodecType.AMR, 1, 0x00));

        assertEquals(List.of(new AudioCodec(AudioCodecType.AMR, 1, 8, 20)), AudioCodec.decode(tlv));
    }

    @Test
    void testIgnoresUnknownCodecType() {
        byte[] tlv = tlv(entry(0x7F, 1, 0x00), entry(AudioCodecType.PCMU, 1, 0x00));

        assertEquals(List.of(new AudioCodec(AudioCodecType.PCMU, 1, 8, 20)), AudioCodec.decode(tlv));
    }

    @Test
    void testRtpClockRateAndBitrate() {
        assertEquals(16000, new AudioCodec(AudioCodecType.AMR_WB, 1, 16, 20).rtpClockRate());
        assertEquals(48000, new AudioCodec(AudioCodecType.OPUS, 1, 16, 20).rtpClockRate());
        assertEquals(24, new AudioCodec(AudioCodecType.OPUS, 1, 24, 20).maxBitrateKbps());
    }

    @Test
    void testDecodesAdvertisedRtpTime() {
        byte[] tlv = tlv(entryWithRtpTime(AudioCodecType.OPUS, 40, 0x02));

        assertEquals(List.of(new AudioCodec(AudioCodecType.OPUS, 1, 24, 40)), AudioCodec.decode(tlv));
    }

    @Test
    void testKeepsAdvertisedRtpTimeAllowedForRate() {
        byte[] tlv = tlv(entryWithRtpTime(AudioCodecType.AAC_ELD, 60, 0x01));

        assertEquals(List.of(new AudioCodec(AudioCodecType.AAC_ELD, 1, 16, 60)), AudioCodec.decode(tlv));
    }

    @Test
    void testSkipsRtpTimeThatHomekitDoesNotAllow() {
        byte[] tlv = tlv(entryWithRtpTime(AudioCodecType.AAC_ELD, 20, 0x01));

        assertEquals(List.of(), AudioCodec.decode(tlv));
    }

    @Test
    void testUsesSpecDefaultPacketTimeWhenNoRtpTimeAdvertised() {
        byte[] tlv = tlv(entry(AudioCodecType.AAC_ELD, 1, 0x01, 0x02));

        assertEquals(List.of(new AudioCodec(AudioCodecType.AAC_ELD, 1, 16, 30),
                new AudioCodec(AudioCodecType.AAC_ELD, 1, 24, 20)), AudioCodec.decode(tlv));
    }

    @Test
    void testPreferredOpusRequires16Or24kHz() {
        AudioCodec opus8 = new AudioCodec(AudioCodecType.OPUS, 1, 8, 20);
        AudioCodec opus24 = new AudioCodec(AudioCodecType.OPUS, 1, 24, 20);
        assertEquals(opus24, AudioCodec.preferred(List.of(opus8, opus24)));
        assertThrows(IllegalArgumentException.class, () -> AudioCodec.preferred(List.of(opus8)));
    }

    @Test
    void testPreferredPrefersOpusOverAacEld() {
        AudioCodec aac = new AudioCodec(AudioCodecType.AAC_ELD, 1, 16, 30);
        AudioCodec opus = new AudioCodec(AudioCodecType.OPUS, 1, 16, 20);
        assertEquals(opus, AudioCodec.preferred(List.of(aac, opus)));
        assertEquals(aac, AudioCodec.preferred(List.of(aac)));
    }

    @Test
    @Test
    void constantBitrateIsPreservedWhenItIsTheOnlyAdvertisedMode() {
        byte[] tlv = tlv(entry(AudioCodecType.OPUS.value, 1, new byte[] { 0x02, 1, 1 }, 0x01));

        assertEquals(List.of(new AudioCodec(AudioCodecType.OPUS, 1, true, 16, 20)), AudioCodec.decode(tlv));
    }

    @Test
    void variableBitrateIsSelectedWhenBothModesAreAdvertised() {
        byte[] tlv = tlv(entry(AudioCodecType.OPUS.value, 1, new byte[] { 0x02, 1, 0, 0x02, 1, 1 }, 0x01));

        assertEquals(List.of(new AudioCodec(AudioCodecType.OPUS, 1, false, 16, 20)), AudioCodec.decode(tlv));
    }

    @Test
    void testPreferredAacEldRequires16Or24kHz() {
        AudioCodec aac8 = new AudioCodec(AudioCodecType.AAC_ELD, 1, 8, 20);
        AudioCodec aac24 = new AudioCodec(AudioCodecType.AAC_ELD, 1, 24, 20);
        assertEquals(aac24, AudioCodec.preferred(List.of(aac8, aac24)));
        assertThrows(IllegalArgumentException.class, () -> AudioCodec.preferred(List.of(aac8)));
    }

    /** Builds one AUDIO_CODEC_CONFIGURATION entry with the given codec, channel count and sample-rate codes. */
    private static byte[] entry(AudioCodecType codec, int channels, int... sampleRateCodes) {
        return entry(codec.value, channels, sampleRateCodes);
    }

    /** Builds one mono AUDIO_CODEC_CONFIGURATION entry that advertises a single RTP packet time in ms. */
    private static byte[] entryWithRtpTime(AudioCodecType codec, int rtpTimeMs, int... sampleRateCodes) {
        return entry(codec.value, 1, new byte[] { 0x04, 1, (byte) rtpTimeMs }, sampleRateCodes);
    }

    private static byte[] entry(int codecType, int channels, int... sampleRateCodes) {
        return entry(codecType, channels, new byte[0], sampleRateCodes);
    }

    private static byte[] entry(int codecType, int channels, byte[] extraParameters, int... sampleRateCodes) {
        ByteArrayOutputStream rates = new ByteArrayOutputStream();
        for (int code : sampleRateCodes) {
            rates.write(0x03);
            rates.write(1);
            rates.write(code);
        }
        byte[] parameters = concat(concat(new byte[] { 0x01, 1, (byte) channels }, extraParameters),
                rates.toByteArray());
        byte[] body = concat(new byte[] { 0x01, 1, (byte) codecType },
                concat(new byte[] { 0x02 }, concat(new byte[] { (byte) parameters.length }, parameters)));
        return concat(new byte[] { 0x01, (byte) body.length }, body);
    }

    private static byte[] tlv(byte[]... entries) {
        byte[] result = new byte[0];
        for (byte[] entry : entries) {
            result = concat(result, entry);
        }
        return result;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}

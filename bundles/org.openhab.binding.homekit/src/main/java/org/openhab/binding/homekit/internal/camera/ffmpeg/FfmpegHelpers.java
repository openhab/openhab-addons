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

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.camera.codec.AudioCodec;

/**
 * Helpers for building the ffmpeg input SDP and command line that transcode a camera's AAC-ELD audio to Opus.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class FfmpegHelpers {

    private static final int AUDIO_OBJECT_TYPE_ESCAPE = 0x1F;
    private static final int AUDIO_OBJECT_TYPE_AAC_ELD = 39;
    private static final int ELD_FRAME_LENGTH_480 = 1;
    private static final int ELD_REMAINING_FLAG_BITS = 8;

    private FfmpegHelpers() {
    }

    /**
     * Returns an SDP description of a browser Opus RTP stream (48 kHz, stereo RTP clock), received on the given
     * loopback UDP port.
     *
     * @param port the loopback UDP port on which ffmpeg receives the RTP
     * @param payloadType the RTP payload type of the Opus packets
     * @return the SDP text
     */
    public static String opusInputSdp(int port, int payloadType) {
        return """
                v=0
                o=- 0 0 IN IP4 127.0.0.1
                s=HomeKit Opus
                c=IN IP4 127.0.0.1
                t=0 0
                m=audio %d RTP/AVP %d
                a=rtpmap:%d opus/48000/2
                """.formatted(port, payloadType, payloadType);
    }

    /**
     * Returns an SDP description of the camera's AAC-ELD RTP stream, received on the given loopback UDP port.
     *
     * @param port the loopback UDP port on which ffmpeg receives the RTP
     * @param codec the AAC-ELD codec negotiated with the camera
     * @param payloadType the RTP payload type the camera uses
     * @return the SDP text
     */
    public static String aacEldInputSdp(int port, AudioCodec codec, int payloadType) {
        return """
                v=0
                o=- 0 0 IN IP4 127.0.0.1
                s=HomeKit AAC-ELD
                c=IN IP4 127.0.0.1
                t=0 0
                m=audio %d RTP/AVP %d
                a=rtpmap:%d mpeg4-generic/%d/%d
                a=fmtp:%d streamtype=5;profile-level-id=39;mode=AAC-hbr;config=%s;sizelength=13;indexlength=3;indexdeltalength=3;constantDuration=%d
                """
                .formatted(port, payloadType, payloadType, codec.sampleRateKhz() * 1000, codec.channels(), payloadType,
                        audioSpecificConfig(codec.sampleRateKhz(), codec.channels()), aacEldSamplesPerPacket(codec));
    }

    /**
     * Returns the number of samples in one AAC-ELD access unit, which is the packet time of the codec in ms multiplied
     * by its sample rate in kHz. This gives 480 samples for both 16 kHz at 30 ms and 24 kHz at 20 ms.
     */
    private static int aacEldSamplesPerPacket(AudioCodec codec) {
        return codec.sampleRateKhz() * codec.packetTimeMs();
    }

    /**
     * Returns the hex encoded AudioSpecificConfig for AAC-ELD with a 480 sample frame, as used in the SDP config
     * parameter (ISO/IEC 14496-3, ELDSpecificConfig without extensions).
     *
     * @param sampleRateKhz the sample rate, either 16 or 24
     * @param channels the channel count, 1 to 15
     * @return the upper case hex string
     * @throws IllegalArgumentException if the sample rate or channel count is unsupported
     */
    static String audioSpecificConfig(int sampleRateKhz, int channels) {
        int samplingFrequencyIndex = switch (sampleRateKhz) {
            case 16 -> 0x8;
            case 24 -> 0x6;
            default -> throw new IllegalArgumentException("Unsupported AAC-ELD sample rate: " + sampleRateKhz);
        };
        if (channels < 1 || channels > 15) {
            throw new IllegalArgumentException("Unsupported AAC-ELD channel count: " + channels);
        }
        StringBuilder bits = new StringBuilder();
        appendBits(bits, AUDIO_OBJECT_TYPE_ESCAPE, 5);
        appendBits(bits, AUDIO_OBJECT_TYPE_AAC_ELD - 32, 6);
        appendBits(bits, samplingFrequencyIndex, 4);
        appendBits(bits, channels, 4);
        appendBits(bits, ELD_FRAME_LENGTH_480, 1);
        appendBits(bits, 0, ELD_REMAINING_FLAG_BITS);
        while (bits.length() % 8 != 0) {
            bits.append('0');
        }
        byte[] bytes = new byte[bits.length() / 8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(bits.substring(i * 8, i * 8 + 8), 2);
        }
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

    private static void appendBits(StringBuilder bits, int value, int count) {
        for (int shift = count - 1; shift >= 0; shift--) {
            bits.append((value >> shift) & 1);
        }
    }

    /**
     * Returns the ffmpeg arguments that encode to Opus.
     *
     * @return the encoder arguments
     */
    public static List<String> opusEncoderArgs() {
        return List.of("-c:a", "libopus", "-ar", "48000", "-ac", "2", "-b:a", "48k");
    }

    /**
     * Returns the ffmpeg arguments that decode AAC-ELD input. Requires an ffmpeg build with libfdk_aac, because
     * ffmpeg's
     * native AAC decoder cannot decode AAC-ELD.
     *
     * @return the decoder arguments
     */
    public static List<String> aacEldDecoderArgs() {
        return List.of("-c:a", "libfdk_aac");
    }

    /**
     * Returns the ffmpeg arguments that encode to AAC-ELD at the camera's sample rate, channel count and bitrate.
     * Requires an ffmpeg build with libfdk_aac, because ffmpeg's native AAC encoder cannot produce AAC-ELD. The
     * global header flag is required: without it libfdk_aac fails to open the ELD encoder on Windows builds.
     * <p>
     * The frame length is the camera's RTP packet time. {@code -max_delay} is in microseconds and is set to the same
     * packet time, so that the RTP muxer flushes one access unit per packet.
     *
     * @param codec the camera's AAC-ELD audio codec
     * @return the encoder arguments
     */
    public static List<String> aacEldEncoderArgs(AudioCodec codec) {
        return List.of("-c:a", "libfdk_aac", "-profile:a", "aac_eld", "-ar",
                String.valueOf(codec.sampleRateKhz() * 1000), "-ac", String.valueOf(codec.channels()), "-frame_length",
                String.valueOf(aacEldSamplesPerPacket(codec)), "-b:a", codec.maxBitrateKbps() + "k", "-flags",
                "+global_header", "-max_delay", String.valueOf(codec.packetTimeMs() * 1000));
    }

    /**
     * Builds the command line for an ffmpeg instance that reads RTP described by an SDP file, encodes it with the given
     * arguments and sends the result as RTP to a loopback UDP port.
     *
     * @param ffmpeg the ffmpeg executable, see {@link FfmpegInstaller}
     * @param inputSdpFile path to the SDP file describing the input RTP
     * @param outputPort the loopback UDP port that receives the output RTP
     * @param inputArgs the decoder arguments applied to the input, see {@link #aacEldDecoderArgs}
     * @param encoderArgs the encoder arguments, see {@link #opusEncoderArgs} and {@link #aacEldEncoderArgs}
     * @param payloadType the RTP payload type of the output
     * @return the command
     */
    public static List<String> transcodeCommandLine(String ffmpeg, String inputSdpFile, int outputPort,
            List<String> inputArgs, List<String> encoderArgs, int payloadType) {
        List<String> command = new ArrayList<>(
                List.of(ffmpeg, "-hide_banner", "-loglevel", "warning", "-protocol_whitelist", "file,rtp,udp"));
        command.addAll(inputArgs);
        command.addAll(List.of("-i", inputSdpFile, "-vn"));
        command.addAll(encoderArgs);
        command.addAll(
                List.of("-payload_type", String.valueOf(payloadType), "-f", "rtp", "rtp://127.0.0.1:" + outputPort));
        return command;
    }
}

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
package org.openhab.binding.homekit.internal.camera.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An audio codec configuration that a HomeKit camera declares it can send and receive,
 * as found in its SupportedAudioStreamConfiguration characteristic.
 *
 * @param constantBitrate true when the camera advertises only constant bit rate. Variable bit rate is used when the
 *            camera advertises it, or advertises no mode
 * @param packetTimeMs the RTP packet time in ms, which the camera advertises and which HomeKit Table 9-20 allows
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record AudioCodec(AudioCodecType codecType, int channels, boolean constantBitrate, int sampleRateKhz,
        int packetTimeMs) {

    /**
     * A codec that uses variable bit rate. Used when the camera does not advertise a bit-rate mode.
     */
    public AudioCodec(AudioCodecType codecType, int channels, int sampleRateKhz, int packetTimeMs) {
        this(codecType, channels, false, sampleRateKhz, packetTimeMs);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioCodec.class);

    private static final int TYPE_AUDIO_CODEC_CONFIGURATION = 0x01;
    private static final int TYPE_CODEC_TYPE = 0x01;
    private static final int TYPE_CODEC_PARAMETERS = 0x02;
    private static final int TYPE_CHANNEL = 0x01;
    private static final int TYPE_BIT_RATE = 0x02;
    private static final int TYPE_SAMPLE_RATE = 0x03;
    private static final int TYPE_RTP_TIME = 0x04;
    private static final int BIT_RATE_VARIABLE = 0;
    private static final int BIT_RATE_CONSTANT = 1;

    /**
     * Returns the audio codec configurations declared by a TLV8 value.
     */
    public static List<AudioCodec> decode(byte[] tlv) {
        List<AudioCodec> result = new ArrayList<>();
        // The codec entries share one tag, so they are read in sequence rather than through Tlv8Codec.decode(),
        // which would merge them.
        int offset = 0;
        while (offset + 2 <= tlv.length) {
            int type = tlv[offset] & 0xFF;
            int start = offset + 2;
            int end = start + (tlv[offset + 1] & 0xFF);
            if (end > tlv.length) {
                return List.of();
            }
            if (type == TYPE_AUDIO_CODEC_CONFIGURATION) {
                try {
                    decodeEntry(Arrays.copyOfRange(tlv, start, end), result);
                } catch (SecurityException e) {
                    return List.of();
                }
            }
            offset = end;
        }
        return result;
    }

    /**
     * Returns the RTP clock rate in Hz. Opus always uses 48 kHz RTP timestamps regardless of its sample rate (RFC
     * 7587).
     */
    public int rtpClockRate() {
        return codecType == AudioCodecType.OPUS ? 48000 : sampleRateKhz * 1000;
    }

    /**
     * Returns the maximum bit rate in kbps that the RTP parameters of a selected stream may request.
     */
    public int maxBitrateKbps() {
        return switch (codecType) {
            case OPUS -> 24;
            case PCMU, PCMA -> 64;
            case AMR -> 13;
            case AMR_WB -> 24;
            case AAC_ELD -> 32;
            case MSBC -> throw new IllegalStateException("Unsupported codec: " + codecType);
        };
    }

    private static void decodeEntry(byte[] entry, List<AudioCodec> result) {
        Map<Integer, byte[]> fields = Tlv8Codec.decode(entry);
        byte[] typeValue = fields.get(TYPE_CODEC_TYPE);
        byte[] parameters = fields.get(TYPE_CODEC_PARAMETERS);
        if (typeValue == null || typeValue.length != 1 || parameters == null) {
            return;
        }
        AudioCodecType codecType = codecTypeOf(typeValue[0] & 0xFF);
        if (codecType == null) {
            LOGGER.debug("Ignoring camera audio codec with unknown type {}", typeValue[0] & 0xFF);
            return;
        }
        int[] allowedRates = allowedSampleRatesKhz(codecType);
        if (allowedRates.length == 0) {
            LOGGER.debug("Ignoring camera audio codec {} that the binding cannot bridge", codecType);
            return;
        }
        Map<Integer, byte[]> codecParameters = Tlv8Codec.decode(parameters);
        byte[] channelValue = codecParameters.get(TYPE_CHANNEL);
        int channels = channelValue == null || channelValue.length == 0 ? 1 : channelValue[0] & 0xFF;
        byte[] rateCodes = codecParameters.get(TYPE_SAMPLE_RATE);
        if (rateCodes == null) {
            return;
        }
        byte[] rtpTimes = codecParameters.get(TYPE_RTP_TIME);
        for (byte rateCode : rateCodes) {
            int sampleRateKhz = sampleRateKhz(rateCode & 0xFF);
            if (Arrays.stream(allowedRates).noneMatch(rate -> rate == sampleRateKhz)) {
                continue;
            }
            int[] packetTimes = packetTimesMs(codecType, sampleRateKhz, rtpTimes);
            if (packetTimes.length == 0) {
                LOGGER.debug("Ignoring camera audio codec {} at {} kHz with no RTP time HomeKit allows", codecType,
                        sampleRateKhz);
            }
            boolean constantBitrate = constantBitrate(codecParameters.get(TYPE_BIT_RATE));
            for (int packetTimeMs : packetTimes) {
                AudioCodec candidate = new AudioCodec(codecType, channels, constantBitrate, sampleRateKhz,
                        packetTimeMs);
                if (!result.contains(candidate)) {
                    result.add(candidate);
                }
            }
        }
    }

    /**
     * Returns true only when the camera advertises constant bit rate and not variable bit rate. A camera that
     * advertises both accepts the variable mode already used for session setup.
     */
    private static boolean constantBitrate(byte @Nullable [] modes) {
        if (modes == null) {
            return false;
        }
        boolean variable = false;
        boolean constant = false;
        for (byte mode : modes) {
            switch (mode & 0xFF) {
                case BIT_RATE_VARIABLE -> variable = true;
                case BIT_RATE_CONSTANT -> constant = true;
            }
        }
        return constant && !variable;
    }

    /**
     * Returns the RTP packet times in ms that the camera advertises for a codec and sample rate, restricted to those
     * that HomeKit Table 9-20 allows. If the camera advertises none, the first value allowed by the table is used.
     */
    private static int[] packetTimesMs(AudioCodecType codecType, int sampleRateKhz, byte @Nullable [] rtpTimes) {
        int[] allowed = allowedPacketTimesMs(codecType, sampleRateKhz);
        if (rtpTimes == null || rtpTimes.length == 0) {
            return new int[] { allowed[0] };
        }
        List<Integer> result = new ArrayList<>();
        for (byte rtpTime : rtpTimes) {
            int packetTimeMs = rtpTime & 0xFF;
            if (Arrays.stream(allowed).anyMatch(ms -> ms == packetTimeMs) && !result.contains(packetTimeMs)) {
                result.add(packetTimeMs);
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Returns the RTP packet times in ms that HomeKit Table 9-20 allows for a codec and sample rate. AAC-ELD at 16 kHz
     * must use 30 or 60 ms, and the other codecs allow 20, 40 or 60 ms.
     */
    private static int[] allowedPacketTimesMs(AudioCodecType codecType, int sampleRateKhz) {
        if (codecType == AudioCodecType.AAC_ELD && sampleRateKhz == 16) {
            return new int[] { 30, 60 };
        }
        return new int[] { 20, 40, 60 };
    }

    private static @Nullable AudioCodecType codecTypeOf(int value) {
        for (AudioCodecType codecType : AudioCodecType.values()) {
            if (codecType.value == value) {
                return codecType;
            }
        }
        return null;
    }

    private static int[] allowedSampleRatesKhz(AudioCodecType codecType) {
        return switch (codecType) {
            case OPUS -> new int[] { 8, 16, 24 };
            case PCMU, PCMA, AMR -> new int[] { 8 };
            case AMR_WB -> new int[] { 16 };
            case AAC_ELD -> new int[] { 16, 24 };
            case MSBC -> new int[0];
        };
    }

    private static int sampleRateKhz(int code) {
        return switch (code) {
            case 0x00 -> 8;
            case 0x01 -> 16;
            case 0x02 -> 24;
            default -> -1;
        };
    }

    /**
     * Returns the audio codec used for the camera stream. Opus is preferred because it supports talk-back. AAC-ELD is
     * used when the camera offers no Opus, and must then be transcoded to Opus for go2rtc.
     *
     * @param codecs the audio codecs the camera supports
     * @return the Opus codec, or else the AAC-ELD codec, at 16 or 24 kHz
     * @throws IllegalArgumentException if the camera supports neither Opus nor AAC-ELD at 16 or 24 kHz
     */
    public static AudioCodec preferred(List<AudioCodec> codecs) {
        return codecs.stream().filter(codec -> AudioCodecType.OPUS == codec.codecType() && isSupportedRate(codec))
                .findFirst()
                .or(() -> codecs.stream()
                        .filter(codec -> AudioCodecType.AAC_ELD == codec.codecType() && isSupportedRate(codec))
                        .findFirst())
                .orElseThrow(
                        () -> new IllegalArgumentException("camera does not support Opus or AAC-ELD at 16 or 24 kHz"));
    }

    private static boolean isSupportedRate(AudioCodec codec) {
        return codec.sampleRateKhz() == 16 || codec.sampleRateKhz() == 24;
    }
}

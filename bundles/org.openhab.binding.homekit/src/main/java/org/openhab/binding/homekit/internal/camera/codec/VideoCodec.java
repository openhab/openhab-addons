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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.VideoCodecType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An H.264 configuration that a HomeKit camera declares it can send, as found in its SupportedVideoStreamConfiguration
 * characteristic. Each instance is one selectable combination of profile, level, image size and frame rate.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record VideoCodec(VideoCodecType codecType, int profile, int level, int width, int height, int frameRate) {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoCodec.class);

    private static final int TYPE_VIDEO_CODEC_CONFIGURATION = 0x01;
    private static final int TYPE_CODEC_TYPE = 0x01;
    private static final int TYPE_CODEC_PARAMETERS = 0x02;
    private static final int TYPE_VIDEO_ATTRIBUTES = 0x03;
    private static final int TYPE_PROFILE_ID = 0x01;
    private static final int TYPE_LEVEL = 0x02;
    private static final int TYPE_IMAGE_WIDTH = 0x01;
    private static final int TYPE_IMAGE_HEIGHT = 0x02;
    private static final int TYPE_FRAME_RATE = 0x03;

    // the H.264 Baseline profile, which every browser's WebRTC decoder accepts
    private static final int PROFILE_BASELINE = 0x00;

    private static final int VIDEO_MAX_BITRATE_KBPS = 2000;

    /**
     * Returns the H.264 configurations declared by a SupportedVideoStreamConfiguration value. Every combination of
     * profile, level and image attributes within an H.264 entry is returned, in the camera's order.
     *
     * @param tlv the decoded TLV8 bytes of the characteristic
     * @return the configurations, or an empty list when the camera does not advertise H.264
     */
    public static List<VideoCodec> decode(byte[] tlv) {
        List<VideoCodec> result = new ArrayList<>();
        try {
            for (byte[] entry : entries(tlv, TYPE_VIDEO_CODEC_CONFIGURATION)) {
                decodeEntry(entry, result);
            }
        } catch (SecurityException e) {
            return List.of();
        }
        return result;
    }

    private static void decodeEntry(byte[] entry, List<VideoCodec> result) {
        Map<Integer, byte[]> fields = Tlv8Codec.decode(entry);
        byte[] typeValue = fields.get(TYPE_CODEC_TYPE);
        byte[] parameters = fields.get(TYPE_CODEC_PARAMETERS);
        if (typeValue == null || typeValue.length != 1 || parameters == null) {
            return;
        }
        VideoCodecType codecType = codecTypeOf(typeValue[0] & 0xFF);
        if (codecType == null) {
            LOGGER.debug("Ignoring camera audio codec with unknown type {}", typeValue[0] & 0xFF);
            return;
        }
        Map<Integer, byte[]> codecParameters = Tlv8Codec.decode(parameters);
        byte[] profiles = codecParameters.get(TYPE_PROFILE_ID);
        byte[] levels = codecParameters.get(TYPE_LEVEL);
        if (profiles == null || profiles.length == 0 || levels == null || levels.length == 0) {
            return;
        }
        for (byte[] attributeTlv : entries(entry, TYPE_VIDEO_ATTRIBUTES)) {
            Map<Integer, byte[]> attribute = Tlv8Codec.decode(attributeTlv);
            byte[] widthBytes = attribute.get(TYPE_IMAGE_WIDTH);
            byte[] heightBytes = attribute.get(TYPE_IMAGE_HEIGHT);
            byte[] frameRateBytes = attribute.get(TYPE_FRAME_RATE);
            if (widthBytes == null || widthBytes.length != 2 || heightBytes == null || heightBytes.length != 2
                    || frameRateBytes == null || frameRateBytes.length != 1) {
                continue;
            }
            int width = ByteBuffer.wrap(widthBytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
            int height = ByteBuffer.wrap(heightBytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
            int frameRate = frameRateBytes[0] & 0xFF;
            for (byte profile : profiles) {
                for (byte level : levels) {
                    VideoCodec candidate = new VideoCodec(codecType, profile & 0xFF, level & 0xFF, width, height,
                            frameRate);
                    if (!result.contains(candidate)) {
                        result.add(candidate);
                    }
                }
            }
        }
    }

    private static @Nullable VideoCodecType codecTypeOf(int value) {
        for (VideoCodecType codecType : VideoCodecType.values()) {
            if (codecType.value == value) {
                return codecType;
            }
        }
        return null;
    }

    /**
     * Returns the value of each occurrence of a tag. Repeated tags must be read in sequence, because
     * {@link Tlv8Codec#decode(byte[])} would merge them.
     */
    private static List<byte[]> entries(byte[] tlv, int tag) {
        List<byte[]> result = new ArrayList<>();
        int offset = 0;
        while (offset + 2 <= tlv.length) {
            int type = tlv[offset] & 0xFF;
            int start = offset + 2;
            int end = Math.min(start + (tlv[offset + 1] & 0xFF), tlv.length);
            if (type == tag) {
                result.add(Arrays.copyOfRange(tlv, start, end));
            }
            offset = end;
        }
        return result;
    }

    /**
     * Returns the H.264 video configuration. Among Baseline configurations, the highest level is chosen. Other
     * H.264 profiles are used when the camera offers no Baseline configuration.
     *
     * @param codecs the video codecs the camera supports
     * @return the H.264 configuration
     * @throws IllegalArgumentException if the camera does not support H.264
     */
    public static VideoCodec preferred(List<VideoCodec> codecs) {
        return codecs.stream().filter(codec -> VideoCodecType.H_264 == codec.codecType())
                .max(Comparator.comparing((VideoCodec codec) -> VideoCodec.PROFILE_BASELINE == codec.profile())
                        .thenComparingInt(VideoCodec::level))
                .orElseThrow(() -> new IllegalArgumentException("camera does not support H.264 video"));
    }

    public int maxBitrateKbps() {
        return VIDEO_MAX_BITRATE_KBPS;
    }
}

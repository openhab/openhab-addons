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
package org.openhab.binding.homekit.internal.camera.tlv;

import static org.openhab.binding.homekit.internal.camera.tlv.TlvHelpers.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;

/**
 * Video half of a Selected RTP Stream Configuration, describing the H.264 codec and RTP parameters the controller
 * selected for the video stream of a camera session. The enclosing session control and audio parameters are not
 * included.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record VideoSelectedParameters(int profile, int level, int width, int height, int frameRate, int payloadType,
        long ssrc, int maxBitrateKbps, float minRtcpIntervalSec) {

    /** H.264 is the only video codec defined by HAP; its codec type value is zero. */
    static final int CODEC_TYPE_H264 = 0x00;
    static final int PACKETIZATION_MODE_NON_INTERLEAVED = 0x00;

    private static final int TYPE_CODEC = 0x01;
    private static final int TYPE_CODEC_PARAMETERS = 0x02;
    private static final int TYPE_ATTRIBUTES = 0x03;
    private static final int TYPE_RTP_PARAMETERS = 0x04;

    private static final int TYPE_PROFILE_ID = 0x01;
    private static final int TYPE_LEVEL = 0x02;
    private static final int TYPE_PACKETIZATION_MODE = 0x03;

    private static final int TYPE_IMAGE_WIDTH = 0x01;
    private static final int TYPE_IMAGE_HEIGHT = 0x02;
    private static final int TYPE_FRAME_RATE = 0x03;

    private static final int TYPE_PAYLOAD_TYPE = 0x01;
    private static final int TYPE_SSRC = 0x02;
    private static final int TYPE_MAX_BIT_RATE = 0x03;
    private static final int TYPE_MIN_RTCP_INTERVAL = 0x04;

    public VideoSelectedParameters {
        if (profile < 0 || profile > 0xFF || level < 0 || level > 0xFF) {
            throw new IllegalArgumentException("H.264 profile or level out of range");
        }
        if (width < 1 || width > 0xFFFF || height < 1 || height > 0xFFFF) {
            throw new IllegalArgumentException("Image size out of range: " + width + "x" + height);
        }
        if (frameRate < 1 || frameRate > 0xFF) {
            throw new IllegalArgumentException("Frame rate out of range: " + frameRate);
        }
        if (payloadType < 0 || payloadType > 0x7F) {
            throw new IllegalArgumentException("RTP payload type out of range: " + payloadType);
        }
        if (ssrc < 0 || ssrc > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("SSRC out of range: " + ssrc);
        }
        if (maxBitrateKbps < 0 || maxBitrateKbps > 0xFFFF) {
            throw new IllegalArgumentException("Max bit rate out of range: " + maxBitrateKbps);
        }
    }

    /**
     * Encodes the parameters into a TLV8 byte array to send in a camera setup request.
     */
    public byte[] encode() {
        Map<Integer, byte[]> codecParameters = new LinkedHashMap<>();
        codecParameters.put(TYPE_PROFILE_ID, u8(profile));
        codecParameters.put(TYPE_LEVEL, u8(level));
        codecParameters.put(TYPE_PACKETIZATION_MODE, u8(PACKETIZATION_MODE_NON_INTERLEAVED));

        Map<Integer, byte[]> attributes = new LinkedHashMap<>();
        attributes.put(TYPE_IMAGE_WIDTH, u16Le(width));
        attributes.put(TYPE_IMAGE_HEIGHT, u16Le(height));
        attributes.put(TYPE_FRAME_RATE, u8(frameRate));

        Map<Integer, byte[]> rtpParameters = new LinkedHashMap<>();
        rtpParameters.put(TYPE_PAYLOAD_TYPE, u8(payloadType));
        rtpParameters.put(TYPE_SSRC, u32Le(ssrc));
        rtpParameters.put(TYPE_MAX_BIT_RATE, u16Le(maxBitrateKbps));
        rtpParameters.put(TYPE_MIN_RTCP_INTERVAL, f32Le(minRtcpIntervalSec));

        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TYPE_CODEC, u8(CODEC_TYPE_H264));
        tlv.put(TYPE_CODEC_PARAMETERS, Tlv8Codec.encode(codecParameters));
        tlv.put(TYPE_ATTRIBUTES, Tlv8Codec.encode(attributes));
        tlv.put(TYPE_RTP_PARAMETERS, Tlv8Codec.encode(rtpParameters));
        return Tlv8Codec.encode(tlv);
    }
}

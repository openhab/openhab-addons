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
import org.openhab.binding.homekit.internal.enums.AudioCodecType;

/**
 * Audio half of a Selected RTP Stream Configuration, describing the codec and RTP parameters the controller selected
 * for the audio stream of a camera session. The enclosing session control and video parameters are not included.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record AudioSelectedParameters(AudioCodecType codecType, int channels, boolean constantBitrate,
        int sampleRateKhz, int packetTimeMs, int payloadType, long syncSource, int maxBitrateKbps,
        float minRtcpIntervalSec, boolean comfortNoise, int comfortNoisePayloadType) {

    private static final int TYPE_CODEC = 0x01;
    private static final int TYPE_CODEC_PARAMETERS = 0x02;
    private static final int TYPE_RTP_PARAMETERS = 0x03;
    private static final int TYPE_COMFORT_NOISE = 0x04;

    private static final int TYPE_CHANNEL = 0x01;
    private static final int TYPE_BIT_RATE = 0x02;
    private static final int TYPE_SAMPLE_RATE = 0x03;
    private static final int TYPE_PACKET_TIME = 0x04;

    private static final int TYPE_PAYLOAD_TYPE = 0x01;
    private static final int TYPE_SYNC_SOURCE = 0x02;
    private static final int TYPE_MAX_BIT_RATE = 0x03;
    private static final int TYPE_MIN_RTCP_INTERVAL = 0x04;
    private static final int TYPE_COMFORT_NOISE_PAYLOAD_TYPE = 0x06;

    public AudioSelectedParameters {
        if (channels < 1 || channels > 0xFF) {
            throw new IllegalArgumentException("Channels out of range: " + channels);
        }
        if (sampleRateCode(sampleRateKhz) < 0) {
            throw new IllegalArgumentException("Unsupported sample rate: " + sampleRateKhz);
        }
        if (packetTimeMs < 0 || packetTimeMs > 0xFF) {
            throw new IllegalArgumentException("Packet time out of range: " + packetTimeMs);
        }
        if (payloadType < 0 || payloadType > 0x7F || comfortNoisePayloadType < 0 || comfortNoisePayloadType > 0x7F) {
            throw new IllegalArgumentException("RTP payload type out of range");
        }
        if (syncSource < 0 || syncSource > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("SSRC out of range: " + syncSource);
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
        codecParameters.put(TYPE_CHANNEL, u8(channels));
        codecParameters.put(TYPE_BIT_RATE, u8(constantBitrate ? 1 : 0));
        codecParameters.put(TYPE_SAMPLE_RATE, u8(sampleRateCode(sampleRateKhz)));
        codecParameters.put(TYPE_PACKET_TIME, u8(packetTimeMs));

        Map<Integer, byte[]> rtpParameters = new LinkedHashMap<>();
        rtpParameters.put(TYPE_PAYLOAD_TYPE, u8(payloadType));
        rtpParameters.put(TYPE_SYNC_SOURCE, u32Le(syncSource));
        rtpParameters.put(TYPE_MAX_BIT_RATE, u16Le(maxBitrateKbps));
        rtpParameters.put(TYPE_MIN_RTCP_INTERVAL, f32Le(minRtcpIntervalSec));
        rtpParameters.put(TYPE_COMFORT_NOISE_PAYLOAD_TYPE, u8(comfortNoisePayloadType));

        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TYPE_CODEC, u8(codecType.value));
        tlv.put(TYPE_CODEC_PARAMETERS, Tlv8Codec.encode(codecParameters));
        tlv.put(TYPE_RTP_PARAMETERS, Tlv8Codec.encode(rtpParameters));
        tlv.put(TYPE_COMFORT_NOISE, u8(comfortNoise ? 1 : 0));
        return Tlv8Codec.encode(tlv);
    }

    private static int sampleRateCode(int khz) {
        return switch (khz) {
            case 8 -> 0x00;
            case 16 -> 0x01;
            case 24 -> 0x02;
            default -> -1;
        };
    }
}

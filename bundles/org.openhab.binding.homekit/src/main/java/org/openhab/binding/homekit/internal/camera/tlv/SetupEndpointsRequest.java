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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.camera.session.SrtpParameters;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;

/**
 * SetupEndpoints write request sent by the controller to set up the SRTP endpoints of a camera session.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record SetupEndpointsRequest(byte[] sessionId, boolean ipv6, String controllerAddress, int videoPort,
        int audioPort, SrtpParameters videoParameters, SrtpParameters audioParameters) {

    private static final int TYPE_SESSION_ID = 0x01;
    private static final int TYPE_CONTROLLER_ADDRESS = 0x03;
    private static final int TYPE_VIDEO_SRTP = 0x04;
    private static final int TYPE_AUDIO_SRTP = 0x05;

    private static final int TYPE_ADDRESS_VERSION = 0x01;
    private static final int TYPE_ADDRESS = 0x02;
    private static final int TYPE_VIDEO_RTP_PORT = 0x03;
    private static final int TYPE_AUDIO_RTP_PORT = 0x04;

    public SetupEndpointsRequest {
        requireLength(sessionId, 16);
        if (videoPort < 0 || videoPort > 0xFFFF || audioPort < 0 || audioPort > 0xFFFF) {
            throw new IllegalArgumentException("RTP port out of range");
        }
    }

    /**
     * Encodes the configuration into a TLV8 byte array to send in a camera setup request.
     */
    public byte[] encode() {
        Map<Integer, byte[]> address = new LinkedHashMap<>();
        address.put(TYPE_ADDRESS_VERSION, u8(ipv6 ? 1 : 0));
        address.put(TYPE_ADDRESS, controllerAddress.getBytes(StandardCharsets.UTF_8));
        address.put(TYPE_VIDEO_RTP_PORT, u16Le(videoPort));
        address.put(TYPE_AUDIO_RTP_PORT, u16Le(audioPort));

        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TYPE_SESSION_ID, sessionId);
        tlv.put(TYPE_CONTROLLER_ADDRESS, Tlv8Codec.encode(address));
        tlv.put(TYPE_VIDEO_SRTP, videoParameters.encode());
        tlv.put(TYPE_AUDIO_SRTP, audioParameters.encode());
        return Tlv8Codec.encode(tlv);
    }
}

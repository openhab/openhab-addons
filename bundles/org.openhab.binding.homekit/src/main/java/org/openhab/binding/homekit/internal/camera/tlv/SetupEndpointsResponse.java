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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.camera.session.SrtpParameters;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.SetupEndpointsStatus;

/**
 * Parsed SetupEndpoints response returned by the accessory. The audio SyncSource and audio SRTP parameters are only
 * present on success. The audio SRTP parameters are the key the accessory uses to encrypt its audio towards the
 * controller. The audio port is the accessory's SRTP port for receiving audio from the controller (talk-back). The
 * video SyncSource and SRTP parameters and port are optional and are only present if the accessory accepted a video
 * stream.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record SetupEndpointsResponse(byte[] sessionId, SetupEndpointsStatus status, @Nullable Long audioSyncSource,
        @Nullable SrtpParameters audioSrtpParameters, @Nullable Integer talkPort, @Nullable Long videoSynchSource,
        @Nullable SrtpParameters videoSrtpParameters, @Nullable Integer videoPort) {

    private static final int TYPE_SESSION_ID = 0x01;
    private static final int TYPE_STATUS = 0x02;
    private static final int TYPE_ACCESSORY_ADDRESS = 0x03;
    private static final int TYPE_VIDEO_SRTP_PARAMS = 0x04;
    private static final int TYPE_AUDIO_SRTP_PARAMS = 0x05;
    private static final int TYPE_VIDEO_SYNC_SOURCE = 0x06;
    private static final int TYPE_AUDIO_SYNC_SOURCE = 0x07;
    private static final int TYPE_ADDRESS_VIDEO_PORT = 0x03;
    private static final int TYPE_ADDRESS_AUDIO_PORT = 0x04;

    public SetupEndpointsResponse {
        requireLength(sessionId, 16);
        if (status == SetupEndpointsStatus.SUCCESS && (audioSyncSource == null || audioSrtpParameters == null)) {
            throw new IllegalArgumentException("Audio SSRC or SRTP missing from successful SetupEndpoints response");
        }
    }

    /**
     * Decodes the camera setup response TLV8 data into the respective parameters.
     */
    public static SetupEndpointsResponse decode(byte[] data) {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(data);
        byte[] sessionId = required(tlv, TYPE_SESSION_ID);
        SetupEndpointsStatus status = SetupEndpointsStatus.fromValue(readU8(required(tlv, TYPE_STATUS)));
        byte[] audioSyncSource = tlv.get(TYPE_AUDIO_SYNC_SOURCE);
        byte[] audioSrtpParams = tlv.get(TYPE_AUDIO_SRTP_PARAMS);
        byte[] videoSyncSource = tlv.get(TYPE_VIDEO_SYNC_SOURCE);
        byte[] videoSrtpParams = tlv.get(TYPE_VIDEO_SRTP_PARAMS);
        byte[] address = tlv.get(TYPE_ACCESSORY_ADDRESS);
        Integer audioPort = null;
        Integer videoPort = null;
        if (address != null) {
            Map<Integer, byte[]> addressTlv = Tlv8Codec.decode(address);
            audioPort = readPort(addressTlv.get(TYPE_ADDRESS_AUDIO_PORT));
            videoPort = readPort(addressTlv.get(TYPE_ADDRESS_VIDEO_PORT));
        }
        return new SetupEndpointsResponse(sessionId, status,
                audioSyncSource == null ? null : readU32Le(audioSyncSource),
                audioSrtpParams == null ? null : SrtpParameters.decode(audioSrtpParams), audioPort,
                videoSyncSource == null ? null : readU32Le(videoSyncSource),
                videoSrtpParams == null ? null : SrtpParameters.decode(videoSrtpParams), videoPort);
    }

    private static @Nullable Integer readPort(byte @Nullable [] port) {
        if (port == null || port.length != 2) {
            return null;
        }
        return (port[0] & 0xFF) | ((port[1] & 0xFF) << 8);
    }
}

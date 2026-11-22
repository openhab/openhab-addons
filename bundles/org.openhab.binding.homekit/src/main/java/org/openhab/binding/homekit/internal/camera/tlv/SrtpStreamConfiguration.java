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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;

/**
 * The TLV8 payload of the HAP Selected RTP Stream Configuration characteristic, which starts or ends a session.
 * The video parameters are optional; when absent, only the audio stream is selected.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record SrtpStreamConfiguration(byte[] sessionId, SessionCommand command,
        @Nullable VideoSelectedParameters videoParameters, AudioSelectedParameters audioParameters) {

    public enum SessionCommand {
        END(0),
        START(1),
        RECONFIGURE(2);

        private final int code;

        SessionCommand(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private static final int TYPE_SESSION_CONTROL = 0x01;
    private static final int TYPE_VIDEO = 0x02;
    private static final int TYPE_AUDIO = 0x03;

    private static final int TYPE_SESSION_IDENTIFIER = 0x01;
    private static final int TYPE_SESSION_COMMAND = 0x02;

    public SrtpStreamConfiguration(byte[] sessionId, SessionCommand command, AudioSelectedParameters audioParameters) {
        this(sessionId, command, null, audioParameters);
    }

    public SrtpStreamConfiguration {
        requireLength(sessionId, 16);
    }

    /**
     * Encodes the configuration into a TLV8 byte array to send in a camera setup request.
     */
    public byte[] encode() {
        Map<Integer, byte[]> control = new LinkedHashMap<>();
        control.put(TYPE_SESSION_IDENTIFIER, sessionId);
        control.put(TYPE_SESSION_COMMAND, u8(command.getCode()));
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TYPE_SESSION_CONTROL, Tlv8Codec.encode(control));
        VideoSelectedParameters videoParams = videoParameters;
        if (videoParams != null) {
            tlv.put(TYPE_VIDEO, videoParams.encode());
        }
        tlv.put(TYPE_AUDIO, audioParameters.encode());
        return Tlv8Codec.encode(tlv);
    }
}

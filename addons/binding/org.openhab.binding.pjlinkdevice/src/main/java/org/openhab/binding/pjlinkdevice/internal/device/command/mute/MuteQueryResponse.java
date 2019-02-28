/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.device.command.mute;

import java.util.Arrays;
import java.util.HashSet;

import org.openhab.binding.pjlinkdevice.internal.device.command.ErrorCode;
import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class MuteQueryResponse extends PrefixedResponse {

    public enum MuteQueryResponseValue {
        OFF("Mute off", "30", false, false),
        VIDEO_MUTE_ON("Video muted", "11", false, true),
        AUDIO_MUTE_ON("Audio muted", "21", true, false),
        AUDIO_AND_VIDEO_MUTE_ON("Audio and video muted", "31", true, true);

        private String text;
        private String code;
        private boolean audioMuted;
        private boolean videoMuted;

        private MuteQueryResponseValue(String text, String code, boolean audioMuted, boolean videoMuted) {
            this.text = text;
            this.code = code;
            this.audioMuted = audioMuted;
            this.videoMuted = videoMuted;
        }

        public String getText() {
            return this.text;
        }

        public static MuteQueryResponseValue parseString(String code) throws ResponseException {
            for (MuteQueryResponseValue result : MuteQueryResponseValue.values()) {
                if (result.code.equals(code)) {
                    return result;
                }
            }

            throw new ResponseException("Cannot understand status: " + code);
        }

        public boolean isAudioMuted() {
            return this.audioMuted;
        }

        public boolean isVideoMuted() {
            return this.videoMuted;
        }
    }

    private MuteQueryResponseValue result = null;

    public MuteQueryResponse() {
        super("AVMT=", new HashSet<ErrorCode>(
                Arrays.asList(new ErrorCode[] { ErrorCode.UNAVAILABLE_TIME, ErrorCode.DEVICE_FAILURE })));
    }

    public MuteQueryResponseValue getResult() {
        return result;
    }

    @Override
    protected void parse0(String responseWithoutPrefix) throws ResponseException {
        this.result = MuteQueryResponseValue.parseString(responseWithoutPrefix);
    }

}

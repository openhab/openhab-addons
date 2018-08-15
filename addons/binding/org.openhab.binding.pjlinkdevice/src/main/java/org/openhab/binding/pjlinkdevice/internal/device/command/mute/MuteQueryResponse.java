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
import java.util.HashMap;
import java.util.HashSet;

import org.openhab.binding.pjlinkdevice.internal.device.command.ErrorCode;
import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class MuteQueryResponse extends PrefixedResponse {

    public enum MuteQueryResponseValue {
        OFF,
        VIDEO_MUTE_ON,
        AUDIO_MUTE_ON,
        AUDIO_AND_VIDEO_MUTE_ON;

        public String getText() {
            final HashMap<MuteQueryResponseValue, String> texts = new HashMap<MuteQueryResponseValue, String>();
            texts.put(OFF, "Mute off");
            texts.put(VIDEO_MUTE_ON, "Video muted");
            texts.put(AUDIO_MUTE_ON, "Audio muted");
            texts.put(AUDIO_AND_VIDEO_MUTE_ON, "Audio and video muted");
            return texts.get(this);
        }

        public static MuteQueryResponseValue parseString(String code) throws ResponseException {
            final HashMap<String, MuteQueryResponseValue> codes = new HashMap<String, MuteQueryResponseValue>();
            codes.put("30", OFF);
            codes.put("11", VIDEO_MUTE_ON);
            codes.put("21", AUDIO_MUTE_ON);
            codes.put("31", AUDIO_AND_VIDEO_MUTE_ON);

            MuteQueryResponseValue result = codes.get(code);
            if (result == null) {
                throw new ResponseException("Cannot understand status: " + code);
            }

            return result;
        }

        public boolean isAudioMuted() {
            return new HashSet<MuteQueryResponseValue>(Arrays.asList(new MuteQueryResponseValue[] {
                    MuteQueryResponseValue.AUDIO_AND_VIDEO_MUTE_ON, MuteQueryResponseValue.AUDIO_MUTE_ON }))
                            .contains(this);
        }

        public boolean isVideoMuted() {
            return new HashSet<MuteQueryResponseValue>(Arrays.asList(new MuteQueryResponseValue[] {
                    MuteQueryResponseValue.AUDIO_AND_VIDEO_MUTE_ON, MuteQueryResponseValue.VIDEO_MUTE_ON }))
                            .contains(this);
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

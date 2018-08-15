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

import java.util.HashMap;

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class MuteInstructionCommand extends AbstractCommand<MuteInstructionRequest, MuteInstructionResponse> {

    public enum MuteInstructionState {
        ON,
        OFF;

        public String getPJLinkRepresentation() {
            final HashMap<MuteInstructionState, String> texts = new HashMap<MuteInstructionState, String>();
            texts.put(ON, "1");
            texts.put(OFF, "0");
            return texts.get(this);
        }
    }

    public enum MuteInstructionChannel {
        VIDEO,
        AUDIO,
        AUDIO_AND_VIDEO;

        public String getPJLinkRepresentation() {
            final HashMap<MuteInstructionChannel, String> texts = new HashMap<MuteInstructionChannel, String>();
            texts.put(VIDEO, "1");
            texts.put(AUDIO, "2");
            texts.put(AUDIO_AND_VIDEO, "3");
            return texts.get(this);
        }
    }

    protected MuteInstructionState targetState;
    protected MuteInstructionChannel targetChannel;

    public MuteInstructionCommand(PJLinkDevice pjLinkDevice, MuteInstructionState targetState,
            MuteInstructionChannel targetChannel) {
        super(pjLinkDevice);
        this.targetState = targetState;
        this.targetChannel = targetChannel;
    }

    @Override
    public MuteInstructionRequest createRequest() {
        return new MuteInstructionRequest(this);
    }

    @Override
    public MuteInstructionResponse parseResponse(String response) throws ResponseException {
        MuteInstructionResponse result = new MuteInstructionResponse();
        result.parse(response);
        return result;
    }
}

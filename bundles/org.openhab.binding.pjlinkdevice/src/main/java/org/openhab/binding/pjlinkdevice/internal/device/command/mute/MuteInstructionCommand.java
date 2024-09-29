/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * This command is used for selecting audio/video mute of the device as described in
 * <a href="https://pjlink.jbmia.or.jp/english/data_cl2/PJLink_5-1.pdf">[PJLinkSpec]</a> 4.5. Mute instruction
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class MuteInstructionCommand extends AbstractCommand<MuteInstructionRequest, MuteInstructionResponse> {

    public enum MuteInstructionState {
        ON("1"),
        OFF("0");

        private String pjLinkRepresentation;

        private MuteInstructionState(String pjLinkRepresentation) {
            this.pjLinkRepresentation = pjLinkRepresentation;
        }

        public String getPJLinkRepresentation() {
            return this.pjLinkRepresentation;
        }
    }

    public enum MuteInstructionChannel {
        VIDEO("1"),
        AUDIO("2"),
        AUDIO_AND_VIDEO("3");

        private String pjLinkRepresentation;

        private MuteInstructionChannel(String pjLinkRepresentation) {
            this.pjLinkRepresentation = pjLinkRepresentation;
        }

        public String getPJLinkRepresentation() {
            return this.pjLinkRepresentation;
        }
    }

    private MuteInstructionState targetState;
    private MuteInstructionChannel targetChannel;

    public MuteInstructionCommand(PJLinkDevice pjLinkDevice, MuteInstructionState targetState,
            MuteInstructionChannel targetChannel) {
        super(pjLinkDevice);
        this.targetState = targetState;
        this.targetChannel = targetChannel;
    }

    public MuteInstructionState getTargetState() {
        return this.targetState;
    }

    public MuteInstructionChannel getTargetChannel() {
        return this.targetChannel;
    }

    @Override
    public MuteInstructionRequest createRequest() {
        return new MuteInstructionRequest(this);
    }

    @Override
    public MuteInstructionResponse parseResponse(String response) throws ResponseException {
        return new MuteInstructionResponse(response);
    }
}

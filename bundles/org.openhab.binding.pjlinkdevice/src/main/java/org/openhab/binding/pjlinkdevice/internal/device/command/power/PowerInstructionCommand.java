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
package org.openhab.binding.pjlinkdevice.internal.device.command.power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * This command is used for switching the device on/off as described in
 * <a href="https://pjlink.jbmia.or.jp/english/data_cl2/PJLink_5-1.pdf">[PJLinkSpec]</a> 4.1. Power control instruction
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PowerInstructionCommand extends AbstractCommand<PowerInstructionRequest, PowerInstructionResponse> {

    public enum PowerInstructionState {
        ON("1"),
        OFF("0");

        private String pjLinkRepresentation;

        private PowerInstructionState(String pjLinkRepresentation) {
            this.pjLinkRepresentation = pjLinkRepresentation;
        }

        public String getPJLinkRepresentation() {
            return this.pjLinkRepresentation;
        }
    }

    private PowerInstructionState target;

    public PowerInstructionCommand(PJLinkDevice pjLinkDevice, PowerInstructionState target) {
        super(pjLinkDevice);
        this.target = target;
    }

    public PowerInstructionState getTarget() {
        return this.target;
    }

    @Override
    public PowerInstructionRequest createRequest() {
        return new PowerInstructionRequest(this);
    }

    @Override
    public PowerInstructionResponse parseResponse(String response) throws ResponseException {
        return new PowerInstructionResponse(response);
    }
}

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
package org.openhab.binding.pjlinkdevice.internal.device.command.input;

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class InputInstructionCommand extends AbstractCommand<InputInstructionRequest, InputInstructionResponse> {

    protected Input target;

    public InputInstructionCommand(PJLinkDevice pjLinkDevice, Input target) {
        super(pjLinkDevice);
        this.target = target;
    }

    @Override
    public InputInstructionRequest createRequest() {
        return new InputInstructionRequest(this);
    }

    @Override
    public InputInstructionResponse parseResponse(String response) throws ResponseException {
        InputInstructionResponse result = new InputInstructionResponse();
        result.parse(response);
        return result;
    }
}

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

import java.util.Arrays;
import java.util.HashSet;

import org.openhab.binding.pjlinkdevice.internal.device.command.AcknowledgeResponseValue;
import org.openhab.binding.pjlinkdevice.internal.device.command.ErrorCode;
import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class InputInstructionResponse extends PrefixedResponse {

    private AcknowledgeResponseValue result;

    public InputInstructionResponse() {
        super("INPT=", new HashSet<ErrorCode>(Arrays.asList(
                new ErrorCode[] { ErrorCode.OUT_OF_PARAMETER, ErrorCode.UNAVAILABLE_TIME, ErrorCode.DEVICE_FAILURE })));
    }

    @Override
    protected void parse0(String responseWithoutPrefix) throws ResponseException {
        result = AcknowledgeResponseValue.getValueForCode(responseWithoutPrefix);
    }

    public AcknowledgeResponseValue getResult() {
        return result;
    }

}

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
import org.openhab.binding.pjlinkdevice.internal.device.command.AcknowledgeResponseValue;
import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * The response part of {@link PowerInstructionCommand}
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PowerInstructionResponse extends PrefixedResponse<AcknowledgeResponseValue> {
    public PowerInstructionResponse(String response) throws ResponseException {
        super("POWR=", response);
    }

    @Override
    protected AcknowledgeResponseValue parseResponseWithoutPrefix(String responseWithoutPrefix)
            throws ResponseException {
        return AcknowledgeResponseValue.getValueForCode(responseWithoutPrefix);
    }
}

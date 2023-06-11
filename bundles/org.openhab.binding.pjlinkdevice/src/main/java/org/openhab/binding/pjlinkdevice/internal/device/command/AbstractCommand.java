/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;

/**
 * Common base class for most PJLink commands.
 *
 * Takes care of generating the request string, sending it to the device, authentication error checking and response
 * parsing.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractCommand<RequestType extends Request, ResponseType extends Response<?>>
        implements Command<ResponseType> {
    private PJLinkDevice pjLinkDevice;

    public AbstractCommand(PJLinkDevice pjLinkDevice) {
        this.pjLinkDevice = pjLinkDevice;
    }

    public PJLinkDevice getDevice() {
        return this.pjLinkDevice;
    }

    protected abstract RequestType createRequest();

    protected abstract ResponseType parseResponse(String response) throws ResponseException;

    @Override
    public ResponseType execute() throws ResponseException, IOException, AuthenticationException {
        RequestType request = createRequest();
        String responseString = this.pjLinkDevice.execute(request.getRequestString() + "\r");
        if ("PJLINK ERRA".equalsIgnoreCase(responseString)) {
            throw new AuthenticationException("Authentication error, wrong password provided?");
        }
        return parseResponse(responseString);
    }
}

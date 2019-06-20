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
package org.openhab.binding.pjlinkdevice.internal.device.command.authentication;

import java.io.IOException;

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.Command;
import org.openhab.binding.pjlinkdevice.internal.device.command.Response;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class AuthenticationCommand implements Command<Response> {

    protected String challenge;
    protected Command<?> testCommand;
    private PJLinkDevice device;

    public AuthenticationCommand(PJLinkDevice pjLinkDevice, String challenge, Command<?> testCommand) {
        this.device = pjLinkDevice;
        this.challenge = challenge;
        this.testCommand = testCommand;
    }

    @Override
    public Response execute() throws ResponseException, IOException, AuthenticationException {
        this.device.addPrefixToNextCommand(this.createRequest().getRequestString());
        return this.testCommand.execute();
    }

    protected AuthenticationRequest createRequest() {
        return new AuthenticationRequest(this);
    }

    public PJLinkDevice getDevice() {
        return this.device;
    }

}

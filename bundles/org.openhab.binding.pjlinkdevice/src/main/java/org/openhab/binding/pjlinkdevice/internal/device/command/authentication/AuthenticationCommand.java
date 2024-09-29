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
package org.openhab.binding.pjlinkdevice.internal.device.command.authentication;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.Command;
import org.openhab.binding.pjlinkdevice.internal.device.command.Response;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * This command is used to authenticate to the device after the connection established.
 * As authentication can only be done in conjunction with a real command, a testCommand must be passed to authenticate.
 *
 * The authentication procedure is described in
 * <a href="https://pjlink.jbmia.or.jp/english/data_cl2/PJLink_5-1.pdf">[PJLinkSpec]</a> chapter 5.1. Authentication
 * procedure
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class AuthenticationCommand<ResponseType extends Response<?>> implements Command<ResponseType> {

    private String challenge;
    private Command<ResponseType> testCommand;
    private PJLinkDevice device;

    public AuthenticationCommand(PJLinkDevice pjLinkDevice, String challenge, Command<ResponseType> testCommand) {
        this.device = pjLinkDevice;
        this.challenge = challenge;
        this.testCommand = testCommand;
    }

    @Override
    public ResponseType execute() throws ResponseException, IOException, AuthenticationException {
        this.device.addPrefixToNextCommand(createRequest().getRequestString());
        return this.testCommand.execute();
    }

    protected AuthenticationRequest<ResponseType> createRequest() {
        return new AuthenticationRequest<>(this);
    }

    public String getChallenge() {
        return this.challenge;
    }

    public PJLinkDevice getDevice() {
        return this.device;
    }
}

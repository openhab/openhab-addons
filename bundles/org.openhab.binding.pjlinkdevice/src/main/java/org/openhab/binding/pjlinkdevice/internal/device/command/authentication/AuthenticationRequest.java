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
package org.openhab.binding.pjlinkdevice.internal.device.command.authentication;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.Request;
import org.openhab.binding.pjlinkdevice.internal.device.command.Response;

/**
 * Calculates the response matching the callenge received from the PJLink device.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class AuthenticationRequest<ResponseType extends Response<?>> implements Request {

    private AuthenticationCommand<ResponseType> command;

    public AuthenticationRequest(AuthenticationCommand<ResponseType> command) {
        this.command = command;
    }

    @Override
    public String getRequestString() throws AuthenticationException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String toBeDigested = (this.command.getChallenge() + this.command.getDevice().getAdminPassword());
            byte[] digest = md.digest(toBeDigested.getBytes());
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException(e);
        }
    }
}

/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.FreeboxResponse;

/**
 * The {@link LoginResponse} is the Java class used to map the
 * response of the login API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class LoginResponse extends FreeboxResponse<LoginResult> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (!getResult().isLoggedIn() && getResult().getChallenge().isEmpty()) {
            throw new FreeboxException("Login failed", this);
        }
    }
}

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.login;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.TokenStatus;

/**
 * The {@link AuthorizationStatus} holds and handle data needed to be sent to API in order to get authorization
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AuthorizationStatus {
    private TokenStatus status = TokenStatus.UNKNOWN;
    private boolean loggedIn;
    private @Nullable String challenge;
    private @Nullable String passwordSalt;
    private boolean passwordSet;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getChallenge() {
        return Objects.requireNonNull(challenge);
    }

    public TokenStatus getStatus() {
        return status;
    }

    public @Nullable String getPasswordSalt() {
        return passwordSalt;
    }

    public boolean isPasswordSet() {
        return passwordSet;
    }
}

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
package org.openhab.binding.freeboxos.internal.api.login;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Challenge} holds and handle data needed to
 * be sent to API in order to get authorization
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Challenge {
    public static class ChallengeResponse extends Response<Challenge> {
    }

    public static enum Status {
        @SerializedName("unknown")
        UNKNOWN, // the app_token is invalid or has been revoked
        @SerializedName("pending")
        PENDING, // the user has not confirmed the autorization request yet
        @SerializedName("timeout")
        TIMEOUT, // the user did not confirmed the authorization within the given time
        @SerializedName("granted")
        GRANTED, // the app_token is valid and can be used to open a session
        @SerializedName("denied")
        DENIED;// the user denied the authorization request
    }

    private Status status = Status.UNKNOWN;

    private @Nullable String challenge;
    private boolean loggedIn;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public @Nullable String getChallenge() {
        return challenge;
    }

    public Status getStatus() {
        return status;
    }
}

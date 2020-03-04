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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthorizationStatus} is the Java class used to map the
 * structure used by the response of the track authorization progress API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class AuthorizationStatus {
    public static enum Status {
        @SerializedName("unknown")
        UNKNOWN,
        @SerializedName("pending")
        PENDING,
        @SerializedName("timeout")
        TIMEOUT,
        @SerializedName("granted")
        GRANTED,
        @SerializedName("denied")
        DENIED;
    }

    private Status status;
    private String challenge;

    public Status getStatus() {
        return status;
    }

    public String getChallenge() {
        return challenge;
    }
}

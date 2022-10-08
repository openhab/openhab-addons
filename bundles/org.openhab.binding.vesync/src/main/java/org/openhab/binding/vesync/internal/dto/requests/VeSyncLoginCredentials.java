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
package org.openhab.binding.vesync.internal.dto.requests;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncLoginCredentials} is the Java class as a DTO to hold login credentials for the Vesync
 * API.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncLoginCredentials extends VeSyncRequest {

    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String passwordMd5;
    @SerializedName("userType")
    public String userType;
    @SerializedName("devToken")
    public String devToken = "";

    public VeSyncLoginCredentials() {
        super();
        userType = "1";
        method = "login";
    }

    public VeSyncLoginCredentials(String email, String password) {
        this();
        this.email = email;
        this.passwordMd5 = password;
    }
}

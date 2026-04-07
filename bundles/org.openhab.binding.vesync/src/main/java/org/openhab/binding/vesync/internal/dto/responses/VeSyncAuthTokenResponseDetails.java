/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncAuthTokenResponseDetails} class is used as a DTO to hold the Vesync's API's login response
 * for when a token is requested for the given user credentials.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncAuthTokenResponseDetails {
    @SerializedName("accountID")
    public String accountID;

    @SerializedName("bizToken")
    public String bizToken;

    @SerializedName("authorizeCode")
    public String authorizeCode;

    @SerializedName("registerTime")
    public String registerTime;

    @SerializedName("accountLockTimeInSec")
    public Integer accountLockTimeInSec = -1;

    public String toString() {
        return "accountLockTimeInSec=" + accountLockTimeInSec + ", accountId=" + accountID + ", bizToken=" + bizToken
                + ", authorizeCode=" + authorizeCode;
    }
}

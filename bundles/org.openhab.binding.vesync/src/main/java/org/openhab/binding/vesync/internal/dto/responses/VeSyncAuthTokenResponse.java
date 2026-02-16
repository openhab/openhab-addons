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
 * The {@link VeSyncAuthTokenResponse} is a Java class used as a DTO to hold the Vesync's API's login response.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncAuthTokenResponse extends VeSyncResponse {

    @SerializedName("result")
    public VeSyncAuthTokenResponse.LoginTokenResponse result;

    public class LoginTokenResponse {
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
            return "accountLockTimeInSec=" + accountLockTimeInSec + ", accountId=" + accountID + ", bizToken="
                    + bizToken + ", authorizeCode=" + authorizeCode;
        }
    }

    @Override
    public String toString() {
        return "VeSyncAuthLoginTokenResponse [msg=" + getMsg() + ", result=" + result + "]";
    }
}

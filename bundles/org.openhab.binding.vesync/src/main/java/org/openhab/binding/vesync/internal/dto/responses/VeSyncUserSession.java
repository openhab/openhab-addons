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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Contains data about the logged in user - including the accountID and token's used
 * for authenticating other payload's.
 *
 * @see unit test - Result may not be in respone if not authenticated
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncUserSession {

    public String token;

    public String getToken() {
        return token;
    }

    @SerializedName("registerTime")
    public String registerTime;

    @SerializedName("accountID")
    public String accountId;

    public String getAccountId() {
        return accountId;
    }

    @SerializedName("registerAppVersion")
    public String registerAppVersion;

    @SerializedName("countryCode")
    public String countryCode;

    @SerializedName("acceptLanguage")
    public String acceptLanguage;

    @Override
    public String toString() {
        return "Data [user=AB" + ", token=" + token + "]";
    }
}

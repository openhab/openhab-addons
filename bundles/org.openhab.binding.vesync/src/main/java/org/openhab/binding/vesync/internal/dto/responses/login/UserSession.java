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
package org.openhab.binding.vesync.internal.dto.responses.login;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserSession} class contains data about the logged in user - including the accountID and token's used
 * for authenticating other payload's.
 *
 * @author David Goodyear - Initial contribution
 */
public class UserSession {

    public String token;

    public String serverUrl;

    public String getToken() {
        return token;
    }

    @SerializedName("registerTime")
    public String registerTime;

    public String accountId;

    public String getAccountId() {
        return accountId;
    }

    @SerializedName("acceptLanguage")
    public String acceptLanguage;

    @SerializedName("countryCode")
    public String countryCode;

    @Override
    public String toString() {
        return "Data [token=" + token + "]";
    }
}

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
 * The {@link RegionSwitchResp} class is used as a DTO to hold the Vesync's API's
 * response to a {@link org.openhab.binding.vesync.internal.dto.requests.login.RegionSwitchReq} request.
 *
 * @author David Goodyear - Initial contribution
 */
public class RegionSwitchResp {

    @SerializedName("currentRegion")
    public String currentRegion = "";

    @SerializedName("countryCode")
    public String countryCode = "";

    @SerializedName("accountID")
    public String accountID;

    @SerializedName("bizToken")
    public String bizToken;

    @SerializedName("token")
    public String token;

    @SerializedName("acceptLanguage")
    public String acceptLanguage;

    public String toString() {
        return "currentRegion=" + currentRegion + ", countryCode=" + countryCode + ", accountID=" + accountID
                + ", bizToken=" + bizToken + ", acceptLanguage=" + acceptLanguage;
    }
}

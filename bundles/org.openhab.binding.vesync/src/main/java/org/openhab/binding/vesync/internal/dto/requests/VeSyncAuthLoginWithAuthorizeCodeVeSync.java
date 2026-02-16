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
package org.openhab.binding.vesync.internal.dto.requests;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncAuthLoginWithAuthorizeCodeVeSync} is a Java class used as a DTO to hold the login token request
 *
 * @author David Goodyear - Initial contribution for new Auth Handling
 */
public class VeSyncAuthLoginWithAuthorizeCodeVeSync extends VeSyncBaseRequest {

    @SerializedName("authorizeCode")
    public String authorizeCode;

    @SerializedName("accountID")
    public String accountId = "";

    @SerializedName("clientInfo")
    public String clientInfo = "OpenHAB";

    @SerializedName("clientType")
    public String clientType = "vesyncApp";

    @SerializedName("clientVersion")
    public String clientVersion = "VeSync 5.6.60";

    @SerializedName("debugMode")
    public boolean debugMode = false;

    @SerializedName("emailSubscriptions")
    public boolean emailSubscriptions = false;

    @SerializedName("osInfo")
    public String osInfo = "Android";

    @SerializedName("terminalId")
    public String terminalId = "287f129ed1ca25cc0888348c0104744b9";

    @SerializedName("timeZone")
    public String timeZone = "America/New_York";

    @SerializedName("token")
    public String token = "";

    @SerializedName("userCountryCode")
    public String userCountryCode = "US";

    public VeSyncAuthLoginWithAuthorizeCodeVeSync(final String authorizeCode, final String accountId) {
        this.method = "loginByAuthorizeCode4Vesync" + "";
        traceId = String.valueOf(System.currentTimeMillis());
        this.authorizeCode = authorizeCode;
        this.accountId = accountId;
    }
}

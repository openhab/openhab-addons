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
package org.openhab.binding.vesync.internal.dto.requests.login;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthCodeReq} class is used as a DTO to perform a regional switch of where the
 * user is logging in.
 *
 * @author David Goodyear - Initial contribution for new Auth Handling
 */
public class RegionSwitchReq extends AuthCodeReq {

    @SerializedName("regionChange")
    public String regionChange = "lastRegion";

    @SerializedName("bizToken")
    public String bizToken = "";

    public RegionSwitchReq(final String authorizeCode, final String countryCode, final String bizToken) {
        super(authorizeCode, "");
        this.bizToken = bizToken;
        this.userCountryCode = countryCode;
    }
}

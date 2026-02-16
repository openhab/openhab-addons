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
public class VeSyncAuthLoginWithAuthorizeCodeVeSyncRegionChange extends VeSyncAuthLoginWithAuthorizeCodeVeSync {

    @SerializedName("regionChange")
    public String regionChange = "lastRegion";

    @SerializedName("bizToken")
    public String bizToken = "";

    public VeSyncAuthLoginWithAuthorizeCodeVeSyncRegionChange(final String authorizeCode, final String countryCode,
            final String bizToken) {
        super(authorizeCode, "");
        this.bizToken = bizToken;
        this.userCountryCode = countryCode;
    }
}

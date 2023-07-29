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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.main;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class AuthCode {

    @SerializedName("ecovacsUid")
    private final String ecovacsUid;

    @SerializedName("authCode")
    private final String authCode;

    public AuthCode(String ecovacsUid, String authCode) {
        this.ecovacsUid = ecovacsUid;
        this.authCode = authCode;
    }

    public String getEcovacsUid() {
        return ecovacsUid;
    }

    public String getAuthCode() {
        return authCode;
    }
}

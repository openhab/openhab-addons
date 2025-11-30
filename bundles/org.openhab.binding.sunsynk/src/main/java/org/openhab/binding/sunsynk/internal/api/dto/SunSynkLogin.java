/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SunSynkLogin} is the internal class for initial connection
 * to a Sun Synk Connect Account.
 * Login via Username and Password
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkLogin {
    // {"sign": "MD5 sign", "nonce": "unix time", "username":"xxx", "password":"xxx", "grant_type":"password",
    // "client_id":"csp-web"}
    @SerializedName("sign")
    private String signature = "";
    @SerializedName("nonce")
    private String nonce = "";
    @SerializedName("username")
    private String userName = "";
    @SerializedName("password")
    private String passWord = "";
    @SerializedName("grant_type")
    private String grantType = "password";
    @SerializedName("client_id")
    private String clientId = "csp-web";
    @SerializedName("source")
    private String source = "sunsynk";

    public SunSynkLogin(String UserName, String PassWord, String signature, Long nonce) {
        this.userName = UserName;
        this.passWord = PassWord;
        this.signature = signature;
        this.nonce = nonce.toString();
    }
}

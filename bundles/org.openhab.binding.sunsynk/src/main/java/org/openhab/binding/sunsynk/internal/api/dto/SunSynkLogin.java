/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link SunSynkLogin} is the internal class for inital connection
 * to a Sun Synk Connect Account.
 * Login via Username and Password
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkLogin {
    // {"username":"xxx", "password":"xxx", "grant_type":"password", "client_id":"csp-web"}
    @SerializedName("username")
    private String UserName = "";
    @SerializedName("password")
    private String PassWord = "";
    @SerializedName("grant_type")
    private String GrantType = "password";
    @SerializedName("client_id")
    private String ClintId = "csp-web";

    public SunSynkLogin(String UserName, String PassWord) {
        this.UserName = UserName;
        this.PassWord = PassWord;
    }
}

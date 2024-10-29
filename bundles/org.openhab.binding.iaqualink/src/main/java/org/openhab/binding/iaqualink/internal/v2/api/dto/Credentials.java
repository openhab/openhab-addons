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
package org.openhab.binding.iaqualink.internal.v2.api.dto;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * AWS access credentials.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class Credentials {

    @SerializedName("AccessKeyId")
    String AccessKeyId;

    @SerializedName("Expiration")
    Date Expiration;

    @SerializedName("IdentityId")
    String IdentityId;

    @SerializedName("SecretKey")
    String SecretKey;

    @SerializedName("SessionToken")
    String SessionToken;

    public void setAccessKeyId(String AccessKeyId) {
        this.AccessKeyId = AccessKeyId;
    }

    public String getAccessKeyId() {
        return AccessKeyId;
    }

    public void setExpiration(Date Expiration) {
        this.Expiration = Expiration;
    }

    public Date getExpiration() {
        return Expiration;
    }

    public void setIdentityId(String IdentityId) {
        this.IdentityId = IdentityId;
    }

    public String getIdentityId() {
        return IdentityId;
    }

    public void setSecretKey(String SecretKey) {
        this.SecretKey = SecretKey;
    }

    public String getSecretKey() {
        return SecretKey;
    }

    public void setSessionToken(String SessionToken) {
        this.SessionToken = SessionToken;
    }

    public String getSessionToken() {
        return SessionToken;
    }
}

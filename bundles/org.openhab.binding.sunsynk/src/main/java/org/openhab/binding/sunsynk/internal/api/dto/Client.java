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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkAuthenticateException;

/**
 * The {@link Client} is the internal class for client information
 * from a Sun Synk Connect Account.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class Client {
    private int code; // 102 username or password probloem
    private String msg = "";
    private boolean success;
    @Nullable
    private APIdata data = new APIdata();
    private int status;
    private String error = ""; // "{"timestamp":"2024-06-16T11:21:17.690+00:00","status":404,"error":"Not
                               // Found","path":"/oauth/toke"}"
    private String path = "";
    private String timestamp = "";

    public Client() {
    }

    public static String getAccessTokenString() {
        return APIdata.staticAccessToken;
    }

    public int getCode() {
        return this.code;
    }

    public int getStatus() {
        return this.status;
    }

    public String getError() {
        return this.error;
    }

    public String getPath() {
        return this.path;
    }

    public String getTimeStamp() {
        return this.timestamp;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setAccessTokenString(String token) {
        APIdata.staticAccessToken = token;
    }

    public Long getExpiresIn() throws SunSynkAuthenticateException {
        return this.getData().getExpiresIn();
    }

    public String getRefreshTokenString() throws SunSynkAuthenticateException {
        return this.getData().getRefreshToken();
    }

    public Long getIssuedAt() throws SunSynkAuthenticateException {
        return this.getData().getIssuedAt();
    }

    public void setIssuedAt(Long issuedAt) throws SunSynkAuthenticateException {
        this.getData().setIssuedAt(issuedAt);
    }

    public APIdata getData() throws SunSynkAuthenticateException {
        APIdata data = this.data; // Nullable inherited from APIdata
        if (data != null) {
            return data;
        } else {
            throw new SunSynkAuthenticateException("Empty client data.");
        }
    }

    @Override
    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "sucess=" + success + ", data=" + data + "]";
    }
}

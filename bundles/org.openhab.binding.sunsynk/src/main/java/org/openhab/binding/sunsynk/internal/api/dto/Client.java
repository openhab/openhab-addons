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

/**
 * The {@link Client} is the internal class for Client information from the sunsynk Account.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class Client {
    private int code;
    private String msg = "";
    private boolean success;
    private APIdata data = new APIdata();

    public Client() {
    }

    public static String getAccessTokenString() {
        return APIdata.static_access_token;
    }

    public int getCode() {
        return this.code;
    }

    public void setAccessTokenString(String token) {
        APIdata.static_access_token = token;
    }

    public Long getExpiresIn() {
        return data.getExpiresIn();
    }

    public String getRefreshTokenString() {
        return data.getRefreshToken();
    }

    public Long getIssuedAt() {
        return data.getIssuedAt();
    }

    public void setIssuedAt(Long issued_at) {
        data.setIssuedAt(issued_at);
    }

    public APIdata getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "sucess=" + success + ", data=" + data + "]";
    }
}

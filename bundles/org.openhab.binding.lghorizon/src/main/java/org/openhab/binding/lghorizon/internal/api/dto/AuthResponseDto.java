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
package org.openhab.binding.lghorizon.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response body of {@code POST /auth-service/v1/authorization} and {@code POST /auth-service/v1/authorization/refresh}.
 *
 * @author Mark - Initial contribution
 */
public class AuthResponseDto {

    @SerializedName("householdId")
    public String householdId;

    @SerializedName("accessToken")
    public String accessToken;

    @SerializedName("refreshToken")
    public String refreshToken;

    @SerializedName("refreshTokenExpiry")
    public long refreshTokenExpiry;

    @SerializedName("username")
    public String username;

    @SerializedName("error")
    public ErrorDto error;

    public static class ErrorDto {
        @SerializedName("statusCode")
        public int statusCode;

        @SerializedName("message")
        public String message;
    }

    @Override
    public String toString() {
        return "AuthResponseDto [refreshTokenExpiry=" + refreshTokenExpiry + ", error=" + error + "]";
    }
}

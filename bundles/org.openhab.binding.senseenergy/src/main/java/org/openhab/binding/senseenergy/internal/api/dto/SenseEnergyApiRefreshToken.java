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
package org.openhab.binding.senseenergy.internal.api.dto;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * {@link SenseEnergyApiRefreshToken }
 *
 * @author Jeff James - Initial contribution
 *
 */
public class SenseEnergyApiRefreshToken {
    public boolean authorized;
    @SerializedName("account_id")
    public long accountId;
    @SerializedName("user_id")
    public long userId;
    @SerializedName("access_token")
    public String accessToken;
    String user;
    @SerializedName("refresh_token")
    public String refreshToken;
    @SerializedName("totp_enabled")
    public boolean totpEnabled;
    public Instant expires;
}

/* @formatter:off
{
    "authorized": true,
    "account_id": 139132,
    "user_id": 135624,
    "access_token": "t1.v2.eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTZW5zZSBJbmMiLCJleHAiOjE3MjYwMTQwNzYsInVzZXJJZCI6MTM1NjI0LCJhY2NvdW50SWQiOjEzOTEzMiwicm9sZXMiOiJ1c2VyIiwiZGhhc2giOiJhZDkyMSJ9.e6cBHBJl4k4EtG5kLPmxjOltbEn9_ulrdkL8ktU_OlNhdHoQtqG9HsPKivTgZcY-0b9GG_Pl_HeYNIeC5ikFfw",
    "roles": "user",
    "refresh_token": "DB6pZEWbY2F1e4hP+G3z/uE2qt+dcEQxAcwUBNSHG5KDabGp",
    "totp_enabled": false,
    "expires": "2024-09-11T00:21:16.805Z"
  }
 * @formatter:on
 */

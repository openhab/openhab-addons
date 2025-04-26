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

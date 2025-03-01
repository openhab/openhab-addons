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
 * The {@link SenseEnergyApiAuthenticate}
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiAuthenticate {
    public boolean authorized;
    @SerializedName("account_id")
    public long accountID;
    @SerializedName("user_id")
    public long userID;
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("refresh_token")
    public String refreshToken;
    public SenseEnergyApiMonitor[] monitors;
    @SerializedName("bridge_server")
    public String bridgeServer;
    @SerializedName("date_created")
    public Instant dateCreated;
    @SerializedName("totp_enabled")
    public transient boolean totpEnabled;
    @SerializedName("ab_cohort")
    public transient String abCohort;
}

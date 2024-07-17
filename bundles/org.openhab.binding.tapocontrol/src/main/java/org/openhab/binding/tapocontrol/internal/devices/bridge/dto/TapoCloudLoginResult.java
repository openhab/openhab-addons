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
package org.openhab.binding.tapocontrol.internal.devices.bridge.dto;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * TapoCloudLogin Result Class as record
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoCloudLoginResult(@Expose @SerializedName("accountId") String accountId,
        @Expose @SerializedName("regTime") String regTime, @Expose @SerializedName("countryCode") String countryCode,
        @Expose @SerializedName("riskDetected") int riskDetected, @Expose @SerializedName("nickname") String nickname,
        @Expose @SerializedName("email") String email, @Expose @SerializedName("token") String token) {

    /* init new emty record */
    public TapoCloudLoginResult() {
        this("", "", "", 0, "", "", "");
    }

    /**********************************************
     * Return default data if recordobject is null
     **********************************************/
    @Override
    public String accountId() {
        return Objects.requireNonNullElse(accountId, "");
    }

    @Override
    public String token() {
        return Objects.requireNonNullElse(token, "");
    }

    @Override
    public String email() {
        return Objects.requireNonNullElse(email, "");
    }

    @Override
    public String nickname() {
        return Objects.requireNonNullElse(nickname, "");
    }

    @Override
    public int riskDetected() {
        return Objects.requireNonNullElse(riskDetected, 0);
    }

    @Override
    public String countryCode() {
        return Objects.requireNonNullElse(countryCode, "");
    }

    @Override
    public String regTime() {
        return Objects.requireNonNullElse(regTime, "");
    }
}

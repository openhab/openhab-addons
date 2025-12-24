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
package org.openhab.binding.unifiprotect.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Smart detection audio event types.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum AudioType implements ApiValueEnum {
    @SerializedName("alrmSmoke")
    ALRM_SMOKE("alrmSmoke"),
    @SerializedName("alrmCmonx")
    ALRM_CMONX("alrmCmonx"),
    @SerializedName("alrmSiren")
    ALRM_SIREN("alrmSiren"),
    @SerializedName("alrmBabyCry")
    ALRM_BABY_CRY("alrmBabyCry"),
    @SerializedName("alrmSpeak")
    ALRM_SPEAK("alrmSpeak"),
    @SerializedName("alrmBark")
    ALRM_BARK("alrmBark"),
    @SerializedName("alrmBurglar")
    ALRM_BURGLAR("alrmBurglar"),
    @SerializedName("alrmCarHorn")
    ALRM_CAR_HORN("alrmCarHorn"),
    @SerializedName("alrmGlassBreak")
    ALRM_GLASS_BREAK("alrmGlassBreak");

    private final String apiValue;

    AudioType(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}

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
 * Range classification for sensor metrics.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum SensorStatus implements ApiValueEnum {
    @SerializedName("neutral")
    NEUTRAL("neutral"),
    @SerializedName("low")
    LOW("low"),
    @SerializedName("safe")
    SAFE("safe"),
    @SerializedName("high")
    HIGH("high"),
    @SerializedName("unknown")
    UNKNOWN("unknown");

    private final String apiValue;

    SensorStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}

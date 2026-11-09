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
package org.openhab.binding.evohome.internal.api.models.v2.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the heat set point capabilities
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Expose optional command limits
 *
 */
public class HeatSetpointCapabilities {

    @SerializedName("maxHeatSetpoint")
    private Double maxHeatSetpoint;

    @SerializedName("minHeatSetpoint")
    private Double minHeatSetpoint;

    @SerializedName("valueResolution")
    private Double valueResolution;

    @SerializedName("allowedSetpointModes")
    private List<String> allowedSetpointModes;

    @SerializedName("maxDuration")
    private String maxDuration;

    @SerializedName("timingResolution")
    private String timingResolution;

    public @Nullable Double getMaxHeatSetpoint() {
        return maxHeatSetpoint;
    }

    public @Nullable Double getMinHeatSetpoint() {
        return minHeatSetpoint;
    }

    public @Nullable Double getValueResolution() {
        return valueResolution;
    }

    public List<String> getAllowedSetpointModes() {
        return allowedSetpointModes == null ? List.of() : allowedSetpointModes;
    }

    public @Nullable String getMaxDuration() {
        return maxDuration;
    }

    public @Nullable String getTimingResolution() {
        return timingResolution;
    }
}

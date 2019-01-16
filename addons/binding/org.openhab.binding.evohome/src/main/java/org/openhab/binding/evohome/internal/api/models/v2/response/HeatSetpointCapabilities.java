/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the heat set point capabilities
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class HeatSetpointCapabilities {

    @SerializedName("maxHeatSetpoint")
    private double maxHeatSetpoint;

    @SerializedName("minHeatSetpoint")
    private double minHeatSetpoint;

    @SerializedName("valueResolution")
    private double valueResolution;

    @SerializedName("allowedSetpointModes")
    private List<String> allowedSetpointModes;

    @SerializedName("maxDuration")
    private String maxDuration;

    @SerializedName("timingResolution")
    private String timingResolution;

}

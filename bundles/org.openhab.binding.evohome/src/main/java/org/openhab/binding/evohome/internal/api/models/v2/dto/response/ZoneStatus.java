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
package org.openhab.binding.evohome.internal.api.models.v2.dto.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for zone status
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class ZoneStatus {

    @SerializedName("zoneId")
    private String zoneId;

    @SerializedName("name")
    private String name;

    @SerializedName("temperatureStatus")
    private TemperatureStatus temperature;

    @SerializedName("setpointStatus")
    private HeatSetpointStatus heatSetpoint;

    @SerializedName("activeFaults")
    private List<ActiveFault> activeFaults;

    public String getZoneId() {
        return zoneId;
    }

    public TemperatureStatus getTemperature() {
        return temperature;
    }

    public HeatSetpointStatus getHeatSetpoint() {
        return heatSetpoint;
    }

    public boolean hasActiveFaults() {
        return !activeFaults.isEmpty();
    }

    public ActiveFault getActiveFault(int index) {
        return activeFaults.get(index);
    }
}

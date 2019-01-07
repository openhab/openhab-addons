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
        return activeFaults.size() > 0;
    }

    public ActiveFault getActiveFault(int index) {
        return activeFaults.get(index);
    }

}

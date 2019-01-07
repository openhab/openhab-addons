/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the zone
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class Zone {

    @SerializedName("zoneId")
    private String zoneId;

    @SerializedName("modelType")
    private String modelType;

    @SerializedName("name")
    private String name;

    @SerializedName("zoneType")
    private String zoneType;

    @SerializedName("heatSetpointCapabilities")
    private HeatSetpointCapabilities heatSetpointCapabilities;

    @SerializedName("scheduleCapabilities")
    private ScheduleCapabilities scheduleCapabilities;

    public String getZoneId() {
        return zoneId;
    }

    public String getName() {
        return name;
    }

}

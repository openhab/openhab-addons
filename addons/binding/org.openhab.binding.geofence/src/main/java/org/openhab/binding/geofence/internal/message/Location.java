/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.message;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/**
 * Location message POJO based on the OwnTracks location report.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class Location extends AbstractBaseMessage {
    /**
     * Altitude measured above sea level (iOS,Android/integer/meters/optional)
     */
    @SerializedName("alt")
    BigDecimal altitude;

    /**
     * Course over ground (iOS/integer/degree/optional)
     */
    @SerializedName("cog")
    BigDecimal groundCourse;

    /**
     * Radius around the region when entering/leaving (iOS/integer/meters/optional)
     */
    @SerializedName("rad")
    BigDecimal radius;

    /**
     *  Vertical accuracy of the altitude element (iOS/integer/meters/optional)
     */
    @SerializedName("vac")
    BigDecimal verticalAccuracy;

    /**
     * velocity (iOS,Android/integer/kmh/optional)
     */
    @SerializedName("vel")
    BigDecimal velocity;

    /**
     * Contains a list of regions the device is currently in (e.g. ["Home","Garage"]). Might be empty.
     * (iOS,Android/list of strings/optional)
     */
    @SerializedName("inregions")
    String[] regionsInside;

    public String[] getRegionsInside() {
        return regionsInside;
    }
}

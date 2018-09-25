/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.json;

import java.time.ZonedDateTime;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirQualityJsonTime} is responsible for storing
 * the "time" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 * @author Gaël L'hopital - Use ZonedDateTime instead of Calendar
 */
public class AirQualityJsonTime {

    @SerializedName("s")
    private String dateString;

    @SerializedName("tz")
    private String timeZone;

    /**
     * Get observation time
     *
     * @return {ZonedDateTime}
     * @throws Exception
     */
    public ZonedDateTime getObservationTime() throws Exception {
        String fullString = dateString.replace(" ", "T") + timeZone;
        ZonedDateTime observationTime = ZonedDateTime.parse(fullString);
        return observationTime;
    }

}

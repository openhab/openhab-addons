/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.json;

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

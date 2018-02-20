/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.json;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirQualityJsonTime} is responsible for storing
 * the "time" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class AirQualityJsonTime {

    @SerializedName("s")
    private String dateString;

    @SerializedName("tz")
    private String timeZone;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    /**
     * Get Time zone from the JSON Response
     * in following format: "+0100"
     *
     * @return {String}
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Get observation time
     *
     * @return {Calendar}
     * @throws Exception
     */
    public Calendar getDateString() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Date date = SDF.parse(dateString + timeZone.replace(":", ""));
        calendar.setTime(date);
        return calendar;
    }

}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// TODO: Auto-generated Javadoc
/**
 * The Class CurrentTime.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class CurrentTime {

    /** The date time. */
    private final DateTime dateTime;

    /**
     * Instantiates a new current time.
     *
     * @param results the results
     */
    public CurrentTime(ScalarWebResult results) {
        final JsonArray resultArray = results.getResults();

        if (resultArray.size() != 1) {
            throw new JsonParseException("Result should only have a single element: " + resultArray);
        }

        final JsonElement elm = resultArray.get(0);

        if (elm.isJsonPrimitive()) {
            // 2017-02-01T15:07:11-0500
            final String dateString = elm.getAsString();
            final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            dateTime = dtf.parseDateTime(dateString);
        } else if (elm.isJsonObject()) {
            final JsonObject obj = elm.getAsJsonObject();

            String myDateTime = null;
            Integer myTimeZoneOffsetMinute = null;
            Integer myDstOffsetMinute = null;

            final JsonElement dateTimeElm = obj.get("dateTime");
            if (dateTimeElm != null) {
                myDateTime = dateTimeElm.getAsString();
            }

            final JsonElement timeZoneElm = obj.get("timeZoneOffsetMinute");
            if (timeZoneElm != null && timeZoneElm.isJsonPrimitive() && timeZoneElm.getAsJsonPrimitive().isNumber()) {
                myTimeZoneOffsetMinute = timeZoneElm.getAsInt();
            }

            final JsonElement dstElm = obj.get("dstOffsetMinute");
            if (dstElm != null && dstElm.isJsonPrimitive() && dstElm.getAsJsonPrimitive().isNumber()) {
                myDstOffsetMinute = dstElm.getAsInt();
            }

            if (StringUtils.isEmpty(myDateTime)) {
                throw new JsonParseException("'dateTime' property was not found: " + obj);
            }

            if (myTimeZoneOffsetMinute == null) {
                throw new JsonParseException("'timeZoneOffsetMinute' property was not found: " + obj);
            }

            if (myDstOffsetMinute == null) {
                throw new JsonParseException("'dstOffsetMinute' property was not found: " + obj);
            }

            int offsetMintues = myTimeZoneOffsetMinute + myDstOffsetMinute;

            dateTime = DateTime.parse(myDateTime)
                    .withZone(DateTimeZone.forOffsetHoursMinutes(offsetMintues / 60, offsetMintues % 60));
        } else {
            throw new JsonParseException("Unknown result element: " + elm);
        }
    }

    /**
     * Gets the date time.
     *
     * @return the date time
     */
    public DateTime getDateTime() {
        return dateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CurrentTime [dateTime=" + dateTime + "]";
    }
}

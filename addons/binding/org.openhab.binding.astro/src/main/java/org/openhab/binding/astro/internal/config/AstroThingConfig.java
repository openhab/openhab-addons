/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.config;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Thing configuration from openHab.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public class AstroThingConfig {
    private String geolocation;
    private Double latitude;
    private Double longitude;
    private Integer interval;
    private String thingUid;

    /**
     * Splits the geolocation into latitude and longitude.
     */
    public void parseGeoLocation() {
        String[] geoParts = StringUtils.split(geolocation, ",");
        if (geoParts.length == 2) {
            latitude = toDouble(geoParts[0]);
            longitude = toDouble(geoParts[1]);
        }
    }

    private Double toDouble(String value) {
        try {
            return Double.parseDouble(StringUtils.trimToNull(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Returns the geolocation.
     */
    public String getGeolocation() {
        return geolocation;
    }

    /**
     * Returns the latitude.
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude.
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Returns the interval.
     */
    public Integer getInterval() {
        return interval;
    }

    /**
     * Returns the thing uid as string.
     */
    public String getThingUid() {
        return thingUid;
    }

    /**
     * Sets the thing uid as string.
     */
    public void setThingUid(String thingUid) {
        this.thingUid = thingUid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        TimeZone tz = TimeZone.getDefault();
        StringBuilder tzInfo = new StringBuilder();
        tzInfo.append(tz.getID());
        tzInfo.append(" (").append(tz.getDisplayName(false, TimeZone.SHORT)).append(" ")
                .append(new SimpleDateFormat("Z").format(Calendar.getInstance().getTime()));
        tzInfo.append(")");
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("thing", thingUid).append("geolocation", geolocation)
                .append("interval", interval).append("systemTimezone", tzInfo.toString())
                .append("daylightSavings", Calendar.getInstance().get(Calendar.DST_OFFSET) != 0).toString();
    }
}

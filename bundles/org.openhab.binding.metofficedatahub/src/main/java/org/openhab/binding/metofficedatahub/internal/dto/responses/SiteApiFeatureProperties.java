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
package org.openhab.binding.metofficedatahub.internal.dto.responses;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SiteApiFeatureProperties} is a Java class used as a DTO to hold part of the response to the Site Specific
 * API.
 *
 * @author David Goodyear - Initial contribution
 */
public class SiteApiFeatureProperties {

    public class SiteLocation {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }

    @SerializedName("location")
    private SiteLocation location;

    public SiteLocation getLocation() {
        return location;
    }

    @SerializedName("requestPointDistance")
    private double requestPointDistance;

    public double getRequestPointDistance() {
        return requestPointDistance;
    }

    @SerializedName("modelRunDate")
    private String modelRunDate;

    public String getModelRunDate() {
        return modelRunDate;
    }

    @SerializedName("timeSeries")
    private SiteApiTimeSeries[] timeSeries;

    public SiteApiTimeSeries[] getTimeSeries() {
        return timeSeries;
    }

    public SiteApiTimeSeries getTimeSeries(final int position) {
        if (position < 0 || position > timeSeries.length - 1) {
            return EMPTY_TIME_SERIES;
        } else {
            return timeSeries[position];
        }
    }

    public SiteApiTimeSeries getTimeSeries(final String timestamp) {
        return getTimeSeries(getHourlyTimeSeriesPositionForCurrentHour(timestamp));
    }

    public static final SiteApiTimeSeries EMPTY_TIME_SERIES = new SiteApiTimeSeries();

    private HashMap<String, Integer> timeseriesPositions = null;

    public int getHourlyTimeSeriesPositionForCurrentHour(String timestamp) {
        if (timeseriesPositions == null) {
            timeseriesPositions = new HashMap<>();
            // Populate the lookup table
            for (int i = 0; i < timeSeries.length; i++) {
                timeseriesPositions.put(timeSeries[i].getTime(), i);
            }
        }
        Integer result = timeseriesPositions.get(timestamp);
        if (result == null) {
            return -1;
        } else {
            return result.intValue();
        }
    }
}

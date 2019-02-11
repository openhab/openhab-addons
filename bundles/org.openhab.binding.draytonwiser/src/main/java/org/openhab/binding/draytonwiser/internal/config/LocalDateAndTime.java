/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class LocalDateAndTime {

    @SerializedName("Year")
    @Expose
    private Integer year;
    @SerializedName("Month")
    @Expose
    private String month;
    @SerializedName("Date")
    @Expose
    private Integer date;
    @SerializedName("Day")
    @Expose
    private String day;
    @SerializedName("Time")
    @Expose
    private Integer time;

    public Integer getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public Integer getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public Integer getTime() {
        return time;
    }

}

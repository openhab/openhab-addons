/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MonitorDTO} contains information about how the monitor is
 * defined in Zoneminder.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class MonitorDTO {

    /**
     * Monitor Id
     */
    @SerializedName("Id")
    public String id;

    /**
     * Monitor name
     */
    @SerializedName("Name")
    public String name;

    /**
     * Current monitor function (e.g. Nodect, Record, etc.)
     */
    @SerializedName("Function")
    public String function;

    /**
     * Monitor enabled ("1") or disabled ("0")
     */
    @SerializedName("Enabled")
    public String enabled;

    /**
     * Number of events in last hour
     */
    @SerializedName("HourEvents")
    public String hourEvents;

    /**
     * Number of events in last day
     */
    @SerializedName("DayEvents")
    public String dayEvents;

    /**
     * Number of events in last week
     */
    @SerializedName("WeekEvents")
    public String weekEvents;

    /**
     * Number of events in last month
     */
    @SerializedName("MonthEvents")
    public String monthEvents;

    /**
     * Total number of events
     */
    @SerializedName("TotalEvents")
    public String totalEvents;

    /**
     * Video with in pixels
     */
    @SerializedName("Width")
    public String width;

    /**
     * Video height in pixels
     */
    @SerializedName("Height")
    public String height;

    /**
     * Path to video stream
     */
    @SerializedName("path")
    public String videoStreamPath;
}

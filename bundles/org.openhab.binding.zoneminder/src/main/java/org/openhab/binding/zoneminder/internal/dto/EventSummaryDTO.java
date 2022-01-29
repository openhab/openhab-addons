/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link EventSummaryDTO} contains event counts for the monitor. If this object
 * doesn't exist in the JSON response, the event counts will be in the monitor object.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class EventSummaryDTO {

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
}

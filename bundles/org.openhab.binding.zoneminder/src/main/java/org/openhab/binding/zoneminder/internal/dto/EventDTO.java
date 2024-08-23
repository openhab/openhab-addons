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
package org.openhab.binding.zoneminder.internal.dto;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EventDTO} represents a Zoneminder event.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class EventDTO {

    /**
     * Id of the event
     */
    @SerializedName("Id")
    public String eventId;

    /**
     * Monitor Id associated with this event
     */
    @SerializedName("MonitorId")
    public String monitorId;

    /**
     * Name of the event
     */
    @SerializedName("Name")
    public String name;

    /**
     * Cause of the event
     */
    @SerializedName("Cause")
    public String cause;

    /**
     * Date/time when the event started
     */
    @SerializedName("StartTime")
    public Date startTime;

    /**
     * Date/time when the event ended
     */
    @SerializedName("EndTime")
    public Date endTime;

    /**
     * Number of frames in the event
     */
    @SerializedName("Frames")
    public Integer frames;

    /**
     * Number of alarm frames in the event
     */
    @SerializedName("AlarmFrames")
    public Integer alarmFrames;

    /**
     * Length of the event in seconds
     */
    @SerializedName("Length")
    public Double length;

    /**
     * Total score of the event
     */
    @SerializedName("TotScore")
    public String totalScore;

    /**
     * Average score of the event
     */
    @SerializedName("AvgScore")
    public String averageScore;

    /**
     * Maximum score of the event
     */
    @SerializedName("MaxScore")
    public String maximumScore;

    /**
     * Event notes
     */
    @SerializedName("Notes")
    public String notes;
}

/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api.model;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Irrigation} class represents a Cloudrain Irrigation API result object
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class Irrigation extends CloudrainAPIItem {

    private final Logger logger = LoggerFactory.getLogger(Irrigation.class);

    @SerializedName("zoneId")
    private @Nullable String zoneId;

    @SerializedName("remainingSeconds")
    private @Nullable Integer remainingSeconds;

    @SerializedName("duration")
    private @Nullable Integer duration;

    @SerializedName("startTime")
    private @Nullable String startTimeString;

    @SerializedName("plannedEndTime")
    private @Nullable String plannedEndTimeString;

    /**
     * Creates an Irrigation object with initialized attributes. Useful for test implementations. Typically objects of
     * this type will be created through reflection by the GSON library when parsing the JSON response of the API
     *
     * @param zoneId the ID of the zone in which the irrigation is active
     * @param remainingSeconds the remaining seconds of the active irrigation
     * @param duration the total duration in seconds
     * @param startTime the start time of the active irrigation
     * @param plannedEndTime the planned end time of the active irrigation
     * @param controllerId the ID of the controller managing this zone
     * @param controllerName the name of the controller managing this zone
     */
    public Irrigation(String zoneId, Integer remainingSeconds, Integer duration, LocalTime startTime,
            LocalTime plannedEndTime, String controllerId, String controllerName) {
        super(controllerId, controllerName);
        this.zoneId = zoneId;
        this.remainingSeconds = remainingSeconds;
        this.duration = duration;
        this.startTimeString = String.format("%tR", startTime);
        this.plannedEndTimeString = String.format("%tR", plannedEndTime);
    }

    public @Nullable String getZoneId() {
        return zoneId;
    }

    public void setZone(String zoneId) {
        this.zoneId = zoneId;
    }

    public @Nullable Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public @Nullable Integer getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Returns the start time as LocalDate. May return null if the String returned from the API could not be parsed.
     *
     * @return the start time as LocalDate.
     */
    public @Nullable LocalTime getStartTime() {
        return parseLocalTime(startTimeString);
    }

    /**
     * Returns the planed end time as LocalDate. May return null if the String returned from the API could not be
     * parsed.
     *
     * @return the planned end time as LocalDate.
     */
    public @Nullable LocalTime getPlannedEndTime() {
        return parseLocalTime(plannedEndTimeString);
    }

    private @Nullable LocalTime parseLocalTime(@Nullable String timeString) {
        LocalTime result = null;
        try {
            if (timeString != null) {
                result = LocalTime.parse(timeString);
            }
        } catch (DateTimeParseException e) {
            logger.warn("Unable to parse start or end time of Irrigation. Details: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Checks whether the irrigation is currently active or not
     *
     * @return true when the irrigation is currently active. False otherwise.
     */
    public boolean isActive() {
        boolean result = false;
        Integer remaining = remainingSeconds;
        if (remaining != null && remaining.intValue() > 0) {
            result = true;
        }
        return result;
    }
}

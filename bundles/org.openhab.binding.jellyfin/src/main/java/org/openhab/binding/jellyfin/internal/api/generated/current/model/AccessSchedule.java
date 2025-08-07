/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An entity representing a user&#39;s access schedule.
 */
@JsonPropertyOrder({ AccessSchedule.JSON_PROPERTY_ID, AccessSchedule.JSON_PROPERTY_USER_ID,
        AccessSchedule.JSON_PROPERTY_DAY_OF_WEEK, AccessSchedule.JSON_PROPERTY_START_HOUR,
        AccessSchedule.JSON_PROPERTY_END_HOUR })

public class AccessSchedule {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private Integer id;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_DAY_OF_WEEK = "DayOfWeek";
    @org.eclipse.jdt.annotation.NonNull
    private DynamicDayOfWeek dayOfWeek;

    public static final String JSON_PROPERTY_START_HOUR = "StartHour";
    @org.eclipse.jdt.annotation.NonNull
    private Double startHour;

    public static final String JSON_PROPERTY_END_HOUR = "EndHour";
    @org.eclipse.jdt.annotation.NonNull
    private Double endHour;

    public AccessSchedule() {
    }

    @JsonCreator
    public AccessSchedule(@JsonProperty(JSON_PROPERTY_ID) Integer id) {
        this();
        this.id = id;
    }

    /**
     * Gets the id of this instance.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getId() {
        return id;
    }

    public AccessSchedule userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets the id of the associated user.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    public AccessSchedule dayOfWeek(@org.eclipse.jdt.annotation.NonNull DynamicDayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }

    /**
     * Gets or sets the day of week.
     * 
     * @return dayOfWeek
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DAY_OF_WEEK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DynamicDayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @JsonProperty(JSON_PROPERTY_DAY_OF_WEEK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDayOfWeek(@org.eclipse.jdt.annotation.NonNull DynamicDayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public AccessSchedule startHour(@org.eclipse.jdt.annotation.NonNull Double startHour) {
        this.startHour = startHour;
        return this;
    }

    /**
     * Gets or sets the start hour.
     * 
     * @return startHour
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_HOUR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getStartHour() {
        return startHour;
    }

    @JsonProperty(JSON_PROPERTY_START_HOUR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartHour(@org.eclipse.jdt.annotation.NonNull Double startHour) {
        this.startHour = startHour;
    }

    public AccessSchedule endHour(@org.eclipse.jdt.annotation.NonNull Double endHour) {
        this.endHour = endHour;
        return this;
    }

    /**
     * Gets or sets the end hour.
     * 
     * @return endHour
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_END_HOUR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getEndHour() {
        return endHour;
    }

    @JsonProperty(JSON_PROPERTY_END_HOUR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndHour(@org.eclipse.jdt.annotation.NonNull Double endHour) {
        this.endHour = endHour;
    }

    /**
     * Return true if this AccessSchedule object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessSchedule accessSchedule = (AccessSchedule) o;
        return Objects.equals(this.id, accessSchedule.id) && Objects.equals(this.userId, accessSchedule.userId)
                && Objects.equals(this.dayOfWeek, accessSchedule.dayOfWeek)
                && Objects.equals(this.startHour, accessSchedule.startHour)
                && Objects.equals(this.endHour, accessSchedule.endHour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, dayOfWeek, startHour, endHour);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessSchedule {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    dayOfWeek: ").append(toIndentedString(dayOfWeek)).append("\n");
        sb.append("    startHour: ").append(toIndentedString(startHour)).append("\n");
        sb.append("    endHour: ").append(toIndentedString(endHour)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

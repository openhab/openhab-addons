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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

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
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AccessSchedule {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private Integer id;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID userId;

    public static final String JSON_PROPERTY_DAY_OF_WEEK = "DayOfWeek";
    @org.eclipse.jdt.annotation.Nullable
    private DynamicDayOfWeek dayOfWeek;

    public static final String JSON_PROPERTY_START_HOUR = "StartHour";
    @org.eclipse.jdt.annotation.Nullable
    private Double startHour;

    public static final String JSON_PROPERTY_END_HOUR = "EndHour";
    @org.eclipse.jdt.annotation.Nullable
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getId() {
        return id;
    }

    public AccessSchedule userId(@org.eclipse.jdt.annotation.Nullable UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets the id of the associated user.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.Nullable UUID userId) {
        this.userId = userId;
    }

    public AccessSchedule dayOfWeek(@org.eclipse.jdt.annotation.Nullable DynamicDayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }

    /**
     * Gets or sets the day of week.
     * 
     * @return dayOfWeek
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DAY_OF_WEEK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DynamicDayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @JsonProperty(value = JSON_PROPERTY_DAY_OF_WEEK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDayOfWeek(@org.eclipse.jdt.annotation.Nullable DynamicDayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public AccessSchedule startHour(@org.eclipse.jdt.annotation.Nullable Double startHour) {
        this.startHour = startHour;
        return this;
    }

    /**
     * Gets or sets the start hour.
     * 
     * @return startHour
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_START_HOUR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getStartHour() {
        return startHour;
    }

    @JsonProperty(value = JSON_PROPERTY_START_HOUR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartHour(@org.eclipse.jdt.annotation.Nullable Double startHour) {
        this.startHour = startHour;
    }

    public AccessSchedule endHour(@org.eclipse.jdt.annotation.Nullable Double endHour) {
        this.endHour = endHour;
        return this;
    }

    /**
     * Gets or sets the end hour.
     * 
     * @return endHour
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_END_HOUR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getEndHour() {
        return endHour;
    }

    @JsonProperty(value = JSON_PROPERTY_END_HOUR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndHour(@org.eclipse.jdt.annotation.Nullable Double endHour) {
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

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `DayOfWeek` to the URL query string
        if (getDayOfWeek() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDayOfWeek%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDayOfWeek()))));
        }

        // add `StartHour` to the URL query string
        if (getStartHour() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sStartHour%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartHour()))));
        }

        // add `EndHour` to the URL query string
        if (getEndHour() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEndHour%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndHour()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AccessSchedule instance;

        public Builder() {
            this(new AccessSchedule());
        }

        protected Builder(AccessSchedule instance) {
            this.instance = instance;
        }

        public AccessSchedule.Builder id(Integer id) {
            this.instance.id = id;
            return this;
        }

        public AccessSchedule.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public AccessSchedule.Builder dayOfWeek(DynamicDayOfWeek dayOfWeek) {
            this.instance.dayOfWeek = dayOfWeek;
            return this;
        }

        public AccessSchedule.Builder startHour(Double startHour) {
            this.instance.startHour = startHour;
            return this;
        }

        public AccessSchedule.Builder endHour(Double endHour) {
            this.instance.endHour = endHour;
            return this;
        }

        /**
         * returns a built AccessSchedule instance.
         *
         * The builder is not reusable.
         */
        public AccessSchedule build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static AccessSchedule.Builder builder() {
        return new AccessSchedule.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AccessSchedule.Builder toBuilder() {
        return new AccessSchedule.Builder().id(getId()).userId(getUserId()).dayOfWeek(getDayOfWeek())
                .startHour(getStartHour()).endHour(getEndHour());
    }
}

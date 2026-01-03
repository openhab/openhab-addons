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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class TaskTriggerInfo.
 */
@JsonPropertyOrder({ TaskTriggerInfo.JSON_PROPERTY_TYPE, TaskTriggerInfo.JSON_PROPERTY_TIME_OF_DAY_TICKS,
        TaskTriggerInfo.JSON_PROPERTY_INTERVAL_TICKS, TaskTriggerInfo.JSON_PROPERTY_DAY_OF_WEEK,
        TaskTriggerInfo.JSON_PROPERTY_MAX_RUNTIME_TICKS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TaskTriggerInfo {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private TaskTriggerInfoType type;

    public static final String JSON_PROPERTY_TIME_OF_DAY_TICKS = "TimeOfDayTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long timeOfDayTicks;

    public static final String JSON_PROPERTY_INTERVAL_TICKS = "IntervalTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long intervalTicks;

    public static final String JSON_PROPERTY_DAY_OF_WEEK = "DayOfWeek";
    @org.eclipse.jdt.annotation.Nullable
    private DayOfWeek dayOfWeek;

    public static final String JSON_PROPERTY_MAX_RUNTIME_TICKS = "MaxRuntimeTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long maxRuntimeTicks;

    public TaskTriggerInfo() {
    }

    public TaskTriggerInfo type(@org.eclipse.jdt.annotation.Nullable TaskTriggerInfoType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TaskTriggerInfoType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable TaskTriggerInfoType type) {
        this.type = type;
    }

    public TaskTriggerInfo timeOfDayTicks(@org.eclipse.jdt.annotation.Nullable Long timeOfDayTicks) {
        this.timeOfDayTicks = timeOfDayTicks;
        return this;
    }

    /**
     * Gets or sets the time of day.
     * 
     * @return timeOfDayTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TIME_OF_DAY_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getTimeOfDayTicks() {
        return timeOfDayTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_TIME_OF_DAY_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeOfDayTicks(@org.eclipse.jdt.annotation.Nullable Long timeOfDayTicks) {
        this.timeOfDayTicks = timeOfDayTicks;
    }

    public TaskTriggerInfo intervalTicks(@org.eclipse.jdt.annotation.Nullable Long intervalTicks) {
        this.intervalTicks = intervalTicks;
        return this;
    }

    /**
     * Gets or sets the interval.
     * 
     * @return intervalTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INTERVAL_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getIntervalTicks() {
        return intervalTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_INTERVAL_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIntervalTicks(@org.eclipse.jdt.annotation.Nullable Long intervalTicks) {
        this.intervalTicks = intervalTicks;
    }

    public TaskTriggerInfo dayOfWeek(@org.eclipse.jdt.annotation.Nullable DayOfWeek dayOfWeek) {
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
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @JsonProperty(value = JSON_PROPERTY_DAY_OF_WEEK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDayOfWeek(@org.eclipse.jdt.annotation.Nullable DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public TaskTriggerInfo maxRuntimeTicks(@org.eclipse.jdt.annotation.Nullable Long maxRuntimeTicks) {
        this.maxRuntimeTicks = maxRuntimeTicks;
        return this;
    }

    /**
     * Gets or sets the maximum runtime ticks.
     * 
     * @return maxRuntimeTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MAX_RUNTIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getMaxRuntimeTicks() {
        return maxRuntimeTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_RUNTIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxRuntimeTicks(@org.eclipse.jdt.annotation.Nullable Long maxRuntimeTicks) {
        this.maxRuntimeTicks = maxRuntimeTicks;
    }

    /**
     * Return true if this TaskTriggerInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskTriggerInfo taskTriggerInfo = (TaskTriggerInfo) o;
        return Objects.equals(this.type, taskTriggerInfo.type)
                && Objects.equals(this.timeOfDayTicks, taskTriggerInfo.timeOfDayTicks)
                && Objects.equals(this.intervalTicks, taskTriggerInfo.intervalTicks)
                && Objects.equals(this.dayOfWeek, taskTriggerInfo.dayOfWeek)
                && Objects.equals(this.maxRuntimeTicks, taskTriggerInfo.maxRuntimeTicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, timeOfDayTicks, intervalTicks, dayOfWeek, maxRuntimeTicks);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskTriggerInfo {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    timeOfDayTicks: ").append(toIndentedString(timeOfDayTicks)).append("\n");
        sb.append("    intervalTicks: ").append(toIndentedString(intervalTicks)).append("\n");
        sb.append("    dayOfWeek: ").append(toIndentedString(dayOfWeek)).append("\n");
        sb.append("    maxRuntimeTicks: ").append(toIndentedString(maxRuntimeTicks)).append("\n");
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

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `TimeOfDayTicks` to the URL query string
        if (getTimeOfDayTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTimeOfDayTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTimeOfDayTicks()))));
        }

        // add `IntervalTicks` to the URL query string
        if (getIntervalTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIntervalTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIntervalTicks()))));
        }

        // add `DayOfWeek` to the URL query string
        if (getDayOfWeek() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDayOfWeek%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDayOfWeek()))));
        }

        // add `MaxRuntimeTicks` to the URL query string
        if (getMaxRuntimeTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMaxRuntimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxRuntimeTicks()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TaskTriggerInfo instance;

        public Builder() {
            this(new TaskTriggerInfo());
        }

        protected Builder(TaskTriggerInfo instance) {
            this.instance = instance;
        }

        public TaskTriggerInfo.Builder type(TaskTriggerInfoType type) {
            this.instance.type = type;
            return this;
        }

        public TaskTriggerInfo.Builder timeOfDayTicks(Long timeOfDayTicks) {
            this.instance.timeOfDayTicks = timeOfDayTicks;
            return this;
        }

        public TaskTriggerInfo.Builder intervalTicks(Long intervalTicks) {
            this.instance.intervalTicks = intervalTicks;
            return this;
        }

        public TaskTriggerInfo.Builder dayOfWeek(DayOfWeek dayOfWeek) {
            this.instance.dayOfWeek = dayOfWeek;
            return this;
        }

        public TaskTriggerInfo.Builder maxRuntimeTicks(Long maxRuntimeTicks) {
            this.instance.maxRuntimeTicks = maxRuntimeTicks;
            return this;
        }

        /**
         * returns a built TaskTriggerInfo instance.
         *
         * The builder is not reusable.
         */
        public TaskTriggerInfo build() {
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
    public static TaskTriggerInfo.Builder builder() {
        return new TaskTriggerInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TaskTriggerInfo.Builder toBuilder() {
        return new TaskTriggerInfo.Builder().type(getType()).timeOfDayTicks(getTimeOfDayTicks())
                .intervalTicks(getIntervalTicks()).dayOfWeek(getDayOfWeek()).maxRuntimeTicks(getMaxRuntimeTicks());
    }
}

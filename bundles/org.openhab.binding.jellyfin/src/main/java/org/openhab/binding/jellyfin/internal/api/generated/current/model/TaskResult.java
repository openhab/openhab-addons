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

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class TaskExecutionInfo.
 */
@JsonPropertyOrder({ TaskResult.JSON_PROPERTY_START_TIME_UTC, TaskResult.JSON_PROPERTY_END_TIME_UTC,
        TaskResult.JSON_PROPERTY_STATUS, TaskResult.JSON_PROPERTY_NAME, TaskResult.JSON_PROPERTY_KEY,
        TaskResult.JSON_PROPERTY_ID, TaskResult.JSON_PROPERTY_ERROR_MESSAGE,
        TaskResult.JSON_PROPERTY_LONG_ERROR_MESSAGE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TaskResult {
    public static final String JSON_PROPERTY_START_TIME_UTC = "StartTimeUtc";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime startTimeUtc;

    public static final String JSON_PROPERTY_END_TIME_UTC = "EndTimeUtc";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime endTimeUtc;

    public static final String JSON_PROPERTY_STATUS = "Status";
    @org.eclipse.jdt.annotation.NonNull
    private TaskCompletionStatus status;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.NonNull
    private String key;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_ERROR_MESSAGE = "ErrorMessage";
    @org.eclipse.jdt.annotation.NonNull
    private String errorMessage;

    public static final String JSON_PROPERTY_LONG_ERROR_MESSAGE = "LongErrorMessage";
    @org.eclipse.jdt.annotation.NonNull
    private String longErrorMessage;

    public TaskResult() {
    }

    public TaskResult startTimeUtc(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startTimeUtc) {
        this.startTimeUtc = startTimeUtc;
        return this;
    }

    /**
     * Gets or sets the start time UTC.
     * 
     * @return startTimeUtc
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_START_TIME_UTC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getStartTimeUtc() {
        return startTimeUtc;
    }

    @JsonProperty(value = JSON_PROPERTY_START_TIME_UTC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartTimeUtc(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startTimeUtc) {
        this.startTimeUtc = startTimeUtc;
    }

    public TaskResult endTimeUtc(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endTimeUtc) {
        this.endTimeUtc = endTimeUtc;
        return this;
    }

    /**
     * Gets or sets the end time UTC.
     * 
     * @return endTimeUtc
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_END_TIME_UTC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getEndTimeUtc() {
        return endTimeUtc;
    }

    @JsonProperty(value = JSON_PROPERTY_END_TIME_UTC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndTimeUtc(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endTimeUtc) {
        this.endTimeUtc = endTimeUtc;
    }

    public TaskResult status(@org.eclipse.jdt.annotation.NonNull TaskCompletionStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Gets or sets the status.
     * 
     * @return status
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TaskCompletionStatus getStatus() {
        return status;
    }

    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatus(@org.eclipse.jdt.annotation.NonNull TaskCompletionStatus status) {
        this.status = status;
    }

    public TaskResult name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public TaskResult key(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the key.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getKey() {
        return key;
    }

    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
    }

    public TaskResult id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public TaskResult errorMessage(@org.eclipse.jdt.annotation.NonNull String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Gets or sets the error message.
     * 
     * @return errorMessage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ERROR_MESSAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty(value = JSON_PROPERTY_ERROR_MESSAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setErrorMessage(@org.eclipse.jdt.annotation.NonNull String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public TaskResult longErrorMessage(@org.eclipse.jdt.annotation.NonNull String longErrorMessage) {
        this.longErrorMessage = longErrorMessage;
        return this;
    }

    /**
     * Gets or sets the long error message.
     * 
     * @return longErrorMessage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LONG_ERROR_MESSAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLongErrorMessage() {
        return longErrorMessage;
    }

    @JsonProperty(value = JSON_PROPERTY_LONG_ERROR_MESSAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLongErrorMessage(@org.eclipse.jdt.annotation.NonNull String longErrorMessage) {
        this.longErrorMessage = longErrorMessage;
    }

    /**
     * Return true if this TaskResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskResult taskResult = (TaskResult) o;
        return Objects.equals(this.startTimeUtc, taskResult.startTimeUtc)
                && Objects.equals(this.endTimeUtc, taskResult.endTimeUtc)
                && Objects.equals(this.status, taskResult.status) && Objects.equals(this.name, taskResult.name)
                && Objects.equals(this.key, taskResult.key) && Objects.equals(this.id, taskResult.id)
                && Objects.equals(this.errorMessage, taskResult.errorMessage)
                && Objects.equals(this.longErrorMessage, taskResult.longErrorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTimeUtc, endTimeUtc, status, name, key, id, errorMessage, longErrorMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskResult {\n");
        sb.append("    startTimeUtc: ").append(toIndentedString(startTimeUtc)).append("\n");
        sb.append("    endTimeUtc: ").append(toIndentedString(endTimeUtc)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("    longErrorMessage: ").append(toIndentedString(longErrorMessage)).append("\n");
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

        // add `StartTimeUtc` to the URL query string
        if (getStartTimeUtc() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStartTimeUtc%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartTimeUtc()))));
        }

        // add `EndTimeUtc` to the URL query string
        if (getEndTimeUtc() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEndTimeUtc%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndTimeUtc()))));
        }

        // add `Status` to the URL query string
        if (getStatus() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStatus%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Key` to the URL query string
        if (getKey() != null) {
            joiner.add(String.format(Locale.ROOT, "%sKey%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKey()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `ErrorMessage` to the URL query string
        if (getErrorMessage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sErrorMessage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getErrorMessage()))));
        }

        // add `LongErrorMessage` to the URL query string
        if (getLongErrorMessage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLongErrorMessage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLongErrorMessage()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TaskResult instance;

        public Builder() {
            this(new TaskResult());
        }

        protected Builder(TaskResult instance) {
            this.instance = instance;
        }

        public TaskResult.Builder startTimeUtc(OffsetDateTime startTimeUtc) {
            this.instance.startTimeUtc = startTimeUtc;
            return this;
        }

        public TaskResult.Builder endTimeUtc(OffsetDateTime endTimeUtc) {
            this.instance.endTimeUtc = endTimeUtc;
            return this;
        }

        public TaskResult.Builder status(TaskCompletionStatus status) {
            this.instance.status = status;
            return this;
        }

        public TaskResult.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public TaskResult.Builder key(String key) {
            this.instance.key = key;
            return this;
        }

        public TaskResult.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public TaskResult.Builder errorMessage(String errorMessage) {
            this.instance.errorMessage = errorMessage;
            return this;
        }

        public TaskResult.Builder longErrorMessage(String longErrorMessage) {
            this.instance.longErrorMessage = longErrorMessage;
            return this;
        }

        /**
         * returns a built TaskResult instance.
         *
         * The builder is not reusable.
         */
        public TaskResult build() {
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
    public static TaskResult.Builder builder() {
        return new TaskResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TaskResult.Builder toBuilder() {
        return new TaskResult.Builder().startTimeUtc(getStartTimeUtc()).endTimeUtc(getEndTimeUtc()).status(getStatus())
                .name(getName()).key(getKey()).id(getId()).errorMessage(getErrorMessage())
                .longErrorMessage(getLongErrorMessage());
    }
}

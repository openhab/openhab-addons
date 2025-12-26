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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * TimerEventInfo
 */
@JsonPropertyOrder({ TimerEventInfo.JSON_PROPERTY_ID, TimerEventInfo.JSON_PROPERTY_PROGRAM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TimerEventInfo {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_PROGRAM_ID = "ProgramId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID programId;

    public TimerEventInfo() {
    }

    public TimerEventInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
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

    public TimerEventInfo programId(@org.eclipse.jdt.annotation.NonNull UUID programId) {
        this.programId = programId;
        return this;
    }

    /**
     * Get programId
     * 
     * @return programId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getProgramId() {
        return programId;
    }

    @JsonProperty(value = JSON_PROPERTY_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramId(@org.eclipse.jdt.annotation.NonNull UUID programId) {
        this.programId = programId;
    }

    /**
     * Return true if this TimerEventInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimerEventInfo timerEventInfo = (TimerEventInfo) o;
        return Objects.equals(this.id, timerEventInfo.id) && Objects.equals(this.programId, timerEventInfo.programId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TimerEventInfo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    programId: ").append(toIndentedString(programId)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `ProgramId` to the URL query string
        if (getProgramId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProgramId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProgramId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TimerEventInfo instance;

        public Builder() {
            this(new TimerEventInfo());
        }

        protected Builder(TimerEventInfo instance) {
            this.instance = instance;
        }

        public TimerEventInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public TimerEventInfo.Builder programId(UUID programId) {
            this.instance.programId = programId;
            return this;
        }

        /**
         * returns a built TimerEventInfo instance.
         *
         * The builder is not reusable.
         */
        public TimerEventInfo build() {
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
    public static TimerEventInfo.Builder builder() {
        return new TimerEventInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TimerEventInfo.Builder toBuilder() {
        return new TimerEventInfo.Builder().id(getId()).programId(getProgramId());
    }
}

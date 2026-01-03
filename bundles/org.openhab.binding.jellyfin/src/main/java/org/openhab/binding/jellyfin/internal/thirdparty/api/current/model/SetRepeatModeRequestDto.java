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
 * Class SetRepeatModeRequestDto.
 */
@JsonPropertyOrder({ SetRepeatModeRequestDto.JSON_PROPERTY_MODE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SetRepeatModeRequestDto {
    public static final String JSON_PROPERTY_MODE = "Mode";
    @org.eclipse.jdt.annotation.Nullable
    private GroupRepeatMode mode;

    public SetRepeatModeRequestDto() {
    }

    public SetRepeatModeRequestDto mode(@org.eclipse.jdt.annotation.Nullable GroupRepeatMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Enum GroupRepeatMode.
     * 
     * @return mode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupRepeatMode getMode() {
        return mode;
    }

    @JsonProperty(value = JSON_PROPERTY_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMode(@org.eclipse.jdt.annotation.Nullable GroupRepeatMode mode) {
        this.mode = mode;
    }

    /**
     * Return true if this SetRepeatModeRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetRepeatModeRequestDto setRepeatModeRequestDto = (SetRepeatModeRequestDto) o;
        return Objects.equals(this.mode, setRepeatModeRequestDto.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SetRepeatModeRequestDto {\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
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

        // add `Mode` to the URL query string
        if (getMode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMode()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SetRepeatModeRequestDto instance;

        public Builder() {
            this(new SetRepeatModeRequestDto());
        }

        protected Builder(SetRepeatModeRequestDto instance) {
            this.instance = instance;
        }

        public SetRepeatModeRequestDto.Builder mode(GroupRepeatMode mode) {
            this.instance.mode = mode;
            return this;
        }

        /**
         * returns a built SetRepeatModeRequestDto instance.
         *
         * The builder is not reusable.
         */
        public SetRepeatModeRequestDto build() {
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
    public static SetRepeatModeRequestDto.Builder builder() {
        return new SetRepeatModeRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SetRepeatModeRequestDto.Builder toBuilder() {
        return new SetRepeatModeRequestDto.Builder().mode(getMode());
    }
}

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
 * Class SetShuffleModeRequestDto.
 */
@JsonPropertyOrder({ SetShuffleModeRequestDto.JSON_PROPERTY_MODE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SetShuffleModeRequestDto {
    public static final String JSON_PROPERTY_MODE = "Mode";
    @org.eclipse.jdt.annotation.Nullable
    private GroupShuffleMode mode;

    public SetShuffleModeRequestDto() {
    }

    public SetShuffleModeRequestDto mode(@org.eclipse.jdt.annotation.Nullable GroupShuffleMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Enum GroupShuffleMode.
     * 
     * @return mode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupShuffleMode getMode() {
        return mode;
    }

    @JsonProperty(value = JSON_PROPERTY_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMode(@org.eclipse.jdt.annotation.Nullable GroupShuffleMode mode) {
        this.mode = mode;
    }

    /**
     * Return true if this SetShuffleModeRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetShuffleModeRequestDto setShuffleModeRequestDto = (SetShuffleModeRequestDto) o;
        return Objects.equals(this.mode, setShuffleModeRequestDto.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SetShuffleModeRequestDto {\n");
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

        private SetShuffleModeRequestDto instance;

        public Builder() {
            this(new SetShuffleModeRequestDto());
        }

        protected Builder(SetShuffleModeRequestDto instance) {
            this.instance = instance;
        }

        public SetShuffleModeRequestDto.Builder mode(GroupShuffleMode mode) {
            this.instance.mode = mode;
            return this;
        }

        /**
         * returns a built SetShuffleModeRequestDto instance.
         *
         * The builder is not reusable.
         */
        public SetShuffleModeRequestDto build() {
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
    public static SetShuffleModeRequestDto.Builder builder() {
        return new SetShuffleModeRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SetShuffleModeRequestDto.Builder toBuilder() {
        return new SetShuffleModeRequestDto.Builder().mode(getMode());
    }
}

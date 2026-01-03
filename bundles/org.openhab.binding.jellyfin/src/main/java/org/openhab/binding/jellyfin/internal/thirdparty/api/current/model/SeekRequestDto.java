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
 * Class SeekRequestDto.
 */
@JsonPropertyOrder({ SeekRequestDto.JSON_PROPERTY_POSITION_TICKS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SeekRequestDto {
    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long positionTicks;

    public SeekRequestDto() {
    }

    public SeekRequestDto positionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    /**
     * Return true if this SeekRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeekRequestDto seekRequestDto = (SeekRequestDto) o;
        return Objects.equals(this.positionTicks, seekRequestDto.positionTicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionTicks);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SeekRequestDto {\n");
        sb.append("    positionTicks: ").append(toIndentedString(positionTicks)).append("\n");
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

        // add `PositionTicks` to the URL query string
        if (getPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPositionTicks()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SeekRequestDto instance;

        public Builder() {
            this(new SeekRequestDto());
        }

        protected Builder(SeekRequestDto instance) {
            this.instance = instance;
        }

        public SeekRequestDto.Builder positionTicks(Long positionTicks) {
            this.instance.positionTicks = positionTicks;
            return this;
        }

        /**
         * returns a built SeekRequestDto instance.
         *
         * The builder is not reusable.
         */
        public SeekRequestDto build() {
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
    public static SeekRequestDto.Builder builder() {
        return new SeekRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SeekRequestDto.Builder toBuilder() {
        return new SeekRequestDto.Builder().positionTicks(getPositionTicks());
    }
}

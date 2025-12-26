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

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class IgnoreWaitRequestDto.
 */
@JsonPropertyOrder({ IgnoreWaitRequestDto.JSON_PROPERTY_IGNORE_WAIT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class IgnoreWaitRequestDto {
    public static final String JSON_PROPERTY_IGNORE_WAIT = "IgnoreWait";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean ignoreWait;

    public IgnoreWaitRequestDto() {
    }

    public IgnoreWaitRequestDto ignoreWait(@org.eclipse.jdt.annotation.NonNull Boolean ignoreWait) {
        this.ignoreWait = ignoreWait;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the client should be ignored.
     * 
     * @return ignoreWait
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IGNORE_WAIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIgnoreWait() {
        return ignoreWait;
    }

    @JsonProperty(value = JSON_PROPERTY_IGNORE_WAIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreWait(@org.eclipse.jdt.annotation.NonNull Boolean ignoreWait) {
        this.ignoreWait = ignoreWait;
    }

    /**
     * Return true if this IgnoreWaitRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IgnoreWaitRequestDto ignoreWaitRequestDto = (IgnoreWaitRequestDto) o;
        return Objects.equals(this.ignoreWait, ignoreWaitRequestDto.ignoreWait);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoreWait);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IgnoreWaitRequestDto {\n");
        sb.append("    ignoreWait: ").append(toIndentedString(ignoreWait)).append("\n");
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

        // add `IgnoreWait` to the URL query string
        if (getIgnoreWait() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIgnoreWait%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIgnoreWait()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private IgnoreWaitRequestDto instance;

        public Builder() {
            this(new IgnoreWaitRequestDto());
        }

        protected Builder(IgnoreWaitRequestDto instance) {
            this.instance = instance;
        }

        public IgnoreWaitRequestDto.Builder ignoreWait(Boolean ignoreWait) {
            this.instance.ignoreWait = ignoreWait;
            return this;
        }

        /**
         * returns a built IgnoreWaitRequestDto instance.
         *
         * The builder is not reusable.
         */
        public IgnoreWaitRequestDto build() {
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
    public static IgnoreWaitRequestDto.Builder builder() {
        return new IgnoreWaitRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public IgnoreWaitRequestDto.Builder toBuilder() {
        return new IgnoreWaitRequestDto.Builder().ignoreWait(getIgnoreWait());
    }
}

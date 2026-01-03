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
 * Class PingRequestDto.
 */
@JsonPropertyOrder({ PingRequestDto.JSON_PROPERTY_PING })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PingRequestDto {
    public static final String JSON_PROPERTY_PING = "Ping";
    @org.eclipse.jdt.annotation.Nullable
    private Long ping;

    public PingRequestDto() {
    }

    public PingRequestDto ping(@org.eclipse.jdt.annotation.Nullable Long ping) {
        this.ping = ping;
        return this;
    }

    /**
     * Gets or sets the ping time.
     * 
     * @return ping
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPing() {
        return ping;
    }

    @JsonProperty(value = JSON_PROPERTY_PING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPing(@org.eclipse.jdt.annotation.Nullable Long ping) {
        this.ping = ping;
    }

    /**
     * Return true if this PingRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PingRequestDto pingRequestDto = (PingRequestDto) o;
        return Objects.equals(this.ping, pingRequestDto.ping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ping);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PingRequestDto {\n");
        sb.append("    ping: ").append(toIndentedString(ping)).append("\n");
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

        // add `Ping` to the URL query string
        if (getPing() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPing()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PingRequestDto instance;

        public Builder() {
            this(new PingRequestDto());
        }

        protected Builder(PingRequestDto instance) {
            this.instance = instance;
        }

        public PingRequestDto.Builder ping(Long ping) {
            this.instance.ping = ping;
            return this;
        }

        /**
         * returns a built PingRequestDto instance.
         *
         * The builder is not reusable.
         */
        public PingRequestDto build() {
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
    public static PingRequestDto.Builder builder() {
        return new PingRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PingRequestDto.Builder toBuilder() {
        return new PingRequestDto.Builder().ping(getPing());
    }
}

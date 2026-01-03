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
 * The quick connect request body.
 */
@JsonPropertyOrder({ QuickConnectDto.JSON_PROPERTY_SECRET })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QuickConnectDto {
    public static final String JSON_PROPERTY_SECRET = "Secret";
    @org.eclipse.jdt.annotation.NonNull
    private String secret;

    public QuickConnectDto() {
    }

    public QuickConnectDto secret(@org.eclipse.jdt.annotation.NonNull String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * Gets or sets the quick connect secret.
     * 
     * @return secret
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SECRET, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getSecret() {
        return secret;
    }

    @JsonProperty(value = JSON_PROPERTY_SECRET, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSecret(@org.eclipse.jdt.annotation.NonNull String secret) {
        this.secret = secret;
    }

    /**
     * Return true if this QuickConnectDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuickConnectDto quickConnectDto = (QuickConnectDto) o;
        return Objects.equals(this.secret, quickConnectDto.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secret);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QuickConnectDto {\n");
        sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
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

        // add `Secret` to the URL query string
        if (getSecret() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSecret%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSecret()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private QuickConnectDto instance;

        public Builder() {
            this(new QuickConnectDto());
        }

        protected Builder(QuickConnectDto instance) {
            this.instance = instance;
        }

        public QuickConnectDto.Builder secret(String secret) {
            this.instance.secret = secret;
            return this;
        }

        /**
         * returns a built QuickConnectDto instance.
         *
         * The builder is not reusable.
         */
        public QuickConnectDto build() {
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
    public static QuickConnectDto.Builder builder() {
        return new QuickConnectDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public QuickConnectDto.Builder toBuilder() {
        return new QuickConnectDto.Builder().secret(getSecret());
    }
}

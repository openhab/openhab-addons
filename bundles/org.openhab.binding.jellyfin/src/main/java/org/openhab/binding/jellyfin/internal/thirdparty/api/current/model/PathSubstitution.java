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
 * Defines the MediaBrowser.Model.Configuration.PathSubstitution.
 */
@JsonPropertyOrder({ PathSubstitution.JSON_PROPERTY_FROM, PathSubstitution.JSON_PROPERTY_TO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PathSubstitution {
    public static final String JSON_PROPERTY_FROM = "From";
    @org.eclipse.jdt.annotation.Nullable
    private String from;

    public static final String JSON_PROPERTY_TO = "To";
    @org.eclipse.jdt.annotation.Nullable
    private String to;

    public PathSubstitution() {
    }

    public PathSubstitution from(@org.eclipse.jdt.annotation.Nullable String from) {
        this.from = from;
        return this;
    }

    /**
     * Gets or sets the value to substitute.
     * 
     * @return from
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_FROM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFrom() {
        return from;
    }

    @JsonProperty(value = JSON_PROPERTY_FROM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFrom(@org.eclipse.jdt.annotation.Nullable String from) {
        this.from = from;
    }

    public PathSubstitution to(@org.eclipse.jdt.annotation.Nullable String to) {
        this.to = to;
        return this;
    }

    /**
     * Gets or sets the value to substitution with.
     * 
     * @return to
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTo() {
        return to;
    }

    @JsonProperty(value = JSON_PROPERTY_TO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTo(@org.eclipse.jdt.annotation.Nullable String to) {
        this.to = to;
    }

    /**
     * Return true if this PathSubstitution object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PathSubstitution pathSubstitution = (PathSubstitution) o;
        return Objects.equals(this.from, pathSubstitution.from) && Objects.equals(this.to, pathSubstitution.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PathSubstitution {\n");
        sb.append("    from: ").append(toIndentedString(from)).append("\n");
        sb.append("    to: ").append(toIndentedString(to)).append("\n");
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

        // add `From` to the URL query string
        if (getFrom() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sFrom%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFrom()))));
        }

        // add `To` to the URL query string
        if (getTo() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTo%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTo()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PathSubstitution instance;

        public Builder() {
            this(new PathSubstitution());
        }

        protected Builder(PathSubstitution instance) {
            this.instance = instance;
        }

        public PathSubstitution.Builder from(String from) {
            this.instance.from = from;
            return this;
        }

        public PathSubstitution.Builder to(String to) {
            this.instance.to = to;
            return this;
        }

        /**
         * returns a built PathSubstitution instance.
         *
         * The builder is not reusable.
         */
        public PathSubstitution build() {
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
    public static PathSubstitution.Builder builder() {
        return new PathSubstitution.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PathSubstitution.Builder toBuilder() {
        return new PathSubstitution.Builder().from(getFrom()).to(getTo());
    }
}

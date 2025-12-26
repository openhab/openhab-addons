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
 * MetadataConfiguration
 */
@JsonPropertyOrder({ MetadataConfiguration.JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MetadataConfiguration {
    public static final String JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED = "UseFileCreationTimeForDateAdded";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean useFileCreationTimeForDateAdded;

    public MetadataConfiguration() {
    }

    public MetadataConfiguration useFileCreationTimeForDateAdded(
            @org.eclipse.jdt.annotation.NonNull Boolean useFileCreationTimeForDateAdded) {
        this.useFileCreationTimeForDateAdded = useFileCreationTimeForDateAdded;
        return this;
    }

    /**
     * Get useFileCreationTimeForDateAdded
     * 
     * @return useFileCreationTimeForDateAdded
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getUseFileCreationTimeForDateAdded() {
        return useFileCreationTimeForDateAdded;
    }

    @JsonProperty(value = JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUseFileCreationTimeForDateAdded(
            @org.eclipse.jdt.annotation.NonNull Boolean useFileCreationTimeForDateAdded) {
        this.useFileCreationTimeForDateAdded = useFileCreationTimeForDateAdded;
    }

    /**
     * Return true if this MetadataConfiguration object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadataConfiguration metadataConfiguration = (MetadataConfiguration) o;
        return Objects.equals(this.useFileCreationTimeForDateAdded,
                metadataConfiguration.useFileCreationTimeForDateAdded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(useFileCreationTimeForDateAdded);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MetadataConfiguration {\n");
        sb.append("    useFileCreationTimeForDateAdded: ").append(toIndentedString(useFileCreationTimeForDateAdded))
                .append("\n");
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

        // add `UseFileCreationTimeForDateAdded` to the URL query string
        if (getUseFileCreationTimeForDateAdded() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUseFileCreationTimeForDateAdded%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUseFileCreationTimeForDateAdded()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MetadataConfiguration instance;

        public Builder() {
            this(new MetadataConfiguration());
        }

        protected Builder(MetadataConfiguration instance) {
            this.instance = instance;
        }

        public MetadataConfiguration.Builder useFileCreationTimeForDateAdded(Boolean useFileCreationTimeForDateAdded) {
            this.instance.useFileCreationTimeForDateAdded = useFileCreationTimeForDateAdded;
            return this;
        }

        /**
         * returns a built MetadataConfiguration instance.
         *
         * The builder is not reusable.
         */
        public MetadataConfiguration build() {
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
    public static MetadataConfiguration.Builder builder() {
        return new MetadataConfiguration.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MetadataConfiguration.Builder toBuilder() {
        return new MetadataConfiguration.Builder()
                .useFileCreationTimeForDateAdded(getUseFileCreationTimeForDateAdded());
    }
}

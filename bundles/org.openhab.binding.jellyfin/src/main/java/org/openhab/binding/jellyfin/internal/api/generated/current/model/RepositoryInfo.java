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
 * Class RepositoryInfo.
 */
@JsonPropertyOrder({ RepositoryInfo.JSON_PROPERTY_NAME, RepositoryInfo.JSON_PROPERTY_URL,
        RepositoryInfo.JSON_PROPERTY_ENABLED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RepositoryInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_URL = "Url";
    @org.eclipse.jdt.annotation.NonNull
    private String url;

    public static final String JSON_PROPERTY_ENABLED = "Enabled";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enabled;

    public RepositoryInfo() {
    }

    public RepositoryInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public RepositoryInfo url(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * Gets or sets the URL.
     * 
     * @return url
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUrl() {
        return url;
    }

    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrl(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
    }

    public RepositoryInfo enabled(@org.eclipse.jdt.annotation.NonNull Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the repository is enabled.
     * 
     * @return enabled
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabled(@org.eclipse.jdt.annotation.NonNull Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Return true if this RepositoryInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepositoryInfo repositoryInfo = (RepositoryInfo) o;
        return Objects.equals(this.name, repositoryInfo.name) && Objects.equals(this.url, repositoryInfo.url)
                && Objects.equals(this.enabled, repositoryInfo.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, enabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RepositoryInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Url` to the URL query string
        if (getUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUrl()))));
        }

        // add `Enabled` to the URL query string
        if (getEnabled() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnabled()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private RepositoryInfo instance;

        public Builder() {
            this(new RepositoryInfo());
        }

        protected Builder(RepositoryInfo instance) {
            this.instance = instance;
        }

        public RepositoryInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public RepositoryInfo.Builder url(String url) {
            this.instance.url = url;
            return this;
        }

        public RepositoryInfo.Builder enabled(Boolean enabled) {
            this.instance.enabled = enabled;
            return this;
        }

        /**
         * returns a built RepositoryInfo instance.
         *
         * The builder is not reusable.
         */
        public RepositoryInfo build() {
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
    public static RepositoryInfo.Builder builder() {
        return new RepositoryInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RepositoryInfo.Builder toBuilder() {
        return new RepositoryInfo.Builder().name(getName()).url(getUrl()).enabled(getEnabled());
    }
}

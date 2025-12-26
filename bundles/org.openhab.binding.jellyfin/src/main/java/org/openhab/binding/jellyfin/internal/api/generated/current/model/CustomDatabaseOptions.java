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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the options for a custom database connector.
 */
@JsonPropertyOrder({ CustomDatabaseOptions.JSON_PROPERTY_PLUGIN_NAME,
        CustomDatabaseOptions.JSON_PROPERTY_PLUGIN_ASSEMBLY, CustomDatabaseOptions.JSON_PROPERTY_CONNECTION_STRING,
        CustomDatabaseOptions.JSON_PROPERTY_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CustomDatabaseOptions {
    public static final String JSON_PROPERTY_PLUGIN_NAME = "PluginName";
    @org.eclipse.jdt.annotation.NonNull
    private String pluginName;

    public static final String JSON_PROPERTY_PLUGIN_ASSEMBLY = "PluginAssembly";
    @org.eclipse.jdt.annotation.NonNull
    private String pluginAssembly;

    public static final String JSON_PROPERTY_CONNECTION_STRING = "ConnectionString";
    @org.eclipse.jdt.annotation.NonNull
    private String connectionString;

    public static final String JSON_PROPERTY_OPTIONS = "Options";
    @org.eclipse.jdt.annotation.NonNull
    private List<CustomDatabaseOption> options = new ArrayList<>();

    public CustomDatabaseOptions() {
    }

    public CustomDatabaseOptions pluginName(@org.eclipse.jdt.annotation.NonNull String pluginName) {
        this.pluginName = pluginName;
        return this;
    }

    /**
     * Gets or sets the Plugin name to search for database providers.
     * 
     * @return pluginName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLUGIN_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPluginName() {
        return pluginName;
    }

    @JsonProperty(value = JSON_PROPERTY_PLUGIN_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPluginName(@org.eclipse.jdt.annotation.NonNull String pluginName) {
        this.pluginName = pluginName;
    }

    public CustomDatabaseOptions pluginAssembly(@org.eclipse.jdt.annotation.NonNull String pluginAssembly) {
        this.pluginAssembly = pluginAssembly;
        return this;
    }

    /**
     * Gets or sets the plugin assembly to search for providers.
     * 
     * @return pluginAssembly
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLUGIN_ASSEMBLY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPluginAssembly() {
        return pluginAssembly;
    }

    @JsonProperty(value = JSON_PROPERTY_PLUGIN_ASSEMBLY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPluginAssembly(@org.eclipse.jdt.annotation.NonNull String pluginAssembly) {
        this.pluginAssembly = pluginAssembly;
    }

    public CustomDatabaseOptions connectionString(@org.eclipse.jdt.annotation.NonNull String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Gets or sets the connection string for the custom database provider.
     * 
     * @return connectionString
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONNECTION_STRING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getConnectionString() {
        return connectionString;
    }

    @JsonProperty(value = JSON_PROPERTY_CONNECTION_STRING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConnectionString(@org.eclipse.jdt.annotation.NonNull String connectionString) {
        this.connectionString = connectionString;
    }

    public CustomDatabaseOptions options(@org.eclipse.jdt.annotation.NonNull List<CustomDatabaseOption> options) {
        this.options = options;
        return this;
    }

    public CustomDatabaseOptions addOptionsItem(CustomDatabaseOption optionsItem) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(optionsItem);
        return this;
    }

    /**
     * Gets or sets the list of extra options for the custom provider.
     * 
     * @return options
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<CustomDatabaseOption> getOptions() {
        return options;
    }

    @JsonProperty(value = JSON_PROPERTY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOptions(@org.eclipse.jdt.annotation.NonNull List<CustomDatabaseOption> options) {
        this.options = options;
    }

    /**
     * Return true if this CustomDatabaseOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomDatabaseOptions customDatabaseOptions = (CustomDatabaseOptions) o;
        return Objects.equals(this.pluginName, customDatabaseOptions.pluginName)
                && Objects.equals(this.pluginAssembly, customDatabaseOptions.pluginAssembly)
                && Objects.equals(this.connectionString, customDatabaseOptions.connectionString)
                && Objects.equals(this.options, customDatabaseOptions.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginName, pluginAssembly, connectionString, options);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CustomDatabaseOptions {\n");
        sb.append("    pluginName: ").append(toIndentedString(pluginName)).append("\n");
        sb.append("    pluginAssembly: ").append(toIndentedString(pluginAssembly)).append("\n");
        sb.append("    connectionString: ").append(toIndentedString(connectionString)).append("\n");
        sb.append("    options: ").append(toIndentedString(options)).append("\n");
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

        // add `PluginName` to the URL query string
        if (getPluginName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPluginName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPluginName()))));
        }

        // add `PluginAssembly` to the URL query string
        if (getPluginAssembly() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPluginAssembly%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPluginAssembly()))));
        }

        // add `ConnectionString` to the URL query string
        if (getConnectionString() != null) {
            joiner.add(String.format(Locale.ROOT, "%sConnectionString%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getConnectionString()))));
        }

        // add `Options` to the URL query string
        if (getOptions() != null) {
            for (int i = 0; i < getOptions().size(); i++) {
                if (getOptions().get(i) != null) {
                    joiner.add(getOptions().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sOptions%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private CustomDatabaseOptions instance;

        public Builder() {
            this(new CustomDatabaseOptions());
        }

        protected Builder(CustomDatabaseOptions instance) {
            this.instance = instance;
        }

        public CustomDatabaseOptions.Builder pluginName(String pluginName) {
            this.instance.pluginName = pluginName;
            return this;
        }

        public CustomDatabaseOptions.Builder pluginAssembly(String pluginAssembly) {
            this.instance.pluginAssembly = pluginAssembly;
            return this;
        }

        public CustomDatabaseOptions.Builder connectionString(String connectionString) {
            this.instance.connectionString = connectionString;
            return this;
        }

        public CustomDatabaseOptions.Builder options(List<CustomDatabaseOption> options) {
            this.instance.options = options;
            return this;
        }

        /**
         * returns a built CustomDatabaseOptions instance.
         *
         * The builder is not reusable.
         */
        public CustomDatabaseOptions build() {
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
    public static CustomDatabaseOptions.Builder builder() {
        return new CustomDatabaseOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public CustomDatabaseOptions.Builder toBuilder() {
        return new CustomDatabaseOptions.Builder().pluginName(getPluginName()).pluginAssembly(getPluginAssembly())
                .connectionString(getConnectionString()).options(getOptions());
    }
}

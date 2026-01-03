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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the MediaBrowser.Common.Plugins.IPlugin.
 */
@JsonPropertyOrder({ IPlugin.JSON_PROPERTY_NAME, IPlugin.JSON_PROPERTY_DESCRIPTION, IPlugin.JSON_PROPERTY_ID,
        IPlugin.JSON_PROPERTY_VERSION, IPlugin.JSON_PROPERTY_ASSEMBLY_FILE_PATH, IPlugin.JSON_PROPERTY_CAN_UNINSTALL,
        IPlugin.JSON_PROPERTY_DATA_FOLDER_PATH })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class IPlugin {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_DESCRIPTION = "Description";
    @org.eclipse.jdt.annotation.Nullable
    private String description;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private UUID id;

    public static final String JSON_PROPERTY_VERSION = "Version";
    @org.eclipse.jdt.annotation.Nullable
    private String version;

    public static final String JSON_PROPERTY_ASSEMBLY_FILE_PATH = "AssemblyFilePath";
    @org.eclipse.jdt.annotation.Nullable
    private String assemblyFilePath;

    public static final String JSON_PROPERTY_CAN_UNINSTALL = "CanUninstall";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean canUninstall;

    public static final String JSON_PROPERTY_DATA_FOLDER_PATH = "DataFolderPath";
    @org.eclipse.jdt.annotation.Nullable
    private String dataFolderPath;

    public IPlugin() {
    }

    @JsonCreator
    public IPlugin(@JsonProperty(JSON_PROPERTY_NAME) String name,
            @JsonProperty(JSON_PROPERTY_DESCRIPTION) String description, @JsonProperty(JSON_PROPERTY_ID) UUID id,
            @JsonProperty(JSON_PROPERTY_VERSION) String version,
            @JsonProperty(JSON_PROPERTY_ASSEMBLY_FILE_PATH) String assemblyFilePath,
            @JsonProperty(JSON_PROPERTY_CAN_UNINSTALL) Boolean canUninstall,
            @JsonProperty(JSON_PROPERTY_DATA_FOLDER_PATH) String dataFolderPath) {
        this();
        this.name = name;
        this.description = description;
        this.id = id;
        this.version = version;
        this.assemblyFilePath = assemblyFilePath;
        this.canUninstall = canUninstall;
        this.dataFolderPath = dataFolderPath;
    }

    /**
     * Gets the name of the plugin.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    /**
     * Gets the Description.
     * 
     * @return description
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDescription() {
        return description;
    }

    /**
     * Gets the unique id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    /**
     * Gets the plugin version.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersion() {
        return version;
    }

    /**
     * Gets the path to the assembly file.
     * 
     * @return assemblyFilePath
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ASSEMBLY_FILE_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAssemblyFilePath() {
        return assemblyFilePath;
    }

    /**
     * Gets a value indicating whether the plugin can be uninstalled.
     * 
     * @return canUninstall
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CAN_UNINSTALL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanUninstall() {
        return canUninstall;
    }

    /**
     * Gets the full path to the data folder, where the plugin can store any miscellaneous files needed.
     * 
     * @return dataFolderPath
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DATA_FOLDER_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDataFolderPath() {
        return dataFolderPath;
    }

    /**
     * Return true if this IPlugin object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPlugin iplugin = (IPlugin) o;
        return Objects.equals(this.name, iplugin.name) && Objects.equals(this.description, iplugin.description)
                && Objects.equals(this.id, iplugin.id) && Objects.equals(this.version, iplugin.version)
                && Objects.equals(this.assemblyFilePath, iplugin.assemblyFilePath)
                && Objects.equals(this.canUninstall, iplugin.canUninstall)
                && Objects.equals(this.dataFolderPath, iplugin.dataFolderPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, version, assemblyFilePath, canUninstall, dataFolderPath);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IPlugin {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    assemblyFilePath: ").append(toIndentedString(assemblyFilePath)).append("\n");
        sb.append("    canUninstall: ").append(toIndentedString(canUninstall)).append("\n");
        sb.append("    dataFolderPath: ").append(toIndentedString(dataFolderPath)).append("\n");
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
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Description` to the URL query string
        if (getDescription() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDescription%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDescription()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Version` to the URL query string
        if (getVersion() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
        }

        // add `AssemblyFilePath` to the URL query string
        if (getAssemblyFilePath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAssemblyFilePath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAssemblyFilePath()))));
        }

        // add `CanUninstall` to the URL query string
        if (getCanUninstall() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCanUninstall%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanUninstall()))));
        }

        // add `DataFolderPath` to the URL query string
        if (getDataFolderPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDataFolderPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDataFolderPath()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private IPlugin instance;

        public Builder() {
            this(new IPlugin());
        }

        protected Builder(IPlugin instance) {
            this.instance = instance;
        }

        public IPlugin.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public IPlugin.Builder description(String description) {
            this.instance.description = description;
            return this;
        }

        public IPlugin.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public IPlugin.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public IPlugin.Builder assemblyFilePath(String assemblyFilePath) {
            this.instance.assemblyFilePath = assemblyFilePath;
            return this;
        }

        public IPlugin.Builder canUninstall(Boolean canUninstall) {
            this.instance.canUninstall = canUninstall;
            return this;
        }

        public IPlugin.Builder dataFolderPath(String dataFolderPath) {
            this.instance.dataFolderPath = dataFolderPath;
            return this;
        }

        /**
         * returns a built IPlugin instance.
         *
         * The builder is not reusable.
         */
        public IPlugin build() {
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
    public static IPlugin.Builder builder() {
        return new IPlugin.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public IPlugin.Builder toBuilder() {
        return new IPlugin.Builder().name(getName()).description(getDescription()).id(getId()).version(getVersion())
                .assemblyFilePath(getAssemblyFilePath()).canUninstall(getCanUninstall())
                .dataFolderPath(getDataFolderPath());
    }
}

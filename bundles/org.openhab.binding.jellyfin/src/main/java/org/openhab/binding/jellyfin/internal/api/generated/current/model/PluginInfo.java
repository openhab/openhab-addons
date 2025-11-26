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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This is a serializable stub class that is used by the api to provide information about installed plugins.
 */
@JsonPropertyOrder({ PluginInfo.JSON_PROPERTY_NAME, PluginInfo.JSON_PROPERTY_VERSION,
        PluginInfo.JSON_PROPERTY_CONFIGURATION_FILE_NAME, PluginInfo.JSON_PROPERTY_DESCRIPTION,
        PluginInfo.JSON_PROPERTY_ID, PluginInfo.JSON_PROPERTY_CAN_UNINSTALL, PluginInfo.JSON_PROPERTY_HAS_IMAGE,
        PluginInfo.JSON_PROPERTY_STATUS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PluginInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_VERSION = "Version";
    @org.eclipse.jdt.annotation.NonNull
    private String version;

    public static final String JSON_PROPERTY_CONFIGURATION_FILE_NAME = "ConfigurationFileName";
    @org.eclipse.jdt.annotation.NonNull
    private String configurationFileName;

    public static final String JSON_PROPERTY_DESCRIPTION = "Description";
    @org.eclipse.jdt.annotation.NonNull
    private String description;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_CAN_UNINSTALL = "CanUninstall";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canUninstall;

    public static final String JSON_PROPERTY_HAS_IMAGE = "HasImage";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasImage;

    public static final String JSON_PROPERTY_STATUS = "Status";
    @org.eclipse.jdt.annotation.NonNull
    private PluginStatus status;

    public PluginInfo() {
    }

    public PluginInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public PluginInfo version(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets or sets the version.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersion() {
        return version;
    }

    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersion(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
    }

    public PluginInfo configurationFileName(@org.eclipse.jdt.annotation.NonNull String configurationFileName) {
        this.configurationFileName = configurationFileName;
        return this;
    }

    /**
     * Gets or sets the name of the configuration file.
     * 
     * @return configurationFileName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONFIGURATION_FILE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getConfigurationFileName() {
        return configurationFileName;
    }

    @JsonProperty(value = JSON_PROPERTY_CONFIGURATION_FILE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConfigurationFileName(@org.eclipse.jdt.annotation.NonNull String configurationFileName) {
        this.configurationFileName = configurationFileName;
    }

    public PluginInfo description(@org.eclipse.jdt.annotation.NonNull String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets or sets the description.
     * 
     * @return description
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDescription() {
        return description;
    }

    @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDescription(@org.eclipse.jdt.annotation.NonNull String description) {
        this.description = description;
    }

    public PluginInfo id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the unique id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public PluginInfo canUninstall(@org.eclipse.jdt.annotation.NonNull Boolean canUninstall) {
        this.canUninstall = canUninstall;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the plugin can be uninstalled.
     * 
     * @return canUninstall
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_UNINSTALL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanUninstall() {
        return canUninstall;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_UNINSTALL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanUninstall(@org.eclipse.jdt.annotation.NonNull Boolean canUninstall) {
        this.canUninstall = canUninstall;
    }

    public PluginInfo hasImage(@org.eclipse.jdt.annotation.NonNull Boolean hasImage) {
        this.hasImage = hasImage;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this plugin has a valid image.
     * 
     * @return hasImage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HAS_IMAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasImage() {
        return hasImage;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_IMAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasImage(@org.eclipse.jdt.annotation.NonNull Boolean hasImage) {
        this.hasImage = hasImage;
    }

    public PluginInfo status(@org.eclipse.jdt.annotation.NonNull PluginStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Gets or sets a value indicating the status of the plugin.
     * 
     * @return status
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PluginStatus getStatus() {
        return status;
    }

    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatus(@org.eclipse.jdt.annotation.NonNull PluginStatus status) {
        this.status = status;
    }

    /**
     * Return true if this PluginInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginInfo pluginInfo = (PluginInfo) o;
        return Objects.equals(this.name, pluginInfo.name) && Objects.equals(this.version, pluginInfo.version)
                && Objects.equals(this.configurationFileName, pluginInfo.configurationFileName)
                && Objects.equals(this.description, pluginInfo.description) && Objects.equals(this.id, pluginInfo.id)
                && Objects.equals(this.canUninstall, pluginInfo.canUninstall)
                && Objects.equals(this.hasImage, pluginInfo.hasImage) && Objects.equals(this.status, pluginInfo.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, configurationFileName, description, id, canUninstall, hasImage, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PluginInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    configurationFileName: ").append(toIndentedString(configurationFileName)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    canUninstall: ").append(toIndentedString(canUninstall)).append("\n");
        sb.append("    hasImage: ").append(toIndentedString(hasImage)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

        // add `Version` to the URL query string
        if (getVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
        }

        // add `ConfigurationFileName` to the URL query string
        if (getConfigurationFileName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sConfigurationFileName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getConfigurationFileName()))));
        }

        // add `Description` to the URL query string
        if (getDescription() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDescription%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDescription()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `CanUninstall` to the URL query string
        if (getCanUninstall() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanUninstall%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanUninstall()))));
        }

        // add `HasImage` to the URL query string
        if (getHasImage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasImage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasImage()))));
        }

        // add `Status` to the URL query string
        if (getStatus() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStatus%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PluginInfo instance;

        public Builder() {
            this(new PluginInfo());
        }

        protected Builder(PluginInfo instance) {
            this.instance = instance;
        }

        public PluginInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public PluginInfo.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public PluginInfo.Builder configurationFileName(String configurationFileName) {
            this.instance.configurationFileName = configurationFileName;
            return this;
        }

        public PluginInfo.Builder description(String description) {
            this.instance.description = description;
            return this;
        }

        public PluginInfo.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public PluginInfo.Builder canUninstall(Boolean canUninstall) {
            this.instance.canUninstall = canUninstall;
            return this;
        }

        public PluginInfo.Builder hasImage(Boolean hasImage) {
            this.instance.hasImage = hasImage;
            return this;
        }

        public PluginInfo.Builder status(PluginStatus status) {
            this.instance.status = status;
            return this;
        }

        /**
         * returns a built PluginInfo instance.
         *
         * The builder is not reusable.
         */
        public PluginInfo build() {
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
    public static PluginInfo.Builder builder() {
        return new PluginInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PluginInfo.Builder toBuilder() {
        return new PluginInfo.Builder().name(getName()).version(getVersion())
                .configurationFileName(getConfigurationFileName()).description(getDescription()).id(getId())
                .canUninstall(getCanUninstall()).hasImage(getHasImage()).status(getStatus());
    }
}

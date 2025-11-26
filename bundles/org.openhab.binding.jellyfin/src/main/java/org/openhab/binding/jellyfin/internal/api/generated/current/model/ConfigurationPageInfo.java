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
 * The configuration page info.
 */
@JsonPropertyOrder({ ConfigurationPageInfo.JSON_PROPERTY_NAME, ConfigurationPageInfo.JSON_PROPERTY_ENABLE_IN_MAIN_MENU,
        ConfigurationPageInfo.JSON_PROPERTY_MENU_SECTION, ConfigurationPageInfo.JSON_PROPERTY_MENU_ICON,
        ConfigurationPageInfo.JSON_PROPERTY_DISPLAY_NAME, ConfigurationPageInfo.JSON_PROPERTY_PLUGIN_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ConfigurationPageInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ENABLE_IN_MAIN_MENU = "EnableInMainMenu";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableInMainMenu;

    public static final String JSON_PROPERTY_MENU_SECTION = "MenuSection";
    @org.eclipse.jdt.annotation.NonNull
    private String menuSection;

    public static final String JSON_PROPERTY_MENU_ICON = "MenuIcon";
    @org.eclipse.jdt.annotation.NonNull
    private String menuIcon;

    public static final String JSON_PROPERTY_DISPLAY_NAME = "DisplayName";
    @org.eclipse.jdt.annotation.NonNull
    private String displayName;

    public static final String JSON_PROPERTY_PLUGIN_ID = "PluginId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID pluginId;

    public ConfigurationPageInfo() {
    }

    public ConfigurationPageInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public ConfigurationPageInfo enableInMainMenu(@org.eclipse.jdt.annotation.NonNull Boolean enableInMainMenu) {
        this.enableInMainMenu = enableInMainMenu;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the configurations page is enabled in the main menu.
     * 
     * @return enableInMainMenu
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_IN_MAIN_MENU, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableInMainMenu() {
        return enableInMainMenu;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_IN_MAIN_MENU, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableInMainMenu(@org.eclipse.jdt.annotation.NonNull Boolean enableInMainMenu) {
        this.enableInMainMenu = enableInMainMenu;
    }

    public ConfigurationPageInfo menuSection(@org.eclipse.jdt.annotation.NonNull String menuSection) {
        this.menuSection = menuSection;
        return this;
    }

    /**
     * Gets or sets the menu section.
     * 
     * @return menuSection
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MENU_SECTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMenuSection() {
        return menuSection;
    }

    @JsonProperty(value = JSON_PROPERTY_MENU_SECTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMenuSection(@org.eclipse.jdt.annotation.NonNull String menuSection) {
        this.menuSection = menuSection;
    }

    public ConfigurationPageInfo menuIcon(@org.eclipse.jdt.annotation.NonNull String menuIcon) {
        this.menuIcon = menuIcon;
        return this;
    }

    /**
     * Gets or sets the menu icon.
     * 
     * @return menuIcon
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MENU_ICON, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMenuIcon() {
        return menuIcon;
    }

    @JsonProperty(value = JSON_PROPERTY_MENU_ICON, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMenuIcon(@org.eclipse.jdt.annotation.NonNull String menuIcon) {
        this.menuIcon = menuIcon;
    }

    public ConfigurationPageInfo displayName(@org.eclipse.jdt.annotation.NonNull String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets or sets the display name.
     * 
     * @return displayName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty(value = JSON_PROPERTY_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayName(@org.eclipse.jdt.annotation.NonNull String displayName) {
        this.displayName = displayName;
    }

    public ConfigurationPageInfo pluginId(@org.eclipse.jdt.annotation.NonNull UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    /**
     * Gets or sets the plugin id.
     * 
     * @return pluginId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLUGIN_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getPluginId() {
        return pluginId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLUGIN_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPluginId(@org.eclipse.jdt.annotation.NonNull UUID pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Return true if this ConfigurationPageInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigurationPageInfo configurationPageInfo = (ConfigurationPageInfo) o;
        return Objects.equals(this.name, configurationPageInfo.name)
                && Objects.equals(this.enableInMainMenu, configurationPageInfo.enableInMainMenu)
                && Objects.equals(this.menuSection, configurationPageInfo.menuSection)
                && Objects.equals(this.menuIcon, configurationPageInfo.menuIcon)
                && Objects.equals(this.displayName, configurationPageInfo.displayName)
                && Objects.equals(this.pluginId, configurationPageInfo.pluginId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enableInMainMenu, menuSection, menuIcon, displayName, pluginId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConfigurationPageInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    enableInMainMenu: ").append(toIndentedString(enableInMainMenu)).append("\n");
        sb.append("    menuSection: ").append(toIndentedString(menuSection)).append("\n");
        sb.append("    menuIcon: ").append(toIndentedString(menuIcon)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    pluginId: ").append(toIndentedString(pluginId)).append("\n");
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

        // add `EnableInMainMenu` to the URL query string
        if (getEnableInMainMenu() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableInMainMenu%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableInMainMenu()))));
        }

        // add `MenuSection` to the URL query string
        if (getMenuSection() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMenuSection%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMenuSection()))));
        }

        // add `MenuIcon` to the URL query string
        if (getMenuIcon() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMenuIcon%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMenuIcon()))));
        }

        // add `DisplayName` to the URL query string
        if (getDisplayName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDisplayName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDisplayName()))));
        }

        // add `PluginId` to the URL query string
        if (getPluginId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPluginId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPluginId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ConfigurationPageInfo instance;

        public Builder() {
            this(new ConfigurationPageInfo());
        }

        protected Builder(ConfigurationPageInfo instance) {
            this.instance = instance;
        }

        public ConfigurationPageInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ConfigurationPageInfo.Builder enableInMainMenu(Boolean enableInMainMenu) {
            this.instance.enableInMainMenu = enableInMainMenu;
            return this;
        }

        public ConfigurationPageInfo.Builder menuSection(String menuSection) {
            this.instance.menuSection = menuSection;
            return this;
        }

        public ConfigurationPageInfo.Builder menuIcon(String menuIcon) {
            this.instance.menuIcon = menuIcon;
            return this;
        }

        public ConfigurationPageInfo.Builder displayName(String displayName) {
            this.instance.displayName = displayName;
            return this;
        }

        public ConfigurationPageInfo.Builder pluginId(UUID pluginId) {
            this.instance.pluginId = pluginId;
            return this;
        }

        /**
         * returns a built ConfigurationPageInfo instance.
         *
         * The builder is not reusable.
         */
        public ConfigurationPageInfo build() {
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
    public static ConfigurationPageInfo.Builder builder() {
        return new ConfigurationPageInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ConfigurationPageInfo.Builder toBuilder() {
        return new ConfigurationPageInfo.Builder().name(getName()).enableInMainMenu(getEnableInMainMenu())
                .menuSection(getMenuSection()).menuIcon(getMenuIcon()).displayName(getDisplayName())
                .pluginId(getPluginId());
    }
}

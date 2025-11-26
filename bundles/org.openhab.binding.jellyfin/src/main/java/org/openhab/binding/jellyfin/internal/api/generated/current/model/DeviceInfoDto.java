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

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A DTO representing device information.
 */
@JsonPropertyOrder({ DeviceInfoDto.JSON_PROPERTY_NAME, DeviceInfoDto.JSON_PROPERTY_CUSTOM_NAME,
        DeviceInfoDto.JSON_PROPERTY_ACCESS_TOKEN, DeviceInfoDto.JSON_PROPERTY_ID,
        DeviceInfoDto.JSON_PROPERTY_LAST_USER_NAME, DeviceInfoDto.JSON_PROPERTY_APP_NAME,
        DeviceInfoDto.JSON_PROPERTY_APP_VERSION, DeviceInfoDto.JSON_PROPERTY_LAST_USER_ID,
        DeviceInfoDto.JSON_PROPERTY_DATE_LAST_ACTIVITY, DeviceInfoDto.JSON_PROPERTY_CAPABILITIES,
        DeviceInfoDto.JSON_PROPERTY_ICON_URL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DeviceInfoDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_CUSTOM_NAME = "CustomName";
    @org.eclipse.jdt.annotation.NonNull
    private String customName;

    public static final String JSON_PROPERTY_ACCESS_TOKEN = "AccessToken";
    @org.eclipse.jdt.annotation.NonNull
    private String accessToken;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_LAST_USER_NAME = "LastUserName";
    @org.eclipse.jdt.annotation.NonNull
    private String lastUserName;

    public static final String JSON_PROPERTY_APP_NAME = "AppName";
    @org.eclipse.jdt.annotation.NonNull
    private String appName;

    public static final String JSON_PROPERTY_APP_VERSION = "AppVersion";
    @org.eclipse.jdt.annotation.NonNull
    private String appVersion;

    public static final String JSON_PROPERTY_LAST_USER_ID = "LastUserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID lastUserId;

    public static final String JSON_PROPERTY_DATE_LAST_ACTIVITY = "DateLastActivity";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateLastActivity;

    public static final String JSON_PROPERTY_CAPABILITIES = "Capabilities";
    @org.eclipse.jdt.annotation.NonNull
    private ClientCapabilitiesDto capabilities;

    public static final String JSON_PROPERTY_ICON_URL = "IconUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String iconUrl;

    public DeviceInfoDto() {
    }

    public DeviceInfoDto name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public DeviceInfoDto customName(@org.eclipse.jdt.annotation.NonNull String customName) {
        this.customName = customName;
        return this;
    }

    /**
     * Gets or sets the custom name.
     * 
     * @return customName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CUSTOM_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCustomName() {
        return customName;
    }

    @JsonProperty(value = JSON_PROPERTY_CUSTOM_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomName(@org.eclipse.jdt.annotation.NonNull String customName) {
        this.customName = customName;
    }

    public DeviceInfoDto accessToken(@org.eclipse.jdt.annotation.NonNull String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Gets or sets the access token.
     * 
     * @return accessToken
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ACCESS_TOKEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAccessToken() {
        return accessToken;
    }

    @JsonProperty(value = JSON_PROPERTY_ACCESS_TOKEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccessToken(@org.eclipse.jdt.annotation.NonNull String accessToken) {
        this.accessToken = accessToken;
    }

    public DeviceInfoDto id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public DeviceInfoDto lastUserName(@org.eclipse.jdt.annotation.NonNull String lastUserName) {
        this.lastUserName = lastUserName;
        return this;
    }

    /**
     * Gets or sets the last name of the user.
     * 
     * @return lastUserName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LAST_USER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLastUserName() {
        return lastUserName;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_USER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastUserName(@org.eclipse.jdt.annotation.NonNull String lastUserName) {
        this.lastUserName = lastUserName;
    }

    public DeviceInfoDto appName(@org.eclipse.jdt.annotation.NonNull String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * Gets or sets the name of the application.
     * 
     * @return appName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAppName() {
        return appName;
    }

    @JsonProperty(value = JSON_PROPERTY_APP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppName(@org.eclipse.jdt.annotation.NonNull String appName) {
        this.appName = appName;
    }

    public DeviceInfoDto appVersion(@org.eclipse.jdt.annotation.NonNull String appVersion) {
        this.appVersion = appVersion;
        return this;
    }

    /**
     * Gets or sets the application version.
     * 
     * @return appVersion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APP_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAppVersion() {
        return appVersion;
    }

    @JsonProperty(value = JSON_PROPERTY_APP_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppVersion(@org.eclipse.jdt.annotation.NonNull String appVersion) {
        this.appVersion = appVersion;
    }

    public DeviceInfoDto lastUserId(@org.eclipse.jdt.annotation.NonNull UUID lastUserId) {
        this.lastUserId = lastUserId;
        return this;
    }

    /**
     * Gets or sets the last user identifier.
     * 
     * @return lastUserId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LAST_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getLastUserId() {
        return lastUserId;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastUserId(@org.eclipse.jdt.annotation.NonNull UUID lastUserId) {
        this.lastUserId = lastUserId;
    }

    public DeviceInfoDto dateLastActivity(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateLastActivity) {
        this.dateLastActivity = dateLastActivity;
        return this;
    }

    /**
     * Gets or sets the date last modified.
     * 
     * @return dateLastActivity
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_LAST_ACTIVITY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateLastActivity() {
        return dateLastActivity;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_LAST_ACTIVITY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateLastActivity(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateLastActivity) {
        this.dateLastActivity = dateLastActivity;
    }

    public DeviceInfoDto capabilities(@org.eclipse.jdt.annotation.NonNull ClientCapabilitiesDto capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    /**
     * Gets or sets the capabilities.
     * 
     * @return capabilities
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAPABILITIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ClientCapabilitiesDto getCapabilities() {
        return capabilities;
    }

    @JsonProperty(value = JSON_PROPERTY_CAPABILITIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCapabilities(@org.eclipse.jdt.annotation.NonNull ClientCapabilitiesDto capabilities) {
        this.capabilities = capabilities;
    }

    public DeviceInfoDto iconUrl(@org.eclipse.jdt.annotation.NonNull String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    /**
     * Gets or sets the icon URL.
     * 
     * @return iconUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ICON_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getIconUrl() {
        return iconUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_ICON_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIconUrl(@org.eclipse.jdt.annotation.NonNull String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * Return true if this DeviceInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceInfoDto deviceInfoDto = (DeviceInfoDto) o;
        return Objects.equals(this.name, deviceInfoDto.name)
                && Objects.equals(this.customName, deviceInfoDto.customName)
                && Objects.equals(this.accessToken, deviceInfoDto.accessToken)
                && Objects.equals(this.id, deviceInfoDto.id)
                && Objects.equals(this.lastUserName, deviceInfoDto.lastUserName)
                && Objects.equals(this.appName, deviceInfoDto.appName)
                && Objects.equals(this.appVersion, deviceInfoDto.appVersion)
                && Objects.equals(this.lastUserId, deviceInfoDto.lastUserId)
                && Objects.equals(this.dateLastActivity, deviceInfoDto.dateLastActivity)
                && Objects.equals(this.capabilities, deviceInfoDto.capabilities)
                && Objects.equals(this.iconUrl, deviceInfoDto.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, customName, accessToken, id, lastUserName, appName, appVersion, lastUserId,
                dateLastActivity, capabilities, iconUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceInfoDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    customName: ").append(toIndentedString(customName)).append("\n");
        sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    lastUserName: ").append(toIndentedString(lastUserName)).append("\n");
        sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    appVersion: ").append(toIndentedString(appVersion)).append("\n");
        sb.append("    lastUserId: ").append(toIndentedString(lastUserId)).append("\n");
        sb.append("    dateLastActivity: ").append(toIndentedString(dateLastActivity)).append("\n");
        sb.append("    capabilities: ").append(toIndentedString(capabilities)).append("\n");
        sb.append("    iconUrl: ").append(toIndentedString(iconUrl)).append("\n");
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

        // add `CustomName` to the URL query string
        if (getCustomName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCustomName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCustomName()))));
        }

        // add `AccessToken` to the URL query string
        if (getAccessToken() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAccessToken%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAccessToken()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `LastUserName` to the URL query string
        if (getLastUserName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastUserName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastUserName()))));
        }

        // add `AppName` to the URL query string
        if (getAppName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAppName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAppName()))));
        }

        // add `AppVersion` to the URL query string
        if (getAppVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAppVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAppVersion()))));
        }

        // add `LastUserId` to the URL query string
        if (getLastUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastUserId()))));
        }

        // add `DateLastActivity` to the URL query string
        if (getDateLastActivity() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateLastActivity%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateLastActivity()))));
        }

        // add `Capabilities` to the URL query string
        if (getCapabilities() != null) {
            joiner.add(getCapabilities().toUrlQueryString(prefix + "Capabilities" + suffix));
        }

        // add `IconUrl` to the URL query string
        if (getIconUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIconUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIconUrl()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private DeviceInfoDto instance;

        public Builder() {
            this(new DeviceInfoDto());
        }

        protected Builder(DeviceInfoDto instance) {
            this.instance = instance;
        }

        public DeviceInfoDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public DeviceInfoDto.Builder customName(String customName) {
            this.instance.customName = customName;
            return this;
        }

        public DeviceInfoDto.Builder accessToken(String accessToken) {
            this.instance.accessToken = accessToken;
            return this;
        }

        public DeviceInfoDto.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public DeviceInfoDto.Builder lastUserName(String lastUserName) {
            this.instance.lastUserName = lastUserName;
            return this;
        }

        public DeviceInfoDto.Builder appName(String appName) {
            this.instance.appName = appName;
            return this;
        }

        public DeviceInfoDto.Builder appVersion(String appVersion) {
            this.instance.appVersion = appVersion;
            return this;
        }

        public DeviceInfoDto.Builder lastUserId(UUID lastUserId) {
            this.instance.lastUserId = lastUserId;
            return this;
        }

        public DeviceInfoDto.Builder dateLastActivity(OffsetDateTime dateLastActivity) {
            this.instance.dateLastActivity = dateLastActivity;
            return this;
        }

        public DeviceInfoDto.Builder capabilities(ClientCapabilitiesDto capabilities) {
            this.instance.capabilities = capabilities;
            return this;
        }

        public DeviceInfoDto.Builder iconUrl(String iconUrl) {
            this.instance.iconUrl = iconUrl;
            return this;
        }

        /**
         * returns a built DeviceInfoDto instance.
         *
         * The builder is not reusable.
         */
        public DeviceInfoDto build() {
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
    public static DeviceInfoDto.Builder builder() {
        return new DeviceInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DeviceInfoDto.Builder toBuilder() {
        return new DeviceInfoDto.Builder().name(getName()).customName(getCustomName()).accessToken(getAccessToken())
                .id(getId()).lastUserName(getLastUserName()).appName(getAppName()).appVersion(getAppVersion())
                .lastUserId(getLastUserId()).dateLastActivity(getDateLastActivity()).capabilities(getCapabilities())
                .iconUrl(getIconUrl());
    }
}

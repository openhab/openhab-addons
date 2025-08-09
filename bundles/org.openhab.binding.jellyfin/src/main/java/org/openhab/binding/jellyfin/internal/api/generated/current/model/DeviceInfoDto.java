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
import java.util.Objects;
import java.util.UUID;

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
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
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
    @JsonProperty(JSON_PROPERTY_CUSTOM_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCustomName() {
        return customName;
    }

    @JsonProperty(JSON_PROPERTY_CUSTOM_NAME)
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
    @JsonProperty(JSON_PROPERTY_ACCESS_TOKEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAccessToken() {
        return accessToken;
    }

    @JsonProperty(JSON_PROPERTY_ACCESS_TOKEN)
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
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
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
    @JsonProperty(JSON_PROPERTY_LAST_USER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLastUserName() {
        return lastUserName;
    }

    @JsonProperty(JSON_PROPERTY_LAST_USER_NAME)
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
    @JsonProperty(JSON_PROPERTY_APP_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAppName() {
        return appName;
    }

    @JsonProperty(JSON_PROPERTY_APP_NAME)
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
    @JsonProperty(JSON_PROPERTY_APP_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAppVersion() {
        return appVersion;
    }

    @JsonProperty(JSON_PROPERTY_APP_VERSION)
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
    @JsonProperty(JSON_PROPERTY_LAST_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getLastUserId() {
        return lastUserId;
    }

    @JsonProperty(JSON_PROPERTY_LAST_USER_ID)
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
    @JsonProperty(JSON_PROPERTY_DATE_LAST_ACTIVITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getDateLastActivity() {
        return dateLastActivity;
    }

    @JsonProperty(JSON_PROPERTY_DATE_LAST_ACTIVITY)
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
    @JsonProperty(JSON_PROPERTY_CAPABILITIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ClientCapabilitiesDto getCapabilities() {
        return capabilities;
    }

    @JsonProperty(JSON_PROPERTY_CAPABILITIES)
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
    @JsonProperty(JSON_PROPERTY_ICON_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIconUrl() {
        return iconUrl;
    }

    @JsonProperty(JSON_PROPERTY_ICON_URL)
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
}

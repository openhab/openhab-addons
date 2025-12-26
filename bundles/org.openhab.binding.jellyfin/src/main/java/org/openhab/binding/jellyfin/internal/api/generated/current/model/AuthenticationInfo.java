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
 * AuthenticationInfo
 */
@JsonPropertyOrder({ AuthenticationInfo.JSON_PROPERTY_ID, AuthenticationInfo.JSON_PROPERTY_ACCESS_TOKEN,
        AuthenticationInfo.JSON_PROPERTY_DEVICE_ID, AuthenticationInfo.JSON_PROPERTY_APP_NAME,
        AuthenticationInfo.JSON_PROPERTY_APP_VERSION, AuthenticationInfo.JSON_PROPERTY_DEVICE_NAME,
        AuthenticationInfo.JSON_PROPERTY_USER_ID, AuthenticationInfo.JSON_PROPERTY_IS_ACTIVE,
        AuthenticationInfo.JSON_PROPERTY_DATE_CREATED, AuthenticationInfo.JSON_PROPERTY_DATE_REVOKED,
        AuthenticationInfo.JSON_PROPERTY_DATE_LAST_ACTIVITY, AuthenticationInfo.JSON_PROPERTY_USER_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AuthenticationInfo {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private Long id;

    public static final String JSON_PROPERTY_ACCESS_TOKEN = "AccessToken";
    @org.eclipse.jdt.annotation.NonNull
    private String accessToken;

    public static final String JSON_PROPERTY_DEVICE_ID = "DeviceId";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceId;

    public static final String JSON_PROPERTY_APP_NAME = "AppName";
    @org.eclipse.jdt.annotation.NonNull
    private String appName;

    public static final String JSON_PROPERTY_APP_VERSION = "AppVersion";
    @org.eclipse.jdt.annotation.NonNull
    private String appVersion;

    public static final String JSON_PROPERTY_DEVICE_NAME = "DeviceName";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceName;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_IS_ACTIVE = "IsActive";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isActive;

    public static final String JSON_PROPERTY_DATE_CREATED = "DateCreated";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateCreated;

    public static final String JSON_PROPERTY_DATE_REVOKED = "DateRevoked";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateRevoked;

    public static final String JSON_PROPERTY_DATE_LAST_ACTIVITY = "DateLastActivity";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateLastActivity;

    public static final String JSON_PROPERTY_USER_NAME = "UserName";
    @org.eclipse.jdt.annotation.NonNull
    private String userName;

    public AuthenticationInfo() {
    }

    public AuthenticationInfo id(@org.eclipse.jdt.annotation.NonNull Long id) {
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
    public Long getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull Long id) {
        this.id = id;
    }

    public AuthenticationInfo accessToken(@org.eclipse.jdt.annotation.NonNull String accessToken) {
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

    public AuthenticationInfo deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Gets or sets the device identifier.
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public AuthenticationInfo appName(@org.eclipse.jdt.annotation.NonNull String appName) {
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

    public AuthenticationInfo appVersion(@org.eclipse.jdt.annotation.NonNull String appVersion) {
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

    public AuthenticationInfo deviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    /**
     * Gets or sets the name of the device.
     * 
     * @return deviceName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceName() {
        return deviceName;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
    }

    public AuthenticationInfo userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the user identifier.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    public AuthenticationInfo isActive(@org.eclipse.jdt.annotation.NonNull Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is active.
     * 
     * @return isActive
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_ACTIVE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsActive() {
        return isActive;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_ACTIVE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsActive(@org.eclipse.jdt.annotation.NonNull Boolean isActive) {
        this.isActive = isActive;
    }

    public AuthenticationInfo dateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    /**
     * Gets or sets the date created.
     * 
     * @return dateCreated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public AuthenticationInfo dateRevoked(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateRevoked) {
        this.dateRevoked = dateRevoked;
        return this;
    }

    /**
     * Gets or sets the date revoked.
     * 
     * @return dateRevoked
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_REVOKED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateRevoked() {
        return dateRevoked;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_REVOKED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateRevoked(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateRevoked) {
        this.dateRevoked = dateRevoked;
    }

    public AuthenticationInfo dateLastActivity(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateLastActivity) {
        this.dateLastActivity = dateLastActivity;
        return this;
    }

    /**
     * Get dateLastActivity
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

    public AuthenticationInfo userName(@org.eclipse.jdt.annotation.NonNull String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Get userName
     * 
     * @return userName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserName() {
        return userName;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserName(@org.eclipse.jdt.annotation.NonNull String userName) {
        this.userName = userName;
    }

    /**
     * Return true if this AuthenticationInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticationInfo authenticationInfo = (AuthenticationInfo) o;
        return Objects.equals(this.id, authenticationInfo.id)
                && Objects.equals(this.accessToken, authenticationInfo.accessToken)
                && Objects.equals(this.deviceId, authenticationInfo.deviceId)
                && Objects.equals(this.appName, authenticationInfo.appName)
                && Objects.equals(this.appVersion, authenticationInfo.appVersion)
                && Objects.equals(this.deviceName, authenticationInfo.deviceName)
                && Objects.equals(this.userId, authenticationInfo.userId)
                && Objects.equals(this.isActive, authenticationInfo.isActive)
                && Objects.equals(this.dateCreated, authenticationInfo.dateCreated)
                && Objects.equals(this.dateRevoked, authenticationInfo.dateRevoked)
                && Objects.equals(this.dateLastActivity, authenticationInfo.dateLastActivity)
                && Objects.equals(this.userName, authenticationInfo.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken, deviceId, appName, appVersion, deviceName, userId, isActive, dateCreated,
                dateRevoked, dateLastActivity, userName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthenticationInfo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
        sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
        sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    appVersion: ").append(toIndentedString(appVersion)).append("\n");
        sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
        sb.append("    dateCreated: ").append(toIndentedString(dateCreated)).append("\n");
        sb.append("    dateRevoked: ").append(toIndentedString(dateRevoked)).append("\n");
        sb.append("    dateLastActivity: ").append(toIndentedString(dateLastActivity)).append("\n");
        sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `AccessToken` to the URL query string
        if (getAccessToken() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAccessToken%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAccessToken()))));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
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

        // add `DeviceName` to the URL query string
        if (getDeviceName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceName()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `IsActive` to the URL query string
        if (getIsActive() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsActive%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsActive()))));
        }

        // add `DateCreated` to the URL query string
        if (getDateCreated() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateCreated%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateCreated()))));
        }

        // add `DateRevoked` to the URL query string
        if (getDateRevoked() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateRevoked%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateRevoked()))));
        }

        // add `DateLastActivity` to the URL query string
        if (getDateLastActivity() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateLastActivity%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateLastActivity()))));
        }

        // add `UserName` to the URL query string
        if (getUserName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AuthenticationInfo instance;

        public Builder() {
            this(new AuthenticationInfo());
        }

        protected Builder(AuthenticationInfo instance) {
            this.instance = instance;
        }

        public AuthenticationInfo.Builder id(Long id) {
            this.instance.id = id;
            return this;
        }

        public AuthenticationInfo.Builder accessToken(String accessToken) {
            this.instance.accessToken = accessToken;
            return this;
        }

        public AuthenticationInfo.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        public AuthenticationInfo.Builder appName(String appName) {
            this.instance.appName = appName;
            return this;
        }

        public AuthenticationInfo.Builder appVersion(String appVersion) {
            this.instance.appVersion = appVersion;
            return this;
        }

        public AuthenticationInfo.Builder deviceName(String deviceName) {
            this.instance.deviceName = deviceName;
            return this;
        }

        public AuthenticationInfo.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public AuthenticationInfo.Builder isActive(Boolean isActive) {
            this.instance.isActive = isActive;
            return this;
        }

        public AuthenticationInfo.Builder dateCreated(OffsetDateTime dateCreated) {
            this.instance.dateCreated = dateCreated;
            return this;
        }

        public AuthenticationInfo.Builder dateRevoked(OffsetDateTime dateRevoked) {
            this.instance.dateRevoked = dateRevoked;
            return this;
        }

        public AuthenticationInfo.Builder dateLastActivity(OffsetDateTime dateLastActivity) {
            this.instance.dateLastActivity = dateLastActivity;
            return this;
        }

        public AuthenticationInfo.Builder userName(String userName) {
            this.instance.userName = userName;
            return this;
        }

        /**
         * returns a built AuthenticationInfo instance.
         *
         * The builder is not reusable.
         */
        public AuthenticationInfo build() {
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
    public static AuthenticationInfo.Builder builder() {
        return new AuthenticationInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AuthenticationInfo.Builder toBuilder() {
        return new AuthenticationInfo.Builder().id(getId()).accessToken(getAccessToken()).deviceId(getDeviceId())
                .appName(getAppName()).appVersion(getAppVersion()).deviceName(getDeviceName()).userId(getUserId())
                .isActive(getIsActive()).dateCreated(getDateCreated()).dateRevoked(getDateRevoked())
                .dateLastActivity(getDateLastActivity()).userName(getUserName());
    }
}

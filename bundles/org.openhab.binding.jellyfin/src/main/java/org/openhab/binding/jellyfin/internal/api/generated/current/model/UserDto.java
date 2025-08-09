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
 * Class UserDto.
 */
@JsonPropertyOrder({ UserDto.JSON_PROPERTY_NAME, UserDto.JSON_PROPERTY_SERVER_ID, UserDto.JSON_PROPERTY_SERVER_NAME,
        UserDto.JSON_PROPERTY_ID, UserDto.JSON_PROPERTY_PRIMARY_IMAGE_TAG, UserDto.JSON_PROPERTY_HAS_PASSWORD,
        UserDto.JSON_PROPERTY_HAS_CONFIGURED_PASSWORD, UserDto.JSON_PROPERTY_HAS_CONFIGURED_EASY_PASSWORD,
        UserDto.JSON_PROPERTY_ENABLE_AUTO_LOGIN, UserDto.JSON_PROPERTY_LAST_LOGIN_DATE,
        UserDto.JSON_PROPERTY_LAST_ACTIVITY_DATE, UserDto.JSON_PROPERTY_CONFIGURATION, UserDto.JSON_PROPERTY_POLICY,
        UserDto.JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO })

public class UserDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_SERVER_ID = "ServerId";
    @org.eclipse.jdt.annotation.NonNull
    private String serverId;

    public static final String JSON_PROPERTY_SERVER_NAME = "ServerName";
    @org.eclipse.jdt.annotation.NonNull
    private String serverName;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_TAG = "PrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String primaryImageTag;

    public static final String JSON_PROPERTY_HAS_PASSWORD = "HasPassword";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasPassword;

    public static final String JSON_PROPERTY_HAS_CONFIGURED_PASSWORD = "HasConfiguredPassword";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasConfiguredPassword;

    public static final String JSON_PROPERTY_HAS_CONFIGURED_EASY_PASSWORD = "HasConfiguredEasyPassword";
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasConfiguredEasyPassword;

    public static final String JSON_PROPERTY_ENABLE_AUTO_LOGIN = "EnableAutoLogin";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAutoLogin;

    public static final String JSON_PROPERTY_LAST_LOGIN_DATE = "LastLoginDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastLoginDate;

    public static final String JSON_PROPERTY_LAST_ACTIVITY_DATE = "LastActivityDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastActivityDate;

    public static final String JSON_PROPERTY_CONFIGURATION = "Configuration";
    @org.eclipse.jdt.annotation.NonNull
    private UserConfiguration _configuration;

    public static final String JSON_PROPERTY_POLICY = "Policy";
    @org.eclipse.jdt.annotation.NonNull
    private UserPolicy policy;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO = "PrimaryImageAspectRatio";
    @org.eclipse.jdt.annotation.NonNull
    private Double primaryImageAspectRatio;

    public UserDto() {
    }

    public UserDto name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public UserDto serverId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
        return this;
    }

    /**
     * Gets or sets the server identifier.
     * 
     * @return serverId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERVER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getServerId() {
        return serverId;
    }

    @JsonProperty(JSON_PROPERTY_SERVER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
    }

    public UserDto serverName(@org.eclipse.jdt.annotation.NonNull String serverName) {
        this.serverName = serverName;
        return this;
    }

    /**
     * Gets or sets the name of the server. This is not used by the server and is for client-side usage only.
     * 
     * @return serverName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERVER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getServerName() {
        return serverName;
    }

    @JsonProperty(JSON_PROPERTY_SERVER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerName(@org.eclipse.jdt.annotation.NonNull String serverName) {
        this.serverName = serverName;
    }

    public UserDto id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public UserDto primaryImageTag(@org.eclipse.jdt.annotation.NonNull String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
        return this;
    }

    /**
     * Gets or sets the primary image tag.
     * 
     * @return primaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPrimaryImageTag() {
        return primaryImageTag;
    }

    @JsonProperty(JSON_PROPERTY_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
    }

    public UserDto hasPassword(@org.eclipse.jdt.annotation.NonNull Boolean hasPassword) {
        this.hasPassword = hasPassword;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance has password.
     * 
     * @return hasPassword
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HAS_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getHasPassword() {
        return hasPassword;
    }

    @JsonProperty(JSON_PROPERTY_HAS_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasPassword(@org.eclipse.jdt.annotation.NonNull Boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public UserDto hasConfiguredPassword(@org.eclipse.jdt.annotation.NonNull Boolean hasConfiguredPassword) {
        this.hasConfiguredPassword = hasConfiguredPassword;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance has configured password.
     * 
     * @return hasConfiguredPassword
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HAS_CONFIGURED_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getHasConfiguredPassword() {
        return hasConfiguredPassword;
    }

    @JsonProperty(JSON_PROPERTY_HAS_CONFIGURED_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasConfiguredPassword(@org.eclipse.jdt.annotation.NonNull Boolean hasConfiguredPassword) {
        this.hasConfiguredPassword = hasConfiguredPassword;
    }

    @Deprecated
    public UserDto hasConfiguredEasyPassword(@org.eclipse.jdt.annotation.NonNull Boolean hasConfiguredEasyPassword) {
        this.hasConfiguredEasyPassword = hasConfiguredEasyPassword;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance has configured easy password.
     * 
     * @return hasConfiguredEasyPassword
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HAS_CONFIGURED_EASY_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getHasConfiguredEasyPassword() {
        return hasConfiguredEasyPassword;
    }

    @Deprecated
    @JsonProperty(JSON_PROPERTY_HAS_CONFIGURED_EASY_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasConfiguredEasyPassword(@org.eclipse.jdt.annotation.NonNull Boolean hasConfiguredEasyPassword) {
        this.hasConfiguredEasyPassword = hasConfiguredEasyPassword;
    }

    public UserDto enableAutoLogin(@org.eclipse.jdt.annotation.NonNull Boolean enableAutoLogin) {
        this.enableAutoLogin = enableAutoLogin;
        return this;
    }

    /**
     * Gets or sets whether async login is enabled or not.
     * 
     * @return enableAutoLogin
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_AUTO_LOGIN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableAutoLogin() {
        return enableAutoLogin;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_AUTO_LOGIN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAutoLogin(@org.eclipse.jdt.annotation.NonNull Boolean enableAutoLogin) {
        this.enableAutoLogin = enableAutoLogin;
    }

    public UserDto lastLoginDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
        return this;
    }

    /**
     * Gets or sets the last login date.
     * 
     * @return lastLoginDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_LOGIN_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    @JsonProperty(JSON_PROPERTY_LAST_LOGIN_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastLoginDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public UserDto lastActivityDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
        return this;
    }

    /**
     * Gets or sets the last activity date.
     * 
     * @return lastActivityDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_ACTIVITY_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getLastActivityDate() {
        return lastActivityDate;
    }

    @JsonProperty(JSON_PROPERTY_LAST_ACTIVITY_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastActivityDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public UserDto _configuration(@org.eclipse.jdt.annotation.NonNull UserConfiguration _configuration) {
        this._configuration = _configuration;
        return this;
    }

    /**
     * Gets or sets the configuration.
     * 
     * @return _configuration
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONFIGURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserConfiguration getConfiguration() {
        return _configuration;
    }

    @JsonProperty(JSON_PROPERTY_CONFIGURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConfiguration(@org.eclipse.jdt.annotation.NonNull UserConfiguration _configuration) {
        this._configuration = _configuration;
    }

    public UserDto policy(@org.eclipse.jdt.annotation.NonNull UserPolicy policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Gets or sets the policy.
     * 
     * @return policy
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_POLICY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserPolicy getPolicy() {
        return policy;
    }

    @JsonProperty(JSON_PROPERTY_POLICY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPolicy(@org.eclipse.jdt.annotation.NonNull UserPolicy policy) {
        this.policy = policy;
    }

    public UserDto primaryImageAspectRatio(@org.eclipse.jdt.annotation.NonNull Double primaryImageAspectRatio) {
        this.primaryImageAspectRatio = primaryImageAspectRatio;
        return this;
    }

    /**
     * Gets or sets the primary image aspect ratio.
     * 
     * @return primaryImageAspectRatio
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getPrimaryImageAspectRatio() {
        return primaryImageAspectRatio;
    }

    @JsonProperty(JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageAspectRatio(@org.eclipse.jdt.annotation.NonNull Double primaryImageAspectRatio) {
        this.primaryImageAspectRatio = primaryImageAspectRatio;
    }

    /**
     * Return true if this UserDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return Objects.equals(this.name, userDto.name) && Objects.equals(this.serverId, userDto.serverId)
                && Objects.equals(this.serverName, userDto.serverName) && Objects.equals(this.id, userDto.id)
                && Objects.equals(this.primaryImageTag, userDto.primaryImageTag)
                && Objects.equals(this.hasPassword, userDto.hasPassword)
                && Objects.equals(this.hasConfiguredPassword, userDto.hasConfiguredPassword)
                && Objects.equals(this.hasConfiguredEasyPassword, userDto.hasConfiguredEasyPassword)
                && Objects.equals(this.enableAutoLogin, userDto.enableAutoLogin)
                && Objects.equals(this.lastLoginDate, userDto.lastLoginDate)
                && Objects.equals(this.lastActivityDate, userDto.lastActivityDate)
                && Objects.equals(this._configuration, userDto._configuration)
                && Objects.equals(this.policy, userDto.policy)
                && Objects.equals(this.primaryImageAspectRatio, userDto.primaryImageAspectRatio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, serverId, serverName, id, primaryImageTag, hasPassword, hasConfiguredPassword,
                hasConfiguredEasyPassword, enableAutoLogin, lastLoginDate, lastActivityDate, _configuration, policy,
                primaryImageAspectRatio);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    serverId: ").append(toIndentedString(serverId)).append("\n");
        sb.append("    serverName: ").append(toIndentedString(serverName)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    primaryImageTag: ").append(toIndentedString(primaryImageTag)).append("\n");
        sb.append("    hasPassword: ").append(toIndentedString(hasPassword)).append("\n");
        sb.append("    hasConfiguredPassword: ").append(toIndentedString(hasConfiguredPassword)).append("\n");
        sb.append("    hasConfiguredEasyPassword: ").append(toIndentedString(hasConfiguredEasyPassword)).append("\n");
        sb.append("    enableAutoLogin: ").append(toIndentedString(enableAutoLogin)).append("\n");
        sb.append("    lastLoginDate: ").append(toIndentedString(lastLoginDate)).append("\n");
        sb.append("    lastActivityDate: ").append(toIndentedString(lastActivityDate)).append("\n");
        sb.append("    _configuration: ").append(toIndentedString(_configuration)).append("\n");
        sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
        sb.append("    primaryImageAspectRatio: ").append(toIndentedString(primaryImageAspectRatio)).append("\n");
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

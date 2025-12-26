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
 * Class UserDto.
 */
@JsonPropertyOrder({ UserDto.JSON_PROPERTY_NAME, UserDto.JSON_PROPERTY_SERVER_ID, UserDto.JSON_PROPERTY_SERVER_NAME,
        UserDto.JSON_PROPERTY_ID, UserDto.JSON_PROPERTY_PRIMARY_IMAGE_TAG, UserDto.JSON_PROPERTY_HAS_PASSWORD,
        UserDto.JSON_PROPERTY_HAS_CONFIGURED_PASSWORD, UserDto.JSON_PROPERTY_HAS_CONFIGURED_EASY_PASSWORD,
        UserDto.JSON_PROPERTY_ENABLE_AUTO_LOGIN, UserDto.JSON_PROPERTY_LAST_LOGIN_DATE,
        UserDto.JSON_PROPERTY_LAST_ACTIVITY_DATE, UserDto.JSON_PROPERTY_CONFIGURATION, UserDto.JSON_PROPERTY_POLICY,
        UserDto.JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
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
    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerId() {
        return serverId;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_SERVER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerName() {
        return serverName;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_NAME, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPrimaryImageTag() {
        return primaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_TAG, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_HAS_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasPassword() {
        return hasPassword;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_PASSWORD, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_HAS_CONFIGURED_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasConfiguredPassword() {
        return hasConfiguredPassword;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_CONFIGURED_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasConfiguredPassword(@org.eclipse.jdt.annotation.NonNull Boolean hasConfiguredPassword) {
        this.hasConfiguredPassword = hasConfiguredPassword;
    }

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
    @JsonProperty(value = JSON_PROPERTY_HAS_CONFIGURED_EASY_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasConfiguredEasyPassword() {
        return hasConfiguredEasyPassword;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_CONFIGURED_EASY_PASSWORD, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUTO_LOGIN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAutoLogin() {
        return enableAutoLogin;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUTO_LOGIN, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_LAST_LOGIN_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_LOGIN_DATE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_LAST_ACTIVITY_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastActivityDate() {
        return lastActivityDate;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_ACTIVITY_DATE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_CONFIGURATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UserConfiguration getConfiguration() {
        return _configuration;
    }

    @JsonProperty(value = JSON_PROPERTY_CONFIGURATION, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_POLICY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UserPolicy getPolicy() {
        return policy;
    }

    @JsonProperty(value = JSON_PROPERTY_POLICY, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getPrimaryImageAspectRatio() {
        return primaryImageAspectRatio;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO, required = false)
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

        // add `ServerId` to the URL query string
        if (getServerId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sServerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerId()))));
        }

        // add `ServerName` to the URL query string
        if (getServerName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sServerName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerName()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `PrimaryImageTag` to the URL query string
        if (getPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageTag()))));
        }

        // add `HasPassword` to the URL query string
        if (getHasPassword() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasPassword()))));
        }

        // add `HasConfiguredPassword` to the URL query string
        if (getHasConfiguredPassword() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasConfiguredPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasConfiguredPassword()))));
        }

        // add `HasConfiguredEasyPassword` to the URL query string
        if (getHasConfiguredEasyPassword() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasConfiguredEasyPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasConfiguredEasyPassword()))));
        }

        // add `EnableAutoLogin` to the URL query string
        if (getEnableAutoLogin() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableAutoLogin%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAutoLogin()))));
        }

        // add `LastLoginDate` to the URL query string
        if (getLastLoginDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastLoginDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastLoginDate()))));
        }

        // add `LastActivityDate` to the URL query string
        if (getLastActivityDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastActivityDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastActivityDate()))));
        }

        // add `Configuration` to the URL query string
        if (getConfiguration() != null) {
            joiner.add(getConfiguration().toUrlQueryString(prefix + "Configuration" + suffix));
        }

        // add `Policy` to the URL query string
        if (getPolicy() != null) {
            joiner.add(getPolicy().toUrlQueryString(prefix + "Policy" + suffix));
        }

        // add `PrimaryImageAspectRatio` to the URL query string
        if (getPrimaryImageAspectRatio() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrimaryImageAspectRatio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageAspectRatio()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UserDto instance;

        public Builder() {
            this(new UserDto());
        }

        protected Builder(UserDto instance) {
            this.instance = instance;
        }

        public UserDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public UserDto.Builder serverId(String serverId) {
            this.instance.serverId = serverId;
            return this;
        }

        public UserDto.Builder serverName(String serverName) {
            this.instance.serverName = serverName;
            return this;
        }

        public UserDto.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public UserDto.Builder primaryImageTag(String primaryImageTag) {
            this.instance.primaryImageTag = primaryImageTag;
            return this;
        }

        public UserDto.Builder hasPassword(Boolean hasPassword) {
            this.instance.hasPassword = hasPassword;
            return this;
        }

        public UserDto.Builder hasConfiguredPassword(Boolean hasConfiguredPassword) {
            this.instance.hasConfiguredPassword = hasConfiguredPassword;
            return this;
        }

        public UserDto.Builder hasConfiguredEasyPassword(Boolean hasConfiguredEasyPassword) {
            this.instance.hasConfiguredEasyPassword = hasConfiguredEasyPassword;
            return this;
        }

        public UserDto.Builder enableAutoLogin(Boolean enableAutoLogin) {
            this.instance.enableAutoLogin = enableAutoLogin;
            return this;
        }

        public UserDto.Builder lastLoginDate(OffsetDateTime lastLoginDate) {
            this.instance.lastLoginDate = lastLoginDate;
            return this;
        }

        public UserDto.Builder lastActivityDate(OffsetDateTime lastActivityDate) {
            this.instance.lastActivityDate = lastActivityDate;
            return this;
        }

        public UserDto.Builder _configuration(UserConfiguration _configuration) {
            this.instance._configuration = _configuration;
            return this;
        }

        public UserDto.Builder policy(UserPolicy policy) {
            this.instance.policy = policy;
            return this;
        }

        public UserDto.Builder primaryImageAspectRatio(Double primaryImageAspectRatio) {
            this.instance.primaryImageAspectRatio = primaryImageAspectRatio;
            return this;
        }

        /**
         * returns a built UserDto instance.
         *
         * The builder is not reusable.
         */
        public UserDto build() {
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
    public static UserDto.Builder builder() {
        return new UserDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UserDto.Builder toBuilder() {
        return new UserDto.Builder().name(getName()).serverId(getServerId()).serverName(getServerName()).id(getId())
                .primaryImageTag(getPrimaryImageTag()).hasPassword(getHasPassword())
                .hasConfiguredPassword(getHasConfiguredPassword())
                .hasConfiguredEasyPassword(getHasConfiguredEasyPassword()).enableAutoLogin(getEnableAutoLogin())
                .lastLoginDate(getLastLoginDate()).lastActivityDate(getLastActivityDate())
                ._configuration(getConfiguration()).policy(getPolicy())
                .primaryImageAspectRatio(getPrimaryImageAspectRatio());
    }
}

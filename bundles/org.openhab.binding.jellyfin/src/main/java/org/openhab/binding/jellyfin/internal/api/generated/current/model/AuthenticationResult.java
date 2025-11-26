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
 * A class representing an authentication result.
 */
@JsonPropertyOrder({ AuthenticationResult.JSON_PROPERTY_USER, AuthenticationResult.JSON_PROPERTY_SESSION_INFO,
        AuthenticationResult.JSON_PROPERTY_ACCESS_TOKEN, AuthenticationResult.JSON_PROPERTY_SERVER_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AuthenticationResult {
    public static final String JSON_PROPERTY_USER = "User";
    @org.eclipse.jdt.annotation.NonNull
    private UserDto user;

    public static final String JSON_PROPERTY_SESSION_INFO = "SessionInfo";
    @org.eclipse.jdt.annotation.NonNull
    private SessionInfoDto sessionInfo;

    public static final String JSON_PROPERTY_ACCESS_TOKEN = "AccessToken";
    @org.eclipse.jdt.annotation.NonNull
    private String accessToken;

    public static final String JSON_PROPERTY_SERVER_ID = "ServerId";
    @org.eclipse.jdt.annotation.NonNull
    private String serverId;

    public AuthenticationResult() {
    }

    public AuthenticationResult user(@org.eclipse.jdt.annotation.NonNull UserDto user) {
        this.user = user;
        return this;
    }

    /**
     * Class UserDto.
     * 
     * @return user
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UserDto getUser() {
        return user;
    }

    @JsonProperty(value = JSON_PROPERTY_USER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUser(@org.eclipse.jdt.annotation.NonNull UserDto user) {
        this.user = user;
    }

    public AuthenticationResult sessionInfo(@org.eclipse.jdt.annotation.NonNull SessionInfoDto sessionInfo) {
        this.sessionInfo = sessionInfo;
        return this;
    }

    /**
     * Session info DTO.
     * 
     * @return sessionInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SESSION_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SessionInfoDto getSessionInfo() {
        return sessionInfo;
    }

    @JsonProperty(value = JSON_PROPERTY_SESSION_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionInfo(@org.eclipse.jdt.annotation.NonNull SessionInfoDto sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public AuthenticationResult accessToken(@org.eclipse.jdt.annotation.NonNull String accessToken) {
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

    public AuthenticationResult serverId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
        return this;
    }

    /**
     * Gets or sets the server id.
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

    /**
     * Return true if this AuthenticationResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticationResult authenticationResult = (AuthenticationResult) o;
        return Objects.equals(this.user, authenticationResult.user)
                && Objects.equals(this.sessionInfo, authenticationResult.sessionInfo)
                && Objects.equals(this.accessToken, authenticationResult.accessToken)
                && Objects.equals(this.serverId, authenticationResult.serverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, sessionInfo, accessToken, serverId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthenticationResult {\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    sessionInfo: ").append(toIndentedString(sessionInfo)).append("\n");
        sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
        sb.append("    serverId: ").append(toIndentedString(serverId)).append("\n");
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

        // add `User` to the URL query string
        if (getUser() != null) {
            joiner.add(getUser().toUrlQueryString(prefix + "User" + suffix));
        }

        // add `SessionInfo` to the URL query string
        if (getSessionInfo() != null) {
            joiner.add(getSessionInfo().toUrlQueryString(prefix + "SessionInfo" + suffix));
        }

        // add `AccessToken` to the URL query string
        if (getAccessToken() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAccessToken%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAccessToken()))));
        }

        // add `ServerId` to the URL query string
        if (getServerId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sServerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AuthenticationResult instance;

        public Builder() {
            this(new AuthenticationResult());
        }

        protected Builder(AuthenticationResult instance) {
            this.instance = instance;
        }

        public AuthenticationResult.Builder user(UserDto user) {
            this.instance.user = user;
            return this;
        }

        public AuthenticationResult.Builder sessionInfo(SessionInfoDto sessionInfo) {
            this.instance.sessionInfo = sessionInfo;
            return this;
        }

        public AuthenticationResult.Builder accessToken(String accessToken) {
            this.instance.accessToken = accessToken;
            return this;
        }

        public AuthenticationResult.Builder serverId(String serverId) {
            this.instance.serverId = serverId;
            return this;
        }

        /**
         * returns a built AuthenticationResult instance.
         *
         * The builder is not reusable.
         */
        public AuthenticationResult build() {
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
    public static AuthenticationResult.Builder builder() {
        return new AuthenticationResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AuthenticationResult.Builder toBuilder() {
        return new AuthenticationResult.Builder().user(getUser()).sessionInfo(getSessionInfo())
                .accessToken(getAccessToken()).serverId(getServerId());
    }
}

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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A class representing an authentication result.
 */
@JsonPropertyOrder({ AuthenticationResult.JSON_PROPERTY_USER, AuthenticationResult.JSON_PROPERTY_SESSION_INFO,
        AuthenticationResult.JSON_PROPERTY_ACCESS_TOKEN, AuthenticationResult.JSON_PROPERTY_SERVER_ID })

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
     * Gets or sets the user.
     * 
     * @return user
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserDto getUser() {
        return user;
    }

    @JsonProperty(JSON_PROPERTY_USER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUser(@org.eclipse.jdt.annotation.NonNull UserDto user) {
        this.user = user;
    }

    public AuthenticationResult sessionInfo(@org.eclipse.jdt.annotation.NonNull SessionInfoDto sessionInfo) {
        this.sessionInfo = sessionInfo;
        return this;
    }

    /**
     * Gets or sets the session info.
     * 
     * @return sessionInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SESSION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SessionInfoDto getSessionInfo() {
        return sessionInfo;
    }

    @JsonProperty(JSON_PROPERTY_SESSION_INFO)
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
}

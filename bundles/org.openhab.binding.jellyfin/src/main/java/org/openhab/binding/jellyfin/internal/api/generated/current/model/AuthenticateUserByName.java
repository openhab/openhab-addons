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
 * The authenticate user by name request body.
 */
@JsonPropertyOrder({ AuthenticateUserByName.JSON_PROPERTY_USERNAME, AuthenticateUserByName.JSON_PROPERTY_PW })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AuthenticateUserByName {
    public static final String JSON_PROPERTY_USERNAME = "Username";
    @org.eclipse.jdt.annotation.NonNull
    private String username;

    public static final String JSON_PROPERTY_PW = "Pw";
    @org.eclipse.jdt.annotation.NonNull
    private String pw;

    public AuthenticateUserByName() {
    }

    public AuthenticateUserByName username(@org.eclipse.jdt.annotation.NonNull String username) {
        this.username = username;
        return this;
    }

    /**
     * Gets or sets the username.
     * 
     * @return username
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USERNAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUsername() {
        return username;
    }

    @JsonProperty(JSON_PROPERTY_USERNAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsername(@org.eclipse.jdt.annotation.NonNull String username) {
        this.username = username;
    }

    public AuthenticateUserByName pw(@org.eclipse.jdt.annotation.NonNull String pw) {
        this.pw = pw;
        return this;
    }

    /**
     * Gets or sets the plain text password.
     * 
     * @return pw
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPw() {
        return pw;
    }

    @JsonProperty(JSON_PROPERTY_PW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPw(@org.eclipse.jdt.annotation.NonNull String pw) {
        this.pw = pw;
    }

    /**
     * Return true if this AuthenticateUserByName object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticateUserByName authenticateUserByName = (AuthenticateUserByName) o;
        return Objects.equals(this.username, authenticateUserByName.username)
                && Objects.equals(this.pw, authenticateUserByName.pw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, pw);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthenticateUserByName {\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    pw: ").append(toIndentedString(pw)).append("\n");
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

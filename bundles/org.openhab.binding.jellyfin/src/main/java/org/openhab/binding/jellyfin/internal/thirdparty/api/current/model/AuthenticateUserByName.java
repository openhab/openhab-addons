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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

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
    @org.eclipse.jdt.annotation.Nullable
    private String username;

    public static final String JSON_PROPERTY_PW = "Pw";
    @org.eclipse.jdt.annotation.Nullable
    private String pw;

    public AuthenticateUserByName() {
    }

    public AuthenticateUserByName username(@org.eclipse.jdt.annotation.Nullable String username) {
        this.username = username;
        return this;
    }

    /**
     * Gets or sets the username.
     * 
     * @return username
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_USERNAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUsername() {
        return username;
    }

    @JsonProperty(value = JSON_PROPERTY_USERNAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsername(@org.eclipse.jdt.annotation.Nullable String username) {
        this.username = username;
    }

    public AuthenticateUserByName pw(@org.eclipse.jdt.annotation.Nullable String pw) {
        this.pw = pw;
        return this;
    }

    /**
     * Gets or sets the plain text password.
     * 
     * @return pw
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPw() {
        return pw;
    }

    @JsonProperty(value = JSON_PROPERTY_PW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPw(@org.eclipse.jdt.annotation.Nullable String pw) {
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

        // add `Username` to the URL query string
        if (getUsername() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sUsername%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUsername()))));
        }

        // add `Pw` to the URL query string
        if (getPw() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPw%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPw()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AuthenticateUserByName instance;

        public Builder() {
            this(new AuthenticateUserByName());
        }

        protected Builder(AuthenticateUserByName instance) {
            this.instance = instance;
        }

        public AuthenticateUserByName.Builder username(String username) {
            this.instance.username = username;
            return this;
        }

        public AuthenticateUserByName.Builder pw(String pw) {
            this.instance.pw = pw;
            return this;
        }

        /**
         * returns a built AuthenticateUserByName instance.
         *
         * The builder is not reusable.
         */
        public AuthenticateUserByName build() {
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
    public static AuthenticateUserByName.Builder builder() {
        return new AuthenticateUserByName.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AuthenticateUserByName.Builder toBuilder() {
        return new AuthenticateUserByName.Builder().username(getUsername()).pw(getPw());
    }
}

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
 * Class SessionUserInfo.
 */
@JsonPropertyOrder({ SessionUserInfo.JSON_PROPERTY_USER_ID, SessionUserInfo.JSON_PROPERTY_USER_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SessionUserInfo {
    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_USER_NAME = "UserName";
    @org.eclipse.jdt.annotation.NonNull
    private String userName;

    public SessionUserInfo() {
    }

    public SessionUserInfo userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
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

    public SessionUserInfo userName(@org.eclipse.jdt.annotation.NonNull String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Gets or sets the name of the user.
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
     * Return true if this SessionUserInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionUserInfo sessionUserInfo = (SessionUserInfo) o;
        return Objects.equals(this.userId, sessionUserInfo.userId)
                && Objects.equals(this.userName, sessionUserInfo.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SessionUserInfo {\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `UserName` to the URL query string
        if (getUserName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SessionUserInfo instance;

        public Builder() {
            this(new SessionUserInfo());
        }

        protected Builder(SessionUserInfo instance) {
            this.instance = instance;
        }

        public SessionUserInfo.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public SessionUserInfo.Builder userName(String userName) {
            this.instance.userName = userName;
            return this;
        }

        /**
         * returns a built SessionUserInfo instance.
         *
         * The builder is not reusable.
         */
        public SessionUserInfo build() {
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
    public static SessionUserInfo.Builder builder() {
        return new SessionUserInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SessionUserInfo.Builder toBuilder() {
        return new SessionUserInfo.Builder().userId(getUserId()).userName(getUserName());
    }
}

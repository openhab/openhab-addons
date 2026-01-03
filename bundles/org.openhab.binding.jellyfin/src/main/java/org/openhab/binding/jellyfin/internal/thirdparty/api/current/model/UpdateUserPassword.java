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
 * The update user password request body.
 */
@JsonPropertyOrder({ UpdateUserPassword.JSON_PROPERTY_CURRENT_PASSWORD, UpdateUserPassword.JSON_PROPERTY_CURRENT_PW,
        UpdateUserPassword.JSON_PROPERTY_NEW_PW, UpdateUserPassword.JSON_PROPERTY_RESET_PASSWORD })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdateUserPassword {
    public static final String JSON_PROPERTY_CURRENT_PASSWORD = "CurrentPassword";
    @org.eclipse.jdt.annotation.Nullable
    private String currentPassword;

    public static final String JSON_PROPERTY_CURRENT_PW = "CurrentPw";
    @org.eclipse.jdt.annotation.Nullable
    private String currentPw;

    public static final String JSON_PROPERTY_NEW_PW = "NewPw";
    @org.eclipse.jdt.annotation.Nullable
    private String newPw;

    public static final String JSON_PROPERTY_RESET_PASSWORD = "ResetPassword";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean resetPassword;

    public UpdateUserPassword() {
    }

    public UpdateUserPassword currentPassword(@org.eclipse.jdt.annotation.Nullable String currentPassword) {
        this.currentPassword = currentPassword;
        return this;
    }

    /**
     * Gets or sets the current sha1-hashed password.
     * 
     * @return currentPassword
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CURRENT_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCurrentPassword() {
        return currentPassword;
    }

    @JsonProperty(value = JSON_PROPERTY_CURRENT_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentPassword(@org.eclipse.jdt.annotation.Nullable String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public UpdateUserPassword currentPw(@org.eclipse.jdt.annotation.Nullable String currentPw) {
        this.currentPw = currentPw;
        return this;
    }

    /**
     * Gets or sets the current plain text password.
     * 
     * @return currentPw
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CURRENT_PW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCurrentPw() {
        return currentPw;
    }

    @JsonProperty(value = JSON_PROPERTY_CURRENT_PW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentPw(@org.eclipse.jdt.annotation.Nullable String currentPw) {
        this.currentPw = currentPw;
    }

    public UpdateUserPassword newPw(@org.eclipse.jdt.annotation.Nullable String newPw) {
        this.newPw = newPw;
        return this;
    }

    /**
     * Gets or sets the new plain text password.
     * 
     * @return newPw
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NEW_PW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getNewPw() {
        return newPw;
    }

    @JsonProperty(value = JSON_PROPERTY_NEW_PW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewPw(@org.eclipse.jdt.annotation.Nullable String newPw) {
        this.newPw = newPw;
    }

    public UpdateUserPassword resetPassword(@org.eclipse.jdt.annotation.Nullable Boolean resetPassword) {
        this.resetPassword = resetPassword;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to reset the password.
     * 
     * @return resetPassword
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_RESET_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getResetPassword() {
        return resetPassword;
    }

    @JsonProperty(value = JSON_PROPERTY_RESET_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResetPassword(@org.eclipse.jdt.annotation.Nullable Boolean resetPassword) {
        this.resetPassword = resetPassword;
    }

    /**
     * Return true if this UpdateUserPassword object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateUserPassword updateUserPassword = (UpdateUserPassword) o;
        return Objects.equals(this.currentPassword, updateUserPassword.currentPassword)
                && Objects.equals(this.currentPw, updateUserPassword.currentPw)
                && Objects.equals(this.newPw, updateUserPassword.newPw)
                && Objects.equals(this.resetPassword, updateUserPassword.resetPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentPassword, currentPw, newPw, resetPassword);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateUserPassword {\n");
        sb.append("    currentPassword: ").append(toIndentedString(currentPassword)).append("\n");
        sb.append("    currentPw: ").append(toIndentedString(currentPw)).append("\n");
        sb.append("    newPw: ").append(toIndentedString(newPw)).append("\n");
        sb.append("    resetPassword: ").append(toIndentedString(resetPassword)).append("\n");
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

        // add `CurrentPassword` to the URL query string
        if (getCurrentPassword() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCurrentPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCurrentPassword()))));
        }

        // add `CurrentPw` to the URL query string
        if (getCurrentPw() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCurrentPw%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCurrentPw()))));
        }

        // add `NewPw` to the URL query string
        if (getNewPw() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sNewPw%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNewPw()))));
        }

        // add `ResetPassword` to the URL query string
        if (getResetPassword() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sResetPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getResetPassword()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UpdateUserPassword instance;

        public Builder() {
            this(new UpdateUserPassword());
        }

        protected Builder(UpdateUserPassword instance) {
            this.instance = instance;
        }

        public UpdateUserPassword.Builder currentPassword(String currentPassword) {
            this.instance.currentPassword = currentPassword;
            return this;
        }

        public UpdateUserPassword.Builder currentPw(String currentPw) {
            this.instance.currentPw = currentPw;
            return this;
        }

        public UpdateUserPassword.Builder newPw(String newPw) {
            this.instance.newPw = newPw;
            return this;
        }

        public UpdateUserPassword.Builder resetPassword(Boolean resetPassword) {
            this.instance.resetPassword = resetPassword;
            return this;
        }

        /**
         * returns a built UpdateUserPassword instance.
         *
         * The builder is not reusable.
         */
        public UpdateUserPassword build() {
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
    public static UpdateUserPassword.Builder builder() {
        return new UpdateUserPassword.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UpdateUserPassword.Builder toBuilder() {
        return new UpdateUserPassword.Builder().currentPassword(getCurrentPassword()).currentPw(getCurrentPw())
                .newPw(getNewPw()).resetPassword(getResetPassword());
    }
}

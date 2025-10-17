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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The update user easy password request body.
 */
@JsonPropertyOrder({ UpdateUserEasyPassword.JSON_PROPERTY_NEW_PASSWORD, UpdateUserEasyPassword.JSON_PROPERTY_NEW_PW,
        UpdateUserEasyPassword.JSON_PROPERTY_RESET_PASSWORD })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdateUserEasyPassword {
    public static final String JSON_PROPERTY_NEW_PASSWORD = "NewPassword";
    @org.eclipse.jdt.annotation.NonNull
    private String newPassword;

    public static final String JSON_PROPERTY_NEW_PW = "NewPw";
    @org.eclipse.jdt.annotation.NonNull
    private String newPw;

    public static final String JSON_PROPERTY_RESET_PASSWORD = "ResetPassword";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean resetPassword;

    public UpdateUserEasyPassword() {
    }

    public UpdateUserEasyPassword newPassword(@org.eclipse.jdt.annotation.NonNull String newPassword) {
        this.newPassword = newPassword;
        return this;
    }

    /**
     * Gets or sets the new sha1-hashed password.
     * 
     * @return newPassword
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NEW_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getNewPassword() {
        return newPassword;
    }

    @JsonProperty(JSON_PROPERTY_NEW_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewPassword(@org.eclipse.jdt.annotation.NonNull String newPassword) {
        this.newPassword = newPassword;
    }

    public UpdateUserEasyPassword newPw(@org.eclipse.jdt.annotation.NonNull String newPw) {
        this.newPw = newPw;
        return this;
    }

    /**
     * Gets or sets the new password.
     * 
     * @return newPw
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NEW_PW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getNewPw() {
        return newPw;
    }

    @JsonProperty(JSON_PROPERTY_NEW_PW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewPw(@org.eclipse.jdt.annotation.NonNull String newPw) {
        this.newPw = newPw;
    }

    public UpdateUserEasyPassword resetPassword(@org.eclipse.jdt.annotation.NonNull Boolean resetPassword) {
        this.resetPassword = resetPassword;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to reset the password.
     * 
     * @return resetPassword
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RESET_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getResetPassword() {
        return resetPassword;
    }

    @JsonProperty(JSON_PROPERTY_RESET_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResetPassword(@org.eclipse.jdt.annotation.NonNull Boolean resetPassword) {
        this.resetPassword = resetPassword;
    }

    /**
     * Return true if this UpdateUserEasyPassword object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateUserEasyPassword updateUserEasyPassword = (UpdateUserEasyPassword) o;
        return Objects.equals(this.newPassword, updateUserEasyPassword.newPassword)
                && Objects.equals(this.newPw, updateUserEasyPassword.newPw)
                && Objects.equals(this.resetPassword, updateUserEasyPassword.resetPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newPassword, newPw, resetPassword);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateUserEasyPassword {\n");
        sb.append("    newPassword: ").append(toIndentedString(newPassword)).append("\n");
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

        // add `NewPassword` to the URL query string
        if (getNewPassword() != null) {
            joiner.add(String.format("%sNewPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNewPassword()))));
        }

        // add `NewPw` to the URL query string
        if (getNewPw() != null) {
            joiner.add(String.format("%sNewPw%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNewPw()))));
        }

        // add `ResetPassword` to the URL query string
        if (getResetPassword() != null) {
            joiner.add(String.format("%sResetPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getResetPassword()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UpdateUserEasyPassword instance;

        public Builder() {
            this(new UpdateUserEasyPassword());
        }

        protected Builder(UpdateUserEasyPassword instance) {
            this.instance = instance;
        }

        public UpdateUserEasyPassword.Builder newPassword(String newPassword) {
            this.instance.newPassword = newPassword;
            return this;
        }

        public UpdateUserEasyPassword.Builder newPw(String newPw) {
            this.instance.newPw = newPw;
            return this;
        }

        public UpdateUserEasyPassword.Builder resetPassword(Boolean resetPassword) {
            this.instance.resetPassword = resetPassword;
            return this;
        }

        /**
         * returns a built UpdateUserEasyPassword instance.
         *
         * The builder is not reusable.
         */
        public UpdateUserEasyPassword build() {
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
    public static UpdateUserEasyPassword.Builder builder() {
        return new UpdateUserEasyPassword.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UpdateUserEasyPassword.Builder toBuilder() {
        return new UpdateUserEasyPassword.Builder().newPassword(getNewPassword()).newPw(getNewPw())
                .resetPassword(getResetPassword());
    }
}

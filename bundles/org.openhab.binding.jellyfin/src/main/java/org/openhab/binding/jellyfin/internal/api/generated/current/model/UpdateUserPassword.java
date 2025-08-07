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
 * The update user password request body.
 */
@JsonPropertyOrder({ UpdateUserPassword.JSON_PROPERTY_CURRENT_PASSWORD, UpdateUserPassword.JSON_PROPERTY_CURRENT_PW,
        UpdateUserPassword.JSON_PROPERTY_NEW_PW, UpdateUserPassword.JSON_PROPERTY_RESET_PASSWORD })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdateUserPassword {
    public static final String JSON_PROPERTY_CURRENT_PASSWORD = "CurrentPassword";
    @org.eclipse.jdt.annotation.NonNull
    private String currentPassword;

    public static final String JSON_PROPERTY_CURRENT_PW = "CurrentPw";
    @org.eclipse.jdt.annotation.NonNull
    private String currentPw;

    public static final String JSON_PROPERTY_NEW_PW = "NewPw";
    @org.eclipse.jdt.annotation.NonNull
    private String newPw;

    public static final String JSON_PROPERTY_RESET_PASSWORD = "ResetPassword";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean resetPassword;

    public UpdateUserPassword() {
    }

    public UpdateUserPassword currentPassword(@org.eclipse.jdt.annotation.NonNull String currentPassword) {
        this.currentPassword = currentPassword;
        return this;
    }

    /**
     * Gets or sets the current sha1-hashed password.
     * 
     * @return currentPassword
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CURRENT_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCurrentPassword() {
        return currentPassword;
    }

    @JsonProperty(JSON_PROPERTY_CURRENT_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentPassword(@org.eclipse.jdt.annotation.NonNull String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public UpdateUserPassword currentPw(@org.eclipse.jdt.annotation.NonNull String currentPw) {
        this.currentPw = currentPw;
        return this;
    }

    /**
     * Gets or sets the current plain text password.
     * 
     * @return currentPw
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CURRENT_PW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCurrentPw() {
        return currentPw;
    }

    @JsonProperty(JSON_PROPERTY_CURRENT_PW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentPw(@org.eclipse.jdt.annotation.NonNull String currentPw) {
        this.currentPw = currentPw;
    }

    public UpdateUserPassword newPw(@org.eclipse.jdt.annotation.NonNull String newPw) {
        this.newPw = newPw;
        return this;
    }

    /**
     * Gets or sets the new plain text password.
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

    public UpdateUserPassword resetPassword(@org.eclipse.jdt.annotation.NonNull Boolean resetPassword) {
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
}

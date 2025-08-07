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
 * Forgot Password request body DTO.
 */
@JsonPropertyOrder({ ForgotPasswordDto.JSON_PROPERTY_ENTERED_USERNAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ForgotPasswordDto {
    public static final String JSON_PROPERTY_ENTERED_USERNAME = "EnteredUsername";
    @org.eclipse.jdt.annotation.Nullable
    private String enteredUsername;

    public ForgotPasswordDto() {
    }

    public ForgotPasswordDto enteredUsername(@org.eclipse.jdt.annotation.Nullable String enteredUsername) {
        this.enteredUsername = enteredUsername;
        return this;
    }

    /**
     * Gets or sets the entered username to have its password reset.
     * 
     * @return enteredUsername
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_ENTERED_USERNAME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEnteredUsername() {
        return enteredUsername;
    }

    @JsonProperty(JSON_PROPERTY_ENTERED_USERNAME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEnteredUsername(@org.eclipse.jdt.annotation.Nullable String enteredUsername) {
        this.enteredUsername = enteredUsername;
    }

    /**
     * Return true if this ForgotPasswordDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ForgotPasswordDto forgotPasswordDto = (ForgotPasswordDto) o;
        return Objects.equals(this.enteredUsername, forgotPasswordDto.enteredUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enteredUsername);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ForgotPasswordDto {\n");
        sb.append("    enteredUsername: ").append(toIndentedString(enteredUsername)).append("\n");
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

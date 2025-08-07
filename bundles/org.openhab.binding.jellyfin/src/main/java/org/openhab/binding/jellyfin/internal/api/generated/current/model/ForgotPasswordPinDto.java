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
 * Forgot Password Pin enter request body DTO.
 */
@JsonPropertyOrder({ ForgotPasswordPinDto.JSON_PROPERTY_PIN })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ForgotPasswordPinDto {
    public static final String JSON_PROPERTY_PIN = "Pin";
    @org.eclipse.jdt.annotation.Nullable
    private String pin;

    public ForgotPasswordPinDto() {
    }

    public ForgotPasswordPinDto pin(@org.eclipse.jdt.annotation.Nullable String pin) {
        this.pin = pin;
        return this;
    }

    /**
     * Gets or sets the entered pin to have the password reset.
     * 
     * @return pin
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_PIN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getPin() {
        return pin;
    }

    @JsonProperty(JSON_PROPERTY_PIN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPin(@org.eclipse.jdt.annotation.Nullable String pin) {
        this.pin = pin;
    }

    /**
     * Return true if this ForgotPasswordPinDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ForgotPasswordPinDto forgotPasswordPinDto = (ForgotPasswordPinDto) o;
        return Objects.equals(this.pin, forgotPasswordPinDto.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pin);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ForgotPasswordPinDto {\n");
        sb.append("    pin: ").append(toIndentedString(pin)).append("\n");
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

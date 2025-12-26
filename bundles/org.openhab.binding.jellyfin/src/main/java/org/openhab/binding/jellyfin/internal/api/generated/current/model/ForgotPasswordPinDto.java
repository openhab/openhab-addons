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
    @JsonProperty(value = JSON_PROPERTY_PIN, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getPin() {
        return pin;
    }

    @JsonProperty(value = JSON_PROPERTY_PIN, required = true)
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

        // add `Pin` to the URL query string
        if (getPin() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPin%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPin()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ForgotPasswordPinDto instance;

        public Builder() {
            this(new ForgotPasswordPinDto());
        }

        protected Builder(ForgotPasswordPinDto instance) {
            this.instance = instance;
        }

        public ForgotPasswordPinDto.Builder pin(String pin) {
            this.instance.pin = pin;
            return this;
        }

        /**
         * returns a built ForgotPasswordPinDto instance.
         *
         * The builder is not reusable.
         */
        public ForgotPasswordPinDto build() {
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
    public static ForgotPasswordPinDto.Builder builder() {
        return new ForgotPasswordPinDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ForgotPasswordPinDto.Builder toBuilder() {
        return new ForgotPasswordPinDto.Builder().pin(getPin());
    }
}

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
    @JsonProperty(value = JSON_PROPERTY_ENTERED_USERNAME, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getEnteredUsername() {
        return enteredUsername;
    }

    @JsonProperty(value = JSON_PROPERTY_ENTERED_USERNAME, required = true)
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

        // add `EnteredUsername` to the URL query string
        if (getEnteredUsername() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnteredUsername%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnteredUsername()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ForgotPasswordDto instance;

        public Builder() {
            this(new ForgotPasswordDto());
        }

        protected Builder(ForgotPasswordDto instance) {
            this.instance = instance;
        }

        public ForgotPasswordDto.Builder enteredUsername(String enteredUsername) {
            this.instance.enteredUsername = enteredUsername;
            return this;
        }

        /**
         * returns a built ForgotPasswordDto instance.
         *
         * The builder is not reusable.
         */
        public ForgotPasswordDto build() {
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
    public static ForgotPasswordDto.Builder builder() {
        return new ForgotPasswordDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ForgotPasswordDto.Builder toBuilder() {
        return new ForgotPasswordDto.Builder().enteredUsername(getEnteredUsername());
    }
}

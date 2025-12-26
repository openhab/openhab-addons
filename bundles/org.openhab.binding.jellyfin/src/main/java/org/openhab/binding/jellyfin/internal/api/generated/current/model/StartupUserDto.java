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
 * The startup user DTO.
 */
@JsonPropertyOrder({ StartupUserDto.JSON_PROPERTY_NAME, StartupUserDto.JSON_PROPERTY_PASSWORD })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StartupUserDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_PASSWORD = "Password";
    @org.eclipse.jdt.annotation.NonNull
    private String password;

    public StartupUserDto() {
    }

    public StartupUserDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the username.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public StartupUserDto password(@org.eclipse.jdt.annotation.NonNull String password) {
        this.password = password;
        return this;
    }

    /**
     * Gets or sets the user&#39;s password.
     * 
     * @return password
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPassword() {
        return password;
    }

    @JsonProperty(value = JSON_PROPERTY_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPassword(@org.eclipse.jdt.annotation.NonNull String password) {
        this.password = password;
    }

    /**
     * Return true if this StartupUserDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StartupUserDto startupUserDto = (StartupUserDto) o;
        return Objects.equals(this.name, startupUserDto.name) && Objects.equals(this.password, startupUserDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, password);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StartupUserDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Password` to the URL query string
        if (getPassword() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPassword()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private StartupUserDto instance;

        public Builder() {
            this(new StartupUserDto());
        }

        protected Builder(StartupUserDto instance) {
            this.instance = instance;
        }

        public StartupUserDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public StartupUserDto.Builder password(String password) {
            this.instance.password = password;
            return this;
        }

        /**
         * returns a built StartupUserDto instance.
         *
         * The builder is not reusable.
         */
        public StartupUserDto build() {
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
    public static StartupUserDto.Builder builder() {
        return new StartupUserDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public StartupUserDto.Builder toBuilder() {
        return new StartupUserDto.Builder().name(getName()).password(getPassword());
    }
}

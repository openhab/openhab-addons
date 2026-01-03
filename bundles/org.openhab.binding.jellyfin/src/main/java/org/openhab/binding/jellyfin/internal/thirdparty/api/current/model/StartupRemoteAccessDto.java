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
 * Startup remote access dto.
 */
@JsonPropertyOrder({ StartupRemoteAccessDto.JSON_PROPERTY_ENABLE_REMOTE_ACCESS,
        StartupRemoteAccessDto.JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StartupRemoteAccessDto {
    public static final String JSON_PROPERTY_ENABLE_REMOTE_ACCESS = "EnableRemoteAccess";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableRemoteAccess;

    public static final String JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING = "EnableAutomaticPortMapping";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAutomaticPortMapping;

    public StartupRemoteAccessDto() {
    }

    public StartupRemoteAccessDto enableRemoteAccess(@org.eclipse.jdt.annotation.NonNull Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
        return this;
    }

    /**
     * Gets or sets a value indicating whether enable remote access.
     * 
     * @return enableRemoteAccess
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_ACCESS, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public Boolean getEnableRemoteAccess() {
        return enableRemoteAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_ACCESS, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEnableRemoteAccess(@org.eclipse.jdt.annotation.NonNull Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
    }

    public StartupRemoteAccessDto enableAutomaticPortMapping(
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutomaticPortMapping) {
        this.enableAutomaticPortMapping = enableAutomaticPortMapping;
        return this;
    }

    /**
     * Gets or sets a value indicating whether enable automatic port mapping.
     * 
     * @return enableAutomaticPortMapping
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public Boolean getEnableAutomaticPortMapping() {
        return enableAutomaticPortMapping;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEnableAutomaticPortMapping(@org.eclipse.jdt.annotation.NonNull Boolean enableAutomaticPortMapping) {
        this.enableAutomaticPortMapping = enableAutomaticPortMapping;
    }

    /**
     * Return true if this StartupRemoteAccessDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StartupRemoteAccessDto startupRemoteAccessDto = (StartupRemoteAccessDto) o;
        return Objects.equals(this.enableRemoteAccess, startupRemoteAccessDto.enableRemoteAccess)
                && Objects.equals(this.enableAutomaticPortMapping, startupRemoteAccessDto.enableAutomaticPortMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enableRemoteAccess, enableAutomaticPortMapping);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StartupRemoteAccessDto {\n");
        sb.append("    enableRemoteAccess: ").append(toIndentedString(enableRemoteAccess)).append("\n");
        sb.append("    enableAutomaticPortMapping: ").append(toIndentedString(enableAutomaticPortMapping)).append("\n");
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

        // add `EnableRemoteAccess` to the URL query string
        if (getEnableRemoteAccess() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableRemoteAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableRemoteAccess()))));
        }

        // add `EnableAutomaticPortMapping` to the URL query string
        if (getEnableAutomaticPortMapping() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableAutomaticPortMapping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAutomaticPortMapping()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private StartupRemoteAccessDto instance;

        public Builder() {
            this(new StartupRemoteAccessDto());
        }

        protected Builder(StartupRemoteAccessDto instance) {
            this.instance = instance;
        }

        public StartupRemoteAccessDto.Builder enableRemoteAccess(Boolean enableRemoteAccess) {
            this.instance.enableRemoteAccess = enableRemoteAccess;
            return this;
        }

        public StartupRemoteAccessDto.Builder enableAutomaticPortMapping(Boolean enableAutomaticPortMapping) {
            this.instance.enableAutomaticPortMapping = enableAutomaticPortMapping;
            return this;
        }

        /**
         * returns a built StartupRemoteAccessDto instance.
         *
         * The builder is not reusable.
         */
        public StartupRemoteAccessDto build() {
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
    public static StartupRemoteAccessDto.Builder builder() {
        return new StartupRemoteAccessDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public StartupRemoteAccessDto.Builder toBuilder() {
        return new StartupRemoteAccessDto.Builder().enableRemoteAccess(getEnableRemoteAccess())
                .enableAutomaticPortMapping(getEnableAutomaticPortMapping());
    }
}

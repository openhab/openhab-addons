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
 * Startup remote access dto.
 */
@JsonPropertyOrder({ StartupRemoteAccessDto.JSON_PROPERTY_ENABLE_REMOTE_ACCESS,
        StartupRemoteAccessDto.JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StartupRemoteAccessDto {
    public static final String JSON_PROPERTY_ENABLE_REMOTE_ACCESS = "EnableRemoteAccess";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableRemoteAccess;

    public static final String JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING = "EnableAutomaticPortMapping";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableAutomaticPortMapping;

    public StartupRemoteAccessDto() {
    }

    public StartupRemoteAccessDto enableRemoteAccess(@org.eclipse.jdt.annotation.Nullable Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
        return this;
    }

    /**
     * Gets or sets a value indicating whether enable remote access.
     * 
     * @return enableRemoteAccess
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_ENABLE_REMOTE_ACCESS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getEnableRemoteAccess() {
        return enableRemoteAccess;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_REMOTE_ACCESS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEnableRemoteAccess(@org.eclipse.jdt.annotation.Nullable Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
    }

    public StartupRemoteAccessDto enableAutomaticPortMapping(
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutomaticPortMapping) {
        this.enableAutomaticPortMapping = enableAutomaticPortMapping;
        return this;
    }

    /**
     * Gets or sets a value indicating whether enable automatic port mapping.
     * 
     * @return enableAutomaticPortMapping
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getEnableAutomaticPortMapping() {
        return enableAutomaticPortMapping;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_AUTOMATIC_PORT_MAPPING)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEnableAutomaticPortMapping(@org.eclipse.jdt.annotation.Nullable Boolean enableAutomaticPortMapping) {
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
}

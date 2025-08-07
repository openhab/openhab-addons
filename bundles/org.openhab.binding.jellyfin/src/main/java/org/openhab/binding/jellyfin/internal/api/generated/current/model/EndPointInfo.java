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
 * EndPointInfo
 */
@JsonPropertyOrder({ EndPointInfo.JSON_PROPERTY_IS_LOCAL, EndPointInfo.JSON_PROPERTY_IS_IN_NETWORK })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class EndPointInfo {
    public static final String JSON_PROPERTY_IS_LOCAL = "IsLocal";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isLocal;

    public static final String JSON_PROPERTY_IS_IN_NETWORK = "IsInNetwork";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isInNetwork;

    public EndPointInfo() {
    }

    public EndPointInfo isLocal(@org.eclipse.jdt.annotation.NonNull Boolean isLocal) {
        this.isLocal = isLocal;
        return this;
    }

    /**
     * Get isLocal
     * 
     * @return isLocal
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_LOCAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsLocal() {
        return isLocal;
    }

    @JsonProperty(JSON_PROPERTY_IS_LOCAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsLocal(@org.eclipse.jdt.annotation.NonNull Boolean isLocal) {
        this.isLocal = isLocal;
    }

    public EndPointInfo isInNetwork(@org.eclipse.jdt.annotation.NonNull Boolean isInNetwork) {
        this.isInNetwork = isInNetwork;
        return this;
    }

    /**
     * Get isInNetwork
     * 
     * @return isInNetwork
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_IN_NETWORK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsInNetwork() {
        return isInNetwork;
    }

    @JsonProperty(JSON_PROPERTY_IS_IN_NETWORK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsInNetwork(@org.eclipse.jdt.annotation.NonNull Boolean isInNetwork) {
        this.isInNetwork = isInNetwork;
    }

    /**
     * Return true if this EndPointInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndPointInfo endPointInfo = (EndPointInfo) o;
        return Objects.equals(this.isLocal, endPointInfo.isLocal)
                && Objects.equals(this.isInNetwork, endPointInfo.isInNetwork);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLocal, isInNetwork);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EndPointInfo {\n");
        sb.append("    isLocal: ").append(toIndentedString(isLocal)).append("\n");
        sb.append("    isInNetwork: ").append(toIndentedString(isInNetwork)).append("\n");
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

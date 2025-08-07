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
 * The server discovery info model.
 */
@JsonPropertyOrder({ ServerDiscoveryInfo.JSON_PROPERTY_ADDRESS, ServerDiscoveryInfo.JSON_PROPERTY_ID,
        ServerDiscoveryInfo.JSON_PROPERTY_NAME, ServerDiscoveryInfo.JSON_PROPERTY_ENDPOINT_ADDRESS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ServerDiscoveryInfo {
    public static final String JSON_PROPERTY_ADDRESS = "Address";
    @org.eclipse.jdt.annotation.NonNull
    private String address;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ENDPOINT_ADDRESS = "EndpointAddress";
    @org.eclipse.jdt.annotation.NonNull
    private String endpointAddress;

    public ServerDiscoveryInfo() {
    }

    public ServerDiscoveryInfo address(@org.eclipse.jdt.annotation.NonNull String address) {
        this.address = address;
        return this;
    }

    /**
     * Gets the address.
     * 
     * @return address
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAddress() {
        return address;
    }

    @JsonProperty(JSON_PROPERTY_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAddress(@org.eclipse.jdt.annotation.NonNull String address) {
        this.address = address;
    }

    public ServerDiscoveryInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the server identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public ServerDiscoveryInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public ServerDiscoveryInfo endpointAddress(@org.eclipse.jdt.annotation.NonNull String endpointAddress) {
        this.endpointAddress = endpointAddress;
        return this;
    }

    /**
     * Gets the endpoint address.
     * 
     * @return endpointAddress
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENDPOINT_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getEndpointAddress() {
        return endpointAddress;
    }

    @JsonProperty(JSON_PROPERTY_ENDPOINT_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndpointAddress(@org.eclipse.jdt.annotation.NonNull String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    /**
     * Return true if this ServerDiscoveryInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerDiscoveryInfo serverDiscoveryInfo = (ServerDiscoveryInfo) o;
        return Objects.equals(this.address, serverDiscoveryInfo.address)
                && Objects.equals(this.id, serverDiscoveryInfo.id)
                && Objects.equals(this.name, serverDiscoveryInfo.name)
                && Objects.equals(this.endpointAddress, serverDiscoveryInfo.endpointAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, id, name, endpointAddress);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServerDiscoveryInfo {\n");
        sb.append("    address: ").append(toIndentedString(address)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    endpointAddress: ").append(toIndentedString(endpointAddress)).append("\n");
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

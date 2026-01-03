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
 * The server discovery info model.
 */
@JsonPropertyOrder({ ServerDiscoveryInfo.JSON_PROPERTY_ADDRESS, ServerDiscoveryInfo.JSON_PROPERTY_ID,
        ServerDiscoveryInfo.JSON_PROPERTY_NAME, ServerDiscoveryInfo.JSON_PROPERTY_ENDPOINT_ADDRESS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ServerDiscoveryInfo {
    public static final String JSON_PROPERTY_ADDRESS = "Address";
    @org.eclipse.jdt.annotation.Nullable
    private String address;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private String id;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_ENDPOINT_ADDRESS = "EndpointAddress";
    @org.eclipse.jdt.annotation.Nullable
    private String endpointAddress;

    public ServerDiscoveryInfo() {
    }

    public ServerDiscoveryInfo address(@org.eclipse.jdt.annotation.Nullable String address) {
        this.address = address;
        return this;
    }

    /**
     * Gets the address.
     * 
     * @return address
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ADDRESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAddress() {
        return address;
    }

    @JsonProperty(value = JSON_PROPERTY_ADDRESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAddress(@org.eclipse.jdt.annotation.Nullable String address) {
        this.address = address;
    }

    public ServerDiscoveryInfo id(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the server identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
    }

    public ServerDiscoveryInfo name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public ServerDiscoveryInfo endpointAddress(@org.eclipse.jdt.annotation.Nullable String endpointAddress) {
        this.endpointAddress = endpointAddress;
        return this;
    }

    /**
     * Gets the endpoint address.
     * 
     * @return endpointAddress
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENDPOINT_ADDRESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEndpointAddress() {
        return endpointAddress;
    }

    @JsonProperty(value = JSON_PROPERTY_ENDPOINT_ADDRESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndpointAddress(@org.eclipse.jdt.annotation.Nullable String endpointAddress) {
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

        // add `Address` to the URL query string
        if (getAddress() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAddress%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAddress()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `EndpointAddress` to the URL query string
        if (getEndpointAddress() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEndpointAddress%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndpointAddress()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ServerDiscoveryInfo instance;

        public Builder() {
            this(new ServerDiscoveryInfo());
        }

        protected Builder(ServerDiscoveryInfo instance) {
            this.instance = instance;
        }

        public ServerDiscoveryInfo.Builder address(String address) {
            this.instance.address = address;
            return this;
        }

        public ServerDiscoveryInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public ServerDiscoveryInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ServerDiscoveryInfo.Builder endpointAddress(String endpointAddress) {
            this.instance.endpointAddress = endpointAddress;
            return this;
        }

        /**
         * returns a built ServerDiscoveryInfo instance.
         *
         * The builder is not reusable.
         */
        public ServerDiscoveryInfo build() {
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
    public static ServerDiscoveryInfo.Builder builder() {
        return new ServerDiscoveryInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ServerDiscoveryInfo.Builder toBuilder() {
        return new ServerDiscoveryInfo.Builder().address(getAddress()).id(getId()).name(getName())
                .endpointAddress(getEndpointAddress());
    }
}

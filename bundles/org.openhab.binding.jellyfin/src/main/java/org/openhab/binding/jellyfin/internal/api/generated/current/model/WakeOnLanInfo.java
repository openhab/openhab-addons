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
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Provides the MAC address and port for wake-on-LAN functionality.
 */
@JsonPropertyOrder({ WakeOnLanInfo.JSON_PROPERTY_MAC_ADDRESS, WakeOnLanInfo.JSON_PROPERTY_PORT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class WakeOnLanInfo {
    public static final String JSON_PROPERTY_MAC_ADDRESS = "MacAddress";
    @org.eclipse.jdt.annotation.NonNull
    private String macAddress;

    public static final String JSON_PROPERTY_PORT = "Port";
    @org.eclipse.jdt.annotation.NonNull
    private Integer port;

    public WakeOnLanInfo() {
    }

    public WakeOnLanInfo macAddress(@org.eclipse.jdt.annotation.NonNull String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    /**
     * Gets the MAC address of the device.
     * 
     * @return macAddress
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAC_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMacAddress() {
        return macAddress;
    }

    @JsonProperty(JSON_PROPERTY_MAC_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMacAddress(@org.eclipse.jdt.annotation.NonNull String macAddress) {
        this.macAddress = macAddress;
    }

    public WakeOnLanInfo port(@org.eclipse.jdt.annotation.NonNull Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Gets or sets the wake-on-LAN port.
     * 
     * @return port
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPort() {
        return port;
    }

    @JsonProperty(JSON_PROPERTY_PORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPort(@org.eclipse.jdt.annotation.NonNull Integer port) {
        this.port = port;
    }

    /**
     * Return true if this WakeOnLanInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WakeOnLanInfo wakeOnLanInfo = (WakeOnLanInfo) o;
        return Objects.equals(this.macAddress, wakeOnLanInfo.macAddress)
                && Objects.equals(this.port, wakeOnLanInfo.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macAddress, port);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WakeOnLanInfo {\n");
        sb.append("    macAddress: ").append(toIndentedString(macAddress)).append("\n");
        sb.append("    port: ").append(toIndentedString(port)).append("\n");
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

        // add `MacAddress` to the URL query string
        if (getMacAddress() != null) {
            joiner.add(String.format("%sMacAddress%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMacAddress()))));
        }

        // add `Port` to the URL query string
        if (getPort() != null) {
            joiner.add(String.format("%sPort%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPort()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private WakeOnLanInfo instance;

        public Builder() {
            this(new WakeOnLanInfo());
        }

        protected Builder(WakeOnLanInfo instance) {
            this.instance = instance;
        }

        public WakeOnLanInfo.Builder macAddress(String macAddress) {
            this.instance.macAddress = macAddress;
            return this;
        }

        public WakeOnLanInfo.Builder port(Integer port) {
            this.instance.port = port;
            return this;
        }

        /**
         * returns a built WakeOnLanInfo instance.
         *
         * The builder is not reusable.
         */
        public WakeOnLanInfo build() {
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
    public static WakeOnLanInfo.Builder builder() {
        return new WakeOnLanInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public WakeOnLanInfo.Builder toBuilder() {
        return new WakeOnLanInfo.Builder().macAddress(getMacAddress()).port(getPort());
    }
}

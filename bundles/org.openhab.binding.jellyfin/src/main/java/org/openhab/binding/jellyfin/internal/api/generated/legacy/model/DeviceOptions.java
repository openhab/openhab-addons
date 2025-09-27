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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An entity representing custom options for a device.
 */
@JsonPropertyOrder({ DeviceOptions.JSON_PROPERTY_ID, DeviceOptions.JSON_PROPERTY_DEVICE_ID,
        DeviceOptions.JSON_PROPERTY_CUSTOM_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DeviceOptions {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private Integer id;

    public static final String JSON_PROPERTY_DEVICE_ID = "DeviceId";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceId;

    public static final String JSON_PROPERTY_CUSTOM_NAME = "CustomName";
    @org.eclipse.jdt.annotation.NonNull
    private String customName;

    public DeviceOptions() {
    }

    @JsonCreator
    public DeviceOptions(@JsonProperty(JSON_PROPERTY_ID) Integer id) {
        this();
        this.id = id;
    }

    /**
     * Gets the id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getId() {
        return id;
    }

    public DeviceOptions deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Gets the device id.
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEVICE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(JSON_PROPERTY_DEVICE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceOptions customName(@org.eclipse.jdt.annotation.NonNull String customName) {
        this.customName = customName;
        return this;
    }

    /**
     * Gets or sets the custom name.
     * 
     * @return customName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CUSTOM_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCustomName() {
        return customName;
    }

    @JsonProperty(JSON_PROPERTY_CUSTOM_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomName(@org.eclipse.jdt.annotation.NonNull String customName) {
        this.customName = customName;
    }

    /**
     * Return true if this DeviceOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceOptions deviceOptions = (DeviceOptions) o;
        return Objects.equals(this.id, deviceOptions.id) && Objects.equals(this.deviceId, deviceOptions.deviceId)
                && Objects.equals(this.customName, deviceOptions.customName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId, customName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceOptions {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
        sb.append("    customName: ").append(toIndentedString(customName)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(
                    String.format("%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format("%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
        }

        // add `CustomName` to the URL query string
        if (getCustomName() != null) {
            joiner.add(String.format("%sCustomName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCustomName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private DeviceOptions instance;

        public Builder() {
            this(new DeviceOptions());
        }

        protected Builder(DeviceOptions instance) {
            this.instance = instance;
        }

        public DeviceOptions.Builder id(Integer id) {
            this.instance.id = id;
            return this;
        }

        public DeviceOptions.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        public DeviceOptions.Builder customName(String customName) {
            this.instance.customName = customName;
            return this;
        }

        /**
         * returns a built DeviceOptions instance.
         *
         * The builder is not reusable.
         */
        public DeviceOptions build() {
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
    public static DeviceOptions.Builder builder() {
        return new DeviceOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DeviceOptions.Builder toBuilder() {
        return new DeviceOptions.Builder().id(getId()).deviceId(getDeviceId()).customName(getCustomName());
    }
}

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
 * A dto representing custom options for a device.
 */
@JsonPropertyOrder({ DeviceOptionsDto.JSON_PROPERTY_ID, DeviceOptionsDto.JSON_PROPERTY_DEVICE_ID,
        DeviceOptionsDto.JSON_PROPERTY_CUSTOM_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DeviceOptionsDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private Integer id;

    public static final String JSON_PROPERTY_DEVICE_ID = "DeviceId";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceId;

    public static final String JSON_PROPERTY_CUSTOM_NAME = "CustomName";
    @org.eclipse.jdt.annotation.NonNull
    private String customName;

    public DeviceOptionsDto() {
    }

    public DeviceOptionsDto id(@org.eclipse.jdt.annotation.NonNull Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull Integer id) {
        this.id = id;
    }

    public DeviceOptionsDto deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Gets or sets the device id.
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceOptionsDto customName(@org.eclipse.jdt.annotation.NonNull String customName) {
        this.customName = customName;
        return this;
    }

    /**
     * Gets or sets the custom name.
     * 
     * @return customName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CUSTOM_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCustomName() {
        return customName;
    }

    @JsonProperty(value = JSON_PROPERTY_CUSTOM_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomName(@org.eclipse.jdt.annotation.NonNull String customName) {
        this.customName = customName;
    }

    /**
     * Return true if this DeviceOptionsDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceOptionsDto deviceOptionsDto = (DeviceOptionsDto) o;
        return Objects.equals(this.id, deviceOptionsDto.id) && Objects.equals(this.deviceId, deviceOptionsDto.deviceId)
                && Objects.equals(this.customName, deviceOptionsDto.customName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId, customName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceOptionsDto {\n");
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
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
        }

        // add `CustomName` to the URL query string
        if (getCustomName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCustomName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCustomName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private DeviceOptionsDto instance;

        public Builder() {
            this(new DeviceOptionsDto());
        }

        protected Builder(DeviceOptionsDto instance) {
            this.instance = instance;
        }

        public DeviceOptionsDto.Builder id(Integer id) {
            this.instance.id = id;
            return this;
        }

        public DeviceOptionsDto.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        public DeviceOptionsDto.Builder customName(String customName) {
            this.instance.customName = customName;
            return this;
        }

        /**
         * returns a built DeviceOptionsDto instance.
         *
         * The builder is not reusable.
         */
        public DeviceOptionsDto build() {
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
    public static DeviceOptionsDto.Builder builder() {
        return new DeviceOptionsDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DeviceOptionsDto.Builder toBuilder() {
        return new DeviceOptionsDto.Builder().id(getId()).deviceId(getDeviceId()).customName(getCustomName());
    }
}

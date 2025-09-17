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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DeviceIdentification
 */
@JsonPropertyOrder({ DeviceIdentification.JSON_PROPERTY_FRIENDLY_NAME, DeviceIdentification.JSON_PROPERTY_MODEL_NUMBER,
        DeviceIdentification.JSON_PROPERTY_SERIAL_NUMBER, DeviceIdentification.JSON_PROPERTY_MODEL_NAME,
        DeviceIdentification.JSON_PROPERTY_MODEL_DESCRIPTION, DeviceIdentification.JSON_PROPERTY_MODEL_URL,
        DeviceIdentification.JSON_PROPERTY_MANUFACTURER, DeviceIdentification.JSON_PROPERTY_MANUFACTURER_URL,
        DeviceIdentification.JSON_PROPERTY_HEADERS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DeviceIdentification {
    public static final String JSON_PROPERTY_FRIENDLY_NAME = "FriendlyName";
    @org.eclipse.jdt.annotation.NonNull
    private String friendlyName;

    public static final String JSON_PROPERTY_MODEL_NUMBER = "ModelNumber";
    @org.eclipse.jdt.annotation.NonNull
    private String modelNumber;

    public static final String JSON_PROPERTY_SERIAL_NUMBER = "SerialNumber";
    @org.eclipse.jdt.annotation.NonNull
    private String serialNumber;

    public static final String JSON_PROPERTY_MODEL_NAME = "ModelName";
    @org.eclipse.jdt.annotation.NonNull
    private String modelName;

    public static final String JSON_PROPERTY_MODEL_DESCRIPTION = "ModelDescription";
    @org.eclipse.jdt.annotation.NonNull
    private String modelDescription;

    public static final String JSON_PROPERTY_MODEL_URL = "ModelUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String modelUrl;

    public static final String JSON_PROPERTY_MANUFACTURER = "Manufacturer";
    @org.eclipse.jdt.annotation.NonNull
    private String manufacturer;

    public static final String JSON_PROPERTY_MANUFACTURER_URL = "ManufacturerUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String manufacturerUrl;

    public static final String JSON_PROPERTY_HEADERS = "Headers";
    @org.eclipse.jdt.annotation.NonNull
    private List<HttpHeaderInfo> headers = new ArrayList<>();

    public DeviceIdentification() {
    }

    public DeviceIdentification friendlyName(@org.eclipse.jdt.annotation.NonNull String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Gets or sets the name of the friendly.
     * 
     * @return friendlyName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FRIENDLY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFriendlyName() {
        return friendlyName;
    }

    @JsonProperty(JSON_PROPERTY_FRIENDLY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFriendlyName(@org.eclipse.jdt.annotation.NonNull String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public DeviceIdentification modelNumber(@org.eclipse.jdt.annotation.NonNull String modelNumber) {
        this.modelNumber = modelNumber;
        return this;
    }

    /**
     * Gets or sets the model number.
     * 
     * @return modelNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelNumber() {
        return modelNumber;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelNumber(@org.eclipse.jdt.annotation.NonNull String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public DeviceIdentification serialNumber(@org.eclipse.jdt.annotation.NonNull String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    /**
     * Gets or sets the serial number.
     * 
     * @return serialNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSerialNumber() {
        return serialNumber;
    }

    @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSerialNumber(@org.eclipse.jdt.annotation.NonNull String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public DeviceIdentification modelName(@org.eclipse.jdt.annotation.NonNull String modelName) {
        this.modelName = modelName;
        return this;
    }

    /**
     * Gets or sets the name of the model.
     * 
     * @return modelName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelName() {
        return modelName;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelName(@org.eclipse.jdt.annotation.NonNull String modelName) {
        this.modelName = modelName;
    }

    public DeviceIdentification modelDescription(@org.eclipse.jdt.annotation.NonNull String modelDescription) {
        this.modelDescription = modelDescription;
        return this;
    }

    /**
     * Gets or sets the model description.
     * 
     * @return modelDescription
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelDescription() {
        return modelDescription;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelDescription(@org.eclipse.jdt.annotation.NonNull String modelDescription) {
        this.modelDescription = modelDescription;
    }

    public DeviceIdentification modelUrl(@org.eclipse.jdt.annotation.NonNull String modelUrl) {
        this.modelUrl = modelUrl;
        return this;
    }

    /**
     * Gets or sets the model URL.
     * 
     * @return modelUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelUrl() {
        return modelUrl;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelUrl(@org.eclipse.jdt.annotation.NonNull String modelUrl) {
        this.modelUrl = modelUrl;
    }

    public DeviceIdentification manufacturer(@org.eclipse.jdt.annotation.NonNull String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    /**
     * Gets or sets the manufacturer.
     * 
     * @return manufacturer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MANUFACTURER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getManufacturer() {
        return manufacturer;
    }

    @JsonProperty(JSON_PROPERTY_MANUFACTURER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setManufacturer(@org.eclipse.jdt.annotation.NonNull String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public DeviceIdentification manufacturerUrl(@org.eclipse.jdt.annotation.NonNull String manufacturerUrl) {
        this.manufacturerUrl = manufacturerUrl;
        return this;
    }

    /**
     * Gets or sets the manufacturer URL.
     * 
     * @return manufacturerUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MANUFACTURER_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getManufacturerUrl() {
        return manufacturerUrl;
    }

    @JsonProperty(JSON_PROPERTY_MANUFACTURER_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setManufacturerUrl(@org.eclipse.jdt.annotation.NonNull String manufacturerUrl) {
        this.manufacturerUrl = manufacturerUrl;
    }

    public DeviceIdentification headers(@org.eclipse.jdt.annotation.NonNull List<HttpHeaderInfo> headers) {
        this.headers = headers;
        return this;
    }

    public DeviceIdentification addHeadersItem(HttpHeaderInfo headersItem) {
        if (this.headers == null) {
            this.headers = new ArrayList<>();
        }
        this.headers.add(headersItem);
        return this;
    }

    /**
     * Gets or sets the headers.
     * 
     * @return headers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HEADERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<HttpHeaderInfo> getHeaders() {
        return headers;
    }

    @JsonProperty(JSON_PROPERTY_HEADERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeaders(@org.eclipse.jdt.annotation.NonNull List<HttpHeaderInfo> headers) {
        this.headers = headers;
    }

    /**
     * Return true if this DeviceIdentification object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentification deviceIdentification = (DeviceIdentification) o;
        return Objects.equals(this.friendlyName, deviceIdentification.friendlyName)
                && Objects.equals(this.modelNumber, deviceIdentification.modelNumber)
                && Objects.equals(this.serialNumber, deviceIdentification.serialNumber)
                && Objects.equals(this.modelName, deviceIdentification.modelName)
                && Objects.equals(this.modelDescription, deviceIdentification.modelDescription)
                && Objects.equals(this.modelUrl, deviceIdentification.modelUrl)
                && Objects.equals(this.manufacturer, deviceIdentification.manufacturer)
                && Objects.equals(this.manufacturerUrl, deviceIdentification.manufacturerUrl)
                && Objects.equals(this.headers, deviceIdentification.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendlyName, modelNumber, serialNumber, modelName, modelDescription, modelUrl,
                manufacturer, manufacturerUrl, headers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceIdentification {\n");
        sb.append("    friendlyName: ").append(toIndentedString(friendlyName)).append("\n");
        sb.append("    modelNumber: ").append(toIndentedString(modelNumber)).append("\n");
        sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    modelDescription: ").append(toIndentedString(modelDescription)).append("\n");
        sb.append("    modelUrl: ").append(toIndentedString(modelUrl)).append("\n");
        sb.append("    manufacturer: ").append(toIndentedString(manufacturer)).append("\n");
        sb.append("    manufacturerUrl: ").append(toIndentedString(manufacturerUrl)).append("\n");
        sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
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

        // add `FriendlyName` to the URL query string
        if (getFriendlyName() != null) {
            joiner.add(String.format("%sFriendlyName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFriendlyName()))));
        }

        // add `ModelNumber` to the URL query string
        if (getModelNumber() != null) {
            joiner.add(String.format("%sModelNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelNumber()))));
        }

        // add `SerialNumber` to the URL query string
        if (getSerialNumber() != null) {
            joiner.add(String.format("%sSerialNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSerialNumber()))));
        }

        // add `ModelName` to the URL query string
        if (getModelName() != null) {
            joiner.add(String.format("%sModelName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelName()))));
        }

        // add `ModelDescription` to the URL query string
        if (getModelDescription() != null) {
            joiner.add(String.format("%sModelDescription%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelDescription()))));
        }

        // add `ModelUrl` to the URL query string
        if (getModelUrl() != null) {
            joiner.add(String.format("%sModelUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelUrl()))));
        }

        // add `Manufacturer` to the URL query string
        if (getManufacturer() != null) {
            joiner.add(String.format("%sManufacturer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getManufacturer()))));
        }

        // add `ManufacturerUrl` to the URL query string
        if (getManufacturerUrl() != null) {
            joiner.add(String.format("%sManufacturerUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getManufacturerUrl()))));
        }

        // add `Headers` to the URL query string
        if (getHeaders() != null) {
            for (int i = 0; i < getHeaders().size(); i++) {
                if (getHeaders().get(i) != null) {
                    joiner.add(getHeaders().get(i).toUrlQueryString(String.format("%sHeaders%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private DeviceIdentification instance;

        public Builder() {
            this(new DeviceIdentification());
        }

        protected Builder(DeviceIdentification instance) {
            this.instance = instance;
        }

        public DeviceIdentification.Builder friendlyName(String friendlyName) {
            this.instance.friendlyName = friendlyName;
            return this;
        }

        public DeviceIdentification.Builder modelNumber(String modelNumber) {
            this.instance.modelNumber = modelNumber;
            return this;
        }

        public DeviceIdentification.Builder serialNumber(String serialNumber) {
            this.instance.serialNumber = serialNumber;
            return this;
        }

        public DeviceIdentification.Builder modelName(String modelName) {
            this.instance.modelName = modelName;
            return this;
        }

        public DeviceIdentification.Builder modelDescription(String modelDescription) {
            this.instance.modelDescription = modelDescription;
            return this;
        }

        public DeviceIdentification.Builder modelUrl(String modelUrl) {
            this.instance.modelUrl = modelUrl;
            return this;
        }

        public DeviceIdentification.Builder manufacturer(String manufacturer) {
            this.instance.manufacturer = manufacturer;
            return this;
        }

        public DeviceIdentification.Builder manufacturerUrl(String manufacturerUrl) {
            this.instance.manufacturerUrl = manufacturerUrl;
            return this;
        }

        public DeviceIdentification.Builder headers(List<HttpHeaderInfo> headers) {
            this.instance.headers = headers;
            return this;
        }

        /**
         * returns a built DeviceIdentification instance.
         *
         * The builder is not reusable.
         */
        public DeviceIdentification build() {
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
    public static DeviceIdentification.Builder builder() {
        return new DeviceIdentification.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DeviceIdentification.Builder toBuilder() {
        return new DeviceIdentification.Builder().friendlyName(getFriendlyName()).modelNumber(getModelNumber())
                .serialNumber(getSerialNumber()).modelName(getModelName()).modelDescription(getModelDescription())
                .modelUrl(getModelUrl()).manufacturer(getManufacturer()).manufacturerUrl(getManufacturerUrl())
                .headers(getHeaders());
    }
}

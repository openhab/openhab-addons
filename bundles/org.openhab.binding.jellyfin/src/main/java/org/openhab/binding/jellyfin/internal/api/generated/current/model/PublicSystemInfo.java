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
 * PublicSystemInfo
 */
@JsonPropertyOrder({ PublicSystemInfo.JSON_PROPERTY_LOCAL_ADDRESS, PublicSystemInfo.JSON_PROPERTY_SERVER_NAME,
        PublicSystemInfo.JSON_PROPERTY_VERSION, PublicSystemInfo.JSON_PROPERTY_PRODUCT_NAME,
        PublicSystemInfo.JSON_PROPERTY_OPERATING_SYSTEM, PublicSystemInfo.JSON_PROPERTY_ID,
        PublicSystemInfo.JSON_PROPERTY_STARTUP_WIZARD_COMPLETED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PublicSystemInfo {
    public static final String JSON_PROPERTY_LOCAL_ADDRESS = "LocalAddress";
    @org.eclipse.jdt.annotation.NonNull
    private String localAddress;

    public static final String JSON_PROPERTY_SERVER_NAME = "ServerName";
    @org.eclipse.jdt.annotation.NonNull
    private String serverName;

    public static final String JSON_PROPERTY_VERSION = "Version";
    @org.eclipse.jdt.annotation.NonNull
    private String version;

    public static final String JSON_PROPERTY_PRODUCT_NAME = "ProductName";
    @org.eclipse.jdt.annotation.NonNull
    private String productName;

    public static final String JSON_PROPERTY_OPERATING_SYSTEM = "OperatingSystem";
    @org.eclipse.jdt.annotation.NonNull
    private String operatingSystem;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_STARTUP_WIZARD_COMPLETED = "StartupWizardCompleted";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean startupWizardCompleted;

    public PublicSystemInfo() {
    }

    public PublicSystemInfo localAddress(@org.eclipse.jdt.annotation.NonNull String localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    /**
     * Gets or sets the local address.
     * 
     * @return localAddress
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOCAL_ADDRESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLocalAddress() {
        return localAddress;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCAL_ADDRESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalAddress(@org.eclipse.jdt.annotation.NonNull String localAddress) {
        this.localAddress = localAddress;
    }

    public PublicSystemInfo serverName(@org.eclipse.jdt.annotation.NonNull String serverName) {
        this.serverName = serverName;
        return this;
    }

    /**
     * Gets or sets the name of the server.
     * 
     * @return serverName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERVER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerName() {
        return serverName;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerName(@org.eclipse.jdt.annotation.NonNull String serverName) {
        this.serverName = serverName;
    }

    public PublicSystemInfo version(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets or sets the server version.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersion() {
        return version;
    }

    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersion(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
    }

    public PublicSystemInfo productName(@org.eclipse.jdt.annotation.NonNull String productName) {
        this.productName = productName;
        return this;
    }

    /**
     * Gets or sets the product name. This is the AssemblyProduct name.
     * 
     * @return productName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRODUCT_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProductName() {
        return productName;
    }

    @JsonProperty(value = JSON_PROPERTY_PRODUCT_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProductName(@org.eclipse.jdt.annotation.NonNull String productName) {
        this.productName = productName;
    }

    public PublicSystemInfo operatingSystem(@org.eclipse.jdt.annotation.NonNull String operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    /**
     * Gets or sets the operating system.
     * 
     * @return operatingSystem
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OPERATING_SYSTEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOperatingSystem() {
        return operatingSystem;
    }

    @JsonProperty(value = JSON_PROPERTY_OPERATING_SYSTEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOperatingSystem(@org.eclipse.jdt.annotation.NonNull String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public PublicSystemInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
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
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public PublicSystemInfo startupWizardCompleted(@org.eclipse.jdt.annotation.NonNull Boolean startupWizardCompleted) {
        this.startupWizardCompleted = startupWizardCompleted;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the startup wizard is completed.
     * 
     * @return startupWizardCompleted
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STARTUP_WIZARD_COMPLETED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getStartupWizardCompleted() {
        return startupWizardCompleted;
    }

    @JsonProperty(value = JSON_PROPERTY_STARTUP_WIZARD_COMPLETED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartupWizardCompleted(@org.eclipse.jdt.annotation.NonNull Boolean startupWizardCompleted) {
        this.startupWizardCompleted = startupWizardCompleted;
    }

    /**
     * Return true if this PublicSystemInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PublicSystemInfo publicSystemInfo = (PublicSystemInfo) o;
        return Objects.equals(this.localAddress, publicSystemInfo.localAddress)
                && Objects.equals(this.serverName, publicSystemInfo.serverName)
                && Objects.equals(this.version, publicSystemInfo.version)
                && Objects.equals(this.productName, publicSystemInfo.productName)
                && Objects.equals(this.operatingSystem, publicSystemInfo.operatingSystem)
                && Objects.equals(this.id, publicSystemInfo.id)
                && Objects.equals(this.startupWizardCompleted, publicSystemInfo.startupWizardCompleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localAddress, serverName, version, productName, operatingSystem, id,
                startupWizardCompleted);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PublicSystemInfo {\n");
        sb.append("    localAddress: ").append(toIndentedString(localAddress)).append("\n");
        sb.append("    serverName: ").append(toIndentedString(serverName)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    productName: ").append(toIndentedString(productName)).append("\n");
        sb.append("    operatingSystem: ").append(toIndentedString(operatingSystem)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    startupWizardCompleted: ").append(toIndentedString(startupWizardCompleted)).append("\n");
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

        // add `LocalAddress` to the URL query string
        if (getLocalAddress() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLocalAddress%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocalAddress()))));
        }

        // add `ServerName` to the URL query string
        if (getServerName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sServerName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerName()))));
        }

        // add `Version` to the URL query string
        if (getVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
        }

        // add `ProductName` to the URL query string
        if (getProductName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProductName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProductName()))));
        }

        // add `OperatingSystem` to the URL query string
        if (getOperatingSystem() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOperatingSystem%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOperatingSystem()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `StartupWizardCompleted` to the URL query string
        if (getStartupWizardCompleted() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStartupWizardCompleted%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartupWizardCompleted()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PublicSystemInfo instance;

        public Builder() {
            this(new PublicSystemInfo());
        }

        protected Builder(PublicSystemInfo instance) {
            this.instance = instance;
        }

        public PublicSystemInfo.Builder localAddress(String localAddress) {
            this.instance.localAddress = localAddress;
            return this;
        }

        public PublicSystemInfo.Builder serverName(String serverName) {
            this.instance.serverName = serverName;
            return this;
        }

        public PublicSystemInfo.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public PublicSystemInfo.Builder productName(String productName) {
            this.instance.productName = productName;
            return this;
        }

        public PublicSystemInfo.Builder operatingSystem(String operatingSystem) {
            this.instance.operatingSystem = operatingSystem;
            return this;
        }

        public PublicSystemInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public PublicSystemInfo.Builder startupWizardCompleted(Boolean startupWizardCompleted) {
            this.instance.startupWizardCompleted = startupWizardCompleted;
            return this;
        }

        /**
         * returns a built PublicSystemInfo instance.
         *
         * The builder is not reusable.
         */
        public PublicSystemInfo build() {
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
    public static PublicSystemInfo.Builder builder() {
        return new PublicSystemInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PublicSystemInfo.Builder toBuilder() {
        return new PublicSystemInfo.Builder().localAddress(getLocalAddress()).serverName(getServerName())
                .version(getVersion()).productName(getProductName()).operatingSystem(getOperatingSystem()).id(getId())
                .startupWizardCompleted(getStartupWizardCompleted());
    }
}

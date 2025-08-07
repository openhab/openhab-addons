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
 * PublicSystemInfo
 */
@JsonPropertyOrder({ PublicSystemInfo.JSON_PROPERTY_LOCAL_ADDRESS, PublicSystemInfo.JSON_PROPERTY_SERVER_NAME,
        PublicSystemInfo.JSON_PROPERTY_VERSION, PublicSystemInfo.JSON_PROPERTY_PRODUCT_NAME,
        PublicSystemInfo.JSON_PROPERTY_OPERATING_SYSTEM, PublicSystemInfo.JSON_PROPERTY_ID,
        PublicSystemInfo.JSON_PROPERTY_STARTUP_WIZARD_COMPLETED })

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
    @Deprecated
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
    @JsonProperty(JSON_PROPERTY_LOCAL_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLocalAddress() {
        return localAddress;
    }

    @JsonProperty(JSON_PROPERTY_LOCAL_ADDRESS)
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
    @JsonProperty(JSON_PROPERTY_SERVER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getServerName() {
        return serverName;
    }

    @JsonProperty(JSON_PROPERTY_SERVER_NAME)
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
    @JsonProperty(JSON_PROPERTY_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getVersion() {
        return version;
    }

    @JsonProperty(JSON_PROPERTY_VERSION)
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
    @JsonProperty(JSON_PROPERTY_PRODUCT_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProductName() {
        return productName;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCT_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProductName(@org.eclipse.jdt.annotation.NonNull String productName) {
        this.productName = productName;
    }

    @Deprecated
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
    @JsonProperty(JSON_PROPERTY_OPERATING_SYSTEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOperatingSystem() {
        return operatingSystem;
    }

    @Deprecated
    @JsonProperty(JSON_PROPERTY_OPERATING_SYSTEM)
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
    @JsonProperty(JSON_PROPERTY_STARTUP_WIZARD_COMPLETED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getStartupWizardCompleted() {
        return startupWizardCompleted;
    }

    @JsonProperty(JSON_PROPERTY_STARTUP_WIZARD_COMPLETED)
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
}

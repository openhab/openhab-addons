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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class SystemInfo.
 */
@JsonPropertyOrder({ SystemInfo.JSON_PROPERTY_LOCAL_ADDRESS, SystemInfo.JSON_PROPERTY_SERVER_NAME,
        SystemInfo.JSON_PROPERTY_VERSION, SystemInfo.JSON_PROPERTY_PRODUCT_NAME,
        SystemInfo.JSON_PROPERTY_OPERATING_SYSTEM, SystemInfo.JSON_PROPERTY_ID,
        SystemInfo.JSON_PROPERTY_STARTUP_WIZARD_COMPLETED, SystemInfo.JSON_PROPERTY_OPERATING_SYSTEM_DISPLAY_NAME,
        SystemInfo.JSON_PROPERTY_PACKAGE_NAME, SystemInfo.JSON_PROPERTY_HAS_PENDING_RESTART,
        SystemInfo.JSON_PROPERTY_IS_SHUTTING_DOWN, SystemInfo.JSON_PROPERTY_SUPPORTS_LIBRARY_MONITOR,
        SystemInfo.JSON_PROPERTY_WEB_SOCKET_PORT_NUMBER, SystemInfo.JSON_PROPERTY_COMPLETED_INSTALLATIONS,
        SystemInfo.JSON_PROPERTY_CAN_SELF_RESTART, SystemInfo.JSON_PROPERTY_CAN_LAUNCH_WEB_BROWSER,
        SystemInfo.JSON_PROPERTY_PROGRAM_DATA_PATH, SystemInfo.JSON_PROPERTY_WEB_PATH,
        SystemInfo.JSON_PROPERTY_ITEMS_BY_NAME_PATH, SystemInfo.JSON_PROPERTY_CACHE_PATH,
        SystemInfo.JSON_PROPERTY_LOG_PATH, SystemInfo.JSON_PROPERTY_INTERNAL_METADATA_PATH,
        SystemInfo.JSON_PROPERTY_TRANSCODING_TEMP_PATH, SystemInfo.JSON_PROPERTY_CAST_RECEIVER_APPLICATIONS,
        SystemInfo.JSON_PROPERTY_HAS_UPDATE_AVAILABLE, SystemInfo.JSON_PROPERTY_ENCODER_LOCATION,
        SystemInfo.JSON_PROPERTY_SYSTEM_ARCHITECTURE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SystemInfo {
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

    public static final String JSON_PROPERTY_OPERATING_SYSTEM_DISPLAY_NAME = "OperatingSystemDisplayName";
    @org.eclipse.jdt.annotation.NonNull
    private String operatingSystemDisplayName;

    public static final String JSON_PROPERTY_PACKAGE_NAME = "PackageName";
    @org.eclipse.jdt.annotation.NonNull
    private String packageName;

    public static final String JSON_PROPERTY_HAS_PENDING_RESTART = "HasPendingRestart";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasPendingRestart;

    public static final String JSON_PROPERTY_IS_SHUTTING_DOWN = "IsShuttingDown";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isShuttingDown;

    public static final String JSON_PROPERTY_SUPPORTS_LIBRARY_MONITOR = "SupportsLibraryMonitor";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsLibraryMonitor;

    public static final String JSON_PROPERTY_WEB_SOCKET_PORT_NUMBER = "WebSocketPortNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer webSocketPortNumber;

    public static final String JSON_PROPERTY_COMPLETED_INSTALLATIONS = "CompletedInstallations";
    @org.eclipse.jdt.annotation.NonNull
    private List<InstallationInfo> completedInstallations;

    public static final String JSON_PROPERTY_CAN_SELF_RESTART = "CanSelfRestart";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canSelfRestart = true;

    public static final String JSON_PROPERTY_CAN_LAUNCH_WEB_BROWSER = "CanLaunchWebBrowser";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canLaunchWebBrowser = false;

    public static final String JSON_PROPERTY_PROGRAM_DATA_PATH = "ProgramDataPath";
    @org.eclipse.jdt.annotation.NonNull
    private String programDataPath;

    public static final String JSON_PROPERTY_WEB_PATH = "WebPath";
    @org.eclipse.jdt.annotation.NonNull
    private String webPath;

    public static final String JSON_PROPERTY_ITEMS_BY_NAME_PATH = "ItemsByNamePath";
    @org.eclipse.jdt.annotation.NonNull
    private String itemsByNamePath;

    public static final String JSON_PROPERTY_CACHE_PATH = "CachePath";
    @org.eclipse.jdt.annotation.NonNull
    private String cachePath;

    public static final String JSON_PROPERTY_LOG_PATH = "LogPath";
    @org.eclipse.jdt.annotation.NonNull
    private String logPath;

    public static final String JSON_PROPERTY_INTERNAL_METADATA_PATH = "InternalMetadataPath";
    @org.eclipse.jdt.annotation.NonNull
    private String internalMetadataPath;

    public static final String JSON_PROPERTY_TRANSCODING_TEMP_PATH = "TranscodingTempPath";
    @org.eclipse.jdt.annotation.NonNull
    private String transcodingTempPath;

    public static final String JSON_PROPERTY_CAST_RECEIVER_APPLICATIONS = "CastReceiverApplications";
    @org.eclipse.jdt.annotation.NonNull
    private List<CastReceiverApplication> castReceiverApplications;

    public static final String JSON_PROPERTY_HAS_UPDATE_AVAILABLE = "HasUpdateAvailable";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasUpdateAvailable = false;

    public static final String JSON_PROPERTY_ENCODER_LOCATION = "EncoderLocation";
    @org.eclipse.jdt.annotation.NonNull
    private String encoderLocation = "System";

    public static final String JSON_PROPERTY_SYSTEM_ARCHITECTURE = "SystemArchitecture";
    @org.eclipse.jdt.annotation.NonNull
    private String systemArchitecture = "X64";

    public SystemInfo() {
    }

    public SystemInfo localAddress(@org.eclipse.jdt.annotation.NonNull String localAddress) {
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

    public SystemInfo serverName(@org.eclipse.jdt.annotation.NonNull String serverName) {
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

    public SystemInfo version(@org.eclipse.jdt.annotation.NonNull String version) {
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

    public SystemInfo productName(@org.eclipse.jdt.annotation.NonNull String productName) {
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

    public SystemInfo operatingSystem(@org.eclipse.jdt.annotation.NonNull String operatingSystem) {
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

    public SystemInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
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

    public SystemInfo startupWizardCompleted(@org.eclipse.jdt.annotation.NonNull Boolean startupWizardCompleted) {
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

    public SystemInfo operatingSystemDisplayName(
            @org.eclipse.jdt.annotation.NonNull String operatingSystemDisplayName) {
        this.operatingSystemDisplayName = operatingSystemDisplayName;
        return this;
    }

    /**
     * Gets or sets the display name of the operating system.
     * 
     * @return operatingSystemDisplayName
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OPERATING_SYSTEM_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOperatingSystemDisplayName() {
        return operatingSystemDisplayName;
    }

    @JsonProperty(value = JSON_PROPERTY_OPERATING_SYSTEM_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOperatingSystemDisplayName(@org.eclipse.jdt.annotation.NonNull String operatingSystemDisplayName) {
        this.operatingSystemDisplayName = operatingSystemDisplayName;
    }

    public SystemInfo packageName(@org.eclipse.jdt.annotation.NonNull String packageName) {
        this.packageName = packageName;
        return this;
    }

    /**
     * Gets or sets the package name.
     * 
     * @return packageName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PACKAGE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPackageName() {
        return packageName;
    }

    @JsonProperty(value = JSON_PROPERTY_PACKAGE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPackageName(@org.eclipse.jdt.annotation.NonNull String packageName) {
        this.packageName = packageName;
    }

    public SystemInfo hasPendingRestart(@org.eclipse.jdt.annotation.NonNull Boolean hasPendingRestart) {
        this.hasPendingRestart = hasPendingRestart;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance has pending restart.
     * 
     * @return hasPendingRestart
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HAS_PENDING_RESTART, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasPendingRestart() {
        return hasPendingRestart;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_PENDING_RESTART, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasPendingRestart(@org.eclipse.jdt.annotation.NonNull Boolean hasPendingRestart) {
        this.hasPendingRestart = hasPendingRestart;
    }

    public SystemInfo isShuttingDown(@org.eclipse.jdt.annotation.NonNull Boolean isShuttingDown) {
        this.isShuttingDown = isShuttingDown;
        return this;
    }

    /**
     * Get isShuttingDown
     * 
     * @return isShuttingDown
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_SHUTTING_DOWN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsShuttingDown() {
        return isShuttingDown;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_SHUTTING_DOWN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsShuttingDown(@org.eclipse.jdt.annotation.NonNull Boolean isShuttingDown) {
        this.isShuttingDown = isShuttingDown;
    }

    public SystemInfo supportsLibraryMonitor(@org.eclipse.jdt.annotation.NonNull Boolean supportsLibraryMonitor) {
        this.supportsLibraryMonitor = supportsLibraryMonitor;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [supports library monitor].
     * 
     * @return supportsLibraryMonitor
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_LIBRARY_MONITOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsLibraryMonitor() {
        return supportsLibraryMonitor;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_LIBRARY_MONITOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsLibraryMonitor(@org.eclipse.jdt.annotation.NonNull Boolean supportsLibraryMonitor) {
        this.supportsLibraryMonitor = supportsLibraryMonitor;
    }

    public SystemInfo webSocketPortNumber(@org.eclipse.jdt.annotation.NonNull Integer webSocketPortNumber) {
        this.webSocketPortNumber = webSocketPortNumber;
        return this;
    }

    /**
     * Gets or sets the web socket port number.
     * 
     * @return webSocketPortNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_WEB_SOCKET_PORT_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getWebSocketPortNumber() {
        return webSocketPortNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_WEB_SOCKET_PORT_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWebSocketPortNumber(@org.eclipse.jdt.annotation.NonNull Integer webSocketPortNumber) {
        this.webSocketPortNumber = webSocketPortNumber;
    }

    public SystemInfo completedInstallations(
            @org.eclipse.jdt.annotation.NonNull List<InstallationInfo> completedInstallations) {
        this.completedInstallations = completedInstallations;
        return this;
    }

    public SystemInfo addCompletedInstallationsItem(InstallationInfo completedInstallationsItem) {
        if (this.completedInstallations == null) {
            this.completedInstallations = new ArrayList<>();
        }
        this.completedInstallations.add(completedInstallationsItem);
        return this;
    }

    /**
     * Gets or sets the completed installations.
     * 
     * @return completedInstallations
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_COMPLETED_INSTALLATIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<InstallationInfo> getCompletedInstallations() {
        return completedInstallations;
    }

    @JsonProperty(value = JSON_PROPERTY_COMPLETED_INSTALLATIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCompletedInstallations(
            @org.eclipse.jdt.annotation.NonNull List<InstallationInfo> completedInstallations) {
        this.completedInstallations = completedInstallations;
    }

    public SystemInfo canSelfRestart(@org.eclipse.jdt.annotation.NonNull Boolean canSelfRestart) {
        this.canSelfRestart = canSelfRestart;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can self restart.
     * 
     * @return canSelfRestart
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_SELF_RESTART, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanSelfRestart() {
        return canSelfRestart;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_SELF_RESTART, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanSelfRestart(@org.eclipse.jdt.annotation.NonNull Boolean canSelfRestart) {
        this.canSelfRestart = canSelfRestart;
    }

    public SystemInfo canLaunchWebBrowser(@org.eclipse.jdt.annotation.NonNull Boolean canLaunchWebBrowser) {
        this.canLaunchWebBrowser = canLaunchWebBrowser;
        return this;
    }

    /**
     * Get canLaunchWebBrowser
     * 
     * @return canLaunchWebBrowser
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_LAUNCH_WEB_BROWSER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanLaunchWebBrowser() {
        return canLaunchWebBrowser;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_LAUNCH_WEB_BROWSER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanLaunchWebBrowser(@org.eclipse.jdt.annotation.NonNull Boolean canLaunchWebBrowser) {
        this.canLaunchWebBrowser = canLaunchWebBrowser;
    }

    public SystemInfo programDataPath(@org.eclipse.jdt.annotation.NonNull String programDataPath) {
        this.programDataPath = programDataPath;
        return this;
    }

    /**
     * Gets or sets the program data path.
     * 
     * @return programDataPath
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROGRAM_DATA_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProgramDataPath() {
        return programDataPath;
    }

    @JsonProperty(value = JSON_PROPERTY_PROGRAM_DATA_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramDataPath(@org.eclipse.jdt.annotation.NonNull String programDataPath) {
        this.programDataPath = programDataPath;
    }

    public SystemInfo webPath(@org.eclipse.jdt.annotation.NonNull String webPath) {
        this.webPath = webPath;
        return this;
    }

    /**
     * Gets or sets the web UI resources path.
     * 
     * @return webPath
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_WEB_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getWebPath() {
        return webPath;
    }

    @JsonProperty(value = JSON_PROPERTY_WEB_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWebPath(@org.eclipse.jdt.annotation.NonNull String webPath) {
        this.webPath = webPath;
    }

    public SystemInfo itemsByNamePath(@org.eclipse.jdt.annotation.NonNull String itemsByNamePath) {
        this.itemsByNamePath = itemsByNamePath;
        return this;
    }

    /**
     * Gets or sets the items by name path.
     * 
     * @return itemsByNamePath
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEMS_BY_NAME_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getItemsByNamePath() {
        return itemsByNamePath;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEMS_BY_NAME_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemsByNamePath(@org.eclipse.jdt.annotation.NonNull String itemsByNamePath) {
        this.itemsByNamePath = itemsByNamePath;
    }

    public SystemInfo cachePath(@org.eclipse.jdt.annotation.NonNull String cachePath) {
        this.cachePath = cachePath;
        return this;
    }

    /**
     * Gets or sets the cache path.
     * 
     * @return cachePath
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CACHE_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCachePath() {
        return cachePath;
    }

    @JsonProperty(value = JSON_PROPERTY_CACHE_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCachePath(@org.eclipse.jdt.annotation.NonNull String cachePath) {
        this.cachePath = cachePath;
    }

    public SystemInfo logPath(@org.eclipse.jdt.annotation.NonNull String logPath) {
        this.logPath = logPath;
        return this;
    }

    /**
     * Gets or sets the log path.
     * 
     * @return logPath
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOG_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLogPath() {
        return logPath;
    }

    @JsonProperty(value = JSON_PROPERTY_LOG_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLogPath(@org.eclipse.jdt.annotation.NonNull String logPath) {
        this.logPath = logPath;
    }

    public SystemInfo internalMetadataPath(@org.eclipse.jdt.annotation.NonNull String internalMetadataPath) {
        this.internalMetadataPath = internalMetadataPath;
        return this;
    }

    /**
     * Gets or sets the internal metadata path.
     * 
     * @return internalMetadataPath
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INTERNAL_METADATA_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getInternalMetadataPath() {
        return internalMetadataPath;
    }

    @JsonProperty(value = JSON_PROPERTY_INTERNAL_METADATA_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInternalMetadataPath(@org.eclipse.jdt.annotation.NonNull String internalMetadataPath) {
        this.internalMetadataPath = internalMetadataPath;
    }

    public SystemInfo transcodingTempPath(@org.eclipse.jdt.annotation.NonNull String transcodingTempPath) {
        this.transcodingTempPath = transcodingTempPath;
        return this;
    }

    /**
     * Gets or sets the transcode path.
     * 
     * @return transcodingTempPath
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_TEMP_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTranscodingTempPath() {
        return transcodingTempPath;
    }

    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_TEMP_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingTempPath(@org.eclipse.jdt.annotation.NonNull String transcodingTempPath) {
        this.transcodingTempPath = transcodingTempPath;
    }

    public SystemInfo castReceiverApplications(
            @org.eclipse.jdt.annotation.NonNull List<CastReceiverApplication> castReceiverApplications) {
        this.castReceiverApplications = castReceiverApplications;
        return this;
    }

    public SystemInfo addCastReceiverApplicationsItem(CastReceiverApplication castReceiverApplicationsItem) {
        if (this.castReceiverApplications == null) {
            this.castReceiverApplications = new ArrayList<>();
        }
        this.castReceiverApplications.add(castReceiverApplicationsItem);
        return this;
    }

    /**
     * Gets or sets the list of cast receiver applications.
     * 
     * @return castReceiverApplications
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAST_RECEIVER_APPLICATIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<CastReceiverApplication> getCastReceiverApplications() {
        return castReceiverApplications;
    }

    @JsonProperty(value = JSON_PROPERTY_CAST_RECEIVER_APPLICATIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCastReceiverApplications(
            @org.eclipse.jdt.annotation.NonNull List<CastReceiverApplication> castReceiverApplications) {
        this.castReceiverApplications = castReceiverApplications;
    }

    public SystemInfo hasUpdateAvailable(@org.eclipse.jdt.annotation.NonNull Boolean hasUpdateAvailable) {
        this.hasUpdateAvailable = hasUpdateAvailable;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance has update available.
     * 
     * @return hasUpdateAvailable
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HAS_UPDATE_AVAILABLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasUpdateAvailable() {
        return hasUpdateAvailable;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_UPDATE_AVAILABLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasUpdateAvailable(@org.eclipse.jdt.annotation.NonNull Boolean hasUpdateAvailable) {
        this.hasUpdateAvailable = hasUpdateAvailable;
    }

    public SystemInfo encoderLocation(@org.eclipse.jdt.annotation.NonNull String encoderLocation) {
        this.encoderLocation = encoderLocation;
        return this;
    }

    /**
     * Get encoderLocation
     * 
     * @return encoderLocation
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENCODER_LOCATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEncoderLocation() {
        return encoderLocation;
    }

    @JsonProperty(value = JSON_PROPERTY_ENCODER_LOCATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderLocation(@org.eclipse.jdt.annotation.NonNull String encoderLocation) {
        this.encoderLocation = encoderLocation;
    }

    public SystemInfo systemArchitecture(@org.eclipse.jdt.annotation.NonNull String systemArchitecture) {
        this.systemArchitecture = systemArchitecture;
        return this;
    }

    /**
     * Get systemArchitecture
     * 
     * @return systemArchitecture
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SYSTEM_ARCHITECTURE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSystemArchitecture() {
        return systemArchitecture;
    }

    @JsonProperty(value = JSON_PROPERTY_SYSTEM_ARCHITECTURE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSystemArchitecture(@org.eclipse.jdt.annotation.NonNull String systemArchitecture) {
        this.systemArchitecture = systemArchitecture;
    }

    /**
     * Return true if this SystemInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SystemInfo systemInfo = (SystemInfo) o;
        return Objects.equals(this.localAddress, systemInfo.localAddress)
                && Objects.equals(this.serverName, systemInfo.serverName)
                && Objects.equals(this.version, systemInfo.version)
                && Objects.equals(this.productName, systemInfo.productName)
                && Objects.equals(this.operatingSystem, systemInfo.operatingSystem)
                && Objects.equals(this.id, systemInfo.id)
                && Objects.equals(this.startupWizardCompleted, systemInfo.startupWizardCompleted)
                && Objects.equals(this.operatingSystemDisplayName, systemInfo.operatingSystemDisplayName)
                && Objects.equals(this.packageName, systemInfo.packageName)
                && Objects.equals(this.hasPendingRestart, systemInfo.hasPendingRestart)
                && Objects.equals(this.isShuttingDown, systemInfo.isShuttingDown)
                && Objects.equals(this.supportsLibraryMonitor, systemInfo.supportsLibraryMonitor)
                && Objects.equals(this.webSocketPortNumber, systemInfo.webSocketPortNumber)
                && Objects.equals(this.completedInstallations, systemInfo.completedInstallations)
                && Objects.equals(this.canSelfRestart, systemInfo.canSelfRestart)
                && Objects.equals(this.canLaunchWebBrowser, systemInfo.canLaunchWebBrowser)
                && Objects.equals(this.programDataPath, systemInfo.programDataPath)
                && Objects.equals(this.webPath, systemInfo.webPath)
                && Objects.equals(this.itemsByNamePath, systemInfo.itemsByNamePath)
                && Objects.equals(this.cachePath, systemInfo.cachePath)
                && Objects.equals(this.logPath, systemInfo.logPath)
                && Objects.equals(this.internalMetadataPath, systemInfo.internalMetadataPath)
                && Objects.equals(this.transcodingTempPath, systemInfo.transcodingTempPath)
                && Objects.equals(this.castReceiverApplications, systemInfo.castReceiverApplications)
                && Objects.equals(this.hasUpdateAvailable, systemInfo.hasUpdateAvailable)
                && Objects.equals(this.encoderLocation, systemInfo.encoderLocation)
                && Objects.equals(this.systemArchitecture, systemInfo.systemArchitecture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localAddress, serverName, version, productName, operatingSystem, id, startupWizardCompleted,
                operatingSystemDisplayName, packageName, hasPendingRestart, isShuttingDown, supportsLibraryMonitor,
                webSocketPortNumber, completedInstallations, canSelfRestart, canLaunchWebBrowser, programDataPath,
                webPath, itemsByNamePath, cachePath, logPath, internalMetadataPath, transcodingTempPath,
                castReceiverApplications, hasUpdateAvailable, encoderLocation, systemArchitecture);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SystemInfo {\n");
        sb.append("    localAddress: ").append(toIndentedString(localAddress)).append("\n");
        sb.append("    serverName: ").append(toIndentedString(serverName)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    productName: ").append(toIndentedString(productName)).append("\n");
        sb.append("    operatingSystem: ").append(toIndentedString(operatingSystem)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    startupWizardCompleted: ").append(toIndentedString(startupWizardCompleted)).append("\n");
        sb.append("    operatingSystemDisplayName: ").append(toIndentedString(operatingSystemDisplayName)).append("\n");
        sb.append("    packageName: ").append(toIndentedString(packageName)).append("\n");
        sb.append("    hasPendingRestart: ").append(toIndentedString(hasPendingRestart)).append("\n");
        sb.append("    isShuttingDown: ").append(toIndentedString(isShuttingDown)).append("\n");
        sb.append("    supportsLibraryMonitor: ").append(toIndentedString(supportsLibraryMonitor)).append("\n");
        sb.append("    webSocketPortNumber: ").append(toIndentedString(webSocketPortNumber)).append("\n");
        sb.append("    completedInstallations: ").append(toIndentedString(completedInstallations)).append("\n");
        sb.append("    canSelfRestart: ").append(toIndentedString(canSelfRestart)).append("\n");
        sb.append("    canLaunchWebBrowser: ").append(toIndentedString(canLaunchWebBrowser)).append("\n");
        sb.append("    programDataPath: ").append(toIndentedString(programDataPath)).append("\n");
        sb.append("    webPath: ").append(toIndentedString(webPath)).append("\n");
        sb.append("    itemsByNamePath: ").append(toIndentedString(itemsByNamePath)).append("\n");
        sb.append("    cachePath: ").append(toIndentedString(cachePath)).append("\n");
        sb.append("    logPath: ").append(toIndentedString(logPath)).append("\n");
        sb.append("    internalMetadataPath: ").append(toIndentedString(internalMetadataPath)).append("\n");
        sb.append("    transcodingTempPath: ").append(toIndentedString(transcodingTempPath)).append("\n");
        sb.append("    castReceiverApplications: ").append(toIndentedString(castReceiverApplications)).append("\n");
        sb.append("    hasUpdateAvailable: ").append(toIndentedString(hasUpdateAvailable)).append("\n");
        sb.append("    encoderLocation: ").append(toIndentedString(encoderLocation)).append("\n");
        sb.append("    systemArchitecture: ").append(toIndentedString(systemArchitecture)).append("\n");
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

        // add `OperatingSystemDisplayName` to the URL query string
        if (getOperatingSystemDisplayName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOperatingSystemDisplayName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOperatingSystemDisplayName()))));
        }

        // add `PackageName` to the URL query string
        if (getPackageName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPackageName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPackageName()))));
        }

        // add `HasPendingRestart` to the URL query string
        if (getHasPendingRestart() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasPendingRestart%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasPendingRestart()))));
        }

        // add `IsShuttingDown` to the URL query string
        if (getIsShuttingDown() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsShuttingDown%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsShuttingDown()))));
        }

        // add `SupportsLibraryMonitor` to the URL query string
        if (getSupportsLibraryMonitor() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsLibraryMonitor%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsLibraryMonitor()))));
        }

        // add `WebSocketPortNumber` to the URL query string
        if (getWebSocketPortNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sWebSocketPortNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWebSocketPortNumber()))));
        }

        // add `CompletedInstallations` to the URL query string
        if (getCompletedInstallations() != null) {
            for (int i = 0; i < getCompletedInstallations().size(); i++) {
                if (getCompletedInstallations().get(i) != null) {
                    joiner.add(getCompletedInstallations().get(i).toUrlQueryString(String.format(Locale.ROOT,
                            "%sCompletedInstallations%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `CanSelfRestart` to the URL query string
        if (getCanSelfRestart() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanSelfRestart%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanSelfRestart()))));
        }

        // add `CanLaunchWebBrowser` to the URL query string
        if (getCanLaunchWebBrowser() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanLaunchWebBrowser%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanLaunchWebBrowser()))));
        }

        // add `ProgramDataPath` to the URL query string
        if (getProgramDataPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProgramDataPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProgramDataPath()))));
        }

        // add `WebPath` to the URL query string
        if (getWebPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sWebPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWebPath()))));
        }

        // add `ItemsByNamePath` to the URL query string
        if (getItemsByNamePath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sItemsByNamePath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemsByNamePath()))));
        }

        // add `CachePath` to the URL query string
        if (getCachePath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCachePath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCachePath()))));
        }

        // add `LogPath` to the URL query string
        if (getLogPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLogPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLogPath()))));
        }

        // add `InternalMetadataPath` to the URL query string
        if (getInternalMetadataPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sInternalMetadataPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getInternalMetadataPath()))));
        }

        // add `TranscodingTempPath` to the URL query string
        if (getTranscodingTempPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sTranscodingTempPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTranscodingTempPath()))));
        }

        // add `CastReceiverApplications` to the URL query string
        if (getCastReceiverApplications() != null) {
            for (int i = 0; i < getCastReceiverApplications().size(); i++) {
                if (getCastReceiverApplications().get(i) != null) {
                    joiner.add(getCastReceiverApplications().get(i).toUrlQueryString(String.format(Locale.ROOT,
                            "%sCastReceiverApplications%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `HasUpdateAvailable` to the URL query string
        if (getHasUpdateAvailable() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasUpdateAvailable%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasUpdateAvailable()))));
        }

        // add `EncoderLocation` to the URL query string
        if (getEncoderLocation() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEncoderLocation%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderLocation()))));
        }

        // add `SystemArchitecture` to the URL query string
        if (getSystemArchitecture() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSystemArchitecture%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSystemArchitecture()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SystemInfo instance;

        public Builder() {
            this(new SystemInfo());
        }

        protected Builder(SystemInfo instance) {
            this.instance = instance;
        }

        public SystemInfo.Builder localAddress(String localAddress) {
            this.instance.localAddress = localAddress;
            return this;
        }

        public SystemInfo.Builder serverName(String serverName) {
            this.instance.serverName = serverName;
            return this;
        }

        public SystemInfo.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public SystemInfo.Builder productName(String productName) {
            this.instance.productName = productName;
            return this;
        }

        public SystemInfo.Builder operatingSystem(String operatingSystem) {
            this.instance.operatingSystem = operatingSystem;
            return this;
        }

        public SystemInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public SystemInfo.Builder startupWizardCompleted(Boolean startupWizardCompleted) {
            this.instance.startupWizardCompleted = startupWizardCompleted;
            return this;
        }

        public SystemInfo.Builder operatingSystemDisplayName(String operatingSystemDisplayName) {
            this.instance.operatingSystemDisplayName = operatingSystemDisplayName;
            return this;
        }

        public SystemInfo.Builder packageName(String packageName) {
            this.instance.packageName = packageName;
            return this;
        }

        public SystemInfo.Builder hasPendingRestart(Boolean hasPendingRestart) {
            this.instance.hasPendingRestart = hasPendingRestart;
            return this;
        }

        public SystemInfo.Builder isShuttingDown(Boolean isShuttingDown) {
            this.instance.isShuttingDown = isShuttingDown;
            return this;
        }

        public SystemInfo.Builder supportsLibraryMonitor(Boolean supportsLibraryMonitor) {
            this.instance.supportsLibraryMonitor = supportsLibraryMonitor;
            return this;
        }

        public SystemInfo.Builder webSocketPortNumber(Integer webSocketPortNumber) {
            this.instance.webSocketPortNumber = webSocketPortNumber;
            return this;
        }

        public SystemInfo.Builder completedInstallations(List<InstallationInfo> completedInstallations) {
            this.instance.completedInstallations = completedInstallations;
            return this;
        }

        public SystemInfo.Builder canSelfRestart(Boolean canSelfRestart) {
            this.instance.canSelfRestart = canSelfRestart;
            return this;
        }

        public SystemInfo.Builder canLaunchWebBrowser(Boolean canLaunchWebBrowser) {
            this.instance.canLaunchWebBrowser = canLaunchWebBrowser;
            return this;
        }

        public SystemInfo.Builder programDataPath(String programDataPath) {
            this.instance.programDataPath = programDataPath;
            return this;
        }

        public SystemInfo.Builder webPath(String webPath) {
            this.instance.webPath = webPath;
            return this;
        }

        public SystemInfo.Builder itemsByNamePath(String itemsByNamePath) {
            this.instance.itemsByNamePath = itemsByNamePath;
            return this;
        }

        public SystemInfo.Builder cachePath(String cachePath) {
            this.instance.cachePath = cachePath;
            return this;
        }

        public SystemInfo.Builder logPath(String logPath) {
            this.instance.logPath = logPath;
            return this;
        }

        public SystemInfo.Builder internalMetadataPath(String internalMetadataPath) {
            this.instance.internalMetadataPath = internalMetadataPath;
            return this;
        }

        public SystemInfo.Builder transcodingTempPath(String transcodingTempPath) {
            this.instance.transcodingTempPath = transcodingTempPath;
            return this;
        }

        public SystemInfo.Builder castReceiverApplications(List<CastReceiverApplication> castReceiverApplications) {
            this.instance.castReceiverApplications = castReceiverApplications;
            return this;
        }

        public SystemInfo.Builder hasUpdateAvailable(Boolean hasUpdateAvailable) {
            this.instance.hasUpdateAvailable = hasUpdateAvailable;
            return this;
        }

        public SystemInfo.Builder encoderLocation(String encoderLocation) {
            this.instance.encoderLocation = encoderLocation;
            return this;
        }

        public SystemInfo.Builder systemArchitecture(String systemArchitecture) {
            this.instance.systemArchitecture = systemArchitecture;
            return this;
        }

        /**
         * returns a built SystemInfo instance.
         *
         * The builder is not reusable.
         */
        public SystemInfo build() {
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
    public static SystemInfo.Builder builder() {
        return new SystemInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SystemInfo.Builder toBuilder() {
        return new SystemInfo.Builder().localAddress(getLocalAddress()).serverName(getServerName())
                .version(getVersion()).productName(getProductName()).operatingSystem(getOperatingSystem()).id(getId())
                .startupWizardCompleted(getStartupWizardCompleted())
                .operatingSystemDisplayName(getOperatingSystemDisplayName()).packageName(getPackageName())
                .hasPendingRestart(getHasPendingRestart()).isShuttingDown(getIsShuttingDown())
                .supportsLibraryMonitor(getSupportsLibraryMonitor()).webSocketPortNumber(getWebSocketPortNumber())
                .completedInstallations(getCompletedInstallations()).canSelfRestart(getCanSelfRestart())
                .canLaunchWebBrowser(getCanLaunchWebBrowser()).programDataPath(getProgramDataPath())
                .webPath(getWebPath()).itemsByNamePath(getItemsByNamePath()).cachePath(getCachePath())
                .logPath(getLogPath()).internalMetadataPath(getInternalMetadataPath())
                .transcodingTempPath(getTranscodingTempPath()).castReceiverApplications(getCastReceiverApplications())
                .hasUpdateAvailable(getHasUpdateAvailable()).encoderLocation(getEncoderLocation())
                .systemArchitecture(getSystemArchitecture());
    }
}

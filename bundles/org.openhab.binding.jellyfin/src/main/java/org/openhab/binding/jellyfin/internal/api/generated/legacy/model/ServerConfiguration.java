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
 * Represents the server configuration.
 */
@JsonPropertyOrder({ ServerConfiguration.JSON_PROPERTY_LOG_FILE_RETENTION_DAYS,
        ServerConfiguration.JSON_PROPERTY_IS_STARTUP_WIZARD_COMPLETED, ServerConfiguration.JSON_PROPERTY_CACHE_PATH,
        ServerConfiguration.JSON_PROPERTY_PREVIOUS_VERSION, ServerConfiguration.JSON_PROPERTY_PREVIOUS_VERSION_STR,
        ServerConfiguration.JSON_PROPERTY_ENABLE_METRICS,
        ServerConfiguration.JSON_PROPERTY_ENABLE_NORMALIZED_ITEM_BY_NAME_IDS,
        ServerConfiguration.JSON_PROPERTY_IS_PORT_AUTHORIZED, ServerConfiguration.JSON_PROPERTY_QUICK_CONNECT_AVAILABLE,
        ServerConfiguration.JSON_PROPERTY_ENABLE_CASE_SENSITIVE_ITEM_IDS,
        ServerConfiguration.JSON_PROPERTY_DISABLE_LIVE_TV_CHANNEL_USER_DATA_NAME,
        ServerConfiguration.JSON_PROPERTY_METADATA_PATH, ServerConfiguration.JSON_PROPERTY_METADATA_NETWORK_PATH,
        ServerConfiguration.JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE,
        ServerConfiguration.JSON_PROPERTY_METADATA_COUNTRY_CODE,
        ServerConfiguration.JSON_PROPERTY_SORT_REPLACE_CHARACTERS,
        ServerConfiguration.JSON_PROPERTY_SORT_REMOVE_CHARACTERS, ServerConfiguration.JSON_PROPERTY_SORT_REMOVE_WORDS,
        ServerConfiguration.JSON_PROPERTY_MIN_RESUME_PCT, ServerConfiguration.JSON_PROPERTY_MAX_RESUME_PCT,
        ServerConfiguration.JSON_PROPERTY_MIN_RESUME_DURATION_SECONDS,
        ServerConfiguration.JSON_PROPERTY_MIN_AUDIOBOOK_RESUME, ServerConfiguration.JSON_PROPERTY_MAX_AUDIOBOOK_RESUME,
        ServerConfiguration.JSON_PROPERTY_LIBRARY_MONITOR_DELAY,
        ServerConfiguration.JSON_PROPERTY_IMAGE_SAVING_CONVENTION, ServerConfiguration.JSON_PROPERTY_METADATA_OPTIONS,
        ServerConfiguration.JSON_PROPERTY_SKIP_DESERIALIZATION_FOR_BASIC_TYPES,
        ServerConfiguration.JSON_PROPERTY_SERVER_NAME, ServerConfiguration.JSON_PROPERTY_UI_CULTURE,
        ServerConfiguration.JSON_PROPERTY_SAVE_METADATA_HIDDEN, ServerConfiguration.JSON_PROPERTY_CONTENT_TYPES,
        ServerConfiguration.JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT,
        ServerConfiguration.JSON_PROPERTY_ENABLE_FOLDER_VIEW,
        ServerConfiguration.JSON_PROPERTY_ENABLE_GROUPING_INTO_COLLECTIONS,
        ServerConfiguration.JSON_PROPERTY_DISPLAY_SPECIALS_WITHIN_SEASONS,
        ServerConfiguration.JSON_PROPERTY_CODECS_USED, ServerConfiguration.JSON_PROPERTY_PLUGIN_REPOSITORIES,
        ServerConfiguration.JSON_PROPERTY_ENABLE_EXTERNAL_CONTENT_IN_SUGGESTIONS,
        ServerConfiguration.JSON_PROPERTY_IMAGE_EXTRACTION_TIMEOUT_MS,
        ServerConfiguration.JSON_PROPERTY_PATH_SUBSTITUTIONS,
        ServerConfiguration.JSON_PROPERTY_ENABLE_SLOW_RESPONSE_WARNING,
        ServerConfiguration.JSON_PROPERTY_SLOW_RESPONSE_THRESHOLD_MS, ServerConfiguration.JSON_PROPERTY_CORS_HOSTS,
        ServerConfiguration.JSON_PROPERTY_ACTIVITY_LOG_RETENTION_DAYS,
        ServerConfiguration.JSON_PROPERTY_LIBRARY_SCAN_FANOUT_CONCURRENCY,
        ServerConfiguration.JSON_PROPERTY_LIBRARY_METADATA_REFRESH_CONCURRENCY,
        ServerConfiguration.JSON_PROPERTY_REMOVE_OLD_PLUGINS,
        ServerConfiguration.JSON_PROPERTY_ALLOW_CLIENT_LOG_UPLOAD })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ServerConfiguration {
    public static final String JSON_PROPERTY_LOG_FILE_RETENTION_DAYS = "LogFileRetentionDays";
    @org.eclipse.jdt.annotation.NonNull
    private Integer logFileRetentionDays;

    public static final String JSON_PROPERTY_IS_STARTUP_WIZARD_COMPLETED = "IsStartupWizardCompleted";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isStartupWizardCompleted;

    public static final String JSON_PROPERTY_CACHE_PATH = "CachePath";
    @org.eclipse.jdt.annotation.NonNull
    private String cachePath;

    public static final String JSON_PROPERTY_PREVIOUS_VERSION = "PreviousVersion";
    @org.eclipse.jdt.annotation.NonNull
    private String previousVersion;

    public static final String JSON_PROPERTY_PREVIOUS_VERSION_STR = "PreviousVersionStr";
    @org.eclipse.jdt.annotation.NonNull
    private String previousVersionStr;

    public static final String JSON_PROPERTY_ENABLE_METRICS = "EnableMetrics";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableMetrics;

    public static final String JSON_PROPERTY_ENABLE_NORMALIZED_ITEM_BY_NAME_IDS = "EnableNormalizedItemByNameIds";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableNormalizedItemByNameIds;

    public static final String JSON_PROPERTY_IS_PORT_AUTHORIZED = "IsPortAuthorized";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPortAuthorized;

    public static final String JSON_PROPERTY_QUICK_CONNECT_AVAILABLE = "QuickConnectAvailable";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean quickConnectAvailable;

    public static final String JSON_PROPERTY_ENABLE_CASE_SENSITIVE_ITEM_IDS = "EnableCaseSensitiveItemIds";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableCaseSensitiveItemIds;

    public static final String JSON_PROPERTY_DISABLE_LIVE_TV_CHANNEL_USER_DATA_NAME = "DisableLiveTvChannelUserDataName";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean disableLiveTvChannelUserDataName;

    public static final String JSON_PROPERTY_METADATA_PATH = "MetadataPath";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataPath;

    public static final String JSON_PROPERTY_METADATA_NETWORK_PATH = "MetadataNetworkPath";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataNetworkPath;

    public static final String JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE = "PreferredMetadataLanguage";
    @org.eclipse.jdt.annotation.NonNull
    private String preferredMetadataLanguage;

    public static final String JSON_PROPERTY_METADATA_COUNTRY_CODE = "MetadataCountryCode";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataCountryCode;

    public static final String JSON_PROPERTY_SORT_REPLACE_CHARACTERS = "SortReplaceCharacters";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> sortReplaceCharacters = new ArrayList<>();

    public static final String JSON_PROPERTY_SORT_REMOVE_CHARACTERS = "SortRemoveCharacters";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> sortRemoveCharacters = new ArrayList<>();

    public static final String JSON_PROPERTY_SORT_REMOVE_WORDS = "SortRemoveWords";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> sortRemoveWords = new ArrayList<>();

    public static final String JSON_PROPERTY_MIN_RESUME_PCT = "MinResumePct";
    @org.eclipse.jdt.annotation.NonNull
    private Integer minResumePct;

    public static final String JSON_PROPERTY_MAX_RESUME_PCT = "MaxResumePct";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxResumePct;

    public static final String JSON_PROPERTY_MIN_RESUME_DURATION_SECONDS = "MinResumeDurationSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer minResumeDurationSeconds;

    public static final String JSON_PROPERTY_MIN_AUDIOBOOK_RESUME = "MinAudiobookResume";
    @org.eclipse.jdt.annotation.NonNull
    private Integer minAudiobookResume;

    public static final String JSON_PROPERTY_MAX_AUDIOBOOK_RESUME = "MaxAudiobookResume";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxAudiobookResume;

    public static final String JSON_PROPERTY_LIBRARY_MONITOR_DELAY = "LibraryMonitorDelay";
    @org.eclipse.jdt.annotation.NonNull
    private Integer libraryMonitorDelay;

    public static final String JSON_PROPERTY_IMAGE_SAVING_CONVENTION = "ImageSavingConvention";
    @org.eclipse.jdt.annotation.NonNull
    private ImageSavingConvention imageSavingConvention;

    public static final String JSON_PROPERTY_METADATA_OPTIONS = "MetadataOptions";
    @org.eclipse.jdt.annotation.NonNull
    private List<MetadataOptions> metadataOptions = new ArrayList<>();

    public static final String JSON_PROPERTY_SKIP_DESERIALIZATION_FOR_BASIC_TYPES = "SkipDeserializationForBasicTypes";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean skipDeserializationForBasicTypes;

    public static final String JSON_PROPERTY_SERVER_NAME = "ServerName";
    @org.eclipse.jdt.annotation.NonNull
    private String serverName;

    public static final String JSON_PROPERTY_UI_CULTURE = "UICulture";
    @org.eclipse.jdt.annotation.NonNull
    private String uiCulture;

    public static final String JSON_PROPERTY_SAVE_METADATA_HIDDEN = "SaveMetadataHidden";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean saveMetadataHidden;

    public static final String JSON_PROPERTY_CONTENT_TYPES = "ContentTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameValuePair> contentTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT = "RemoteClientBitrateLimit";
    @org.eclipse.jdt.annotation.NonNull
    private Integer remoteClientBitrateLimit;

    public static final String JSON_PROPERTY_ENABLE_FOLDER_VIEW = "EnableFolderView";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableFolderView;

    public static final String JSON_PROPERTY_ENABLE_GROUPING_INTO_COLLECTIONS = "EnableGroupingIntoCollections";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableGroupingIntoCollections;

    public static final String JSON_PROPERTY_DISPLAY_SPECIALS_WITHIN_SEASONS = "DisplaySpecialsWithinSeasons";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean displaySpecialsWithinSeasons;

    public static final String JSON_PROPERTY_CODECS_USED = "CodecsUsed";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> codecsUsed = new ArrayList<>();

    public static final String JSON_PROPERTY_PLUGIN_REPOSITORIES = "PluginRepositories";
    @org.eclipse.jdt.annotation.NonNull
    private List<RepositoryInfo> pluginRepositories = new ArrayList<>();

    public static final String JSON_PROPERTY_ENABLE_EXTERNAL_CONTENT_IN_SUGGESTIONS = "EnableExternalContentInSuggestions";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableExternalContentInSuggestions;

    public static final String JSON_PROPERTY_IMAGE_EXTRACTION_TIMEOUT_MS = "ImageExtractionTimeoutMs";
    @org.eclipse.jdt.annotation.NonNull
    private Integer imageExtractionTimeoutMs;

    public static final String JSON_PROPERTY_PATH_SUBSTITUTIONS = "PathSubstitutions";
    @org.eclipse.jdt.annotation.NonNull
    private List<PathSubstitution> pathSubstitutions = new ArrayList<>();

    public static final String JSON_PROPERTY_ENABLE_SLOW_RESPONSE_WARNING = "EnableSlowResponseWarning";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSlowResponseWarning;

    public static final String JSON_PROPERTY_SLOW_RESPONSE_THRESHOLD_MS = "SlowResponseThresholdMs";
    @org.eclipse.jdt.annotation.NonNull
    private Long slowResponseThresholdMs;

    public static final String JSON_PROPERTY_CORS_HOSTS = "CorsHosts";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> corsHosts = new ArrayList<>();

    public static final String JSON_PROPERTY_ACTIVITY_LOG_RETENTION_DAYS = "ActivityLogRetentionDays";
    @org.eclipse.jdt.annotation.NonNull
    private Integer activityLogRetentionDays;

    public static final String JSON_PROPERTY_LIBRARY_SCAN_FANOUT_CONCURRENCY = "LibraryScanFanoutConcurrency";
    @org.eclipse.jdt.annotation.NonNull
    private Integer libraryScanFanoutConcurrency;

    public static final String JSON_PROPERTY_LIBRARY_METADATA_REFRESH_CONCURRENCY = "LibraryMetadataRefreshConcurrency";
    @org.eclipse.jdt.annotation.NonNull
    private Integer libraryMetadataRefreshConcurrency;

    public static final String JSON_PROPERTY_REMOVE_OLD_PLUGINS = "RemoveOldPlugins";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean removeOldPlugins;

    public static final String JSON_PROPERTY_ALLOW_CLIENT_LOG_UPLOAD = "AllowClientLogUpload";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean allowClientLogUpload;

    public ServerConfiguration() {
    }

    public ServerConfiguration logFileRetentionDays(@org.eclipse.jdt.annotation.NonNull Integer logFileRetentionDays) {
        this.logFileRetentionDays = logFileRetentionDays;
        return this;
    }

    /**
     * Gets or sets the number of days we should retain log files.
     * 
     * @return logFileRetentionDays
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LOG_FILE_RETENTION_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getLogFileRetentionDays() {
        return logFileRetentionDays;
    }

    @JsonProperty(JSON_PROPERTY_LOG_FILE_RETENTION_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLogFileRetentionDays(@org.eclipse.jdt.annotation.NonNull Integer logFileRetentionDays) {
        this.logFileRetentionDays = logFileRetentionDays;
    }

    public ServerConfiguration isStartupWizardCompleted(
            @org.eclipse.jdt.annotation.NonNull Boolean isStartupWizardCompleted) {
        this.isStartupWizardCompleted = isStartupWizardCompleted;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is first run.
     * 
     * @return isStartupWizardCompleted
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_STARTUP_WIZARD_COMPLETED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsStartupWizardCompleted() {
        return isStartupWizardCompleted;
    }

    @JsonProperty(JSON_PROPERTY_IS_STARTUP_WIZARD_COMPLETED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsStartupWizardCompleted(@org.eclipse.jdt.annotation.NonNull Boolean isStartupWizardCompleted) {
        this.isStartupWizardCompleted = isStartupWizardCompleted;
    }

    public ServerConfiguration cachePath(@org.eclipse.jdt.annotation.NonNull String cachePath) {
        this.cachePath = cachePath;
        return this;
    }

    /**
     * Gets or sets the cache path.
     * 
     * @return cachePath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CACHE_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCachePath() {
        return cachePath;
    }

    @JsonProperty(JSON_PROPERTY_CACHE_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCachePath(@org.eclipse.jdt.annotation.NonNull String cachePath) {
        this.cachePath = cachePath;
    }

    public ServerConfiguration previousVersion(@org.eclipse.jdt.annotation.NonNull String previousVersion) {
        this.previousVersion = previousVersion;
        return this;
    }

    /**
     * Gets or sets the last known version that was ran using the configuration.
     * 
     * @return previousVersion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREVIOUS_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreviousVersion() {
        return previousVersion;
    }

    @JsonProperty(JSON_PROPERTY_PREVIOUS_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreviousVersion(@org.eclipse.jdt.annotation.NonNull String previousVersion) {
        this.previousVersion = previousVersion;
    }

    public ServerConfiguration previousVersionStr(@org.eclipse.jdt.annotation.NonNull String previousVersionStr) {
        this.previousVersionStr = previousVersionStr;
        return this;
    }

    /**
     * Gets or sets the stringified PreviousVersion to be stored/loaded, because System.Version itself isn&#39;t
     * xml-serializable.
     * 
     * @return previousVersionStr
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREVIOUS_VERSION_STR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreviousVersionStr() {
        return previousVersionStr;
    }

    @JsonProperty(JSON_PROPERTY_PREVIOUS_VERSION_STR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreviousVersionStr(@org.eclipse.jdt.annotation.NonNull String previousVersionStr) {
        this.previousVersionStr = previousVersionStr;
    }

    public ServerConfiguration enableMetrics(@org.eclipse.jdt.annotation.NonNull Boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable prometheus metrics exporting.
     * 
     * @return enableMetrics
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_METRICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableMetrics() {
        return enableMetrics;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_METRICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableMetrics(@org.eclipse.jdt.annotation.NonNull Boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }

    public ServerConfiguration enableNormalizedItemByNameIds(
            @org.eclipse.jdt.annotation.NonNull Boolean enableNormalizedItemByNameIds) {
        this.enableNormalizedItemByNameIds = enableNormalizedItemByNameIds;
        return this;
    }

    /**
     * Get enableNormalizedItemByNameIds
     * 
     * @return enableNormalizedItemByNameIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_NORMALIZED_ITEM_BY_NAME_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableNormalizedItemByNameIds() {
        return enableNormalizedItemByNameIds;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_NORMALIZED_ITEM_BY_NAME_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableNormalizedItemByNameIds(
            @org.eclipse.jdt.annotation.NonNull Boolean enableNormalizedItemByNameIds) {
        this.enableNormalizedItemByNameIds = enableNormalizedItemByNameIds;
    }

    public ServerConfiguration isPortAuthorized(@org.eclipse.jdt.annotation.NonNull Boolean isPortAuthorized) {
        this.isPortAuthorized = isPortAuthorized;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is port authorized.
     * 
     * @return isPortAuthorized
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_PORT_AUTHORIZED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPortAuthorized() {
        return isPortAuthorized;
    }

    @JsonProperty(JSON_PROPERTY_IS_PORT_AUTHORIZED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPortAuthorized(@org.eclipse.jdt.annotation.NonNull Boolean isPortAuthorized) {
        this.isPortAuthorized = isPortAuthorized;
    }

    public ServerConfiguration quickConnectAvailable(
            @org.eclipse.jdt.annotation.NonNull Boolean quickConnectAvailable) {
        this.quickConnectAvailable = quickConnectAvailable;
        return this;
    }

    /**
     * Gets or sets a value indicating whether quick connect is available for use on this server.
     * 
     * @return quickConnectAvailable
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_QUICK_CONNECT_AVAILABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getQuickConnectAvailable() {
        return quickConnectAvailable;
    }

    @JsonProperty(JSON_PROPERTY_QUICK_CONNECT_AVAILABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setQuickConnectAvailable(@org.eclipse.jdt.annotation.NonNull Boolean quickConnectAvailable) {
        this.quickConnectAvailable = quickConnectAvailable;
    }

    public ServerConfiguration enableCaseSensitiveItemIds(
            @org.eclipse.jdt.annotation.NonNull Boolean enableCaseSensitiveItemIds) {
        this.enableCaseSensitiveItemIds = enableCaseSensitiveItemIds;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [enable case sensitive item ids].
     * 
     * @return enableCaseSensitiveItemIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_CASE_SENSITIVE_ITEM_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableCaseSensitiveItemIds() {
        return enableCaseSensitiveItemIds;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_CASE_SENSITIVE_ITEM_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableCaseSensitiveItemIds(@org.eclipse.jdt.annotation.NonNull Boolean enableCaseSensitiveItemIds) {
        this.enableCaseSensitiveItemIds = enableCaseSensitiveItemIds;
    }

    public ServerConfiguration disableLiveTvChannelUserDataName(
            @org.eclipse.jdt.annotation.NonNull Boolean disableLiveTvChannelUserDataName) {
        this.disableLiveTvChannelUserDataName = disableLiveTvChannelUserDataName;
        return this;
    }

    /**
     * Get disableLiveTvChannelUserDataName
     * 
     * @return disableLiveTvChannelUserDataName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISABLE_LIVE_TV_CHANNEL_USER_DATA_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDisableLiveTvChannelUserDataName() {
        return disableLiveTvChannelUserDataName;
    }

    @JsonProperty(JSON_PROPERTY_DISABLE_LIVE_TV_CHANNEL_USER_DATA_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisableLiveTvChannelUserDataName(
            @org.eclipse.jdt.annotation.NonNull Boolean disableLiveTvChannelUserDataName) {
        this.disableLiveTvChannelUserDataName = disableLiveTvChannelUserDataName;
    }

    public ServerConfiguration metadataPath(@org.eclipse.jdt.annotation.NonNull String metadataPath) {
        this.metadataPath = metadataPath;
        return this;
    }

    /**
     * Gets or sets the metadata path.
     * 
     * @return metadataPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataPath() {
        return metadataPath;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataPath(@org.eclipse.jdt.annotation.NonNull String metadataPath) {
        this.metadataPath = metadataPath;
    }

    public ServerConfiguration metadataNetworkPath(@org.eclipse.jdt.annotation.NonNull String metadataNetworkPath) {
        this.metadataNetworkPath = metadataNetworkPath;
        return this;
    }

    /**
     * Get metadataNetworkPath
     * 
     * @return metadataNetworkPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_NETWORK_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataNetworkPath() {
        return metadataNetworkPath;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_NETWORK_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataNetworkPath(@org.eclipse.jdt.annotation.NonNull String metadataNetworkPath) {
        this.metadataNetworkPath = metadataNetworkPath;
    }

    public ServerConfiguration preferredMetadataLanguage(
            @org.eclipse.jdt.annotation.NonNull String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
        return this;
    }

    /**
     * Gets or sets the preferred metadata language.
     * 
     * @return preferredMetadataLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreferredMetadataLanguage() {
        return preferredMetadataLanguage;
    }

    @JsonProperty(JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
    }

    public ServerConfiguration metadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
        return this;
    }

    /**
     * Gets or sets the metadata country code.
     * 
     * @return metadataCountryCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_COUNTRY_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataCountryCode() {
        return metadataCountryCode;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_COUNTRY_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
    }

    public ServerConfiguration sortReplaceCharacters(
            @org.eclipse.jdt.annotation.NonNull List<String> sortReplaceCharacters) {
        this.sortReplaceCharacters = sortReplaceCharacters;
        return this;
    }

    public ServerConfiguration addSortReplaceCharactersItem(String sortReplaceCharactersItem) {
        if (this.sortReplaceCharacters == null) {
            this.sortReplaceCharacters = new ArrayList<>();
        }
        this.sortReplaceCharacters.add(sortReplaceCharactersItem);
        return this;
    }

    /**
     * Gets or sets characters to be replaced with a &#39; &#39; in strings to create a sort name.
     * 
     * @return sortReplaceCharacters
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SORT_REPLACE_CHARACTERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getSortReplaceCharacters() {
        return sortReplaceCharacters;
    }

    @JsonProperty(JSON_PROPERTY_SORT_REPLACE_CHARACTERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortReplaceCharacters(@org.eclipse.jdt.annotation.NonNull List<String> sortReplaceCharacters) {
        this.sortReplaceCharacters = sortReplaceCharacters;
    }

    public ServerConfiguration sortRemoveCharacters(
            @org.eclipse.jdt.annotation.NonNull List<String> sortRemoveCharacters) {
        this.sortRemoveCharacters = sortRemoveCharacters;
        return this;
    }

    public ServerConfiguration addSortRemoveCharactersItem(String sortRemoveCharactersItem) {
        if (this.sortRemoveCharacters == null) {
            this.sortRemoveCharacters = new ArrayList<>();
        }
        this.sortRemoveCharacters.add(sortRemoveCharactersItem);
        return this;
    }

    /**
     * Gets or sets characters to be removed from strings to create a sort name.
     * 
     * @return sortRemoveCharacters
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SORT_REMOVE_CHARACTERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getSortRemoveCharacters() {
        return sortRemoveCharacters;
    }

    @JsonProperty(JSON_PROPERTY_SORT_REMOVE_CHARACTERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortRemoveCharacters(@org.eclipse.jdt.annotation.NonNull List<String> sortRemoveCharacters) {
        this.sortRemoveCharacters = sortRemoveCharacters;
    }

    public ServerConfiguration sortRemoveWords(@org.eclipse.jdt.annotation.NonNull List<String> sortRemoveWords) {
        this.sortRemoveWords = sortRemoveWords;
        return this;
    }

    public ServerConfiguration addSortRemoveWordsItem(String sortRemoveWordsItem) {
        if (this.sortRemoveWords == null) {
            this.sortRemoveWords = new ArrayList<>();
        }
        this.sortRemoveWords.add(sortRemoveWordsItem);
        return this;
    }

    /**
     * Gets or sets words to be removed from strings to create a sort name.
     * 
     * @return sortRemoveWords
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SORT_REMOVE_WORDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getSortRemoveWords() {
        return sortRemoveWords;
    }

    @JsonProperty(JSON_PROPERTY_SORT_REMOVE_WORDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortRemoveWords(@org.eclipse.jdt.annotation.NonNull List<String> sortRemoveWords) {
        this.sortRemoveWords = sortRemoveWords;
    }

    public ServerConfiguration minResumePct(@org.eclipse.jdt.annotation.NonNull Integer minResumePct) {
        this.minResumePct = minResumePct;
        return this;
    }

    /**
     * Gets or sets the minimum percentage of an item that must be played in order for playstate to be updated.
     * 
     * @return minResumePct
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIN_RESUME_PCT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMinResumePct() {
        return minResumePct;
    }

    @JsonProperty(JSON_PROPERTY_MIN_RESUME_PCT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinResumePct(@org.eclipse.jdt.annotation.NonNull Integer minResumePct) {
        this.minResumePct = minResumePct;
    }

    public ServerConfiguration maxResumePct(@org.eclipse.jdt.annotation.NonNull Integer maxResumePct) {
        this.maxResumePct = maxResumePct;
        return this;
    }

    /**
     * Gets or sets the maximum percentage of an item that can be played while still saving playstate. If this
     * percentage is crossed playstate will be reset to the beginning and the item will be marked watched.
     * 
     * @return maxResumePct
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_RESUME_PCT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxResumePct() {
        return maxResumePct;
    }

    @JsonProperty(JSON_PROPERTY_MAX_RESUME_PCT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxResumePct(@org.eclipse.jdt.annotation.NonNull Integer maxResumePct) {
        this.maxResumePct = maxResumePct;
    }

    public ServerConfiguration minResumeDurationSeconds(
            @org.eclipse.jdt.annotation.NonNull Integer minResumeDurationSeconds) {
        this.minResumeDurationSeconds = minResumeDurationSeconds;
        return this;
    }

    /**
     * Gets or sets the minimum duration that an item must have in order to be eligible for playstate updates..
     * 
     * @return minResumeDurationSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIN_RESUME_DURATION_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMinResumeDurationSeconds() {
        return minResumeDurationSeconds;
    }

    @JsonProperty(JSON_PROPERTY_MIN_RESUME_DURATION_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinResumeDurationSeconds(@org.eclipse.jdt.annotation.NonNull Integer minResumeDurationSeconds) {
        this.minResumeDurationSeconds = minResumeDurationSeconds;
    }

    public ServerConfiguration minAudiobookResume(@org.eclipse.jdt.annotation.NonNull Integer minAudiobookResume) {
        this.minAudiobookResume = minAudiobookResume;
        return this;
    }

    /**
     * Gets or sets the minimum minutes of a book that must be played in order for playstate to be updated.
     * 
     * @return minAudiobookResume
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIN_AUDIOBOOK_RESUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMinAudiobookResume() {
        return minAudiobookResume;
    }

    @JsonProperty(JSON_PROPERTY_MIN_AUDIOBOOK_RESUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinAudiobookResume(@org.eclipse.jdt.annotation.NonNull Integer minAudiobookResume) {
        this.minAudiobookResume = minAudiobookResume;
    }

    public ServerConfiguration maxAudiobookResume(@org.eclipse.jdt.annotation.NonNull Integer maxAudiobookResume) {
        this.maxAudiobookResume = maxAudiobookResume;
        return this;
    }

    /**
     * Gets or sets the remaining minutes of a book that can be played while still saving playstate. If this percentage
     * is crossed playstate will be reset to the beginning and the item will be marked watched.
     * 
     * @return maxAudiobookResume
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_AUDIOBOOK_RESUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxAudiobookResume() {
        return maxAudiobookResume;
    }

    @JsonProperty(JSON_PROPERTY_MAX_AUDIOBOOK_RESUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxAudiobookResume(@org.eclipse.jdt.annotation.NonNull Integer maxAudiobookResume) {
        this.maxAudiobookResume = maxAudiobookResume;
    }

    public ServerConfiguration libraryMonitorDelay(@org.eclipse.jdt.annotation.NonNull Integer libraryMonitorDelay) {
        this.libraryMonitorDelay = libraryMonitorDelay;
        return this;
    }

    /**
     * Gets or sets the delay in seconds that we will wait after a file system change to try and discover what has been
     * added/removed Some delay is necessary with some items because their creation is not atomic. It involves the
     * creation of several different directories and files.
     * 
     * @return libraryMonitorDelay
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIBRARY_MONITOR_DELAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getLibraryMonitorDelay() {
        return libraryMonitorDelay;
    }

    @JsonProperty(JSON_PROPERTY_LIBRARY_MONITOR_DELAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLibraryMonitorDelay(@org.eclipse.jdt.annotation.NonNull Integer libraryMonitorDelay) {
        this.libraryMonitorDelay = libraryMonitorDelay;
    }

    public ServerConfiguration imageSavingConvention(
            @org.eclipse.jdt.annotation.NonNull ImageSavingConvention imageSavingConvention) {
        this.imageSavingConvention = imageSavingConvention;
        return this;
    }

    /**
     * Gets or sets the image saving convention.
     * 
     * @return imageSavingConvention
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_SAVING_CONVENTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ImageSavingConvention getImageSavingConvention() {
        return imageSavingConvention;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_SAVING_CONVENTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageSavingConvention(
            @org.eclipse.jdt.annotation.NonNull ImageSavingConvention imageSavingConvention) {
        this.imageSavingConvention = imageSavingConvention;
    }

    public ServerConfiguration metadataOptions(
            @org.eclipse.jdt.annotation.NonNull List<MetadataOptions> metadataOptions) {
        this.metadataOptions = metadataOptions;
        return this;
    }

    public ServerConfiguration addMetadataOptionsItem(MetadataOptions metadataOptionsItem) {
        if (this.metadataOptions == null) {
            this.metadataOptions = new ArrayList<>();
        }
        this.metadataOptions.add(metadataOptionsItem);
        return this;
    }

    /**
     * Get metadataOptions
     * 
     * @return metadataOptions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MetadataOptions> getMetadataOptions() {
        return metadataOptions;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataOptions(@org.eclipse.jdt.annotation.NonNull List<MetadataOptions> metadataOptions) {
        this.metadataOptions = metadataOptions;
    }

    public ServerConfiguration skipDeserializationForBasicTypes(
            @org.eclipse.jdt.annotation.NonNull Boolean skipDeserializationForBasicTypes) {
        this.skipDeserializationForBasicTypes = skipDeserializationForBasicTypes;
        return this;
    }

    /**
     * Get skipDeserializationForBasicTypes
     * 
     * @return skipDeserializationForBasicTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SKIP_DESERIALIZATION_FOR_BASIC_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSkipDeserializationForBasicTypes() {
        return skipDeserializationForBasicTypes;
    }

    @JsonProperty(JSON_PROPERTY_SKIP_DESERIALIZATION_FOR_BASIC_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSkipDeserializationForBasicTypes(
            @org.eclipse.jdt.annotation.NonNull Boolean skipDeserializationForBasicTypes) {
        this.skipDeserializationForBasicTypes = skipDeserializationForBasicTypes;
    }

    public ServerConfiguration serverName(@org.eclipse.jdt.annotation.NonNull String serverName) {
        this.serverName = serverName;
        return this;
    }

    /**
     * Get serverName
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

    public ServerConfiguration uiCulture(@org.eclipse.jdt.annotation.NonNull String uiCulture) {
        this.uiCulture = uiCulture;
        return this;
    }

    /**
     * Get uiCulture
     * 
     * @return uiCulture
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_UI_CULTURE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUiCulture() {
        return uiCulture;
    }

    @JsonProperty(JSON_PROPERTY_UI_CULTURE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUiCulture(@org.eclipse.jdt.annotation.NonNull String uiCulture) {
        this.uiCulture = uiCulture;
    }

    public ServerConfiguration saveMetadataHidden(@org.eclipse.jdt.annotation.NonNull Boolean saveMetadataHidden) {
        this.saveMetadataHidden = saveMetadataHidden;
        return this;
    }

    /**
     * Get saveMetadataHidden
     * 
     * @return saveMetadataHidden
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SAVE_METADATA_HIDDEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveMetadataHidden() {
        return saveMetadataHidden;
    }

    @JsonProperty(JSON_PROPERTY_SAVE_METADATA_HIDDEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveMetadataHidden(@org.eclipse.jdt.annotation.NonNull Boolean saveMetadataHidden) {
        this.saveMetadataHidden = saveMetadataHidden;
    }

    public ServerConfiguration contentTypes(@org.eclipse.jdt.annotation.NonNull List<NameValuePair> contentTypes) {
        this.contentTypes = contentTypes;
        return this;
    }

    public ServerConfiguration addContentTypesItem(NameValuePair contentTypesItem) {
        if (this.contentTypes == null) {
            this.contentTypes = new ArrayList<>();
        }
        this.contentTypes.add(contentTypesItem);
        return this;
    }

    /**
     * Get contentTypes
     * 
     * @return contentTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTENT_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameValuePair> getContentTypes() {
        return contentTypes;
    }

    @JsonProperty(JSON_PROPERTY_CONTENT_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContentTypes(@org.eclipse.jdt.annotation.NonNull List<NameValuePair> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public ServerConfiguration remoteClientBitrateLimit(
            @org.eclipse.jdt.annotation.NonNull Integer remoteClientBitrateLimit) {
        this.remoteClientBitrateLimit = remoteClientBitrateLimit;
        return this;
    }

    /**
     * Get remoteClientBitrateLimit
     * 
     * @return remoteClientBitrateLimit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getRemoteClientBitrateLimit() {
        return remoteClientBitrateLimit;
    }

    @JsonProperty(JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteClientBitrateLimit(@org.eclipse.jdt.annotation.NonNull Integer remoteClientBitrateLimit) {
        this.remoteClientBitrateLimit = remoteClientBitrateLimit;
    }

    public ServerConfiguration enableFolderView(@org.eclipse.jdt.annotation.NonNull Boolean enableFolderView) {
        this.enableFolderView = enableFolderView;
        return this;
    }

    /**
     * Get enableFolderView
     * 
     * @return enableFolderView
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_FOLDER_VIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableFolderView() {
        return enableFolderView;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_FOLDER_VIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableFolderView(@org.eclipse.jdt.annotation.NonNull Boolean enableFolderView) {
        this.enableFolderView = enableFolderView;
    }

    public ServerConfiguration enableGroupingIntoCollections(
            @org.eclipse.jdt.annotation.NonNull Boolean enableGroupingIntoCollections) {
        this.enableGroupingIntoCollections = enableGroupingIntoCollections;
        return this;
    }

    /**
     * Get enableGroupingIntoCollections
     * 
     * @return enableGroupingIntoCollections
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_GROUPING_INTO_COLLECTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableGroupingIntoCollections() {
        return enableGroupingIntoCollections;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_GROUPING_INTO_COLLECTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableGroupingIntoCollections(
            @org.eclipse.jdt.annotation.NonNull Boolean enableGroupingIntoCollections) {
        this.enableGroupingIntoCollections = enableGroupingIntoCollections;
    }

    public ServerConfiguration displaySpecialsWithinSeasons(
            @org.eclipse.jdt.annotation.NonNull Boolean displaySpecialsWithinSeasons) {
        this.displaySpecialsWithinSeasons = displaySpecialsWithinSeasons;
        return this;
    }

    /**
     * Get displaySpecialsWithinSeasons
     * 
     * @return displaySpecialsWithinSeasons
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISPLAY_SPECIALS_WITHIN_SEASONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDisplaySpecialsWithinSeasons() {
        return displaySpecialsWithinSeasons;
    }

    @JsonProperty(JSON_PROPERTY_DISPLAY_SPECIALS_WITHIN_SEASONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplaySpecialsWithinSeasons(
            @org.eclipse.jdt.annotation.NonNull Boolean displaySpecialsWithinSeasons) {
        this.displaySpecialsWithinSeasons = displaySpecialsWithinSeasons;
    }

    public ServerConfiguration codecsUsed(@org.eclipse.jdt.annotation.NonNull List<String> codecsUsed) {
        this.codecsUsed = codecsUsed;
        return this;
    }

    public ServerConfiguration addCodecsUsedItem(String codecsUsedItem) {
        if (this.codecsUsed == null) {
            this.codecsUsed = new ArrayList<>();
        }
        this.codecsUsed.add(codecsUsedItem);
        return this;
    }

    /**
     * Get codecsUsed
     * 
     * @return codecsUsed
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CODECS_USED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getCodecsUsed() {
        return codecsUsed;
    }

    @JsonProperty(JSON_PROPERTY_CODECS_USED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodecsUsed(@org.eclipse.jdt.annotation.NonNull List<String> codecsUsed) {
        this.codecsUsed = codecsUsed;
    }

    public ServerConfiguration pluginRepositories(
            @org.eclipse.jdt.annotation.NonNull List<RepositoryInfo> pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
        return this;
    }

    public ServerConfiguration addPluginRepositoriesItem(RepositoryInfo pluginRepositoriesItem) {
        if (this.pluginRepositories == null) {
            this.pluginRepositories = new ArrayList<>();
        }
        this.pluginRepositories.add(pluginRepositoriesItem);
        return this;
    }

    /**
     * Get pluginRepositories
     * 
     * @return pluginRepositories
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLUGIN_REPOSITORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<RepositoryInfo> getPluginRepositories() {
        return pluginRepositories;
    }

    @JsonProperty(JSON_PROPERTY_PLUGIN_REPOSITORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPluginRepositories(@org.eclipse.jdt.annotation.NonNull List<RepositoryInfo> pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
    }

    public ServerConfiguration enableExternalContentInSuggestions(
            @org.eclipse.jdt.annotation.NonNull Boolean enableExternalContentInSuggestions) {
        this.enableExternalContentInSuggestions = enableExternalContentInSuggestions;
        return this;
    }

    /**
     * Get enableExternalContentInSuggestions
     * 
     * @return enableExternalContentInSuggestions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_EXTERNAL_CONTENT_IN_SUGGESTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableExternalContentInSuggestions() {
        return enableExternalContentInSuggestions;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_EXTERNAL_CONTENT_IN_SUGGESTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableExternalContentInSuggestions(
            @org.eclipse.jdt.annotation.NonNull Boolean enableExternalContentInSuggestions) {
        this.enableExternalContentInSuggestions = enableExternalContentInSuggestions;
    }

    public ServerConfiguration imageExtractionTimeoutMs(
            @org.eclipse.jdt.annotation.NonNull Integer imageExtractionTimeoutMs) {
        this.imageExtractionTimeoutMs = imageExtractionTimeoutMs;
        return this;
    }

    /**
     * Get imageExtractionTimeoutMs
     * 
     * @return imageExtractionTimeoutMs
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_EXTRACTION_TIMEOUT_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getImageExtractionTimeoutMs() {
        return imageExtractionTimeoutMs;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_EXTRACTION_TIMEOUT_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageExtractionTimeoutMs(@org.eclipse.jdt.annotation.NonNull Integer imageExtractionTimeoutMs) {
        this.imageExtractionTimeoutMs = imageExtractionTimeoutMs;
    }

    public ServerConfiguration pathSubstitutions(
            @org.eclipse.jdt.annotation.NonNull List<PathSubstitution> pathSubstitutions) {
        this.pathSubstitutions = pathSubstitutions;
        return this;
    }

    public ServerConfiguration addPathSubstitutionsItem(PathSubstitution pathSubstitutionsItem) {
        if (this.pathSubstitutions == null) {
            this.pathSubstitutions = new ArrayList<>();
        }
        this.pathSubstitutions.add(pathSubstitutionsItem);
        return this;
    }

    /**
     * Get pathSubstitutions
     * 
     * @return pathSubstitutions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH_SUBSTITUTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<PathSubstitution> getPathSubstitutions() {
        return pathSubstitutions;
    }

    @JsonProperty(JSON_PROPERTY_PATH_SUBSTITUTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPathSubstitutions(@org.eclipse.jdt.annotation.NonNull List<PathSubstitution> pathSubstitutions) {
        this.pathSubstitutions = pathSubstitutions;
    }

    public ServerConfiguration enableSlowResponseWarning(
            @org.eclipse.jdt.annotation.NonNull Boolean enableSlowResponseWarning) {
        this.enableSlowResponseWarning = enableSlowResponseWarning;
        return this;
    }

    /**
     * Gets or sets a value indicating whether slow server responses should be logged as a warning.
     * 
     * @return enableSlowResponseWarning
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_SLOW_RESPONSE_WARNING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSlowResponseWarning() {
        return enableSlowResponseWarning;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_SLOW_RESPONSE_WARNING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSlowResponseWarning(@org.eclipse.jdt.annotation.NonNull Boolean enableSlowResponseWarning) {
        this.enableSlowResponseWarning = enableSlowResponseWarning;
    }

    public ServerConfiguration slowResponseThresholdMs(
            @org.eclipse.jdt.annotation.NonNull Long slowResponseThresholdMs) {
        this.slowResponseThresholdMs = slowResponseThresholdMs;
        return this;
    }

    /**
     * Gets or sets the threshold for the slow response time warning in ms.
     * 
     * @return slowResponseThresholdMs
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SLOW_RESPONSE_THRESHOLD_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getSlowResponseThresholdMs() {
        return slowResponseThresholdMs;
    }

    @JsonProperty(JSON_PROPERTY_SLOW_RESPONSE_THRESHOLD_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSlowResponseThresholdMs(@org.eclipse.jdt.annotation.NonNull Long slowResponseThresholdMs) {
        this.slowResponseThresholdMs = slowResponseThresholdMs;
    }

    public ServerConfiguration corsHosts(@org.eclipse.jdt.annotation.NonNull List<String> corsHosts) {
        this.corsHosts = corsHosts;
        return this;
    }

    public ServerConfiguration addCorsHostsItem(String corsHostsItem) {
        if (this.corsHosts == null) {
            this.corsHosts = new ArrayList<>();
        }
        this.corsHosts.add(corsHostsItem);
        return this;
    }

    /**
     * Gets or sets the cors hosts.
     * 
     * @return corsHosts
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CORS_HOSTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getCorsHosts() {
        return corsHosts;
    }

    @JsonProperty(JSON_PROPERTY_CORS_HOSTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCorsHosts(@org.eclipse.jdt.annotation.NonNull List<String> corsHosts) {
        this.corsHosts = corsHosts;
    }

    public ServerConfiguration activityLogRetentionDays(
            @org.eclipse.jdt.annotation.NonNull Integer activityLogRetentionDays) {
        this.activityLogRetentionDays = activityLogRetentionDays;
        return this;
    }

    /**
     * Gets or sets the number of days we should retain activity logs.
     * 
     * @return activityLogRetentionDays
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ACTIVITY_LOG_RETENTION_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getActivityLogRetentionDays() {
        return activityLogRetentionDays;
    }

    @JsonProperty(JSON_PROPERTY_ACTIVITY_LOG_RETENTION_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setActivityLogRetentionDays(@org.eclipse.jdt.annotation.NonNull Integer activityLogRetentionDays) {
        this.activityLogRetentionDays = activityLogRetentionDays;
    }

    public ServerConfiguration libraryScanFanoutConcurrency(
            @org.eclipse.jdt.annotation.NonNull Integer libraryScanFanoutConcurrency) {
        this.libraryScanFanoutConcurrency = libraryScanFanoutConcurrency;
        return this;
    }

    /**
     * Gets or sets the how the library scan fans out.
     * 
     * @return libraryScanFanoutConcurrency
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIBRARY_SCAN_FANOUT_CONCURRENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getLibraryScanFanoutConcurrency() {
        return libraryScanFanoutConcurrency;
    }

    @JsonProperty(JSON_PROPERTY_LIBRARY_SCAN_FANOUT_CONCURRENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLibraryScanFanoutConcurrency(
            @org.eclipse.jdt.annotation.NonNull Integer libraryScanFanoutConcurrency) {
        this.libraryScanFanoutConcurrency = libraryScanFanoutConcurrency;
    }

    public ServerConfiguration libraryMetadataRefreshConcurrency(
            @org.eclipse.jdt.annotation.NonNull Integer libraryMetadataRefreshConcurrency) {
        this.libraryMetadataRefreshConcurrency = libraryMetadataRefreshConcurrency;
        return this;
    }

    /**
     * Gets or sets the how many metadata refreshes can run concurrently.
     * 
     * @return libraryMetadataRefreshConcurrency
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIBRARY_METADATA_REFRESH_CONCURRENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getLibraryMetadataRefreshConcurrency() {
        return libraryMetadataRefreshConcurrency;
    }

    @JsonProperty(JSON_PROPERTY_LIBRARY_METADATA_REFRESH_CONCURRENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLibraryMetadataRefreshConcurrency(
            @org.eclipse.jdt.annotation.NonNull Integer libraryMetadataRefreshConcurrency) {
        this.libraryMetadataRefreshConcurrency = libraryMetadataRefreshConcurrency;
    }

    public ServerConfiguration removeOldPlugins(@org.eclipse.jdt.annotation.NonNull Boolean removeOldPlugins) {
        this.removeOldPlugins = removeOldPlugins;
        return this;
    }

    /**
     * Gets or sets a value indicating whether older plugins should automatically be deleted from the plugin folder.
     * 
     * @return removeOldPlugins
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REMOVE_OLD_PLUGINS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRemoveOldPlugins() {
        return removeOldPlugins;
    }

    @JsonProperty(JSON_PROPERTY_REMOVE_OLD_PLUGINS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoveOldPlugins(@org.eclipse.jdt.annotation.NonNull Boolean removeOldPlugins) {
        this.removeOldPlugins = removeOldPlugins;
    }

    public ServerConfiguration allowClientLogUpload(@org.eclipse.jdt.annotation.NonNull Boolean allowClientLogUpload) {
        this.allowClientLogUpload = allowClientLogUpload;
        return this;
    }

    /**
     * Gets or sets a value indicating whether clients should be allowed to upload logs.
     * 
     * @return allowClientLogUpload
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALLOW_CLIENT_LOG_UPLOAD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowClientLogUpload() {
        return allowClientLogUpload;
    }

    @JsonProperty(JSON_PROPERTY_ALLOW_CLIENT_LOG_UPLOAD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowClientLogUpload(@org.eclipse.jdt.annotation.NonNull Boolean allowClientLogUpload) {
        this.allowClientLogUpload = allowClientLogUpload;
    }

    /**
     * Return true if this ServerConfiguration object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerConfiguration serverConfiguration = (ServerConfiguration) o;
        return Objects.equals(this.logFileRetentionDays, serverConfiguration.logFileRetentionDays)
                && Objects.equals(this.isStartupWizardCompleted, serverConfiguration.isStartupWizardCompleted)
                && Objects.equals(this.cachePath, serverConfiguration.cachePath)
                && Objects.equals(this.previousVersion, serverConfiguration.previousVersion)
                && Objects.equals(this.previousVersionStr, serverConfiguration.previousVersionStr)
                && Objects.equals(this.enableMetrics, serverConfiguration.enableMetrics)
                && Objects.equals(this.enableNormalizedItemByNameIds, serverConfiguration.enableNormalizedItemByNameIds)
                && Objects.equals(this.isPortAuthorized, serverConfiguration.isPortAuthorized)
                && Objects.equals(this.quickConnectAvailable, serverConfiguration.quickConnectAvailable)
                && Objects.equals(this.enableCaseSensitiveItemIds, serverConfiguration.enableCaseSensitiveItemIds)
                && Objects.equals(this.disableLiveTvChannelUserDataName,
                        serverConfiguration.disableLiveTvChannelUserDataName)
                && Objects.equals(this.metadataPath, serverConfiguration.metadataPath)
                && Objects.equals(this.metadataNetworkPath, serverConfiguration.metadataNetworkPath)
                && Objects.equals(this.preferredMetadataLanguage, serverConfiguration.preferredMetadataLanguage)
                && Objects.equals(this.metadataCountryCode, serverConfiguration.metadataCountryCode)
                && Objects.equals(this.sortReplaceCharacters, serverConfiguration.sortReplaceCharacters)
                && Objects.equals(this.sortRemoveCharacters, serverConfiguration.sortRemoveCharacters)
                && Objects.equals(this.sortRemoveWords, serverConfiguration.sortRemoveWords)
                && Objects.equals(this.minResumePct, serverConfiguration.minResumePct)
                && Objects.equals(this.maxResumePct, serverConfiguration.maxResumePct)
                && Objects.equals(this.minResumeDurationSeconds, serverConfiguration.minResumeDurationSeconds)
                && Objects.equals(this.minAudiobookResume, serverConfiguration.minAudiobookResume)
                && Objects.equals(this.maxAudiobookResume, serverConfiguration.maxAudiobookResume)
                && Objects.equals(this.libraryMonitorDelay, serverConfiguration.libraryMonitorDelay)
                && Objects.equals(this.imageSavingConvention, serverConfiguration.imageSavingConvention)
                && Objects.equals(this.metadataOptions, serverConfiguration.metadataOptions)
                && Objects.equals(this.skipDeserializationForBasicTypes,
                        serverConfiguration.skipDeserializationForBasicTypes)
                && Objects.equals(this.serverName, serverConfiguration.serverName)
                && Objects.equals(this.uiCulture, serverConfiguration.uiCulture)
                && Objects.equals(this.saveMetadataHidden, serverConfiguration.saveMetadataHidden)
                && Objects.equals(this.contentTypes, serverConfiguration.contentTypes)
                && Objects.equals(this.remoteClientBitrateLimit, serverConfiguration.remoteClientBitrateLimit)
                && Objects.equals(this.enableFolderView, serverConfiguration.enableFolderView)
                && Objects.equals(this.enableGroupingIntoCollections, serverConfiguration.enableGroupingIntoCollections)
                && Objects.equals(this.displaySpecialsWithinSeasons, serverConfiguration.displaySpecialsWithinSeasons)
                && Objects.equals(this.codecsUsed, serverConfiguration.codecsUsed)
                && Objects.equals(this.pluginRepositories, serverConfiguration.pluginRepositories)
                && Objects.equals(this.enableExternalContentInSuggestions,
                        serverConfiguration.enableExternalContentInSuggestions)
                && Objects.equals(this.imageExtractionTimeoutMs, serverConfiguration.imageExtractionTimeoutMs)
                && Objects.equals(this.pathSubstitutions, serverConfiguration.pathSubstitutions)
                && Objects.equals(this.enableSlowResponseWarning, serverConfiguration.enableSlowResponseWarning)
                && Objects.equals(this.slowResponseThresholdMs, serverConfiguration.slowResponseThresholdMs)
                && Objects.equals(this.corsHosts, serverConfiguration.corsHosts)
                && Objects.equals(this.activityLogRetentionDays, serverConfiguration.activityLogRetentionDays)
                && Objects.equals(this.libraryScanFanoutConcurrency, serverConfiguration.libraryScanFanoutConcurrency)
                && Objects.equals(this.libraryMetadataRefreshConcurrency,
                        serverConfiguration.libraryMetadataRefreshConcurrency)
                && Objects.equals(this.removeOldPlugins, serverConfiguration.removeOldPlugins)
                && Objects.equals(this.allowClientLogUpload, serverConfiguration.allowClientLogUpload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logFileRetentionDays, isStartupWizardCompleted, cachePath, previousVersion,
                previousVersionStr, enableMetrics, enableNormalizedItemByNameIds, isPortAuthorized,
                quickConnectAvailable, enableCaseSensitiveItemIds, disableLiveTvChannelUserDataName, metadataPath,
                metadataNetworkPath, preferredMetadataLanguage, metadataCountryCode, sortReplaceCharacters,
                sortRemoveCharacters, sortRemoveWords, minResumePct, maxResumePct, minResumeDurationSeconds,
                minAudiobookResume, maxAudiobookResume, libraryMonitorDelay, imageSavingConvention, metadataOptions,
                skipDeserializationForBasicTypes, serverName, uiCulture, saveMetadataHidden, contentTypes,
                remoteClientBitrateLimit, enableFolderView, enableGroupingIntoCollections, displaySpecialsWithinSeasons,
                codecsUsed, pluginRepositories, enableExternalContentInSuggestions, imageExtractionTimeoutMs,
                pathSubstitutions, enableSlowResponseWarning, slowResponseThresholdMs, corsHosts,
                activityLogRetentionDays, libraryScanFanoutConcurrency, libraryMetadataRefreshConcurrency,
                removeOldPlugins, allowClientLogUpload);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServerConfiguration {\n");
        sb.append("    logFileRetentionDays: ").append(toIndentedString(logFileRetentionDays)).append("\n");
        sb.append("    isStartupWizardCompleted: ").append(toIndentedString(isStartupWizardCompleted)).append("\n");
        sb.append("    cachePath: ").append(toIndentedString(cachePath)).append("\n");
        sb.append("    previousVersion: ").append(toIndentedString(previousVersion)).append("\n");
        sb.append("    previousVersionStr: ").append(toIndentedString(previousVersionStr)).append("\n");
        sb.append("    enableMetrics: ").append(toIndentedString(enableMetrics)).append("\n");
        sb.append("    enableNormalizedItemByNameIds: ").append(toIndentedString(enableNormalizedItemByNameIds))
                .append("\n");
        sb.append("    isPortAuthorized: ").append(toIndentedString(isPortAuthorized)).append("\n");
        sb.append("    quickConnectAvailable: ").append(toIndentedString(quickConnectAvailable)).append("\n");
        sb.append("    enableCaseSensitiveItemIds: ").append(toIndentedString(enableCaseSensitiveItemIds)).append("\n");
        sb.append("    disableLiveTvChannelUserDataName: ").append(toIndentedString(disableLiveTvChannelUserDataName))
                .append("\n");
        sb.append("    metadataPath: ").append(toIndentedString(metadataPath)).append("\n");
        sb.append("    metadataNetworkPath: ").append(toIndentedString(metadataNetworkPath)).append("\n");
        sb.append("    preferredMetadataLanguage: ").append(toIndentedString(preferredMetadataLanguage)).append("\n");
        sb.append("    metadataCountryCode: ").append(toIndentedString(metadataCountryCode)).append("\n");
        sb.append("    sortReplaceCharacters: ").append(toIndentedString(sortReplaceCharacters)).append("\n");
        sb.append("    sortRemoveCharacters: ").append(toIndentedString(sortRemoveCharacters)).append("\n");
        sb.append("    sortRemoveWords: ").append(toIndentedString(sortRemoveWords)).append("\n");
        sb.append("    minResumePct: ").append(toIndentedString(minResumePct)).append("\n");
        sb.append("    maxResumePct: ").append(toIndentedString(maxResumePct)).append("\n");
        sb.append("    minResumeDurationSeconds: ").append(toIndentedString(minResumeDurationSeconds)).append("\n");
        sb.append("    minAudiobookResume: ").append(toIndentedString(minAudiobookResume)).append("\n");
        sb.append("    maxAudiobookResume: ").append(toIndentedString(maxAudiobookResume)).append("\n");
        sb.append("    libraryMonitorDelay: ").append(toIndentedString(libraryMonitorDelay)).append("\n");
        sb.append("    imageSavingConvention: ").append(toIndentedString(imageSavingConvention)).append("\n");
        sb.append("    metadataOptions: ").append(toIndentedString(metadataOptions)).append("\n");
        sb.append("    skipDeserializationForBasicTypes: ").append(toIndentedString(skipDeserializationForBasicTypes))
                .append("\n");
        sb.append("    serverName: ").append(toIndentedString(serverName)).append("\n");
        sb.append("    uiCulture: ").append(toIndentedString(uiCulture)).append("\n");
        sb.append("    saveMetadataHidden: ").append(toIndentedString(saveMetadataHidden)).append("\n");
        sb.append("    contentTypes: ").append(toIndentedString(contentTypes)).append("\n");
        sb.append("    remoteClientBitrateLimit: ").append(toIndentedString(remoteClientBitrateLimit)).append("\n");
        sb.append("    enableFolderView: ").append(toIndentedString(enableFolderView)).append("\n");
        sb.append("    enableGroupingIntoCollections: ").append(toIndentedString(enableGroupingIntoCollections))
                .append("\n");
        sb.append("    displaySpecialsWithinSeasons: ").append(toIndentedString(displaySpecialsWithinSeasons))
                .append("\n");
        sb.append("    codecsUsed: ").append(toIndentedString(codecsUsed)).append("\n");
        sb.append("    pluginRepositories: ").append(toIndentedString(pluginRepositories)).append("\n");
        sb.append("    enableExternalContentInSuggestions: ")
                .append(toIndentedString(enableExternalContentInSuggestions)).append("\n");
        sb.append("    imageExtractionTimeoutMs: ").append(toIndentedString(imageExtractionTimeoutMs)).append("\n");
        sb.append("    pathSubstitutions: ").append(toIndentedString(pathSubstitutions)).append("\n");
        sb.append("    enableSlowResponseWarning: ").append(toIndentedString(enableSlowResponseWarning)).append("\n");
        sb.append("    slowResponseThresholdMs: ").append(toIndentedString(slowResponseThresholdMs)).append("\n");
        sb.append("    corsHosts: ").append(toIndentedString(corsHosts)).append("\n");
        sb.append("    activityLogRetentionDays: ").append(toIndentedString(activityLogRetentionDays)).append("\n");
        sb.append("    libraryScanFanoutConcurrency: ").append(toIndentedString(libraryScanFanoutConcurrency))
                .append("\n");
        sb.append("    libraryMetadataRefreshConcurrency: ").append(toIndentedString(libraryMetadataRefreshConcurrency))
                .append("\n");
        sb.append("    removeOldPlugins: ").append(toIndentedString(removeOldPlugins)).append("\n");
        sb.append("    allowClientLogUpload: ").append(toIndentedString(allowClientLogUpload)).append("\n");
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

        // add `LogFileRetentionDays` to the URL query string
        if (getLogFileRetentionDays() != null) {
            joiner.add(String.format("%sLogFileRetentionDays%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLogFileRetentionDays()))));
        }

        // add `IsStartupWizardCompleted` to the URL query string
        if (getIsStartupWizardCompleted() != null) {
            joiner.add(String.format("%sIsStartupWizardCompleted%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsStartupWizardCompleted()))));
        }

        // add `CachePath` to the URL query string
        if (getCachePath() != null) {
            joiner.add(String.format("%sCachePath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCachePath()))));
        }

        // add `PreviousVersion` to the URL query string
        if (getPreviousVersion() != null) {
            joiner.add(String.format("%sPreviousVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreviousVersion()))));
        }

        // add `PreviousVersionStr` to the URL query string
        if (getPreviousVersionStr() != null) {
            joiner.add(String.format("%sPreviousVersionStr%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreviousVersionStr()))));
        }

        // add `EnableMetrics` to the URL query string
        if (getEnableMetrics() != null) {
            joiner.add(String.format("%sEnableMetrics%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableMetrics()))));
        }

        // add `EnableNormalizedItemByNameIds` to the URL query string
        if (getEnableNormalizedItemByNameIds() != null) {
            joiner.add(String.format("%sEnableNormalizedItemByNameIds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableNormalizedItemByNameIds()))));
        }

        // add `IsPortAuthorized` to the URL query string
        if (getIsPortAuthorized() != null) {
            joiner.add(String.format("%sIsPortAuthorized%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPortAuthorized()))));
        }

        // add `QuickConnectAvailable` to the URL query string
        if (getQuickConnectAvailable() != null) {
            joiner.add(String.format("%sQuickConnectAvailable%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getQuickConnectAvailable()))));
        }

        // add `EnableCaseSensitiveItemIds` to the URL query string
        if (getEnableCaseSensitiveItemIds() != null) {
            joiner.add(String.format("%sEnableCaseSensitiveItemIds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableCaseSensitiveItemIds()))));
        }

        // add `DisableLiveTvChannelUserDataName` to the URL query string
        if (getDisableLiveTvChannelUserDataName() != null) {
            joiner.add(String.format("%sDisableLiveTvChannelUserDataName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDisableLiveTvChannelUserDataName()))));
        }

        // add `MetadataPath` to the URL query string
        if (getMetadataPath() != null) {
            joiner.add(String.format("%sMetadataPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataPath()))));
        }

        // add `MetadataNetworkPath` to the URL query string
        if (getMetadataNetworkPath() != null) {
            joiner.add(String.format("%sMetadataNetworkPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataNetworkPath()))));
        }

        // add `PreferredMetadataLanguage` to the URL query string
        if (getPreferredMetadataLanguage() != null) {
            joiner.add(String.format("%sPreferredMetadataLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferredMetadataLanguage()))));
        }

        // add `MetadataCountryCode` to the URL query string
        if (getMetadataCountryCode() != null) {
            joiner.add(String.format("%sMetadataCountryCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataCountryCode()))));
        }

        // add `SortReplaceCharacters` to the URL query string
        if (getSortReplaceCharacters() != null) {
            for (int i = 0; i < getSortReplaceCharacters().size(); i++) {
                joiner.add(String.format("%sSortReplaceCharacters%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getSortReplaceCharacters().get(i)))));
            }
        }

        // add `SortRemoveCharacters` to the URL query string
        if (getSortRemoveCharacters() != null) {
            for (int i = 0; i < getSortRemoveCharacters().size(); i++) {
                joiner.add(String.format("%sSortRemoveCharacters%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getSortRemoveCharacters().get(i)))));
            }
        }

        // add `SortRemoveWords` to the URL query string
        if (getSortRemoveWords() != null) {
            for (int i = 0; i < getSortRemoveWords().size(); i++) {
                joiner.add(String.format("%sSortRemoveWords%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getSortRemoveWords().get(i)))));
            }
        }

        // add `MinResumePct` to the URL query string
        if (getMinResumePct() != null) {
            joiner.add(String.format("%sMinResumePct%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMinResumePct()))));
        }

        // add `MaxResumePct` to the URL query string
        if (getMaxResumePct() != null) {
            joiner.add(String.format("%sMaxResumePct%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxResumePct()))));
        }

        // add `MinResumeDurationSeconds` to the URL query string
        if (getMinResumeDurationSeconds() != null) {
            joiner.add(String.format("%sMinResumeDurationSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMinResumeDurationSeconds()))));
        }

        // add `MinAudiobookResume` to the URL query string
        if (getMinAudiobookResume() != null) {
            joiner.add(String.format("%sMinAudiobookResume%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMinAudiobookResume()))));
        }

        // add `MaxAudiobookResume` to the URL query string
        if (getMaxAudiobookResume() != null) {
            joiner.add(String.format("%sMaxAudiobookResume%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxAudiobookResume()))));
        }

        // add `LibraryMonitorDelay` to the URL query string
        if (getLibraryMonitorDelay() != null) {
            joiner.add(String.format("%sLibraryMonitorDelay%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLibraryMonitorDelay()))));
        }

        // add `ImageSavingConvention` to the URL query string
        if (getImageSavingConvention() != null) {
            joiner.add(String.format("%sImageSavingConvention%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageSavingConvention()))));
        }

        // add `MetadataOptions` to the URL query string
        if (getMetadataOptions() != null) {
            for (int i = 0; i < getMetadataOptions().size(); i++) {
                if (getMetadataOptions().get(i) != null) {
                    joiner.add(getMetadataOptions().get(i).toUrlQueryString(String.format("%sMetadataOptions%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `SkipDeserializationForBasicTypes` to the URL query string
        if (getSkipDeserializationForBasicTypes() != null) {
            joiner.add(String.format("%sSkipDeserializationForBasicTypes%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSkipDeserializationForBasicTypes()))));
        }

        // add `ServerName` to the URL query string
        if (getServerName() != null) {
            joiner.add(String.format("%sServerName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerName()))));
        }

        // add `UICulture` to the URL query string
        if (getUiCulture() != null) {
            joiner.add(String.format("%sUICulture%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUiCulture()))));
        }

        // add `SaveMetadataHidden` to the URL query string
        if (getSaveMetadataHidden() != null) {
            joiner.add(String.format("%sSaveMetadataHidden%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveMetadataHidden()))));
        }

        // add `ContentTypes` to the URL query string
        if (getContentTypes() != null) {
            for (int i = 0; i < getContentTypes().size(); i++) {
                if (getContentTypes().get(i) != null) {
                    joiner.add(getContentTypes().get(i).toUrlQueryString(String.format("%sContentTypes%s%s", prefix,
                            suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `RemoteClientBitrateLimit` to the URL query string
        if (getRemoteClientBitrateLimit() != null) {
            joiner.add(String.format("%sRemoteClientBitrateLimit%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRemoteClientBitrateLimit()))));
        }

        // add `EnableFolderView` to the URL query string
        if (getEnableFolderView() != null) {
            joiner.add(String.format("%sEnableFolderView%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableFolderView()))));
        }

        // add `EnableGroupingIntoCollections` to the URL query string
        if (getEnableGroupingIntoCollections() != null) {
            joiner.add(String.format("%sEnableGroupingIntoCollections%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableGroupingIntoCollections()))));
        }

        // add `DisplaySpecialsWithinSeasons` to the URL query string
        if (getDisplaySpecialsWithinSeasons() != null) {
            joiner.add(String.format("%sDisplaySpecialsWithinSeasons%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDisplaySpecialsWithinSeasons()))));
        }

        // add `CodecsUsed` to the URL query string
        if (getCodecsUsed() != null) {
            for (int i = 0; i < getCodecsUsed().size(); i++) {
                joiner.add(String.format("%sCodecsUsed%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getCodecsUsed().get(i)))));
            }
        }

        // add `PluginRepositories` to the URL query string
        if (getPluginRepositories() != null) {
            for (int i = 0; i < getPluginRepositories().size(); i++) {
                if (getPluginRepositories().get(i) != null) {
                    joiner.add(getPluginRepositories().get(i).toUrlQueryString(String.format("%sPluginRepositories%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `EnableExternalContentInSuggestions` to the URL query string
        if (getEnableExternalContentInSuggestions() != null) {
            joiner.add(String.format("%sEnableExternalContentInSuggestions%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableExternalContentInSuggestions()))));
        }

        // add `ImageExtractionTimeoutMs` to the URL query string
        if (getImageExtractionTimeoutMs() != null) {
            joiner.add(String.format("%sImageExtractionTimeoutMs%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageExtractionTimeoutMs()))));
        }

        // add `PathSubstitutions` to the URL query string
        if (getPathSubstitutions() != null) {
            for (int i = 0; i < getPathSubstitutions().size(); i++) {
                if (getPathSubstitutions().get(i) != null) {
                    joiner.add(getPathSubstitutions().get(i).toUrlQueryString(String.format("%sPathSubstitutions%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `EnableSlowResponseWarning` to the URL query string
        if (getEnableSlowResponseWarning() != null) {
            joiner.add(String.format("%sEnableSlowResponseWarning%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSlowResponseWarning()))));
        }

        // add `SlowResponseThresholdMs` to the URL query string
        if (getSlowResponseThresholdMs() != null) {
            joiner.add(String.format("%sSlowResponseThresholdMs%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSlowResponseThresholdMs()))));
        }

        // add `CorsHosts` to the URL query string
        if (getCorsHosts() != null) {
            for (int i = 0; i < getCorsHosts().size(); i++) {
                joiner.add(String.format("%sCorsHosts%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getCorsHosts().get(i)))));
            }
        }

        // add `ActivityLogRetentionDays` to the URL query string
        if (getActivityLogRetentionDays() != null) {
            joiner.add(String.format("%sActivityLogRetentionDays%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getActivityLogRetentionDays()))));
        }

        // add `LibraryScanFanoutConcurrency` to the URL query string
        if (getLibraryScanFanoutConcurrency() != null) {
            joiner.add(String.format("%sLibraryScanFanoutConcurrency%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLibraryScanFanoutConcurrency()))));
        }

        // add `LibraryMetadataRefreshConcurrency` to the URL query string
        if (getLibraryMetadataRefreshConcurrency() != null) {
            joiner.add(String.format("%sLibraryMetadataRefreshConcurrency%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLibraryMetadataRefreshConcurrency()))));
        }

        // add `RemoveOldPlugins` to the URL query string
        if (getRemoveOldPlugins() != null) {
            joiner.add(String.format("%sRemoveOldPlugins%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRemoveOldPlugins()))));
        }

        // add `AllowClientLogUpload` to the URL query string
        if (getAllowClientLogUpload() != null) {
            joiner.add(String.format("%sAllowClientLogUpload%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowClientLogUpload()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ServerConfiguration instance;

        public Builder() {
            this(new ServerConfiguration());
        }

        protected Builder(ServerConfiguration instance) {
            this.instance = instance;
        }

        public ServerConfiguration.Builder logFileRetentionDays(Integer logFileRetentionDays) {
            this.instance.logFileRetentionDays = logFileRetentionDays;
            return this;
        }

        public ServerConfiguration.Builder isStartupWizardCompleted(Boolean isStartupWizardCompleted) {
            this.instance.isStartupWizardCompleted = isStartupWizardCompleted;
            return this;
        }

        public ServerConfiguration.Builder cachePath(String cachePath) {
            this.instance.cachePath = cachePath;
            return this;
        }

        public ServerConfiguration.Builder previousVersion(String previousVersion) {
            this.instance.previousVersion = previousVersion;
            return this;
        }

        public ServerConfiguration.Builder previousVersionStr(String previousVersionStr) {
            this.instance.previousVersionStr = previousVersionStr;
            return this;
        }

        public ServerConfiguration.Builder enableMetrics(Boolean enableMetrics) {
            this.instance.enableMetrics = enableMetrics;
            return this;
        }

        public ServerConfiguration.Builder enableNormalizedItemByNameIds(Boolean enableNormalizedItemByNameIds) {
            this.instance.enableNormalizedItemByNameIds = enableNormalizedItemByNameIds;
            return this;
        }

        public ServerConfiguration.Builder isPortAuthorized(Boolean isPortAuthorized) {
            this.instance.isPortAuthorized = isPortAuthorized;
            return this;
        }

        public ServerConfiguration.Builder quickConnectAvailable(Boolean quickConnectAvailable) {
            this.instance.quickConnectAvailable = quickConnectAvailable;
            return this;
        }

        public ServerConfiguration.Builder enableCaseSensitiveItemIds(Boolean enableCaseSensitiveItemIds) {
            this.instance.enableCaseSensitiveItemIds = enableCaseSensitiveItemIds;
            return this;
        }

        public ServerConfiguration.Builder disableLiveTvChannelUserDataName(Boolean disableLiveTvChannelUserDataName) {
            this.instance.disableLiveTvChannelUserDataName = disableLiveTvChannelUserDataName;
            return this;
        }

        public ServerConfiguration.Builder metadataPath(String metadataPath) {
            this.instance.metadataPath = metadataPath;
            return this;
        }

        public ServerConfiguration.Builder metadataNetworkPath(String metadataNetworkPath) {
            this.instance.metadataNetworkPath = metadataNetworkPath;
            return this;
        }

        public ServerConfiguration.Builder preferredMetadataLanguage(String preferredMetadataLanguage) {
            this.instance.preferredMetadataLanguage = preferredMetadataLanguage;
            return this;
        }

        public ServerConfiguration.Builder metadataCountryCode(String metadataCountryCode) {
            this.instance.metadataCountryCode = metadataCountryCode;
            return this;
        }

        public ServerConfiguration.Builder sortReplaceCharacters(List<String> sortReplaceCharacters) {
            this.instance.sortReplaceCharacters = sortReplaceCharacters;
            return this;
        }

        public ServerConfiguration.Builder sortRemoveCharacters(List<String> sortRemoveCharacters) {
            this.instance.sortRemoveCharacters = sortRemoveCharacters;
            return this;
        }

        public ServerConfiguration.Builder sortRemoveWords(List<String> sortRemoveWords) {
            this.instance.sortRemoveWords = sortRemoveWords;
            return this;
        }

        public ServerConfiguration.Builder minResumePct(Integer minResumePct) {
            this.instance.minResumePct = minResumePct;
            return this;
        }

        public ServerConfiguration.Builder maxResumePct(Integer maxResumePct) {
            this.instance.maxResumePct = maxResumePct;
            return this;
        }

        public ServerConfiguration.Builder minResumeDurationSeconds(Integer minResumeDurationSeconds) {
            this.instance.minResumeDurationSeconds = minResumeDurationSeconds;
            return this;
        }

        public ServerConfiguration.Builder minAudiobookResume(Integer minAudiobookResume) {
            this.instance.minAudiobookResume = minAudiobookResume;
            return this;
        }

        public ServerConfiguration.Builder maxAudiobookResume(Integer maxAudiobookResume) {
            this.instance.maxAudiobookResume = maxAudiobookResume;
            return this;
        }

        public ServerConfiguration.Builder libraryMonitorDelay(Integer libraryMonitorDelay) {
            this.instance.libraryMonitorDelay = libraryMonitorDelay;
            return this;
        }

        public ServerConfiguration.Builder imageSavingConvention(ImageSavingConvention imageSavingConvention) {
            this.instance.imageSavingConvention = imageSavingConvention;
            return this;
        }

        public ServerConfiguration.Builder metadataOptions(List<MetadataOptions> metadataOptions) {
            this.instance.metadataOptions = metadataOptions;
            return this;
        }

        public ServerConfiguration.Builder skipDeserializationForBasicTypes(Boolean skipDeserializationForBasicTypes) {
            this.instance.skipDeserializationForBasicTypes = skipDeserializationForBasicTypes;
            return this;
        }

        public ServerConfiguration.Builder serverName(String serverName) {
            this.instance.serverName = serverName;
            return this;
        }

        public ServerConfiguration.Builder uiCulture(String uiCulture) {
            this.instance.uiCulture = uiCulture;
            return this;
        }

        public ServerConfiguration.Builder saveMetadataHidden(Boolean saveMetadataHidden) {
            this.instance.saveMetadataHidden = saveMetadataHidden;
            return this;
        }

        public ServerConfiguration.Builder contentTypes(List<NameValuePair> contentTypes) {
            this.instance.contentTypes = contentTypes;
            return this;
        }

        public ServerConfiguration.Builder remoteClientBitrateLimit(Integer remoteClientBitrateLimit) {
            this.instance.remoteClientBitrateLimit = remoteClientBitrateLimit;
            return this;
        }

        public ServerConfiguration.Builder enableFolderView(Boolean enableFolderView) {
            this.instance.enableFolderView = enableFolderView;
            return this;
        }

        public ServerConfiguration.Builder enableGroupingIntoCollections(Boolean enableGroupingIntoCollections) {
            this.instance.enableGroupingIntoCollections = enableGroupingIntoCollections;
            return this;
        }

        public ServerConfiguration.Builder displaySpecialsWithinSeasons(Boolean displaySpecialsWithinSeasons) {
            this.instance.displaySpecialsWithinSeasons = displaySpecialsWithinSeasons;
            return this;
        }

        public ServerConfiguration.Builder codecsUsed(List<String> codecsUsed) {
            this.instance.codecsUsed = codecsUsed;
            return this;
        }

        public ServerConfiguration.Builder pluginRepositories(List<RepositoryInfo> pluginRepositories) {
            this.instance.pluginRepositories = pluginRepositories;
            return this;
        }

        public ServerConfiguration.Builder enableExternalContentInSuggestions(
                Boolean enableExternalContentInSuggestions) {
            this.instance.enableExternalContentInSuggestions = enableExternalContentInSuggestions;
            return this;
        }

        public ServerConfiguration.Builder imageExtractionTimeoutMs(Integer imageExtractionTimeoutMs) {
            this.instance.imageExtractionTimeoutMs = imageExtractionTimeoutMs;
            return this;
        }

        public ServerConfiguration.Builder pathSubstitutions(List<PathSubstitution> pathSubstitutions) {
            this.instance.pathSubstitutions = pathSubstitutions;
            return this;
        }

        public ServerConfiguration.Builder enableSlowResponseWarning(Boolean enableSlowResponseWarning) {
            this.instance.enableSlowResponseWarning = enableSlowResponseWarning;
            return this;
        }

        public ServerConfiguration.Builder slowResponseThresholdMs(Long slowResponseThresholdMs) {
            this.instance.slowResponseThresholdMs = slowResponseThresholdMs;
            return this;
        }

        public ServerConfiguration.Builder corsHosts(List<String> corsHosts) {
            this.instance.corsHosts = corsHosts;
            return this;
        }

        public ServerConfiguration.Builder activityLogRetentionDays(Integer activityLogRetentionDays) {
            this.instance.activityLogRetentionDays = activityLogRetentionDays;
            return this;
        }

        public ServerConfiguration.Builder libraryScanFanoutConcurrency(Integer libraryScanFanoutConcurrency) {
            this.instance.libraryScanFanoutConcurrency = libraryScanFanoutConcurrency;
            return this;
        }

        public ServerConfiguration.Builder libraryMetadataRefreshConcurrency(
                Integer libraryMetadataRefreshConcurrency) {
            this.instance.libraryMetadataRefreshConcurrency = libraryMetadataRefreshConcurrency;
            return this;
        }

        public ServerConfiguration.Builder removeOldPlugins(Boolean removeOldPlugins) {
            this.instance.removeOldPlugins = removeOldPlugins;
            return this;
        }

        public ServerConfiguration.Builder allowClientLogUpload(Boolean allowClientLogUpload) {
            this.instance.allowClientLogUpload = allowClientLogUpload;
            return this;
        }

        /**
         * returns a built ServerConfiguration instance.
         *
         * The builder is not reusable.
         */
        public ServerConfiguration build() {
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
    public static ServerConfiguration.Builder builder() {
        return new ServerConfiguration.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ServerConfiguration.Builder toBuilder() {
        return new ServerConfiguration.Builder().logFileRetentionDays(getLogFileRetentionDays())
                .isStartupWizardCompleted(getIsStartupWizardCompleted()).cachePath(getCachePath())
                .previousVersion(getPreviousVersion()).previousVersionStr(getPreviousVersionStr())
                .enableMetrics(getEnableMetrics()).enableNormalizedItemByNameIds(getEnableNormalizedItemByNameIds())
                .isPortAuthorized(getIsPortAuthorized()).quickConnectAvailable(getQuickConnectAvailable())
                .enableCaseSensitiveItemIds(getEnableCaseSensitiveItemIds())
                .disableLiveTvChannelUserDataName(getDisableLiveTvChannelUserDataName()).metadataPath(getMetadataPath())
                .metadataNetworkPath(getMetadataNetworkPath()).preferredMetadataLanguage(getPreferredMetadataLanguage())
                .metadataCountryCode(getMetadataCountryCode()).sortReplaceCharacters(getSortReplaceCharacters())
                .sortRemoveCharacters(getSortRemoveCharacters()).sortRemoveWords(getSortRemoveWords())
                .minResumePct(getMinResumePct()).maxResumePct(getMaxResumePct())
                .minResumeDurationSeconds(getMinResumeDurationSeconds()).minAudiobookResume(getMinAudiobookResume())
                .maxAudiobookResume(getMaxAudiobookResume()).libraryMonitorDelay(getLibraryMonitorDelay())
                .imageSavingConvention(getImageSavingConvention()).metadataOptions(getMetadataOptions())
                .skipDeserializationForBasicTypes(getSkipDeserializationForBasicTypes()).serverName(getServerName())
                .uiCulture(getUiCulture()).saveMetadataHidden(getSaveMetadataHidden()).contentTypes(getContentTypes())
                .remoteClientBitrateLimit(getRemoteClientBitrateLimit()).enableFolderView(getEnableFolderView())
                .enableGroupingIntoCollections(getEnableGroupingIntoCollections())
                .displaySpecialsWithinSeasons(getDisplaySpecialsWithinSeasons()).codecsUsed(getCodecsUsed())
                .pluginRepositories(getPluginRepositories())
                .enableExternalContentInSuggestions(getEnableExternalContentInSuggestions())
                .imageExtractionTimeoutMs(getImageExtractionTimeoutMs()).pathSubstitutions(getPathSubstitutions())
                .enableSlowResponseWarning(getEnableSlowResponseWarning())
                .slowResponseThresholdMs(getSlowResponseThresholdMs()).corsHosts(getCorsHosts())
                .activityLogRetentionDays(getActivityLogRetentionDays())
                .libraryScanFanoutConcurrency(getLibraryScanFanoutConcurrency())
                .libraryMetadataRefreshConcurrency(getLibraryMetadataRefreshConcurrency())
                .removeOldPlugins(getRemoveOldPlugins()).allowClientLogUpload(getAllowClientLogUpload());
    }
}

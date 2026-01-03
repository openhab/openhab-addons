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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * LibraryOptions
 */
@JsonPropertyOrder({ LibraryOptions.JSON_PROPERTY_ENABLED, LibraryOptions.JSON_PROPERTY_ENABLE_PHOTOS,
        LibraryOptions.JSON_PROPERTY_ENABLE_REALTIME_MONITOR, LibraryOptions.JSON_PROPERTY_ENABLE_L_U_F_S_SCAN,
        LibraryOptions.JSON_PROPERTY_ENABLE_CHAPTER_IMAGE_EXTRACTION,
        LibraryOptions.JSON_PROPERTY_EXTRACT_CHAPTER_IMAGES_DURING_LIBRARY_SCAN,
        LibraryOptions.JSON_PROPERTY_ENABLE_TRICKPLAY_IMAGE_EXTRACTION,
        LibraryOptions.JSON_PROPERTY_EXTRACT_TRICKPLAY_IMAGES_DURING_LIBRARY_SCAN,
        LibraryOptions.JSON_PROPERTY_PATH_INFOS, LibraryOptions.JSON_PROPERTY_SAVE_LOCAL_METADATA,
        LibraryOptions.JSON_PROPERTY_ENABLE_INTERNET_PROVIDERS,
        LibraryOptions.JSON_PROPERTY_ENABLE_AUTOMATIC_SERIES_GROUPING,
        LibraryOptions.JSON_PROPERTY_ENABLE_EMBEDDED_TITLES, LibraryOptions.JSON_PROPERTY_ENABLE_EMBEDDED_EXTRAS_TITLES,
        LibraryOptions.JSON_PROPERTY_ENABLE_EMBEDDED_EPISODE_INFOS,
        LibraryOptions.JSON_PROPERTY_AUTOMATIC_REFRESH_INTERVAL_DAYS,
        LibraryOptions.JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE, LibraryOptions.JSON_PROPERTY_METADATA_COUNTRY_CODE,
        LibraryOptions.JSON_PROPERTY_SEASON_ZERO_DISPLAY_NAME, LibraryOptions.JSON_PROPERTY_METADATA_SAVERS,
        LibraryOptions.JSON_PROPERTY_DISABLED_LOCAL_METADATA_READERS,
        LibraryOptions.JSON_PROPERTY_LOCAL_METADATA_READER_ORDER,
        LibraryOptions.JSON_PROPERTY_DISABLED_SUBTITLE_FETCHERS, LibraryOptions.JSON_PROPERTY_SUBTITLE_FETCHER_ORDER,
        LibraryOptions.JSON_PROPERTY_DISABLED_MEDIA_SEGMENT_PROVIDERS,
        LibraryOptions.JSON_PROPERTY_MEDIA_SEGMENT_PROVIDER_ORDER,
        LibraryOptions.JSON_PROPERTY_SKIP_SUBTITLES_IF_EMBEDDED_SUBTITLES_PRESENT,
        LibraryOptions.JSON_PROPERTY_SKIP_SUBTITLES_IF_AUDIO_TRACK_MATCHES,
        LibraryOptions.JSON_PROPERTY_SUBTITLE_DOWNLOAD_LANGUAGES,
        LibraryOptions.JSON_PROPERTY_REQUIRE_PERFECT_SUBTITLE_MATCH,
        LibraryOptions.JSON_PROPERTY_SAVE_SUBTITLES_WITH_MEDIA, LibraryOptions.JSON_PROPERTY_SAVE_LYRICS_WITH_MEDIA,
        LibraryOptions.JSON_PROPERTY_SAVE_TRICKPLAY_WITH_MEDIA, LibraryOptions.JSON_PROPERTY_DISABLED_LYRIC_FETCHERS,
        LibraryOptions.JSON_PROPERTY_LYRIC_FETCHER_ORDER, LibraryOptions.JSON_PROPERTY_PREFER_NONSTANDARD_ARTISTS_TAG,
        LibraryOptions.JSON_PROPERTY_USE_CUSTOM_TAG_DELIMITERS, LibraryOptions.JSON_PROPERTY_CUSTOM_TAG_DELIMITERS,
        LibraryOptions.JSON_PROPERTY_DELIMITER_WHITELIST, LibraryOptions.JSON_PROPERTY_AUTOMATICALLY_ADD_TO_COLLECTION,
        LibraryOptions.JSON_PROPERTY_ALLOW_EMBEDDED_SUBTITLES, LibraryOptions.JSON_PROPERTY_TYPE_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryOptions {
    public static final String JSON_PROPERTY_ENABLED = "Enabled";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enabled;

    public static final String JSON_PROPERTY_ENABLE_PHOTOS = "EnablePhotos";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enablePhotos;

    public static final String JSON_PROPERTY_ENABLE_REALTIME_MONITOR = "EnableRealtimeMonitor";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableRealtimeMonitor;

    public static final String JSON_PROPERTY_ENABLE_L_U_F_S_SCAN = "EnableLUFSScan";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableLUFSScan;

    public static final String JSON_PROPERTY_ENABLE_CHAPTER_IMAGE_EXTRACTION = "EnableChapterImageExtraction";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableChapterImageExtraction;

    public static final String JSON_PROPERTY_EXTRACT_CHAPTER_IMAGES_DURING_LIBRARY_SCAN = "ExtractChapterImagesDuringLibraryScan";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean extractChapterImagesDuringLibraryScan;

    public static final String JSON_PROPERTY_ENABLE_TRICKPLAY_IMAGE_EXTRACTION = "EnableTrickplayImageExtraction";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableTrickplayImageExtraction;

    public static final String JSON_PROPERTY_EXTRACT_TRICKPLAY_IMAGES_DURING_LIBRARY_SCAN = "ExtractTrickplayImagesDuringLibraryScan";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean extractTrickplayImagesDuringLibraryScan;

    public static final String JSON_PROPERTY_PATH_INFOS = "PathInfos";
    @org.eclipse.jdt.annotation.Nullable
    private List<MediaPathInfo> pathInfos = new ArrayList<>();

    public static final String JSON_PROPERTY_SAVE_LOCAL_METADATA = "SaveLocalMetadata";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean saveLocalMetadata;

    public static final String JSON_PROPERTY_ENABLE_INTERNET_PROVIDERS = "EnableInternetProviders";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableInternetProviders;

    public static final String JSON_PROPERTY_ENABLE_AUTOMATIC_SERIES_GROUPING = "EnableAutomaticSeriesGrouping";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableAutomaticSeriesGrouping;

    public static final String JSON_PROPERTY_ENABLE_EMBEDDED_TITLES = "EnableEmbeddedTitles";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableEmbeddedTitles;

    public static final String JSON_PROPERTY_ENABLE_EMBEDDED_EXTRAS_TITLES = "EnableEmbeddedExtrasTitles";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableEmbeddedExtrasTitles;

    public static final String JSON_PROPERTY_ENABLE_EMBEDDED_EPISODE_INFOS = "EnableEmbeddedEpisodeInfos";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableEmbeddedEpisodeInfos;

    public static final String JSON_PROPERTY_AUTOMATIC_REFRESH_INTERVAL_DAYS = "AutomaticRefreshIntervalDays";
    @org.eclipse.jdt.annotation.Nullable
    private Integer automaticRefreshIntervalDays;

    public static final String JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE = "PreferredMetadataLanguage";
    @org.eclipse.jdt.annotation.Nullable
    private String preferredMetadataLanguage;

    public static final String JSON_PROPERTY_METADATA_COUNTRY_CODE = "MetadataCountryCode";
    @org.eclipse.jdt.annotation.Nullable
    private String metadataCountryCode;

    public static final String JSON_PROPERTY_SEASON_ZERO_DISPLAY_NAME = "SeasonZeroDisplayName";
    @org.eclipse.jdt.annotation.Nullable
    private String seasonZeroDisplayName;

    public static final String JSON_PROPERTY_METADATA_SAVERS = "MetadataSavers";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> metadataSavers;

    public static final String JSON_PROPERTY_DISABLED_LOCAL_METADATA_READERS = "DisabledLocalMetadataReaders";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> disabledLocalMetadataReaders = new ArrayList<>();

    public static final String JSON_PROPERTY_LOCAL_METADATA_READER_ORDER = "LocalMetadataReaderOrder";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> localMetadataReaderOrder;

    public static final String JSON_PROPERTY_DISABLED_SUBTITLE_FETCHERS = "DisabledSubtitleFetchers";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> disabledSubtitleFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_SUBTITLE_FETCHER_ORDER = "SubtitleFetcherOrder";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> subtitleFetcherOrder = new ArrayList<>();

    public static final String JSON_PROPERTY_DISABLED_MEDIA_SEGMENT_PROVIDERS = "DisabledMediaSegmentProviders";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> disabledMediaSegmentProviders = new ArrayList<>();

    public static final String JSON_PROPERTY_MEDIA_SEGMENT_PROVIDER_ORDER = "MediaSegmentProviderOrder";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> mediaSegmentProviderOrder = new ArrayList<>();

    public static final String JSON_PROPERTY_SKIP_SUBTITLES_IF_EMBEDDED_SUBTITLES_PRESENT = "SkipSubtitlesIfEmbeddedSubtitlesPresent";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean skipSubtitlesIfEmbeddedSubtitlesPresent;

    public static final String JSON_PROPERTY_SKIP_SUBTITLES_IF_AUDIO_TRACK_MATCHES = "SkipSubtitlesIfAudioTrackMatches";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean skipSubtitlesIfAudioTrackMatches;

    public static final String JSON_PROPERTY_SUBTITLE_DOWNLOAD_LANGUAGES = "SubtitleDownloadLanguages";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> subtitleDownloadLanguages;

    public static final String JSON_PROPERTY_REQUIRE_PERFECT_SUBTITLE_MATCH = "RequirePerfectSubtitleMatch";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean requirePerfectSubtitleMatch;

    public static final String JSON_PROPERTY_SAVE_SUBTITLES_WITH_MEDIA = "SaveSubtitlesWithMedia";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean saveSubtitlesWithMedia;

    public static final String JSON_PROPERTY_SAVE_LYRICS_WITH_MEDIA = "SaveLyricsWithMedia";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean saveLyricsWithMedia = false;

    public static final String JSON_PROPERTY_SAVE_TRICKPLAY_WITH_MEDIA = "SaveTrickplayWithMedia";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean saveTrickplayWithMedia = false;

    public static final String JSON_PROPERTY_DISABLED_LYRIC_FETCHERS = "DisabledLyricFetchers";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> disabledLyricFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_LYRIC_FETCHER_ORDER = "LyricFetcherOrder";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> lyricFetcherOrder = new ArrayList<>();

    public static final String JSON_PROPERTY_PREFER_NONSTANDARD_ARTISTS_TAG = "PreferNonstandardArtistsTag";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean preferNonstandardArtistsTag = false;

    public static final String JSON_PROPERTY_USE_CUSTOM_TAG_DELIMITERS = "UseCustomTagDelimiters";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean useCustomTagDelimiters = false;

    public static final String JSON_PROPERTY_CUSTOM_TAG_DELIMITERS = "CustomTagDelimiters";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> customTagDelimiters = new ArrayList<>();

    public static final String JSON_PROPERTY_DELIMITER_WHITELIST = "DelimiterWhitelist";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> delimiterWhitelist = new ArrayList<>();

    public static final String JSON_PROPERTY_AUTOMATICALLY_ADD_TO_COLLECTION = "AutomaticallyAddToCollection";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean automaticallyAddToCollection;

    public static final String JSON_PROPERTY_ALLOW_EMBEDDED_SUBTITLES = "AllowEmbeddedSubtitles";
    @org.eclipse.jdt.annotation.Nullable
    private EmbeddedSubtitleOptions allowEmbeddedSubtitles;

    public static final String JSON_PROPERTY_TYPE_OPTIONS = "TypeOptions";
    @org.eclipse.jdt.annotation.Nullable
    private List<TypeOptions> typeOptions = new ArrayList<>();

    public LibraryOptions() {
    }

    public LibraryOptions enabled(@org.eclipse.jdt.annotation.Nullable Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get enabled
     * 
     * @return enabled
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabled(@org.eclipse.jdt.annotation.Nullable Boolean enabled) {
        this.enabled = enabled;
    }

    public LibraryOptions enablePhotos(@org.eclipse.jdt.annotation.Nullable Boolean enablePhotos) {
        this.enablePhotos = enablePhotos;
        return this;
    }

    /**
     * Get enablePhotos
     * 
     * @return enablePhotos
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_PHOTOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnablePhotos() {
        return enablePhotos;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_PHOTOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnablePhotos(@org.eclipse.jdt.annotation.Nullable Boolean enablePhotos) {
        this.enablePhotos = enablePhotos;
    }

    public LibraryOptions enableRealtimeMonitor(@org.eclipse.jdt.annotation.Nullable Boolean enableRealtimeMonitor) {
        this.enableRealtimeMonitor = enableRealtimeMonitor;
        return this;
    }

    /**
     * Get enableRealtimeMonitor
     * 
     * @return enableRealtimeMonitor
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_REALTIME_MONITOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableRealtimeMonitor() {
        return enableRealtimeMonitor;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_REALTIME_MONITOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableRealtimeMonitor(@org.eclipse.jdt.annotation.Nullable Boolean enableRealtimeMonitor) {
        this.enableRealtimeMonitor = enableRealtimeMonitor;
    }

    public LibraryOptions enableLUFSScan(@org.eclipse.jdt.annotation.Nullable Boolean enableLUFSScan) {
        this.enableLUFSScan = enableLUFSScan;
        return this;
    }

    /**
     * Get enableLUFSScan
     * 
     * @return enableLUFSScan
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_L_U_F_S_SCAN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableLUFSScan() {
        return enableLUFSScan;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_L_U_F_S_SCAN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableLUFSScan(@org.eclipse.jdt.annotation.Nullable Boolean enableLUFSScan) {
        this.enableLUFSScan = enableLUFSScan;
    }

    public LibraryOptions enableChapterImageExtraction(
            @org.eclipse.jdt.annotation.Nullable Boolean enableChapterImageExtraction) {
        this.enableChapterImageExtraction = enableChapterImageExtraction;
        return this;
    }

    /**
     * Get enableChapterImageExtraction
     * 
     * @return enableChapterImageExtraction
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_CHAPTER_IMAGE_EXTRACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableChapterImageExtraction() {
        return enableChapterImageExtraction;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_CHAPTER_IMAGE_EXTRACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableChapterImageExtraction(
            @org.eclipse.jdt.annotation.Nullable Boolean enableChapterImageExtraction) {
        this.enableChapterImageExtraction = enableChapterImageExtraction;
    }

    public LibraryOptions extractChapterImagesDuringLibraryScan(
            @org.eclipse.jdt.annotation.Nullable Boolean extractChapterImagesDuringLibraryScan) {
        this.extractChapterImagesDuringLibraryScan = extractChapterImagesDuringLibraryScan;
        return this;
    }

    /**
     * Get extractChapterImagesDuringLibraryScan
     * 
     * @return extractChapterImagesDuringLibraryScan
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EXTRACT_CHAPTER_IMAGES_DURING_LIBRARY_SCAN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getExtractChapterImagesDuringLibraryScan() {
        return extractChapterImagesDuringLibraryScan;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTRACT_CHAPTER_IMAGES_DURING_LIBRARY_SCAN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExtractChapterImagesDuringLibraryScan(
            @org.eclipse.jdt.annotation.Nullable Boolean extractChapterImagesDuringLibraryScan) {
        this.extractChapterImagesDuringLibraryScan = extractChapterImagesDuringLibraryScan;
    }

    public LibraryOptions enableTrickplayImageExtraction(
            @org.eclipse.jdt.annotation.Nullable Boolean enableTrickplayImageExtraction) {
        this.enableTrickplayImageExtraction = enableTrickplayImageExtraction;
        return this;
    }

    /**
     * Get enableTrickplayImageExtraction
     * 
     * @return enableTrickplayImageExtraction
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_TRICKPLAY_IMAGE_EXTRACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableTrickplayImageExtraction() {
        return enableTrickplayImageExtraction;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_TRICKPLAY_IMAGE_EXTRACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableTrickplayImageExtraction(
            @org.eclipse.jdt.annotation.Nullable Boolean enableTrickplayImageExtraction) {
        this.enableTrickplayImageExtraction = enableTrickplayImageExtraction;
    }

    public LibraryOptions extractTrickplayImagesDuringLibraryScan(
            @org.eclipse.jdt.annotation.Nullable Boolean extractTrickplayImagesDuringLibraryScan) {
        this.extractTrickplayImagesDuringLibraryScan = extractTrickplayImagesDuringLibraryScan;
        return this;
    }

    /**
     * Get extractTrickplayImagesDuringLibraryScan
     * 
     * @return extractTrickplayImagesDuringLibraryScan
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EXTRACT_TRICKPLAY_IMAGES_DURING_LIBRARY_SCAN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getExtractTrickplayImagesDuringLibraryScan() {
        return extractTrickplayImagesDuringLibraryScan;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTRACT_TRICKPLAY_IMAGES_DURING_LIBRARY_SCAN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExtractTrickplayImagesDuringLibraryScan(
            @org.eclipse.jdt.annotation.Nullable Boolean extractTrickplayImagesDuringLibraryScan) {
        this.extractTrickplayImagesDuringLibraryScan = extractTrickplayImagesDuringLibraryScan;
    }

    public LibraryOptions pathInfos(@org.eclipse.jdt.annotation.Nullable List<MediaPathInfo> pathInfos) {
        this.pathInfos = pathInfos;
        return this;
    }

    public LibraryOptions addPathInfosItem(MediaPathInfo pathInfosItem) {
        if (this.pathInfos == null) {
            this.pathInfos = new ArrayList<>();
        }
        this.pathInfos.add(pathInfosItem);
        return this;
    }

    /**
     * Get pathInfos
     * 
     * @return pathInfos
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PATH_INFOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaPathInfo> getPathInfos() {
        return pathInfos;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH_INFOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPathInfos(@org.eclipse.jdt.annotation.Nullable List<MediaPathInfo> pathInfos) {
        this.pathInfos = pathInfos;
    }

    public LibraryOptions saveLocalMetadata(@org.eclipse.jdt.annotation.Nullable Boolean saveLocalMetadata) {
        this.saveLocalMetadata = saveLocalMetadata;
        return this;
    }

    /**
     * Get saveLocalMetadata
     * 
     * @return saveLocalMetadata
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SAVE_LOCAL_METADATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveLocalMetadata() {
        return saveLocalMetadata;
    }

    @JsonProperty(value = JSON_PROPERTY_SAVE_LOCAL_METADATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveLocalMetadata(@org.eclipse.jdt.annotation.Nullable Boolean saveLocalMetadata) {
        this.saveLocalMetadata = saveLocalMetadata;
    }

    public LibraryOptions enableInternetProviders(
            @org.eclipse.jdt.annotation.Nullable Boolean enableInternetProviders) {
        this.enableInternetProviders = enableInternetProviders;
        return this;
    }

    /**
     * Get enableInternetProviders
     * 
     * @return enableInternetProviders
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_INTERNET_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableInternetProviders() {
        return enableInternetProviders;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_INTERNET_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableInternetProviders(@org.eclipse.jdt.annotation.Nullable Boolean enableInternetProviders) {
        this.enableInternetProviders = enableInternetProviders;
    }

    public LibraryOptions enableAutomaticSeriesGrouping(
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutomaticSeriesGrouping) {
        this.enableAutomaticSeriesGrouping = enableAutomaticSeriesGrouping;
        return this;
    }

    /**
     * Get enableAutomaticSeriesGrouping
     * 
     * @return enableAutomaticSeriesGrouping
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUTOMATIC_SERIES_GROUPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAutomaticSeriesGrouping() {
        return enableAutomaticSeriesGrouping;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUTOMATIC_SERIES_GROUPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAutomaticSeriesGrouping(
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutomaticSeriesGrouping) {
        this.enableAutomaticSeriesGrouping = enableAutomaticSeriesGrouping;
    }

    public LibraryOptions enableEmbeddedTitles(@org.eclipse.jdt.annotation.Nullable Boolean enableEmbeddedTitles) {
        this.enableEmbeddedTitles = enableEmbeddedTitles;
        return this;
    }

    /**
     * Get enableEmbeddedTitles
     * 
     * @return enableEmbeddedTitles
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_EMBEDDED_TITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableEmbeddedTitles() {
        return enableEmbeddedTitles;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_EMBEDDED_TITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableEmbeddedTitles(@org.eclipse.jdt.annotation.Nullable Boolean enableEmbeddedTitles) {
        this.enableEmbeddedTitles = enableEmbeddedTitles;
    }

    public LibraryOptions enableEmbeddedExtrasTitles(
            @org.eclipse.jdt.annotation.Nullable Boolean enableEmbeddedExtrasTitles) {
        this.enableEmbeddedExtrasTitles = enableEmbeddedExtrasTitles;
        return this;
    }

    /**
     * Get enableEmbeddedExtrasTitles
     * 
     * @return enableEmbeddedExtrasTitles
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_EMBEDDED_EXTRAS_TITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableEmbeddedExtrasTitles() {
        return enableEmbeddedExtrasTitles;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_EMBEDDED_EXTRAS_TITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableEmbeddedExtrasTitles(@org.eclipse.jdt.annotation.Nullable Boolean enableEmbeddedExtrasTitles) {
        this.enableEmbeddedExtrasTitles = enableEmbeddedExtrasTitles;
    }

    public LibraryOptions enableEmbeddedEpisodeInfos(
            @org.eclipse.jdt.annotation.Nullable Boolean enableEmbeddedEpisodeInfos) {
        this.enableEmbeddedEpisodeInfos = enableEmbeddedEpisodeInfos;
        return this;
    }

    /**
     * Get enableEmbeddedEpisodeInfos
     * 
     * @return enableEmbeddedEpisodeInfos
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_EMBEDDED_EPISODE_INFOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableEmbeddedEpisodeInfos() {
        return enableEmbeddedEpisodeInfos;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_EMBEDDED_EPISODE_INFOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableEmbeddedEpisodeInfos(@org.eclipse.jdt.annotation.Nullable Boolean enableEmbeddedEpisodeInfos) {
        this.enableEmbeddedEpisodeInfos = enableEmbeddedEpisodeInfos;
    }

    public LibraryOptions automaticRefreshIntervalDays(
            @org.eclipse.jdt.annotation.Nullable Integer automaticRefreshIntervalDays) {
        this.automaticRefreshIntervalDays = automaticRefreshIntervalDays;
        return this;
    }

    /**
     * Get automaticRefreshIntervalDays
     * 
     * @return automaticRefreshIntervalDays
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUTOMATIC_REFRESH_INTERVAL_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAutomaticRefreshIntervalDays() {
        return automaticRefreshIntervalDays;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTOMATIC_REFRESH_INTERVAL_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAutomaticRefreshIntervalDays(
            @org.eclipse.jdt.annotation.Nullable Integer automaticRefreshIntervalDays) {
        this.automaticRefreshIntervalDays = automaticRefreshIntervalDays;
    }

    public LibraryOptions preferredMetadataLanguage(
            @org.eclipse.jdt.annotation.Nullable String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
        return this;
    }

    /**
     * Gets or sets the preferred metadata language.
     * 
     * @return preferredMetadataLanguage
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreferredMetadataLanguage() {
        return preferredMetadataLanguage;
    }

    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredMetadataLanguage(@org.eclipse.jdt.annotation.Nullable String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
    }

    public LibraryOptions metadataCountryCode(@org.eclipse.jdt.annotation.Nullable String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
        return this;
    }

    /**
     * Gets or sets the metadata country code.
     * 
     * @return metadataCountryCode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataCountryCode() {
        return metadataCountryCode;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataCountryCode(@org.eclipse.jdt.annotation.Nullable String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
    }

    public LibraryOptions seasonZeroDisplayName(@org.eclipse.jdt.annotation.Nullable String seasonZeroDisplayName) {
        this.seasonZeroDisplayName = seasonZeroDisplayName;
        return this;
    }

    /**
     * Get seasonZeroDisplayName
     * 
     * @return seasonZeroDisplayName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SEASON_ZERO_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeasonZeroDisplayName() {
        return seasonZeroDisplayName;
    }

    @JsonProperty(value = JSON_PROPERTY_SEASON_ZERO_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeasonZeroDisplayName(@org.eclipse.jdt.annotation.Nullable String seasonZeroDisplayName) {
        this.seasonZeroDisplayName = seasonZeroDisplayName;
    }

    public LibraryOptions metadataSavers(@org.eclipse.jdt.annotation.Nullable List<String> metadataSavers) {
        this.metadataSavers = metadataSavers;
        return this;
    }

    public LibraryOptions addMetadataSaversItem(String metadataSaversItem) {
        if (this.metadataSavers == null) {
            this.metadataSavers = new ArrayList<>();
        }
        this.metadataSavers.add(metadataSaversItem);
        return this;
    }

    /**
     * Get metadataSavers
     * 
     * @return metadataSavers
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_METADATA_SAVERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getMetadataSavers() {
        return metadataSavers;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_SAVERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataSavers(@org.eclipse.jdt.annotation.Nullable List<String> metadataSavers) {
        this.metadataSavers = metadataSavers;
    }

    public LibraryOptions disabledLocalMetadataReaders(
            @org.eclipse.jdt.annotation.Nullable List<String> disabledLocalMetadataReaders) {
        this.disabledLocalMetadataReaders = disabledLocalMetadataReaders;
        return this;
    }

    public LibraryOptions addDisabledLocalMetadataReadersItem(String disabledLocalMetadataReadersItem) {
        if (this.disabledLocalMetadataReaders == null) {
            this.disabledLocalMetadataReaders = new ArrayList<>();
        }
        this.disabledLocalMetadataReaders.add(disabledLocalMetadataReadersItem);
        return this;
    }

    /**
     * Get disabledLocalMetadataReaders
     * 
     * @return disabledLocalMetadataReaders
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DISABLED_LOCAL_METADATA_READERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDisabledLocalMetadataReaders() {
        return disabledLocalMetadataReaders;
    }

    @JsonProperty(value = JSON_PROPERTY_DISABLED_LOCAL_METADATA_READERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledLocalMetadataReaders(
            @org.eclipse.jdt.annotation.Nullable List<String> disabledLocalMetadataReaders) {
        this.disabledLocalMetadataReaders = disabledLocalMetadataReaders;
    }

    public LibraryOptions localMetadataReaderOrder(
            @org.eclipse.jdt.annotation.Nullable List<String> localMetadataReaderOrder) {
        this.localMetadataReaderOrder = localMetadataReaderOrder;
        return this;
    }

    public LibraryOptions addLocalMetadataReaderOrderItem(String localMetadataReaderOrderItem) {
        if (this.localMetadataReaderOrder == null) {
            this.localMetadataReaderOrder = new ArrayList<>();
        }
        this.localMetadataReaderOrder.add(localMetadataReaderOrderItem);
        return this;
    }

    /**
     * Get localMetadataReaderOrder
     * 
     * @return localMetadataReaderOrder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCAL_METADATA_READER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getLocalMetadataReaderOrder() {
        return localMetadataReaderOrder;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCAL_METADATA_READER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalMetadataReaderOrder(
            @org.eclipse.jdt.annotation.Nullable List<String> localMetadataReaderOrder) {
        this.localMetadataReaderOrder = localMetadataReaderOrder;
    }

    public LibraryOptions disabledSubtitleFetchers(
            @org.eclipse.jdt.annotation.Nullable List<String> disabledSubtitleFetchers) {
        this.disabledSubtitleFetchers = disabledSubtitleFetchers;
        return this;
    }

    public LibraryOptions addDisabledSubtitleFetchersItem(String disabledSubtitleFetchersItem) {
        if (this.disabledSubtitleFetchers == null) {
            this.disabledSubtitleFetchers = new ArrayList<>();
        }
        this.disabledSubtitleFetchers.add(disabledSubtitleFetchersItem);
        return this;
    }

    /**
     * Get disabledSubtitleFetchers
     * 
     * @return disabledSubtitleFetchers
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DISABLED_SUBTITLE_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDisabledSubtitleFetchers() {
        return disabledSubtitleFetchers;
    }

    @JsonProperty(value = JSON_PROPERTY_DISABLED_SUBTITLE_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledSubtitleFetchers(
            @org.eclipse.jdt.annotation.Nullable List<String> disabledSubtitleFetchers) {
        this.disabledSubtitleFetchers = disabledSubtitleFetchers;
    }

    public LibraryOptions subtitleFetcherOrder(@org.eclipse.jdt.annotation.Nullable List<String> subtitleFetcherOrder) {
        this.subtitleFetcherOrder = subtitleFetcherOrder;
        return this;
    }

    public LibraryOptions addSubtitleFetcherOrderItem(String subtitleFetcherOrderItem) {
        if (this.subtitleFetcherOrder == null) {
            this.subtitleFetcherOrder = new ArrayList<>();
        }
        this.subtitleFetcherOrder.add(subtitleFetcherOrderItem);
        return this;
    }

    /**
     * Get subtitleFetcherOrder
     * 
     * @return subtitleFetcherOrder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_FETCHER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getSubtitleFetcherOrder() {
        return subtitleFetcherOrder;
    }

    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_FETCHER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleFetcherOrder(@org.eclipse.jdt.annotation.Nullable List<String> subtitleFetcherOrder) {
        this.subtitleFetcherOrder = subtitleFetcherOrder;
    }

    public LibraryOptions disabledMediaSegmentProviders(
            @org.eclipse.jdt.annotation.Nullable List<String> disabledMediaSegmentProviders) {
        this.disabledMediaSegmentProviders = disabledMediaSegmentProviders;
        return this;
    }

    public LibraryOptions addDisabledMediaSegmentProvidersItem(String disabledMediaSegmentProvidersItem) {
        if (this.disabledMediaSegmentProviders == null) {
            this.disabledMediaSegmentProviders = new ArrayList<>();
        }
        this.disabledMediaSegmentProviders.add(disabledMediaSegmentProvidersItem);
        return this;
    }

    /**
     * Get disabledMediaSegmentProviders
     * 
     * @return disabledMediaSegmentProviders
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DISABLED_MEDIA_SEGMENT_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDisabledMediaSegmentProviders() {
        return disabledMediaSegmentProviders;
    }

    @JsonProperty(value = JSON_PROPERTY_DISABLED_MEDIA_SEGMENT_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledMediaSegmentProviders(
            @org.eclipse.jdt.annotation.Nullable List<String> disabledMediaSegmentProviders) {
        this.disabledMediaSegmentProviders = disabledMediaSegmentProviders;
    }

    public LibraryOptions mediaSegmentProviderOrder(
            @org.eclipse.jdt.annotation.Nullable List<String> mediaSegmentProviderOrder) {
        this.mediaSegmentProviderOrder = mediaSegmentProviderOrder;
        return this;
    }

    public LibraryOptions addMediaSegmentProviderOrderItem(String mediaSegmentProviderOrderItem) {
        if (this.mediaSegmentProviderOrder == null) {
            this.mediaSegmentProviderOrder = new ArrayList<>();
        }
        this.mediaSegmentProviderOrder.add(mediaSegmentProviderOrderItem);
        return this;
    }

    /**
     * Get mediaSegmentProviderOrder
     * 
     * @return mediaSegmentProviderOrder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SEGMENT_PROVIDER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getMediaSegmentProviderOrder() {
        return mediaSegmentProviderOrder;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SEGMENT_PROVIDER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSegmentProviderOrder(
            @org.eclipse.jdt.annotation.Nullable List<String> mediaSegmentProviderOrder) {
        this.mediaSegmentProviderOrder = mediaSegmentProviderOrder;
    }

    public LibraryOptions skipSubtitlesIfEmbeddedSubtitlesPresent(
            @org.eclipse.jdt.annotation.Nullable Boolean skipSubtitlesIfEmbeddedSubtitlesPresent) {
        this.skipSubtitlesIfEmbeddedSubtitlesPresent = skipSubtitlesIfEmbeddedSubtitlesPresent;
        return this;
    }

    /**
     * Get skipSubtitlesIfEmbeddedSubtitlesPresent
     * 
     * @return skipSubtitlesIfEmbeddedSubtitlesPresent
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SKIP_SUBTITLES_IF_EMBEDDED_SUBTITLES_PRESENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSkipSubtitlesIfEmbeddedSubtitlesPresent() {
        return skipSubtitlesIfEmbeddedSubtitlesPresent;
    }

    @JsonProperty(value = JSON_PROPERTY_SKIP_SUBTITLES_IF_EMBEDDED_SUBTITLES_PRESENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSkipSubtitlesIfEmbeddedSubtitlesPresent(
            @org.eclipse.jdt.annotation.Nullable Boolean skipSubtitlesIfEmbeddedSubtitlesPresent) {
        this.skipSubtitlesIfEmbeddedSubtitlesPresent = skipSubtitlesIfEmbeddedSubtitlesPresent;
    }

    public LibraryOptions skipSubtitlesIfAudioTrackMatches(
            @org.eclipse.jdt.annotation.Nullable Boolean skipSubtitlesIfAudioTrackMatches) {
        this.skipSubtitlesIfAudioTrackMatches = skipSubtitlesIfAudioTrackMatches;
        return this;
    }

    /**
     * Get skipSubtitlesIfAudioTrackMatches
     * 
     * @return skipSubtitlesIfAudioTrackMatches
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SKIP_SUBTITLES_IF_AUDIO_TRACK_MATCHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSkipSubtitlesIfAudioTrackMatches() {
        return skipSubtitlesIfAudioTrackMatches;
    }

    @JsonProperty(value = JSON_PROPERTY_SKIP_SUBTITLES_IF_AUDIO_TRACK_MATCHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSkipSubtitlesIfAudioTrackMatches(
            @org.eclipse.jdt.annotation.Nullable Boolean skipSubtitlesIfAudioTrackMatches) {
        this.skipSubtitlesIfAudioTrackMatches = skipSubtitlesIfAudioTrackMatches;
    }

    public LibraryOptions subtitleDownloadLanguages(
            @org.eclipse.jdt.annotation.Nullable List<String> subtitleDownloadLanguages) {
        this.subtitleDownloadLanguages = subtitleDownloadLanguages;
        return this;
    }

    public LibraryOptions addSubtitleDownloadLanguagesItem(String subtitleDownloadLanguagesItem) {
        if (this.subtitleDownloadLanguages == null) {
            this.subtitleDownloadLanguages = new ArrayList<>();
        }
        this.subtitleDownloadLanguages.add(subtitleDownloadLanguagesItem);
        return this;
    }

    /**
     * Get subtitleDownloadLanguages
     * 
     * @return subtitleDownloadLanguages
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_DOWNLOAD_LANGUAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getSubtitleDownloadLanguages() {
        return subtitleDownloadLanguages;
    }

    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_DOWNLOAD_LANGUAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleDownloadLanguages(
            @org.eclipse.jdt.annotation.Nullable List<String> subtitleDownloadLanguages) {
        this.subtitleDownloadLanguages = subtitleDownloadLanguages;
    }

    public LibraryOptions requirePerfectSubtitleMatch(
            @org.eclipse.jdt.annotation.Nullable Boolean requirePerfectSubtitleMatch) {
        this.requirePerfectSubtitleMatch = requirePerfectSubtitleMatch;
        return this;
    }

    /**
     * Get requirePerfectSubtitleMatch
     * 
     * @return requirePerfectSubtitleMatch
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REQUIRE_PERFECT_SUBTITLE_MATCH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequirePerfectSubtitleMatch() {
        return requirePerfectSubtitleMatch;
    }

    @JsonProperty(value = JSON_PROPERTY_REQUIRE_PERFECT_SUBTITLE_MATCH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequirePerfectSubtitleMatch(
            @org.eclipse.jdt.annotation.Nullable Boolean requirePerfectSubtitleMatch) {
        this.requirePerfectSubtitleMatch = requirePerfectSubtitleMatch;
    }

    public LibraryOptions saveSubtitlesWithMedia(@org.eclipse.jdt.annotation.Nullable Boolean saveSubtitlesWithMedia) {
        this.saveSubtitlesWithMedia = saveSubtitlesWithMedia;
        return this;
    }

    /**
     * Get saveSubtitlesWithMedia
     * 
     * @return saveSubtitlesWithMedia
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SAVE_SUBTITLES_WITH_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveSubtitlesWithMedia() {
        return saveSubtitlesWithMedia;
    }

    @JsonProperty(value = JSON_PROPERTY_SAVE_SUBTITLES_WITH_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveSubtitlesWithMedia(@org.eclipse.jdt.annotation.Nullable Boolean saveSubtitlesWithMedia) {
        this.saveSubtitlesWithMedia = saveSubtitlesWithMedia;
    }

    public LibraryOptions saveLyricsWithMedia(@org.eclipse.jdt.annotation.Nullable Boolean saveLyricsWithMedia) {
        this.saveLyricsWithMedia = saveLyricsWithMedia;
        return this;
    }

    /**
     * Get saveLyricsWithMedia
     * 
     * @return saveLyricsWithMedia
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SAVE_LYRICS_WITH_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveLyricsWithMedia() {
        return saveLyricsWithMedia;
    }

    @JsonProperty(value = JSON_PROPERTY_SAVE_LYRICS_WITH_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveLyricsWithMedia(@org.eclipse.jdt.annotation.Nullable Boolean saveLyricsWithMedia) {
        this.saveLyricsWithMedia = saveLyricsWithMedia;
    }

    public LibraryOptions saveTrickplayWithMedia(@org.eclipse.jdt.annotation.Nullable Boolean saveTrickplayWithMedia) {
        this.saveTrickplayWithMedia = saveTrickplayWithMedia;
        return this;
    }

    /**
     * Get saveTrickplayWithMedia
     * 
     * @return saveTrickplayWithMedia
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SAVE_TRICKPLAY_WITH_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveTrickplayWithMedia() {
        return saveTrickplayWithMedia;
    }

    @JsonProperty(value = JSON_PROPERTY_SAVE_TRICKPLAY_WITH_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveTrickplayWithMedia(@org.eclipse.jdt.annotation.Nullable Boolean saveTrickplayWithMedia) {
        this.saveTrickplayWithMedia = saveTrickplayWithMedia;
    }

    public LibraryOptions disabledLyricFetchers(
            @org.eclipse.jdt.annotation.Nullable List<String> disabledLyricFetchers) {
        this.disabledLyricFetchers = disabledLyricFetchers;
        return this;
    }

    public LibraryOptions addDisabledLyricFetchersItem(String disabledLyricFetchersItem) {
        if (this.disabledLyricFetchers == null) {
            this.disabledLyricFetchers = new ArrayList<>();
        }
        this.disabledLyricFetchers.add(disabledLyricFetchersItem);
        return this;
    }

    /**
     * Get disabledLyricFetchers
     * 
     * @return disabledLyricFetchers
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DISABLED_LYRIC_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDisabledLyricFetchers() {
        return disabledLyricFetchers;
    }

    @JsonProperty(value = JSON_PROPERTY_DISABLED_LYRIC_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledLyricFetchers(@org.eclipse.jdt.annotation.Nullable List<String> disabledLyricFetchers) {
        this.disabledLyricFetchers = disabledLyricFetchers;
    }

    public LibraryOptions lyricFetcherOrder(@org.eclipse.jdt.annotation.Nullable List<String> lyricFetcherOrder) {
        this.lyricFetcherOrder = lyricFetcherOrder;
        return this;
    }

    public LibraryOptions addLyricFetcherOrderItem(String lyricFetcherOrderItem) {
        if (this.lyricFetcherOrder == null) {
            this.lyricFetcherOrder = new ArrayList<>();
        }
        this.lyricFetcherOrder.add(lyricFetcherOrderItem);
        return this;
    }

    /**
     * Get lyricFetcherOrder
     * 
     * @return lyricFetcherOrder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LYRIC_FETCHER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getLyricFetcherOrder() {
        return lyricFetcherOrder;
    }

    @JsonProperty(value = JSON_PROPERTY_LYRIC_FETCHER_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLyricFetcherOrder(@org.eclipse.jdt.annotation.Nullable List<String> lyricFetcherOrder) {
        this.lyricFetcherOrder = lyricFetcherOrder;
    }

    public LibraryOptions preferNonstandardArtistsTag(
            @org.eclipse.jdt.annotation.Nullable Boolean preferNonstandardArtistsTag) {
        this.preferNonstandardArtistsTag = preferNonstandardArtistsTag;
        return this;
    }

    /**
     * Get preferNonstandardArtistsTag
     * 
     * @return preferNonstandardArtistsTag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PREFER_NONSTANDARD_ARTISTS_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getPreferNonstandardArtistsTag() {
        return preferNonstandardArtistsTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PREFER_NONSTANDARD_ARTISTS_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferNonstandardArtistsTag(
            @org.eclipse.jdt.annotation.Nullable Boolean preferNonstandardArtistsTag) {
        this.preferNonstandardArtistsTag = preferNonstandardArtistsTag;
    }

    public LibraryOptions useCustomTagDelimiters(@org.eclipse.jdt.annotation.Nullable Boolean useCustomTagDelimiters) {
        this.useCustomTagDelimiters = useCustomTagDelimiters;
        return this;
    }

    /**
     * Get useCustomTagDelimiters
     * 
     * @return useCustomTagDelimiters
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_USE_CUSTOM_TAG_DELIMITERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getUseCustomTagDelimiters() {
        return useCustomTagDelimiters;
    }

    @JsonProperty(value = JSON_PROPERTY_USE_CUSTOM_TAG_DELIMITERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUseCustomTagDelimiters(@org.eclipse.jdt.annotation.Nullable Boolean useCustomTagDelimiters) {
        this.useCustomTagDelimiters = useCustomTagDelimiters;
    }

    public LibraryOptions customTagDelimiters(@org.eclipse.jdt.annotation.Nullable List<String> customTagDelimiters) {
        this.customTagDelimiters = customTagDelimiters;
        return this;
    }

    public LibraryOptions addCustomTagDelimitersItem(String customTagDelimitersItem) {
        if (this.customTagDelimiters == null) {
            this.customTagDelimiters = new ArrayList<>();
        }
        this.customTagDelimiters.add(customTagDelimitersItem);
        return this;
    }

    /**
     * Get customTagDelimiters
     * 
     * @return customTagDelimiters
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CUSTOM_TAG_DELIMITERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getCustomTagDelimiters() {
        return customTagDelimiters;
    }

    @JsonProperty(value = JSON_PROPERTY_CUSTOM_TAG_DELIMITERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomTagDelimiters(@org.eclipse.jdt.annotation.Nullable List<String> customTagDelimiters) {
        this.customTagDelimiters = customTagDelimiters;
    }

    public LibraryOptions delimiterWhitelist(@org.eclipse.jdt.annotation.Nullable List<String> delimiterWhitelist) {
        this.delimiterWhitelist = delimiterWhitelist;
        return this;
    }

    public LibraryOptions addDelimiterWhitelistItem(String delimiterWhitelistItem) {
        if (this.delimiterWhitelist == null) {
            this.delimiterWhitelist = new ArrayList<>();
        }
        this.delimiterWhitelist.add(delimiterWhitelistItem);
        return this;
    }

    /**
     * Get delimiterWhitelist
     * 
     * @return delimiterWhitelist
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DELIMITER_WHITELIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDelimiterWhitelist() {
        return delimiterWhitelist;
    }

    @JsonProperty(value = JSON_PROPERTY_DELIMITER_WHITELIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDelimiterWhitelist(@org.eclipse.jdt.annotation.Nullable List<String> delimiterWhitelist) {
        this.delimiterWhitelist = delimiterWhitelist;
    }

    public LibraryOptions automaticallyAddToCollection(
            @org.eclipse.jdt.annotation.Nullable Boolean automaticallyAddToCollection) {
        this.automaticallyAddToCollection = automaticallyAddToCollection;
        return this;
    }

    /**
     * Get automaticallyAddToCollection
     * 
     * @return automaticallyAddToCollection
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUTOMATICALLY_ADD_TO_COLLECTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAutomaticallyAddToCollection() {
        return automaticallyAddToCollection;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTOMATICALLY_ADD_TO_COLLECTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAutomaticallyAddToCollection(
            @org.eclipse.jdt.annotation.Nullable Boolean automaticallyAddToCollection) {
        this.automaticallyAddToCollection = automaticallyAddToCollection;
    }

    public LibraryOptions allowEmbeddedSubtitles(
            @org.eclipse.jdt.annotation.Nullable EmbeddedSubtitleOptions allowEmbeddedSubtitles) {
        this.allowEmbeddedSubtitles = allowEmbeddedSubtitles;
        return this;
    }

    /**
     * An enum representing the options to disable embedded subs.
     * 
     * @return allowEmbeddedSubtitles
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ALLOW_EMBEDDED_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public EmbeddedSubtitleOptions getAllowEmbeddedSubtitles() {
        return allowEmbeddedSubtitles;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_EMBEDDED_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowEmbeddedSubtitles(
            @org.eclipse.jdt.annotation.Nullable EmbeddedSubtitleOptions allowEmbeddedSubtitles) {
        this.allowEmbeddedSubtitles = allowEmbeddedSubtitles;
    }

    public LibraryOptions typeOptions(@org.eclipse.jdt.annotation.Nullable List<TypeOptions> typeOptions) {
        this.typeOptions = typeOptions;
        return this;
    }

    public LibraryOptions addTypeOptionsItem(TypeOptions typeOptionsItem) {
        if (this.typeOptions == null) {
            this.typeOptions = new ArrayList<>();
        }
        this.typeOptions.add(typeOptionsItem);
        return this;
    }

    /**
     * Get typeOptions
     * 
     * @return typeOptions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<TypeOptions> getTypeOptions() {
        return typeOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTypeOptions(@org.eclipse.jdt.annotation.Nullable List<TypeOptions> typeOptions) {
        this.typeOptions = typeOptions;
    }

    /**
     * Return true if this LibraryOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryOptions libraryOptions = (LibraryOptions) o;
        return Objects.equals(this.enabled, libraryOptions.enabled)
                && Objects.equals(this.enablePhotos, libraryOptions.enablePhotos)
                && Objects.equals(this.enableRealtimeMonitor, libraryOptions.enableRealtimeMonitor)
                && Objects.equals(this.enableLUFSScan, libraryOptions.enableLUFSScan)
                && Objects.equals(this.enableChapterImageExtraction, libraryOptions.enableChapterImageExtraction)
                && Objects.equals(this.extractChapterImagesDuringLibraryScan,
                        libraryOptions.extractChapterImagesDuringLibraryScan)
                && Objects.equals(this.enableTrickplayImageExtraction, libraryOptions.enableTrickplayImageExtraction)
                && Objects.equals(this.extractTrickplayImagesDuringLibraryScan,
                        libraryOptions.extractTrickplayImagesDuringLibraryScan)
                && Objects.equals(this.pathInfos, libraryOptions.pathInfos)
                && Objects.equals(this.saveLocalMetadata, libraryOptions.saveLocalMetadata)
                && Objects.equals(this.enableInternetProviders, libraryOptions.enableInternetProviders)
                && Objects.equals(this.enableAutomaticSeriesGrouping, libraryOptions.enableAutomaticSeriesGrouping)
                && Objects.equals(this.enableEmbeddedTitles, libraryOptions.enableEmbeddedTitles)
                && Objects.equals(this.enableEmbeddedExtrasTitles, libraryOptions.enableEmbeddedExtrasTitles)
                && Objects.equals(this.enableEmbeddedEpisodeInfos, libraryOptions.enableEmbeddedEpisodeInfos)
                && Objects.equals(this.automaticRefreshIntervalDays, libraryOptions.automaticRefreshIntervalDays)
                && Objects.equals(this.preferredMetadataLanguage, libraryOptions.preferredMetadataLanguage)
                && Objects.equals(this.metadataCountryCode, libraryOptions.metadataCountryCode)
                && Objects.equals(this.seasonZeroDisplayName, libraryOptions.seasonZeroDisplayName)
                && Objects.equals(this.metadataSavers, libraryOptions.metadataSavers)
                && Objects.equals(this.disabledLocalMetadataReaders, libraryOptions.disabledLocalMetadataReaders)
                && Objects.equals(this.localMetadataReaderOrder, libraryOptions.localMetadataReaderOrder)
                && Objects.equals(this.disabledSubtitleFetchers, libraryOptions.disabledSubtitleFetchers)
                && Objects.equals(this.subtitleFetcherOrder, libraryOptions.subtitleFetcherOrder)
                && Objects.equals(this.disabledMediaSegmentProviders, libraryOptions.disabledMediaSegmentProviders)
                && Objects.equals(this.mediaSegmentProviderOrder, libraryOptions.mediaSegmentProviderOrder)
                && Objects.equals(this.skipSubtitlesIfEmbeddedSubtitlesPresent,
                        libraryOptions.skipSubtitlesIfEmbeddedSubtitlesPresent)
                && Objects.equals(this.skipSubtitlesIfAudioTrackMatches,
                        libraryOptions.skipSubtitlesIfAudioTrackMatches)
                && Objects.equals(this.subtitleDownloadLanguages, libraryOptions.subtitleDownloadLanguages)
                && Objects.equals(this.requirePerfectSubtitleMatch, libraryOptions.requirePerfectSubtitleMatch)
                && Objects.equals(this.saveSubtitlesWithMedia, libraryOptions.saveSubtitlesWithMedia)
                && Objects.equals(this.saveLyricsWithMedia, libraryOptions.saveLyricsWithMedia)
                && Objects.equals(this.saveTrickplayWithMedia, libraryOptions.saveTrickplayWithMedia)
                && Objects.equals(this.disabledLyricFetchers, libraryOptions.disabledLyricFetchers)
                && Objects.equals(this.lyricFetcherOrder, libraryOptions.lyricFetcherOrder)
                && Objects.equals(this.preferNonstandardArtistsTag, libraryOptions.preferNonstandardArtistsTag)
                && Objects.equals(this.useCustomTagDelimiters, libraryOptions.useCustomTagDelimiters)
                && Objects.equals(this.customTagDelimiters, libraryOptions.customTagDelimiters)
                && Objects.equals(this.delimiterWhitelist, libraryOptions.delimiterWhitelist)
                && Objects.equals(this.automaticallyAddToCollection, libraryOptions.automaticallyAddToCollection)
                && Objects.equals(this.allowEmbeddedSubtitles, libraryOptions.allowEmbeddedSubtitles)
                && Objects.equals(this.typeOptions, libraryOptions.typeOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, enablePhotos, enableRealtimeMonitor, enableLUFSScan, enableChapterImageExtraction,
                extractChapterImagesDuringLibraryScan, enableTrickplayImageExtraction,
                extractTrickplayImagesDuringLibraryScan, pathInfos, saveLocalMetadata, enableInternetProviders,
                enableAutomaticSeriesGrouping, enableEmbeddedTitles, enableEmbeddedExtrasTitles,
                enableEmbeddedEpisodeInfos, automaticRefreshIntervalDays, preferredMetadataLanguage,
                metadataCountryCode, seasonZeroDisplayName, metadataSavers, disabledLocalMetadataReaders,
                localMetadataReaderOrder, disabledSubtitleFetchers, subtitleFetcherOrder, disabledMediaSegmentProviders,
                mediaSegmentProviderOrder, skipSubtitlesIfEmbeddedSubtitlesPresent, skipSubtitlesIfAudioTrackMatches,
                subtitleDownloadLanguages, requirePerfectSubtitleMatch, saveSubtitlesWithMedia, saveLyricsWithMedia,
                saveTrickplayWithMedia, disabledLyricFetchers, lyricFetcherOrder, preferNonstandardArtistsTag,
                useCustomTagDelimiters, customTagDelimiters, delimiterWhitelist, automaticallyAddToCollection,
                allowEmbeddedSubtitles, typeOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryOptions {\n");
        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
        sb.append("    enablePhotos: ").append(toIndentedString(enablePhotos)).append("\n");
        sb.append("    enableRealtimeMonitor: ").append(toIndentedString(enableRealtimeMonitor)).append("\n");
        sb.append("    enableLUFSScan: ").append(toIndentedString(enableLUFSScan)).append("\n");
        sb.append("    enableChapterImageExtraction: ").append(toIndentedString(enableChapterImageExtraction))
                .append("\n");
        sb.append("    extractChapterImagesDuringLibraryScan: ")
                .append(toIndentedString(extractChapterImagesDuringLibraryScan)).append("\n");
        sb.append("    enableTrickplayImageExtraction: ").append(toIndentedString(enableTrickplayImageExtraction))
                .append("\n");
        sb.append("    extractTrickplayImagesDuringLibraryScan: ")
                .append(toIndentedString(extractTrickplayImagesDuringLibraryScan)).append("\n");
        sb.append("    pathInfos: ").append(toIndentedString(pathInfos)).append("\n");
        sb.append("    saveLocalMetadata: ").append(toIndentedString(saveLocalMetadata)).append("\n");
        sb.append("    enableInternetProviders: ").append(toIndentedString(enableInternetProviders)).append("\n");
        sb.append("    enableAutomaticSeriesGrouping: ").append(toIndentedString(enableAutomaticSeriesGrouping))
                .append("\n");
        sb.append("    enableEmbeddedTitles: ").append(toIndentedString(enableEmbeddedTitles)).append("\n");
        sb.append("    enableEmbeddedExtrasTitles: ").append(toIndentedString(enableEmbeddedExtrasTitles)).append("\n");
        sb.append("    enableEmbeddedEpisodeInfos: ").append(toIndentedString(enableEmbeddedEpisodeInfos)).append("\n");
        sb.append("    automaticRefreshIntervalDays: ").append(toIndentedString(automaticRefreshIntervalDays))
                .append("\n");
        sb.append("    preferredMetadataLanguage: ").append(toIndentedString(preferredMetadataLanguage)).append("\n");
        sb.append("    metadataCountryCode: ").append(toIndentedString(metadataCountryCode)).append("\n");
        sb.append("    seasonZeroDisplayName: ").append(toIndentedString(seasonZeroDisplayName)).append("\n");
        sb.append("    metadataSavers: ").append(toIndentedString(metadataSavers)).append("\n");
        sb.append("    disabledLocalMetadataReaders: ").append(toIndentedString(disabledLocalMetadataReaders))
                .append("\n");
        sb.append("    localMetadataReaderOrder: ").append(toIndentedString(localMetadataReaderOrder)).append("\n");
        sb.append("    disabledSubtitleFetchers: ").append(toIndentedString(disabledSubtitleFetchers)).append("\n");
        sb.append("    subtitleFetcherOrder: ").append(toIndentedString(subtitleFetcherOrder)).append("\n");
        sb.append("    disabledMediaSegmentProviders: ").append(toIndentedString(disabledMediaSegmentProviders))
                .append("\n");
        sb.append("    mediaSegmentProviderOrder: ").append(toIndentedString(mediaSegmentProviderOrder)).append("\n");
        sb.append("    skipSubtitlesIfEmbeddedSubtitlesPresent: ")
                .append(toIndentedString(skipSubtitlesIfEmbeddedSubtitlesPresent)).append("\n");
        sb.append("    skipSubtitlesIfAudioTrackMatches: ").append(toIndentedString(skipSubtitlesIfAudioTrackMatches))
                .append("\n");
        sb.append("    subtitleDownloadLanguages: ").append(toIndentedString(subtitleDownloadLanguages)).append("\n");
        sb.append("    requirePerfectSubtitleMatch: ").append(toIndentedString(requirePerfectSubtitleMatch))
                .append("\n");
        sb.append("    saveSubtitlesWithMedia: ").append(toIndentedString(saveSubtitlesWithMedia)).append("\n");
        sb.append("    saveLyricsWithMedia: ").append(toIndentedString(saveLyricsWithMedia)).append("\n");
        sb.append("    saveTrickplayWithMedia: ").append(toIndentedString(saveTrickplayWithMedia)).append("\n");
        sb.append("    disabledLyricFetchers: ").append(toIndentedString(disabledLyricFetchers)).append("\n");
        sb.append("    lyricFetcherOrder: ").append(toIndentedString(lyricFetcherOrder)).append("\n");
        sb.append("    preferNonstandardArtistsTag: ").append(toIndentedString(preferNonstandardArtistsTag))
                .append("\n");
        sb.append("    useCustomTagDelimiters: ").append(toIndentedString(useCustomTagDelimiters)).append("\n");
        sb.append("    customTagDelimiters: ").append(toIndentedString(customTagDelimiters)).append("\n");
        sb.append("    delimiterWhitelist: ").append(toIndentedString(delimiterWhitelist)).append("\n");
        sb.append("    automaticallyAddToCollection: ").append(toIndentedString(automaticallyAddToCollection))
                .append("\n");
        sb.append("    allowEmbeddedSubtitles: ").append(toIndentedString(allowEmbeddedSubtitles)).append("\n");
        sb.append("    typeOptions: ").append(toIndentedString(typeOptions)).append("\n");
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

        // add `Enabled` to the URL query string
        if (getEnabled() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnabled()))));
        }

        // add `EnablePhotos` to the URL query string
        if (getEnablePhotos() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnablePhotos%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnablePhotos()))));
        }

        // add `EnableRealtimeMonitor` to the URL query string
        if (getEnableRealtimeMonitor() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableRealtimeMonitor%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableRealtimeMonitor()))));
        }

        // add `EnableLUFSScan` to the URL query string
        if (getEnableLUFSScan() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableLUFSScan%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableLUFSScan()))));
        }

        // add `EnableChapterImageExtraction` to the URL query string
        if (getEnableChapterImageExtraction() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableChapterImageExtraction%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableChapterImageExtraction()))));
        }

        // add `ExtractChapterImagesDuringLibraryScan` to the URL query string
        if (getExtractChapterImagesDuringLibraryScan() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sExtractChapterImagesDuringLibraryScan%s=%s", prefix,
                    suffix, ApiClient.urlEncode(ApiClient.valueToString(getExtractChapterImagesDuringLibraryScan()))));
        }

        // add `EnableTrickplayImageExtraction` to the URL query string
        if (getEnableTrickplayImageExtraction() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableTrickplayImageExtraction%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableTrickplayImageExtraction()))));
        }

        // add `ExtractTrickplayImagesDuringLibraryScan` to the URL query string
        if (getExtractTrickplayImagesDuringLibraryScan() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sExtractTrickplayImagesDuringLibraryScan%s=%s", prefix,
                    suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExtractTrickplayImagesDuringLibraryScan()))));
        }

        // add `PathInfos` to the URL query string
        if (getPathInfos() != null) {
            for (int i = 0; i < getPathInfos().size(); i++) {
                if (getPathInfos().get(i) != null) {
                    joiner.add(getPathInfos().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sPathInfos%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `SaveLocalMetadata` to the URL query string
        if (getSaveLocalMetadata() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSaveLocalMetadata%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveLocalMetadata()))));
        }

        // add `EnableInternetProviders` to the URL query string
        if (getEnableInternetProviders() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableInternetProviders%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableInternetProviders()))));
        }

        // add `EnableAutomaticSeriesGrouping` to the URL query string
        if (getEnableAutomaticSeriesGrouping() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableAutomaticSeriesGrouping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAutomaticSeriesGrouping()))));
        }

        // add `EnableEmbeddedTitles` to the URL query string
        if (getEnableEmbeddedTitles() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableEmbeddedTitles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableEmbeddedTitles()))));
        }

        // add `EnableEmbeddedExtrasTitles` to the URL query string
        if (getEnableEmbeddedExtrasTitles() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableEmbeddedExtrasTitles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableEmbeddedExtrasTitles()))));
        }

        // add `EnableEmbeddedEpisodeInfos` to the URL query string
        if (getEnableEmbeddedEpisodeInfos() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableEmbeddedEpisodeInfos%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableEmbeddedEpisodeInfos()))));
        }

        // add `AutomaticRefreshIntervalDays` to the URL query string
        if (getAutomaticRefreshIntervalDays() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAutomaticRefreshIntervalDays%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAutomaticRefreshIntervalDays()))));
        }

        // add `PreferredMetadataLanguage` to the URL query string
        if (getPreferredMetadataLanguage() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPreferredMetadataLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferredMetadataLanguage()))));
        }

        // add `MetadataCountryCode` to the URL query string
        if (getMetadataCountryCode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMetadataCountryCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataCountryCode()))));
        }

        // add `SeasonZeroDisplayName` to the URL query string
        if (getSeasonZeroDisplayName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSeasonZeroDisplayName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeasonZeroDisplayName()))));
        }

        // add `MetadataSavers` to the URL query string
        if (getMetadataSavers() != null) {
            for (int i = 0; i < getMetadataSavers().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sMetadataSavers%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getMetadataSavers().get(i)))));
            }
        }

        // add `DisabledLocalMetadataReaders` to the URL query string
        if (getDisabledLocalMetadataReaders() != null) {
            for (int i = 0; i < getDisabledLocalMetadataReaders().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sDisabledLocalMetadataReaders%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDisabledLocalMetadataReaders().get(i)))));
            }
        }

        // add `LocalMetadataReaderOrder` to the URL query string
        if (getLocalMetadataReaderOrder() != null) {
            for (int i = 0; i < getLocalMetadataReaderOrder().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sLocalMetadataReaderOrder%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getLocalMetadataReaderOrder().get(i)))));
            }
        }

        // add `DisabledSubtitleFetchers` to the URL query string
        if (getDisabledSubtitleFetchers() != null) {
            for (int i = 0; i < getDisabledSubtitleFetchers().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sDisabledSubtitleFetchers%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDisabledSubtitleFetchers().get(i)))));
            }
        }

        // add `SubtitleFetcherOrder` to the URL query string
        if (getSubtitleFetcherOrder() != null) {
            for (int i = 0; i < getSubtitleFetcherOrder().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sSubtitleFetcherOrder%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getSubtitleFetcherOrder().get(i)))));
            }
        }

        // add `DisabledMediaSegmentProviders` to the URL query string
        if (getDisabledMediaSegmentProviders() != null) {
            for (int i = 0; i < getDisabledMediaSegmentProviders().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sDisabledMediaSegmentProviders%s%s=%s", prefix,
                        suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDisabledMediaSegmentProviders().get(i)))));
            }
        }

        // add `MediaSegmentProviderOrder` to the URL query string
        if (getMediaSegmentProviderOrder() != null) {
            for (int i = 0; i < getMediaSegmentProviderOrder().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sMediaSegmentProviderOrder%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getMediaSegmentProviderOrder().get(i)))));
            }
        }

        // add `SkipSubtitlesIfEmbeddedSubtitlesPresent` to the URL query string
        if (getSkipSubtitlesIfEmbeddedSubtitlesPresent() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSkipSubtitlesIfEmbeddedSubtitlesPresent%s=%s", prefix,
                    suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSkipSubtitlesIfEmbeddedSubtitlesPresent()))));
        }

        // add `SkipSubtitlesIfAudioTrackMatches` to the URL query string
        if (getSkipSubtitlesIfAudioTrackMatches() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSkipSubtitlesIfAudioTrackMatches%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSkipSubtitlesIfAudioTrackMatches()))));
        }

        // add `SubtitleDownloadLanguages` to the URL query string
        if (getSubtitleDownloadLanguages() != null) {
            for (int i = 0; i < getSubtitleDownloadLanguages().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sSubtitleDownloadLanguages%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getSubtitleDownloadLanguages().get(i)))));
            }
        }

        // add `RequirePerfectSubtitleMatch` to the URL query string
        if (getRequirePerfectSubtitleMatch() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRequirePerfectSubtitleMatch%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequirePerfectSubtitleMatch()))));
        }

        // add `SaveSubtitlesWithMedia` to the URL query string
        if (getSaveSubtitlesWithMedia() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSaveSubtitlesWithMedia%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveSubtitlesWithMedia()))));
        }

        // add `SaveLyricsWithMedia` to the URL query string
        if (getSaveLyricsWithMedia() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSaveLyricsWithMedia%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveLyricsWithMedia()))));
        }

        // add `SaveTrickplayWithMedia` to the URL query string
        if (getSaveTrickplayWithMedia() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSaveTrickplayWithMedia%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveTrickplayWithMedia()))));
        }

        // add `DisabledLyricFetchers` to the URL query string
        if (getDisabledLyricFetchers() != null) {
            for (int i = 0; i < getDisabledLyricFetchers().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sDisabledLyricFetchers%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDisabledLyricFetchers().get(i)))));
            }
        }

        // add `LyricFetcherOrder` to the URL query string
        if (getLyricFetcherOrder() != null) {
            for (int i = 0; i < getLyricFetcherOrder().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sLyricFetcherOrder%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getLyricFetcherOrder().get(i)))));
            }
        }

        // add `PreferNonstandardArtistsTag` to the URL query string
        if (getPreferNonstandardArtistsTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPreferNonstandardArtistsTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferNonstandardArtistsTag()))));
        }

        // add `UseCustomTagDelimiters` to the URL query string
        if (getUseCustomTagDelimiters() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sUseCustomTagDelimiters%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUseCustomTagDelimiters()))));
        }

        // add `CustomTagDelimiters` to the URL query string
        if (getCustomTagDelimiters() != null) {
            for (int i = 0; i < getCustomTagDelimiters().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sCustomTagDelimiters%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getCustomTagDelimiters().get(i)))));
            }
        }

        // add `DelimiterWhitelist` to the URL query string
        if (getDelimiterWhitelist() != null) {
            for (int i = 0; i < getDelimiterWhitelist().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sDelimiterWhitelist%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDelimiterWhitelist().get(i)))));
            }
        }

        // add `AutomaticallyAddToCollection` to the URL query string
        if (getAutomaticallyAddToCollection() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAutomaticallyAddToCollection%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAutomaticallyAddToCollection()))));
        }

        // add `AllowEmbeddedSubtitles` to the URL query string
        if (getAllowEmbeddedSubtitles() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAllowEmbeddedSubtitles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowEmbeddedSubtitles()))));
        }

        // add `TypeOptions` to the URL query string
        if (getTypeOptions() != null) {
            for (int i = 0; i < getTypeOptions().size(); i++) {
                if (getTypeOptions().get(i) != null) {
                    joiner.add(getTypeOptions().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sTypeOptions%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private LibraryOptions instance;

        public Builder() {
            this(new LibraryOptions());
        }

        protected Builder(LibraryOptions instance) {
            this.instance = instance;
        }

        public LibraryOptions.Builder enabled(Boolean enabled) {
            this.instance.enabled = enabled;
            return this;
        }

        public LibraryOptions.Builder enablePhotos(Boolean enablePhotos) {
            this.instance.enablePhotos = enablePhotos;
            return this;
        }

        public LibraryOptions.Builder enableRealtimeMonitor(Boolean enableRealtimeMonitor) {
            this.instance.enableRealtimeMonitor = enableRealtimeMonitor;
            return this;
        }

        public LibraryOptions.Builder enableLUFSScan(Boolean enableLUFSScan) {
            this.instance.enableLUFSScan = enableLUFSScan;
            return this;
        }

        public LibraryOptions.Builder enableChapterImageExtraction(Boolean enableChapterImageExtraction) {
            this.instance.enableChapterImageExtraction = enableChapterImageExtraction;
            return this;
        }

        public LibraryOptions.Builder extractChapterImagesDuringLibraryScan(
                Boolean extractChapterImagesDuringLibraryScan) {
            this.instance.extractChapterImagesDuringLibraryScan = extractChapterImagesDuringLibraryScan;
            return this;
        }

        public LibraryOptions.Builder enableTrickplayImageExtraction(Boolean enableTrickplayImageExtraction) {
            this.instance.enableTrickplayImageExtraction = enableTrickplayImageExtraction;
            return this;
        }

        public LibraryOptions.Builder extractTrickplayImagesDuringLibraryScan(
                Boolean extractTrickplayImagesDuringLibraryScan) {
            this.instance.extractTrickplayImagesDuringLibraryScan = extractTrickplayImagesDuringLibraryScan;
            return this;
        }

        public LibraryOptions.Builder pathInfos(List<MediaPathInfo> pathInfos) {
            this.instance.pathInfos = pathInfos;
            return this;
        }

        public LibraryOptions.Builder saveLocalMetadata(Boolean saveLocalMetadata) {
            this.instance.saveLocalMetadata = saveLocalMetadata;
            return this;
        }

        public LibraryOptions.Builder enableInternetProviders(Boolean enableInternetProviders) {
            this.instance.enableInternetProviders = enableInternetProviders;
            return this;
        }

        public LibraryOptions.Builder enableAutomaticSeriesGrouping(Boolean enableAutomaticSeriesGrouping) {
            this.instance.enableAutomaticSeriesGrouping = enableAutomaticSeriesGrouping;
            return this;
        }

        public LibraryOptions.Builder enableEmbeddedTitles(Boolean enableEmbeddedTitles) {
            this.instance.enableEmbeddedTitles = enableEmbeddedTitles;
            return this;
        }

        public LibraryOptions.Builder enableEmbeddedExtrasTitles(Boolean enableEmbeddedExtrasTitles) {
            this.instance.enableEmbeddedExtrasTitles = enableEmbeddedExtrasTitles;
            return this;
        }

        public LibraryOptions.Builder enableEmbeddedEpisodeInfos(Boolean enableEmbeddedEpisodeInfos) {
            this.instance.enableEmbeddedEpisodeInfos = enableEmbeddedEpisodeInfos;
            return this;
        }

        public LibraryOptions.Builder automaticRefreshIntervalDays(Integer automaticRefreshIntervalDays) {
            this.instance.automaticRefreshIntervalDays = automaticRefreshIntervalDays;
            return this;
        }

        public LibraryOptions.Builder preferredMetadataLanguage(String preferredMetadataLanguage) {
            this.instance.preferredMetadataLanguage = preferredMetadataLanguage;
            return this;
        }

        public LibraryOptions.Builder metadataCountryCode(String metadataCountryCode) {
            this.instance.metadataCountryCode = metadataCountryCode;
            return this;
        }

        public LibraryOptions.Builder seasonZeroDisplayName(String seasonZeroDisplayName) {
            this.instance.seasonZeroDisplayName = seasonZeroDisplayName;
            return this;
        }

        public LibraryOptions.Builder metadataSavers(List<String> metadataSavers) {
            this.instance.metadataSavers = metadataSavers;
            return this;
        }

        public LibraryOptions.Builder disabledLocalMetadataReaders(List<String> disabledLocalMetadataReaders) {
            this.instance.disabledLocalMetadataReaders = disabledLocalMetadataReaders;
            return this;
        }

        public LibraryOptions.Builder localMetadataReaderOrder(List<String> localMetadataReaderOrder) {
            this.instance.localMetadataReaderOrder = localMetadataReaderOrder;
            return this;
        }

        public LibraryOptions.Builder disabledSubtitleFetchers(List<String> disabledSubtitleFetchers) {
            this.instance.disabledSubtitleFetchers = disabledSubtitleFetchers;
            return this;
        }

        public LibraryOptions.Builder subtitleFetcherOrder(List<String> subtitleFetcherOrder) {
            this.instance.subtitleFetcherOrder = subtitleFetcherOrder;
            return this;
        }

        public LibraryOptions.Builder disabledMediaSegmentProviders(List<String> disabledMediaSegmentProviders) {
            this.instance.disabledMediaSegmentProviders = disabledMediaSegmentProviders;
            return this;
        }

        public LibraryOptions.Builder mediaSegmentProviderOrder(List<String> mediaSegmentProviderOrder) {
            this.instance.mediaSegmentProviderOrder = mediaSegmentProviderOrder;
            return this;
        }

        public LibraryOptions.Builder skipSubtitlesIfEmbeddedSubtitlesPresent(
                Boolean skipSubtitlesIfEmbeddedSubtitlesPresent) {
            this.instance.skipSubtitlesIfEmbeddedSubtitlesPresent = skipSubtitlesIfEmbeddedSubtitlesPresent;
            return this;
        }

        public LibraryOptions.Builder skipSubtitlesIfAudioTrackMatches(Boolean skipSubtitlesIfAudioTrackMatches) {
            this.instance.skipSubtitlesIfAudioTrackMatches = skipSubtitlesIfAudioTrackMatches;
            return this;
        }

        public LibraryOptions.Builder subtitleDownloadLanguages(List<String> subtitleDownloadLanguages) {
            this.instance.subtitleDownloadLanguages = subtitleDownloadLanguages;
            return this;
        }

        public LibraryOptions.Builder requirePerfectSubtitleMatch(Boolean requirePerfectSubtitleMatch) {
            this.instance.requirePerfectSubtitleMatch = requirePerfectSubtitleMatch;
            return this;
        }

        public LibraryOptions.Builder saveSubtitlesWithMedia(Boolean saveSubtitlesWithMedia) {
            this.instance.saveSubtitlesWithMedia = saveSubtitlesWithMedia;
            return this;
        }

        public LibraryOptions.Builder saveLyricsWithMedia(Boolean saveLyricsWithMedia) {
            this.instance.saveLyricsWithMedia = saveLyricsWithMedia;
            return this;
        }

        public LibraryOptions.Builder saveTrickplayWithMedia(Boolean saveTrickplayWithMedia) {
            this.instance.saveTrickplayWithMedia = saveTrickplayWithMedia;
            return this;
        }

        public LibraryOptions.Builder disabledLyricFetchers(List<String> disabledLyricFetchers) {
            this.instance.disabledLyricFetchers = disabledLyricFetchers;
            return this;
        }

        public LibraryOptions.Builder lyricFetcherOrder(List<String> lyricFetcherOrder) {
            this.instance.lyricFetcherOrder = lyricFetcherOrder;
            return this;
        }

        public LibraryOptions.Builder preferNonstandardArtistsTag(Boolean preferNonstandardArtistsTag) {
            this.instance.preferNonstandardArtistsTag = preferNonstandardArtistsTag;
            return this;
        }

        public LibraryOptions.Builder useCustomTagDelimiters(Boolean useCustomTagDelimiters) {
            this.instance.useCustomTagDelimiters = useCustomTagDelimiters;
            return this;
        }

        public LibraryOptions.Builder customTagDelimiters(List<String> customTagDelimiters) {
            this.instance.customTagDelimiters = customTagDelimiters;
            return this;
        }

        public LibraryOptions.Builder delimiterWhitelist(List<String> delimiterWhitelist) {
            this.instance.delimiterWhitelist = delimiterWhitelist;
            return this;
        }

        public LibraryOptions.Builder automaticallyAddToCollection(Boolean automaticallyAddToCollection) {
            this.instance.automaticallyAddToCollection = automaticallyAddToCollection;
            return this;
        }

        public LibraryOptions.Builder allowEmbeddedSubtitles(EmbeddedSubtitleOptions allowEmbeddedSubtitles) {
            this.instance.allowEmbeddedSubtitles = allowEmbeddedSubtitles;
            return this;
        }

        public LibraryOptions.Builder typeOptions(List<TypeOptions> typeOptions) {
            this.instance.typeOptions = typeOptions;
            return this;
        }

        /**
         * returns a built LibraryOptions instance.
         *
         * The builder is not reusable.
         */
        public LibraryOptions build() {
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
    public static LibraryOptions.Builder builder() {
        return new LibraryOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LibraryOptions.Builder toBuilder() {
        return new LibraryOptions.Builder().enabled(getEnabled()).enablePhotos(getEnablePhotos())
                .enableRealtimeMonitor(getEnableRealtimeMonitor()).enableLUFSScan(getEnableLUFSScan())
                .enableChapterImageExtraction(getEnableChapterImageExtraction())
                .extractChapterImagesDuringLibraryScan(getExtractChapterImagesDuringLibraryScan())
                .enableTrickplayImageExtraction(getEnableTrickplayImageExtraction())
                .extractTrickplayImagesDuringLibraryScan(getExtractTrickplayImagesDuringLibraryScan())
                .pathInfos(getPathInfos()).saveLocalMetadata(getSaveLocalMetadata())
                .enableInternetProviders(getEnableInternetProviders())
                .enableAutomaticSeriesGrouping(getEnableAutomaticSeriesGrouping())
                .enableEmbeddedTitles(getEnableEmbeddedTitles())
                .enableEmbeddedExtrasTitles(getEnableEmbeddedExtrasTitles())
                .enableEmbeddedEpisodeInfos(getEnableEmbeddedEpisodeInfos())
                .automaticRefreshIntervalDays(getAutomaticRefreshIntervalDays())
                .preferredMetadataLanguage(getPreferredMetadataLanguage()).metadataCountryCode(getMetadataCountryCode())
                .seasonZeroDisplayName(getSeasonZeroDisplayName()).metadataSavers(getMetadataSavers())
                .disabledLocalMetadataReaders(getDisabledLocalMetadataReaders())
                .localMetadataReaderOrder(getLocalMetadataReaderOrder())
                .disabledSubtitleFetchers(getDisabledSubtitleFetchers()).subtitleFetcherOrder(getSubtitleFetcherOrder())
                .disabledMediaSegmentProviders(getDisabledMediaSegmentProviders())
                .mediaSegmentProviderOrder(getMediaSegmentProviderOrder())
                .skipSubtitlesIfEmbeddedSubtitlesPresent(getSkipSubtitlesIfEmbeddedSubtitlesPresent())
                .skipSubtitlesIfAudioTrackMatches(getSkipSubtitlesIfAudioTrackMatches())
                .subtitleDownloadLanguages(getSubtitleDownloadLanguages())
                .requirePerfectSubtitleMatch(getRequirePerfectSubtitleMatch())
                .saveSubtitlesWithMedia(getSaveSubtitlesWithMedia()).saveLyricsWithMedia(getSaveLyricsWithMedia())
                .saveTrickplayWithMedia(getSaveTrickplayWithMedia()).disabledLyricFetchers(getDisabledLyricFetchers())
                .lyricFetcherOrder(getLyricFetcherOrder()).preferNonstandardArtistsTag(getPreferNonstandardArtistsTag())
                .useCustomTagDelimiters(getUseCustomTagDelimiters()).customTagDelimiters(getCustomTagDelimiters())
                .delimiterWhitelist(getDelimiterWhitelist())
                .automaticallyAddToCollection(getAutomaticallyAddToCollection())
                .allowEmbeddedSubtitles(getAllowEmbeddedSubtitles()).typeOptions(getTypeOptions());
    }
}

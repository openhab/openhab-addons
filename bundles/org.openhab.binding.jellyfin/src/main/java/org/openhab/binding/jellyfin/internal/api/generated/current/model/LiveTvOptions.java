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
 * LiveTvOptions
 */
@JsonPropertyOrder({ LiveTvOptions.JSON_PROPERTY_GUIDE_DAYS, LiveTvOptions.JSON_PROPERTY_RECORDING_PATH,
        LiveTvOptions.JSON_PROPERTY_MOVIE_RECORDING_PATH, LiveTvOptions.JSON_PROPERTY_SERIES_RECORDING_PATH,
        LiveTvOptions.JSON_PROPERTY_ENABLE_RECORDING_SUBFOLDERS,
        LiveTvOptions.JSON_PROPERTY_ENABLE_ORIGINAL_AUDIO_WITH_ENCODED_RECORDINGS,
        LiveTvOptions.JSON_PROPERTY_TUNER_HOSTS, LiveTvOptions.JSON_PROPERTY_LISTING_PROVIDERS,
        LiveTvOptions.JSON_PROPERTY_PRE_PADDING_SECONDS, LiveTvOptions.JSON_PROPERTY_POST_PADDING_SECONDS,
        LiveTvOptions.JSON_PROPERTY_MEDIA_LOCATIONS_CREATED, LiveTvOptions.JSON_PROPERTY_RECORDING_POST_PROCESSOR,
        LiveTvOptions.JSON_PROPERTY_RECORDING_POST_PROCESSOR_ARGUMENTS,
        LiveTvOptions.JSON_PROPERTY_SAVE_RECORDING_N_F_O, LiveTvOptions.JSON_PROPERTY_SAVE_RECORDING_IMAGES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LiveTvOptions {
    public static final String JSON_PROPERTY_GUIDE_DAYS = "GuideDays";
    @org.eclipse.jdt.annotation.NonNull
    private Integer guideDays;

    public static final String JSON_PROPERTY_RECORDING_PATH = "RecordingPath";
    @org.eclipse.jdt.annotation.NonNull
    private String recordingPath;

    public static final String JSON_PROPERTY_MOVIE_RECORDING_PATH = "MovieRecordingPath";
    @org.eclipse.jdt.annotation.NonNull
    private String movieRecordingPath;

    public static final String JSON_PROPERTY_SERIES_RECORDING_PATH = "SeriesRecordingPath";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesRecordingPath;

    public static final String JSON_PROPERTY_ENABLE_RECORDING_SUBFOLDERS = "EnableRecordingSubfolders";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableRecordingSubfolders;

    public static final String JSON_PROPERTY_ENABLE_ORIGINAL_AUDIO_WITH_ENCODED_RECORDINGS = "EnableOriginalAudioWithEncodedRecordings";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableOriginalAudioWithEncodedRecordings;

    public static final String JSON_PROPERTY_TUNER_HOSTS = "TunerHosts";
    @org.eclipse.jdt.annotation.NonNull
    private List<TunerHostInfo> tunerHosts;

    public static final String JSON_PROPERTY_LISTING_PROVIDERS = "ListingProviders";
    @org.eclipse.jdt.annotation.NonNull
    private List<ListingsProviderInfo> listingProviders;

    public static final String JSON_PROPERTY_PRE_PADDING_SECONDS = "PrePaddingSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer prePaddingSeconds;

    public static final String JSON_PROPERTY_POST_PADDING_SECONDS = "PostPaddingSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer postPaddingSeconds;

    public static final String JSON_PROPERTY_MEDIA_LOCATIONS_CREATED = "MediaLocationsCreated";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> mediaLocationsCreated;

    public static final String JSON_PROPERTY_RECORDING_POST_PROCESSOR = "RecordingPostProcessor";
    @org.eclipse.jdt.annotation.NonNull
    private String recordingPostProcessor;

    public static final String JSON_PROPERTY_RECORDING_POST_PROCESSOR_ARGUMENTS = "RecordingPostProcessorArguments";
    @org.eclipse.jdt.annotation.NonNull
    private String recordingPostProcessorArguments;

    public static final String JSON_PROPERTY_SAVE_RECORDING_N_F_O = "SaveRecordingNFO";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean saveRecordingNFO;

    public static final String JSON_PROPERTY_SAVE_RECORDING_IMAGES = "SaveRecordingImages";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean saveRecordingImages;

    public LiveTvOptions() {
    }

    public LiveTvOptions guideDays(@org.eclipse.jdt.annotation.NonNull Integer guideDays) {
        this.guideDays = guideDays;
        return this;
    }

    /**
     * Get guideDays
     * 
     * @return guideDays
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_GUIDE_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getGuideDays() {
        return guideDays;
    }

    @JsonProperty(value = JSON_PROPERTY_GUIDE_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGuideDays(@org.eclipse.jdt.annotation.NonNull Integer guideDays) {
        this.guideDays = guideDays;
    }

    public LiveTvOptions recordingPath(@org.eclipse.jdt.annotation.NonNull String recordingPath) {
        this.recordingPath = recordingPath;
        return this;
    }

    /**
     * Get recordingPath
     * 
     * @return recordingPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RECORDING_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRecordingPath() {
        return recordingPath;
    }

    @JsonProperty(value = JSON_PROPERTY_RECORDING_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordingPath(@org.eclipse.jdt.annotation.NonNull String recordingPath) {
        this.recordingPath = recordingPath;
    }

    public LiveTvOptions movieRecordingPath(@org.eclipse.jdt.annotation.NonNull String movieRecordingPath) {
        this.movieRecordingPath = movieRecordingPath;
        return this;
    }

    /**
     * Get movieRecordingPath
     * 
     * @return movieRecordingPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MOVIE_RECORDING_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMovieRecordingPath() {
        return movieRecordingPath;
    }

    @JsonProperty(value = JSON_PROPERTY_MOVIE_RECORDING_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMovieRecordingPath(@org.eclipse.jdt.annotation.NonNull String movieRecordingPath) {
        this.movieRecordingPath = movieRecordingPath;
    }

    public LiveTvOptions seriesRecordingPath(@org.eclipse.jdt.annotation.NonNull String seriesRecordingPath) {
        this.seriesRecordingPath = seriesRecordingPath;
        return this;
    }

    /**
     * Get seriesRecordingPath
     * 
     * @return seriesRecordingPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_RECORDING_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeriesRecordingPath() {
        return seriesRecordingPath;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_RECORDING_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesRecordingPath(@org.eclipse.jdt.annotation.NonNull String seriesRecordingPath) {
        this.seriesRecordingPath = seriesRecordingPath;
    }

    public LiveTvOptions enableRecordingSubfolders(
            @org.eclipse.jdt.annotation.NonNull Boolean enableRecordingSubfolders) {
        this.enableRecordingSubfolders = enableRecordingSubfolders;
        return this;
    }

    /**
     * Get enableRecordingSubfolders
     * 
     * @return enableRecordingSubfolders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_RECORDING_SUBFOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableRecordingSubfolders() {
        return enableRecordingSubfolders;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_RECORDING_SUBFOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableRecordingSubfolders(@org.eclipse.jdt.annotation.NonNull Boolean enableRecordingSubfolders) {
        this.enableRecordingSubfolders = enableRecordingSubfolders;
    }

    public LiveTvOptions enableOriginalAudioWithEncodedRecordings(
            @org.eclipse.jdt.annotation.NonNull Boolean enableOriginalAudioWithEncodedRecordings) {
        this.enableOriginalAudioWithEncodedRecordings = enableOriginalAudioWithEncodedRecordings;
        return this;
    }

    /**
     * Get enableOriginalAudioWithEncodedRecordings
     * 
     * @return enableOriginalAudioWithEncodedRecordings
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_ORIGINAL_AUDIO_WITH_ENCODED_RECORDINGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableOriginalAudioWithEncodedRecordings() {
        return enableOriginalAudioWithEncodedRecordings;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_ORIGINAL_AUDIO_WITH_ENCODED_RECORDINGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableOriginalAudioWithEncodedRecordings(
            @org.eclipse.jdt.annotation.NonNull Boolean enableOriginalAudioWithEncodedRecordings) {
        this.enableOriginalAudioWithEncodedRecordings = enableOriginalAudioWithEncodedRecordings;
    }

    public LiveTvOptions tunerHosts(@org.eclipse.jdt.annotation.NonNull List<TunerHostInfo> tunerHosts) {
        this.tunerHosts = tunerHosts;
        return this;
    }

    public LiveTvOptions addTunerHostsItem(TunerHostInfo tunerHostsItem) {
        if (this.tunerHosts == null) {
            this.tunerHosts = new ArrayList<>();
        }
        this.tunerHosts.add(tunerHostsItem);
        return this;
    }

    /**
     * Get tunerHosts
     * 
     * @return tunerHosts
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TUNER_HOSTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<TunerHostInfo> getTunerHosts() {
        return tunerHosts;
    }

    @JsonProperty(value = JSON_PROPERTY_TUNER_HOSTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTunerHosts(@org.eclipse.jdt.annotation.NonNull List<TunerHostInfo> tunerHosts) {
        this.tunerHosts = tunerHosts;
    }

    public LiveTvOptions listingProviders(
            @org.eclipse.jdt.annotation.NonNull List<ListingsProviderInfo> listingProviders) {
        this.listingProviders = listingProviders;
        return this;
    }

    public LiveTvOptions addListingProvidersItem(ListingsProviderInfo listingProvidersItem) {
        if (this.listingProviders == null) {
            this.listingProviders = new ArrayList<>();
        }
        this.listingProviders.add(listingProvidersItem);
        return this;
    }

    /**
     * Get listingProviders
     * 
     * @return listingProviders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LISTING_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ListingsProviderInfo> getListingProviders() {
        return listingProviders;
    }

    @JsonProperty(value = JSON_PROPERTY_LISTING_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setListingProviders(@org.eclipse.jdt.annotation.NonNull List<ListingsProviderInfo> listingProviders) {
        this.listingProviders = listingProviders;
    }

    public LiveTvOptions prePaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer prePaddingSeconds) {
        this.prePaddingSeconds = prePaddingSeconds;
        return this;
    }

    /**
     * Get prePaddingSeconds
     * 
     * @return prePaddingSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRE_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPrePaddingSeconds() {
        return prePaddingSeconds;
    }

    @JsonProperty(value = JSON_PROPERTY_PRE_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrePaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer prePaddingSeconds) {
        this.prePaddingSeconds = prePaddingSeconds;
    }

    public LiveTvOptions postPaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer postPaddingSeconds) {
        this.postPaddingSeconds = postPaddingSeconds;
        return this;
    }

    /**
     * Get postPaddingSeconds
     * 
     * @return postPaddingSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_POST_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPostPaddingSeconds() {
        return postPaddingSeconds;
    }

    @JsonProperty(value = JSON_PROPERTY_POST_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPostPaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer postPaddingSeconds) {
        this.postPaddingSeconds = postPaddingSeconds;
    }

    public LiveTvOptions mediaLocationsCreated(@org.eclipse.jdt.annotation.NonNull List<String> mediaLocationsCreated) {
        this.mediaLocationsCreated = mediaLocationsCreated;
        return this;
    }

    public LiveTvOptions addMediaLocationsCreatedItem(String mediaLocationsCreatedItem) {
        if (this.mediaLocationsCreated == null) {
            this.mediaLocationsCreated = new ArrayList<>();
        }
        this.mediaLocationsCreated.add(mediaLocationsCreatedItem);
        return this;
    }

    /**
     * Get mediaLocationsCreated
     * 
     * @return mediaLocationsCreated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_LOCATIONS_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getMediaLocationsCreated() {
        return mediaLocationsCreated;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_LOCATIONS_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaLocationsCreated(@org.eclipse.jdt.annotation.NonNull List<String> mediaLocationsCreated) {
        this.mediaLocationsCreated = mediaLocationsCreated;
    }

    public LiveTvOptions recordingPostProcessor(@org.eclipse.jdt.annotation.NonNull String recordingPostProcessor) {
        this.recordingPostProcessor = recordingPostProcessor;
        return this;
    }

    /**
     * Get recordingPostProcessor
     * 
     * @return recordingPostProcessor
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RECORDING_POST_PROCESSOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRecordingPostProcessor() {
        return recordingPostProcessor;
    }

    @JsonProperty(value = JSON_PROPERTY_RECORDING_POST_PROCESSOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordingPostProcessor(@org.eclipse.jdt.annotation.NonNull String recordingPostProcessor) {
        this.recordingPostProcessor = recordingPostProcessor;
    }

    public LiveTvOptions recordingPostProcessorArguments(
            @org.eclipse.jdt.annotation.NonNull String recordingPostProcessorArguments) {
        this.recordingPostProcessorArguments = recordingPostProcessorArguments;
        return this;
    }

    /**
     * Get recordingPostProcessorArguments
     * 
     * @return recordingPostProcessorArguments
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RECORDING_POST_PROCESSOR_ARGUMENTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRecordingPostProcessorArguments() {
        return recordingPostProcessorArguments;
    }

    @JsonProperty(value = JSON_PROPERTY_RECORDING_POST_PROCESSOR_ARGUMENTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordingPostProcessorArguments(
            @org.eclipse.jdt.annotation.NonNull String recordingPostProcessorArguments) {
        this.recordingPostProcessorArguments = recordingPostProcessorArguments;
    }

    public LiveTvOptions saveRecordingNFO(@org.eclipse.jdt.annotation.NonNull Boolean saveRecordingNFO) {
        this.saveRecordingNFO = saveRecordingNFO;
        return this;
    }

    /**
     * Get saveRecordingNFO
     * 
     * @return saveRecordingNFO
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SAVE_RECORDING_N_F_O, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveRecordingNFO() {
        return saveRecordingNFO;
    }

    @JsonProperty(value = JSON_PROPERTY_SAVE_RECORDING_N_F_O, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveRecordingNFO(@org.eclipse.jdt.annotation.NonNull Boolean saveRecordingNFO) {
        this.saveRecordingNFO = saveRecordingNFO;
    }

    public LiveTvOptions saveRecordingImages(@org.eclipse.jdt.annotation.NonNull Boolean saveRecordingImages) {
        this.saveRecordingImages = saveRecordingImages;
        return this;
    }

    /**
     * Get saveRecordingImages
     * 
     * @return saveRecordingImages
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SAVE_RECORDING_IMAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveRecordingImages() {
        return saveRecordingImages;
    }

    @JsonProperty(value = JSON_PROPERTY_SAVE_RECORDING_IMAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveRecordingImages(@org.eclipse.jdt.annotation.NonNull Boolean saveRecordingImages) {
        this.saveRecordingImages = saveRecordingImages;
    }

    /**
     * Return true if this LiveTvOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveTvOptions liveTvOptions = (LiveTvOptions) o;
        return Objects.equals(this.guideDays, liveTvOptions.guideDays)
                && Objects.equals(this.recordingPath, liveTvOptions.recordingPath)
                && Objects.equals(this.movieRecordingPath, liveTvOptions.movieRecordingPath)
                && Objects.equals(this.seriesRecordingPath, liveTvOptions.seriesRecordingPath)
                && Objects.equals(this.enableRecordingSubfolders, liveTvOptions.enableRecordingSubfolders)
                && Objects.equals(this.enableOriginalAudioWithEncodedRecordings,
                        liveTvOptions.enableOriginalAudioWithEncodedRecordings)
                && Objects.equals(this.tunerHosts, liveTvOptions.tunerHosts)
                && Objects.equals(this.listingProviders, liveTvOptions.listingProviders)
                && Objects.equals(this.prePaddingSeconds, liveTvOptions.prePaddingSeconds)
                && Objects.equals(this.postPaddingSeconds, liveTvOptions.postPaddingSeconds)
                && Objects.equals(this.mediaLocationsCreated, liveTvOptions.mediaLocationsCreated)
                && Objects.equals(this.recordingPostProcessor, liveTvOptions.recordingPostProcessor)
                && Objects.equals(this.recordingPostProcessorArguments, liveTvOptions.recordingPostProcessorArguments)
                && Objects.equals(this.saveRecordingNFO, liveTvOptions.saveRecordingNFO)
                && Objects.equals(this.saveRecordingImages, liveTvOptions.saveRecordingImages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guideDays, recordingPath, movieRecordingPath, seriesRecordingPath,
                enableRecordingSubfolders, enableOriginalAudioWithEncodedRecordings, tunerHosts, listingProviders,
                prePaddingSeconds, postPaddingSeconds, mediaLocationsCreated, recordingPostProcessor,
                recordingPostProcessorArguments, saveRecordingNFO, saveRecordingImages);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LiveTvOptions {\n");
        sb.append("    guideDays: ").append(toIndentedString(guideDays)).append("\n");
        sb.append("    recordingPath: ").append(toIndentedString(recordingPath)).append("\n");
        sb.append("    movieRecordingPath: ").append(toIndentedString(movieRecordingPath)).append("\n");
        sb.append("    seriesRecordingPath: ").append(toIndentedString(seriesRecordingPath)).append("\n");
        sb.append("    enableRecordingSubfolders: ").append(toIndentedString(enableRecordingSubfolders)).append("\n");
        sb.append("    enableOriginalAudioWithEncodedRecordings: ")
                .append(toIndentedString(enableOriginalAudioWithEncodedRecordings)).append("\n");
        sb.append("    tunerHosts: ").append(toIndentedString(tunerHosts)).append("\n");
        sb.append("    listingProviders: ").append(toIndentedString(listingProviders)).append("\n");
        sb.append("    prePaddingSeconds: ").append(toIndentedString(prePaddingSeconds)).append("\n");
        sb.append("    postPaddingSeconds: ").append(toIndentedString(postPaddingSeconds)).append("\n");
        sb.append("    mediaLocationsCreated: ").append(toIndentedString(mediaLocationsCreated)).append("\n");
        sb.append("    recordingPostProcessor: ").append(toIndentedString(recordingPostProcessor)).append("\n");
        sb.append("    recordingPostProcessorArguments: ").append(toIndentedString(recordingPostProcessorArguments))
                .append("\n");
        sb.append("    saveRecordingNFO: ").append(toIndentedString(saveRecordingNFO)).append("\n");
        sb.append("    saveRecordingImages: ").append(toIndentedString(saveRecordingImages)).append("\n");
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

        // add `GuideDays` to the URL query string
        if (getGuideDays() != null) {
            joiner.add(String.format(Locale.ROOT, "%sGuideDays%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGuideDays()))));
        }

        // add `RecordingPath` to the URL query string
        if (getRecordingPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRecordingPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecordingPath()))));
        }

        // add `MovieRecordingPath` to the URL query string
        if (getMovieRecordingPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMovieRecordingPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMovieRecordingPath()))));
        }

        // add `SeriesRecordingPath` to the URL query string
        if (getSeriesRecordingPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesRecordingPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesRecordingPath()))));
        }

        // add `EnableRecordingSubfolders` to the URL query string
        if (getEnableRecordingSubfolders() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableRecordingSubfolders%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableRecordingSubfolders()))));
        }

        // add `EnableOriginalAudioWithEncodedRecordings` to the URL query string
        if (getEnableOriginalAudioWithEncodedRecordings() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableOriginalAudioWithEncodedRecordings%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableOriginalAudioWithEncodedRecordings()))));
        }

        // add `TunerHosts` to the URL query string
        if (getTunerHosts() != null) {
            for (int i = 0; i < getTunerHosts().size(); i++) {
                if (getTunerHosts().get(i) != null) {
                    joiner.add(getTunerHosts().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sTunerHosts%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `ListingProviders` to the URL query string
        if (getListingProviders() != null) {
            for (int i = 0; i < getListingProviders().size(); i++) {
                if (getListingProviders().get(i) != null) {
                    joiner.add(getListingProviders().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sListingProviders%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `PrePaddingSeconds` to the URL query string
        if (getPrePaddingSeconds() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrePaddingSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrePaddingSeconds()))));
        }

        // add `PostPaddingSeconds` to the URL query string
        if (getPostPaddingSeconds() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPostPaddingSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPostPaddingSeconds()))));
        }

        // add `MediaLocationsCreated` to the URL query string
        if (getMediaLocationsCreated() != null) {
            for (int i = 0; i < getMediaLocationsCreated().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sMediaLocationsCreated%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getMediaLocationsCreated().get(i)))));
            }
        }

        // add `RecordingPostProcessor` to the URL query string
        if (getRecordingPostProcessor() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRecordingPostProcessor%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecordingPostProcessor()))));
        }

        // add `RecordingPostProcessorArguments` to the URL query string
        if (getRecordingPostProcessorArguments() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRecordingPostProcessorArguments%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecordingPostProcessorArguments()))));
        }

        // add `SaveRecordingNFO` to the URL query string
        if (getSaveRecordingNFO() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSaveRecordingNFO%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveRecordingNFO()))));
        }

        // add `SaveRecordingImages` to the URL query string
        if (getSaveRecordingImages() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSaveRecordingImages%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveRecordingImages()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LiveTvOptions instance;

        public Builder() {
            this(new LiveTvOptions());
        }

        protected Builder(LiveTvOptions instance) {
            this.instance = instance;
        }

        public LiveTvOptions.Builder guideDays(Integer guideDays) {
            this.instance.guideDays = guideDays;
            return this;
        }

        public LiveTvOptions.Builder recordingPath(String recordingPath) {
            this.instance.recordingPath = recordingPath;
            return this;
        }

        public LiveTvOptions.Builder movieRecordingPath(String movieRecordingPath) {
            this.instance.movieRecordingPath = movieRecordingPath;
            return this;
        }

        public LiveTvOptions.Builder seriesRecordingPath(String seriesRecordingPath) {
            this.instance.seriesRecordingPath = seriesRecordingPath;
            return this;
        }

        public LiveTvOptions.Builder enableRecordingSubfolders(Boolean enableRecordingSubfolders) {
            this.instance.enableRecordingSubfolders = enableRecordingSubfolders;
            return this;
        }

        public LiveTvOptions.Builder enableOriginalAudioWithEncodedRecordings(
                Boolean enableOriginalAudioWithEncodedRecordings) {
            this.instance.enableOriginalAudioWithEncodedRecordings = enableOriginalAudioWithEncodedRecordings;
            return this;
        }

        public LiveTvOptions.Builder tunerHosts(List<TunerHostInfo> tunerHosts) {
            this.instance.tunerHosts = tunerHosts;
            return this;
        }

        public LiveTvOptions.Builder listingProviders(List<ListingsProviderInfo> listingProviders) {
            this.instance.listingProviders = listingProviders;
            return this;
        }

        public LiveTvOptions.Builder prePaddingSeconds(Integer prePaddingSeconds) {
            this.instance.prePaddingSeconds = prePaddingSeconds;
            return this;
        }

        public LiveTvOptions.Builder postPaddingSeconds(Integer postPaddingSeconds) {
            this.instance.postPaddingSeconds = postPaddingSeconds;
            return this;
        }

        public LiveTvOptions.Builder mediaLocationsCreated(List<String> mediaLocationsCreated) {
            this.instance.mediaLocationsCreated = mediaLocationsCreated;
            return this;
        }

        public LiveTvOptions.Builder recordingPostProcessor(String recordingPostProcessor) {
            this.instance.recordingPostProcessor = recordingPostProcessor;
            return this;
        }

        public LiveTvOptions.Builder recordingPostProcessorArguments(String recordingPostProcessorArguments) {
            this.instance.recordingPostProcessorArguments = recordingPostProcessorArguments;
            return this;
        }

        public LiveTvOptions.Builder saveRecordingNFO(Boolean saveRecordingNFO) {
            this.instance.saveRecordingNFO = saveRecordingNFO;
            return this;
        }

        public LiveTvOptions.Builder saveRecordingImages(Boolean saveRecordingImages) {
            this.instance.saveRecordingImages = saveRecordingImages;
            return this;
        }

        /**
         * returns a built LiveTvOptions instance.
         *
         * The builder is not reusable.
         */
        public LiveTvOptions build() {
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
    public static LiveTvOptions.Builder builder() {
        return new LiveTvOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LiveTvOptions.Builder toBuilder() {
        return new LiveTvOptions.Builder().guideDays(getGuideDays()).recordingPath(getRecordingPath())
                .movieRecordingPath(getMovieRecordingPath()).seriesRecordingPath(getSeriesRecordingPath())
                .enableRecordingSubfolders(getEnableRecordingSubfolders())
                .enableOriginalAudioWithEncodedRecordings(getEnableOriginalAudioWithEncodedRecordings())
                .tunerHosts(getTunerHosts()).listingProviders(getListingProviders())
                .prePaddingSeconds(getPrePaddingSeconds()).postPaddingSeconds(getPostPaddingSeconds())
                .mediaLocationsCreated(getMediaLocationsCreated()).recordingPostProcessor(getRecordingPostProcessor())
                .recordingPostProcessorArguments(getRecordingPostProcessorArguments())
                .saveRecordingNFO(getSaveRecordingNFO()).saveRecordingImages(getSaveRecordingImages());
    }
}

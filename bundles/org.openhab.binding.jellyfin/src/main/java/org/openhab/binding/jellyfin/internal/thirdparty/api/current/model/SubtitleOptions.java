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
 * SubtitleOptions
 */
@JsonPropertyOrder({ SubtitleOptions.JSON_PROPERTY_SKIP_IF_EMBEDDED_SUBTITLES_PRESENT,
        SubtitleOptions.JSON_PROPERTY_SKIP_IF_AUDIO_TRACK_MATCHES, SubtitleOptions.JSON_PROPERTY_DOWNLOAD_LANGUAGES,
        SubtitleOptions.JSON_PROPERTY_DOWNLOAD_MOVIE_SUBTITLES,
        SubtitleOptions.JSON_PROPERTY_DOWNLOAD_EPISODE_SUBTITLES, SubtitleOptions.JSON_PROPERTY_OPEN_SUBTITLES_USERNAME,
        SubtitleOptions.JSON_PROPERTY_OPEN_SUBTITLES_PASSWORD_HASH,
        SubtitleOptions.JSON_PROPERTY_IS_OPEN_SUBTITLE_VIP_ACCOUNT,
        SubtitleOptions.JSON_PROPERTY_REQUIRE_PERFECT_MATCH })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SubtitleOptions {
    public static final String JSON_PROPERTY_SKIP_IF_EMBEDDED_SUBTITLES_PRESENT = "SkipIfEmbeddedSubtitlesPresent";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean skipIfEmbeddedSubtitlesPresent;

    public static final String JSON_PROPERTY_SKIP_IF_AUDIO_TRACK_MATCHES = "SkipIfAudioTrackMatches";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean skipIfAudioTrackMatches;

    public static final String JSON_PROPERTY_DOWNLOAD_LANGUAGES = "DownloadLanguages";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> downloadLanguages;

    public static final String JSON_PROPERTY_DOWNLOAD_MOVIE_SUBTITLES = "DownloadMovieSubtitles";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean downloadMovieSubtitles;

    public static final String JSON_PROPERTY_DOWNLOAD_EPISODE_SUBTITLES = "DownloadEpisodeSubtitles";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean downloadEpisodeSubtitles;

    public static final String JSON_PROPERTY_OPEN_SUBTITLES_USERNAME = "OpenSubtitlesUsername";
    @org.eclipse.jdt.annotation.Nullable
    private String openSubtitlesUsername;

    public static final String JSON_PROPERTY_OPEN_SUBTITLES_PASSWORD_HASH = "OpenSubtitlesPasswordHash";
    @org.eclipse.jdt.annotation.Nullable
    private String openSubtitlesPasswordHash;

    public static final String JSON_PROPERTY_IS_OPEN_SUBTITLE_VIP_ACCOUNT = "IsOpenSubtitleVipAccount";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isOpenSubtitleVipAccount;

    public static final String JSON_PROPERTY_REQUIRE_PERFECT_MATCH = "RequirePerfectMatch";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean requirePerfectMatch;

    public SubtitleOptions() {
    }

    public SubtitleOptions skipIfEmbeddedSubtitlesPresent(
            @org.eclipse.jdt.annotation.Nullable Boolean skipIfEmbeddedSubtitlesPresent) {
        this.skipIfEmbeddedSubtitlesPresent = skipIfEmbeddedSubtitlesPresent;
        return this;
    }

    /**
     * Get skipIfEmbeddedSubtitlesPresent
     * 
     * @return skipIfEmbeddedSubtitlesPresent
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SKIP_IF_EMBEDDED_SUBTITLES_PRESENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSkipIfEmbeddedSubtitlesPresent() {
        return skipIfEmbeddedSubtitlesPresent;
    }

    @JsonProperty(value = JSON_PROPERTY_SKIP_IF_EMBEDDED_SUBTITLES_PRESENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSkipIfEmbeddedSubtitlesPresent(
            @org.eclipse.jdt.annotation.Nullable Boolean skipIfEmbeddedSubtitlesPresent) {
        this.skipIfEmbeddedSubtitlesPresent = skipIfEmbeddedSubtitlesPresent;
    }

    public SubtitleOptions skipIfAudioTrackMatches(
            @org.eclipse.jdt.annotation.Nullable Boolean skipIfAudioTrackMatches) {
        this.skipIfAudioTrackMatches = skipIfAudioTrackMatches;
        return this;
    }

    /**
     * Get skipIfAudioTrackMatches
     * 
     * @return skipIfAudioTrackMatches
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SKIP_IF_AUDIO_TRACK_MATCHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSkipIfAudioTrackMatches() {
        return skipIfAudioTrackMatches;
    }

    @JsonProperty(value = JSON_PROPERTY_SKIP_IF_AUDIO_TRACK_MATCHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSkipIfAudioTrackMatches(@org.eclipse.jdt.annotation.Nullable Boolean skipIfAudioTrackMatches) {
        this.skipIfAudioTrackMatches = skipIfAudioTrackMatches;
    }

    public SubtitleOptions downloadLanguages(@org.eclipse.jdt.annotation.Nullable List<String> downloadLanguages) {
        this.downloadLanguages = downloadLanguages;
        return this;
    }

    public SubtitleOptions addDownloadLanguagesItem(String downloadLanguagesItem) {
        if (this.downloadLanguages == null) {
            this.downloadLanguages = new ArrayList<>();
        }
        this.downloadLanguages.add(downloadLanguagesItem);
        return this;
    }

    /**
     * Get downloadLanguages
     * 
     * @return downloadLanguages
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DOWNLOAD_LANGUAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDownloadLanguages() {
        return downloadLanguages;
    }

    @JsonProperty(value = JSON_PROPERTY_DOWNLOAD_LANGUAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownloadLanguages(@org.eclipse.jdt.annotation.Nullable List<String> downloadLanguages) {
        this.downloadLanguages = downloadLanguages;
    }

    public SubtitleOptions downloadMovieSubtitles(@org.eclipse.jdt.annotation.Nullable Boolean downloadMovieSubtitles) {
        this.downloadMovieSubtitles = downloadMovieSubtitles;
        return this;
    }

    /**
     * Get downloadMovieSubtitles
     * 
     * @return downloadMovieSubtitles
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DOWNLOAD_MOVIE_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDownloadMovieSubtitles() {
        return downloadMovieSubtitles;
    }

    @JsonProperty(value = JSON_PROPERTY_DOWNLOAD_MOVIE_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownloadMovieSubtitles(@org.eclipse.jdt.annotation.Nullable Boolean downloadMovieSubtitles) {
        this.downloadMovieSubtitles = downloadMovieSubtitles;
    }

    public SubtitleOptions downloadEpisodeSubtitles(
            @org.eclipse.jdt.annotation.Nullable Boolean downloadEpisodeSubtitles) {
        this.downloadEpisodeSubtitles = downloadEpisodeSubtitles;
        return this;
    }

    /**
     * Get downloadEpisodeSubtitles
     * 
     * @return downloadEpisodeSubtitles
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DOWNLOAD_EPISODE_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDownloadEpisodeSubtitles() {
        return downloadEpisodeSubtitles;
    }

    @JsonProperty(value = JSON_PROPERTY_DOWNLOAD_EPISODE_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownloadEpisodeSubtitles(@org.eclipse.jdt.annotation.Nullable Boolean downloadEpisodeSubtitles) {
        this.downloadEpisodeSubtitles = downloadEpisodeSubtitles;
    }

    public SubtitleOptions openSubtitlesUsername(@org.eclipse.jdt.annotation.Nullable String openSubtitlesUsername) {
        this.openSubtitlesUsername = openSubtitlesUsername;
        return this;
    }

    /**
     * Get openSubtitlesUsername
     * 
     * @return openSubtitlesUsername
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_OPEN_SUBTITLES_USERNAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOpenSubtitlesUsername() {
        return openSubtitlesUsername;
    }

    @JsonProperty(value = JSON_PROPERTY_OPEN_SUBTITLES_USERNAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOpenSubtitlesUsername(@org.eclipse.jdt.annotation.Nullable String openSubtitlesUsername) {
        this.openSubtitlesUsername = openSubtitlesUsername;
    }

    public SubtitleOptions openSubtitlesPasswordHash(
            @org.eclipse.jdt.annotation.Nullable String openSubtitlesPasswordHash) {
        this.openSubtitlesPasswordHash = openSubtitlesPasswordHash;
        return this;
    }

    /**
     * Get openSubtitlesPasswordHash
     * 
     * @return openSubtitlesPasswordHash
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_OPEN_SUBTITLES_PASSWORD_HASH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOpenSubtitlesPasswordHash() {
        return openSubtitlesPasswordHash;
    }

    @JsonProperty(value = JSON_PROPERTY_OPEN_SUBTITLES_PASSWORD_HASH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOpenSubtitlesPasswordHash(@org.eclipse.jdt.annotation.Nullable String openSubtitlesPasswordHash) {
        this.openSubtitlesPasswordHash = openSubtitlesPasswordHash;
    }

    public SubtitleOptions isOpenSubtitleVipAccount(
            @org.eclipse.jdt.annotation.Nullable Boolean isOpenSubtitleVipAccount) {
        this.isOpenSubtitleVipAccount = isOpenSubtitleVipAccount;
        return this;
    }

    /**
     * Get isOpenSubtitleVipAccount
     * 
     * @return isOpenSubtitleVipAccount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_OPEN_SUBTITLE_VIP_ACCOUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsOpenSubtitleVipAccount() {
        return isOpenSubtitleVipAccount;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_OPEN_SUBTITLE_VIP_ACCOUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsOpenSubtitleVipAccount(@org.eclipse.jdt.annotation.Nullable Boolean isOpenSubtitleVipAccount) {
        this.isOpenSubtitleVipAccount = isOpenSubtitleVipAccount;
    }

    public SubtitleOptions requirePerfectMatch(@org.eclipse.jdt.annotation.Nullable Boolean requirePerfectMatch) {
        this.requirePerfectMatch = requirePerfectMatch;
        return this;
    }

    /**
     * Get requirePerfectMatch
     * 
     * @return requirePerfectMatch
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REQUIRE_PERFECT_MATCH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequirePerfectMatch() {
        return requirePerfectMatch;
    }

    @JsonProperty(value = JSON_PROPERTY_REQUIRE_PERFECT_MATCH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequirePerfectMatch(@org.eclipse.jdt.annotation.Nullable Boolean requirePerfectMatch) {
        this.requirePerfectMatch = requirePerfectMatch;
    }

    /**
     * Return true if this SubtitleOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubtitleOptions subtitleOptions = (SubtitleOptions) o;
        return Objects.equals(this.skipIfEmbeddedSubtitlesPresent, subtitleOptions.skipIfEmbeddedSubtitlesPresent)
                && Objects.equals(this.skipIfAudioTrackMatches, subtitleOptions.skipIfAudioTrackMatches)
                && Objects.equals(this.downloadLanguages, subtitleOptions.downloadLanguages)
                && Objects.equals(this.downloadMovieSubtitles, subtitleOptions.downloadMovieSubtitles)
                && Objects.equals(this.downloadEpisodeSubtitles, subtitleOptions.downloadEpisodeSubtitles)
                && Objects.equals(this.openSubtitlesUsername, subtitleOptions.openSubtitlesUsername)
                && Objects.equals(this.openSubtitlesPasswordHash, subtitleOptions.openSubtitlesPasswordHash)
                && Objects.equals(this.isOpenSubtitleVipAccount, subtitleOptions.isOpenSubtitleVipAccount)
                && Objects.equals(this.requirePerfectMatch, subtitleOptions.requirePerfectMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skipIfEmbeddedSubtitlesPresent, skipIfAudioTrackMatches, downloadLanguages,
                downloadMovieSubtitles, downloadEpisodeSubtitles, openSubtitlesUsername, openSubtitlesPasswordHash,
                isOpenSubtitleVipAccount, requirePerfectMatch);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubtitleOptions {\n");
        sb.append("    skipIfEmbeddedSubtitlesPresent: ").append(toIndentedString(skipIfEmbeddedSubtitlesPresent))
                .append("\n");
        sb.append("    skipIfAudioTrackMatches: ").append(toIndentedString(skipIfAudioTrackMatches)).append("\n");
        sb.append("    downloadLanguages: ").append(toIndentedString(downloadLanguages)).append("\n");
        sb.append("    downloadMovieSubtitles: ").append(toIndentedString(downloadMovieSubtitles)).append("\n");
        sb.append("    downloadEpisodeSubtitles: ").append(toIndentedString(downloadEpisodeSubtitles)).append("\n");
        sb.append("    openSubtitlesUsername: ").append(toIndentedString(openSubtitlesUsername)).append("\n");
        sb.append("    openSubtitlesPasswordHash: ").append(toIndentedString(openSubtitlesPasswordHash)).append("\n");
        sb.append("    isOpenSubtitleVipAccount: ").append(toIndentedString(isOpenSubtitleVipAccount)).append("\n");
        sb.append("    requirePerfectMatch: ").append(toIndentedString(requirePerfectMatch)).append("\n");
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

        // add `SkipIfEmbeddedSubtitlesPresent` to the URL query string
        if (getSkipIfEmbeddedSubtitlesPresent() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSkipIfEmbeddedSubtitlesPresent%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSkipIfEmbeddedSubtitlesPresent()))));
        }

        // add `SkipIfAudioTrackMatches` to the URL query string
        if (getSkipIfAudioTrackMatches() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSkipIfAudioTrackMatches%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSkipIfAudioTrackMatches()))));
        }

        // add `DownloadLanguages` to the URL query string
        if (getDownloadLanguages() != null) {
            for (int i = 0; i < getDownloadLanguages().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sDownloadLanguages%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDownloadLanguages().get(i)))));
            }
        }

        // add `DownloadMovieSubtitles` to the URL query string
        if (getDownloadMovieSubtitles() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDownloadMovieSubtitles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDownloadMovieSubtitles()))));
        }

        // add `DownloadEpisodeSubtitles` to the URL query string
        if (getDownloadEpisodeSubtitles() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDownloadEpisodeSubtitles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDownloadEpisodeSubtitles()))));
        }

        // add `OpenSubtitlesUsername` to the URL query string
        if (getOpenSubtitlesUsername() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sOpenSubtitlesUsername%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOpenSubtitlesUsername()))));
        }

        // add `OpenSubtitlesPasswordHash` to the URL query string
        if (getOpenSubtitlesPasswordHash() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sOpenSubtitlesPasswordHash%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOpenSubtitlesPasswordHash()))));
        }

        // add `IsOpenSubtitleVipAccount` to the URL query string
        if (getIsOpenSubtitleVipAccount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsOpenSubtitleVipAccount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsOpenSubtitleVipAccount()))));
        }

        // add `RequirePerfectMatch` to the URL query string
        if (getRequirePerfectMatch() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRequirePerfectMatch%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequirePerfectMatch()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SubtitleOptions instance;

        public Builder() {
            this(new SubtitleOptions());
        }

        protected Builder(SubtitleOptions instance) {
            this.instance = instance;
        }

        public SubtitleOptions.Builder skipIfEmbeddedSubtitlesPresent(Boolean skipIfEmbeddedSubtitlesPresent) {
            this.instance.skipIfEmbeddedSubtitlesPresent = skipIfEmbeddedSubtitlesPresent;
            return this;
        }

        public SubtitleOptions.Builder skipIfAudioTrackMatches(Boolean skipIfAudioTrackMatches) {
            this.instance.skipIfAudioTrackMatches = skipIfAudioTrackMatches;
            return this;
        }

        public SubtitleOptions.Builder downloadLanguages(List<String> downloadLanguages) {
            this.instance.downloadLanguages = downloadLanguages;
            return this;
        }

        public SubtitleOptions.Builder downloadMovieSubtitles(Boolean downloadMovieSubtitles) {
            this.instance.downloadMovieSubtitles = downloadMovieSubtitles;
            return this;
        }

        public SubtitleOptions.Builder downloadEpisodeSubtitles(Boolean downloadEpisodeSubtitles) {
            this.instance.downloadEpisodeSubtitles = downloadEpisodeSubtitles;
            return this;
        }

        public SubtitleOptions.Builder openSubtitlesUsername(String openSubtitlesUsername) {
            this.instance.openSubtitlesUsername = openSubtitlesUsername;
            return this;
        }

        public SubtitleOptions.Builder openSubtitlesPasswordHash(String openSubtitlesPasswordHash) {
            this.instance.openSubtitlesPasswordHash = openSubtitlesPasswordHash;
            return this;
        }

        public SubtitleOptions.Builder isOpenSubtitleVipAccount(Boolean isOpenSubtitleVipAccount) {
            this.instance.isOpenSubtitleVipAccount = isOpenSubtitleVipAccount;
            return this;
        }

        public SubtitleOptions.Builder requirePerfectMatch(Boolean requirePerfectMatch) {
            this.instance.requirePerfectMatch = requirePerfectMatch;
            return this;
        }

        /**
         * returns a built SubtitleOptions instance.
         *
         * The builder is not reusable.
         */
        public SubtitleOptions build() {
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
    public static SubtitleOptions.Builder builder() {
        return new SubtitleOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SubtitleOptions.Builder toBuilder() {
        return new SubtitleOptions.Builder().skipIfEmbeddedSubtitlesPresent(getSkipIfEmbeddedSubtitlesPresent())
                .skipIfAudioTrackMatches(getSkipIfAudioTrackMatches()).downloadLanguages(getDownloadLanguages())
                .downloadMovieSubtitles(getDownloadMovieSubtitles())
                .downloadEpisodeSubtitles(getDownloadEpisodeSubtitles())
                .openSubtitlesUsername(getOpenSubtitlesUsername())
                .openSubtitlesPasswordHash(getOpenSubtitlesPasswordHash())
                .isOpenSubtitleVipAccount(getIsOpenSubtitleVipAccount()).requirePerfectMatch(getRequirePerfectMatch());
    }
}

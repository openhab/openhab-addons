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
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * AllThemeMediaResult
 */
@JsonPropertyOrder({ AllThemeMediaResult.JSON_PROPERTY_THEME_VIDEOS_RESULT,
        AllThemeMediaResult.JSON_PROPERTY_THEME_SONGS_RESULT,
        AllThemeMediaResult.JSON_PROPERTY_SOUNDTRACK_SONGS_RESULT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AllThemeMediaResult {
    public static final String JSON_PROPERTY_THEME_VIDEOS_RESULT = "ThemeVideosResult";
    @org.eclipse.jdt.annotation.NonNull
    private ThemeMediaResult themeVideosResult;

    public static final String JSON_PROPERTY_THEME_SONGS_RESULT = "ThemeSongsResult";
    @org.eclipse.jdt.annotation.NonNull
    private ThemeMediaResult themeSongsResult;

    public static final String JSON_PROPERTY_SOUNDTRACK_SONGS_RESULT = "SoundtrackSongsResult";
    @org.eclipse.jdt.annotation.NonNull
    private ThemeMediaResult soundtrackSongsResult;

    public AllThemeMediaResult() {
    }

    public AllThemeMediaResult themeVideosResult(
            @org.eclipse.jdt.annotation.NonNull ThemeMediaResult themeVideosResult) {
        this.themeVideosResult = themeVideosResult;
        return this;
    }

    /**
     * Class ThemeMediaResult.
     * 
     * @return themeVideosResult
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_THEME_VIDEOS_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ThemeMediaResult getThemeVideosResult() {
        return themeVideosResult;
    }

    @JsonProperty(value = JSON_PROPERTY_THEME_VIDEOS_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThemeVideosResult(@org.eclipse.jdt.annotation.NonNull ThemeMediaResult themeVideosResult) {
        this.themeVideosResult = themeVideosResult;
    }

    public AllThemeMediaResult themeSongsResult(@org.eclipse.jdt.annotation.NonNull ThemeMediaResult themeSongsResult) {
        this.themeSongsResult = themeSongsResult;
        return this;
    }

    /**
     * Class ThemeMediaResult.
     * 
     * @return themeSongsResult
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_THEME_SONGS_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ThemeMediaResult getThemeSongsResult() {
        return themeSongsResult;
    }

    @JsonProperty(value = JSON_PROPERTY_THEME_SONGS_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThemeSongsResult(@org.eclipse.jdt.annotation.NonNull ThemeMediaResult themeSongsResult) {
        this.themeSongsResult = themeSongsResult;
    }

    public AllThemeMediaResult soundtrackSongsResult(
            @org.eclipse.jdt.annotation.NonNull ThemeMediaResult soundtrackSongsResult) {
        this.soundtrackSongsResult = soundtrackSongsResult;
        return this;
    }

    /**
     * Class ThemeMediaResult.
     * 
     * @return soundtrackSongsResult
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SOUNDTRACK_SONGS_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ThemeMediaResult getSoundtrackSongsResult() {
        return soundtrackSongsResult;
    }

    @JsonProperty(value = JSON_PROPERTY_SOUNDTRACK_SONGS_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSoundtrackSongsResult(@org.eclipse.jdt.annotation.NonNull ThemeMediaResult soundtrackSongsResult) {
        this.soundtrackSongsResult = soundtrackSongsResult;
    }

    /**
     * Return true if this AllThemeMediaResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AllThemeMediaResult allThemeMediaResult = (AllThemeMediaResult) o;
        return Objects.equals(this.themeVideosResult, allThemeMediaResult.themeVideosResult)
                && Objects.equals(this.themeSongsResult, allThemeMediaResult.themeSongsResult)
                && Objects.equals(this.soundtrackSongsResult, allThemeMediaResult.soundtrackSongsResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeVideosResult, themeSongsResult, soundtrackSongsResult);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AllThemeMediaResult {\n");
        sb.append("    themeVideosResult: ").append(toIndentedString(themeVideosResult)).append("\n");
        sb.append("    themeSongsResult: ").append(toIndentedString(themeSongsResult)).append("\n");
        sb.append("    soundtrackSongsResult: ").append(toIndentedString(soundtrackSongsResult)).append("\n");
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

        // add `ThemeVideosResult` to the URL query string
        if (getThemeVideosResult() != null) {
            joiner.add(getThemeVideosResult().toUrlQueryString(prefix + "ThemeVideosResult" + suffix));
        }

        // add `ThemeSongsResult` to the URL query string
        if (getThemeSongsResult() != null) {
            joiner.add(getThemeSongsResult().toUrlQueryString(prefix + "ThemeSongsResult" + suffix));
        }

        // add `SoundtrackSongsResult` to the URL query string
        if (getSoundtrackSongsResult() != null) {
            joiner.add(getSoundtrackSongsResult().toUrlQueryString(prefix + "SoundtrackSongsResult" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AllThemeMediaResult instance;

        public Builder() {
            this(new AllThemeMediaResult());
        }

        protected Builder(AllThemeMediaResult instance) {
            this.instance = instance;
        }

        public AllThemeMediaResult.Builder themeVideosResult(ThemeMediaResult themeVideosResult) {
            this.instance.themeVideosResult = themeVideosResult;
            return this;
        }

        public AllThemeMediaResult.Builder themeSongsResult(ThemeMediaResult themeSongsResult) {
            this.instance.themeSongsResult = themeSongsResult;
            return this;
        }

        public AllThemeMediaResult.Builder soundtrackSongsResult(ThemeMediaResult soundtrackSongsResult) {
            this.instance.soundtrackSongsResult = soundtrackSongsResult;
            return this;
        }

        /**
         * returns a built AllThemeMediaResult instance.
         *
         * The builder is not reusable.
         */
        public AllThemeMediaResult build() {
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
    public static AllThemeMediaResult.Builder builder() {
        return new AllThemeMediaResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AllThemeMediaResult.Builder toBuilder() {
        return new AllThemeMediaResult.Builder().themeVideosResult(getThemeVideosResult())
                .themeSongsResult(getThemeSongsResult()).soundtrackSongsResult(getSoundtrackSongsResult());
    }
}

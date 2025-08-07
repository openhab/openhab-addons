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
    @JsonProperty(JSON_PROPERTY_THEME_VIDEOS_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ThemeMediaResult getThemeVideosResult() {
        return themeVideosResult;
    }

    @JsonProperty(JSON_PROPERTY_THEME_VIDEOS_RESULT)
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
    @JsonProperty(JSON_PROPERTY_THEME_SONGS_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ThemeMediaResult getThemeSongsResult() {
        return themeSongsResult;
    }

    @JsonProperty(JSON_PROPERTY_THEME_SONGS_RESULT)
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
    @JsonProperty(JSON_PROPERTY_SOUNDTRACK_SONGS_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ThemeMediaResult getSoundtrackSongsResult() {
        return soundtrackSongsResult;
    }

    @JsonProperty(JSON_PROPERTY_SOUNDTRACK_SONGS_RESULT)
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
}

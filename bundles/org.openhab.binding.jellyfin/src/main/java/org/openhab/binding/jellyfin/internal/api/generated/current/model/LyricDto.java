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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * LyricResponse model.
 */
@JsonPropertyOrder({ LyricDto.JSON_PROPERTY_METADATA, LyricDto.JSON_PROPERTY_LYRICS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LyricDto {
    public static final String JSON_PROPERTY_METADATA = "Metadata";
    @org.eclipse.jdt.annotation.NonNull
    private LyricMetadata metadata;

    public static final String JSON_PROPERTY_LYRICS = "Lyrics";
    @org.eclipse.jdt.annotation.NonNull
    private List<LyricLine> lyrics = new ArrayList<>();

    public LyricDto() {
    }

    public LyricDto metadata(@org.eclipse.jdt.annotation.NonNull LyricMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets or sets Metadata for the lyrics.
     * 
     * @return metadata
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LyricMetadata getMetadata() {
        return metadata;
    }

    @JsonProperty(JSON_PROPERTY_METADATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadata(@org.eclipse.jdt.annotation.NonNull LyricMetadata metadata) {
        this.metadata = metadata;
    }

    public LyricDto lyrics(@org.eclipse.jdt.annotation.NonNull List<LyricLine> lyrics) {
        this.lyrics = lyrics;
        return this;
    }

    public LyricDto addLyricsItem(LyricLine lyricsItem) {
        if (this.lyrics == null) {
            this.lyrics = new ArrayList<>();
        }
        this.lyrics.add(lyricsItem);
        return this;
    }

    /**
     * Gets or sets a collection of individual lyric lines.
     * 
     * @return lyrics
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LYRICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LyricLine> getLyrics() {
        return lyrics;
    }

    @JsonProperty(JSON_PROPERTY_LYRICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLyrics(@org.eclipse.jdt.annotation.NonNull List<LyricLine> lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * Return true if this LyricDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LyricDto lyricDto = (LyricDto) o;
        return Objects.equals(this.metadata, lyricDto.metadata) && Objects.equals(this.lyrics, lyricDto.lyrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, lyrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LyricDto {\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    lyrics: ").append(toIndentedString(lyrics)).append("\n");
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

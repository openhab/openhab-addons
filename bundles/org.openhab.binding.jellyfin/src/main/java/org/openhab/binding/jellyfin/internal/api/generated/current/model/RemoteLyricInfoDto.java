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
 * The remote lyric info dto.
 */
@JsonPropertyOrder({ RemoteLyricInfoDto.JSON_PROPERTY_ID, RemoteLyricInfoDto.JSON_PROPERTY_PROVIDER_NAME,
        RemoteLyricInfoDto.JSON_PROPERTY_LYRICS })

public class RemoteLyricInfoDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_PROVIDER_NAME = "ProviderName";
    @org.eclipse.jdt.annotation.NonNull
    private String providerName;

    public static final String JSON_PROPERTY_LYRICS = "Lyrics";
    @org.eclipse.jdt.annotation.NonNull
    private LyricDto lyrics;

    public RemoteLyricInfoDto() {
    }

    public RemoteLyricInfoDto id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id for the lyric.
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

    public RemoteLyricInfoDto providerName(@org.eclipse.jdt.annotation.NonNull String providerName) {
        this.providerName = providerName;
        return this;
    }

    /**
     * Gets the provider name.
     * 
     * @return providerName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProviderName() {
        return providerName;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderName(@org.eclipse.jdt.annotation.NonNull String providerName) {
        this.providerName = providerName;
    }

    public RemoteLyricInfoDto lyrics(@org.eclipse.jdt.annotation.NonNull LyricDto lyrics) {
        this.lyrics = lyrics;
        return this;
    }

    /**
     * Gets the lyrics.
     * 
     * @return lyrics
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LYRICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LyricDto getLyrics() {
        return lyrics;
    }

    @JsonProperty(JSON_PROPERTY_LYRICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLyrics(@org.eclipse.jdt.annotation.NonNull LyricDto lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * Return true if this RemoteLyricInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteLyricInfoDto remoteLyricInfoDto = (RemoteLyricInfoDto) o;
        return Objects.equals(this.id, remoteLyricInfoDto.id)
                && Objects.equals(this.providerName, remoteLyricInfoDto.providerName)
                && Objects.equals(this.lyrics, remoteLyricInfoDto.lyrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, providerName, lyrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RemoteLyricInfoDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
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

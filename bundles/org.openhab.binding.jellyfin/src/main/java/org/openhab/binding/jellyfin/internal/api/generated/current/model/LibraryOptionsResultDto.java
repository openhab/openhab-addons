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
 * Library options result dto.
 */
@JsonPropertyOrder({ LibraryOptionsResultDto.JSON_PROPERTY_METADATA_SAVERS,
        LibraryOptionsResultDto.JSON_PROPERTY_METADATA_READERS, LibraryOptionsResultDto.JSON_PROPERTY_SUBTITLE_FETCHERS,
        LibraryOptionsResultDto.JSON_PROPERTY_LYRIC_FETCHERS, LibraryOptionsResultDto.JSON_PROPERTY_TYPE_OPTIONS })

public class LibraryOptionsResultDto {
    public static final String JSON_PROPERTY_METADATA_SAVERS = "MetadataSavers";
    @org.eclipse.jdt.annotation.NonNull
    private List<LibraryOptionInfoDto> metadataSavers = new ArrayList<>();

    public static final String JSON_PROPERTY_METADATA_READERS = "MetadataReaders";
    @org.eclipse.jdt.annotation.NonNull
    private List<LibraryOptionInfoDto> metadataReaders = new ArrayList<>();

    public static final String JSON_PROPERTY_SUBTITLE_FETCHERS = "SubtitleFetchers";
    @org.eclipse.jdt.annotation.NonNull
    private List<LibraryOptionInfoDto> subtitleFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_LYRIC_FETCHERS = "LyricFetchers";
    @org.eclipse.jdt.annotation.NonNull
    private List<LibraryOptionInfoDto> lyricFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_TYPE_OPTIONS = "TypeOptions";
    @org.eclipse.jdt.annotation.NonNull
    private List<LibraryTypeOptionsDto> typeOptions = new ArrayList<>();

    public LibraryOptionsResultDto() {
    }

    public LibraryOptionsResultDto metadataSavers(
            @org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> metadataSavers) {
        this.metadataSavers = metadataSavers;
        return this;
    }

    public LibraryOptionsResultDto addMetadataSaversItem(LibraryOptionInfoDto metadataSaversItem) {
        if (this.metadataSavers == null) {
            this.metadataSavers = new ArrayList<>();
        }
        this.metadataSavers.add(metadataSaversItem);
        return this;
    }

    /**
     * Gets or sets the metadata savers.
     * 
     * @return metadataSavers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_SAVERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LibraryOptionInfoDto> getMetadataSavers() {
        return metadataSavers;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_SAVERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataSavers(@org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> metadataSavers) {
        this.metadataSavers = metadataSavers;
    }

    public LibraryOptionsResultDto metadataReaders(
            @org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> metadataReaders) {
        this.metadataReaders = metadataReaders;
        return this;
    }

    public LibraryOptionsResultDto addMetadataReadersItem(LibraryOptionInfoDto metadataReadersItem) {
        if (this.metadataReaders == null) {
            this.metadataReaders = new ArrayList<>();
        }
        this.metadataReaders.add(metadataReadersItem);
        return this;
    }

    /**
     * Gets or sets the metadata readers.
     * 
     * @return metadataReaders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_READERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LibraryOptionInfoDto> getMetadataReaders() {
        return metadataReaders;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_READERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataReaders(@org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> metadataReaders) {
        this.metadataReaders = metadataReaders;
    }

    public LibraryOptionsResultDto subtitleFetchers(
            @org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> subtitleFetchers) {
        this.subtitleFetchers = subtitleFetchers;
        return this;
    }

    public LibraryOptionsResultDto addSubtitleFetchersItem(LibraryOptionInfoDto subtitleFetchersItem) {
        if (this.subtitleFetchers == null) {
            this.subtitleFetchers = new ArrayList<>();
        }
        this.subtitleFetchers.add(subtitleFetchersItem);
        return this;
    }

    /**
     * Gets or sets the subtitle fetchers.
     * 
     * @return subtitleFetchers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUBTITLE_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LibraryOptionInfoDto> getSubtitleFetchers() {
        return subtitleFetchers;
    }

    @JsonProperty(JSON_PROPERTY_SUBTITLE_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleFetchers(@org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> subtitleFetchers) {
        this.subtitleFetchers = subtitleFetchers;
    }

    public LibraryOptionsResultDto lyricFetchers(
            @org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> lyricFetchers) {
        this.lyricFetchers = lyricFetchers;
        return this;
    }

    public LibraryOptionsResultDto addLyricFetchersItem(LibraryOptionInfoDto lyricFetchersItem) {
        if (this.lyricFetchers == null) {
            this.lyricFetchers = new ArrayList<>();
        }
        this.lyricFetchers.add(lyricFetchersItem);
        return this;
    }

    /**
     * Gets or sets the list of lyric fetchers.
     * 
     * @return lyricFetchers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LYRIC_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LibraryOptionInfoDto> getLyricFetchers() {
        return lyricFetchers;
    }

    @JsonProperty(JSON_PROPERTY_LYRIC_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLyricFetchers(@org.eclipse.jdt.annotation.NonNull List<LibraryOptionInfoDto> lyricFetchers) {
        this.lyricFetchers = lyricFetchers;
    }

    public LibraryOptionsResultDto typeOptions(
            @org.eclipse.jdt.annotation.NonNull List<LibraryTypeOptionsDto> typeOptions) {
        this.typeOptions = typeOptions;
        return this;
    }

    public LibraryOptionsResultDto addTypeOptionsItem(LibraryTypeOptionsDto typeOptionsItem) {
        if (this.typeOptions == null) {
            this.typeOptions = new ArrayList<>();
        }
        this.typeOptions.add(typeOptionsItem);
        return this;
    }

    /**
     * Gets or sets the type options.
     * 
     * @return typeOptions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LibraryTypeOptionsDto> getTypeOptions() {
        return typeOptions;
    }

    @JsonProperty(JSON_PROPERTY_TYPE_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTypeOptions(@org.eclipse.jdt.annotation.NonNull List<LibraryTypeOptionsDto> typeOptions) {
        this.typeOptions = typeOptions;
    }

    /**
     * Return true if this LibraryOptionsResultDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryOptionsResultDto libraryOptionsResultDto = (LibraryOptionsResultDto) o;
        return Objects.equals(this.metadataSavers, libraryOptionsResultDto.metadataSavers)
                && Objects.equals(this.metadataReaders, libraryOptionsResultDto.metadataReaders)
                && Objects.equals(this.subtitleFetchers, libraryOptionsResultDto.subtitleFetchers)
                && Objects.equals(this.lyricFetchers, libraryOptionsResultDto.lyricFetchers)
                && Objects.equals(this.typeOptions, libraryOptionsResultDto.typeOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadataSavers, metadataReaders, subtitleFetchers, lyricFetchers, typeOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryOptionsResultDto {\n");
        sb.append("    metadataSavers: ").append(toIndentedString(metadataSavers)).append("\n");
        sb.append("    metadataReaders: ").append(toIndentedString(metadataReaders)).append("\n");
        sb.append("    subtitleFetchers: ").append(toIndentedString(subtitleFetchers)).append("\n");
        sb.append("    lyricFetchers: ").append(toIndentedString(lyricFetchers)).append("\n");
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
}

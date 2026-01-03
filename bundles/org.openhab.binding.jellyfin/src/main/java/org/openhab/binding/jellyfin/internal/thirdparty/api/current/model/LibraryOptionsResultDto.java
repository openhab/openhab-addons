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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Library options result dto.
 */
@JsonPropertyOrder({ LibraryOptionsResultDto.JSON_PROPERTY_METADATA_SAVERS,
        LibraryOptionsResultDto.JSON_PROPERTY_METADATA_READERS, LibraryOptionsResultDto.JSON_PROPERTY_SUBTITLE_FETCHERS,
        LibraryOptionsResultDto.JSON_PROPERTY_LYRIC_FETCHERS,
        LibraryOptionsResultDto.JSON_PROPERTY_MEDIA_SEGMENT_PROVIDERS,
        LibraryOptionsResultDto.JSON_PROPERTY_TYPE_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryOptionsResultDto {
    public static final String JSON_PROPERTY_METADATA_SAVERS = "MetadataSavers";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryOptionInfoDto> metadataSavers = new ArrayList<>();

    public static final String JSON_PROPERTY_METADATA_READERS = "MetadataReaders";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryOptionInfoDto> metadataReaders = new ArrayList<>();

    public static final String JSON_PROPERTY_SUBTITLE_FETCHERS = "SubtitleFetchers";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryOptionInfoDto> subtitleFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_LYRIC_FETCHERS = "LyricFetchers";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryOptionInfoDto> lyricFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_MEDIA_SEGMENT_PROVIDERS = "MediaSegmentProviders";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryOptionInfoDto> mediaSegmentProviders = new ArrayList<>();

    public static final String JSON_PROPERTY_TYPE_OPTIONS = "TypeOptions";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryTypeOptionsDto> typeOptions = new ArrayList<>();

    public LibraryOptionsResultDto() {
    }

    public LibraryOptionsResultDto metadataSavers(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> metadataSavers) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_METADATA_SAVERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryOptionInfoDto> getMetadataSavers() {
        return metadataSavers;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_SAVERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataSavers(@org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> metadataSavers) {
        this.metadataSavers = metadataSavers;
    }

    public LibraryOptionsResultDto metadataReaders(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> metadataReaders) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_METADATA_READERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryOptionInfoDto> getMetadataReaders() {
        return metadataReaders;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_READERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataReaders(@org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> metadataReaders) {
        this.metadataReaders = metadataReaders;
    }

    public LibraryOptionsResultDto subtitleFetchers(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> subtitleFetchers) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryOptionInfoDto> getSubtitleFetchers() {
        return subtitleFetchers;
    }

    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleFetchers(@org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> subtitleFetchers) {
        this.subtitleFetchers = subtitleFetchers;
    }

    public LibraryOptionsResultDto lyricFetchers(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> lyricFetchers) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LYRIC_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryOptionInfoDto> getLyricFetchers() {
        return lyricFetchers;
    }

    @JsonProperty(value = JSON_PROPERTY_LYRIC_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLyricFetchers(@org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> lyricFetchers) {
        this.lyricFetchers = lyricFetchers;
    }

    public LibraryOptionsResultDto mediaSegmentProviders(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> mediaSegmentProviders) {
        this.mediaSegmentProviders = mediaSegmentProviders;
        return this;
    }

    public LibraryOptionsResultDto addMediaSegmentProvidersItem(LibraryOptionInfoDto mediaSegmentProvidersItem) {
        if (this.mediaSegmentProviders == null) {
            this.mediaSegmentProviders = new ArrayList<>();
        }
        this.mediaSegmentProviders.add(mediaSegmentProvidersItem);
        return this;
    }

    /**
     * Gets or sets the list of MediaSegment Providers.
     * 
     * @return mediaSegmentProviders
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SEGMENT_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryOptionInfoDto> getMediaSegmentProviders() {
        return mediaSegmentProviders;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SEGMENT_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSegmentProviders(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> mediaSegmentProviders) {
        this.mediaSegmentProviders = mediaSegmentProviders;
    }

    public LibraryOptionsResultDto typeOptions(
            @org.eclipse.jdt.annotation.Nullable List<LibraryTypeOptionsDto> typeOptions) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryTypeOptionsDto> getTypeOptions() {
        return typeOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTypeOptions(@org.eclipse.jdt.annotation.Nullable List<LibraryTypeOptionsDto> typeOptions) {
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
                && Objects.equals(this.mediaSegmentProviders, libraryOptionsResultDto.mediaSegmentProviders)
                && Objects.equals(this.typeOptions, libraryOptionsResultDto.typeOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadataSavers, metadataReaders, subtitleFetchers, lyricFetchers, mediaSegmentProviders,
                typeOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryOptionsResultDto {\n");
        sb.append("    metadataSavers: ").append(toIndentedString(metadataSavers)).append("\n");
        sb.append("    metadataReaders: ").append(toIndentedString(metadataReaders)).append("\n");
        sb.append("    subtitleFetchers: ").append(toIndentedString(subtitleFetchers)).append("\n");
        sb.append("    lyricFetchers: ").append(toIndentedString(lyricFetchers)).append("\n");
        sb.append("    mediaSegmentProviders: ").append(toIndentedString(mediaSegmentProviders)).append("\n");
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

        // add `MetadataSavers` to the URL query string
        if (getMetadataSavers() != null) {
            for (int i = 0; i < getMetadataSavers().size(); i++) {
                if (getMetadataSavers().get(i) != null) {
                    joiner.add(getMetadataSavers().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sMetadataSavers%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `MetadataReaders` to the URL query string
        if (getMetadataReaders() != null) {
            for (int i = 0; i < getMetadataReaders().size(); i++) {
                if (getMetadataReaders().get(i) != null) {
                    joiner.add(getMetadataReaders().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sMetadataReaders%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `SubtitleFetchers` to the URL query string
        if (getSubtitleFetchers() != null) {
            for (int i = 0; i < getSubtitleFetchers().size(); i++) {
                if (getSubtitleFetchers().get(i) != null) {
                    joiner.add(getSubtitleFetchers().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sSubtitleFetchers%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `LyricFetchers` to the URL query string
        if (getLyricFetchers() != null) {
            for (int i = 0; i < getLyricFetchers().size(); i++) {
                if (getLyricFetchers().get(i) != null) {
                    joiner.add(getLyricFetchers().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sLyricFetchers%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `MediaSegmentProviders` to the URL query string
        if (getMediaSegmentProviders() != null) {
            for (int i = 0; i < getMediaSegmentProviders().size(); i++) {
                if (getMediaSegmentProviders().get(i) != null) {
                    joiner.add(getMediaSegmentProviders().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sMediaSegmentProviders%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
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

        private LibraryOptionsResultDto instance;

        public Builder() {
            this(new LibraryOptionsResultDto());
        }

        protected Builder(LibraryOptionsResultDto instance) {
            this.instance = instance;
        }

        public LibraryOptionsResultDto.Builder metadataSavers(List<LibraryOptionInfoDto> metadataSavers) {
            this.instance.metadataSavers = metadataSavers;
            return this;
        }

        public LibraryOptionsResultDto.Builder metadataReaders(List<LibraryOptionInfoDto> metadataReaders) {
            this.instance.metadataReaders = metadataReaders;
            return this;
        }

        public LibraryOptionsResultDto.Builder subtitleFetchers(List<LibraryOptionInfoDto> subtitleFetchers) {
            this.instance.subtitleFetchers = subtitleFetchers;
            return this;
        }

        public LibraryOptionsResultDto.Builder lyricFetchers(List<LibraryOptionInfoDto> lyricFetchers) {
            this.instance.lyricFetchers = lyricFetchers;
            return this;
        }

        public LibraryOptionsResultDto.Builder mediaSegmentProviders(List<LibraryOptionInfoDto> mediaSegmentProviders) {
            this.instance.mediaSegmentProviders = mediaSegmentProviders;
            return this;
        }

        public LibraryOptionsResultDto.Builder typeOptions(List<LibraryTypeOptionsDto> typeOptions) {
            this.instance.typeOptions = typeOptions;
            return this;
        }

        /**
         * returns a built LibraryOptionsResultDto instance.
         *
         * The builder is not reusable.
         */
        public LibraryOptionsResultDto build() {
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
    public static LibraryOptionsResultDto.Builder builder() {
        return new LibraryOptionsResultDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LibraryOptionsResultDto.Builder toBuilder() {
        return new LibraryOptionsResultDto.Builder().metadataSavers(getMetadataSavers())
                .metadataReaders(getMetadataReaders()).subtitleFetchers(getSubtitleFetchers())
                .lyricFetchers(getLyricFetchers()).mediaSegmentProviders(getMediaSegmentProviders())
                .typeOptions(getTypeOptions());
    }
}

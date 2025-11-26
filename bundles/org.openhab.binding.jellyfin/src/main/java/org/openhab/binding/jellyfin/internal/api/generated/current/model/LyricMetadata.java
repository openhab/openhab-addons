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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * LyricMetadata model.
 */
@JsonPropertyOrder({ LyricMetadata.JSON_PROPERTY_ARTIST, LyricMetadata.JSON_PROPERTY_ALBUM,
        LyricMetadata.JSON_PROPERTY_TITLE, LyricMetadata.JSON_PROPERTY_AUTHOR, LyricMetadata.JSON_PROPERTY_LENGTH,
        LyricMetadata.JSON_PROPERTY_BY, LyricMetadata.JSON_PROPERTY_OFFSET, LyricMetadata.JSON_PROPERTY_CREATOR,
        LyricMetadata.JSON_PROPERTY_VERSION, LyricMetadata.JSON_PROPERTY_IS_SYNCED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LyricMetadata {
    public static final String JSON_PROPERTY_ARTIST = "Artist";
    @org.eclipse.jdt.annotation.NonNull
    private String artist;

    public static final String JSON_PROPERTY_ALBUM = "Album";
    @org.eclipse.jdt.annotation.NonNull
    private String album;

    public static final String JSON_PROPERTY_TITLE = "Title";
    @org.eclipse.jdt.annotation.NonNull
    private String title;

    public static final String JSON_PROPERTY_AUTHOR = "Author";
    @org.eclipse.jdt.annotation.NonNull
    private String author;

    public static final String JSON_PROPERTY_LENGTH = "Length";
    @org.eclipse.jdt.annotation.NonNull
    private Long length;

    public static final String JSON_PROPERTY_BY = "By";
    @org.eclipse.jdt.annotation.NonNull
    private String by;

    public static final String JSON_PROPERTY_OFFSET = "Offset";
    @org.eclipse.jdt.annotation.NonNull
    private Long offset;

    public static final String JSON_PROPERTY_CREATOR = "Creator";
    @org.eclipse.jdt.annotation.NonNull
    private String creator;

    public static final String JSON_PROPERTY_VERSION = "Version";
    @org.eclipse.jdt.annotation.NonNull
    private String version;

    public static final String JSON_PROPERTY_IS_SYNCED = "IsSynced";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isSynced;

    public LyricMetadata() {
    }

    public LyricMetadata artist(@org.eclipse.jdt.annotation.NonNull String artist) {
        this.artist = artist;
        return this;
    }

    /**
     * Gets or sets the song artist.
     * 
     * @return artist
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getArtist() {
        return artist;
    }

    @JsonProperty(value = JSON_PROPERTY_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtist(@org.eclipse.jdt.annotation.NonNull String artist) {
        this.artist = artist;
    }

    public LyricMetadata album(@org.eclipse.jdt.annotation.NonNull String album) {
        this.album = album;
        return this;
    }

    /**
     * Gets or sets the album this song is on.
     * 
     * @return album
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAlbum() {
        return album;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbum(@org.eclipse.jdt.annotation.NonNull String album) {
        this.album = album;
    }

    public LyricMetadata title(@org.eclipse.jdt.annotation.NonNull String title) {
        this.title = title;
        return this;
    }

    /**
     * Gets or sets the title of the song.
     * 
     * @return title
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTitle() {
        return title;
    }

    @JsonProperty(value = JSON_PROPERTY_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTitle(@org.eclipse.jdt.annotation.NonNull String title) {
        this.title = title;
    }

    public LyricMetadata author(@org.eclipse.jdt.annotation.NonNull String author) {
        this.author = author;
        return this;
    }

    /**
     * Gets or sets the author of the lyric data.
     * 
     * @return author
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AUTHOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAuthor() {
        return author;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTHOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAuthor(@org.eclipse.jdt.annotation.NonNull String author) {
        this.author = author;
    }

    public LyricMetadata length(@org.eclipse.jdt.annotation.NonNull Long length) {
        this.length = length;
        return this;
    }

    /**
     * Gets or sets the length of the song in ticks.
     * 
     * @return length
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getLength() {
        return length;
    }

    @JsonProperty(value = JSON_PROPERTY_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLength(@org.eclipse.jdt.annotation.NonNull Long length) {
        this.length = length;
    }

    public LyricMetadata by(@org.eclipse.jdt.annotation.NonNull String by) {
        this.by = by;
        return this;
    }

    /**
     * Gets or sets who the LRC file was created by.
     * 
     * @return by
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBy() {
        return by;
    }

    @JsonProperty(value = JSON_PROPERTY_BY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBy(@org.eclipse.jdt.annotation.NonNull String by) {
        this.by = by;
    }

    public LyricMetadata offset(@org.eclipse.jdt.annotation.NonNull Long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Gets or sets the lyric offset compared to audio in ticks.
     * 
     * @return offset
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OFFSET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getOffset() {
        return offset;
    }

    @JsonProperty(value = JSON_PROPERTY_OFFSET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOffset(@org.eclipse.jdt.annotation.NonNull Long offset) {
        this.offset = offset;
    }

    public LyricMetadata creator(@org.eclipse.jdt.annotation.NonNull String creator) {
        this.creator = creator;
        return this;
    }

    /**
     * Gets or sets the software used to create the LRC file.
     * 
     * @return creator
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CREATOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCreator() {
        return creator;
    }

    @JsonProperty(value = JSON_PROPERTY_CREATOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCreator(@org.eclipse.jdt.annotation.NonNull String creator) {
        this.creator = creator;
    }

    public LyricMetadata version(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets or sets the version of the creator used.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersion() {
        return version;
    }

    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersion(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
    }

    public LyricMetadata isSynced(@org.eclipse.jdt.annotation.NonNull Boolean isSynced) {
        this.isSynced = isSynced;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this lyric is synced.
     * 
     * @return isSynced
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_SYNCED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsSynced() {
        return isSynced;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_SYNCED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsSynced(@org.eclipse.jdt.annotation.NonNull Boolean isSynced) {
        this.isSynced = isSynced;
    }

    /**
     * Return true if this LyricMetadata object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LyricMetadata lyricMetadata = (LyricMetadata) o;
        return Objects.equals(this.artist, lyricMetadata.artist) && Objects.equals(this.album, lyricMetadata.album)
                && Objects.equals(this.title, lyricMetadata.title) && Objects.equals(this.author, lyricMetadata.author)
                && Objects.equals(this.length, lyricMetadata.length) && Objects.equals(this.by, lyricMetadata.by)
                && Objects.equals(this.offset, lyricMetadata.offset)
                && Objects.equals(this.creator, lyricMetadata.creator)
                && Objects.equals(this.version, lyricMetadata.version)
                && Objects.equals(this.isSynced, lyricMetadata.isSynced);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist, album, title, author, length, by, offset, creator, version, isSynced);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LyricMetadata {\n");
        sb.append("    artist: ").append(toIndentedString(artist)).append("\n");
        sb.append("    album: ").append(toIndentedString(album)).append("\n");
        sb.append("    title: ").append(toIndentedString(title)).append("\n");
        sb.append("    author: ").append(toIndentedString(author)).append("\n");
        sb.append("    length: ").append(toIndentedString(length)).append("\n");
        sb.append("    by: ").append(toIndentedString(by)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    isSynced: ").append(toIndentedString(isSynced)).append("\n");
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

        // add `Artist` to the URL query string
        if (getArtist() != null) {
            joiner.add(String.format(Locale.ROOT, "%sArtist%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getArtist()))));
        }

        // add `Album` to the URL query string
        if (getAlbum() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbum%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbum()))));
        }

        // add `Title` to the URL query string
        if (getTitle() != null) {
            joiner.add(String.format(Locale.ROOT, "%sTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTitle()))));
        }

        // add `Author` to the URL query string
        if (getAuthor() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAuthor%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAuthor()))));
        }

        // add `Length` to the URL query string
        if (getLength() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLength%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLength()))));
        }

        // add `By` to the URL query string
        if (getBy() != null) {
            joiner.add(String.format(Locale.ROOT, "%sBy%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBy()))));
        }

        // add `Offset` to the URL query string
        if (getOffset() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOffset%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOffset()))));
        }

        // add `Creator` to the URL query string
        if (getCreator() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCreator%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCreator()))));
        }

        // add `Version` to the URL query string
        if (getVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
        }

        // add `IsSynced` to the URL query string
        if (getIsSynced() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsSynced%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsSynced()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LyricMetadata instance;

        public Builder() {
            this(new LyricMetadata());
        }

        protected Builder(LyricMetadata instance) {
            this.instance = instance;
        }

        public LyricMetadata.Builder artist(String artist) {
            this.instance.artist = artist;
            return this;
        }

        public LyricMetadata.Builder album(String album) {
            this.instance.album = album;
            return this;
        }

        public LyricMetadata.Builder title(String title) {
            this.instance.title = title;
            return this;
        }

        public LyricMetadata.Builder author(String author) {
            this.instance.author = author;
            return this;
        }

        public LyricMetadata.Builder length(Long length) {
            this.instance.length = length;
            return this;
        }

        public LyricMetadata.Builder by(String by) {
            this.instance.by = by;
            return this;
        }

        public LyricMetadata.Builder offset(Long offset) {
            this.instance.offset = offset;
            return this;
        }

        public LyricMetadata.Builder creator(String creator) {
            this.instance.creator = creator;
            return this;
        }

        public LyricMetadata.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public LyricMetadata.Builder isSynced(Boolean isSynced) {
            this.instance.isSynced = isSynced;
            return this;
        }

        /**
         * returns a built LyricMetadata instance.
         *
         * The builder is not reusable.
         */
        public LyricMetadata build() {
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
    public static LyricMetadata.Builder builder() {
        return new LyricMetadata.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LyricMetadata.Builder toBuilder() {
        return new LyricMetadata.Builder().artist(getArtist()).album(getAlbum()).title(getTitle()).author(getAuthor())
                .length(getLength()).by(getBy()).offset(getOffset()).creator(getCreator()).version(getVersion())
                .isSynced(getIsSynced());
    }
}

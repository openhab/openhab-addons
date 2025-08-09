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
 * LyricMetadata model.
 */
@JsonPropertyOrder({ LyricMetadata.JSON_PROPERTY_ARTIST, LyricMetadata.JSON_PROPERTY_ALBUM,
        LyricMetadata.JSON_PROPERTY_TITLE, LyricMetadata.JSON_PROPERTY_AUTHOR, LyricMetadata.JSON_PROPERTY_LENGTH,
        LyricMetadata.JSON_PROPERTY_BY, LyricMetadata.JSON_PROPERTY_OFFSET, LyricMetadata.JSON_PROPERTY_CREATOR,
        LyricMetadata.JSON_PROPERTY_VERSION, LyricMetadata.JSON_PROPERTY_IS_SYNCED })

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
    @JsonProperty(JSON_PROPERTY_ARTIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getArtist() {
        return artist;
    }

    @JsonProperty(JSON_PROPERTY_ARTIST)
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
    @JsonProperty(JSON_PROPERTY_ALBUM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAlbum() {
        return album;
    }

    @JsonProperty(JSON_PROPERTY_ALBUM)
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
    @JsonProperty(JSON_PROPERTY_TITLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTitle() {
        return title;
    }

    @JsonProperty(JSON_PROPERTY_TITLE)
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
    @JsonProperty(JSON_PROPERTY_AUTHOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAuthor() {
        return author;
    }

    @JsonProperty(JSON_PROPERTY_AUTHOR)
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
    @JsonProperty(JSON_PROPERTY_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getLength() {
        return length;
    }

    @JsonProperty(JSON_PROPERTY_LENGTH)
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
    @JsonProperty(JSON_PROPERTY_BY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBy() {
        return by;
    }

    @JsonProperty(JSON_PROPERTY_BY)
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
    @JsonProperty(JSON_PROPERTY_OFFSET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getOffset() {
        return offset;
    }

    @JsonProperty(JSON_PROPERTY_OFFSET)
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
    @JsonProperty(JSON_PROPERTY_CREATOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCreator() {
        return creator;
    }

    @JsonProperty(JSON_PROPERTY_CREATOR)
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
    @JsonProperty(JSON_PROPERTY_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getVersion() {
        return version;
    }

    @JsonProperty(JSON_PROPERTY_VERSION)
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
    @JsonProperty(JSON_PROPERTY_IS_SYNCED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsSynced() {
        return isSynced;
    }

    @JsonProperty(JSON_PROPERTY_IS_SYNCED)
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
}

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

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class LibrarySummary.
 */
@JsonPropertyOrder({ ItemCounts.JSON_PROPERTY_MOVIE_COUNT, ItemCounts.JSON_PROPERTY_SERIES_COUNT,
        ItemCounts.JSON_PROPERTY_EPISODE_COUNT, ItemCounts.JSON_PROPERTY_ARTIST_COUNT,
        ItemCounts.JSON_PROPERTY_PROGRAM_COUNT, ItemCounts.JSON_PROPERTY_TRAILER_COUNT,
        ItemCounts.JSON_PROPERTY_SONG_COUNT, ItemCounts.JSON_PROPERTY_ALBUM_COUNT,
        ItemCounts.JSON_PROPERTY_MUSIC_VIDEO_COUNT, ItemCounts.JSON_PROPERTY_BOX_SET_COUNT,
        ItemCounts.JSON_PROPERTY_BOOK_COUNT, ItemCounts.JSON_PROPERTY_ITEM_COUNT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ItemCounts {
    public static final String JSON_PROPERTY_MOVIE_COUNT = "MovieCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer movieCount;

    public static final String JSON_PROPERTY_SERIES_COUNT = "SeriesCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer seriesCount;

    public static final String JSON_PROPERTY_EPISODE_COUNT = "EpisodeCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer episodeCount;

    public static final String JSON_PROPERTY_ARTIST_COUNT = "ArtistCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer artistCount;

    public static final String JSON_PROPERTY_PROGRAM_COUNT = "ProgramCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer programCount;

    public static final String JSON_PROPERTY_TRAILER_COUNT = "TrailerCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer trailerCount;

    public static final String JSON_PROPERTY_SONG_COUNT = "SongCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer songCount;

    public static final String JSON_PROPERTY_ALBUM_COUNT = "AlbumCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer albumCount;

    public static final String JSON_PROPERTY_MUSIC_VIDEO_COUNT = "MusicVideoCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer musicVideoCount;

    public static final String JSON_PROPERTY_BOX_SET_COUNT = "BoxSetCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer boxSetCount;

    public static final String JSON_PROPERTY_BOOK_COUNT = "BookCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer bookCount;

    public static final String JSON_PROPERTY_ITEM_COUNT = "ItemCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer itemCount;

    public ItemCounts() {
    }

    public ItemCounts movieCount(@org.eclipse.jdt.annotation.Nullable Integer movieCount) {
        this.movieCount = movieCount;
        return this;
    }

    /**
     * Gets or sets the movie count.
     * 
     * @return movieCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MOVIE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMovieCount() {
        return movieCount;
    }

    @JsonProperty(value = JSON_PROPERTY_MOVIE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMovieCount(@org.eclipse.jdt.annotation.Nullable Integer movieCount) {
        this.movieCount = movieCount;
    }

    public ItemCounts seriesCount(@org.eclipse.jdt.annotation.Nullable Integer seriesCount) {
        this.seriesCount = seriesCount;
        return this;
    }

    /**
     * Gets or sets the series count.
     * 
     * @return seriesCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SERIES_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSeriesCount() {
        return seriesCount;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesCount(@org.eclipse.jdt.annotation.Nullable Integer seriesCount) {
        this.seriesCount = seriesCount;
    }

    public ItemCounts episodeCount(@org.eclipse.jdt.annotation.Nullable Integer episodeCount) {
        this.episodeCount = episodeCount;
        return this;
    }

    /**
     * Gets or sets the episode count.
     * 
     * @return episodeCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EPISODE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getEpisodeCount() {
        return episodeCount;
    }

    @JsonProperty(value = JSON_PROPERTY_EPISODE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpisodeCount(@org.eclipse.jdt.annotation.Nullable Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public ItemCounts artistCount(@org.eclipse.jdt.annotation.Nullable Integer artistCount) {
        this.artistCount = artistCount;
        return this;
    }

    /**
     * Gets or sets the artist count.
     * 
     * @return artistCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ARTIST_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getArtistCount() {
        return artistCount;
    }

    @JsonProperty(value = JSON_PROPERTY_ARTIST_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtistCount(@org.eclipse.jdt.annotation.Nullable Integer artistCount) {
        this.artistCount = artistCount;
    }

    public ItemCounts programCount(@org.eclipse.jdt.annotation.Nullable Integer programCount) {
        this.programCount = programCount;
        return this;
    }

    /**
     * Gets or sets the program count.
     * 
     * @return programCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROGRAM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getProgramCount() {
        return programCount;
    }

    @JsonProperty(value = JSON_PROPERTY_PROGRAM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramCount(@org.eclipse.jdt.annotation.Nullable Integer programCount) {
        this.programCount = programCount;
    }

    public ItemCounts trailerCount(@org.eclipse.jdt.annotation.Nullable Integer trailerCount) {
        this.trailerCount = trailerCount;
        return this;
    }

    /**
     * Gets or sets the trailer count.
     * 
     * @return trailerCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TRAILER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTrailerCount() {
        return trailerCount;
    }

    @JsonProperty(value = JSON_PROPERTY_TRAILER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrailerCount(@org.eclipse.jdt.annotation.Nullable Integer trailerCount) {
        this.trailerCount = trailerCount;
    }

    public ItemCounts songCount(@org.eclipse.jdt.annotation.Nullable Integer songCount) {
        this.songCount = songCount;
        return this;
    }

    /**
     * Gets or sets the song count.
     * 
     * @return songCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SONG_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSongCount() {
        return songCount;
    }

    @JsonProperty(value = JSON_PROPERTY_SONG_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSongCount(@org.eclipse.jdt.annotation.Nullable Integer songCount) {
        this.songCount = songCount;
    }

    public ItemCounts albumCount(@org.eclipse.jdt.annotation.Nullable Integer albumCount) {
        this.albumCount = albumCount;
        return this;
    }

    /**
     * Gets or sets the album count.
     * 
     * @return albumCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ALBUM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAlbumCount() {
        return albumCount;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumCount(@org.eclipse.jdt.annotation.Nullable Integer albumCount) {
        this.albumCount = albumCount;
    }

    public ItemCounts musicVideoCount(@org.eclipse.jdt.annotation.Nullable Integer musicVideoCount) {
        this.musicVideoCount = musicVideoCount;
        return this;
    }

    /**
     * Gets or sets the music video count.
     * 
     * @return musicVideoCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MUSIC_VIDEO_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMusicVideoCount() {
        return musicVideoCount;
    }

    @JsonProperty(value = JSON_PROPERTY_MUSIC_VIDEO_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMusicVideoCount(@org.eclipse.jdt.annotation.Nullable Integer musicVideoCount) {
        this.musicVideoCount = musicVideoCount;
    }

    public ItemCounts boxSetCount(@org.eclipse.jdt.annotation.Nullable Integer boxSetCount) {
        this.boxSetCount = boxSetCount;
        return this;
    }

    /**
     * Gets or sets the box set count.
     * 
     * @return boxSetCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BOX_SET_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBoxSetCount() {
        return boxSetCount;
    }

    @JsonProperty(value = JSON_PROPERTY_BOX_SET_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBoxSetCount(@org.eclipse.jdt.annotation.Nullable Integer boxSetCount) {
        this.boxSetCount = boxSetCount;
    }

    public ItemCounts bookCount(@org.eclipse.jdt.annotation.Nullable Integer bookCount) {
        this.bookCount = bookCount;
        return this;
    }

    /**
     * Gets or sets the book count.
     * 
     * @return bookCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BOOK_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBookCount() {
        return bookCount;
    }

    @JsonProperty(value = JSON_PROPERTY_BOOK_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBookCount(@org.eclipse.jdt.annotation.Nullable Integer bookCount) {
        this.bookCount = bookCount;
    }

    public ItemCounts itemCount(@org.eclipse.jdt.annotation.Nullable Integer itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    /**
     * Gets or sets the item count.
     * 
     * @return itemCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getItemCount() {
        return itemCount;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemCount(@org.eclipse.jdt.annotation.Nullable Integer itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * Return true if this ItemCounts object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemCounts itemCounts = (ItemCounts) o;
        return Objects.equals(this.movieCount, itemCounts.movieCount)
                && Objects.equals(this.seriesCount, itemCounts.seriesCount)
                && Objects.equals(this.episodeCount, itemCounts.episodeCount)
                && Objects.equals(this.artistCount, itemCounts.artistCount)
                && Objects.equals(this.programCount, itemCounts.programCount)
                && Objects.equals(this.trailerCount, itemCounts.trailerCount)
                && Objects.equals(this.songCount, itemCounts.songCount)
                && Objects.equals(this.albumCount, itemCounts.albumCount)
                && Objects.equals(this.musicVideoCount, itemCounts.musicVideoCount)
                && Objects.equals(this.boxSetCount, itemCounts.boxSetCount)
                && Objects.equals(this.bookCount, itemCounts.bookCount)
                && Objects.equals(this.itemCount, itemCounts.itemCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieCount, seriesCount, episodeCount, artistCount, programCount, trailerCount, songCount,
                albumCount, musicVideoCount, boxSetCount, bookCount, itemCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ItemCounts {\n");
        sb.append("    movieCount: ").append(toIndentedString(movieCount)).append("\n");
        sb.append("    seriesCount: ").append(toIndentedString(seriesCount)).append("\n");
        sb.append("    episodeCount: ").append(toIndentedString(episodeCount)).append("\n");
        sb.append("    artistCount: ").append(toIndentedString(artistCount)).append("\n");
        sb.append("    programCount: ").append(toIndentedString(programCount)).append("\n");
        sb.append("    trailerCount: ").append(toIndentedString(trailerCount)).append("\n");
        sb.append("    songCount: ").append(toIndentedString(songCount)).append("\n");
        sb.append("    albumCount: ").append(toIndentedString(albumCount)).append("\n");
        sb.append("    musicVideoCount: ").append(toIndentedString(musicVideoCount)).append("\n");
        sb.append("    boxSetCount: ").append(toIndentedString(boxSetCount)).append("\n");
        sb.append("    bookCount: ").append(toIndentedString(bookCount)).append("\n");
        sb.append("    itemCount: ").append(toIndentedString(itemCount)).append("\n");
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

        // add `MovieCount` to the URL query string
        if (getMovieCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMovieCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMovieCount()))));
        }

        // add `SeriesCount` to the URL query string
        if (getSeriesCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSeriesCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesCount()))));
        }

        // add `EpisodeCount` to the URL query string
        if (getEpisodeCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEpisodeCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEpisodeCount()))));
        }

        // add `ArtistCount` to the URL query string
        if (getArtistCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sArtistCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getArtistCount()))));
        }

        // add `ProgramCount` to the URL query string
        if (getProgramCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProgramCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProgramCount()))));
        }

        // add `TrailerCount` to the URL query string
        if (getTrailerCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTrailerCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTrailerCount()))));
        }

        // add `SongCount` to the URL query string
        if (getSongCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSongCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSongCount()))));
        }

        // add `AlbumCount` to the URL query string
        if (getAlbumCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAlbumCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumCount()))));
        }

        // add `MusicVideoCount` to the URL query string
        if (getMusicVideoCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMusicVideoCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMusicVideoCount()))));
        }

        // add `BoxSetCount` to the URL query string
        if (getBoxSetCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBoxSetCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBoxSetCount()))));
        }

        // add `BookCount` to the URL query string
        if (getBookCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBookCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBookCount()))));
        }

        // add `ItemCount` to the URL query string
        if (getItemCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sItemCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemCount()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ItemCounts instance;

        public Builder() {
            this(new ItemCounts());
        }

        protected Builder(ItemCounts instance) {
            this.instance = instance;
        }

        public ItemCounts.Builder movieCount(Integer movieCount) {
            this.instance.movieCount = movieCount;
            return this;
        }

        public ItemCounts.Builder seriesCount(Integer seriesCount) {
            this.instance.seriesCount = seriesCount;
            return this;
        }

        public ItemCounts.Builder episodeCount(Integer episodeCount) {
            this.instance.episodeCount = episodeCount;
            return this;
        }

        public ItemCounts.Builder artistCount(Integer artistCount) {
            this.instance.artistCount = artistCount;
            return this;
        }

        public ItemCounts.Builder programCount(Integer programCount) {
            this.instance.programCount = programCount;
            return this;
        }

        public ItemCounts.Builder trailerCount(Integer trailerCount) {
            this.instance.trailerCount = trailerCount;
            return this;
        }

        public ItemCounts.Builder songCount(Integer songCount) {
            this.instance.songCount = songCount;
            return this;
        }

        public ItemCounts.Builder albumCount(Integer albumCount) {
            this.instance.albumCount = albumCount;
            return this;
        }

        public ItemCounts.Builder musicVideoCount(Integer musicVideoCount) {
            this.instance.musicVideoCount = musicVideoCount;
            return this;
        }

        public ItemCounts.Builder boxSetCount(Integer boxSetCount) {
            this.instance.boxSetCount = boxSetCount;
            return this;
        }

        public ItemCounts.Builder bookCount(Integer bookCount) {
            this.instance.bookCount = bookCount;
            return this;
        }

        public ItemCounts.Builder itemCount(Integer itemCount) {
            this.instance.itemCount = itemCount;
            return this;
        }

        /**
         * returns a built ItemCounts instance.
         *
         * The builder is not reusable.
         */
        public ItemCounts build() {
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
    public static ItemCounts.Builder builder() {
        return new ItemCounts.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ItemCounts.Builder toBuilder() {
        return new ItemCounts.Builder().movieCount(getMovieCount()).seriesCount(getSeriesCount())
                .episodeCount(getEpisodeCount()).artistCount(getArtistCount()).programCount(getProgramCount())
                .trailerCount(getTrailerCount()).songCount(getSongCount()).albumCount(getAlbumCount())
                .musicVideoCount(getMusicVideoCount()).boxSetCount(getBoxSetCount()).bookCount(getBookCount())
                .itemCount(getItemCount());
    }
}

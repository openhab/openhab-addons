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
 * Class LibrarySummary.
 */
@JsonPropertyOrder({ ItemCounts.JSON_PROPERTY_MOVIE_COUNT, ItemCounts.JSON_PROPERTY_SERIES_COUNT,
        ItemCounts.JSON_PROPERTY_EPISODE_COUNT, ItemCounts.JSON_PROPERTY_ARTIST_COUNT,
        ItemCounts.JSON_PROPERTY_PROGRAM_COUNT, ItemCounts.JSON_PROPERTY_TRAILER_COUNT,
        ItemCounts.JSON_PROPERTY_SONG_COUNT, ItemCounts.JSON_PROPERTY_ALBUM_COUNT,
        ItemCounts.JSON_PROPERTY_MUSIC_VIDEO_COUNT, ItemCounts.JSON_PROPERTY_BOX_SET_COUNT,
        ItemCounts.JSON_PROPERTY_BOOK_COUNT, ItemCounts.JSON_PROPERTY_ITEM_COUNT })

public class ItemCounts {
    public static final String JSON_PROPERTY_MOVIE_COUNT = "MovieCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer movieCount;

    public static final String JSON_PROPERTY_SERIES_COUNT = "SeriesCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer seriesCount;

    public static final String JSON_PROPERTY_EPISODE_COUNT = "EpisodeCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer episodeCount;

    public static final String JSON_PROPERTY_ARTIST_COUNT = "ArtistCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer artistCount;

    public static final String JSON_PROPERTY_PROGRAM_COUNT = "ProgramCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer programCount;

    public static final String JSON_PROPERTY_TRAILER_COUNT = "TrailerCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer trailerCount;

    public static final String JSON_PROPERTY_SONG_COUNT = "SongCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer songCount;

    public static final String JSON_PROPERTY_ALBUM_COUNT = "AlbumCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer albumCount;

    public static final String JSON_PROPERTY_MUSIC_VIDEO_COUNT = "MusicVideoCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer musicVideoCount;

    public static final String JSON_PROPERTY_BOX_SET_COUNT = "BoxSetCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer boxSetCount;

    public static final String JSON_PROPERTY_BOOK_COUNT = "BookCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer bookCount;

    public static final String JSON_PROPERTY_ITEM_COUNT = "ItemCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer itemCount;

    public ItemCounts() {
    }

    public ItemCounts movieCount(@org.eclipse.jdt.annotation.NonNull Integer movieCount) {
        this.movieCount = movieCount;
        return this;
    }

    /**
     * Gets or sets the movie count.
     * 
     * @return movieCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MOVIE_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMovieCount() {
        return movieCount;
    }

    @JsonProperty(JSON_PROPERTY_MOVIE_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMovieCount(@org.eclipse.jdt.annotation.NonNull Integer movieCount) {
        this.movieCount = movieCount;
    }

    public ItemCounts seriesCount(@org.eclipse.jdt.annotation.NonNull Integer seriesCount) {
        this.seriesCount = seriesCount;
        return this;
    }

    /**
     * Gets or sets the series count.
     * 
     * @return seriesCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERIES_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSeriesCount() {
        return seriesCount;
    }

    @JsonProperty(JSON_PROPERTY_SERIES_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesCount(@org.eclipse.jdt.annotation.NonNull Integer seriesCount) {
        this.seriesCount = seriesCount;
    }

    public ItemCounts episodeCount(@org.eclipse.jdt.annotation.NonNull Integer episodeCount) {
        this.episodeCount = episodeCount;
        return this;
    }

    /**
     * Gets or sets the episode count.
     * 
     * @return episodeCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EPISODE_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    @JsonProperty(JSON_PROPERTY_EPISODE_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpisodeCount(@org.eclipse.jdt.annotation.NonNull Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public ItemCounts artistCount(@org.eclipse.jdt.annotation.NonNull Integer artistCount) {
        this.artistCount = artistCount;
        return this;
    }

    /**
     * Gets or sets the artist count.
     * 
     * @return artistCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ARTIST_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getArtistCount() {
        return artistCount;
    }

    @JsonProperty(JSON_PROPERTY_ARTIST_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtistCount(@org.eclipse.jdt.annotation.NonNull Integer artistCount) {
        this.artistCount = artistCount;
    }

    public ItemCounts programCount(@org.eclipse.jdt.annotation.NonNull Integer programCount) {
        this.programCount = programCount;
        return this;
    }

    /**
     * Gets or sets the program count.
     * 
     * @return programCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROGRAM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getProgramCount() {
        return programCount;
    }

    @JsonProperty(JSON_PROPERTY_PROGRAM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramCount(@org.eclipse.jdt.annotation.NonNull Integer programCount) {
        this.programCount = programCount;
    }

    public ItemCounts trailerCount(@org.eclipse.jdt.annotation.NonNull Integer trailerCount) {
        this.trailerCount = trailerCount;
        return this;
    }

    /**
     * Gets or sets the trailer count.
     * 
     * @return trailerCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRAILER_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTrailerCount() {
        return trailerCount;
    }

    @JsonProperty(JSON_PROPERTY_TRAILER_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrailerCount(@org.eclipse.jdt.annotation.NonNull Integer trailerCount) {
        this.trailerCount = trailerCount;
    }

    public ItemCounts songCount(@org.eclipse.jdt.annotation.NonNull Integer songCount) {
        this.songCount = songCount;
        return this;
    }

    /**
     * Gets or sets the song count.
     * 
     * @return songCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SONG_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSongCount() {
        return songCount;
    }

    @JsonProperty(JSON_PROPERTY_SONG_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSongCount(@org.eclipse.jdt.annotation.NonNull Integer songCount) {
        this.songCount = songCount;
    }

    public ItemCounts albumCount(@org.eclipse.jdt.annotation.NonNull Integer albumCount) {
        this.albumCount = albumCount;
        return this;
    }

    /**
     * Gets or sets the album count.
     * 
     * @return albumCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALBUM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAlbumCount() {
        return albumCount;
    }

    @JsonProperty(JSON_PROPERTY_ALBUM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumCount(@org.eclipse.jdt.annotation.NonNull Integer albumCount) {
        this.albumCount = albumCount;
    }

    public ItemCounts musicVideoCount(@org.eclipse.jdt.annotation.NonNull Integer musicVideoCount) {
        this.musicVideoCount = musicVideoCount;
        return this;
    }

    /**
     * Gets or sets the music video count.
     * 
     * @return musicVideoCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MUSIC_VIDEO_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMusicVideoCount() {
        return musicVideoCount;
    }

    @JsonProperty(JSON_PROPERTY_MUSIC_VIDEO_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMusicVideoCount(@org.eclipse.jdt.annotation.NonNull Integer musicVideoCount) {
        this.musicVideoCount = musicVideoCount;
    }

    public ItemCounts boxSetCount(@org.eclipse.jdt.annotation.NonNull Integer boxSetCount) {
        this.boxSetCount = boxSetCount;
        return this;
    }

    /**
     * Gets or sets the box set count.
     * 
     * @return boxSetCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BOX_SET_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getBoxSetCount() {
        return boxSetCount;
    }

    @JsonProperty(JSON_PROPERTY_BOX_SET_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBoxSetCount(@org.eclipse.jdt.annotation.NonNull Integer boxSetCount) {
        this.boxSetCount = boxSetCount;
    }

    public ItemCounts bookCount(@org.eclipse.jdt.annotation.NonNull Integer bookCount) {
        this.bookCount = bookCount;
        return this;
    }

    /**
     * Gets or sets the book count.
     * 
     * @return bookCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BOOK_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getBookCount() {
        return bookCount;
    }

    @JsonProperty(JSON_PROPERTY_BOOK_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBookCount(@org.eclipse.jdt.annotation.NonNull Integer bookCount) {
        this.bookCount = bookCount;
    }

    public ItemCounts itemCount(@org.eclipse.jdt.annotation.NonNull Integer itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    /**
     * Gets or sets the item count.
     * 
     * @return itemCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getItemCount() {
        return itemCount;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemCount(@org.eclipse.jdt.annotation.NonNull Integer itemCount) {
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
}

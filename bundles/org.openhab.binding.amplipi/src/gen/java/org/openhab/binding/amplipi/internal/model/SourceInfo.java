/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amplipi.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceInfo {

    private String name;

    private String state;

    private String artist;

    private String track;

    private String album;

    private String station;

    private String imgUrl;

    /**
     * Get name
     *
     * @return name
     **/
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SourceInfo name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get state
     *
     * @return state
     **/
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public SourceInfo state(String state) {
        this.state = state;
        return this;
    }

    /**
     * Get artist
     *
     * @return artist
     **/
    @JsonProperty("artist")
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public SourceInfo artist(String artist) {
        this.artist = artist;
        return this;
    }

    /**
     * Get track
     *
     * @return track
     **/
    @JsonProperty("track")
    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public SourceInfo track(String track) {
        this.track = track;
        return this;
    }

    /**
     * Get album
     *
     * @return album
     **/
    @JsonProperty("album")
    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public SourceInfo album(String album) {
        this.album = album;
        return this;
    }

    /**
     * Get station
     *
     * @return station
     **/
    @JsonProperty("station")
    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public SourceInfo station(String station) {
        this.station = station;
        return this;
    }

    /**
     * Get imgUrl
     *
     * @return imgUrl
     **/
    @JsonProperty("img_url")
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public SourceInfo imgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SourceInfo {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    artist: ").append(toIndentedString(artist)).append("\n");
        sb.append("    track: ").append(toIndentedString(track)).append("\n");
        sb.append("    album: ").append(toIndentedString(album)).append("\n");
        sb.append("    station: ").append(toIndentedString(station)).append("\n");
        sb.append("    imgUrl: ").append(toIndentedString(imgUrl)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

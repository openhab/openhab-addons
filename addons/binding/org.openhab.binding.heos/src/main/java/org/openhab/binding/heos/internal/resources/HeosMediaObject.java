/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link HeosMediaObjekt } represents a Media object within
 * the HEOS network. Information about the an media object are stored here
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosMediaObject {

    private final String[] supportedMediaItems = { "type", "station", "song", "album", "artist", "image_url", "qid",
            "mid", "albumId" };

    private Map<String, String> mediaInfo;

    private String type;
    private String station;
    private String song;
    private String album;
    private String artist;
    private String imageUrl;
    private String qid;
    private String mid;
    private String albumId;

    public HeosMediaObject() {
        initObject();
    }

    public void updateMediaInfo(Map<String, String> values) {
        this.mediaInfo = values;

        for (String key : this.mediaInfo.keySet()) {
            switch (key) {
                case "song":
                    this.song = this.mediaInfo.get(key);
                    break;
                case "album":
                    this.album = this.mediaInfo.get(key);
                    break;
                case "artist":
                    this.artist = this.mediaInfo.get(key);
                    break;
                case "image_url":
                    this.imageUrl = this.mediaInfo.get(key);
                    break;
                case "qid":
                    this.qid = this.mediaInfo.get(key);
                    break;
                case "mid":
                    this.mid = this.mediaInfo.get(key);
                    break;
                case "album_id":
                    this.albumId = this.mediaInfo.get(key);
                    break;
                case "type":
                    this.type = this.getMediaInfo().get(key);
                    if (type.equals("station")) {
                        this.station = this.getMediaInfo().get("station");
                    } else {
                        this.station = "No Station";
                    }
                    break;
            }
        }
    }

    private void initObject() {
        mediaInfo = new HashMap<String, String>(9);

        for (String key : supportedMediaItems) {
            mediaInfo.put(key, null);
        }
    }

    public Map<String, String> getMediaInfo() {
        return mediaInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
        mediaInfo.put("song", song);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
        mediaInfo.put("album", album);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
        mediaInfo.put("artis", artist);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        mediaInfo.put("image_url", imageUrl);
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
        mediaInfo.put("qid", qid);
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
        mediaInfo.put("mid", mid);
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
        mediaInfo.put("album_id", albumId);
    }

    public String[] getSupportedMediaItems() {
        return supportedMediaItems;
    }
}

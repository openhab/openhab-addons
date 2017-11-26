/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

import java.util.HashMap;

/**
 * The {@link HeosMediaObjekt } represents a Media object within
 * the HEOS network. Information about the an media object are stored here
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosMediaObject {

    private final String[] supportedMediaItems = { "type", "station", "song", "album", "artist", "image_url", "qid",
            "mid", "album_id" };

    private HashMap<String, String> mediaInfo;

    private String type;
    private String station;
    private String song;
    private String album;
    private String artist;
    private String image_url;
    private String qid;
    private String mid;
    private String album_id;

    public HeosMediaObject() {

        initObject();

    }

    public void updateMediaInfo(HashMap<String, String> values) {

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
                    this.image_url = this.mediaInfo.get(key);
                    break;
                case "qid":
                    this.qid = this.mediaInfo.get(key);
                    break;
                case "mid":
                    this.mid = this.mediaInfo.get(key);
                    break;
                case "album_id":
                    this.album_id = this.mediaInfo.get(key);
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

    public HashMap<String, String> getMediaInfo() {
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
        mediaInfo.put("image_url", image_url);
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

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
        mediaInfo.put("album_id", album_id);
    }

    public String[] getSupportedMediaItems() {
        return supportedMediaItems;
    }

}

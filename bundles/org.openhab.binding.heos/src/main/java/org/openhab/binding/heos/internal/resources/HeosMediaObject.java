/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.resources;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link HeosMediaObjekt } represents a Media object within
 * the HEOS network. Information about the an media object are stored here
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosMediaObject {

    private static final String[] SUPPORTED_MEDIA_ITEM_STRINGS = { TYPE, STATION, SONG, ALBUM, ARTIST, IMAGE_URL, QID, MID,
            ALBUM_ID };

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
                case SONG:
                    this.song = this.mediaInfo.get(key);
                    break;
                case ALBUM:
                    this.album = this.mediaInfo.get(key);
                    break;
                case ARTIST:
                    this.artist = this.mediaInfo.get(key);
                    break;
                case IMAGE_URL:
                    this.imageUrl = this.mediaInfo.get(key);
                    break;
                case QID:
                    this.qid = this.mediaInfo.get(key);
                    break;
                case MID:
                    this.mid = this.mediaInfo.get(key);
                    break;
                case ALBUM_ID:
                    this.albumId = this.mediaInfo.get(key);
                    break;
                case TYPE:
                    this.type = this.getMediaInfo().get(key);
                    if (type.equals(STATION)) {
                        this.station = this.getMediaInfo().get(STATION);
                    } else {
                        this.station = "No Station";
                    }
                    break;
            }
        }
    }

    private void initObject() {
        mediaInfo = new HashMap<String, String>(9);

        for (String key : SUPPORTED_MEDIA_ITEM_STRINGS) {
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
        mediaInfo.put(SONG, song);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
        mediaInfo.put(ALBUM, album);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
        mediaInfo.put(ARTIST, artist);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        mediaInfo.put(IMAGE_URL, imageUrl);
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
        mediaInfo.put(QID, qid);
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
        mediaInfo.put(MID, mid);
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
        mediaInfo.put(ALBUM_ID, albumId);
    }

    public String[] getSupportedMediaItems() {
        return SUPPORTED_MEDIA_ITEM_STRINGS;
    }
}

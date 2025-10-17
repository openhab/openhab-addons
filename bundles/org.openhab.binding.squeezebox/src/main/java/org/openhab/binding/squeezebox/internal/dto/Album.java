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
package org.openhab.binding.squeezebox.internal.dto;

/**
 * Squeezebox Album data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Album extends BaseEntry {
    private String album;
    private String title;
    private String year;
    private String artwork_track_id;

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtworkTrackId() {
        return artwork_track_id;
    }

    @Override
    public String getName() {
        return album;
    }

    @Override
    public String getImagesUrl() {
        return "http://192.168.254.1:9000/music/" + artwork_track_id + "/cover.jpg";
    }

}

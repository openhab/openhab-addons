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
package org.openhab.binding.spotify.internal.api.model;

import java.util.List;

/**
 * Spotify Web Api Track data class : A track entry.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Track extends BaseEntry {
    private Album album;
	private List<Artist> artists;

    public Album getAlbum() {
        return album;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    @Override
    public List<Image> getImages() {
        if (album != null) {
            return album.getImages();
        }

        return null;
    }
}

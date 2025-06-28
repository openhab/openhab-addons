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
 * Spotify Api Album data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class Album extends BaseEntry {

    private String albumType;
    private List<Artist> artists;
    private List<String> availableMarkets;
    private ExternalUrl externalUrls;
    private String href;
    private String type;

    private Tracks tracks;

    public String getAlbumType() {
        return albumType;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public List<String> getAvailableMarkets() {
        return availableMarkets;
    }

    public ExternalUrl getExternalUrls() {
        return externalUrls;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }

    public Tracks getTracks() {
        return tracks;
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.api.model;

import java.util.List;

/**
 * Spotify Web Api Device data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class Item {

    private Album album;
    private List<Artist> artists;
    private List<String> availableMarkets;
    private Integer discNumber;
    private long durationMs;
    private Boolean explicit;
    private ExternalIds externalIds;
    private ExternalUrl externalUrls;
    private String href;
    private String id;
    private String name;
    private Integer popularity;
    private String previewUrl;
    private Integer trackNumber;
    private String type;
    private String uri;

    public Album getAlbum() {
        return album;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public List<String> getAvailableMarkets() {
        return availableMarkets;
    }

    public Integer getDiscNumber() {
        return discNumber;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Boolean getExplicit() {
        return explicit;
    }

    public ExternalIds getExternalIds() {
        return externalIds;
    }

    public ExternalUrl getExternalUrls() {
        return externalUrls;
    }

    public String getHref() {
        return href;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }
}

/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tidal.internal.api.model;

import java.util.List;

import com.google.gson.annotations.JsonAdapter;

/**
 * Tidal Web Api Playlist data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Playlist extends BaseEntry {
    private String name;
    private String description;
    private boolean bounded;
    private String duration;
    private int numberOfItems;
    private Link<Playlist>[] externalLinks;
    public String createdAt;
    public String lastModifiedAt;
    public String accessType;
    public String playlistType;

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getBounded() {
        return bounded;
    }

    public String getDuration() {
        return duration;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public Link<Playlist>[] getExternalLinks() {
        return externalLinks;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLastModifiedAt() {
        return lastModifiedAt;
    }

    public String getAccessType() {
        return accessType;
    }

    public String getPlaylistType() {
        return playlistType;
    }

    public List<Track> getTracks() {
        List<BaseEntry> items = getRelationShip().getItems().getData();
        return items.stream().map(entry -> (Track) entry).toList();
    }

}

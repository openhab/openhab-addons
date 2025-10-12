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
package org.openhab.binding.tidal.internal.api.model;

import java.util.List;

import com.google.gson.annotations.JsonAdapter;

/**
 * Spotify Web Api Artists data class : collection of Artist
 *
 * @author Laurent Arnal - Initial contribution
 */
@JsonAdapter(BaseEntryAdapter.class)
public abstract class BaseEntry {
    private String id;
    private String type;
    private RelationShip relationships;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public abstract String getName();

    public String getArtwork() {
        if (relationships == null) {
            return "";
        }
        Link<Artwork> artworksLink = relationships.getCoverArt();
        if (artworksLink == null) {
            artworksLink = relationships.getProfileArt();
        }

        if (artworksLink == null) {
            return "";
        }
        List<Artwork> artworks = artworksLink.getData();
        if (artworks == null) {
            return "";
        }
        Artwork art = artworks.getFirst();
        if (art == null || art.getFiles() == null) {
            return "";
        }
        return art.getFiles()[0].getHref();
    }

    public RelationShip getRelationShip() {
        return relationships;
    }

}

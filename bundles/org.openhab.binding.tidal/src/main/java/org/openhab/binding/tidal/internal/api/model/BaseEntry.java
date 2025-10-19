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

import org.openhab.core.media.BaseDto;

import com.google.gson.annotations.JsonAdapter;

/**
 * Spotify Web Api Artists data class : collection of Artist
 *
 * @author Laurent Arnal - Initial contribution
 */
@JsonAdapter(BaseEntryAdapter.class)
public abstract class BaseEntry extends BaseDto {
    private RelationShip relationships;

    public String getArtwork() {
        if (relationships == null) {
            return "";
        }
        Link<BaseEntry> artworksLink = relationships.getCoverArt();
        if (artworksLink == null) {
            artworksLink = relationships.getProfileArt();
        }

        if (artworksLink == null) {
            return "";
        }

        List<BaseEntry> artworks = artworksLink.getData();
        if (artworks == null) {
            return "";
        }
        BaseEntry baseEntry = artworks.getFirst();
        if (baseEntry instanceof Artwork) {
            Artwork art = (Artwork) baseEntry;
            if (art == null || art.getFiles() == null) {
                return "";
            }
            return art.getFiles()[0].getHref();
        }
        return "";
    }

    public RelationShip getRelationShip() {
        return relationships;
    }

}

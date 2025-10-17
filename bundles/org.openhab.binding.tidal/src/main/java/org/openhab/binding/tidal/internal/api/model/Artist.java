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
 * Tidal Api Artist data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Artist extends BaseEntry {
    private String name;
    private double popularity;
    private Link<Artist>[] externalLinks;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPopularity() {
        return popularity;
    }

    public Link<Artist>[] getExternalLinks() {
        return externalLinks;
    }

    public List<Album> getAlbums() {
        List<BaseEntry> items = getRelationShip().getAlbums().getData();
        return items.stream().map(entry -> (Album) entry).toList();
    }
}

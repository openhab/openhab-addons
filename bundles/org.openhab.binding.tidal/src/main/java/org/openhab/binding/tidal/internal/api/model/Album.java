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

import com.google.gson.annotations.JsonAdapter;

/**
 * Tidal Api Album data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Album extends BaseEntry {
    private String title;
    private String barcodeId;
    private int numberOfVolumes;
    private int numberOfItems;
    private String duration;
    private boolean explicit;
    private String releaseDate;
    private double popularity;
    private String[] availability;
    private String[] mediaTags;
    private Link[] externalLinks;
    private RelationShip relationships;

    @Override
    public String getName() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public String getBarcodeId() {
        return barcodeId;
    }

    public int getNumberOfVolumes() {
        return numberOfVolumes;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public String getDuration() {
        return duration;
    }

    public boolean getExplicit() {
        return explicit;
    }

    public String getreleaseDate() {
        return releaseDate;
    }

    public double getPopularity() {
        return popularity;
    }

    public String[] getAvailability() {
        return availability;
    }

    public String[] getMediaTags() {
        return mediaTags;
    }

    public Link[] getExternalLinks() {
        return externalLinks;
    }

    public RelationShip getRelationShip() {
        return relationships;
    }

}

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
 * Tidal Api Track data class.
 *
 * @author Laurent Arnal - Initial contribution
 */

@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Track extends BaseEntry {
    private String title;
    private String version;
    private String isrc;
    private String duration;
    private boolean explicit;
    private double popularity;
    public String accessType;
    private String[] availability;
    private String[] mediaTags;
    private Link[] externalLinks;
    private RelationShip relationships;
    private boolean spotlighted;

    @Override
    public String getName() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public String getIsrc() {
        return isrc;
    }

    public String getDuration() {
        return duration;
    }

    public boolean getExplicit() {
        return explicit;
    }

    public double getPopularity() {
        return popularity;
    }

    public String getAccessType() {
        return accessType;
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

    public boolean getSpotlighted() {
        return spotlighted;
    }
}

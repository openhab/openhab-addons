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
 * Spotify Api Episode data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Episode {

    private String audioPreviewUrl;
    private String description;
    private String htmlDescription;
    private long durationMs;
    private boolean explicit;
    private String href;
    private String id;
    private List<Image> images;
    private String name;
    private String type;
    private String uri;

    public String getHref() {
        return href;
    }

    public String getId() {
        return id;
    }

    public List<Image> getImages() {
        return images;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getDescription() {
        return description;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean getExplicit() {
        return explicit;
    }

    public String getAudioPreviewUrl() {
        return audioPreviewUrl;
    }

}

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
 * Spotify Web Api Artists data class : collection of Artist
 *
 * @author Laurent Arnal - Initial contribution
 */
public class BaseEntry {
    private String id;
    private String uri;
    private String name;
    private List<Image> images;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public List<Image> getImages() {
        return images;
    }

    public String getImagesUrl() {
        try {
            if (images != null && images.getFirst() != null) {
                return images.getFirst().getUrl();
            }
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

}

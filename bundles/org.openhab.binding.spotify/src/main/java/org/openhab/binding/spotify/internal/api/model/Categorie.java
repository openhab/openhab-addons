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

/**
 * Spotify Web Api Categorie data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Categorie {

    private String name;
    private String id;
    private String href;
    private Image[] icons;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return href;
    }

    public Image[] getImages() {
        return icons;
    }
}

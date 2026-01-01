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

import org.openhab.core.media.Image;

/**
 * Spotify Web Api Categorie data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Categorie extends BaseEntry {
    private String href;
    private List<Image> icons;

    @Override
    public String getUri() {
        return href;
    }

    @Override
    public List<Image> getImages() {
        return icons;
    }
}

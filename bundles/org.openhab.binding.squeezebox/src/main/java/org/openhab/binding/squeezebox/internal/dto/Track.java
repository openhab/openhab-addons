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
package org.openhab.binding.squeezebox.internal.dto;

/**
 * Squeezebox Track data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Track extends BaseEntry {
    private String title;
    private String coverid;
    private String artist;
    private String url;

    @Override
    public String getName() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getImagesUrl() {
        return "http://192.168.254.1:9000/music/" + coverid + "/cover.jpg";
    }
}

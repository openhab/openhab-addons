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

import org.openhab.core.media.BaseDto;

/**
 * Squeezebox Artist data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Artist extends BaseDto {
    private String artist;
    private String artwork_track_id;

    public String getArtist() {
        return artist;
    }

    @Override
    public String getName() {
        return artist;
    }

    @Override
    public String getArtwork() {
        // return "http://192.168.254.1:9000/music/" + artwork_track_id + "/cover.jpg";
        return "http://192.168.254.1:9000/imageproxy/mai/artist/" + getId() + "/image.png";
    }

}

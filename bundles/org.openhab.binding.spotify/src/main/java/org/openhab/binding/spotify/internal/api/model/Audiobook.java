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
 * Spotify Api Audiobook data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Audiobook extends BaseEntry {

    private String description;
    private String htmlDescription;
    private String edition;
    private boolean explicit;
    private String href;
    private String mediaType;
    private String publisher;
    private String type;
    private int totalChapters;

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public String getEdition() {
        return edition;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getMediaType() {
        return mediaType;
    }

    public boolean getExplicit() {
        return explicit;
    }

    public int getTotalChapters() {
        return totalChapters;
    }

}

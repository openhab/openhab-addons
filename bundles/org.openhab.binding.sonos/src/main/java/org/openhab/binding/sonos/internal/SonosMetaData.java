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
package org.openhab.binding.sonos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SonosMetaData} is a datastructure to the metadata
 * of audio in the Sonos ecosystem
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class SonosMetaData {

    private final String id;
    private final String parentId;
    private final String resource;
    private final String streamContent;
    private final String albumArtUri;
    private final String title;
    private final String upnpClass;
    private final String creator;
    private final String album;
    private final String albumArtist;

    public SonosMetaData(String id, String parentId, String res, String streamContent, String albumArtUri, String title,
            String upnpClass, String creator, String album, String albumArtist) {
        this.id = id;
        this.parentId = parentId;
        this.resource = res;
        this.streamContent = streamContent;
        this.albumArtUri = albumArtUri;
        this.title = title;
        this.upnpClass = upnpClass;
        this.creator = creator;
        this.album = album;
        this.albumArtist = albumArtist;
    }

    @Override
    public String toString() {
        return "SonosMetaData [id=" + id + ", parentID=" + parentId + ", resource=" + resource + " ,streamContent="
                + streamContent + ", arturi=" + albumArtUri + ", title=" + title + ", upnpclass=" + upnpClass
                + ", creator=" + creator + ", album=" + album + ", albumtartist=" + albumArtist + "]";
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public String getCreator() {
        return creator;
    }

    public String getResource() {
        return resource;
    }

    public String getStreamContent() {
        return streamContent;
    }

    public String getTitle() {
        return title;
    }

    public String getUpnpClass() {
        return upnpClass;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }
}

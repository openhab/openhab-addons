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

import java.io.Serializable;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.StringUtils;

/**
 * The {@link SonosEntry} is a datastructure to describe
 * multimedia "entries" in the Sonos ecosystem
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class SonosEntry implements Serializable {

    private static final long serialVersionUID = -4543607156929701588L;

    private final String id;
    private final String title;
    private final String parentId;
    private final String upnpClass;
    private final String res;
    private final String album;
    private final String albumArtUri;
    private final String creator;
    private final int originalTrackNumber;
    private final @Nullable SonosResourceMetaData resourceMetaData;
    private @Nullable String desc;

    public SonosEntry(String id, String title, String parentId, String album, String albumArtUri, String creator,
            String upnpClass, String res) {
        this(id, title, parentId, album, albumArtUri, creator, upnpClass, res, -1);
    }

    public SonosEntry(String id, String title, String parentId, String album, String albumArtUri, String creator,
            String upnpClass, String res, int originalTrackNumber) {
        this(id, title, parentId, album, albumArtUri, creator, upnpClass, res, originalTrackNumber, null);
    }

    public SonosEntry(String id, String title, String parentId, String album, String albumArtUri, String creator,
            String upnpClass, String res, int originalTrackNumber, @Nullable SonosResourceMetaData resourceMetaData) {
        this.id = id;
        this.title = title;
        this.parentId = parentId;
        this.album = album;
        this.albumArtUri = albumArtUri;
        this.creator = creator;
        this.upnpClass = upnpClass;
        this.res = res;
        this.originalTrackNumber = originalTrackNumber;
        this.resourceMetaData = resourceMetaData;
        this.desc = null;
    }

    /**
     * @return the title of the entry.
     */
    @Override
    public String toString() {
        return title;
    }

    /**
     * @return the unique identifier of this entry.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the title of the entry.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the unique identifier of the parent of this entry.
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @return a URI of this entry.
     */
    public String getRes() {
        return res;
    }

    /**
     * @return the UPnP classname for this entry.
     */
    public String getUpnpClass() {
        return upnpClass;
    }

    /**
     * @return the name of the album.
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @return the URI for the album art.
     */
    public String getAlbumArtUri() {
        return Objects.requireNonNull(StringUtils.unEscapeXml(albumArtUri));
    }

    /**
     * @return the name of the artist who created the entry.
     */
    public String getCreator() {
        return creator;
    }

    public int getOriginalTrackNumber() {
        return originalTrackNumber;
    }

    /**
     * The resourceMetaData field from the ResMD parent, this will be login info for
     * streaming accounts to use in favorites
     *
     * @return
     */
    public @Nullable SonosResourceMetaData getResourceMetaData() {
        return resourceMetaData;
    }

    public @Nullable String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

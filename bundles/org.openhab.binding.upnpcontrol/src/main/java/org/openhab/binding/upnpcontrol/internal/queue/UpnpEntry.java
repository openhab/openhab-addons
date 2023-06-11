/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upnpcontrol.internal.util.StringUtils;

/**
 *
 * @author Mark Herwege - Initial contribution
 * @author Karel Goderis - Based on UPnP logic in Sonos binding
 */
@NonNullByDefault
public class UpnpEntry {

    private static final String DIRECTORY_ROOT = "0";

    private static final Pattern CONTAINER_PATTERN = Pattern.compile("object.container");

    private String id;
    private String refId;
    private String parentId;
    private String upnpClass;
    private String title = "";
    private List<UpnpEntryRes> resList = new ArrayList<>();
    private String album = "";
    private String albumArtUri = "";
    private String creator = "";
    private String artist = "";
    private String publisher = "";
    private String genre = "";
    private @Nullable Integer originalTrackNumber;

    private boolean isContainer;

    public UpnpEntry() {
        this("", "", "", "");
    }

    public UpnpEntry(String id, String refId, String parentId, String upnpClass) {
        this.id = id;
        this.refId = refId;
        this.parentId = parentId;
        this.upnpClass = upnpClass;

        Matcher matcher = CONTAINER_PATTERN.matcher(upnpClass);
        isContainer = matcher.find();
    }

    public UpnpEntry withTitle(String title) {
        this.title = title;
        return this;
    }

    public UpnpEntry withAlbum(String album) {
        this.album = album;
        return this;
    }

    public UpnpEntry withAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
        return this;
    }

    public UpnpEntry withCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public UpnpEntry withArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public UpnpEntry withPublisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    public UpnpEntry withGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public UpnpEntry withResList(List<UpnpEntryRes> resList) {
        this.resList = resList;
        return this;
    }

    public UpnpEntry withTrackNumber(@Nullable Integer originalTrackNumber) {
        this.originalTrackNumber = originalTrackNumber;
        return this;
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
     * @return the identifier of the entry this reference intry refers to.
     */
    public String getRefId() {
        return refId;
    }

    /**
     * @return the unique identifier of the parent of this entry.
     */
    public String getParentId() {
        return parentId.isEmpty() ? DIRECTORY_ROOT : parentId;
    }

    /**
     * @return a URI for this entry. Thumbnail resources are not considered.
     */
    public String getRes() {
        return resList.stream().filter(res -> !res.isThumbnailRes()).map(UpnpEntryRes::getRes).findAny().orElse("");
    }

    public List<String> getProtocolList() {
        return resList.stream().map(UpnpEntryRes::getProtocolInfo).collect(Collectors.toList());
    }

    /**
     * @return the UPnP classname for this entry.
     */
    public String getUpnpClass() {
        return upnpClass;
    }

    public boolean isContainer() {
        return isContainer;
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
        return StringUtils.unEscapeXml(albumArtUri);
    }

    /**
     * @return the name of the artist who created the entry.
     */
    public String getCreator() {
        return creator;
    }

    public String getArtist() {
        return artist;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getGenre() {
        return genre;
    }

    public @Nullable Integer getOriginalTrackNumber() {
        return originalTrackNumber;
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
public class UpnpEntry {

    private final String id;
    private final String title;
    private final String parentId;
    private final String upnpClass;
    private final List<UpnpEntryRes> resList;
    private final String album;
    private final String albumArtUri;
    private final String creator;
    private final int originalTrackNumber;
    private String desc;

    public UpnpEntry(String id, String title, String parentId, String album, String albumArtUri, String creator,
            String upnpClass, List<UpnpEntryRes> resList) {
        this(id, title, parentId, album, albumArtUri, creator, upnpClass, resList, -1);
    }

    public UpnpEntry(String id, String title, String parentId, String album, String albumArtUri, String creator,
            String upnpClass, List<UpnpEntryRes> resList, int originalTrackNumber) {
        this.id = id;
        this.title = title;
        this.parentId = parentId;
        this.album = album;
        this.albumArtUri = albumArtUri;
        this.creator = creator;
        this.upnpClass = upnpClass;
        this.resList = resList;
        this.originalTrackNumber = originalTrackNumber;
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
        return resList.get(0).getRes();
    }

    public List<String> getProtocolList() {
        List<String> protocolList = new ArrayList<>();
        for (UpnpEntryRes entryRes : resList) {
            protocolList.add(entryRes.getProtocolInfo());
        }
        return protocolList;
    }

    /**
     * @return the UPnP classname for this entry.
     */
    public String getUpnpClass() {
        return upnpClass;
    }

    public boolean isContainer() {
        Pattern pattern = Pattern.compile("object.container");
        Matcher matcher = pattern.matcher(getUpnpClass());
        return (matcher.find());
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
        return StringEscapeUtils.unescapeXml(albumArtUri);
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}

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
package org.openhab.binding.linkplay.internal.client.upnp;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Mark Herwege - Initial contribution
 * @author Karel Goderis - Based on UPnP logic in Sonos binding
 * @author Dan Cunningham - Added to LinkPlay binding (from UPnP Control binding)
 * 
 */
@NonNullByDefault
public class UpnpXMLParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpnpXMLParser.class);

    // No static metadata format patterns; XML is built dynamically to omit empty tags

    private enum Element {
        TITLE,
        CLASS,
        ALBUM,
        ALBUM_ART_URI,
        CREATOR,
        ARTIST,
        PUBLISHER,
        GENRE,
        TRACK_NUMBER,
        RES
    }

    public static Map<String, @Nullable String> getRenderingControlFromXML(String xml) {
        if (xml.isEmpty()) {
            LOGGER.debug("Could not parse Rendering Control from empty xml");
            return Collections.emptyMap();
        }
        RenderingControlEventHandler handler = new RenderingControlEventHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.debug("Could not parse Rendering Control from XML");
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.debug("Could not parse Rendering Control from XML");
        }
        return handler.getChanges();
    }

    private static class RenderingControlEventHandler extends DefaultHandler {

        private final Map<String, @Nullable String> changes = new HashMap<>();

        RenderingControlEventHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if (qName == null) {
                return;
            }
            switch (qName) {
                case "Volume":
                case "Mute":
                case "Loudness":
                    String channel = attributes == null ? null : attributes.getValue("channel");
                    String val = attributes == null ? null : attributes.getValue("val");
                    if (channel != null && val != null) {
                        changes.put(channel + qName, val);
                    }
                    break;
                default:
                    if ((attributes != null) && (attributes.getValue("val") != null)) {
                        changes.put(qName, attributes.getValue("val"));
                    }
                    break;
            }
        }

        public Map<String, @Nullable String> getChanges() {
            return changes;
        }
    }

    public static Map<String, String> getAVTransportFromXML(String xml) {
        if (xml.isEmpty()) {
            LOGGER.debug("Could not parse AV Transport from empty xml");
            return Collections.emptyMap();
        }
        AVTransportEventHandler handler = new AVTransportEventHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.debug("Could not parse AV Transport from XML", e);
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.debug("Could not parse AV Transport from XML", s);
        }
        return handler.getChanges();
    }

    private static class AVTransportEventHandler extends DefaultHandler {

        private final Map<String, String> changes = new HashMap<>();

        AVTransportEventHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            /*
             * The events are all of the form <qName val="value"/> so we can get all
             * the info we need from here.
             */
            if ((qName != null) && (attributes != null) && (attributes.getValue("val") != null)) {
                changes.put(qName, attributes.getValue("val"));
            }
        }

        public Map<String, String> getChanges() {
            return changes;
        }
    }

    public static List<UpnpEntry> getEntriesFromXML(String xml) {
        if (xml.isEmpty()) {
            LOGGER.debug("Could not parse Entries from empty xml");
            return Collections.emptyList();
        }
        EntryHandler handler = new EntryHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.debug("Could not parse Entries from XML", e);
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.debug("Could not parse Entries from XML", s);
        }
        return handler.getEntries();
    }

    private static class EntryHandler extends DefaultHandler {

        // Maintain a set of elements it is not useful to complain about.
        // This list will be initialized on the first failure case.
        private static List<String> ignore = new ArrayList<>();

        private String id = "";
        private String refId = "";
        private String parentId = "";
        private StringBuilder upnpClass = new StringBuilder();
        private List<UpnpEntryRes> resList = new ArrayList<>();
        private StringBuilder res = new StringBuilder();
        private StringBuilder title = new StringBuilder();
        private StringBuilder album = new StringBuilder();
        private StringBuilder albumArtUri = new StringBuilder();
        private StringBuilder creator = new StringBuilder();
        private StringBuilder artist = new StringBuilder();
        private List<String> artistList = new ArrayList<>();
        private StringBuilder publisher = new StringBuilder();
        private StringBuilder genre = new StringBuilder();
        private StringBuilder trackNumber = new StringBuilder();
        private @Nullable Element element = null;

        private List<UpnpEntry> entries = new ArrayList<>();

        EntryHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if (qName == null) {
                element = null;
                return;
            }
            switch (qName) {
                case "container":
                case "item":
                    if (attributes != null) {
                        if (attributes.getValue("id") != null) {
                            id = attributes.getValue("id");
                        }
                        if (attributes.getValue("refID") != null) {
                            refId = attributes.getValue("refID");
                        }
                        if (attributes.getValue("parentID") != null) {
                            parentId = attributes.getValue("parentID");
                        }
                    }
                    break;
                case "res":
                    if (attributes != null) {
                        String protocolInfo = attributes.getValue("protocolInfo");
                        Long size;
                        try {
                            size = Long.parseLong(attributes.getValue("size"));
                        } catch (NumberFormatException e) {
                            size = null;
                        }
                        String duration = attributes.getValue("duration");
                        String importUri = attributes.getValue("importUri");
                        resList.add(0, new UpnpEntryRes(protocolInfo, size, duration, importUri));
                        element = Element.RES;
                    }
                    break;
                case "dc:title":
                    element = Element.TITLE;
                    break;
                case "upnp:class":
                    element = Element.CLASS;
                    break;
                case "dc:creator":
                    element = Element.CREATOR;
                    break;
                case "upnp:artist":
                    element = Element.ARTIST;
                    break;
                case "dc:publisher":
                    element = Element.PUBLISHER;
                    break;
                case "upnp:genre":
                    element = Element.GENRE;
                    break;
                case "upnp:album":
                    element = Element.ALBUM;
                    break;
                case "upnp:albumArtURI":
                    element = Element.ALBUM_ART_URI;
                    break;
                case "upnp:originalTrackNumber":
                    element = Element.TRACK_NUMBER;
                    break;
                default:
                    if (ignore.isEmpty()) {
                        ignore.add("");
                        ignore.add("DIDL-Lite");
                        ignore.add("type");
                        ignore.add("ordinal");
                        ignore.add("description");
                        ignore.add("writeStatus");
                        ignore.add("storageUsed");
                        ignore.add("supported");
                        ignore.add("pushSource");
                        ignore.add("icon");
                        ignore.add("playlist");
                        ignore.add("date");
                        ignore.add("rating");
                        ignore.add("userrating");
                        ignore.add("episodeSeason");
                        ignore.add("childCountContainer");
                        ignore.add("modificationTime");
                        ignore.add("containerContent");
                    }
                    if (!ignore.contains(localName)) {
                        LOGGER.debug("Did not recognise element named {}", localName);
                    }
                    element = null;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            Element el = element;
            if (el == null || ch == null) {
                return;
            }
            switch (el) {
                case TITLE:
                    title.append(ch, start, length);
                    break;
                case CLASS:
                    upnpClass.append(ch, start, length);
                    break;
                case RES:
                    res.append(ch, start, length);
                    break;
                case ALBUM:
                    album.append(ch, start, length);
                    break;
                case ALBUM_ART_URI:
                    albumArtUri.append(ch, start, length);
                    break;
                case CREATOR:
                    creator.append(ch, start, length);
                    break;
                case ARTIST:
                    artist.append(ch, start, length);
                    break;
                case PUBLISHER:
                    publisher.append(ch, start, length);
                    break;
                case GENRE:
                    genre.append(ch, start, length);
                    break;
                case TRACK_NUMBER:
                    trackNumber.append(ch, start, length);
                    break;
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if ("container".equals(qName) || "item".equals(qName)) {
                element = null;

                Integer trackNumberVal;
                try {
                    trackNumberVal = Integer.parseInt(trackNumber.toString());
                } catch (NumberFormatException e) {
                    trackNumberVal = null;
                }

                entries.add(new UpnpEntry(id, refId, parentId, upnpClass.toString()).withTitle(title.toString())
                        .withAlbum(album.toString()).withAlbumArtUri(albumArtUri.toString())
                        .withCreator(creator.toString())
                        .withArtist(!artistList.isEmpty() ? artistList.get(0) : artist.toString())
                        .withPublisher(publisher.toString()).withGenre(genre.toString()).withTrackNumber(trackNumberVal)
                        .withResList(resList));

                title = new StringBuilder();
                upnpClass = new StringBuilder();
                resList = new ArrayList<>();
                album = new StringBuilder();
                albumArtUri = new StringBuilder();
                creator = new StringBuilder();
                artistList = new ArrayList<>();
                publisher = new StringBuilder();
                genre = new StringBuilder();
                trackNumber = new StringBuilder();
            } else if ("res".equals(qName)) {
                resList.get(0).setRes(res.toString());
                res = new StringBuilder();
            } else if ("upnp:artist".equals(qName)) {
                artistList.add(artist.toString());
                artist = new StringBuilder();
            }
        }

        public List<UpnpEntry> getEntries() {
            return entries;
        }
    }

    public static String compileMetadataString(UpnpEntry entry) {
        String id = entry.getId();
        String parentId = entry.getParentId();
        String tmpTitle = StringUtils.escapeXml(entry.getTitle());
        String title = (tmpTitle == null) ? "" : tmpTitle;
        String upnpClass = entry.getUpnpClass();
        String tmpAlbum = StringUtils.escapeXml(entry.getAlbum());
        String album = (tmpAlbum == null) ? "" : tmpAlbum;
        String albumArtUri = entry.getAlbumArtUri();
        String tmpCreator = StringUtils.escapeXml(entry.getCreator());
        String creator = (tmpCreator == null) ? "" : tmpCreator;
        String tmpArtist = StringUtils.escapeXml(entry.getArtist());
        String artist = (tmpArtist == null) ? "" : tmpArtist;
        String tmpPublisher = StringUtils.escapeXml(entry.getPublisher());
        String publisher = (tmpPublisher == null) ? "" : tmpPublisher;
        String tmpGenre = StringUtils.escapeXml(entry.getGenre());
        String genre = (tmpGenre == null) ? "" : tmpGenre;
        Integer trackNumber = entry.getOriginalTrackNumber();
        String trackNumberStr = (trackNumber == null) ? "" : trackNumber.toString();

        StringBuilder sb = new StringBuilder();
        sb.append("<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ")
                .append("xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" ")
                .append("xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">");
        sb.append("<item id=\"").append(StringUtils.escapeXml(id)).append("\" parentID=\"")
                .append(StringUtils.escapeXml(parentId)).append("\" restricted=\"1\">");
        if (!title.isEmpty()) {
            sb.append("<dc:title>").append(title).append("</dc:title>");
        }
        if (!upnpClass.isEmpty()) {
            sb.append("<upnp:class>").append(upnpClass).append("</upnp:class>");
        }
        if (!album.isEmpty()) {
            sb.append("<upnp:album>").append(album).append("</upnp:album>");
        }
        if (!albumArtUri.isEmpty()) {
            sb.append("<upnp:albumArtURI>").append(StringUtils.escapeXml(albumArtUri)).append("</upnp:albumArtURI>");
        }
        if (!creator.isEmpty()) {
            sb.append("<dc:creator>").append(creator).append("</dc:creator>");
        }
        if (!artist.isEmpty()) {
            sb.append("<upnp:artist>").append(artist).append("</upnp:artist>");
        }
        if (!publisher.isEmpty()) {
            sb.append("<dc:publisher>").append(publisher).append("</dc:publisher>");
        }
        if (!genre.isEmpty()) {
            sb.append("<upnp:genre>").append(genre).append("</upnp:genre>");
        }
        if (!trackNumberStr.isEmpty()) {
            sb.append("<upnp:originalTrackNumber>").append(trackNumberStr).append("</upnp:originalTrackNumber>");
        }

        String res = entry.getRes();
        List<String> protocolList = entry.getProtocolList();
        if (!res.isEmpty() && !protocolList.isEmpty()) {
            String protocolInfo = protocolList.get(0);
            sb.append("<res protocolInfo=\"").append(StringUtils.escapeXml(protocolInfo)).append("\">")
                    .append(StringUtils.escapeXml(res)).append("</res>");
        }

        sb.append("</item></DIDL-Lite>");
        return sb.toString();
    }

    /**
     * Create notification metadata for a URI and include a <res> entry with protocolInfo derived from mediaType.
     *
     * @param uri The media URI
     * @param title Optional title to display (defaults to uri if null/empty)
     * @param album Optional album name
     * @param artist Optional artist/creator
     * @param albumArtUri Optional album art URI
     * @param mediaType Optional media type hint like "mp3", "wav", "ogg", etc. If null, inferred from URI.
     * @return DIDL-Lite metadata string
     */
    public static String createNotificationMetadataForUri(String uri, @Nullable String title, @Nullable String album,
            @Nullable String artist, @Nullable String albumArtUri, @Nullable String mediaType) {
        String effectiveTitle = (title == null || title.isEmpty()) ? uri : title;
        String effectiveAlbum = album == null ? "" : album;
        String effectiveArtist = artist == null ? "" : artist;
        String effectiveAlbumArt = albumArtUri == null ? "" : albumArtUri;

        UpnpEntry entry = new UpnpEntry("Notification", "", "0", "object.item.audioItem.musicTrack")
                .withTitle(effectiveTitle).withAlbum(effectiveAlbum).withArtist(effectiveArtist)
                .withCreator(effectiveArtist).withAlbumArtUri(effectiveAlbumArt).withTrackNumber(null);

        // Build a resource for the URI
        String type = (mediaType == null || mediaType.isEmpty()) ? inferMediaTypeFromUri(uri) : mediaType;
        String protocolInfo = buildProtocolInfo(type);
        UpnpEntryRes res = new UpnpEntryRes(protocolInfo, null, null, null);
        res.setRes(uri);
        List<UpnpEntryRes> resList = new ArrayList<>();
        resList.add(res);
        entry.withResList(resList);

        return compileMetadataString(entry);
    }

    public static String createNotificationMetadataForUri(String uri, @Nullable String title) {
        return createNotificationMetadataForUri(uri, title, null);
    }

    public static String createNotificationMetadataForUri(String uri, @Nullable String title, @Nullable String album,
            @Nullable String artist, @Nullable String albumArtUri) {
        return createNotificationMetadataForUri(uri, title, album, artist, albumArtUri, null);
    }

    public static String createNotificationMetadataForUri(String uri, @Nullable String title,
            @Nullable String mediaType) {
        return createNotificationMetadataForUri(uri, title, null, null, null, mediaType);
    }

    private static String inferMediaTypeFromUri(String uri) {
        String lc = uri.toLowerCase();
        if (lc.contains(".mp3")) {
            return "mp3";
        } else if (lc.contains(".wav") || lc.contains(".wave")) {
            return "wav";
        } else if (lc.contains(".ogg")) {
            return "ogg";
        } else if (lc.contains(".aac")) {
            return "aac";
        } else if (lc.contains(".flac")) {
            return "flac";
        } else if (lc.contains(".m4a") || lc.contains(".mp4")) {
            return "m4a";
        } else if (lc.contains(".opus")) {
            return "opus";
        }
        return "mp3"; // sensible default
    }

    private static String buildProtocolInfo(String mediaType) {
        String type = mediaType.toLowerCase();
        switch (type) {
            case "mp3":
                return "http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;";
            case "wav":
                // Many devices accept audio/wav. DLNA profile LPCM varies by sample rate/bit depth; omit PN.
                return "http-get:*:audio/wav:DLNA.ORG_OP=01;";
            case "ogg":
                return "http-get:*:audio/ogg:DLNA.ORG_OP=01;";
            case "aac":
                return "http-get:*:audio/aac:DLNA.ORG_OP=01;";
            case "flac":
                return "http-get:*:audio/flac:DLNA.ORG_OP=01;";
            case "m4a":
                return "http-get:*:audio/mp4:DLNA.ORG_OP=01;";
            case "opus":
                return "http-get:*:audio/ogg:codecs=opus;DLNA.ORG_OP=01;";
            default:
                return "http-get:*:audio/" + StringUtils.escapeXml(type) + ":DLNA.ORG_OP=01;";
        }
    }

    public static @Nullable PlayQueue getPlayQueueFromXML(String xml) {
        if (xml.isEmpty()) {
            LOGGER.debug("Could not parse PlayQueue from empty xml");
            return null;
        }

        // Extract raw XML for each playlist
        Map<String, String> playlistRawXml = extractPlayListsFromQueue(xml);

        PlayQueueHandler handler = new PlayQueueHandler(playlistRawXml);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.debug("Could not parse PlayQueue from XML", e);
            return null;
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.debug("Could not parse PlayQueue from XML", s);
            return null;
        }
        return handler.getPlayQueue();
    }

    /**
     * Extract raw XML for each playlist from a PlayQueue response
     * 
     * @param xml The full PlayQueue XML
     * @return Map of playlist names (List1, List2, etc.) to their raw XML content in PlayList format
     */
    private static Map<String, String> extractPlayListsFromQueue(String xml) {
        Map<String, String> result = new HashMap<>();
        if (xml.isEmpty()) {
            return result;
        }

        // Find all List elements (List1, List2, etc.)
        int searchFrom = 0;
        while (true) {
            int listStart = xml.indexOf("<List", searchFrom);
            if (listStart == -1) {
                break;
            }

            // Find the end of the opening tag to get the list name
            int openTagEnd = xml.indexOf('>', listStart);
            if (openTagEnd == -1) {
                break;
            }

            // Extract list name (e.g., "List1", "List2")
            String openTag = xml.substring(listStart + 1, openTagEnd);
            String listName = openTag.split("\\s")[0]; // Get just the tag name

            // Find the closing tag
            String closeTag = "</" + listName + ">";
            int listEnd = xml.indexOf(closeTag, openTagEnd);
            if (listEnd == -1) {
                break;
            }

            // Extract the inner content (without the ListN tags)
            String innerContent = xml.substring(openTagEnd + 1, listEnd);

            // Convert ListN structure to PlayList structure
            String playListXml = "<PlayList>" + innerContent + "</PlayList>";
            result.put(listName, playListXml);

            searchFrom = listEnd + closeTag.length();
        }

        return result;
    }

    private static class PlayQueueHandler extends DefaultHandler {

        private PlayQueue playQueue = new PlayQueue();
        private @Nullable PlayList currentPlayList = null;
        private @Nullable PlayListInfo currentListInfo = null;
        private StringBuilder currentValue = new StringBuilder();
        private boolean inCurrentPlayList = false;
        private boolean inPlayListInfo = false;
        private boolean inListInfo = false;
        private Map<String, String> playlistRawXml;
        private @Nullable String currentListName = null;

        PlayQueueHandler(Map<String, String> playlistRawXml) {
            this.playlistRawXml = playlistRawXml;
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            currentValue.setLength(0);
            if (qName == null) {
                return;
            }

            if ("CurrentPlayList".equals(qName)) {
                inCurrentPlayList = true;
            } else if ("PlayListInfo".equals(qName)) {
                inPlayListInfo = true;
            } else if (qName.matches("List\\d+")) {
                currentPlayList = new PlayList();
                currentListName = qName;
            } else if ("ListInfo".equals(qName)) {
                inListInfo = true;
                currentListInfo = new PlayListInfo();
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            if (ch != null) {
                currentValue.append(ch, start, length);
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if (qName == null) {
                return;
            }

            String value = currentValue.toString().trim();
            PlayList currentPlayList = this.currentPlayList;
            // Top-level PlayQueue elements
            if ("TotalQueue".equals(qName)) {
                try {
                    playQueue.setTotalQueue(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    LOGGER.debug("Could not parse TotalQueue value: {}", value);
                }
            } else if ("CurrentPlayList".equals(qName)) {
                inCurrentPlayList = false;
            } else if ("Name".equals(qName) && inCurrentPlayList && !inPlayListInfo) {
                playQueue.setCurrentPlayListName(value);
            } else if ("PlayListInfo".equals(qName)) {
                inPlayListInfo = false;
            } else if (qName.matches("List\\d+")) {
                PlayList playList = currentPlayList;
                if (playList != null) {
                    // Set the raw XML for this playlist
                    String listName = currentListName;
                    if (listName != null) {
                        String rawXml = playlistRawXml.getOrDefault(listName, "");
                        playList.setRawXml(rawXml);
                    }
                    playQueue.addPlayList(playList);
                    currentPlayList = null;
                    currentListName = null;
                }
            } else if ("Name".equals(qName) && currentPlayList != null && !inListInfo) {
                currentPlayList.setName(value);
            } else if ("ListInfo".equals(qName)) {
                inListInfo = false;
                PlayList playList = currentPlayList;
                PlayListInfo listInfo = currentListInfo;
                if (playList != null && listInfo != null) {
                    playList.setListInfo(listInfo);
                    currentListInfo = null;
                }
            } else if (inListInfo) {
                PlayListInfo listInfo = currentListInfo;
                if (listInfo != null) {
                    // ListInfo fields
                    parseListInfoField(qName, value, listInfo);
                }
            }
        }

        private void parseListInfoField(String qName, String value, PlayListInfo listInfo) {
            try {
                switch (qName) {
                    case "Source":
                        listInfo.setSource(value);
                        break;
                    case "SearchUrl":
                        listInfo.setSearchUrl(value);
                        break;
                    case "PicUrl":
                        listInfo.setPicUrl(value);
                        break;
                    case "Login_username":
                        listInfo.setLoginUsername(value);
                        break;
                    case "AutoGenerate":
                        listInfo.setAutoGenerate(Integer.parseInt(value));
                        break;
                    case "StationLimit":
                        listInfo.setStationLimit(Integer.parseInt(value));
                        break;
                    case "MarkSearch":
                        listInfo.setMarkSearch(Integer.parseInt(value));
                        break;
                    case "Quality":
                        listInfo.setQuality(Integer.parseInt(value));
                        break;
                    case "requestQuality":
                        listInfo.setRequestQuality(value);
                        break;
                    case "UpdateTime":
                        listInfo.setUpdateTime(Integer.parseInt(value));
                        break;
                    case "LastPlayIndex":
                        listInfo.setLastPlayIndex(Integer.parseInt(value));
                        break;
                    case "AlarmPlayIndex":
                        listInfo.setAlarmPlayIndex(Integer.parseInt(value));
                        break;
                    case "RealIndex":
                        listInfo.setRealIndex(Integer.parseInt(value));
                        break;
                    case "UserId":
                        listInfo.setUserId(Integer.parseInt(value));
                        break;
                    case "ContentType":
                        listInfo.setContentType(value);
                        break;
                    case "StationBackup":
                        listInfo.setStationBackup(Integer.parseInt(value));
                        break;
                    case "TrackNumber":
                        listInfo.setTrackNumber(Integer.parseInt(value));
                        break;
                    case "SwitchPageMode":
                        listInfo.setSwitchPageMode(Integer.parseInt(value));
                        break;
                    case "PressType":
                        listInfo.setPressType(Integer.parseInt(value));
                        break;
                    case "Volume":
                        listInfo.setVolume(Integer.parseInt(value));
                        break;
                    case "TempQueue":
                        listInfo.setTempQueue(Integer.parseInt(value));
                        break;
                    default:
                        // Ignore unknown fields
                        break;
                }
            } catch (NumberFormatException e) {
                LOGGER.debug("Could not parse integer value for {}: {}", qName, value);
            }
        }

        public PlayQueue getPlayQueue() {
            return playQueue;
        }
    }

    public static String extractPlayListXmlFromBrowseResponse(String xml) {
        if (xml.isEmpty()) {
            return "";
        }
        // Find the PlayList section within QueueContext
        int playListStart = xml.indexOf("<PlayList>");
        int playListEnd = xml.indexOf("</PlayList>");

        if (playListStart != -1 && playListEnd != -1) {
            playListEnd += "</PlayList>".length();
            return xml.substring(playListStart, playListEnd);
        }
        return "";
    }

    /**
     * Create a simple PlayList XML for a single track URL
     * 
     * @param url The URL to play
     * @param listName Optional name for the playlist (can be empty)
     * @return XML string in PlayList format
     */
    public static String createSimplePlayListXml(String url, String listName) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<PlayList>\n");
        xml.append("<ListName>").append(StringUtils.escapeXml(listName.isEmpty() ? "Notification" : listName))
                .append("</ListName>\n");
        xml.append("<ListInfo>\n");
        xml.append("<SourceName>URL</SourceName>\n");
        xml.append("<SearchUrl></SearchUrl>\n");
        xml.append("<Login_username></Login_username>\n");
        xml.append("<MarkSearch>0</MarkSearch>\n");
        xml.append("<TrackNumber>1</TrackNumber>\n");
        xml.append("<Quality>0</Quality>\n");
        xml.append("<requestQuality>High</requestQuality>\n");
        xml.append("<UpdateTime>0</UpdateTime>\n");
        xml.append("<LastPlayIndex>1</LastPlayIndex>\n");
        xml.append("<AlarmPlayIndex>0</AlarmPlayIndex>\n");
        xml.append("<RealIndex>0</RealIndex>\n");
        xml.append("<UserId>0</UserId>\n");
        xml.append("<ContentType>url</ContentType>\n");
        xml.append("<StationBackup>0</StationBackup>\n");
        xml.append("<SwitchPageMode>0</SwitchPageMode>\n");
        xml.append("<CurrentPage>0</CurrentPage>\n");
        xml.append("<TotalPages>0</TotalPages>\n");
        xml.append("<searching>0</searching>\n");
        xml.append("<PressType>0</PressType>\n");
        xml.append("<Volume>0</Volume>\n");
        xml.append("<FadeEnable>0</FadeEnable>\n");
        xml.append("<FadeInMS>0</FadeInMS>\n");
        xml.append("<FadeOutMS>0</FadeOutMS>\n");
        xml.append("<Modified>0</Modified>\n");
        xml.append("<TempQueue>1</TempQueue>\n");
        xml.append("</ListInfo>\n");
        xml.append("<Tracks>\n");
        xml.append("<Track1>\n");
        xml.append("<URL>").append(StringUtils.escapeXml(url)).append("</URL>\n");
        xml.append("<Metadata></Metadata>\n");
        xml.append("<Id>1</Id>\n");
        xml.append("<Source>URL</Source>\n");
        xml.append("<ChapterNumber>0</ChapterNumber>\n");
        xml.append("<Chapters></Chapters>\n");
        xml.append("</Track1>\n");
        xml.append("</Tracks>\n");
        xml.append("</PlayList>");
        return xml.toString();
    }

    public static @Nullable PlayList getPlayListFromBrowseQueueResponse(String xml) {
        if (xml.isEmpty()) {
            LOGGER.debug("Could not parse BrowseQueueResponse from empty xml");
            return null;
        }

        // Extract and store raw PlayList XML
        String playListXml = extractPlayListXmlFromBrowseResponse(xml);

        BrowseQueueResponseHandler handler = new BrowseQueueResponseHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.error("Could not parse BrowseQueueResponse from XML", e);
            return null;
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.debug("Could not parse BrowseQueueResponse from XML", s);
            return null;
        }

        PlayList result = handler.getPlayList();
        if (result != null) {
            result.setRawXml(playListXml);
        }
        return result;
    }

    private static class BrowseQueueResponseHandler extends DefaultHandler {

        private @Nullable PlayList playList = null;
        private @Nullable PlayListInfo listInfo = null;
        private StringBuilder currentValue = new StringBuilder();
        private boolean inPlayList = false;
        private boolean inListInfo = false;
        private boolean inTracks = false;

        BrowseQueueResponseHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            currentValue.setLength(0);
            if (qName == null) {
                return;
            }

            if ("PlayList".equals(qName)) {
                // Handle both wrapped (with QueueContext) and standalone PlayList
                inPlayList = true;
                playList = new PlayList();
            } else if ("ListInfo".equals(qName) && inPlayList) {
                inListInfo = true;
                listInfo = new PlayListInfo();
            } else if ("Tracks".equals(qName)) {
                inTracks = true;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            if (ch != null && !inTracks) {
                currentValue.append(ch, start, length);
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if (qName == null) {
                return;
            }

            String value = currentValue.toString().trim();

            if ("PlayList".equals(qName) && inPlayList) {
                inPlayList = false;
                PlayList pl = playList;
                PlayListInfo info = listInfo;
                if (pl != null && info != null) {
                    pl.setListInfo(info);
                }
            } else if ("ListName".equals(qName) && inPlayList && !inListInfo) {
                PlayList pl = playList;
                if (pl != null) {
                    pl.setName(value);
                }
            } else if ("ListInfo".equals(qName)) {
                inListInfo = false;
            } else if ("Tracks".equals(qName)) {
                inTracks = false;
            } else if (inListInfo && !inTracks) {
                PlayListInfo info = listInfo;
                if (info != null) {
                    parseListInfoField(qName, value, info);
                }
            }
        }

        private void parseListInfoField(String qName, String value, PlayListInfo listInfo) {
            try {
                switch (qName) {
                    case "SourceName":
                        listInfo.setSource(value);
                        break;
                    case "SearchUrl":
                        listInfo.setSearchUrl(value);
                        break;
                    case "PicUrl":
                        listInfo.setPicUrl(value);
                        break;
                    case "Login_username":
                        listInfo.setLoginUsername(value);
                        break;
                    case "AutoGenerate":
                        listInfo.setAutoGenerate(Integer.parseInt(value));
                        break;
                    case "StationLimit":
                        listInfo.setStationLimit(Integer.parseInt(value));
                        break;
                    case "MarkSearch":
                        listInfo.setMarkSearch(Integer.parseInt(value));
                        break;
                    case "Quality":
                        listInfo.setQuality(Integer.parseInt(value));
                        break;
                    case "requestQuality":
                        listInfo.setRequestQuality(value);
                        break;
                    case "UpdateTime":
                        listInfo.setUpdateTime(Integer.parseInt(value));
                        break;
                    case "LastPlayIndex":
                        listInfo.setLastPlayIndex(Integer.parseInt(value));
                        break;
                    case "AlarmPlayIndex":
                        listInfo.setAlarmPlayIndex(Integer.parseInt(value));
                        break;
                    case "RealIndex":
                        listInfo.setRealIndex(Integer.parseInt(value));
                        break;
                    case "UserId":
                        listInfo.setUserId(Integer.parseInt(value));
                        break;
                    case "ContentType":
                        listInfo.setContentType(value);
                        break;
                    case "StationBackup":
                        listInfo.setStationBackup(Integer.parseInt(value));
                        break;
                    case "TrackNumber":
                        listInfo.setTrackNumber(Integer.parseInt(value));
                        break;
                    case "SwitchPageMode":
                        listInfo.setSwitchPageMode(Integer.parseInt(value));
                        break;
                    case "PressType":
                        listInfo.setPressType(Integer.parseInt(value));
                        break;
                    case "Volume":
                        listInfo.setVolume(Integer.parseInt(value));
                        break;
                    case "TempQueue":
                        listInfo.setTempQueue(Integer.parseInt(value));
                        break;
                    case "CurrentPage":
                        listInfo.setCurrentPage(Integer.parseInt(value));
                        break;
                    case "TotalPages":
                        listInfo.setTotalPages(Integer.parseInt(value));
                        break;
                    case "searching":
                        listInfo.setSearching(Integer.parseInt(value));
                        break;
                    case "FadeEnable":
                        listInfo.setFadeEnable(Integer.parseInt(value));
                        break;
                    case "FadeInMS":
                        listInfo.setFadeInMS(Integer.parseInt(value));
                        break;
                    case "FadeOutMS":
                        listInfo.setFadeOutMS(Integer.parseInt(value));
                        break;
                    case "Modified":
                        listInfo.setModified(Integer.parseInt(value));
                        break;
                    default:
                        // Ignore unknown fields
                        break;
                }
            } catch (NumberFormatException e) {
                LOGGER.debug("Could not parse integer value for {}: {}", qName, value);
            }
        }

        public @Nullable PlayList getPlayList() {
            return playList;
        }
    }
}

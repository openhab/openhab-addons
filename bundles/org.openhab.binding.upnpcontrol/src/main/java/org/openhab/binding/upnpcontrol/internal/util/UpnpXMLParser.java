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
package org.openhab.binding.upnpcontrol.internal.util;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
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
import org.openhab.binding.upnpcontrol.internal.queue.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.queue.UpnpEntryRes;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Mark Herwege - Initial contribution
 * @author Karel Goderis - Based on UPnP logic in Sonos binding
 */
@NonNullByDefault
public class UpnpXMLParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpnpXMLParser.class);

    private static final String METADATA_PATTERN = "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
            + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" "
            + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">"
            + "<item id=\"{0}\" parentID=\"{1}\" restricted=\"true\"><dc:title>{2}</dc:title>"
            + "<upnp:class>{3}</upnp:class><upnp:album>{4}</upnp:album>"
            + "<upnp:albumArtURI>{5}</upnp:albumArtURI><dc:creator>{6}</dc:creator>"
            + "<upnp:artist>{7}</upnp:artist><dc:publisher>{8}</dc:publisher>"
            + "<upnp:genre>{9}</upnp:genre><upnp:originalTrackNumber>{10}</upnp:originalTrackNumber>"
            + "</item></DIDL-Lite>";

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
            SAXParser saxParser = factory.newSAXParser();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.error("Could not parse Rendering Control from string '{}'", xml);
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.error("Could not parse Rendering Control from string '{}'", xml);
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
            SAXParser saxParser = factory.newSAXParser();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.error("Could not parse AV Transport from string '{}'", xml, e);
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.debug("Could not parse AV Transport from string '{}'", xml, s);
        }
        return handler.getChanges();
    }

    private static class AVTransportEventHandler extends DefaultHandler {

        private final Map<String, String> changes = new HashMap<String, String>();

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
            SAXParser saxParser = factory.newSAXParser();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.error("Could not parse Entries from string '{}'", xml, e);
        } catch (SAXException | ParserConfigurationException s) {
            LOGGER.debug("Could not parse Entries from string '{}'", xml, s);
        }
        return handler.getEntries();
    }

    private static class EntryHandler extends DefaultHandler {

        // Maintain a set of elements it is not useful to complain about.
        // This list will be initialized on the first failure case.
        private static List<String> ignore = new ArrayList<String>();

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
        String title = StringUtils.escapeXml(entry.getTitle());
        String upnpClass = entry.getUpnpClass();
        String album = StringUtils.escapeXml(entry.getAlbum());
        String albumArtUri = entry.getAlbumArtUri();
        String creator = StringUtils.escapeXml(entry.getCreator());
        String artist = StringUtils.escapeXml(entry.getArtist());
        String publisher = StringUtils.escapeXml(entry.getPublisher());
        String genre = StringUtils.escapeXml(entry.getGenre());
        Integer trackNumber = entry.getOriginalTrackNumber();

        final MessageFormat messageFormat = new MessageFormat(METADATA_PATTERN);
        String metadata = messageFormat.format(new Object[] { id, parentId, title, upnpClass, album, albumArtUri,
                creator, artist, publisher, genre, trackNumber });

        return metadata;
    }
}

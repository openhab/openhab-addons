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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * The {@link SonosXMLParser} is a class of helper functions
 * to parse XML data returned by the Zone Players
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class SonosXMLParser {

    static final Logger LOGGER = LoggerFactory.getLogger(SonosXMLParser.class);

    private static final String METADATA_FORMAT_PATTERN = """
            <DIDL-Lite xmlns:dc="http://purl.org/dc/elements/1.1/" \
            xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/" \
            xmlns:r="urn:schemas-rinconnetworks-com:metadata-1-0/" \
            xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/">\
            <item id="{0}" parentID="{1}" restricted="true">\
            <dc:title>{2}</dc:title>\
            <upnp:class>{3}</upnp:class>\
            <desc id="cdudn" nameSpace="urn:schemas-rinconnetworks-com:metadata-1-0/">{4}</desc>\
            </item>\
            </DIDL-Lite>\
            """;

    private enum Element {
        TITLE,
        CLASS,
        ALBUM,
        ALBUM_ART_URI,
        CREATOR,
        RES,
        TRACK_NUMBER,
        RESMD,
        DESC
    }

    private enum CurrentElement {
        item,
        res,
        streamContent,
        albumArtURI,
        title,
        upnpClass,
        creator,
        album,
        albumArtist,
        desc
    }

    /**
     * @param xml
     * @return a list of alarms from the given xml string.
     */
    public static List<SonosAlarm> getAlarmsFromStringResult(String xml) {
        AlarmHandler handler = new AlarmHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse Alarms from string '{}'", xml);
        }
        return handler.getAlarms();
    }

    /**
     * @param xml
     * @return a list of Entries from the given xml string.
     */
    public static List<SonosEntry> getEntriesFromString(String xml) {
        EntryHandler handler = new EntryHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse Entries from string '{}'", xml);
        }

        return handler.getArtists();
    }

    /**
     * Returns the meta data which is needed to play Pandora
     * (and others?) favorites
     *
     * @param xml
     * @return The value of the desc xml tag
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static @Nullable SonosResourceMetaData getResourceMetaData(String xml)
            throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        SAXParser saxParser = factory.newSAXParser();
        ResourceMetaDataHandler handler = new ResourceMetaDataHandler();
        try {
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException e) {
            LOGGER.warn("Could not parse Resource MetaData from string '{}'", xml);
        }
        return handler.getMetaData();
    }

    /**
     * @param xml
     * @return zone group from the given xml
     */
    public static List<SonosZoneGroup> getZoneGroupFromXML(String xml) {
        ZoneGroupHandler handler = new ZoneGroupHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse ZoneGroup from string '{}'", xml);
        }

        return handler.getGroups();
    }

    public static List<String> getRadioTimeFromXML(String xml) {
        OpmlHandler handler = new OpmlHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse RadioTime from string '{}'", xml);
        }

        return handler.getTextFields();
    }

    public static Map<String, String> getRenderingControlFromXML(String xml) {
        RenderingControlEventHandler handler = new RenderingControlEventHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse Rendering Control from string '{}'", xml);
        }
        return handler.getChanges();
    }

    public static Map<String, String> getAVTransportFromXML(String xml) {
        AVTransportEventHandler handler = new AVTransportEventHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse AV Transport from string '{}'", xml);
        }
        return handler.getChanges();
    }

    public static SonosMetaData getMetaDataFromXML(String xml) {
        MetaDataHandler handler = new MetaDataHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse MetaData from string '{}'", xml);
        }

        return handler.getMetaData();
    }

    public static List<SonosMusicService> getMusicServicesFromXML(String xml) {
        MusicServiceHandler handler = new MusicServiceHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse music services from string '{}'", xml);
        }
        return handler.getServices();
    }

    private static class EntryHandler extends DefaultHandler {

        // Maintain a set of elements about which it is unuseful to complain about.
        // This list will be initialized on the first failure case
        private static @Nullable List<String> ignore;

        private String id = "";
        private String parentId = "";
        private StringBuilder upnpClass = new StringBuilder();
        private StringBuilder res = new StringBuilder();
        private StringBuilder title = new StringBuilder();
        private StringBuilder album = new StringBuilder();
        private StringBuilder albumArtUri = new StringBuilder();
        private StringBuilder creator = new StringBuilder();
        private StringBuilder trackNumber = new StringBuilder();
        private StringBuilder desc = new StringBuilder();
        private @Nullable Element element;

        private List<SonosEntry> artists = new ArrayList<>();

        EntryHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            String name = qName == null ? "" : qName;
            switch (name) {
                case "container":
                case "item":
                    if (attributes != null) {
                        id = attributes.getValue("id");
                        parentId = attributes.getValue("parentID");
                    }
                    break;
                case "res":
                    element = Element.RES;
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
                case "upnp:album":
                    element = Element.ALBUM;
                    break;
                case "upnp:albumArtURI":
                    element = Element.ALBUM_ART_URI;
                    break;
                case "upnp:originalTrackNumber":
                    element = Element.TRACK_NUMBER;
                    break;
                case "r:resMD":
                    element = Element.RESMD;
                    break;
                default:
                    List<String> curIgnore = ignore;
                    if (curIgnore == null) {
                        curIgnore = new ArrayList<>();
                        curIgnore.add("DIDL-Lite");
                        curIgnore.add("r:type");
                        curIgnore.add("r:ordinal");
                        curIgnore.add("r:description");
                        ignore = curIgnore;
                    }

                    if (!curIgnore.contains(qName)) {
                        LOGGER.debug("Did not recognise element named {}", qName);
                    }
                    element = null;
                    break;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            Element elt = element;
            if (elt == null || ch == null) {
                return;
            }
            switch (elt) {
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
                case TRACK_NUMBER:
                    trackNumber.append(ch, start, length);
                    break;
                case RESMD:
                    desc.append(ch, start, length);
                    break;
                case DESC:
                    break;
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if (("container".equals(qName) || "item".equals(qName))) {
                element = null;

                int trackNumberVal = 0;
                try {
                    trackNumberVal = Integer.parseInt(trackNumber.toString());
                } catch (NumberFormatException e) {
                }

                SonosResourceMetaData md = null;

                // The resource description is needed for playing favorites on pandora
                if (!desc.toString().isEmpty()) {
                    try {
                        md = getResourceMetaData(desc.toString());
                    } catch (SAXException | ParserConfigurationException ignore) {
                        LOGGER.debug("Failed to parse embeded", ignore);
                    }
                }

                artists.add(new SonosEntry(id, title.toString(), parentId, album.toString(), albumArtUri.toString(),
                        creator.toString(), upnpClass.toString(), res.toString(), trackNumberVal, md));
                title = new StringBuilder();
                upnpClass = new StringBuilder();
                res = new StringBuilder();
                album = new StringBuilder();
                albumArtUri = new StringBuilder();
                creator = new StringBuilder();
                trackNumber = new StringBuilder();
                desc = new StringBuilder();
            }
        }

        public List<SonosEntry> getArtists() {
            return artists;
        }
    }

    private static class ResourceMetaDataHandler extends DefaultHandler {

        private String id = "";
        private String parentId = "";
        private StringBuilder title = new StringBuilder();
        private StringBuilder upnpClass = new StringBuilder();
        private StringBuilder desc = new StringBuilder();
        private @Nullable Element element;
        private @Nullable SonosResourceMetaData metaData;

        ResourceMetaDataHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            String name = qName == null ? "" : qName;
            switch (name) {
                case "container":
                case "item":
                    if (attributes != null) {
                        id = attributes.getValue("id");
                        parentId = attributes.getValue("parentID");
                    }
                    break;
                case "desc":
                    element = Element.DESC;
                    break;
                case "upnp:class":
                    element = Element.CLASS;
                    break;
                case "dc:title":
                    element = Element.TITLE;
                    break;
                default:
                    element = null;
                    break;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            Element elt = element;
            if (elt == null || ch == null) {
                return;
            }
            switch (elt) {
                case TITLE:
                    title.append(ch, start, length);
                    break;
                case CLASS:
                    upnpClass.append(ch, start, length);
                    break;
                case DESC:
                    desc.append(ch, start, length);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if ("DIDL-Lite".equals(qName)) {
                metaData = new SonosResourceMetaData(id, parentId, title.toString(), upnpClass.toString(),
                        desc.toString());
                element = null;
                desc = new StringBuilder();
                upnpClass = new StringBuilder();
                title = new StringBuilder();
            }
        }

        public @Nullable SonosResourceMetaData getMetaData() {
            return metaData;
        }
    }

    private static class AlarmHandler extends DefaultHandler {

        private @Nullable String id;
        private String startTime = "";
        private String duration = "";
        private String recurrence = "";
        private @Nullable String enabled;
        private String roomUUID = "";
        private String programURI = "";
        private String programMetaData = "";
        private String playMode = "";
        private @Nullable String volume;
        private @Nullable String includeLinkedZones;

        private List<SonosAlarm> alarms = new ArrayList<>();

        AlarmHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if ("Alarm".equals(qName) && attributes != null) {
                id = attributes.getValue("ID");
                duration = attributes.getValue("Duration");
                recurrence = attributes.getValue("Recurrence");
                startTime = attributes.getValue("StartTime");
                enabled = attributes.getValue("Enabled");
                roomUUID = attributes.getValue("RoomUUID");
                programURI = attributes.getValue("ProgramURI");
                programMetaData = attributes.getValue("ProgramMetaData");
                playMode = attributes.getValue("PlayMode");
                volume = attributes.getValue("Volume");
                includeLinkedZones = attributes.getValue("IncludeLinkedZones");
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if ("Alarm".equals(qName)) {
                int finalID = 0;
                int finalVolume = 0;
                boolean finalEnabled = !"0".equals(enabled);
                boolean finalIncludeLinkedZones = !"0".equals(includeLinkedZones);

                try {
                    String id = this.id;
                    if (id == null) {
                        throw new NumberFormatException();
                    }
                    finalID = Integer.parseInt(id);
                    String volume = this.volume;
                    if (volume == null) {
                        throw new NumberFormatException();
                    }
                    finalVolume = Integer.parseInt(volume);
                } catch (NumberFormatException e) {
                    LOGGER.debug("Error parsing Integer");
                }

                alarms.add(new SonosAlarm(finalID, startTime, duration, recurrence, finalEnabled, roomUUID, programURI,
                        programMetaData, playMode, finalVolume, finalIncludeLinkedZones));
            }
        }

        public List<SonosAlarm> getAlarms() {
            return alarms;
        }
    }

    private static class ZoneGroupHandler extends DefaultHandler {

        private final List<SonosZoneGroup> groups = new ArrayList<>();
        private final List<String> currentGroupPlayers = new ArrayList<>();
        private final List<String> currentGroupPlayerZones = new ArrayList<>();
        private String coordinator = "";
        private String groupId = "";

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if ("ZoneGroup".equals(qName) && attributes != null) {
                groupId = attributes.getValue("ID");
                coordinator = attributes.getValue("Coordinator");
            } else if ("ZoneGroupMember".equals(qName) && attributes != null) {
                currentGroupPlayers.add(attributes.getValue("UUID"));
                String zoneName = attributes.getValue("ZoneName");
                if (zoneName != null) {
                    currentGroupPlayerZones.add(zoneName);
                }
                String htInfoSet = attributes.getValue("HTSatChanMapSet");
                if (htInfoSet != null) {
                    currentGroupPlayers.addAll(getAllHomeTheaterMembers(htInfoSet));
                }
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if ("ZoneGroup".equals(qName)) {
                groups.add(new SonosZoneGroup(groupId, coordinator, currentGroupPlayers, currentGroupPlayerZones));
                currentGroupPlayers.clear();
                currentGroupPlayerZones.clear();
            }
        }

        public List<SonosZoneGroup> getGroups() {
            return groups;
        }

        private Set<String> getAllHomeTheaterMembers(String homeTheaterDescription) {
            Set<String> homeTheaterMembers = new HashSet<>();
            Matcher matcher = Pattern.compile("(RINCON_\\w+)").matcher(homeTheaterDescription);
            while (matcher.find()) {
                String member = matcher.group();
                homeTheaterMembers.add(member);
            }
            return homeTheaterMembers;
        }
    }

    private static class OpmlHandler extends DefaultHandler {

        // <opml version="1">
        // <head>
        // <status>200</status>
        //
        // </head>
        // <body>
        // <outline type="text" text="Q-Music 103.3" guide_id="s2398" key="station"
        // image="http://radiotime-logos.s3.amazonaws.com/s87683q.png" preset_id="s2398"/>
        // <outline type="text" text="Bjorn Verhoeven" guide_id="p257265" seconds_remaining="2230" duration="7200"
        // key="show"/>
        // <outline type="text" text="Top 40-Pop"/>
        // <outline type="text" text="37m remaining"/>
        // <outline type="object" text="NowPlaying">
        // <nowplaying>
        // <logo>http://radiotime-logos.s3.amazonaws.com/s87683.png</logo>
        // <twitter_id />
        // </nowplaying>
        // </outline>
        // </body>
        // </opml>

        private final List<String> textFields = new ArrayList<>();
        private @Nullable String textField;
        private @Nullable String type;
        // private String logo;

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if ("outline".equals(qName)) {
                type = attributes == null ? null : attributes.getValue("type");
                if ("text".equals(type)) {
                    textField = attributes == null ? null : attributes.getValue("text");
                } else {
                    textField = null;
                }
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if ("outline".equals(qName)) {
                String field = textField;
                if (field != null) {
                    textFields.add(field);
                }
            }
        }

        public List<String> getTextFields() {
            return textFields;
        }
    }

    private static class AVTransportEventHandler extends DefaultHandler {

        /*
         * <Event xmlns="urn:schemas-upnp-org:metadata-1-0/AVT/" xmlns:r="urn:schemas-rinconnetworks-com:metadata-1-0/">
         * <InstanceID val="0">
         * <TransportState val="PLAYING"/>
         * <CurrentPlayMode val="NORMAL"/>
         * <CurrentPlayMode val="0"/>
         * <NumberOfTracks val="29"/>
         * <CurrentTrack val="12"/>
         * <CurrentSection val="0"/>
         * <CurrentTrackURI val=
         * "x-file-cifs://192.168.1.1/Storage4/Sonos%20Music/Queens%20Of%20The%20Stone%20Age/Lullabies%20To%20Paralyze/Queens%20Of%20The%20Stone%20Age%20-%20Lullabies%20To%20Paralyze%20-%2012%20-%20Broken%20Box.wma"
         * />
         * <CurrentTrackDuration val="0:03:02"/>
         * <CurrentTrackMetaData val=
         * "&lt;DIDL-Lite xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot; xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot; xmlns:r=&quot;urn:schemas-rinconnetworks-com:metadata-1-0/&quot; xmlns=&quot;urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/&quot;&gt;&lt;item id=&quot;-1&quot; parentID=&quot;-1&quot; restricted=&quot;true&quot;&gt;&lt;res protocolInfo=&quot;x-file-cifs:*:audio/x-ms-wma:*&quot; duration=&quot;0:03:02&quot;&gt;x-file-cifs://192.168.1.1/Storage4/Sonos%20Music/Queens%20Of%20The%20Stone%20Age/Lullabies%20To%20Paralyze/Queens%20Of%20The%20Stone%20Age%20-%20Lullabies%20To%20Paralyze%20-%2012%20-%20Broken%20Box.wma&lt;/res&gt;&lt;r:streamContent&gt;&lt;/r:streamContent&gt;&lt;dc:title&gt;Broken Box&lt;/dc:title&gt;&lt;upnp:class&gt;object.item.audioItem.musicTrack&lt;/upnp:class&gt;&lt;dc:creator&gt;Queens Of The Stone Age&lt;/dc:creator&gt;&lt;upnp:album&gt;Lullabies To Paralyze&lt;/upnp:album&gt;&lt;r:albumArtist&gt;Queens Of The Stone Age&lt;/r:albumArtist&gt;&lt;/item&gt;&lt;/DIDL-Lite&gt;"
         * /><r:NextTrackURI val=
         * "x-file-cifs://192.168.1.1/Storage4/Sonos%20Music/Queens%20Of%20The%20Stone%20Age/Lullabies%20To%20Paralyze/Queens%20Of%20The%20Stone%20Age%20-%20Lullabies%20To%20Paralyze%20-%2013%20-%20&apos;&apos;You%20Got%20A%20Killer%20Scene%20There,%20Man...&apos;&apos;.wma"
         * /><r:NextTrackMetaData val=
         * "&lt;DIDL-Lite xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot; xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot; xmlns:r=&quot;urn:schemas-rinconnetworks-com:metadata-1-0/&quot; xmlns=&quot;urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/&quot;&gt;&lt;item id=&quot;-1&quot; parentID=&quot;-1&quot; restricted=&quot;true&quot;&gt;&lt;res protocolInfo=&quot;x-file-cifs:*:audio/x-ms-wma:*&quot; duration=&quot;0:04:56&quot;&gt;x-file-cifs://192.168.1.1/Storage4/Sonos%20Music/Queens%20Of%20The%20Stone%20Age/Lullabies%20To%20Paralyze/Queens%20Of%20The%20Stone%20Age%20-%20Lullabies%20To%20Paralyze%20-%2013%20-%20&amp;apos;&amp;apos;You%20Got%20A%20Killer%20Scene%20There,%20Man...&amp;apos;&amp;apos;.wma&lt;/res&gt;&lt;dc:title&gt;&amp;apos;&amp;apos;You Got A Killer Scene There, Man...&amp;apos;&amp;apos;&lt;/dc:title&gt;&lt;upnp:class&gt;object.item.audioItem.musicTrack&lt;/upnp:class&gt;&lt;dc:creator&gt;Queens Of The Stone Age&lt;/dc:creator&gt;&lt;upnp:album&gt;Lullabies To Paralyze&lt;/upnp:album&gt;&lt;r:albumArtist&gt;Queens Of The Stone Age&lt;/r:albumArtist&gt;&lt;/item&gt;&lt;/DIDL-Lite&gt;"
         * /><r:EnqueuedTransportURI
         * val="x-rincon-playlist:RINCON_000E582126EE01400#A:ALBUMARTIST/Queens%20Of%20The%20Stone%20Age"/><r:
         * EnqueuedTransportURIMetaData val=
         * "&lt;DIDL-Lite xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot; xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot; xmlns:r=&quot;urn:schemas-rinconnetworks-com:metadata-1-0/&quot; xmlns=&quot;urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/&quot;&gt;&lt;item id=&quot;A:ALBUMARTIST/Queens%20Of%20The%20Stone%20Age&quot; parentID=&quot;A:ALBUMARTIST&quot; restricted=&quot;true&quot;&gt;&lt;dc:title&gt;Queens Of The Stone Age&lt;/dc:title&gt;&lt;upnp:class&gt;object.container&lt;/upnp:class&gt;&lt;desc id=&quot;cdudn&quot; nameSpace=&quot;urn:schemas-rinconnetworks-com:metadata-1-0/&quot;&gt;RINCON_AssociatedZPUDN&lt;/desc&gt;&lt;/item&gt;&lt;/DIDL-Lite&gt;"
         * />
         * <PlaybackStorageMedium val="NETWORK"/>
         * <AVTransportURI val="x-rincon-queue:RINCON_000E5812BC1801400#0"/>
         * <AVTransportURIMetaData val=""/>
         * <CurrentTransportActions val="Play, Stop, Pause, Seek, Next, Previous"/>
         * <TransportStatus val="OK"/>
         * <r:SleepTimerGeneration val="0"/>
         * <r:AlarmRunning val="0"/>
         * <r:SnoozeRunning val="0"/>
         * <r:RestartPending val="0"/>
         * <TransportPlaySpeed val="NOT_IMPLEMENTED"/>
         * <CurrentMediaDuration val="NOT_IMPLEMENTED"/>
         * <RecordStorageMedium val="NOT_IMPLEMENTED"/>
         * <PossiblePlaybackStorageMedia val="NONE, NETWORK"/>
         * <PossibleRecordStorageMedia val="NOT_IMPLEMENTED"/>
         * <RecordMediumWriteStatus val="NOT_IMPLEMENTED"/>
         * <CurrentRecordQualityMode val="NOT_IMPLEMENTED"/>
         * <PossibleRecordQualityModes val="NOT_IMPLEMENTED"/>
         * <NextAVTransportURI val="NOT_IMPLEMENTED"/>
         * <NextAVTransportURIMetaData val="NOT_IMPLEMENTED"/>
         * </InstanceID>
         * </Event>
         */

        private final Map<String, String> changes = new HashMap<>();

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            /*
             * The events are all of the form <qName val="value"/> so we can get all
             * the info we need from here.
             */
            if (qName == null) {
                // this means that qName isn't defined in EventType, which is expected for some elements
                LOGGER.info("{} is not defined in EventType. ", qName);
            } else {
                String val = attributes == null ? null : attributes.getValue("val");
                if (val != null) {
                    String key = qName.contains(":") ? qName.split(":")[1] : qName;
                    changes.put(key, val);
                }
            }
        }

        public Map<String, String> getChanges() {
            return changes;
        }
    }

    private static class MetaDataHandler extends DefaultHandler {

        private @Nullable CurrentElement currentElement;

        private String id = "-1";
        private String parentId = "-1";
        private StringBuilder resource = new StringBuilder();
        private StringBuilder streamContent = new StringBuilder();
        private StringBuilder albumArtUri = new StringBuilder();
        private StringBuilder title = new StringBuilder();
        private StringBuilder upnpClass = new StringBuilder();
        private StringBuilder creator = new StringBuilder();
        private StringBuilder album = new StringBuilder();
        private StringBuilder albumArtist = new StringBuilder();

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            String name = qName == null ? "" : qName;
            switch (name) {
                case "item":
                    currentElement = CurrentElement.item;
                    if (attributes != null) {
                        id = attributes.getValue("id");
                        parentId = attributes.getValue("parentID");
                    }
                    break;
                case "res":
                    currentElement = CurrentElement.res;
                    break;
                case "r:streamContent":
                    currentElement = CurrentElement.streamContent;
                    break;
                case "upnp:albumArtURI":
                    currentElement = CurrentElement.albumArtURI;
                    break;
                case "dc:title":
                    currentElement = CurrentElement.title;
                    break;
                case "upnp:class":
                    currentElement = CurrentElement.upnpClass;
                    break;
                case "dc:creator":
                    currentElement = CurrentElement.creator;
                    break;
                case "upnp:album":
                    currentElement = CurrentElement.album;
                    break;
                case "r:albumArtist":
                    currentElement = CurrentElement.albumArtist;
                    break;
                default:
                    // unknown element
                    currentElement = null;
                    break;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            CurrentElement elt = currentElement;
            if (elt == null || ch == null) {
                return;
            }
            switch (elt) {
                case item:
                    break;
                case res:
                    resource.append(ch, start, length);
                    break;
                case streamContent:
                    streamContent.append(ch, start, length);
                    break;
                case albumArtURI:
                    albumArtUri.append(ch, start, length);
                    break;
                case title:
                    title.append(ch, start, length);
                    break;
                case upnpClass:
                    upnpClass.append(ch, start, length);
                    break;
                case creator:
                    creator.append(ch, start, length);
                    break;
                case album:
                    album.append(ch, start, length);
                    break;
                case albumArtist:
                    albumArtist.append(ch, start, length);
                    break;
                case desc:
                    break;
            }
        }

        public SonosMetaData getMetaData() {
            return new SonosMetaData(id, parentId, resource.toString(), streamContent.toString(),
                    albumArtUri.toString(), title.toString(), upnpClass.toString(), creator.toString(),
                    album.toString(), albumArtist.toString());
        }
    }

    private static class RenderingControlEventHandler extends DefaultHandler {

        private final Map<String, String> changes = new HashMap<>();

        private boolean getPresetName = false;
        private @Nullable String presetName;

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if (qName == null) {
                return;
            }
            String channel;
            String val;
            switch (qName) {
                case "Volume":
                case "Mute":
                case "Loudness":
                    channel = attributes == null ? null : attributes.getValue("channel");
                    val = attributes == null ? null : attributes.getValue("val");
                    if (channel != null && val != null) {
                        changes.put(qName + channel, val);
                    }
                    break;
                case "Bass":
                case "Treble":
                case "OutputFixed":
                case "NightMode":
                case "DialogLevel":
                case "SubEnabled":
                case "SubGain":
                case "SurroundEnabled":
                case "SurroundMode":
                case "SurroundLevel":
                case "HTAudioIn":
                case "MusicSurroundLevel":
                case "HeightChannelLevel":
                    val = attributes == null ? null : attributes.getValue("val");
                    if (val != null) {
                        changes.put(qName, val);
                    }
                    break;
                case "PresetNameList":
                    getPresetName = true;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            if (getPresetName && ch != null) {
                presetName = new String(ch, start, length);
            }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName)
                throws SAXException {
            if (getPresetName) {
                getPresetName = false;
                String preset = presetName;
                if (qName != null && preset != null) {
                    changes.put(qName, preset);
                }
            }
        }

        public Map<String, String> getChanges() {
            return changes;
        }
    }

    private static class MusicServiceHandler extends DefaultHandler {

        private final List<SonosMusicService> services = new ArrayList<>();

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            // All services are of the form <services Id="value" Name="value">...</Service>
            if ("Service".equals(qName) && attributes != null && attributes.getValue("Id") != null
                    && attributes.getValue("Name") != null) {
                services.add(new SonosMusicService(attributes.getValue("Id"), attributes.getValue("Name")));
            }
        }

        public List<SonosMusicService> getServices() {
            return services;
        }
    }

    public static @Nullable String getRoomName(URL descriptorURL) {
        RoomNameHandler roomNameHandler = new RoomNameHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(descriptorURL.openStream()), roomNameHandler);
        } catch (SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse Sonos room name from URL '{}'", descriptorURL);
        } catch (IOException e) {
            LOGGER.debug("Could not fetch descriptor XML from URL '{}': {}", descriptorURL, e.getMessage());
        }
        return roomNameHandler.getRoomName();
    }

    private static class RoomNameHandler extends DefaultHandler {

        private @Nullable String roomName;
        private boolean roomNameTag;

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if ("roomName".equalsIgnoreCase(qName)) {
                roomNameTag = true;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            if (roomNameTag && ch != null) {
                roomName = new String(ch, start, length);
                roomNameTag = false;
            }
        }

        public @Nullable String getRoomName() {
            return roomName;
        }
    }

    public static @Nullable String parseModelDescription(URL descriptorURL) {
        ModelNameHandler modelNameHandler = new ModelNameHandler();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new InputSource(descriptorURL.openStream()), modelNameHandler);
        } catch (SAXException | ParserConfigurationException e) {
            LOGGER.warn("Could not parse Sonos model name from URL '{}'", descriptorURL);
        } catch (IOException e) {
            LOGGER.debug("Could not fetch descriptor XML from URL '{}': {}", descriptorURL, e.getMessage());
        }
        return modelNameHandler.getModelName();
    }

    private static class ModelNameHandler extends DefaultHandler {

        private @Nullable String modelName;
        private boolean modelNameTag;

        @Override
        public void startElement(@Nullable String uri, @Nullable String localName, @Nullable String qName,
                @Nullable Attributes attributes) throws SAXException {
            if ("modelName".equalsIgnoreCase(qName)) {
                modelNameTag = true;
            }
        }

        @Override
        public void characters(char @Nullable [] ch, int start, int length) throws SAXException {
            if (modelNameTag && ch != null) {
                modelName = new String(ch, start, length);
                modelNameTag = false;
            }
        }

        public @Nullable String getModelName() {
            return modelName;
        }
    }

    /**
     * Build a valid thing type ID from the model name provided by UPnP
     *
     * @param sonosModelName Sonos model name provided via UPnP device
     * @return a valid thing type ID that can then be used for ThingType creation
     */
    public static String buildThingTypeIdFromModelName(String sonosModelName) {
        // For Ikea SYMFONISK models, the model name now starts with "SYMFONISK" with recent firmwares
        if (sonosModelName.toUpperCase().contains("SYMFONISK")) {
            return "SYMFONISK";
        }
        String id = sonosModelName;
        // Remove until the first space (in practice, it removes the leading "Sonos " from the model name)
        Matcher matcher = Pattern.compile("\\s(.*)").matcher(id);
        if (matcher.find()) {
            id = matcher.group(1);
            // Remove a potential ending text surrounded with parenthesis
            matcher = Pattern.compile("(.*)\\s\\(.*\\)").matcher(id);
            if (matcher.find()) {
                id = matcher.group(1);
            }
        }
        // Finally remove unexpected characters in a thing type ID
        id = id.replaceAll("[^a-zA-Z0-9_]", "");
        // ZP80 is translated to CONNECT and ZP100 to CONNECTAMP
        switch (id) {
            case "ZP80":
                id = "CONNECT";
                break;
            case "ZP100":
                id = "CONNECTAMP";
                break;
            default:
                break;
        }
        return id;
    }

    public static String compileMetadataString(SonosEntry entry) {
        /**
         * If the entry contains resource meta data we will override this with
         * that data.
         */
        String id = entry.getId();
        String parentId = entry.getParentId();
        String title = entry.getTitle();
        String upnpClass = entry.getUpnpClass();

        /**
         * By default 'RINCON_AssociatedZPUDN' is used for most operations,
         * however when playing a favorite entry that is associated withh a
         * subscription like pandora we need to use the desc string asscoiated
         * with that item.
         */
        String desc = entry.getDesc();
        if (desc == null) {
            desc = "RINCON_AssociatedZPUDN";
        }

        /**
         * If resource meta data exists, use it over the parent data
         */
        SonosResourceMetaData resourceMetaData = entry.getResourceMetaData();
        if (resourceMetaData != null) {
            id = resourceMetaData.getId();
            parentId = resourceMetaData.getParentId();
            title = resourceMetaData.getTitle();
            desc = resourceMetaData.getDesc();
            upnpClass = resourceMetaData.getUpnpClass();
        }

        title = StringUtils.escapeXml(title);

        return new MessageFormat(METADATA_FORMAT_PATTERN).format(new Object[] { id, parentId, title, upnpClass, desc });
    }
}

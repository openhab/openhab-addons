/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
public class UpnpXMLParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpnpXMLParser.class);

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

    public static Map<String, String> getAVTransportFromXML(String xml) {
        AVTransportEventHandler handler = new AVTransportEventHandler();
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new StringReader(xml)));
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            LOGGER.error("Could not parse AV Transport from string '{}'", xml);
        } catch (SAXException s) {
            LOGGER.debug("Could not parse AV Transport from string '{}'", xml);
        }
        return handler.getChanges();
    }

    /**
     * @param xml
     * @return a list of Entries from the given xml string.
     * @throws IOException
     * @throws SAXException
     */
    public static List<UpnpEntry> getEntriesFromString(String xml) {
        EntryHandler handler = new EntryHandler();
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new StringReader(xml)));
        } catch (IOException e) {
            LOGGER.error("Could not parse Entries from string '{}'", xml);
        } catch (SAXException s) {
            LOGGER.debug("Could not parse Entries from string '{}'", xml);
        }

        return handler.getEntries();
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

        private final Map<String, String> changes = new HashMap<String, String>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            /*
             * The events are all of the form <qName val="value"/> so we can get all
             * the info we need from here.
             */
            try {
                if (atts.getValue("val") != null) {
                    changes.put(localName, atts.getValue("val"));
                }
            } catch (IllegalArgumentException e) {
                // this means that localName isn't defined in EventType, which is expected for some elements
                LOGGER.info("{} is not defined in EventType. ", localName);
            }
        }

        public Map<String, String> getChanges() {
            return changes;
        }

    }

    private static class EntryHandler extends DefaultHandler {

        // Maintain a set of elements about which it is unuseful to complain about.
        // This list will be initialized on the first failure case
        private static List<String> ignore = null;

        private String id;
        private String parentId;
        private StringBuilder upnpClass = new StringBuilder();
        private StringBuilder res = new StringBuilder();
        private StringBuilder title = new StringBuilder();
        private StringBuilder album = new StringBuilder();
        private StringBuilder albumArtUri = new StringBuilder();
        private StringBuilder creator = new StringBuilder();
        private StringBuilder trackNumber = new StringBuilder();
        private StringBuilder desc = new StringBuilder();
        private Element element = null;

        private List<UpnpEntry> entries = new ArrayList<>();

        EntryHandler() {
            // shouldn't be used outside of this package.
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            if (qName.equals("container") || qName.equals("item")) {
                id = attributes.getValue("id");
                parentId = attributes.getValue("parentID");
            } else if (qName.equals("res")) {
                element = Element.RES;
            } else if (qName.equals("dc:title")) {
                element = Element.TITLE;
            } else if (qName.equals("upnp:class")) {
                element = Element.CLASS;
            } else if (qName.equals("dc:creator")) {
                element = Element.CREATOR;
            } else if (qName.equals("upnp:album")) {
                element = Element.ALBUM;
            } else if (qName.equals("upnp:albumArtURI")) {
                element = Element.ALBUM_ART_URI;
            } else if (qName.equals("upnp:originalTrackNumber")) {
                element = Element.TRACK_NUMBER;
            } else if (qName.equals("r:resMD")) {
                element = Element.RESMD;
            } else {
                if (ignore == null) {
                    ignore = new ArrayList<String>();
                    ignore.add("DIDL-Lite");
                    ignore.add("type");
                    ignore.add("ordinal");
                    ignore.add("description");
                }

                if (!ignore.contains(localName)) {
                    LOGGER.debug("Did not recognise element named {}", localName);
                }
                element = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (element == null) {
                return;
            }
            switch (element) {
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
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("container") || qName.equals("item")) {
                element = null;

                int trackNumberVal = 0;
                try {
                    trackNumberVal = Integer.parseInt(trackNumber.toString());
                } catch (Exception e) {
                }

                entries.add(new UpnpEntry(id, title.toString(), parentId, album.toString(), albumArtUri.toString(),
                        creator.toString(), upnpClass.toString(), res.toString(), trackNumberVal));
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

        public List<UpnpEntry> getEntries() {
            return entries;
        }
    }
}

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
        if ((xml == null) || (xml.isEmpty())) {
            LOGGER.debug("Could not parse AV Transport from empty xml");
            return new HashMap<String, String>();
        }
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
    public static List<UpnpEntry> getEntriesFromXML(String xml) {
        if ((xml == null) || (xml.isEmpty())) {
            LOGGER.debug("Could not parse Entries from empty xml");
            return new ArrayList<UpnpEntry>();
        }
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
        private String refId;
        private String parentId;
        private StringBuilder upnpClass = new StringBuilder();
        private List<UpnpEntryRes> resList = new ArrayList<>();
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
                refId = attributes.getValue("refID");
                parentId = attributes.getValue("parentID");
            } else if (qName.equals("res")) {
                String protocolInfo = attributes.getValue("protocolInfo");
                Long size = Long.parseLong(attributes.getValue("size"));
                String importUri = attributes.getValue("importUri");
                resList.add(0, new UpnpEntryRes(protocolInfo, size, importUri));
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
                    ignore.add("writeStatus");
                    ignore.add("storageUsed");
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
                    break;
                case DESC:
                    desc.append(ch, start, length);
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("container") || qName.equals("item")) {
                element = null;

                Integer trackNumberVal;
                try {
                    trackNumberVal = Integer.parseInt(trackNumber.toString());
                } catch (NumberFormatException e) {
                    trackNumberVal = null;
                }

                entries.add(new UpnpEntry(id, title.toString(), refId, parentId, album.toString(),
                        albumArtUri.toString(), creator.toString(), upnpClass.toString(), resList, trackNumberVal));
                title = new StringBuilder();
                upnpClass = new StringBuilder();
                resList = new ArrayList<>();
                album = new StringBuilder();
                albumArtUri = new StringBuilder();
                creator = new StringBuilder();
                trackNumber = new StringBuilder();
                desc = new StringBuilder();
            } else if (qName.equals("res")) {
                resList.get(0).setRes(res.toString());
                res = new StringBuilder();
            }
        }

        public List<UpnpEntry> getEntries() {
            return entries;
        }
    }
}

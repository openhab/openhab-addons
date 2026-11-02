/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.icalendar.internal.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Converts the multistatus response of a CalDAV {@code calendar-query} REPORT into a single iCalendar document,
 * so it can be handled like a calendar downloaded by a plain HTTP GET.
 *
 * @author YOUR FULL NAME - Initial contribution
 */
@NonNullByDefault
public class CalDavResponseConverter {
    /** Request body for a read-only query of all events of a calendar collection. */
    public static final String CALENDAR_QUERY_BODY = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<c:calendar-query xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
            + "<d:prop><c:calendar-data/></d:prop>"
            + "<c:filter><c:comp-filter name=\"VCALENDAR\"><c:comp-filter name=\"VEVENT\"/></c:comp-filter></c:filter>"
            + "</c:calendar-query>";

    private static final String CALDAV_NS = "urn:ietf:params:xml:ns:caldav";
    private static final String DAV_NS = "DAV:";
    private static final String CRLF = "\r\n";

    private CalDavResponseConverter() {
    }

    /**
     * Converts a CalDAV multistatus response to one iCalendar document.
     *
     * @param xml the response body of the REPORT request
     * @return the merged iCalendar document (contains all returned events and the needed time zones once)
     * @throws CalendarException if the response is not a valid multistatus response
     */
    public static String toICalendar(InputStream xml) throws CalendarException {
        final Document document;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(xml);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new CalendarException("CalDAV response is not valid XML: " + e.getMessage(), e);
        }

        if (document.getDocumentElement() == null || !DAV_NS.equals(document.getDocumentElement().getNamespaceURI())
                || !"multistatus".equals(document.getDocumentElement().getLocalName())) {
            throw new CalendarException("CalDAV response is not a multistatus response.");
        }

        final StringBuilder result = new StringBuilder();
        result.append("BEGIN:VCALENDAR").append(CRLF);
        result.append("VERSION:2.0").append(CRLF);
        result.append("PRODID:-//openHAB//iCalendar Binding CalDAV//EN").append(CRLF);

        final Set<String> knownTimeZones = new HashSet<>();
        final NodeList responses = document.getElementsByTagNameNS(DAV_NS, "response");
        for (int i = 0; i < responses.getLength(); i++) {
            for (String data : extractCalendarData((Element) responses.item(i))) {
                for (String component : extractComponents(data)) {
                    if (component.startsWith("BEGIN:VTIMEZONE")) {
                        final String tzid = findTimeZoneId(component);
                        if (tzid != null && !knownTimeZones.add(tzid)) {
                            continue;
                        }
                    }
                    result.append(component);
                }
            }
        }
        result.append("END:VCALENDAR").append(CRLF);
        return result.toString();
    }

    /**
     * Collects the calendar data of one multistatus response. Fails if the resource or one of its property results
     * reports an error or if no calendar data is contained, so that an incomplete result never replaces a valid
     * calendar.
     */
    private static List<String> extractCalendarData(Element response) throws CalendarException {
        final Element responseStatus = findChild(response, "status");
        if (responseStatus != null && !isSuccess(responseStatus.getTextContent())) {
            throw new CalendarException(
                    "CalDAV server reported a failure for a resource: " + responseStatus.getTextContent().trim());
        }
        final List<String> result = new ArrayList<>();
        for (Node node = response.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (!(node instanceof Element propstat) || !DAV_NS.equals(propstat.getNamespaceURI())
                    || !"propstat".equals(propstat.getLocalName())) {
                continue;
            }
            final Element status = findChild(propstat, "status");
            if (status == null || !isSuccess(status.getTextContent())) {
                throw new CalendarException("CalDAV server reported a failure for the calendar data: "
                        + (status == null ? "no status" : status.getTextContent().trim()));
            }
            final NodeList dataNodes = propstat.getElementsByTagNameNS(CALDAV_NS, "calendar-data");
            for (int i = 0; i < dataNodes.getLength(); i++) {
                final String data = dataNodes.item(i).getTextContent();
                if (data != null && !data.isBlank()) {
                    result.add(data);
                }
            }
        }
        if (result.isEmpty()) {
            throw new CalendarException("CalDAV response contains a resource without calendar data.");
        }
        return result;
    }

    private static @Nullable Element findChild(Element parent, String localName) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && DAV_NS.equals(element.getNamespaceURI())
                    && localName.equals(element.getLocalName())) {
                return element;
            }
        }
        return null;
    }

    /** Checks a DAV status line (e.g. "HTTP/1.1 200 OK") for a 2xx status code. */
    private static boolean isSuccess(@Nullable String statusLine) {
        if (statusLine == null) {
            return false;
        }
        final String[] parts = statusLine.trim().split("\\s+");
        if (parts.length < 2) {
            return false;
        }
        try {
            final int code = Integer.parseInt(parts[1]);
            return code >= 200 && code < 300;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Splits an iCalendar object into its top-level components (VEVENT, VTIMEZONE, ...), each ending with CRLF.
     * Properties of the VCALENDAR object itself are dropped.
     */
    private static List<String> extractComponents(String data) {
        final List<String> components = new ArrayList<>();
        final List<String> lines = unfold(data);
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (String line : lines) {
            final String upper = line.toUpperCase();
            if (upper.startsWith("BEGIN:")) {
                depth++;
                if (depth == 1) {
                    continue; // the VCALENDAR itself
                }
            } else if (upper.startsWith("END:")) {
                depth--;
                if (depth == 0) {
                    break;
                }
                if (depth == 1) {
                    current.append(line).append(CRLF);
                    components.add(current.toString());
                    current = new StringBuilder();
                    continue;
                }
            }
            if (depth >= 2) {
                current.append(line).append(CRLF);
            }
        }
        return components;
    }

    /** Joins folded lines again (continuation lines start with a space or a tab, which stay part of the line). */
    private static List<String> unfold(String data) {
        final List<String> result = new ArrayList<>();
        for (String raw : data.split("\r\n|\n|\r")) {
            if (raw.isEmpty()) {
                continue;
            }
            if ((raw.charAt(0) == ' ' || raw.charAt(0) == '\t') && !result.isEmpty()) {
                result.set(result.size() - 1, result.get(result.size() - 1) + raw.substring(1));
            } else {
                result.add(raw);
            }
        }
        return result;
    }

    private static @Nullable String findTimeZoneId(String component) {
        for (String line : unfold(component)) {
            if (line.toUpperCase().startsWith("TZID:")) {
                return line.substring(5).trim();
            }
        }
        return null;
    }
}

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
package org.openhab.binding.knx.internal.project;

import java.io.InputStream;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * StAX access shared by the readers of the project files. Elements and attributes are matched by local name
 * in any namespace. No method here puts an attribute value into a message.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class XmlInput {

    private XmlInput() {
    }

    /**
     * Opens a file. Every event that goes through the reader counts the nesting depth, so also the elements
     * that are skipped; over {@link ReadLimits#maxDepth()} the parser is stopped and
     * {@link #failure(XMLStreamException, String, ReadBudget)} reports the limit.
     */
    static XMLStreamReader open(InputStream in, String file, ReadBudget budget) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return new DepthLimitedReader(factory.createXMLStreamReader(in), file, budget);
    }

    private static final class DepthLimitedReader extends StreamReaderDelegate {

        private final String file;
        private final ReadBudget budget;
        private int depth;

        DepthLimitedReader(XMLStreamReader parent, String file, ReadBudget budget) {
            super(parent);
            this.file = file;
            this.budget = budget;
        }

        @Override
        public int next() throws XMLStreamException {
            int event = super.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
                if (depth > budget.limits().maxDepth()) {
                    String message = file + " nests elements deeper than " + budget.limits().maxDepth()
                            + " levels (line " + getLocation().getLineNumber() + ")";
                    budget.exceed(message);
                    throw new XMLStreamException(message);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
            return event;
        }
    }

    /** The next event; a document type declaration is refused, the project files never have one. */
    static int next(XMLStreamReader xml, String file) throws XMLStreamException, KnxProjectException {
        int event = xml.next();
        if (event == XMLStreamConstants.DTD) {
            throw new KnxProjectException(Reason.MALFORMED, file + " has a document type declaration");
        }
        return event;
    }

    /** Skips the rest of the current element, its end tag included. */
    static void skipElement(XMLStreamReader xml, String file) throws XMLStreamException, KnxProjectException {
        int depth = 1;
        while (depth > 0 && xml.hasNext()) {
            int event = next(xml, file);
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    static @Nullable String attribute(XMLStreamReader xml, String localName) {
        return xml.getAttributeValue(null, localName);
    }

    /** The attribute, or {@code ""} when it is missing. */
    static String text(XMLStreamReader xml, String localName) {
        String value = attribute(xml, localName);
        return value == null ? "" : value;
    }

    /** Whether the attribute is present, without reading its value. */
    static boolean has(XMLStreamReader xml, String localName) {
        for (int i = 0; i < xml.getAttributeCount(); i++) {
            if (localName.equals(xml.getAttributeLocalName(i))) {
                return true;
            }
        }
        return false;
    }

    /** A required whole number in {@code [min, max]}. */
    static int number(XMLStreamReader xml, String localName, int min, int max, String file) throws KnxProjectException {
        Integer value = optionalNumber(xml, localName, min, max, file);
        if (value == null) {
            throw invalid(xml, localName, file);
        }
        return value;
    }

    /** An optional whole number in {@code [min, max]}; present but not such a number is malformed. */
    static @Nullable Integer optionalNumber(XMLStreamReader xml, String localName, int min, int max, String file)
            throws KnxProjectException {
        String value = attribute(xml, localName);
        if (value == null) {
            return null;
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number >= min && number <= max) {
                return number;
            }
        } catch (NumberFormatException e) {
            // reported below without the value
        }
        throw invalid(xml, localName, file);
    }

    private static KnxProjectException invalid(XMLStreamReader xml, String localName, String file) {
        return new KnxProjectException(Reason.MALFORMED, file + ": " + xml.getLocalName() + "@" + localName
                + " is missing or not a valid number" + at(xml.getLocation()));
    }

    /**
     * The failure for a parser error: the limit that caused it, if any, else a malformed file. The parser's
     * own message is not passed on, as it can quote the document.
     */
    static KnxProjectException failure(XMLStreamException e, String file, ReadBudget budget)
            throws KnxProjectException {
        budget.throwIfExceeded();
        return new KnxProjectException(Reason.MALFORMED, file + " is not well-formed XML" + at(e.getLocation()));
    }

    private static String at(@Nullable Location location) {
        return location == null ? "" : " (line " + location.getLineNumber() + ")";
    }

    static void close(@Nullable XMLStreamReader xml) {
        if (xml != null) {
            try {
                xml.close();
            } catch (XMLStreamException e) {
                // nothing left to release
            }
        }
    }
}

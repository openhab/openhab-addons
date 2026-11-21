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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Reads {@code project.xml}. Of {@code ProjectInformation} only name, address style and date are taken; the
 * device certificates in it are never read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProjectInfoReader {

    private static final String FILE = "project.xml";
    private static final Pattern NAMESPACE = Pattern.compile("http://knx\\.org/xml/project/(\\d{1,4})");

    private ProjectInfoReader() {
    }

    static KnxProjectInfo read(InputStream in, String partId, ReadBudget budget) throws KnxProjectException {
        String createdBy = null;
        String toolVersion = null;
        Integer schemaVersion = null;
        String name = "";
        KnxGroupAddressStyle style = null;
        String lastModified = null;
        XMLStreamReader xml = null;
        try {
            xml = XmlInput.open(in, FILE, budget);
            Deque<String> open = new ArrayDeque<>();
            while (xml.hasNext()) {
                int event = XmlInput.next(xml, FILE);
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String element = xml.getLocalName();
                    if (open.isEmpty()) {
                        if (!"KNX".equals(element)) {
                            throw new KnxProjectException(Reason.MALFORMED, FILE + " is not a KNX project file");
                        }
                        createdBy = XmlInput.attribute(xml, "CreatedBy");
                        toolVersion = XmlInput.attribute(xml, "ToolVersion");
                        schemaVersion = schemaVersion(xml.getNamespaceURI());
                    } else if (open.size() == 2 && "ProjectInformation".equals(element)
                            && "Project".equals(open.peek())) {
                        name = XmlInput.text(xml, "Name");
                        style = KnxGroupAddressStyle.fromProjectValue(XmlInput.attribute(xml, "GroupAddressStyle"));
                        lastModified = XmlInput.attribute(xml, "LastModified");
                    }
                    open.push(element);
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    open.pop();
                }
            }
        } catch (XMLStreamException e) {
            throw XmlInput.failure(e, FILE, budget);
        } finally {
            XmlInput.close(xml);
        }
        return new KnxProjectInfo(partId, name, style == null ? KnxGroupAddressStyle.THREE_LEVEL : style, createdBy,
                toolVersion, schemaVersion, lastModified);
    }

    private static @Nullable Integer schemaVersion(@Nullable String namespace) {
        if (namespace == null) {
            return null;
        }
        Matcher matcher = NAMESPACE.matcher(namespace);
        return matcher.matches() ? Integer.valueOf(matcher.group(1)) : null;
    }
}

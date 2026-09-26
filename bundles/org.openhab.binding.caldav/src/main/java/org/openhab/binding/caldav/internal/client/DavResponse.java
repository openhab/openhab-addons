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
package org.openhab.binding.caldav.internal.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Structured DAV responses; unsuccessful propstats never become successful data.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
public record DavResponse(List<Resource> resources, String token) {

    public static final int MAX_RESOURCES = 5000;
    public record Resource(String href, int status, String etag, String data) {
    }

    public static DavResponse parse(String xml, URI collection) throws Exception {
        Element root = CalDavXml.parse(xml).getDocumentElement();
        if (!"DAV:".equals(root.getNamespaceURI()) || !"multistatus".equals(root.getLocalName())) {
            throw new IOException("Expected DAV multistatus");
        }
        List<Resource> resources = new ArrayList<>();
        for (Element response : children(root, "DAV:", "response")) {
            if (resources.size() >= MAX_RESOURCES) {
                throw new IOException("Too many calendar resources");
            }
            String href = text(response, "DAV:", "href");
            if (href.isBlank()) {
                throw new IOException("DAV response has no resource identifier");
            }
            URI target = CalDavUris.resolve(collection, href);
            int status = status(text(response, "DAV:", "status"));
            String etag = "", data = "";
            boolean success = false;
            for (Element propstat : children(response, "DAV:", "propstat")) {
                int propertyStatus = status(text(propstat, "DAV:", "status"));
                if (propertyStatus == 200) {
                    success = true;
                    for (Element prop : children(propstat, "DAV:", "prop")) {
                        String value = text(prop, "DAV:", "getetag");
                        if (!value.isEmpty()) {
                            etag = value;
                        }
                        value = text(prop, "urn:ietf:params:xml:ns:caldav", "calendar-data");
                        if (!value.isEmpty()) {
                            data = value;
                        }
                    }
                } else if (propertyStatus != 404) {
                    throw new IOException("DAV property retrieval failed");
                }
            }
            if (status == 0) {
                status = success ? 200 : 500;
            }
            if (target.equals(collection) && status == 200 && etag.isEmpty() && data.isEmpty()) {
                continue;
            }
            resources.add(new Resource(target.toString(), status, etag, data));
        }
        return new DavResponse(List.copyOf(resources), text(root, "DAV:", "sync-token"));
    }

    static int status(String value) throws IOException {
        if (value.isEmpty()) {
            return 0;
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length < 2 || !parts[0].startsWith("HTTP/")) {
            throw new IOException("Invalid DAV status");
        }
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid DAV status", e);
        }
    }

    static String text(Element parent, String namespace, String name) {
        List<Element> values = children(parent, namespace, name);
        return values.isEmpty() ? "" : values.getFirst().getTextContent().trim();
    }

    static List<Element> children(Element parent, String namespace, String name) {
        List<Element> children = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && namespace.equals(element.getNamespaceURI())
                    && name.equals(element.getLocalName())) {
                children.add(element);
            }
        }
        return children;
    }
}

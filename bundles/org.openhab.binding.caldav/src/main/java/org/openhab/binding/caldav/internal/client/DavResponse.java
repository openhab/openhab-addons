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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Structured DAV responses; unsuccessful propstats never become successful data.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Collection synchronization truncation
 * @author Andreas Vilippus - Canonical resource identities and alias consistency
 */
@NonNullByDefault
public record DavResponse(List<Resource> resources, String token, boolean truncated) {

    public static final int MAX_RESOURCES = 5000;
    public record Resource(String href, int status, String etag, String data) {
    }

    public static DavResponse parse(String xml, URI collection) throws Exception {
        Element root = CalDavXml.parse(xml).getDocumentElement();
        if (!"DAV:".equals(root.getNamespaceURI()) || !"multistatus".equals(root.getLocalName())) {
            throw new IOException("Expected DAV multistatus");
        }
        List<Resource> resources = new ArrayList<>();
        Map<String, Resource> identities = new HashMap<>();
        URI canonicalCollection = CalDavUris.canonicalize(collection);
        int members = 0;
        boolean truncated = false;
        for (Element response : children(root, "DAV:", "response")) {
            String href = text(response, "DAV:", "href");
            if (href.isBlank()) {
                throw new IOException("DAV response has no resource identifier");
            }
            URI target;
            try {
                target = CalDavUris.canonicalize(CalDavUris.resolve(collection, href));
                if (!CalDavUris.isRoundTrip(collection, target, href)) {
                    throw new IllegalArgumentException("Ambiguous DAV resource reference");
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid DAV resource reference", e);
            }
            int status = status(text(response, "DAV:", "status"));
            if (target.equals(canonicalCollection) && status == 507) {
                // RFC 6578 section 3.6 identifies truncation by the collection status; DAV:error is optional.
                truncated = true;
                continue;
            }
            String etag = "", data = "";
            boolean success = false;
            for (Element propstat : children(response, "DAV:", "propstat")) {
                int propertyStatus = status(text(propstat, "DAV:", "status"));
                if (propertyStatus == HttpStatus.OK_200) {
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
                } else if (propertyStatus != HttpStatus.NOT_FOUND_404) {
                    throw new IOException("DAV property retrieval failed");
                }
            }
            if (status == 0) {
                status = success ? HttpStatus.OK_200 : HttpStatus.INTERNAL_SERVER_ERROR_500;
            }
            if (target.equals(canonicalCollection) && status == HttpStatus.OK_200 && etag.isEmpty() && data.isEmpty()) {
                continue;
            }
            if (++members > MAX_RESOURCES) {
                throw new IOException("Too many calendar resources");
            }
            Resource resource = new Resource(target.toString(), status, etag, data);
            Resource previous = identities.putIfAbsent(resource.href(), resource);
            if (previous == null) {
                resources.add(resource);
            } else if (!previous.equals(resource)) {
                throw new IOException("Conflicting DAV resource entries");
            }
        }
        return new DavResponse(List.copyOf(resources), text(root, "DAV:", "sync-token"), truncated);
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

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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Element;

/**
 * Parses the DAV and CalDAV properties used during discovery.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
public final class CalendarDiscoveryParser {
    private static final String DAV_NAMESPACE = "DAV:";
    private static final String CALDAV_NAMESPACE = "urn:ietf:params:xml:ns:caldav";

    private CalendarDiscoveryParser() {
    }

    public static URI currentUserPrincipal(String xml, URI baseUri) throws Exception {
        return propertyHref(xml, baseUri, DAV_NAMESPACE, "current-user-principal");
    }

    public static URI calendarHome(String xml, URI baseUri) throws Exception {
        return propertyHref(xml, baseUri, CALDAV_NAMESPACE, "calendar-home-set");
    }

    private static URI propertyHref(String xml, URI baseUri, String namespace, String name) throws Exception {
        for (Element response : responses(xml)) {
            for (Element prop : properties(response)) {
                for (Element property : DavResponse.children(prop, namespace, name)) {
                    String href = DavResponse.text(property, DAV_NAMESPACE, "href");
                    if (!href.isBlank()) {
                        return CalDavUris.resolve(baseUri, href);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Required discovery property is unavailable");
    }

    public static List<CalendarCollection> collections(String xml, URI baseUri) throws Exception {
        List<CalendarCollection> collections = new ArrayList<>();
        for (Element response : responses(xml)) {
            boolean calendar = false;
            String name = "";
            for (Element prop : properties(response)) {
                String displayName = DavResponse.text(prop, DAV_NAMESPACE, "displayname");
                if (!displayName.isBlank()) {
                    name = displayName;
                }
                for (Element type : DavResponse.children(prop, DAV_NAMESPACE, "resourcetype")) {
                    calendar |= !DavResponse.children(type, CALDAV_NAMESPACE, "calendar").isEmpty();
                }
            }
            if (calendar) {
                String href = DavResponse.text(response, DAV_NAMESPACE, "href");
                if (href.isBlank()) {
                    throw new java.io.IOException("Calendar collection has no href");
                }
                collections
                        .add(new CalendarCollection(CalDavUris.resolve(baseUri, href), name.isBlank() ? href : name));
            }
        }
        return List.copyOf(collections);
    }

    private static List<Element> responses(String xml) throws Exception {
        Element root = CalDavXml.parse(xml).getDocumentElement();
        if (!DAV_NAMESPACE.equals(root.getNamespaceURI()) || !"multistatus".equals(root.getLocalName())) {
            throw new java.io.IOException("Expected DAV multistatus");
        }
        List<Element> responses = DavResponse.children(root, DAV_NAMESPACE, "response");
        if (responses.size() > DavResponse.MAX_RESOURCES) {
            throw new java.io.IOException("Too many discovery resources");
        }
        return responses;
    }

    private static List<Element> properties(Element response) throws java.io.IOException {
        int status = DavResponse.status(DavResponse.text(response, DAV_NAMESPACE, "status"));
        if (status != 0 && status != 200) {
            throw new java.io.IOException("Discovery resource retrieval failed");
        }
        List<Element> properties = new ArrayList<>();
        for (Element propstat : DavResponse.children(response, DAV_NAMESPACE, "propstat")) {
            int propertyStatus = DavResponse.status(DavResponse.text(propstat, DAV_NAMESPACE, "status"));
            if (propertyStatus == 200) {
                properties.addAll(DavResponse.children(propstat, DAV_NAMESPACE, "prop"));
            } else if (propertyStatus != 404) {
                throw new java.io.IOException("Discovery property retrieval failed");
            }
        }
        return properties;
    }
}

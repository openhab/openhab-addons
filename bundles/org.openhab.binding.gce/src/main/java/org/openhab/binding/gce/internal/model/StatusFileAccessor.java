/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.gce.internal.model;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.xml.sax.SAXException;

/**
 * This class takes care of providing the IPX status file
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class StatusFileAccessor {
    private static final String URL_TEMPLATE = "http://%s/globalstatus.xml";

    private final DocumentBuilder builder;
    private final String url;

    public StatusFileAccessor(String hostname) {
        this.url = URL_TEMPLATE.formatted(hostname);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException("Error initializing StatusFileAccessor", e);
        }
    }

    public StatusFile read() throws SAXException, IOException {
        return new StatusFile(builder.parse(url));
    }
}

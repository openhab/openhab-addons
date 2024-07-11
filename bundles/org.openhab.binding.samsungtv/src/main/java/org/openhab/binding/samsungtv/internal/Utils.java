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
package org.openhab.binding.samsungtv.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link Utils} is a collection of static utilities
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static DocumentBuilderFactory factory = getDocumentBuilder();

    private static DocumentBuilderFactory getDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            LOGGER.debug("XMLParser Configuration Error: {}", e.getMessage());
        }
        return factory;
    }

    /**
     * Build {@link Document} from {@link String} which contains XML content.
     *
     * @param xml
     *            {@link String} which contains XML content.
     * @return {@link Optional Document} or empty if convert has failed.
     */
    public static Optional<Document> loadXMLFromString(String xml, String host) {
        try {
            return Optional.ofNullable(factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml))));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.debug("{}: Error loading XML: {}", host, e.getMessage());
        }
        return Optional.empty();
    }

    public static boolean isSoundChannel(String name) {
        return (name.contains("Volume") || name.contains("Mute"));
    }

    public static String b64encode(String str) {
        return Base64.getUrlEncoder().encodeToString(str.getBytes());
    }

    public static String truncCmd(Command command) {
        String cmd = command.toString();
        return (cmd.length() <= 80) ? cmd : cmd.substring(0, 80) + "...";
    }

    public static String getModelName(@Nullable RemoteDevice device) {
        return Objects.requireNonNull(Optional.ofNullable(device).map(a -> a.getDetails()).map(a -> a.getModelDetails())
                .map(a -> a.getModelName()).orElse(""));
    }

    public static String getManufacturer(@Nullable RemoteDevice device) {
        return Objects.requireNonNull(Optional.ofNullable(device).map(a -> a.getDetails())
                .map(a -> a.getManufacturerDetails()).map(a -> a.getManufacturer()).orElse(""));
    }

    public static String getFriendlyName(@Nullable RemoteDevice device) {
        return Objects.requireNonNull(
                Optional.ofNullable(device).map(a -> a.getDetails()).map(a -> a.getFriendlyName()).orElse(""));
    }

    public static String getUdn(@Nullable RemoteDevice device) {
        return Objects.requireNonNull(Optional.ofNullable(device).map(a -> a.getIdentity()).map(a -> a.getUdn())
                .map(a -> a.getIdentifierString()).orElse(""));
    }

    public static String getHost(@Nullable RemoteDevice device) {
        return Objects.requireNonNull(Optional.ofNullable(device).map(a -> a.getIdentity())
                .map(a -> a.getDescriptorURL()).map(a -> a.getHost()).orElse(""));
    }

    public static String getType(@Nullable RemoteDevice device) {
        return Objects
                .requireNonNull(Optional.ofNullable(device).map(a -> a.getType()).map(a -> a.getType()).orElse(""));
    }
}

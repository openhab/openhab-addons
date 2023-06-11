/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link SamsungTvUtils} provides some utilities for internal use.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class SamsungTvUtils {

    /**
     * Build {@link String} type {@link HashMap} from variable number of
     * {@link String}s.
     *
     * @param data
     *            Variable number of {@link String} parameters which will be
     *            added to hash map.
     */
    public static HashMap<String, String> buildHashMap(String... data) {
        HashMap<String, String> result = new HashMap<>();

        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Odd number of arguments");
        }
        String key = null;
        Integer step = -1;

        for (String value : data) {
            step++;
            switch (step % 2) {
                case 0:
                    if (value == null) {
                        throw new IllegalArgumentException("Null key value");
                    }
                    key = value;
                    continue;
                case 1:
                    if (key != null) {
                        result.put(key, value);
                    }
                    break;
            }
        }

        return result;
    }

    /**
     * Build {@link Document} from {@link String} which contains XML content.
     *
     * @param xml
     *            {@link String} which contains XML content.
     * @return {@link Document} or null if convert has failed.
     */
    public static @Nullable Document loadXMLFromString(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            // Silently ignore exception and return null.
        }

        return null;
    }
}

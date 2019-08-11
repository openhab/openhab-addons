/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hpprinter.internal.api;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link HPType} is responsible for determining what type of printer the
 * Web Interface supports.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPType {
    public static final String ENDPOINT = "/DevMgmt/ProductUsageDyn.xml";

    public enum PrinterType {
        NOT_SUPPORTED,
        MONOCHROME,
        SINGLECOLOR,
        MULTICOLOR
    }

    public HPType(InputSource source) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(source);

        // Get Ink Levels
        NodeList consumableInk = document.getDocumentElement().getElementsByTagName("pudyn:Consumable");

        printerType = PrinterType.MONOCHROME; //Set Monochrome by default.
        for (int i = 0; i < consumableInk.getLength(); i++) {
            Element currInk = (Element) consumableInk.item(i);

            String inkName = currInk.getElementsByTagName("dd:MarkerColor").item(0).getTextContent();

            String consumeType = currInk.getElementsByTagName("dd:ConsumableTypeEnum").item(0).getTextContent();
            if (consumeType.equalsIgnoreCase("printhead")) {
                continue;
            }

            switch (inkName.toLowerCase()) {
                case "cyan":
                    printerType = PrinterType.MULTICOLOR; //Is multicolor if it has this ink
                    break;

                case "cyanmagentayellow":
                    printerType = PrinterType.SINGLECOLOR; //Is singlecolor if it has this ink
                    break;
            }
        }
    }

    private PrinterType printerType;
    public PrinterType getType() {
        return printerType;
    }
}

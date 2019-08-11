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
 * The {@link HPUsage} is responsible for handling reading of usage data.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPUsage {
    public static final String ENDPOINT = "/DevMgmt/ProductUsageDyn.xml";

    public HPUsage(InputSource source) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(source);

        // Get Ink Levels
        NodeList consumableInk = document.getDocumentElement().getElementsByTagName("pudyn:Consumable");

        for (int i = 0; i < consumableInk.getLength(); i++) {
            Element currInk = (Element) consumableInk.item(i);

            String inkName = currInk.getElementsByTagName("dd:MarkerColor").item(0).getTextContent();

            String consumeType = currInk.getElementsByTagName("dd:ConsumableTypeEnum").item(0).getTextContent();

            if (consumeType.equalsIgnoreCase("printhead")) {
                continue;
            }

            String inkRemaining = currInk.getElementsByTagName("dd:ConsumableRawPercentageLevelRemaining").item(0)
                    .getTextContent();

            switch (inkName.toLowerCase()) {
            case "black":
                inkBlack = Integer.valueOf(inkRemaining);
                break;

            case "yellow":
                inkYellow = Integer.valueOf(inkRemaining);
                break;

            case "magenta":
                inkMagenta = Integer.valueOf(inkRemaining);
                break;

            case "cyan":
                inkCyan = Integer.valueOf(inkRemaining);
                break;

            case "cyanmagentayellow":
                inkColor = Integer.valueOf(inkRemaining);
                break;
            }
        }

        //Get other usage info
        NodeList jamEvents = document.getDocumentElement().getElementsByTagName("dd:JamEvents");
        this.jamEvents = Integer.valueOf(jamEvents.item(0).getTextContent());

        NodeList totalImpressions = document.getDocumentElement()
                .getElementsByTagName("dd:TotalImpressions");
        this.totalImpressions = Integer.valueOf(totalImpressions.item(0).getTextContent());

        NodeList totalColorImpressions = document.getDocumentElement()
                .getElementsByTagName("dd:ColorImpressions");
        this.totalColorImpressions = Integer.valueOf(totalColorImpressions.item(0).getTextContent());

        NodeList totalMonochromeImpressions = document.getDocumentElement()
                .getElementsByTagName("dd:MonochromeImpressions");
        this.totalMonochromeImpressions = Integer.valueOf(totalMonochromeImpressions.item(0).getTextContent());

        NodeList totalSubscriptionImpressions = document.getDocumentElement()
                .getElementsByTagName("pudyn:SubscriptionImpressions");
        this.totalSubscriptionImpressions = Integer.valueOf(totalSubscriptionImpressions.item(0).getTextContent());
    }

    private int totalSubscriptionImpressions;
    public int getTotalSubscriptionImpressions() {
        return totalSubscriptionImpressions;
    }

    private int totalMonochromeImpressions;
    public int getTotalMonochromeImpressions() {
        return totalMonochromeImpressions;
    }

    private int totalColorImpressions;
    public int getTotalColorImpressions() {
        return totalColorImpressions;
    }

    private int totalImpressions;
    public int getTotalImpressions() {
        return totalImpressions;
    }

    private int jamEvents;
    public int getJamEvents() {
        return jamEvents;
    }

    private int inkBlack;
    public int getInkBlack() {
        return inkBlack;
    }

    private int inkCyan;
    public int getInkCyan() {
        return inkCyan;
    }

    private int inkMagenta;
    public int getInkMagenta() {
        return inkMagenta;
    }

    private int inkYellow;
    public int getInkYellow() {
        return inkYellow;
    }

    private int inkColor;
    public int getInkColor() {
        return inkColor;
    }

}

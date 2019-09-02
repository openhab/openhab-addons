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

    private int totalSubscriptionImpressions;
    private int mispickEvents;
    private int totalMonochromeImpressions;
    private int totalColorImpressions;
    private int frontpanelCancelCount;
    private int totalImpressions;
    private int jamEvents;
    private int inkBlack;
    private int inkCyan;
    private int inkMagenta;
    private int inkYellow;
    private int inkColor;
    private float inkBlackMarking;
    private float inkCyanMarking;
    private float inkMagentaMarking;
    private float inkYellowMarking;
    private float inkColorMarking;

    public HPUsage() {
    }

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

            int markAgentLength = currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed").getLength();
            
            float totalMarking = 0;
            if (markAgentLength > 0) { // Check to make sure Cumulative Marking Agent exists
                for (int ai = 0; ai < markAgentLength; ai++) {
                    Element currMarking = (Element) currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed")
                            .item(ai);

                    float marking = Integer
                            .valueOf(currMarking.getElementsByTagName("dd:ValueFloat").item(0).getTextContent());

                    switch (currMarking.getElementsByTagName("dd:Unit").item(0).getTextContent().toLowerCase()) {
                    case "microliters":
                        marking = marking / 1000; // Convert to litres
                        break;
                    case "liters":
                        marking = marking * 1000; // Convert to millilitres
                    }

                    totalMarking = totalMarking + marking; // Sum the marking counts together
                }
            }

            switch (inkName.toLowerCase()) {
            case "black":
                inkBlack = Integer.valueOf(inkRemaining);
                inkBlackMarking = totalMarking;
                break;

            case "yellow":
                inkYellow = Integer.valueOf(inkRemaining);
                inkYellowMarking = totalMarking;
                break;

            case "magenta":
                inkMagenta = Integer.valueOf(inkRemaining);
                inkMagentaMarking = totalMarking;
                break;

            case "cyan":
                inkCyan = Integer.valueOf(inkRemaining);
                inkCyanMarking = totalMarking;
                break;

            case "cyanmagentayellow":
                inkColor = Integer.valueOf(inkRemaining);
                inkColorMarking = totalMarking;
                break;
            }
        }

        // Get other usage info
        NodeList jamEvents = document.getDocumentElement().getElementsByTagName("dd:JamEvents");
        if (jamEvents.getLength() > 0)
            this.jamEvents = Integer.valueOf(jamEvents.item(0).getTextContent());

        NodeList totalImpressions = document.getDocumentElement().getElementsByTagName("dd:TotalImpressions");
        if (totalImpressions.getLength() > 0)
            this.totalImpressions = Integer.valueOf(totalImpressions.item(0).getTextContent());

        NodeList totalColorImpressions = document.getDocumentElement().getElementsByTagName("dd:ColorImpressions");
        if (totalColorImpressions.getLength() > 0)
            this.totalColorImpressions = Integer.valueOf(totalColorImpressions.item(0).getTextContent());

        NodeList totalMonochromeImpressions = document.getDocumentElement()
                .getElementsByTagName("dd:MonochromeImpressions");
        if (totalMonochromeImpressions.getLength() > 0)
            this.totalMonochromeImpressions = Integer.valueOf(totalMonochromeImpressions.item(0).getTextContent());

        NodeList totalSubscriptionImpressions = document.getDocumentElement()
                .getElementsByTagName("pudyn:SubscriptionImpressions");
        if (totalSubscriptionImpressions.getLength() > 0)
            this.totalSubscriptionImpressions = Integer.valueOf(totalSubscriptionImpressions.item(0).getTextContent());

        NodeList mispickEvents = document.getDocumentElement().getElementsByTagName("dd:MispickEvents");
        if (mispickEvents.getLength() > 0)
            this.mispickEvents = Integer.valueOf(mispickEvents.item(0).getTextContent());

        NodeList frontpanelCancelCount = document.getDocumentElement()
                .getElementsByTagName("dd:TotalFrontPanelCancelPresses");
        if (frontpanelCancelCount.getLength() > 0)
            this.frontpanelCancelCount = Integer.valueOf(frontpanelCancelCount.item(0).getTextContent());
    }

    public int getFrontPanelCancelCount() {
        return frontpanelCancelCount;
    }

    public int getTotalSubscriptionImpressions() {
        return totalSubscriptionImpressions;
    }

    public int getMispickEvents() {
        return mispickEvents;
    }

    public int getTotalMonochromeImpressions() {
        return totalMonochromeImpressions;
    }

    public int getTotalColorImpressions() {
        return totalColorImpressions;
    }

    public int getTotalImpressions() {
        return totalImpressions;
    }

    public int getJamEvents() {
        return jamEvents;
    }

    public int getInkBlack() {
        return inkBlack;
    }

    public float getInkBlackMarking() {
        return inkBlackMarking;
    }

    public int getInkCyan() {
        return inkCyan;
    }

    public float getInkCyanMarking() {
        return inkCyanMarking;
    }

    public int getInkMagenta() {
        return inkMagenta;
    }

    public float getInkMagentaMarking() {
        return inkMagentaMarking;
    }

    public int getInkYellow() {
        return inkYellow;
    }

    public float getInkYellowMarking() {
        return inkYellowMarking;
    }

    public int getInkColor() {
        return inkColor;
    }

    public float getInkColorMarking() {
        return inkColorMarking;
    }

}

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
 * Web Interface supports including any features.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPType {
    public static final String ENDPOINT = "/DevMgmt/ProductUsageDyn.xml";

    private PrinterType printerType = PrinterType.UNKNOWN;
    private boolean jamEvents = false;
    private boolean mispickEvents = false;
    private boolean subscriptionImpressions = false;
    private boolean frontPanelCancel = false;
    private boolean cumuMarking = false;

    public enum PrinterType {
        UNKNOWN, MONOCHROME, SINGLECOLOR, MULTICOLOR
    }

    public HPType() { 
    }

    public HPType(InputSource source) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(source);

        // Check what Ink/Toner colours are present
        NodeList consumableInk = document.getDocumentElement().getElementsByTagName("pudyn:Consumable");

        for (int i = 0; i < consumableInk.getLength(); i++) {
            Element currInk = (Element) consumableInk.item(i);

            String inkName = currInk.getElementsByTagName("dd:MarkerColor").item(0).getTextContent();

            String consumeType = currInk.getElementsByTagName("dd:ConsumableTypeEnum").item(0).getTextContent();
            if (consumeType.equalsIgnoreCase("printhead")) {
                continue;
            }

            if (currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed").getLength() > 0) 
            cumuMarking = true;

            switch (inkName.toLowerCase()) {
            case "cyan":
            case "magenta":
            case "yellow":
                printerType = PrinterType.MULTICOLOR; // Is multicolor if it has this ink
                break;

            case "cyanmagentayellow":
                printerType = PrinterType.SINGLECOLOR; // Is singlecolor if it has this ink
                break;

            case "black":
                if (printerType == PrinterType.UNKNOWN)
                    printerType = PrinterType.MONOCHROME; // Is Monochrome
                break;
            }
        }

        NodeList subUnit = document.getDocumentElement().getElementsByTagName("pudyn:PrinterSubunit");
        Element currSubUnit = (Element) subUnit.item(0);

        if (currSubUnit.getElementsByTagName("dd:JamEvents").getLength() > 0)
            jamEvents = true;

        if (currSubUnit.getElementsByTagName("dd:MispickEvents").getLength() > 0)
            mispickEvents = true;

        if (currSubUnit.getElementsByTagName("pudyn:SubscriptionImpressions").getLength() > 0)
            subscriptionImpressions = true;

        if (currSubUnit.getElementsByTagName("dd:TotalFrontPanelCancelPresses").getLength() > 0)
            frontPanelCancel = true;
    }

    public PrinterType getType() {
        return printerType;
    }

    /**
     * Printer data contains Cumulative Marking Agent Used.
     * 
     * pudyn:ProductUsageDyn -> pudyn:ConsumableSubunit -> pudyn:Consumable -> dd2:CumulativeMarkingAgentUsed
     * 
     * @return {boolean} True if supported.
     */
    public boolean hasCumulativeMarking() {
        return cumuMarking;
    }

    /**
     * Printer data contains Jam Events.
     * 
     * pudyn:ProductUsageDyn -> pudyn:PrinterSubunit -> dd:JamEvents
     * 
     * @return {boolean} True if supported.
     */
    public boolean hasJamEvents() {
        return jamEvents;
    }

    /**
     * Printer data contains Mispick Events.
     * 
     * pudyn:ProductUsageDyn -> pudyn:PrinterSubunit -> dd:MispickEvents
     * 
     * @return {boolean} True if supported.
     */
    public boolean hasMispickEvents() {
        return mispickEvents;
    }

    /**
     * Printer data contains Subscription Impressions count.
     * 
     * pudyn:ProductUsageDyn -> pudyn:PrinterSubunit ->
     * pudyn:SubscriptionImpressions
     * 
     * @return {boolean} True if supported.
     */
    public boolean hasSubscriptionCount() {
        return subscriptionImpressions;
    }

    /**
     * Printer data contains Front panel cancel presses count.
     * 
     * pudyn:ProductUsageDyn -> pudyn:PrinterSubunit ->
     * dd:TotalFrontPanelCancelPresses
     * 
     * @return {boolean} True if supported.
     */
    public boolean hasTotalFrontPanelCancelPresses() {
        return frontPanelCancel;
    }
}

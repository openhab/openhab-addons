/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
    private boolean jamEvents;
    private boolean mispickEvents;
    private boolean subscriptionImpressions;
    private boolean frontPanelCancel;
    private boolean cumuMarking;
    private boolean scanAdf;
    private boolean scanFlatbed;
    private boolean scanToHost;
    private boolean scanToEmail;
    private boolean scanToFolder;

    private boolean printApplication;
    private boolean printApplicationChrome;

    public enum PrinterType {
        UNKNOWN,
        MONOCHROME,
        SINGLECOLOR,
        MULTICOLOR
    }

    public HPType(Document document) {
        // Check what Ink/Toner colours are present
        NodeList consumableInk = document.getDocumentElement().getElementsByTagName("pudyn:Consumable");

        for (int i = 0; i < consumableInk.getLength(); i++) {
            Element currInk = (Element) consumableInk.item(i);

            String inkName = currInk.getElementsByTagName("dd:MarkerColor").item(0).getTextContent();

            String consumeType = currInk.getElementsByTagName("dd:ConsumableTypeEnum").item(0).getTextContent();
            if (consumeType.equalsIgnoreCase("printhead")) {
                continue;
            }

            if (currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed").getLength() > 0) {
                cumuMarking = true;
            }

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
                    if (printerType == PrinterType.UNKNOWN) {
                        printerType = PrinterType.MONOCHROME; // Is Monochrome
                    }
                    break;
            }
        }

        NodeList printerSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:PrinterSubunit");
        Element currPrinterSubUnit = (Element) printerSubUnit.item(0);

        if (currPrinterSubUnit.getElementsByTagName("dd:JamEvents").getLength() > 0) {
            jamEvents = true;
        }

        if (currPrinterSubUnit.getElementsByTagName("dd:MispickEvents").getLength() > 0) {
            mispickEvents = true;
        }

        if (currPrinterSubUnit.getElementsByTagName("pudyn:SubscriptionImpressions").getLength() > 0) {
            subscriptionImpressions = true;
        }

        if (currPrinterSubUnit.getElementsByTagName("dd:TotalFrontPanelCancelPresses").getLength() > 0) {
            frontPanelCancel = true;
        }

        NodeList scannerSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:ScanApplicationSubunit");
        if (scannerSubUnit.getLength() > 0) {
            Element currScannerSubUnit = (Element) scannerSubUnit.item(0);

            if (currScannerSubUnit.getElementsByTagName("dd:AdfImages").getLength() > 0) {
                scanAdf = true;
            }

            if (currScannerSubUnit.getElementsByTagName("dd:FlatbedImages").getLength() > 0) {
                scanFlatbed = true;
            }

            if (currScannerSubUnit.getElementsByTagName("dd:ImagesSentToEmail").getLength() > 0) {
                scanToEmail = true;
            }

            if (currScannerSubUnit.getElementsByTagName("dd:ImagesSentToFolder").getLength() > 0) {
                scanToFolder = true;
            }

            if (currScannerSubUnit.getElementsByTagName("dd:ScanToHostImages").getLength() > 0) {
                scanToHost = true;
            }
        }

        NodeList appSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:ScanApplicationSubunit");
        if (appSubUnit.getLength() > 0) {
            printApplication = true;

            Element currAppSubUnit = (Element) appSubUnit.item(0);
            
            if (currAppSubUnit.getElementsByTagName("pudyn:RemoteDeviceType").getLength() > 0) {
                printApplicationChrome = true;
            }
            
        }
    }

    public PrinterType getType() {
        return printerType;
    }

    /**
     * Printer data contains Scan to Email.
     *
     * pudyn:ProductUsageDyn -> pudyn:ScanApplicationSubunit -> dd:ImagesSentToEmail
     *
     * @return {boolean} True if supported.
     */
    public boolean hasScanToEmail() {
        return scanToEmail;
    }

    /**
     * Printer data contains Scan to Folder.
     *
     * pudyn:ProductUsageDyn -> pudyn:ScanApplicationSubunit -> dd:ImagesSentToFolder
     *
     * @return {boolean} True if supported.
     */
    public boolean hasScanToFolder() {
        return scanToFolder;
    }

    /**
     * Printer data contains Scan to Host.
     *
     * pudyn:ProductUsageDyn -> pudyn:ScanApplicationSubunit -> dd:ScanToHostImages
     *
     * @return {boolean} True if supported.
     */
    public boolean hasScanToHost() {
        return scanToHost;
    }

    /**
     * Printer data contains Scanner Automatic Document Feeder.
     *
     * pudyn:ProductUsageDyn -> pudyn:ScanApplicationSubunit -> dd:AdfImages
     *
     * @return {boolean} True if supported.
     */
    public boolean hasScannerADF() {
        return scanAdf;
    }

    /**
     * Printer data contains Scanner Flatbed.
     *
     * pudyn:ProductUsageDyn -> pudyn:ScanApplicationSubunit -> dd:FlatbedImages
     *
     * @return {boolean} True if supported.
     */
    public boolean hasScannerFlatbed() {
        return scanFlatbed;
    }

    
    /**
     * Printer data contains Print Application Usage Information.
     *
     * pudyn:ProductUsageDyn -> pudyn:MobileApplicationSubunit
     *
     * @return {boolean} True if supported.
     */
    public boolean hasPrintApplication() {
        return printApplication;
    }

    public boolean hasPrintApplicationChrome() {
        return printApplicationChrome;
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

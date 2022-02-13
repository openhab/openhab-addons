/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link HPProductUsageFeatures} is responsible for determining what type of printer usage
 * data the Web Interface supports including any features.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPProductUsageFeatures {
    public static final String ENDPOINT = "/DevMgmt/ProductUsageDyn.xml";

    private PrinterType printerType = PrinterType.UNKNOWN;
    private boolean jamEvents;
    private boolean mispickEvents;
    private boolean subscriptionImpressions;
    private boolean frontPanelCancel;
    private boolean cumuMarking;
    private boolean pagesRemaining;

    private boolean scanAdf;
    private boolean scanFlatbed;
    private boolean scanToHost;
    private boolean scanToEmail;
    private boolean scanToFolder;

    private boolean printApplication;
    private boolean printApplicationChrome;

    private boolean scannerEngine;
    private boolean copyApplication;

    private boolean cloudPrint;

    public enum PrinterType {
        UNKNOWN,
        MONOCHROME,
        SINGLECOLOR,
        MULTICOLOR
    }

    public HPProductUsageFeatures(final Document document) {
        // Check what Ink/Toner colours are present
        final NodeList consumableInk = document.getDocumentElement().getElementsByTagName("pudyn:Consumable");

        for (int i = 0; i < consumableInk.getLength(); i++) {
            final Element currInk = (Element) consumableInk.item(i);
            final String inkName = currInk.getElementsByTagName("dd:MarkerColor").item(0).getTextContent();
            final String consumeType = currInk.getElementsByTagName("dd:ConsumableTypeEnum").item(0).getTextContent();

            if ("printhead".equalsIgnoreCase(consumeType)) {
                continue;
            }

            cumuMarking = (currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed").getLength() > 0);

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

        pagesRemaining = (((Element) consumableInk.item(0)).getElementsByTagName("dd:EstimatedPagesRemaining")
                .getLength() > 0);

        final NodeList printerSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:PrinterSubunit");
        if (printerSubUnit.getLength() > 0) {
            final Element currPrinterSubUnit = (Element) printerSubUnit.item(0);

            jamEvents = (currPrinterSubUnit.getElementsByTagName("dd:JamEvents").getLength() > 0);
            mispickEvents = (currPrinterSubUnit.getElementsByTagName("dd:MispickEvents").getLength() > 0);
            subscriptionImpressions = (currPrinterSubUnit.getElementsByTagName("pudyn:SubscriptionImpressions")
                    .getLength() > 0);
            frontPanelCancel = (currPrinterSubUnit.getElementsByTagName("dd:TotalFrontPanelCancelPresses")
                    .getLength() > 0);
        }

        final NodeList scannerSubUnit = document.getDocumentElement()
                .getElementsByTagName("pudyn:ScanApplicationSubunit");
        if (scannerSubUnit.getLength() > 0) {
            final Element currScannerSubUnit = (Element) scannerSubUnit.item(0);

            scanAdf = (currScannerSubUnit.getElementsByTagName("dd:AdfImages").getLength() > 0);
            scanFlatbed = (currScannerSubUnit.getElementsByTagName("dd:FlatbedImages").getLength() > 0);
            scanToEmail = (currScannerSubUnit.getElementsByTagName("dd:ImagesSentToEmail").getLength() > 0);
            scanToFolder = (currScannerSubUnit.getElementsByTagName("dd:ImagesSentToFolder").getLength() > 0);
            scanToHost = (currScannerSubUnit.getElementsByTagName("dd:ScanToHostImages").getLength() > 0);
        }

        final NodeList scanAppSubUnit = document.getDocumentElement()
                .getElementsByTagName("pudyn:ScanApplicationSubunit");
        if (scanAppSubUnit.getLength() > 0) {
            printApplication = true;

            final Element currAppSubUnit = (Element) scanAppSubUnit.item(0);

            printApplicationChrome = (currAppSubUnit.getElementsByTagName("pudyn:RemoteDeviceType").getLength() > 0);
        }

        final NodeList printAppSubUnit = document.getDocumentElement()
                .getElementsByTagName("pudyn:PrintApplicationSubunit");
        if (printAppSubUnit.getLength() > 0) {
            final Element currPrintAppSubUnit = (Element) printAppSubUnit.item(0);

            cloudPrint = (currPrintAppSubUnit.getElementsByTagName("dd:CloudPrintImpressions").getLength() > 0);
        }

        final NodeList scannerEngineSubUnit = document.getDocumentElement()
                .getElementsByTagName("pudyn:ScannerEngineSubunit");
        scannerEngine = (scannerEngineSubUnit.getLength() > 0);

        final NodeList copyAppSubUnit = document.getDocumentElement()
                .getElementsByTagName("pudyn:CopyApplicationSubunit");
        copyApplication = (copyAppSubUnit.getLength() > 0);
    }

    public PrinterType getType() {
        return printerType;
    }

    /**
     * Printer data contains Scanner Engine.
     * 
     * pudyn:ProductUsageDyn -> pudyn:ScannerEngineSubunit
     * 
     * return {boolean} True is supported.
     */
    public boolean hasScannerEngine() {
        return scannerEngine;
    }

    /**
     * Printer data contains Copy Application.
     * 
     * pudyn:ProductUsageDyn -> pudyn:ScannerEngineSubunit
     * 
     * return {boolean} True is supported.
     */
    public boolean hasCopyApplication() {
        return copyApplication;
    }

    /**
     * Printer data contains Estimated Pages Remaining.
     * 
     * pudyn:ProductUsageDyn -> pudyn:ConsumableSubunit -> pudyn:Consumable -> dd:EstimatedPagesRemaining
     * 
     * return {boolean} True is supported.
     */
    public boolean hasPagesRemaining() {
        return pagesRemaining;
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
    public boolean hasScanADF() {
        return scanAdf;
    }

    /**
     * Printer data contains Scanner Flatbed.
     *
     * pudyn:ProductUsageDyn -> pudyn:ScanApplicationSubunit -> dd:FlatbedImages
     *
     * @return {boolean} True if supported.
     */
    public boolean hasScanFlatbed() {
        return scanFlatbed;
    }

    /**
     * Printer data has Google Cloud Print impressions.
     * 
     * pudyn:ProductUsageDyn -> pudyn:PrintApplicationSubunit -> dd:CloudPrintImpressions
     * 
     * @return {boolean} True if supported.
     */
    public boolean hasCloudPrint() {
        return cloudPrint;
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

    /**
     * Printer data contains Chrome Print Application Usage Information.
     *
     * pudyn:ProductUsageDyn -> pudyn:MobileApplicationSubunit
     *
     * @return {boolean} True if supported.
     */
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

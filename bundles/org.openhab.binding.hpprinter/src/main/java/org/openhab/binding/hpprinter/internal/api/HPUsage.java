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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    private int inkBlackPagesRemaining;
    private int inkColorPagesRemaining;
    private int inkCyanPagesRemaining;
    private int inkMagentaPagesRemaining;
    private int inkYellowPagesRemaining;

    // Scan
    private int scanAdfCount;
    private int scanFlatbedCount;
    private int scanToEmailCount;
    private int scanToFolderCount;
    private int scanToHostCount;

    // Scanner
    private int scannerAdfCount;
    private int scannerFlatbedCount;
    private int scannerJamEvents;
    private int scannerMispickEvents;

    // Copy
    private int copyAdfCount;
    private int copyFlatbedCount;
    private int copyTotalImpressions;
    private int copyTotalMonochromeImpressions;
    private int copyTotalColorImpressions;

    // App
    private int appWindowsCount;
    private int appOsxCount;
    private int appIosCount;
    private int appAndroidCount;
    private int appSamsungCount;
    private int appChromeCount;

    // Other
    private int cloudPrintImpressions;

    public HPUsage() {
    }

    public HPUsage(Document document) {
        // Get Ink Levels
        NodeList consumableInk = document.getDocumentElement().getElementsByTagName("pudyn:Consumable");

        for (int i = 0; i < consumableInk.getLength(); i++) {
            Element currInk = (Element) consumableInk.item(i);

            String inkName = currInk.getElementsByTagName("dd:MarkerColor").item(0).getTextContent();

            String consumeType = currInk.getElementsByTagName("dd:ConsumableTypeEnum").item(0).getTextContent();

            if ("printhead".equalsIgnoreCase(consumeType)) {
                continue;
            }

            String inkRemaining = currInk.getElementsByTagName("dd:ConsumableRawPercentageLevelRemaining").item(0)
                    .getTextContent();

            int markAgentLength = currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed").getLength();
            Boolean hasPagesRemaining = currInk.getElementsByTagName("dd:EstimatedPagesRemaining").getLength() > 0;
            float totalMarking = 0;
            int pagesRemaining = 0;
            if (markAgentLength > 0) { // Check to make sure Cumulative Marking Agent exists
                for (int ai = 0; ai < markAgentLength; ai++) {
                    Element currMarking = (Element) currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed")
                            .item(ai);

                    float marking = Integer
                            .parseInt(currMarking.getElementsByTagName("dd:ValueFloat").item(0).getTextContent());

                    switch (currMarking.getElementsByTagName("dd:Unit").item(0).getTextContent().toLowerCase()) {
                        case "microliters":
                            marking = marking / 1000; // Convert from microlitres to millilitres
                            break;
                        case "liters":
                            marking = marking * 1000; // Convert to litres to millilitres
                    }

                    totalMarking = totalMarking + marking; // Sum the marking counts together
                }
            }

            if (hasPagesRemaining) {
                pagesRemaining = Integer.parseInt(((Element) currInk).getElementsByTagName("dd:EstimatedPagesRemaining")
                        .item(0).getTextContent());
            }

            switch (inkName.toLowerCase()) {
                case "black":
                    inkBlack = Integer.parseInt(inkRemaining);
                    inkBlackMarking = totalMarking;
                    inkBlackPagesRemaining = pagesRemaining;
                    break;

                case "yellow":
                    inkYellow = Integer.parseInt(inkRemaining);
                    inkYellowMarking = totalMarking;
                    inkYellowPagesRemaining = pagesRemaining;
                    break;

                case "magenta":
                    inkMagenta = Integer.parseInt(inkRemaining);
                    inkMagentaMarking = totalMarking;
                    inkMagentaPagesRemaining = pagesRemaining;
                    break;

                case "cyan":
                    inkCyan = Integer.parseInt(inkRemaining);
                    inkCyanMarking = totalMarking;
                    inkCyanPagesRemaining = pagesRemaining;
                    break;

                case "cyanmagentayellow":
                    inkColor = Integer.parseInt(inkRemaining);
                    inkColorMarking = totalMarking;
                    inkColorPagesRemaining = pagesRemaining;
                    break;
            }
        }

        // Get other usage info
        NodeList jamEvents = document.getDocumentElement().getElementsByTagName("dd:JamEvents");
        if (jamEvents.getLength() > 0) {
            this.jamEvents = Integer.parseInt(jamEvents.item(0).getTextContent());
        }

        NodeList totalImpressions = document.getDocumentElement().getElementsByTagName("dd:TotalImpressions");
        if (totalImpressions.getLength() > 0) {
            this.totalImpressions = Integer.parseInt(totalImpressions.item(0).getTextContent());
        }

        NodeList totalColorImpressions = document.getDocumentElement().getElementsByTagName("dd:ColorImpressions");
        if (totalColorImpressions.getLength() > 0) {
            this.totalColorImpressions = Integer.parseInt(totalColorImpressions.item(0).getTextContent());
        }

        NodeList totalMonochromeImpressions = document.getDocumentElement()
                .getElementsByTagName("dd:MonochromeImpressions");
        if (totalMonochromeImpressions.getLength() > 0) {
            this.totalMonochromeImpressions = Integer.parseInt(totalMonochromeImpressions.item(0).getTextContent());
        }

        NodeList totalSubscriptionImpressions = document.getDocumentElement()
                .getElementsByTagName("pudyn:SubscriptionImpressions");
        if (totalSubscriptionImpressions.getLength() > 0) {
            this.totalSubscriptionImpressions = Integer.parseInt(totalSubscriptionImpressions.item(0).getTextContent());
        }

        NodeList mispickEvents = document.getDocumentElement().getElementsByTagName("dd:MispickEvents");
        if (mispickEvents.getLength() > 0) {
            this.mispickEvents = Integer.parseInt(mispickEvents.item(0).getTextContent());
        }

        NodeList frontpanelCancelCount = document.getDocumentElement()
                .getElementsByTagName("dd:TotalFrontPanelCancelPresses");
        if (frontpanelCancelCount.getLength() > 0) {
            this.frontpanelCancelCount = Integer.parseInt(frontpanelCancelCount.item(0).getTextContent());
        }

        // Print Apps
        NodeList printAppsSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:PrintApplicationSubunit");
        if (printAppsSubUnit.getLength() > 0) {
            Element currPrintAppsSubUnit = (Element) printAppsSubUnit.item(0);

            NodeList cloudPrintImpressions = currPrintAppsSubUnit.getElementsByTagName("dd:CloudPrintImpressions");

            if (cloudPrintImpressions.getLength() > 0) {
                this.cloudPrintImpressions = Integer.parseInt(cloudPrintImpressions.item(0).getTextContent());
            }
        }

        // Scan
        NodeList scanSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:ScanApplicationSubunit");
        if (scanSubUnit.getLength() > 0) {
            Element currScanSubUnit = (Element) scanSubUnit.item(0);

            this.scanAdfCount = setInt("dd:AdfImages", currScanSubUnit);
            this.scanFlatbedCount = setInt("dd:FlatbedImages", currScanSubUnit);

            this.scanToEmailCount = setInt("dd:ImagesSentToEmail", currScanSubUnit);
            this.scanToFolderCount = setInt("dd:ImagesSentToFolder", currScanSubUnit);

            this.scanToHostCount = setInt("dd:ScanToHostImages", currScanSubUnit);
        }

        // Scanner
        NodeList scannerSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:ScannerEngineSubunit");
        if (scannerSubUnit.getLength() > 0) {
            Element currScannerSubUnit = (Element) scannerSubUnit.item(0);

            this.scannerAdfCount = setInt("dd:AdfImages", currScannerSubUnit);
            this.scannerFlatbedCount = setInt("dd:FlatbedImages", currScannerSubUnit);
            this.scannerJamEvents = setInt("dd:JamEvents", currScannerSubUnit);
            this.scannerMispickEvents = setInt("dd:MispickEvents", currScannerSubUnit);
        }

        // Copy
        NodeList copySubUnit = document.getDocumentElement().getElementsByTagName("pudyn:CopyApplicationSubunit");
        if (copySubUnit.getLength() > 0) {
            Element currCopySubUnit = (Element) copySubUnit.item(0);

            this.copyAdfCount = setInt("dd:AdfImages", currCopySubUnit);
            this.copyFlatbedCount = setInt("dd:FlatbedImages", currCopySubUnit);
            this.copyTotalColorImpressions = setInt("dd:ColorImpressions", currCopySubUnit);
            this.copyTotalMonochromeImpressions = setInt("dd:MonochromeImpressions", currCopySubUnit);
            this.copyTotalImpressions = setInt("dd:TotalImpressions", currCopySubUnit);
        }

        // App Usage
        NodeList appSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:MobileApplicationSubunit");
        if (appSubUnit.getLength() > 0) {
            Element currAppSubUnit = (Element) appSubUnit.item(0);

            this.appWindowsCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "Windows",
                    "pudyn:TotalImpressions");
            this.appOsxCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "OSX",
                    "pudyn:TotalImpressions");
            this.appIosCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "iOS",
                    "pudyn:TotalImpressions");
            this.appAndroidCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "Android",
                    "pudyn:TotalImpressions");
            this.appSamsungCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "samsung",
                    "pudyn:TotalImpressions");
            this.appChromeCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "Chrome",
                    "pudyn:TotalImpressions");
        }
    }

    private static int setIntCollateDirectChildren(Element parentNode, String collateTagName,
            String collateTagNameValue, String valueTagName) {
        int value = 0;

        for (Node n = parentNode.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                Element nodeItem = (Element) n;
                if (nodeItem.getElementsByTagName(collateTagName).item(0).getTextContent()
                        .equalsIgnoreCase(collateTagNameValue)) {
                    int nodeValue = Integer
                            .parseInt(nodeItem.getElementsByTagName(valueTagName).item(0).getTextContent());

                    value += nodeValue;
                }
            }
        }

        return value;
    }

    private int setInt(String tagName, Element parentNode) {
        NodeList nodeList = parentNode.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return Integer.parseInt(nodeList.item(0).getTextContent());
        }
        return 0;
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

    public int getCloudPrintImpressions() {
        return cloudPrintImpressions;
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

    public int getInkBlackPagesRemaining() {
        return inkBlackPagesRemaining;
    }

    public int getInkColorPagesRemaining() {
        return inkColorPagesRemaining;
    }

    public int getInkMagentaPagesRemaining() {
        return inkMagentaPagesRemaining;
    }

    public int getInkCyanPagesRemaining() {
        return inkCyanPagesRemaining;
    }

    public int getInkYellowPagesRemaining() {
        return inkYellowPagesRemaining;
    }

    public int getInkColor() {
        return inkColor;
    }

    public float getInkColorMarking() {
        return inkColorMarking;
    }

    public int getScanAdfCount() {
        return scanAdfCount;
    }

    public int getScanFlatbedCount() {
        return scanFlatbedCount;
    }

    public int getScanToEmailCount() {
        return scanToEmailCount;
    }

    public int getScanToFolderCount() {
        return scanToFolderCount;
    }

    public int getScanToHostCount() {
        return scanToHostCount;
    }

    public int getAppWindowsCount() {
        return appWindowsCount;
    }

    public int getAppOSXCount() {
        return appOsxCount;
    }

    public int getAppIosCount() {
        return appIosCount;
    }

    public int getAppAndroidCount() {
        return appAndroidCount;
    }

    public int getAppSamsungCount() {
        return appSamsungCount;
    }

    public int getAppChromeCount() {
        return appChromeCount;
    }

    public int getScannerAdfCount() {
        return scannerAdfCount;
    }

    public int getScannerFlatbedCount() {
        return scannerFlatbedCount;
    }

    public int getScannerJamEvents() {
        return scannerJamEvents;
    }

    public int getScannerMispickEvents() {
        return scannerMispickEvents;
    }

    public int getCopyAdfCount() {
        return copyAdfCount;
    }

    public int getCopyFlatbedCount() {
        return copyFlatbedCount;
    }

    public int getCopyTotalImpressions() {
        return copyTotalImpressions;
    }

    public int getCopyTotalColorImpressions() {
        return copyTotalColorImpressions;
    }

    public int getCopyTotalMonochromeImpressions() {
        return copyTotalMonochromeImpressions;
    }
}

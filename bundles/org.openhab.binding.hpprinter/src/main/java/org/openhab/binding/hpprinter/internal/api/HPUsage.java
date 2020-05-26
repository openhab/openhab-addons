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

    private int scanAdfCount;
    private int scanFlatbedCount;
    private int scanToEmailCount;
    private int scanToFolderCount;
    private int scanToHostCount;

    private int appWindowsCount;
    private int appOsxCount;
    private int appIosCount;
    private int appAndroidCount;
    private int appSamsungCount;
    private int appChromeCount;

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

            float totalMarking = 0;
            if (markAgentLength > 0) { // Check to make sure Cumulative Marking Agent exists
                for (int ai = 0; ai < markAgentLength; ai++) {
                    Element currMarking = (Element) currInk.getElementsByTagName("dd2:CumulativeMarkingAgentUsed")
                            .item(ai);

                    float marking = Integer
                            .parseInt(currMarking.getElementsByTagName("dd:ValueFloat").item(0).getTextContent());

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
                    inkBlack = Integer.parseInt(inkRemaining);
                    inkBlackMarking = totalMarking;
                    break;

                case "yellow":
                    inkYellow = Integer.parseInt(inkRemaining);
                    inkYellowMarking = totalMarking;
                    break;

                case "magenta":
                    inkMagenta = Integer.parseInt(inkRemaining);
                    inkMagentaMarking = totalMarking;
                    break;

                case "cyan":
                    inkCyan = Integer.parseInt(inkRemaining);
                    inkCyanMarking = totalMarking;
                    break;

                case "cyanmagentayellow":
                    inkColor = Integer.parseInt(inkRemaining);
                    inkColorMarking = totalMarking;
                    break;
            }
        }

        // Get other usage info
        NodeList jamEvents = document.getDocumentElement().getElementsByTagName("dd:JamEvents");
        if (jamEvents.getLength() > 0)
            this.jamEvents = Integer.parseInt(jamEvents.item(0).getTextContent());

        NodeList totalImpressions = document.getDocumentElement().getElementsByTagName("dd:TotalImpressions");
        if (totalImpressions.getLength() > 0)
            this.totalImpressions = Integer.parseInt(totalImpressions.item(0).getTextContent());

        NodeList totalColorImpressions = document.getDocumentElement().getElementsByTagName("dd:ColorImpressions");
        if (totalColorImpressions.getLength() > 0)
            this.totalColorImpressions = Integer.parseInt(totalColorImpressions.item(0).getTextContent());

        NodeList totalMonochromeImpressions = document.getDocumentElement()
                .getElementsByTagName("dd:MonochromeImpressions");
        if (totalMonochromeImpressions.getLength() > 0)
            this.totalMonochromeImpressions = Integer.parseInt(totalMonochromeImpressions.item(0).getTextContent());

        NodeList totalSubscriptionImpressions = document.getDocumentElement()
                .getElementsByTagName("pudyn:SubscriptionImpressions");
        if (totalSubscriptionImpressions.getLength() > 0)
            this.totalSubscriptionImpressions = Integer.parseInt(totalSubscriptionImpressions.item(0).getTextContent());

        NodeList mispickEvents = document.getDocumentElement().getElementsByTagName("dd:MispickEvents");
        if (mispickEvents.getLength() > 0)
            this.mispickEvents = Integer.parseInt(mispickEvents.item(0).getTextContent());

        NodeList frontpanelCancelCount = document.getDocumentElement()
                .getElementsByTagName("dd:TotalFrontPanelCancelPresses");
        if (frontpanelCancelCount.getLength() > 0)
            this.frontpanelCancelCount = Integer.parseInt(frontpanelCancelCount.item(0).getTextContent());


        
        //Scanner
        NodeList scanSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:ScanApplicationSubunit");
        if (scanSubUnit.getLength() > 0) {
            Element currScannerSubUnit = (Element) scanSubUnit.item(0);

            this.scanAdfCount = setInt("dd:AdfImages", currScannerSubUnit);
            this.scanFlatbedCount = setInt("dd:FlatbedImages", currScannerSubUnit);

            this.scanToEmailCount = setInt("dd:ImagesSentToEmail", currScannerSubUnit);
            this.scanToFolderCount = setInt("dd:ImagesSentToFolder", currScannerSubUnit);

            this.scanToHostCount = setInt("dd:ScanToHostImages", currScannerSubUnit);
        }

        //App Usage
        NodeList appSubUnit = document.getDocumentElement().getElementsByTagName("pudyn:MobileApplicationSubunit");
        if (appSubUnit.getLength() > 0) {
            Element currAppSubUnit = (Element) appSubUnit.item(0);
            
            this.appWindowsCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "Windows", "pudyn:TotalImpressions");
            this.appOsxCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "OSX", "pudyn:TotalImpressions");
            this.appIosCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "iOS", "pudyn:TotalImpressions");
            this.appAndroidCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "Android", "pudyn:TotalImpressions");
            this.appSamsungCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "samsung", "pudyn:TotalImpressions");
            this.appChromeCount = setIntCollateDirectChildren(currAppSubUnit, "pudyn:RemoteDeviceType", "Chrome", "pudyn:TotalImpressions");
        }
    }

    private static int setIntCollateDirectChildren(Element parentNode, String collateTagName, String collateTagNameValue, String valueTagName) {
        int value = 0;

        for (Node n = parentNode.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                Element nodeItem = (Element) n;
                if (nodeItem.getElementsByTagName(collateTagName).item(0).getTextContent().equalsIgnoreCase(collateTagNameValue)) {
                    int nodeValue = Integer.parseInt(nodeItem.getElementsByTagName(valueTagName).item(0).getTextContent());

                    value += nodeValue;
                }
            }
        }

        return value;
    }

    private int setInt(String tagName, Element parentNode) {
        NodeList nodeList = parentNode.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0)
                return Integer.parseInt(nodeList.item(0).getTextContent());
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

}

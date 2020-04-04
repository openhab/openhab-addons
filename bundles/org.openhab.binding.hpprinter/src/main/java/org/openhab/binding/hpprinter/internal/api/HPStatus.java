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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link HPStatus} is responsible for handling reading of status data.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPStatus {
    public static final String ENDPOINT = "/DevMgmt/ProductStatusDyn.xml";

    private static final Map<String, String> STATUS_MESSAGES = initializeStatus();

    private final @Nullable String printerStatus;

    public HPStatus(Document document) {
        NodeList nodes = document.getDocumentElement().getElementsByTagName("psdyn:Status");

        String localPrinterStatus = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String statusCategory = element.getElementsByTagName("pscat:StatusCategory").item(0).getTextContent();
            if (!"genuineHP".equals(statusCategory)) {
                localPrinterStatus = STATUS_MESSAGES.getOrDefault(statusCategory, statusCategory);
            }
        }
        printerStatus = localPrinterStatus;
    }

    private static Map<String, String> initializeStatus() {
        Map<String, String> statusMap = new HashMap<>();

        statusMap.put("processing", "Printing");
        statusMap.put("scanProcessing", "Scanning");
        statusMap.put("inPowerSave", "Power Save");
        statusMap.put("ready", "Idle");
        statusMap.put("initializing", "Initializing...");
        statusMap.put("closeDoorOrCover", "Door/Cover Open");
        statusMap.put("inkSystemInitializing", "Loading Ink");
        statusMap.put("shuttingDown", "Shutting Down");
        return statusMap;
    }

    public @Nullable String getPrinterStatus() {
        return STATUS_MESSAGES.get(printerStatus);
    }
}

/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

    private final String printerStatus;
    private final boolean trayEmptyOrOpen;

    public HPStatus(Document document) {
        NodeList nodes = document.getDocumentElement().getElementsByTagName("psdyn:Status");

        String localPrinterStatus = "Unknown";
        boolean localTrayEmptyOrOpen = false;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String statusCategory = element.getElementsByTagName("pscat:StatusCategory").item(0).getTextContent();
            if (!"genuineHP".equals(statusCategory) && !"trayEmpty".equals(statusCategory)) {
                localPrinterStatus = statusCategory;
            }
            if ("trayEmpty".equals(statusCategory)) {
                localTrayEmptyOrOpen = true;
            }
        }
        trayEmptyOrOpen = localTrayEmptyOrOpen;
        printerStatus = localPrinterStatus;
    }

    public boolean getTrayEmptyOrOpen() {
        return trayEmptyOrOpen;
    }

    public @Nullable String getPrinterStatus() {
        return printerStatus;
    }
}

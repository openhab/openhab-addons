/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link HPScannerStatus} is responsible for handling reading of scanner status data.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPFeatures {
    public static final String ENDPOINT = "/DevMgmt/DiscoveryTree.xml";

    private final boolean productStatus;
    private final boolean productUsage;
    private final boolean scannerStatus;

    public HPFeatures(Document document) {
        Element root = (Element) document.getDocumentElement();

        boolean localProductStatus = false;
        boolean localProductUsage = false;
        boolean localScannerStatus = false;

        for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                Element feature = (Element) n;

                NodeList resourceType = feature.getElementsByTagName("dd:ResourceType");

                if (resourceType.getLength() > 0) {
                    switch (resourceType.item(0).getTextContent()) {
                        case "ledm:hpLedmProductStatusDyn":
                            localProductStatus = true;
                            break;

                        case "ledm:hpLedmProductUsageDyn":
                            localProductUsage = true;
                            break;

                        case "eSCL:eSclManifest":
                            localScannerStatus = true;
                            break;
                    }
                }
            }
        }

        productStatus = localProductStatus;
        productUsage = localProductUsage;
        scannerStatus = localScannerStatus;
    }

    public boolean getProductStatusSupported() {
        return productStatus;
    }

    public boolean getProductUsageSupported() {
        return productUsage;
    }

    public boolean getScannerStatusSupported() {
        return scannerStatus;
    }
}

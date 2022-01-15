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
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link HPScannerStatus} is responsible for handling reading of scanner status data.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPScannerStatus {
    public static final String ENDPOINT = "/eSCL/ScannerStatus";

    private final String status;
    private final Boolean adfLoaded;

    public HPScannerStatus(Document document) {
        String localScannerStatus = "Unknown";
        Boolean localAdfLoaded = false;

        Element nodes = (Element) document.getDocumentElement();

        NodeList state = nodes.getElementsByTagName("pwg:State");
        if (state.getLength() > 0) {
            localScannerStatus = state.item(0).getTextContent();
        }

        NodeList adfState = nodes.getElementsByTagName("scan:AdfState");
        if (adfState.getLength() > 0) {
            String adfStatus = adfState.item(0).getTextContent();
            localAdfLoaded = convertAdfStatus(adfStatus);
        }

        adfLoaded = localAdfLoaded;
        status = localScannerStatus;
    }

    private static Boolean convertAdfStatus(String status) {
        return "ScannerAdfLoaded".equals(status);
    }

    public Boolean getAdfLoaded() {
        return adfLoaded;
    }

    public @Nullable String getScannerStatus() {
        return status;
    }
}

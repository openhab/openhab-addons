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

/**
 * The {@link HPScannerStatusFeatures} is responsible for determining what type of printer scanner
 * status features the Web Interface supports.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPScannerStatusFeatures {
    public static final String ENDPOINT = "/eSCL/ScannerStatus";

    private final boolean hasStatus;
    private final boolean hasAdf;

    public HPScannerStatusFeatures(Document document) {
        boolean localHasStatus = false;
        boolean localHasAdf = false;

        Element nodes = (Element) document.getDocumentElement();
        localHasStatus = (nodes.getElementsByTagName("pwg:State").getLength() > 0);
        localHasAdf = (nodes.getElementsByTagName("scan:AdfState").getLength() > 0);

        hasStatus = localHasStatus;
        hasAdf = localHasAdf;
    }

    public boolean hasStatus() {
        return hasStatus;
    }

    public boolean hasAdf() {
        return hasAdf;
    }
}

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
import java.util.HashMap;
import java.util.Map;

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
 * The {@link HPStatus} is responsible for handling reading of status data.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPStatus {
    public static final String ENDPOINT = "/DevMgmt/ProductStatusDyn.xml";

    private String printerStatus = "";
    
    public HPStatus() {
    }

    public HPStatus(InputSource source) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(source);

        NodeList nodes = document.getDocumentElement().getElementsByTagName("psdyn:Status");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (element.getElementsByTagName("pscat:StatusCategory").item(0).getTextContent() != "genuineHP") {
                printerStatus = getPrinterStatusMessage(
                        element.getElementsByTagName("pscat:StatusCategory").item(0).getTextContent());
            }
        }
    }

    private String getPrinterStatusMessage(String statusMsg) {
        Map<String, String> smap = new HashMap<>();

        smap.put("processing", "Printing");
        smap.put("scanProcessing", "Scanning");
        smap.put("inPowerSave", "Power Save");
        smap.put("ready", "Idle");
        smap.put("closeDoorOrCover", "Door/Cover Open");
        smap.put("inkSystemInitializing", "Loading Ink");

        return smap.getOrDefault(statusMsg, statusMsg);
    }

    public String getPrinterStatus() {
        return getPrinterStatusMessage(printerStatus);
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccSystemInformation.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccSystemInformation {

    /** The action header. */
    private String actionHeader;

    /** The wol mac address. */
    private String wolMacAddress;

    /**
     * Instantiates a new ircc system information.
     */
    public IrccSystemInformation() {
        actionHeader = "CERS-DEVICE-ID";
    }

    /**
     * Instantiates a new ircc system information.
     *
     * @param sysInfoXml the sys info xml
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public IrccSystemInformation(Document sysInfoXml) throws ParserConfigurationException, SAXException, IOException {

        this(); // defines default settings

        final NodeList actionHeaderNodeList = sysInfoXml.getElementsByTagName("actionHeader");
        if (actionHeaderNodeList.getLength() > 0) {
            actionHeader = ((Element) actionHeaderNodeList.item(0)).getAttribute("name");
        }

        final NodeList functions = sysInfoXml.getElementsByTagName("function");
        for (int k = functions.getLength() - 1; k >= 0; k--) {
            final Element function = (Element) functions.item(k);
            final String functionName = function.getAttribute("name");
            if ("wol".equalsIgnoreCase(functionName)) {
                final NodeList items = function.getElementsByTagName("functionItem");
                for (int i = items.getLength() - 1; i >= 0; i--) {
                    final Element item = (Element) items.item(i);
                    if ("mac".equalsIgnoreCase(item.getAttribute("field"))) {
                        wolMacAddress = item.getAttribute("value");
                    }
                }
            }
        }
    }

    /**
     * Gets the action header.
     *
     * @return the action header
     */
    public String getActionHeader() {
        return actionHeader;
    }

    /**
     * Gets the wol mac address.
     *
     * @return the wol mac address
     */
    public String getWolMacAddress() {
        return wolMacAddress;
    }
}
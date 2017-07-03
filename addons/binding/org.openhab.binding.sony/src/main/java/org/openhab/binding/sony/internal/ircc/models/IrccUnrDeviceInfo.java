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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccUnrDeviceInfo.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccUnrDeviceInfo {

    /** The Constant NOTSPECIFIED. */
    private final static String NOTSPECIFIED = "Not Specified";

    /** The version. */
    private String version;

    /** The action list url. */
    private String actionListUrl;

    /**
     * Instantiates a new ircc unr device info.
     */
    public IrccUnrDeviceInfo() {
        version = NOTSPECIFIED;
    }

    /**
     * Instantiates a new ircc unr device info.
     *
     * @param service the service
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public IrccUnrDeviceInfo(Node service) throws ParserConfigurationException, SAXException, IOException {
        final NodeList nodes = service.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("X_UNR_Version".equalsIgnoreCase(nodeName)) {
                version = node.getTextContent();
            } else if ("X_CERS_ActionList_URL".equalsIgnoreCase(nodeName)) {
                actionListUrl = node.getTextContent();
            }
        }
    }

    /**
     * Gets the action list url.
     *
     * @return the action list url
     */
    public String getActionListUrl() {
        return actionListUrl;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Checks if is version specified.
     *
     * @return true, if is version specified
     */
    public boolean isVersionSpecified() {
        return version == NOTSPECIFIED;
    }
}
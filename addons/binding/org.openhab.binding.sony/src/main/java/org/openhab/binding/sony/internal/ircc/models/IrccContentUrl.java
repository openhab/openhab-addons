/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccContentUrl.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccContentUrl {

    /** The url. */
    private String url;

    /** The content information. */
    private IrccContentInformation contentInformation;

    /**
     * Instantiates a new ircc content url.
     *
     * @param xml the xml
     */
    public IrccContentUrl(Element xml) {
        final NodeList contents = xml.getChildNodes();
        for (int i = contents.getLength() - 1; i >= 0; i--) {
            final Node infoItem = contents.item(i);
            final String nodeName = infoItem.getNodeName();

            if ("url".equalsIgnoreCase(nodeName)) {
                url = infoItem.getTextContent();
            } else if ("contentInformation".equalsIgnoreCase(nodeName)) {
                contentInformation = new IrccContentInformation((Element) infoItem);
            }
        }
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the content information.
     *
     * @return the content information
     */
    public IrccContentInformation getContentInformation() {
        return contentInformation;
    }
}

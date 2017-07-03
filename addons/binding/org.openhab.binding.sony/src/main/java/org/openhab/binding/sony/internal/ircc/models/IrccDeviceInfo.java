/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccDeviceInfo.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccDeviceInfo {

    /** The version. */
    private String version;

    /** The categories. */
    private List<String> categories = new ArrayList<String>();

    /**
     * Instantiates a new ircc device info.
     *
     * @param service the service
     */
    public IrccDeviceInfo(Node service) {
        version = "1.0"; // default version

        final NodeList nodes = service.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("X_IRCC_Version".equalsIgnoreCase(nodeName)) {
                version = node.getTextContent();
            } else if ("X_IRCC_CategoryList".equalsIgnoreCase(nodeName)) {

                final NodeList irccCategories = ((Element) node).getElementsByTagNameNS(IrccState.SONY_AV_NS,
                        "X_CategoryInfo");
                for (int j = irccCategories.getLength() - 1; j >= 0; j--) {
                    categories.add(irccCategories.item(j).getTextContent());
                }
            }
        }
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
     * Gets the categories.
     *
     * @return the categories
     */
    public List<String> getCategories() {
        return categories;
    }
}
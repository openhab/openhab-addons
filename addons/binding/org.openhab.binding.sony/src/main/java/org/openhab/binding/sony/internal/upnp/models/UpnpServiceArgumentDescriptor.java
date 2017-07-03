/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.upnp.models;

import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class UpnpServiceArgumentDescriptor.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class UpnpServiceArgumentDescriptor {

    /** The arg name. */
    private String argName;

    /** The in. */
    private boolean in;

    /** The data type. */
    private String dataType;

    /**
     * Instantiates a new upnp service argument descriptor.
     *
     * @param arg the arg
     * @param stateTable the state table
     */
    public UpnpServiceArgumentDescriptor(Node arg, Map<String, String> stateTable) {
        final NodeList argList = arg.getChildNodes();
        for (int i = argList.getLength() - 1; i >= 0; i--) {
            final Node argItem = argList.item(i);
            final String argItemName = argItem.getNodeName();

            if ("name".equalsIgnoreCase(argItemName)) {
                argName = argItem.getTextContent();
            } else if ("direction".equalsIgnoreCase(argItemName)) {
                in = argItem.getTextContent().equals("in");
            } else if ("relatedStateVariable".equalsIgnoreCase(argItemName)) {
                final String stateVarName = argItem.getTextContent();
                dataType = stateTable.get(stateVarName);
            }
        }
    }

    /**
     * Gets the arg name.
     *
     * @return the arg name
     */
    public String getArgName() {
        return argName;
    }

    /**
     * Checks if is in.
     *
     * @return true, if is in
     */
    public boolean isIn() {
        return in;
    }
}
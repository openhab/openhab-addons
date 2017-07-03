/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.upnp.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class UpnpServiceDescriptor.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class UpnpServiceDescriptor {

    /** The actions. */
    private Map<String, UpnpServiceActionDescriptor> actions = new HashMap<String, UpnpServiceActionDescriptor>();

    /**
     * Instantiates a new upnp service descriptor.
     *
     * @param parent the parent
     * @param xml the xml
     */
    public UpnpServiceDescriptor(UpnpService parent, Document xml) {
        Map<String, String> stateTable = new HashMap<String, String>();
        final NodeList serviceStateTable = xml.getElementsByTagName("stateVariable");
        for (int i = serviceStateTable.getLength() - 1; i >= 0; i--) {
            final Node stateVariable = serviceStateTable.item(i);
            final NodeList variables = stateVariable.getChildNodes();
            String varName = null, varType = null;
            for (int j = variables.getLength() - 1; j >= 0; j--) {
                final Node n = variables.item(j);
                if ("name".equalsIgnoreCase(n.getNodeName())) {
                    varName = n.getTextContent();
                } else if ("datatype".equalsIgnoreCase(n.getNodeName())) {
                    varType = n.getTextContent();
                }
            }
            if (StringUtils.isNotEmpty(varName) && StringUtils.isNotEmpty(varType)) {
                stateTable.put(varName, varType);
            }
        }

        final NodeList actionNodes = xml.getElementsByTagName("action");
        for (int i = actionNodes.getLength() - 1; i >= 0; i--) {
            final Node action = actionNodes.item(i);
            final UpnpServiceActionDescriptor actionDesc = new UpnpServiceActionDescriptor(parent, (Element) action,
                    stateTable);
            actions.put(actionDesc.getActionName(), actionDesc);
        }
    }

    /**
     * Gets the action descriptor.
     *
     * @param actionName the action name
     * @return the action descriptor
     */
    public UpnpServiceActionDescriptor getActionDescriptor(String actionName) {
        return actions.get(actionName);
    }
}
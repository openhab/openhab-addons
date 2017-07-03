/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.upnp.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class UpnpServiceActionDescriptor.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class UpnpServiceActionDescriptor {

    /** The parent. */
    private UpnpService parent;

    /** The action name. */
    private String actionName;

    /** The arguments. */
    private List<UpnpServiceArgumentDescriptor> arguments = new ArrayList<UpnpServiceArgumentDescriptor>();

    /**
     * Instantiates a new upnp service action descriptor.
     *
     * @param parent the parent
     * @param action the action
     * @param stateTable the state table
     */
    public UpnpServiceActionDescriptor(UpnpService parent, Element action, Map<String, String> stateTable) {
        this.parent = parent;
        final NodeList actionChildren = action.getChildNodes();
        for (int j = actionChildren.getLength() - 1; j >= 0; j--) {
            final Node actionChild = actionChildren.item(j);
            final String nodeName = actionChild.getNodeName();
            if ("name".equalsIgnoreCase(nodeName)) {
                actionName = actionChild.getTextContent();
            } else if ("argumentList".equalsIgnoreCase(nodeName)) {
                final NodeList argList = ((Element) actionChild).getElementsByTagName("argument");
                for (int k = argList.getLength() - 1; k >= 0; k--) {
                    arguments.add(new UpnpServiceArgumentDescriptor(argList.item(k), stateTable));
                }
            }
        }

    }

    /**
     * Gets the action name.
     *
     * @return the action name
     */
    public String getActionName() {
        return actionName;

    }

    /**
     * Gets the soap.
     *
     * @param parms the parms
     * @return the soap
     */
    public String getSoap(String... parms) {

        final StringBuilder sb = new StringBuilder(
                "<?xml version=\"1.0\"?>\n<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n  <s:Body>\n    <u:");
        sb.append(actionName);
        sb.append(" xmlns:u=\"");
        sb.append(parent.getServiceType());
        sb.append("\">");

        int parmIdx = 0;
        for (int i = 0; i < arguments.size(); i++) {
            final UpnpServiceArgumentDescriptor arg = arguments.get(i);
            if (arg.isIn()) {
                final String parm = parmIdx >= parms.length ? "" : parms[parmIdx++];
                sb.append("\n      <");
                sb.append(arg.getArgName());
                sb.append(">");
                sb.append(parm);
                sb.append("</");
                sb.append(arg.getArgName());
                sb.append(">");
            }
        }

        sb.append("\n    </u:");
        sb.append(actionName);
        sb.append(">\n  </s:Body>\n</s:Envelope>\n");
        return sb.toString();

        // StringBuilder sb = new StringBuilder(
        // "<?xml version=\"1.0\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"
        // SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><SOAP-ENV:Body><m:");
        // sb.append(actionName);
        // sb.append(" xmlns:m=\"urn:schemas-sony-com:service:IRCC:1\">");
        // sb.append(" xmlns:m=\"");
        // sb.append(parent.getServiceType());
        // sb.append("\">");
        //
        // for (int i = 0; i < arguments.size(); i++) {
        // final IrccServiceArgumentDescriptor arg = arguments.get(i);
        // final String parm = i >= parms.length ? "" : parms[i];
        // sb.append("<");
        // sb.append(arg.argName);
        // sb.append(" xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"");
        // sb.append(arg.dataType);
        // sb.append("\">");
        // sb.append(parm);
        // sb.append("</");
        // sb.append(arg.argName);
        // sb.append(">");
        // }
        //
        // sb.append("</m:");
        // sb.append(actionName);
        // sb.append("></SOAP-ENV:Body></SOAP-ENV:Envelope>");
        // return sb.toString();
    }
}
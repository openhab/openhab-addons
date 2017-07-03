/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccCodeList.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccCodeList {

    /** The cmds. */
    private Map<String, String> cmds = new HashMap<String, String>();

    /**
     * Instantiates a new ircc code list.
     *
     * @param codeList the code list
     */
    public IrccCodeList(Node codeList) {
        final NodeList nodes = codeList.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("X_IRCCCode".equalsIgnoreCase(nodeName)) {
                final Element irccCode = (Element) node;
                final String cmd = irccCode.getAttribute("command");
                final String cmdValue = irccCode.getTextContent();
                cmds.put(cmd, cmdValue);
            }
        }
    }

    /**
     * Gets the commands.
     *
     * @return the commands
     */
    public Map<String, String> getCommands() {
        return Collections.unmodifiableMap(cmds);
    }
}
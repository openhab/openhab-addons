/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class DialApp.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DialApp {

    /** The id. */
    private String id;

    /** The name. */
    private String name;

    /** The icon url. */
    private String iconUrl;

    /** The actions. */
    private final List<String> actions = new ArrayList<String>();

    /**
     * Instantiates a new dial app.
     *
     * @param appNode the app node
     */
    public DialApp(Node appNode) {
        final NodeList nodes = appNode.getChildNodes();

        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("id".equalsIgnoreCase(nodeName)) {
                id = node.getTextContent();
            } else if ("name".equalsIgnoreCase(nodeName)) {
                name = node.getTextContent();
            } else if ("icon_url".equalsIgnoreCase(nodeName)) {
                iconUrl = node.getTextContent();
            } else if ("supportAction".equalsIgnoreCase(nodeName)) {
                final NodeList actionNode = node.getChildNodes();
                for (int k = actionNode.getLength() - 1; k >= 0; k--) {
                    final String actionElementName = node.getLocalName();
                    if ("action".equalsIgnoreCase(actionElementName)) {
                        getActions().add(node.getTextContent());
                    }
                }
            }
        }
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the icon url.
     *
     * @return the icon url
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     */
    public List<String> getActions() {
        return Collections.unmodifiableList(actions);
    }
}

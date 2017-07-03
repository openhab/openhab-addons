/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial.models;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class DialAppState.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DialAppState {

    /** The Constant RUNNING. */
    public final static String RUNNING = "running";

    /** The Constant STOPPED. */
    public final static String STOPPED = "stopped";

    /** The Constant INSTALLABLE. */
    public final static String INSTALLABLE = "installable";

    /** The state. */
    private String state;

    /** The install url. */
    private String installUrl;

    /** The Constant DIALINSTALLABLE. */
    private final static String DIALINSTALLABLE = "installable=";

    /**
     * Instantiates a new dial app state.
     *
     * @param appNode the app node
     */
    public DialAppState(Node appNode) {
        final NodeList nodes = appNode.getChildNodes();

        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            final Node node = nodes.item(i);
            final String nodeName = node.getLocalName();

            if ("service".equalsIgnoreCase(nodeName)) {
                final NodeList serviceNodes = node.getChildNodes();
                for (int k = serviceNodes.getLength() - 1; k >= 0; k--) {
                    final Node mynode = serviceNodes.item(k);
                    final String mynodeName = mynode.getLocalName();
                    if ("state".equalsIgnoreCase(mynodeName)) {
                        String text = mynode.getTextContent();
                        if (text == null) {
                            continue;
                        }
                        text = text.toLowerCase();
                        if (text.startsWith(DIALINSTALLABLE)) {
                            state = INSTALLABLE;
                            installUrl = text.substring(DIALINSTALLABLE.length());
                        } else {
                            state = text;
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Gets the install url.
     *
     * @return the install url
     */
    public String getInstallUrl() {
        return installUrl;
    }

    /**
     * Checks if is running.
     *
     * @return true, if is running
     */
    public boolean isRunning() {
        return RUNNING.equals(state);
    }
}

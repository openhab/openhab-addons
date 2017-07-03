/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class DialApps.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DialApps {

    /** The apps. */
    private Map<String, DialApp> apps = new HashMap<String, DialApp>();

    /**
     * Instantiates a new dial apps.
     *
     * @param actionXml the action xml
     */
    public DialApps(Document actionXml) {
        final NodeList actions = actionXml.getElementsByTagName("app");
        for (int k = actions.getLength() - 1; k >= 0; k--) {
            final Node action = actions.item(k);
            final DialApp app = new DialApp(action);
            apps.put(app.getId(), app);
        }

    }

    /**
     * Gets the dial ids.
     *
     * @return the dial ids
     */
    public Set<String> getDialIds() {
        return Collections.unmodifiableSet(apps.keySet());
    }

    /**
     * Gets the dial app.
     *
     * @param dialId the dial id
     * @return the dial app
     */
    public DialApp getDialApp(String dialId) {
        return apps.get(dialId);
    }

    /**
     * Gets the apps.
     *
     * @return the apps
     */
    public Collection<DialApp> getApps() {
        return Collections.unmodifiableCollection(apps.values());
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccActions.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccActions {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(IrccActions.class);

    /** The action urls. */
    private Map<String, String> actionUrls = new HashMap<String, String>();

    /** The registration mode. */
    private int registrationMode;

    /**
     * Instantiates a new ircc actions.
     *
     * @param baseUri the base uri
     */
    public IrccActions(URI baseUri) {
        // no defaults
    }

    /**
     * Instantiates a new ircc actions.
     *
     * @param actionXml the action xml
     * @param baseUri the base uri
     * @throws URISyntaxException the URI syntax exception
     */
    public IrccActions(Document actionXml, URI baseUri) throws URISyntaxException {
        final NodeList actions = actionXml.getElementsByTagName("action");
        for (int k = actions.getLength() - 1; k >= 0; k--) {
            final Element action = (Element) actions.item(k);
            final String actionName = action.getAttribute("name");
            final String url = action.getAttribute("url");

            if (actionName != null && url != null) {
                final URI actionUri = NetUtilities.getUri(baseUri, url);
                actionUrls.put(actionName, actionUri.toString());
                logger.debug("Found action: {} for {}", actionName, actionUri);
            }

            final String mode = action.getAttribute("mode");
            if (StringUtils.isNotEmpty(mode)) {
                try {
                    registrationMode = Integer.parseInt(mode);
                } catch (NumberFormatException e) {
                    // do nothing
                }
            }
        }

    }

    /**
     * Gets the url for action.
     *
     * @param actionName the action name
     * @return the url for action
     */
    public String getUrlForAction(String actionName) {
        return actionUrls.get(actionName);
    }

    /**
     * Gets the registration mode.
     *
     * @return the registration mode
     */
    public int getRegistrationMode() {
        return registrationMode;
    }
}
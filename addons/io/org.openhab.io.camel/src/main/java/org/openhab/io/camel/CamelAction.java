/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.camel;

import java.util.Map;

import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.openhab.io.camel.internal.CamelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides methods that can be used in automation rules
 * to send messages to Apache Camel routing functionality.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public class CamelAction {

    private static final Logger logger = LoggerFactory.getLogger(CamelAction.class);

    public static CamelService camelService = null;

    /**
     * Sends message to Apache Camel route
     *
     * @param actionId actionId defined in Camel route
     * @param headers message headers to be included in Camel message
     * @param message message body for Camel message
     *
     */
    @ActionDoc(text = "Sends message to camel route")
    static public void sendCamelAction(String actionId, Map<String, Object> headers, String message) {
        logger.debug("sending message '{}' to Camel route '{}' with headers '{}'", message, actionId, headers);

        if (camelService != null) {
            camelService.sendCamelAction(actionId, headers, message);
        }
    }
}

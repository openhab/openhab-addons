/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.camel.internal.endpoint;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenHAB Camel component producer implementation.
 *
 * Send data from Camel routes to openHAB items.
 *
 * @author Pauli Anttila - Initial contribution
 */
class OpenhabProducer extends DefaultProducer {

    private static final Logger logger = LoggerFactory.getLogger(OpenhabProducer.class);

    public OpenhabProducer(OpenhabEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting Producer {} for {}", this, getEndpoint());
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping Producer {} for {}", this, getEndpoint());
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.debug("Received {} in Producer of {}", exchange, getEndpoint());

        CamelCallback callb = ((OpenhabEndpoint) getEndpoint()).getCallback();
        if (callb != null) {
            String uri = getEndpoint().getEndpointConfiguration().getURI().toString();
            if (uri != null) {
                String itemName = getEndpoint().getEndpointConfiguration().getParameter("itemName");
                if (itemName == null || itemName.equals("")) {
                    // try to find itemName from message headers
                    itemName = exchange.getIn().getHeader("itemName").toString();
                }
                if (itemName != null) {
                    if (uri.startsWith("openhab://" + OpenhabComponent.ROUTE_COMMANDS)) {
                        try {
                            callb.sendCommand(itemName, exchange.getIn().getBody().toString());
                        } catch (Exception e) {
                            logger.debug("Error occured", e);
                        }
                    } else if (uri.startsWith("openhab://" + OpenhabComponent.ROUTE_STATUS)) {
                        try {
                            callb.sendStatusUpdate(itemName, exchange.getIn().getBody().toString());
                        } catch (Exception e) {
                            logger.debug("Error occured", e);
                        }
                    }
                }
            }
        }
    }
}
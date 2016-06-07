/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.camel.internal.endpoint;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenHAB Camel component consumer implementation.
 *
 * Send data from openHAB items to Camel routes.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OpenhabConsumer extends DefaultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OpenhabConsumer.class);

    public OpenhabConsumer(OpenhabEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    public OpenhabEndpoint getEndpoint() {
        return (OpenhabEndpoint) super.getEndpoint();
    }

    @Override
    public void start() throws Exception {
        log.info("Starting Consumer {} for Endpoint {}", this.getClass().getName(), getEndpoint());
        getEndpoint().addConsumer(this);
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping Consumer {}", this.getClass().getName());
        getEndpoint().removeConsumer(this);
    }

    void processExchange(final Exchange exchange) {
        logger.debug("processExchange: exchange={}, item={}, value={}", exchange,
                exchange.getIn().getHeader("ItemName"), exchange.getIn().getBody());

        boolean sync = true;
        try {
            sync = getAsyncProcessor().process(exchange, new AsyncCallback() {
                @Override
                public void done(boolean doneSync) {
                    logger.debug("Exchange '{}' done", exchange);

                    if (exchange.getException() != null) {
                        log.warn("Error occured during exchange '{}' processing", exchange, exchange.getException());
                    }
                }
            });
        } catch (Throwable e) {
            exchange.setException(e);
        }

        if (sync) {
            if (exchange.getException() != null) {
                log.warn("Error occured during exchange '{}' processing", exchange, exchange.getException());
            }
        }
    }
}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.camel.internal.endpoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel endpoint implementation for openHAB component.
 *
 * @author Pauli Anttila - Initial contribution
 */
class OpenhabEndpoint extends DefaultEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OpenhabEndpoint.class);

    private String uri;
    private String uriName;
    private final List<OpenhabConsumer> consumers = new CopyOnWriteArrayList<OpenhabConsumer>();
    private Configuration configuration;
    private CamelCallback callback = null;

    public OpenhabEndpoint(String uri, String uriName, Configuration configuration,
            CamelCallback callback) {
        this.uri = uri;
        this.uriName = uriName;
        this.configuration = configuration;
        this.callback = callback;
    }

    /**
     * The {@link Producer} is the piece that produces pieces of info for the outside world.
     *
     */
    @Override
    public Producer createProducer() throws Exception {
        logger.info("Creating producer for {}.", uriName);
        return new OpenhabProducer(this);
    }

    /**
     * The {@link Consumer} is the piece that reads info from the outside world and puts data into Camel.
     */
    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        logger.info("Creating consumer for {}.", uriName);
        OpenhabConsumer consumer = new OpenhabConsumer(this, processor);
        return consumer;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected String createEndpointUri() {
        return uri;
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting Endpoint {}", this);
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping Endpoint {}", this);
    }

    @Override
    public String toString() {
        return uriName;
    }

    public void addConsumer(OpenhabConsumer consumer) {

        consumers.add(consumer);
    }

    public void removeConsumer(OpenhabConsumer consumer) {
        consumers.remove(consumer);
    }

    public CamelCallback getCallback() {
        return callback;
    }

    public void sendItemUpdate(String itemName, String value) {
        sendItem(itemName, value);
    }

    public void sendItemCommand(String itemName, String value) {
        sendItem(itemName, value);
    }

    public void storeItem(String itemName, String value) {
        sendItem(itemName, value);
    }

    public void sendAction(String actionId, Map<String, Object> headers, String message) {
        String aId = configuration.getActionId();
        if (actionId.equals(aId) || "*".equals(aId)) {
            if (!consumers.isEmpty()) {
                logger.debug("actionId '{}' match", actionId);
                Exchange exchange = createExchange();
                exchange.getIn().setBody(message);
                if (headers != null) {
                    exchange.getIn().setHeaders(headers);
                }

                for (OpenhabConsumer consumer : consumers) {
                    logger.debug("Delegate action exchange...");
                    consumer.processExchange(exchange);
                }
            }
        } else {
            logger.debug("actionId '{}' does not match the configuration '{}', ignore", actionId,
                    configuration.getActionId());
        }
    }

    private void sendItem(String itemName, String value) {
        String iname = configuration.getItemName();

        if (itemName.equals(iname) || "*".equals(iname)) {
            if (!consumers.isEmpty()) {
                logger.debug("ItemName '{}' match to endpoint uri", itemName);
                Exchange exchange = createExchange();
                exchange.getIn().setBody(value);
                exchange.getIn().setHeader("itemName", itemName);

                for (OpenhabConsumer consumer : consumers) {
                    logger.debug("Delegate itemUpdate exchange...");
                    consumer.processExchange(exchange);
                }
            }
        } else {
            logger.debug("ItemName '{}' does not match to endpoint uri '{}', ignore", itemName,
                    configuration.getItemName());
        }
    }
}
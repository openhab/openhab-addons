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

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenhabComponent} Camel component to send and receive data to/from Apache Camel routes.
 */
public class OpenhabComponent extends DefaultComponent {

    public static final String ROUTE_STATUS = "status";
    public static final String ROUTE_COMMANDS = "command";
    public static final String ROUTE_ACTION = "action";
    public static final String ROUTE_PERSISTENCE = "persistence";

    private static final Logger logger = LoggerFactory.getLogger(OpenhabComponent.class);

    public final List<OpenhabEndpoint> statusEndpoints = new CopyOnWriteArrayList<OpenhabEndpoint>();
    public final List<OpenhabEndpoint> commandEndpoints = new CopyOnWriteArrayList<OpenhabEndpoint>();
    public final List<OpenhabEndpoint> actionEndpoints = new CopyOnWriteArrayList<OpenhabEndpoint>();
    public final List<OpenhabEndpoint> persistenceEndpoints = new CopyOnWriteArrayList<OpenhabEndpoint>();

    private CamelCallback callback = null;

    public OpenhabComponent(CamelCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {

        // if the provided URI is not understood, let's return null, meaning "don't know how to interpret it".
        if (!uri.startsWith("openhab://")) {
            return null;
        }

        String uriName = uri.substring("openhab://".length());

        logger.info("Creating endpoint " + "uri='{}' " + " remaining='{}' " + "parameters='{}' " + "uriname='{}'", uri,
                remaining, parameters, uriName);

        Configuration configuration = new Configuration();
        setProperties(configuration, parameters);

        Endpoint endpoint = null;

        if (remaining != null) {
            switch (remaining) {
                case ROUTE_STATUS:
                    endpoint = new OpenhabEndpoint(uri, uriName, configuration, callback);
                    statusEndpoints.add((OpenhabEndpoint) endpoint);
                    break;
                case ROUTE_COMMANDS:
                    endpoint = new OpenhabEndpoint(uri, uriName, configuration, callback);
                    commandEndpoints.add((OpenhabEndpoint) endpoint);
                    break;
                case ROUTE_ACTION:
                    endpoint = new OpenhabEndpoint(uri, uriName, configuration, callback);
                    actionEndpoints.add((OpenhabEndpoint) endpoint);
                    break;
                case ROUTE_PERSISTENCE:
                    endpoint = new OpenhabEndpoint(uri, uriName, configuration, callback);
                    persistenceEndpoints.add((OpenhabEndpoint) endpoint);
                    break;
                default:
                    logger.error("Unknow parameter '{}'", remaining);
            }
        }

        return endpoint;
    }

    public void sendItemUpdate(String itemName, String value) {
        for (OpenhabEndpoint endpoint : statusEndpoints) {
            endpoint.sendItemUpdate(itemName, value);
        }
    }

    public void sendItemCommand(String itemName, String value) {
        for (OpenhabEndpoint endpoint : commandEndpoints) {
            endpoint.sendItemCommand(itemName, value);
        }
    }

    public void sendAction(String actionId, Map<String, Object> headers, String message) {
        for (OpenhabEndpoint endpoint : actionEndpoints) {
            endpoint.sendAction(actionId, headers, message);
        }
    }

    public void storeItem(String itemName, String value) {
        for (OpenhabEndpoint endpoint : persistenceEndpoints) {
            endpoint.storeItem(itemName, value);
        }
    }
}

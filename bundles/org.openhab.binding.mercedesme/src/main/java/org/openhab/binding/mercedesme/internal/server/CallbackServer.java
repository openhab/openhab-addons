/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mercedesme.internal.server;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CallbackServer} class defines an HTTP Server for authentication callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CallbackServer {
    private final static Logger logger = LoggerFactory.getLogger(CallbackServer.class);
    private Optional<Server> server = Optional.empty();
    private final int port;
    private final OAuthFactory oAuthFactory;

    public CallbackServer(OAuthFactory oAuthFactory, int port) {
        this.oAuthFactory = oAuthFactory;
        this.port = port;
    }

    public void start() {
        logger.error("Start Callback Server");
        if (!server.isEmpty()) {
            logger.info("Callback server already started");
            return;
        }
        server = Optional.of(new Server());
        ServerConnector connector = new ServerConnector(server.get());
        connector.setPort(port);
        server.get().setConnectors(new Connector[] { connector });
        ServletHandler servletHandler = new ServletHandler();
        server.get().setHandler(servletHandler);
        servletHandler.addServletWithMapping(CallbackServlet.class, Constants.CALLBACK_ENDPOINT);
        try {
            server.get().start();
        } catch (Exception e) {
            logger.error("Cannot start Callback Server {}", e.getMessage());
        }
    }

    public void stop() {
        logger.error("Stop Callback Server");
        try {
            if (!server.isEmpty()) {
                server.get().stop();
                server = Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Cannot stop Callback Server {}", e.getMessage());
        }
    }
}

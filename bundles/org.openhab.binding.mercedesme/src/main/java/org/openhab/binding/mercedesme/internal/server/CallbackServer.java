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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * The {@link CallbackServer} class defines an HTTP Server for authentication callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CallbackServer {
    private Server server;

    // public CallbackServer(@NonNull InetSocketAddress inetSocketAddress) {
    // // TODO Auto-generated constructor stub
    // }

    public void start() {
        server = new Server();
        // SslContextFactory.Server sslServer = new SslContextFactory.Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8090);
        server.setConnectors(new Connector[] { connector });
        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        servletHandler.addServletWithMapping(CallbackServlet.class, "/mb-callback");
        try {
            server.start();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}

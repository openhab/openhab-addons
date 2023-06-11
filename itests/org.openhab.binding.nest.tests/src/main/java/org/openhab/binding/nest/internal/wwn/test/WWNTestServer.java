/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn.test;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Embedded jetty server used in the tests.
 *
 * Based on {@code TestServer} of the FS Internet Radio Binding.
 *
 * @author Velin Yordanov - Initial contribution
 * @author Wouter Born - Increase test coverage
 */
@NonNullByDefault
public class WWNTestServer {
    private @Nullable Server server;
    private String host;
    private int port;
    private int timeout;
    private ServletHolder servletHolder;

    public WWNTestServer(String host, int port, int timeout, ServletHolder servletHolder) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.servletHolder = servletHolder;
    }

    public void startServer() throws Exception {
        Server server = new Server();

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(servletHolder, "/*");
        server.setHandler(handler);

        // HTTP connector
        ServerConnector http = new ServerConnector(server);
        http.setHost(host);
        http.setPort(port);
        http.setIdleTimeout(timeout);
        server.addConnector(http);

        server.start();

        this.server = server;
    }

    public void stopServer() throws Exception {
        Server server = this.server;
        if (server == null) {
            return;
        }

        server.stop();
        this.server = null;
    }
}

/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AuthServer} provides HTTP Server to show servlet content of the authentication process
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AuthServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServer.class);
    private static final Map<Integer, AuthServer> SERVER_MAP = new HashMap<>();
    private static final AccessTokenResponse INVALID_ACCESS_TOKEN = new AccessTokenResponse();

    private final HttpClient httpClient;

    private Optional<Server> server = Optional.empty();
    private AccountConfiguration config;
    public String callbackUrl;

    public AuthServer(HttpClient hc, AccountConfiguration config, String callbackUrl) {
        httpClient = hc;
        SERVER_MAP.put(Integer.valueOf(config.callbackPort), this);
        this.config = config;
        this.callbackUrl = callbackUrl;
        INVALID_ACCESS_TOKEN.setAccessToken(Constants.EMPTY);
    }

    public void dispose() {
        SERVER_MAP.remove(Integer.valueOf(config.callbackPort));
    }

    public boolean start() {
        // avoid real server start for unit tests
        if (server.isPresent() || Constants.JUNIT_SERVER_ADDR.equals(callbackUrl)) {
            return true;
        }
        server = Optional.of(new Server());
        ServerConnector connector = new ServerConnector(server.get());
        connector.setPort(config.callbackPort);
        server.get().setConnectors(new Connector[] { connector });
        ServletHandler servletHandler = new ServletHandler();
        server.get().setHandler(servletHandler);
        servletHandler.addServletWithMapping(AuthServlet.class, Constants.CALLBACK_ENDPOINT);
        try {
            server.get().start();
            return true;
        } catch (Exception e) {
            LOGGER.trace("Cannot start Callback Server for port {}, Error {}", config.callbackPort, e.getMessage());
            server = Optional.empty();
            return false;
        }
    }

    public void stop() {
        try {
            if (server.isPresent()) {
                server.get().stop();
                server = Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.trace("Cannot start Callback Server for port {}, Error {}", config.callbackPort, e.getMessage());
        }
    }

    @Nullable
    public static AuthServer getServer(int port) {
        return SERVER_MAP.get(port);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getRegion() {
        return config.region;
    }
}

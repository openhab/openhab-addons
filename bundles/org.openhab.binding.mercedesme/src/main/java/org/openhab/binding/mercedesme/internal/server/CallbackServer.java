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
import java.util.Locale;
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
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.i18n.LocaleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CallbackServer} class defines an HTTP Server for authentication callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CallbackServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackServer.class);
    private static final Map<Integer, CallbackServer> SERVER_MAP = new HashMap<Integer, CallbackServer>();
    private static final AccessTokenResponse INVALID_ACCESS_TOKEN = new AccessTokenResponse();

    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final String oauthID;

    private Optional<Server> server = Optional.empty();
    private AccessTokenRefreshListener listener;
    private AccountConfiguration config;
    public String callbackUrl;

    public CallbackServer(AccessTokenRefreshListener l, HttpClient hc, AccountConfiguration config, String callbackUrl,
            LocaleProvider lp) {
        listener = l;
        httpClient = hc;
        localeProvider = lp;
        oauthID = Constants.BINDING_ID + "_" + config.email;
        SERVER_MAP.put(Integer.valueOf(config.callbackPort), this);
        this.config = config;
        this.callbackUrl = callbackUrl;
        INVALID_ACCESS_TOKEN.setAccessToken(Constants.EMPTY);
    }

    public void dispose() {
        SERVER_MAP.remove(Integer.valueOf(config.callbackPort));
    }

    public void deleteOAuthServiceAndAccessToken() {
    }

    public boolean start() {
        LOGGER.debug("Start Callback Server for port {}", config.callbackPort);
        if (server.isPresent()) {
            LOGGER.debug("Callback server for port {} already started", config.callbackPort);
            return true;
        }
        server = Optional.of(new Server());
        ServerConnector connector = new ServerConnector(server.get());
        connector.setPort(config.callbackPort);
        server.get().setConnectors(new Connector[] { connector });
        ServletHandler servletHandler = new ServletHandler();
        server.get().setHandler(servletHandler);
        servletHandler.addServletWithMapping(CallbackServlet.class, Constants.CALLBACK_ENDPOINT);
        try {
            server.get().start();
        } catch (Exception e) {
            LOGGER.warn("Cannot start Callback Server for port {}, Error {}", config.callbackPort, e.getMessage());
            return false;
        }
        return true;
    }

    public void stop() {
        LOGGER.debug("Stop Callback Server");
        try {
            if (server.isPresent()) {
                server.get().stop();
                server = Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot start Callback Server for port {}, Error {}", config.callbackPort, e.getMessage());
        }
    }

    @Nullable
    public static CallbackServer getServer(int port) {
        return SERVER_MAP.get(port);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getRegion() {
        return config.region;
    }

    public String getMail() {
        return config.email;
    }

    public Locale getLocale() {
        return localeProvider.getLocale();
    }
}

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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.openhab.binding.mercedesme.internal.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
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
    private static final Map<Integer, OAuthClientService> AUTH_MAP = new HashMap<Integer, OAuthClientService>();
    private static final Map<Integer, CallbackServer> SERVER_MAP = new HashMap<Integer, CallbackServer>();

    private Optional<Server> server = Optional.empty();
    private Optional<AccessTokenResponse> token = Optional.empty();
    private AccessTokenRefreshListener listener;
    private AccountConfiguration config;
    private OAuthClientService oacs;
    private String callbackUrl;

    public CallbackServer(AccessTokenRefreshListener l, HttpClient hc, OAuthFactory oAuthFactory,
            AccountConfiguration config, String callbackUrl) {
        oacs = oAuthFactory.createOAuthClientService(Constants.OAUTH_CLIENT_NAME + config.callbackPort,
                Constants.MB_TOKEN_URL, Constants.MB_AUTH_URL, config.clientId, config.clientSecret, config.getScope(),
                false);
        AUTH_MAP.put(Integer.valueOf(config.callbackPort), oacs);
        SERVER_MAP.put(Integer.valueOf(config.callbackPort), this);
        listener = l;
        this.config = config;
        this.callbackUrl = callbackUrl;
    }

    public String getAuthorizationUrl() {
        try {
            return oacs.getAuthorizationUrl(callbackUrl, null, null);
        } catch (OAuthException e) {
            LOGGER.error("Error creating Authorization URL {}", e.getMessage());
            return Constants.EMPTY;
        }
    }

    public String getScope() {
        return config.getScope();
    }

    public void start() {
        LOGGER.debug("Start Callback Server for port {}", config.callbackPort);
        if (!server.isEmpty()) {
            LOGGER.debug("Callback server for port {} already started", config.callbackPort);
            return;
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
        }
    }

    public void stop() {
        LOGGER.debug("Stop Callback Server");
        try {
            if (!server.isEmpty()) {
                server.get().stop();
                server = Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot start Callback Server for port {}, Error {}", config.callbackPort, e.getMessage());
        }
    }

    public String getToken() {
        if (token.isEmpty()) {
            LOGGER.debug("Token empty - Manual Authorization needed at {}", callbackUrl);
            return Constants.EMPTY;
        } else {
            if (token.get().isExpired(LocalDateTime.now(), 10)) {
                LOGGER.trace("Token expired - start refreshing");
                try {
                    AccessTokenResponse act = oacs.refreshToken();
                    token = Optional.of(act);
                    listener.onAccessTokenResponse(act);
                } catch (OAuthException | IOException | OAuthResponseException e) {
                    LOGGER.warn("Error refreshing token. Reason: {} Manual Authorization needed at {}", e.getMessage(),
                            callbackUrl);
                }
            } else {
                LOGGER.trace("Token valid - do nothing");
            }
        }
        return token.get().getAccessToken();
    }

    /**
     * Function is used to restore last tokenResponse from Persistence.
     * Use case e.g. is startup
     *
     * @param accessTokenResponse
     */
    public void setToken(AccessTokenResponse accessTokenResponse) {
        token = Optional.of(accessTokenResponse);
    }

    /**
     * Static callback for Servlet calls
     *
     * @param port
     * @param code
     */
    public static void callback(int port, String code) {
        LOGGER.trace("Callback from Servlet {} {}", port, code);
        try {
            OAuthClientService oacs = AUTH_MAP.get(port);
            LOGGER.trace("Get token from code {}", code);
            // get CallbackServer instance
            CallbackServer srv = SERVER_MAP.get(port);
            LOGGER.trace("Deliver token to {}", srv);
            if (srv != null && oacs != null) {
                AccessTokenResponse atr = oacs.getAccessTokenResponseByAuthorizationCode(code, srv.callbackUrl);
                srv.token = Optional.of(atr);
                srv.listener.onAccessTokenResponse(atr);
            } else {
                LOGGER.warn("Either Callbackserver  {} or Authorization Service {} not found", srv, oacs);
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            LOGGER.warn("Exception getting token from code {} {}", code, e.getMessage());
        }
    }

    public static String getAuthorizationUrl(int port) {
        CallbackServer srv = SERVER_MAP.get(port);
        if (srv != null) {
            return srv.getAuthorizationUrl();
        } else {
            LOGGER.debug("No Callbackserver found for {}", port);
            return Constants.EMPTY;
        }
    }

    public static String getScope(int port) {
        CallbackServer srv = SERVER_MAP.get(port);
        if (srv != null) {
            return srv.getScope();
        } else {
            LOGGER.debug("No Callbackserver found for {}", port);
            return Constants.EMPTY;
        }
    }
}

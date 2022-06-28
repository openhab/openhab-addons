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
    private final static Logger logger = LoggerFactory.getLogger(CallbackServer.class);

    private AccessTokenRefreshListener listener;
    private Optional<Server> server = Optional.empty();
    private OAuthClientService oacs;
    private Optional<AccessTokenResponse> token = Optional.empty();
    private final static Map<Integer, OAuthClientService> authMap = new HashMap<Integer, OAuthClientService>();
    private final static Map<Integer, CallbackServer> serverMap = new HashMap<Integer, CallbackServer>();

    private AccountConfiguration config;
    private String callbackUrl;

    public CallbackServer(AccessTokenRefreshListener l, HttpClient hc, OAuthFactory oAuthFactory,
            AccountConfiguration config, String callbackUrl) {
        oacs = oAuthFactory.createOAuthClientService(Constants.OAUTH_CLIENT_NAME + config.callbackPort,
                Constants.MB_TOKEN_URL, Constants.MB_AUTH_URL, config.clientId, config.clientSecret, config.getScope(),
                false);
        authMap.put(Integer.valueOf(config.callbackPort), oacs);
        serverMap.put(Integer.valueOf(config.callbackPort), this);
        listener = l;
        this.config = config;
        this.callbackUrl = callbackUrl;
    }

    public String getAuthorizationUrl() {
        try {
            return oacs.getAuthorizationUrl(callbackUrl, null, null);
        } catch (OAuthException e) {
            logger.error("Error creating Authorization URL {}", e.getMessage());
            return Constants.EMPTY;
        }
    }

    public String getScope() {
        return config.getScope();
    }

    public void start() {
        logger.debug("Start Callback Server for port {}", config.callbackPort);
        if (!server.isEmpty()) {
            logger.debug("Callback server for port {} already started", config.callbackPort);
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
            logger.warn("Cannot start Callback Server for port {}, Error {}", config.callbackPort, e.getMessage());
        }
    }

    public void stop() {
        logger.debug("Stop Callback Server");
        try {
            if (!server.isEmpty()) {
                server.get().stop();
                server = Optional.empty();
            }
        } catch (Exception e) {
            logger.warn("Cannot start Callback Server for port {}, Error {}", config.callbackPort, e.getMessage());
        }
    }

    public String getToken() {
        if (token.isEmpty()) {
            logger.debug("Token empty - Manual Authorization needed at {}", callbackUrl);
            return Constants.EMPTY;
        } else {
            if (token.get().isExpired(LocalDateTime.now(), 10)) {
                logger.trace("Token expired - start refreshing");
                try {
                    AccessTokenResponse act = oacs.refreshToken();
                    token = Optional.of(act);
                    listener.onAccessTokenResponse(act);
                } catch (OAuthException | IOException | OAuthResponseException e) {
                    logger.warn("Error refreshing token {}", e.getMessage());
                }
            } else {
                logger.trace("Token valid - do nothing");
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
        logger.trace("Callback from Servlet {} {}", port, code);
        try {
            OAuthClientService oacs = authMap.get(port);
            logger.trace("Get token from code {}", code);
            // get CallbackServer instance
            CallbackServer srv = serverMap.get(port);
            logger.trace("Deliver token to {}", srv);
            if (srv != null && oacs != null) {
                AccessTokenResponse atr = oacs.getAccessTokenResponseByAuthorizationCode(code, srv.callbackUrl);
                srv.token = Optional.of(atr);
                srv.listener.onAccessTokenResponse(atr);
            } else {
                logger.warn("Either Cllbackserver {} or Authorization Service {} not found", srv, oacs);
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.warn("Exception getting token from code {} {}", code, e.getMessage());
        }
    }

    public static String getAuthorizationUrl(int port) {
        CallbackServer srv = serverMap.get(port);
        if (srv != null) {
            return srv.getAuthorizationUrl();
        } else {
            logger.debug("No Callbackserver found for {}", port);
            return Constants.EMPTY;
        }
    }

    public static String getScope(int port) {
        CallbackServer srv = serverMap.get(port);
        if (srv != null) {
            return srv.getScope();
        } else {
            logger.debug("No Callbackserver found for {}", port);
            return Constants.EMPTY;
        }
    }
}

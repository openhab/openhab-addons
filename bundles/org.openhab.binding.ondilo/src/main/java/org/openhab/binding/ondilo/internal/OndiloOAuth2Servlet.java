/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ondilo.internal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OndiloHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author MikeTheTux - Initial contribution
 */
@Component(service = OndiloOAuth2Servlet.class, immediate = true)
@NonNullByDefault
public class OndiloOAuth2Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(OndiloOAuth2Servlet.class);

    private static final String SERVLET_ALIAS = "/ondilo/oauth2callback"; // The URL path for the servlet

    // The HttpService is used to register the servlet in the OSGi environment
    @Reference
    private @Nullable HttpService httpService;

    private @Nullable OAuthFactory oAuthFactory;

    @Reference
    public void setOAuthFactory(OAuthFactory oAuthFactory) {
        this.oAuthFactory = oAuthFactory;
    }

    // This method is called when the OSGi component is activated
    protected void activate() {
        try {
            HttpService httpService = this.httpService;
            if (httpService != null) {
                httpService.registerServlet(SERVLET_ALIAS, this, null, null);
                logger.trace("OndiloOAuth2Servlet registered at {}", SERVLET_ALIAS);
            } else {
                logger.error("HttpService is not available. Cannot register OndiloOAuth2Servlet.");
                return;
            }
        } catch (ServletException | NamespaceException e) {
            logger.error("Failed to register OndiloOAuth2Servlet", e);
        }
    }

    // This method is called when the OSGi component is deactivated
    protected void deactivate() {
        HttpService httpService = this.httpService;
        if (httpService != null) {
            httpService.unregister(SERVLET_ALIAS);
            httpService = null;
            logger.trace("OndiloOAuth2Servlet unregistered from {}", SERVLET_ALIAS);
        }
    }

    private void sendError(HttpServletResponse response, int statusCode, String logMessage, String clientMessage)
            throws IOException {
        logger.error("{}", logMessage);
        response.sendError(statusCode, clientMessage);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        OAuthFactory oAuthFactory = this.oAuthFactory;
        if (request == null || response == null || oAuthFactory == null) {
            String logMsg = "Received null request or response in OndiloOAuth2Servlet";
            logger.error(logMsg);
            if (response != null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            }
            return;
        }
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String error = request.getParameter("error");

        if (error != null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "OAuth2 error received: " + error,
                    "OAuth2 Error: " + error);
            return;
        }

        if (code != null && state != null) {
            logger.trace("Received OAuth2 callback with code: {}... and state: {}", code.substring(0, 5), state);
            OndiloBridgeHandler onlidoBridgeHander = OndiloBridgeHandler.getOAuthServiceByState(state);
            OAuthClientService oAuthService = oAuthFactory.getOAuthClientService(state);

            StringBuffer url = request.getRequestURL();
            String queryString = request.getQueryString();
            String fullUrl = ((url != null) ? url.toString() : "") + ((queryString != null) ? "?" + queryString : "");

            if (oAuthService != null && onlidoBridgeHander != null) {
                try {
                    String authorizationCode = oAuthService.extractAuthCodeFromAuthResponse(fullUrl);
                    logger.trace("Authorization code extracted successfully: {}...", authorizationCode.substring(0, 5));
                    onlidoBridgeHander.onOAuth2Authorized(authorizationCode, oAuthService);

                    try (PrintWriter writer = response.getWriter()) {
                        writer.println("<h1>Ondilo OAuth2 Authentication Successful!</h1>");
                        writer.println("<p>You can now close this window and return to openHAB</p>");
                    }
                } catch (OAuthException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Failed to extract authorization code from OAuth2 response: " + e.getMessage(),
                            "Invalid OAuth2 response");
                    return;
                }
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "OAuthClientService is not available. Cannot process OAuth2 callback.",
                        "OAuthClientService not available");
            }
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid oAuth2 response. Missing 'code' or 'state' parameter",
                    "Invalid OAuth2 response. Missing 'code' or 'state' parameter");
        }
    }
}

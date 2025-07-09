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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.common.lang.Nullable;

/**
 * The {@link OndiloHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author MikeTheTux - Initial contribution
 */
@Component(service = OndiloOAuth2Servlet.class, immediate = true)
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
            httpService.registerServlet(SERVLET_ALIAS, this, null, null);
            logger.info("OndiloOAuth2Servlet registered at {}", SERVLET_ALIAS);
        } catch (ServletException | NamespaceException e) {
            logger.error("Failed to register OndiloOAuth2Servlet", e);
        }
    }

    // This method is called when the OSGi component is deactivated
    protected void deactivate() {
        httpService.unregister(SERVLET_ALIAS);
        logger.info("OndiloOAuth2Servlet unregistered from {}", SERVLET_ALIAS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String error = request.getParameter("error");

        if (error != null) {
            logger.error("OAuth2 error received: {}", error);
            response.getWriter().println("OAuth2 Error: " + error);
            return;
        }

        if (code != null && state != null) {
            // This is the callback from the OAuth2 provider
            logger.trace("Received OAuth2 callback with code: {}... and state: {}", code.substring(0, 5), state);
            OndiloBridgeHandler onlidoBridgeHander = OndiloBridgeHandler.getOAuthServiceByState(state);
            OAuthClientService oAuthService = oAuthFactory.getOAuthClientService(state);

            StringBuffer url = request.getRequestURL(); // protocol + server + port + path
            String queryString = request.getQueryString(); // parameters
            String fullUrl = url.toString();
            if (queryString != null) {
                fullUrl += "?" + queryString;
            }

            if (oAuthService != null && onlidoBridgeHander != null) {
                try {
                    String authorizationCode = oAuthService.extractAuthCodeFromAuthResponse(fullUrl);
                    logger.trace("Authorization code extracted successfully: {}...", authorizationCode.substring(0, 5));
                    onlidoBridgeHander.onOAuth2Authorized(authorizationCode, oAuthService);

                    response.getWriter().println("<h1>Ondilo oAuth2 Authentication Successful!</h1>");
                    response.getWriter().println("<p>You can now close this window and return to openHAB</p>");
                } catch (OAuthException e) {
                    logger.error("Failed to extract authorization code from OAuth2 response", e);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid OAuth2 response");
                    return;
                }
            } else {
                // If the OAuthClientService is not available, we cannot process the callback
                logger.error("OAuthClientService is not available. Cannot process OAuth2 callback.");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuthClientService not available");
            }
        } else {
            logger.error("Invalid Request");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request. Missing 'code' or 'state' parameters");
        }
    }
}

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
package org.openhab.binding.tado.internal.servlet;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.auth.OAuthorizerV2;
import org.openhab.binding.tado.internal.handler.TadoHomeHandler;
import org.openhab.binding.tado.swagger.codegen.api.auth.Authorizer;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;

/**
 * The {@link TadoAuthenticationServlet} manages the authorization with the Tado API.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class TadoAuthenticationServlet extends HttpServlet {
    private static final long serialVersionUID = 19530226123451L;

    public static final String PATH = "/tado";

    private static final String REPLACE_TAG = "$REPLACE$";

    private static final String HTML_AUTH_PASSED = "<span style=\"color: #00cc00\">authenicated</span>";
    private static final String HTML_AUTH_NOT_REQUIRED = "<span style=\"color: #808080\">authentication not required</span>";
    private static final String HTML_AUTH_ERROR_TEMPLATE = "<span style=\"color: #ff0000\">$REPLACE$</span>";
    private static final String HTML_AUTH_START_TEMPLATE = "<a href=\"$REPLACE$\"><span style=\"color: #cc3300\">click to authenticate</span></a>";

    private static final String PARAM_NAME = "oauth";
    private static final String PARAM_VALUE = "start";

    private static final String ERROR_BAD_URL = "no user authentication uri";

    private static final String HTML_STATUS_PAGE_TEMPLATE = """
                <html>
                    <body>
                        <h2 style="font-family: Arial">tado°</h2>
                        <p style="font-family: Arial">Status: $REPLACE$</p>
                    </body>
                </html>
            """;

    private final TadoHomeHandler tadoHomeHandler;

    public TadoAuthenticationServlet(TadoHomeHandler tadoHomeHandler) {
        this.tadoHomeHandler = tadoHomeHandler;
    }

    /**
     * Fulfils HTTP GET requests.
     *
     * Depending on whether the request has a query string, the behavior differs as follows:
     *
     * <ul>
     * <li>If there is no query string, it serves the status page</li>
     * <li>If there is a given query string, it serves the user authentication page</li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        OAuthorizerV2 authorizerV2 = tadoHomeHandler.getApi().getAuthorizerV2();

        if (request.getQueryString() == null) {
            serveStatusPage(request, response, authorizerV2);
        } else

        // if the query string is '?oauth=start' then serve the user authentication page
        if (PARAM_VALUE.equals(request.getParameter(PARAM_NAME))) {
            if (authorizerV2 == null) {
                serveStatusPage(request, response, authorizerV2);
            } else {
                serveUserAuthenticationPage(response, authorizerV2);
            }
        }
    }

    /**
     * Serves the status page.
     *
     * Depending on the type of {@link Authorizer} it serves three flavours of page:
     *
     * <ul>
     * <li>A page saying that authentication is not required</li>
     * <li>A page saying that authentication is already done</li>
     * <li>A page saying that authentication is required with a link to start the process</li>
     * </ul>
     *
     * @throws IOException
     */
    private void serveStatusPage(HttpServletRequest request, HttpServletResponse response,
            @Nullable OAuthorizerV2 authorizerV2) throws IOException {
        String dynamicHtml = null;

        if (authorizerV2 == null) {
            dynamicHtml = HTML_AUTH_NOT_REQUIRED;
        }

        if (dynamicHtml == null) {
            try {
                AccessTokenResponse accessToken = Objects.requireNonNull(authorizerV2).getAccessTokenResponse();
                if (accessToken != null) {
                    dynamicHtml = HTML_AUTH_PASSED;
                }
            } catch (OAuthException | IOException | OAuthResponseException e) {
                dynamicHtml = HTML_AUTH_ERROR_TEMPLATE.replace(REPLACE_TAG,
                        e.getMessage() instanceof String exception ? exception : e.getClass().getName());
            }
        }

        if (dynamicHtml == null) {
            if (request.getRequestURL() instanceof StringBuffer baseUrl) {
                String dynamicUrl = baseUrl.append("?").append(PARAM_NAME).append("=").append(PARAM_VALUE).toString();
                dynamicHtml = HTML_AUTH_START_TEMPLATE.replace(REPLACE_TAG, dynamicUrl);
            } else {
                dynamicHtml = HTML_AUTH_ERROR_TEMPLATE.replace(REPLACE_TAG, ERROR_BAD_URL);
            }
        }

        String content = HTML_STATUS_PAGE_TEMPLATE.replace(REPLACE_TAG, dynamicHtml);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.TEXT_HTML);
        response.getWriter().write(content);
    }

    /**
     * Serves the user authentication page.
     *
     * Tries to get the Tado remote RFC-8628 Authentication User URI from the authentication
     * service, which if valid is opened via an HTTP redirect. Or if the URI is empty, or the
     * authentication failed, it serves a local error page instead.
     *
     * @throws IOException
     */
    private void serveUserAuthenticationPage(HttpServletResponse response, OAuthorizerV2 authorizerV2)
            throws IOException {
        String dynamicHtml = null;

        try {
            String userAuthenticationUri = authorizerV2.getUserAuthenticationUri();
            if (userAuthenticationUri != null && !userAuthenticationUri.isBlank()) {
                response.sendRedirect(userAuthenticationUri);
            } else {
                dynamicHtml = HTML_AUTH_ERROR_TEMPLATE.replace(REPLACE_TAG, ERROR_BAD_URL);
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            dynamicHtml = HTML_AUTH_ERROR_TEMPLATE.replace(REPLACE_TAG,
                    e.getMessage() instanceof String message ? message : e.getClass().getName());
        }

        if (dynamicHtml != null) {
            String content = HTML_STATUS_PAGE_TEMPLATE.replace(REPLACE_TAG, dynamicHtml);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.TEXT_HTML);
            response.getWriter().write(content);
        }
    }
}

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tado.internal.handler.TadoHandlerFactory;
import org.openhab.core.auth.client.oauth2.DeviceCodeResponseDTO;
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

    public static final String PARAM_NAME_USER = "user";
    private static final String PARAM_NAME_OAUTH = "oauth";
    private static final String PARAM_VALUE_START = "start";

    private static final String ERROR_BAD_URL = "no verification uri";

    private static final String HTML_STATUS_PAGE_TEMPLATE = """
            <html>
                <body>
                    <h2 style="font-family: Arial; text-align: center">tado°</h2>
                    <p style="font-family: Arial; text-align: center">Status: $REPLACE$</p>
                </body>
            </html>
            """;

    private final TadoHandlerFactory tadoHandlerFactory;

    public TadoAuthenticationServlet(TadoHandlerFactory tadoHandlerFactory) {
        this.tadoHandlerFactory = tadoHandlerFactory;
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
        // if the query string contains "oauth=start" then serve the user authentication page
        if (PARAM_VALUE_START.equals(request.getParameter(PARAM_NAME_OAUTH))
                && tadoHandlerFactory.hasOAuthClientService(request.getParameter(PARAM_NAME_USER))) {
            serveUserAuthenticationPage(request, response);
        } else {
            serveStatusPage(request, response);
        }
    }

    /**
     * Serves the status page.
     *
     * Depending on the type of authentication, it serves three flavours of page:
     *
     * <ul>
     * <li>A page saying that authentication is not required</li>
     * <li>A page saying that authentication is already done</li>
     * <li>A page saying that authentication is required with a link to start the process</li>
     * </ul>
     *
     * @throws IOException
     */
    private void serveStatusPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String dynamicHtml = null;

        if (!tadoHandlerFactory.hasOAuthClientService(request.getParameter(PARAM_NAME_USER))) {
            dynamicHtml = HTML_AUTH_NOT_REQUIRED;
        }

        if (dynamicHtml == null) {
            try {
                if (tadoHandlerFactory.getAccessTokenResponse(request.getParameter(PARAM_NAME_USER)) != null) {
                    dynamicHtml = HTML_AUTH_PASSED;
                }
            } catch (OAuthException | OAuthResponseException e) {
                // error already logged => fall through
            }
        }

        if (dynamicHtml == null) {
            if (request.getRequestURL() instanceof StringBuffer baseUrl) {
                String dynamicUrl = baseUrl.append("?").append(PARAM_NAME_OAUTH).append("=").append(PARAM_VALUE_START)
                        .append("&").append(PARAM_NAME_USER).append("=").append(request.getParameter(PARAM_NAME_USER))
                        .toString();
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
    private void serveUserAuthenticationPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String dynamicHtml = null;

        try {
            DeviceCodeResponseDTO deviceCodeResponse = tadoHandlerFactory
                    .getDeviceCodeResponse(request.getParameter(PARAM_NAME_USER));
            String userVerificationUri = deviceCodeResponse.getVerificationUriComplete();
            if (userVerificationUri != null && !userVerificationUri.isBlank()) {
                response.sendRedirect(userVerificationUri);
            } else {
                dynamicHtml = HTML_AUTH_ERROR_TEMPLATE.replace(REPLACE_TAG, ERROR_BAD_URL);
            }
        } catch (OAuthException e) {
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

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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.auth.OAuthAuthorizerV2;
import org.openhab.binding.tado.internal.handler.TadoHomeHandler;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;
import org.openhab.binding.tado.swagger.codegen.api.auth.Authorizer;

/**
 * The {@link TadoAuthenticationServlet} manages the authorization with the Tado API.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class TadoAuthenticationServlet extends HttpServlet {

    public static final String PATH = "/tado";

    private static final long serialVersionUID = 19530226123451L;

    private static final String REPLACE_TAG = "$REPLACE$";

    private static final String HTML_STATUS_PAGE_TEMPLATE = """
                <html>
                    <body>
                        <h1 style="font-family: Arial"><i>°tado</i> Authentication Servlet</h1>
                        <p>&nbsp;</p>
                        <h3 style="font-family: Arial">Status: $REPLACE$</h3>
                    </body>
                </html>
            """;

    private static final String HTML_AUTH_IS_AUTHENTICATED = "<span style=\"color: #00cc00\">Authenicated</span>";
    private static final String HTML_AUTH_NOT_APPLICABLE = "<span style=\"color: #808080\">Authentication not required</span>";
    private static final String HTML_AUTH_ERROR_TEMPLATE = "<span style=\"color: #ff0000\">$REPLACE$</span>";
    private static final String HTML_AUTH_START_TEMPLATE = "<a href=\"$REPLACE$\"><span style=\"color: #cc3300\">Click Here to Authenticate</span></a>";

    private static final String PARAM_NAME = "oauth";
    private static final String PARAM_VALUE = "start";

    private static final String NO_URL = "(error no url)";

    private final TadoHomeHandler tadoHomeHandler;

    public TadoAuthenticationServlet(TadoHomeHandler tadoHomeHandler) {
        this.tadoHomeHandler = tadoHomeHandler;
    }

    /**
     * Fulfils Http GET requests. Depending on whether the request has a query string, the behavior differs as follows:
     * <ul>
     * <li>If there is no query string, it serves a status page</li>
     * <li>If there is a specific query string, it redirects to tado's authentication server</li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        OAuthAuthorizerV2 authorizerV2 = tadoHomeHandler.getApi().getOAuthAuthorizerV2();

        if (request.getQueryString() == null) {
            getStatusPage(request, response, authorizerV2);
        } else

        // query string '?oauth=start' => redirect to oauth authentication page
        if (PARAM_VALUE.equals(request.getParameter(PARAM_NAME))) {
            if (authorizerV2 == null) {
                getStatusPage(request, response, authorizerV2);
            } else {
                String redirectUri = null;
                try {
                    redirectUri = authorizerV2.startAuthentication();
                } catch (ApiException e) {
                }
                if (redirectUri != null) {
                    response.sendRedirect(redirectUri);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Authentication start error");
                }
            }
        }
    }

    /**
     * Serves up a status page. Depending on the type of {@link Authorizer} it serves three flavors of page:
     * <ul>
     * <li>A page saying that authentication is not required</li>
     * <li>A page saying that authentication is already done</li>
     * <li>A page saying that authentication is required with a link to start the process</li>
     * </ul>
     */
    private void getStatusPage(HttpServletRequest request, HttpServletResponse response,
            @Nullable OAuthAuthorizerV2 authorizerV2) throws IOException {
        String dynamicHtml;

        if (authorizerV2 == null) {
            dynamicHtml = HTML_AUTH_NOT_APPLICABLE;
        } else if (authorizerV2.isAuthenticated()) {
            dynamicHtml = HTML_AUTH_IS_AUTHENTICATED;
        } else {
            if (request.getRequestURL() instanceof StringBuffer baseUrl) {
                String dynamicUrl = baseUrl.append("?").append(PARAM_NAME).append("=").append(PARAM_VALUE).toString();
                dynamicHtml = HTML_AUTH_START_TEMPLATE.replace(REPLACE_TAG, dynamicUrl);
            } else {
                dynamicHtml = HTML_AUTH_ERROR_TEMPLATE.replace(REPLACE_TAG, NO_URL);
            }
        }

        String content = HTML_STATUS_PAGE_TEMPLATE.replace(REPLACE_TAG, dynamicHtml);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.TEXT_HTML);
        response.getWriter().write(content);
    }
}

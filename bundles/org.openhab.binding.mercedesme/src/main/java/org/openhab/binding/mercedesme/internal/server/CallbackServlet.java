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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CallbackServlet} class provides authentication callback endpoint
 *
 * @author Bernd Weymann - Initial contribution
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class CallbackServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(CallbackServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter(Constants.CODE);
        if (code != null) {
            CallbackServer.callback(request.getLocalPort(), code);
            logger.trace("Code successfully extracted {}", request.getParameterMap());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(request.getParameterMap());
            response.getWriter().println("{ \"status\": \"ok\"}");
        } else {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<HTML>");
            response.getWriter().println("<BODY>");
            response.getWriter().println("<B>Call Parameters</B>");
            response.getWriter().println("<BR>");
            response.getWriter().println(request.getParameterMap());
            response.getWriter().println("<BR><BR>");
            response.getWriter().println("<B>Configured scopes</B><BR>");
            String[] scopes = CallbackServer.getScope(request.getLocalPort()).split(Constants.SPACE);
            for (int i = 0; i < scopes.length; i++) {
                response.getWriter().println(scopes[i] + "<BR>");
            }
            response.getWriter().println("<BR><BR>");
            response.getWriter().println("<B>Get your access token for openHAB MercedesMe Binding</B>");
            response.getWriter().println("<BR>");
            response.getWriter().println("<a href=\"" + CallbackServer.getAuthorizationUrl(request.getLocalPort())
                    + "\">Start Authorization</a>");
            response.getWriter().println("</BODY>");
            response.getWriter().println("</HTML>");
        }
        logger.debug("Call from {}:{} parameters {}", request.getLocalAddr(), request.getLocalPort(),
                request.getParameterMap());
    }
}

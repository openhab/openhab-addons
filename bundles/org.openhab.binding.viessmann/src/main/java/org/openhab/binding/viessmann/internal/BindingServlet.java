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
package org.openhab.binding.viessmann.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link BindingServlet} provides the redirectUri for OAuth.
 *
 * @author Ronny Grun - Initial Contribution
 */
@NonNullByDefault
public class BindingServlet extends HttpServlet {
    private static final long serialVersionUID = -1936912347819461265L;

    private final Logger logger = LoggerFactory.getLogger(BindingServlet.class);

    String servletUrlWithoutRoot;
    String servletUrl;
    HttpService httpService;

    final List<Thing> accountHandlers = new ArrayList<>();

    public BindingServlet(HttpService httpService) {
        this.httpService = httpService;
        servletUrlWithoutRoot = "viessmann/authcode";
        servletUrl = "/" + servletUrlWithoutRoot;
        try {
            httpService.registerServlet(servletUrl, this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.warn("Register servlet fails. {}", e.getMessage());
        }
    }

    public void dispose() {
        httpService.unregister(servletUrl);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }
        String requestUri = req.getRequestURI();
        if (requestUri == null) {
            return;
        }

        String queryString = req.getQueryString();

        String code;
        String error = "{\"error\": \"invalid-code-request\"}";
        String errorCallUriFromBrowser = "You are attempting to authenticate via your browser. "
                + "The viessmann binding does not support this. Authentication is handled automatically by the binding.";

        StringBuilder html = new StringBuilder();
        int codeEnd;
        if (queryString != null && queryString.contains("code=")) {
            if (!queryString.contains("&")) {
                codeEnd = queryString.length();
            } else {
                codeEnd = queryString.indexOf("&");
            }
            code = queryString.substring(queryString.indexOf("code=") + 5, codeEnd);
            logger.debug("doGet Authcode: {}", code);

            html.append("{\"code\": \"");
            html.append(code);
            html.append("\"}");
        } else {
            String ua = req.getHeader("User-Agent");
            logger.trace("User-Agent: {}", ua);

            if (!ua.contains("Jetty")) {
                html.append(errorCallUriFromBrowser);
                logger.warn("doGet Authcode warn: {}", errorCallUriFromBrowser);
            } else {
                html.append(error);
                logger.debug("doGet Authcode error: {}", error);
            }
        }

        resp.addHeader("content-type", "application/json");
        try {
            resp.getWriter().write(html.toString());
        } catch (IOException e) {
            logger.warn("Return html failed with uri syntax error. {}", e.getMessage());
        }
    }
}

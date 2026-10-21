/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.volvocars.internal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvocars.internal.api.VolvoCarsException;
import org.openhab.binding.volvocars.internal.handler.AccountHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves the page to authorize openHAB to access a Volvo ID account and receives the OAuth redirect.
 * <p>
 * If the redirect URI registered for the application does not point to this servlet, the user can paste the
 * URL the browser was redirected to into the form on this page instead.
 *
 * @author Michael Weger - Initial contribution
 */
@Component(service = { Servlet.class, VolvoCarsAuthServlet.class }, property = {
        HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=" + VolvoCarsAuthServlet.SERVLET_PATH })
@NonNullByDefault
public class VolvoCarsAuthServlet extends HttpServlet {
    public static final String SERVLET_PATH = "/volvocars";
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(VolvoCarsAuthServlet.class);
    private final Set<AccountHandler> accounts = ConcurrentHashMap.newKeySet();

    @Deactivate
    protected void deactivate() {
        accounts.clear();
    }

    public void register(AccountHandler handler) {
        accounts.add(handler);
    }

    public void unregister(AccountHandler handler) {
        accounts.remove(handler);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (request == null || response == null) {
            return;
        }
        String error = request.getParameter("error");
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String authorize = request.getParameter("authorize");

        if (error != null) {
            String description = request.getParameter("error_description");
            writePage(response, "Authorization failed",
                    "<p>Volvo ID returned: " + escape(error) + (description != null ? " - " + escape(description) : "")
                            + "</p><p><a href=\"" + SERVLET_PATH + "\">Back</a></p>");
        } else if (code != null && state != null) {
            handleCallback(response, code, state);
        } else if (authorize != null) {
            Optional<AccountHandler> account = accounts.stream()
                    .filter(handler -> handler.getThing().getUID().getAsString().equals(authorize)).findFirst();
            if (account.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown account");
                return;
            }
            try {
                response.sendRedirect(account.get().createAuthorizationUrl());
            } catch (VolvoCarsException e) {
                response.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
            }
        } else {
            writeIndexPage(response);
        }
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (request == null || response == null) {
            return;
        }
        Map<String, String> parameters = parseQuery(request.getParameter("redirectedUrl"));
        String code = parameters.get("code");
        String state = parameters.get("state");
        if (code == null || state == null) {
            writePage(response, "Authorization failed",
                    "<p>The URL does not contain an authorization code.</p><p><a href=\"" + SERVLET_PATH
                            + "\">Back</a></p>");
            return;
        }
        handleCallback(response, code, state);
    }

    private void handleCallback(HttpServletResponse response, String code, String state) throws IOException {
        Optional<AccountHandler> account = accounts.stream().filter(handler -> handler.isPendingAuthorization(state))
                .findFirst();
        if (account.isEmpty()) {
            writePage(response, "Authorization failed",
                    "<p>Unknown or expired authorization request. Please start again.</p><p><a href=\"" + SERVLET_PATH
                            + "\">Back</a></p>");
            return;
        }
        AccountHandler handler = account.get();
        try {
            handler.authorize(code, state);
            writePage(response, "Authorization successful",
                    "<p>openHAB is now authorized to access the Volvo account of <b>"
                            + escape(String.valueOf(handler.getThing().getLabel()))
                            + "</b>. You can close this window.</p>");
        } catch (VolvoCarsException e) {
            logger.debug("Authorization of {} failed: {}", handler.getThing().getUID(), e.getMessage());
            writePage(response, "Authorization failed", "<p>" + escape(String.valueOf(e.getMessage()))
                    + "</p><p><a href=\"" + SERVLET_PATH + "\">Back</a></p>");
        }
    }

    private void writeIndexPage(HttpServletResponse response) throws IOException {
        StringBuilder body = new StringBuilder();
        if (accounts.isEmpty()) {
            body.append("<p>No Volvo Cars account Thing is configured.</p>");
        } else {
            body.append("<ul>");
            accounts.stream().sorted(
                    (a, b) -> a.getThing().getUID().getAsString().compareTo(b.getThing().getUID().getAsString()))
                    .forEach(handler -> {
                        String uid = handler.getThing().getUID().getAsString();
                        body.append("<li>").append(escape(String.valueOf(handler.getThing().getLabel()))).append(" (")
                                .append(escape(uid)).append("): ")
                                .append(handler.isAuthorized() ? "authorized" : "not authorized")
                                .append(" - <a href=\"").append(SERVLET_PATH).append("?authorize=").append(escape(uid))
                                .append("\">").append(handler.isAuthorized() ? "Authorize again" : "Authorize")
                                .append("</a></li>");
                    });
            body.append("</ul>");
        }
        body.append("<h2>Redirect not received?</h2>").append(
                "<p>If your browser was redirected to an address that does not reach openHAB, copy the complete URL ")
                .append("from the address bar and paste it here:</p>").append("<form method=\"post\" action=\"")
                .append(SERVLET_PATH).append("\"><input type=\"text\" name=\"redirectedUrl\" size=\"80\"> ")
                .append("<input type=\"submit\" value=\"Submit\"></form>");
        writePage(response, "Volvo Cars Authorization", body.toString());
    }

    private void writePage(HttpServletResponse response, String title, String body) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>" + escape(title)
                    + "</title></head><body><h1>" + escape(title) + "</h1>" + body + "</body></html>");
        }
    }

    static Map<String, String> parseQuery(@Nullable String url) {
        Map<String, String> parameters = new HashMap<>();
        if (url == null || url.isBlank()) {
            return parameters;
        }
        String query;
        try {
            query = new URI(url.trim()).getRawQuery();
        } catch (URISyntaxException e) {
            return parameters;
        }
        if (query == null) {
            return parameters;
        }
        for (String pair : query.split("&")) {
            int index = pair.indexOf('=');
            if (index > 0) {
                parameters.put(URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8));
            }
        }
        return parameters;
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&#39;");
    }
}

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
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.BINDING_NAME;

import java.io.IOException;
import java.net.URLEncoder;
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
import org.unbescape.html.HtmlEscape;

/**
 * This servlet provides the base navigation page, with hyperlinks for the defined account things
 *
 * @author Michael Geramb - Initial Contribution
 */
@NonNullByDefault
public class BindingServlet extends HttpServlet {
    private static final long serialVersionUID = -1453738923337413163L;

    private final Logger logger = LoggerFactory.getLogger(BindingServlet.class);

    String servletUrlWithoutRoot;
    String servletUrl;
    HttpService httpService;

    List<Thing> accountHandlers = new ArrayList<>();

    public BindingServlet(HttpService httpService) {
        this.httpService = httpService;
        servletUrlWithoutRoot = "amazonechocontrol";
        servletUrl = "/" + servletUrlWithoutRoot;
        try {
            httpService.registerServlet(servletUrl, this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.warn("Register servlet fails", e);
        }
    }

    public void addAccountThing(Thing accountThing) {
        synchronized (accountHandlers) {
            accountHandlers.add(accountThing);
        }
    }

    public void removeAccountThing(Thing accountThing) {
        synchronized (accountHandlers) {
            accountHandlers.remove(accountThing);
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
        String uri = requestUri.substring(servletUrl.length());
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            uri += "?" + queryString;
        }
        logger.debug("doGet {}", uri);

        if (!"/".equals(uri)) {
            String newUri = req.getServletPath() + "/";
            resp.sendRedirect(newUri);
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>" + HtmlEscape.escapeHtml4(BINDING_NAME) + "</title><head><body>");
        html.append("<h1>" + HtmlEscape.escapeHtml4(BINDING_NAME) + "</h1>");

        synchronized (accountHandlers) {
            if (accountHandlers.isEmpty()) {
                html.append("No Account thing created.");
            } else {
                for (Thing accountHandler : accountHandlers) {
                    String url = URLEncoder.encode(accountHandler.getUID().getId(), "UTF8");
                    html.append("<a href='./" + url + " '>" + HtmlEscape.escapeHtml4(accountHandler.getLabel())
                            + "</a><br>");
                }
            }
        }
        html.append("</body></html>");

        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(html.toString());
        } catch (IOException e) {
            logger.warn("return html failed with uri syntax error", e);
        }
    }
}

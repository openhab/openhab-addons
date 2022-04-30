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
package org.openhab.binding.jellyfin.internal.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jellyfin.sdk.api.client.exception.ApiClientException;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.jellyfin.internal.handler.JellyfinServerHandler;
import org.openhab.binding.jellyfin.internal.util.SyncCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JellyfinBridgeServlet} is responsible for handling user login.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class JellyfinBridgeServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(JellyfinBridgeServlet.class);
    private static final long serialVersionUID = 2157912759968949550L;
    private final JellyfinServerHandler server;

    public JellyfinBridgeServlet(JellyfinServerHandler server) {
        this.server = server;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestUri = req.getRequestURI();
        if (requestUri == null) {
            return;
        }
        String user = req.getParameter("username");
        String password = req.getParameter("password");
        if (user != null && password != null && !user.isBlank() && !password.isBlank()) {
            try {
                server.updateCredentials(server.login(user, password));
            } catch (SyncCallback.SyncCallbackError | ApiClientException e) {
                logger.warn("Server error while login: {}", e.getMessage());
            }
        }
        String newUri = req.getServletPath() + "/";
        resp.sendRedirect(newUri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestUri = req.getRequestURI();
        if (requestUri == null) {
            return;
        }
        String uri = requestUri.substring(this.getServletContext().getContextPath().length());
        logger.debug("doGet {}", uri);
        if (!uri.endsWith("/")) {
            String newUri = req.getServletPath() + "/";
            resp.sendRedirect(newUri);
            return;
        }
        String serverUrl = server.getServerUrl();
        String label = server.getThing().getLabel();
        String serverName = label != null ? label : "Jellyfin Binding";
        boolean online = server.isOnline();
        boolean authenticated = online && server.isAuthenticated();
        String html = renderPage(serverUrl, serverName, online, authenticated);
        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(html);
        } catch (IOException e) {
            logger.warn("return html failed with uri syntax error", e);
        }
    }

    @NotNull
    private String renderPage(String serverUrl, String serverName, boolean online, boolean authenticated) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>OpenHAB Jellyfin Binding</title><style>");
        // css
        html.append(
                "*{box-sizing:border-box}body{background-color:#101010;font-family:Arial,sans-serif;padding:50px}.container{margin:20px auto;padding:10px;padding-bottom:0px;width:300px;background-color:#fff;border-radius:5px}h1{color:#777;font-size:32px;margin:15px auto;text-align:center}form{text-align:center}input{padding:12px 0;margin-bottom:10px;border-radius:3px;border:2px solid transparent;text-align:center;width:90%;font-size:16px;transition:border .2s,background-color .2s}form .field{background-color:#ecf0f1}form .field:focus{border:2px solid #3498db}form .btn{background-color:#00a4dc;color:#fff;line-height:25px;cursor:pointer}form .btn:active,form .btn:hover{background-color:#1f78b4;border:2px solid #1f78b4}.pass-link{text-align:center}.pass-link a:link,.pass-link a:visited{font-size:12px;color:#777} .status{padding-bottom: 18px;}");
        html.append(
                ".oh-logo{background-image: url(/images/openhab-logo.svg);background-size: 89px;background-repeat: no-repeat;height: 44px; width: 144px; margin-left: 40px;}");
        html.append(".logo{background-image: url(").append(serverUrl).append(
                "/web/assets/img/banner-light.png);background-size: 140px;margin: 10px auto;background-repeat: no-repeat;width: 44px;height: 44px;}");
        html.append("</style></head><body>");
        // open container
        html.append("<div class=\"container\">");
        // add logos and title
        html.append("<h2 class=\"logo\"><p class=\"oh-logo\"><p></h2><h1>").append(serverName).append("</h1>");
        if (online) {
            if (!authenticated) {
                // add form
                html.append(
                        "<form action=\"#\" method=\"POST\"><input name=\"username\" type=\"text\" placeholder=\"username\" class=\"field\"><input name=\"password\" type=\"password\" placeholder=\"password\" class=\"field\"><input type=\"submit\" value=\"Login\" class=\"btn\"></form>");
            } else {
                html.append("<h1 class=\"status\">✅ Connected</h1>");
            }
        } else {
            html.append("<h1 class=\"status\">❌ Offline</h1>");
        }
        // close container
        html.append("</div>");
        // add server link
        html.append("<div class=\"pass-link\"><a target=\"_blank\" href=\"").append(serverUrl)
                .append("\" >Server Url: ").append(serverUrl).append("</a></div>");
        html.append("</body></html>");
        return html.toString();
    }
}

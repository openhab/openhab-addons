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
package org.openhab.binding.groheondus.internal;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.groheondus.internal.handler.GroheOndusAccountHandler;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public class AccountServlet extends HttpServlet {
    private static final long serialVersionUID = -6321196284331950479L;
    private final Logger logger = LoggerFactory.getLogger(AccountServlet.class);

    private HttpService httpService;
    private String bridgeId;
    private GroheOndusAccountHandler accountHandler;

    public AccountServlet(HttpService httpService, String bridgeId, GroheOndusAccountHandler accountHandler) {
        this.httpService = httpService;
        this.bridgeId = bridgeId;
        this.accountHandler = accountHandler;

        try {
            httpService.registerServlet(servletUrl(), this, null, httpService.createDefaultHttpContext());
        } catch (Exception e) {
            logger.warn("Register servlet fails", e);
        }
    }

    private String servletUrl() {
        return "/groheondus/" + URLEncoder.encode(bridgeId, StandardCharsets.UTF_8);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null || resp == null) {
            return;
        }
        resp.addHeader("content-type", "text/html;charset=UTF-8");
        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<html>");
        htmlString.append("<head>");
        htmlString.append("<title>Set refresh token</title>");
        htmlString.append("</head>");
        htmlString.append("<body>");
        htmlString.append("<header>");
        htmlString.append("<h1>Set refresh token for accout: ");
        htmlString.append(bridgeId);
        htmlString.append("</h1>");
        htmlString.append("</header>");
        htmlString.append("<div>Has refresh token: ");
        if (this.accountHandler.hasRefreshToken()) {
            htmlString.append("yes");
            htmlString.append(
                    "<input type=\"submit\" value=\"Delete\" onclick=\"fetch(window.location.href, {method: 'DELETE'}).then(window.location.reload())\">");
        } else {
            htmlString.append("no");
        }
        htmlString.append("</div>");
        htmlString.append("<form method=\"post\">");
        htmlString.append("<label for=\"refreshToken\">Refresh Token: </label>");
        htmlString.append("<input type=\"text\" id=\"refreshToken\" autocomplete=\"off\" name=\"refreshToken\">");
        htmlString.append("<input type=\"submit\" value=\"Save\">");
        htmlString.append("</form>");
        htmlString.append("</body>");
        htmlString.append("</html>");

        resp.getWriter().write(htmlString.toString());
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }

        Map<String, String[]> map = req.getParameterMap();
        this.accountHandler.setRefreshToken(map.get("refreshToken")[0]);

        resp.addHeader("Location", "/groheondus");
        resp.setStatus(HttpStatus.MOVED_TEMPORARILY_302);
    }

    @Override
    protected void doDelete(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }

        this.accountHandler.deleteRefreshToken();

        resp.setStatus(HttpStatus.OK_200);
    }

    public void dispose() {
        httpService.unregister(servletUrl());
    }
}

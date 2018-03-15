/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.amazonechocontrol.handler.AccountHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards the login dialog from amazon to the user, so the user can enter a captcha
 *
 * @author Michael Geramb - Initial Contribution
 */
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = -1453738923337413163L;

    private final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

    HttpService httpService;
    String servletUrlWithoutRoot;
    String servletUrl;
    Connection connection;
    AccountHandler account;
    AccountConfiguration configuration;

    public LoginServlet(HttpService httpService, String id, AccountHandler account,
            AccountConfiguration configuration) {
        this.httpService = httpService;
        this.account = account;
        this.configuration = configuration;
        reCreateConnection();
        servletUrlWithoutRoot = "amazonechocontrol/" + id;
        servletUrl = "/" + servletUrlWithoutRoot;
        try {
            httpService.registerServlet(servletUrl, this, null, httpService.createDefaultHttpContext());
        } catch (ServletException e) {

            logger.warn("Register servlet fails {}", e);
        } catch (NamespaceException e) {

            logger.warn("Register servlet fails {}", e);
        }
    }

    private void reCreateConnection() {
        this.connection = new Connection(configuration.email, configuration.password, configuration.amazonSite);
    }

    public void dispose() {
        httpService.unregister(servletUrl);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("content-type", "text/html;charset=UTF-8");

        Map<String, String[]> map = req.getParameterMap();
        StringBuilder postDataBuilder = new StringBuilder();
        for (String name : map.keySet()) {

            if (postDataBuilder.length() > 0) {
                postDataBuilder.append('&');
            }
            postDataBuilder.append(name);
            postDataBuilder.append('=');
            String value = map.get(name)[0];
            postDataBuilder.append(URLEncoder.encode(value, "UTF-8"));
            if (name.equals("email") && !value.equalsIgnoreCase(configuration.email)) {
                resp.getWriter().write(
                        "<html>Email must match the configured email of your thing. Change your configuration or retype your email</html>");
                return;
            }
            if (name.equals("password") && !value.equals(configuration.password)) {
                resp.getWriter().write(
                        "<html>Password must match the configured password of your thing. Change your configuration or retype your password</html>");
                return;
            }
        }
        String postData = postDataBuilder.toString();
        resp.addHeader("content-type", "text/html;charset=UTF-8");
        String errorHtml = null;
        try {
            errorHtml = connection.postLoginData(null, postData);
            if (errorHtml == null) {
                resp.getWriter().write("<html>Login succeeded</html>");
                account.setConnection(this.connection);
                reCreateConnection();
                return;
            }
            errorHtml = replaceLoginUrl(errorHtml);

        } catch (URISyntaxException e) {
            logger.error("Post login data failed with uri syntax error{}", e);
            errorHtml = "<html>Internal error</html>";
        }
        resp.addHeader("Content-Location", servletUrl);
        resp.getWriter().write(errorHtml);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().substring(servletUrl.length());
        logger.debug("doGet {}", uri);
        if (!uri.startsWith("/")) {
            String newUri = req.getServletPath() + "/" + uri;
            resp.sendRedirect(newUri);
            return;
        }
        try {
            String html = this.connection.getLoginPage();
            html = replaceLoginUrl(html);

            resp.addHeader("content-type", "text/html;charset=UTF-8");
            resp.getWriter().write(html);
        } catch (URISyntaxException e) {
            logger.error("get failed with uri syntax error {}", e);
        }
    }

    private String replaceLoginUrl(String html) {
        String result = html.replace("https://www." + connection.getAmazonSite() + "/", "");
        return result;
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
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
    private static final String FORWARD_URI_PART = "/FORWARD/";

    private final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

    HttpService httpService;
    String servletUrlWithoutRoot;
    String servletUrl;
    Connection connection;
    AccountHandler account;
    AccountConfiguration configuration;
    String id;

    public LoginServlet(HttpService httpService, String id, AccountHandler account,
            AccountConfiguration configuration) {
        this.httpService = httpService;
        this.account = account;
        this.id = id;
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
                returnError(resp,
                        "Email must match the configured email of your thing. Change your configuration or retype your email.");
                return;
            }

            if (name.equals("password") && !value.equals(configuration.password)) {
                returnError(resp,
                        "Password must match the configured password of your thing. Change your configuration or retype your password.");
                return;
            }
        }

        String uri = req.getRequestURI();
        if (!uri.startsWith(servletUrl)) {
            returnError(resp, "Invalid request uri '" + uri + "'");
            return;
        }
        String relativeUrl = uri.substring(servletUrl.length()).replace(FORWARD_URI_PART, "/");

        String postUrl = "https://www." + connection.getAmazonSite() + relativeUrl;
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            postUrl += "?" + queryString;
        }
        String referer = "https://www." + connection.getAmazonSite();
        String postData = postDataBuilder.toString();
        HandleProxyRequest(resp, "POST", postUrl, referer, postData);

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().substring(servletUrl.length());
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            uri += "?" + queryString;
        }
        logger.debug("doGet {}", uri);
        try {
            if (uri.startsWith(FORWARD_URI_PART)) {

                String getUrl = "https://www." + connection.getAmazonSite() + "/"
                        + uri.substring(FORWARD_URI_PART.length());

                this.HandleProxyRequest(resp, "GET", getUrl, null, null);
                return;
            }
            if (!uri.equals("/")) {
                String newUri = req.getServletPath() + "/";
                resp.sendRedirect(newUri);
                return;
            }
            String html = this.connection.getLoginPage();
            returnHtml(resp, html);

        } catch (URISyntaxException e) {
            logger.error("get failed with uri syntax error {}", e);
        }
    }

    void HandleProxyRequest(HttpServletResponse resp, String verb, String url, String referer, String postData)
            throws IOException {

        HttpsURLConnection urlConnection;
        try {
            urlConnection = connection.makeRequest(verb, url, referer, postData, false, false);
            if (urlConnection.getResponseCode() == 302) {
                {
                    String location = urlConnection.getHeaderField("location");
                    if (location.contains("//alexa.")) {
                        if (connection.verifyLogin()) {
                            resp.getWriter().write(
                                    "<html>Login succeeded. The account thing sould now be online.<br><a href='/paperui/index.html#/configuration/things/view/"
                                            + BINDING_ID + ":" + THING_TYPE_ACCOUNT.getId() + ":" + id
                                            + "'>Check Thing in Paper UI</a></html>");
                            account.setConnection(this.connection);
                            reCreateConnection();
                            return;
                        }
                    }
                    String startString = "https://www." + connection.getAmazonSite() + "/";
                    String newLocation = null;
                    if (location.startsWith(startString)) {
                        newLocation = servletUrl + FORWARD_URI_PART + location.substring(startString.length());
                    } else {
                        startString = "/";
                        if (location.startsWith(startString)) {
                            newLocation = servletUrl + FORWARD_URI_PART + location.substring(startString.length());
                        }
                    }
                    if (newLocation != null) {
                        logger.debug("Redirect mapped from {} to {}", location, newLocation);
                        resp.addHeader("location", newLocation);
                        resp.sendError(302);
                        return;
                    }
                    returnError(resp, "Invalid redirect to '" + location + "'");
                    return;
                }
            }

        } catch (URISyntaxException e) {

            returnError(resp, e.getLocalizedMessage());
            return;
        }

        String response = connection.convertStream(urlConnection.getInputStream());
        returnHtml(resp, response);
    }

    private void returnHtml(HttpServletResponse resp, String html) {
        String resultHtml = html.replace("https://www." + connection.getAmazonSite() + "/", servletUrl + "/");
        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(resultHtml);
        } catch (IOException e) {
            logger.error("return html failed with uri syntax error {}", e);
        }
    }

    void returnError(HttpServletResponse resp, String errorMessage) {
        try {

            resp.getWriter().write("<html>" + StringEscapeUtils.escapeHtml(errorMessage) + "<br><a href='" + servletUrl
                    + "'>Try again</a></html>");
        } catch (IOException e) {
            logger.info("Returning error message failed {}", e);
        }
    }
}

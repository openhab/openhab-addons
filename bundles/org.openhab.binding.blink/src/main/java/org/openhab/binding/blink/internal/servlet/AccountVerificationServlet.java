/*
  Copyright (c) 2010-2021 Contributors to the openHAB project
  <p>
  See the NOTICE file(s) distributed with this work for additional
  information.
  <p>
  This program and the accompanying materials are made available under the
  terms of the Eclipse Public License 2.0 which is available at
  http://www.eclipse.org/legal/epl-2.0
  <p>
  SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blink.internal.servlet;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.handler.AccountHandler;
import org.openhab.binding.blink.internal.service.AccountService;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class AccountVerificationServlet extends HttpServlet {

    private static final String SETTINGS_THINGS = "/settings/things/";
    private final Logger logger = LoggerFactory.getLogger(AccountVerificationServlet.class);
    private final HttpService httpService;
    private final AccountHandler accountHandler;
    private final AccountService blinkService;
    private final String servletUrl;
    private final BundleContext bundleContext;

    public AccountVerificationServlet(HttpService httpService, BundleContext bundleContext,
            AccountHandler accountHandler, AccountService blinkService) {
        this.httpService = httpService;
        this.bundleContext = bundleContext;
        this.accountHandler = accountHandler;
        this.blinkService = blinkService;

        try {
            servletUrl = "/blink/" + URLEncoder.encode(accountHandler.getThing().getUID().getId(), "UTF8");
            httpService.registerServlet(servletUrl, this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void dispose() {
        httpService.unregister(servletUrl);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (request == null) {
            logger.warn("Ignoring received request without response.");
            return;
        }
        if (response == null) {
            logger.warn("Ignoring illegal request.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        if (request.getParameter("resend") != null) {
            blinkService.login(accountHandler.getConfiguration(), accountHandler.getBlinkAccount().generatedClientId,
                    true);
        }
        response.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            generateVerificationPage(response.getWriter(), false);
        } catch (IOException e) {
            logger.warn("return html failed with uri syntax error", e);
        }
    }

    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (request == null) {
            logger.warn("Ignoring received request without response.");
            return;
        }
        if (response == null) {
            logger.warn("Ignoring illegal request.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        String pin = request.getParameter("pin");
        if (pin == null)
            pin = "";
        if (blinkService.verifyPin(accountHandler.getBlinkAccount(), pin)) {
            accountHandler.setOnline();
            response.sendRedirect(SETTINGS_THINGS);
        } else {
            generateVerificationPage(response.getWriter(), true);
        }
    }

    private void generateVerificationPage(PrintWriter writer, boolean validationError) throws IOException {
        URL url = this.bundleContext.getBundle()
                .getEntry("org/openhab/binding/blink/internal/servlet/validation.html");
        if (url == null)
            throw new IllegalArgumentException("validation.html not found");
        InputStream ioStream = url.openStream();
        if (ioStream == null) {
            throw new IllegalArgumentException("validation.html not found");
        }

        new BufferedReader(new InputStreamReader(ioStream)).lines().map(l -> {
            if ("{{error}}".equals(l.trim())) {
                if (validationError) {
                    return "<div class=\"error\">" +
                            "    <b>Invalid 2 factor verification PIN code.</b><br/>\n" +
                            "    The code is only valid for a 40 minute period. Please try disabling and enabling the blink Account Thing\n" +
                            "    to generate a new PIN code if you think that might be the problem." +
                            "</div>";
                } else {
                    return "";
                }
            }
            return l;
        }).forEach(writer::write);

    }

}

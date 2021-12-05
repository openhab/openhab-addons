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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.handler.AccountHandler;
import org.openhab.binding.blink.internal.service.AccountService;
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

    public AccountVerificationServlet(HttpService httpService, AccountHandler accountHandler,
            AccountService blinkService) {
        this.httpService = httpService;
        this.accountHandler = accountHandler;
        this.blinkService = blinkService;

        try {
            servletUrl = "/blink/" + URLEncoder.encode(accountHandler.getThing().getUID().getId(), "UTF8");
            httpService.registerServlet(servletUrl, this, null, httpService.createDefaultHttpContext());
            ServletContext context = this.getServletContext();
            logger.info(context.getContextPath());
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

    private void generateVerificationPage(PrintWriter writer, boolean validationError) {
        writer.write("<html><head><title>Blink 2FA</title><head><body>");
        writer.write("<h1>Blink 2FA</h1>");
        writer.write("<form method=\"POST\">");
        if (validationError) {
            writer.write("<span class=\"error\">Invalid 2FA verification PIN code.<br/>" +
                    "The code is only valid for a 10 minute period. Please try disabling and enabling the Blink Account " +
                    "Thing to generate a new PIN code if you think that might be the problem.</span>");
        }
        writer.write("<input type=\"text\" name=\"pin\" pattern=\"[0-9]{6}\" maxlength=\"6\">");
        writer.write("<input type=\"submit\" value=\"Validate\">");
        writer.write("<a href=\"?resend\">Resend pin code</a>");
        writer.write("</form></body></html>");
    }

}

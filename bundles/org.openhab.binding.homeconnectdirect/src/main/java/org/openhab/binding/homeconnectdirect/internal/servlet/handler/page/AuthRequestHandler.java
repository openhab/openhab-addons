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
package org.openhab.binding.homeconnectdirect.internal.servlet.handler.page;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_ASSETS_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_BASE_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnectdirect.internal.servlet.ServletSecurityContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.ServletUtils;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.LoginForm;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerException;

/**
 * Request handler for authentication page.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class AuthRequestHandler {

    private static final String CSRF_SESSION_ATTRIBUTE = "HCD_CSRF_TOKEN";
    private static final String TEMPLATE = "/templates/login.html";

    private final ServletSecurityContext securityContext;

    public AuthRequestHandler(ServletSecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public void loginPage(RequestHandlerContext context) throws RequestHandlerException {
        HttpSession session = context.getRequest().getSession(); // get or create
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute(CSRF_SESSION_ATTRIBUTE, csrfToken);

        try (InputStream is = getClass().getResourceAsStream(TEMPLATE)) {
            if (is == null) {
                throw new RequestHandlerException("Template not found: " + TEMPLATE);
            }

            String template = new String(is.readAllBytes(), UTF_8);
            String content = ServletUtils.replacePlaceholders(template, key -> switch (key) {
                case "servletPath" -> SERVLET_BASE_PATH;
                case "assetPath" -> SERVLET_ASSETS_PATH;
                case "csrfToken" -> csrfToken;
                default -> null;
            });
            context.sendHtml(content);
        } catch (IOException e) {
            throw new RequestHandlerException("Error reading template " + TEMPLATE, e);
        }
    }

    public void handleLogin(RequestHandlerContext context) throws RequestHandlerException {
        var session = context.getRequest().getSession(true);
        if (session == null) {
            throw new RequestHandlerException("Could not create session.");
        }

        var expectedToken = (String) session.getAttribute(CSRF_SESSION_ATTRIBUTE);
        session.removeAttribute(CSRF_SESSION_ATTRIBUTE); // Use token only once
        var formData = context.getRequestObject(LoginForm.class);

        var configuredPassword = context.getConfiguration().loginPassword;
        if (formData == null || expectedToken == null || !expectedToken.equals(formData.csrfToken())) {
            context.sendJson(
                    new ErrorMessage(HttpStatus.FORBIDDEN_403, "Invalid CSRF Token", createAndSaveCsrfToken(session)),
                    HttpStatus.FORBIDDEN_403);
        } else if (configuredPassword != null && !configuredPassword.isBlank()
                && configuredPassword.equals(formData.password())) {
            var token = securityContext.createAndRegisterAuthorizationToken();
            context.sendJson(new TokenMessage(token));
            session.invalidate();
        } else {
            context.sendJson(
                    new ErrorMessage(HttpStatus.UNAUTHORIZED_401, "Invalid password", createAndSaveCsrfToken(session)),
                    HttpStatus.UNAUTHORIZED_401);
        }
    }

    public void handleLogout(RequestHandlerContext context) {
        securityContext.invalidateAuthorization(context.getRequest());
        HttpSession session = context.getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        context.sendNoContent();
    }

    private String createAndSaveCsrfToken(HttpSession session) {
        var newToken = UUID.randomUUID().toString();
        session.setAttribute(CSRF_SESSION_ATTRIBUTE, newToken);
        return newToken;
    }

    private record ErrorMessage(int code, String error, String newCsrfToken) {
        // error message
    }

    private record TokenMessage(String token) {
        // token message
    }
}

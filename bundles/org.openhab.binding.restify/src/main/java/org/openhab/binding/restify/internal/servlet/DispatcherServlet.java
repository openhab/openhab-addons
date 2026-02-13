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
package org.openhab.binding.restify.internal.servlet;

import static java.util.Objects.requireNonNull;
import static javax.servlet.http.HttpServletResponse.*;
import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;
import static org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method.*;

import java.io.IOException;
import java.io.Serial;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jspecify.annotations.NonNull;
import org.openhab.binding.restify.internal.endpoint.Endpoint;
import org.openhab.binding.restify.internal.endpoint.EndpointRegistry;
import org.openhab.binding.restify.internal.endpoint.RegistrationException;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = { Servlet.class, DispatcherServlet.class }, immediate = true)
@HttpWhiteboardServletName(DispatcherServlet.SERVLET_PATH)
@HttpWhiteboardServletPattern({ DispatcherServlet.SERVLET_PATH, DispatcherServlet.SERVLET_PATH + "/*" })
public class DispatcherServlet extends HttpServlet {
    public static final String SERVLET_PATH = "/" + BINDING_ID;
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
    private final EndpointRegistry registry = new EndpointRegistry();
    private final JsonEncoder jsonEncoder;
    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final AuthorizationService authorizationService;
    private final Engine engine;

    @Activate
    public DispatcherServlet(@Reference JsonEncoder jsonEncoder, @Reference TranslationProvider i18nProvider,
            @Reference AuthorizationService authorizationService, @Reference Engine engine) {
        this.jsonEncoder = jsonEncoder;
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.i18nProvider = i18nProvider;
        this.authorizationService = authorizationService;
        this.engine = engine;
        logger.info("Starting DispatcherServlet");
    }

    private void process(Method method, @Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws IOException {
        requireNonNull(req, "Request must not be null");
        requireNonNull(resp, "Response must not be null");
        var uri = findRequestUri(req);
        logger.debug("Processing {}:{}", method, uri);
        try {
            var json = process(method, uri, req.getHeader("Authorization"));
            resp.setStatus(SC_OK);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(jsonEncoder.encode(json));
        } catch (UserRequestException e) {
            respondWithError(resp, e, req.getLocale());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            respondWithError(resp, SC_BAD_REQUEST, ex);
        } catch (Exception ex) {
            respondWithError(resp, SC_INTERNAL_SERVER_ERROR, ex);
        }
        resp.getWriter().close();
    }

    private static @NonNull String findRequestUri(@NonNull HttpServletRequest req) {
        var fullUri = requireNonNull(req.getRequestURI(), "Request URI must not be null");
        if (!fullUri.startsWith(SERVLET_PATH)) {
            throw new IllegalStateException("Request URI must start with " + SERVLET_PATH);
        }
        return fullUri.substring(SERVLET_PATH.length());
    }

    public Json.JsonObject process(Method method, String path, @Nullable String authorization)
            throws AuthorizationException, NotFoundException, ParameterException {
        var response = registry.find(path, method).orElseThrow(() -> new NotFoundException(path, method));
        authorizationService.authorize(response.authorization(), authorization);
        return engine.evaluate(response.schema());
    }

    private void respondWithError(HttpServletResponse resp, UserRequestException e, Locale locale) throws IOException {
        var statusCode = e.getStatusCode();
        var translatedMessage = i18nProvider.getText(bundle, e.getMessageKey(), e.getMessageKey(), locale,
                e.getMessageArguments());
        logger.error("{}: {}", statusCode, translatedMessage, e);
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        var errorResponse = Json.object();
        errorResponse.put("code", statusCode);
        errorResponse.put("error", translatedMessage);
        resp.getWriter().write(jsonEncoder.encode(errorResponse));
    }

    private void respondWithError(HttpServletResponse resp, int statusCode, Exception e) throws IOException {
        var message = e.getMessage();
        if (message == null) {
            message = e.toString();
        }
        logger.error("{}: {}", statusCode, message, e);
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        var errorResponse = Json.object();
        errorResponse.put("code", statusCode);
        errorResponse.put("error", message);
        resp.getWriter().write(jsonEncoder.encode(errorResponse));
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        process(GET, req, resp);
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        process(POST, req, resp);
    }

    @Override
    protected void doPut(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        process(PUT, req, resp);
    }

    @Override
    protected void doDelete(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        process(DELETE, req, resp);
    }

    public void register(String path, Method method, Endpoint endpoint) throws RegistrationException {
        registry.register(path, method, endpoint);
    }

    public void unregister(String path, Method method) {
        registry.unregister(path, method);
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}

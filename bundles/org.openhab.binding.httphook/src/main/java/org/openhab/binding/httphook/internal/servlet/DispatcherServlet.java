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
package org.openhab.binding.httphook.internal.servlet;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static javax.servlet.http.HttpServletResponse.*;
import static org.openhab.binding.httphook.internal.HttpHookBindingConstants.BINDING_ID;
import static org.openhab.binding.httphook.internal.servlet.DispatcherServlet.Method.*;

import java.io.IOException;
import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.httphook.internal.endpoint.Endpoint;
import org.openhab.binding.httphook.internal.endpoint.EndpointRegistry;
import org.openhab.binding.httphook.internal.endpoint.RegistrationException;
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

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final AuthorizationService authorizationService;

    @Activate
    public DispatcherServlet(@Reference TranslationProvider i18nProvider,
            @Reference AuthorizationService authorizationService) {
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.i18nProvider = i18nProvider;
        this.authorizationService = authorizationService;
        logger.info("Starting DispatcherServlet");
    }

    private void process(Method method, @Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws IOException {
        requireNonNull(req, "Request must not be null");
        requireNonNull(resp, "Response must not be null");
        try {
            var uri = findRequestUri(req);
            logger.debug("Processing {}:{}", method, uri);
            var endpointResponse = process(method, uri, req.getHeader("Authorization"), req);
            resp.setStatus(SC_OK);
            resp.setContentType(endpointResponse.contentType());
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(endpointResponse.body());
        } catch (UserRequestException e) {
            respondWithError(resp, e, req.getLocale());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            respondWithError(resp, SC_BAD_REQUEST, ex);
        } catch (Exception ex) {
            respondWithError(resp, SC_INTERNAL_SERVER_ERROR, ex);
        }
        resp.getWriter().close();
    }

    private static String findRequestUri(HttpServletRequest req) {
        var fullUri = requireNonNull(req.getRequestURI(), "Request URI must not be null");
        if (!fullUri.startsWith(SERVLET_PATH)) {
            throw new IllegalStateException("Request URI must start with " + SERVLET_PATH);
        }
        return fullUri.substring(SERVLET_PATH.length());
    }

    public EndpointResponse process(Method method, String path, @Nullable String authorization, String requestContext)
            throws AuthorizationException, NotFoundException, TransformationFailedException {
        var endpoint = registry.find(path, method).orElseThrow(() -> new NotFoundException(path, method));
        authorizationService.authorize(endpoint.authorization(), authorization);
        var body = endpoint.transformation().apply(requestContext)
                .orElseThrow(() -> new TransformationFailedException(path, method));
        return new EndpointResponse(body, endpoint.contentType());
    }

    private EndpointResponse process(Method method, String path, @Nullable String authorization,
            HttpServletRequest request)
            throws IOException, AuthorizationException, NotFoundException, TransformationFailedException {
        var endpoint = registry.find(path, method).orElseThrow(() -> new NotFoundException(path, method));
        authorizationService.authorize(endpoint.authorization(), authorization);
        var requestContext = createRequestContext(method, path, request);
        var body = endpoint.transformation().apply(requestContext)
                .orElseThrow(() -> new TransformationFailedException(path, method));
        return new EndpointResponse(body, endpoint.contentType());
    }

    private String createRequestContext(Method method, String path, HttpServletRequest request) throws IOException {
        Map<String, @Nullable Object> requestContext = new LinkedHashMap<>();
        requestContext.put("method", method.name());
        requestContext.put("path", path);
        requestContext.put("queryString", request.getQueryString());
        requestContext.put("headers", getHeaders(request));
        requestContext.put("remoteAddr", request.getRemoteAddr());
        requestContext.put("body", readBody(request));
        return objectMapper.writeValueAsString(requestContext);
    }

    private static Map<String, List<String>> getHeaders(HttpServletRequest request) {
        var headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return Map.of();
        }
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String headerName : Collections.list(headerNames)) {
            var headerValues = request.getHeaders(headerName);
            headers.put(headerName, headerValues == null ? List.of() : Collections.list(headerValues));
        }
        return headers;
    }

    private static String readBody(HttpServletRequest request) throws IOException {
        var reader = request.getReader();
        if (reader == null) {
            return "";
        }
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private void respondWithError(HttpServletResponse resp, UserRequestException e, Locale locale) throws IOException {
        var statusCode = e.getStatusCode();
        var translatedMessage = i18nProvider.getText(bundle, e.getMessageKey(), e.getMessageKey(), locale,
                e.getMessageArguments());
        logger.error("{}: {}", statusCode, translatedMessage, e);
        writeJsonError(resp, statusCode, translatedMessage != null ? translatedMessage : e.getMessageKey());
    }

    private void respondWithError(HttpServletResponse resp, int statusCode, Exception e) throws IOException {
        logger.error("{}: {}", statusCode, e.getMessage(), e);
        writeJsonError(resp, statusCode, e.getLocalizedMessage());
    }

    private void writeJsonError(HttpServletResponse resp, int statusCode, @Nullable String message) throws IOException {
        var body = new LinkedHashMap<String, Object>();
        body.put("code", statusCode);
        body.put("error", requireNonNullElse(message, ""));
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(body));
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

    public record EndpointResponse(String body, String contentType) {
    }
}

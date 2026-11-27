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

import static jakarta.servlet.http.HttpServletResponse.*;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.openhab.binding.httphook.internal.HttpHookBindingConstants.BINDING_ID;
import static org.openhab.binding.httphook.internal.servlet.DispatcherServlet.Method.*;

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Martin Grzeslowski - Initial contribution
 * @author Martin Grześlowski - Protect credentials and isolate browser responses
 */
@NonNullByDefault
@Component(service = { Servlet.class, DispatcherServlet.class }, immediate = true)
@HttpWhiteboardServletName(DispatcherServlet.SERVLET_PATH)
@HttpWhiteboardServletPattern({ DispatcherServlet.SERVLET_PATH, DispatcherServlet.SERVLET_PATH + "/*" })
public class DispatcherServlet extends HttpServlet {
    public static final String SERVLET_PATH = "/" + BINDING_ID;
    private static final int MAX_BODY_SIZE = 64 * 1024;
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "proxy-authorization", "cookie",
            "cookie2", "set-cookie", "x-api-key", "x-auth-token");
    private static final Set<String> INLINE_MEDIA_TYPES = Set.of("application/json", "text/plain", "text/csv");
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
        resp.setHeader("Content-Security-Policy", "sandbox; default-src 'none'");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        try {
            var uri = findRequestUri(req);
            logger.debug("Processing {}:{}", method, uri);
            var endpointResponse = process(method, uri, req.getHeader("Authorization"), req);
            resp.setStatus(SC_OK);
            var contentType = endpointResponse.contentType();
            if (!INLINE_MEDIA_TYPES.contains(contentType.split(";", 2)[0].strip().toLowerCase(Locale.ROOT))) {
                // Serve potentially active content as a download on the openHAB UI origin.
                resp.setHeader("Content-Disposition", "attachment");
            }
            resp.setContentType(contentType);
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(endpointResponse.body());
        } catch (UserRequestException e) {
            respondWithError(resp, req.getLocale(), e.getStatusCode(), e.getMessageKey(), e, e.getMessageArguments());
        } catch (IllegalArgumentException | IllegalStateException e) {
            respondWithError(resp, req.getLocale(), SC_BAD_REQUEST, "servlet.error.bad-request", e);
        } catch (Exception e) {
            respondWithError(resp, req.getLocale(), SC_INTERNAL_SERVER_ERROR, "servlet.error.internal", e);
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
            HttpServletRequest request) throws IOException, UserRequestException {
        var endpoint = registry.find(path, method).orElseThrow(() -> new NotFoundException(path, method));
        authorizationService.authorize(endpoint.authorization(), authorization);
        var requestContext = createRequestContext(method, path, request);
        var body = endpoint.transformation().apply(requestContext)
                .orElseThrow(() -> new TransformationFailedException(path, method));
        return new EndpointResponse(body, endpoint.contentType());
    }

    private String createRequestContext(Method method, String path, HttpServletRequest request)
            throws IOException, UserRequestException {
        var body = readBody(request);
        Map<String, @Nullable Object> requestContext = new LinkedHashMap<>();
        requestContext.put("method", method.name());
        requestContext.put("path", path);
        requestContext.put("queryString", request.getQueryString());
        requestContext.put("headers", getHeaders(request));
        requestContext.put("remoteAddr", request.getRemoteAddr());
        requestContext.put("body", body);
        return objectMapper.writeValueAsString(requestContext);
    }

    private static Map<String, List<String>> getHeaders(HttpServletRequest request) {
        var headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return Map.of();
        }
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String headerName : Collections.list(headerNames)) {
            if (SENSITIVE_HEADERS.contains(headerName.toLowerCase(Locale.ROOT))) {
                continue;
            }
            var headerValues = request.getHeaders(headerName);
            headers.put(headerName, headerValues == null ? List.of() : Collections.list(headerValues));
        }
        return headers;
    }

    private static String readBody(HttpServletRequest request) throws IOException, UserRequestException {
        if (request.getContentLengthLong() <= MAX_BODY_SIZE) {
            var bytes = request.getInputStream().readNBytes(MAX_BODY_SIZE + 1);
            if (bytes.length <= MAX_BODY_SIZE) {
                var encoding = requireNonNullElse(request.getCharacterEncoding(), StandardCharsets.ISO_8859_1.name());
                return new String(bytes, encoding).lines().collect(Collectors.joining("\n"));
            }
        }
        throw new UserRequestException(SC_REQUEST_ENTITY_TOO_LARGE, "servlet.error.body-too-large", MAX_BODY_SIZE);
    }

    private void respondWithError(HttpServletResponse resp, Locale locale, int statusCode, String messageKey,
            Exception e, Object... messageArguments) throws IOException {
        var translatedMessage = requireNonNullElse(
                i18nProvider.getText(bundle, messageKey, messageKey, locale, messageArguments), messageKey);
        if (statusCode >= SC_INTERNAL_SERVER_ERROR) {
            logger.error("{}: {}", statusCode, translatedMessage, e);
        } else {
            logger.debug("{}: {}", statusCode, translatedMessage);
        }
        writeJsonError(resp, statusCode, translatedMessage);
    }

    private void writeJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        var body = new LinkedHashMap<String, Object>();
        body.put("code", statusCode);
        body.put("error", message);
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

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

import static jakarta.servlet.http.HttpServletResponse.*;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method.*;

import java.io.IOException;
import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.config.Config;
import org.openhab.binding.restify.internal.config.ConfigWatcher;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = { Servlet.class, DispatcherServlet.class }, property = {
        "osgi.http.whiteboard.servlet.pattern=/restify/*",
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)" })
public class DispatcherServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class.getName());
    private final EndpointRegistry registry = new EndpointRegistry();
    private final JsonEncoder jsonEncoder;
    private final ConfigWatcher configWatcher;
    private final Engine engine;

    @Activate
    public DispatcherServlet(@Reference JsonEncoder jsonEncoder, @Reference ConfigWatcher configWatcher,
            @Reference Engine engine) {
        this.jsonEncoder = jsonEncoder;
        this.configWatcher = configWatcher;
        this.engine = engine;
        logger.info("Starting DispatcherServlet");
    }

    private void process(Method method, @Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws IOException {
        requireNonNull(req, "Request must not be null");
        requireNonNull(resp, "Response must not be null");
        var uri = req.getRequestURI();
        logger.debug("Processing {}:{}", method, uri);
        try {
            var json = process(method, uri, req.getHeader("Authorization"));
            resp.setStatus(SC_OK);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(jsonEncoder.encode(json));
        } catch (UserRequestException e) {
            respondWithError(resp, e.getStatusCode(), e);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            respondWithError(resp, SC_BAD_REQUEST, ex);
        } catch (Exception ex) {
            respondWithError(resp, SC_INTERNAL_SERVER_ERROR, ex);
        }
        resp.getWriter().close();
    }

    public Json.JsonObject process(Method method, String path, @Nullable String authorization)
            throws AuthorizationException, NotFoundException, ParameterException {
        var config = configWatcher.currentConfig();
        var response = registry.find(path, method).orElseThrow(() -> new NotFoundException(path, method));
        if (response.authorization() != null) {
            authorize(config, response.authorization(), authorization);
        }
        return engine.evaluate(response.schema());
    }

    private void authorize(Config config, @Nullable Authorization required, @Nullable String provided)
            throws AuthorizationException {
        if (required == null) {
            // TODO add global config flag that allows to disable authorization for all endpoints, then we can skip
            // authorization if the flag is disabled
            return;
        }
        if (provided == null) {
            throw new AuthorizationException("Authorization required");
        }

        switch (required) {
            case Authorization.Basic basic -> authorize(config, basic, provided);
            case Authorization.Bearer bearer -> authorize(bearer, provided);
        }
    }

    private void authorize(Config config, Authorization.Basic basic, String provided) throws AuthorizationException {
        var expected = "Basic " + basic.username() + ":" + basic.password();
        if (!provided.equals(expected)) {
            throw new AuthorizationException("Invalid username or password");
        }
    }

    private void authorize(Authorization.Bearer bearer, String provided) throws AuthorizationException {
        if (!provided.equals("Bearer " + bearer.token())) {
            throw new AuthorizationException("Invalid token");
        }
    }

    private void respondWithError(HttpServletResponse resp, int statusCode, Exception e) throws IOException {
        logger.error("%s: %s".formatted(statusCode, e.getMessage()), e);
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"code\": %d, \"error\": \"%s\"}".formatted(statusCode, e.getMessage()));
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

    public void register(String path, Method method, Response response) {
        registry.register(path, method, response);
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

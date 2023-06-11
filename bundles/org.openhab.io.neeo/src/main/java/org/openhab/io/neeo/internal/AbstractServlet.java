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
package org.openhab.io.neeo.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.io.neeo.NeeoService;
import org.openhab.io.neeo.internal.servletservices.ServletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of {@link HttpServlet} handles all the routing for the servlet. {@link ServletService}'s are
 * added in the constructor and then delegated to by this class.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractServlet extends HttpServlet implements AutoCloseable {

    /** The serial UID */
    private static final long serialVersionUID = -9109038869609595306L;

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(AbstractServlet.class);

    /** The services for this servlet */
    private final ServletService[] services;

    /** URL of the servlet */
    private final String servletUrl;

    /** Any event filters */
    private final @Nullable List<EventFilter> eventFilters;

    /**
     * Creates a servlet to serve the status/definitions web pages
     *
     * @param context the non-null service context
     * @param servletUrl the non-null servletUrl
     * @param services the non-null list of services
     */
    AbstractServlet(ServiceContext context, String servletUrl, ServletService... services) {
        NeeoUtil.requireNotEmpty(servletUrl, "servletUrl cannot be empty");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(services, "services cannot be null");

        this.servletUrl = servletUrl;
        this.services = services;

        final List<EventFilter> efs = new ArrayList<>();
        for (ServletService service : services) {
            EventFilter ef = service.getEventFilter();
            if (ef != null) {
                efs.add(ef);
            }
        }
        if (efs.isEmpty()) {
            eventFilters = null;
        } else {
            eventFilters = efs;
        }
    }

    /**
     * Returns the URL the servlet listens on
     *
     * @return a non-null, non-empty servlet URL
     */
    public String getServletUrl() {
        return servletUrl;
    }

    /**
     * Handles the get by routing it to the appropriate {@link ServletService} or logging it if no route found
     */
    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        if (logger.isDebugEnabled()) {
            logger.debug("doGet: {}", getFullURL(req));
        }

        final String pathInfo = NeeoUtil.decodeURIComponent(req.getPathInfo());

        // invalid path - probably someone typed the path in manually
        if (pathInfo.isEmpty()) {
            return;
        }

        final String[] paths = StringUtils.split(pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo, '/');
        final ServletService service = getService(paths);

        if (service == null) {
            logger.debug("Unknown/unhandled route: {}", pathInfo);
        } else {
            service.handleGet(req, paths, resp);
        }
    }

    /**
     * Handles the post by routing it to the appropriate {@link ServletService} or logging it if no route found
     */
    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        if (logger.isDebugEnabled()) {
            req.getReader().mark(150000);
            logger.debug("doPost: {} with {}", getFullURL(req),
                    new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            req.getReader().reset();
        }

        final String pathInfo = NeeoUtil.decodeURIComponent(req.getPathInfo());
        final String[] paths = StringUtils.split(pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo, '/');
        final ServletService service = getService(paths);

        if (service == null) {
            logger.debug("Unknown/unhandled route: {}", pathInfo);
        } else {
            service.handlePost(req, paths, resp);
        }
    }

    /**
     * Gets the {@link ServletService} for the given path.
     *
     * @param paths the non-null, non-empty paths
     * @return the service that can handle the path or null if none can
     */
    protected @Nullable ServletService getService(String[] paths) {
        Objects.requireNonNull(paths, "paths cannot be null");
        if (paths.length == 0) {
            throw new IllegalArgumentException("paths cannot be of 0 length");
        }

        for (ServletService service : services) {
            if (service.canHandleRoute(paths)) {
                return service;
            }
        }
        return null;
    }

    /**
     * Helper method to get the full URL from the given request
     *
     * @param request the non-null request
     * @return the full URL
     */
    protected static String getFullURL(HttpServletRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return NeeoUtil.decodeURIComponent(requestURL.toString());
        } else {
            return NeeoUtil.decodeURIComponent(requestURL.append('?').append(queryString).toString());
        }
    }

    /**
     * Called to have the servlet receive and handle an event (from {@link NeeoService}). The servlet will simply
     * delegate the event to each {@link ServletService} until one of the services handles the event
     *
     * @param event the non-null event
     */
    public void receive(Event event) {
        Objects.requireNonNull(event, "event cannot be null");

        for (ServletService target : services) {
            if (target.handleEvent(event)) {
                break;
            }
        }
    }

    /**
     * Returns all the {@link EventFilter} (s) to use to filter events
     *
     * @return the possibly null event filters;
     */
    public @Nullable List<EventFilter> getEventFilters() {
        return eventFilters;
    }

    /**
     * Simply closes the API and each of the services
     */
    @Override
    public void close() {
        for (ServletService service : services) {
            NeeoUtil.close(service);
        }
    }
}

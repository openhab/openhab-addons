/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.openhab.io.neeo.internal.NeeoApi;
import org.openhab.io.neeo.internal.NeeoDeviceKeys;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.models.BrainStatus;
import org.openhab.io.neeo.internal.servletservices.BrainStatusService;
import org.openhab.io.neeo.internal.servletservices.NeeoBrainService;
import org.openhab.io.neeo.internal.servletservices.SearchService;
import org.openhab.io.neeo.internal.servletservices.ServletService;
import org.openhab.io.neeo.internal.servletservices.ThingStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of {@link HttpServlet} handles all the routing for the servlet. {@link ServletService}'s are
 * added in the constructor and then delegated to by this class.
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoServlet extends HttpServlet implements AutoCloseable {

    /** The serial UID */
    private static final long serialVersionUID = -9109038869609595306L;

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoServlet.class);

    /** The NEEO API to use. Can be null if not a NEEO servlet */
    private final NeeoApi api;

    /** The services for this servlet */
    private final ServletService[] services;

    /** The event filters used by this servlet (may be null if services don't implement an event filter) */
    private final List<EventFilter> eventFilters;

    /** URL of the servlet */
    private final String servletUrl;

    /**
     * Creates a servlet to serve the status/definitions web pages
     *
     * @param service the non-null parent service
     * @param context the non-null service context
     */
    NeeoServlet(NeeoService service, String servletUrl, ServiceContext context) {
        NeeoUtil.requireNotEmpty(servletUrl, "servletUrl cannot be empty");
        Objects.requireNonNull(service, "service cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        this.servletUrl = servletUrl;
        api = null;

        services = new ServletService[] { new BrainStatusService(service), new ThingStatusService(service, context) };
        eventFilters = Stream.of(services).map((s) -> s.getEventFilter()).filter((ef -> ef != null))
                .collect(Collectors.toList());
    }

    /**
     * Create a servlet to handle transport duties with the NEEO brain
     *
     * @param brainId the non-empty brain id
     * @param ipAddress the non-null ip address
     * @param context the non-null service context
     */
    NeeoServlet(String brainId, InetAddress ipAddress, String servletUrl, ServiceContext context) throws IOException {
        NeeoUtil.requireNotEmpty(servletUrl, "servletUrl cannot be empty");
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        Objects.requireNonNull(ipAddress, "ipAddress cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        this.servletUrl = servletUrl;
        api = new NeeoApi(ipAddress.getHostAddress(), brainId, context);
        api.start();

        services = new ServletService[] { new SearchService(context), new NeeoBrainService(api, context) };
        eventFilters = Stream.of(services).map((s) -> s.getEventFilter()).filter((ef -> ef != null))
                .collect(Collectors.toList());
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
     * Returns the status of the brain
     *
     * @return a non-null {@link BrainStatus}
     */
    public BrainStatus getBrainStatus() {
        return new BrainStatus(api.getBrainId(), api.getBrainUrl(), NeeoUtil.getServletUrl(api.getBrainId()),
                api.isConnected());
    }

    /**
     * Returns the {@link NeeoApi} related to the brain
     *
     * @return a non-null {@link NeeoApi}
     */
    public NeeoApi getBrainApi() {
        return api;
    }

    /**
     * Returns the device keys used by the brain
     *
     * @return a non-null {@link NeeoDeviceKeys}
     */
    public NeeoDeviceKeys getDeviceKeys() {
        return api.getDeviceKeys();
    }

    /**
     * Handles the get by routing it to the appropriate {@link ServletService} or logging it if no route found
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        if (logger.isDebugEnabled()) {
            logger.debug("doGet: {}", getFullURL(req));
        }

        final String pathInfo = NeeoUtil.decodeURIComponent(req.getPathInfo());

        // invalid path - probably someone typed the path in manually
        if (StringUtils.isEmpty(pathInfo)) {
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
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        if (logger.isDebugEnabled()) {
            req.getReader().mark(150000);
            logger.debug("doPost: {} with {}", getFullURL(req), IOUtils.toString(req.getReader()));
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
    private ServletService getService(String[] paths) {
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
    private static String getFullURL(HttpServletRequest request) {
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
    void receive(Event event) {
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
    public List<EventFilter> getEventFilters() {
        return eventFilters;
    }

    /**
     * Simply closes the API and each of the services
     */
    @Override
    public void close() {
        NeeoUtil.close(api);
        for (ServletService service : services) {
            NeeoUtil.close(service);
        }
    }
}

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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.http.servlet.BaseOpenHABServlet;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WundergroundUpdateReceiverServlet} is responsible for receiving updates,and
 * updating the matching channels.
 *
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
public class WundergroundUpdateReceiverServlet extends BaseOpenHABServlet
        implements WundergroundUpdateReceiverServletControls {

    public static final String SERVLET_URL = "/weatherstation/updateweatherstation.php";
    private static final long serialVersionUID = -5296703727081438023L;
    private static final Pattern CLEANER = Pattern.compile("[^\\w-]");

    private final Logger logger = LoggerFactory.getLogger(WundergroundUpdateReceiverServlet.class);
    private final Map<String, WundergroundUpdateReceiverHandler> handlers = new HashMap<>();

    private static final Object LOCK = new Object();
    private final WundergroundUpdateReceiverDiscoveryService discoveryService;

    private boolean active = false;
    private String errorDetail = "";

    public WundergroundUpdateReceiverServlet(HttpService httpService,
            WundergroundUpdateReceiverDiscoveryService discoveryService) {
        super(httpService);
        this.discoveryService = discoveryService;
    }

    public boolean isActive() {
        synchronized (LOCK) {
            return this.active;
        }
    }

    public String getErrorDetail() {
        synchronized (LOCK) {
            return this.errorDetail;
        }
    }

    public Set<String> getStationIds() {
        return this.handlers.keySet();
    }

    public void activate() {
        activate(SERVLET_URL, httpService.createDefaultHttpContext());
    }

    public void addHandler(WundergroundUpdateReceiverHandler handler) {
        synchronized (this.handlers) {
            if (this.handlers.containsKey(handler.getStationId())) {
                errorDetail = "Handler handling request for stationId " + handler.getStationId() + " is already added";
                logger.warn("Error during handler registration - StationId {} already being handled",
                        handler.getStationId());
                return;
            }
            this.handlers.put(handler.getStationId(), handler);
            errorDetail = "";
            if (!isActive()) {
                activate();
            }
        }
    }

    public void removeHandler(String stationId) {
        synchronized (this.handlers) {
            WundergroundUpdateReceiverHandler handler = this.handlers.get(stationId);
            if (handler != null) {
                this.handlers.remove(stationId);
            }
            if (this.handlers.isEmpty() && !this.discoveryService.isBackgroundDiscoveryEnabled()) {
                deactivate();
            }
        }
    }

    public void deactivate() {
        synchronized (LOCK) {
            logger.debug("Stopping servlet {} at {}", getClass().getSimpleName(), SERVLET_URL);
            try {
                super.deactivate(SERVLET_URL);
            } catch (IllegalArgumentException ignored) {
                // SERVLET_URL is already unregistered
            }
            errorDetail = "";
            active = false;
        }
    }

    public void handlerConfigUpdated(WundergroundUpdateReceiverHandler handler) {
        synchronized (this.handlers) {
            final Set<Map.Entry<String, WundergroundUpdateReceiverHandler>> changedStationIds = this.handlers.entrySet()
                    .stream().filter(entry -> handler.getThing().getUID().equals(entry.getValue().getThing().getUID()))
                    .collect(toSet());
            changedStationIds.forEach(entry -> {
                logger.debug("Re-assigning listener from station id {} to station id {}", entry.getKey(),
                        handler.getStationId());
                this.removeHandler(entry.getKey());
                this.addHandler(handler);
            });
        }
    }

    public void dispose() {
        synchronized (this.handlers) {
            Set<String> stationIds = new HashSet<>(getStationIds());
            stationIds.forEach(this::removeHandler);
            deactivate();
        }
    }

    @Override
    protected void activate(String alias, HttpContext httpContext) {
        synchronized (LOCK) {
            try {
                logger.debug("Starting servlet {} at {}", getClass().getSimpleName(), alias);
                Dictionary<String, String> props = new Hashtable<>(1, 10);
                httpService.registerServlet(alias, this, props, httpContext);
                errorDetail = "";
                active = true;
            } catch (NamespaceException e) {
                active = false;
                errorDetail = "Servlet couldn't be registered - alias " + alias + " already in use";
                logger.warn("Error during servlet registration - alias {} already in use", alias, e);
            } catch (ServletException e) {
                active = false;
                errorDetail = "Servlet couldn't be registered - " + e.getMessage();
                logger.warn("Error during servlet registration", e);
            }
        }
    }

    protected Map<String, String> normalizeParameterMap(Map<String, String[]> parameterMap) {
        return parameterMap.entrySet().stream()
                .collect(toMap(e -> makeUidSafeString(e.getKey()), e -> String.join("", e.getValue())));
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (!active) {
            return;
        }
        if (req == null) {
            return;
        }
        if (resp == null) {
            return;
        }
        if (req.getRequestURI() == null) {
            return;
        }
        logger.trace("doGet {}", req.getQueryString());

        String stationId = req.getParameter(STATION_ID_PARAMETER);
        Map<String, String> states = normalizeParameterMap(req.getParameterMap());
        Optional.ofNullable(this.handlers.get(stationId)).ifPresentOrElse(handler -> {
            String queryString = req.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                states.put(LAST_QUERY, queryString);
            }
            handler.updateChannelStates(states);
        }, () -> {
            this.discoveryService.addUnhandledStationId(stationId, states);
        });

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html;charset=utf-8");
        resp.setContentLength(7);
        resp.setDateHeader("Date", Instant.now().toEpochMilli());
        resp.setHeader("Connection", "close");
        PrintWriter writer = resp.getWriter();
        writer.write("success");
        writer.flush();
        writer.close();
    }

    protected Map<String, WundergroundUpdateReceiverHandler> getHandlers() {
        return Collections.unmodifiableMap(this.handlers);
    }

    private String makeUidSafeString(String key) {
        return CLEANER.matcher(key).replaceAll("-");
    }
}

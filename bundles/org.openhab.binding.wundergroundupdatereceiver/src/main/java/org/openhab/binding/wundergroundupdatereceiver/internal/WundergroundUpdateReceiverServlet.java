/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WundergroundUpdateReceiverServlet} is responsible for receiving updates,and
 * updating the matching channels.
 *
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
@HttpWhiteboardServletName(WundergroundUpdateReceiverServlet.SERVLET_URL)
@HttpWhiteboardServletPattern(WundergroundUpdateReceiverServlet.SERVLET_URL)
@Component(immediate = true, service = { Servlet.class, WundergroundUpdateReceiverServlet.class })
public class WundergroundUpdateReceiverServlet extends HttpServlet
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

    @Activate
    public WundergroundUpdateReceiverServlet(
            final @Reference WundergroundUpdateReceiverDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
        errorDetail = "";
        active = discoveryService.isBackgroundDiscoveryEnabled();
    }

    @Override
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
                enable();
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
                disable();
            }
        }
    }

    @Override
    public void enable() {
        active = true;
    }

    @Deactivate
    @Override
    public void disable() {
        errorDetail = "";
        active = false;
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
            disable();
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

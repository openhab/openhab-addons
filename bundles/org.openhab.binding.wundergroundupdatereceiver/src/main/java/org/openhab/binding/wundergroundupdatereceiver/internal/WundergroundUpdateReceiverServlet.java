/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
public class WundergroundUpdateReceiverServlet extends BaseOpenHABServlet {

    public static final String SERVLET_URL = "/weatherstation/updateweatherstation.php";
    private static final long serialVersionUID = -5296703727081438023L;

    private final Logger logger = LoggerFactory.getLogger(WundergroundUpdateReceiverServlet.class);
    private final Map<String, WundergroundUpdateReceiverHandler> handlers = new HashMap<>();

    private static final Object LOCK = new Object();

    private boolean active = false;

    public WundergroundUpdateReceiverServlet(HttpService httpService) {
        super(httpService);
    }

    public boolean isActive() {
        synchronized (LOCK) {
            return this.active;
        }
    }

    public Set<String> getStationIds() {
        return this.handlers.keySet();
    }

    public void activate() {
        activate(SERVLET_URL, httpService.createDefaultHttpContext());
    }

    @Override
    protected void activate(String alias, HttpContext httpContext) {
        synchronized (LOCK) {
            try {
                logger.debug("Starting servlet {} at {}", getClass().getSimpleName(), alias);
                Dictionary<String, String> props = new Hashtable<>(1, 10);
                httpService.registerServlet(alias, this, props, httpContext);
                active = true;
            } catch (NamespaceException e) {
                logger.error("Error during servlet registration - alias {} already in use", alias, e);
            } catch (ServletException e) {
                logger.error("Error during servlet registration", e);
            }
        }
    }

    public void addHandler(WundergroundUpdateReceiverHandler handler) {
        synchronized (this.handlers) {
            if (this.handlers.containsKey(handler.getStationId())) {
                throw new IllegalArgumentException(String
                        .format("Handler handling request for stationId %s is already added", handler.getStationId()));
            }
            this.handlers.put(handler.getStationId(), handler);
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
            if (this.handlers.isEmpty()) {
                deactivate();
            }
        }
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
        logger.debug("doGet {}", req.getQueryString());

        Optional<WundergroundUpdateReceiverHandler> maybeHandler = Optional
                .ofNullable(this.handlers.get(req.getParameter(STATION_ID_PARAMETER)));
        maybeHandler.ifPresent(handler -> {
            Map<String, String> states = req.getParameterMap().entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, e -> String.join("", e.getValue())));
            String queryString = req.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                states.put(LAST_QUERY, queryString);
            }
            handler.updateChannelStates(handler, states);
        });

        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = resp.getWriter();
        writer.write("success");
        writer.flush();
        writer.close();
    }

    public void deactivate() {
        synchronized (LOCK) {
            logger.debug("Stopping servlet {} at {}", getClass().getSimpleName(), SERVLET_URL);
            super.deactivate(SERVLET_URL);
            active = false;
        }
    }

    public void handlerConfigUpdated(WundergroundUpdateReceiverHandler handler) {
        synchronized (this.handlers) {
            final Set<Map.Entry<String, WundergroundUpdateReceiverHandler>> changedStationIds = this.handlers.entrySet()
                    .stream().filter(entry -> handler.getThing().getUID().equals(entry.getValue().getThing().getUID()))
                    .collect(toSet());
            changedStationIds.forEach(entry -> {
                logger.info("Re-assigning listener from station ID {} to station id {}", entry.getKey(),
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
        }
    }
}

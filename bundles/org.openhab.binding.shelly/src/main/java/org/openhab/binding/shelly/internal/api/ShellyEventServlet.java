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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api2.Shelly2RpcSocket;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shelly2RpcServlet} implements the WebSocket callback for Gen2 devices
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@WebServlet(name = "ShellyEventServlet", urlPatterns = { SHELLY1_CALLBACK_URI, SHELLY2_CALLBACK_URI })
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ShellyEventServlet extends WebSocketServlet {
    private static final long serialVersionUID = -1210354558091063207L;
    private final Logger logger = LoggerFactory.getLogger(ShellyEventServlet.class);

    private final ShellyHandlerFactory handlerFactory;
    private final ShellyThingTable thingTable;

    @Activate
    public ShellyEventServlet(@Reference ShellyHandlerFactory handlerFactory, @Reference ShellyThingTable thingTable) {
        this.handlerFactory = handlerFactory;
        this.thingTable = thingTable;
        logger.debug("Shelly EventServlet started at {} and {}", SHELLY1_CALLBACK_URI, SHELLY2_CALLBACK_URI);
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("ShellyEventServlet: Stopping");
    }

    /**
     * Servlet handler. Shelly1: http request, Shelly2: WebSocket call
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse resp)
            throws ServletException, IOException, IllegalArgumentException {
        String path = getString(request.getRequestURI()).toLowerCase();

        if (path.equals(SHELLY2_CALLBACK_URI)) { // Shelly2 WebSocket
            super.service(request, resp);
            return;
        }

        // Shelly1: http events, URL looks like
        // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/relay/n?xxxxx or
        // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/roller/n?xxxxx or
        // <ip address>:<remote port>/shelly/event/shellyht-XXXXXX/sensordata?hum=53,temp=26.50
        String deviceName = "";
        String index = "";
        String type = "";
        try {
            String ipAddress = request.getRemoteAddr();
            Map<String, String[]> parameters = request.getParameterMap();
            logger.debug("ShellyEventServlet: {} Request from {}:{}{}?{}", request.getProtocol(), ipAddress,
                    request.getRemotePort(), path, parameters.toString());
            if (!path.toLowerCase().startsWith(SHELLY1_CALLBACK_URI) || !path.contains("/event/shelly")) {
                logger.warn("ShellyEventServlet received unknown request: path = {}", path);
                return;
            }

            deviceName = substringBetween(path, "/event/", "/").toLowerCase();
            if (path.contains("/" + EVENT_TYPE_RELAY + "/") || path.contains("/" + EVENT_TYPE_ROLLER + "/")
                    || path.contains("/" + EVENT_TYPE_LIGHT + "/")) {
                index = substringAfterLast(path, "/").toLowerCase();
                type = substringBetween(path, deviceName + "/", "/" + index);
            } else {
                index = "";
                type = substringAfterLast(path, "/").toLowerCase();
            }
            logger.trace("{}: Process event of type type={}, index={}", deviceName, type, index);

            Map<String, String> parms = new TreeMap<>();
            for (Map.Entry<String, String[]> p : parameters.entrySet()) {
                parms.put(p.getKey(), p.getValue()[0]);

            }
            handlerFactory.onEvent(ipAddress, deviceName, index, type, parms);
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Exception processing callback: path={}; index={}, type={}, parameters={}", deviceName,
                    path, index, type, request.getParameterMap().toString());
        } finally {
            resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            resp.getWriter().write("");
        }
    }

    /*
     * @Override
     * public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     * {
     * response.getWriter().println("HTTP GET method not implemented.");
     * }
     */
    /**
     * WebSocket: register Shelly2RpcSocket class
     */
    @Override
    public void configure(@Nullable WebSocketServletFactory factory) {
        if (factory != null) {
            factory.getPolicy().setIdleTimeout(15000);
            factory.setCreator(new Shelly2WebSocketCreator(thingTable));
            factory.register(Shelly2RpcSocket.class);
        }
    }

    public static class Shelly2WebSocketCreator implements WebSocketCreator {
        private final Logger logger = LoggerFactory.getLogger(Shelly2WebSocketCreator.class);

        private final ShellyThingTable thingTable;

        public Shelly2WebSocketCreator(ShellyThingTable thingTable) {
            this.thingTable = thingTable;
        }

        @Override
        public Object createWebSocket(@Nullable ServletUpgradeRequest req, @Nullable ServletUpgradeResponse resp) {
            logger.debug("WebSocket: Create socket from servlet");
            return new Shelly2RpcSocket(thingTable, true);
        }
    }
}

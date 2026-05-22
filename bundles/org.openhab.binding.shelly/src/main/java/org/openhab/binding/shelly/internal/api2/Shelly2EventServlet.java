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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.getString;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.osgi.service.component.ComponentException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link Shelly2EventServlet} implements the WebSocket callback for Gen2 devices
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@WebServlet(name = "Shelly2EventServlet", urlPatterns = { SHELLY2_CALLBACK_URI })
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class Shelly2EventServlet extends JettyWebSocketServlet {
    private static final long serialVersionUID = -1210354558091063207L;
    private final Logger logger = LoggerFactory.getLogger(Shelly2EventServlet.class);

    private final ShellyThingTable thingTable;
    private final WebSocketClient webSocketClient;

    @Activate
    public Shelly2EventServlet(@Reference ShellyThingTable thingTable, @Reference WebSocketFactory webSocketFactory) {
        this.thingTable = thingTable;
        WebSocketClient client = Shelly2RpcSocket.createWebSocketClient(webSocketFactory, "shelly2servlet");
        this.webSocketClient = client;
        try {
            client.start();
            logger.debug("Shelly2EventServlet started at {}", SHELLY2_CALLBACK_URI);
        } catch (Exception e) {
            logger.warn("Failed to start servlet WebSocket client: {}", e.getMessage(), e);
            throw new ComponentException("Failed to activate: Unable to start WebSocket client: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Shelly2EventServlet: Stopping");
        try {
            webSocketClient.stop();
        } catch (Exception e) {
            logger.warn("Failed to stop servlet WebSocket client: {}", e.getMessage(), e);
        }
    }

    /**
     * Servlet handler. WebSocket call.
     */
    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException, IllegalArgumentException {
        if (request == null) {
            logger.trace("Shelly2EventServlet.service unexpectedly received a null request. Request not processed");
            return;
        }
        String path = getString(request.getRequestURI()).toLowerCase(Locale.ROOT);
        if (!path.equals(SHELLY2_CALLBACK_URI)) {
            logger.warn("Shelly2EventServlet received unknown request: path = {}", path);
            return;
        }

        if (resp != null) {
            super.service(request, resp);
        }
    }

    /**
     * WebSocket: register Shelly2RpcSocket class
     */
    @Override
    @org.eclipse.jdt.annotation.NonNullByDefault({})
    public void configure(JettyWebSocketServletFactory factory) {
        factory.addMapping("/*", new Shelly2WebSocketCreator(thingTable, webSocketClient));
    }

    public static class Shelly2WebSocketCreator implements JettyWebSocketCreator {
        private final Logger logger = LoggerFactory.getLogger(Shelly2WebSocketCreator.class);

        private final ShellyThingTable thingTable;
        private final WebSocketClient webSocketClient;

        // A dedicated thread pool isn't needed - but passing the one from the ThingHander here is complicated,
        // which is why we simply acquire the same thread pool from the source.
        private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingHandler");

        public Shelly2WebSocketCreator(ShellyThingTable thingTable, WebSocketClient webSocketClient) {
            this.thingTable = thingTable;
            this.webSocketClient = webSocketClient;
        }

        @Override
        public Object createWebSocket(@Nullable JettyServerUpgradeRequest req,
                @Nullable JettyServerUpgradeResponse resp) throws IOException {
            if (logger.isDebugEnabled()) {
                logger.debug("WebSocket: Inbound servlet request from {}",
                        req != null ? req.getHttpServletRequest().getRemoteHost() : "");
            }
            return new Shelly2RpcSocket(thingTable, true, webSocketClient, scheduler);
        }
    }
}

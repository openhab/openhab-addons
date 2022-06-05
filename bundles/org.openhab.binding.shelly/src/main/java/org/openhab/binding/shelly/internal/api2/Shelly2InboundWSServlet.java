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
package org.openhab.binding.shelly.internal.api2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shelly2InboundWSServlet} implements the WebSocket callback for Gen2 devices
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@WebServlet(name = "Shelly WebSocket Servlet", urlPatterns = { "/shelly/wsevent" })
@Component(service = Shelly2InboundWSServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class Shelly2InboundWSServlet extends WebSocketServlet {
    private static final long serialVersionUID = -1210354558091063207L;
    private final Logger logger = LoggerFactory.getLogger(Shelly2InboundWSServlet.class);
    private final ShellyThingTable thingTable;

    @Activate
    public Shelly2InboundWSServlet(@Reference HttpService httpService, @Reference ShellyThingTable thingTable) {
        this.thingTable = thingTable;
        try {
            httpService.registerServlet("/shelly/wsevent", this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException | IllegalArgumentException e) {
            logger.warn("Unable to initialize WebSocket servlet", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("HTTP GET method not implemented.");
    }

    @Override
    public void configure(@Nullable WebSocketServletFactory factory) {
        if (factory != null) {
            factory.getPolicy().setIdleTimeout(15000);
            // factory.register(Shelly2InboundWS.class);
            factory.setCreator(new Shelly2WebSocketCreator(thingTable));
            factory.register(Shelly2WebSocket.class);
        }
    }

    public static class Shelly2WebSocketCreator implements WebSocketCreator {
        private final ShellyThingTable thingTable;

        public Shelly2WebSocketCreator(ShellyThingTable thingTable) {
            this.thingTable = thingTable;
        }

        @Override
        public Object createWebSocket(@Nullable ServletUpgradeRequest req, @Nullable ServletUpgradeResponse resp) {
            return new Shelly2WebSocket(thingTable, true);
        }
    }
}

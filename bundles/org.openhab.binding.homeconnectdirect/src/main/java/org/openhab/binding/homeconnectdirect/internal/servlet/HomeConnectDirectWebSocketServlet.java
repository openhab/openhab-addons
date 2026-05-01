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
package org.openhab.binding.homeconnectdirect.internal.servlet;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_WEB_SOCKET_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_WEB_SOCKET_PATTERN;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SUPPORTED_THING_TYPES;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.SLASH;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletUtils.filterOutMessage;

import java.io.IOException;
import java.io.Serial;
import java.util.function.Consumer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils;
import org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils;
import org.openhab.binding.homeconnectdirect.internal.handler.BaseHomeConnectDirectHandler;
import org.openhab.binding.homeconnectdirect.internal.handler.model.ApplianceMessage;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.MessageFilter;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 *
 * Home Connect Direct web socket servlet. Proxy values from the appliance to the web console.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
@WebServlet(urlPatterns = { SERVLET_WEB_SOCKET_PATTERN })
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class HomeConnectDirectWebSocketServlet extends WebSocketServlet {

    @Serial
    private static final long serialVersionUID = 3_406_770_341_849_696_274L;

    private final Logger logger;
    private final ThingRegistry thingRegistry;
    private final ConfigurationAdmin configurationAdmin;
    private final ServletSecurityContext securityContext;
    private final Gson gson;

    @Activate
    public HomeConnectDirectWebSocketServlet(@Reference ThingRegistry thingRegistry,
            @Reference ConfigurationAdmin configurationAdmin) {
        this.logger = LoggerFactory.getLogger(HomeConnectDirectWebSocketServlet.class);
        this.thingRegistry = thingRegistry;
        this.configurationAdmin = configurationAdmin;
        this.securityContext = ServletSecurityContext.get();
        this.gson = ConfigurationUtils.createGson();
    }

    @Override
    public void configure(@Nullable WebSocketServletFactory factory) {
        if (factory != null) {
            factory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> {

                if (!securityContext.isValidAuthorization(servletUpgradeRequest.getHttpServletRequest(),
                        configurationAdmin)) {
                    logger.debug("WebSocket authorization failed!");
                    servletUpgradeResponse.setStatusCode(HttpStatus.UNAUTHORIZED_401);
                    return null;
                }

                var filter = getFilterFromRequest(servletUpgradeRequest.getHttpServletRequest());
                var path = servletUpgradeRequest.getRequestURI().getPath();
                var uid = StringUtils.substringAfter(path, SERVLET_WEB_SOCKET_PATH + SLASH);
                var thingHandler = getThingHandler(uid);

                if (thingHandler != null) {
                    return new HomeConnectDirectWebSocketHandler(thingHandler, gson, filter);
                } else {
                    logger.debug("Could not find ThingHandler for WebSocket connection! uid={}", uid);
                    servletUpgradeResponse.setStatusCode(HttpStatus.NOT_FOUND_404);
                    return null;
                }
            });
        } else {
            logger.warn("Could not configure WebSocket Servlet!");
        }
    }

    private @Nullable BaseHomeConnectDirectHandler getThingHandler(String uid) {
        return thingRegistry.stream().filter(thing -> SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID()))
                .filter(thing -> thing.getUID().toString().equals(uid)).map(Thing::getHandler)
                .filter(BaseHomeConnectDirectHandler.class::isInstance).map(BaseHomeConnectDirectHandler.class::cast)
                .findFirst().orElse(null);
    }

    protected @Nullable MessageFilter getFilterFromRequest(HttpServletRequest request) {
        var filterParameter = request.getParameter("filter");
        if (filterParameter != null) {
            try {
                return gson.fromJson(filterParameter, MessageFilter.class);
            } catch (Exception e) {
                logger.debug("Could not parse filter from request! error={}", e.getMessage());
            }
        }
        return null;
    }

    @WebSocket
    protected static class HomeConnectDirectWebSocketHandler {
        private static final String PING_MESSAGE = "PING";
        private static final String PONG_MESSAGE = "PONG";

        private final Logger logger;
        private final BaseHomeConnectDirectHandler thingHandler;
        private final Gson gson;
        private final Consumer<ApplianceMessage> eventConsumer;
        private final @Nullable MessageFilter filter;
        private @Nullable Session session;

        public HomeConnectDirectWebSocketHandler(BaseHomeConnectDirectHandler thingHandler, Gson gson,
                @Nullable MessageFilter filter) {
            this.thingHandler = thingHandler;
            this.logger = LoggerFactory.getLogger(HomeConnectDirectWebSocketHandler.class);
            this.gson = gson;
            this.eventConsumer = this::sendMessage;
            this.filter = filter;
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            this.session = session;
            thingHandler.getApplianceMessages().forEach(this::sendMessage);
            thingHandler.registerApplianceMessageListener(eventConsumer);
        }

        @OnWebSocketMessage
        public void onMessage(Session session, String message) {
            if (PING_MESSAGE.equals(message)) {
                try {
                    session.getRemote().sendString(PONG_MESSAGE);
                } catch (IOException e) {
                    logger.debug("Could not send PONG! error={}", e.getMessage());
                }
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            this.session = null;
            thingHandler.removeApplianceMessageListener(eventConsumer);
        }

        @OnWebSocketError
        public void onError(Throwable cause) {
            logger.debug("WebSocket error occurred: {}", cause.getMessage());
            this.session = null;
            thingHandler.removeApplianceMessageListener(eventConsumer);
        }

        public void sendMessage(ApplianceMessage message) {
            var currentSession = this.session;
            if (currentSession != null && !filterOutMessage(message, filter)) {
                try {
                    currentSession.getRemote().sendString(gson.toJson(message));
                } catch (IOException e) {
                    logger.debug("Could not send web socket message! error={}", e.getMessage());
                }
            }
        }
    }
}

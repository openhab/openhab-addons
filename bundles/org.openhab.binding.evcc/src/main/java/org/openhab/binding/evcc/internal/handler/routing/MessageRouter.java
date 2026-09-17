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
package org.openhab.binding.evcc.internal.handler.routing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * Routes incoming websocket messages to handlers based on registered routes.
 *
 * This router provides:
 * - Centralized extraction logic
 * - Clear separation of concerns between routing and handler logic
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class MessageRouter {

    private final Logger logger = LoggerFactory.getLogger(MessageRouter.class);
    private final List<HandlerRoute> routes = new ArrayList<>();

    /**
     * Register a handler route.
     */
    public void registerRoute(HandlerRoute route) {
        routes.add(route);
        logger.debug("Registered route: {}", route.describe());
    }

    /**
     * Route an incoming message.
     *
     * @param key The message key (e.g., "battery", "pv", "grid")
     * @param value The message value
     */
    public boolean route(String key, JsonElement value) {
        boolean matched = false;
        for (HandlerRoute route : routes) {
            if (route.matches(key)) {
                matched = true;
                JsonElement processed = route.process(value);
                if (processed != null) {
                    try {
                        route.getHandler().handleUpdate(route.getDispatchKey(), processed);
                    } catch (Exception e) {
                        logger.warn("Handler failed to process routed message for {}", route.getRouteKey(), e);
                    }
                } else {
                    logger.debug("Message extraction failed for route: {}", route.getRouteKey());
                }
            }
        }
        return matched;
    }
}

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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.handler.EvccThingLifecycleAware;
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
    // CopyOnWriteArrayList allows safe concurrent iteration in route() while
    // registerRoute()/unregisterRoutes() may be invoked from handler lifecycle callbacks.
    private final List<HandlerRoute> routes = new CopyOnWriteArrayList<>();

    /**
     * Register a handler route.
     */
    public void registerRoute(HandlerRoute route) {
        routes.add(route);
        logger.debug("Registered route: {}", route.describe());
    }

    /**
     * Remove all routes registered for the given handler.
     *
     * Must be called when a handler is disposed so that the router stops dispatching
     * updates to it; otherwise stale routes would keep invoking the disposed handler
     * for as long as the bridge (and therefore this router) remains alive.
     *
     * @param handler The handler whose routes should be removed
     */
    public void unregisterRoutes(EvccThingLifecycleAware handler) {
        boolean removed = routes.removeIf(route -> handler.equals(route.getHandler()));
        if (removed) {
            logger.debug("Unregistered all routes for handler: {}", handler);
        }
    }

    /**
     * Route an incoming message.
     *
     * @param key The message key (e.g., "battery", "pv", "grid")
     * @param value The message value
     */
    public boolean route(String key, JsonElement value) {
        return route(key, value, null);
    }

    /**
     * Route an incoming message, optionally scoped to a single changed segment.
     * <p>
     * Several routes can share the same {@code key} (e.g. the co2/feedin/grid/solar forecast
     * routes all match "forecast"), each extracting its own sub-property from the same merged
     * cache value. When the caller knows that only one specific top-level property actually
     * changed (e.g. a "forecast.solar" delta), passing it as {@code changedSegment} lets sibling
     * routes whose statically known {@link HandlerRoute#getTargetKey()} differs be skipped,
     * instead of redundantly redispatching their unchanged, merged-cache data.
     *
     * @param key The message key (e.g., "battery", "pv", "grid")
     * @param value The message value
     * @param changedSegment The single top-level property known to have changed, or {@code null}
     *            when this is not determinable (all matching routes are then considered)
     */
    public boolean route(String key, JsonElement value, @Nullable String changedSegment) {
        boolean matched = false;
        for (HandlerRoute route : routes) {
            if (route.matches(key)) {
                if (changedSegment != null) {
                    String targetKey = route.getTargetKey();
                    if (targetKey != null && !targetKey.equals(changedSegment)) {
                        logger.trace("Skipping route {} - update only affects segment '{}'", route.describe(),
                                changedSegment);
                        continue;
                    }
                }
                logger.trace("Route matched for key '{}': {}", key, route.describe());
                matched = true;
                JsonElement processed = route.process(value);
                if (processed != null) {
                    logger.trace("Extraction succeeded, dispatching to handler with dispatch key '{}'",
                            route.getDispatchKey());
                    EvccThingLifecycleAware handler = route.getHandler();
                    // Synchronize on the handler instance itself - the same monitor its own
                    // dispose() synchronizes on - so the disposed check and the dispatch call
                    // are atomic with respect to concurrent disposal (e.g. during
                    // reinitialization).
                    synchronized (handler) {
                        if (handler.isDisposed()) {
                            logger.debug("Skipping dispatch to already disposed handler for route: {}",
                                    route.getRouteKey());
                            continue;
                        }
                        try {
                            @Nullable
                            StateTransformer transformer = route.getTransformer();
                            if (transformer != null && processed.isJsonObject()) {
                                handler.applyNormalizedUpdate(transformer.transform(processed.getAsJsonObject()));
                            } else {
                                handler.handleUpdate(route.getDispatchKey(), processed);
                            }
                        } catch (Exception e) {
                            logger.warn("Handler failed to process routed message for {}", route.getRouteKey(), e);
                        }
                    }
                } else {
                    logger.debug("Message extraction failed for route: {}", route.getRouteKey());
                }
            }
        }
        if (!matched && !routes.isEmpty()) {
            logger.trace("No route matched for key '{}'", key);
        }
        return matched;
    }
}

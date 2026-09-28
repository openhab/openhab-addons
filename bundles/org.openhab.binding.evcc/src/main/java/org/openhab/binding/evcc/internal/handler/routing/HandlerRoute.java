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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.handler.EvccThingLifecycleAware;

import com.google.gson.JsonElement;

/**
 * Configuration for routing a message to a handler.
 *
 * Combines an extraction strategy with a target handler.
 * A route defines the pipeline: incoming message → extraction → handler dispatch.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class HandlerRoute {

    private final String routeKey;
    private final ExtractionStrategy extraction;
    private final EvccThingLifecycleAware handler;
    private final String dispatchKey;
    private final @Nullable StateTransformer transformer;

    /**
     * Create a handler route with extraction, handler, and dispatch key.
     *
     * @param routeKey The incoming message key to match (e.g., "battery", "pv")
     * @param extraction The extraction strategy to apply to the message
     * @param handler The target handler to dispatch to
     * @param dispatchKey The key to pass to the handler's handleUpdate method
     */
    public HandlerRoute(String routeKey, ExtractionStrategy extraction, EvccThingLifecycleAware handler,
            String dispatchKey) {
        this(routeKey, extraction, handler, dispatchKey, null);
    }

    /**
     * Create a handler route that normalizes the extracted data before dispatch.
     *
     * @param routeKey The incoming message key to match (e.g., "grid", "loadpoints")
     * @param extraction The extraction strategy to apply to the message
     * @param handler The target handler to dispatch to
     * @param dispatchKey The key to pass to the handler's handleUpdate method
     * @param transformer The transformer applied to the extracted object before dispatch, or {@code null} for none
     */
    public HandlerRoute(String routeKey, ExtractionStrategy extraction, EvccThingLifecycleAware handler,
            String dispatchKey, @Nullable StateTransformer transformer) {
        this.routeKey = routeKey;
        this.extraction = extraction;
        this.handler = handler;
        this.dispatchKey = dispatchKey;
        this.transformer = transformer;
    }

    /**
     * Get the transformer that normalizes extracted data before dispatch.
     *
     * @return The transformer, or {@code null} when the extracted data is dispatched unchanged
     */
    @Nullable
    public StateTransformer getTransformer() {
        return transformer;
    }

    /**
     * Process an incoming message through this route's extraction pipeline.
     *
     * @param message The raw incoming message
     * @return The extracted data, or null if extraction failed
     */
    @Nullable
    public JsonElement process(JsonElement message) {
        return extraction.extract(message);
    }

    /**
     * Get the immediate top-level property name this route's extraction targets, if statically
     * determinable.
     *
     * @return the target key, or {@code null} when it cannot be determined
     * @see ExtractionStrategy#getTargetKey()
     */
    @Nullable
    public String getTargetKey() {
        return extraction.getTargetKey();
    }

    /**
     * Get the incoming message key that this route matches.
     *
     * @return The route key (e.g., "battery", "grid")
     */
    public String getRouteKey() {
        return routeKey;
    }

    /**
     * Get the target handler for this route.
     *
     * @return The handler that will receive processed messages
     */
    public EvccThingLifecycleAware getHandler() {
        return handler;
    }

    /**
     * Get the dispatch key to pass to the handler.
     *
     * @return The key passed to handler.handleUpdate(dispatchKey, data)
     */
    public String getDispatchKey() {
        return dispatchKey;
    }

    /**
     * Check whether this route matches the incoming message key.
     *
     * @param key The incoming message key
     * @return true when this route should process the message
     */
    public boolean matches(String key) {
        return routeKey.equals(key);
    }

    /**
     * Get a human-readable description of this route.
     *
     * @return A descriptive string showing the route configuration
     */
    public String describe() {
        return "Route(" + routeKey + " -> " + dispatchKey + "): " + extraction.describe();
    }
}

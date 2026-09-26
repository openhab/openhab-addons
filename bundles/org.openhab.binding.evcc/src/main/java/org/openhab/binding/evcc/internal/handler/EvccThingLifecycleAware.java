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
package org.openhab.binding.evcc.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EvccThingLifecycleAware} is responsible for sharing the evcc api response
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public interface EvccThingLifecycleAware {
    /**
     * This method shall create the channels from the JSON received from the evcc API
     * 
     * @param state the responded JSON
     */
    void initializeThingFromLatestState(JsonObject state);

    /**
     * This method shall return the to the thing corresponding JSON object
     * 
     * @param state the cached API JSON response
     * @return to the thing corresponding JSON object
     */
    JsonObject getStateFromCachedState(JsonObject state);

    /**
     * This method shall handle the mini updates received from the evcc WS
     *
     * @param key the key of the updated value
     * @param value the updated value
     */
    void handleUpdate(String key, JsonElement value);

    /**
     * Apply an already-normalized update whose members map directly to channel keys.
     *
     * Used by the router for routes that carry a {@code StateTransformer}: the transformation has already reshaped
     * the extracted data into final channel keys, so the members are applied as-is (without the dispatch-key
     * prefixing performed by {@link #handleUpdate}).
     *
     * @param normalized The normalized update whose members map directly to channel keys
     */
    void applyNormalizedUpdate(JsonObject normalized);

    /**
     * This method shall return the type of the thing
     *
     * @return the type of the thing
     */
    String getType();

    /**
     * This method shall return the index of the thing (if applicable)
     *
     * @return the index of the thing
     */
    Object getIdentifier();

    /**
     * Whether this handler has started or completed disposal.
     *
     * Callers that dispatch updates to this handler asynchronously (e.g. from websocket
     * callbacks) must check this - while synchronized on the handler instance together with
     * the handler's own dispose() - before invoking any other method on it. This closes the
     * race window where a message is already in flight to a handler that is concurrently
     * being disposed (for example during binding reinitialization), which would otherwise
     * reach openHAB core APIs (isLinked(), updateState(), ...) after the framework has
     * detached the handler's callback.
     *
     * @return true once dispose() has started (or completed) for this handler
     */
    boolean isDisposed();
}

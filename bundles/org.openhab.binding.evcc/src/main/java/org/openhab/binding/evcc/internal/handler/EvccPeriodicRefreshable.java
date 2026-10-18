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

import com.google.gson.JsonObject;

/**
 * Implemented by handlers whose data is only delivered as part of evcc's initial/full-state websocket
 * push and is not (reliably) pushed again via partial updates. The {@link EvccBridgeHandler} periodically
 * fetches a fresh full state snapshot via HTTP and forwards it to every registered handler that implements
 * this interface, so their channels do not go stale between full-state pushes.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public interface EvccPeriodicRefreshable {

    /**
     * Refresh this handler's channels from a freshly fetched full-state snapshot.
     *
     * @param state The full evcc state snapshot
     */
    void refreshFromState(JsonObject state);
}

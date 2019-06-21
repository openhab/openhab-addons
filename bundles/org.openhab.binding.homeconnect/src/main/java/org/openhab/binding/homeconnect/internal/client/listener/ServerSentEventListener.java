/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.client.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.model.Event;

/**
 * {@link ServerSentEventListener} inform about new events from Home Connect SSE interface.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public interface ServerSentEventListener {

    /**
     * Inform listener about new event
     *
     * @param event
     */
    void onEvent(Event event);

    /**
     * If SSE client did a reconnect
     */
    default void onReconnect() {
    }

    /**
     * If reconnect failed
     */
    default void onReconnectFailed() {
    }
}

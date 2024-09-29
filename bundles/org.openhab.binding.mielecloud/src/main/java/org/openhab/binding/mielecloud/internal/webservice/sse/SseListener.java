/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;

/**
 * Listens to events received via a SSE connection and errors concerning that connection.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public interface SseListener {
    /**
     * Called when an event is received via a SSE connection.
     *
     * @param event The received event.
     */
    void onServerSentEvent(ServerSentEvent event);

    /**
     * Called when an error occurs that is related to the connection and cannot be handled automatically.
     *
     * @param connectionError The connection error.
     * @param failedReconnectAttempts The number of attempts that were made to reconnect to the event stream.
     */
    void onConnectionError(ConnectionError connectionError, int failedReconnectAttempts);
}

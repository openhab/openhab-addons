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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener for the connection status.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public interface ConnectionStatusListener {
    /**
     * Called regularly while the connection is up and running.
     */
    void onConnectionAlive();

    /**
     * Called when a connection error is encountered.
     *
     * @param connectionError The error.
     * @param failedReconnectAttempts The number of failed attempts to reconnect.
     */
    void onConnectionError(ConnectionError connectionError, int failedReconnectAttempts);
}

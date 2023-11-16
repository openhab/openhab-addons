/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal.owserver;

/**
 * The {@link OwserverConnectionState} defines the state for connections to owservers
 *
 * @author Jan N. Klug - Initial contribution
 */

public enum OwserverConnectionState {
    /**
     * The {@link OwserverConnection} is being torn down (mostly due to dispose of handler).
     * No refresh, etc. are possible.
     */
    STOPPED,
    /**
     * The connection is open.
     */
    OPENED,
    /**
     * The connection is closed. On next read / write it will be opened.
     */
    CLOSED,
    /**
     * The connection is erroneous and was closed by the {@link OwserverConnection}. After due wait time, it
     * is tried to reopen it by a scheduled task of
     * {@link org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler#reportConnectionState(OwserverConnectionState)}.
     */
    FAILED
}

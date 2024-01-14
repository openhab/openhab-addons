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
package org.openhab.binding.samsungtv.internal.service.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 * Interface for receiving data from Samsung TV services.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Arjan Mels - Added methods to put/get configuration
 */
@NonNullByDefault
public interface EventListener {
    /**
     * Invoked when value is received from the TV.
     *
     * @param variable Name of the variable.
     * @param value Value of the variable value.
     */
    void valueReceived(String variable, State value);

    /**
     * Report an error to this event listener
     *
     * @param statusDetail hint about the actual underlying problem
     * @param message of the error
     * @param e exception that might have occurred
     */
    void reportError(ThingStatusDetail statusDetail, String message, Throwable e);

    /**
     * Get configuration item
     *
     * @param key key of configuration item
     * @param value value of key
     */
    void putConfig(String key, Object value);

    /**
     * Put configuration item
     *
     * @param key key of configuration item
     * @return value of key
     */
    Object getConfig(String key);

    /**
     * Get WebSocket Factory
     *
     * @return WebSocket Factory
     */
    WebSocketFactory getWebSocketFactory();
}

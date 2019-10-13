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
package org.openhab.binding.deconz.internal.netutils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deconz.internal.dto.SensorState;

/**
 * Informs about updated sensor states
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface WebSocketValueUpdateListener {
    /**
     * A sensor state was updated.
     *
     * @param sensorID The sensor ID (API endpoint)
     * @param newState The new state
     */
    void websocketUpdate(String sensorID, SensorState newState);
}

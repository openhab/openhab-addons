/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyWebSocketRealtimeUpdate;

/**
 * The {@link SenseEnergyWebSocket } interface for callbacks pertaining to the web socket
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public interface SenseEnergyWebSocketListener {
    /**
     * called when web socket connects
     */
    void onWebSocketConnect();

    /**
     * called when the web socket is closed
     * 
     * @param statusCode
     * @param reason
     */
    void onWebSocketClose(int statusCode, @Nullable String reason);

    /**
     * called when there is an error on the web socket
     * 
     * @param msg
     */
    void onWebSocketError(String msg);

    /**
     * called with an updated energy usage report
     * 
     * @param update
     */
    void onWebSocketRealtimeUpdate(SenseEnergyWebSocketRealtimeUpdate update);
}

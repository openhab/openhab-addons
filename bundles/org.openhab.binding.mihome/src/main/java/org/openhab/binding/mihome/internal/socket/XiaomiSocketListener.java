/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mihome.internal.socket;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Interface for a listener on the {@link XiaomiSocket}.
 * When it is registered on the socket, it gets called back each time, the {@link XiaomiSocket} receives data.
 *
 * @author Patrick Boos - Initial contribution
 */
@NonNullByDefault
public interface XiaomiSocketListener {
    /**
     * Callback method for the {@link XiaomiSocketListener}
     *
     * @param message - The received message in JSON format
     */
    void onDataReceived(JsonObject message);
}

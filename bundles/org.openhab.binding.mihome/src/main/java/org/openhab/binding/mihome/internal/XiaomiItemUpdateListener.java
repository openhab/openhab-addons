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
package org.openhab.binding.mihome.internal;

import com.google.gson.JsonObject;

/**
 * Listener for item/sensor updates.
 *
 * @author Patrick Boos - Initial contribution
 */
public interface XiaomiItemUpdateListener {
    /**
     * Callback method to notify the listener about a device state update
     *
     * @param sid the itemID of the device
     * @param command the command type of the received message
     * @param message the received message
     *
     * @author Patrick Boos - Initial contribution
     */

    void onItemUpdate(String sid, String command, JsonObject message);

    /**
     * Returns the itemID, to which the listener listens
     * 
     * @return itemID of the device
     */
    String getItemId();
}

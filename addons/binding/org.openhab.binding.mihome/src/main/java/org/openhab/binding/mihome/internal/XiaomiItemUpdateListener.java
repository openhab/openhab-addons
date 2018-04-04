/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

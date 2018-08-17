/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.message;

import com.google.gson.annotations.SerializedName;

/**
 * Transition message POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class Transition extends AbstractBaseMessage {
    //events
    private static final String EVENT_ENTER = "enter";
    private static final String EVENT_LEAVE = "leave";

    /**
     * Event that triggered the transition (iOS,Android/string/required)
     *      enter The device entered the defined geographical region or BLE Beacon range (iOS)
     *      leave The device left the defined geographical region or BLE Beacon range (iOS)
     */
    @SerializedName("event")
    String event;

    /**
     * Name of the waypoint (iOS,Android/string/optional)
     */
    @SerializedName("desc")
    String regionName;

    public String getRegionName() {
        return regionName;
    }

    public boolean isEntering() {
        return EVENT_ENTER.equals(event);
    }

    public boolean isLeaving() {
        return EVENT_LEAVE.equals(event);
    }
}

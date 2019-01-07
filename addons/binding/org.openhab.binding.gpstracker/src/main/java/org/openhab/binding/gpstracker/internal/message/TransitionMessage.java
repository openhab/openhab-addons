/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.message;

import com.google.gson.annotations.SerializedName;

/**
 * TransitionMessage message POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TransitionMessage extends LocationMessage {

    /**
     * Event that triggered the transition (iOS,Android/string/required)
     *      enter The tracker entered the defined geographical region or BLE Beacon range (iOS)
     *      leave The tracker left the defined geographical region or BLE Beacon range (iOS)
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

    public String getEvent() {
        return event;
    }
}

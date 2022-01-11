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
     * enter The tracker entered the defined geographical region or BLE Beacon range (iOS)
     * leave The tracker left the defined geographical region or BLE Beacon range (iOS)
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

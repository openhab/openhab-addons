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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import com.google.gson.annotations.SerializedName;

/**
 * A room as represented by the controller.
 *
 * Json example:
 * {"@type":"room","id":"hz_1","iconId":"icon_room_bedroom","name":"Bedroom"}
 *
 * @author Stefan Kästle - Initial contribution
 */
public class Room {

    @SerializedName("@type")
    public String type;

    public String id;
    public String name;
}

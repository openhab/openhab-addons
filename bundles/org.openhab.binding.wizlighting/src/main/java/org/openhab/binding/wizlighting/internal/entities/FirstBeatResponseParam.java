/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents the "params" returned in a "firstBeat"
 *
 * The incoming JSON looks like this:
 *
 * {"method": "firstBeat", "id": 0, "env": "pro", "params": {"mac": "bulbMacAddress",
 * "homeId": xxxxxx, "fwVersion": "1.15.2"}}
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class FirstBeatResponseParam {
    // The MAC address the response is coming from
    @Expose(serialize = true, deserialize = true)
    public String mac = "bulbMacAddress";
    // Home ID of the bulb
    @Expose(serialize = false, deserialize = true)
    public int homeId;
    // Firmware version of the bulb
    @Expose(serialize = true, deserialize = true)
    public String fwVersion = "1.15.2";
}

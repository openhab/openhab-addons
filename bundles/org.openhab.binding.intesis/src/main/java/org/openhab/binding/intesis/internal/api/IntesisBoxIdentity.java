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
package org.openhab.binding.intesis.internal.api;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Rocky Amatulli - Initial contribution
 */
@NonNullByDefault
public class IntesisBoxIdentity {

    public HashMap<String, String> value = new HashMap<String, String>();

    public IntesisBoxIdentity(String data) {
        String[] value = data.substring(3).split(",");
        this.value.put("MODEL", value[0]); // The Intesis device model reference
        this.value.put("MAC", value[1]); // The 6 bytes of the MAC address
        this.value.put("IP", value[2]); // The IP address of the IntesisBox
        this.value.put("PROTOCOL", value[3]); // The external protocol supported
        this.value.put("VERSION", value[4]); // The firmware version running in the device
        this.value.put("RSSI", value[5]); // The received Signal Strength Indication for the Wi-Fi
        this.value.put("NAME", value[6]); // The host name of the IntesisBox
    }
}

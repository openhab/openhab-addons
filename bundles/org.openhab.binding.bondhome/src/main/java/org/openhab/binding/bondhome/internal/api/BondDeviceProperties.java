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
package org.openhab.binding.bondhome.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This POJO represents the properties of a Bond device
 *
 * The incoming JSON looks like this:
 *
 * {"max_speed": 3, "trust_state": false, "addr": "10101", "freq": 434300, "bps": 3000, "zero_gap": 30}
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondDeviceProperties {
    // The current properties hash
    @SerializedName("_")
    @Expose(serialize = false, deserialize = true)
    public @Nullable String hash;
    // The maximum speed of a fan
    @SerializedName("max_speed")
    @Expose(serialize = true, deserialize = true)
    public int maxSpeed;
    // Whether or not to "trust" that the device state remembered by the bond bridge is
    // correct for toggle switches
    @SerializedName("trust_state")
    @Expose(serialize = true, deserialize = true)
    public boolean trustState;
    // The device address
    @Expose(serialize = true, deserialize = true)
    public String addr = "";
    // The fan radio frequency
    @Expose(serialize = true, deserialize = true)
    public int freq;
    // Undocumented
    @Expose(serialize = true, deserialize = true)
    public int bps;
    // Undocumented
    @SerializedName("zero_gap")
    @Expose(serialize = true, deserialize = true)
    public int zeroGap;
}

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
package org.openhab.binding.bondhome.internal.api;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This POJO represents a bond device
 *
 * The incoming JSON looks like this:
 *
 * {"name": "My Fan", "type": "CF", "template": "A1", "location": "Kitchen",
 * "actions": {"_": "7fc1e84b"}, "properties": {"_": "84cd8a43"}, "state": {"_":
 * "ad9bcde4"}, "commands": {"_": "ad9bcde4" }}
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondDevice {
    // The current device hash
    @SerializedName("_")
    @Expose(serialize = false, deserialize = true)
    public @Nullable String hash;
    // The name associated with the device in the bond app
    @Expose(serialize = true, deserialize = true)
    public @Nullable String name;
    // The device type
    @Expose(serialize = true, deserialize = true)
    public BondDeviceType type = BondDeviceType.GENERIC_DEVICE;
    // The remote control template being used
    @Expose(serialize = true, deserialize = true)
    public @Nullable String template;
    // A list of the available actions
    @Expose(serialize = false, deserialize = true)
    public List<BondDeviceAction> actions = Arrays.asList(BondDeviceAction.TURN_ON);
    // The current hash of the properties object
    @Expose(serialize = false, deserialize = true)
    public @Nullable BondHash properties;
    // The current hash of the state object
    @Expose(serialize = false, deserialize = true)
    public @Nullable BondHash state;
    // The current hash of the commands object - only applies to a bridge
    @Expose(serialize = false, deserialize = true)
    public @Nullable BondHash commands;
}

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
package org.openhab.binding.bondhome.internal.api;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

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
    public String hash = API_HASH;
    // The name associated with the device in the bond app
    @Expose(serialize = true, deserialize = true)
    public String name = API_MISSING_DEVICE_NAME;
    // The device type
    @Expose(serialize = true, deserialize = true)
    public BondDeviceType type = BondDeviceType.GenericDevice;
    // The remote control template being used
    @Expose(serialize = true, deserialize = true)
    public String template = API_MISSING_TEMPLATE;
    // A list of the available actions
    @Expose(serialize = false, deserialize = true)
    public BondDeviceAction[] actions = { BondDeviceAction.TurnOn };
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

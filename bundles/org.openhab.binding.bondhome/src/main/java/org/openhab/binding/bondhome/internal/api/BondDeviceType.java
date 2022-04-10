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

import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * This enum represents the possible device types
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
public enum BondDeviceType {
    @SerializedName("CF")
    CeilingFan(THING_TYPE_BOND_FAN),
    @SerializedName("MS")
    MotorizedShades(THING_TYPE_BOND_SHADES),
    @SerializedName("FP")
    Fireplace(THING_TYPE_BOND_FIREPLACE),
    @SerializedName("GX")
    GenericDevice(THING_TYPE_BOND_GENERIC);

    private ThingTypeUID deviceTypeUid;

    private BondDeviceType(final ThingTypeUID deviceTypeUid) {
        this.deviceTypeUid = deviceTypeUid;
    }

    /**
     * Gets the device type name for request deviceType
     *
     * @return the deviceType name
     */
    public ThingTypeUID getThingTypeUID() {
        return deviceTypeUid;
    }
}

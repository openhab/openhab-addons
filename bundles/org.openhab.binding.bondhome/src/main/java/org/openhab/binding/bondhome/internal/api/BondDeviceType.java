/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * This enum represents the possible device types
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public enum BondDeviceType {
    @SerializedName("CF")
    CEILING_FAN(THING_TYPE_BOND_FAN),
    @SerializedName("MS")
    MOTORIZED_SHADES(THING_TYPE_BOND_SHADES),
    @SerializedName("FP")
    FIREPLACE(THING_TYPE_BOND_FIREPLACE),
    @SerializedName("GX")
    GENERIC_DEVICE(THING_TYPE_BOND_GENERIC),
    @SerializedName("LT")
    LIGHT(THING_TYPE_BOND_LIGHT);

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

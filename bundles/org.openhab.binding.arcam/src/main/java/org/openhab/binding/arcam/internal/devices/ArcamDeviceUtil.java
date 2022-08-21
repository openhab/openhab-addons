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
package org.openhab.binding.arcam.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.arcam.internal.ArcamBindingConstants;
import org.openhab.binding.arcam.internal.ArcamZone;
import org.openhab.binding.arcam.internal.exceptions.NotFoundException;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * This class provides utility methods for the device classes
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamDeviceUtil {
    public static ArcamDevice getDeviceFromThingUID(ThingUID thingUID) {
        String id = thingUID.getAsString();

        if (id.contains(ArcamAVR5.AVR5)) {
            return new ArcamAVR5();
        }
        if (id.contains(ArcamAVR10.AVR10)) {
            return new ArcamAVR10();
        }
        if (id.contains(ArcamAVR20.AVR20)) {
            return new ArcamAVR20();
        }
        if (id.contains(ArcamAVR30.AVR30)) {
            return new ArcamAVR30();
        }
        if (id.contains(ArcamAVR40.AVR40)) {
            return new ArcamAVR40();
        }
        if (id.contains(ArcamSA10.SA10)) {
            return new ArcamSA10();
        }
        if (id.contains(ArcamSA20.SA20)) {
            return new ArcamSA20();
        }
        if (id.contains(ArcamSA30.SA30)) {
            return new ArcamSA30();
        }

        throw new NotFoundException("Could not find an Arcam device from the thingUID: " + id);
    }

    @Nullable
    public static ThingTypeUID getThingTypeUIDFromModelName(String modelName) {
        if (modelName.contains("AVR5")) {
            return ArcamBindingConstants.AVR5_THING_TYPE_UID;
        }
        if (modelName.contains("AVR10")) {
            return ArcamBindingConstants.AVR10_THING_TYPE_UID;
        }
        if (modelName.contains("AVR20")) {
            return ArcamBindingConstants.AVR20_THING_TYPE_UID;
        }
        if (modelName.contains("AVR30")) {
            return ArcamBindingConstants.AVR30_THING_TYPE_UID;
        }
        if (modelName.contains("AVR40")) {
            return ArcamBindingConstants.AVR40_THING_TYPE_UID;
        }
        if (modelName.contains("SA10")) {
            return ArcamBindingConstants.SA10_THING_TYPE_UID;
        }
        if (modelName.contains("SA20")) {
            return ArcamBindingConstants.SA20_THING_TYPE_UID;
        }
        if (modelName.contains("SA30")) {
            return ArcamBindingConstants.SA30_THING_TYPE_UID;
        }

        return null;
    }

    public static byte zoneToByte(ArcamZone zone) {
        if (zone == ArcamZone.MASTER) {
            return 0x01;
        }

        return 0x02;
    }
}

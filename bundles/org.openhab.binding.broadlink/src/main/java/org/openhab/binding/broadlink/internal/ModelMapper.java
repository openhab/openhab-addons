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
package org.openhab.binding.broadlink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;

/**
 * Mappings of internal values to user-visible ones.
 *
 * @author Cato Sognen - Initial contribution
 * @author John Marshall - V2 and V3 updates
 */
@NonNullByDefault
public class ModelMapper {
    private static final StringType UNKNOWN = new StringType("UNKNOWN");

    public static ThingTypeUID getThingType(int model, Logger logger) {
        logger.debug("Looking for thing type corresponding to model {}", model);

        switch (model) {
            case 0x0000:
                return BroadlinkBindingConstants.THING_TYPE_SP1;
            case 0x2717:
            case 0x2719:
            case 0x271A:
            case 0x2720:
            case 0x2728:
            case 0x273E:
            case 0x7530:
            case 0x7539:
            case 0x753E:
            case 0x7540:
            case 0x7544:
            case 0x7546:
            case 0x7547:
            case 0x7918:
            case 0x7919:
            case 0x791A:
            case 0x7D0D:
                return BroadlinkBindingConstants.THING_TYPE_SP2;
            case 0x2711:
            case 0x2716:
            case 0x271D:
            case 0x2736:
                return BroadlinkBindingConstants.THING_TYPE_SP2S;
            case 0x2733:
            case 0x7D00:
                return BroadlinkBindingConstants.THING_TYPE_SP3;
            case 0x9479:
            case 0x947A:
                return BroadlinkBindingConstants.THING_TYPE_SP3S;
            case 0x2737:
            case 0x278F:
            case 0x27C2:
            case 0x27C7:
            case 0x27CC:
            case 0x27CD:
            case 0x27D0:
            case 0x27D1:
            case 0x27D3:
            case 0x27DC:
            case 0x27DE:
                return BroadlinkBindingConstants.THING_TYPE_RM3;
            case 0x2712:
            case 0x272A:
            case 0x273D:
            case 0x277C:
            case 0x2783:
            case 0x2787:
            case 0x278B:
            case 0x2797:
            case 0x279D:
            case 0x27A1:
            case 0x27A6:
            case 0x27A9:
            case 0x27C3:
                return BroadlinkBindingConstants.THING_TYPE_RM_PRO;
            case 0x5F36:
            case 0x6507:
            case 0x6508:
                return BroadlinkBindingConstants.THING_TYPE_RM3Q;
            case 0x51DA:
            case 0x5209:
            case 0x520C:
            case 0x520D:
            case 0x5211:
            case 0x5212:
            case 0x5216:
            case 0x6070:
            case 0x610E:
            case 0x610F:
            case 0x62BC:
            case 0x62BE:
            case 0x6364:
            case 0x648D:
            case 0x6539:
            case 0x653A:
                return BroadlinkBindingConstants.THING_TYPE_RM4_MINI;
            case 0x520B:
            case 0x5213:
            case 0x5218:
            case 0x6026:
            case 0x6184:
            case 0x61A2:
            case 0x649B:
            case 0x653C:
                return BroadlinkBindingConstants.THING_TYPE_RM4_PRO;
            case 0x2714:
                return BroadlinkBindingConstants.THING_TYPE_A1;
            case 0x4EB5:
            case 0x4EF7:
            case 0x4F1B:
            case 0x4F65:
                return BroadlinkBindingConstants.THING_TYPE_MP1;
            default: {
                String modelAsHexString = Integer.toHexString(model);
                logger.warn(
                        "Device identifying itself as '{}' (0x{}) is not currently supported. Please report this to the developer!",
                        model, modelAsHexString);
                throw new UnsupportedOperationException("Device identifying itself as '" + model + "' (hex 0x"
                        + modelAsHexString + ") is not currently supported. Please report this to the developer!");
            }
        }
    }

    private static <T extends Enum<T>> StringType lookup(T[] values, byte b) {
        int index = Byte.toUnsignedInt(b);
        return index < values.length ? new StringType(values[index].toString()) : UNKNOWN;
    }

    private enum AirValue {
        PERFECT,
        GOOD,
        NORMAL,
        BAD
    }

    public static StringType getAirValue(byte b) {
        return lookup(AirValue.values(), b);
    }

    private enum LightValues {
        DARK,
        DIM,
        NORMAL,
        BRIGHT
    }

    public static StringType getLightValue(byte b) {
        return lookup(LightValues.values(), b);
    }

    private enum NoiseValues {
        QUIET,
        NORMAL,
        NOISY,
        EXTREME
    }

    public static StringType getNoiseValue(byte b) {
        return lookup(NoiseValues.values(), b);
    }
}

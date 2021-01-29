/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.slf4j.LoggerFactory;

/**
 * Mappings of internal values to user-visible ones.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class ModelMapper {

    private static final StringType UNKNOWN = new StringType("UNKNOWN");
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelMapper.class);

    public static ThingTypeUID getThingType(int model) {

        String modelAsHexString = Integer.toHexString(model);
        LOGGER.info(
                "Attempting to map Broadlink device with model '{}' (0x{})",
                model, modelAsHexString);

        if (model == 0)
            return BroadlinkBindingConstants.THING_TYPE_SP1;
        if (model == 0x2711)
            return BroadlinkBindingConstants.THING_TYPE_SP2;
        if (model == 0x2719 || model == 0x7919 || model == 0x271a || model == 0x791a)
            return BroadlinkBindingConstants.THING_TYPE_SP2; // Honeywell SP2
        if (model == 0x2720)
            return BroadlinkBindingConstants.THING_TYPE_SP2; // SPMini
        if (model == 0x753e)
            return BroadlinkBindingConstants.THING_TYPE_SP3; // SP mini 3 - does NOT have a nightlight
        if (model == 0x7d00)
            return BroadlinkBindingConstants.THING_TYPE_SP3; // OEM-branded SP3
        if (model == 0x947a || model == 0x9479)
            return BroadlinkBindingConstants.THING_TYPE_SP3; // NB: this is ACTUALLY an SP3S - see
                                                             // https://github.com/mjg59/python-broadlink/blob/master/broadlink/__init__.py
        if (model == 0x2728)
            return BroadlinkBindingConstants.THING_TYPE_SP2; // SPMini2
        if (model == 0x2733 || model == 0x273e)
            return BroadlinkBindingConstants.THING_TYPE_SP2; // OEM-branded SPMini
        if (model >= 0x7530 && model <= 0x7918)
            return BroadlinkBindingConstants.THING_TYPE_SP2; // OEM-branded SPMini2
        if (model == 0x2736)
            return BroadlinkBindingConstants.THING_TYPE_SP2; // SPMiniPlus
        if (model == 0x2712)
            return BroadlinkBindingConstants.THING_TYPE_RM2;
        if (model == 0x2737)
            return BroadlinkBindingConstants.THING_TYPE_RM3; // RM Mini
        if (model == 0x27c2)
            return BroadlinkBindingConstants.THING_TYPE_RM3; // RM Mini 3, firmware rev v40
        if (model == 0x5f36)
            return BroadlinkBindingConstants.THING_TYPE_RM3Q; // RM Mini 3, firmware rev v44057 - has sendCode quirk
                                                              // from RM4
        if (model == 0x51da)
            return BroadlinkBindingConstants.THING_TYPE_RM4; // RM4b
        if (model >= 0x6020 && model <= 0x602f)
            return BroadlinkBindingConstants.THING_TYPE_RM4; // RM4 Pro - wide range of identifiers exist
        if (model >= 0x610f && model <= 0x62bf)
            return BroadlinkBindingConstants.THING_TYPE_RM4; // RM4c - wide range of identifiers exist
        if (model == 0x6539)
            return BroadlinkBindingConstants.THING_TYPE_RM4; // RM4c Mini
        if (model == 0x610e)
            return BroadlinkBindingConstants.THING_TYPE_RM4; // RM4 Mini
        if (model == 0x61a2)
            return BroadlinkBindingConstants.THING_TYPE_RM4; // RM4 Pro
        if (model == 0x649b)
            return BroadlinkBindingConstants.THING_TYPE_RM4; // RM4 Pro
        if (model == 0x273d)
            return BroadlinkBindingConstants.THING_TYPE_RM; // RM Pro Phicomm
        if (model == 0x2783)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Home Plus
        if (model == 0x277c)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Home Plus GDT
        if (model == 0x272a)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro Plus
        if (model == 0x2787)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro Plus2
        if (model == 0x279d)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro Plus3
        if (model == 0x27a9)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro Plus_300
        if (model == 0x278b)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro Plus BL
        if (model == 0x2797)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro Plus HYC
        if (model == 0x27a1)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro Plus R1
        if (model == 0x27a6)
            return BroadlinkBindingConstants.THING_TYPE_RM2; // RM2 Pro PP
        if (model == 0x278f)
            return BroadlinkBindingConstants.THING_TYPE_RM; // RM Mini Shate
        if (model == 0x2714)
            return BroadlinkBindingConstants.THING_TYPE_A1;
        if (model == 0x4eb5)
            return BroadlinkBindingConstants.THING_TYPE_MP1;
        if (model == 0x4f65)
            return BroadlinkBindingConstants.THING_TYPE_MP1_1K3S2U;
        if (model == 20251)
            return BroadlinkBindingConstants.THING_TYPE_MP2;
        if (model == 0x4ef7)
            return BroadlinkBindingConstants.THING_TYPE_MP1; // Honyar OEM MP1
        // if (model == 0x2722)
        // return BroadlinkBindingConstants.THING_TYPE_S1C;
        // if (model == 0x4e4d)
        // return null;
        String modelAsHexString = Integer.toHexString(model);
        LOGGER.error(
                "Device identifying itself as '{}' (0x{}) is not currently supported. Please report this to the developer!",
                model, modelAsHexString);
        LOGGER.error(
                "Join the discussion at https://community.openhab.org/t/broadlink-binding-for-rmx-a1-spx-and-mp-any-interest/22768/616");
        throw new UnsupportedOperationException("Device identifying itself as '" + model + "' (hex 0x"
                + modelAsHexString + ") is not currently supported. Please report this to the developer!");
    }

    private static StringType lookup(StringType[] values, byte b) {
        int index = Byte.toUnsignedInt(b);
        if (index < values.length) {
            return values[index];
        } else {
            return UNKNOWN;
        }
    }

    private static final StringType[] AIR_VALUES = { new StringType("PERFECT"), new StringType("GOOD"),
            new StringType("NORMAL"), new StringType("BAD") };

    public static StringType getAirValue(byte b) {
        return lookup(AIR_VALUES, b);
    }

    private static final StringType[] LIGHT_VALUES = { new StringType("DARK"), new StringType("DIM"),
            new StringType("NORMAL"), new StringType("BRIGHT") };

    public static StringType getLightValue(byte b) {
        return lookup(LIGHT_VALUES, b);
    }

    private static final StringType[] NOISE_VALUES = { new StringType("QUIET"), new StringType("NORMAL"),
            new StringType("NOISY"), new StringType("EXTREME") };

    public static StringType getNoiseValue(byte b) {
        return lookup(NOISE_VALUES, b);
    }
}

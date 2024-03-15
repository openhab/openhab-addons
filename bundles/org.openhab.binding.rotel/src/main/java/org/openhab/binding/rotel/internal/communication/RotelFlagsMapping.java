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
package org.openhab.binding.rotel.internal.communication;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;

/**
 * Class managing the mapping of message flags with indicators
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelFlagsMapping {

    public static final RotelFlagsMapping MAPPING1 = new RotelFlagsMapping(
            Map.of(RotelFlagInfoType.MULTI_INPUT, new int[] { 3, 1 }, //
                    RotelFlagInfoType.ZONE2, new int[] { 5, 0 }, //
                    RotelFlagInfoType.CENTER, new int[] { 8, 6 }, //
                    RotelFlagInfoType.SURROUND_LEFT, new int[] { 8, 4 }, //
                    RotelFlagInfoType.SURROUND_RIGHT, new int[] { 8, 3 }));
    public static final RotelFlagsMapping MAPPING2 = new RotelFlagsMapping(
            Map.of(RotelFlagInfoType.ZONE2, new int[] { 4, 7 }, //
                    RotelFlagInfoType.CENTER, new int[] { 5, 6 }, //
                    RotelFlagInfoType.SURROUND_LEFT, new int[] { 5, 4 }, //
                    RotelFlagInfoType.SURROUND_RIGHT, new int[] { 5, 3 }));
    public static final RotelFlagsMapping MAPPING3 = new RotelFlagsMapping(
            Map.of(RotelFlagInfoType.MULTI_INPUT, new int[] { 4, 7 }, //
                    RotelFlagInfoType.ZONE2, new int[] { 1, 7 }, //
                    RotelFlagInfoType.ZONE3, new int[] { 2, 7 }, //
                    RotelFlagInfoType.ZONE4, new int[] { 6, 2 }, //
                    RotelFlagInfoType.CENTER, new int[] { 5, 6 }, //
                    RotelFlagInfoType.SURROUND_LEFT, new int[] { 5, 4 }, //
                    RotelFlagInfoType.SURROUND_RIGHT, new int[] { 5, 3 }));
    public static final RotelFlagsMapping MAPPING4 = new RotelFlagsMapping(
            Map.of(RotelFlagInfoType.MULTI_INPUT, new int[] { 3, 1 }, //
                    RotelFlagInfoType.ZONE2, new int[] { 1, 7 }, //
                    RotelFlagInfoType.ZONE3, new int[] { 2, 7 }, //
                    RotelFlagInfoType.ZONE4, new int[] { 6, 2 }, //
                    RotelFlagInfoType.CENTER, new int[] { 8, 6 }, //
                    RotelFlagInfoType.SURROUND_LEFT, new int[] { 8, 4 }, //
                    RotelFlagInfoType.SURROUND_RIGHT, new int[] { 8, 3 }));
    public static final RotelFlagsMapping MAPPING5 = new RotelFlagsMapping(
            Map.of(RotelFlagInfoType.ZONE2, new int[] { 3, 2 }, //
                    RotelFlagInfoType.ZONE3, new int[] { 4, 2 }, //
                    RotelFlagInfoType.ZONE4, new int[] { 4, 1 }, //
                    RotelFlagInfoType.CENTER, new int[] { 5, 6 }, //
                    RotelFlagInfoType.SURROUND_LEFT, new int[] { 5, 4 }, //
                    RotelFlagInfoType.SURROUND_RIGHT, new int[] { 5, 3 }));
    public static final RotelFlagsMapping MAPPING6 = new RotelFlagsMapping(
            Map.of(RotelFlagInfoType.ZONE, new int[] { 1, 1 }));
    public static final RotelFlagsMapping NO_MAPPING = new RotelFlagsMapping(Map.of());

    private Map<RotelFlagInfoType, int @Nullable []> infos;

    private RotelFlagsMapping(Map<RotelFlagInfoType, int @Nullable []> infos) {
        this.infos = infos;
    }

    /**
     * Get the availability of the information
     *
     * @return true if the information is available
     */
    public boolean isInfoPresent(RotelFlagInfoType infoType) {
        return infos.get(infoType) != null;
    }

    /**
     * Get the information
     *
     * @param infoType the type of information
     * @param flags the table of flags
     *
     * @return true if the information is ON in the flags or false if OFF
     *
     * @throws RotelException in case the information is undefined
     */
    public boolean isInfoOn(RotelFlagInfoType infoType, byte[] flags) throws RotelException {
        int[] info = infos.get(infoType);
        if (info == null || info.length != 2 || info[0] > flags.length) {
            throw new RotelException("Info " + infoType.name() + " not available in flags");
        }
        return RotelFlagsMapping.isBitFlagOn(flags, info[0], info[1]);
    }

    /**
     * Set the information
     *
     * @param infoType the type of information
     * @param flags the table of flags
     * @param on true to set the information to ON or false to set it to OFF
     *
     * @throws RotelException in case the information is undefined
     */
    public void setInfo(RotelFlagInfoType infoType, byte[] flags, boolean on) throws RotelException {
        int[] info = infos.get(infoType);
        if (info == null || info.length != 2 || info[0] > flags.length) {
            throw new RotelException("Info " + infoType.name() + " not available in flags");
        }
        RotelFlagsMapping.setBitFlag(flags, info[0], info[1], on);
    }

    /**
     * Check whether more than front channels are ON
     *
     * @param flags the table of flags
     *
     * @return true if the indicators show that center or surround channels are ON in the flags
     *
     * @throws RotelException in case the center or surround channel indicators are undefined
     */
    public boolean isMoreThan2Channels(byte[] flags) throws RotelException {
        int[] center = infos.get(RotelFlagInfoType.CENTER);
        int[] surroundLeft = infos.get(RotelFlagInfoType.SURROUND_LEFT);
        int[] surroundRight = infos.get(RotelFlagInfoType.SURROUND_RIGHT);
        return (center != null && center.length == 2 && center[0] <= flags.length
                && RotelFlagsMapping.isBitFlagOn(flags, center[0], center[1]))
                || (surroundLeft != null && surroundLeft.length == 2 && surroundLeft[0] <= flags.length
                        && RotelFlagsMapping.isBitFlagOn(flags, surroundLeft[0], surroundLeft[1]))
                || (surroundRight != null && surroundRight.length == 2 && surroundRight[0] <= flags.length
                        && RotelFlagsMapping.isBitFlagOn(flags, surroundRight[0], surroundRight[1]));
    }

    /**
     * Get a bit value inside the provided table of flags
     *
     * @param flags the table of flags
     * @param flagNumber the flag number to consider
     * @param bitNumber the bit number in the flag to consider
     *
     * @return true if the bit value in the flag is 1 or false if 0
     *
     * @throws RotelException in case of out of bounds value for the flag number or the bit number
     */
    public static boolean isBitFlagOn(byte[] flags, int flagNumber, int bitNumber) throws RotelException {
        if (flagNumber < 1 || flagNumber > flags.length) {
            throw new RotelException("Flag number out of bounds");
        }
        if (bitNumber < 0 || bitNumber > 7) {
            throw new RotelException("Bit number out of bounds");
        }
        int val = flags[flagNumber - 1] & 0x000000FF;
        return (val & (1 << bitNumber)) != 0;
    }

    /**
     * Set a bit value to 1 or 0 in the provided table of flags
     *
     * @param flags the table of flags
     * @param flagNumber the flag number to consider
     * @param bitNumber the bit number in the flag to consider
     * @param on true to set the bit value to 1 or false to set it to 0
     *
     * @throws RotelException in case of out of bounds value for the flag number or the bit number
     */
    private static void setBitFlag(byte[] flags, int flagNumber, int bitNumber, boolean on) throws RotelException {
        if (flagNumber < 1 || flagNumber > flags.length) {
            throw new RotelException("Flag number out of bounds");
        }
        if (bitNumber < 0 || bitNumber > 7) {
            throw new RotelException("Bit number out of bounds");
        }
        if (on) {
            flags[flagNumber - 1] |= (1 << bitNumber);
        } else {
            flags[flagNumber - 1] &= ~(1 << bitNumber);
        }
    }
}

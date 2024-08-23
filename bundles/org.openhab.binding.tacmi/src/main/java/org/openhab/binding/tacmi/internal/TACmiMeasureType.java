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
package org.openhab.binding.tacmi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enum holds all the different measures and states available to be
 * retrieved by the TACmi binding, including the scale factors needed to convert the received values to the real
 * numbers.
 *
 * @author Timo Wendt - Initial contribution
 * @author Wolfgang Klimt - improvements
 * @author Christian Niessner - Ported to OpenHAB2
 */
@NonNullByDefault
public enum TACmiMeasureType {
    NONE(0, 1),
    TEMPERATURE(1, 10),
    UNKNOWN2(2, 1),
    UNKNOWN3(3, 1),
    SECONDS(4, 1),
    UNKNOWN5(5, 1),
    UNKNOWN6(6, 1),
    UNKNOWN7(7, 1),
    UNKNOWN8(8, 1),
    UNKNOWN9(9, 1),
    KILOWATT(10, 100),
    KILOWATTHOURS(11, 10),
    MEGAWATTHOURS(12, 1),
    UNKNOWN13(13, 1),
    UNKNOWN14(14, 1),
    UNKNOWN15(15, 1),
    UNKNOWN16(16, 1),
    UNKNOWN17(17, 1),
    UNKNOWN18(18, 1),
    UNKNOWN19(19, 1),
    UNKNOWN20(20, 1),
    UNKNOWN21(21, 1),

    UNSUPPORTED(-1, 1);

    private final int typeval;
    private final int offset;

    private static final Logger logger = LoggerFactory.getLogger(TACmiMeasureType.class);

    private TACmiMeasureType(int typeval, int offset) {
        this.typeval = typeval;
        this.offset = offset;
    }

    public int getTypeValue() {
        return typeval;
    }

    public int getOffset() {
        return offset;
    }

    /**
     * Return measure type for a specific int value
     */
    public static TACmiMeasureType fromInt(int type) {
        for (TACmiMeasureType mtype : TACmiMeasureType.values()) {
            if (mtype.getTypeValue() == type) {
                return mtype;
            }
        }
        logger.debug("Received unexpected measure type {}", type);
        return TACmiMeasureType.UNSUPPORTED;
    }
}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal.pulse.sml;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * The OBIS codes that are read from a list response, with the channel each one feeds, the SML unit code it must carry,
 * and the unit it is published in. An entry with another unit code is ignored, never guessed.
 *
 * The set of quantities and the OBIS and unit codes (DLMS/COSEM unit enumeration: 27 W, 30 Wh, 33 A, 35 V, 44 Hz)
 * were taken from the observed frame; the approach of mapping a fixed OBIS table to sensors follows the Home Assistant
 * integration <a href="https://github.com/marq24/ha-tibber-pulse-local">marq24/ha-tibber-pulse-local</a>. No code was
 * copied.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
enum Obis {

    POWER("power", 0x10, 0x07, UnitCode.WATT, v -> new QuantityType<>(v, Units.WATT)),
    ENERGY_IMPORT("energy-import", 0x01, 0x08, UnitCode.WATT_HOUR, Obis::kilowattHours),
    ENERGY_EXPORT("energy-export", 0x02, 0x08, UnitCode.WATT_HOUR, Obis::kilowattHours),
    POWER_L1("power-l1", 0x24, 0x07, UnitCode.WATT, v -> new QuantityType<>(v, Units.WATT)),
    POWER_L2("power-l2", 0x38, 0x07, UnitCode.WATT, v -> new QuantityType<>(v, Units.WATT)),
    POWER_L3("power-l3", 0x4c, 0x07, UnitCode.WATT, v -> new QuantityType<>(v, Units.WATT)),
    VOLTAGE_L1("voltage-l1", 0x20, 0x07, UnitCode.VOLT, v -> new QuantityType<>(v, Units.VOLT)),
    VOLTAGE_L2("voltage-l2", 0x34, 0x07, UnitCode.VOLT, v -> new QuantityType<>(v, Units.VOLT)),
    VOLTAGE_L3("voltage-l3", 0x48, 0x07, UnitCode.VOLT, v -> new QuantityType<>(v, Units.VOLT)),
    CURRENT_L1("current-l1", 0x1f, 0x07, UnitCode.AMPERE, v -> new QuantityType<>(v, Units.AMPERE)),
    CURRENT_L2("current-l2", 0x33, 0x07, UnitCode.AMPERE, v -> new QuantityType<>(v, Units.AMPERE)),
    CURRENT_L3("current-l3", 0x47, 0x07, UnitCode.AMPERE, v -> new QuantityType<>(v, Units.AMPERE)),
    FREQUENCY("frequency", 0x0e, 0x07, UnitCode.HERTZ, v -> new QuantityType<>(v, Units.HERTZ)),
    /** Import power, used only to derive the net power when 1-0:16.7.0 is absent. */
    IMPORT_POWER(null, 0x01, 0x07, UnitCode.WATT, v -> new QuantityType<>(v, Units.WATT)),
    /** Export power, used only to derive the net power when 1-0:16.7.0 is absent. */
    EXPORT_POWER(null, 0x02, 0x07, UnitCode.WATT, v -> new QuantityType<>(v, Units.WATT));

    /**
     * Converts a decoded value to its state value. Not a {@code Function} so that the null annotations of the result
     * are part of the contract.
     */
    @FunctionalInterface
    private interface Quantity {
        QuantityType<?> of(BigDecimal value);
    }

    /** SML unit codes (DLMS/COSEM unit enumeration) accepted by the table. */
    private static final class UnitCode {
        static final int WATT = 27;
        static final int WATT_HOUR = 30;
        static final int AMPERE = 33;
        static final int VOLT = 35;
        static final int HERTZ = 44;
    }

    // A list, because the null defaults do not reach the elements of an array.
    private static final List<Obis> VALUES = List.of(values());

    private final @Nullable String channelId;
    private final byte[] code;
    private final int unitCode;
    private final Quantity quantity;

    /** All codes are of the form {@code 1-0:C.D.E*255}, binary {@code 01 00 C D E ff}. */
    Obis(@Nullable String channelId, int c, int d, int unitCode, Quantity quantity) {
        this.channelId = channelId;
        this.code = new byte[] { 0x01, 0x00, (byte) c, (byte) d, 0x00, (byte) 0xff };
        this.unitCode = unitCode;
        this.quantity = quantity;
    }

    /** Wh counters are published in kWh; moving the decimal point is exact. */
    private static QuantityType<?> kilowattHours(BigDecimal wattHours) {
        return new QuantityType<>(wattHours.movePointLeft(3), Units.KILOWATT_HOUR);
    }

    /**
     * @return the channel id, or null for the auxiliary codes that feed no channel directly
     */
    @Nullable
    String channelId() {
        return channelId;
    }

    int unitCode() {
        return unitCode;
    }

    /**
     * @param value the decoded value in the unit of the entry (raw x 10^scaler)
     * @return the state value in the published unit
     */
    QuantityType<?> toQuantity(BigDecimal value) {
        return quantity.of(value);
    }

    /**
     * @param objName the six-byte binary OBIS code of an entry
     * @return the matching table row, or null for any other code
     */
    static @Nullable Obis find(byte[] objName) {
        if (objName.length != 6) {
            return null;
        }
        for (Obis obis : VALUES) {
            if (Arrays.equals(obis.code, objName)) {
                return obis;
            }
        }
        return null;
    }
}

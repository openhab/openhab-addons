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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Physical units for the decoded values of {@link GeniMeasurand}.
 *
 * The binding reads class-10 numeric members directly in their declared binary format. Source units include any
 * required scaling; {@link GeniMeasurand} selects the publication unit and rounds after conversion.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum GeniUnit {
    Ampere(Units.AMPERE),
    Volt(Units.VOLT),
    Watt(Units.WATT),
    Metre(SIUnits.METRE),
    Second(Units.SECOND),
    Dimensionless(Units.ONE),
    CubicMetrePerSecond(Units.CUBICMETRE_PER_SECOND),
    /** One watt-second equals one joule. */
    WattSecond(Units.JOULE),
    /** 0.0001 metres, corresponding to unit 91 in the GENIBUS Protocol Specification. */
    TenthMillimetre(SIUnits.METRE.divide(10000)),
    RevolutionsPerMinute(Units.RPM);

    private final Unit<?> unit;

    GeniUnit(Unit<?> unit) {
        this.unit = unit;
    }

    Unit<?> unit() {
        return unit;
    }
}

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
package org.openhab.binding.miio.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.SmartHomeUnits;

/**
 * Enum of the units used in the miio protocol
 * Used to find the right {@link javax.measure.Unit} given the string of the unit
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum MiIoQuantiyTypes {

    CELCIUS(SIUnits.CELSIUS),
    FAHRENHEIT(ImperialUnits.FAHRENHEIT),
    SECOND(SmartHomeUnits.SECOND),
    MINUTE(SmartHomeUnits.MINUTE),
    HOUR(SmartHomeUnits.HOUR),
    SECONDS(SmartHomeUnits.SECOND),
    MINUTES(SmartHomeUnits.MINUTE),
    HOURS(SmartHomeUnits.HOUR),
    AMPERE(SmartHomeUnits.AMPERE),
    WATT(SmartHomeUnits.WATT);

    private final Unit<?> unit;

    private static Map<String, Unit<?>> stringMap = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::toString, MiIoQuantiyTypes::getUnit));

    private MiIoQuantiyTypes(Unit<?> unit) {
        this.unit = unit;
    }

    public Unit<?> getUnit() {
        return unit;
    }

    public static @Nullable Unit<?> get(String unitName) {
        return stringMap.get(unitName.toUpperCase());
    }
}

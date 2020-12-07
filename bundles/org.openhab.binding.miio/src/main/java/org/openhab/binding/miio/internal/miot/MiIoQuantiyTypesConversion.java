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
package org.openhab.binding.miio.internal.miot;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum of the unitTypes used in the miio protocol
 * Used to find the right {@link javax.measure.unitType} given the string of the unitType
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum MiIoQuantiyTypesConversion {

    TIME("Time", "seconds", "minutes", "minute", "hour", "hours"),
    TEMPERATURE("Temperature", "c", "celcius", "f", "farenheith"),
    DIMENSIONLESS("Dimensionless", "percent", "percentage"),
    ELECTRIC_POTENTIAL("ElectricPotential", "volt"),;

    private final String unitType;
    private final String[] aliasses;

    // private static Map<String, String> stringMap = Arrays.stream(values())
    // .collect(Collectors.toMap(Enum::toString, MiIoQuantiyTypesConversion::getunitType));

    private static Map<String, String> aliasMap() {
        Map<String, String> aliassesMap = new HashMap<>();
        for (MiIoQuantiyTypesConversion miIoQuantiyType : values()) {
            for (String alias : miIoQuantiyType.getAliasses()) {
                aliassesMap.put(alias.toLowerCase(), miIoQuantiyType.getunitType());
            }
        }
        return aliassesMap;
    }

    private MiIoQuantiyTypesConversion(String unitType, String... aliasses) {
        this.unitType = unitType;
        this.aliasses = aliasses;
    }

    public String getunitType() {
        return unitType;
    }

    public String[] getAliasses() {
        return aliasses;
    }

    public static @Nullable String getType(@Nullable String unitTypeName) {
        if (unitTypeName != null) {
            return aliasMap().get(unitTypeName.toLowerCase());
        }
        return null;
    }
}

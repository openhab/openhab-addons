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
package org.openhab.binding.solarman.internal.typeprovider;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCharge;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link ChannelUtils} class provides utility functions for handling channel types and units in the Solarman
 * binding.
 * It includes methods for determining item types, units of measure, and channel type IDs.
 *
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class ChannelUtils {

    /**
     * Determines the item type for a given parameter item.
     *
     * @param item The parameter item to determine the type for
     * @return The item type as a string
     */
    public static String getItemType(ParameterItem item) {
        @Nullable
        Integer rule = item.getRule();

        @Nullable
        String uom = item.getUom();
        if (uom == null) {
            uom = "UNKN";
        }

        return switch (rule) {
            case 5, 6, 7, 9 -> CoreItemFactory.STRING;
            case 8 -> CoreItemFactory.DATETIME;
            default -> {
                yield computeNumberType(uom);
            }
        };
    }

    /**
     * Computes the number type based on the unit of measure (UOM).
     *
     * @param uom The unit of measure as a string
     * @return The number type as a string
     */
    private static String computeNumberType(String uom) {
        return switch (uom.toUpperCase()) {
            case "A" -> CoreItemFactory.NUMBER + ":" + ElectricCurrent.class.getSimpleName();
            case "AH" -> CoreItemFactory.NUMBER + ":" + ElectricCharge.class.getSimpleName();
            case "V" -> CoreItemFactory.NUMBER + ":" + ElectricPotential.class.getSimpleName();
            case "°C" -> CoreItemFactory.NUMBER + ":" + Temperature.class.getSimpleName();
            case "W", "KW", "VA", "KVA", "VAR", "KVAR" -> CoreItemFactory.NUMBER + ":" + Power.class.getSimpleName();
            case "WH", "KWH" -> CoreItemFactory.NUMBER + ":" + Energy.class.getSimpleName();
            case "S" -> CoreItemFactory.NUMBER + ":" + Time.class.getSimpleName();
            case "HZ" -> CoreItemFactory.NUMBER + ":" + Frequency.class.getSimpleName();
            case "%" -> CoreItemFactory.NUMBER + ":" + Dimensionless.class.getSimpleName();
            default -> CoreItemFactory.NUMBER;
        };
    }

    /**
     * Retrieves the unit of measure (UOM) from a string definition.
     *
     * @param uom The unit of measure as a string
     * @return The corresponding {@link Unit}, or null if not found
     */
    public static @Nullable Unit<?> getUnitFromDefinition(String uom) {
        return switch (uom.toUpperCase()) {
            case "A" -> Units.AMPERE;
            case "AH" -> Units.AMPERE_HOUR;
            case "V" -> Units.VOLT;
            case "°C" -> SIUnits.CELSIUS;
            case "W" -> Units.WATT;
            case "KW" -> MetricPrefix.KILO(Units.WATT);
            case "VA" -> Units.VOLT_AMPERE;
            case "KVA" -> MetricPrefix.KILO(Units.VOLT_AMPERE);
            case "VAR" -> Units.VAR;
            case "KVAR" -> MetricPrefix.KILO(Units.VAR);
            case "WH" -> Units.WATT_HOUR;
            case "KWH" -> MetricPrefix.KILO(Units.WATT_HOUR);
            case "S" -> Units.SECOND;
            case "HZ" -> Units.HERTZ;
            case "%" -> Units.PERCENT;
            default -> null;
        };
    }

    /**
     * Escapes a name string by replacing specific characters with hyphens and converting to lowercase.
     *
     * @param name The name to escape
     * @return The escaped name
     */
    public static String escapeName(String name) {
        name = name.trim();
        name = name.replace("+", "plus");
        name = name.toLowerCase();
        name = name.replaceAll("[ .()/\\\\&_]", "-");
        return name;
    }

    /**
     * Computes a channel type ID based on the inverter definition ID, group, and name.
     *
     * @param inverterDefinitionId The inverter definition ID
     * @param group The group
     * @param name The name
     * @return The computed {@link ChannelTypeUID}
     */
    public static ChannelTypeUID computeChannelTypeId(String inverterDefinitionId, String group, String name) {
        return new ChannelTypeUID(SolarmanBindingConstants.SOLARMAN_BINDING_ID,
                String.format("%s-%s-%s", escapeName(inverterDefinitionId), escapeName(group), escapeName(name)));
    }
}

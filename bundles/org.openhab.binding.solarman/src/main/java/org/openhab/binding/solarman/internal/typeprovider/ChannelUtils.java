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
import javax.measure.quantity.*;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Catalin Sanda - Initial contribution
 */
public class ChannelUtils {
    public static String getItemType(ParameterItem item) {
        return switch (item.getRule()) {
            case 5, 6, 7, 9 -> CoreItemFactory.STRING;
            case 8 -> CoreItemFactory.DATETIME;
            default -> computeNumberType(item.getUom());
        };
    }

    private static String computeNumberType(String uom) {
        // @TODO there is probably a better way to do this
        return switch (uom.toUpperCase()) {
            case "A" -> CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(ElectricCurrent.class);
            case "V" -> CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(ElectricPotential.class);
            case "°C" -> CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(Temperature.class);
            case "W", "KW", "VA", "KVA", "VAR", "KVAR" ->
                CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(Power.class);
            case "WH", "KWH" -> CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(Energy.class);
            case "S" -> CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(Time.class);
            case "HZ" -> CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(Frequency.class);
            case "%" -> CoreItemFactory.NUMBER + ":" + ClassUtils.getShortClassName(Dimensionless.class);
            default -> CoreItemFactory.NUMBER;
        };
    }

    public static Unit<?> getUnitFromDefinition(String uom) {
        return switch (uom.toUpperCase()) {
            case "A" -> Units.AMPERE;
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

    public static String escapeName(String name) {
        name = name.replace("+", "plus");
        name = name.replace("-", "minus");
        return StringUtils.replaceChars(StringUtils.lowerCase(name), " .()/\\&", "_");
    }

    public static ChannelTypeUID computeChannelTypeId(String inverterDefinitionId, String group, String name) {
        return new ChannelTypeUID(SolarmanBindingConstants.SOLARMAN_BINDING_ID,
                String.format("%s_%s_%s", escapeName(inverterDefinitionId), escapeName(group), escapeName(name)));
    }
}

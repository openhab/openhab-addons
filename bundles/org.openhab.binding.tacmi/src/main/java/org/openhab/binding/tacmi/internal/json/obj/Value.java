/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal.json.obj;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.TACmiBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * Class holding the Value JSON element
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 */
@NonNullByDefault
public class Value {
    public double value = 0;
    public String unit = "-1";

    public String getDesc() {
        final var el = TYPE_MAP.get(Integer.parseInt(unit));
        assert el != null : "Type not set or supported";
        return el.desc;
    }

    public @Nullable String getType() {
        final var el = TYPE_MAP.get(Integer.parseInt(unit));
        assert el != null : "Type not set or supported";
        return el.unitname;
    }

    public @Nullable ChannelTypeUID getChannelType() {
        final var el = TYPE_MAP.get(Integer.parseInt(unit));
        assert el != null : "Type not set or supported";
        if (!el.unitname.startsWith("Number")) {
            return TACmiBindingConstants.CHANNEL_TYPE_SCHEME_STATE_RO_UID;
        }
        return TACmiBindingConstants.CHANNEL_TYPE_SCHEME_NUMERIC_RO_UID;
    }

    public State getState() {
        final var el = TYPE_MAP.get(Integer.parseInt(unit));
        assert el != null : "Type not set or supported";
        if (el.unitname.startsWith("Switch")) {
            return (value != 0) ? OnOffType.ON : OnOffType.OFF;
        }
        if (el.unit != null) {
            // Must copy to a local variable to make the null-checker happy
            final var tmp = Objects.requireNonNull(el.unit);
            return new QuantityType<>(value * el.factor, tmp);
        }
        return new DecimalType(Double.valueOf(value * el.factor));
    }

    static class TypeInfo {
        public final String desc;
        public final String unitname;
        public final @Nullable Unit<@NonNull ?> unit;
        public final double factor;

        public TypeInfo(String desc, String unitname, @Nullable Unit<@NonNull ?> unit) {
            this.desc = desc;
            this.unitname = unitname;
            this.factor = 1;
            this.unit = unit;
        }

        public TypeInfo(String desc, String unitname, double factor, Unit<?> unit) {
            this.desc = desc;
            this.unitname = unitname;
            this.factor = factor;
            this.unit = unit;
        }
    }

    static final Map<Integer, TypeInfo> TYPE_MAP;
    static {
     // @formatter:off
        // -1 is reserved to return null
        Map<Integer, TypeInfo> lMap = new HashMap<Integer, TypeInfo>();
        lMap.put(0,  new TypeInfo("Unknown",  "Number:Dimensionless", null));
        lMap.put(1,  new TypeInfo("°C",       "Number:Temperature",   SIUnits.CELSIUS));
        lMap.put(2,  new TypeInfo("W/m²",     "Number",               null));
        lMap.put(3,  new TypeInfo("l/h",      "Number",               null));
        lMap.put(4,  new TypeInfo("sec",      "Number:Time",          Units.SECOND));
        lMap.put(5,  new TypeInfo("min",      "Number:Time",          Units.MINUTE));
        lMap.put(6,  new TypeInfo("l/Imp",    "Number",               null));
        lMap.put(7,  new TypeInfo("K",        "Number:Temperature",   Units.KELVIN));
        lMap.put(8,  new TypeInfo("%",        "Number:Dimensionless", Units.PERCENT));
        lMap.put(10, new TypeInfo("kW",       "Number:Power",         1000, Units.WATT));
        lMap.put(11, new TypeInfo("kWh",      "Number:Energy",        Units.KILOWATT_HOUR));
        lMap.put(12, new TypeInfo("MWh",      "Number:Energy",        Units.MEGAWATT_HOUR));
        lMap.put(13, new TypeInfo("V",        "Number:ElectricPotential", Units.VOLT));
        lMap.put(14, new TypeInfo("mA",       "Number:ElectricCurrent", 1/1000, Units.AMPERE));
        lMap.put(15, new TypeInfo("hr",       "Number:Duration",      Units.HOUR));
        lMap.put(16, new TypeInfo("Days",     "Number:Duration",      Units.DAY));
        lMap.put(17, new TypeInfo("Imp",      "Number:ElectricResistance", null));
        lMap.put(18, new TypeInfo("kΩ",       "Number",               1000, Units.OHM));
        lMap.put(19, new TypeInfo("l",        "Number:Volume",        Units.LITRE));
        lMap.put(20, new TypeInfo("km/h",     "Number:Speed",         SIUnits.KILOMETRE_PER_HOUR));
        lMap.put(21, new TypeInfo("Hz",       "Number:Frequency",     Units.HERTZ));
        lMap.put(22, new TypeInfo("l/min",    "Number",               Units.LITRE_PER_MINUTE));
        lMap.put(23, new TypeInfo("bar",      "Number:Pressure",      Units.BAR));
        lMap.put(24, new TypeInfo("Unknown",  "Number",               null));
        lMap.put(25, new TypeInfo("km",       "Number:Length",        1000, SIUnits.METRE));
        lMap.put(26, new TypeInfo("m",        "Number:Length",        SIUnits.METRE));
        lMap.put(27, new TypeInfo("mm",       "Number:Length",        1/1000, SIUnits.METRE));
        lMap.put(28, new TypeInfo("m³",       "Number",               null));
        lMap.put(35, new TypeInfo("l/d",      "Number",               null));
        lMap.put(36, new TypeInfo("m/s",      "Number",               null));
        lMap.put(37, new TypeInfo("m³/min",   "Number",               null));
        lMap.put(38, new TypeInfo("m³/h",     "Number",               null));
        lMap.put(39, new TypeInfo("m³/d",     "Number",               null));
        lMap.put(40, new TypeInfo("mm/min",   "Number",               null));
        lMap.put(41, new TypeInfo("mm/h",     "Number",               null));
        lMap.put(42, new TypeInfo("mm/d",     "Number",               null));
        lMap.put(43, new TypeInfo("ON/OFF",   "Switch",               null));
        lMap.put(44, new TypeInfo("NO/YES",   "Switch",               null));
        lMap.put(46, new TypeInfo("°C",       "Number:Temperature",   SIUnits.CELSIUS));
        lMap.put(50, new TypeInfo("€",        "Number",               null));
        lMap.put(51, new TypeInfo("$",        "Number",               null));
        lMap.put(52, new TypeInfo("g/m³",     "Number",               null));
        lMap.put(53, new TypeInfo("Unknown",  "Number",               null));
        lMap.put(54, new TypeInfo("°",        "Number",               Units.DEGREE_ANGLE));
        lMap.put(56, new TypeInfo("°",        "Number",               Units.DEGREE_ANGLE));
        lMap.put(57, new TypeInfo("sec",      "Number:Duration",      Units.SECOND));
        lMap.put(58, new TypeInfo("Unknown",  "Number",               null));
        lMap.put(59, new TypeInfo("%",        "Number",               Units.PERCENT));
        lMap.put(60, new TypeInfo("h",        "Number:Time",          Units.HOUR));
        lMap.put(63, new TypeInfo("A",        "Number:Current",       Units.AMPERE));
        lMap.put(65, new TypeInfo("mbar",     "Number:Pressure",      Units.MILLIBAR));
        lMap.put(66, new TypeInfo("Pa",       "Number",               null));
        lMap.put(67, new TypeInfo("ppm",      "Number",               null));
        lMap.put(68, new TypeInfo("Unknown",  "Number",               null));
        lMap.put(69, new TypeInfo("W",        "Number:Power",         Units.WATT));
        lMap.put(70, new TypeInfo("t",        "Number",               1000, SIUnits.KILOGRAM));
        lMap.put(71, new TypeInfo("kg",       "Number:Mass",          SIUnits.KILOGRAM));
        lMap.put(72, new TypeInfo("g",        "Number:Mass",          1/1000, SIUnits.KILOGRAM));
        lMap.put(73, new TypeInfo("cm",       "Number:Length",        1/100, SIUnits.METRE));
        lMap.put(74, new TypeInfo("K",        "Number:Temperature",   Units.KELVIN));
        lMap.put(75, new TypeInfo("lx",       "Number",               Units.LUX));
     // @formatter:on
        TYPE_MAP = Collections.unmodifiableMap(lMap);
    }
}

/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.TACmiBindingConstants;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.ProductUnit;
import tech.units.indriya.unit.TransformedUnit;

/**
 * Class holding the Value JSON element
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 */
@NonNullByDefault
public class Value {
    public double value = 0;
    public String unit = "-1";
    private @Nullable TypeInfo typeInfo = null;

    // Always return a "valid" type info
    // This simplifies the rest of the code, as less error checking is needed.
    private TypeInfo getTypeInfo() {
        // Need a local variable to satisfy null-checks
        var rv = typeInfo;
        if (rv != null) {
            return rv;
        }

        try {
            rv = TYPE_MAP.get(Integer.parseInt(unit));
        } catch (Exception e) {
        }
        if (rv == null) {
            rv = new TypeInfo("Undefined: " + unit, "Number", null);
        }
        typeInfo = rv;
        return rv;
    }

    public String getDesc() {
        return getTypeInfo().desc;
    }

    public @Nullable String getType() {
        return getTypeInfo().unitname;
    }

    public ChannelTypeUID getChannelType() {
        final var ti = getTypeInfo();
        if (!ti.unitname.startsWith("Number")) {
            return TACmiBindingConstants.CHANNEL_TYPE_SCHEME_STATE_RO_UID;
        }
        return TACmiBindingConstants.CHANNEL_TYPE_SCHEME_NUMERIC_RO_UID;
    }

    public State getState() {
        final var ti = getTypeInfo();
        if (ti.unitname.startsWith("Switch")) {
            return (value != 0) ? OnOffType.ON : OnOffType.OFF;
        }
        final var unit = ti.unit;
        if (unit != null) {
            return new QuantityType<>(value, unit);
        }
        return new DecimalType(Double.valueOf(value));
    }

    static class TypeInfo {
        public final String desc;
        public final String unitname;
        public final @Nullable Unit<@NonNull ?> unit;

        public TypeInfo(String desc, String unitname, @Nullable Unit<@NonNull ?> unit) {
            this.desc = desc;
            this.unitname = unitname;
            this.unit = unit;
        }
    }

    static final Unit<VolumetricFlowRate> LITRE_PER_HOUR = new ProductUnit<>(
            tech.units.indriya.unit.Units.LITRE.divide(tech.units.indriya.unit.Units.HOUR));
    static final Unit<VolumetricFlowRate> LITRE_PER_DAY = new ProductUnit<>(
            tech.units.indriya.unit.Units.LITRE.divide(tech.units.indriya.unit.Units.DAY));
    static final Unit<Power> KILOWATT = MetricPrefix.KILO(Units.WATT);
    static final Unit<ElectricCurrent> MILLIAMPERE = MetricPrefix.MILLI(Units.AMPERE);
    static final Unit<Power> KILOOHM = MetricPrefix.KILO(Units.WATT);
    static final Unit<Length> KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);
    static final Unit<Length> CENTIMETRE = MetricPrefix.CENTI(SIUnits.METRE);
    static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(SIUnits.METRE);
    static final Unit<Speed> MILLIMETRE_PER_MINUTE = new TransformedUnit<>("mm/min", Units.MILLIMETRE_PER_HOUR,
            MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(60)));
    static final Unit<Speed> MILLIMETRE_PER_DAY = new TransformedUnit<>("mm/d", Units.MILLIMETRE_PER_HOUR,
            MultiplyConverter.ofRational(BigInteger.valueOf(24), BigInteger.ONE));
    static final Unit<Density> GRAM_PER_CUBICMETRE = new TransformedUnit<>("g/m³", Units.KILOGRAM_PER_CUBICMETRE,
            MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000)));
    static final Unit<Mass> TONNE = new TransformedUnit<>("t", SIUnits.KILOGRAM,
            MultiplyConverter.ofRational(BigInteger.valueOf(1000), BigInteger.ONE));
    static final Unit<Mass> GRAMM = new TransformedUnit<>("g", SIUnits.KILOGRAM,
            MultiplyConverter.ofRational(BigInteger.ONE, BigInteger.valueOf(1000)));

    static {
        SimpleUnitFormat.getInstance().label(LITRE_PER_HOUR, "l/h");
        SimpleUnitFormat.getInstance().label(LITRE_PER_DAY, "l/d");
        SimpleUnitFormat.getInstance().label(KILOWATT, "kW");
        SimpleUnitFormat.getInstance().label(MILLIAMPERE, "mA");
        SimpleUnitFormat.getInstance().label(KILOOHM, "kΩ");
        SimpleUnitFormat.getInstance().label(KILOMETRE, "km");
        SimpleUnitFormat.getInstance().label(CENTIMETRE, "cm");
        SimpleUnitFormat.getInstance().label(MILLIMETRE, "mm");
        SimpleUnitFormat.getInstance().label(MILLIMETRE_PER_MINUTE, "mm/min");
        SimpleUnitFormat.getInstance().label(MILLIMETRE_PER_DAY, "mm/d");
        SimpleUnitFormat.getInstance().label(GRAM_PER_CUBICMETRE, "g/m³");
        SimpleUnitFormat.getInstance().label(TONNE, "t");
        SimpleUnitFormat.getInstance().label(GRAMM, "g");
    }

    static final Map<Integer, TypeInfo> TYPE_MAP;
    static {
    // @formatter:off
        // -1 is reserved to return null
        Map<Integer, TypeInfo> lMap = new HashMap<Integer, TypeInfo>();
        lMap.put(0,  new TypeInfo("Unknown",  "Number",                    null));
        lMap.put(1,  new TypeInfo("°C",       "Number:Temperature",        SIUnits.CELSIUS));
        lMap.put(2,  new TypeInfo("W/m²",     "Number:Intensity",          Units.WATT_HOUR_PER_SQUARE_METRE));
        lMap.put(3,  new TypeInfo("l/h",      "Number:VolumetricFlowRate", LITRE_PER_HOUR));
        lMap.put(4,  new TypeInfo("sec",      "Number:Time",               Units.SECOND));
        lMap.put(5,  new TypeInfo("min",      "Number:Time",               Units.MINUTE));
        lMap.put(6,  new TypeInfo("l/Imp",    "Number",                    null));
        lMap.put(7,  new TypeInfo("K",        "Number:Temperature",        Units.KELVIN));
        lMap.put(8,  new TypeInfo("%",        "Number:Dimensionless",      Units.PERCENT));
        lMap.put(10, new TypeInfo("kW",       "Number:Power",              KILOWATT));
        lMap.put(11, new TypeInfo("kWh",      "Number:Energy",             Units.KILOWATT_HOUR));
        lMap.put(12, new TypeInfo("MWh",      "Number:Energy",             Units.MEGAWATT_HOUR));
        lMap.put(13, new TypeInfo("V",        "Number:ElectricPotential",  Units.VOLT));
        lMap.put(14, new TypeInfo("mA",       "Number:ElectricCurrent",    MILLIAMPERE));
        lMap.put(15, new TypeInfo("hr",       "Number:Duration",           Units.HOUR));
        lMap.put(16, new TypeInfo("Days",     "Number:Duration",           Units.DAY));
        lMap.put(17, new TypeInfo("Imp",      "Number",                    null));
        lMap.put(18, new TypeInfo("kΩ",       "Number:ElectricResistance", KILOOHM));
        lMap.put(19, new TypeInfo("l",        "Number:Volume",             Units.LITRE));
        lMap.put(20, new TypeInfo("km/h",     "Number:Speed",              SIUnits.KILOMETRE_PER_HOUR));
        lMap.put(21, new TypeInfo("Hz",       "Number:Frequency",          Units.HERTZ));
        lMap.put(22, new TypeInfo("l/min",    "Number:VolumetricFlowRate", Units.LITRE_PER_MINUTE));
        lMap.put(23, new TypeInfo("bar",      "Number:Pressure",           Units.BAR));
        lMap.put(24, new TypeInfo("Unknown",  "Number",                    null));
        lMap.put(25, new TypeInfo("km",       "Number:Length",             KILOMETRE));
        lMap.put(26, new TypeInfo("m",        "Number:Length",             SIUnits.METRE));
        lMap.put(27, new TypeInfo("mm",       "Number:Length",             MILLIMETRE));
        lMap.put(28, new TypeInfo("m³",       "Number:Volume",             SIUnits.CUBIC_METRE));
        lMap.put(35, new TypeInfo("l/d",      "Number:VolumetricFlowRate", LITRE_PER_DAY));
        lMap.put(36, new TypeInfo("m/s",      "Number:Speed",              Units.METRE_PER_SECOND));
        lMap.put(37, new TypeInfo("m³/min",   "Number:VolumetricFlowRate", Units.CUBICMETRE_PER_MINUTE));
        lMap.put(38, new TypeInfo("m³/h",     "Number:VolumetricFlowRate", Units.CUBICMETRE_PER_HOUR));
        lMap.put(39, new TypeInfo("m³/d",     "Number:VolumetricFlowRate", Units.CUBICMETRE_PER_DAY));
        lMap.put(40, new TypeInfo("mm/min",   "Number:Speed",              MILLIMETRE_PER_MINUTE));
        lMap.put(41, new TypeInfo("mm/h",     "Number:Speed",              Units.MILLIMETRE_PER_HOUR));
        lMap.put(42, new TypeInfo("mm/d",     "Number:Speed",              MILLIMETRE_PER_DAY));
        lMap.put(43, new TypeInfo("ON/OFF",   "Switch",                    null));
        lMap.put(44, new TypeInfo("NO/YES",   "Switch",                    null));
        lMap.put(46, new TypeInfo("°C",       "Number:Temperature",        SIUnits.CELSIUS));
        lMap.put(50, new TypeInfo("€",        "Number",                    null));
        lMap.put(51, new TypeInfo("$",        "Number",                    null));
        lMap.put(52, new TypeInfo("g/m³",     "Number:Density",            GRAM_PER_CUBICMETRE));
        lMap.put(53, new TypeInfo("Unknown",  "Number",                    null));
        lMap.put(54, new TypeInfo("°",        "Number:Angle",              Units.DEGREE_ANGLE));
        lMap.put(56, new TypeInfo("°",        "Number:Angle",              Units.DEGREE_ANGLE));
        lMap.put(57, new TypeInfo("sec",      "Number:Duration",           Units.SECOND));
        lMap.put(58, new TypeInfo("Unknown",  "Number",                    null));
        lMap.put(59, new TypeInfo("%",        "Number:Dimensionless",      Units.PERCENT));
        lMap.put(60, new TypeInfo("h",        "Number:Time",               Units.HOUR));
        lMap.put(63, new TypeInfo("A",        "Number:Current",            Units.AMPERE));
        lMap.put(65, new TypeInfo("mbar",     "Number:Pressure",           Units.MILLIBAR));
        lMap.put(66, new TypeInfo("Pa",       "Number:Pressure",           SIUnits.PASCAL));
        lMap.put(67, new TypeInfo("ppm",      "Number:Dimensionless",      Units.PARTS_PER_MILLION));
        lMap.put(68, new TypeInfo("Unknown",  "Number",                    null));
        lMap.put(69, new TypeInfo("W",        "Number:Power",              Units.WATT));
        lMap.put(70, new TypeInfo("t",        "Number:Mass",               TONNE));
        lMap.put(71, new TypeInfo("kg",       "Number:Mass",               SIUnits.KILOGRAM));
        lMap.put(72, new TypeInfo("g",        "Number:Mass",               GRAMM));
        lMap.put(73, new TypeInfo("cm",       "Number:Length",             CENTIMETRE));
        lMap.put(74, new TypeInfo("K",        "Number:Temperature",        Units.KELVIN));
        lMap.put(75, new TypeInfo("lx",       "Number:Illuminance",        Units.LUX));
        // I assume this should be Bq/m³, but I'll stick to the JSon documentation
        lMap.put(76, new TypeInfo("Bg/m³",    "Number:Illuminance",        Units.BECQUEREL_PER_CUBIC_METRE));
    // @formatter:on
        TYPE_MAP = Collections.unmodifiableMap(lMap);
    }
}

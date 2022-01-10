/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.generic.internal;

import java.math.BigInteger;
import java.util.UUID;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCharge;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.library.dimension.ArealDensity;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.ProductUnit;
import tech.units.indriya.unit.TransformedUnit;

/**
 * The {@link BluetoothUnit} maps bluetooth units to openHAB units.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public enum BluetoothUnit {

    UNITLESS(0x2700, "org.bluetooth.unit.unitless", Units.ONE),
    METRE(0x2701, "org.bluetooth.unit.length.metre", SIUnits.METRE),
    KILOGRAM(0x2702, "org.bluetooth.unit.mass.kilogram", SIUnits.KILOGRAM),
    SECOND(0x2703, "org.bluetooth.unit.time.second", Units.SECOND),
    AMPERE(0x2704, "org.bluetooth.unit.electric_current.ampere", Units.AMPERE),
    KELVIN(0x2705, "org.bluetooth.unit.thermodynamic_temperature.kelvin", Units.KELVIN),
    MOLE(0x2706, "org.bluetooth.unit.amount_of_substance.mole", Units.MOLE),
    CANDELA(0x2707, "org.bluetooth.unit.luminous_intensity.candela", Units.CANDELA),
    SQUARE_METRES(0x2710, "org.bluetooth.unit.area.square_metres", SIUnits.SQUARE_METRE),
    CUBIC_METRES(0x2711, "org.bluetooth.unit.volume.cubic_metres", SIUnits.CUBIC_METRE),
    METRE_PER_SECOND(0x2712, "org.bluetooth.unit.velocity.metres_per_second", Units.METRE_PER_SECOND),
    METRE_PER_SQUARE_SECOND(0X2713, "org.bluetooth.unit.acceleration.metres_per_second_squared",
            Units.METRE_PER_SQUARE_SECOND),
    WAVENUMBER(0x2714, "org.bluetooth.unit.wavenumber.reciprocal_metre", Units.ONE),
    KILOGRAM_PER_CUBIC_METRE(0x2715, "org.bluetooth.unit.density.kilogram_per_cubic_metre",
            Units.KILOGRAM_PER_CUBICMETRE),
    KILOGRAM_PER_SQUARE_METRE(0x2716, "org.bluetooth.unit.surface_density.kilogram_per_square_metre",
            BUnits.KILOGRAM_PER_SQUARE_METER),
    CUBIC_METRE_PER_KILOGRAM(0x2717, "org.bluetooth.unit.specific_volume.cubic_metre_per_kilogram", Units.ONE),
    AMPERE_PER_SQUARE_METRE(0x2718, "org.bluetooth.unit.current_density.ampere_per_square_metre", Units.ONE),
    AMPERE_PER_METRE(0x2719, "org.bluetooth.unit.magnetic_field_strength.ampere_per_metre", Units.ONE),
    MOLE_PER_CUBIC_METRE(0x271A, "org.bluetooth.unit.amount_concentration.mole_per_cubic_metre", Units.ONE),
    CONCENTRATION_KILOGRAM_PER_CUBIC_METRE(0x271B, "org.bluetooth.unit.mass_concentration.kilogram_per_cubic_metre",
            Units.KILOGRAM_PER_CUBICMETRE),
    CANDELA_PER_SQUARE_METRE(0x271C, "org.bluetooth.unit.luminance.candela_per_square_metre", Units.ONE),
    REFRACTIVE_INDEX(0x271D, "org.bluetooth.unit.refractive_index", Units.ONE),
    RELATIVE_PERMEABILITY(0x271E, "org.bluetooth.unit.relative_permeability", Units.ONE),
    RADIAN(0x2720, "org.bluetooth.unit.plane_angle.radian", Units.RADIAN),
    STERADIAN(0x2721, "org.bluetooth.unit.solid_angle.steradian", Units.STERADIAN),
    HERTZ(0x2722, "org.bluetooth.unit.frequency.hertz", Units.HERTZ),
    NEWTON(0x2723, "org.bluetooth.unit.force.newton", Units.NEWTON),
    PASCAL(0x2724, "org.bluetooth.unit.pressure.pascal", SIUnits.PASCAL),
    JOULE(0x2725, "org.bluetooth.unit.energy.joule", Units.JOULE),
    WATT(0x2726, "org.bluetooth.unit.power.watt", Units.WATT),
    COULOMB(0x2727, "org.bluetooth.unit.electric_charge.coulomb", Units.COULOMB),
    VOLT(0x2728, "org.bluetooth.unit.electric_potential_difference.volt", Units.VOLT),
    FARAD(0x2729, "org.bluetooth.unit.capacitance.farad", Units.FARAD),
    OHM(0x272A, "org.bluetooth.unit.electric_resistance.ohm", Units.OHM),
    SIEMENS(0x272B, "org.bluetooth.unit.electric_conductance.siemens", Units.SIEMENS),
    WEBER(0x272C, "org.bluetooth.unit.magnetic_flux.weber", Units.WEBER),
    TESLA(0x272D, "org.bluetooth.unit.magnetic_flux_density.tesla", Units.TESLA),
    HENRY(0x272E, "org.bluetooth.unit.inductance.henry", Units.HENRY),
    DEGREE_CELSIUS(0x272F, "org.bluetooth.unit.thermodynamic_temperature.degree_celsius", SIUnits.CELSIUS),
    LUMEN(0x2730, "org.bluetooth.unit.luminous_flux.lumen", Units.LUMEN),
    LUX(0x2731, "org.bluetooth.unit.illuminance.lux", Units.LUX),
    BECQUEREL(0x2732, "org.bluetooth.unit.activity_referred_to_a_radionuclide.becquerel", Units.BECQUEREL),
    GRAY(0x2733, "org.bluetooth.unit.absorbed_dose.gray", Units.GRAY),
    SIEVERT(0x2734, "org.bluetooth.unit.dose_equivalent.sievert", Units.SIEVERT),
    KATAL(0x2735, "org.bluetooth.unit.catalytic_activity.katal", Units.KATAL),
    PASCAL_SECOND(0x2740, "org.bluetooth.unit.dynamic_viscosity.pascal_second", Units.ONE),
    NEWTON_METRE(0x2741, "org.bluetooth.unit.moment_of_force.newton_metre", Units.ONE),
    NEWTON_PER_METRE(0x2742, "org.bluetooth.unit.surface_tension.newton_per_metre", Units.ONE),
    RADIAN_PER_SECOND(0x2743, "org.bluetooth.unit.angular_velocity.radian_per_second", Units.ONE),
    RADIAN_PER_SECOND_SQUARED(0x2744, "org.bluetooth.unit.angular_acceleration.radian_per_second_squared", Units.ONE),
    FLUX_WATT_PER_SQUARE_METRE(0x2745, "org.bluetooth.unit.heat_flux_density.watt_per_square_metre", Units.ONE),
    JOULE_PER_KELVIN(0x2746, "org.bluetooth.unit.heat_capacity.joule_per_kelvin", Units.ONE),
    JOULE_PER_KILOGRAM_KELVIN(0x2747, "org.bluetooth.unit.specific_heat_capacity.joule_per_kilogram_kelvin", Units.ONE),
    JOULE_PER_KILOGRAM(0x2748, "org.bluetooth.unit.specific_energy.joule_per_kilogram", Units.ONE),
    WATT_PER_METRE_KELVIN(0x2749, "org.bluetooth.unit.thermal_conductivity.watt_per_metre_kelvin", Units.ONE),
    JOULE_PER_CUBIC_METRE(0x274A, "org.bluetooth.unit.energy_density.joule_per_cubic_metre", Units.ONE),
    VOLT_PER_METRE(0x274B, "org.bluetooth.unit.electric_field_strength.volt_per_metre", Units.ONE),
    CHARGE_DENSITY_COULOMB_PER_CUBIC_METRE(0x274C, "org.bluetooth.unit.electric_charge_density.coulomb_per_cubic_metre",
            Units.ONE),
    CHARGE_DENSITY_COULOMB_PER_SQUARE_METRE(0x274D,
            "org.bluetooth.unit.surface_charge_density.coulomb_per_square_metre", Units.ONE),
    FLUX_DENSITY_COULOMB_PER_SQUARE_METRE(0x274E, "org.bluetooth.unit.electric_flux_density.coulomb_per_square_metre",
            Units.ONE),
    FARAD_PER_METRE(0x274F, "org.bluetooth.unit.permittivity.farad_per_metre", Units.ONE),
    HENRY_PER_METRE(0x2750, "org.bluetooth.unit.permeability.henry_per_metre", Units.ONE),
    JOULE_PER_MOLE(0x2751, "org.bluetooth.unit.molar_energy.joule_per_mole", Units.ONE),
    JOULE_PER_MOLE_KELVIN(0x2752, "org.bluetooth.unit.molar_entropy.joule_per_mole_kelvin", Units.ONE),
    COULOMB_PER_KILOGRAM(0x2753, "org.bluetooth.unit.exposure.coulomb_per_kilogram", Units.ONE),
    GRAY_PER_SECOND(0x2754, "org.bluetooth.unit.absorbed_dose_rate.gray_per_second", BUnits.GRAY_PER_SECOND),
    WATT_PER_STERADIAN(0x2755, "org.bluetooth.unit.radiant_intensity.watt_per_steradian", BUnits.WATT_PER_STERADIAN),
    WATT_PER_STERADIAN_PER_SQUARE_METRE(0x2756, "org.bluetooth.unit.radiance.watt_per_square_metre_steradian",
            BUnits.WATT_PER_STERADIAN_PER_SQUARE_METRE),
    KATAL_PER_CUBIC_METRE(0x2757, "org.bluetooth.unit.catalytic_activity_concentration.katal_per_cubic_metre",
            Units.ONE),
    MINUTE(0x2760, "org.bluetooth.unit.time.minute", Units.MINUTE),
    HOUR(0x2761, "org.bluetooth.unit.time.hour", Units.HOUR),
    DAY(0x2762, "org.bluetooth.unit.time.day", Units.DAY),
    ANGLE_DEGREE(0x2763, "org.bluetooth.unit.plane_angle.degree", Units.DEGREE_ANGLE),
    ANGLE_MINUTE(0x2764, "org.bluetooth.unit.plane_angle.minute", BUnits.MINUTE_ANGLE),
    ANGLE_SECOND(0x2765, "org.bluetooth.unit.plane_angle.second", BUnits.SECOND_ANGLE),
    HECTARE(0x2766, "org.bluetooth.unit.area.hectare", BUnits.HECTARE),
    LITRE(0x2767, "org.bluetooth.unit.volume.litre", Units.LITRE),
    TONNE(0x2768, "org.bluetooth.unit.mass.tonne", MetricPrefix.KILO(SIUnits.KILOGRAM)),
    BAR(0x2780, "org.bluetooth.unit.pressure.bar", Units.BAR),
    MILLIMETRE_OF_MERCURY(0x2781, "org.bluetooth.unit.pressure.millimetre_of_mercury", Units.MILLIMETRE_OF_MERCURY),
    ÅNGSTRÖM(0x2782, "org.bluetooth.unit.length.ångström", Units.ONE),
    NAUTICAL_MILE(0x2783, "org.bluetooth.unit.length.nautical_mile", BUnits.NAUTICAL_MILE),
    BARN(0x2784, "org.bluetooth.unit.area.barn", BUnits.BARN),
    KNOT(0x2785, "org.bluetooth.unit.velocity.knot", Units.KNOT),
    NEPER(0x2786, "org.bluetooth.unit.logarithmic_radio_quantity.neper", Units.ONE),
    BEL(0x2787, "org.bluetooth.unit.logarithmic_radio_quantity.bel", Units.ONE),
    YARD(0x27A0, "org.bluetooth.unit.length.yard", ImperialUnits.YARD),
    PARSEC(0x27A1, "org.bluetooth.unit.length.parsec", Units.ONE),
    INCH(0x27A2, "org.bluetooth.unit.length.inch", ImperialUnits.INCH),
    FOOT(0x27A3, "org.bluetooth.unit.length.foot", ImperialUnits.FOOT),
    MILE(0x27A4, "org.bluetooth.unit.length.mile", ImperialUnits.MILE),
    POUND_FORCE_PER_SQUARE_INCH(0x27A5, "org.bluetooth.unit.pressure.pound_force_per_square_inch", Units.ONE),
    KILOMETRE_PER_HOUR(0x27A6, "org.bluetooth.unit.velocity.kilometre_per_hour", SIUnits.KILOMETRE_PER_HOUR),
    MILES_PER_HOUR(0x27A7, "org.bluetooth.unit.velocity.mile_per_hour", ImperialUnits.MILES_PER_HOUR),
    REVOLUTION_PER_MINUTE(0x27A8, "org.bluetooth.unit.angular_velocity.revolution_per_minute",
            BUnits.REVOLUTION_PER_MINUTE),
    GRAM_CALORIE(0x27A9, "org.bluetooth.unit.energy.gram_calorie", Units.ONE),
    KILOGRAM_CALORIE(0x27AA, "org.bluetooth.unit.energy.kilogram_calorie", Units.ONE),
    KILOWATT_HOUR(0x27AB, "org.bluetooth.unit.energy.kilowatt_hour", Units.KILOWATT_HOUR),
    DEGREE_FAHRENHEIT(0x27AC, "org.bluetooth.unit.thermodynamic_temperature.degree_fahrenheit",
            ImperialUnits.FAHRENHEIT),
    PERCENTAGE(0x27AD, "org.bluetooth.unit.percentage", Units.PERCENT),
    PER_MILLE(0x27AE, "org.bluetooth.unit.per_mille", Units.ONE),
    BEATS_PER_MINUTE(0x27AF, "org.bluetooth.unit.period.beats_per_minute", BUnits.BEATS_PER_MINUTE),
    AMPERE_HOURS(0x27B0, "org.bluetooth.unit.electric_charge.ampere_hours", BUnits.AMPERE_HOUR),
    MILLIGRAM_PER_DECILITRE(0x27B1, "org.bluetooth.unit.mass_density.milligram_per_decilitre", Units.ONE),
    MILLIMOLE_PER_LITRE(0x27B2, "org.bluetooth.unit.mass_density.millimole_per_litre", Units.ONE),
    YEAR(0x27B3, "org.bluetooth.unit.time.year", Units.YEAR),
    MONTH(0x27B4, "org.bluetooth.unit.time.month", Units.ONE),
    COUNT_PER_CUBIC_METRE(0x27B5, "org.bluetooth.unit.concentration.count_per_cubic_metre", Units.ONE),
    WATT_PER_SQUARE_METRE(0x27B6, "org.bluetooth.unit.irradiance.watt_per_square_metre", Units.IRRADIANCE),
    MILLILITER_PER_KILOGRAM_PER_MINUTE(0x27B7, "org.bluetooth.unit.transfer_rate.milliliter_per_kilogram_per_minute",
            Units.ONE),
    POUND(0x27B8, "org.bluetooth.unit.mass.pound", BUnits.POUND),
    METABOLIC_EQUIVALENT(0x27B9, "org.bluetooth.unit.metabolic_equivalent", Units.ONE),
    STEP_PER_MINUTE(0x27BA, "org.bluetooth.unit.step_per_minute", BUnits.STEP_PER_MINUTE),
    STROKE_PER_MINUTE(0x27BC, "org.bluetooth.unit.stroke_per_minute", BUnits.STROKE_PER_MINUTE),
    KILOMETER_PER_MINUTE(0x27BD, "org.bluetooth.unit.velocity.kilometer_per_minute", BUnits.KILOMETRE_PER_MINUTE),
    LUMEN_PER_WATT(0x27BE, "org.bluetooth.unit.luminous_efficacy.lumen_per_watt", BUnits.LUMEN_PER_WATT),
    LUMEN_HOUR(0x27BF, "org.bluetooth.unit.luminous_energy.lumen_hour", BUnits.LUMEN_HOUR),
    LUX_HOUR(0x27C0, "org.bluetooth.unit.luminous_exposure.lux_hour", BUnits.LUX_HOUR),
    GRAM_PER_SECOND(0x27C1, "org.bluetooth.unit.mass_flow.gram_per_second", BUnits.GRAM_PER_SECOND),
    LITRE_PER_SECOND(0x27C2, "org.bluetooth.unit.volume_flow.litre_per_second", BUnits.LITRE_PER_SECOND),
    DECIBEL_SPL(0x27C3, "org.bluetooth.unit.sound_pressure.decibel_spl", Units.ONE),
    PARTS_PER_MILLION(0x27C4, "org.bluetooth.unit.concentration.parts_per_million", Units.PARTS_PER_MILLION),
    PARTS_PER_BILLION(0x27C5, "org.bluetooth.unit.concentration.parts_per_billion", Units.PARTS_PER_BILLION);

    private UUID uuid;

    private String type;

    private Unit<?> unit;

    private BluetoothUnit(long key, String type, Unit<?> unit) {
        this.uuid = new UUID((key << 32) | 0x1000, BluetoothBindingConstants.BLUETOOTH_BASE_UUID);
        this.type = type;
        this.unit = unit;
    }

    public static @Nullable BluetoothUnit findByType(String type) {
        for (BluetoothUnit unit : BluetoothUnit.values()) {
            if (unit.type.equals(type)) {
                return unit;
            }
        }
        return null;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public Unit<?> getUnit() {
        return unit;
    }

    /**
     * This class contains the set of units that are not yet defined in Units.
     * Once these units are added to the core then this class will be removed.
     *
     * @author cpetty
     * @deprecated
     */
    @Deprecated
    public static class BUnits {
        public static final Unit<ArealDensity> KILOGRAM_PER_SQUARE_METER = addUnit(
                new ProductUnit<ArealDensity>(SIUnits.KILOGRAM.divide(SIUnits.SQUARE_METRE)));

        public static final Unit<RadiationExposure> COULOMB_PER_KILOGRAM = addUnit(
                new ProductUnit<RadiationExposure>(Units.COULOMB.divide(SIUnits.KILOGRAM)));

        public static final Unit<RadiationDoseAbsorptionRate> GRAY_PER_SECOND = addUnit(
                new ProductUnit<RadiationDoseAbsorptionRate>(Units.GRAY.divide(Units.SECOND)));

        public static final Unit<Mass> POUND = addUnit(
                new TransformedUnit<Mass>(SIUnits.KILOGRAM, MultiplyConverter.of(0.45359237)));

        public static final Unit<Angle> MINUTE_ANGLE = addUnit(new TransformedUnit<Angle>(Units.RADIAN,
                MultiplyConverter.ofPiExponent(1).concatenate(MultiplyConverter.ofRational(1, 180 * 60))));

        public static final Unit<Angle> SECOND_ANGLE = addUnit(new TransformedUnit<Angle>(Units.RADIAN,
                MultiplyConverter.ofPiExponent(1).concatenate(MultiplyConverter.ofRational(1, 180 * 60 * 60))));

        public static final Unit<Area> HECTARE = addUnit(SIUnits.SQUARE_METRE.multiply(10000.0));
        public static final Unit<Area> BARN = addUnit(SIUnits.SQUARE_METRE.multiply(10E-28));

        public static final Unit<Length> NAUTICAL_MILE = addUnit(SIUnits.METRE.multiply(1852.0));

        public static final Unit<RadiantIntensity> WATT_PER_STERADIAN = addUnit(
                new ProductUnit<RadiantIntensity>(Units.WATT.divide(Units.STERADIAN)));

        public static final Unit<Radiance> WATT_PER_STERADIAN_PER_SQUARE_METRE = addUnit(
                new ProductUnit<Radiance>(WATT_PER_STERADIAN.divide(SIUnits.SQUARE_METRE)));

        public static final Unit<Frequency> CYCLES_PER_MINUTE = addUnit(new TransformedUnit<Frequency>(Units.HERTZ,
                MultiplyConverter.ofRational(BigInteger.valueOf(60), BigInteger.ONE)));

        public static final Unit<Angle> REVOLUTION = addUnit(new TransformedUnit<Angle>(Units.RADIAN,
                MultiplyConverter.ofPiExponent(1).concatenate(MultiplyConverter.ofRational(2, 1))));
        public static final Unit<AngularVelocity> REVOLUTION_PER_MINUTE = addUnit(
                new ProductUnit<AngularVelocity>(REVOLUTION.divide(Units.MINUTE)));

        public static final Unit<Dimensionless> STEPS = addUnit(Units.ONE.alternate("steps"));
        public static final Unit<Dimensionless> BEATS = addUnit(Units.ONE.alternate("beats"));
        public static final Unit<Dimensionless> STROKE = addUnit(Units.ONE.alternate("stroke"));

        public static final Unit<Frequency> STEP_PER_MINUTE = addUnit(
                new ProductUnit<Frequency>(STEPS.divide(Units.MINUTE)));

        public static final Unit<Frequency> BEATS_PER_MINUTE = addUnit(
                new ProductUnit<Frequency>(BEATS.divide(Units.MINUTE)));

        public static final Unit<Frequency> STROKE_PER_MINUTE = addUnit(
                new ProductUnit<Frequency>(STROKE.divide(Units.MINUTE)));

        public static final Unit<MassFlowRate> GRAM_PER_SECOND = addUnit(
                new ProductUnit<MassFlowRate>(SIUnits.GRAM.divide(Units.SECOND)));

        public static final Unit<LuminousEfficacy> LUMEN_PER_WATT = addUnit(
                new ProductUnit<LuminousEfficacy>(Units.LUMEN.divide(Units.WATT)));

        public static final Unit<LuminousEnergy> LUMEN_SECOND = addUnit(
                new ProductUnit<LuminousEnergy>(Units.LUMEN.multiply(Units.SECOND)));

        public static final Unit<LuminousEnergy> LUMEN_HOUR = addUnit(
                new ProductUnit<LuminousEnergy>(Units.LUMEN.multiply(Units.HOUR)));

        public static final Unit<ElectricCharge> AMPERE_HOUR = addUnit(
                new ProductUnit<ElectricCharge>(Units.AMPERE.multiply(Units.HOUR)));

        public static final Unit<LuminousExposure> LUX_HOUR = addUnit(
                new ProductUnit<LuminousExposure>(Units.LUX.multiply(Units.HOUR)));

        public static final Unit<Speed> KILOMETRE_PER_MINUTE = addUnit(SIUnits.KILOMETRE_PER_HOUR.multiply(60.0));

        public static final Unit<VolumetricFlowRate> LITRE_PER_SECOND = addUnit(
                new ProductUnit<VolumetricFlowRate>(Units.LITRE.divide(Units.SECOND)));

        static {
            SimpleUnitFormat.getInstance().label(GRAY_PER_SECOND, "Gy/s");
            SimpleUnitFormat.getInstance().label(MINUTE_ANGLE, "'");
            SimpleUnitFormat.getInstance().label(SECOND_ANGLE, "\"");
            SimpleUnitFormat.getInstance().label(HECTARE, "ha");
            SimpleUnitFormat.getInstance().label(NAUTICAL_MILE, "NM");
            SimpleUnitFormat.getInstance().label(KILOGRAM_PER_SQUARE_METER, "kg/m²");
            SimpleUnitFormat.getInstance().label(POUND, "lb");
            SimpleUnitFormat.getInstance().label(CYCLES_PER_MINUTE, "cpm");
            SimpleUnitFormat.getInstance().label(GRAM_PER_SECOND, "g/s");
            SimpleUnitFormat.getInstance().label(LUMEN_SECOND, "lm·s");
            SimpleUnitFormat.getInstance().label(LUMEN_HOUR, "lm·h");
            SimpleUnitFormat.getInstance().label(LUMEN_PER_WATT, "lm/W");
            SimpleUnitFormat.getInstance().label(LUX_HOUR, "lx·h");
            SimpleUnitFormat.getInstance().label(KILOMETRE_PER_MINUTE, "km/min");
            SimpleUnitFormat.getInstance().label(LITRE_PER_SECOND, "l/s");
            SimpleUnitFormat.getInstance().label(BEATS_PER_MINUTE, "bpm");
            SimpleUnitFormat.getInstance().label(STEP_PER_MINUTE, "steps/min");
            SimpleUnitFormat.getInstance().label(STROKE_PER_MINUTE, "spm");
            SimpleUnitFormat.getInstance().label(REVOLUTION_PER_MINUTE, "rpm");
        }

        private static <U extends Unit<?>> U addUnit(U unit) {
            return unit;
        }

        public interface AngularVelocity extends Quantity<AngularVelocity> {
        }

        public interface LuminousEnergy extends Quantity<LuminousEnergy> {
        }

        public interface LuminousEfficacy extends Quantity<LuminousEfficacy> {
        }

        public interface LuminousExposure extends Quantity<LuminousExposure> {
        }

        public interface RadiantIntensity extends Quantity<RadiantIntensity> {
        }

        public interface Radiance extends Quantity<Radiance> {
        }

        public interface RadiationExposure extends Quantity<RadiationExposure> {
        }

        public interface RadiationDoseAbsorptionRate extends Quantity<RadiationDoseAbsorptionRate> {
        }

        public interface MassFlowRate extends Quantity<MassFlowRate> {
        }
    }
}

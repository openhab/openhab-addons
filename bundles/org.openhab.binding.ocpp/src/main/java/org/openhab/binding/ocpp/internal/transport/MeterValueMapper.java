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
package org.openhab.binding.ocpp.internal.transport;

import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.model.core.MeterValue;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.SampledValue;

/**
 * Maps an OCPP 1.6 {@code MeterValues} request to connector channel states.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class MeterValueMapper {

    private static final String DEFAULT_MEASURAND = "Energy.Active.Import.Register";
    private static final Logger LOGGER = LoggerFactory.getLogger(MeterValueMapper.class);

    private static final Set<String> PER_PHASE_CHANNELS = Set.of(CHANNEL_CURRENT_L1, CHANNEL_CURRENT_L2,
            CHANNEL_CURRENT_L3, CHANNEL_VOLTAGE_L1, CHANNEL_VOLTAGE_L2, CHANNEL_VOLTAGE_L3);

    // Currents deliberately do not sum across phases; only powers and energies do.
    private static final Set<String> SUMMABLE_MEASURANDS = Set.of("Power.Active.Import", "Power.Active.Export",
            "Power.Reactive.Import", "Power.Reactive.Export", "Power.Offered", "Energy.Active.Import.Register",
            "Energy.Active.Export.Register", "Energy.Reactive.Import.Register", "Energy.Reactive.Export.Register",
            "Energy.Active.Import.Interval", "Energy.Active.Export.Interval", "Energy.Reactive.Import.Interval",
            "Energy.Reactive.Export.Interval");

    private MeterValueMapper() {
    }

    /** Flatten a MeterValues request into channelId -&gt; state. */
    public static Map<String, State> toStates(MeterValuesRequest request) {
        Map<String, State> states = new LinkedHashMap<>();
        MeterValue[] meterValues = request.getMeterValue();
        if (meterValues == null) {
            return states;
        }
        for (MeterValue meterValue : meterValues) {
            SampledValue[] samples = meterValue.getSampledValue();
            if (samples == null) {
                continue;
            }
            Map<String, State> direct = new LinkedHashMap<>();
            Map<String, State> summed = new LinkedHashMap<>();
            for (SampledValue sample : samples) {
                try {
                    String measurand = measurandOf(sample);
                    String phase = sample.getPhase();
                    String channelId = channelFor(measurand, phase);
                    if (channelId == null) {
                        LOGGER.debug("No channel represents measurand {} with phase {}; sample ignored", measurand,
                                phase);
                        continue;
                    }
                    State state = toState(sample.getValue(), sample.getUnit(), measurand);
                    if (state == null) {
                        continue;
                    }
                    if (phase == null || PER_PHASE_CHANNELS.contains(channelId)) {
                        direct.put(channelId, state);
                    } else if (basePhase(phase) != null && SUMMABLE_MEASURANDS.contains(measurand)) {
                        summed.merge(channelId, state, MeterValueMapper::sum);
                    } else {
                        LOGGER.debug("Ignoring phase {} sample of {} — no meaningful aggregate for channel {}", phase,
                                measurand, channelId);
                    }
                } catch (RuntimeException e) {
                    LOGGER.warn("Skipping MeterValues sample (measurand={} value={} unit={}): {}",
                            sample.getMeasurand(), sample.getValue(), sample.getUnit(), e.getMessage());
                }
            }
            summed.forEach((channelId, state) -> {
                if (!direct.containsKey(channelId)) {
                    states.put(channelId, state);
                }
            });
            states.putAll(direct);
        }
        return states;
    }

    private static State sum(State a, State b) {
        if (a instanceof QuantityType<?> first && b instanceof QuantityType<?> second) {
            State total = sumQuantities(first, second);
            if (total != null) {
                return total;
            }
        } else if (a instanceof DecimalType first && b instanceof DecimalType second) {
            return new DecimalType(first.doubleValue() + second.doubleValue());
        }
        LOGGER.debug("Cannot aggregate {} and {}; keeping the first sample", a, b);
        return a;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static @Nullable State sumQuantities(QuantityType<?> first, QuantityType<?> second) {
        Unit<?> unit = first.getUnit();
        QuantityType converted = second.toUnit(unit);
        return converted == null ? null : ((QuantityType) first).add(converted);
    }

    static String measurandOf(SampledValue sample) {
        String measurand = sample.getMeasurand();
        return measurand == null || measurand.isBlank() ? DEFAULT_MEASURAND : measurand;
    }

    static @Nullable String channelFor(String measurand, @Nullable String phase) {
        switch (measurand) {
            case "Current.Import":
                return phase == null ? CHANNEL_CURRENT_IMPORT
                        : phaseChannel(phase, CHANNEL_CURRENT_L1, CHANNEL_CURRENT_L2, CHANNEL_CURRENT_L3);
            case "Current.Export":
                return CHANNEL_CURRENT_EXPORT;
            case "Current.Offered":
                return CHANNEL_CURRENT_OFFERED;
            case "Voltage":
                return phase == null ? CHANNEL_VOLTAGE
                        : phaseChannel(phase, CHANNEL_VOLTAGE_L1, CHANNEL_VOLTAGE_L2, CHANNEL_VOLTAGE_L3);
            case "Frequency":
                return CHANNEL_FREQUENCY;
            case "Power.Active.Import":
                return CHANNEL_POWER_ACTIVE_IMPORT;
            case "Power.Active.Export":
                return CHANNEL_POWER_ACTIVE_EXPORT;
            case "Power.Reactive.Import":
                return CHANNEL_POWER_REACTIVE_IMPORT;
            case "Power.Reactive.Export":
                return CHANNEL_POWER_REACTIVE_EXPORT;
            case "Power.Factor":
                return CHANNEL_POWER_FACTOR;
            case "Power.Offered":
                return CHANNEL_POWER_OFFERED;
            case "Energy.Active.Import.Register":
                return CHANNEL_ENERGY_ACTIVE_IMPORT;
            case "Energy.Active.Export.Register":
                return CHANNEL_ENERGY_ACTIVE_EXPORT;
            case "Energy.Reactive.Import.Register":
                return CHANNEL_ENERGY_REACTIVE_IMPORT;
            case "Energy.Reactive.Export.Register":
                return CHANNEL_ENERGY_REACTIVE_EXPORT;
            case "Energy.Active.Import.Interval":
                return CHANNEL_ENERGY_ACTIVE_IMPORT_INTERVAL;
            case "Energy.Active.Export.Interval":
                return CHANNEL_ENERGY_ACTIVE_EXPORT_INTERVAL;
            case "Energy.Reactive.Import.Interval":
                return CHANNEL_ENERGY_REACTIVE_IMPORT_INTERVAL;
            case "Energy.Reactive.Export.Interval":
                return CHANNEL_ENERGY_REACTIVE_EXPORT_INTERVAL;
            case "SoC":
                return CHANNEL_SOC;
            case "RPM":
                return CHANNEL_RPM;
            case "Temperature":
                return CHANNEL_TEMPERATURE;
            default:
                return null;
        }
    }

    /** The single conductor a phase identifier denotes, or {@code null} for line-to-line/neutral. */
    private static @Nullable String basePhase(String phase) {
        switch (phase) {
            case "L1":
            case "L1-N":
                return "L1";
            case "L2":
            case "L2-N":
                return "L2";
            case "L3":
            case "L3-N":
                return "L3";
            default:
                return null;
        }
    }

    private static @Nullable String phaseChannel(String phase, String l1, String l2, String l3) {
        String base = basePhase(phase);
        if (base == null) {
            return null;
        }
        switch (base) {
            case "L1":
                return l1;
            case "L2":
                return l2;
            default:
                return l3;
        }
    }

    static @Nullable State toState(@Nullable String value, @Nullable String unit, String measurand) {
        if ((unit == null || unit.isBlank()) && measurand.startsWith("Energy.")) {
            return toState(value, "Wh");
        }
        return toState(value, unit);
    }

    static @Nullable State toState(@Nullable String value, @Nullable String unit) {
        if (value == null || value.isBlank()) {
            return null;
        }
        double parsed;
        try {
            parsed = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        if (!Double.isFinite(parsed)) {
            // NaN/Infinity are not representable as a QuantityType.
            return null;
        }
        if (unit == null || unit.isBlank()) {
            return new DecimalType(parsed);
        }
        switch (unit) {
            case "A":
                return new QuantityType<>(parsed, Units.AMPERE);
            case "V":
                return new QuantityType<>(parsed, Units.VOLT);
            case "W":
                return new QuantityType<>(parsed, Units.WATT);
            case "kW":
                return new QuantityType<>(parsed, MetricPrefix.KILO(Units.WATT));
            case "Wh":
                return new QuantityType<>(parsed, Units.WATT_HOUR);
            case "kWh":
                return new QuantityType<>(parsed, Units.KILOWATT_HOUR);
            case "VA":
                return new QuantityType<>(parsed, Units.VOLT_AMPERE);
            case "kVA":
                return new QuantityType<>(parsed, Units.KILOVOLT_AMPERE);
            case "var":
                return new QuantityType<>(parsed, Units.VAR);
            case "kvar":
                return new QuantityType<>(parsed, Units.KILOVAR);
            case "varh":
                return new QuantityType<>(parsed, Units.VAR_HOUR);
            case "kvarh":
                return new QuantityType<>(parsed, Units.KILOVAR_HOUR);
            case "Percent":
                return new QuantityType<>(parsed, Units.PERCENT);
            case "Hertz":
                return new QuantityType<>(parsed, Units.HERTZ);
            case "K":
                return new QuantityType<>(parsed, Units.KELVIN);
            case "Celsius":
            case "Celcius": // OCPP spec's own mis-spelling
                return new QuantityType<>(parsed, SIUnits.CELSIUS);
            case "Fahrenheit":
                return new QuantityType<>(parsed, ImperialUnits.FAHRENHEIT);
            default:
                LOGGER.debug("Unrecognised MeterValues unit '{}'; keeping a dimensionless value", unit);
                return new DecimalType(parsed);
        }
    }
}

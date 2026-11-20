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
package org.openhab.binding.e3dc.internal;

import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;

/**
 * Describes where one channel's value comes from in an RSCP response and how to scale it.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCChannelSpec(int tag, int container, double divisor, int bitMask, int byteIndex, int nestedTag,
        int valueIndex, boolean list, boolean countBits) {

    /** Marks a spec that reads the first occurrence of its tag. */
    public static final int NO_INDEX = -1;

    public static E3DCChannelSpec of(int tag) {
        return new E3DCChannelSpec(tag, 0, 1, 0, -1, 0, NO_INDEX, false, false);
    }

    /** Renders the whole list of idle periods as text; they are containers, not one value. */
    public static E3DCChannelSpec idlePeriods() {
        return new E3DCChannelSpec(RscpTag.EMS_GET_IDLE_PERIODS_2, 0, 1, 0, -1, 0, NO_INDEX, true, false);
    }

    /** Reports the stored messages, which arrive as one container per message. */
    public static E3DCChannelSpec messages() {
        return new E3DCChannelSpec(RscpTag.EMS_STORED_ERRORS, 0, 1, 0, -1, 0, NO_INDEX, true, false);
    }

    public boolean isList() {
        return list;
    }

    public static E3DCChannelSpec in(int container, int tag) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, 0, NO_INDEX, false, false);
    }

    public static E3DCChannelSpec scaledIn(int container, int tag, double divisor) {
        return new E3DCChannelSpec(tag, container, divisor, 0, -1, 0, NO_INDEX, false, false);
    }

    /**
     * A value the device reports as a fraction of a whole instead of as a percentage, for example {@code 0.7} for
     * 70 percent of the installed peak power.
     */
    public static E3DCChannelSpec fraction(int tag) {
        return new E3DCChannelSpec(tag, 0, 0.01, 0, -1, 0, NO_INDEX, false, false);
    }

    /** One of several occurrences of the tag, selected by position because the values carry no index. */
    public static E3DCChannelSpec indexedIn(int container, int tag, int valueIndex) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, 0, valueIndex, false, false);
    }

    public static E3DCChannelSpec nested(int container, int nestedContainer, int tag) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, nestedContainer, NO_INDEX, false, false);
    }

    public static E3DCChannelSpec scaledNested(int container, int nestedContainer, int tag, double divisor) {
        return new E3DCChannelSpec(tag, container, divisor, 0, -1, nestedContainer, NO_INDEX, false, false);
    }

    /** One occurrence, selected by the index its container carries, or by position when it carries none. */
    public static E3DCChannelSpec indexedNested(int container, int nestedContainer, int tag, int valueIndex) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, nestedContainer, valueIndex, false, false);
    }

    public static E3DCChannelSpec bitCount(int tag) {
        return new E3DCChannelSpec(tag, 0, 1, 0, -1, 0, NO_INDEX, false, true);
    }

    public static E3DCChannelSpec bitCountIn(int container, int tag) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, 0, NO_INDEX, false, true);
    }

    public static E3DCChannelSpec bit(int tag, int bitMask) {
        return new E3DCChannelSpec(tag, 0, 1, bitMask, -1, 0, NO_INDEX, false, false);
    }

    public static E3DCChannelSpec bitIn(int container, int tag, int bitMask) {
        return new E3DCChannelSpec(tag, container, 1, bitMask, -1, 0, NO_INDEX, false, false);
    }

    public static E3DCChannelSpec bitAt(int tag, int byteIndex, int bitMask) {
        return new E3DCChannelSpec(tag, 0, 1, bitMask, byteIndex, 0, NO_INDEX, false, false);
    }

    public boolean isBitField() {
        return bitMask != 0;
    }

    public List<Integer> valuePath() {
        List<Integer> path = new ArrayList<>(3);
        if (container != 0) {
            path.add(container);
            if (nestedTag != 0) {
                path.add(nestedTag);
            }
        }
        path.add(tag);
        return path;
    }

    public double scale(double raw) {
        if (countBits) {
            return Long.bitCount((long) raw);
        }
        if (divisor == 1) {
            return raw;
        }
        if (divisor < 1) {
            // a fraction of a whole becomes its percentage; the float the device sends carries binary noise, so
            // the percentage is rounded instead of showing a long decimal part
            return Math.round(raw / divisor * 100) / 100.0;
        }
        return raw / divisor;
    }

    /**
     * The power plant channels, with tag, container and divisor from the RSCP interface definitions. Energy
     * counters are reported by the device in Wh and published as kWh; channels that mirror one bit of a status
     * word carry that mask.
     */
    public static E3DCChannelRegistry powerPlant() {
        E3DCChannelRegistry registry = new E3DCChannelRegistry();

        registry.group(E3DCBindingConstants.GROUP_POWER, Map.ofEntries(//
                entry("solar-power", E3DCChannelSpec.of(RscpTag.EMS_POWER_PV)), //
                entry("battery-power", E3DCChannelSpec.of(RscpTag.EMS_POWER_BAT)), //
                entry("house-power", E3DCChannelSpec.of(RscpTag.EMS_POWER_HOME)), //
                entry("grid-power", E3DCChannelSpec.of(RscpTag.EMS_POWER_GRID)), //
                entry("additional-power", E3DCChannelSpec.of(RscpTag.EMS_POWER_ADD)), //
                entry("wallbox-power", E3DCChannelSpec.of(RscpTag.EMS_POWER_WB_ALL)), //
                entry("wallbox-solar-power", E3DCChannelSpec.of(RscpTag.EMS_POWER_WB_SOLAR)), //
                entry("autarky",
                        E3DCChannelSpec.nested(RscpTag.DB_HISTORY_DATA_DAY, RscpTag.DB_SUM_CONTAINER,
                                RscpTag.DB_AUTARKY)), //
                entry("self-consumption", E3DCChannelSpec.nested(RscpTag.DB_HISTORY_DATA_DAY, RscpTag.DB_SUM_CONTAINER,
                        RscpTag.DB_CONSUMED_PRODUCTION))));

        registry.group(E3DCBindingConstants.GROUP_BATTERY, Map.ofEntries(//
                entry("soc", E3DCChannelSpec.of(RscpTag.EMS_BAT_SOC)), //
                // the battery reports a state of charge of its own next to the one the energy manager calculates
                entry("rsoc", E3DCChannelSpec.of(RscpTag.BAT_RSOC)), //
                entry("remaining-charge-power", E3DCChannelSpec.of(RscpTag.EMS_REMAINING_BAT_CHARGE_POWER)), //
                entry("remaining-discharge-power", E3DCChannelSpec.of(RscpTag.EMS_REMAINING_BAT_DISCHARGE_POWER)), //
                entry("charging-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 1)), //
                entry("discharging-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 2)), //
                entry("charging-throttled", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 8)), //
                entry("charging-time-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 32)), //
                entry("discharging-time-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 64))));

        // the limits the device reports and the ones the user sets describe the same topic
        registry.group(E3DCBindingConstants.GROUP_BATTERY_LIMITS, Map.ofEntries(//
                entry("max-charge-power",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_MAX_CHARGE_POWER)), //
                entry("max-discharge-power",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_MAX_DISCHARGE_POWER)), //
                entry("discharge-start-power",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_DISCHARGE_START_POWER)), //
                entry("power-limits-used",
                        E3DCChannelSpec.bitIn(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_POWER_LIMITS_USED, 1)), //
                entry("charge-limit-battery", E3DCChannelSpec.of(RscpTag.EMS_BAT_CHARGE_LIMIT)), //
                entry("charge-limit-dcdc", E3DCChannelSpec.of(RscpTag.EMS_DCDC_CHARGE_LIMIT)), //
                entry("charge-limit-used", E3DCChannelSpec.of(RscpTag.EMS_USED_CHARGE_LIMIT)), //
                entry("charge-limit-user", E3DCChannelSpec.of(RscpTag.EMS_USER_CHARGE_LIMIT)), //
                entry("discharge-limit-battery", E3DCChannelSpec.of(RscpTag.EMS_BAT_DISCHARGE_LIMIT)), //
                entry("discharge-limit-dcdc", E3DCChannelSpec.of(RscpTag.EMS_DCDC_DISCHARGE_LIMIT)), //
                entry("discharge-limit-used", E3DCChannelSpec.of(RscpTag.EMS_USED_DISCHARGE_LIMIT)), //
                entry("discharge-limit-user", E3DCChannelSpec.of(RscpTag.EMS_USER_DISCHARGE_LIMIT))));

        registry.group(E3DCBindingConstants.GROUP_BATTERY_MODE, Map.ofEntries(//
                entry("active-mode", E3DCChannelSpec.of(RscpTag.EMS_MODE)), //
                entry("weather-regulated-charge", E3DCChannelSpec.bitIn(RscpTag.EMS_GET_POWER_SETTINGS,
                        RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED, 1))));

        // the device reports one list of periods; each channel renders the periods of its direction
        registry.group(E3DCBindingConstants.GROUP_IDLE_PERIODS, Map.ofEntries(//
                entry("enabled", E3DCChannelSpec.of(RscpTag.EMS_GET_IDLE_PERIODS_ENABLE)), //
                entry("charge-periods", E3DCChannelSpec.idlePeriods()), //
                entry("discharge-periods", E3DCChannelSpec.idlePeriods())));

        registry.group(E3DCBindingConstants.GROUP_WALLBOX_PRIORITY, Map.ofEntries(//
                entry("charge-battery-before-car", E3DCChannelSpec.bit(RscpTag.EMS_BATTERY_BEFORE_CAR_MODE, 1)), //
                entry("discharge-battery-to-car", E3DCChannelSpec.bit(RscpTag.EMS_BATTERY_TO_CAR_MODE, 1)), //
                entry("discharge-until", E3DCChannelSpec.of(RscpTag.EMS_GET_WB_DISCHARGE_BAT_UNTIL)), //
                entry("battery-off-in-mix-mode",
                        E3DCChannelSpec.bit(RscpTag.EMS_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT, 1))));

        registry.group(E3DCBindingConstants.GROUP_GRID, Map.ofEntries(//
                entry("active-phases", E3DCChannelSpec.bitCount(RscpTag.EMS_BALANCED_PHASES)), //
                entry("limit-active", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 16)), //
                entry("derate-at-percent", E3DCChannelSpec.fraction(RscpTag.EMS_DERATE_AT_PERCENT_VALUE)), //
                entry("derate-at-power", E3DCChannelSpec.of(RscpTag.EMS_DERATE_AT_POWER_VALUE))));

        registry.group(E3DCBindingConstants.GROUP_INVERTER, inverterChannels());

        registry.group(E3DCBindingConstants.GROUP_SYSTEM, Map.ofEntries(//
                entry("powersave",
                        E3DCChannelSpec.bitIn(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_POWERSAVE_ENABLED, 1)), //
                // both channels read the same answer; the handler tells them apart by their id
                entry(E3DCBindingConstants.CHANNEL_HAS_MESSAGES, E3DCChannelSpec.messages()), //
                entry(E3DCBindingConstants.CHANNEL_MESSAGES, E3DCChannelSpec.messages())));

        // the values of the manual charge sit in one container, which the device answers as a whole
        registry.group(E3DCBindingConstants.GROUP_MANUAL_BATTERY_CHARGE, Map.ofEntries(//
                entry("manual-charge-active",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_MANUAL_CHARGE, RscpTag.EMS_MANUAL_CHARGE_ACTIVE)), //
                // both energies are reported in Wh and published in Wh, see E3DCPowerPlantHandler
                entry("manual-charge-energy",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_MANUAL_CHARGE, RscpTag.EMS_MANUAL_CHARGE_ENERGY_COUNTER)), //
                entry("manual-charge-target",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_MANUAL_CHARGE, RscpTag.EMS_UNDEFINED_POWER_VALUE)), //
                entry("manual-charge-last-start",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_MANUAL_CHARGE, RscpTag.EMS_MANUAL_CHARGE_LASTSTART))));

        registry.group(E3DCBindingConstants.GROUP_HISTORY, historyChannels());

        registry.group(E3DCBindingConstants.GROUP_EMERGENCY, Map.ofEntries(//
                entry("percent", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_EP_RESERVE)), //
                entry("energy", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_EP_RESERVE_W)), //
                entry("energy-max", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_EP_RESERVE_MAX_W)), //
                entry("last-soc", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_LAST_SOC)), //
                entry("power-status", E3DCChannelSpec.of(RscpTag.EMS_EMERGENCY_POWER_STATUS)), //
                entry("power-available", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 4))));

        return registry;
    }

    /**
     * The energy counters of the history periods. A period is requested by its own container and reports the
     * values in a sum container, which is why the specs name two containers; the counters arrive in Wh and
     * the channels publish kWh.
     */
    private static Map<String, E3DCChannelSpec> historyChannels() {
        Map<String, E3DCChannelSpec> channels = new LinkedHashMap<>();
        addPeriod(channels, "day", RscpTag.DB_HISTORY_DATA_DAY);
        addPeriod(channels, "week", RscpTag.DB_HISTORY_DATA_WEEK);
        addPeriod(channels, "month", RscpTag.DB_HISTORY_DATA_MONTH);
        addPeriod(channels, "year", RscpTag.DB_HISTORY_DATA_YEAR);
        return channels;
    }

    private static void addPeriod(Map<String, E3DCChannelSpec> channels, String prefix, int periodContainer) {
        channels.put(prefix + "-solar", history(periodContainer, RscpTag.DB_DC_POWER, 1000));
        channels.put(prefix + "-house", history(periodContainer, RscpTag.DB_CONSUMPTION, 1000));
        channels.put(prefix + "-grid-in", history(periodContainer, RscpTag.DB_GRID_POWER_IN, 1000));
        channels.put(prefix + "-grid-out", history(periodContainer, RscpTag.DB_GRID_POWER_OUT, 1000));
        channels.put(prefix + "-battery-in", history(periodContainer, RscpTag.DB_BAT_POWER_IN, 1000));
        channels.put(prefix + "-battery-out", history(periodContainer, RscpTag.DB_BAT_POWER_OUT, 1000));
        // the two shares are reported in percent, so they must not be scaled
        channels.put(prefix + "-autarky", history(periodContainer, RscpTag.DB_AUTARKY, 1));
        channels.put(prefix + "-consumed-production", history(periodContainer, RscpTag.DB_CONSUMED_PRODUCTION, 1));
    }

    private static E3DCChannelSpec history(int periodContainer, int tag, double divisor) {
        return E3DCChannelSpec.scaledNested(periodContainer, RscpTag.DB_SUM_CONTAINER, tag, divisor);
    }

    /**
     * The wallbox channels. Per wallbox values live in the {@code WB_DATA} container; plugged, locked and
     * charging are bits of one status byte inside the {@code WB_EXTERN_DATA} byte array.
     */
    public static E3DCChannelRegistry wallbox() {
        return new E3DCChannelRegistry().group(E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, Map.ofEntries(//
                entry("plugged", E3DCChannelSpec.bitAt(RscpTag.WB_EXTERN_DATA, 2, 8)), //
                entry("locked", E3DCChannelSpec.bitAt(RscpTag.WB_EXTERN_DATA, 2, 16)), //
                entry("charging", E3DCChannelSpec.bitAt(RscpTag.WB_EXTERN_DATA, 2, 32)), //
                entry("key-state", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_KEY_STATE, 1)), //
                entry("soc", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_SOC)), //
                entry("sun-mode", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_SUN_MODE_ACTIVE)), //
                entry("suspended", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_ABORT_CHARGING)), //
                entry("min-charge-current", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_MIN_CHARGE_CURRENT)), //
                entry("max-charge-current", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_MAX_CHARGE_CURRENT)), //
                entry("number-phases", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_NUMBER_PHASES)), //
                entry("auto-phase-switch", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_AUTO_PHASE_SWITCH_ENABLED))))
                .group(E3DCBindingConstants.GROUP_WALLBOX_SESSION, Map.ofEntries(//
                        entry("power-l1", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_PM_POWER_L1)), //
                        entry("power-l2", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_PM_POWER_L2)), //
                        entry("power-l3", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_PM_POWER_L3)), //
                        entry("phase-l1", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_PM_ACTIVE_PHASES, 1)), //
                        entry("phase-l2", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_PM_ACTIVE_PHASES, 2)), //
                        entry("phase-l3", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_PM_ACTIVE_PHASES, 4)), //
                        entry("energy-l1", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_PM_ENERGY_L1, 1000)), //
                        entry("energy-l2", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_PM_ENERGY_L2, 1000)), //
                        entry("energy-l3", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_PM_ENERGY_L3, 1000))))
                .group(E3DCBindingConstants.GROUP_WALLBOX_COUNTERS, Map.ofEntries(//
                        entry("total", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_ENERGY_ALL, 1000)), //
                        entry("solar", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_ENERGY_SOLAR, 1000))));
    }

    public static E3DCChannelRegistry powerMeter() {
        return new E3DCChannelRegistry().group(E3DCBindingConstants.GROUP_METER, Map.ofEntries(//
                entry("power-l1", E3DCChannelSpec.in(RscpTag.PM_DATA, RscpTag.PM_POWER_L1)), //
                entry("power-l2", E3DCChannelSpec.in(RscpTag.PM_DATA, RscpTag.PM_POWER_L2)), //
                entry("power-l3", E3DCChannelSpec.in(RscpTag.PM_DATA, RscpTag.PM_POWER_L3)), //
                entry("voltage-l1", E3DCChannelSpec.in(RscpTag.PM_DATA, RscpTag.PM_VOLTAGE_L1)), //
                entry("voltage-l2", E3DCChannelSpec.in(RscpTag.PM_DATA, RscpTag.PM_VOLTAGE_L2)), //
                entry("voltage-l3", E3DCChannelSpec.in(RscpTag.PM_DATA, RscpTag.PM_VOLTAGE_L3)), //
                entry("active-phases", E3DCChannelSpec.bitCountIn(RscpTag.PM_DATA, RscpTag.PM_ACTIVE_PHASES)), //
                entry("energy-l1", E3DCChannelSpec.scaledIn(RscpTag.PM_DATA, RscpTag.PM_ENERGY_L1, 1000)), //
                entry("energy-l2", E3DCChannelSpec.scaledIn(RscpTag.PM_DATA, RscpTag.PM_ENERGY_L2, 1000)), //
                entry("energy-l3", E3DCChannelSpec.scaledIn(RscpTag.PM_DATA, RscpTag.PM_ENERGY_L3, 1000)), //
                entry("max-phase-power", E3DCChannelSpec.in(RscpTag.PM_DATA, RscpTag.PM_MAX_PHASE_POWER))));
    }

    /**
     * The channels of the inverter. Every value arrives as a container of its own carrying the value under
     * {@code PVI_VALUE}; the trackers and temperature sensors are created at runtime, see
     * {@link E3DCInverterChannels}.
     */
    private static Map<String, E3DCChannelSpec> inverterChannels() {
        Map<String, E3DCChannelSpec> channels = new LinkedHashMap<>();
        for (int phase = 0; phase < 3; phase++) {
            String suffix = "-l" + (phase + 1);
            channels.put("ac-power" + suffix, phase(RscpTag.PVI_AC_POWER, phase, 1));
            channels.put("ac-voltage" + suffix, phase(RscpTag.PVI_AC_VOLTAGE, phase, 1));
            channels.put("ac-current" + suffix, phase(RscpTag.PVI_AC_CURRENT, phase, 1));
            channels.put("apparent-power" + suffix, phase(RscpTag.PVI_AC_APPARENT_POWER, phase, 1));
            channels.put("reactive-power" + suffix, phase(RscpTag.PVI_AC_REACTIVE_POWER, phase, 1));
            channels.put("energy-total" + suffix, phase(RscpTag.PVI_AC_ENERGY_ALL, phase, 1000));
            channels.put("energy-day" + suffix, phase(RscpTag.PVI_AC_ENERGY_DAY, phase, 1000));
        }
        channels.put("ac-frequency", parameter(RscpTag.PVI_AC_FREQUENCY));
        // these two are answered as plain values rather than in a container of their own
        channels.put("on-grid", E3DCChannelSpec.in(RscpTag.PVI_DATA, RscpTag.PVI_ON_GRID));
        channels.put("string-count", E3DCChannelSpec.in(RscpTag.PVI_DATA, RscpTag.PVI_USED_STRING_COUNT));
        return channels;
    }

    /** One phase of a value the inverter reports once per phase, addressed by the position of its container. */
    private static E3DCChannelSpec phase(int tag, int phase, double divisor) {
        return new E3DCChannelSpec(RscpTag.PVI_VALUE, RscpTag.PVI_DATA, divisor, 0, -1, tag, phase, false, false);
    }

    private static E3DCChannelSpec parameter(int tag) {
        return E3DCChannelSpec.nested(RscpTag.PVI_DATA, tag, RscpTag.PVI_VALUE);
    }

    public static E3DCChannelRegistry battery() {
        Map<String, E3DCChannelSpec> channels = new LinkedHashMap<>();
        channels.put("voltage", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_MODULE_VOLTAGE));
        channels.put("current", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_CURRENT));
        channels.put("charge-cycles", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_CHARGE_CYCLES));
        channels.put("soh", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_ASOC));
        channels.put("status-code", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_STATUS_CODE));
        channels.put("error-code", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_ERROR_CODE));
        channels.put("training-mode", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_TRAINING_MODE));
        channels.put("design-capacity", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_DESIGN_CAPACITY));
        channels.put("usable-capacity", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_USABLE_CAPACITY));
        channels.put("usable-remaining-capacity",
                E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_USABLE_REMAINING_CAPACITY));
        return new E3DCChannelRegistry().group(E3DCBindingConstants.GROUP_PACK, channels);
    }

    /**
     * The channels of one battery cell module. A module reports its values inside a {@code BAT_DCB_INFO}
     * container that identifies itself with {@code BAT_DCB_INDEX}; the handler picks the container of the
     * configured module.
     */
    public static E3DCChannelRegistry batteryModule() {
        Map<String, Integer> tags = new LinkedHashMap<>();
        tags.put("soc", RscpTag.BAT_DCB_SOC);
        tags.put("soh", RscpTag.BAT_DCB_SOH);
        tags.put("voltage", RscpTag.BAT_DCB_VOLTAGE);
        tags.put("voltage-avg-30s", RscpTag.BAT_DCB_VOLTAGE_AVG_30S);
        tags.put("current", RscpTag.BAT_DCB_CURRENT);
        tags.put("current-avg-30s", RscpTag.BAT_DCB_CURRENT_AVG_30S);
        tags.put("remaining-capacity", RscpTag.BAT_DCB_REMAINING_CAPACITY);
        tags.put("full-charge-capacity", RscpTag.BAT_DCB_FULL_CHARGE_CAPACITY);
        tags.put("design-capacity", RscpTag.BAT_DCB_DESIGN_CAPACITY);
        tags.put("charge-high-temperature", RscpTag.BAT_DCB_CHARGE_HIGH_TEMPERATURE);
        tags.put("charge-low-temperature", RscpTag.BAT_DCB_CHARGE_LOW_TEMPERATURE);
        tags.put("cycles", RscpTag.BAT_DCB_CYCLE_COUNT);
        tags.put("status", RscpTag.BAT_DCB_STATUS);
        tags.put("warning", RscpTag.BAT_DCB_WARNING);
        tags.put("alarm", RscpTag.BAT_DCB_ALARM);
        tags.put("error", RscpTag.BAT_DCB_ERROR);

        Map<String, E3DCChannelSpec> channels = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> tag : tags.entrySet()) {
            channels.put(tag.getKey(), E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, tag.getValue()));
        }
        return new E3DCChannelRegistry().group(E3DCBindingConstants.GROUP_MODULE, channels);
    }
}

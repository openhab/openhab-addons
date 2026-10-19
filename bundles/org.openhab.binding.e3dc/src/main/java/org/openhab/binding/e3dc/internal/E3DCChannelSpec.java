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
 * @param tag the tag carrying the value
 * @param container the container the tag is nested in, or {@code 0} for the root container
 * @param divisor the raw value is divided by this to get the channel value
 * @param bitMask when non zero, the value is treated as a bit field and this bit is reported
 * @param byteIndex when non negative, the value is a byte array and this element is used
 * @param nestedTag when non zero, the tag sits in a container nested inside {@code container}, as in
 *            the history data where the values are wrapped in a sum container
 * @param valueIndex when non negative, the tag occurs several times in the response and this occurrence
 *            is read, as the photovoltaic trackers report one value per tracker
 * @param list whether the channel renders a list of containers as text rather than one value
 * @param countBits whether the value is a bit mask whose set bits are counted, such as the phases in use
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCChannelSpec(int tag, int container, double divisor, int bitMask, int byteIndex, int nestedTag,
        int valueIndex, boolean list, boolean countBits) {

    /** Marks a spec that reads the first occurrence of its tag. */
    public static final int NO_INDEX = -1;

    /** A plain value in the root container. */
    public static E3DCChannelSpec of(int tag) {
        return new E3DCChannelSpec(tag, 0, 1, 0, -1, 0, NO_INDEX, false, false);
    }

    /**
     * A channel that does not report a single value but renders the whole list of idle periods as text.
     *
     * <p>
     * The periods are a list of containers rather than one value, so they cannot be published by the
     * generic value extraction.
     */
    public static E3DCChannelSpec idlePeriods() {
        return new E3DCChannelSpec(RscpTag.EMS_GET_IDLE_PERIODS_2, 0, 1, 0, -1, 0, NO_INDEX, true, false);
    }

    /** Whether this channel renders a list of containers instead of one value. */
    public boolean isList() {
        return list;
    }

    /** A value nested in a container, as used by the power settings and the wallbox data. */
    public static E3DCChannelSpec in(int container, int tag) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, 0, NO_INDEX, false, false);
    }

    /**
     * A value that needs scaling and sits in a container, as used by the energy counters of the
     * inverter.
     */
    public static E3DCChannelSpec scaledIn(int container, int tag, double divisor) {
        return new E3DCChannelSpec(tag, container, divisor, 0, -1, 0, NO_INDEX, false, false);
    }

    /**
     * One value that a container reports several times, such as the direct current values of the
     * photovoltaic trackers. The occurrence is selected by its position, because the values carry no
     * index of their own.
     */
    public static E3DCChannelSpec indexedIn(int container, int tag, int valueIndex) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, 0, valueIndex, false, false);
    }

    /**
     * A value wrapped in a container inside another container, as used by the history data: the
     * response of the history request carries the values in a sum container.
     */
    public static E3DCChannelSpec nested(int container, int nestedContainer, int tag) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, nestedContainer, NO_INDEX, false, false);
    }

    /** A nested history value that needs scaling, for example Wh to kWh. */
    public static E3DCChannelSpec scaledNested(int container, int nestedContainer, int tag, double divisor) {
        return new E3DCChannelSpec(tag, container, divisor, 0, -1, nestedContainer, NO_INDEX, false, false);
    }

    /**
     * One occurrence of a value that a container reports as several containers of its own, as the inverter
     * reports its direct current values once per tracker. The occurrence is selected by the position of the
     * value's container, because the containers carry no index of their own.
     */
    public static E3DCChannelSpec indexedNested(int container, int nestedContainer, int tag, int valueIndex) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, nestedContainer, valueIndex, false, false);
    }

    /** A bit mask reported as the number of its set bits, such as the phases in use. */
    public static E3DCChannelSpec bitCount(int tag) {
        return new E3DCChannelSpec(tag, 0, 1, 0, -1, 0, NO_INDEX, false, true);
    }

    /** A bit mask inside a container, reported as the number of its set bits. */
    public static E3DCChannelSpec bitCountIn(int container, int tag) {
        return new E3DCChannelSpec(tag, container, 1, 0, -1, 0, NO_INDEX, false, true);
    }

    /** One bit of a numeric bit field, reported as a switch. */
    public static E3DCChannelSpec bit(int tag, int bitMask) {
        return new E3DCChannelSpec(tag, 0, 1, bitMask, -1, 0, NO_INDEX, false, false);
    }

    /** One bit inside a container. */
    public static E3DCChannelSpec bitIn(int container, int tag, int bitMask) {
        return new E3DCChannelSpec(tag, container, 1, bitMask, -1, 0, NO_INDEX, false, false);
    }

    /** One bit of a byte inside a byte array value, as used by the wallbox status byte. */
    public static E3DCChannelSpec bitAt(int tag, int byteIndex, int bitMask) {
        return new E3DCChannelSpec(tag, 0, 1, bitMask, byteIndex, 0, NO_INDEX, false, false);
    }

    public boolean isBitField() {
        return bitMask != 0;
    }

    /**
     * The tag chain to follow from the response root to reach the value: the container, the optional
     * nested container and the tag itself.
     */
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

    /** Applies the divisor and returns the value as it should be published. */
    public double scale(double raw) {
        if (countBits) {
            return Long.bitCount((long) raw);
        }
        return divisor == 1 ? raw : raw / divisor;
    }

    /**
     * The power plant channels.
     *
     * <p>
     * Tag, container and divisor come from the RSCP interface definitions. Energy counters are
     * reported by the device in Wh and published as kWh. Channels that mirror one bit of a status
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
                entry("max-charge-power",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_MAX_CHARGE_POWER)), //
                entry("max-discharge-power",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_MAX_DISCHARGE_POWER)), //
                entry("discharge-start-power",
                        E3DCChannelSpec.in(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_DISCHARGE_START_POWER)), //
                entry("charge-limit-battery", E3DCChannelSpec.of(RscpTag.EMS_BAT_CHARGE_LIMIT)), //
                entry("charge-limit-dcdc", E3DCChannelSpec.of(RscpTag.EMS_DCDC_CHARGE_LIMIT)), //
                entry("charge-limit-used", E3DCChannelSpec.of(RscpTag.EMS_USED_CHARGE_LIMIT)), //
                entry("charge-limit-user", E3DCChannelSpec.of(RscpTag.EMS_USER_CHARGE_LIMIT)), //
                entry("discharge-limit-battery", E3DCChannelSpec.of(RscpTag.EMS_BAT_DISCHARGE_LIMIT)), //
                entry("discharge-limit-dcdc", E3DCChannelSpec.of(RscpTag.EMS_DCDC_DISCHARGE_LIMIT)), //
                entry("discharge-limit-used", E3DCChannelSpec.of(RscpTag.EMS_USED_DISCHARGE_LIMIT)), //
                entry("discharge-limit-user", E3DCChannelSpec.of(RscpTag.EMS_USER_DISCHARGE_LIMIT)), //
                entry("set-power", E3DCChannelSpec.of(RscpTag.EMS_SET_POWER)), //
                entry("wallbox-discharge-to-car", E3DCChannelSpec.bit(RscpTag.EMS_BATTERY_TO_CAR_MODE, 1)), //
                entry("wallbox-charge-before-car", E3DCChannelSpec.bit(RscpTag.EMS_BATTERY_BEFORE_CAR_MODE, 1)), //
                entry("wallbox-discharge-until", E3DCChannelSpec.of(RscpTag.EMS_GET_WB_DISCHARGE_BAT_UNTIL)), //
                entry("wallbox-disable-at-mix-mode",
                        E3DCChannelSpec.bit(RscpTag.EMS_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT, 1))));

        registry.group(E3DCBindingConstants.GROUP_GRID,
                Map.ofEntries(entry("active-phases", E3DCChannelSpec.bitCount(RscpTag.EMS_BALANCED_PHASES))));

        registry.group(E3DCBindingConstants.GROUP_INVERTER, inverterChannels());

        registry.group(E3DCBindingConstants.GROUP_SYSTEM, Map.ofEntries(//
                entry("software-version", E3DCChannelSpec.of(RscpTag.INFO_SW_RELEASE)), //
                entry("serial-number", E3DCChannelSpec.of(RscpTag.INFO_SERIAL_NUMBER)), //
                entry("production-date", E3DCChannelSpec.of(RscpTag.INFO_PRODUCTION_DATE)), //
                entry("time-zone", E3DCChannelSpec.of(RscpTag.INFO_TIME_ZONE)), //
                entry("installed-peak-power", E3DCChannelSpec.of(RscpTag.EMS_INSTALLED_PEAK_POWER)), //
                entry("inverter-count", E3DCChannelSpec.of(RscpTag.PVI_INVERTER_COUNT)), //
                entry("battery-count", E3DCChannelSpec.of(RscpTag.BAT_AVAILABLE_BATTERIES)), //
                entry("mode", E3DCChannelSpec.of(RscpTag.EMS_MODE)), //
                entry("coupling-mode", E3DCChannelSpec.of(RscpTag.EMS_COUPLING_MODE)), //
                entry("emergency-power-status", E3DCChannelSpec.of(RscpTag.EMS_EMERGENCY_POWER_STATUS)), //
                entry("charging-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 1)), //
                entry("discharging-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 2)), //
                entry("emergency-power-available", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 4)), //
                entry("charging-throttled", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 8)), //
                entry("grid-in-limit", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 16)), //
                entry("charging-time-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 32)), //
                entry("discharging-time-lock", E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 64)), //
                entry("weather-regulated-charge",
                        E3DCChannelSpec.bitIn(RscpTag.EMS_GET_POWER_SETTINGS,
                                RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED, 1)), //
                entry("power-limits-used",
                        E3DCChannelSpec.bitIn(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_POWER_LIMITS_USED, 1)), //
                entry("powersave",
                        E3DCChannelSpec.bitIn(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_POWERSAVE_ENABLED, 1)), //
                entry("derate-at-percent", E3DCChannelSpec.of(RscpTag.EMS_DERATE_AT_PERCENT_VALUE)), //
                entry("derate-at-power", E3DCChannelSpec.of(RscpTag.EMS_DERATE_AT_POWER_VALUE)), //
                entry("idle-periods-enabled", E3DCChannelSpec.of(RscpTag.EMS_GET_IDLE_PERIODS_ENABLE)), //
                entry("idle-periods", E3DCChannelSpec.idlePeriods()), //
                entry("manual-charge-energy", E3DCChannelSpec.scaledIn(RscpTag.EMS_GET_MANUAL_CHARGE,
                        RscpTag.EMS_MANUAL_CHARGE_ENERGY_COUNTER, 1000))));

        registry.group(E3DCBindingConstants.GROUP_HISTORY, historyChannels());

        registry.group(E3DCBindingConstants.GROUP_RESERVE, Map.ofEntries(//
                entry("percent", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_EP_RESERVE)), //
                entry("energy", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_EP_RESERVE_W)), //
                entry("energy-max", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_EP_RESERVE_MAX_W)), //
                entry("last-soc", E3DCChannelSpec.in(RscpTag.SE_EP_RESERVE, RscpTag.SE_PARAM_LAST_SOC))));

        return registry;
    }

    /**
     * The energy counters of the history periods.
     *
     * <p>
     * A period is requested by its own container and reports the values in a sum container, which is why
     * the specs name two containers. The device reports the counters in Wh and the channels publish kWh,
     * so every one of them needs the same divisor.
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

    /** One value of a history period, wrapped in its sum container. */
    private static E3DCChannelSpec history(int periodContainer, int tag, double divisor) {
        return E3DCChannelSpec.scaledNested(periodContainer, RscpTag.DB_SUM_CONTAINER, tag, divisor);
    }

    /**
     * The wallbox channels.
     *
     * <p>
     * Per wallbox values live in the {@code WB_DATA} container. The values the wallbox accepts are read
     * directly rather than derived from a status bit: the charging mode, the suspended state and the
     * automatic phase switch are values of their own. Plugged, locked and charging are bits of one status
     * byte inside the {@code WB_EXTERN_DATA} byte array.
     */
    public static E3DCChannelRegistry wallbox() {
        return new E3DCChannelRegistry().group(E3DCBindingConstants.GROUP_WALLBOX, Map.ofEntries(//
                entry("power-l1", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_PM_POWER_L1)), //
                entry("power-l2", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_PM_POWER_L2)), //
                entry("power-l3", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_PM_POWER_L3)), //
                entry("energy-total", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_ENERGY_ALL, 1000)), //
                entry("phase-l1", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_PM_ACTIVE_PHASES, 1)), //
                entry("energy-l1", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_PM_ENERGY_L1, 1000)), //
                entry("phase-l2", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_PM_ACTIVE_PHASES, 2)), //
                entry("energy-l2", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_PM_ENERGY_L2, 1000)), //
                entry("phase-l3", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_PM_ACTIVE_PHASES, 4)), //
                entry("energy-l3", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_PM_ENERGY_L3, 1000)), //
                entry("energy-solar", E3DCChannelSpec.scaledIn(RscpTag.WB_DATA, RscpTag.WB_ENERGY_SOLAR, 1000)), //
                entry("soc", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_SOC)), //
                entry("min-charge-current", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_MIN_CHARGE_CURRENT)), //
                entry("max-charge-current", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_MAX_CHARGE_CURRENT)), //
                entry("number-phases", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_NUMBER_PHASES)), //
                entry("auto-phase-switch", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_AUTO_PHASE_SWITCH_ENABLED)), //
                entry("key-state", E3DCChannelSpec.bitIn(RscpTag.WB_DATA, RscpTag.WB_KEY_STATE, 1)), //
                entry("plugged", E3DCChannelSpec.bitAt(RscpTag.WB_EXTERN_DATA, 2, 8)), //
                entry("locked", E3DCChannelSpec.bitAt(RscpTag.WB_EXTERN_DATA, 2, 16)), //
                entry("charging", E3DCChannelSpec.bitAt(RscpTag.WB_EXTERN_DATA, 2, 32)), //
                entry("suspended", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_ABORT_CHARGING)), //
                entry("sun-mode", E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_SUN_MODE_ACTIVE))));
    }

    /** The power meter channels. */
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
     * The channels of the inverter.
     *
     * <p>
     * Every value the inverter reports arrives as a container of its own, which carries the value under
     * {@code PVI_VALUE} next to the inverter index. The direct current values are reported once per
     * photovoltaic tracker, so the first two trackers are exposed as their own channels; the device reports
     * its tracker count as {@code string-count}, which tells whether the second one is in use.
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
        for (int sensor = 0; sensor < 4; sensor++) {
            channels.put("temperature-" + (sensor + 1), E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA,
                    RscpTag.PVI_TEMPERATURE, RscpTag.PVI_VALUE, sensor));
        }
        // these two are answered as plain values rather than in a container of their own
        channels.put("on-grid", E3DCChannelSpec.in(RscpTag.PVI_DATA, RscpTag.PVI_ON_GRID));
        channels.put("string-count", E3DCChannelSpec.in(RscpTag.PVI_DATA, RscpTag.PVI_USED_STRING_COUNT));
        tracker(channels, "tracker-1", 0);
        tracker(channels, "tracker-2", 1);
        return channels;
    }

    /** One phase of a value the inverter reports once per phase, addressed by the position of its container. */
    private static E3DCChannelSpec phase(int tag, int phase, double divisor) {
        return new E3DCChannelSpec(RscpTag.PVI_VALUE, RscpTag.PVI_DATA, divisor, 0, -1, tag, phase, false, false);
    }

    /** One value the inverter reports as a parameter carrying its value in a container of its own. */
    private static E3DCChannelSpec parameter(int tag) {
        return E3DCChannelSpec.nested(RscpTag.PVI_DATA, tag, RscpTag.PVI_VALUE);
    }

    /**
     * The channels of one photovoltaic tracker, counted from 1. The trackers beyond the first two are not
     * part of the registry; they are created at runtime once the device has reported how many it has.
     */
    static Map<String, E3DCChannelSpec> trackerChannels(int number) {
        Map<String, E3DCChannelSpec> channels = new LinkedHashMap<>();
        tracker(channels, "tracker-" + number, number - 1);
        return channels;
    }

    /** The direct current values of one tracker, addressed by the position of the value's container. */
    private static void tracker(Map<String, E3DCChannelSpec> channels, String prefix, int index) {
        channels.put(prefix + "-power",
                E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA, RscpTag.PVI_DC_POWER, RscpTag.PVI_VALUE, index));
        channels.put(prefix + "-voltage",
                E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA, RscpTag.PVI_DC_VOLTAGE, RscpTag.PVI_VALUE, index));
        channels.put(prefix + "-current",
                E3DCChannelSpec.indexedNested(RscpTag.PVI_DATA, RscpTag.PVI_DC_CURRENT, RscpTag.PVI_VALUE, index));
    }

    /** The channels of one battery, which the device reports inside the battery container. */
    public static E3DCChannelRegistry battery() {
        Map<String, E3DCChannelSpec> channels = new LinkedHashMap<>();
        channels.put("voltage", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_MODULE_VOLTAGE));
        channels.put("current", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_CURRENT));
        channels.put("charge-cycles", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_CHARGE_CYCLES));
        channels.put("soh", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_ASOC));
        channels.put("status-code", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_STATUS_CODE));
        channels.put("error-code", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_ERROR_CODE));
        channels.put("device-name", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_DEVICE_NAME));
        channels.put("training-mode", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_TRAINING_MODE));
        channels.put("design-capacity", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_DESIGN_CAPACITY));
        channels.put("usable-capacity", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_USABLE_CAPACITY));
        channels.put("usable-remaining-capacity",
                E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_USABLE_REMAINING_CAPACITY));
        return new E3DCChannelRegistry().group(E3DCBindingConstants.GROUP_PACK, channels);
    }

    /**
     * The channels of one battery cell module.
     *
     * <p>
     * A module reports its values inside a {@code BAT_DCB_INFO} container, and the container identifies
     * itself with {@code BAT_DCB_INDEX}. Several modules arrive as several such containers, so every
     * channel names the same container and the handler picks the one belonging to the configured
     * module.
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
        tags.put("design-voltage", RscpTag.BAT_DCB_DESIGN_VOLTAGE);
        tags.put("max-charge-voltage", RscpTag.BAT_DCB_MAX_CHARGE_VOLTAGE);
        tags.put("max-charge-current", RscpTag.BAT_DCB_MAX_CHARGE_CURRENT);
        tags.put("max-discharge-current", RscpTag.BAT_DCB_MAX_DISCHARGE_CURRENT);
        tags.put("charge-high-temperature", RscpTag.BAT_DCB_CHARGE_HIGH_TEMPERATURE);
        tags.put("charge-low-temperature", RscpTag.BAT_DCB_CHARGE_LOW_TEMPERATURE);
        tags.put("cycles", RscpTag.BAT_DCB_CYCLE_COUNT);
        tags.put("status", RscpTag.BAT_DCB_STATUS);
        tags.put("warning", RscpTag.BAT_DCB_WARNING);
        tags.put("alarm", RscpTag.BAT_DCB_ALARM);
        tags.put("error", RscpTag.BAT_DCB_ERROR);
        tags.put("nr-sensor", RscpTag.BAT_DCB_NR_SENSOR);
        tags.put("nr-series-cell", RscpTag.BAT_DCB_NR_SERIES_CELL);
        tags.put("nr-parallel-cell", RscpTag.BAT_DCB_NR_PARALLEL_CELL);
        tags.put("firmware-version", RscpTag.BAT_DCB_FW_VERSION);
        tags.put("protocol-version", RscpTag.BAT_DCB_PROTOCOL_VERSION);
        tags.put("table-version", RscpTag.BAT_DCB_DATA_TABLE_VERSION);
        tags.put("pcb-version", RscpTag.BAT_DCB_PCB_VERSION);
        tags.put("device-name", RscpTag.BAT_DCB_DEVICE_NAME);
        tags.put("manufacturer", RscpTag.BAT_DCB_MANUFACTURE_NAME);
        tags.put("manufacture-date", RscpTag.BAT_DCB_MANUFACTURE_DATE);
        tags.put("serial-number", RscpTag.BAT_DCB_SERIALNO);
        tags.put("serial-code", RscpTag.BAT_DCB_SERIALCODE);

        Map<String, E3DCChannelSpec> channels = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> tag : tags.entrySet()) {
            channels.put(tag.getKey(), E3DCChannelSpec.in(RscpTag.BAT_DCB_INFO, tag.getValue()));
        }
        return new E3DCChannelRegistry().group(E3DCBindingConstants.GROUP_MODULE, channels);
    }
}

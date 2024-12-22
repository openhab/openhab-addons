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
package org.openhab.binding.growatt.internal;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link GrowattChannels} class defines the channel ids and respective UoM and scaling factors.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattChannels {

    /**
     * Class encapsulating units of measure and scale information.
     */
    public static class UoM {
        public final Unit<?> units;
        public final float divisor;

        public UoM(Unit<?> units, float divisor) {
            this.units = units;
            this.divisor = divisor;
        }
    }

    /**
     * Map of the channel ids to their respective UoM and scaling factors
     */
    private static final Map<String, UoM> CHANNEL_ID_UOM_MAP = Map.ofEntries(
            // inverter state
            new AbstractMap.SimpleEntry<String, UoM>("system-status", new UoM(Units.ONE, 1)),

            // solar generation
            new AbstractMap.SimpleEntry<String, UoM>("pv-power", new UoM(Units.WATT, 10)),

            // electric data for strings #1 and #2
            new AbstractMap.SimpleEntry<String, UoM>("pv1-voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-power", new UoM(Units.WATT, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("pv2-voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-power", new UoM(Units.WATT, 10)),

            // grid electric data (1-phase resp. 3-phase)
            new AbstractMap.SimpleEntry<String, UoM>("grid-frequency", new UoM(Units.HERTZ, 100)),

            new AbstractMap.SimpleEntry<String, UoM>("grid-voltage-r", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-voltage-s", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-voltage-t", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-voltage-rs", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-voltage-st", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-voltage-tr", new UoM(Units.VOLT, 10)),

            // inverter output
            new AbstractMap.SimpleEntry<String, UoM>("inverter-current-r", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-current-s", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-current-t", new UoM(Units.AMPERE, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("inverter-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-power-t", new UoM(Units.WATT, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("inverter-va", new UoM(Units.VOLT_AMPERE, 10)),

            // battery discharge / charge power
            new AbstractMap.SimpleEntry<String, UoM>("charge-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("charge-power", new UoM(Units.WATT, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("discharge-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("discharge-va", new UoM(Units.VOLT_AMPERE, 10)),

            // export power to grid
            new AbstractMap.SimpleEntry<String, UoM>("export-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("export-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("export-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("export-power-t", new UoM(Units.WATT, 10)),

            // power to user
            new AbstractMap.SimpleEntry<String, UoM>("import-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("import-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("import-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("import-power-t", new UoM(Units.WATT, 10)),

            // power to local
            new AbstractMap.SimpleEntry<String, UoM>("load-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("load-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("load-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("load-power-t", new UoM(Units.WATT, 10)),

            // inverter output energy
            new AbstractMap.SimpleEntry<String, UoM>("inverter-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // solar DC input energy
            new AbstractMap.SimpleEntry<String, UoM>("pv-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("pv-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy exported to grid
            new AbstractMap.SimpleEntry<String, UoM>("export-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("export-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy imported from grid
            new AbstractMap.SimpleEntry<String, UoM>("import-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("import-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy supplied to load
            new AbstractMap.SimpleEntry<String, UoM>("load-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("load-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy imported to charge
            new AbstractMap.SimpleEntry<String, UoM>("import-charge-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("import-charge-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // inverter energy to charge
            new AbstractMap.SimpleEntry<String, UoM>("inverter-charge-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-charge-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy supplied from discharge
            new AbstractMap.SimpleEntry<String, UoM>("discharge-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("discharge-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // inverter up time
            new AbstractMap.SimpleEntry<String, UoM>("total-work-time", new UoM(Units.HOUR, 7200)),

            // bus voltages
            new AbstractMap.SimpleEntry<String, UoM>("p-bus-voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("n-bus-voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("sp-bus-voltage", new UoM(Units.VOLT, 10)),

            // temperatures
            new AbstractMap.SimpleEntry<String, UoM>("pv-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv-ipm-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv-boost-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("temperature-4", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-temperature", new UoM(SIUnits.CELSIUS, 10)),

            // battery data
            new AbstractMap.SimpleEntry<String, UoM>("battery-type", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-display", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-soc", new UoM(Units.PERCENT, 1)),

            // fault codes
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-0", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-1", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-2", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-3", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-4", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-5", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-6", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("system-fault-7", new UoM(Units.ONE, 1)),

            // miscellaneous
            new AbstractMap.SimpleEntry<String, UoM>("system-work-mode", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("sp-display-status", new UoM(Units.ONE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("constant-power-ok", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("load-percent", new UoM(Units.PERCENT, 10)),

            // reactive 'power' resp. 'energy'
            new AbstractMap.SimpleEntry<String, UoM>("rac", new UoM(Units.VAR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("erac-today", new UoM(Units.KILOVAR_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("erac-total", new UoM(Units.KILOVAR_HOUR, 10)),

            /*
             * ============== CHANNELS ADDED IN PR #17795 ==============
             */

            // battery instantaneous measurements
            new AbstractMap.SimpleEntry<String, UoM>("battery-voltage2", new UoM(Units.VOLT, 100)),
            new AbstractMap.SimpleEntry<String, UoM>("charge-va", new UoM(Units.VOLT_AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-va", new UoM(Units.VOLT_AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-watt", new UoM(Units.WATT, 10)),

            // battery energy
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-energy-today",
                    new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-energy-total",
                    new UoM(Units.KILOWATT_HOUR, 10)),

            // inverter
            new AbstractMap.SimpleEntry<String, UoM>("inverter-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-fan-speed", new UoM(Units.PERCENT, 1)),

            /*
             * ============== CHANNELS ADDED IN PR #17810 ==============
             */

            // DC electric data for strings #3 and #4
            new AbstractMap.SimpleEntry<String, UoM>("pv3-voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv3-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv3-power", new UoM(Units.WATT, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("pv4-voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv4-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv4-power", new UoM(Units.WATT, 10)),

            // solar DC pv energy
            new AbstractMap.SimpleEntry<String, UoM>("pv3-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv3-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // power factor
            new AbstractMap.SimpleEntry<String, UoM>("power-factor", new UoM(Units.PERCENT, 10)),

            // emergency power supply (eps)
            new AbstractMap.SimpleEntry<String, UoM>("eps-voltage-r", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eps-voltage-s", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eps-voltage-t", new UoM(Units.VOLT, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("eps-current-r", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eps-current-s", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eps-current-t", new UoM(Units.AMPERE, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("eps-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eps-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eps-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eps-power-t", new UoM(Units.WATT, 10))

    //
    );

    public static Map<String, UoM> getMap() {
        return GrowattChannels.CHANNEL_ID_UOM_MAP;
    }

    /**
     * Map of Growatt JSON field names that are not used in the creation of device Channels.
     */
    @SuppressWarnings("unchecked")
    public static final Map<String, Set<String>> UNUSED_FIELDS = Stream.of(new Object[][] {
        // @formatter:off

            // simple inverter
            { "simple", Set.of(
                    "datalogserial",
                    "pvserial") },

            // sph inverter
            { "sph", Set.of(
                    "datalogserial",
                    "pvserial") },

            // spf inverter
            { "spf", Set.of(
                    "datalogserial",
                    "pvserial") },

            // mid inverter
            { "mid", Set.of(
                    "datalogserial",
                    "pvserial",
                    "deratingmode",
                    "iso",
                    "dcir",
                    "dcis",
                    "dcit",
                    "gfci",
                    "ipf",
                    "realoppercent",
                    "opfullwatt",
                    "standbyflag",
                    "warningcode",
                    "invstartdelaytime",
                    "bdconoffstate",
                    "drycontactstate",
                    "priority",
                    "epsfac",
                    "dcv",
                    "bdc1_sysstatemode",
                    "bdc1_faultcode",
                    "bdc1_warncode",
                    "bdc1_vbat",
                    "bdc1_ibat",
                    "bdc1_soc",
                    "bdc1_vbus1",
                    "bdc1_vbus2",
                    "bdc1_ibb",
                    "bdc1_illc",
                    "bdc1_tempb",
                    "bdc1_edischrtotal",
                    "bdc1_echrtotal",
                    "bdc1_flag",
                    "bdc2_sysstatemode",
                    "bdc2_faultcode",
                    "bdc2_warncode",
                    "bdc2_vbat",
                    "bdc2_ibat",
                    "bdc2_soc",
                    "bdc2_vbus1",
                    "bdc2_vbus2",
                    "bdc2_ibb",
                    "bdc2_illc",
                    "bdc2_tempa",
                    "bdc2_tempb",
                    "bdc2_pdischr",
                    "bdc2_pchr",
                    "bdc2_edischrtotal",
                    "bdc2_echrtotal",
                    "bdc2_flag",
                    "bms_status",
                    "bms_error",
                    "bms_warninfo",
                    "bms_batterycurr",
                    "bms_batterytemp",
                    "bms_maxcurr",
                    "bms_deltavolt",
                    "bms_cyclecnt",
                    "bms_soh",
                    "bms_constantvolt",
                    "bms_bms_info",
                    "bms_packinfo",
                    "bms_usingcap",
                    "bms_fw",
                    "bms_mcuversion",
                    "bms_commtype") },

            // smart meter
            { "meter", Set.of(
                    "datalogserial",
                    "pvserial",
                    "pvstatus",
                    "app_power_l1",
                    "app_power_l2",
                    "app_power_l3",
                    "react_power_l1",
                    "react_power_l2",
                    "react_power_l3",
                    "powerfactor_l1",
                    "powerfactor_l2",
                    "powerfactor_l3",
                    "pos_act_power",
                    "rev_act_power",
                    "app_power") },

        // @formatter:on
    }).collect(Collectors.toMap(objects -> (String) objects[0], objects -> (Set<String>) objects[1]));
}

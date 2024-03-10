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
            new AbstractMap.SimpleEntry<String, UoM>("erac-total", new UoM(Units.KILOVAR_HOUR, 10))
    //
    );

    public static Map<String, UoM> getMap() {
        return GrowattChannels.CHANNEL_ID_UOM_MAP;
    }
}

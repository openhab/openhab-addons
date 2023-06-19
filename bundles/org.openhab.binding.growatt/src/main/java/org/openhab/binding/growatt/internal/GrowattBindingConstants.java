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
package org.openhab.binding.growatt.internal;

import java.util.AbstractMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GrowattBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattBindingConstants {

    private static final String BINDING_ID = "growatt";

    /**
     * List of Thing Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "inverter");

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
     * Map of supported channel ids and respective UoM
     */
    public static final Map<String, UoM> CHANNEL_ID_UOM_MAP = Map.ofEntries(
            // inverter state
            new AbstractMap.SimpleEntry<String, UoM>("status", new UoM(Units.ONE, 1)),

            // solar generation
            new AbstractMap.SimpleEntry<String, UoM>("pv-power-in", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv-power-out", new UoM(Units.WATT, 10)),

            // electric data for strings #1 and #2
            new AbstractMap.SimpleEntry<String, UoM>("pv1-potential", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-power", new UoM(Units.WATT, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("pv2-potential", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-power", new UoM(Units.WATT, 10)),

            // grid electric data (1-phase resp. 3-phase)
            new AbstractMap.SimpleEntry<String, UoM>("grid-frequency", new UoM(Units.HERTZ, 100)),

            new AbstractMap.SimpleEntry<String, UoM>("grid-potential", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-potential-2", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-potential-3", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-potential-rs", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-potential-st", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-potential-tr", new UoM(Units.VOLT, 10)),

            // solar power to grid
            new AbstractMap.SimpleEntry<String, UoM>("grid-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-current-2", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-current-3", new UoM(Units.AMPERE, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("grid-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-power-2", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-power-3", new UoM(Units.WATT, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("grid-va", new UoM(Units.VOLT_AMPERE, 10)),

            // grid power to battery
            new AbstractMap.SimpleEntry<String, UoM>("grid-charge-current", new UoM(Units.VOLT_AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-charge-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-charge-va", new UoM(Units.VOLT_AMPERE, 10)),

            // grid power from battery
            new AbstractMap.SimpleEntry<String, UoM>("grid-discharge-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-discharge-va", new UoM(Units.VOLT_AMPERE, 10)),

            // battery discharge / charge power
            new AbstractMap.SimpleEntry<String, UoM>("battery-charge-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-va", new UoM(Units.VOLT_AMPERE, 10)),

            // power to grid
            new AbstractMap.SimpleEntry<String, UoM>("to-grid-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-grid-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-grid-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-grid-power-t", new UoM(Units.WATT, 10)),

            // power to user
            new AbstractMap.SimpleEntry<String, UoM>("to-user-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-user-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-user-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-user-power-t", new UoM(Units.WATT, 10)),

            // power to local
            new AbstractMap.SimpleEntry<String, UoM>("to-local-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-local-power-r", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-local-power-s", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-local-power-t", new UoM(Units.WATT, 10)),

            // pv energy
            new AbstractMap.SimpleEntry<String, UoM>("pv-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy taken from solar
            new AbstractMap.SimpleEntry<String, UoM>("pv-grid-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-grid-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-grid-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),

            new AbstractMap.SimpleEntry<String, UoM>("pv-grid-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1-grid-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-grid-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy supplied to grid
            new AbstractMap.SimpleEntry<String, UoM>("to-grid-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-grid-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy supplied to user
            new AbstractMap.SimpleEntry<String, UoM>("to-user-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-user-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy supplied to local
            new AbstractMap.SimpleEntry<String, UoM>("to-local-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("to-local-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy taken from grid to charge
            new AbstractMap.SimpleEntry<String, UoM>("grid-charge-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-charge-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy supplied to grid from discharge of battery
            new AbstractMap.SimpleEntry<String, UoM>("grid-discharge-energy-today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-discharge-energy-total", new UoM(Units.KILOWATT_HOUR, 10)),

            // energy taken from battery
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-energy-today",
                    new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-discharge-energy-total",
                    new UoM(Units.KILOWATT_HOUR, 10)),

            // inverter up time
            new AbstractMap.SimpleEntry<String, UoM>("total-work-time", new UoM(Units.HOUR, 7200)),

            // bus voltages
            new AbstractMap.SimpleEntry<String, UoM>("p-bus-potential", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("n-bus-potential", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("sp-bus-potential", new UoM(Units.VOLT, 10)),

            // temperatures
            new AbstractMap.SimpleEntry<String, UoM>("pv-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv-ipm-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv-boost-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("temperature-4", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2-temperature", new UoM(SIUnits.CELSIUS, 10)),

            // battery data
            new AbstractMap.SimpleEntry<String, UoM>("battery-type", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-potential", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-temperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-display", new UoM(Units.ONE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("battery-soc", new UoM(Units.ONE, 100)),

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

            // rac ??
            new AbstractMap.SimpleEntry<String, UoM>("rac", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("erac-today", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("erac-total", new UoM(Units.ONE, 1)),

            // duplicates ??
            new AbstractMap.SimpleEntry<String, UoM>("output-potential", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("output-frequency", new UoM(Units.HERTZ, 100)),
            new AbstractMap.SimpleEntry<String, UoM>("load-percent", new UoM(Units.ONE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("inverter-current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-input-power", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("grid-input-va", new UoM(Units.VOLT_AMPERE, 10))
    //
    );
}

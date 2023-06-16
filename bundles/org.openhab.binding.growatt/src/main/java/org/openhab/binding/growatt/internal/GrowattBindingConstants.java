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
            new AbstractMap.SimpleEntry<String, UoM>("pvstatus", new UoM(Units.ONE, 1)),
            new AbstractMap.SimpleEntry<String, UoM>("pvpowerin", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv1watt", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2voltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2current", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pv2watt", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvpowerout", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvfrequency", new UoM(Units.HERTZ, 100)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridvoltage", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridcurrent", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridpower", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridvoltage2", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridcurrent2", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridpower2", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridvoltage3", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridcurrent3", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvgridpower3", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvenergytoday", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvenergytotal", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("totworktime", new UoM(Units.SECOND, 2)),
            new AbstractMap.SimpleEntry<String, UoM>("epv1today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("epv1total", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("epv2today", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("epv2total", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("epvtotal", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvtemperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvipmtemperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pvboosttemperature", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("temp4", new UoM(SIUnits.CELSIUS, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("Vac_RS", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("Vac_ST", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("Vac_TR", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("uwBatVolt_DSP", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("pbusvolt", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("nbusvolt", new UoM(Units.VOLT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eacCharToday", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eacCharTotal", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("ebatDischarToday", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("ebatDischarTotal", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eacDischarToday", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("eacDischarTotal", new UoM(Units.KILOWATT_HOUR, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("ACCharCurr", new UoM(Units.AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("ACDischarWatt", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("ACDischarVA", new UoM(Units.VOLT_AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("BatDischarWatt", new UoM(Units.WATT, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("BatDischarVA", new UoM(Units.VOLT_AMPERE, 10)),
            new AbstractMap.SimpleEntry<String, UoM>("BatWatt", new UoM(Units.WATT, 10))
    //
    );
}

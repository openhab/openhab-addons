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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.util.List;

/**
 * The {@link ExtendedRuntimeDTO} contains the last three 5 minute interval values
 * sent by the thermostat for the past 15 minutes of runtime. The interval values
 * are valuable when you are interested in analyzing the runtime data in a more
 * granular fashion, at 5 minute increments rather than the more general 15 minute
 * value from the Runtime Object. For the runtime values (i.e. heatPump, auxHeat,
 * cool, etc.) refer to the Thermostat.Settings values (hasHeatPump, heatStages,
 * coolStages) to determine whether a heat pump exists and how many stages the
 * thermostat supports. The actual temperature and humidity will also be updated
 * when the equipment state changes by the thermostat, this may occur at a frequency
 * of 3 minutes, however it is only transmitted when there is an equipment state
 * change on the thermostat.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ExtendedRuntimeDTO {

    /*
     * The UTC timestamp of the last value read. This timestamp is updated at a 15 min
     * interval by the thermostat. For the 1st value, it is timestamp - 10 mins, for
     * the 2nd value it is timestamp - 5 mins. Consider day boundaries being straddled
     * when using these values.
     */
    public String lastReadingTimestamp;

    /*
     * The UTC date of the last runtime reading. Format: YYYY-MM-DD
     */
    public String runtimeDate;

    /*
     * The last 5 minute interval which was updated by the thermostat telemetry update.
     * Subtract 2 from this interval to obtain the beginning interval for the last 3
     * readings. Multiply by 5 mins to obtain the minutes of the day. Range: 0-287
     */
    public Integer runtimeInterval;

    /*
     * The last three 5 minute actual temperature readings
     */
    public List<Integer> actualTemperature;

    /*
     * The last three 5 minute actual humidity readings.
     */
    public List<Integer> actualHumidity;

    /*
     * The last three 5 minute desired heat temperature readings.
     */
    public List<Integer> desiredHeat;

    /*
     * The last three 5 minute desired cool temperature readings.
     */
    public List<Integer> desiredCool;

    /*
     * The last three 5 minute desired humidity readings.
     */
    public List<Integer> desiredHumidity;

    /*
     * The last three 5 minute desired de-humidification readings.
     */
    public List<Integer> desiredDehumidity;

    /*
     * The last three 5 minute desired Demand Management temeprature offsets.
     * This value is Demand Management adjustment value which was applied by
     * the thermostat. If the thermostat decided not to honour the adjustment,
     * it will send 0 for the interval. Compare these values with the values
     * sent in the DM message to determine whether the thermostat applied
     * the adjustment.
     */
    public List<Integer> dmOffset;

    /*
     * The last three 5 minute HVAC Mode reading. These values indicate which
     * stage was energized in the 5 minute interval. Values: heatStage10n,
     * heatStage20n, heatStage30n, heatOff, compressorCoolStage10n,
     * compressorCoolStage20n, compressorCoolOff, compressorHeatStage10n,
     * compressorHeatStage20n, compressorHeatOff, economyCycle.
     */
    public List<Integer> hvacMode;

    /*
     * The last three 5 minute HVAC Runtime values in seconds (0-300 seconds)
     * per interval. This value corresponds to the heat pump stage 1 runtime.
     */
    public List<Integer> heatPump1;

    /*
     * The last three 5 minute HVAC Runtime values in seconds (0-300 seconds)
     * per interval. This value corresponds to the heat pump stage 2 runtime.
     */
    public List<Integer> heatPump2;

    /*
     * The last three 5 minute HVAC Runtime values in seconds (0-300 seconds)
     * per interval. This value corresponds to the auxiliary heat stage 1. If
     * the thermostat does not have a heat pump, this is heat stage 1.
     */
    public List<Integer> auxHeat1;

    /*
     * The last three 5 minute HVAC Runtime values in seconds (0-300 seconds)
     * per interval. This value corresponds to the auxiliary heat stage 2. If
     * the thermostat does not have a heat pump, this is heat stage 2.
     */
    public List<Integer> auxHeat2;

    /*
     * The last three 5 minute HVAC Runtime values in seconds (0-300 seconds)
     * per interval. This value corresponds to the heat stage 3 if the thermostat
     * does not have a heat pump. Auxiliary stage 3 is not supported.
     */
    public List<Integer> auxHeat3;

    /*
     * The last three 5 minute HVAC Runtime values in seconds (0-300 seconds)
     * per interval. This value corresponds to the cooling stage 1.
     */
    public List<Integer> cool1;

    /*
     * The last three 5 minute HVAC Runtime values in seconds (0-300 seconds)
     * per interval. This value corresponds to the cooling stage 2.
     */
    public List<Integer> cool2;

    /*
     * The last three 5 minute fan Runtime values in seconds (0-300 seconds) per interval.
     */
    public List<Integer> fan;

    /*
     * The last three 5 minute humidifier Runtime values in seconds (0-300 seconds) per interval.
     */
    public List<Integer> humidifier;

    /*
     * The last three 5 minute de-humidifier Runtime values in seconds (0-300 seconds) per interval.
     */
    public List<Integer> dehumidifier;

    /*
     * The last three 5 minute economizer Runtime values in seconds (0-300 seconds) per interval.
     */
    public List<Integer> economizer;

    /*
     * The last three 5 minute ventilator Runtime values in seconds (0-300 seconds) per interval.
     */
    public List<Integer> ventilator;

    /*
     * The latest value of the current electricity bill as interpolated from the
     * thermostat's readings from a paired electricity meter.
     */
    public List<Integer> currentElectricityBill;

    /*
     * The latest estimate of the projected electricity bill as interpolated from the
     * thermostat's readings from a paired electricity meter.
     */
    public List<Integer> projectedElectricityBill;
}

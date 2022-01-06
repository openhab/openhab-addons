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

import java.util.Date;
import java.util.List;

/**
 * The {@link RuntimeDTO} represents the last known thermostat running state. This state
 * is composed from the last interval status message received from a thermostat. It is
 * also updated each time the thermostat posts configuration changes to the server.
 * The runtime object contains the last 5 minute interval value sent by the thermostat
 * for the past 15 minutes of runtime. The thermostat updates the server every 15 minutes
 * with the last three 5 minute readings. The actual temperature and humidity will also
 * be updated when the equipment state changes by the thermostat, this may occur at a
 * frequency of 3 minutes, however it is only transmitted when there is an equipment
 * state change on the thermostat. The runtime object contains two fields, desiredHeatRange
 * and desiredCoolRange, which can be queried and used to determine that any holds being
 * set through the API will not be adjusted. The API caller should check these ranges
 * before calling the setHold function to mitigate against the new set points being
 * adjusted by the server if the values are outside the acceptable ranges.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class RuntimeDTO {
    /*
     * The current runtime revision. Equivalent in meaning to the runtime
     * revision number in the thermostat summary call.
     */
    public String runtimeRev;

    /*
     * Whether the thermostat is currently connected to the server.
     */
    public Boolean connected;

    /*
     * The UTC date/time stamp of when the thermostat first connected
     * to the ecobee server.
     */
    public Date firstConnected;

    /*
     * The last recorded connection date and time.
     */
    public Date connectDateTime;

    /*
     * The last recorded disconnection date and time.
     */
    public Date disconnectDateTime;

    /*
     * The UTC date/time stamp of when the thermostat was updated.
     * Format: YYYY-MM-DD HH:MM:SS
     */
    public Date lastModified;

    /*
     * The UTC date/time stamp of when the thermostat last posted its
     * runtime information. Format: YYYY-MM-DD HH:MM:SS
     */
    public Date lastStatusModified;

    /*
     * The UTC date of the last runtime reading. Format: YYYY-MM-DD
     */
    public String runtimeDate;

    /*
     * The last 5 minute interval which was updated by the thermostat
     * telemetry update. Subtract 2 from this interval to obtain the
     * beginning interval for the last 3 readings. Multiply by 5 mins
     * to obtain the minutes of the day. Range: 0-287
     */
    public Integer runtimeInterval;

    /*
     * The current temperature displayed on the thermostat.
     */
    public Integer actualTemperature;

    /*
     * The current humidity % shown on the thermostat.
     */
    public Integer actualHumidity;

    /*
     * The dry-bulb temperature recorded by the thermostat. When
     * Energy.FeelsLikeMode is set to humidex, Runtime.actualTemperature
     * will report a "feels like" temperature.
     */
    public Integer rawTemperature;

    /*
     * The currently displayed icon on the thermostat.
     */
    public Integer showIconMode;

    /*
     * The desired heat temperature as per the current running
     * program or active event.
     */
    public Integer desiredHeat;

    /*
     * The desired cool temperature as per the current running
     * program or active event.
     */
    public Integer desiredCool;

    /*
     * The desired humidity set point.
     */
    public Integer desiredHumidity;

    /*
     * The desired dehumidification set point.
     */
    public Integer desiredDehumidity;

    /*
     * The desired fan mode. Values: auto, on or null if the HVAC
     * system is off and the thermostat is not controlling a fan independently.
     */
    public String desiredFanMode;

    /*
     * This field provides the possible valid range for which a desiredHeat
     * setpoint can be set to. This value takes into account the thermostat
     * heat temperature limits as well the running program or active events.
     * Values are returned as an Integer array representing the canonical
     * minimum and maximim, e.g. [450,790].
     */
    public List<Integer> desiredHeatRange;

    /*
     * This field provides the possible valid range for which a desiredCool
     * setpoint can be set to. This value takes into account the thermostat
     * cool temperature limits as well the running program or active events.
     * Values are returned as an Integer array representing the canonical
     * minimum and maximim, e.g. [650,920].
     */
    public List<Integer> desiredCoolRange;
}

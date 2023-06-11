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
package org.openhab.binding.ecobee.internal.dto.thermostat;

/**
 * The {@link EventDTO} represents a scheduled thermostat
 * program change. All events have a start and end time during which the
 * thermostat runtime settings will be modified. Events may not be directly
 * modified, various Functions provide the capability to modify the calendar
 * events and to modify the program. The event list is sorted with events
 * ordered by whether they are currently running and the internal priority
 * of each event. It is safe to take the first event which is running and
 * show it as the currently running event. When the resume function is used,
 * events are removed in the order they are listed here.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class EventDTO {
    /*
     * The type of event. Values: hold, demandResponse, sensor, switchOccupancy,
     * vacation, quickSave, today, autoAway, autoHome
     */
    public String type;

    /*
     * The unique event name.
     */
    public String name;

    /*
     * Whether the event is currently active or not.
     */
    public Boolean running;

    /*
     * The event start date in thermostat local time.
     */
    public String startDate;

    /*
     * The event start time in thermostat local time.
     */
    public String startTime;

    /*
     * The event end date in thermostat local time.
     */
    public String endDate;

    /*
     * The event end time in thermostat local time.
     */
    public String endTime;

    /*
     * Whether there are persons occupying the property during the event.
     */
    public Boolean isOccupied;

    /*
     * Whether cooling will be turned off during the event.
     */
    public Boolean isCoolOff;

    /*
     * Whether heating will be turned off during the event.
     */
    public Boolean isHeatOff;

    /*
     * The cooling absolute temperature to set.
     */
    public Integer coolHoldTemp;

    /*
     * The heating absolute temperature to set.
     */
    public Integer heatHoldTemp;

    /*
     * The fan mode during the event. Values: auto, on Default: based on current climate and hvac mode.
     */
    public String fan;

    /*
     * The ventilator mode during the vent. Values: auto, minontime, on, off.
     */
    public String vent;

    /*
     * The minimum amount of time the ventilator equipment must stay on on each duty cycle.
     */
    public Integer ventilatorMinOnTime;

    /*
     * Whether this event is mandatory or the end user can cancel it.
     */
    public Boolean isOptional;

    /*
     * Whether the event is using a relative temperature setting to the currently
     * active program climate. See the Note at the bottom of this page for more information.
     */
    public Boolean isTemperatureRelative;

    /*
     * The relative cool temperature adjustment.
     */
    public Integer coolRelativeTemp;

    /*
     * The relative heat temperature adjustment.
     */
    public Integer heatRelativeTemp;

    /*
     * Whether the event uses absolute temperatures to set the values. Default:
     * true for DRs. See the Note at the bottom of this page for more information.
     */
    public Boolean isTemperatureAbsolute;

    /*
     * Indicates the % scheduled runtime during a Demand Response event. Valid range
     * is 0 - 100%. Default = 100, indicates no change to schedule.
     */
    public Integer dutyCyclePercentage;

    /*
     * The minimum number of minutes to run the fan each hour. Range: 0-60, Default: 0
     */
    public Integer fanMinOnTime;

    /*
     * True if this calendar event was created because of the occupied sensor.
     */
    public Boolean occupiedSensorActive;

    /*
     * True if this calendar event was created because of the occupied sensor.
     */
    public Boolean unoccupiedSensorActive;

    /*
     * Unsupported. Future feature.
     */
    public Integer drRampUpTemp;

    /*
     * Unsupported. Future feature.
     */
    public Integer drRampUpTime;

    /*
     * Unique identifier set by the server to link one or more events and alerts together.
     */
    public String linkRef;

    /*
     * Used for display purposes to indicate what climate (if any) is being used for the hold.
     */
    public String holdClimateRef;
}

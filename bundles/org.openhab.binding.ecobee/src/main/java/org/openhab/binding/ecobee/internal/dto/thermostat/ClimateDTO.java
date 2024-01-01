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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.util.List;

/**
 * The {@link ClimateDTO} maps to the thermostat's Climate object.
 *
 * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/objects/Climate.shtml">Climate</a>
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ClimateDTO {

    /*
     * The unique climate name. The name may be changed without affecting the
     * program integrity so long as uniqueness is maintained.
     */
    public String name;

    /*
     * The unique climate identifier. Changing the identifier is not possible
     * and it is generated on the server for each climate. If this value is
     * not supplied a new climate will be created. For the default climates
     * and existing user created climates the climateRef should be
     * supplied - see note above.
     */
    public String climateRef;

    /*
     * A flag indicating whether the property is occupied by persons during this climate
     */
    public Boolean isOccupied;

    /*
     * A flag indicating whether ecobee optimized climate settings are used by this climate.
     */
    public Boolean isOptimized;

    /*
     * The cooling fan mode. Default: on. Values: auto, on.
     */
    public String coolFan;

    /*
     * The heating fan mode. Default: on. Values: auto, on.
     */
    public String heatFan;

    /*
     * The ventilator mode. Default: off. Values: auto, minontime, on, off.
     */
    public String vent;

    /*
     * The minimum time, in minutes, to run the ventilator each hour.
     */
    public Integer ventilatorMinOnTime;

    /*
     * The climate owner. Default: system. Values: adHoc, demandResponse, quickSave,
     * sensorAction, switchOccupancy, system, template, user.
     */
    public String owner;

    /*
     * The type of climate. Default: program. Values: calendarEvent, program.
     */
    public String type;

    /*
     * The integer conversion of the HEX color value used to display this
     * climate on the thermostat and on the web portal.
     */
    public Integer colour;

    /*
     * The cool temperature for this climate.
     */
    public Integer coolTemp;

    /*
     * The heat temperature for this climate.
     */
    public Integer heatTemp;

    /*
     * The list of sensors in use for the specific climate. The sensors listed here
     * are used for temperature averaging within that climate. Only the sensorId
     * and name are listed in the climate.
     */
    public List<RemoteSensorDTO> sensors;
}

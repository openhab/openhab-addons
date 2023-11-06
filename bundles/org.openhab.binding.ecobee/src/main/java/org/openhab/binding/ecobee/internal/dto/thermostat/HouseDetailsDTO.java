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
 * The {@link HouseDetailsDTO} contains contains the information about the
 * house the thermostat is installed in.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class HouseDetailsDTO {
    /*
     * The style of house. Values: other, apartment, condominium, detached,
     * loft, multiPlex, rowHouse, semiDetached, townhouse, and 0 for unknown.
     */
    public String style;

    /*
     * The size of the house in square feet.
     */
    public Integer size;

    /*
     * The number of floors or levels in the house.
     */
    public Integer numberOfFloors;

    /*
     * The number of rooms in the house.
     */
    public Integer numberOfRooms;

    /*
     * The number of occupants living in the house.
     */
    public Integer numberOfOccupants;

    /*
     * The age of house in years.
     */
    public Integer age;

    /*
     * This field defines the window efficiency of the house. Valid values
     * are in the range 1 - 7. Changing the value of this field alters the
     * settings the thermostat uses for the humidifier when in 'frost Control'
     * mode. See the NOTE above before updating this value.
     */
    public Integer windowEfficiency;
}

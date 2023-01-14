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
 * The {@link LocationDTO} describes the physical location and coordinates of the
 * thermostat as entered by the thermostat owner. The address information is used
 * in a geocode look up to obtain the thermostat coordinates. The coordinates
 * are used to obtain accurate weather information.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class LocationDTO {
    /*
     * The timezone offset in minutes from UTC.
     */
    public Integer timeZoneOffsetMinutes;

    /*
     * The Olson timezone the thermostat resides in (e.g America/Toronto).
     */
    public String timeZone;

    /*
     * Whether the thermostat should factor in daylight savings when displaying the date and time.
     */
    public Boolean isDaylightSaving;

    /*
     * The thermostat location street address.
     */
    public String streetAddress;

    /*
     * The thermostat location city.
     */
    public String city;

    /*
     * The thermostat location State or Province
     */
    public String provinceState;

    /*
     * The thermostat location country.
     */
    public String country;

    /*
     * The thermostat location ZIP or Postal code.
     */
    public String postalCode;

    /*
     * The thermostat owner's phone number.
     */
    public String phoneNumber;

    /*
     * The lat/long geographic coordinates of the thermostat location.
     */
    public String mapCoordinates;
}

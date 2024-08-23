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

/**
 * The {@link RemoteSensorCapabilityDTO}represents the specific capability of a
 * sensor connected to the thermostat. For the occupancy type capability the
 * data will only show computed occupancy, as does the thermostat.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class RemoteSensorCapabilityDTO {
    /*
     * The unique sensor capability identifier. For example: 1
     */
    public String id;

    /*
     * The type of sensor capability. Values: adc, co2, dryContact,
     * humidity, temperature, occupancy, unknown.
     */
    public String type;

    /*
     * The data value for this capability, always a String. Temperature
     * values are expressed as degrees Fahrenheit, multiplied by 10. For
     * example, a temperature of 72F would be returned as the value "720".
     * Occupancy values are "true" or "false". Humidity is expressed as
     * a % value such as "45". Unknown values are returned as "unknown".
     */
    public String value;
}

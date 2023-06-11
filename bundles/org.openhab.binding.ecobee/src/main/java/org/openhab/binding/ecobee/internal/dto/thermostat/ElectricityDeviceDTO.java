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
 * The {@link ElectricityDeviceDTO} represents an energy recording device. At this time,
 * only meters are supported by the API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ElectricityDeviceDTO {

    /*
     * The name of the device.
     */
    public String name;

    /*
     * The list of Electricity Tiers containing the break down of daily electricity
     * consumption of the device for the day, broken down per pricing tier.
     */
    public List<ElectricityTierDTO> tiers;

    /*
     * The last date/time the reading was updated in UTC time.
     */
    public String lastUpdate;

    /*
     * The last three daily electricity cost reads from the device in cents with a
     * three decimal place precision.
     */
    public List<String> cost;

    /*
     * The last three daily electricity consumption reads from the device in KWh
     * with a three decimal place precision.
     */
    public List<String> consumption;
}

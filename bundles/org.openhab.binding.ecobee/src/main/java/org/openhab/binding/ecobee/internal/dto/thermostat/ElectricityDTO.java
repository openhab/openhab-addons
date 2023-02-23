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

import java.util.List;

/**
 * The {@link ElectricityDTO} contains the last collected electricity usage
 * measurements for the thermostat. An electricity object is composed of
 * Electricity Devices, each of which contains readings from an Electricity Tier.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ElectricityDTO {

    /*
     * The list of ElectricityDevice objects associated with the thermostat, each representing
     * a device such as an electric meter or remote load control.
     */
    public List<ElectricityDeviceDTO> electricityDevices;
}

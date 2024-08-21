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

import org.openhab.binding.ecobee.internal.dto.AbstractResponseDTO;

/**
 * The {@link ThermostatResponseDTO} contains the list of thermostats in response to a
 * thermostat request.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ThermostatResponseDTO extends AbstractResponseDTO {

    /*
     * List of thermostats matching the selection criteria in the thermostat request.
     */
    public List<ThermostatDTO> thermostatList;
}

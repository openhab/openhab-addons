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

import org.openhab.binding.ecobee.internal.dto.SelectionDTO;

/**
 * The {@link ThermostatRequestDTO} contains the information needed to make a thermostat request.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ThermostatRequestDTO {

    public ThermostatRequestDTO(SelectionDTO selection) {
        this.selection = selection;
    }

    /*
     * Specifies which thermostats will be returned in the response.
     */
    public SelectionDTO selection;
}

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

import org.openhab.binding.ecobee.internal.dto.SelectionDTO;

/**
 * The {@link ThermostatUpdateRequestDTO} contains the information needed to make a
 * request to update a thermostat.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ThermostatUpdateRequestDTO {

    public ThermostatUpdateRequestDTO(SelectionDTO selection) {
        this.selection = selection;
    }

    /*
     * Thermostat object containing changes.
     */
    public ThermostatDTO thermostat;

    /*
     * Specifies the thermostat to be updated.
     */
    public SelectionDTO selection;
}

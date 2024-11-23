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
package org.openhab.binding.ojelectronics.internal.models.thermostat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ojelectronics.internal.models.RequestModelBase;

/**
 * Model for updating a thermostat
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class UpdateThermostatRequestModel extends RequestModelBase {

    public UpdateThermostatRequestModel(ThermostatModel thermostat) {
        setThermostat = thermostat;
        thermostatID = thermostat.serialNumber;
    }

    public ThermostatModel setThermostat;

    public String thermostatID;
}

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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.ThermostatZoneType;

/**
 * The {@link Zone} holds temperature data for a given zone.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class Zone extends NAObject {
    private ThermostatZoneType type = ThermostatZoneType.UNKNOWN;
    private double temp;

    public double getTemp() {
        return temp;
    }

    public ThermostatZoneType getType() {
        return type;
    }
}

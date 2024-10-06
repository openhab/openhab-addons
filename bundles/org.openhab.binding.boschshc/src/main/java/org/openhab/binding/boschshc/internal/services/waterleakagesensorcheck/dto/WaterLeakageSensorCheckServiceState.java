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
package org.openhab.binding.boschshc.internal.services.waterleakagesensorcheck.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * DTO for the check state of water leakage sensors.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class WaterLeakageSensorCheckServiceState extends BoschSHCServiceState {

    public WaterLeakageSensorCheckServiceState() {
        super("waterLeakageSensorCheckState");
    }

    public String result;
}

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
package org.openhab.binding.boschshc.internal.services.waterleakagesensortilt.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.dto.EnabledDisabledState;

/**
 * Service for notifications relating to the water detection sensor.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class WaterLeakageSensorTiltServiceState extends BoschSHCServiceState {

    public WaterLeakageSensorTiltServiceState() {
        super("waterLeakageSensorTiltState");
    }

    public EnabledDisabledState pushNotificationState;

    public EnabledDisabledState acousticSignalState;
}

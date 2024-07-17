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
package org.openhab.binding.boschshc.internal.services.waterleakagesensor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.waterleakagesensor.dto.WaterLeakageSensorServiceState;

/**
 * Service for the water leakage sensor.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class WaterLeakageSensorService extends BoschSHCService<WaterLeakageSensorServiceState> {

    public WaterLeakageSensorService() {
        super("WaterLeakageSensor", WaterLeakageSensorServiceState.class);
    }
}

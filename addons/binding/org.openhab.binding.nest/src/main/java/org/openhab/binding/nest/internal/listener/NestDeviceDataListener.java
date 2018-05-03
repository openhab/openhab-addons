/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;

/**
 * Used to track incoming data for devices
 *
 * @author David Bennett - Initial Contribution
 * @author Martin van Wingerden - Separated listeners to also use them for the handlers
 */
@NonNullByDefault
public interface NestDeviceDataListener {

    /**
     * Called when a thermostat is discovered.
     */
    void onNewNestThermostatData(Thermostat thermostat);

    /**
     * Called when a camera is discovered.
     */
    void onNewNestCameraData(Camera camera);

    /**
     * Called when a smoke detector is discovered.
     */
    void onNewNestSmokeDetectorData(SmokeDetector smokeDetector);

    /**
     * Called when a structure is discovered.
     */
    void onNewNestStructureData(Structure struct);
}

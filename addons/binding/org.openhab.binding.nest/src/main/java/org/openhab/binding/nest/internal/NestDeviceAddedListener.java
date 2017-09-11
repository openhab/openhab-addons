/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;

/**
 * Used by the discovery service to track when devices are added.
 *
 * @author David Bennett - Initial Contribution
 */
public interface NestDeviceAddedListener {

    /**
     * Called when a thermostat is discovered.
     */
    public void onThermostatAdded(Thermostat thermostat);

    /**
     * Called when a camera is discovered.
     */
    public void onCameraAdded(Camera thermostat);

    /**
     * Called when a smoke detector is discovered.
     */
    public void onSmokeDetectorAdded(SmokeDetector thermostat);

    /**
     * Called when a structure is discovered.
     */
    public void onStructureAdded(Structure struct);
}

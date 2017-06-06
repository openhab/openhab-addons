package org.openhab.binding.nest.internal;

import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;

/**
 * Used by the discovery service to track when devices are added.
 *
 * @author David Bennett
 */
public interface NestDeviceAddedListener {

    public void onThermostatAdded(Thermostat thermostat);

    public void onCameraAdded(Camera thermostat);

    public void onSmokeDetectorAdded(SmokeDetector thermostat);

    public void onStructureAdded(Structure struct);
}

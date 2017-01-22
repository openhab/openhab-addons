package org.openhab.binding.dsmr.device;

import org.openhab.binding.dsmr.device.DSMRDeviceConstants.DeviceState;
import org.openhab.binding.dsmr.device.DSMRDeviceConstants.DeviceStateDetail;

/**
 * This interface listens for change in the DSMR Device state
 *
 * @author Marcel Volaart
 * @since 2.0.0
 */
public interface DSMRDeviceStateListener {
    /**
     * This method is called when the DSMR Device updates it state (state can be the same)
     *
     * @param oldState {@link DSMRDeviceConstants.DeviceState} representing the old state
     * @param newState {@link DSMRDeviceConstants.DeviceState} representing the new state
     * @param stateDetails {@link DSMRDeviceConstants.DeviceStateDetail} containing details about the updated state
     */
    public void stateUpdated(DeviceState oldState, DeviceState newState, DeviceStateDetail stateDetails);

    /**
     * This method is called when the DSMR Device changes it state (state won't be the same)
     *
     * @param oldState {@link DSMRDeviceConstants.DeviceState} representing the old state
     * @param newState {@link DSMRDeviceConstants.DeviceState} representing the new state
     * @param stateDetails {@link DSMRDeviceConstants.DeviceStateDetail} containing details about the new state
     */
    public void stateChanged(DeviceState oldState, DeviceState newState, DeviceStateDetail stateDetails);
}

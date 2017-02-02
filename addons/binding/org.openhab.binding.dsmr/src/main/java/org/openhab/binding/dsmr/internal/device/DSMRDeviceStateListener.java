/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import org.openhab.binding.dsmr.internal.device.DSMRDeviceConstants.DeviceState;

/**
 * This interface listens for change in the DSMR Device state
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public interface DSMRDeviceStateListener {
    /**
     * This method is called when the DSMR Device updates it state (state can be the same)
     *
     * @param oldState {@link DSMRDeviceConstants.DeviceState} representing the old state
     * @param newState {@link DSMRDeviceConstants.DeviceState} representing the new state
     * @param stateDetails String containing details about the updated state
     */
    public void stateUpdated(DeviceState oldState, DeviceState newState, String stateDetails);

    /**
     * This method is called when the DSMR Device changes it state (state won't be the same)
     *
     * @param oldState {@link DSMRDeviceConstants.DeviceState} representing the old state
     * @param newState {@link DSMRDeviceConstants.DeviceState} representing the new state
     * @param stateDetails String containing details about the new state
     */
    public void stateChanged(DeviceState oldState, DeviceState newState, String stateDetails);
}

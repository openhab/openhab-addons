/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.client;

import java.util.List;
import java.util.Map;

/**
 * Provides an interface to the wink api. Currently cloud based, but future implementations may
 * be local.
 *
 * @author scrosby
 *
 */
public interface IWinkClient {
    /**
     * Get a list of all devices connected to the wink hub
     *
     * @return List<IWinkDevice> unordered list of devices connected to this hub
     */
    public List<IWinkDevice> listDevices();

    /**
     * Retrieves a specific device identified by the device uid
     *
     * @param type Supported Wink Device
     * @param Id UID of the device to retrieve
     * @return IWinkDevice object representing the device specified
     */
    public IWinkDevice getDevice(WinkSupportedDevice type, String Id);

    /**
     * Updates the state of a specified device.
     *
     * @param device Current device object
     * @param updatedState A Map of states as strings to be updated and their new values
     * @return IWinkDevice the updated result of the change.
     */
    public IWinkDevice updateDeviceState(IWinkDevice device, Map<String, String> updatedState);
}

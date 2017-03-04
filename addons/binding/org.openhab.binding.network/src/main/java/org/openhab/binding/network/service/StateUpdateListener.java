/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

/**
 * Implement this callback to be notified of device state updates.
 *
 * @author Marc Mettke - Initial contribution
 */
public interface StateUpdateListener {
    /**
     * This method is called by the {@see NetworkService} if a new device state is known.
     *
     * @param pingTimeInMS A positive greater than 0 ping time in ms.
     *            -1 if the device is not reachable and 0 if the device is reachable but no time information is
     *            available.
     */
    public void updatedDeviceState(double pingTimeInMS);

    /**
     * This method is called by the {@see NetworkService} if the given configuration is invalid.
     */
    public void invalidConfig();
}

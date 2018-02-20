/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator;

import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;

/**
 * Adapter with methods called from events within the {@link HomematicGateway} class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface HomematicGatewayAdapter {

    /**
     * Called when a datapoint has been updated.
     */
    public void onStateUpdated(HmDatapoint dp);

    /**
     * Called when a new device has been detected on the gateway.
     */
    public void onNewDevice(HmDevice device);

    /**
     * Called when a device has been deleted from the gateway.
     */
    public void onDeviceDeleted(HmDevice device);

    /**
     * Called when the devices values should be reloaded from the gateway.
     */
    public void reloadDeviceValues(HmDevice device);

    /**
     * Called when all values for all devices should be reloaded from the gateway.
     */
    public void reloadAllDeviceValues();

    /**
     * Called when a device has been loaded from the gateway.
     */
    public void onDeviceLoaded(HmDevice device);

    /**
     * Called when the connection is lost to the gateway.
     */
    public void onConnectionLost();

    /**
     * Called when the connection is resumed to the gateway.
     */
    public void onConnectionResumed();

    /**
     * Returns the configuration of a datapoint.
     */
    public HmDatapointConfig getDatapointConfig(HmDatapoint dp);

}

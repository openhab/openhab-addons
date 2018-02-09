/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.tellstick.device.TellstickException;
import org.tellstick.device.iface.Device;

/**
 * Interface for telldus controllers. This is used to send and get status of devices from the controller.
 *
 * @author Jarle Hjortland
 *
 */
public interface TelldusDeviceController {

    /**
     * Send an event to the controller.
     *
     * @param device
     * @param resendCount
     * @param isdimmer
     * @param command
     * @throws TellstickException
     */
    void handleSendEvent(Device device, int resendCount, boolean isdimmer, Command command) throws TellstickException;

    /**
     * Get the state of the given device.
     *
     * @param dev
     * @return
     */
    State calcState(Device dev);

    /**
     * Get the current dim state for a device.
     *
     * @param device
     * @return
     */
    BigDecimal calcDimValue(Device device);

    /**
     *
     * Clean up the device controller before shutdown.
     *
     */
    void dispose();

}
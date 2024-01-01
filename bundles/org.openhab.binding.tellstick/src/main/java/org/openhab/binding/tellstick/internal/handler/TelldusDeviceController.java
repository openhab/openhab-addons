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
package org.openhab.binding.tellstick.internal.handler;

import java.math.BigDecimal;

import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.tellstick.device.TellstickException;
import org.tellstick.device.iface.Device;

/**
 * Interface for telldus controllers. This is used to send and get status of devices from the controller.
 *
 * @author Jarle Hjortland - Initial contribution
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

/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.driver;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.message.Msg;

/**
 * Interface for classes that want to listen to notifications from the driver
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public interface DriverListener {
    /**
     * Notifies that the driver has disconnected
     */
    public abstract void disconnected();

    /**
     * Notifies that the modem database has completed
     */
    public abstract void modemDBCompleted();

    /**
     * Notifies that the modem database has been updated
     *
     * @param address the updated device address
     * @param group the updated link group
     */
    public abstract void modemDBUpdated(InsteonAddress address, int group);

    /**
     * Notifies that the modem has been found
     *
     * @param device the modem device
     */
    public abstract void modemFound(InsteonDevice device);

    /**
     * Notifies that the modem has been reset
     */
    public abstract void modemReset();

    /**
     * Notifies that a product data has been updated
     *
     * @param address the updated product data device address
     */
    public abstract void productDataUpdated(InsteonAddress address);

    /**
     * Notifies that a message has been received from a device
     *
     * @param address the device address the message was received from
     * @param msg the message received
     */
    public abstract void messageReceived(InsteonAddress address, Msg msg);

    /**
     * Notifies that a request has been sent to a device
     *
     * @param address the device address the request was sent to
     * @param time the time the request was sent
     */
    public abstract void requestSent(InsteonAddress address, long time);

    /**
     * Notifies that an im message has been received
     *
     * @param msg the message received
     */
    public abstract void imMessageReceived(Msg msg);

    /**
     * Notifies that an im request has been sent
     *
     * @param time the time the request was sent
     */
    public abstract void imRequestSent(long time);
}

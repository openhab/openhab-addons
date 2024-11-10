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
package org.openhab.binding.insteon.internal.device;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.transport.message.Msg;

/**
 * Interface for classes that represent a device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public interface Device {
    /**
     * Returns the address for this device
     *
     * @return the device address
     */
    public DeviceAddress getAddress();

    /**
     * Returns the product data for this device
     *
     * @return the device product data if defined, otherwise null
     */
    public @Nullable ProductData getProductData();

    /**
     * Returns the type for this device
     *
     * @return the device type if defined, otherwise null
     */
    public @Nullable DeviceType getType();

    /**
     * Returns a feature based on name for this device
     *
     * @param name the device feature name to match
     * @return the device feature if found, otherwise null
     */
    public @Nullable DeviceFeature getFeature(String name);

    /**
     * Returns the list of features for this device
     *
     * @return the list of device features
     */
    public List<DeviceFeature> getFeatures();

    /**
     * Polls this device
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void doPoll(long delay);

    /**
     * Handles an incoming message for this device
     *
     * @param msg the incoming message
     */
    public void handleMessage(Msg msg);

    /**
     * Sends a message after a delay to this device
     *
     * @param msg the message to be sent
     * @param feature device feature associated to the message
     * @param delay time (in milliseconds) to delay before sending message
     */
    public void sendMessage(Msg msg, DeviceFeature feature, long delay);

    /**
     * Handles next request for this device
     *
     * @return time (in milliseconds) before processing the subsequent request
     */
    public long handleNextRequest();

    /**
     * Notifies that a message request was replied for this device
     *
     * @param msg the message received
     */
    public void requestReplied(Msg msg);

    /**
     * Notifies that a message request was sent to this device
     *
     * @param msg the message sent
     * @param time the time the request was sent
     */
    public void requestSent(Msg msg, long time);

    /**
     * Refreshes this device
     */
    public void refresh();
}

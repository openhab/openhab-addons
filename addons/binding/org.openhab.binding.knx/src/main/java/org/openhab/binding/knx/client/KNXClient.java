/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.handler.GroupAddressListener;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.datapoint.Datapoint;

/**
 * Client for communicating with the KNX bus.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public interface KNXClient {

    /**
     * Check whether the client is connected
     *
     * @return {@code true} if the client currently is connected
     */
    boolean isConnected();

    /**
     * Determines whether the supplied address is occupied by a device in the KNX network or not.
     *
     * @param address the individual address to check
     * @return {@code true} if the address is occupied
     * @throws KNXException on network or send errors
     */
    boolean isReachable(@Nullable IndividualAddress address) throws KNXException;

    /**
     * Get the {@link DeviceInfoClient} which allows further device inspection.
     *
     * @return the device infor client
     * @throws IllegalStateException in case the client is not connected
     */
    DeviceInfoClient getDeviceInfoClient();

    /**
     * Initiates a basic restart of the device with the given address.
     *
     * @param address the individual address of the device
     */
    void restartNetworkDevice(@Nullable IndividualAddress address);

    /**
     * Register the given listener to be informed on KNX bus traffic.
     *
     * @param listener the listener
     * @return {@code true} if it wasn't registered before
     */
    boolean registerGroupAddressListener(GroupAddressListener listener);

    /**
     * Remove the given listener.
     *
     * @param listener the listener
     * @return {@code true} if it was successfully removed
     */
    boolean unregisterGroupAddressListener(GroupAddressListener listener);

    /**
     * Schedule the given data point for asynchronous reading.
     *
     * @param datapoint the datapoint
     */
    void readDatapoint(Datapoint datapoint);

    /**
     * Write a command to the KNX bus.
     *
     * @param commandSpec the outbound spec
     * @throws KNXException if any problem with the communication arises.
     */
    void writeToKNX(OutboundSpec commandSpec) throws KNXException;

    /**
     * Send a state as a read-response to the KNX bus.
     *
     * @param responseSpec the outbound spec
     * @throws KNXException if any problem with the communication arises.
     */
    void respondToKNX(OutboundSpec responseSpec) throws KNXException;

}

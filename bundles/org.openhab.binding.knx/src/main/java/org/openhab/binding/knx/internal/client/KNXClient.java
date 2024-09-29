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
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;

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
     * @return the device info client
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
     */
    void registerGroupAddressListener(GroupAddressListener listener);

    /**
     * Remove the given listener.
     *
     * @param listener the listener
     */
    void unregisterGroupAddressListener(GroupAddressListener listener);

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

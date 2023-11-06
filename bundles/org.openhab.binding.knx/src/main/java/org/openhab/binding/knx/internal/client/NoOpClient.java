/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public class NoOpClient implements KNXClient {

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isReachable(@Nullable IndividualAddress address) throws KNXException {
        return false;
    }

    @Override
    public DeviceInfoClient getDeviceInfoClient() {
        throw new IllegalStateException("KNX client not properly configured");
    }

    @Override
    public void restartNetworkDevice(@Nullable IndividualAddress address) {
        throw new IllegalStateException("KNX client not properly configured");
    }

    @Override
    public void registerGroupAddressListener(GroupAddressListener listener) {
    }

    @Override
    public void unregisterGroupAddressListener(GroupAddressListener listener) {
    }

    @Override
    public void readDatapoint(Datapoint datapoint) {
    }

    @Override
    public void writeToKNX(OutboundSpec commandSpec) throws KNXException {
    }

    @Override
    public void respondToKNX(OutboundSpec responseSpec) throws KNXException {
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.client.DeviceInfoClient;
import org.openhab.binding.knx.client.KNXClient;
import org.openhab.binding.knx.client.OutboundSpec;
import org.openhab.binding.knx.handler.GroupAddressListener;

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
    public boolean registerGroupAddressListener(GroupAddressListener listener) {
        return false;
    }

    @Override
    public boolean unregisterGroupAddressListener(GroupAddressListener listener) {
        return false;
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

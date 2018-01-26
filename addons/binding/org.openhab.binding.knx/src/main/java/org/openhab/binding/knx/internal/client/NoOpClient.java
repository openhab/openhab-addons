/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.knx.client.DeviceInfoClient;
import org.openhab.binding.knx.client.KNXClient;
import org.openhab.binding.knx.client.OutboundSpec;
import org.openhab.binding.knx.handler.GroupAddressListener;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */

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
    public @NonNull DeviceInfoClient getDeviceInfoClient() {
        throw new IllegalStateException("KNX client not properly configured");
    }

    @Override
    public void restartNetworkDevice(@Nullable IndividualAddress address) {
        throw new IllegalStateException("KNX client not properly configured");
    }

    @Override
    public boolean registerGroupAddressListener(@NonNull GroupAddressListener listener) {
        return false;
    }

    @Override
    public boolean unregisterGroupAddressListener(@NonNull GroupAddressListener listener) {
        return false;
    }

    @Override
    public void readDatapoint(@NonNull Datapoint datapoint) {
    }

    @Override
    public void writeToKNX(@NonNull OutboundSpec commandSpec) throws KNXException {
    }

    @Override
    public void respondToKNX(@NonNull OutboundSpec responseSpec, @NonNull State state) throws KNXException {
    }

}

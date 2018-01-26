/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.knx.handler.GroupAddressListener;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;

/**
 * Client for communicating with the KNX bus.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public interface KNXClient {

    boolean isConnected();

    boolean isReachable(@Nullable IndividualAddress address) throws KNXException;

    DeviceInfoClient getDeviceInfoClient();

    void restartNetworkDevice(@Nullable IndividualAddress address);

    boolean registerGroupAddressListener(GroupAddressListener listener);

    boolean unregisterGroupAddressListener(GroupAddressListener listener);

    void readDatapoint(Datapoint datapoint);

    void writeToKNX(OutboundSpec commandSpec) throws KNXException;

    void respondToKNX(OutboundSpec responseSpec, State state) throws KNXException;

}

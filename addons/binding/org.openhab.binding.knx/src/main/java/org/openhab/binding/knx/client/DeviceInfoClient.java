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

import tuwien.auto.calimero.IndividualAddress;

/**
 * Client to retrieve further information about KNX devices.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public interface DeviceInfoClient {

    byte @Nullable [] readDeviceDescription(IndividualAddress address, int descType, boolean authenticate,
            long timeout);

    byte @Nullable [] readDeviceMemory(IndividualAddress address, int startAddress, int bytes, boolean authenticate,
            long timeout);

    byte @Nullable [] readDeviceProperties(IndividualAddress address, final int interfaceObjectIndex,
            final int propertyId, final int start, final int elements, boolean authenticate, long timeout);

    public boolean isConnected();

}

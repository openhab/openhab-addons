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

import tuwien.auto.calimero.IndividualAddress;

/**
 * Client to retrieve further information about KNX devices.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public interface DeviceInfoClient {

    byte @Nullable [] readDeviceDescription(IndividualAddress address, int descType, boolean authenticate, long timeout)
            throws InterruptedException;

    byte @Nullable [] readDeviceMemory(IndividualAddress address, int startAddress, int bytes, boolean authenticate,
            long timeout) throws InterruptedException;

    byte @Nullable [] readDeviceProperties(IndividualAddress address, final int interfaceObjectIndex,
            final int propertyId, final int start, final int elements, boolean authenticate, long timeout)
            throws InterruptedException;

    public boolean isConnected();
}

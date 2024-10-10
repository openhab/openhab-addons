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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.handler.X10DeviceHandler;

/**
 * The {@link X10Device} represents an X10 device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class X10Device extends BaseDevice<X10Address, X10DeviceHandler> {
    public X10Device(X10Address address) {
        super(address);
    }

    /**
     * Factory method for creating a X10Device from a device address, modem and product data
     *
     * @param address the device address
     * @param modem the device modem
     * @param productData the device product data
     * @return the newly created X10Device
     */
    public static X10Device makeDevice(X10Address address, @Nullable InsteonModem modem, ProductData productData) {
        X10Device device = new X10Device(address);
        device.setModem(modem);

        DeviceType deviceType = productData.getDeviceType();
        if (deviceType != null) {
            device.instantiateFeatures(deviceType);
            device.setFlags(deviceType.getFlags());
        }
        device.setProductData(productData);

        return device;
    }
}

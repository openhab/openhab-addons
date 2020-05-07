/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluez.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.bluetooth.AbstractBluetoothAdapterService;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluez.BlueZBluetoothDevice;

/**
 * The {@link BlueZAdapterService} is implements the BlueZ {@link BluetoothAdapter} instance to be exposed to
 * the openHAB bluetooth framework.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class BlueZAdapterService extends AbstractBluetoothAdapterService<BlueZBluetoothDevice>
        implements BluetoothAdapter {

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        super.setThingHandler(handler);
        getHandler().setBluezAdapterService(this);
    }

    private BlueZBridgeHandler getHandler() {
        return (BlueZBridgeHandler) handler;
    }

    @Override
    public BluetoothAddress getAddress() {
        return getHandler().getAddress();
    }

    @Override
    protected BlueZBluetoothDevice createDevice(BluetoothAddress address) {
        return new BlueZBluetoothDevice(this, address);
    }

}

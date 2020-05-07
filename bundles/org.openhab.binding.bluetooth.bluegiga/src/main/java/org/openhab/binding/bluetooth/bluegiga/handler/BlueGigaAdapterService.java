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
package org.openhab.binding.bluetooth.bluegiga.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.bluetooth.AbstractBluetoothAdapterService;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluegiga.BlueGigaBluetoothDevice;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlueGigaAdapterService} is implements BlueGiga's {@link BluetoothAdapter} instance to be exposed to
 * the openHAB bluetooth framework.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class BlueGigaAdapterService extends AbstractBluetoothAdapterService<BlueGigaBluetoothDevice>
        implements BluetoothAdapter {

    private final Logger logger = LoggerFactory.getLogger(BlueGigaAdapterService.class);

    @Override
    public BluetoothAddress getAddress() {
        return getHandler().getAddress();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        super.setThingHandler(handler);
        getHandler().setBlueGigaAdapterService(this);
    }

    public BlueGigaBridgeHandler getHandler() {
        return (BlueGigaBridgeHandler) handler;
    }

    @Override
    protected BlueGigaBluetoothDevice createDevice(BluetoothAddress address) {
        return new BlueGigaBluetoothDevice(this, address, BluetoothAddressType.UNKNOWN);
    }

    @Override
    public void scanStart() {
        super.scanStart();
        logger.debug("Start active scan");
        // Stop the passive scan
        getHandler().cancelScheduledPassiveScan();
        getHandler().bgEndProcedure();

        // Start a active scan
        getHandler().bgStartScanning(true);
    }

    @Override
    public void scanStop() {
        super.scanStop();
        logger.debug("Stop active scan");

        // Stop the active scan
        getHandler().bgEndProcedure();

        // Start a passive scan after idle delay
        getHandler().schedulePassiveScan();
    }
}

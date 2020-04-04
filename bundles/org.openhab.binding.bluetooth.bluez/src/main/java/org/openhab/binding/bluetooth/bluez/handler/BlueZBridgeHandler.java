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

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.bluetooth.AbstractBluetoothBridgeHandler;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluez.BlueZBluetoothDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothException;
import tinyb.BluetoothManager;

/**
 * The {@link BlueZBridgeHandler} is responsible for talking to the BlueZ stack.
 * It provides a private interface for {@link BlueZBluetoothDevice}s to access the stack and provides top
 * level adaptor functionality for scanning and arbitration.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Hilbrand Bouwkamp - Simplified calling scan and better handling manual scanning
 * @author Connor Petty - Simplified device scan logic
 */
@NonNullByDefault
public class BlueZBridgeHandler extends AbstractBluetoothBridgeHandler<BlueZBluetoothDevice> {

    private final Logger logger = LoggerFactory.getLogger(BlueZBridgeHandler.class);

    private @NonNullByDefault({}) tinyb.BluetoothAdapter adapter;

    // Our BT address
    private @NonNullByDefault({}) BluetoothAddress adapterAddress;

    private @NonNullByDefault({}) ScheduledFuture<?> discoveryJob;

    /**
     * Constructor
     *
     * @param bridge the bridge definition for this handler
     */
    public BlueZBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        super.initialize();
        BluetoothManager manager;
        try {
            manager = BluetoothManager.getBluetoothManager();
            if (manager == null) {
                throw new IllegalStateException("Received null BlueZ manager");
            }
        } catch (UnsatisfiedLinkError e) {
            throw new IllegalStateException("BlueZ JNI connection cannot be established.", e);
        } catch (RuntimeException e) {
            // we do not get anything more specific from TinyB here
            if (e.getMessage() != null && e.getMessage().contains("AccessDenied")) {
                throw new IllegalStateException(
                        "Cannot access BlueZ stack due to permission problems. Make sure that your OS user is part of the 'bluetooth' group of BlueZ.");
            } else {
                throw new IllegalStateException("Cannot access BlueZ layer.", e);
            }
        }

        final BlueZAdapterConfiguration configuration = getConfigAs(BlueZAdapterConfiguration.class);
        if (configuration.address != null) {
            adapterAddress = new BluetoothAddress(configuration.address);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "address not set");
            return;
        }

        logger.debug("Creating BlueZ adapter with address '{}'", adapterAddress);

        for (tinyb.BluetoothAdapter adapter : manager.getAdapters()) {
            if (adapter == null) {
                logger.warn("got null adapter from bluetooth manager");
                continue;
            }
            if (adapter.getAddress().equals(adapterAddress.toString())) {
                this.adapter = adapter;
                discoveryJob = scheduler.scheduleWithFixedDelay(this::refreshDevices, 0, 10, TimeUnit.SECONDS);
                return;
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No adapter for this address found.");
    }

    private void startDiscovery() {
        // we need to make sure the adapter is powered first
        if (!adapter.getPowered()) {
            adapter.setPowered(true);
        }
        if (!adapter.getDiscovering()) {
            adapter.setRssiDiscoveryFilter(-96);
            adapter.startDiscovery();
        }
    }

    private void refreshDevices() {
        refreshTry: try {
            logger.debug("Refreshing Bluetooth device list...");
            List<tinyb.BluetoothDevice> tinybDevices = adapter.getDevices();
            logger.debug("Found {} Bluetooth devices.", tinybDevices.size());
            for (tinyb.BluetoothDevice tinybDevice : tinybDevices) {
                BlueZBluetoothDevice device = getDevice(new BluetoothAddress(tinybDevice.getAddress()));
                device.updateTinybDevice(tinybDevice);
                deviceDiscovered(device);
            }
            // For whatever reason, bluez will sometimes turn off scanning. So we just make sure it keeps running.
            startDiscovery();
        } catch (BluetoothException ex) {
            String message = ex.getMessage();
            if (message != null) {
                if (message.contains("Operation already in progress")) {
                    // we shouldn't go offline in this case
                    break refreshTry;
                }
                int idx = message.lastIndexOf(':');
                if (idx != -1) {
                    message = message.substring(idx).trim();
                }
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public BluetoothAddress getAddress() {
        return adapterAddress;
    }

    @Override
    protected BlueZBluetoothDevice createDevice(BluetoothAddress address) {
        return new BlueZBluetoothDevice(this, address);
    }

    @Override
    public void dispose() {
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
        if (adapter != null && adapter.getDiscovering()) {
            adapter.stopDiscovery();
        }
        super.dispose();
    }

}

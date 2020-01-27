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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothDiscoveryListener;
import org.openhab.binding.bluetooth.bluez.BlueZBluetoothDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothManager;

/**
 * The {@link BlueZBridgeHandler} is responsible for talking to the BlueZ stack.
 * It provides a private interface for {@link BlueZBluetoothDevice}s to access the stack and provides top
 * level adaptor functionality for scanning and arbitration.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Hilbrand Bouwkamp - Simplified calling scan and better handling manual scanning
 */
@NonNullByDefault
public class BlueZBridgeHandler extends BaseBridgeHandler implements BluetoothAdapter {

    private final Logger logger = LoggerFactory.getLogger(BlueZBridgeHandler.class);

    private @NonNullByDefault({}) tinyb.BluetoothAdapter adapter;

    // Our BT address
    private @NonNullByDefault({}) BluetoothAddress adapterAddress;

    // Internal flag for the discovery configuration
    private boolean discoveryConfigActive = true;
    // Actual discovery status.
    private volatile boolean discoveryActive = true;

    // private final @NonNullByDefault({
    // DefaultLocation.RETURN_TYPE }) Map<String, BlueZBluetoothDevice> trackedDevices = new ConcurrentHashMap<>();

    // Map of Bluetooth devices known to this bridge.
    // This contains the devices from the most recent scan
    private final @NonNullByDefault({
            DefaultLocation.RETURN_TYPE }) Map<BluetoothAddress, BlueZBluetoothDevice> devices = new ConcurrentHashMap<>();

    // Set of discovery listeners
    protected final Set<BluetoothDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();

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

        discoveryActive = discoveryConfigActive = Boolean.TRUE.equals(configuration.discovery);
        if (discoveryConfigActive) {
            logger.debug("Deactivated discovery participation.");
        }

        logger.debug("Creating BlueZ adapter with address '{}'", adapterAddress);

        for (tinyb.BluetoothAdapter adapter : manager.getAdapters()) {
            if (adapter == null) {
                logger.warn("got null adapter from bluetooth manager");
                continue;
            }
            if (adapter.getAddress().equals(adapterAddress.toString())) {
                this.adapter = adapter;
                updateStatus(ThingStatus.ONLINE);
                discoveryJob = scheduler.scheduleWithFixedDelay(this::refreshDevices, 0, 10, TimeUnit.SECONDS);
                return;
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No adapter for this address found.");
    }

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void addDiscoveryListener(BluetoothDiscoveryListener listener) {
        discoveryListeners.add(listener);
    }

    @Override
    public void removeDiscoveryListener(@Nullable BluetoothDiscoveryListener listener) {
        discoveryListeners.remove(listener);
    }

    private void startDiscovery() {
        if (!adapter.getDiscovering()) {
            adapter.setRssiDiscoveryFilter(-96);
            adapter.startDiscovery();
        }
    }

    private void refreshDevices() {
        logger.debug("Refreshing Bluetooth device list...");
        List<tinyb.BluetoothDevice> tinybDevices = adapter.getDevices();
        logger.debug("Found {} Bluetooth devices.", tinybDevices.size());
        for (tinyb.BluetoothDevice tinybDevice : tinybDevices) {
            BlueZBluetoothDevice device = getDevice(new BluetoothAddress(tinybDevice.getAddress()));
            device.updateTinybDevice(tinybDevice);
            notifyDiscoveryListeners(device);
        }
        // clean up orphaned entries
        synchronized (devices) {
            for (BlueZBluetoothDevice device : devices.values()) {
                if (shouldRemove(device)) {
                    logger.debug("Removing device '{}' due to inactivity", device.getAddress());
                    device.dispose();
                    devices.remove(device.getAddress());
                }
            }
        }
        // For whatever reason, bluez will sometimes turn off scanning. So we just make sure it keeps running.
        startDiscovery();
    }

    private boolean shouldRemove(BlueZBluetoothDevice device) {
        // we can't remove devices with listeners since that means they have a handler.
        if (device.hasListeners()) {
            return false;
        }
        // devices that are connected won't receive any scan notifications so we can't remove them for being idle
        if (device.getConnectionState() == ConnectionState.CONNECTED) {
            return false;
        }
        // we remove devices we haven't seen in a while
        return device.getTimeSinceSeen(TimeUnit.MINUTES) > 5;
    }

    @Override
    public void scanStart() {
        // Enable scanning even while discovery is disabled in config. This allows manual starting discovery.
        discoveryActive = true;
    }

    @Override
    public void scanStop() {
        // Set active discovery state back to the configured discovery state.
        discoveryActive = discoveryConfigActive;
        // We need to keep the adapter in discovery mode as we otherwise won't get any RSSI updates either
    }

    @Override
    public BluetoothAddress getAddress() {
        return adapterAddress;
    }

    @Override
    public BlueZBluetoothDevice getDevice(BluetoothAddress bluetoothAddress) {
        synchronized (devices) {
            BlueZBluetoothDevice device = devices.get(bluetoothAddress);
            if (device == null) {
                device = new BlueZBluetoothDevice(this, bluetoothAddress);
                device.initialize();
            }
            devices.put(bluetoothAddress, device);
            return device;
        }
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
        synchronized (devices) {
            for (BlueZBluetoothDevice device : devices.values()) {
                device.dispose();
            }
            devices.clear();
        }
    }

    private void notifyDiscoveryListeners(BluetoothDevice device) {
        if (discoveryActive) {
            if (deviceReachable(device)) {
                for (BluetoothDiscoveryListener listener : discoveryListeners) {
                    listener.deviceDiscovered(device);
                }
            } else {
                logger.trace("Not notifying listeners for device '{}', because it is not reachable.",
                        device.getAddress());
            }
        }
    }

    private boolean deviceReachable(BluetoothDevice device) {
        Integer rssi = device.getRssi();
        return rssi != null && rssi != 0;
    }
}

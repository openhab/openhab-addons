/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private final Map<String, tinyb.BluetoothDevice> tinybDeviceCache = new ConcurrentHashMap<>();

    // Our BT address
    private @NonNullByDefault({}) BluetoothAddress address;

    // Internal flag for the discovery configuration
    private boolean discoveryConfigActive = true;
    // Actual discovery status.
    private boolean discoveryActive = true;

    // Map of Bluetooth devices known to this bridge.
    // This is all devices we have heard on the network - not just things bound to the bridge
    private final Map<String, BluetoothDevice> devices = new ConcurrentHashMap<>();

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
        try {
            BluetoothManager.getBluetoothManager();
        } catch (UnsatisfiedLinkError e) {
            throw new IllegalStateException("BlueZ JNI connection cannot be established.", e);
        } catch (RuntimeException e) {
            // we do not get anything more specific from TinyB here
            if (e.getMessage().contains("AccessDenied")) {
                throw new IllegalStateException(
                        "Cannot access BlueZ stack due to permission problems. Make sure that your OS user is part of the 'bluetooth' group of BlueZ.");
            } else {
                throw new IllegalStateException("Cannot access BlueZ layer.", e);
            }
        }

        final BlueZAdapterConfiguration configuration = getConfigAs(BlueZAdapterConfiguration.class);
        if (configuration.address != null) {
            address = new BluetoothAddress(configuration.address);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "address not set");
            return;
        }

        discoveryActive = discoveryConfigActive = Boolean.TRUE.equals(configuration.discovery);
        if (discoveryConfigActive) {
            logger.debug("Deactivated discovery participation.");
        }

        logger.debug("Creating BlueZ adapter with address '{}'", address);
        for (tinyb.BluetoothAdapter a : BluetoothManager.getBluetoothManager().getAdapters()) {
            if (a.getAddress().equals(address.toString())) {
                adapter = a;
                updateStatus(ThingStatus.ONLINE);
                startDiscovery();
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
        Set<String> newAddresses = new HashSet<>();
        List<tinyb.BluetoothDevice> tinybDevices = adapter.getDevices();
        logger.debug("Found {} Bluetooth devices.", tinybDevices.size());
        synchronized (tinybDeviceCache) {
            tinybDeviceCache.clear();
            tinybDevices.stream().forEach(d -> tinybDeviceCache.put(d.getAddress(), d));
        }
        for (tinyb.BluetoothDevice tinybDevice : tinybDevices) {
            synchronized (devices) {
                newAddresses.add(tinybDevice.getAddress());
                BlueZBluetoothDevice device = (BlueZBluetoothDevice) devices.get(tinybDevice.getAddress());
                if (device == null) {
                    createAndRegisterBlueZDevice(tinybDevice);
                } else {
                    device.updateTinybDevice(tinybDevice);
                    notifyDiscoveryListeners(device);
                }
            }
        }
        // clean up orphaned entries
        synchronized (devices) {
            Set<String> oldAdresses = devices.keySet();
            for (String address : oldAdresses) {
                if (!newAddresses.contains(address)) {
                    devices.remove(address);
                }
            }
        }
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
        return address;
    }

    @Override
    public BluetoothDevice getDevice(BluetoothAddress bluetoothAddress) {
        String address = bluetoothAddress.toString();
        synchronized (devices) {
            if (devices.containsKey(address)) {
                return devices.get(address);
            } else {
                BlueZBluetoothDevice device = new BlueZBluetoothDevice(this, bluetoothAddress, "");
                device.initialize();
                devices.put(address, device);
                return device;
            }
        }
    }

    @Override
    public void dispose() {
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
        for (BluetoothDevice device : devices.values()) {
            ((BlueZBluetoothDevice) device).dispose();
        }
        devices.clear();
    }

    public Collection<tinyb.BluetoothDevice> getTinyBDevices() {
        synchronized (tinybDeviceCache) {
            return Collections.unmodifiableCollection(tinybDeviceCache.values());
        }
    }

    private BlueZBluetoothDevice createAndRegisterBlueZDevice(tinyb.BluetoothDevice tinybDevice) {
        BlueZBluetoothDevice device = new BlueZBluetoothDevice(this, tinybDevice);
        tinybDevice.getManufacturerData().entrySet().stream().map(Map.Entry::getKey).filter(Objects::nonNull)
                .findFirst().ifPresent(manufacturerId ->
                // Convert to unsigned int to match the convention in BluetoothCompanyIdentifiers
                device.setManufacturerId(manufacturerId & 0xFFFF));
        device.initialize();
        devices.put(tinybDevice.getAddress(), device);
        notifyDiscoveryListeners(device);
        return device;
    }

    private void notifyDiscoveryListeners(BluetoothDevice device) {
        if (discoveryActive && deviceReachable(device)) {
            for (BluetoothDiscoveryListener listener : discoveryListeners) {
                listener.deviceDiscovered(device);
            }
        } else {
            logger.trace("Not notifying listeners for device '{}', because it is not reachable.", device.getAddress());
        }
    }

    private boolean deviceReachable(BluetoothDevice device) {
        Integer rssi = device.getRssi();
        return rssi != null && rssi != 0;
    }
}

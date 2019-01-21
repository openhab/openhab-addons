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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.binding.bluetooth.bluez.BlueZAdapterConstants;
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
 */
public class BlueZBridgeHandler extends BaseBridgeHandler implements BluetoothAdapter {

    private final Logger logger = LoggerFactory.getLogger(BlueZBridgeHandler.class);

    private tinyb.BluetoothAdapter adapter;

    private final Map<String, tinyb.BluetoothDevice> tinybDeviceCache = new ConcurrentHashMap<>();

    // Our BT address
    private BluetoothAddress address;

    // internal flag for the discovery configuration
    private boolean discoveryActive = true;

    // Map of Bluetooth devices known to this bridge.
    // This is all devices we have heard on the network - not just things bound to the bridge
    private final Map<String, BluetoothDevice> devices = new ConcurrentHashMap<>();

    // Set of discovery listeners
    protected final Set<BluetoothDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();

    private ScheduledFuture<?> discoveryJob;

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

        Object cfgAddress = getConfig().get(BlueZAdapterConstants.PROPERTY_ADDRESS);
        if (cfgAddress != null) {
            address = new BluetoothAddress(cfgAddress.toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "address not set");
            return;
        }

        Object discovery = getConfig().get(BlueZAdapterConstants.PROPERTY_DISCOVERY);
        if (discovery != null && discovery.toString().equalsIgnoreCase(Boolean.FALSE.toString())) {
            discoveryActive = false;
            logger.debug("Deactivated discovery participation.");
        }

        logger.debug("Creating BlueZ adapter with address '{}'", address);
        for (tinyb.BluetoothAdapter a : BluetoothManager.getBluetoothManager().getAdapters()) {
            if (a.getAddress().equals(address.toString())) {
                adapter = a;
                updateStatus(ThingStatus.ONLINE);
                if (!adapter.getDiscovering()) {
                    adapter.startDiscovery();
                }
                discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                    checkForNewDevices();
                }, 0, 10, TimeUnit.SECONDS);
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

    @Override
    public void scanStart() {
        if (adapter != null) {
            if (!adapter.getDiscovering()) {
                adapter.setRssiDiscoveryFilter(-96);
                adapter.startDiscovery();
            }
            for (tinyb.BluetoothDevice tinybDevice : adapter.getDevices()) {
                synchronized (devices) {
                    logger.debug("Device {} has RSSI {}", tinybDevice.getAddress(), tinybDevice.getRSSI());
                    BluetoothDevice device = devices.get(tinybDevice.getAddress());
                    if (device == null) {
                        createAndRegisterBlueZDevice(tinybDevice);
                    } else {
                        // let's update the rssi and txpower values
                        device.setRssi(tinybDevice.getRSSI());
                        device.setTxPower(tinybDevice.getTxPower());
                        // The Bluetooth discovery expects a complete list on every scan,
                        // so we also have to report the already known devices.
                        notifyDiscoveryListeners(device);
                    }
                }
            }
        }
    }

    @Override
    public void scanStop() {
        // nothing do do here, we need to keep the adapter in discovery mode as we otherwise won't get any RSSI updates
        // either
    }

    @Override
    public BluetoothAddress getAddress() {
        return address;
    }

    @Override
    public BluetoothDevice getDevice(BluetoothAddress address) {
        if (devices.containsKey(address.toString())) {
            return devices.get(address.toString());
        } else {
            synchronized (devices) {
                if (devices.containsKey(address.toString())) {
                    return devices.get(address.toString());
                } else {
                    BlueZBluetoothDevice device = new BlueZBluetoothDevice(this, address, "");
                    device.initialize();
                    devices.put(address.toString(), device);
                    return device;
                }
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

    private void checkForNewDevices() {
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

    private BlueZBluetoothDevice createAndRegisterBlueZDevice(tinyb.BluetoothDevice tinybDevice) {
        BlueZBluetoothDevice device = new BlueZBluetoothDevice(this, tinybDevice);
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

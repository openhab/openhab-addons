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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bluez.exceptions.BluezFailedException;
import org.bluez.exceptions.BluezInvalidArgumentsException;
import org.bluez.exceptions.BluezNotReadyException;
import org.bluez.exceptions.BluezNotSupportedException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.freedesktop.dbus.types.Variant;
import org.openhab.binding.bluetooth.AbstractBluetoothBridgeHandler;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluez.internal.events.AdapterDiscoveringChangedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.AdapterPoweredChangedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEventListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;

/**
 * The {@link BlueZBridgeHandler} is responsible for talking to the BlueZ stack, using DBus Unix Socket.
 * This Binding does not use any JNI.
 * It provides a private interface for {@link BlueZBluetoothDevice}s to access the stack and provides top
 * level adaptor functionality for scanning and arbitration.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Hilbrand Bouwkamp - Simplified calling scan and better handling manual scanning
 * @author Connor Petty - Simplified device scan logic
 * @author Benjamin Lafois - Replaced tinyB with bluezDbus
 *
 */
@NonNullByDefault
public class BlueZBridgeHandler extends AbstractBluetoothBridgeHandler<BlueZBluetoothDevice>
        implements BlueZEventListener {

    private final Logger logger = LoggerFactory.getLogger(BlueZBridgeHandler.class);

    // ADAPTER from BlueZ-DBus Library
    private @Nullable BluetoothAdapter adapter;

    // Our BT address
    private @Nullable BluetoothAddress adapterAddress;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private final DeviceManagerFactory deviceManagerFactory;

    /**
     * Constructor
     *
     * @param bridge the bridge definition for this handler
     */
    public BlueZBridgeHandler(Bridge bridge, DeviceManagerFactory deviceManagerFactory) {
        super(bridge);
        this.deviceManagerFactory = deviceManagerFactory;
    }

    @Override
    public void initialize() {
        super.initialize();

        // Load configuration
        final BlueZAdapterConfiguration configuration = getConfigAs(BlueZAdapterConfiguration.class);
        String addr = configuration.address;
        if (addr != null) {
            this.adapterAddress = new BluetoothAddress(addr.toUpperCase());
        } else {
            // If configuration does not contain adapter address to use, exit with error.
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "address not set");
            return;
        }

        logger.debug("Creating BlueZ adapter with address '{}'", adapterAddress);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing");
        deviceManagerFactory.getPropertiesChangedHandler().addListener(this);
        discoveryJob = scheduler.scheduleWithFixedDelay(this::initializeAndRefreshDevices, 5, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        deviceManagerFactory.getPropertiesChangedHandler().removeListener(this);
        logger.debug("Termination of DBus BlueZ handler");

        Future<?> job = discoveryJob;
        if (job != null) {
            job.cancel(false);
            discoveryJob = null;
        }

        BluetoothAdapter localAdatper = this.adapter;
        if (localAdatper != null) {
            localAdatper.stopDiscovery();
            this.adapter = null;
        }

        super.dispose();
    }

    private @Nullable BluetoothAdapter prepareAdapter(DeviceManagerWrapper deviceManager) {
        // next lets check if we can find our adapter in the manager.
        BluetoothAdapter localAdapter = adapter;
        if (localAdapter == null) {
            BluetoothAddress localAddress = adapterAddress;
            if (localAddress != null) {
                localAdapter = adapter = deviceManager.getAdapter(localAddress);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No adapter address provided");
                return null;
            }
        }
        if (localAdapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Native adapter could not be found for address '" + adapterAddress + "'");
            return null;
        }
        // now lets confirm that the adapter is powered
        if (!localAdapter.isPowered()) {
            localAdapter.setPowered(true);
            // give the device some time to power on
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "Adapter is not powered, attempting to turn on...");
            return null;
        }

        Map<String, Variant<?>> filter = new HashMap<>();
        filter.put("DuplicateData", new Variant<>(true));
        try {
            adapter.setDiscoveryFilter(filter);
        } catch (BluezInvalidArgumentsException | BluezFailedException | BluezNotSupportedException
                | BluezNotReadyException e) {
            throw new RuntimeException(e);
        }

        // now lets make sure that discovery is turned on
        if (!localAdapter.startDiscovery()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Trying to start discovery");
            return null;
        }
        return localAdapter;
    }

    private void initializeAndRefreshDevices() {
        logger.debug("initializeAndRefreshDevice()");

        try {
            // first check if the device manager is ready
            DeviceManagerWrapper deviceManager = deviceManagerFactory.getDeviceManager();
            if (deviceManager == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Bluez DeviceManager not available yet.");
                return;
            }

            BluetoothAdapter adapter = prepareAdapter(deviceManager);
            if (adapter == null) {
                // adapter isn't prepared yet
                return;
            }

            // now lets refresh devices
            List<BluetoothDevice> bluezDevices = deviceManager.getDevices(adapter);
            logger.debug("Found {} Bluetooth devices.", bluezDevices.size());
            for (BluetoothDevice bluezDevice : bluezDevices) {
                if (bluezDevice.getAddress() == null) {
                    // For some reasons, sometimes the address is null..
                    continue;
                }
                BlueZBluetoothDevice device = getDevice(new BluetoothAddress(bluezDevice.getAddress()));
                device.updateBlueZDevice(bluezDevice);
                deviceDiscovered(device);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception ex) {
            // don't know what kind of exception the bluez library might throw at us so lets catch them here so our
            // scheduler loop doesn't get terminated
            logger.warn("Unknown exception", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    @Override
    public @Nullable BluetoothAddress getAddress() {
        return adapterAddress;
    }

    @Override
    protected BlueZBluetoothDevice createDevice(BluetoothAddress address) {
        logger.debug("createDevice {}", address);
        return new BlueZBluetoothDevice(this, address);
    }

    @Override
    public void onDBusBlueZEvent(BlueZEvent event) {
        BluetoothAdapter localAdapter = this.adapter;
        String adapterName = event.getAdapterName();
        if (adapterName == null || localAdapter == null) {
            // We cannot be sure that this event concerns this adapter.. So ignore message
            return;
        }
        String localName = localAdapter.getDeviceName();

        if (!adapterName.equals(localName)) {
            // does not concern this adapter
            return;
        }

        BluetoothAddress address = event.getDevice();

        if (address != null) {
            // now lets forward the event to the corresponding bluetooth device
            BlueZBluetoothDevice device = getDevice(address);
            event.dispatch(device);
        }
    }

    @Override
    public void onDiscoveringChanged(AdapterDiscoveringChangedEvent event) {
        // do nothing for now
    }

    @Override
    public void onPoweredChange(AdapterPoweredChangedEvent event) {
        // do nothing for now
    }
}

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
package org.openhab.binding.bluetooth.dbusbluez.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.freedesktop.dbus.exceptions.DBusException;
import org.openhab.binding.bluetooth.AbstractBluetoothBridgeHandler;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.dbusbluez.DBusBlueZBluetoothDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;

/**
 * The {@link DBusBlueZBridgeHandler} is responsible for talking to the BlueZ stack, using DBus Unix Socket.
 * This Binding does not use any JNI.
 * It provides a private interface for {@link DBusBlueZBluetoothDevice}s to access the stack and provides top
 * level adaptor functionality for scanning and arbitration.
 *
 * @author Benjamin Lafois - Initial contribution and API
 */
@NonNullByDefault
public class DBusBlueZBridgeHandler extends AbstractBluetoothBridgeHandler<DBusBlueZBluetoothDevice> {

    private final Logger logger = LoggerFactory.getLogger(DBusBlueZBridgeHandler.class);

    // ADAPTER from BlueZ-DBus Library
    private @NonNullByDefault({}) BluetoothAdapter adapter;
    private @NonNullByDefault({}) DeviceManager deviceManager;

    // Our BT address
    private @NonNullByDefault({}) BluetoothAddress adapterAddress;

    private DBusBlueZPropertiesChangedHandler propertiesChangedHandler = new DBusBlueZPropertiesChangedHandler();

    private final ReentrantLock lockDiscoveryJob = new ReentrantLock();

    private @Nullable ScheduledFuture<?> discoveryJob;

    /**
     * Constructor
     *
     * @param bridge the bridge definition for this handler
     */
    public DBusBlueZBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        super.initialize();
        initializeInternal();
    }

    /**
     * This function initializes the library
     *
     * @return True if succeeded initializing the library, false otherwise
     * @throws InterruptedException
     */
    private boolean initializeDeviceManager() {
        logger.debug("Initializing Device Manager");

        try {
            initializeDeviceManagerInternal();
        } catch (DBusException e1) {
            logger.error("failed create instance caused by D-BUS.", e1);
            return false;
        }

        // This call will return for sure the DM, otherwize createInstance
        // would have failed previously
        this.deviceManager = DeviceManager.getInstance();

        if (this.deviceManager != null) {
            logger.debug("Device Manager correctly instanciated");

            // a handler must be instanciated to get all notifications
            // from DBUS (new device, RSSI update, characteristic notification...)
            try {
                this.deviceManager.registerPropertyHandler(this.propertiesChangedHandler);
            } catch (DBusException e) {
                // Shoudl not happen..
                logger.error("Error registering properties changed handler", e);
                return false;
            }
        } else {
            // should normally not happen..
            logger.debug("Device Manager could not be instanciated but no error.");
        }

        return (this.deviceManager != null);
    }

    private void initializeDeviceManagerInternal() throws DBusException {
        try {
            // if this is the first call to the library, this call
            // should throw an exception (that we are catching)
            this.deviceManager = DeviceManager.getInstance();

            // Experimental - seems reuse does not work
            // this.deviceManager.closeConnection();
            // DeviceManager.createInstance(false);
        } catch (IllegalStateException e) {
            // Exception caused by first call to the library
            DeviceManager.createInstance(false);
        }
    }

    /**
     * This function finds the adapter providing the address in param
     *
     * @return
     */
    private boolean initializeAdapter() {

        List<BluetoothAdapter> adapters = this.deviceManager.getAdapters();

        if (adapters == null || adapters.isEmpty()) {
            return false;
        }

        for (BluetoothAdapter btAdapter : adapters) {
            if (btAdapter.getAddress() != null
                    && btAdapter.getAddress().equalsIgnoreCase(this.adapterAddress.toString())) {
                // Found the good adapter
                this.adapter = btAdapter;

                logger.debug("Turning off adapter...");
                // Power cycle OFF / ON for a clean start
                this.adapter.setPowered(false);
                logger.debug("Adapter state: {}", this.adapter.isPowered());

                // Only restart the adapter 1 second at least after stopping it. stop/start too quick will not work.
                scheduler.schedule(() -> {
                    logger.debug("Turning on adapter...");
                    this.adapter.setPowered(true);
                    logger.debug("Adapter DBUS path: {}", this.adapter.getDbusPath());
                }, 1, TimeUnit.SECONDS);

                return true;
            }
        }

        return false;
    }

    private void initializeInternal() {

        // Load configuration
        final DBusBlueZAdapterConfiguration configuration = getConfigAs(DBusBlueZAdapterConfiguration.class);
        if (configuration.address != null) {
            this.adapterAddress = new BluetoothAddress(configuration.address.toUpperCase());
        } else {
            // If configuration does not contain adapter address to use, exit with error.
            logger.info("Adapter MAC address not provided");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "address not set");
            return;
        }

        logger.debug("Creating BlueZ adapter with address '{}'", adapterAddress);

        if (!initializeDeviceManager()) {
            // Device manager not initialized so exiting.
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Library error.");
            return;
        }

        if (!initializeAdapter()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Bluetooth adapter could not be found.");
            return;
        }

        // Beyond this point, lib is initialized and adapter was found.
        updateStatus(ThingStatus.ONLINE);

        this.discoveryJob = scheduler.scheduleAtFixedRate(this::refreshDevices, 10, 10, TimeUnit.SECONDS);
    }

    /**
     *
     * @return
     */
    public boolean isCurrentlyScanning() {
        boolean scanning = this.adapter.isDiscovering();
        logger.debug("Is adapter currently scanning : {}", scanning);
        return scanning;
    }

    @Override
    public BluetoothAddress getAddress() {
        return adapterAddress;
    }

    @Override
    public void dispose() {

        logger.debug("Termination of DBus BlueZ handler");

        if (this.discoveryJob != null) {
            this.discoveryJob.cancel(true);
            this.discoveryJob = null;
        }

        if (this.adapter != null) {
            this.adapter.stopDiscovery();
            this.adapter = null;
        }

        if (this.deviceManager != null) {
            this.deviceManager.closeConnection();
            this.deviceManager = null;
        }

        super.dispose();
    }

    private void startDiscovery() {
        // we need to make sure the adapter is powered first
        if (!adapter.isPowered()) {
            adapter.setPowered(true);
        }
        if (Boolean.FALSE.equals(adapter.isDiscovering())) {
            adapter.startDiscovery();
        }
    }

    private void refreshDevices() {
        logger.debug("refreshDevices()");

        if (lockDiscoveryJob.tryLock()) {

            try {

                List<BluetoothDevice> dBusBlueZDevices = this.deviceManager.getDevices(true);

                logger.debug("Found {} Bluetooth devices.", dBusBlueZDevices.size());

                for (BluetoothDevice dBusBlueZDevice : dBusBlueZDevices) {
                    if (dBusBlueZDevice.getAddress() == null) {
                        // For some reasons, sometimes the address is null..
                        continue;
                    }
                    DBusBlueZBluetoothDevice device = getDevice(new BluetoothAddress(dBusBlueZDevice.getAddress()));
                    device.updateDBusBlueZDevice(dBusBlueZDevice);
                    deviceDiscovered(device);
                }

                if (!isCurrentlyScanning()) {
                    startDiscovery();
                }

            } catch (Exception e) {
                logger.error("Error in refresh process", e);
            } finally {
                lockDiscoveryJob.unlock();
            }

        }

    }

    @Override
    protected DBusBlueZBluetoothDevice createDevice(BluetoothAddress address) {
        DBusBlueZBluetoothDevice device = new DBusBlueZBluetoothDevice(this, address);
        this.propertiesChangedHandler.addListener(device);
        return device;
    }

}

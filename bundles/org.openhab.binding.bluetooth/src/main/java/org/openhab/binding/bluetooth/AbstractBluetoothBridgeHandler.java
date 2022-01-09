/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a abstract superclass for BluetoothAdapter implementations. This class takes care of inactive device cleanup
 * as well as handling background and active discovery logic.
 *
 * Subclasses will primarily be responsible for device discovery
 *
 * @author Connor Petty - Initial contribution from refactored code
 */
@NonNullByDefault
public abstract class AbstractBluetoothBridgeHandler<BD extends BaseBluetoothDevice> extends BaseBridgeHandler
        implements BluetoothAdapter {

    private final Logger logger = LoggerFactory.getLogger(AbstractBluetoothBridgeHandler.class);

    // Set of discovery listeners
    private final Set<BluetoothDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();

    // Map of Bluetooth devices known to this bridge.
    // This contains the devices from the most recent scan
    private final Map<BluetoothAddress, BD> devices = new ConcurrentHashMap<>();

    // Actual discovery status.
    protected volatile boolean activeScanEnabled = false;

    private BaseBluetoothBridgeHandlerConfiguration config = new BaseBluetoothBridgeHandlerConfiguration();

    private @Nullable ScheduledFuture<?> inactiveRemovalJob;

    /**
     * Constructor
     *
     * @param bridge the bridge definition for this handler
     */
    public AbstractBluetoothBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    @Override
    public @Nullable String getLocation() {
        return getThing().getLocation();
    }

    @Override
    public @Nullable String getLabel() {
        return getThing().getLabel();
    }

    @Override
    public void initialize() {
        config = getConfigAs(BaseBluetoothBridgeHandlerConfiguration.class);

        int intervalSecs = config.inactiveDeviceCleanupInterval;
        inactiveRemovalJob = scheduler.scheduleWithFixedDelay(this::removeInactiveDevices, intervalSecs, intervalSecs,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> inactiveRemovalJob = this.inactiveRemovalJob;
        if (inactiveRemovalJob != null) {
            inactiveRemovalJob.cancel(true);
        }
        this.inactiveRemovalJob = null;

        synchronized (devices) {
            for (BD device : devices.values()) {
                removeDevice(device);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void removeInactiveDevices() {
        // clean up orphaned entries
        synchronized (devices) {
            for (BD device : devices.values()) {
                if (shouldRemove(device)) {
                    logger.debug("Removing device '{}' due to inactivity", device.getAddress());
                    removeDevice(device);
                }
            }
        }
    }

    protected void removeDevice(BD device) {
        device.dispose();
        synchronized (devices) {
            devices.remove(device.getAddress());
        }
        discoveryListeners.forEach(listener -> listener.deviceRemoved(device));
    }

    private boolean shouldRemove(BD device) {
        // we can't remove devices with listeners since that means they have a handler.
        if (device.hasListeners()) {
            return false;
        }
        // devices that are connected won't receive any scan notifications so we can't remove them for being idle
        if (device.getConnectionState() == ConnectionState.CONNECTED) {
            return false;
        }

        ZonedDateTime lastActiveTime = device.getLastSeenTime();
        if (lastActiveTime == null) {
            // we want any new device to at least live a certain amount of time so it has a chance to be discovered or
            // listened to.
            lastActiveTime = device.createTime;
        }
        // we remove devices we haven't seen in a while
        return ZonedDateTime.now().minusSeconds(config.inactiveDeviceCleanupThreshold).isAfter(lastActiveTime);
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
        // Enable scanning even while discovery is disabled in config. This allows manual starting discovery.
        activeScanEnabled = true;
        refreshDiscoveredDevices();
    }

    protected void refreshDiscoveredDevices() {
        logger.debug("Refreshing Bluetooth device list...");
        synchronized (devices) {
            devices.values().forEach(this::deviceDiscovered);
        }
    }

    @Override
    public void scanStop() {
        // Set active discovery state back to the configured discovery state.
        activeScanEnabled = false;
        // We need to keep the adapter in discovery mode as we otherwise won't get any RSSI updates either
    }

    @Override
    public BD getDevice(BluetoothAddress address) {
        synchronized (devices) {
            return Objects.requireNonNull(devices.computeIfAbsent(address, this::createDevice));
        }
    }

    protected abstract BD createDevice(BluetoothAddress address);

    @Override
    public boolean hasHandlerForDevice(BluetoothAddress address) {
        String addrStr = address.toString();
        /*
         * This type of search is inefficient and won't scale as the number of bluetooth Thing children increases on
         * this bridge. But implementing a more efficient search would require a bit more overhead.
         * Luckily though, it is reasonable to assume that the number of Thing children will remain small.
         */
        for (Thing childThing : getThing().getThings()) {
            Object childAddr = childThing.getConfiguration().get(BluetoothBindingConstants.CONFIGURATION_ADDRESS);
            if (addrStr.equals(childAddr)) {
                return childThing.getHandler() != null;
            }
        }
        return false;
    }

    public void deviceDiscovered(BluetoothDevice device) {
        if (hasHandlerForDevice(device.getAddress())) {
            // no point in discovering a device that already has a handler
            return;
        }
        if (config.backgroundDiscovery || activeScanEnabled) {
            if (deviceReachable(device)) {
                discoveryListeners.forEach(listener -> listener.deviceDiscovered(device));
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

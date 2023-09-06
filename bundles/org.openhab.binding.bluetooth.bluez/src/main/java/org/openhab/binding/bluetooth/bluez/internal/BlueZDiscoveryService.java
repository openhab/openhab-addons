/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter;

/**
 * This is a discovery service, which checks whether we are running on a Linux with a BlueZ stack.
 * If this is the case, we create a bridge handler that provides Bluetooth access through BlueZ.
 *
 * @author Kai Kreuzer - Initial Contribution and API
 * @author Hilbrand Bouwkamp - Moved background scan to actual background method
 * @author Connor Petty - Replaced tinyB with bluezDbus
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.bluetooth.bluez")
public class BlueZDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(BlueZDiscoveryService.class);

    private final DeviceManagerFactory deviceManagerFactory;
    private @Nullable Future<?> backgroundScan;

    @Activate
    public BlueZDiscoveryService(@Reference DeviceManagerFactory deviceManagerFactory) {
        super(Collections.singleton(BlueZAdapterConstants.THING_TYPE_BLUEZ), 1, true);
        this.deviceManagerFactory = deviceManagerFactory;
    }

    private static void cancel(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(false);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        backgroundScan = scheduler.scheduleWithFixedDelay(() -> {
            DeviceManagerWrapper deviceManager = deviceManagerFactory.getDeviceManager();
            if (deviceManager == null) {
                return;
            }
            startScan();
        }, 5, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        cancel(backgroundScan);
        backgroundScan = null;
    }

    @Override
    protected void startScan() {
        DeviceManagerWrapper deviceManager = deviceManagerFactory.getDeviceManager();
        if (deviceManager == null) {
            logger.warn("The DeviceManager is not available");
            return;
        }
        // the first time the device manager is not null we can cancel background discovery
        stopBackgroundDiscovery();
        deviceManager.scanForBluetoothAdapters().stream()//
                .map(this::createDiscoveryResult)//
                .forEach(this::thingDiscovered);
    }

    private DiscoveryResult createDiscoveryResult(BluetoothAdapter adapter) {
        return DiscoveryResultBuilder.create(new ThingUID(BlueZAdapterConstants.THING_TYPE_BLUEZ, getId(adapter)))
                .withLabel("Bluetooth Interface " + adapter.getName())
                .withProperty(BlueZAdapterConstants.PROPERTY_ADDRESS, adapter.getAddress())
                .withRepresentationProperty(BlueZAdapterConstants.PROPERTY_ADDRESS).build();
    }

    private String getId(BluetoothAdapter adapter) {
        return adapter.getDeviceName().replaceAll("[^a-zA-Z0-9_]", "");
    }
}

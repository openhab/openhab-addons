/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.MerossBindingConstants;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.handler.MerossBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossDiscoveryService} class is responsible for performing device discovery
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MerossDiscoveryService.class)
public class MerossDiscoveryService extends AbstractThingHandlerDiscoveryService<MerossBridgeHandler> {
    public static final String DEVICE_UUID = "deviceUUID";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String FIRMWARE_VERSION = "firmwareVersion";
    private final Logger logger = LoggerFactory.getLogger(MerossDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private @Nullable ScheduledFuture<?> scanTask;

    public MerossDiscoveryService() {
        super(MerossBridgeHandler.class, MerossBindingConstants.DISCOVERABLE_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS,
                true);
    }

    @Override
    public void startBackgroundDiscovery() {
        discoverDevices();
    }

    @Override
    protected void startScan() {
        ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
        }
        this.scanTask = scheduler.schedule(() -> discoverDevices(), 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    public void discoverDevices() {
        List<Device> devices = null;
        if (thingHandler.getMerossHttpConnector() != null) {
            var merossHttpConnector = thingHandler.getMerossHttpConnector();
            if (merossHttpConnector != null) {
                devices = merossHttpConnector.readDevices();
            }
        }
        if (devices == null || devices.isEmpty()) {
            logger.debug("No device found");
        } else {
            ThingUID bridgeUID = thingHandler.getThing().getUID();
            devices.forEach(device -> {
                ThingTypeUID thingTypeUID;
                if (isLightType(device.deviceType())) {
                    thingTypeUID = MerossBindingConstants.THING_TYPE_LIGHT;
                } else {
                    logger.debug("Unsupported device found: name {} : type {}", device.devName(), device.deviceType());
                    return;
                }
                ThingUID deviceThing = new ThingUID(thingTypeUID, thingHandler.getThing().getUID(), device.uuid());
                Map<String, Object> deviceProperties = new HashMap<>();
                deviceProperties.put(MerossBindingConstants.PROPERTY_DEVICE_NAME, device.devName());
                deviceProperties.put(DEVICE_UUID, device.uuid());
                deviceProperties.put(DEVICE_TYPE, device.deviceType());
                deviceProperties.put(FIRMWARE_VERSION, device.firmwareVersion());
                thingDiscovered(DiscoveryResultBuilder.create(deviceThing).withLabel(device.devName())
                        .withProperties(deviceProperties)
                        .withRepresentationProperty(MerossBindingConstants.PROPERTY_DEVICE_NAME).withBridge(bridgeUID)
                        .build());
            });
        }
    }

    public boolean isLightType(String typeName) {
        String targetString = typeName.substring(0, 3);
        Set<String> types = MerossBindingConstants.DISCOVERABLE_LIGHT_HARDWARE_TYPES;
        return types.stream().anyMatch(type -> type.equals(targetString));
    }
}

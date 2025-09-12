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

import java.net.ConnectException;
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
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossDiscoveryService} class is responsible for performing device discovery
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Added garage door support
 * @author Mark Herwege - Discovery on bridge initialization, allow manual scan
 * @author Mark Herwege - Device uuid config parameter and representation property
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MerossDiscoveryService.class)
public class MerossDiscoveryService extends AbstractThingHandlerDiscoveryService<MerossBridgeHandler> {

    public static final String DEVICE_TYPE = "deviceType";
    public static final String DEVICE_SUB_TYPE = "subType";
    public static final String FIRMWARE_VERSION = "firmwareVersion";
    public static final String HARDWARE_VERSION = "hardwareVersion";
    public static final String REGION = "region";

    private final Logger logger = LoggerFactory.getLogger(MerossDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private @Nullable ScheduledFuture<?> scanTask;

    public MerossDiscoveryService() {
        super(MerossBridgeHandler.class, MerossBindingConstants.DISCOVERABLE_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS,
                true);
    }

    @Override
    public void startBackgroundDiscovery() {
        // Don't allow automatic background discovery to avoid excessive cloud traffic and being blocked by Meross.
        // Discovery will happen on bridge initialization and when manually triggered.
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

    @Override
    public void setThingHandler(ThingHandler handler) {
        super.setThingHandler(handler);
        if (handler instanceof MerossBridgeHandler bridgeHandler) {
            bridgeHandler.setDiscoveryService(this);
        }
    }

    public void discoverDevices() {
        List<Device> devices;
        try {
            devices = thingHandler.discoverDevices();
        } catch (ConnectException e) {
            logger.debug("Connection error, could not retrieve devices");
            return;
        }
        if (devices.isEmpty()) {
            logger.debug("No device found");
        } else {
            ThingUID bridgeUID = thingHandler.getThing().getUID();
            devices.forEach(device -> {
                String deviceType = device.deviceType();
                ThingTypeUID thingTypeUID;
                if (isLightType(deviceType)) {
                    thingTypeUID = MerossBindingConstants.THING_TYPE_LIGHT;
                } else if (isDoorType(deviceType)) {
                    switch (deviceType) {
                        case MerossBindingConstants.MSG100:
                            thingTypeUID = MerossBindingConstants.THING_TYPE_DOOR;
                            break;
                        case MerossBindingConstants.MSG200:
                            thingTypeUID = MerossBindingConstants.THING_TYPE_TRIPLE_DOOR;
                            break;
                        default:
                            logger.debug("Device type {} not recognized, default to single garage door opener",
                                    deviceType);
                            thingTypeUID = MerossBindingConstants.THING_TYPE_DOOR;
                            break;
                    }
                    ;
                } else {
                    logger.debug("Unsupported device found: name {} : type {}", device.devName(), device.deviceType());
                    return;
                }
                ThingUID deviceThing = new ThingUID(thingTypeUID, thingHandler.getThing().getUID(), device.uuid());
                Map<String, Object> deviceProperties = new HashMap<>();
                deviceProperties.put(MerossBindingConstants.PROPERTY_DEVICE_NAME, device.devName());
                deviceProperties.put(MerossBindingConstants.PROPERTY_DEVICE_UUID, device.uuid());
                deviceProperties.put(DEVICE_TYPE, device.deviceType());
                deviceProperties.put(DEVICE_SUB_TYPE, device.deviceType());
                deviceProperties.put(FIRMWARE_VERSION, device.subType());
                deviceProperties.put(HARDWARE_VERSION, device.hardwareVersion());
                deviceProperties.put(REGION, device.region());
                thingDiscovered(DiscoveryResultBuilder.create(deviceThing).withLabel(device.devName())
                        .withProperties(deviceProperties)
                        .withRepresentationProperty(MerossBindingConstants.PROPERTY_DEVICE_UUID).withBridge(bridgeUID)
                        .build());
            });
        }
    }

    public boolean isLightType(String typeName) {
        String targetString = typeName.substring(0, 3);
        Set<String> types = MerossBindingConstants.DISCOVERABLE_LIGHT_HARDWARE_TYPES;
        return types.stream().anyMatch(type -> type.equals(targetString));
    }

    public boolean isDoorType(String typeName) {
        String targetString = typeName.substring(0, 3);
        Set<String> types = MerossBindingConstants.DISCOVERABLE_DOOR_HARDWARE_TYPES;
        return types.stream().anyMatch(type -> type.equals(targetString));
    }
}

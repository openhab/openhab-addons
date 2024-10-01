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
package org.openhab.binding.melcloud.internal.discovery;

import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.MelCloudBindingConstants;
import org.openhab.binding.melcloud.internal.api.dto.Device;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudLoginException;
import org.openhab.binding.melcloud.internal.handler.MelCloudAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MelCloudDiscoveryService} creates things based on the configured location.
 *
 * @author Luca Calcaterra - Initial Contribution
 * @author Pauli Anttila - Refactoring
 * @author Wietse van Buitenen - Check device type, added heatpump device
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MelCloudDiscoveryService.class)
public class MelCloudDiscoveryService extends AbstractThingHandlerDiscoveryService<MelCloudAccountHandler> {
    private final Logger logger = LoggerFactory.getLogger(MelCloudDiscoveryService.class);

    private static final String PROPERTY_DEVICE_ID = "deviceID";
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;

    private @Nullable ScheduledFuture<?> scanTask;

    /**
     * Creates a MelCloudDiscoveryService with enabled autostart.
     */
    public MelCloudDiscoveryService() {
        super(MelCloudAccountHandler.class, MelCloudBindingConstants.DISCOVERABLE_THING_TYPE_UIDS,
                DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
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

    private void discoverDevices() {
        logger.debug("Discover devices");
        try {
            List<Device> deviceList = thingHandler.getDeviceList();

            if (deviceList.isEmpty()) {
                logger.debug("No devices found");
            } else {
                ThingUID bridgeUID = thingHandler.getThing().getUID();

                deviceList.forEach(device -> {
                    ThingTypeUID thingTypeUid = null;
                    if (device.getType() == 0) {
                        thingTypeUid = THING_TYPE_ACDEVICE;
                    } else if (device.getType() == 1) {
                        thingTypeUid = THING_TYPE_HEATPUMPDEVICE;
                    } else {
                        logger.debug("Unsupported device found: name {} : type: {}", device.getDeviceName(),
                                device.getType());
                        return;
                    }
                    ThingUID deviceThing = new ThingUID(thingTypeUid, thingHandler.getThing().getUID(),
                            device.getDeviceID().toString());

                    Map<String, Object> deviceProperties = new HashMap<>();
                    deviceProperties.put(PROPERTY_DEVICE_ID, device.getDeviceID().toString());
                    deviceProperties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
                    deviceProperties.put(Thing.PROPERTY_MAC_ADDRESS, device.getMacAddress());
                    deviceProperties.put("deviceName", device.getDeviceName());
                    deviceProperties.put("buildingID", device.getBuildingID().toString());

                    String label = createLabel(device);
                    logger.debug("Found device: {} : {}", label, deviceProperties);

                    thingDiscovered(
                            DiscoveryResultBuilder.create(deviceThing).withLabel(label).withProperties(deviceProperties)
                                    .withRepresentationProperty(PROPERTY_DEVICE_ID).withBridge(bridgeUID).build());
                });
            }
        } catch (MelCloudLoginException e) {
            logger.debug("Login error occurred during device list fetch, reason {}. ", e.getMessage(), e);
        } catch (MelCloudCommException e) {
            logger.debug("Error occurred during device list fetch, reason {}. ", e.getMessage(), e);
        }
    }

    private String createLabel(Device device) {
        StringBuilder sb = new StringBuilder();
        if (device.getType() == 0) {
            sb.append("A.C. Device - ");
        } else if (device.getType() == 1) {
            sb.append("Heatpump Device - ");
        }
        if (device.getBuildingName() instanceof String) {
            sb.append(device.getBuildingName()).append(" - ");
        }
        sb.append(device.getDeviceName());
        return sb.toString();
    }
}

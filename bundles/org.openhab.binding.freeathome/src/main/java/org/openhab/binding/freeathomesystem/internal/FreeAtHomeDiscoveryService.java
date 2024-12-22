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
package org.openhab.binding.freeathome.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathome.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeHttpCommunicationException;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeDiscoveryService} is responsible for performing discovery of things
 *
 * @author Andras Uhrin - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeDiscoveryService extends AbstractThingHandlerDiscoveryService<FreeAtHomeBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDiscoveryService.class);
    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob = null;

    private static final long BACKGROUND_DISCOVERY_DELAY = 1L;
    private boolean isScanTerminated;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ThingUID bridgeUID = thingHandler.getThing().getUID();

            List<String> deviceList;

            try {
                deviceList = thingHandler.getDeviceDeviceList();

                for (int i = 0; (i < deviceList.size()) && !isScanTerminated; i++) {
                    FreeAtHomeDeviceDescription device = thingHandler.getFreeatHomeDeviceDescription(deviceList.get(i));

                    ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.DEVICE_TYPE_UID, bridgeUID, device.deviceId);
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put("deviceId", device.deviceId);
                    properties.put("interface", device.interfaceType);

                    String deviceLabel = device.deviceLabel;

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withLabel(deviceLabel)
                            .withRepresentationProperty("deviceId").withBridge(bridgeUID).withProperties(properties)
                            .build();

                    thingDiscovered(discoveryResult);

                    logger.debug("Thing discovered - DeviceId: {} - Device label: {}", device.getDeviceId(),
                            device.getDeviceLabel());
                }

                stopScan();
            } catch (FreeAtHomeHttpCommunicationException e) {
                logger.debug("Communication error in device discovery with the bridge: {}",
                        thingHandler.getThing().getLabel());
            } catch (RuntimeException e) {
                logger.debug("Scanning interrupted");
            }
        }
    };

    public FreeAtHomeDiscoveryService(int timeout) {
        super(FreeAtHomeBridgeHandler.class, FreeAtHomeBindingConstants.SUPPORTED_THING_TYPES_UIDS, timeout, false);
    }

    public FreeAtHomeDiscoveryService() {
        this(90);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(FreeAtHomeBindingConstants.BRIDGE_TYPE_UID);
    }

    @Override
    protected void startScan() {
        if (backgroundDiscoveryJob == null) {
            this.removeOlderResults(Instant.now().toEpochMilli());

            isScanTerminated = false;
            backgroundDiscoveryJob = scheduler.schedule(runnable, BACKGROUND_DISCOVERY_DELAY, TimeUnit.SECONDS);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();

        isScanTerminated = true;

        ScheduledFuture<?> localDiscoveryJob = backgroundDiscoveryJob;

        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
        }

        backgroundDiscoveryJob = null;

        removeOlderResults(Instant.now().toEpochMilli());
    }

    @Override
    public void deactivate() {
        removeOlderResults(Instant.now().toEpochMilli());
    }
}

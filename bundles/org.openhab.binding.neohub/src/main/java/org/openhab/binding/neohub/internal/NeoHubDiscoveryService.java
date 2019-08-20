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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;
import org.openhab.binding.neohub.internal.NeoHubInfoResponse.DeviceInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for neo devices
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class NeoHubDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NeoHubDiscoveryService.class);

    private ScheduledFuture<?> discoveryScheduler;
    private NeoHubHandler hub;

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_NEOSTAT, THING_TYPE_NEOPLUG).collect(Collectors.toSet()));

    public NeoHubDiscoveryService(NeoHubHandler hub) {
        // note: background discovery is enabled in the super method
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIMEOUT);
        this.hub = hub;
    }

    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (hub.getThing().getStatus() == ThingStatus.ONLINE) {
            discoverDevices();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("start background discovery..");

        if (discoveryScheduler == null || discoveryScheduler.isCancelled()) {
            discoveryScheduler = scheduler.scheduleWithFixedDelay(this::startScan, 10, DISCOVERY_REFRESH_PERIOD,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("stop background discovery..");

        if (discoveryScheduler != null && !discoveryScheduler.isCancelled()) {
            discoveryScheduler.cancel(true);
        }
    }

    private void discoverDevices() {
        NeoHubInfoResponse infoResponse;
        if ((infoResponse = hub.fromNeoHubFetchPollingResponse()) != null) {
            List<DeviceInfo> devices;
            if ((devices = infoResponse.getDevices()) != null) {
                for (DeviceInfo device : devices) {
                    publishDevice(device);
                }
            }
        }
    }

    private void publishDevice(DeviceInfo deviceInfo) {
        String deviceType;
        String deviceOpenHabId;
        String deviceNeohubName;
        ThingUID bridgeUID;
        ThingUID deviceUID;
        ThingTypeUID deviceTypeUID;
        DiscoveryResult device;

        bridgeUID = hub.getThing().getUID();

        if (deviceInfo.getDeviceType().intValue() == 6) {
            deviceType = DEVICE_ID_NEOPLUG;
            deviceTypeUID = THING_TYPE_NEOPLUG;
        } else {
            deviceType = DEVICE_ID_NEOSTAT;
            deviceTypeUID = THING_TYPE_NEOSTAT;
        }

        deviceNeohubName = deviceInfo.getDeviceName();
        deviceOpenHabId = deviceNeohubName.replaceAll("\\s+", "_");
        deviceUID = new ThingUID(deviceTypeUID, bridgeUID, deviceOpenHabId);

        device = DiscoveryResultBuilder.create(deviceUID).withBridge(bridgeUID).withLabel(deviceOpenHabId)
                .withProperty(DEVICE_NAME, deviceNeohubName).withRepresentationProperty(DEVICE_NAME).build();

        thingDiscovered(device);

        logger.debug("discovered device={}, name={} ..", deviceType, deviceOpenHabId);
    }

}

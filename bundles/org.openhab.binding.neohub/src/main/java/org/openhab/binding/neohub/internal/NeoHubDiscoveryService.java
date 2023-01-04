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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neohub.internal.NeoHubAbstractDeviceData.AbstractRecord;
import org.openhab.binding.neohub.internal.NeoHubInfoResponse.InfoRecord;
import org.openhab.binding.neohub.internal.NeoHubLiveDeviceData.LiveDataRecord;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for neo devices
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NeoHubDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NeoHubDiscoveryService.class);

    private @Nullable ScheduledFuture<?> discoveryScheduler;

    private NeoHubHandler hub;

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_NEOSTAT, THING_TYPE_NEOPLUG, THING_TYPE_NEOCONTACT, THING_TYPE_NEOTEMPERATURESENSOR)
                    .collect(Collectors.toSet()));

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

        ScheduledFuture<?> discoveryScheduler = this.discoveryScheduler;
        if (discoveryScheduler == null || discoveryScheduler.isCancelled()) {
            this.discoveryScheduler = scheduler.scheduleWithFixedDelay(this::startScan, 10, DISCOVERY_REFRESH_PERIOD,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("stop background discovery..");

        ScheduledFuture<?> discoveryScheduler = this.discoveryScheduler;
        if (discoveryScheduler != null && !discoveryScheduler.isCancelled()) {
            discoveryScheduler.cancel(true);
        }
    }

    private void discoverDevices() {
        NeoHubAbstractDeviceData deviceData = hub.fromNeoHubGetDeviceData();
        NeoHubGetEngineersData engineerData = hub.isLegacyApiSelected() ? null : hub.fromNeoHubGetEngineersData();

        if (deviceData != null) {
            List<? extends AbstractRecord> deviceRecords = deviceData.getDevices();

            if (deviceRecords != null) {
                int deviceType;

                for (AbstractRecord deviceRecord : deviceRecords) {

                    // the record came from the legacy API (deviceType included)
                    if (deviceRecord instanceof InfoRecord) {
                        deviceType = ((InfoRecord) deviceRecord).getDeviceType();
                        publishDevice(deviceRecord, deviceType);
                        continue;
                    }

                    // the record came from the new API (deviceType NOT included)
                    if (deviceRecord instanceof LiveDataRecord) {
                        if (engineerData == null) {
                            break;
                        }
                        String deviceName = ((LiveDataRecord) deviceRecord).getDeviceName();
                        // exclude repeater nodes from being discovered
                        if (MATCHER_HEATMISER_REPEATER.matcher(deviceName).matches()) {
                            continue;
                        }
                        deviceType = engineerData.getDeviceType(deviceName);
                        publishDevice(deviceRecord, deviceType);
                    }
                }
            }
        }
    }

    private void publishDevice(AbstractRecord device, int deviceId) {
        if (deviceId <= 0) {
            return;
        }

        String deviceType;
        String deviceOpenHabId;
        String deviceNeohubName;
        ThingUID deviceUID;
        ThingTypeUID deviceTypeUID;
        DiscoveryResult discoveredDevice;

        ThingUID bridgeUID = hub.getThing().getUID();

        switch (deviceId) {
            case HEATMISER_DEVICE_TYPE_CONTACT: {
                deviceType = DEVICE_ID_NEOCONTACT;
                deviceTypeUID = THING_TYPE_NEOCONTACT;
                break;
            }
            case HEATMISER_DEVICE_TYPE_PLUG: {
                deviceType = DEVICE_ID_NEOPLUG;
                deviceTypeUID = THING_TYPE_NEOPLUG;
                break;
            }
            case HEATMISER_DEVICE_TYPE_TEMPERATURE_SENSOR: {
                deviceType = DEVICE_ID_NEOTEMPERATURESENSOR;
                deviceTypeUID = THING_TYPE_NEOTEMPERATURESENSOR;
                break;
            }
            // all other device types are assumed to be thermostats
            default: {
                deviceType = DEVICE_ID_NEOSTAT;
                deviceTypeUID = THING_TYPE_NEOSTAT;
            }
        }

        deviceNeohubName = device.getDeviceName();
        deviceOpenHabId = deviceNeohubName.replaceAll("\\s+", "_");
        deviceUID = new ThingUID(deviceTypeUID, bridgeUID, deviceOpenHabId);

        discoveredDevice = DiscoveryResultBuilder.create(deviceUID).withBridge(bridgeUID).withLabel(deviceOpenHabId)
                .withProperty(DEVICE_NAME, deviceNeohubName).withRepresentationProperty(DEVICE_NAME).build();

        thingDiscovered(discoveredDevice);

        logger.debug("discovered device: id={}, type={}, name={} ..", deviceId, deviceType, deviceOpenHabId);
    }
}

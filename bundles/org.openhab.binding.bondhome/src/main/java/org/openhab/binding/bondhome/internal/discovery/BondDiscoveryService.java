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
package org.openhab.binding.bondhome.internal.discovery;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.api.BondDevice;
import org.openhab.binding.bondhome.internal.api.BondHttpApi;
import org.openhab.binding.bondhome.internal.handler.BondBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class does discovery of discoverable things
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class BondDiscoveryService extends AbstractDiscoveryService {
    private static final long REFRESH_INTERVAL_MINUTES = 60;
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = SUPPORTED_DEVICE_TYPES;
    private final Logger logger = LoggerFactory.getLogger(BondDiscoveryService.class);
    private @Nullable ScheduledFuture<?> discoveryJob;
    private final BondBridgeHandler bridgeHandler;
    private BondHttpApi api;

    public BondDiscoveryService(final BondBridgeHandler bridgeHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 10);
        this.bridgeHandler = bridgeHandler;
        this.api = bridgeHandler.getBridgeAPI();
        this.discoveryJob = null;
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected synchronized void startScan() {
        logger.debug("Start scan for Bond devices.");
        try {
            final ThingUID bridgeUid = bridgeHandler.getThing().getUID();
            api = bridgeHandler.getBridgeAPI();
            List<String> deviceList = api.getDevices();
            if (deviceList != null) {
                for (final String deviceId : deviceList) {
                    BondDevice thisDevice = api.getDevice(deviceId);
                    if (thisDevice != null) {
                        final ThingUID deviceUid = new ThingUID(thisDevice.type.getThingTypeUID(), bridgeUid, deviceId);
                        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(deviceUid)
                                .withBridge(bridgeUid).withLabel(thisDevice.name)
                                .withProperty(CONFIG_DEVICE_ID, deviceId)
                                .withProperty(PROPERTIES_DEVICE_NAME, thisDevice.name)
                                .withRepresentationProperty(CONFIG_DEVICE_ID).build();
                        thingDiscovered(discoveryResult);
                    }
                }
            }
        } catch (IOException ignored) {
            logger.warn("Error getting devices for discovery: {}", ignored.getMessage());
        } finally {
            removeOlderResults(getTimestampOfLastScan());
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }
}

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
package org.openhab.binding.bondhome.internal.discovery;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.BondException;
import org.openhab.binding.bondhome.internal.api.BondDevice;
import org.openhab.binding.bondhome.internal.api.BondHttpApi;
import org.openhab.binding.bondhome.internal.handler.BondBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class does discovery of discoverable things
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final long REFRESH_INTERVAL_MINUTES = 60;
    private final Logger logger = LoggerFactory.getLogger(BondDiscoveryService.class);
    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable BondBridgeHandler bridgeHandler;
    private @Nullable BondHttpApi api;

    public BondDiscoveryService() {
        super(SUPPORTED_THING_TYPES, 10);
        this.discoveryJob = null;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof BondBridgeHandler localHandler) {
            bridgeHandler = localHandler;
            localHandler.setDiscoveryService(this);
            api = localHandler.getBridgeAPI();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoverNow();
    }

    public synchronized void discoverNow() {
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
        }
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
                    String deviceName;
                    if (thisDevice.type != null && (deviceName = thisDevice.name) != null) {
                        final ThingUID deviceUid = new ThingUID(thisDevice.type.getThingTypeUID(), bridgeUid, deviceId);
                        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(deviceUid)
                                .withBridge(bridgeUid).withLabel(thisDevice.name)
                                .withProperty(CONFIG_DEVICE_ID, deviceId)
                                .withProperty(PROPERTIES_DEVICE_NAME, deviceName)
                                .withRepresentationProperty(CONFIG_DEVICE_ID).build();
                        thingDiscovered(discoveryResult);
                    }
                }
            }
        } catch (BondException ignored) {
            logger.warn("Error getting devices for discovery: {}", ignored.getMessage());
        } finally {
            removeOlderResults(getTimestampOfLastScan());
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }
}

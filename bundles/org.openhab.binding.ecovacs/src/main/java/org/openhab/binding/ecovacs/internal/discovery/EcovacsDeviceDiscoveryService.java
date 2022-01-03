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
package org.openhab.binding.ecovacs.internal.discovery;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.handler.EcovacsApiHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcovacsDeviceDiscoveryService} is used for discovering devices registered in the cloud account.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.ecovacs")
public class EcovacsDeviceDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(EcovacsDeviceDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;

    private @NonNullByDefault({}) EcovacsApiHandler apiHandler;
    private @Nullable Future<?> onDemandScanFuture;
    private @Nullable Future<?> backgroundScanFuture;

    public EcovacsDeviceDiscoveryService() {
        super(Collections.singleton(THING_TYPE_VACUUM), DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof EcovacsApiHandler) {
            this.apiHandler = (EcovacsApiHandler) handler;
            this.apiHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return apiHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        backgroundScanFuture = scheduler.scheduleWithFixedDelay(this::scanForDevices, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        Future<?> backgroundScanFuture = this.backgroundScanFuture;
        if (backgroundScanFuture != null) {
            backgroundScanFuture.cancel(true);
        }
        this.backgroundScanFuture = null;
    }

    @Override
    public synchronized void startScan() {
        Future<?> onDemandScanFuture = this.onDemandScanFuture;
        if (onDemandScanFuture != null && !onDemandScanFuture.isDone()) {
            logger.debug("Ecovacs device discovery scan already in progress");
            return;
        }

        logger.debug("Starting Ecovacs discovery scan");
        this.onDemandScanFuture = scheduler.submit(this::scanForDevices);
    }

    @Override
    public synchronized void stopScan() {
        logger.debug("Stopping Ecovacs discovery scan");
        Future<?> onDemandScanFuture = this.onDemandScanFuture;
        if (onDemandScanFuture != null) {
            onDemandScanFuture.cancel(true);
        }
        this.onDemandScanFuture = null;
        super.stopScan();
    }

    private void scanForDevices() {
        EcovacsApi api = apiHandler != null ? apiHandler.getApi() : null;
        long timestampOfLastScan = getTimestampOfLastScan();
        if (api == null) {
            return;
        }

        try {
            List<EcovacsDevice> devices = api.getDevices();
            logger.debug("Ecovacs discovery found {} devices", devices.size());

            for (EcovacsDevice device : devices) {
                deviceDiscovered(device);
            }
            for (Thing thing : apiHandler.getThing().getThings()) {
                String serial = thing.getUID().getId();
                if (!devices.stream().anyMatch(d -> serial.equals(d.getSerialNumber()))) {
                    thingRemoved(thing.getUID());
                }
            }
        } catch (EcovacsApiException e) {
            logger.debug("Could not retrieve devices from Ecovacs API", e);
        } finally {
            removeOlderResults(timestampOfLastScan);
        }
    }

    private void deviceDiscovered(EcovacsDevice device) {
        ThingUID thingUID = new ThingUID(THING_TYPE_VACUUM, apiHandler.getThing().getUID(), device.getSerialNumber());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withBridge(apiHandler.getThing().getUID()).withLabel(device.getModelName())
                .withProperty(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber())
                .withProperty(Thing.PROPERTY_MODEL_ID, device.getModelName())
                .withRepresentationProperty(Thing.PROPERTY_MODEL_ID).build();
        thingDiscovered(discoveryResult);
    }
}

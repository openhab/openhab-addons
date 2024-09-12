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
package org.openhab.binding.gardena.internal.discovery;

import static org.openhab.binding.gardena.internal.GardenaBindingConstants.BINDING_ID;

import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.handler.GardenaAccountHandler;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.binding.gardena.internal.util.PropertyUtils;
import org.openhab.binding.gardena.internal.util.UidUtils;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaDeviceDiscoveryService} is used to discover devices that are connected to Gardena smart system.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = GardenaDeviceDiscoveryService.class)
@NonNullByDefault
public class GardenaDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<GardenaAccountHandler> {

    private final Logger logger = LoggerFactory.getLogger(GardenaDeviceDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private @Nullable Future<?> scanFuture;

    public GardenaDeviceDiscoveryService() {
        super(GardenaAccountHandler.class,
                Collections.unmodifiableSet(Stream.of(new ThingTypeUID(BINDING_ID, "-")).collect(Collectors.toSet())),
                DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Gardena discovery scan");
        loadDevices();
    }

    @Override
    public void stopScan() {
        logger.debug("Stopping Gardena discovery scan");
        final Future<?> scanFuture = this.scanFuture;
        if (scanFuture != null) {
            scanFuture.cancel(true);
        }
        super.stopScan();
    }

    /**
     * Starts a thread which loads all Gardena devices registered in the account.
     */
    private void loadDevices() {
        if (scanFuture == null) {
            scanFuture = scheduler.submit(() -> {
                GardenaSmart gardena = thingHandler.getGardenaSmart();
                if (gardena != null) {
                    for (Device device : gardena.getAllDevices()) {
                        deviceDiscovered(device);
                    }

                    for (Thing thing : thingHandler.getThing().getThings()) {
                        try {
                            gardena.getDevice(UidUtils.getGardenaDeviceId(thing));
                        } catch (GardenaException ex) {
                            thingRemoved(thing.getUID());
                        }
                    }

                    logger.debug("Finished Gardena device discovery scan on gateway '{}'", gardena.getId());
                    scanFuture = null;
                    removeOlderResults(getTimestampOfLastScan());
                }
            });
        } else {
            logger.debug("Gardena device discovery scan in progress");
        }
    }

    /**
     * Waits for the discovery scan to finish and then returns.
     */
    public void waitForScanFinishing() {
        final Future<?> scanFuture = this.scanFuture;
        if (scanFuture != null) {
            logger.debug("Waiting for finishing Gardena device discovery scan");
            try {
                scanFuture.get();
                logger.debug("Gardena device discovery scan finished");
            } catch (CancellationException ex) {
                // ignore
            } catch (Exception ex) {
                logger.error("Error waiting for device discovery scan: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Generates the DiscoveryResult from a Gardena device.
     */
    public void deviceDiscovered(Device device) {
        if (device.active) {
            ThingUID accountUID = thingHandler.getThing().getUID();
            ThingUID thingUID = UidUtils.generateThingUID(device, thingHandler.getThing());

            try {
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(accountUID)
                        .withLabel(PropertyUtils.getPropertyValue(device, "common.attributes.name.value", String.class))
                        .withProperty("id", device.id).withProperty("type", device.deviceType)
                        .withRepresentationProperty("id").build();
                thingDiscovered(discoveryResult);
            } catch (GardenaException ex) {
                logger.warn("{}", ex.getMessage());
            }
        }
    }
}

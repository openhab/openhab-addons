/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.discovery;

import static org.openhab.binding.gardena.GardenaBindingConstants.BINDING_ID;

import java.util.concurrent.Future;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.gardena.handler.GardenaBridgeHandler;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.model.Location;
import org.openhab.binding.gardena.util.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link GardenaDeviceDiscoveryService} is used to discover devices that are connected to Gardena Smart Home.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaDeviceDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(GardenaDeviceDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    private GardenaBridgeHandler bridgeHandler;
    private Future<?> scanFuture;

    public GardenaDeviceDiscoveryService(GardenaBridgeHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startScan() {
        logger.debug("Starting Gardena discovery scan");
        loadDevices();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopScan() {
        logger.debug("Stopping Gardena discovery scan");
        if (scanFuture != null) {
            scanFuture.cancel(true);
        }
        super.stopScan();
    }

    /**
     * Starts a thread which loads all Homematic devices connected to the gateway.
     */
    public void loadDevices() {
        if (scanFuture == null) {
            scanFuture = scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        GardenaSmart gardena = bridgeHandler.getGardenaSmart();
                        gardena.loadAllDevices();
                        for (Location location : gardena.getLocations()) {
                            for (String deviceId : location.getDeviceIds()) {
                                deviceDiscovered(gardena.getDevice(deviceId));
                            }
                        }

                        for (Thing thing : bridgeHandler.getThing().getThings()) {
                            try {
                                gardena.getDevice(UidUtils.getGardenaDeviceId(thing));
                            } catch (GardenaException ex) {
                                thingRemoved(thing.getUID());
                            }
                        }

                        logger.debug("Finished Gardena device discovery scan on gateway '{}'",
                                bridgeHandler.getGardenaSmart().getId());
                    } catch (Throwable ex) {
                        logger.error(ex.getMessage(), ex);
                    } finally {
                        scanFuture = null;
                        removeOlderResults(getTimestampOfLastScan());
                    }
                }
            });
        } else {
            logger.debug("Gardena devices discovery scan in progress");
        }
    }

    /**
     * Generates the DiscoveryResult from a Gardena device.
     */
    public void deviceDiscovered(Device device) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = UidUtils.generateThingUID(device, bridgeHandler.getThing());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withLabel(device.getName()).build();
        thingDiscovered(discoveryResult);
    }

    /**
     * Removes the Gardena device.
     */
    public void deviceRemoved(Device device) {
        ThingUID thingUID = UidUtils.generateThingUID(device, bridgeHandler.getThing());
        thingRemoved(thingUID);
    }

}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homepilot.discovery;

import static org.openhab.binding.homepilot.HomePilotBindingConstants.*;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homepilot.handler.HomePilotBridgeHandler;
import org.openhab.binding.homepilot.internal.HomePilotDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link HomePilotDeviceDiscoveryService} is used to discover devices that are connected to a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomePilotDeviceDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(HomePilotDeviceDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 300;

    private HomePilotBridgeHandler bridgeHandler;
    private Future<?> scanFuture;
    private DiscoveryServiceCallback discoveryServiceCallback;

    public HomePilotDeviceDiscoveryService(HomePilotBridgeHandler bridgeHandler) {
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
        logger.info("Starting HomePilot discovery scan");
        loadDevices();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stopScan() {
        logger.info("Stopping HomePilot discovery scan");
        bridgeHandler.getGateway().cancelLoadAllDevices();
        waitForScanFinishing();
        super.stopScan();
    }

    /**
     * Waits for the discovery scan to finish and then returns.
     */
    public void waitForScanFinishing() {
        if (scanFuture != null) {
            logger.info("Waiting for finishing HomePilot device discovery scan");
            try {
                scanFuture.get();
                logger.debug("HomePilot device discovery scan finished");
            } catch (CancellationException ex) {
                // ignore
            } catch (Exception ex) {
                logger.error("Error waiting for device discovery scan: {}", ex.getMessage(), ex);
            }
        }
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
                        List<HomePilotDevice> allKnownDevices = bridgeHandler.getGateway().loadAllDevices();
                        // discover all things
                        for (HomePilotDevice device : allKnownDevices) {
                            ThingUID uid = createUID(device);
                            deviceDiscovered(uid, device);
                        }

                        logger.debug("Finished HomePilot device discovery scan on gateway '{}'",
                                bridgeHandler.getGateway().getId());
                    } catch (Throwable ex) {
                        logger.error(ex.getMessage(), ex);
                    } finally {
                        scanFuture = null;
                        bridgeHandler.setOfflineStatus();
                        removeOlderResults(getTimestampOfLastScan());
                    }
                }
            });
        } else {
            logger.debug("HomePilot devices discovery scan in progress");
        }
    }

    private void deviceRemoved(ThingUID thingUID) {
        thingRemoved(thingUID);
    }

    private void deviceDiscovered(ThingUID thingUID, HomePilotDevice device) {
        // device.setU
        String label = device.getName() != null ? device.getName() : "unknown";

        if (discoveryServiceCallback.getExistingDiscoveryResult(thingUID) == null
                && discoveryServiceCallback.getExistingThing(thingUID) == null) {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withBridge(bridgeHandler.getThing().getUID()).withLabel(label)
                    .withProperty("description", device.getDescription())
                    .withProperty(CHANNEL_POSITION, device.getPosition()).build();
            thingDiscovered(discoveryResult);
        }
    }

    private ThingUID createUID(HomePilotDevice device) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(device.getTypeUID(), bridgeUID, device.getDeviceId());
        return thingUID;
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }
}

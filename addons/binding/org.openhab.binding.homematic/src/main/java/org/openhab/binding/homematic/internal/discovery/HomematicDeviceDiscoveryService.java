/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.discovery;

import static org.openhab.binding.homematic.HomematicBindingConstants.BINDING_ID;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homematic.handler.HomematicBridgeHandler;
import org.openhab.binding.homematic.internal.communicator.HomematicGateway;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link HomematicDeviceDiscoveryService} is used to discover devices that are connected to a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicDeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(HomematicDeviceDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 300;

    private HomematicBridgeHandler bridgeHandler;
    private Future<?> scanFuture;

    public HomematicDeviceDiscoveryService(HomematicBridgeHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Homematic discovery scan");
        loadDevices();
    }

    @Override
    public synchronized void stopScan() {
        logger.debug("Stopping Homematic discovery scan");
        HomematicGateway gateway = bridgeHandler.getGateway();
        if (gateway != null) {
            gateway.cancelLoadAllDeviceMetadata();
        }
        waitForScanFinishing();
        super.stopScan();
    }

    /**
     * Waits for the discovery scan to finish and then returns.
     */
    public void waitForScanFinishing() {
        if (scanFuture != null) {
            logger.debug("Waiting for finishing Homematic device discovery scan");
            try {
                scanFuture.get();
                logger.debug("Homematic device discovery scan finished");
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
                        final HomematicGateway gateway = bridgeHandler.getGateway();
                        gateway.loadAllDeviceMetadata();
                        bridgeHandler.getTypeGenerator().validateFirmwares();
                        logger.debug("Finished Homematic device discovery scan on gateway '{}'", gateway.getId());
                    } catch (Throwable ex) {
                        logger.error("{}", ex.getMessage(), ex);
                    } finally {
                        scanFuture = null;
                        bridgeHandler.setOfflineStatus();
                        removeOlderResults(getTimestampOfLastScan());
                    }
                }
            });
        } else {
            logger.debug("Homematic devices discovery scan in progress");
        }
    }

    /**
     * Removes the Homematic device.
     */
    public void deviceRemoved(HmDevice device) {
        ThingUID thingUID = UidUtils.generateThingUID(device, bridgeHandler.getThing());
        thingRemoved(thingUID);
    }

    /**
     * Generates the DiscoveryResult from a Homematic device.
     */
    public void deviceDiscovered(HmDevice device) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID typeUid = UidUtils.generateThingTypeUID(device);
        ThingUID thingUID = new ThingUID(typeUid, bridgeUID, device.getAddress());
        String label = device.getName() != null ? device.getName() : device.getAddress();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .build();
        thingDiscovered(discoveryResult);
    }

}

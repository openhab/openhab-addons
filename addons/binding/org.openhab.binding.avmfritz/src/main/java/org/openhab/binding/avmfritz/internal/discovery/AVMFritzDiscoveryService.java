/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.avmfritz.BindingConstants;
import org.openhab.binding.avmfritz.handler.BoxHandler;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaDiscoveryCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discover all AHA (AVM Home Automation) devices connected to a FRITZ!Box
 * device.
 *
 * @author Robert Bausdorf - Initial contribution
 *
 */
public class AVMFritzDiscoveryService extends AbstractDiscoveryService {

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(AVMFritzDiscoveryService.class);

    /**
     * Maximum time to search for devices.
     */
    private static final int SEARCH_TIME = 30;

    /**
     * Initial delay in s for scanning job.
     */
    private static final int INITIAL_DELAY = 5;

    /**
     * Scan interval in s for scanning job.
     */
    private static final int SCAN_INTERVAL = 180;
    /**
     * Handler of the bridge of which devices have to be discovered.
     */
    private BoxHandler bridgeHandler;
    /**
     * Schedule for scanning
     */
    private ScheduledFuture<?> scanningJob;

    public AVMFritzDiscoveryService(BoxHandler bridgeHandler) {
        super(BindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        logger.debug("initialize discovery service");
        this.bridgeHandler = bridgeHandler;
        if (bridgeHandler == null) {
            logger.warn("no bridge handler for scan given");
        }
        this.activate(null);
    }

    public void deactivate() {
        super.deactivate();
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
        if (bridgeHandler.getWebInterface() != null) {
            logger.debug("start manual scan on bridge {}", bridgeHandler.getThing().getUID());
            FritzAhaDiscoveryCallback callback = new FritzAhaDiscoveryCallback(bridgeHandler.getWebInterface(), this);
            bridgeHandler.getWebInterface().asyncGet(callback);
        }
    }

    /**
     * Stops a running scan.
     */
    @Override
    protected synchronized void stopScan() {
        logger.debug("stop manual scan on bridge {}", bridgeHandler.getThing().getUID());
        super.stopScan();
    }

    /**
     * Add one discovered AHA device to inbox.
     *
     * @param device Device model received from a FRITZ!Box
     */
    public void onDeviceAddedInternal(DeviceModel device) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID,
                device.getProductName().replaceAll("[^a-zA-Z0-9_]", "_"));

        if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID,
                    device.getIdentifier().replaceAll("[^a-zA-Z0-9_]", "_"));

            Map<String, Object> properties = new HashMap<>();
            properties.put(THING_AIN, device.getIdentifier());
            properties.put(PROPERTY_VENDOR, device.getManufacturer());
            properties.put(PROPERTY_MODEL_ID, device.getDeviceId());
            properties.put(PROPERTY_SERIAL_NUMBER, device.getIdentifier());
            properties.put(PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(THING_AIN).withBridge(bridgeUID).withLabel(device.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported device: {}", device);
        }
    }

    /**
     * Starts background scanning for attached devices.
     */
    @Override
    protected void startBackgroundDiscovery() {
        if (scanningJob == null || scanningJob.isCancelled()) {
            logger.debug("start background scanning job at intervall {}s", SCAN_INTERVAL);
            scanningJob = AbstractDiscoveryService.scheduler.scheduleWithFixedDelay(() -> {
                if (bridgeHandler.getWebInterface() != null) {
                    logger.debug("start background scan on bridge {}", bridgeHandler.getThing().getUID());
                    FritzAhaDiscoveryCallback callback = new FritzAhaDiscoveryCallback(bridgeHandler.getWebInterface(),
                            this);
                    bridgeHandler.getWebInterface().asyncGet(callback);
                }
            }, INITIAL_DELAY, SCAN_INTERVAL, TimeUnit.SECONDS);
        } else {
            logger.debug("scanningJob active");
        }
    }

    /**
     * Stops background scanning for attached devices.
     */
    @Override
    protected void stopBackgroundDiscovery() {
        if (scanningJob != null && !scanningJob.isCancelled()) {
            logger.debug("stop background scanning job");
            scanningJob.cancel(true);
            scanningJob = null;
        }
    }
}

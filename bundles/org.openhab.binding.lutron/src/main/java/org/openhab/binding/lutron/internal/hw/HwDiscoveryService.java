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
package org.openhab.binding.lutron.internal.hw;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.lutron.internal.LutronHandlerFactory;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The Discovery Service for Lutron HomeWorks processors. There is no great way to automatically
 * discover modules in the legacy HomeWorks processor (that I know of) so this service simply iterates
 * through possible addresses and asks for status on that address. If it's a valid module, the processor will return
 * with the dimmer status and it will be discovered.
 *
 * @author Andrew Shilliday - Initial contribution
 */
public class HwDiscoveryService extends AbstractThingHandlerDiscoveryService<@NonNull HwSerialBridgeHandler> {
    private Logger logger = LoggerFactory.getLogger(HwDiscoveryService.class);

    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    public HwDiscoveryService() {
        super(HwSerialBridgeHandler.class, LutronHandlerFactory.HW_DISCOVERABLE_DEVICE_TYPES_UIDS, 10);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    protected void startScan() {
        scheduler.submit(() -> {
            if (isScanning.compareAndSet(false, true)) {
                try {
                    logger.debug("Starting scan for HW Dimmers");
                    for (int m = 1; m <= 8; m++) { // Modules
                        for (int o = 1; o <= 4; o++) { // Outputs
                            String address = String.format("[01:01:00:%02d:%02d]", m, o);
                            thingHandler.sendCommand("RDL, " + address);
                            Thread.sleep(5);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.debug("Scan interrupted");
                } finally {
                    isScanning.set(false);
                }
            }
        });
    }

    /**
     * Called by the bridge when it receives a status update for a dimmer that is not registered.
     */
    public void declareUnknownDimmer(String address) {
        if (address == null) {
            logger.info("Discovered HomeWorks dimmer with no address or thing handler");
            return;
        }
        String addressUid = address.replaceAll("[\\[\\]]", "").replace(":", "-");
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID uid = new ThingUID(HwConstants.THING_TYPE_HWDIMMER, bridgeUID, addressUid);

        Map<String, Object> props = new HashMap<>();

        props.put("address", address);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(props)
                .withRepresentationProperty("address").build();

        thingDiscovered(result);

        logger.debug("Discovered {}", uid);
    }
}

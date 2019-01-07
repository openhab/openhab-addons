/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.hw;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lutron.internal.LutronHandlerFactory;
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
 *
 */
public class HwDiscoveryService extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(HwDiscoveryService.class);

    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private final HwSerialBridgeHandler handler;

    public HwDiscoveryService(HwSerialBridgeHandler handler) {
        super(LutronHandlerFactory.HW_DISCOVERABLE_DEVICE_TYPES_UIDS, 10);
        this.handler = handler;
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
                            handler.sendCommand("RDL, " + address);
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
            logger.info("Discovered HomeWorks dimmer with no address");
            return;
        }
        String addressUid = address.replaceAll("[\\[\\]]", "").replaceAll(":", "-");
        ThingUID bridgeUID = this.handler.getThing().getUID();
        ThingUID uid = new ThingUID(HwConstants.THING_TYPE_HWDIMMER, bridgeUID, addressUid);

        Map<String, Object> props = new HashMap<>();

        props.put("address", address);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(props)
                .withRepresentationProperty("address").build();

        thingDiscovered(result);

        logger.debug("Discovered {}", uid);
    }
}

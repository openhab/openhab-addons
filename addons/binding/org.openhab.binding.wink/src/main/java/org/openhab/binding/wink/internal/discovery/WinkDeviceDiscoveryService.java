/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.internal.discovery;

import static org.openhab.binding.wink.WinkBindingConstants.BINDING_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkClient;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.openhab.binding.wink.handler.WinkHub2BridgeHandler;
import org.openhab.binding.wink.internal.WinkHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to discover new devices associated with a wink Hub
 *
 * @author Sebastian Marchand
 */
public class WinkDeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(WinkDeviceDiscoveryService.class);
    private WinkHub2BridgeHandler hubHandler;

    public WinkDeviceDiscoveryService(WinkHub2BridgeHandler hubHandler) throws IllegalArgumentException {
        super(WinkHandlerFactory.DISCOVERABLE_DEVICE_TYPES_UIDS, 10);

        this.hubHandler = hubHandler;
    }

    private ScheduledFuture<?> scanTask;

    @Override
    protected void startScan() {
        logger.debug("Starting Wink Discovery Scan");
        if (this.scanTask == null || this.scanTask.isDone()) {
            this.scanTask = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    List<IWinkDevice> devices = WinkClient.getInstance().listDevices();
                    logger.debug("Found {} connected devices", devices.size());
                    ThingUID bridgeThingId = hubHandler.getThing().getUID();
                    for (IWinkDevice device : devices) {
                        if (!WinkSupportedDevice.HUB.equals(device.getDeviceType())) {
                            logger.debug("Creating Discovery result {}", device);
                            ThingUID thingId = new ThingUID(
                                    new ThingTypeUID(BINDING_ID, device.getDeviceType().getDeviceType()),
                                    device.getId());
                            Map<String, Object> props = new HashMap<String, Object>();
                            props.put("uuid", device.getId());

                            DiscoveryResult result = DiscoveryResultBuilder.create(thingId).withLabel(device.getName())
                                    .withProperties(props).withBridge(bridgeThingId).build();
                            thingDiscovered(result);
                            logger.debug("Discovered Thing: {}", thingId);
                        }
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

}

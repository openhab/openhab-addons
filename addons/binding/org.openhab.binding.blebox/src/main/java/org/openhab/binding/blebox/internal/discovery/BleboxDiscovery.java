/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.internal.BleboxDeviceConfiguration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BleboxDiscovery} is responsible for find
 * and add Blebox devices to Smarthome Inbox
 *
 * @author Szymon Tokarski - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true)
public class BleboxDiscovery extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(BleboxDiscovery.class);
    private BleboxScanner scanner = new BleboxScanner(this);

    public BleboxDiscovery() {
        super(BleboxBindingConstants.SUPPORTED_THING_TYPES_UIDS, 30, true);
    }

    @Override
    protected void startScan() {
        logger.trace("Start Blebox devices discovery.");

        scheduler.execute(scannerRunnable);
    }

    private Runnable scannerRunnable = new Runnable() {
        @Override
        public void run() {
            scanner.discoverDevices();
        }
    };

    /**
     * Method to add an Blebox device to the Smarthome Inbox.
     */
    public void addDevice(String ip, String deviceType, String deviceId, String deviceName) {
        logger.trace("addDevice(): Adding new Blebox device on IP {} to Smarthome inbox", ip);

        Map<String, Object> properties = new HashMap<>();
        properties.put(BleboxDeviceConfiguration.IP, ip);
        properties.put(BleboxDeviceConfiguration.POLL_INTERVAL, BleboxDeviceConfiguration.DEFAULT_POLL_INTERVAL);

        try {
            ThingUID thingUID = new ThingUID(BleboxBindingConstants.BINDING_ID, deviceType, deviceId);

            if (thingUID != null) {
                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withLabel("Blebox - " + deviceName).build();
                thingDiscovered(result);

                logger.trace("addDevice(): '{}' was added to Smarthome inbox.", result.getThingUID());
            }
        } catch (Exception e) {
            logger.warn("addDevice(): Error: {}", e);
        }
    }
}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.discovery;

import static org.openhab.binding.roku.RokuBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.roku.RokuBindingConstants;
import org.openhab.binding.roku.handler.RokuDiscoveryProvider;
import org.openhab.binding.roku.internal.RokuState;
import org.openhab.binding.roku.internal.protocol.RokuCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuBindingDiscovery} class implements the abstract discovery service,
 * for automatically detecting roku devices on your network.
 *
 * @author Jarod Peters - Initial contribution
 */
public class RokuDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(RokuDiscoveryService.class);

    public RokuDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 60, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.info("Start Roku background discovery");
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                discover();
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    public void startScan() {
        logger.info("Start Roku scan");
        discover();
    }

    private synchronized void discover() {
        logger.debug("Try to discover a roku device");

        RokuDiscoveryProvider rokuDevice = new RokuDiscoveryProvider(RokuDiscoveryProvider.DEFAULT_MCAST_GRP,
                RokuDiscoveryProvider.DEFAULT_MCAST_PORT);
        try {
            rokuDevice.discover();
        } catch (IOException e) {
            logger.error("IOException occurred during discovery", e);
        }

        ArrayList<String> res = rokuDevice.getResults();
        for (int i = 0; i < res.size(); i++) {
            String[] obj = res.get(i).split(":");
            String ipAddress = obj[0];
            Number port = Integer.valueOf(obj[1]);
            Number refreshInterval = 30;
            RokuState state = new RokuState(new RokuCommunication(ipAddress, port));
            try {
                state.updateDeviceInformation();
            } catch (IOException e) {
                logger.debug("Thing discoverd '{}' but is not communicating", ipAddress, e);
            }
            Map<String, Object> properties = new HashMap<>();
            properties.put(RokuBindingConstants.IP_ADDRESS, ipAddress);
            properties.put(RokuBindingConstants.PORT, port);
            properties.put(RokuBindingConstants.REFRESH_INTERVAL, refreshInterval);
            properties.put(Thing.PROPERTY_VENDOR, state.getVendorName().toFullString());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, state.getSerialNumber().toFullString());
            ThingUID uid = new ThingUID(THING_TYPE_ROKU, state.getSerialNumber().toFullString());
            if (uid != null) {
                String label = "Roku Device";
                if (!state.getUserDeviceName().toFullString().equals("")) {
                    label = state.getUserDeviceName().toFullString();
                }
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                        .build();
                thingDiscovered(result);

                logger.debug("Thing discovered '{}'", result);
            }
        }
    }

}

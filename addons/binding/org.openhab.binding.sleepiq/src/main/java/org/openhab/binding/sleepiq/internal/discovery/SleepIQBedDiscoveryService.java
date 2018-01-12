/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sleepiq.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.sleepiq.SleepIQBindingConstants;
import org.openhab.binding.sleepiq.handler.SleepIQCloudHandler;
import org.openhab.binding.sleepiq.handler.SleepIQDualBedHandler;
import org.openhab.binding.sleepiq.internal.config.SleepIQBedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.sleepiq.api.model.Bed;

/**
 * The {@link SleepIQBedDiscoveryService} is responsible for processing the
 * results of devices found through the SleepIQ cloud service.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class SleepIQBedDiscoveryService extends AbstractDiscoveryService {
    private static final int TIMEOUT = 60;

    private final Logger logger = LoggerFactory.getLogger(SleepIQBedDiscoveryService.class);

    private final SleepIQCloudHandler cloudHandler;

    /**
     * Discovers beds in the SleepIQ cloud service account.
     *
     * @param cloudHandler the cloud service handler (bridge)
     */
    public SleepIQBedDiscoveryService(final SleepIQCloudHandler cloudHandler) {
        super(SleepIQDualBedHandler.SUPPORTED_THING_TYPE_UIDS, TIMEOUT, true);
        this.cloudHandler = cloudHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery for new beds");
        startScan();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scan for new beds");

        for (Bed bed : cloudHandler.getBeds()) {
            // only dual chamber beds are supported currently
            if (!bed.isDualSleep()) {
                logger.info("Found a bed that is not dual chamber - currently unsupported");
                continue;
            }

            ThingUID bridgeUID = cloudHandler.getThing().getUID();
            ThingUID thingUID = new ThingUID(SleepIQBindingConstants.THING_TYPE_DUAL_BED, bridgeUID,
                    bed.getMacAddress());

            // thing already exists
            if (cloudHandler.getThingByUID(thingUID) != null) {
                continue;
            }

            logger.debug("New bed found with MAC address {}", bed.getMacAddress());

            @SuppressWarnings({ "unchecked", "rawtypes" })
            Map<String, Object> properties = (Map) cloudHandler.updateProperties(bed, new HashMap<>());
            properties.put(SleepIQBedConfiguration.BED_ID, bed.getBedId());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(bed.getName() + " - " + bed.getModel()).build();
            thingDiscovered(discoveryResult);
        }

        stopScan();
    }
}
